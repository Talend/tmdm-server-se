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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.xml.XMLConstants;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.AliasedFieldMetadata;
import org.talend.mdm.commmon.metadata.CompoundFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.Types;

import com.amalto.core.query.user.DateConstant;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.query.user.TimeConstant;
import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.StorageMetadataUtils;

public class ViewSearchResultsWriter implements DataRecordWriter {

    private SecuredStorage.UserDelegator delegator = SecuredStorage.UNSECURED;

    @Override
    public void write(DataRecord record, OutputStream output) throws IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(output, "UTF-8")); //$NON-NLS-1$
        write(record, out);
    }

    @Override
    public void write(DataRecord record, Writer writer) throws IOException {
        writer.write("<result xmlns:metadata=\"" + DataRecordReader.METADATA_NAMESPACE + "\"" //$NON-NLS-1$ //$NON-NLS-2$
                + " xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        for (FieldMetadata fieldMetadata : record.getSetFields()) {
            Object value = record.get(fieldMetadata);
            String name = fieldMetadata.getName();
            if (value != null) {
                writer.append("\t<").append(name).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
                processValue(writer, fieldMetadata, value);
                writer.append("</").append(name).append(">\n"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                writer.append("\t<").append(name).append("/>\n"); //$NON-NLS-1$//$NON-NLS-2$
            }
        }
        writer.append("</result>"); //$NON-NLS-1$
        writer.flush();
    }

    @Override
    public void setSecurityDelegator(SecuredStorage.UserDelegator delegator) {
        if(delegator == null) {
            throw new IllegalArgumentException("Delegator cannot be null.");
        }
        this.delegator = delegator;
    }

    private void processValue(Writer out, FieldMetadata fieldMetadata, Object value) throws IOException {
        if (value == null) {
            throw new IllegalArgumentException("Not supposed to write null values to XML."); //$NON-NLS-1$
        }
        if (delegator.hide(fieldMetadata)) {
            out.append(StringUtils.EMPTY);
            return;
        }
        String stringValue;
        TypeMetadata type = fieldMetadata.getType();
        if (fieldMetadata instanceof SimpleTypeFieldMetadata) {
            type = MetadataUtils.getSuperConcreteType(type);
        }
        if (Types.DATE.equals(type.getName())) {
            synchronized (DateConstant.DATE_FORMAT) {
                stringValue = formatDateValue(DateConstant.DATE_FORMAT, value);
            }
        } else if (Types.DATETIME.equals(type.getName())) {
            synchronized (DateTimeConstant.DATE_FORMAT) {
                stringValue = formatDateValue(DateTimeConstant.DATE_FORMAT, value);
            }
        } else if (Types.TIME.equals(type.getName())) {
            synchronized (TimeConstant.TIME_FORMAT) {
                stringValue = formatDateValue(TimeConstant.TIME_FORMAT, value);
            }
        } else if (value instanceof Object[]) {
            StringBuilder valueAsString = new StringBuilder();
            CompoundFieldMetadata compoundFields = (CompoundFieldMetadata) ((ReferenceFieldMetadata) fieldMetadata)
                    .getReferencedField();
            int i = 0;
            for (Object current : ((Object[]) value)) {
                valueAsString.append('[').append(StorageMetadataUtils.toString(current, compoundFields.getFields()[i++]))
                        .append(']');
            }
            stringValue = valueAsString.toString();
        } else {
            stringValue = String.valueOf(value);
        }
        if (fieldMetadata instanceof ReferenceFieldMetadata) {
            if (value instanceof DataRecord) {
                // TODO Replace with MetadataUtils.toString()
                DataRecord referencedRecord = (DataRecord) value;
                StringBuilder fkValueAsString = new StringBuilder();
                for (FieldMetadata keyField : referencedRecord.getType().getKeyFields()) {
                    fkValueAsString.append('[').append(referencedRecord.get(keyField)).append(']');
                }
                stringValue = fkValueAsString.toString();
            } else {
                if (!stringValue.startsWith("[")) { //$NON-NLS-1$
                    stringValue = "[" + StorageMetadataUtils.toString(value, ((ReferenceFieldMetadata) fieldMetadata).getReferencedField()) + ']'; //$NON-NLS-1$
                }
            }
        }
        if (fieldMetadata instanceof AliasedFieldMetadata && ((AliasedFieldMetadata)fieldMetadata).getAliasedField() instanceof ReferenceFieldMetadata) {
            if (!stringValue.startsWith("[")) { //$NON-NLS-1$
                stringValue = "[" + StorageMetadataUtils.toString(value, ((ReferenceFieldMetadata) ((AliasedFieldMetadata)fieldMetadata).getAliasedField()).getReferencedField()) + ']'; //$NON-NLS-1$
            }
        }
        out.append(StringEscapeUtils.escapeXml(stringValue));
    }

    private String formatDateValue(DateFormat dateFormat, Object value) throws IOException {
        try {
            if (String.valueOf(value).contains(",") || value instanceof List) {
                String[] dates = String.valueOf(value).split(",");
                StringBuffer sb = new StringBuffer();
                for (String date : dates) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(dateFormat.format(dateFormat.parse(date.replaceAll("\\[|\\]", ""))));
                }
                return sb.toString();
            } else if (value instanceof String) {
                return dateFormat.format(dateFormat.parse(String.valueOf(value)));
            } else {
                return dateFormat.format(value);
            }
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}
