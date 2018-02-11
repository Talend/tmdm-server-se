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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.talend.mdm.commmon.util.core.ICoreConstants;

public class SecurityConfig {

    private static final Logger LOGGER = Logger.getLogger(SecurityConfig.class);

    private static final String PRIVATE_INTERNAL_USER = "MDMInternalUser"; //$NON-NLS-1$

    private static Set<String> SECURITY_PERMISSIONS;

    static {
        SECURITY_PERMISSIONS = new TreeSet<String>();
        SECURITY_PERMISSIONS.add(ICoreConstants.ADMIN_PERMISSION);
        SECURITY_PERMISSIONS.add(ICoreConstants.AUTHENTICATED_PERMISSION);
        SECURITY_PERMISSIONS.add(ICoreConstants.UI_AUTHENTICATED_PERMISSION);
    }

    private SecurityConfig() {
    }

    public static boolean isSecurityPermission(String role) {
        return SECURITY_PERMISSIONS.contains(role);
    }

    public static void invokeSynchronousPrivateInternal(Runnable runnable) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication != null) {
            LOGGER.warn("Private internal invocation with non-null authentication"); //$NON-NLS-1$
            runnable.run();
        } else {
            context = SecurityContextHolder.createEmptyContext();
            List<GrantedAuthority> roles = AuthorityUtils.createAuthorityList(ICoreConstants.ADMIN_PERMISSION,
                    ICoreConstants.AUTHENTICATED_PERMISSION);
            authentication = new UsernamePasswordAuthenticationToken(PRIVATE_INTERNAL_USER, "", roles); //$NON-NLS-1$
            context.setAuthentication(authentication);
            SyncTaskExecutor delegateExecutor = new SyncTaskExecutor();
            DelegatingSecurityContextExecutor executor = new DelegatingSecurityContextExecutor(delegateExecutor, context);
            executor.execute(runnable);
        }
    }
    
    public static String getInternalUserName(){
        return PRIVATE_INTERNAL_USER;
    }
}
