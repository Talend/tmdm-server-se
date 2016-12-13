/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.util;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SessionContextHolder {

    private static final Logger LOG = Logger.getLogger(SessionContextHolder.class);

    public static HttpSession currentSession() {
        HttpSession session;
        RequestAttributes requestAttrs = RequestContextHolder.currentRequestAttributes();
        if (requestAttrs instanceof ServletRequestAttributes) {
            ServletRequestAttributes servletRequestAttrs = (ServletRequestAttributes) requestAttrs;
            session = servletRequestAttrs.getRequest().getSession(false); // do not create a session
        } else {
            // Unknown context
            session = null;
        }
        if (LOG.isTraceEnabled()) {
            if (session == null) {
                LOG.trace("Called with null session"); //$NON-NLS-1$
            } else {
                LOG.trace("Session id: " + session.getId() + " ;creation: " + new Date(session.getCreationTime()) //$NON-NLS-1$ //$NON-NLS-2$
                        + " ;last access: " + new Date(session.getLastAccessedTime())); //$NON-NLS-1$
            }
        }
        return session;
    }
}
