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

import java.util.Map;
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

    public static void userCreateSuccess(String user, Map<String, String> newUser) {
    }

    public static void userRolesModifySuccess(String user, String targetUser, Set<String> oldRoles, Set<String> newRoles) {
    }

    public static void userModifyFail(String user, String userName, Exception e) {
    }

    public static void userCreateFail(String user, String userName, Exception e) {
    }

    public static void userDeleteSuccess(String user, String userName) {
    }

    public static void userDeleteFail(String user, String userName, Exception e) {
    }

    public static void roleCreateSuccess(String user, RolePOJO role) {
    }

    public static void roleCreateOrModifyFail(String user, String roleName, Exception ex) {
    }

    public static void roleModifySuccess(String user, RolePOJO oldRole, RolePOJO newRole) {
    }

    public static void roleDeleteSuccess(String user, String roleName) {
    }

    public static void roleDeleteFail(String user, String roleName, Exception ex) {
    }

    public static void roleCreateFail(String user, String roleName, Exception ex) {
    }

    public static void roleModifyFail(String user, String roleName, Exception ex) {
    }

    public static void viewModifySuccess(String user, ViewPOJO oldView, ViewPOJO newView) {
    }

    public static void viewCreateSuccess(String user, ViewPOJO newView) {
    }

    public static void viewCreateOrModifyFail(String user, String viewName, Exception ex) {
    }

    public static void viewModifyFail(String user, String viewName, Exception ex) {
    }

    public static void viewCreateFail(String user, String viewName, Exception ex) {
    }

    public static void viewDeleteSuccess(String user, String viewName) {
    }

    public static void viewlDeleteFail(String user, String viewName, Exception e) {
    }

    public static void dataModelCreateSuccess(String user, DataModelPOJO dataModel) {
    }

    public static void dataModelDeleteSuccess(String user, String dataModelName) {
    }

    public static void dataModelModifySuccess(String user, DataModelPOJO oldDataModel, DataModelPOJO newDataModel) {
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
