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

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import com.amalto.core.storage.transaction.TransactionManager;
import org.apache.commons.lang.NotImplementedException;

import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.datasource.DataSourceFactory;

@SuppressWarnings("nls")
public class MockServer implements Server {

    private final DataSourceFactory dataSourceFactory = DataSourceFactory.getInstance();

    private final MDMTransactionManager transactionManager;

    private StorageAdmin storageAdmin;

    private MetadataRepositoryAdmin metadataRepositoryAdmin;

    private static final String DATASOURCES_FILE = "com/amalto/core/server/datasources-test.xml";

    public MockServer() {
        transactionManager = new MDMTransactionManager();
    }

    static String getDatasourcesFilePath() {
        URL url = MockServer.class.getClassLoader().getResource(DATASOURCES_FILE);
        junit.framework.Assert.assertNotNull(url);
        return new File(url.getFile()).getAbsolutePath();
    }

    static InputStream getDatasourcesInputStream() {
        InputStream is = MockServer.class.getClassLoader().getResourceAsStream(DATASOURCES_FILE);
        junit.framework.Assert.assertNotNull(is);
        return is;
    }

    @Override
    public DataSourceDefinition getDefinition(String dataSourceName, String container) {
        return dataSourceFactory.getDataSource(getDatasourcesInputStream(), dataSourceName, container);
    }

    @Override
    public boolean hasDataSource(String dataSourceName, String container, StorageType type) {
        boolean isDataSourceDefinitionPresent = dataSourceFactory.hasDataSource(getDatasourcesInputStream(), dataSourceName);
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
        return isDataSourceDefinitionPresent;
    }

    @Override
    public StorageAdmin getStorageAdmin() {
        if (storageAdmin == null) {
            storageAdmin = ServerContext.INSTANCE.getLifecycle().createStorageAdmin();
        }
        return storageAdmin;
    }

    @Override
    public MetadataRepositoryAdmin getMetadataRepositoryAdmin() {
        if (metadataRepositoryAdmin == null) {
            metadataRepositoryAdmin = ServerContext.INSTANCE.getLifecycle().createMetadataRepositoryAdmin();
        }
        return metadataRepositoryAdmin;
    }

    @Override
    public void close() {
        ServerLifecycle lifecycle = ServerContext.INSTANCE.getLifecycle();
        if (metadataRepositoryAdmin != null) {
            lifecycle.destroyMetadataRepositoryAdmin(metadataRepositoryAdmin);
        }
        if (storageAdmin != null) {
            lifecycle.destroyStorageAdmin(storageAdmin);
        }
    }

    @Override
    public void init() {
        // nothing to do
    }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
}
