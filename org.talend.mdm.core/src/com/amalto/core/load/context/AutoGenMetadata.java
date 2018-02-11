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

package com.amalto.core.load.context;

import com.amalto.core.load.Metadata;
import com.amalto.core.save.generator.AutoIdGenerator;

/**
 *
 */
class AutoGenMetadata implements Metadata {
    private final Metadata delegate;

    private final String[] idPaths;

    private final AutoIdGenerator generator;

    private String[] autoGenId;

    AutoGenMetadata(Metadata delegate, String[] idPaths, AutoIdGenerator generator) {
        this.delegate = delegate;
        this.idPaths = idPaths;
        this.generator = generator;
    }

    private static String[] generateId(Metadata metadata, String[] idPaths, AutoIdGenerator generator) {
        for (String idPath : idPaths) {
            metadata.setId(idPath, generator.generateId(metadata.getDataClusterName(), metadata.getName(), idPath));
        }
        return metadata.getId();
    }

    public String[] getId() {
        if (autoGenId == null) {
            autoGenId = generateId(delegate, idPaths, generator);
        }
        return autoGenId;
    }

    public void setId(String idElementName, String id) {
        throw new UnsupportedOperationException("AutoGen id is read-only");
    }

    public void setContainer(String container) {
        delegate.setContainer(container);
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public void setDmn(String dmn) {
        delegate.setDmn(dmn);
    }

    public String getContainer() {
        return delegate.getContainer();
    }

    public String getName() {
        return delegate.getName();
    }

    public String getDMR() {
        return delegate.getDMR();
    }

    public String getTaskId() {
        return delegate.getTaskId();
    }

    public String getSP() {
        return delegate.getSP();
    }

    public String getVersion() {
        return delegate.getVersion();
    }

    public void reset() {
        delegate.reset();
        autoGenId = null;
    }

    public String getDMN() {
        return delegate.getDMN();
    }

    public String getDataClusterName() {
        return delegate.getDataClusterName();
    }

    public void setDataClusterName(String dataClusterName) {
        delegate.setDataClusterName(dataClusterName);
    }
}
