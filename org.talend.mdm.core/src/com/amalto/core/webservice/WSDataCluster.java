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

@XmlType(name="WSDataCluster")
public class WSDataCluster {
    protected java.lang.String name;
    protected java.lang.String description;
    protected java.lang.String vocabulary;
    
    public WSDataCluster() {
    }
    
    public WSDataCluster(java.lang.String name, java.lang.String description, java.lang.String vocabulary) {
        this.name = name;
        this.description = description;
        this.vocabulary = vocabulary;
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
    
    public java.lang.String getVocabulary() {
        return vocabulary;
    }
    
    public void setVocabulary(java.lang.String vocabulary) {
        this.vocabulary = vocabulary;
    }
}
