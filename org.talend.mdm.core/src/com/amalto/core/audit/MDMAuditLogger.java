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

import org.apache.log4j.Logger;

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
}
