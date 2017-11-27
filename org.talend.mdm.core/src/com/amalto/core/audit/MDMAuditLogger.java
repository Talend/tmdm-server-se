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

import org.springframework.util.SystemPropertyUtils;
import org.talend.logging.audit.AuditLoggerFactory;
import org.talend.logging.audit.Context;
import org.talend.logging.audit.ContextBuilder;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.audit.logger.impl.MDMEventAuditLogger;

public class MDMAuditLogger {

    private static final String SYSPROP_CONFIG_FILE = "talend.logging.audit.config";

    private static MDMEventAuditLogger auditLogger;

    static {
        System.setProperty(SYSPROP_CONFIG_FILE,
                SystemPropertyUtils.resolvePlaceholders(MDMConfiguration.getConfiguration().getProperty(SYSPROP_CONFIG_FILE)));
        auditLogger = AuditLoggerFactory.getEventAuditLogger(MDMEventAuditLogger.class);
    }

    private MDMAuditLogger() {
    }

    public static void loginSuccess(String userName) {
        Context ctx = ContextBuilder.create("user", userName).build();
        auditLogger.loginSuccess(ctx);
    }

    public static void loginFail(String userName, Exception ex) {
        Context ctx = ContextBuilder.create("user", userName).build();
        auditLogger.loginFail(ctx, ex);
    }

    public static void logoutSuccess(String userName) {
        Context ctx = ContextBuilder.create("user", userName).build();
        auditLogger.logoutSuccess(ctx);
    }
}
