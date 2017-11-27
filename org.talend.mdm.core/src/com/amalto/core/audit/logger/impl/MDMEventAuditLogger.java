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

    @AuditEvent(category = "security", message = "User has logged out successfully")
    void logoutSuccess(Object... args);
}
