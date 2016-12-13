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

@XmlType(name="WSTransformerPluginV2PutConfiguration")
public class WSTransformerPluginV2PutConfiguration {
    protected java.lang.String jndiName;
    protected java.lang.String configuration;
    
    public WSTransformerPluginV2PutConfiguration() {
    }
    
    public WSTransformerPluginV2PutConfiguration(java.lang.String jndiName, java.lang.String configuration) {
        this.jndiName = jndiName;
        this.configuration = configuration;
    }
    
    public java.lang.String getJndiName() {
        return jndiName;
    }
    
    public void setJndiName(java.lang.String jndiName) {
        this.jndiName = jndiName;
    }
    
    public java.lang.String getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(java.lang.String configuration) {
        this.configuration = configuration;
    }
}
