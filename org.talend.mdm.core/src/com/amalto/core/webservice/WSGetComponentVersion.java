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

@XmlType(name="WSGetComponentVersion")
public class WSGetComponentVersion {
    protected com.amalto.core.webservice.WSComponent component;
    protected java.lang.String id;
    
    public WSGetComponentVersion() {
    }
    
    public WSGetComponentVersion(com.amalto.core.webservice.WSComponent component, java.lang.String id) {
        this.component = component;
        this.id = id;
    }
    
    public com.amalto.core.webservice.WSComponent getComponent() {
        return component;
    }
    
    public void setComponent(com.amalto.core.webservice.WSComponent component) {
        this.component = component;
    }
    
    public java.lang.String getId() {
        return id;
    }
    
    public void setId(java.lang.String id) {
        this.id = id;
    }
}
