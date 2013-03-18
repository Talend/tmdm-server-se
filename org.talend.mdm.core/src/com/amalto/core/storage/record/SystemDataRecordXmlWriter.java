/*
 * Copyright (C) 2006-2013 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.record;

import com.amalto.core.metadata.ClassRepository;
import org.talend.mdm.commmon.metadata.*;
import com.amalto.core.query.user.DateConstant;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.query.user.TimeConstant;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;

public class SystemDataRecordXmlWriter implements DataRecordWriter {

    private final String rootElementName;

    private final ClassRepository repository;

    private ComplexTypeMetadata type;

    public SystemDataRecordXmlWriter(ClassRepository repository) {
        this(repository, ((String) null));
    }

    private SystemDataRecordXmlWriter(ClassRepository repository, String rootElementName) {
        this.repository = repository;
        this.rootElementName = rootElementName;
    }

    public SystemDataRecordXmlWriter(ClassRepository repository, ComplexTypeMetadata type) {
        this.repository = repository;
        this.type = type;
        this.rootElementName = type.getName();
    }

    public void write(DataRecord record, OutputStream output) throws IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(output, "UTF-8")); //$NON-NLS-1$
        write(record, out);
    }

    public void write(DataRecord record, Writer writer) throws IOException {
        FieldPrinter fieldPrinter = new FieldPrinter(record, writer);
        Set<FieldMetadata> fields = type == null ? new HashSet<FieldMetadata>(record.getType().getFields()) : new HashSet<FieldMetadata>(type.getFields());
        // Print isMany=false as attributes
        fieldPrinter.printAttributes(true);
        writer.write("<" + getRootElementName(record)); //$NON-NLS-1$ //$NON-NLS-2$
        Iterator<FieldMetadata> iterator = fields.iterator();
        while (iterator.hasNext()) {
            FieldMetadata field = iterator.next();
            if (field instanceof SimpleTypeFieldMetadata && !field.isMany() && isValidAttributeType(field.getType())) {
                writer.append(' ');
                field.accept(fieldPrinter);
                iterator.remove();
            }
        }
        writer.write('>');
        // Print isMany=true as elements
        fieldPrinter.printAttributes(false);
        for (FieldMetadata field : fields) {
            field.accept(fieldPrinter);
        }
        writer.write("</" + getRootElementName(record) + ">"); //$NON-NLS-1$ //$NON-NLS-2$
        writer.flush();
    }

    private static boolean isValidAttributeType(TypeMetadata type) {
        if ("string".equals(type.getName()) //$NON-NLS-1$
                || "date".equals(type.getName()) //$NON-NLS-1$
                || ClassRepository.EMBEDDED_XML.equals(type.getName())) {
            return false;
        }
        return true;
    }

    private String getRootElementName(DataRecord record) {
        return rootElementName == null ? record.getType().getName() : rootElementName;
    }

    private class FieldPrinter extends DefaultMetadataVisitor<Void> {

        private final DataRecord record;

        private final Writer out;

        private boolean printAttributes;

        public FieldPrinter(DataRecord record, Writer out) {
            this.record = record;
            this.out = out;
        }

        @Override
        public Void visit(ReferenceFieldMetadata referenceField) {
            throw new NotImplementedException();
        }

        @Override
        public Void visit(ContainedTypeFieldMetadata containedField) {
            try {
                if (containedField.getType().getName().startsWith("array-list-holder")) {
                    DataRecord containedRecord = (DataRecord) record.get(containedField);
                    if (containedRecord != null) {
                        List list = (List) containedRecord.get("list");
                        if (list != null && list.isEmpty()) {
                            return null;
                        }
                    }
                }
                if (!containedField.isMany()) {
                    DataRecord containedRecord = (DataRecord) record.get(containedField);
                    if (containedRecord != null) {
                        handleContainedField(containedField, containedRecord);
                    }
                } else {
                    List<DataRecord> recordList = (List<DataRecord>) record.get(containedField);
                    if (recordList != null && !recordList.isEmpty()) {
                        for (DataRecord dataRecord : recordList) {
                            handleContainedField(containedField, dataRecord);
                        }
                    }
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Could not serialize XML for contained field '" + containedField.getName() + "' of type '" + containedField.getContainingType().getName() + "'.", e);
            }
        }

        private void handleContainedField(ContainedTypeFieldMetadata containedField, DataRecord containedRecord) throws IOException {
            if (ClassRepository.MAP_TYPE_NAME.equals(containedRecord.getType().getName())) {
                out.write(String.valueOf(containedRecord.get("value")));
            } else {
                // TODO Limit new field printer instances
                DefaultMetadataVisitor<Void> fieldPrinter = new FieldPrinter(containedRecord, out);
                Collection<FieldMetadata> fields = containedRecord.getType().getFields();
                Class javaClass = repository.getJavaClass(containedRecord.getType().getName());
                if (javaClass == null || javaClass.isInterface() || Modifier.isAbstract(javaClass.getModifiers())) {
                    out.write("<" + containedField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    out.write("<" + containedField.getName() + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" //$NON-NLS-1$
                            + " xsi:type=\"java:" //$NON-NLS-1$
                            + javaClass.getName()
                            + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                for (FieldMetadata field : fields) {
                    field.accept(fieldPrinter);
                }
                out.write("</" + containedField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        @Override
        public Void visit(SimpleTypeFieldMetadata simpleField) {
            try {
                Object value = record.get(simpleField);
                if (value != null) {
                    if (!simpleField.isMany()) {
                        if (printAttributes) {
                            out.write(simpleField.getName() + "='"); //$NON-NLS-1$
                            handleSimpleValue(simpleField, value);
                            out.write('\'');
                        } else {
                            if (ClassRepository.EMBEDDED_XML.equals(simpleField.getType().getName())) {
                                out.write("<" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                            } else {
                                out.write("<" + simpleField.getName() //$NON-NLS-1$
                                        + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" //$NON-NLS-1$
                                        + " xsi:type=\"java:" //$NON-NLS-1$
                                        + value.getClass().getName()
                                        + "\">"); //$NON-NLS-1$
                            }
                            handleSimpleValue(simpleField, value);
                            out.write("</" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    } else {
                        if (printAttributes) {
                            throw new IllegalStateException("Can't write repeatable elements as attributes.");
                        }
                        List valueAsList = (List) value;
                        for (Object currentValue : valueAsList) {
                            if (currentValue != null) {
                                if (ClassRepository.EMBEDDED_XML.equals(simpleField.getType().getName())) {
                                    out.write("<" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                                } else {
                                    out.write("<" + simpleField.getName() //$NON-NLS-1$
                                            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" //$NON-NLS-1$
                                            + " xsi:type=\"java:" //$NON-NLS-1$
                                            + currentValue.getClass().getName()
                                            + "\">"); //$NON-NLS-1$
                                }
                                handleSimpleValue(simpleField, currentValue);
                                out.write("</" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
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
            throw new NotImplementedException();
        }

        private void handleSimpleValue(FieldMetadata simpleField, Object value) throws IOException {
            if (value == null) {
                throw new IllegalArgumentException("Not supposed to write null values to XML.");
            }
            if ("date".equals(simpleField.getType().getName())) { //$NON-NLS-1$
                synchronized (DateConstant.DATE_FORMAT) {
                    out.write((DateConstant.DATE_FORMAT).format(value));
                }
            } else if ("dateTime".equals(simpleField.getType().getName())) { //$NON-NLS-1$
                synchronized (DateTimeConstant.DATE_FORMAT) {
                    out.write((DateTimeConstant.DATE_FORMAT).format(value));
                }
            } else if ("time".equals(simpleField.getType().getName())) { //$NON-NLS-1$
                synchronized (TimeConstant.TIME_FORMAT) {
                    out.write((TimeConstant.TIME_FORMAT).format(value));
                }
            } else if (ClassRepository.EMBEDDED_XML.equals(simpleField.getType().getName())) {
                out.write(value.toString());
            } else {
                out.write(StringEscapeUtils.escapeXml(value.toString()));
            }
        }

        public void printAttributes(boolean printAttributes) {
            this.printAttributes = printAttributes;
        }
    }
}
