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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.EUUIDCustomType;

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.action.FieldUpdateAction;
import com.amalto.core.history.action.LogicalDeleteAction;
import com.amalto.core.history.action.PhysicalDeleteAction;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.ReportDocumentSaverContext;
import com.amalto.core.save.SaverSession;
import com.amalto.core.save.UserAction;
import com.amalto.core.util.SynchronizedNow;

class GenerateActions implements DocumentSaver {

    private final DocumentSaver next;

    private final static SynchronizedNow now = new SynchronizedNow();

    GenerateActions(DocumentSaver next) {
        this.next = next;
    }

    @Override
    public void save(SaverSession session, DocumentSaverContext context) {
        MutableDocument userDocument = context.getUserDocument();
        MutableDocument databaseDocument = context.getDatabaseDocument();
        if (databaseDocument == null) {
            throw new IllegalStateException("Database document is expected to be set."); //$NON-NLS-1$
        }
        // Get source of modification (only if we're in the context of an update report).
        String source = context.getChangeSource();
        long mdmUpdateTime = now.getTime();
        Date date = new Date(mdmUpdateTime);
        SaverSource saverSource = session.getSaverSource();
        String userName = saverSource.getUserName();
        ComplexTypeMetadata type = context.getUserDocument().getType();
        // Generate actions
        List<Action> actions;
        MetadataRepository metadataRepository = saverSource.getMetadataRepository(context.getDataModelName());
        // Generate field update actions for UUID and AutoIncrement elements.
        UpdateActionCreator updateActions = new UpdateActionCreator(databaseDocument, userDocument, date, source, userName,
                context.generateTouchActions(), metadataRepository, context.getDataCluster(), context.getDataModelName(),
                saverSource);
        UserAction userAction = context.getUserAction();
        switch (userAction) {
        case CREATE_STRICT:
        case CREATE:
            CreateActions createActions;
            if (userAction == UserAction.CREATE) {
                // Create action will override provided ids values with generated values (if any)...
                createActions = new CreateActions(userDocument, date, source, userName, context.getDataCluster(),
                        context.getDataModelName(), saverSource);
            } else {
                // CASE_STRICT:
                // ... and this will *not* override provided ids values with generated values (if any).
                createActions = new CreateWithProvidedIdActions(userDocument, date, source, userName, context.getDataCluster(),
                        context.getDataModelName(), saverSource);
            }
            Action createAction = new OverrideCreateAction(date, source, userName, userDocument, type);
            Collection<FieldMetadata> keys = type.getKeyFields();
            List<String> idValues = new ArrayList<String>(keys.size());
            boolean isAutoIncrementKey = false;
            for (FieldMetadata key : keys) {
                if (EUUIDCustomType.AUTO_INCREMENT.getName().equalsIgnoreCase(key.getType().getName())
                        || EUUIDCustomType.UUID.getName().equalsIgnoreCase(key.getType().getName())) {
                    isAutoIncrementKey = true;
                }
            }
                    
            // Builds action list (be sure to include actual creation as first action).
            actions = new LinkedList<Action>();
            actions.add(createAction);
            if (!isAutoIncrementKey || context.getId().length < 1) {
                actions.addAll(type.accept(createActions));
            }
            updateActions.setCreateAction(true);
            actions.addAll(type.accept(updateActions));

            // Don't re-generate auto-increment id & UUID
            if (!isAutoIncrementKey || context.getId().length < 1) {
                for (FieldMetadata key : keys) {
                    if (isAutoIncrementKey) {
                        for (Action action : actions) {
                            if (action instanceof FieldUpdateAction) {
                                FieldUpdateAction fieldUpdateAction = (FieldUpdateAction) action;
                                if (key.equals(fieldUpdateAction.getField())) {
                                    idValues.add(fieldUpdateAction.getNewValue());
                                    break; // Found the action on the key... no need to iterate over all of them.
                                }
                            }
                        }
                    } else {
                        idValues.add(context.getUserDocument().createAccessor(key.getPath()).get());
                    }
                }
                // Join ids read from XML document and generated ID values.
                context.setId(idValues.toArray(new String[idValues.size()]));
            }
            break;
        case UPDATE:
            // get updated paths
            updateActions.setCreateAction(false);
            actions = type.accept(updateActions);
            break;
        case REPLACE:
            // "Is replace" (similar to creation but without clean up of empty elements).
            // Builds action list (be sure to include actual creation as first action).
            actions = new LinkedList<Action>();
            actions.add(new OverrideReplaceAction(date, source, userName, userDocument, type));
            actions.addAll(type.accept(updateActions));
            break;
        case PARTIAL_UPDATE:
            PartialUpdateActionCreator partialUpdateActionCreator = new PartialUpdateActionCreator(databaseDocument,
                    userDocument, date, context.preserveOldCollectionValues(), context.getPartialUpdateIndex(),
                    context.getPartialUpdatePivot(), context.getPartialUpdateKey(), source, userName,
                    context.generateTouchActions(), metadataRepository, context.getDataCluster(), context.getDataModelName(),
                    saverSource);
            actions = type.accept(partialUpdateActionCreator);
            break;
        case PARTIAL_DELETE:
            userDocument = new PartialDeleteSimulator(databaseDocument, userDocument, context.getPartialUpdatePivot(),
                    context.getPartialUpdateKey()).simulateDelete();
            updateActions = new UpdateActionCreator(databaseDocument, userDocument, date, source, userName,
                    context.generateTouchActions(), metadataRepository,context.getDataCluster(),
                    context.getDataModelName(), saverSource);
            updateActions.setPartialDelete(true);
            actions = type.accept(updateActions);
            break;
        case LOGICAL_DELETE:
            actions = Collections.<Action> singletonList(new LogicalDeleteAction(date, source, userName, type));
            break;
        case PHYSICAL_DELETE:
            actions = Collections.<Action> singletonList(new PhysicalDeleteAction(date, source, userName, type));
            break;
        default:
            throw new NotImplementedException("Support for '" + userAction + "'."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // Handle generated actions
        if (!context.getActions().isEmpty()) {
            // Actions were previously generated, now check if there was previously generated ones (keep everything on
            // id then).
            List<Action> mergedActions = new LinkedList<Action>();
            // Get all actions on id fields
            Map<FieldMetadata, Action> keyActions = new HashMap<FieldMetadata, Action>();
            for (Action action : context.getActions()) {
                if (action instanceof FieldUpdateAction) {
                    FieldUpdateAction fieldUpdateAction = (FieldUpdateAction) action;
                    FieldMetadata field = fieldUpdateAction.getField();
                    if (field.isKey()) {
                        keyActions.put(field, action);
                    }
                }
            }
            // Now merge the actions
            for (Action action : actions) {
                if (action instanceof FieldUpdateAction) {
                    FieldUpdateAction fieldUpdateAction = (FieldUpdateAction) action;
                    FieldMetadata field = fieldUpdateAction.getField();
                    if (field.isKey()) {
                        mergedActions.add(keyActions.get(field));
                    } else {
                        mergedActions.add(action);
                    }
                } else {
                    mergedActions.add(action);
                }
            }
            context.setActions(mergedActions);
        } else {
            // No previously generated actions, replace actions in context.
            context.setActions(actions);
        }
        // Ignore rest of save chain if there's no change to perform.
        boolean hasModificationActions = hasModificationActions(actions);
        if (hasModificationActions || isInvokeBeforeSaving(context)) { // Ignore rest of save chain if there's no change
                                                                       // to perform.
            next.save(session, context);
        }
    }

    private boolean hasModificationActions(List<Action> actions) {
        for (Action action : actions) {
            if (!action.isTransient()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInvokeBeforeSaving(DocumentSaverContext context) {
        if (context instanceof ReportDocumentSaverContext) {
            if (((ReportDocumentSaverContext) context).getDelegate() instanceof StorageSaver) {
                StorageSaver saver = (StorageSaver) ((ReportDocumentSaverContext) context).getDelegate();
                return saver.isInvokeBeforeSaving();
            } else if (((ReportDocumentSaverContext) context).getDelegate() instanceof UserContext) {
                UserContext saver = (UserContext) ((ReportDocumentSaverContext) context).getDelegate();
                return saver.isInvokeBeforeSaving();
            }
        }
        return false;
    }

    @Override
    public String[] getSavedId() {
        return next.getSavedId();
    }

    @Override
    public String getSavedConceptName() {
        return next.getSavedConceptName();
    }

    @Override
    public String getBeforeSavingMessage() {
        return next.getBeforeSavingMessage();
    }

    @Override
    public String getBeforeSavingMessageType() {
        return next.getBeforeSavingMessageType();
    }

}
