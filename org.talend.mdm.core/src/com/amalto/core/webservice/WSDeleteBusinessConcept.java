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

@XmlType(name="WSDeleteBusinessConcept")
public class WSDeleteBusinessConcept {
    protected com.amalto.core.webservice.WSDataModelPK wsDataModelPK;
    protected java.lang.String businessConceptName;
    
    public WSDeleteBusinessConcept() {
    }
    
    public WSDeleteBusinessConcept(com.amalto.core.webservice.WSDataModelPK wsDataModelPK, java.lang.String businessConceptName) {
        this.wsDataModelPK = wsDataModelPK;
        this.businessConceptName = businessConceptName;
    }
    
    public com.amalto.core.webservice.WSDataModelPK getWsDataModelPK() {
        return wsDataModelPK;
    }
    
    public void setWsDataModelPK(com.amalto.core.webservice.WSDataModelPK wsDataModelPK) {
        this.wsDataModelPK = wsDataModelPK;
    }
    
    public java.lang.String getBusinessConceptName() {
        return businessConceptName;
    }
    
    public void setBusinessConceptName(java.lang.String businessConceptName) {
        this.businessConceptName = businessConceptName;
    }
}
