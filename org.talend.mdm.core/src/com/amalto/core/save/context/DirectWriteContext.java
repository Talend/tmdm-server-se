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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.amalto.core.history.Action;
import com.amalto.core.history.EmptyDocument;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.SaverSession;
import com.amalto.core.save.UserAction;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.Util;

class DirectWriteContext implements DocumentSaverContext {

    private final String dataCluster;

    private final String record;

    public DirectWriteContext(String dataCluster, String record) {
        this.dataCluster = dataCluster;
        this.record = record;
    }

    @Override
    public String getChangeSource() {
        return StringUtils.EMPTY;
    }

    @Override
    public DocumentSaver createSaver() {
        return new DocumentSaver() {

            @Override
            public void save(SaverSession session, DocumentSaverContext context) {
                try {
                    final XmlServer xmlServer = Util.getXmlServerCtrlLocal();
                    xmlServer.putDocumentFromString(record, null, dataCluster);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to save system record.", e);
                }
            }

            @Override
            public String[] getSavedId() {
                return new String[0];
            }

            @Override
            public String getSavedConceptName() {
                return null;
            }

            @Override
            public String getBeforeSavingMessage() {
                return null;
            }

            @Override
            public String getBeforeSavingMessageType() {
                return BeforeSaving.TYPE_INFO;
            }
        };
    }

    @Override
    public MutableDocument getDatabaseDocument() {
        return EmptyDocument.INSTANCE;
    }

    @Override
    public void setDatabaseDocument(MutableDocument databaseDocument) {
    }

    @Override
    public MutableDocument getUserDocument() {
        return EmptyDocument.INSTANCE;
    }

    @Override
    public void setUserDocument(MutableDocument document) {
    }

    @Override
    public List<Action> getActions() {
        return Collections.emptyList();
    }

    @Override
    public void setActions(List<Action> actions) {
    }

    @Override
    public String getDataCluster() {
        return dataCluster;
    }

    @Override
    public String getDataModelName() {
        return dataCluster;
    }

    @Override
    public String getTaskId() {
        return null;
    }

    @Override
    public void setTaskId(String taskId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean preserveOldCollectionValues() {
        return false;
    }

    @Override
    public MutableDocument getUpdateReportDocument() {
        return null;
    }

    @Override
    public void setUpdateReportDocument(MutableDocument updateReportDocument) {
        throw new UnsupportedOperationException("No supported in this implementation.");
    }

    @Override
    public UserAction getUserAction() {
        return UserAction.AUTO;
    }

    @Override
    public void setUserAction(UserAction userAction) {
    }

    @Override
    public String getPartialUpdatePivot() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getPartialUpdateKey() {
        return StringUtils.EMPTY;
    }

    @Override
    public int getPartialUpdateIndex() {
        return -1;
    }

    @Override
    public boolean generateTouchActions() {
        return true;
    }

    @Override
    public boolean isInvokeBeforeSaving() {
        return false; // No support for system context
    }

    @Override
    public String[] getId() {
        return new String[0];
    }

    @Override
    public void setId(String[] id) {

    }

}
