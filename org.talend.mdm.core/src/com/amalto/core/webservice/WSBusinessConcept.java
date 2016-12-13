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

@XmlType(name="WSBusinessConcept")
public class WSBusinessConcept {
    protected java.lang.String name;
    protected java.lang.String businessTemplate;
    protected com.amalto.core.webservice.WSKey wsUniqueKey;
    protected com.amalto.core.webservice.WSI18NString[] wsLabel;
    protected com.amalto.core.webservice.WSI18NString[] wsDescription;
    
    public WSBusinessConcept() {
    }
    
    public WSBusinessConcept(java.lang.String name, java.lang.String businessTemplate, com.amalto.core.webservice.WSKey wsUniqueKey, com.amalto.core.webservice.WSI18NString[] wsLabel, com.amalto.core.webservice.WSI18NString[] wsDescription) {
        this.name = name;
        this.businessTemplate = businessTemplate;
        this.wsUniqueKey = wsUniqueKey;
        this.wsLabel = wsLabel;
        this.wsDescription = wsDescription;
    }
    
    public java.lang.String getName() {
        return name;
    }
    
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    public java.lang.String getBusinessTemplate() {
        return businessTemplate;
    }
    
    public void setBusinessTemplate(java.lang.String businessTemplate) {
        this.businessTemplate = businessTemplate;
    }
    
    public com.amalto.core.webservice.WSKey getWsUniqueKey() {
        return wsUniqueKey;
    }
    
    public void setWsUniqueKey(com.amalto.core.webservice.WSKey wsUniqueKey) {
        this.wsUniqueKey = wsUniqueKey;
    }
    
    public com.amalto.core.webservice.WSI18NString[] getWsLabel() {
        return wsLabel;
    }
    
    public void setWsLabel(com.amalto.core.webservice.WSI18NString[] wsLabel) {
        this.wsLabel = wsLabel;
    }
    
    public com.amalto.core.webservice.WSI18NString[] getWsDescription() {
        return wsDescription;
    }
    
    public void setWsDescription(com.amalto.core.webservice.WSI18NString[] wsDescription) {
        this.wsDescription = wsDescription;
    }
}
