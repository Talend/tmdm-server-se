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

package com.amalto.core.storage.datasource;

/**
 * Represents a "datasource": a representation of how to connect to the underlying database.
 * @see com.amalto.core.storage.Storage#init(DataSourceDefinition)
 */
public interface DataSource {
    /**
     * @return A unique name as {@link String} that identifies the datasource in MDM.
     */
    String getName();

    /**
     * @return <code>true</code> if this datasource can potentially be shared by multiple instances of
     * {@link com.amalto.core.storage.Storage}, <code>false</code> otherwise. When this method returns <code>true</code>,
     * storage should enforce all necessary actions (e.g. naming conventions) to ensure no collision between
     * storages.
     */
    boolean isShared();

    /**
     * Flags the data source as "shared" (see {@link #isShared()}).
     * @param isShared <code>true</code> is data source is shared, <code>false</code> otherwise.
     */
    void setShared(boolean isShared);

    /**
     * Returns a copy of current {@link com.amalto.core.storage.datasource.DataSource datasource} suited for given parameters.
     * @param container A MDM container name.
     */
    DataSource transform(String container);
}
