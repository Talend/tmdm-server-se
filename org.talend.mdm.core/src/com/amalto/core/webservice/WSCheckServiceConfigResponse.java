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

@XmlType(name="WSCheckServiceConfigResponse")
public class WSCheckServiceConfigResponse {
    protected java.lang.Boolean checkResult;
    
    public WSCheckServiceConfigResponse() {
    }
    
    public WSCheckServiceConfigResponse(java.lang.Boolean checkResult) {
        this.checkResult = checkResult;
    }
    
    public java.lang.Boolean getCheckResult() {
        return checkResult;
    }
    
    public void setCheckResult(java.lang.Boolean checkResult) {
        this.checkResult = checkResult;
    }
}
