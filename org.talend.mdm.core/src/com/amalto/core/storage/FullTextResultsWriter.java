/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;

import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordWriter;

public class FullTextResultsWriter implements DataRecordWriter {

    private final String keyword;

    private SecuredStorage.UserDelegator delegator = SecuredStorage.UNSECURED;

    public FullTextResultsWriter(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public void write(DataRecord record, OutputStream output) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8"); //$NON-NLS-1$
        write(record, writer);
    }

    @Override
    public void write(DataRecord record, Writer writer) throws IOException {
        Collection<FieldMetadata> keyFields = record.getType().getKeyFields();

        writer.write("<item>"); //$NON-NLS-1$
        {
            {
                writer.write("<ids>"); //$NON-NLS-1$
                for (FieldMetadata keyField : keyFields) {
                    writer.write("<id>" + StringEscapeUtils.escapeXml(StorageMetadataUtils.toString(record.get(keyField), keyField)) + "</id>"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                writer.write("</ids>"); //$NON-NLS-1$
            }
            {
                writer.write("<title>"); //$NON-NLS-1$
                writer.write(record.getType().getName());
                for (FieldMetadata keyField : keyFields) {
                    writer.write(" " + StringEscapeUtils.escapeXml(StorageMetadataUtils.toString(record.get(keyField), keyField))); //$NON-NLS-1$
                }
                writer.write("</title>"); //$NON-NLS-1$
            }
            {
                writer.write("<text>"); //$NON-NLS-1$
                String[] snippetWords = record.getType().accept(new SnippetCreator(record));
                StringBuilder builder = new StringBuilder();
                for (String snippetWord : snippetWords) {
                    builder.append(snippetWord).append(" ... "); //$NON-NLS-1$
                }
                writer.write(builder.toString());
                writer.write("</text>"); //$NON-NLS-1$
            }
            {
                writer.write("<typeName>"); //$NON-NLS-1$
                writer.write(record.getType().getName());
                writer.write("</typeName>"); //$NON-NLS-1$
            }
        }
        writer.write("</item>"); //$NON-NLS-1$
        writer.flush();
    }

    @Override
    public void setSecurityDelegator(SecuredStorage.UserDelegator delegator) {
        if(delegator == null) {
            throw new IllegalArgumentException("Delegator cannot be null.");
        }
        this.delegator = delegator;
    }

    private class SnippetCreator extends DefaultMetadataVisitor<String[]> {

        private final DataRecord record;

        private final String[] snippetWords = new String[] { StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY }; // Prevent
                                                                                                                        // "null"
                                                                                                                        // values
                                                                                                                        // in
                                                                                                                        // results

        boolean hasMetKeyword;

        boolean done;

        public SnippetCreator(DataRecord record) {
            this.record = record;
            hasMetKeyword = false;
            done = false;
        }

        @Override
        public String[] visit(ComplexTypeMetadata complexType) {
            super.visit(complexType);
            return snippetWords;
        }

        @Override
        public String[] visit(ContainedComplexTypeMetadata containedType) {
            super.visit(containedType);
            return snippetWords;
        }

        @Override
        public String[] visit(ContainedTypeFieldMetadata containedField) {
            super.visit(containedField);
            return snippetWords;
        }

        @Override
        public String[] visit(SimpleTypeFieldMetadata simpleField) {
            if (delegator.hide(simpleField)) {
                return snippetWords;
            }
            if (!done) {
                List<String> values;
                Object valueAsObject = record.get(simpleField);
                if (valueAsObject != null) {
                    if (simpleField.isMany()) {
                        List list = (List) valueAsObject;
                        values = new ArrayList<>(list.size());
                        for (Object o : list) {
                            values.add(String.valueOf(o));
                        }
                    } else {
                        values = Collections.singletonList(String.valueOf(valueAsObject));
                    }
                    for (String value : values) {
                        if (value.contains(keyword)) {
                            snippetWords[1] = "<b>" + StringEscapeUtils.escapeXml(value) + "</b>"; //$NON-NLS-1$ //$NON-NLS-2$
                            hasMetKeyword = true;
                            break;
                        } else {
                            snippetWords[hasMetKeyword ? 0 : 2] = StringEscapeUtils.escapeXml(value);
                            if (hasMetKeyword) {
                                done = true;
                                break;
                            }
                        }
                    }
                }
            }
            return snippetWords;
        }
    }
}
