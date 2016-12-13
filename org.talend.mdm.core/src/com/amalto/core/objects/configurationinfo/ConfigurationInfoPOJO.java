/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.configurationinfo;

import java.util.HashMap;
import java.util.HashSet;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;

public class ConfigurationInfoPOJO extends ObjectPOJO{

    private String name;

    private int major;

    private int minor;

    private int revision;

    private String build;

    private String releaseNote = ""; //$NON-NLS-1$

    private String date;

    private HashMap<String, String> properties = new HashMap<String, String>();

    public ConfigurationInfoPOJO() {
    }

    public ConfigurationInfoPOJO(String name) {
        this.name = name;
    }

    /**
     * @return Returns the Name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
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

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getReleaseNote() {
        return releaseNote;
    }

    public void setReleaseNote(String releaseNote) {
        this.releaseNote = releaseNote;
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

    @Override
    public ObjectPOJOPK getPK() {
        if (getName() == null) {
            return null;
        }
        return new ObjectPOJOPK(new String[]{name});
    }

    /**
     * ************************************************************************************
     * Helpers
     * *************************************************************************************
     */
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public String removeProperty(String key) {
        return properties.remove(key);
    }

    public HashSet<String> getPropertyKeys() {
        return new HashSet<String>(properties.keySet());
    }

    public String getVersionString() {
        return major + '.' + minor + '.' + revision + '-' + build + ' ' + releaseNote;
    }

}
