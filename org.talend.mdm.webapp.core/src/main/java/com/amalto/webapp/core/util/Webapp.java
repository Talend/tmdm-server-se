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

import java.util.Map;

import com.amalto.core.server.ServerAccess;

public interface Webapp extends ServerAccess {

    public Map<Boolean, Integer> getWelcomePortletConfig();
    
    public String getProductInfo();

    public static final Webapp INSTANCE = WebappFactory.createWebapp();

    public static final class WebappFactory {

        private WebappFactory() {
        }

        private static Webapp createWebapp() {
            try {
                return new WebappImpl(ServerAccess.INSTANCE);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}