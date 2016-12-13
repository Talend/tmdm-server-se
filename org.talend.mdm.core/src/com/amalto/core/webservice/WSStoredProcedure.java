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

@XmlType(name="WSStoredProcedure")
public class WSStoredProcedure {
    protected java.lang.String name;
    protected java.lang.String description;
    protected java.lang.String procedure;
    protected java.lang.Boolean refreshCache;
    
    public WSStoredProcedure() {
    }
    
    public WSStoredProcedure(java.lang.String name, java.lang.String description, java.lang.String procedure, java.lang.Boolean refreshCache) {
        this.name = name;
        this.description = description;
        this.procedure = procedure;
        this.refreshCache = refreshCache;
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
    
    public java.lang.String getProcedure() {
        return procedure;
    }
    
    public void setProcedure(java.lang.String procedure) {
        this.procedure = procedure;
    }
    
    public java.lang.Boolean getRefreshCache() {
        return refreshCache;
    }
    
    public void setRefreshCache(java.lang.Boolean refreshCache) {
        this.refreshCache = refreshCache;
    }
}
