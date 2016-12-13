/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.server.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.util.ResourceUtils;
import org.springframework.web.util.ServletContextPropertyUtils;

public class MDMContextListener implements ServletContextListener {

    public static final String ROOT_LOCATION_PARAM = "mdmRootLocation"; //$NON-NLS-1$

    public static final String ROOT_LOCATION_KEY = "mdm.root"; //$NON-NLS-1$

    public static final String ROOT_LOCATION_URL_KEY = "mdm.root.url"; //$NON-NLS-1$

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        String location = servletContext.getInitParameter(ROOT_LOCATION_PARAM);
        String resolvedLocation = ServletContextPropertyUtils.resolvePlaceholders(location, servletContext);
        servletContext.log("Initializing MDM root folder from [" + resolvedLocation + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            File file;
            if (ResourceUtils.isUrl(resolvedLocation)) {
                URL resolvedLocationURL = new URL(resolvedLocation);
                file = ResourceUtils.getFile(resolvedLocationURL);
            } else {
               file = ResourceUtils.getFile(resolvedLocation);
            }
            if (!file.exists()) {
                throw new FileNotFoundException("MDM Root folder [" + resolvedLocation + "] not found"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (!file.isDirectory()) {
                throw new FileNotFoundException("MDM Root folder [" + resolvedLocation + "] is not a directory"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            System.setProperty(ROOT_LOCATION_KEY, file.getAbsolutePath());
            System.setProperty(ROOT_LOCATION_URL_KEY, file.toURI().toURL().toString());
            servletContext.log("Set MDM root system property: '" + ROOT_LOCATION_KEY + "' = [" + file.getAbsolutePath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            servletContext.log("Set MDM root url system property: '" + ROOT_LOCATION_URL_KEY + "' = [" + file.toURI().toURL().toString() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Invalid '" + ROOT_LOCATION_PARAM + "' parameter", e); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid '" + ROOT_LOCATION_PARAM + "' parameter", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.getProperties().remove(ROOT_LOCATION_KEY);
        System.getProperties().remove(ROOT_LOCATION_URL_KEY);
    }

}
