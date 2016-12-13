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
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;

/**
 *
 */
public interface ServerLifecycle {
    
    Server createServer();
    
    void destroyServer(Server server);

    StorageAdmin createStorageAdmin();

    void destroyStorageAdmin(StorageAdmin storageAdmin);

    MetadataRepositoryAdmin createMetadataRepositoryAdmin();

    void destroyMetadataRepositoryAdmin(MetadataRepositoryAdmin metadataRepositoryAdmin);

    Storage createStorage(String storageName, StorageType storageType, DataSourceDefinition definition);

    Storage createTemporaryStorage(DataSource dataSource, StorageType storageType);

    void destroyStorage(Storage storage, boolean dropExistingData);
}
