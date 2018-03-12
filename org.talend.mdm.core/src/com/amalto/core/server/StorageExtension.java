/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server;

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;

/**
 * A storage extension that can be implemented to provide additional {@link com.amalto.core.storage.Storage}
 * implementation that are not part of the original MDM server.
 */
public interface StorageExtension {

    /**
     * Test if this extension can accept some initialization parameters.
     * 
     * @param dataSource A {@link com.amalto.core.storage.datasource.DataSourceDefinition datasource} to check with this
     * storage extension.
     * @return <code>true</code> if a {@link com.amalto.core.storage.Storage storage} can be created with the supplied
     * parameters.
     * @see #create(String, com.amalto.core.storage.StorageType)
     */
    boolean accept(DataSource dataSource);

    /**
     * Returns an implementation of {@link com.amalto.core.storage.Storage storage}. Implementation is <b>not</b>
     * expected to be initialized (through {@link Storage#init(com.amalto.core.storage.datasource.DataSourceDefinition)}
     * .
     * 
     * @param storageName The storage name.
     * @param storageType The storage {@link com.amalto.core.storage.StorageType type}.
     * @return A {@link com.amalto.core.storage.Storage storage} ready to be initialized.
     */
    Storage create(String storageName, StorageType storageType);


    Storage createTemporary(StorageType storageType);
}
