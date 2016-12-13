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

@XmlType(name="WSDropItem")
public class WSDropItem {
    protected com.amalto.core.webservice.WSItemPK wsItemPK;
    protected java.lang.String partPath;
    protected java.lang.Boolean override;
    protected java.lang.Boolean withReport = false;
    protected java.lang.String source = ""; //$NON-NLS-1$
    protected java.lang.Boolean invokeBeforeDeleting = false;

    public WSDropItem() {
    }
    
    public WSDropItem(com.amalto.core.webservice.WSItemPK wsItemPK, java.lang.String partPath, java.lang.Boolean override) {
        this.wsItemPK = wsItemPK;
        this.partPath = partPath;
        this.override = override;
    }

    public java.lang.String getSource() {
        return source;
    }

    public void setSource(java.lang.String source) {
        this.source = source;
    }

    public Boolean getWithReport() {
        return withReport;
    }

    public void setWithReport(Boolean withReport) {
        this.withReport = withReport;
    }

    public Boolean getInvokeBeforeDeleting() {
        return invokeBeforeDeleting;
    }

    public void setInvokeBeforeDeleting(Boolean invokeBeforeDeleting) {
        this.invokeBeforeDeleting = invokeBeforeDeleting;
    }

    public com.amalto.core.webservice.WSItemPK getWsItemPK() {
        return wsItemPK;
    }
    
    public void setWsItemPK(com.amalto.core.webservice.WSItemPK wsItemPK) {
        this.wsItemPK = wsItemPK;
    }
    
    public java.lang.String getPartPath() {
        return partPath;
    }
    
    public void setPartPath(java.lang.String partPath) {
        this.partPath = partPath;
    }
    
    public java.lang.Boolean getOverride() {
        return override;
    }
    
    public void setOverride(java.lang.Boolean override) {
        this.override = override;
    }
}
