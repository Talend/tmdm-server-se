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
import com.amalto.core.objects.view.ViewPOJO;

public class MDMAuditLogger {

    public static void loginSuccess(String userName) {
    }

    public static void loginFail(String userName, Exception ex) {
    }

    public static void logoutSuccess(String userName) {
    }

    public static void roleCreated(String user, RolePOJO role) {
    }

    public static void roleCreateOrModifyFail(String user, String roleName, Exception ex) {
    }

    public static void roleModified(String user, RolePOJO oldRole, RolePOJO newRole) {
    }

    public static void roleDeleted(String user, String roleName) {
    }

    public static void roleDeleteFail(String user, String roleName, Exception ex) {
    }

    public static void userRolesModified(String user, String targetUser, Set<String> oldRoles, Set<String> newRoles) {
    }

    public static void roleCreateFail(String user, String roleName, Exception ex) {
    }

    public static void roleModifyFail(String user, String roleName, Exception ex) {
    }

    public static void viewModified(String user, ViewPOJO oldView, ViewPOJO newView) {
    }

    public static void viewCreated(String user, ViewPOJO newView) {
    }

    public static void viewCreateOrModifyFail(String user, String viewName, Exception ex) {
    }

    public static void viewModifyFail(String user, String viewName, Exception ex) {
    }

    public static void viewCreateFail(String user, String viewName, Exception ex) {
    }

    public static void viewDeleted(String user, String viewName) {
    }

    public static void viewlDeleteFail(String user, String viewName, Exception e) {
    }

    public static void dataModelCreated(String user, DataModelPOJO dataModel) {
    }

    public static void dataModelDeleted(String user, String dataModelName) {
    }

    public static void dataModelModified(String user, DataModelPOJO oldDataModel, DataModelPOJO newDataModel) {
    }

    public static void dataModelCreateOrModifyFail(String user, String dataModelName, Exception ex) {
    }

    public static void dataModelDeleteFail(String user, String dataModelName, Exception ex) {
    }

    public static void dataModelModifyFail(String user, String dataModelName, Exception e) {
    }

    public static void dataModelCreateFail(String user, String dataModelName, Exception e) {
    }
}
