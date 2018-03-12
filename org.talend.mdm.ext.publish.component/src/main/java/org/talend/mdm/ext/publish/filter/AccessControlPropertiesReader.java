/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish.filter;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class AccessControlPropertiesReader {

    private static Logger log = Logger.getLogger(AccessControlPropertiesReader.class);

    private static final String configFileName = "AccessControllerConfig.properties";//$NON-NLS-1$

    /** unique instance */
    private static AccessControlPropertiesReader sInstance = null;

    private Properties configProperties = null;

    /**
     * Private constuctor
     */
    private AccessControlPropertiesReader() {
        super();
    }

    /**
     * Get the unique instance of this class.
     */
    public static synchronized AccessControlPropertiesReader getInstance() {

        if (sInstance == null) {
            sInstance = new AccessControlPropertiesReader();
        }

        return sInstance;

    }

    private Properties getProperties() {

        if (configProperties == null) {
            // reload
            configProperties = new Properties();
            try {
                InputStream is = getClass().getClassLoader().getResourceAsStream(configFileName);
                configProperties.load(is);
                if (is != null)
                    is.close();
            } catch (Exception e) {
                log.error(" File " + configFileName + " not found", e);
            }
        } else {

        }

        return configProperties;
    }

    public String getProperty(String key) {

        return getProperties().getProperty(key);
    }

    // test
    public static void main(String[] args) {

        System.out.println(AccessControlPropertiesReader.getInstance().getProperty("datamodel.name.ban.pattern"));

    }

}
