/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class GxtFactory {

    private static final Logger LOG = Logger.getLogger(GxtFactory.class);

    private static final String GXT_PROPERTIES = "mdm.gxt.properties"; //$NON-NLS-1$

    private static final String GXT_EXCLUDED_PROPERTIES = "mdm.gxt.excluded.properties"; //$NON-NLS-1$

    private static final String GXT_CSS_PROPERTIES = "mdm.gxt.css.properties"; //$NON-NLS-1$

    private static GxtFactory instance;

    /** the collection of actions that we know about */
    private Properties entries;

    private Properties excludedEntries;

    private Properties cssResources;

    public synchronized static final GxtFactory getInstance() {
        if (instance == null) {
            instance = new GxtFactory();
        }
        return instance;
    }

    private GxtFactory() {
        init();
    }
    
    private void init() {
        try {
            entries = loadProperties(GXT_PROPERTIES);
            excludedEntries = loadProperties(GXT_EXCLUDED_PROPERTIES);
            cssResources = loadProperties(GXT_CSS_PROPERTIES);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public String getGxtEntryModule(String context, String application) {
        String key = context + '.' + application;
        return entries.getProperty(key);
    }

    public String[] getGxtCss(String context, String application) {
        String key = context + '.' + application;
        String cssString = cssResources.getProperty(key);
        return StringUtils.split(cssString, ',');
    }

    public Boolean isExcluded(String context, String application) {
        String key = context + '.' + application;
        String value = excludedEntries.getProperty(key);
        return Boolean.valueOf(value);
    }
    
    private Properties loadProperties(String location) throws IOException {
        Enumeration<URL> resourceUrls = getClass().getClassLoader().getResources(location);
        Properties props = new Properties();
        URL url;
        while (resourceUrls.hasMoreElements()) {
            url = resourceUrls.nextElement();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Loading properties from " + url.getFile()); //$NON-NLS-1$
            }
            InputStream is = null;
            try {
                is = url.openStream();
                props.load(is);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        if(LOG.isDebugEnabled()) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            props.list(pw);
            LOG.debug("-- " + location + " --"); //$NON-NLS-1$ //$NON-NLS-2$            
            LOG.debug(sw.toString());
        }
        return props;
    }
}
