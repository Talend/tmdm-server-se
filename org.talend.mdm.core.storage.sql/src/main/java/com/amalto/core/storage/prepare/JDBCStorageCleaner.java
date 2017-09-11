/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

public class JDBCStorageCleaner implements StorageCleaner {

    private static final Logger LOGGER = Logger.getLogger(JDBCStorageCleaner.class);

    private StorageCleaner next;

    public JDBCStorageCleaner(StorageCleaner next) {
        this.next = next;
    }

    public void clean(Storage storage) {
        try {
            DataSource storageDataSource = storage.getDataSource();
            if (!(storageDataSource instanceof RDBMSDataSource)) {
                throw new IllegalArgumentException("Storage to clean does not seem to be a RDBMS storage.");
            }
            if (((RDBMSDataSource) storageDataSource).hasInit()) { // Only clean if datasource has initialization information.
                RDBMSDataSource dataSource = (RDBMSDataSource) storageDataSource;
                RDBMSDataSource.DataSourceDialect dialect = dataSource.getDialectName();
                switch (dialect) {
                    case MYSQL:
                        next = new MySQLStorageCleaner(next);
                        break;
                    case H2:
                        next = new H2StorageCleaner(next);
                        break;
                    case ORACLE_10G:
                        next = new OracleStorageCleaner(next);
                        break;
                    case SQL_SERVER:
                        next = new SQLServerStorageCleaner(next);
                        break;
                    case POSTGRES:
                        next = new PostgresStorageCleaner(next);
                        break;
                    case DB2:
                        next = new DB2StorageCleaner(next);
                        break;
                    default:
                        throw new NotImplementedException("Can not clean storages based on dialect '" + dialect + "'");
                }
            } else {
                LOGGER.warn("Can not clean storage '" + storage.getName() + "' because datasource has no init section.");
            }
            next.clean(storage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
