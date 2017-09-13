/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;

import java.util.Set;

public class OracleStorageTableResolver extends StorageTableResolver {

    public OracleStorageTableResolver(Set<FieldMetadata> indexedFields, int maxLength) {
        super(indexedFields, maxLength);
    }

    @Override
    public boolean isIndexed(FieldMetadata field) {
        // Oracle seems to already index FK, no need to recreate index (and this also cause error ORA-01408).
        return !field.isKey() && !(field instanceof ReferenceFieldMetadata) && super.isIndexed(field);
    }
}
