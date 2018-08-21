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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.util.core.ICoreConstants;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.action.FieldUpdateAction;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.SaverSession;
import com.amalto.core.save.UserAction;

class Security implements DocumentSaver {

    private static final Logger LOGGER = Logger.getLogger(Security.class);

    private static final String NO_ADD = "X_No_Add"; //$NON-NLS-1$

    private static final String NO_REMOVE = "X_No_Remove"; //$NON-NLS-1$

    private final DocumentSaver next;

    public Security(DocumentSaver next) {
        this.next = next;
    }

    public void save(SaverSession session, DocumentSaverContext context) {
        List<Action> actions = context.getActions();
        SaverSource saverSource = session.getSaverSource();
        Set<String> currentUserRoles = saverSource.getCurrentUserRoles();

        // admin has all rights, so bypass security checks
        boolean bypassSecurityChecks = MDMConfiguration.getAdminUser().equals(saverSource.getUserName())
                || "Update".equals(context.getDatabaseDocument().getType().getName()); //$NON-NLS-1$
        // administration has all roles, so bypass security checks
        if (currentUserRoles.contains(ICoreConstants.ADMIN_PERMISSION)) {
            bypassSecurityChecks = true;
        }

        if (!bypassSecurityChecks) {
            // First check rights on the type
            ComplexTypeMetadata type = context.getUserDocument().getType();
            List<String> typeWriteUsers = type.getWriteUsers();
            boolean isAllowed = false;
            for (String currentUserRole : currentUserRoles) {
                if (typeWriteUsers.contains(currentUserRole)) {
                    isAllowed = true;
                    break;
                }
            }
            
            // if has rights for 'write', then further check for 'create'
            UserAction userAction = context.getUserAction();
            if (isAllowed && userAction == UserAction.CREATE) {
                List<String> typeNoCreateUsers = type.getDenyCreate();
                for (String currentUserRole : currentUserRoles) {
                    if (typeNoCreateUsers.contains(currentUserRole)) {
                        isAllowed = false;
                        break;
                    }
                }
            }
            
            if (!isAllowed) {
                throw new RuntimeException("User '" + saverSource.getUserName() + "' is not allowed to write to type '" + type.getName() + "'.");
            }

            // Then check security on all actions (updates...)
            Set<Action> failedActions = new HashSet<Action>();
            Map<String, ForeignKeyInfoWrapper> fkCheckMap = getFkStatusInfoMap(context);
            for (Action action : actions) {
                // do not check replace action as it's an update.
                if (!(action instanceof OverrideReplaceAction) && (isFkUpdateDenied(action, fkCheckMap, currentUserRoles) || !action.isAllowed(currentUserRoles))) {
                    failedActions.add(action);
                }
            }
            if (!failedActions.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                Iterator<Action> iterator = failedActions.iterator();
                while (iterator.hasNext()) {
                    builder.append(iterator.next().getDetails());
                    if (iterator.hasNext()) {
                        builder.append(" / ");  //$NON-NLS-1$
                    }
                }
                LOGGER.error("User '" + saverSource.getUserName() + "' is not allowed to perform following operation(s): " + builder);
                actions.removeAll(failedActions);

                // Revert all unauthorized actions in case of replace or create.
                if (context.getUserAction() == UserAction.CREATE
                                || context.getUserAction() == UserAction.REPLACE) {
                    LOGGER.warn("Following operation(s) are being ignored for replace/create: " + builder);
                    for (Action failedAction : failedActions) {
                        actions.add(new ReverseAction(failedAction));
                    }
                }

                // Make a no op if failedActions is empty and display a warning.
                if (actions.isEmpty()) {
                    LOGGER.warn("No more actions to perform after security checks, abort save operations.");
                    return;
                }
            }
        }

        next.save(session, context);
    }

