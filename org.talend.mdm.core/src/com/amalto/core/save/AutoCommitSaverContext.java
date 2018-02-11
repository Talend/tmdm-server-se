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

package com.amalto.core.save;

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.save.context.AutoCommit;
import com.amalto.core.save.context.DocumentSaver;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class AutoCommitSaverContext implements DocumentSaverContext {

    private final DocumentSaverContext delegate;

    private AutoCommitSaverContext(DocumentSaverContext delegate) {
        this.delegate = delegate;
    }

    public static AutoCommitSaverContext decorate(DocumentSaverContext context) {
        return new AutoCommitSaverContext(context);
    }

    @Override
    public MutableDocument getUpdateReportDocument() {
        return delegate.getUpdateReportDocument();
    }

    @Override
    public void setUpdateReportDocument(MutableDocument updateReportDocument) {
        delegate.setUpdateReportDocument(updateReportDocument);
    }

    @Override
    public UserAction getUserAction() {
        return delegate.getUserAction();
    }

    @Override
    public void setUserAction(UserAction userAction) {
        delegate.setUserAction(userAction);
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
        return delegate.getPartialUpdateIndex();
    }

    @Override
    public boolean generateTouchActions() {
        return delegate.generateTouchActions();
    }

    @Override
    public boolean isInvokeBeforeSaving() {
        return delegate.isInvokeBeforeSaving();
    }

    @Override
    public String getChangeSource() {
        return delegate.getChangeSource();
    }

    @Override
    public DocumentSaver createSaver() {
        return new AutoCommit(delegate.createSaver());
    }

    @Override
    public MutableDocument getDatabaseDocument() {
        return delegate.getDatabaseDocument();
    }

    @Override
    public MutableDocument getUserDocument() {
        return delegate.getUserDocument();
    }

    @Override
    public void setUserDocument(MutableDocument document) {
        delegate.setUserDocument(document);
    }

    @Override
    public List<Action> getActions() {
        return delegate.getActions();
    }

    @Override
    public void setActions(List<Action> actions) {
        delegate.setActions(actions);
    }

    @Override
    public String getDataCluster() {
        return delegate.getDataCluster();
    }

    @Override
    public String getDataModelName() {
        return delegate.getDataModelName();
    }

    @Override
    public void setDatabaseDocument(MutableDocument databaseDocument) {
        delegate.setDatabaseDocument(databaseDocument);
    }

    @Override
    public void setTaskId(String taskId) {
        delegate.setTaskId(taskId);
    }

    @Override
    public String getTaskId() {
        return delegate.getTaskId();
    }

    @Override
    public boolean preserveOldCollectionValues() {
        return delegate.preserveOldCollectionValues();
    }

    @Override
    public String[] getId() {
        return delegate.getId();
    }

    @Override
    public void setId(String[] id) {
        delegate.setId(id);
    }
}
