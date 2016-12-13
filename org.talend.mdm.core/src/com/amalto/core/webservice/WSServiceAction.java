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

@XmlType(name="WSServiceAction")
public class WSServiceAction {
    protected java.lang.String jndiName;
    protected com.amalto.core.webservice.WSServiceActionCode wsAction;
    protected java.lang.String methodName;
    protected java.lang.String[] methodParameters;
    
    public WSServiceAction() {
    }
    
    public WSServiceAction(java.lang.String jndiName, com.amalto.core.webservice.WSServiceActionCode wsAction, java.lang.String methodName, java.lang.String[] methodParameters) {
        this.jndiName = jndiName;
        this.wsAction = wsAction;
        this.methodName = methodName;
        this.methodParameters = methodParameters;
    }
    
    public java.lang.String getJndiName() {
        return jndiName;
    }
    
    public void setJndiName(java.lang.String jndiName) {
        this.jndiName = jndiName;
    }
    
    public com.amalto.core.webservice.WSServiceActionCode getWsAction() {
        return wsAction;
    }
    
    public void setWsAction(com.amalto.core.webservice.WSServiceActionCode wsAction) {
        this.wsAction = wsAction;
    }
    
    public java.lang.String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(java.lang.String methodName) {
        this.methodName = methodName;
    }
    
    public java.lang.String[] getMethodParameters() {
        return methodParameters;
    }
    
    public void setMethodParameters(java.lang.String[] methodParameters) {
        this.methodParameters = methodParameters;
    }
}
