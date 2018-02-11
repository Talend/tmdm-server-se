/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import java.io.InputStream;
import java.util.List;

import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.server.api.XmlServer;
import org.apache.commons.lang.StringUtils;

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.load.action.LoadAction;
import com.amalto.core.save.UserAction;
import com.amalto.core.util.XSDKey;

class BulkLoadContext implements DocumentSaverContext {

    private final String dataCluster;

    private final String dataModelName;

    private final BulkLoadSaver bulkLoadSaver;

    public BulkLoadContext(String dataCluster, String dataModelName, XSDKey keyMetadata, InputStream documentStream,
            LoadAction loadAction, XmlServer server) {
        this.dataCluster = dataCluster;
        this.dataModelName = dataModelName;
        bulkLoadSaver = new BulkLoadSaver(loadAction, documentStream, keyMetadata, server);
    }

    @Override
    public String getChangeSource() {
        return StringUtils.EMPTY;
    }

    public DocumentSaver createSaver() {
        return bulkLoadSaver;
    }

    public MutableDocument getDatabaseDocument() {
        throw new UnsupportedOperationException();
    }

    public MutableDocument getUserDocument() {
        throw new UnsupportedOperationException();
    }

    public void setUserDocument(MutableDocument document) {
        throw new UnsupportedOperationException();
    }

    public List<Action> getActions() {
        throw new UnsupportedOperationException();
    }

    public void setActions(List<Action> actions) {
        throw new UnsupportedOperationException();
    }

    public String[] getId() {
        throw new UnsupportedOperationException();
    }

    public void setId(String[] id) {
        throw new UnsupportedOperationException();
    }

    public String getDataCluster() {
        return dataCluster;
    }

    public String getDataModelName() {
        return dataModelName;
    }

    public void setDatabaseDocument(MutableDocument databaseDocument) {
        throw new UnsupportedOperationException();
    }

    public void setTaskId(String taskId) {
        throw new UnsupportedOperationException();
    }

    public String getTaskId() {
        throw new UnsupportedOperationException();
    }

    public boolean preserveOldCollectionValues() {
        return false;
    }

    public MutableDocument getUpdateReportDocument() {
        throw new UnsupportedOperationException("No supported in this implementation.");
    }

    public void setUpdateReportDocument(MutableDocument updateReportDocument) {
        throw new UnsupportedOperationException("No supported in this implementation.");
    }

    public UserAction getUserAction() {
        return UserAction.REPLACE;
    }

    public void setUserAction(UserAction userAction) {
        // Only REPLACE for this context.
    }

    public String getPartialUpdatePivot() {
        return StringUtils.EMPTY;
    }

    public String getPartialUpdateKey() {
        return StringUtils.EMPTY;
    }

    @Override
    public int getPartialUpdateIndex() {
        throw new UnsupportedOperationException("No supported in this implementation.");
    }

    @Override
    public boolean generateTouchActions() {
        return true;
    }

    @Override
    public boolean isInvokeBeforeSaving() {
        return false; // No support in bulk load
    }
}
