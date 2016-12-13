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

@XmlType(name="WSPutBusinessConceptSchema")
public class WSPutBusinessConceptSchema {
    protected com.amalto.core.webservice.WSDataModelPK wsDataModelPK;
    protected java.lang.String businessConceptSchema;
    
    public WSPutBusinessConceptSchema() {
    }
    
    public WSPutBusinessConceptSchema(com.amalto.core.webservice.WSDataModelPK wsDataModelPK, java.lang.String businessConceptSchema) {
        this.wsDataModelPK = wsDataModelPK;
        this.businessConceptSchema = businessConceptSchema;
    }
    
    public com.amalto.core.webservice.WSDataModelPK getWsDataModelPK() {
        return wsDataModelPK;
    }
    
    public void setWsDataModelPK(com.amalto.core.webservice.WSDataModelPK wsDataModelPK) {
        this.wsDataModelPK = wsDataModelPK;
    }
    
    public java.lang.String getBusinessConceptSchema() {
        return businessConceptSchema;
    }
    
    public void setBusinessConceptSchema(java.lang.String businessConceptSchema) {
        this.businessConceptSchema = businessConceptSchema;
    }
}
