/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server.security.jaas;

import java.security.Principal;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.security.authentication.jaas.AuthorityGranter;

import com.amalto.core.server.security.MDMPrincipal;

public class DefaultAuthorityGranter implements AuthorityGranter {

    @Override
    public Set<String> grant(Principal principal) {
        Set<String> set = new TreeSet<String>();
        if (principal instanceof MDMPrincipal) {
            set.addAll(((MDMPrincipal) principal).getRoles());
        }
        return set;
    }

}
