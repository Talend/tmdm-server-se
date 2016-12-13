/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage;

import com.amalto.core.server.StorageExtension;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.inmemory.MemoryStorage;

import java.util.UUID;

public class HibernateStorageExtension implements StorageExtension {
    @Override
    public boolean accept(DataSource dataSource) {
        return dataSource instanceof RDBMSDataSource;
    }

    @Override
    public Storage create(String storageName, StorageType storageType) {
        return new HibernateStorage(storageName, storageType);
    }

    @Override
    public Storage createTemporary(StorageType storageType) {
        return new MemoryStorage(UUID.randomUUID().toString(), storageType);
    }
}
