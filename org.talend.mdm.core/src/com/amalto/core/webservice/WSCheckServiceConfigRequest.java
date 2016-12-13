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

@XmlType(name="WSCheckServiceConfigRequest")
public class WSCheckServiceConfigRequest {
    protected java.lang.String jndiName;
    protected java.lang.String conf;
    
    public WSCheckServiceConfigRequest() {
    }
    
    public WSCheckServiceConfigRequest(java.lang.String jndiName, java.lang.String conf) {
        this.jndiName = jndiName;
        this.conf = conf;
    }
    
    public java.lang.String getJndiName() {
        return jndiName;
    }
    
    public void setJndiName(java.lang.String jndiName) {
        this.jndiName = jndiName;
    }
    
    public java.lang.String getConf() {
        return conf;
    }
    
    public void setConf(java.lang.String conf) {
        this.conf = conf;
    }
}
