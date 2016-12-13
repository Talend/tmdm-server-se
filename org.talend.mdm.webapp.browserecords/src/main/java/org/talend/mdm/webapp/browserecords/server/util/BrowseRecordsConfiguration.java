/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class BrowseRecordsConfiguration {

    private static final Logger logger = Logger.getLogger(BrowseRecordsConfiguration.class);

    private static final String CONFIG_PROPERTIES_PATH = "org/talend/mdm/webapp/browserecords/BrowseRecords.properties"; //$NON-NLS-1$

    private static Properties properties;

    public static final int TEXTAREA_THRESHOLD_DEFAULT_LENGTH = 60;

    public static void reload() throws IOException {
        properties = new Properties();
        properties.load(BrowseRecordsConfiguration.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES_PATH));
    }

    private static Properties getProperties() throws IOException {
        if (properties == null) {
            reload();
        }

        return properties;
    }

    public static String getPropertyValue(String strKey) {
        String value = null;

        try {
            properties = getProperties();
            value = properties.getProperty(strKey);
        } catch (IOException e) {
            logger.error("Error happened when you loading properties! ", e); //$NON-NLS-1$
        }

        return value;
    }

    public static int getAutoTextAreaLength() {
        String myLength = getPropertyValue("autoTextAreaLength");//$NON-NLS-1$

        if (myLength == null || myLength.trim().length() == 0)
            return TEXTAREA_THRESHOLD_DEFAULT_LENGTH;

        return Integer.parseInt(myLength);
    }

    public static boolean isAutoValidate() {
        String autoValidate = getPropertyValue("auto.validation.flag");//$NON-NLS-1$

        if (autoValidate == null || autoValidate.trim().length() == 0)
            return true;

        return Boolean.parseBoolean(autoValidate);
    }

    public static boolean dataMigrationMultiLingualFieldAuto() {
        String autoMigration = getPropertyValue("data.migration.multiLingualField.auto");//$NON-NLS-1$

        if (autoMigration == null || autoMigration.trim().length() == 0)
            return true;

        return Boolean.parseBoolean(autoMigration);
    }

    public static boolean IsUseRelations() {
        String useRelations = getPropertyValue("use.relations.flag");//$NON-NLS-1$

        if (useRelations == null || useRelations.trim().length() == 0)
            return true;

        return Boolean.parseBoolean(useRelations);
    }
}
