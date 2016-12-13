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

@XmlType(name="WSPutBusinessConcept")
public class WSPutBusinessConcept {
    protected com.amalto.core.webservice.WSDataModelPK wsDataModelPK;
    protected com.amalto.core.webservice.WSBusinessConcept businessConcept;
    
    public WSPutBusinessConcept() {
    }
    
    public WSPutBusinessConcept(com.amalto.core.webservice.WSDataModelPK wsDataModelPK, com.amalto.core.webservice.WSBusinessConcept businessConcept) {
        this.wsDataModelPK = wsDataModelPK;
        this.businessConcept = businessConcept;
    }
    
    public com.amalto.core.webservice.WSDataModelPK getWsDataModelPK() {
        return wsDataModelPK;
    }
    
    public void setWsDataModelPK(com.amalto.core.webservice.WSDataModelPK wsDataModelPK) {
        this.wsDataModelPK = wsDataModelPK;
    }
    
    public com.amalto.core.webservice.WSBusinessConcept getBusinessConcept() {
        return businessConcept;
    }
    
    public void setBusinessConcept(com.amalto.core.webservice.WSBusinessConcept businessConcept) {
        this.businessConcept = businessConcept;
    }
}
