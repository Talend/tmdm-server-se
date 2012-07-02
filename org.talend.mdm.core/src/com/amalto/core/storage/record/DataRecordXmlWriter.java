/*
 * Copyright (C) 2006-2012 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.record;

import com.amalto.core.metadata.*;
import com.amalto.core.query.user.DateConstant;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.query.user.TimeConstant;
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;

import javax.xml.XMLConstants;
import java.io.*;
import java.util.List;

public class DataRecordXmlWriter implements DataRecordWriter {

    public void write(DataRecord record, OutputStream output) throws IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(output));
        write(record, out);
    }

    public void write(DataRecord record, Writer writer) throws IOException {
        DefaultMetadataVisitor<Void> fieldPrinter = new FieldPrinter(record, writer);
        List<FieldMetadata> fields = record.getType().getFields();
        writer.write("<" + record.getType().getName() + ">");
        for (FieldMetadata field : fields) {
            field.accept(fieldPrinter);
        }
        writer.write("</" + record.getType().getName() + ">");
        writer.flush();
    }

    private class FieldPrinter extends DefaultMetadataVisitor<Void> {

        private final DataRecord record;

        private final Writer out;

        public FieldPrinter(DataRecord record, Writer out) {
            this.record = record;
            this.out = out;
        }

        @Override
        public Void visit(ReferenceFieldMetadata referenceField) {
            try {
                Object value = record.get(referenceField);
                if (value != null) {
                    if (!referenceField.isMany()) {
                        DataRecord referencedRecord = (DataRecord) record.get(referenceField);
                        writeReferenceElement(referenceField, referencedRecord);
                        out.write(getFK(referencedRecord));
                        out.write("</" + referenceField.getName() + ">");
                    } else {
                        List<DataRecord> valueAsList = (List<DataRecord>) value;
                        for (DataRecord currentValue : valueAsList) {
                            writeReferenceElement(referenceField, currentValue);
                            out.write(getFK(currentValue));
                            out.write("</" + referenceField.getName() + ">");
                        }
                    }
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Could not serialize XML for reference field '" + referenceField.getName() + "' of type '" + referenceField.getContainingType().getName() + "'.", e);
            }
        }

        private void writeReferenceElement(ReferenceFieldMetadata referenceField, DataRecord currentValue) throws IOException {
            if (currentValue.getType().equals(referenceField.getReferencedType())) {
                out.write("<" + referenceField.getName() + ">");
            } else {
                out.write("<" + referenceField.getName() + " xmlns:tmdm=\"" + SkipAttributeDocumentBuilder.TALEND_NAMESPACE + "\" tmdm:type=\"" + currentValue.getType().getName() + "\">");
            }
        }

        @Override
        public Void visit(ContainedTypeFieldMetadata containedField) {
            try {
                if (!containedField.isMany()) {
                    DataRecord containedRecord = (DataRecord) record.get(containedField);
                    if (containedRecord != null) {
                        // TODO Limit new field printer instances
                        DefaultMetadataVisitor<Void> fieldPrinter = new FieldPrinter(containedRecord, out);
                        List<FieldMetadata> fields = containedRecord.getType().getFields();
                        writeContainedField(containedField, containedRecord);
                        for (FieldMetadata field : fields) {
                            field.accept(fieldPrinter);
                        }
                        out.write("</" + containedField.getName() + ">");
                    }
                } else {
                    List<DataRecord> recordList = (List<DataRecord>) record.get(containedField);
                    if (recordList != null) {
                        for (DataRecord dataRecord : recordList) {
                            // TODO Limit new field printer instances
                            DefaultMetadataVisitor<Void> fieldPrinter = new FieldPrinter(dataRecord, out);
                            List<FieldMetadata> fields = dataRecord.getType().getFields();
                            writeContainedField(containedField, dataRecord);
                            for (FieldMetadata field : fields) {
                                field.accept(fieldPrinter);
                            }
                            out.write("</" + containedField.getName() + ">");
                        }
                    }
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Could not serialize XML for contained field '" + containedField.getName() + "' of type '" + containedField.getContainingType().getName() + "'.", e);
            }
        }

        private void writeContainedField(ContainedTypeFieldMetadata containedField, DataRecord currentValue) throws IOException {
            if (currentValue.getType().equals(containedField.getContainedType())) {
                out.write("<" + containedField.getName() + ">");
            } else {
                out.write("<" + containedField.getName() + " xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI + "\" xsi:type=\"" + currentValue.getType().getName() + "\">");
            }
        }

        @Override
        public Void visit(SimpleTypeFieldMetadata simpleField) {
            try {
                Object value = record.get(simpleField);
                if (value != null) {
                    if (!simpleField.isMany()) {
                        out.write("<" + simpleField.getName() + ">");
                        handleSimpleValue(simpleField, value);
                        out.write("</" + simpleField.getName() + ">");
                    } else {
                        List valueAsList = (List) value;
                        for (Object currentValue : valueAsList) {
                            out.write("<" + simpleField.getName() + ">");
                            handleSimpleValue(simpleField, currentValue);
                            out.write("</" + simpleField.getName() + ">");
                        }
                    }
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Could not serialize XML for simple field '" + simpleField.getName() + "' of type '" + simpleField.getContainingType().getName() + "'.", e);
            }
        }

        @Override
        public Void visit(EnumerationFieldMetadata enumField) {
            try {
                Object value = record.get(enumField);
                if (value != null) {
                    if (!enumField.isMany()) {
                        out.write("<" + enumField.getName() + ">");
                        handleSimpleValue(enumField, value);
                        out.write("</" + enumField.getName() + ">");
                    } else {
                        List valueAsList = (List) value;
                        for (Object currentValue : valueAsList) {
                            out.write("<" + enumField.getName() + ">");
                            handleSimpleValue(enumField, currentValue);
                            out.write("</" + enumField.getName() + ">");
                        }
                    }
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Could not serialize XML for enumeration field '" + enumField.getName() + "' of type '" + enumField.getContainingType().getName() + "'.", e);
            }
        }

        private void handleSimpleValue(FieldMetadata simpleField, Object value) throws IOException {
            if (value == null) {
                throw new IllegalArgumentException("Not supposed to write null values to XML.");
            }
            if ("date".equals(simpleField.getType().getName())) {
                synchronized (DateConstant.DATE_FORMAT) {
                    out.write((DateConstant.DATE_FORMAT).format(value));
                }
            } else if ("dateTime".equals(simpleField.getType().getName())) {
                synchronized (DateTimeConstant.DATE_FORMAT) {
                    out.write((DateTimeConstant.DATE_FORMAT).format(value));
                }
            } else if ("time".equals(simpleField.getType().getName())) {
                synchronized (TimeConstant.TIME_FORMAT) {
                    out.write((TimeConstant.TIME_FORMAT).format(value));
                }
            } else if (value != null) {
                out.write(value.toString());
            }
        }

        private String getFK(DataRecord record) {
            StringBuilder builder = new StringBuilder();
            List<FieldMetadata> keyFields = record.getType().getKeyFields();
            for (FieldMetadata keyField : keyFields) {
                builder.append('[').append(record.get(keyField)).append(']');
            }
            return builder.toString();
        }
    }
}
