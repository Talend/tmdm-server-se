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

import java.util.HashMap;
import java.util.Map;

import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.server.ServerAccess;

public class WebappImpl implements Webapp {

    private static final String WELCOME_PORTLET_REFRESH_ONSTART = "welcomeportal.portlet.refresh.onstart"; //$NON-NLS-1$

    private static final String WELCOME_PORTLET_REFRESH_INTERVAL = "welcomeportal.portlet.refresh.interval"; //$NON-NLS-1$

    private static final int DEFAULT_WELCOME_PORTLET_REFRESH_INTERVAL = 10000;

    private static final int interval;

    private static final boolean onStart;

    static {
        Object intervalValueObject = MDMConfiguration.getConfiguration().get(WELCOME_PORTLET_REFRESH_INTERVAL);
        if (intervalValueObject != null) {
            interval = Integer.parseInt(String.valueOf(intervalValueObject));
        } else {
            interval = DEFAULT_WELCOME_PORTLET_REFRESH_INTERVAL;
        }

        Object onStartObject = MDMConfiguration.getConfiguration().get(WELCOME_PORTLET_REFRESH_ONSTART);
        if (onStartObject != null) {
            onStart = Boolean.parseBoolean(String.valueOf(onStartObject));
        } else {
            onStart = false;
        }
    }

    private ServerAccess serverAccess;

    public WebappImpl(ServerAccess serverAccess) {
        this.serverAccess = serverAccess;
    }

    @Override
    public ServerAccessInfo getInfo() {
        return serverAccess.getInfo();
    }

    @Override
    public int getWorkflowTasksCount() {
        return serverAccess.getWorkflowTasksCount();
    }
    
    @Override
    public int[] getDSCTasksCount() {
        return serverAccess.getDSCTasksCount();
    }

    @Override
    public boolean isExpired() throws Exception {
        return serverAccess.isExpired();
    }
    
    @Override
    public boolean isExpired(String language) throws Exception {
        return serverAccess.isExpired(language);
    }

    @Override
    public boolean isDataSteWardShip() throws Exception {
        return serverAccess.isDataSteWardShip();
    }

    @Override
    public boolean isEnterpriseVersion() {
        return serverAccess.isEnterpriseVersion();
    }

    @Override
    public String getLicenseWarning(String language) throws Exception {
        return serverAccess.getLicenseWarning(language);
    }

    @Override
    public Map<Boolean, Integer> getWelcomePortletConfig() {
        Map<Boolean, Integer> refreshConfig = new HashMap<Boolean, Integer>(1);
        refreshConfig.put(onStart, interval);
        return refreshConfig;
    }
    
    @Override
    public String getProductInfo() {
        ServerAccessInfo info = getInfo();
        return info.getProductName() + " " + info.getProductEdition(); //$NON-NLS-1$

    }
}
