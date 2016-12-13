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

@XmlType(name="WSServiceGetDocument")
public class WSServiceGetDocument {
    protected java.lang.String description;
    protected java.lang.String configure;
    protected java.lang.String document;
    protected java.lang.String configureSchema;
    protected java.lang.String defaultConfig;
    
    public WSServiceGetDocument() {
    }
    
    public WSServiceGetDocument(java.lang.String description, java.lang.String configure, java.lang.String document, java.lang.String configureSchema, java.lang.String defaultConfig) {
        this.description = description;
        this.configure = configure;
        this.document = document;
        this.configureSchema = configureSchema;
        this.defaultConfig = defaultConfig;
    }
    
    public java.lang.String getDescription() {
        return description;
    }
    
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    public java.lang.String getConfigure() {
        return configure;
    }
    
    public void setConfigure(java.lang.String configure) {
        this.configure = configure;
    }
    
    public java.lang.String getDocument() {
        return document;
    }
    
    public void setDocument(java.lang.String document) {
        this.document = document;
    }
    
    public java.lang.String getConfigureSchema() {
        return configureSchema;
    }
    
    public void setConfigureSchema(java.lang.String configureSchema) {
        this.configureSchema = configureSchema;
    }
    
    public java.lang.String getDefaultConfig() {
        return defaultConfig;
    }
    
    public void setDefaultConfig(java.lang.String defaultConfig) {
        this.defaultConfig = defaultConfig;
    }
}
