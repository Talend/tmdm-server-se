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

@XmlType(name="WSRoleSpecificationInstance")
public class WSRoleSpecificationInstance {
    protected java.lang.String instanceName;
    protected boolean writable;
    protected java.lang.String[] parameter;
    
    public WSRoleSpecificationInstance() {
    }
    
    public WSRoleSpecificationInstance(java.lang.String instanceName, boolean writable, java.lang.String[] parameter) {
        this.instanceName = instanceName;
        this.writable = writable;
        this.parameter = parameter;
    }
    
    public java.lang.String getInstanceName() {
        return instanceName;
    }
    
    public void setInstanceName(java.lang.String instanceName) {
        this.instanceName = instanceName;
    }
    
    public boolean isWritable() {
        return writable;
    }
    
    public void setWritable(boolean writable) {
        this.writable = writable;
    }
    
    public java.lang.String[] getParameter() {
        return parameter;
    }
    
    public void setParameter(java.lang.String[] parameter) {
        this.parameter = parameter;
    }
}
