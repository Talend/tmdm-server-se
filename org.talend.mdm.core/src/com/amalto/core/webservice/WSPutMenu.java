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

@XmlType(name="WSPutMenu")
public class WSPutMenu {
    protected com.amalto.core.webservice.WSMenu wsMenu;
    
    public WSPutMenu() {
    }
    
    public WSPutMenu(com.amalto.core.webservice.WSMenu wsMenu) {
        this.wsMenu = wsMenu;
    }
    
    public com.amalto.core.webservice.WSMenu getWsMenu() {
        return wsMenu;
    }
    
    public void setWsMenu(com.amalto.core.webservice.WSMenu wsMenu) {
        this.wsMenu = wsMenu;
    }
}
