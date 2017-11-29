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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.SystemPropertyUtils;
import org.talend.logging.audit.AuditLoggerFactory;
import org.talend.logging.audit.Context;
import org.talend.logging.audit.ContextBuilder;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.audit.logger.impl.MDMEventAuditLogger;

@SuppressWarnings("nls")
public class MDMAuditLogger {

    private static final Logger LOGGER = Logger.getLogger(MDMAuditLogger.class);
    private static final String AUDIT_CONFIG_FILE_LOCATION = "talend.logging.audit.config";
    private static final MDMEventAuditLogger auditLogger;

    static {
        String auditConfigFileLocation = MDMConfiguration.getConfiguration().getProperty(AUDIT_CONFIG_FILE_LOCATION);
        if (StringUtils.isBlank(auditConfigFileLocation)) {
            LOGGER.warn("Audit is disabled.");
            auditLogger = null;
        } else {
            auditConfigFileLocation = SystemPropertyUtils.resolvePlaceholders(auditConfigFileLocation);
            LOGGER.info("Configuring audit using file '" + auditConfigFileLocation + "'");
            System.setProperty(AUDIT_CONFIG_FILE_LOCATION, auditConfigFileLocation);
            auditLogger = AuditLoggerFactory.getEventAuditLogger(MDMEventAuditLogger.class);
        }
    }

    private MDMAuditLogger() {
    }

    public static boolean isAuditEnabled() {
        return auditLogger != null;
    }

    public static void loginSuccess(String userName) {
        if (isAuditEnabled()) {
            Context ctx = ContextBuilder.create("user", userName).build();
            auditLogger.loginSuccess(ctx);
        }
    }

    public static void loginFail(String userName, Exception ex) {
        if (isAuditEnabled()) {
            Context ctx = ContextBuilder.create("user", userName).build();
            auditLogger.loginFail(ctx, ex);
        }
    }

    public static void logoutSuccess(String userName) {
        if (isAuditEnabled()) {
            Context ctx = ContextBuilder.create("user", userName).build();
            auditLogger.logoutSuccess(ctx);
        }
    }
}
