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

package com.amalto.core.history.action;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;

import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.save.UserAction;

/**
 * Action performed on a field with maxOccurs = 1.
 */
public class FieldUpdateAction extends AbstractFieldAction {
    
    public static final String MODIFY_ADD_MARKER_VALUE = "tree-node-add"; //$NON-NLS-1$
    
    public static final String MODIFY_UPDATE_MARKER_VALUE = "tree-node-update"; //$NON-NLS-1$
    
    public static final String MODIFY_REMOVE_MARKER_VALUE = "tree-node-remove"; //$NON-NLS-1$

    protected final String path;

    protected final String oldValue;

    protected final String newValue;

    protected final FieldMetadata updatedField;

    protected final UserAction userAction;

    public FieldUpdateAction(Date date,
                             String source,
                             String userName,
                             String path,
                             String oldValue,
                             String newValue,
                             FieldMetadata updatedField,
                             UserAction userAction) {
        super(date, source, userName, updatedField);
        this.path = path;
        this.updatedField = updatedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.userAction = userAction;
    }

    public MutableDocument perform(MutableDocument document) {
        Accessor accessor = document.createAccessor(path);
        if (getOldValue() != null) {
            if (getNewValue() == null) {
                accessor.delete();
            } else {
                if (!accessor.exist()) {
                    accessor.createAndSet(getNewValue());
                } else {
                    accessor.set(getNewValue());
                }
            }
        } else {
            accessor.createAndSet(getNewValue());
        }
        return document;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public MutableDocument undo(MutableDocument document) {
        Accessor accessor = document.createAccessor(path);
        if (getOldValue() != null) {
            if (getNewValue() == null) {
                accessor.createAndSet(getOldValue());
            } else {
                accessor.set(getOldValue());
            }
        } else { // old value == null
            accessor.delete();
        }
        return document;
    }

    public MutableDocument addModificationMark(MutableDocument document) {
        Accessor accessor = document.createAccessor(path);
        if (accessor.exist()) {
            if (getOldValue() != null) {
                if (getNewValue() == null) {
                    accessor.markModified(Accessor.Marker.REMOVE);
                } else {
                    accessor.markModified(Accessor.Marker.UPDATE);
                }
            } else {
                if (getNewValue() != null) {
                    accessor.markModified(Accessor.Marker.ADD);
                }
            }
        }
        return document;
    }

    public MutableDocument removeModificationMark(MutableDocument document) {
        Accessor accessor = document.createAccessor(path);
        accessor.markUnmodified();
        return document;
    }

    public boolean isAllowed(Set<String> roles) {
        boolean isAllowed = false;
        List<String> allowedUserRoles = updatedField.getWriteUsers();
        for (String role : roles) {
            if (allowedUserRoles.contains(role) && hasAddOrRemoveRights(role)) {
                isAllowed = true;
                break;
            }
        }
        return isAllowed;
    }
    
    private boolean hasAddOrRemoveRights(String role) {
        if (updatedField instanceof ReferenceFieldMetadata && updatedField.isMany()) {
            List<String> noAddRoles = updatedField.getNoAddRoles();
            List<String> noRemoveRoles = updatedField.getNoRemoveRoles();
            if (noAddRoles == null || noAddRoles.isEmpty() || noRemoveRoles == null || noRemoveRoles.isEmpty()) {
                return true;
            }
            switch (userAction) {
            case CREATE:
                if (noAddRoles.contains(role)) {
                    return false;
                }
                break;
            case REPLACE:
                if (hasRemoveForeignKeyRight(noRemoveRoles, role)) {
                    return false;
                } else if (hasAddForeignKeyRight(noAddRoles, role)) {
                    return false;
                }
                break;
            default:
            }
        }
        return true;
    }

    private boolean hasAddForeignKeyRight(List<String> noAddRoles, String role) {
        return noAddRoles.contains(role) && StringUtils.isNotEmpty(newValue) && StringUtils.isEmpty(oldValue);
    }

    private boolean hasRemoveForeignKeyRight(List<String> noRemoveRoles, String role) {
        return noRemoveRoles.contains(role) && StringUtils.isEmpty(newValue) && StringUtils.isNotEmpty(oldValue);
    }

    public String getDetails() {
        return "update field '" + updatedField.getName() + "' of type '" + updatedField.getContainingType().getName() + "'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public String toString() {
        return "FieldUpdateAction{" + //$NON-NLS-1$
                "path='" + path + '\'' + //$NON-NLS-1$
                ", oldValue='" + getOldValue() + '\'' + //$NON-NLS-1$
                ", newValue='" + getNewValue() + '\'' + //$NON-NLS-1$
                '}';
    }

    public UserAction getUserAction() {
        return userAction;
    }

    @Override
    public FieldMetadata getField() {
        return updatedField;
    }

    public String getPath() {
        return path;
    }
}
