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

@XmlType(name="WSTransformerV2")
public class WSTransformerV2 {
    protected java.lang.String name;
    protected java.lang.String description;
    protected com.amalto.core.webservice.WSTransformerProcessStep[] processSteps;
    
    public WSTransformerV2() {
    }
    
    public WSTransformerV2(java.lang.String name, java.lang.String description, com.amalto.core.webservice.WSTransformerProcessStep[] processSteps) {
        this.name = name;
        this.description = description;
        this.processSteps = processSteps;
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
    
    public com.amalto.core.webservice.WSTransformerProcessStep[] getProcessSteps() {
        return processSteps;
    }
    
    public void setProcessSteps(com.amalto.core.webservice.WSTransformerProcessStep[] processSteps) {
        this.processSteps = processSteps;
    }
}
