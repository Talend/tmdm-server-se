/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox.util;

import java.io.File;
import java.util.Properties;

/**
 * A Jobox configuration object. See {@link #JoboxConfig(java.util.Properties)} for initialization documentation.
 *
 * @see #getJoboxHome()
 * @see #getDeployPath()
 * @see #getWorkPath()
 */
public class JoboxConfig {

    /**
     * Home for the Jobox container (deployment and working directory will be created as sub folders of this one).
     * Relative paths will be resolved from the "." directory returned by the JVM that executes Jobox.
     */
    public static final String JOBOX_HOME_PATH = "jobox_home_path";

    /**
     * Directory where jobs are deployed to.
     */
    private static final String DEPLOY_DIR_NAME = "deploy";

    /**
     * Directory where jobs are unzipped before they can be executed.
     */
    private static final String WORK_DIR_NAME = "work";

    /**
     * Current Jobox home (read from configuration properties).
     */
    private final String joboxHome;

    /**
     * Initializes a new Jobox configuration
     *
     * @param props Configuration properties that <b>MUST</b> contain the {@link #JOBOX_HOME_PATH} property.
     */
    public JoboxConfig(Properties props) {
        String configurationJoboxHome = props.getProperty(JOBOX_HOME_PATH);
        if (configurationJoboxHome != null) {
            joboxHome = configurationJoboxHome;
        } else {
            throw new IllegalArgumentException("Jobox home can not be null");
        }
    }

    /**
     * @return The Jobox home. This can not be null.
     */
    public String getJoboxHome() {
        return joboxHome;
    }

    /**
     * @return Returns the directory where jobs are deployed as ZIPs.
     */
    public String getDeployPath() {
        return joboxHome + File.separator + DEPLOY_DIR_NAME;
    }

    /**
     * @return Returns the directory where jobs are unzipped.
     */
    public String getWorkPath() {
        return joboxHome + File.separator + WORK_DIR_NAME;
    }

}
