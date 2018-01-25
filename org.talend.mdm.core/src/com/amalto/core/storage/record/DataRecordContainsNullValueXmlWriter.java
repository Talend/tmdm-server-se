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
import java.util.Map;

import javax.xml.XMLConstants;

import com.amalto.core.query.user.metadata.*;
import com.amalto.core.storage.StagingStorage;
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
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.record.metadata.DataRecordMetadata;

public class DataRecordContainsNullValueXmlWriter extends DataRecordXmlWriter {


    public DataRecordContainsNullValueXmlWriter() {
        super();
    }

    public DataRecordContainsNullValueXmlWriter(boolean includeMetadata) {
        super(includeMetadata);
    }

    public DataRecordContainsNullValueXmlWriter(String rootElementName) {
        super(rootElementName);
    }

    public DataRecordContainsNullValueXmlWriter(ComplexTypeMetadata type) {
        super(type);
    }


    @Override
    public void write(DataRecord record, Writer writer) throws IOException {
        DefaultMetadataVisitor<Void> fieldPrinter = new ContainsNullValueFieldPrinter(record, writer);
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

    class ContainsNullValueFieldPrinter extends DataRecordXmlWriter.FieldPrinter {

        public ContainsNullValueFieldPrinter(DataRecord record, Writer out) {
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
                        out.write("</" + referenceField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    } else {
                        List<DataRecord> valueAsList = (List<DataRecord>) value;
                        for (DataRecord currentValue : valueAsList) {
                            if (currentValue != null) {
                                writeReferenceElement(referenceField, currentValue);
                                out.write(StorageMetadataUtils.toString(currentValue));
                                out.write("</" + referenceField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                            } else {
                                out.write("<" + referenceField.getName() + "/>"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                        if(valueAsList.size() == 0) {
                            out.write("<" + referenceField.getName() + "/>"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                } else if (containsFieldToValue) {
                    out.write("<" + referenceField.getName() + "/>"); //$NON-NLS-1$ //$NON-NLS-2$
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
                    boolean containsFieldToValue = record.containsFieldToValue(containedField);
                    DataRecord containedRecord = (DataRecord) record.get(containedField);
                    // TMDM-6232 Unable to save reusable type value
                    if (containedRecord != null) {
                        // TODO Limit new field printer instances
                        DefaultMetadataVisitor<Void> fieldPrinter = new ContainsNullValueFieldPrinter(containedRecord, out);
                        Collection<FieldMetadata> fields = containedRecord.getType().getFields();
                        writeContainedField(containedField, containedRecord);
                        for (FieldMetadata field : fields) {
                            field.accept(fieldPrinter);
                        }
                        out.write("</" + containedField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (containsFieldToValue) {
                        out.write("<" + containedField.getName() + "/>"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } else {
                    boolean containsFieldToValue = record.containsFieldToValue(containedField);
                    List<DataRecord> recordList = (List<DataRecord>) record.get(containedField);
                    if (recordList != null) {
                        for (DataRecord dataRecord : recordList) {
                            // TODO Limit new field printer instances
                            DefaultMetadataVisitor<Void> fieldPrinter = new ContainsNullValueFieldPrinter(dataRecord, out);
                            Collection<FieldMetadata> fields = dataRecord.getType().getFields();
                            writeContainedField(containedField, dataRecord);
                            for (FieldMetadata field : fields) {
                                field.accept(fieldPrinter);
                            }
                            out.write("</" + containedField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        if (recordList.size() == 0) {
                            out.write("<" + containedField.getName() + "/>"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    } else if (containsFieldToValue) {
                        out.write("<" + containedField.getName() + "/>"); //$NON-NLS-1$ //$NON-NLS-2$
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
                if (currentValue == null) {
                    out.write("<" + containedField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    out.write("<" + containedField.getName() + " xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI //$NON-NLS-1$ //$NON-NLS-2$
                            + "\" xsi:type=\"" + currentValue.getType().getName() + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
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
                        out.write("<" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                        handleSimpleValue(simpleField, value);
                        out.write("</" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    } else {
                        List valueAsList = (List) value;
                        for (Object currentValue : valueAsList) {
                            if (currentValue != null) {
                                out.write("<" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                                handleSimpleValue(simpleField, currentValue);
                                out.write("</" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                            } else {
                                out.write("<" + simpleField.getName() + "/>"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                        if(valueAsList.size() == 0) {
                            out.write("<" + simpleField.getName() + "/>"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                } else if (containsFieldToValue) {
                    out.write("<" + simpleField.getName() + "/>"); //$NON-NLS-1$ //$NON-NLS-2$
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
                        out.write("<" + enumField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                        handleSimpleValue(enumField, value);
                        out.write("</" + enumField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    } else {
                        List valueAsList = (List) value;
                        for (Object currentValue : valueAsList) {
                            if (currentValue != null) {
                                out.write("<" + enumField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                                handleSimpleValue(enumField, currentValue);
                                out.write("</" + enumField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                            if (currentValue != null) {
                                out.write("<" + enumField.getName() + "/>"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                } else if (containsFieldToValue) {
                    out.write("<" + enumField.getName() + "/>"); //$NON-NLS-1$ //$NON-NLS-2$
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