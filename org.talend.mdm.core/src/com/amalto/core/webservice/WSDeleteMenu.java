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

@XmlType(name="WSDeleteMenu")
public class WSDeleteMenu {
    protected com.amalto.core.webservice.WSMenuPK wsMenuPK;
    
    public WSDeleteMenu() {
    }
    
    public WSDeleteMenu(com.amalto.core.webservice.WSMenuPK wsMenuPK) {
        this.wsMenuPK = wsMenuPK;
    }
    
    public com.amalto.core.webservice.WSMenuPK getWsMenuPK() {
        return wsMenuPK;
    }
    
    public void setWsMenuPK(com.amalto.core.webservice.WSMenuPK wsMenuPK) {
        this.wsMenuPK = wsMenuPK;
    }
}
