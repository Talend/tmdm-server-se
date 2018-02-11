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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.amalto.core.storage.SecuredStorage;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringEscapeUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.Types;

import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.query.user.DateConstant;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.query.user.TimeConstant;

public class SystemDataRecordXmlWriter implements DataRecordWriter {

    private final String rootElementName;

    private final ClassRepository repository;

    private final ComplexTypeMetadata type;

    public SystemDataRecordXmlWriter(ClassRepository repository, ComplexTypeMetadata type) {
        this.repository = repository;
        this.type = type;
        this.rootElementName = type.getName();
    }

    @Override
    public void write(DataRecord record, OutputStream output) throws IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(output, "UTF-8")); //$NON-NLS-1$
        write(record, out);
    }

    @Override
    public void write(DataRecord record, Writer writer) throws IOException {
        FieldPrinter fieldPrinter = new FieldPrinter(record, writer);
        Set<FieldMetadata> fields = type == null ? new HashSet<FieldMetadata>(record.getType().getFields())
                : new HashSet<FieldMetadata>(type.getFields());
        // Print isMany=false as attributes
        fieldPrinter.printAttributes(true);
        writer.write("<" + getRootElementName(record)); //$NON-NLS-1$ 
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

    @Override
    public void setSecurityDelegator(SecuredStorage.UserDelegator delegator) {
        // System objects don't have hide information on fields, prefer a clear exception iso. promise that can't be
        // hold.
        throw new UnsupportedOperationException();
    }

    private static boolean isValidAttributeType(TypeMetadata type) {
        return !(Types.STRING.equals(type.getName()) || Types.DATE.equals(type.getName()) || Types.BOOLEAN.equals(type.getName()) || ClassRepository.EMBEDDED_XML
                .equals(type.getName()));
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
                throw new RuntimeException("Could not serialize XML for contained field '" + containedField.getName()
                        + "' of type '" + containedField.getContainingType().getName() + "'.", e);
            }
        }

        private void handleContainedField(ContainedTypeFieldMetadata containedField, DataRecord containedRecord)
                throws IOException {
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
                            + javaClass.getName() + "\">"); //$NON-NLS-1$ 
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
                            } else if ("base64Binary".equals(simpleField.getType().getName())) { //$NON-NLS-1$
                                out.write("<" + simpleField.getName() //$NON-NLS-1$
                                        + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" //$NON-NLS-1$
                                        + " xsi:type=\"[B\">"); //$NON-NLS-1$
                            } else {
                                out.write("<" + simpleField.getName() //$NON-NLS-1$
                                        + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" //$NON-NLS-1$
                                        + " xsi:type=\"java:" //$NON-NLS-1$
                                        + value.getClass().getName() + "\">"); //$NON-NLS-1$
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
                                } else if (Types.BASE64_BINARY.equals(simpleField.getType().getName())) {
                                    out.write("<" + simpleField.getName() //$NON-NLS-1$
                                            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" //$NON-NLS-1$
                                            + " xsi:type=\"[B\">"); //$NON-NLS-1$
                                } else {
                                    out.write("<" + simpleField.getName() //$NON-NLS-1$
                                            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" //$NON-NLS-1$
                                            + " xsi:type=\"java:" //$NON-NLS-1$
                                            + currentValue.getClass().getName() + "\">"); //$NON-NLS-1$
                                }
                                handleSimpleValue(simpleField, currentValue);
                                out.write("</" + simpleField.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                } else {
                    if (!printAttributes) {
                        // TMDM-5572: Prints empty elements for null values
                        out.write("<" + simpleField.getName() + "/>"); //$NON-NLS-1$ //$NON-NLS-2$
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
            throw new NotImplementedException();
        }

        private void handleSimpleValue(FieldMetadata simpleField, Object value) throws IOException {
            if (value == null) {
                throw new IllegalArgumentException("Not supposed to write null values to XML.");
            }
            if (Types.DATE.equals(simpleField.getType().getName())) {
                synchronized (DateConstant.DATE_FORMAT) {
                    out.write((DateConstant.DATE_FORMAT).format(value));
                }
            } else if (Types.DATETIME.equals(simpleField.getType().getName())) {
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
