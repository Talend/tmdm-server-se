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

import com.amalto.core.storage.StorageType;

import java.util.Arrays;

/**
 * Represents a set of {@link com.amalto.core.storage.datasource.DataSource datasource} that can be used by a
 * {@link com.amalto.core.storage.Storage storage}.
 * It contains at least one of:
 * <ul>
 *     <li>{@link StorageType#MASTER master}</li>
 *     <li>{@link StorageType#STAGING staging}</li>
 *     <li>{@link StorageType#SYSTEM system}</li>
 * </ul>
 */
public class DataSourceDefinition {

    private final DataSource master;

    private final DataSource staging;

    private final DataSource system;

    public DataSourceDefinition(DataSource master, DataSource staging, DataSource system) {
        this.master = master;
        this.staging = staging;
        this.system = system;
        // Compute isShared status
        DataSource[] dataSources = new DataSource[] {master, staging, system};
        boolean[][] isShared = computeSharedStatus(dataSources);
        if (master != null) {
            master.setShared(isShared(isShared[0]));
        }
        if (staging != null) {
            staging.setShared(isShared(isShared[1]));
        }
        if (system != null) {
            system.setShared(isShared(isShared[2]));
        }
    }

    private static boolean[][] computeSharedStatus(DataSource[] dataSources) {
        boolean isShared[][] = new boolean[dataSources.length][dataSources.length];
        int i = 0;
        for (DataSource dataSource : dataSources) {
            int j = 0;
            if (dataSource == null) {
                Arrays.fill(isShared[i], false);
            } else {
                for (DataSource source : dataSources) {
                    if (i != j) { // Don't count a self equals as a "share".
                        isShared[i][j] = dataSource.equals(source);
                    }
                    j++;
                }
            }
            i++;
        }
        return isShared;
    }

    private boolean isShared(boolean[] booleans) {
        for (boolean isShared : booleans) {
            if (isShared) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param type A {@link com.amalto.core.storage.StorageType storage type}.
     * @return The {@link com.amalto.core.storage.datasource.DataSource datasource} for the given <code>type</code> or
     * <code>null</code> if it does not exist.
     */
    public DataSource get(StorageType type) {
        switch (type) {
            case MASTER:
                return getMaster();
            case STAGING:
                return getStaging();
            case SYSTEM:
                return getSystem();
            default:
                throw new UnsupportedOperationException("No support for '" + type + "'.");
        }
    }

    /**
     * @return The {@link com.amalto.core.storage.datasource.DataSource datasource} for the {@link com.amalto.core.storage.StorageType#MASTER master} type.
     */
    public DataSource getMaster() {
        return master;
    }

    /**
     * @return <code>true</code> if definition has information for {@link com.amalto.core.storage.StorageType#MASTER master}
     * storage, <code>false</code> otherwise.
     */
    public boolean hasStaging() {
        return staging != null;
    }

    /**
     * @return The {@link com.amalto.core.storage.datasource.DataSource datasource} for the {@link com.amalto.core.storage.StorageType#STAGING staging} type.
     */
    public DataSource getStaging() {
        return staging;
    }

    /**
     * @return The {@link com.amalto.core.storage.datasource.DataSource datasource} for the {@link com.amalto.core.storage.StorageType#SYSTEM system} type.
     */
    public DataSource getSystem() {
        return system;
    }

    /**
     * Transforms the current datasource definitions for the given <code>container</code> and <code>revisionId</code>.
     * 
     * @param container A MDM container name.
     * @return A copy of current {@link com.amalto.core.storage.datasource.DataSourceDefinition definition} with
     * {@link com.amalto.core.storage.datasource.DataSource datasources} modified for given parameters.
     */
    public DataSourceDefinition transform(String container) {
        DataSource transformedMaster = null;
        if (master != null) {
            transformedMaster = master.transform(container);
        }
        DataSource transformedStaging = null;
        if (staging != null) {
            transformedStaging = staging.transform(container);
        }
        DataSource transformedSystem = null;
        if (system != null) {
            transformedSystem = system.transform(container);
        }
        return new DataSourceDefinition(transformedMaster, transformedStaging, transformedSystem);
    }
}
