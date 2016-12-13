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

@XmlType(name="WSDeleteItem")
public class WSDeleteItem {
    protected com.amalto.core.webservice.WSItemPK wsItemPK;
    protected java.lang.Boolean override;
    protected java.lang.Boolean withReport = false;
    protected java.lang.String source = ""; //$NON-NLS-1$
    protected java.lang.Boolean invokeBeforeDeleting = false;

    public WSDeleteItem() {
    }

    public WSDeleteItem(com.amalto.core.webservice.WSItemPK wsItemPK, java.lang.Boolean override) {
        this.wsItemPK = wsItemPK;
        this.override = override;
    }

    public com.amalto.core.webservice.WSItemPK getWsItemPK() {
        return wsItemPK;
    }

    public void setWsItemPK(com.amalto.core.webservice.WSItemPK wsItemPK) {
        this.wsItemPK = wsItemPK;
    }

    public java.lang.Boolean getOverride() {
        return override;
    }

    public void setOverride(java.lang.Boolean override) {
        this.override = override;
    }

    public java.lang.Boolean getWithReport() {
        return withReport;
    }

    public void setWithReport(java.lang.Boolean withReport) {
        this.withReport = withReport;
    }

    public java.lang.String getSource() {
            return source;
        }

    public void setSource(java.lang.String source) {
        this.source = source;
    }

    public java.lang.Boolean getInvokeBeforeDeleting() {
            return invokeBeforeDeleting;
        }

    public void setInvokeBeforeDeleting(java.lang.Boolean invokeBeforeDeleting) {
        this.invokeBeforeDeleting = invokeBeforeDeleting;
    }
}
