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

import com.amalto.core.objects.role.RolePOJO;

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
}
