/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.record;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONWriter;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;

import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.StorageResults;

/**
 * A {@link com.amalto.core.storage.record.DataRecordWriter} implementation to serialize a {@link DataRecord record} to
 * JSON.
 */
public class DataRecordJSONWriter implements DataRecordWriter {

    // Controls attribute name to be lowcase or raw value defined in schema, default: true, lowcase
    private boolean ignoreCase = true;

    private SecuredStorage.UserDelegator delegator = SecuredStorage.UNSECURED;

    public DataRecordJSONWriter(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    private void writeRecord(final DataRecord record, final JSONWriter writer) throws JSONException {
        writer.object();
        {
            if(record != null){
                for (FieldMetadata field : record.getType().getFields()) {
                    field.accept(new DefaultMetadataVisitor<Void>() {

                        private Void handleSimpleField(FieldMetadata field) {
                            if (delegator.hide(field)) {
                                return null;
                            }
                            String fieldName = getQualifiedName(field.getName());
                            try {
                                if (!field.isMany()) {
                                    writer.key(fieldName).value(StorageMetadataUtils.toString(record.get(field, true), false));
                                } else {
                                    List<Object> values = (List<Object>) record.get(field);
                                    if (values != null) {
                                        writer.key(fieldName).array();
                                        for (Object value : values) {
                                            writer.value(StorageMetadataUtils.toString(value, false));
                                        }
                                        writer.endArray();
                                    }
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException("Unable to serialize simple field '" + field.getName() + "'", e);//$NON-NLS-1$ //$NON-NLS-2$
                            }
                            return null;
                        }

                        @Override
                        public Void visit(SimpleTypeFieldMetadata simpleField) {
                            return handleSimpleField(simpleField);
                        }

                        @Override
                        public Void visit(EnumerationFieldMetadata enumField) {
                            return handleSimpleField(enumField);
                        }

                        @Override
                        public Void visit(ContainedTypeFieldMetadata containedField) {
                            if (delegator.hide(containedField)) {
                                return null;
                            }
                            String containedName = getQualifiedName(containedField.getName());
                            try {
                                writer.key(containedName);
                                if (!containedField.isMany()) {
                                    writeRecord((DataRecord) record.get(containedField), writer);
                                } else {
                                    List<DataRecord> values = (List<DataRecord>) record.get(containedField);
                                    {
                                        writer.array();
                                        if (values != null) {

                                            for (DataRecord value : values) {
                                                writeRecord(value, writer);
                                            }
                                        }
                                        writer.endArray();
                                    }
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException("Unable to serialize complex field '" + containedField.getName() + "'", e);//$NON-NLS-1$ //$NON-NLS-2$
                            }
                            return null;
                        }

                        @Override
                        public Void visit(ReferenceFieldMetadata referenceField) {
                            return handleSimpleField(referenceField);
                        }
                    });
                }
            }
        }
        writer.endObject();
    }

    @Override
    public void write(StorageResults recordList, OutputStream output) throws IOException {
        Writer writer = new OutputStreamWriter(output, Charset.forName("UTF-8"));//$NON-NLS-1$
        for (Iterator<DataRecord> iterator = recordList.iterator(); iterator.hasNext();) {
            JSONWriter jsonWriter = new JSONWriter(writer);
            DataRecord record = iterator.next();
            write(record, jsonWriter);
            if (iterator.hasNext()) {
                output.write(",".getBytes()); //$NON-NLS-1$
            }
            writer.flush();
        }
    }

    private void write(DataRecord record, JSONWriter jsonWriter) throws IOException {
        String typeName = getQualifiedName(record.getType().getName());
        try {
            jsonWriter.object().key(typeName);
            {
                if (!delegator.hide(record.getType())) {
                    writeRecord(record, jsonWriter);
                }
            }
            jsonWriter.endObject();
        } catch (JSONException e) {
            throw new IOException("Could not serialize to JSON.", e);//$NON-NLS-1$
        }
    }

    @Override
    public void write(DataRecord record, OutputStream output) throws IOException {
        write(record, new OutputStreamWriter(output, Charset.forName("UTF-8")));//$NON-NLS-1$
    }

    @Override
    public void write(DataRecord record, Writer writer) throws IOException {
        JSONWriter jsonWriter = new JSONWriter(writer);
        String typeName = getQualifiedName(record.getType().getName());
        try {
            jsonWriter.object().key(typeName);
            {
                if (!delegator.hide(record.getType())) {
                    writeRecord(record, jsonWriter);
                }
            }
            jsonWriter.endObject();
            writer.flush();
        } catch (JSONException e) {
            throw new IOException("Could not serialize to JSON.", e);//$NON-NLS-1$
        }
    }

    @Override
    public void setSecurityDelegator(SecuredStorage.UserDelegator delegator) {
        if(delegator == null) {
            throw new IllegalArgumentException("Delegator cannot be null.");//$NON-NLS-1$
        }
        this.delegator = delegator;
    }

    private String getQualifiedName(String name) {
        return ignoreCase ? name.toLowerCase() : name;
    }
}
