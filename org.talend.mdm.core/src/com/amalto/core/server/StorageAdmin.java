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

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;

/**
 * API for the {@link Storage} administration. There's usually a 1-to-1 mapping between a Storage and a MDM data
 * cluster. However this might not be true for System objects (stored in the amaltoOBJECTS* data clusters), where all
 * system objects are stored in the same Storage. But this is be transparent to the API user (calling code should just
 * not expect a 1-1 mapping between Storage instance and data cluster).
 */
public interface StorageAdmin {

    String STAGING_SUFFIX = "#STAGING"; //$NON-NLS-1

    /**
     * Internal/System storage name: this is the name for the Storage instance that stores all MDM internal
     * configuration.
     */
    String SYSTEM_STORAGE = "__SYSTEM"; //$NON-NLS-1

    /**
     * @return An array that contains all storage names. Returns an empty array if there's currently no storage.
     */
    String[] getAll();

    /**
     * Removes a storage from MDM: this is what happens when a user deletes a container. This method does not always
     * removes container's data (this depends on the value of <code>dropExistingData</code>).
     * 
     * @param storageName A storage name.
     * @param type The storage {@link com.amalto.core.storage.StorageType type} (Staging, Master...).
     * @param dropExistingData <code>true</code> removes also storage data during operation.
     */
    void delete(String storageName, StorageType type, boolean dropExistingData);

    /**
     * Removes a storage from MDM (MASTER, STAGGING, SYSTEM): this is what happens when a user deletes a container. This
     * method does not always removes container's data (this depends on the value of <code>dropExistingData</code>).
     * 
     * @param storageName A storage name.
     * @param dropExistingData <code>true</code> removes also storage data during operation.
     */
    void delete(String storageName, boolean dropExistingData);

    /**
     * Deletes all storages for a given revision. This code is equivalent to: <code>
     * String[] names = storageAdmin.getAll(revisionId);
     * for(String name : names) {
     * storageAdmin.delete(revisionId, dropExistingData);
     * }
     * </code>
     *
     * @param dropExistingData <code>true</code> removes also storage data during operation.
     */
    void deleteAll(boolean dropExistingData);

    /**
     * Creates a new storage based on parameter information. If a storage already exists, this return the previously
     * created instance.
     *
     * @param dataModelName A data model name (one managed by
     * {@link com.amalto.core.server.MetadataRepositoryAdmin#get(String)}).
     * @param storageName A non-null, non-empty identifier for the new storage instance.
     * @param type The storage {@link com.amalto.core.storage.StorageType type} (Staging, Master...).
     * @param dataSourceName A datasource name (a data source that must exists in datasources configuration file).
     */
    Storage create(String dataModelName, String storageName, StorageType type, String dataSourceName);

    /**
     * @param storageName A non-null, non-empty identifier for the storage instance.
     * @param storageType Indicates whether caller checks for {@link com.amalto.core.storage.StorageType#MASTER} or
     * {@link com.amalto.core.storage.StorageType#STAGING}. @return Returns <code>true</code> if storage already exist
     * for this name, revision and type.
     */
    boolean exist(String storageName, StorageType storageType);

    /**
     * To be closed on MDM server shutdown: this method ensures all managed storages are correctly closed.
     */
    void close();

    /**
     * @param storageName A {@link com.amalto.core.storage.Storage} name.
     * @return The datasource name that should be used with storage <code>storageName</code>.
     */
    String getDatasource(String storageName);

    /**
     * @param storageName A {@link com.amalto.core.storage.Storage} name.
     * @param type The storage {@link com.amalto.core.storage.StorageType type} (Staging, Master...).
     * @return A previously created {@link Storage} or <code>null</code> if no storage was previously created.
     * @see #create(String, String, com.amalto.core.storage.StorageType, String)
     */
    Storage get(String storageName, StorageType type);

    /**
     * Returns if <code>storageName</code> is allowed to get a staging area (for instance Update Report and
     * Crossreferencing are not allowed to).
     * 
     * @param storageName A storage name.
     * @return <code>true</code> if <code>storageName</code> can have a staging area, <code>false</code> otherwise.
     */
    boolean supportStaging(String storageName);

    /**
     * @param name A data model or a data container name.
     * @return The {@link com.amalto.core.storage.StorageType type} of storage to be associated with the name.
     */
    StorageType getType(String name);
}
