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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource;

class DB2StorageInitializer implements StorageInitializer {

    private static final Logger LOGGER = Logger.getLogger(DB2StorageInitializer.class);

    @Override
    public boolean supportInitialization(Storage storage) {
        return true;
    }

    public boolean isInitialized(Storage storage) {
        try {
            RDBMSDataSource dataSource = getDataSource(storage);
            Driver driver = (Driver) Class.forName(dataSource.getDriverClassName()).newInstance();
            Connection connection = driver.connect(dataSource.getConnectionURL() + "?user=" + dataSource.getUserName() + "&password=" + dataSource.getPassword(), new Properties());//$NON-NLS-1$ //$NON-NLS-2$
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
            Driver driver = (Driver) Class.forName(dataSource.getDriverClassName()).newInstance();
            Connection connection = driver.connect(dataSource.getInitConnectionURL() + "?user=" + dataSource.getInitUserName() + "&password=" + dataSource.getInitPassword(), new Properties());  //$NON-NLS-1$ //$NON-NLS-2$
            try {
                if (connection != null) {
                    Statement statement = connection.createStatement();
                    try {
                        statement.execute("CREATE DATABASE " + dataSource.getDatabaseName() + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                    } catch (SQLException e) {
                        // Assumes database is already created.
                        LOGGER.warn("Exception occurred during CREATE DATABASE statement.", e);
                    } finally {
                        statement.close();
                    }
                }
            } finally {
                if (connection != null) { // DB2 may return null if not reachable (not very compliant with Javadoc).
                    connection.close();
                }
            }
            LOGGER.info("DB2 database " + dataSource.getDatabaseName() + " has been prepared.");
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred during initialization of DB2 database", e);
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
