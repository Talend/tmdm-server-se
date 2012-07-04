/*
 * Copyright (C) 2006-2012 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import com.amalto.core.metadata.ComplexTypeMetadata;
import com.amalto.core.metadata.FieldMetadata;

import java.util.Set;

class StorageTableResolver implements TableResolver {

    private final Set<FieldMetadata> indexedFields;

    public StorageTableResolver(Set<FieldMetadata> indexedFields) {
        this.indexedFields = indexedFields;
    }

    public String get(ComplexTypeMetadata type) {
        return type.getName().toUpperCase();
    }

    public String get(FieldMetadata field) {
        return field.getName();
    }

    public boolean isIndexed(FieldMetadata field) {
        return indexedFields.contains(field);
    }
}
