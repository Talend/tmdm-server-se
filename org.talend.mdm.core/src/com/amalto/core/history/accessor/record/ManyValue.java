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

import java.util.ArrayList;
import java.util.List;

class ManyValue implements Setter, Getter {

    static final Setter SET = new ManyValue();

    static final Getter GET = new ManyValue();

    @Override
    public void set(MetadataRepository repository, DataRecord record, PathElement element, String value) {
        List list = (List) record.get(element.field);
        if (list == null) {
            list = new ArrayList(element.index);
            record.set(element.field, list);
        }
        while(element.index >= list.size()) {
            list.add(null);
        }
        if (value == null) {
            list.set(element.index, null);
        }
        if (element.field instanceof ReferenceFieldMetadata) {
            ComplexTypeMetadata referencedType = ((ReferenceFieldMetadata) element.field).getReferencedType();
            DataRecord referencedRecord = (DataRecord) StorageMetadataUtils.convert(String.valueOf(value), element.field, referencedType);
            list.set(element.index, referencedRecord);
        } else {
            list.set(element.index, StorageMetadataUtils.convert(String.valueOf(value), element.field));
        }
    }

    @Override
    public String get(DataRecord record, PathElement element) {
        if(element.field instanceof ContainedTypeFieldMetadata) {
            return StringUtils.EMPTY;
        }
        List list = (List) record.get(element.field);
        Object item = list.get(element.index);
        return StorageMetadataUtils.toString(item, element.field);
    }
}
