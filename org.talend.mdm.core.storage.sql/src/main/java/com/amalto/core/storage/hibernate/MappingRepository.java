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

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MappingRepository {

    private final Map<TypeMetadata, TypeMapping> userToMapping = new HashMap<TypeMetadata, TypeMapping>();

    public TypeMapping getMappingFromUser(TypeMetadata type) {
        if (type instanceof TypeMapping) {
            return (TypeMapping) type;
        }
        TypeMapping mapping = userToMapping.get(type);
        if (mapping == null && type instanceof ComplexTypeMetadata) {
            // In case, type is a nested type, only the entity type is in repository
            mapping = userToMapping.get(((ComplexTypeMetadata) type).getEntity());
        }
        return mapping;
    }

    public TypeMapping getMappingFromDatabase(TypeMetadata type) {
        if (type instanceof TypeMapping) {
            return (TypeMapping) type;
        }
        for (Map.Entry<TypeMetadata, TypeMapping> entry : userToMapping.entrySet()) {
            TypeMapping mapping = entry.getValue();
            if (mapping.getDatabase().equals(type)) {
                return mapping;
            }
        }
        return null;
    }

    public void addMapping(TypeMapping mapping) {
        userToMapping.put(mapping.getUser(), mapping);
    }

    public Collection<TypeMapping> getAllTypeMappings() {
        return userToMapping.values();
    }
}
