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

@XmlType(name="WSPutItemWithCustomReport")
public class WSPutItemWithCustomReport {
    protected com.amalto.core.webservice.WSPutItemWithReport wsPutItemWithReport;
    protected java.lang.String user;
    
    public WSPutItemWithCustomReport() {
    }
    
    public WSPutItemWithCustomReport(com.amalto.core.webservice.WSPutItemWithReport wsPutItemWithReport, java.lang.String user) {
        this.wsPutItemWithReport = wsPutItemWithReport;
        this.user = user;
    }
    
    public com.amalto.core.webservice.WSPutItemWithReport getWsPutItemWithReport() {
        return wsPutItemWithReport;
    }
    
    public void setWsPutItemWithReport(com.amalto.core.webservice.WSPutItemWithReport wsPutItemWithReport) {
        this.wsPutItemWithReport = wsPutItemWithReport;
    }
    
    public java.lang.String getUser() {
        return user;
    }
    
    public void setUser(java.lang.String user) {
        this.user = user;
    }
}
