/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server;

import com.amalto.core.objects.DroppedItemPOJO;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.query.user.Expression;
import com.amalto.core.storage.DispatchWrapper;
import com.amalto.core.storage.StagingStorage;
import com.amalto.core.storage.datasource.DataSourceDefinition;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.io.IOUtils;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSourceFactory;
import com.amalto.core.util.XtentisException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import java.io.*;
import java.util.*;

public class StorageAdminImpl implements StorageAdmin {

    public static final String MATCH_RULE_POJO_CLASS = "com.amalto.core.storage.task.config.MatchRulePOJO"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(StorageAdminImpl.class);

    /**
     * Default datasource name to be used for user/master data (from datasources configuration content).
     */
    private static final String DEFAULT_USER_DATA_SOURCE_NAME = MDMConfiguration.getConfiguration().getProperty(
            "db.default.datasource", "RDBMS-1"); //$NON-NLS-1$ //$NON-NLS-2$

    // Default value is "false" (meaning the storage will not remove existing data).
    private static final boolean autoClean = Boolean.valueOf(MDMConfiguration.getConfiguration().getProperty(
            "db.autoClean", "false")); //$NON-NLS-1$ //$NON-NLS-2$

    private static final String LICENSE_POJO_CLASS = "com.amalto.core.util.license.LicensePOJO"; //$NON-NLS-1$

    private static final String VERSIONING_POJO_CLASS = "com.amalto.core.objects.versioning.ejb.VersioningSystemPOJO"; //$NON-NLS-1$

    private static final String[] OPTIONAL_CLASSES = new String[] { LICENSE_POJO_CLASS, VERSIONING_POJO_CLASS,
            MATCH_RULE_POJO_CLASS };

    // TODO Change value to an EnumMap
    private final Map<String, MultiKeyMap> storages = new StorageMap();

    public String[] getAll() {
        Set<String> allStorageNames = new HashSet<>();
        for (Map.Entry<String, MultiKeyMap> currentStorage : storages.entrySet()) {
            MultiKeyMap value = currentStorage.getValue();
            if (value.containsKey(StringUtils.EMPTY, StorageType.MASTER)) {
                allStorageNames.add(currentStorage.getKey());
            }
            if (value.containsKey(StringUtils.EMPTY, StorageType.STAGING)) {
                allStorageNames.add(currentStorage.getKey());
            }
        }
        return allStorageNames.toArray(new String[allStorageNames.size()]);
    }

