/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save.generator;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;

import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;

@SuppressWarnings("nls")
public class AutoIncrementUtil {

    public static String getConceptForAutoIncrement(String storageName, String conceptName) {
        String concept = null;
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage storage = storageAdmin.get(storageName, storageAdmin.getType(storageName));
        if (storage != null) {
            MetadataRepository metadataRepository = storage.getMetadataRepository();
            if (metadataRepository != null) {
                if (conceptName.contains(".")) {
                    concept = conceptName.split("\\.")[0];
                } else {
                    concept = conceptName;
                }
                ComplexTypeMetadata complexType = metadataRepository.getComplexType(concept);
                if (complexType != null) {
                    concept = MetadataUtils.getSuperConcreteType(complexType).getName();
                }
            }
        }
        return concept;
    }

    /**
     * For inherited type, the origin fieldPath is like this: B.Id or C.id, B and C super type is A,
     * it used the super type as the concept. so it will return to A.id
     *
     * If the field path is in one complex type, like Course/Detail/Count, Course and Detail, both are the complex type,
     * the field path is don't modify.
     *
     * @param storageName the storage name
     * @param concept concept name, if it's the inherited, it is the super type name.
     * @param fieldPath the AUTO_INCREMENT type field's field path, with the concept name as the prefix.
     * @return the field path
     */
    public static String getAutoIncrementFieldPath(String storageName, String concept, String fieldPath) {
        if (storageName == null || concept == null || fieldPath == null) {
            return null;
        }
        // if the field is inherited, it should be remove the concept name from the fieldPath
        if (concept.equals(getConceptForAutoIncrement(storageName, fieldPath)) && fieldPath.contains(".")) {
            return fieldPath.substring(fieldPath.indexOf(".") + 1);
        }
        return fieldPath;
    }

}
