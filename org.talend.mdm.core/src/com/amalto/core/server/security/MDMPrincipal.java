/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MDMPrincipal implements Principal, Serializable {

    private static final long serialVersionUID = -5870428831570655056L;

    private final String username;

    private final Set<String> roles;

    public MDMPrincipal(String username) {
        this.username = username;
        roles = new HashSet<String>();
    }

    @Override
    public String getName() {
        return username;
    }

    public void addRole(String role) {
        this.roles.add(role);
    }

    public void addRoles(Collection<String> roles) {
        this.roles.addAll(roles);
    }

    public Set<String> getRoles() {
        return roles;
    }

    public int hashCode() {
        return username.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof MDMPrincipal) {
            MDMPrincipal that = (MDMPrincipal) obj;
            if (username.equals(that.username))
                return true;
        }
        return false;
    }
}
