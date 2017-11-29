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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.amalto.core.objects.role.RolePOJO;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;

@SuppressWarnings("nls")
public class MDMAuditLogger {

    private static final Logger LOGGER = Logger.getLogger(MDMAuditLogger.class);

    public static void loginSuccess(String userName) {
        JsonObject object = new JsonObject();
        object.addProperty("logMessage", "User has logged in successfully");
        object.addProperty("user", userName);
        LOGGER.info(object.toString());
    }

    public static void loginFail(String userName, Exception ex) {
        JsonObject object = new JsonObject();
        object.addProperty("logMessage", "User login attempt failed");
        object.addProperty("user", userName);
        LOGGER.info(object.toString());
    }

    public static void logoutSuccess(String userName) {
        JsonObject object = new JsonObject();
        object.addProperty("logMessage", "User has logged out successfully");
        object.addProperty("user", userName);
        LOGGER.info(object.toString());
    }

    public static void roleCreated(String user, RolePOJO role) {
        JsonObject object = new JsonObject();
        object.addProperty("logMessage", "Role has been created");
        object.addProperty("user", user);
        object.addProperty("roleName", Strings.nullToEmpty(role.getName()));
        object.addProperty("roleDescription", Strings.nullToEmpty(role.getDescription()));
        LOGGER.info(object.toString());
    }

    public static void roleModified(String user, RolePOJO oldRole, RolePOJO newRole) {
        if (!StringUtils.equals(oldRole.getDescription(), newRole.getDescription())) {
            JsonObject object = new JsonObject();
            object.addProperty("logMessage", "Role has been modified");
            object.addProperty("user", user);
            object.addProperty("roleDescriptionChange",
                    Strings.nullToEmpty(oldRole.getDescription()) + " -> " + Strings.nullToEmpty(newRole.getDescription()));
            LOGGER.info(object.toString());
        }
    }

    public static void roleDeleted(String user, String roleName) {
        JsonObject object = new JsonObject();
        object.addProperty("logMessage", "Role has been deleted");
        object.addProperty("user", user);
        object.addProperty("roleName", Strings.nullToEmpty(roleName));
        LOGGER.info(object.toString());
    }

}
