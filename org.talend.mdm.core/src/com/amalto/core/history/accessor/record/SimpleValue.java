/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.history.accessor.record;

import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.record.DataRecord;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.*;

class SimpleValue implements Setter, Getter {

    static final Setter SET = new SimpleValue();

    static final Getter GET = new SimpleValue();

    @Override
    public void set(MetadataRepository repository, DataRecord record, PathElement element, String value) {
        if (value == null) {
            record.set(element.field, null);
        }
        if (element.field instanceof ReferenceFieldMetadata) {
            ComplexTypeMetadata referencedType = ((ReferenceFieldMetadata) element.field).getReferencedType();
            DataRecord referencedRecord = (DataRecord) StorageMetadataUtils.convert(String.valueOf(value), element.field, referencedType);
            record.set(element.field, referencedRecord);
        } else {
            record.set(element.field, StorageMetadataUtils.convert(String.valueOf(value), element.field));
        }
    }

    @Override
    public String get(DataRecord record, PathElement element) {
        if (record == null || element.field instanceof ContainedTypeFieldMetadata) {
            return StringUtils.EMPTY;
        }
        if (element.field instanceof ReferenceFieldMetadata) {
            if (record.get(element.field) != null && record.get(element.field)  instanceof DataRecord) { //$NON-NLS-1$
                DataRecord referencedRecord = (DataRecord) record.get(element.field);
                if (referencedRecord == null) {
                    return StringUtils.EMPTY;
                }
                StringBuilder builder = new StringBuilder();
                for (FieldMetadata keyField : ((ReferenceFieldMetadata) element.field).getReferencedType().getKeyFields()) {
                    builder.append('[').append(referencedRecord.get(keyField)).append(']');
                }
                return builder.toString();
            }
            return StringUtils.EMPTY;
        } else {
            Object o = record.get(element.field);
            return String.valueOf(StorageMetadataUtils.toString(o, element.field));
        }
    }
}
