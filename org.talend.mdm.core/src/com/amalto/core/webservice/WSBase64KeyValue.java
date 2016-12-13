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

@XmlType(name="WSBase64KeyValue")
public class WSBase64KeyValue {
    protected java.lang.String key;
    protected java.lang.String base64StringValue;
    
    public WSBase64KeyValue() {
    }
    
    public WSBase64KeyValue(java.lang.String key, java.lang.String base64StringValue) {
        this.key = key;
        this.base64StringValue = base64StringValue;
    }
    
    public java.lang.String getKey() {
        return key;
    }
    
    public void setKey(java.lang.String key) {
        this.key = key;
    }
    
    public java.lang.String getBase64StringValue() {
        return base64StringValue;
    }
    
    public void setBase64StringValue(java.lang.String base64StringValue) {
        this.base64StringValue = base64StringValue;
    }
}
