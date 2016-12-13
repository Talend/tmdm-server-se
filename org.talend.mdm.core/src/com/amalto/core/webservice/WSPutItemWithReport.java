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

@XmlType(name="WSPutItemWithReport")
public class WSPutItemWithReport {
    protected com.amalto.core.webservice.WSPutItem wsPutItem;
    protected java.lang.String source;
    protected java.lang.Boolean invokeBeforeSaving;
    
    public WSPutItemWithReport() {
    }
    
    public WSPutItemWithReport(com.amalto.core.webservice.WSPutItem wsPutItem, java.lang.String source, java.lang.Boolean invokeBeforeSaving) {
        this.wsPutItem = wsPutItem;
        this.source = source;
        this.invokeBeforeSaving = invokeBeforeSaving;
    }
    
    public com.amalto.core.webservice.WSPutItem getWsPutItem() {
        return wsPutItem;
    }
    
    public void setWsPutItem(com.amalto.core.webservice.WSPutItem wsPutItem) {
        this.wsPutItem = wsPutItem;
    }
    
    public java.lang.String getSource() {
        return source;
    }
    
    public void setSource(java.lang.String source) {
        this.source = source;
    }
    
    public java.lang.Boolean getInvokeBeforeSaving() {
        return invokeBeforeSaving;
    }
    
    public void setInvokeBeforeSaving(java.lang.Boolean invokeBeforeSaving) {
        this.invokeBeforeSaving = invokeBeforeSaving;
    }
}
