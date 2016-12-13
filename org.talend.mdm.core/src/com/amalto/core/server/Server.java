/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server;

import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.transaction.TransactionManager;

/**
 * Represents a MDM Server with all needed basics services (Storage, Metadata, Datasource, Transaction management).
 * This interface only represents the server, for convenience all server current state is handled in {@link ServerContext}.
 * @see ServerContext#INSTANCE
 * @see ServerContext#get()
 */
public interface Server {

    /**
     * Method to use to check if a datasource suitable for all parameter values is available (if it exists).
     *
     * @param dataSourceName A datasource name in the datasource configuration content.
     * @param container      A name of a container.
     * @param type           The storage type to use. Since a datasource defines more than one way to connect to database (there's
     *                       one per storage type), this parameter is needed.
     * @return <code>true</code> if a datasource that fulfills all parameter is available, <code>false</code> otherwise.
     * @see com.amalto.core.storage.StorageType
     * @see StorageAdmin#create(String, String, com.amalto.core.storage.StorageType, String)
     */
    boolean hasDataSource(String dataSourceName, String container, StorageType type);

    /**
     * Returns the {@link com.amalto.core.storage.datasource.DataSourceDefinition definition} for the given datasource
     * name. If datasource name doesn't exist in configuration, returns <code>null</code>.
     *
     * @param dataSourceName A datasource name in the datasource configuration content.
     * @param container      A name of a container.
     * @return A {@link com.amalto.core.storage.datasource.DataSourceDefinition definition} for the given datasource
     * name or <code>null</code> if not found.
     * @see #getDefinition(String, String)
     */
    DataSourceDefinition getDefinition(String dataSourceName, String container);

    /**
     * @return The {@link StorageAdmin admin} that takes care of all {@link com.amalto.core.storage.Storage storage}
     * related admin operations (creation / removal...).
     */
    StorageAdmin getStorageAdmin();

    /**
     * @return The {@link MetadataRepositoryAdmin admin} that takes care of all {@link org.talend.mdm.commmon.metadata.MetadataRepository data model}
     * related admin operations (parse new data model, update existing...).
     */
    MetadataRepositoryAdmin getMetadataRepositoryAdmin();

    /**
     * Free all resources managed by this MDM server. It's usually better to call {@link ServerContext#close()}
     * when you want to clean up everything when the whole container is being shut down.
     * @see ServerContext#close()
     */
    void close();

    /**
     * Initializes all resources needed for this MDM server.
     */
    void init();

    /**
     * @return The {@link com.amalto.core.storage.transaction.TransactionManager manager} instance that manages all
     * {@link com.amalto.core.storage.transaction.Transaction transactions} in this MDM server.
     */
    TransactionManager getTransactionManager();
}