    private boolean isFkUpdateDenied(Action action, Map<String, ForeignKeyInfoWrapper> fkCheckMap, Set<String> roles) {
        if (action instanceof FieldUpdateAction) {
            FieldUpdateAction entry = (FieldUpdateAction) action;
            if (entry.getField() instanceof ReferenceFieldMetadata && entry.getField().isMany() && entry.getUserAction() != UserAction.UPDATE) {
                String path = entry.getPath().replaceAll("\\[\\d*\\]", StringUtils.EMPTY);//$NON-NLS-1$
                if (fkCheckMap.containsKey(path)) {
                    ForeignKeyInfoWrapper fkItem = fkCheckMap.get(path);
                    String denyRight = getDenyAddOrRemoveRight(entry, roles);
                    int oldValueSize = fkItem.getOldValueSet().size();
                    int newValueSize = fkItem.getNewValueSet().size();
                    if (NO_ADD.equals(denyRight) && oldValueSize < newValueSize) {//$NON-NLS-1$
                        return true;
                    } else if (NO_REMOVE.equals(denyRight) && oldValueSize > newValueSize) {//$NON-NLS-1$
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getDenyAddOrRemoveRight(FieldUpdateAction updatedField, Set<String> roles) {
        List<String> allowedUserRoles = updatedField.getField().getWriteUsers();
        for (String role : roles) {
            if (allowedUserRoles.contains(role)) {
                List<String> noAddRoles = updatedField.getField().getNoAddRoles();
                List<String> noRemoveRoles = updatedField.getField().getNoRemoveRoles();
                if (noRemoveRoles != null && noRemoveRoles.contains(role)) {
                    return NO_REMOVE;//$NON-NLS-1$
                } else if (noAddRoles != null && noAddRoles.contains(role)) {
                    return NO_ADD;//$NON-NLS-1$
                }
            }
        }
        return StringUtils.EMPTY;
    }

    private Map<String, ForeignKeyInfoWrapper> getFkStatusInfoMap(DocumentSaverContext context) {
        Map<String, ForeignKeyInfoWrapper> fkCheckMap = new HashMap<>();
        List<Action> actions = context.getActions();
        for (Action item : actions) {
            if (item instanceof FieldUpdateAction) {
                FieldUpdateAction entry = (FieldUpdateAction) item;
                String path = entry.getPath().replaceAll("\\[\\d*\\]", StringUtils.EMPTY);//$NON-NLS-1$
                String oldValue = entry.getOldValue();
                String newValue = entry.getNewValue();
                ForeignKeyInfoWrapper fkItem = null;
                if (fkCheckMap.containsKey(path)) {
                    fkItem = fkCheckMap.get(path);
                } else {
                    fkItem = new ForeignKeyInfoWrapper(path);
                    fkCheckMap.put(path, fkItem);
                }
                if (StringUtils.isNotEmpty(oldValue)) {
                    fkItem.getOldValueSet().add(oldValue);
                }
                if (StringUtils.isNotEmpty(newValue)) {
                    fkItem.getNewValueSet().add(newValue);
                }
            }
        }
        return fkCheckMap;
    }

    public String[] getSavedId() {
        return next.getSavedId();
    }

    public String getSavedConceptName() {
        return next.getSavedConceptName();
    }

    public String getBeforeSavingMessage() {
        return next.getBeforeSavingMessage();
    }

    public String getBeforeSavingMessageType() {
        return next.getBeforeSavingMessageType();
    }

    private static class ForeignKeyInfoWrapper {

        private final String key;

        private Set<String> oldValueSet = new HashSet<>();

        private Set<String> newValueSet = new HashSet<>();

        public ForeignKeyInfoWrapper(String key) {
            super();
            this.key = key;
        }

        public Set<String> getOldValueSet() {
            return oldValueSet;
        }

        public Set<String> getNewValueSet() {
            return newValueSet;
        }
    }

    // Special action implementation for reverting an underlying action when perform() is called.
    private static class ReverseAction implements Action {

        private final Action action;

        public ReverseAction(Action action) {
            this.action = action;
        }

        public MutableDocument perform(MutableDocument document) {
            return action.undo(document);
        }

        public MutableDocument undo(MutableDocument document) {
            return document;
        }

        public MutableDocument addModificationMark(MutableDocument document) {
            return action.addModificationMark(document);
        }

        public MutableDocument removeModificationMark(MutableDocument document) {
            return action.removeModificationMark(document);
        }

        public Date getDate() {
            return action.getDate();
        }

        public String getSource() {
            return action.getSource();
        }

        public String getUserName() {
            return action.getUserName();
        }

        public boolean isAllowed(Set<String> roles) {
            return true;
        }

        public String getDetails() {
            return "revert modification: (" + action.getDetails() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public boolean isTransient() {
            return action.isTransient();
        }
    }
}
