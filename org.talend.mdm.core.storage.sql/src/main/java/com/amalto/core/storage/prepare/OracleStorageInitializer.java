/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.prepare;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource;

class OracleStorageInitializer implements StorageInitializer {

    private static final Logger LOGGER = Logger.getLogger(OracleStorageInitializer.class);

    @Override
    public boolean supportInitialization(Storage storage) {
        return true;
    }

    public boolean isInitialized(Storage storage) {
        try {
            RDBMSDataSource dataSource = getDataSource(storage);
            Class.forName(dataSource.getDriverClassName());
            Connection connection = DriverManager.getConnection(dataSource.getConnectionURL(), dataSource.getUserName(), dataSource.getPassword());
            connection.close();
            return true;
        } catch (SQLException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void initialize(Storage storage) {
        try {
            RDBMSDataSource dataSource = getDataSource(storage);
            Class.forName(dataSource.getDriverClassName());
            Connection connection = DriverManager.getConnection(dataSource.getConnectionURL(), dataSource.getUserName(), dataSource.getPassword());
            try {
                Statement statement = connection.createStatement();
                try {
                    statement.execute("grant connect, dba to " + dataSource.getUserName()); //$NON-NLS-1$
                    statement.execute("alter user " + dataSource.getUserName() + " account unlock"); //$NON-NLS-1$ //$NON-NLS-2$
                } catch (SQLException e) {
                    // Assumes database is already created.
                    LOGGER.warn("Exception occurred during CREATE USER statement.", e);
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }
            LOGGER.info("Oracle database " + dataSource.getDatabaseName() + " has been prepared.");
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred during initialization of Oracle database", e);
        }
    }

    private RDBMSDataSource getDataSource(Storage storage) {
        DataSource storageDataSource = storage.getDataSource();
        if (!(storageDataSource instanceof RDBMSDataSource)) {
            throw new IllegalArgumentException("Storage to initialize does not seem to be a RDBMS storage.");
        }

        return (RDBMSDataSource) storageDataSource;
    }
}
