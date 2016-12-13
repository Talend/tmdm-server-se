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

@XmlType(name="WSRole")
public class WSRole {
    protected java.lang.String name;
    protected java.lang.String description;
    protected com.amalto.core.webservice.WSRoleSpecification[] specification;
    
    public WSRole() {
    }
    
    public WSRole(java.lang.String name, java.lang.String description, com.amalto.core.webservice.WSRoleSpecification[] specification) {
        this.name = name;
        this.description = description;
        this.specification = specification;
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
    
    public com.amalto.core.webservice.WSRoleSpecification[] getSpecification() {
        return specification;
    }
    
    public void setSpecification(com.amalto.core.webservice.WSRoleSpecification[] specification) {
        this.specification = specification;
    }
}
