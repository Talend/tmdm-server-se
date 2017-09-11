/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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
import java.sql.Statement;

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource;

class H2StorageCleaner implements StorageCleaner {

    private final StorageCleaner next;

    public H2StorageCleaner(StorageCleaner next) {
        this.next = next;
    }

    @Override
    public void clean(Storage storage) {
        try {
            DataSource storageDataSource = storage.getDataSource();
            if (!(storageDataSource instanceof RDBMSDataSource)) {
                throw new IllegalArgumentException("Storage to clean does not seem to be a RDBMS storage.");
            }

            RDBMSDataSource dataSource = (RDBMSDataSource) storageDataSource;
            Class.forName(dataSource.getDriverClassName()).newInstance();
            Connection connection = DriverManager.getConnection(dataSource.getConnectionURL(), dataSource.getUserName(),
                    dataSource.getPassword());
            try {
                Statement statement = connection.createStatement();
                try {
                    statement.execute("drop all objects delete files;"); //$NON-NLS-1$
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }

            next.clean(storage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
