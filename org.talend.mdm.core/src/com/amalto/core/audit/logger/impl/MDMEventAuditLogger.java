/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.audit.logger.impl;

import org.talend.logging.audit.AuditEvent;
import org.talend.logging.audit.StandardEventAuditLogger;

public interface MDMEventAuditLogger extends StandardEventAuditLogger {

    @AuditEvent(category = "security", message = "User has logged out successfully") //$NON-NLS-1$ //$NON-NLS-2$
    void logoutSuccess(Object... args);

    @AuditEvent(category = "active", message = "Role name has been changed") //$NON-NLS-1$ //$NON-NLS-2$
    void roleNameChanged(Object... args);

    @AuditEvent(category = "active", message = "Role description has been changed") //$NON-NLS-1$ //$NON-NLS-2$
    void roleDescriptionChanged(Object... args);

    @AuditEvent(category = "active", message = "Role digest has been changed") //$NON-NLS-1$ //$NON-NLS-2$
    void roleDigestChanged(Object... args);

    @AuditEvent(category = "active", message = "Role permision has been changed") //$NON-NLS-1$ //$NON-NLS-2$
    void rolePermisionChanged(Object... args);
}
