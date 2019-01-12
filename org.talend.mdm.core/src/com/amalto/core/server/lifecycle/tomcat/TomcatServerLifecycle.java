/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server.lifecycle.tomcat;

import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;

import com.amalto.core.server.MetadataRepositoryAdmin;
import com.amalto.core.server.PersistenceExtension;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerLifecycle;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.server.StorageAdminImpl;
import com.amalto.core.server.StorageExtension;
import com.amalto.core.storage.CacheStorage;
import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageLogger;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.dispatch.CompositeStorage;

public class TomcatServerLifecycle implements ServerLifecycle {

    private static final Logger LOGGER = Logger.getLogger(TomcatServerLifecycle.class);

    private static Storage defaultWrap(Storage storage) {
        storage = new CacheStorage(new SecuredStorage(storage, SecuredStorage.UNSECURED));
        storage = new StorageLogger(storage);
        return storage;
    }

    @Override
    public Server createServer() {
        LOGGER.info("Using MDM in Tomcat context.");
        return new TomcatServer();
    }

    @Override
    public void destroyServer(Server server) {
    }

    @Override
    public StorageAdmin createStorageAdmin() {
        return new StorageAdminImpl();
    }

    @Override
    public void destroyStorageAdmin(StorageAdmin storageAdmin) {
        storageAdmin.close();
    }

    @Override
    public MetadataRepositoryAdmin createMetadataRepositoryAdmin() {
        return new TomcatMetadataRepositoryAdmin();
    }

    @Override
    public void destroyMetadataRepositoryAdmin(MetadataRepositoryAdmin metadataRepositoryAdmin) {
        metadataRepositoryAdmin.close();
    }

    public Storage createStorage(String storageName, StorageType storageType, DataSourceDefinition definition) {
        List<Storage> storageForDispatch = new LinkedList<Storage>();
        // Invoke extensions for storage extensions
        ServiceLoader<StorageExtension> extensions = ServiceLoader.load(StorageExtension.class);
        for (StorageExtension extension : extensions) {
            if (extension.accept(definition.get(storageType))) {
                Storage extensionStorage = extension.create(storageName, storageType);
                extensionStorage.init(definition);
                storageForDispatch.add(defaultWrap(extensionStorage));
            } else {
                LOGGER.debug("Extension '" + extension + "' is not eligible for datasource '" + definition + "'.");
            }
        }
        // Create actual storage
        int size = storageForDispatch.size();
        if (size > 1) {
            return new CompositeStorage(storageForDispatch.toArray(new Storage[size]));
        } else {
            return storageForDispatch.get(0); // Don't wrap in composite if there's no extension
        }
    }

    @Override
    public Storage createTemporaryStorage(DataSource dataSource, StorageType storageType) {
        List<Storage> storageForDispatch = new LinkedList<Storage>();
        // Invoke extensions for storage extensions
        ServiceLoader<StorageExtension> extensions = ServiceLoader.load(StorageExtension.class);
        for (StorageExtension extension : extensions) {
            if (extension.accept(dataSource)) {
                Storage extensionStorage = extension.createTemporary(storageType);
                extensionStorage.init(null);
                storageForDispatch.add(defaultWrap(extensionStorage));
            } else {
                LOGGER.debug("Extension '" + extension + "' is not eligible for datasource '" + dataSource + "'.");
            }
        }
        // Create actual storage
        int size = storageForDispatch.size();
        if (size > 1) {
            return new CompositeStorage(storageForDispatch.toArray(new Storage[size]));
        } else {
            return storageForDispatch.get(0); // Don't wrap in composite if there's no extension
        }
    }

    public void destroyStorage(Storage storage, boolean dropExistingData) {
        if (storage != null) {
            storage.close(dropExistingData);
        }
    }

    @Override
    public PersistenceExtension createPersistence(Server server) {
        ServiceLoader<PersistenceExtension> extensions = ServiceLoader.load(PersistenceExtension.class);
        for (PersistenceExtension extension : extensions) {
            if (extension.accept(server)) {
                return extension;
            }
        }
        return new DefaultPersistenceExtension();
    }

    private static class DefaultPersistenceExtension implements PersistenceExtension {

        @Override
        public boolean accept(Server server) {
            return false;
        }

        @Override
        public void update() {
        }
    }
}