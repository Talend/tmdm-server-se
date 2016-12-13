/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.webservice;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="WSVersion")
public class WSVersion {
    protected int major;
    protected int minor;
    protected int revision;
    protected java.lang.String build;
    protected java.lang.String description;
    protected java.lang.String date;
    
    public WSVersion() {
    }
    
    public WSVersion(int major, int minor, int revision, java.lang.String build, java.lang.String description, java.lang.String date) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.build = build;
        this.description = description;
        this.date = date;
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
    
    public java.lang.String getBuild() {
        return build;
    }
    
    public void setBuild(java.lang.String build) {
        this.build = build;
    }
    
    public java.lang.String getDescription() {
        return description;
    }
    
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    public java.lang.String getDate() {
        return date;
    }
    
    public void setDate(java.lang.String date) {
        this.date = date;
    }
}
