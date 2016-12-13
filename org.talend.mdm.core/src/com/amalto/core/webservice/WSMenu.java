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

@XmlType(name="WSMenu")
public class WSMenu {
    protected java.lang.String name;
    protected java.lang.String description;
    protected com.amalto.core.webservice.WSMenuEntry[] menuEntries;
    
    public WSMenu() {
    }
    
    public WSMenu(java.lang.String name, java.lang.String description, com.amalto.core.webservice.WSMenuEntry[] menuEntries) {
        this.name = name;
        this.description = description;
        this.menuEntries = menuEntries;
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
    
    public com.amalto.core.webservice.WSMenuEntry[] getMenuEntries() {
        return menuEntries;
    }
    
    public void setMenuEntries(com.amalto.core.webservice.WSMenuEntry[] menuEntries) {
        this.menuEntries = menuEntries;
    }
}
