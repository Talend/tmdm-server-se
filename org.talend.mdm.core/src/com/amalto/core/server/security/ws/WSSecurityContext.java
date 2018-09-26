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

import java.security.Principal;
import java.util.HashSet;

import org.apache.cxf.security.SecurityContext;

import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.server.security.MDMPrincipal;

public class WSSecurityContext implements SecurityContext {

    private ILocalUser user;

    WSSecurityContext(ILocalUser user) {
        this.user = user;
    }

    @Override
    public Principal getUserPrincipal() {
        return new MDMPrincipal(user.getUsername());
    }

    @Override
    public boolean isUserInRole(String role) {
        HashSet<String> roles = user.getRoles();
        return roles.contains(role);
    }

}
