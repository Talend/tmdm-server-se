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

@XmlType(name="WSRoleSpecification")
public class WSRoleSpecification {
    protected java.lang.String objectType;
    protected boolean admin;
    protected com.amalto.core.webservice.WSRoleSpecificationInstance[] instance;
    
    public WSRoleSpecification() {
    }
    
    public WSRoleSpecification(java.lang.String objectType, boolean admin, com.amalto.core.webservice.WSRoleSpecificationInstance[] instance) {
        this.objectType = objectType;
        this.admin = admin;
        this.instance = instance;
    }
    
    public java.lang.String getObjectType() {
        return objectType;
    }
    
    public void setObjectType(java.lang.String objectType) {
        this.objectType = objectType;
    }
    
    public boolean isAdmin() {
        return admin;
    }
    
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
    
    public com.amalto.core.webservice.WSRoleSpecificationInstance[] getInstance() {
        return instance;
    }
    
    public void setInstance(com.amalto.core.webservice.WSRoleSpecificationInstance[] instance) {
        this.instance = instance;
    }
}
