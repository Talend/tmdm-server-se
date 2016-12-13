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

@XmlType(name="WSMenuMenuEntriesDescriptions")
public class WSMenuMenuEntriesDescriptions {
    protected java.lang.String language;
    protected java.lang.String label;
    
    public WSMenuMenuEntriesDescriptions() {
    }
    
    public WSMenuMenuEntriesDescriptions(java.lang.String language, java.lang.String label) {
        this.language = language;
        this.label = label;
    }
    
    public java.lang.String getLanguage() {
        return language;
    }
    
    public void setLanguage(java.lang.String language) {
        this.language = language;
    }
    
    public java.lang.String getLabel() {
        return label;
    }
    
    public void setLabel(java.lang.String label) {
        this.label = label;
    }
}
