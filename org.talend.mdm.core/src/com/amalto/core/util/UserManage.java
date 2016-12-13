/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.util.Set;

import org.talend.mdm.commmon.util.webapp.XSystemObjects;

public abstract class UserManage {

    public static final String PROVISIONING_CLUSTER = XSystemObjects.DC_PROVISIONING.getName();

    public static final String USER_CONCEPT = "User"; //$NON-NLS-1$

    public final int getViewerUsers() {
        return getUserCount(XSystemObjects.ROLE_DEFAULT_VIEWER.getName());
    }

    public final int getNormalUsers() {
        return getUserCount(XSystemObjects.ROLE_DEFAULT_USER.getName()) + getUserCount(XSystemObjects.ROLE_DEFAULT_WEB.getName());
    }

    public final int getNBAdminUsers() {
        return getUserCount(XSystemObjects.ROLE_DEFAULT_ADMIN.getName());
    }

    public abstract int getActiveUsers();

    public abstract boolean isExistUser(User user);

    public abstract boolean isActivatingUser(User user);

    public abstract Set<String> getOriginalRole(User user);

    protected abstract int getUserCount(String role);
}
