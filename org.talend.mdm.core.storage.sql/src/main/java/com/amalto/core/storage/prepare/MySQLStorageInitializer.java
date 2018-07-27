/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
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

class MySQLStorageInitializer implements StorageInitializer {

    private static final Logger LOGGER = Logger.getLogger(MySQLStorageInitializer.class);

    @Override
    public boolean supportInitialization(Storage storage) {
        return true;
    }

    @Override
    public boolean isInitialized(Storage storage) {
        try {
            RDBMSDataSource dataSource = getDataSource(storage);
            Driver driver = (Driver) Class.forName(dataSource.getDriverClassName()).newInstance();
            Connection connection = driver.connect(dataSource.getConnectionURL()
                    + "?user=" + dataSource.getUserName() + "&password=" + dataSource.getPassword(), new Properties());//$NON-NLS-1$ //$NON-NLS-2$
            connection.close();
            return true;
        } catch (SQLException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(Storage storage) {
        try {
            RDBMSDataSource dataSource = getDataSource(storage);
            Driver driver = (Driver) Class.forName(dataSource.getDriverClassName()).newInstance();

            Properties properties = new Properties();
            properties.put("user", dataSource.getInitUserName()); //$NON-NLS-1$
            properties.put("password", dataSource.getInitPassword()); //$NON-NLS-1$

            Connection connection = driver.connect(dataSource.getInitConnectionURL(), properties);
            try {
                Statement statement = connection.createStatement();
                try {
                    statement.execute("CREATE DATABASE IF NOT EXISTS " + dataSource.getDatabaseName() //$NON-NLS-1$
                            + " DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;"); //$NON-NLS-1$
                } catch (SQLException e) {
                    // Assumes database is already created.
                    LOGGER.warn("Exception occurred during CREATE DATABASE statement.", e);
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }
            LOGGER.info("MySQL database " + dataSource.getDatabaseName() + " has been prepared.");
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred during initialization of MySQL database", e);
        }
    }

    private RDBMSDataSource getDataSource(Storage storage) {
        DataSource storageDataSource = storage.getDataSource();
        if (!(storageDataSource instanceof RDBMSDataSource)) {
            throw new IllegalArgumentException("Storage to initialize does not seem to be a RDBMS storage.");
        }

        RDBMSDataSource dataSource = (RDBMSDataSource) storageDataSource;
        if (!dataSource.hasInit()) {
            throw new IllegalArgumentException("Data source '" + dataSource.getName()
                    + "' does not define initialization information.");
        }
        return dataSource;
    }
}
