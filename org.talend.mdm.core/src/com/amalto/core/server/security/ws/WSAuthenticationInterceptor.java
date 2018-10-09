/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server.security.ws;

import org.apache.cxf.interceptor.security.AuthenticationException;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.security.SecurityContext;

import com.amalto.core.util.LocalUser;

public class WSAuthenticationInterceptor extends AbstractPhaseInterceptor<Message> {

    public WSAuthenticationInterceptor() {
        super(Phase.UNMARSHAL);
    }

    @Override
    public void handleMessage(Message message) {
        message.put(SecurityContext.class, createSecurityContext());
    }

    private SecurityContext createSecurityContext() {
        try {
            return new WSSecurityContext(LocalUser.getLocalUser());
        } catch (Exception e) {
            throw new AuthenticationException("Failed to create security context." + e.getMessage());
        }
    }
}
