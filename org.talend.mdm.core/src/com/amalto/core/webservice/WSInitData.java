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

@XmlType(name="WSInitData")
public class WSInitData {
    protected boolean zap;
    protected java.lang.String xmlSchema;
    
    public WSInitData() {
    }
    
    public WSInitData(boolean zap, java.lang.String xmlSchema) {
        this.zap = zap;
        this.xmlSchema = xmlSchema;
    }
    
    public boolean isZap() {
        return zap;
    }
    
    public void setZap(boolean zap) {
        this.zap = zap;
    }
    
    public java.lang.String getXmlSchema() {
        return xmlSchema;
    }
    
    public void setXmlSchema(java.lang.String xmlSchema) {
        this.xmlSchema = xmlSchema;
    }
}
