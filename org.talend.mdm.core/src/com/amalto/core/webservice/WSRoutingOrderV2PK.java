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

@XmlType(name="WSRoutingOrderV2PK")
public class WSRoutingOrderV2PK {
    protected java.lang.String name;
    protected com.amalto.core.webservice.WSRoutingOrderV2Status status;
    
    public WSRoutingOrderV2PK() {
    }
    
    public WSRoutingOrderV2PK(java.lang.String name, com.amalto.core.webservice.WSRoutingOrderV2Status status) {
        this.name = name;
        this.status = status;
    }
    
    public java.lang.String getName() {
        return name;
    }
    
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    public com.amalto.core.webservice.WSRoutingOrderV2Status getStatus() {
        return status;
    }
    
    public void setStatus(com.amalto.core.webservice.WSRoutingOrderV2Status status) {
        this.status = status;
    }
}
