/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.server.servlet;

import static com.amalto.core.util.Util.ROOT_LOCATION_KEY;
import static com.amalto.core.util.Util.ROOT_LOCATION_PARAM;
import static com.amalto.core.util.Util.ROOT_LOCATION_URL_KEY;
import static com.amalto.core.util.Util.WEB_SESSION_TIMEOUT_IN_SECONDS;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.springframework.util.ResourceUtils;
import org.springframework.web.util.ServletContextPropertyUtils;

public class MDMContextListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(MDMContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();

        int webSessionTimeoutInSeconds = getSessionTimeoutInSeconds(servletContext);
        System.setProperty(WEB_SESSION_TIMEOUT_IN_SECONDS, webSessionTimeoutInSeconds + ""); //$NON-NLS-1$

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

    private int getSessionTimeoutInSeconds(ServletContext servletContext) {
        int webSessionTimeoutInSeconds;
        try {
            webSessionTimeoutInSeconds = servletContext.getSessionTimeout() * 60;
        } catch (Exception e) {
            LOGGER.warn("Failed to retrieve session timeout, using default value of 30 mins.", e); //$NON-NLS-1$
            webSessionTimeoutInSeconds = 30 * 60;
        }
        return webSessionTimeoutInSeconds;
    }
}