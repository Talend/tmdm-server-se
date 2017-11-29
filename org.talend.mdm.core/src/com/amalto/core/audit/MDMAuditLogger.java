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

import org.apache.log4j.Logger;
import org.springframework.util.SystemPropertyUtils;
import org.talend.logging.audit.AuditLoggerFactory;
import org.talend.logging.audit.Context;
import org.talend.logging.audit.ContextBuilder;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.audit.logger.impl.MDMEventAuditLogger;
import com.amalto.core.objects.role.RolePOJO;
import com.amalto.core.util.LocalUser;
import com.google.common.base.Strings;

import net.sf.saxon.exslt.Sets;

@SuppressWarnings("nls")
public class MDMAuditLogger {

    private static final Logger LOGGER = Logger.getLogger(MDMAuditLogger.class);
    private static final String AUDIT_CONFIG_FILE_LOCATION = "talend.logging.audit.config";
    private static final MDMEventAuditLogger auditLogger;

    static {
        String auditConfigFileLocation = MDMConfiguration.getConfiguration().getProperty(AUDIT_CONFIG_FILE_LOCATION);
        if (auditConfigFileLocation == null) {
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

    public static void roleCreated(String user, RolePOJO role) {
        if (isAuditEnabled()) {
            Context ctx = ContextBuilder.create("user", user).build();
            ctx.put("role name", Strings.nullToEmpty(role.getName()));
            ctx.put("role description", Strings.nullToEmpty(role.getDescription()));
            ctx.put("role digest", Strings.nullToEmpty(role.getDigest()));
            ctx.put("role permision", String.valueOf(role.getRoleSpecifications()));
            auditLogger.roleCreated(ctx);
        }
    }

    public static void roleChanged(String user, RolePOJO oldRole, RolePOJO newRole) {
        if (isAuditEnabled()) {
            Context ctx = ContextBuilder.create("user", user).build();
            if (!oldRole.getName().equals(newRole.getName())) {
                ctx.put("old role name", oldRole.getName());
                ctx.put("new role name", newRole.getName());
                auditLogger.roleNameChanged(ctx);
            }

            if (!oldRole.getDescription().equals(newRole.getDescription())) {
                ctx.put("old role description", oldRole.getDescription());
                ctx.put("new role description", newRole.getDescription());
                auditLogger.roleDescriptionChanged(ctx);
            }

            if (!oldRole.getDigest().equals(newRole.getDigest())) {
                ctx.put("old role digest", oldRole.getDigest());
                ctx.put("new role digest", newRole.getDigest());
                auditLogger.roleDigestChanged(ctx);
            }

            if (!oldRole.getRoleSpecifications().equals(newRole.getRoleSpecifications())) {
                ctx.put("old role permision", oldRole.getRoleSpecifications().toString());
                ctx.put("new role permision", newRole.getRoleSpecifications().toString());
                auditLogger.rolePermisionChanged(ctx);
            }
        }
    }

    public static void roleDeleted(String user, String roleName) {
        if (isAuditEnabled()) {
            Context ctx = ContextBuilder.create("user", user).build();
            ctx.put("role name", roleName);
            auditLogger.roleDeleted(ctx);
        }
    }

    public static void userRoleChanged(String user, String assignedUser, Set<String> oldRoles, Set<String> newRoles) {
        if (isAuditEnabled()) {

            if (oldRoles == null && newRoles != null) {
                for (String role : newRoles) {
                    Context ctx = ContextBuilder.create("user", user).build();
                    ctx.put("assigned user", assignedUser);
                    ctx.put("assigned role", role);
                    auditLogger.roleAssigned(user, newRoles, role);
                }
            } else if (oldRoles != null && newRoles == null) {
                for (String role : oldRoles) {
                    Context ctx = ContextBuilder.create("user", user).build();
                    ctx.put("rovoked user", assignedUser);
                    ctx.put("rovoked role", role);
                    auditLogger.roleRevoked(ctx);
                }
            }
            oldRoles.forEach((role) -> {
                if (!newRoles.contains(role)) {
                    Context ctx = ContextBuilder.create("user", user).build();
                    ctx.put("rovoked user", assignedUser);
                    ctx.put("rovoked role", role);
                    auditLogger.roleRevoked(ctx);
                }
            });

            newRoles.forEach((role) -> {
                if (!oldRoles.contains(role)) {
                    Context ctx = ContextBuilder.create("user", user).build();
                    ctx.put("assigned user", assignedUser);
                    ctx.put("assigned role", role);
                    auditLogger.roleAssigned(ctx);
                }
            });
        }
    }
}
