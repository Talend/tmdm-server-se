// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package com.amalto.core.server;

import com.amalto.core.ejb.DroppedItemPOJO;
import com.amalto.core.ejb.ObjectPOJO;
import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.query.user.Expression;
import org.apache.commons.io.IOUtils;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import com.amalto.core.objects.datamodel.ejb.DataModelPOJO;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceFactory;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.hibernate.HibernateStorage;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import java.io.*;
import java.util.*;

public class StorageAdminImpl implements StorageAdmin {

    private static final Logger LOGGER = Logger.getLogger(StorageAdminImpl.class);

    private static final String JCA_ADAPTER_DATA_MODEL = "jcaAdapter.xsd"; //$NON-NLS-1$

    /**
     * Default datasource name to be used for user/master data (from datasources configuration content).
     */
    private static final String DEFAULT_USER_DATA_SOURCE_NAME = MDMConfiguration.getConfiguration().getProperty("db.default.datasource", "RDBMS-1"); //$NON-NLS-1$ //$NON-NLS-2$

    // Default value is "false" (meaning the storage will not remove existing data).
    private static final boolean autoClean = Boolean.valueOf(MDMConfiguration.getConfiguration().getProperty("db.autoClean", "false")); //$NON-NLS-1$ //$NON-NLS-2$

    private static final String LICENSE_POJO_CLASS = "com.amalto.core.util.license.LicensePOJO"; //$NON-NLS-1$

    private static final String VERSIONING_POJO_CLASS = "com.amalto.core.objects.versioning.ejb.VersioningSystemPOJO"; //$NON-NLS-1$

    private static final String[] OPTIONAL_CLASSES = new String[] {LICENSE_POJO_CLASS, VERSIONING_POJO_CLASS};

    private final Map<String, Map<String, Storage>> storages = new StorageMap();

    public String[] getAll(String revisionID) {
        if (isHead(revisionID)) {
            revisionID = null;
        }
        Set<String> allStorageNames = new HashSet<String>();
        for (Map.Entry<String, Map<String, Storage>> currentStorage : storages.entrySet()) {
            if (currentStorage.getValue().containsKey(revisionID)) {
                allStorageNames.add(currentStorage.getKey());
            }
        }
        return allStorageNames.toArray(new String[allStorageNames.size()]);
    }

    public void delete(String revisionID, String storageName, boolean dropExistingData) {
        Storage storage = getRegisteredStorage(storageName, revisionID);
        if (storage == null) {
            LOGGER.warn("Storage '" + storageName + "' does not exist.");
            return;
        }
        ServerContext.INSTANCE.getLifecycle().destroyStorage(storage, dropExistingData);
        storages.get(storageName).remove(revisionID);
        if (storages.get(storageName).isEmpty()) {
            storages.remove(storageName);
        }
    }

    public void deleteAll(String revisionID, boolean dropExistingData) {
        for (String clusterName : new HashSet<String>(storages.keySet())) {
            delete(revisionID, clusterName, dropExistingData);
        }
    }

    public Storage create(String dataModelName, String storageName, String dataSourceName, String revisionId) {
        if (MDMConfiguration.getConfiguration().get(DataSourceFactory.DB_DATASOURCES) == null) {
            LOGGER.warn("Configuration does not allow creation of SQL storage for '" + dataModelName + "'.");
            return null;
        }
        if (SYSTEM_STORAGE.equals(storageName)) {
            return internalCreateSystemStorage(dataSourceName);
        }
        String actualStorageName = StringUtils.substringBefore(storageName, STAGING_SUFFIX);
        String actualDataModelName = StringUtils.substringBefore(dataModelName, STAGING_SUFFIX);
        try {
            Storage masterDataModelStorage = internalCreateStorage(actualDataModelName, actualStorageName, dataSourceName, StorageType.MASTER, revisionId);
            if (supportStaging(actualStorageName)) {
                boolean hasDataSource = ServerContext.INSTANCE.get().hasDataSource(dataSourceName, actualStorageName, StorageType.STAGING);
                if (hasDataSource) {
                    internalCreateStorage(actualDataModelName, actualStorageName, dataSourceName, StorageType.STAGING, revisionId);
                }
            }
            return masterDataModelStorage;
        } catch (Exception e) {
            throw new RuntimeException("Could not create storage '" + actualStorageName + "' with data model '" + dataModelName + "'.", e);
        }
    }

