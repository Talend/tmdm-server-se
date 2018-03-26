/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.record;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

import org.apache.commons.lang.StringEscapeUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.Types;

import com.amalto.core.query.user.DateConstant;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.query.user.TimeConstant;
import com.amalto.core.query.user.metadata.StagingBlockKey;
import com.amalto.core.query.user.metadata.TaskId;
import com.amalto.core.query.user.metadata.Timestamp;
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;
import com.amalto.core.storage.StagingStorage;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.record.metadata.DataRecordMetadata;

public class DataRecordIncludeNullValueXmlWriter extends DataRecordXmlWriter {

    public DataRecordIncludeNullValueXmlWriter() {
        super();
    }

    public DataRecordIncludeNullValueXmlWriter(boolean includeMetadata) {
        super(includeMetadata);
    }

    public DataRecordIncludeNullValueXmlWriter(String rootElementName) {
        super(rootElementName);
    }

    public DataRecordIncludeNullValueXmlWriter(ComplexTypeMetadata type) {
        super(type);
    }

    @Override
    public void write(DataRecord record, Writer writer) throws IOException {
        DefaultMetadataVisitor<Void> fieldPrinter = new FieldPrinter(record, writer);
        Collection<FieldMetadata> fields = type == null ? record.getType().getFields() : type.getFields();
        if (includeMetadata) {
            writer.write("<" + getRootElementName(record) + " xmlns:metadata=\"" + DataRecordReader.METADATA_NAMESPACE + "\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            writer.write("<" + getRootElementName(record) + ">"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // Includes metadata in serialized XML (if requested).
        if (includeMetadata) {
            DataRecordMetadata recordMetadata = record.getRecordMetadata();
            Map<String, String> properties = recordMetadata.getRecordProperties();
            writeMetadataField(writer, Timestamp.INSTANCE, recordMetadata.getLastModificationTime());
            writeMetadataField(writer, TaskId.INSTANCE, recordMetadata.getTaskId());
            writeMetadataField(writer, StagingBlockKey.INSTANCE, properties.get(StagingStorage.METADATA_STAGING_BLOCK_KEY));
        }
        // Print record fields
        if (!delegator.hide(record.getType())) {
            for (FieldMetadata field : fields) {
                field.accept(fieldPrinter);
            }
        }
        writer.write("</" + getRootElementName(record) + ">"); //$NON-NLS-1$ //$NON-NLS-2$
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
            if (delegator.hide(referenceField)) {
                return null;
            }
            try {
                Object value = record.get(referenceField);

                if (!referenceField.isMany()) {
                    DataRecord referencedRecord = (DataRecord) record.get(referenceField);
                    if (value != null) {
                        writeReferenceElement(referenceField, referencedRecord);
                    } else {
                        out.write("<" + referenceField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    out.write(StorageMetadataUtils.toString(referencedRecord));
                    out.write("</" + referenceField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    if (value != null && !((List<DataRecord>) value).isEmpty()) {
                        List<DataRecord> valueAsList = (List<DataRecord>) value;
                        for (DataRecord currentValue : valueAsList) {
                            if (currentValue != null) {
                                writeReferenceElement(referenceField, currentValue);
                                out.write(StorageMetadataUtils.toString(currentValue));
                                out.write("</" + referenceField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    } else {
                        out.write("<" + referenceField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                        out.write("</" + referenceField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }

                return null;
            } catch (IOException e) {
                throw new RuntimeException("Could not serialize XML for reference field '" + referenceField.getName()
                        + "' of type '" + referenceField.getContainingType().getName() + "'.", e);
            }
        }

        private void writeReferenceElement(ReferenceFieldMetadata referenceField, DataRecord currentValue) throws IOException {
            if (currentValue.getType().equals(referenceField.getReferencedType())) {
                out.write("<" + referenceField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                out.write("<" + referenceField.getName() + " xmlns:tmdm=\"" + SkipAttributeDocumentBuilder.TALEND_NAMESPACE //$NON-NLS-1$ //$NON-NLS-2$
                        + "\" tmdm:type=\"" + currentValue.getType().getName() + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        @Override
        public Void visit(ContainedTypeFieldMetadata containedField) {
            if (delegator.hide(containedField)) {
                return null;
            }
            try {
                if (!containedField.isMany()) {
                    DataRecord containedRecord = (DataRecord) record.get(containedField);
                    // TMDM-6232 Unable to save reusable type value
                    if (containedRecord != null) {
                        // TODO Limit new field printer instances
                        DefaultMetadataVisitor<Void> fieldPrinter = new FieldPrinter(containedRecord, out);
                        Collection<FieldMetadata> fields = containedRecord.getType().getFields();
                        writeContainedField(containedField, containedRecord);
                        for (FieldMetadata field : fields) {
                            field.accept(fieldPrinter);
                        }
                        out.write("</" + containedField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    } else {
                        out.write("<" + containedField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                        out.write("</" + containedField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } else {
                    List<DataRecord> recordList = (List<DataRecord>) record.get(containedField);
                    if (recordList != null) {
                        List<DataRecord> writeableDataRecord = new ArrayList<DataRecord>();
                        for (DataRecord dataRecord : recordList) {
                            if (dataRecord.isEmpty()) {
                                continue;
                            }
                            writeableDataRecord.add(dataRecord);
                        }

                        if (writeableDataRecord.isEmpty() && !recordList.isEmpty()) {
                            writeableDataRecord.add(recordList.get(0));
                        }

                        for (DataRecord dataRecord : writeableDataRecord) {
                            // TODO Limit new field printer instances
                            DefaultMetadataVisitor<Void> fieldPrinter = new FieldPrinter(dataRecord, out);
                            Collection<FieldMetadata> fields = dataRecord.getType().getFields();
                            writeContainedField(containedField, dataRecord);
                            for (FieldMetadata field : fields) {
                                field.accept(fieldPrinter);
                            }
                            out.write("</" + containedField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$

                        }
                    } else {
                        out.write("<" + containedField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                        out.write("</" + containedField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Could not serialize XML for contained field '" + containedField.getName()
                        + "' of type '" + containedField.getContainingType().getName() + "'.", e);
            }
        }

        private void writeContainedField(ContainedTypeFieldMetadata containedField, DataRecord currentValue) throws IOException {
            if (containedField.getContainedType().getSubTypes().size() == 0) {
                out.write("<" + containedField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                out.write("<" + containedField.getName() + " xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI //$NON-NLS-1$ //$NON-NLS-2$
                        + "\" xsi:type=\"" + currentValue.getType().getName() + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        @Override
        public Void visit(SimpleTypeFieldMetadata simpleField) {
            if (delegator.hide(simpleField)) {
                return null;
            }
            try {
                Object value = record.get(simpleField);

                if (!simpleField.isMany()) {
                    out.write("<" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    if (value != null) {
                        handleSimpleValue(simpleField, value);
                    }
                    out.write("</" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    if (value != null && !((List) value).isEmpty()) {
                        List valueAsList = (List) value;
                        for (Object currentValue : valueAsList) {
                            if (currentValue != null) {
                                out.write("<" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                                handleSimpleValue(simpleField, currentValue);
                                out.write("</" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                            } else {
                                out.write("<" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                                out.write("</" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    } else {
                        out.write("<" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                        out.write("</" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }

                return null;
            } catch (IOException e) {
                throw new RuntimeException("Could not serialize XML for simple field '" + simpleField.getName() + "' of type '"
                        + simpleField.getContainingType().getName() + "'.", e);
            }
        }

        @Override
        public Void visit(EnumerationFieldMetadata enumField) {
            if (delegator.hide(enumField)) {
                return null;
            }
            try {
                Object value = record.get(enumField);

                if (!enumField.isMany()) {
                    out.write("<" + enumField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    if (value != null) {
                        handleSimpleValue(enumField, value);
                    }
                    out.write("</" + enumField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    if (value != null) {
                        List valueAsList = (List) value;
                        for (Object currentValue : valueAsList) {
                            if (currentValue != null) {
                                out.write("<" + enumField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                                handleSimpleValue(enumField, currentValue);
                                out.write("</" + enumField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                            } else {
                                out.write("<" + enumField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                                out.write("</" + enumField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    } else {
                        out.write("<" + enumField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                        out.write("</" + enumField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Could not serialize XML for enumeration field '" + enumField.getName() + "' of type '"
                        + enumField.getContainingType().getName() + "'.", e);
            }
        }

        private void handleSimpleValue(FieldMetadata simpleField, Object value) throws IOException {
            if (value == null) {
                throw new IllegalArgumentException("Not supposed to write null values to XML.");
            }
            TypeMetadata type = MetadataUtils.getSuperConcreteType(simpleField.getType());
            if (!(value instanceof String)) {
                if (Types.DATE.equals(type.getName())) {
                    synchronized (DateConstant.DATE_FORMAT) {
                        out.write((DateConstant.DATE_FORMAT).format(value));
                    }
                } else if (Types.DATETIME.equals(type.getName())) {
                    synchronized (DateTimeConstant.DATE_FORMAT) {
                        out.write((DateTimeConstant.DATE_FORMAT).format(value));
                    }
                } else if (Types.TIME.equals(type.getName())) {
                    synchronized (TimeConstant.TIME_FORMAT) {
                        out.write((TimeConstant.TIME_FORMAT).format(value));
                    }
                } else {
                    out.write(StringEscapeUtils.escapeXml(value.toString()));
                }
            } else {
                out.write(StringEscapeUtils.escapeXml(value.toString()));
            }
        }
    }
}
