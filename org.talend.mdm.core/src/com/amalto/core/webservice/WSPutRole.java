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

@XmlType(name="WSPutRole")
public class WSPutRole {
    protected com.amalto.core.webservice.WSRole wsRole;
    
    public WSPutRole() {
    }
    
    public WSPutRole(com.amalto.core.webservice.WSRole wsRole) {
        this.wsRole = wsRole;
    }
    
    public com.amalto.core.webservice.WSRole getWsRole() {
        return wsRole;
    }
    
    public void setWsRole(com.amalto.core.webservice.WSRole wsRole) {
        this.wsRole = wsRole;
    }
}