    public boolean supportStaging(String storageName) {
        return !XSystemObjects.DC_UPDATE_PREPORT.getName().equalsIgnoreCase(storageName)
                && !XSystemObjects.DC_CROSSREFERENCING.getName().equalsIgnoreCase(storageName);
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
        // Loads definition for JCAAdapters (Logging, SVN...)
        InputStream stream = this.getClass().getResourceAsStream(JCA_ADAPTER_DATA_MODEL);
        if (stream == null) {
            throw new IllegalStateException("Could not find resource '" + JCA_ADAPTER_DATA_MODEL + "' in classpath.");
        }
        repository.load(stream);
        // Load additional types (PROVISIONING...)
        String[] models = new String[]{
                "/com/amalto/core/initdb/data/datamodel/PROVISIONING", //$NON-NLS-1$
                "/com/amalto/core/initdb/data/datamodel/CONF", //$NON-NLS-1$
                "/com/amalto/core/initdb/data/datamodel/Reporting", //$NON-NLS-1$
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
                    LOGGER.debug("Ignore LicencePOJO parsing. Not running enterprise edition.", e);
                }
            }
        }
        // Init system storage
        Storage storage = new HibernateStorage(SYSTEM_STORAGE, StorageType.SYSTEM);
        ServerContext instance = ServerContext.INSTANCE;
        DataSource dataSource = instance.get().getDataSource(dataSourceName, SYSTEM_STORAGE, StorageType.SYSTEM);
        storage.init(dataSource);
        storage.prepare(repository, Collections.<Expression>emptySet(), false, false);
        registerStorage(SYSTEM_STORAGE, null, storage);
        return storage;
    }

    // Returns null if storage can not be created (e.g. because of missing data source configuration).
    private Storage internalCreateStorage(String dataModelName, String storageName, String dataSourceName, StorageType storageType, String revisionId) {
        ServerContext instance = ServerContext.INSTANCE;
        DataSource dataSource = instance.get().getDataSource(dataSourceName, storageName, revisionId, storageType);
        String registeredStorageName = storageName;
        if (dataSource instanceof RDBMSDataSource) {
            // May get request for "StorageName/Concept", but for SQL it does not make any sense.
            // See com.amalto.core.storage.StorageWrapper.createCluster()
            storageName = StringUtils.substringBefore(storageName, "/"); //$NON-NLS-1$
            dataModelName = StringUtils.substringBefore(dataModelName, "/"); //$NON-NLS-1$
            if (storageType == StorageType.STAGING) {
                if (!dataModelName.endsWith(STAGING_SUFFIX)) {
                    dataModelName += STAGING_SUFFIX;
                }
                if (!storageName.endsWith(STAGING_SUFFIX)) {
                    registeredStorageName = storageName + STAGING_SUFFIX;
                }
            }
            if (getRegisteredStorage(registeredStorageName, revisionId) != null) {
                LOGGER.warn("Storage for '" + storageName + "' already exists. It needs to be deleted before it can be recreated.");
                return get(storageName, revisionId);
            }
            // Replace all container name, so re-read the configuration.
            dataSource = instance.get().getDataSource(dataSourceName, storageName, revisionId, storageType);
        }
        if (!instance.get().hasDataSource(dataSourceName, storageName, storageType)) {
            LOGGER.warn("Can not initialize " + storageType + " storage for '" + storageName + "': data source '" + dataSourceName + "' configuration is incomplete.");
            return null;
        }
        // Create storage
        if (XSystemObjects.DC_UPDATE_PREPORT.getName().equals(storageName) && dataSource instanceof RDBMSDataSource) {
            RDBMSDataSource previousDataSource = (RDBMSDataSource) dataSource;
            dataSource = new RDBMSDataSource(previousDataSource) {
                @Override
                public boolean supportFullText() {
                    if (LOGGER.isDebugEnabled() && super.supportFullText()) {
                        LOGGER.debug("Disabling full text for update report storage.");
                    }
                    return false;
                }
            };
        }
        Storage dataModelStorage = instance.getLifecycle().createStorage(storageName, dataSourceName, storageType);
        dataModelStorage.init(dataSource);
        MetadataRepositoryAdmin metadataRepositoryAdmin = instance.get().getMetadataRepositoryAdmin();
        boolean hasDataModel = metadataRepositoryAdmin.exist(dataModelName);
        if (!hasDataModel) {
            throw new UnsupportedOperationException("Data model '" + dataModelName + "' must exist before container '" + storageName + "' can be created.");
        }
        MetadataRepository metadataRepository = metadataRepositoryAdmin.get(dataModelName);
        Set<Expression> indexedExpressions = metadataRepositoryAdmin.getIndexedExpressions(dataModelName);
        try {
            dataModelStorage.prepare(metadataRepository, indexedExpressions, hasDataModel, autoClean);
        } catch (Exception e) {
            throw new RuntimeException("Could not create storage for container '" + storageName + "' (" + storageType + ") using data model '" + dataModelName + "'.", e);
        }
        switch (storageType) {
            case MASTER:
            case STAGING:
                registerStorage(registeredStorageName, revisionId, dataModelStorage);
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

    public boolean exist(String revision, String storageName, StorageType storageType) {
        if (storageName.contains("/")) { //$NON-NLS-1$
            // Handle legacy scenarios where callers pass container names such as 'Product/ProductFamily'
            storageName = StringUtils.substringBefore(storageName, "/"); //$NON-NLS-1$
        }
        Storage storage;
        switch (storageType) {
            case STAGING:
                if (!storageName.endsWith(STAGING_SUFFIX)) {
                    storage = getRegisteredStorage(storageName + STAGING_SUFFIX, revision);
                } else {
                    storage = getRegisteredStorage(storageName, revision);
                }
                break;
            case MASTER:
                storage = getRegisteredStorage(storageName, revision);
                break;
            case SYSTEM:
                storage = getRegisteredStorage(SYSTEM_STORAGE, null);
                break;
            default:
                throw new NotImplementedException("No support for storage type '" + storageType + "'.");
        }
        if (storage == null) {
            LOGGER.info("Container '" + storageName + "' does not exist in revision '" + revision + "', creating it.");
            String dataSourceName = getDatasource(storageName);
            storage = create(storageName, storageName, dataSourceName, revision);
        }
        return storage != null && storage.getType() == storageType;
    }

    private static boolean isHead(String revision) {
        return revision == null || "HEAD".equals(revision) || revision.isEmpty(); //$NON-NLS-1$
    }

    public void close() {
        deleteAll(null, false);
    }

    @Override
    public String getDatasource(String storageName) {
        // This is not customized: in fact, there should be a way to customize storage -> datasource mapping (like
        // MDM container configuration).
        return DEFAULT_USER_DATA_SOURCE_NAME;
    }

    public Storage get(String storageName, String revisionId) {
        Storage storage = getRegisteredStorage(storageName, revisionId);
        Map<String, XSystemObjects> xDataClustersMap = XSystemObjects.getXSystemObjects(XObjectType.DATA_CLUSTER);
        if (getRegisteredStorage(SYSTEM_STORAGE, null) != null
                && !XSystemObjects.DC_UPDATE_PREPORT.getName().equals(storageName)
                && !XSystemObjects.DC_CROSSREFERENCING.getName().equals(storageName)
                && (XSystemObjects.isXSystemObject(xDataClustersMap, storageName)
                    || storageName.startsWith("amaltoOBJECTS"))) { //$NON-NLS-1$
            return getRegisteredStorage(SYSTEM_STORAGE, null);
        }
        if (storage == null) {
            // May get request for "StorageName/Concept", but for SQL it does not make any sense.
            storageName = StringUtils.substringBefore(storageName, "/"); //$NON-NLS-1$
            storage = getRegisteredStorage(storageName, revisionId);
            if (storage != null && !(storage.getDataSource() instanceof RDBMSDataSource)) {
                throw new IllegalStateException("Expected a SQL storage for '" + storageName + "' but got a '" + storage.getClass().getName() + "'.");
            }
        }
        if (storage == null && supportStaging(storageName) && !isHead(revisionId)) {
            LOGGER.info("Container '" + storageName + "' does not exist in revision '" + revisionId + "', creating it.");
            String dataSourceName = getDatasource(storageName);
            storage = create(storageName, storageName, dataSourceName, revisionId);
        }
        return storage;
    }

    @Override
    public Collection<Storage> get(String storageName) {
        return storages.get(storageName).values();
    }

    private void registerStorage(String storageName, String revisionId, Storage storage) {
        if (isHead(revisionId)) {
            storages.get(storageName).put(null, storage);
        } else {
            storages.get(storageName).put(revisionId, storage);
        }
    }

    private Storage getRegisteredStorage(String storageName, String revisionId) {
        if (isHead(revisionId)) {
            return storages.get(storageName).get(null);
        } else {
            return storages.get(storageName).get(revisionId);
        }
    }

    private static class StorageMap extends HashMap<String, Map<String, Storage>> {
        @Override
        public Map<String, Storage> get(Object o) {
            Map<String, Storage> value = super.get(o);
            if (value == null) {
                value = new HashMap<String, Storage>();
                super.put((String) o, value);
            }
            return value;
        }
    }
}
