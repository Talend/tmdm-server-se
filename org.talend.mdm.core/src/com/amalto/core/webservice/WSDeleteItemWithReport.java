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

@XmlType(name="WSDeleteItemWithReport")
public class WSDeleteItemWithReport {
    protected com.amalto.core.webservice.WSItemPK wsItemPK;
    protected java.lang.String source;
    protected java.lang.String operateType;
    protected java.lang.String updatePath;
    protected java.lang.String user;
    protected java.lang.Boolean invokeBeforeSaving;
    protected java.lang.Boolean pushToUpdateReport;
    protected java.lang.Boolean override;
    
    public WSDeleteItemWithReport() {
    }
    
    public WSDeleteItemWithReport(com.amalto.core.webservice.WSItemPK wsItemPK, java.lang.String source, java.lang.String operateType, java.lang.String updatePath, java.lang.String user, java.lang.Boolean invokeBeforeSaving, java.lang.Boolean pushToUpdateReport, java.lang.Boolean override) {
        this.wsItemPK = wsItemPK;
        this.source = source;
        this.operateType = operateType;
        this.updatePath = updatePath;
        this.user = user;
        this.invokeBeforeSaving = invokeBeforeSaving;
        this.pushToUpdateReport = pushToUpdateReport;
        this.override = override;
    }
    
    public com.amalto.core.webservice.WSItemPK getWsItemPK() {
        return wsItemPK;
    }
    
    public void setWsItemPK(com.amalto.core.webservice.WSItemPK wsItemPK) {
        this.wsItemPK = wsItemPK;
    }
    
    public java.lang.String getSource() {
        return source;
    }
    
    public void setSource(java.lang.String source) {
        this.source = source;
    }
    
    public java.lang.String getOperateType() {
        return operateType;
    }
    
    public void setOperateType(java.lang.String operateType) {
        this.operateType = operateType;
    }
    
    public java.lang.String getUpdatePath() {
        return updatePath;
    }
    
    public void setUpdatePath(java.lang.String updatePath) {
        this.updatePath = updatePath;
    }
    
    public java.lang.String getUser() {
        return user;
    }
    
    public void setUser(java.lang.String user) {
        this.user = user;
    }
    
    public java.lang.Boolean getInvokeBeforeSaving() {
        return invokeBeforeSaving;
    }
    
    public void setInvokeBeforeSaving(java.lang.Boolean invokeBeforeSaving) {
        this.invokeBeforeSaving = invokeBeforeSaving;
    }
    
    public java.lang.Boolean getPushToUpdateReport() {
        return pushToUpdateReport;
    }
    
    public void setPushToUpdateReport(java.lang.Boolean pushToUpdateReport) {
        this.pushToUpdateReport = pushToUpdateReport;
    }
    
    public java.lang.Boolean getOverride() {
        return override;
    }
    
    public void setOverride(java.lang.Boolean override) {
        this.override = override;
    }
}
