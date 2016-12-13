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

@XmlType(name="WSI18NString")
public class WSI18NString {
    protected com.amalto.core.webservice.WSLanguage language;
    protected java.lang.String label;
    
    public WSI18NString() {
    }
    
    public WSI18NString(com.amalto.core.webservice.WSLanguage language, java.lang.String label) {
        this.language = language;
        this.label = label;
    }
    
    public com.amalto.core.webservice.WSLanguage getLanguage() {
        return language;
    }
    
    public void setLanguage(com.amalto.core.webservice.WSLanguage language) {
        this.language = language;
    }
    
    public java.lang.String getLabel() {
        return label;
    }
    
    public void setLabel(java.lang.String label) {
        this.label = label;
    }
}
