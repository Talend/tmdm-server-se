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
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class FullTextIndexCleaner implements StorageCleaner, StorageInitializer {

    private static final Logger LOGGER = Logger.getLogger(FullTextIndexCleaner.class);

    private StorageInitializer initializer;

    public FullTextIndexCleaner() {
    }

    public FullTextIndexCleaner(StorageInitializer initializer) {
        this.initializer = initializer;
    }

    private static void doClean(Storage storage) {
        DataSource storageDataSource = storage.getDataSource();
        if (!(storageDataSource instanceof RDBMSDataSource)) {
            throw new IllegalArgumentException("Storage to clean does not seem to be a RDBMS storage.");
        }

        RDBMSDataSource dataSource = (RDBMSDataSource) storageDataSource;
        if (dataSource.supportFullText()) {
            String dataSourceIndexDirectory = dataSource.getIndexDirectory();
            File indexDirectory = new File(dataSourceIndexDirectory + '/' + storage.getName());
            if (indexDirectory.exists()) {
                try {
                    FileUtils.deleteDirectory(indexDirectory);
                } catch (IOException e) {
                    throw new IllegalStateException("Could not successfully delete '" + indexDirectory.getAbsolutePath() + "'", e);
                }
            } else {
                LOGGER.warn("Directory '" + indexDirectory + "' does not exist. No need to clean full text indexes.");
            }
        }
    }

    public void clean(Storage storage) {
        doClean(storage);
    }

    @Override
    public boolean supportInitialization(Storage storage) {
        return initializer.supportInitialization(storage);
    }

    @Override
    public boolean isInitialized(Storage storage) {
        return initializer.isInitialized(storage);
    }

    @Override
    public void initialize(Storage storage) {
        initializer.initialize(storage);
        doClean(storage);
    }
}
