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

@XmlType(name="WSMDMJob")
public class WSMDMJob {
    protected java.lang.String jobName;
    protected java.lang.String jobVersion;
    protected java.lang.String suffix;
    
    public WSMDMJob() {
    }
    
    public WSMDMJob(java.lang.String jobName, java.lang.String jobVersion, java.lang.String suffix) {
        this.jobName = jobName;
        this.jobVersion = jobVersion;
        this.suffix = suffix;
    }
    
    public java.lang.String getJobName() {
        return jobName;
    }
    
    public void setJobName(java.lang.String jobName) {
        this.jobName = jobName;
    }
    
    public java.lang.String getJobVersion() {
        return jobVersion;
    }
    
    public void setJobVersion(java.lang.String jobVersion) {
        this.jobVersion = jobVersion;
    }
    
    public java.lang.String getSuffix() {
        return suffix;
    }
    
    public void setSuffix(java.lang.String suffix) {
        this.suffix = suffix;
    }
}
