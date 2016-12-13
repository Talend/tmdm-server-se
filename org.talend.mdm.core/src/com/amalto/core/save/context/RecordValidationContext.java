/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save.context;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.UserAction;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;


public class RecordValidationContext implements DocumentSaverContext {
    
    private final Storage storage; 
    
    private final String dataModelName;
    
    private List<Action> actions = new LinkedList<Action>();
    
    private String taskId = StringUtils.EMPTY;
    
    private UserAction userAction;
    
    private final boolean invokeBeforeSaving;
    
    private MutableDocument updateReportDocument;
    
    private MutableDocument userDocument;
    
    private MutableDocument databaseDocument;
    
    private String[] ids = new String[0];

    public RecordValidationContext(Storage storage, String dataModelName, UserAction userAction, boolean invokeBeforeSaving, MutableDocument userDocument) {
        super();
        this.storage = storage;
        this.dataModelName = dataModelName;
        this.userAction = userAction;
        this.invokeBeforeSaving = invokeBeforeSaving;
        this.userDocument = userDocument;
    }

    @Override
    public String getChangeSource() {
        return StringUtils.EMPTY;
    }

    @Override
    public DocumentSaver createSaver() {
        DocumentSaver saver = SaverContextFactory.invokeSaverExtension(new Save());
        switch (storage.getType()) {
            case MASTER:
                saver = new Validation(saver);
                if (invokeBeforeSaving) {
                    saver = new BeforeSaving(saver);
                }
                return new Init(new ID(new GenerateActions(new Security(new UpdateReport(new ApplyActions(saver))))));
            case STAGING:
                return new Init(new ID(new GenerateActions(new Security(new ApplyActions(saver)))));
            default:
                throw new NotImplementedException("No support for storage type '" + storage.getType() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public MutableDocument getDatabaseDocument() {
       return databaseDocument;
    }

    @Override
    public MutableDocument getUserDocument() {
        return userDocument;
    }

    @Override
    public void setUserDocument(MutableDocument document) {
        this.userDocument = document;
    }

    @Override
    public List<Action> getActions() {
        return actions;
    }

    @Override
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public String[] getId() {
        return ids;
    }

    @Override
    public void setId(String[] id) {
        ids = id;
    }

    @Override
    public String getDataCluster() {
        StorageType storageType = storage.getType();
        switch (storageType) {
            case MASTER:
                return storage.getName();
            case STAGING:
                return storage.getName() + StorageAdmin.STAGING_SUFFIX;
            default:
                throw new UnsupportedOperationException("No support for type '" + storageType + "'."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public String getDataModelName() {
        return this.dataModelName;
    }

    @Override
    public void setDatabaseDocument(MutableDocument databaseDocument) {
        this.databaseDocument = databaseDocument;
    }

    @Override
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public boolean preserveOldCollectionValues() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public MutableDocument getUpdateReportDocument() {
        return updateReportDocument;
    }

    @Override
    public void setUpdateReportDocument(MutableDocument updateReportDocument) {
        this.updateReportDocument = updateReportDocument;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPartialUpdateKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getPartialUpdateIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean generateTouchActions() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInvokeBeforeSaving() {
        // TODO Auto-generated method stub
        return true;
    }
}