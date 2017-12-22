/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.audit;

import java.util.Set;

import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.objects.role.RolePOJO;

public class MDMAuditLogger {


    public static void userRolesModified(String user, String targetUser, Set<String> oldRoles, Set<String> newRoles) {
    }

    public static void roleCreated(String user, RolePOJO role) {
    }

    public static void roleCreationOrModificationFailed(String user, String roleName, Exception ex) {
    }

    public static void roleModified(String user, RolePOJO oldRole, RolePOJO newRole) {
    }

    public static void roleDeleted(String user, String roleName) {
    }

    public static void roleDeletionFailed(String user, String roleName, Exception ex) {
    }

    public static void roleCreationFailed(String user, String roleName, Exception ex) {
    }

    public static void roleModificationFailed(String user, String roleName, Exception ex) {
    }

    public static void userRolesModificationFailed(String user, String targetUser, Exception ex) {
    }

    public static void dataModelCreated(String user, DataModelPOJO dataModel) {
    }

    public static void dataModelDeleted(String user, String dataModelName) {
    }

    public static void dataModelModified(String user, DataModelPOJO oldDataModel, DataModelPOJO newDataModel) {
    }

    public static void dataModelCreationOrModificationFailed(String user, String dataModelName, Exception ex) {
    }

    public static void dataModelDeletionFailed(String user, String dataModelName, Exception ex) {
    }

    public static void dataModelModificationFailed(String user, String dataModelName, Exception e) {
    }

    public static void dataModelCreationFailed(String user, String dataModelName, Exception e) {
    }
}