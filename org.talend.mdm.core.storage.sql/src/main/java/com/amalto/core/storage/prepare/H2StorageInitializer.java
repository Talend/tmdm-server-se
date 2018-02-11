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
import java.sql.DriverManager;

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource;

class H2StorageInitializer implements StorageInitializer {

    @Override
    public boolean supportInitialization(Storage storage) {
        return true;
    }

    @Override
    public boolean isInitialized(Storage storage) {
        try {
            DataSource storageDataSource = storage.getDataSource();
            if (!(storageDataSource instanceof RDBMSDataSource)) {
                throw new IllegalArgumentException("Storage to clean does not seem to be a RDBMS storage.");
            }
            RDBMSDataSource dataSource = (RDBMSDataSource) storageDataSource;
            Class.forName(dataSource.getDriverClassName()).newInstance();
            // On H2, opening a connection implicitly creates the database files, adds "IFEXISTS" to throw an exception
            // if doesn't exist.
            Connection connection = DriverManager.getConnection(dataSource.getConnectionURL() + ";IFEXISTS=TRUE", //$NON-NLS-1$
                    dataSource.getUserName(),
                    dataSource.getPassword());
            connection.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void initialize(Storage storage) {
        try {
            DataSource storageDataSource = storage.getDataSource();
            if (!(storageDataSource instanceof RDBMSDataSource)) {
                throw new IllegalArgumentException("Storage to clean does not seem to be a RDBMS storage.");
            }

            RDBMSDataSource dataSource = (RDBMSDataSource) storageDataSource;
            Class.forName(dataSource.getDriverClassName()).newInstance();

            // On H2, opening a connection implicitly creates the database files.
            Connection connection = DriverManager.getConnection(dataSource.getConnectionURL(), dataSource.getUserName(),
                    dataSource.getPassword());
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred during initialization of H2 database", e);
        }
    }
}
