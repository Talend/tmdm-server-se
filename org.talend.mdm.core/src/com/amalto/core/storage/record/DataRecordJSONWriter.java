/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import java.util.List;

import com.amalto.core.storage.SecuredStorage;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONWriter;
import org.talend.mdm.commmon.metadata.*;

import com.amalto.core.storage.StorageMetadataUtils;

/**
 * A {@link com.amalto.core.storage.record.DataRecordWriter} implementation to serialize a {@link DataRecord record} to
 * JSON.
 */
public class DataRecordJSONWriter implements DataRecordWriter {

    private SecuredStorage.UserDelegator delegator = SecuredStorage.UNSECURED;

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
                            try {
                                if (!field.isMany()) {
                                    writer.key(field.getName().toLowerCase()).value(
                                            StorageMetadataUtils.toString(record.get(field), false));
                                } else {
                                    List<Object> values = (List<Object>) record.get(field);
                                    if (values != null) {
                                        writer.key(field.getName().toLowerCase()).array();
                                        for (Object value : values) {
                                            writer.value(StorageMetadataUtils.toString(value, false));
                                        }
                                        writer.endArray();
                                    }
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException("Unable to serialize simple field '" + field.getName() + "'", e);
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
                            try {
                                writer.key(containedField.getName().toLowerCase());
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
                                throw new RuntimeException("Unable to serialize complex field '" + containedField.getName() + "'", e);
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
    public void write(DataRecord record, OutputStream output) throws IOException {
        write(record, new OutputStreamWriter(output, Charset.forName("UTF-8")));
    }

    @Override
    public void write(DataRecord record, Writer writer) throws IOException {
        JSONWriter jsonWriter = new JSONWriter(writer);
        try {
            jsonWriter.object().key(record.getType().getName().toLowerCase());
            {
                if (!delegator.hide(record.getType())) {
                    writeRecord(record, jsonWriter);
                }
            }
            jsonWriter.endObject();
            writer.flush();
        } catch (JSONException e) {
            throw new IOException("Could not serialize to JSON.", e);
        }
    }

    @Override
    public void setSecurityDelegator(SecuredStorage.UserDelegator delegator) {
        if(delegator == null) {
            throw new IllegalArgumentException("Delegator cannot be null.");
        }
        this.delegator = delegator;
    }
}
