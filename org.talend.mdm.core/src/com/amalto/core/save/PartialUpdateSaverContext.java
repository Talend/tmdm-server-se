/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.save;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.save.context.DocumentSaver;

public class PartialUpdateSaverContext implements DocumentSaverContext {

    private final DocumentSaverContext delegate;

    private final int index;

    private final boolean overwrite;

    private UserAction userAction;

    private String pivot;

    private final String key;

    private PartialUpdateSaverContext(DocumentSaverContext delegate,
                                      String pivot,
                                      String key,
                                      int index,
                                      boolean overwrite,
                                      UserAction userAction) {
        this.delegate = delegate;
        this.pivot = pivot;
        this.key = key;
        this.index = index;
        this.overwrite = overwrite;
        this.userAction = userAction;
    }

    public static DocumentSaverContext decorate(DocumentSaverContext context,
                                                String pivot,
                                                String key,
                                                int index,
                                                boolean overwrite,
                                                boolean delete) {
        if (pivot == null) {
            pivot = StringUtils.EMPTY;
        }
        if (key == null) {
            key = StringUtils.EMPTY;
        }
        if (delete) {
            return new PartialUpdateSaverContext(context, pivot, key, index, overwrite, UserAction.PARTIAL_DELETE);
        } else if (pivot.length() > 1) {
            return new PartialUpdateSaverContext(context, pivot, key, index, overwrite, UserAction.PARTIAL_UPDATE);
        } else {
            return new PartialUpdateSaverContext(context, pivot, key, index, overwrite, UserAction.UPDATE);
        }    
    }

    @Override
    public String getChangeSource() {
        return delegate.getChangeSource();
    }

    public DocumentSaver createSaver() {
        return delegate.createSaver();
    }

    public MutableDocument getDatabaseDocument() {
        return delegate.getDatabaseDocument();
    }

    public MutableDocument getUserDocument() {
        return delegate.getUserDocument();
    }

    public void setUserDocument(MutableDocument document) {
        delegate.setUserDocument(document);
    }

    public List<Action> getActions() {
        return delegate.getActions();
    }

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
        String localName = databaseDocument.asDOM().getDocumentElement().getLocalName();
        pivot = StringUtils.substringAfter(pivot, localName + '/');
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
        return !overwrite;
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
        return userAction;
    }

    @Override
    public void setUserAction(UserAction userAction) {
        this.userAction = userAction;
    }

    @Override
    public String getPartialUpdatePivot() {
        return pivot;
    }

    @Override
    public String getPartialUpdateKey() {
        return key;
    }

    @Override
    public int getPartialUpdateIndex() {
        return index;
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
    public String[] getId() {
        return delegate.getId();
    }

    @Override
    public void setId(String[] id) {
        delegate.setId(id);
    }
    
    public DocumentSaverContext getDelegate() {
        return delegate;
    }

}
