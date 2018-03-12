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

package com.amalto.core.storage.prepare;

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

public class JDBCStorageInitializer implements StorageInitializer {

    private static final Logger LOGGER = Logger.getLogger(JDBCStorageInitializer.class);

    @Override
    public boolean supportInitialization(Storage storage) {
        try {
            StorageInitializer initializer = getInitializer(storage);
            initializer.isInitialized(storage);
            return true;
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exception occurred during supportInitialization().", e);
            }
            return false;
        }
    }

    public boolean isInitialized(Storage storage) {
        try {
            StorageInitializer jdbcStorageInitializer = getInitializer(storage);
            return jdbcStorageInitializer.isInitialized(storage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void initialize(Storage storage) {
        try {
            StorageInitializer jdbcStorageInitializer = getInitializer(storage);
            jdbcStorageInitializer.initialize(storage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private StorageInitializer getInitializer(Storage storage) {
        DataSource storageDataSource = storage.getDataSource();
        if (!(storageDataSource instanceof RDBMSDataSource)) {
            throw new IllegalArgumentException("Storage to initialize does not seem to be a RDBMS storage.");
        }

        RDBMSDataSource dataSource = (RDBMSDataSource) storageDataSource;
        RDBMSDataSource.DataSourceDialect dialect = dataSource.getDialectName();
        StorageInitializer jdbcStorageInitializer;
        switch (dialect) {
            case MYSQL:
                jdbcStorageInitializer = new MySQLStorageInitializer();
                break;
            case H2:
                jdbcStorageInitializer = new H2StorageInitializer();
                break;
            case ORACLE_10G:
                jdbcStorageInitializer = new OracleStorageInitializer();
                break;
            case SQL_SERVER:
                jdbcStorageInitializer = new SQLServerStorageInitializer();
                break;
            case POSTGRES:
                jdbcStorageInitializer = new PostgresStorageInitializer();
                break;
            case DB2:
                jdbcStorageInitializer = new DB2StorageInitializer();
                break;
            default:
                throw new NotImplementedException("Can not initialize storages based on dialect '" + dialect + "'");
        }
        return new FullTextIndexCleaner(jdbcStorageInitializer);
    }

}
