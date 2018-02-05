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
import java.util.Collection;
import java.util.List;

import javax.xml.XMLConstants;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;

import com.amalto.core.storage.StorageMetadataUtils;

@SuppressWarnings("nls")
public class DataRecordWithNullFieldsXmlWriter extends DataRecordXmlWriter {

    public DataRecordWithNullFieldsXmlWriter() {
        super();
    }

    public DataRecordWithNullFieldsXmlWriter(boolean includeMetadata) {
        super(includeMetadata);
    }

    public DataRecordWithNullFieldsXmlWriter(String rootElementName) {
        super(rootElementName);
    }

    public DataRecordWithNullFieldsXmlWriter(ComplexTypeMetadata type) {
        super(type);
    }

    @Override
    public void write(DataRecord record, Writer writer) throws IOException {
        DefaultMetadataVisitor<Void> fieldPrinter = new FieldWithNullValuePrinter(record, writer);
        write(record, writer, fieldPrinter);
    }

    class FieldWithNullValuePrinter extends DataRecordXmlWriter.FieldPrinter {

        public FieldWithNullValuePrinter(DataRecord record, Writer out) {
            super(record, out);
        }

        @Override
        public Void visit(ReferenceFieldMetadata referenceField) {
            if (delegator.hide(referenceField)) {
                return null;
            }
            try {
                Object value = record.get(referenceField);
                boolean containsFieldToValue = record.containsFieldToValue(referenceField);
                if (value != null) {
                    if (!referenceField.isMany()) {
                        DataRecord referencedRecord = (DataRecord) record.get(referenceField);
                        writeReferenceElement(referenceField, referencedRecord);
                        out.write(StorageMetadataUtils.toString(referencedRecord));
                        out.write("</" + referenceField.getName() + ">");
                    } else {
                        List<DataRecord> valueAsList = (List<DataRecord>) value;
                        for (DataRecord currentValue : valueAsList) {
                            if (currentValue != null) {
                                writeReferenceElement(referenceField, currentValue);
                                out.write(StorageMetadataUtils.toString(currentValue));
                                out.write("</" + referenceField.getName() + ">");
                            } else {
                                out.write("<" + referenceField.getName() + "/>");
                            }
                        }
                        if (valueAsList.isEmpty()) {
                            out.write("<" + referenceField.getName() + "/>");
                        }
                    }
                } else if (containsFieldToValue) {
                    out.write("<" + referenceField.getName() + "/>");
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Could not serialize XML for reference field '" + referenceField.getName()
                        + "' of type '" + referenceField.getContainingType().getName() + "'.", e);
            }
        }

        @Override
        public Void visit(ContainedTypeFieldMetadata containedField) {
            if (delegator.hide(containedField)) {
                return null;
            }
            try {
                if (!containedField.isMany()) {
                    boolean containsFieldToValue = record.containsFieldToValue(containedField);
                    DataRecord containedRecord = (DataRecord) record.get(containedField);
                    // TMDM-6232 Unable to save reusable type value
                    if (containedRecord != null) {
                        // TODO Limit new field printer instances
                        DefaultMetadataVisitor<Void> fieldPrinter = new FieldWithNullValuePrinter(containedRecord, out);
                        Collection<FieldMetadata> fields = containedRecord.getType().getFields();
                        writeContainedField(containedField, containedRecord);
                        for (FieldMetadata field : fields) {
                            field.accept(fieldPrinter);
                        }
                        out.write("</" + containedField.getName() + ">");
                    } else if (containsFieldToValue) {
                        out.write("<" + containedField.getName() + "/>");
                    }
                } else {
                    boolean containsFieldToValue = record.containsFieldToValue(containedField);
                    List<DataRecord> recordList = (List<DataRecord>) record.get(containedField);
                    if (recordList != null) {
                        for (DataRecord dataRecord : recordList) {
                            // TODO Limit new field printer instances
                            DefaultMetadataVisitor<Void> fieldPrinter = new FieldWithNullValuePrinter(dataRecord, out);
                            Collection<FieldMetadata> fields = dataRecord.getType().getFields();
                            writeContainedField(containedField, dataRecord);
                            for (FieldMetadata field : fields) {
                                field.accept(fieldPrinter);
                            }
                            out.write("</" + containedField.getName() + ">");
                        }
                        if (recordList.isEmpty()) {
                            out.write("<" + containedField.getName() + "/>");
                        }
                    } else if (containsFieldToValue) {
                        out.write("<" + containedField.getName() + "/>");
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
                out.write("<" + containedField.getName() + ">");
            } else {
                if (currentValue == null) {
                    out.write("<" + containedField.getName() + ">");
                } else {
                    out.write("<" + containedField.getName() + " xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI
                            + "\" xsi:type=\"" + currentValue.getType().getName() + "\">");
                }
            }
        }

        @Override
        public Void visit(SimpleTypeFieldMetadata simpleField) {
            if (delegator.hide(simpleField)) {
                return null;
            }
            try {
                boolean containsFieldToValue = record.containsFieldToValue(simpleField);
                Object value = record.get(simpleField);
                if (value != null) {
                    if (!simpleField.isMany()) {
                        out.write("<" + simpleField.getName() + ">");
                        handleSimpleValue(simpleField, value);
                        out.write("</" + simpleField.getName() + ">");
                    } else {
                        List valueAsList = (List) value;
                        for (Object currentValue : valueAsList) {
                            if (currentValue != null) {
                                out.write("<" + simpleField.getName() + ">");
                                handleSimpleValue(simpleField, currentValue);
                                out.write("</" + simpleField.getName() + ">");
                            } else {
                                out.write("<" + simpleField.getName() + "/>");
                            }
                        }
                        if (valueAsList.isEmpty()) {
                            out.write("<" + simpleField.getName() + "/>");
                        }
                    }
                } else if (containsFieldToValue) {
                    out.write("<" + simpleField.getName() + "/>");
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
                boolean containsFieldToValue = record.containsFieldToValue(enumField);
                Object value = record.get(enumField);
                if (value != null) {
                    if (!enumField.isMany()) {
                        out.write("<" + enumField.getName() + ">");
                        handleSimpleValue(enumField, value);
                        out.write("</" + enumField.getName() + ">");
                    } else {
                        List valueAsList = (List) value;
                        for (Object currentValue : valueAsList) {
                            if (currentValue != null) {
                                out.write("<" + enumField.getName() + ">");
                                handleSimpleValue(enumField, currentValue);
                                out.write("</" + enumField.getName() + ">");
                            } else {
                                out.write("<" + enumField.getName() + "/>");
                            }
                        }
                    }
                } else if (containsFieldToValue) {
                    out.write("<" + enumField.getName() + "/>");
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Could not serialize XML for enumeration field '" + enumField.getName() + "' of type '"
                        + enumField.getContainingType().getName() + "'.", e);
            }
        }
    }
}