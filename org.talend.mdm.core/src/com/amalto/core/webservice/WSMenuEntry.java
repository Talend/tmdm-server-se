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

@XmlType(name="WSMenuEntry")
public class WSMenuEntry {
    protected java.lang.String id;
    protected com.amalto.core.webservice.WSMenuMenuEntriesDescriptions[] descriptions;
    protected java.lang.String context;
    protected java.lang.String application;
    protected java.lang.String icon;
    protected com.amalto.core.webservice.WSMenuEntry[] subMenus;
    
    public WSMenuEntry() {
    }
    
    public WSMenuEntry(java.lang.String id, com.amalto.core.webservice.WSMenuMenuEntriesDescriptions[] descriptions, java.lang.String context, java.lang.String application, java.lang.String icon, com.amalto.core.webservice.WSMenuEntry[] subMenus) {
        this.id = id;
        this.descriptions = descriptions;
        this.context = context;
        this.application = application;
        this.icon = icon;
        this.subMenus = subMenus;
    }
    
    public java.lang.String getId() {
        return id;
    }
    
    public void setId(java.lang.String id) {
        this.id = id;
    }
    
    public com.amalto.core.webservice.WSMenuMenuEntriesDescriptions[] getDescriptions() {
        return descriptions;
    }
    
    public void setDescriptions(com.amalto.core.webservice.WSMenuMenuEntriesDescriptions[] descriptions) {
        this.descriptions = descriptions;
    }
    
    public java.lang.String getContext() {
        return context;
    }
    
    public void setContext(java.lang.String context) {
        this.context = context;
    }
    
    public java.lang.String getApplication() {
        return application;
    }
    
    public void setApplication(java.lang.String application) {
        this.application = application;
    }
    
    public java.lang.String getIcon() {
        return icon;
    }
    
    public void setIcon(java.lang.String icon) {
        this.icon = icon;
    }
    
    public com.amalto.core.webservice.WSMenuEntry[] getSubMenus() {
        return subMenus;
    }
    
    public void setSubMenus(com.amalto.core.webservice.WSMenuEntry[] subMenus) {
        this.subMenus = subMenus;
    }
}
