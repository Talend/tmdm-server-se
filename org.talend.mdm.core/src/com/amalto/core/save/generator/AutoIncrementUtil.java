/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

    public static String getAutoIncrementFieldName(String storageName, String concept, String conceptName) {
        if (concept == null) {
            return null;
        }
        String autoIncrementFieldName;
        if (getConceptForAutoIncrement(storageName, conceptName).equals(concept) && conceptName.contains(".")) {
            autoIncrementFieldName = conceptName.substring(conceptName.indexOf(".") + 1);
        } else {
            autoIncrementFieldName = conceptName;
        }
        return autoIncrementFieldName;
    }

}
