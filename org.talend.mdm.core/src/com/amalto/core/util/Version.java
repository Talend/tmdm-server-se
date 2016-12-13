/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Version {

    private static Properties props;

    private static final Logger LOG = Logger.getLogger(Version.class);

    private static final String PROP_FILE = "/product_version.properties"; //$NON-NLS-1$

    /**
     * Returns a <code>Version</code> object holding the package version implementation
     */
    public static Version getVersion(Class<?> clazz) {
        if (props == null)
            loadProps(clazz);
        return new Version(Integer.parseInt(props.getProperty("major")), //$NON-NLS-1$
                Integer.parseInt(props.getProperty("minor")), //$NON-NLS-1$
                Integer.parseInt(props.getProperty("rev")), //$NON-NLS-1$
                props.getProperty("build.number"), //$NON-NLS-1$
                props.getProperty("description"), //$NON-NLS-1$
                props.getProperty("build.date") //$NON-NLS-1$
        );
    }

    /**
     * Returns <code>String</code> representation of package version information.
     */
    public static String getVersionAsString(Class<?> clazz) {
        if (props == null)
            loadProps(clazz);
        return getVersionAsString();
    }
    
    public static String getSimpleVersionAsString(Class<?> clazz) {
        if (props == null)
            loadProps(clazz);
        return "v" + props.getProperty("major") + '.' + props.getProperty("minor") + '.' + props.getProperty("rev") + '-' //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + props.getProperty("build.number"); //$NON-NLS-1$
    }

    private static String getVersionAsString() {
        return "v" + props.getProperty("major") + '.' //$NON-NLS-1$ //$NON-NLS-2$
                + props.getProperty("minor") + '.' + props.getProperty("rev") //$NON-NLS-1$ //$NON-NLS-2$
                + '-' + props.getProperty("build.number") + ' ' //$NON-NLS-1$
                + props.getProperty("build.date") + " : " //$NON-NLS-1$ //$NON-NLS-2$
                + props.getProperty("description"); //$NON-NLS-1$
    }

    // load props as resource on classpath
    private static void loadProps(Class<?> clazz) {
        InputStream is;
        props = new Properties();
        is = clazz.getResourceAsStream(PROP_FILE);
        if (is == null) {
            props.setProperty("major", "0");
            props.setProperty("minor", "0");
            props.getProperty("build.number", "00000");
            props.getProperty("build.date", "1970/01/01");
            props.getProperty("description", "N/A");
            return;
        }
        try {
            props.load(is);
            is.close();
        } catch (IOException ioe) {
            LOG.error(ioe.getMessage(), ioe);
        }
    }

    @Override
    /**
     * Returns a Version String with Release Notes
     * @return the Version String
     */
    public String toString() {
        return getVersionAsString();
    }

    /*********************************************************************************
     * Bean Implementation
     *********************************************************************************/

    private int major;

    private int minor;

    private int revision;

    private String build;

    private String description;

    private String date;

    public Version(int major, int minor, int revision, String build, String description, String date) {
        super();
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.build = build;
        this.description = description;
        this.date = date;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }
}
