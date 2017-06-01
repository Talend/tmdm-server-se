/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import org.apache.log4j.Logger;

import java.sql.*;

class SQLServerStorageInitializer implements StorageInitializer {

    private static final Logger LOGGER = Logger.getLogger(SQLServerStorageInitializer.class);

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
            Connection connection = DriverManager.getConnection(dataSource.getInitConnectionURL(), dataSource.getInitUserName(), dataSource.getInitPassword());
            try {
                Statement statement = connection.createStatement();
                try {
                    statement.execute("USE master;"); //$NON-NLS-1$
                    statement.execute("CREATE DATABASE " + dataSource.getDatabaseName() + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                } catch (SQLException e) {
                    // Assumes database is already created.
                    LOGGER.warn("Exception occurred during CREATE DATABASE statement.", e);
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }
            LOGGER.info("SQL Server database " + dataSource.getDatabaseName() + " has been prepared.");
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred during initialization of SQL Server database", e);
        }
    }

    private RDBMSDataSource getDataSource(Storage storage) {
        DataSource storageDataSource = storage.getDataSource();
        if (!(storageDataSource instanceof RDBMSDataSource)) {
            throw new IllegalArgumentException("Storage to initialize does not seem to be a RDBMS storage.");
        }

        RDBMSDataSource dataSource = (RDBMSDataSource) storageDataSource;
        if (!dataSource.hasInit()) {
            throw new IllegalArgumentException("Data source '" + dataSource.getName() + "' does not define initialization information.");
        }
        return dataSource;
    }
}
