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

    private static final String AUDIT_CONFIG_FILE_LOCATION = "talend.logging.audit.config";  //$NON-NLS-1$

    private static MDMEventAuditLogger auditLogger;

    static {
        System.setProperty(AUDIT_CONFIG_FILE_LOCATION,
                SystemPropertyUtils.resolvePlaceholders(MDMConfiguration.getConfiguration().getProperty(AUDIT_CONFIG_FILE_LOCATION)));
        auditLogger = AuditLoggerFactory.getEventAuditLogger(MDMEventAuditLogger.class);
    }

    private MDMAuditLogger() {
    }

    public static void loginSuccess(String userName) {
        Context ctx = ContextBuilder.create("user", userName).build(); //$NON-NLS-1$
        auditLogger.loginSuccess(ctx);
    }

    public static void loginFail(String userName, Exception ex) {
        Context ctx = ContextBuilder.create("user", userName).build(); //$NON-NLS-1$
        auditLogger.loginFail(ctx, ex);
    }

    public static void logoutSuccess(String userName) {
        Context ctx = ContextBuilder.create("user", userName).build(); //$NON-NLS-1$
        auditLogger.logoutSuccess(ctx);
    }
}