    /**
     * Delete storages of the given container (storage name)
     * 
     * @param String the storage name
     * @param dropExistingData true to drop existing data
     */
    public void delete(String storageName, boolean dropExistingData) {
        delete(storageName, StorageType.MASTER, dropExistingData);
        delete(storageName, StorageType.STAGING, dropExistingData);
        //delete from system db
        try {
            //this means the SYSTEM storage still exist
            //so we can delete the cluster from it
            if (getRegisteredStorage(SYSTEM_STORAGE, StorageType.SYSTEM) != null) {
                DataClusterPOJO dataClusterPOJO = new DataClusterPOJO(storageName);
                ObjectPOJO.remove(DataClusterPOJO.class, dataClusterPOJO.getPK());
                if (DefaultDataCluster.EXISTED_DATA_CLUSTERS.containsKey(dataClusterPOJO.getPK().getUniqueId())) {
                    DefaultDataCluster.EXISTED_DATA_CLUSTERS.remove(dataClusterPOJO.getPK().getUniqueId());
                }
            }
            
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Unable to delete container.", e); //$NON-NLS-1$
            }
            throw new RuntimeException("Unable to delete container.", e); //$NON-NLS-1$
        }    
    }

    /**
     * Delete storage of one type
     * 
     * @param storageName The name of the storage
     * @param type The storage {@link com.amalto.core.storage.StorageType type} (Staging, Master...).
     * @param dropExistingData true to drop existing data
     */
    public void delete(String storageName, StorageType type, boolean dropExistingData) {
        Storage storage = getRegisteredStorage(storageName, type);
        if (storage == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Storage '" + storageName + "' is already deleted.");
            }
            return;
        }
        ServerContext.INSTANCE.getLifecycle().destroyStorage(storage, dropExistingData);
        storages.get(storageName).remove(StringUtils.EMPTY, storage.getType());
        if (storages.get(storageName).isEmpty()) {
            storages.remove(storageName);
        }
    }

    public void deleteAll(boolean dropExistingData) {
        for (String clusterName : new HashSet<>(storages.keySet())) {
            delete(clusterName, StorageType.MASTER, dropExistingData);
            delete(clusterName, StorageType.STAGING, dropExistingData);
        }
    }

    public Storage create(String dataModelName, String storageName, StorageType type, String dataSourceName) {
        if (MDMConfiguration.getConfiguration().get(DataSourceFactory.DB_DATASOURCES) == null) {
            throw new IllegalStateException("MDM Configuration is not configured for RDBMS storage.");
        }
        if (DispatchWrapper.isMDMInternal(storageName)) {
            return internalCreateSystemStorage(dataSourceName);
        }
        String actualStorageName = StringUtils.substringBefore(storageName, STAGING_SUFFIX);
        String actualDataModelName = StringUtils.substringBefore(dataModelName, STAGING_SUFFIX);
        try {
            switch (type) {
            case MASTER:
                return internalCreateStorage(actualDataModelName, actualStorageName, dataSourceName, StorageType.MASTER);
            case STAGING:
                if (supportStaging(actualStorageName)) {
                    boolean hasDataSource = ServerContext.INSTANCE.get().hasDataSource(dataSourceName, actualStorageName,
                            StorageType.STAGING);
                    if (hasDataSource) {
                        return internalCreateStorage(actualDataModelName, actualStorageName, dataSourceName, StorageType.STAGING);
                    } else {
                        throw new IllegalArgumentException("Data source '" + dataSourceName + "' does not exist for STAGING.");
                    }
                } else {
                    throw new IllegalArgumentException("Storage '" + actualStorageName + "' does not support STAGING.");
                }
            case SYSTEM:
            default:
                throw new IllegalStateException("System storages are not created by this method.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create storage '" + actualStorageName + "' with data model '" + dataModelName
                    + "'.", e);
        }
    }

    public boolean supportStaging(String storageName) {
        final Server server = ServerContext.INSTANCE.get();
        final boolean supportStaging = server.hasDataSource(getDatasource(storageName), storageName, StorageType.STAGING);
        return !XSystemObjects.DC_UPDATE_PREPORT.getName().equalsIgnoreCase(storageName)
                && !XSystemObjects.DC_CROSSREFERENCING.getName().equalsIgnoreCase(storageName) && supportStaging;
    }

    @Override
    public StorageType getType(String name) {
        if (StringUtils.isEmpty(name)) {
            return StorageType.MASTER;
        }
        if (name.endsWith(STAGING_SUFFIX)) {
            return StorageType.STAGING;
        }
        if (name.equals(SYSTEM_STORAGE)) {
            return StorageType.SYSTEM;
        }
        return StorageType.MASTER;
    }

    private Storage internalCreateSystemStorage(String dataSourceName) {
        ClassRepository repository = new ClassRepository();
        // Parses ObjectPOJO classes
        Class[] objectsToParse = new Class[ObjectPOJO.OBJECT_TYPES.length];
        int i = 0;
        for (Object[] objects : ObjectPOJO.OBJECT_TYPES) {
            objectsToParse[i++] = (Class) objects[1];
        }
        repository.load(objectsToParse);
        // Additional POJOs
        repository.load(DroppedItemPOJO.class);
        // Load additional types (PROVISIONING...)
        String[] models = new String[] { "/com/amalto/core/initdb/data/datamodel/PROVISIONING", //$NON-NLS-1$
                "/com/amalto/core/initdb/data/datamodel/CONF", //$NON-NLS-1$
                "/com/amalto/core/initdb/data/datamodel/SearchTemplate" //$NON-NLS-1$
        };
        for (String model : models) {
            InputStream builtInStream = this.getClass().getResourceAsStream(model);
            if (builtInStream == null) {
                throw new RuntimeException("Built in model '" + model + "' cannot be found.");
            }
            try {
                DataModelPOJO modelPOJO = ObjectPOJO.unmarshal(DataModelPOJO.class, IOUtils.toString(builtInStream, "UTF-8")); //$NON-NLS-1$
                repository.load(new ByteArrayInputStream(modelPOJO.getSchema().getBytes("UTF-8"))); //$NON-NLS-1$
            } catch (Exception e) {
                throw new RuntimeException("Could not parse builtin data model '" + model + "'.", e);
            } finally {
                try {
                    builtInStream.close();
                } catch (IOException e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Error on internal model stream close().", e);
                    }
                }
            }
        }
        // Additional POJO handling
        for (String optionalClass : OPTIONAL_CLASSES) {
            try {
                // Keep the Class.forName() call (LicensePOJO might not be present).
                Class<?> clazz = Class.forName(optionalClass);
                repository.load(clazz);
            } catch (ClassNotFoundException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignore '" + optionalClass + "' parsing: class is not available.", e);
                }
            }
        }
        // Init system storage
        ServerContext instance = ServerContext.INSTANCE;
        DataSourceDefinition dataSource = instance.get().getDefinition(dataSourceName, SYSTEM_STORAGE);
        Storage storage = instance.getLifecycle().createStorage(SYSTEM_STORAGE, StorageType.SYSTEM, dataSource);
        storage.init(dataSource);
        storage.prepare(repository, Collections.<Expression> emptySet(), false, false);
        registerStorage(SYSTEM_STORAGE, storage);
        return storage;
    }

    // Returns null if storage can not be created (e.g. because of missing data source configuration).
    private Storage internalCreateStorage(String dataModelName, String storageName, String dataSourceName, StorageType storageType) {
        ServerContext instance = ServerContext.INSTANCE;
        String registeredStorageName = storageName;
        // May get request for "StorageName/Concept", but for SQL it does not make any sense.
        // See com.amalto.core.storage.StorageWrapper.createCluster()
        storageName = StringUtils.substringBefore(storageName, "/"); //$NON-NLS-1$
        dataModelName = StringUtils.substringBefore(dataModelName, "/"); //$NON-NLS-1$
        if (getRegisteredStorage(registeredStorageName, storageType) != null) {
            LOGGER.warn("Storage for '"
                    + storageName
                    + "' already exists. This is probably normal. If you want MDM to recreate it from scratch, delete the container and restart.");
            return get(storageName, storageType);
        }
        // Replace all container name, so re-read the configuration.
        DataSourceDefinition definition = instance.get().getDefinition(dataSourceName, storageName);
        if (!instance.get().hasDataSource(dataSourceName, storageName, storageType)) {
            LOGGER.warn("Can not initialize " + storageType + " storage for '" + storageName + "': data source '"
                    + dataSourceName + "' configuration is incomplete.");
            return null;
        }
        // Create storage
        Storage dataModelStorage = instance.getLifecycle().createStorage(storageName, storageType, definition);
        MetadataRepositoryAdmin metadataRepositoryAdmin = instance.get().getMetadataRepositoryAdmin();
        boolean hasDataModel = metadataRepositoryAdmin.exist(dataModelName);
        if (!hasDataModel) {
            throw new UnsupportedOperationException("Data model '" + dataModelName + "' must exist before container '"
                    + storageName + "' can be created.");
        }
        if (storageType == StorageType.STAGING && dataModelName.endsWith(STAGING_SUFFIX)) {
            dataModelName += STAGING_SUFFIX;
        }
        MetadataRepository metadataRepository = metadataRepositoryAdmin.get(dataModelName);
        Set<Expression> indexedExpressions = metadataRepositoryAdmin.getIndexedExpressions(dataModelName);
        try {
            dataModelStorage.prepare(metadataRepository, indexedExpressions, true, autoClean);
        } catch (Exception e) {
            throw new RuntimeException("Could not create storage for container '" + storageName + "' (" + storageType
                    + ") using data model '" + dataModelName + "'.", e);
        }
        switch (storageType) {
        case MASTER:
            registerStorage(registeredStorageName, dataModelStorage);
            break;
        case STAGING:
            registerStorage(registeredStorageName, new StagingStorage(dataModelStorage));
            break;
        default:
            throw new IllegalArgumentException("No support for storage type '" + storageType + "'.");
        }
        if (LOGGER.isDebugEnabled()) {
            StringBuilder capabilitiesAsString = new StringBuilder();
            int capabilities = dataModelStorage.getCapabilities();
            capabilitiesAsString.append(" TRANSACTION"); //$NON-NLS-1$
            if ((capabilities & Storage.CAP_TRANSACTION) == Storage.CAP_TRANSACTION) {
                capabilitiesAsString.append("(+)"); //$NON-NLS-1$
            } else {
                capabilitiesAsString.append("(-)"); //$NON-NLS-1$
            }
            capabilitiesAsString.append(" FULL TEXT"); //$NON-NLS-1$
            if ((capabilities & Storage.CAP_FULL_TEXT) == Storage.CAP_FULL_TEXT) {
                capabilitiesAsString.append("(+)"); //$NON-NLS-1$
            } else {
                capabilitiesAsString.append("(-)"); //$NON-NLS-1$
            }
            capabilitiesAsString.append(" INTEGRITY"); //$NON-NLS-1$
            if ((capabilities & Storage.CAP_INTEGRITY) == Storage.CAP_INTEGRITY) {
                capabilitiesAsString.append("(+)"); //$NON-NLS-1$
            } else {
                capabilitiesAsString.append("(-)"); //$NON-NLS-1$
            }
            LOGGER.debug("Storage capabilities:" + capabilitiesAsString);
        }
        return dataModelStorage;
    }

    public boolean exist(String storageName, StorageType storageType) {
        if (storageName.contains("/")) { //$NON-NLS-1$
            // Handle legacy scenarios where callers pass container names such as 'Product/ProductFamily'
            storageName = StringUtils.substringBefore(storageName, "/"); //$NON-NLS-1$
        }
        Storage storage;
        switch (storageType) {
        case STAGING:
            if (storageName.endsWith(STAGING_SUFFIX)) {
                storageName = StringUtils.substringBefore(storageName, STAGING_SUFFIX);
            }
            storage = getRegisteredStorage(storageName, StorageType.STAGING);
            break;
        case MASTER:
            storage = getRegisteredStorage(storageName, StorageType.MASTER);
            break;
        case SYSTEM:
            storage = getRegisteredStorage(SYSTEM_STORAGE, StorageType.SYSTEM);
            break;
        default:
            throw new NotImplementedException("No support for storage type '" + storageType + "'.");
        }
        return storage != null && storage.getType() == storageType;
    }

    public void close() {
        deleteAll(false);
        delete(SYSTEM_STORAGE, StorageType.SYSTEM, false);
    }

    @Override
    public String getDatasource(String storageName) {
        // This is not customized: in fact, there should be a way to customize storage -> datasource mapping (like
        // MDM container configuration).
        return DEFAULT_USER_DATA_SOURCE_NAME;
    }

    public Storage get(String storageName, StorageType type) {
        // Remove #STAGING (if any) at end of storage name
        String cleanedStorageName = StringUtils.substringBeforeLast(storageName, STAGING_SUFFIX);
        // Look up for already registered storages
        Storage storage = getRegisteredStorage(cleanedStorageName, type);
        Map<String, XSystemObjects> xDataClustersMap = XSystemObjects.getXSystemObjects(XObjectType.DATA_CLUSTER);
        if (getRegisteredStorage(SYSTEM_STORAGE, StorageType.SYSTEM) != null
                && !XSystemObjects.DC_UPDATE_PREPORT.getName().equals(cleanedStorageName)
                && !XSystemObjects.DC_CROSSREFERENCING.getName().equals(cleanedStorageName)
                && (XSystemObjects.isXSystemObject(xDataClustersMap, cleanedStorageName) || cleanedStorageName
                        .startsWith("amaltoOBJECTS"))) { //$NON-NLS-1$
            return getRegisteredStorage(SYSTEM_STORAGE, StorageType.SYSTEM);
        }
        if (storage == null) {
            // May get request for "StorageName/Concept" (especially in case of XML DB -> SQL migration).
            storage = getRegisteredStorage(StringUtils.substringBefore(cleanedStorageName, "/"), type); //$NON-NLS-1$
        }
        if (storage == null) {
            LOGGER.info("Container '" + cleanedStorageName + "' does not exist.");
            // If data model xsd exists on server, create storage
            MetadataRepositoryAdmin metadataRepositoryAdmin = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin();
            if (metadataRepositoryAdmin.exist(storageName)) {
                String dataSourceName = getDatasource(cleanedStorageName);
                storage = create(cleanedStorageName, cleanedStorageName, type, dataSourceName);
            }
        }
        return storage;
    }

    private void registerStorage(String storageName, Storage storage) {
        MultiKeyMap multiKeyMap = storages.get(storageName);
        if (multiKeyMap == null) {
            multiKeyMap = new MultiKeyMap();
            storages.put(storageName, multiKeyMap);
        }
        multiKeyMap.put(StringUtils.EMPTY, storage.getType(), storage);
    }

    private Storage getRegisteredStorage(String storageName, StorageType storageType) {
        return (Storage) storages.get(storageName).get(StringUtils.EMPTY, storageType);
    }

    private static class StorageMap extends HashMap<String, MultiKeyMap> {

        @Override
        public MultiKeyMap get(Object o) {
            MultiKeyMap value = super.get(o);
            if (value == null) {
                value = new MultiKeyMap();
                super.put((String) o, value);
            }
            return value;
        }
    }
}
