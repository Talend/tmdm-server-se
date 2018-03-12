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

public class StorageSaver implements DocumentSaverContext {

    private final Storage storage;
    
    private final String dataModelName;
    
    private List<Action> actions = new LinkedList<Action>();

    private String taskId = StringUtils.EMPTY;

    private UserAction userAction;

    private final boolean invokeBeforeSaving;

    private final boolean updateReport;

    private MutableDocument updateReportDocument;

    private MutableDocument userDocument;

    private MutableDocument databaseDocument;

    private String[] ids = new String[0];

    private final boolean validate;

    private final boolean preserveOldCollectionValues;

    public StorageSaver(Storage storage,
                        String dataModelName,
                        MutableDocument userDocument,
                        UserAction userAction,
                        boolean invokeBeforeSaving,
                        boolean updateReport,
                        boolean validate) {
        this.storage = storage;
        this.dataModelName = dataModelName;
        this.userDocument = userDocument;
        this.userAction = userAction;
        this.invokeBeforeSaving = invokeBeforeSaving;
        this.updateReport = updateReport;
        this.validate = validate;
        this.preserveOldCollectionValues = false;
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
                if (validate) {
                    saver = new Validation(saver);
                }
                // Intentionally: no break here.
            case STAGING:
                if (invokeBeforeSaving) {
                    saver = new BeforeSaving(saver);
                }
                saver = new ApplyActions(saver); // Apply actions is mandatory
                if (updateReport) {
                    saver = new UpdateReport(saver);
                }
                return new Init(new ID(new GenerateActions(new Security(saver))));
            case SYSTEM:
                return new Init(new ID(new GenerateActions(new ApplyActions(saver))));
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
        userDocument = document;
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
            case SYSTEM:
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
        return preserveOldCollectionValues;
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
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPartialUpdateKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPartialUpdateIndex() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean generateTouchActions() {
        return false;
    }

    @Override
    public boolean isInvokeBeforeSaving() {
        return this.invokeBeforeSaving;
    }
}
