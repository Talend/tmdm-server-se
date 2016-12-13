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

@XmlType(name="WSMDMJobArray")
public class WSMDMJobArray {
    protected com.amalto.core.webservice.WSMDMJob[] wsMDMJob;
    
    public WSMDMJobArray() {
    }
    
    public WSMDMJobArray(com.amalto.core.webservice.WSMDMJob[] wsMDMJob) {
        this.wsMDMJob = wsMDMJob;
    }
    
    public com.amalto.core.webservice.WSMDMJob[] getWsMDMJob() {
        return wsMDMJob;
    }
    
    public void setWsMDMJob(com.amalto.core.webservice.WSMDMJob[] wsMDMJob) {
        this.wsMDMJob = wsMDMJob;
    }
}
