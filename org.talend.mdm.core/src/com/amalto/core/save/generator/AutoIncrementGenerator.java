/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save.generator;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;

@SuppressWarnings("nls")
public class AutoIncrementGenerator {

    private static final Logger LOGGER = Logger.getLogger(AutoIncrementGenerator.class);

    private static final boolean ENABLE_CLUSTERED_AUTO_INCREMENT;

    static {
        // Enable clustered auto increment generator
        ENABLE_CLUSTERED_AUTO_INCREMENT = MDMConfiguration.isClusterEnabled();
        if (ENABLE_CLUSTERED_AUTO_INCREMENT) {
            LOGGER.info("Enable clustered access support for auto increment generator.");
        } else {
            LOGGER.info("Clustered access support for auto increment generator is disabled.");
        }
    }

    public static AutoIdGenerator get() {
        if (ENABLE_CLUSTERED_AUTO_INCREMENT) {
            return HazelcastAutoIncrementGenerator.getInstance();
        }
        return InMemoryAutoIncrementGenerator.getInstance();
    }
    
    public static String getConceptForAutoIncrement(String storageName, String conceptName) {
        String concept = null;
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage storage = storageAdmin.get(storageName, storageAdmin.getType(storageName));
        if (storage != null) {
            MetadataRepository metadataRepository = storage.getMetadataRepository();
            if (metadataRepository != null) {
                if (conceptName.contains(".")) { //$NON-NLS-1$
                    concept = conceptName.split("\\.")[0];//$NON-NLS-1$
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

}
