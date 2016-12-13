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

@XmlType(name="WSDataModel")
public class WSDataModel {
    protected java.lang.String name;
    protected java.lang.String description;
    protected java.lang.String xsdSchema;
    
    public WSDataModel() {
    }
    
    public WSDataModel(java.lang.String name, java.lang.String description, java.lang.String xsdSchema) {
        this.name = name;
        this.description = description;
        this.xsdSchema = xsdSchema;
    }
    
    public java.lang.String getName() {
        return name;
    }
    
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    public java.lang.String getDescription() {
        return description;
    }
    
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    public java.lang.String getXsdSchema() {
        return xsdSchema;
    }
    
    public void setXsdSchema(java.lang.String xsdSchema) {
        this.xsdSchema = xsdSchema;
    }
}
