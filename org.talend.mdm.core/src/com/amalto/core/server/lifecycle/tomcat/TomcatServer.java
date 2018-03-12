/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.server.lifecycle.tomcat;

import com.amalto.core.server.*;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.datasource.DataSourceFactory;
import com.amalto.core.storage.transaction.TransactionManager;
import org.apache.commons.lang.NotImplementedException;

class TomcatServer implements Server {
    private final DataSourceFactory dataSourceFactory = DataSourceFactory.getInstance();

    private MDMTransactionManager transactionManager;

    private StorageAdmin storageAdmin;

    private MetadataRepositoryAdmin metadataRepositoryAdmin;

    TomcatServer() {
    }

    @Override
    public boolean hasDataSource(String dataSourceName, String container, StorageType type) {
        boolean isDataSourceDefinitionPresent = dataSourceFactory.hasDataSource(dataSourceName);
        if (isDataSourceDefinitionPresent) {
            DataSourceDefinition dataSource = dataSourceFactory.getDataSource(dataSourceName, container);
            switch (type) {
            case MASTER:
                return dataSource.getMaster() != null;
            case STAGING:
                return dataSource.getStaging() != null;
            default:
                throw new NotImplementedException("Not supported: " + type);
            }
        }
        return false;
    }

    @Override
    public DataSourceDefinition getDefinition(String dataSourceName, String container) {
        return dataSourceFactory.getDataSource(dataSourceName, container);
    }

    public StorageAdmin getStorageAdmin() {
        if (storageAdmin == null) {
            ServerLifecycle lifecycle = ServerContext.INSTANCE.getLifecycle();
            storageAdmin = lifecycle.createStorageAdmin();
        }
        return storageAdmin;
    }

    public MetadataRepositoryAdmin getMetadataRepositoryAdmin() {
        if (metadataRepositoryAdmin == null) {
            ServerLifecycle lifecycle = ServerContext.INSTANCE.getLifecycle();
            metadataRepositoryAdmin = lifecycle.createMetadataRepositoryAdmin();
        }
        return metadataRepositoryAdmin;
    }

    public void close() {
        ServerLifecycle lifecycle = ServerContext.INSTANCE.getLifecycle();
        lifecycle.destroyStorageAdmin(storageAdmin);
        lifecycle.destroyMetadataRepositoryAdmin(metadataRepositoryAdmin);
    }

    public void init() {
        transactionManager = new MDMTransactionManager();
        transactionManager.init();
    }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
}
