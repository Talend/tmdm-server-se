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

@XmlType(name="WSFindBackgroundJobPKs")
public class WSFindBackgroundJobPKs {
    protected com.amalto.core.webservice.BackgroundJobStatusType status;
    
    public WSFindBackgroundJobPKs() {
    }
    
    public WSFindBackgroundJobPKs(com.amalto.core.webservice.BackgroundJobStatusType status) {
        this.status = status;
    }
    
    public com.amalto.core.webservice.BackgroundJobStatusType getStatus() {
        return status;
    }
    
    public void setStatus(com.amalto.core.webservice.BackgroundJobStatusType status) {
        this.status = status;
    }
}
