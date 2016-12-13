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

@XmlType(name="WSGetBusinessConceptKey")
public class WSGetBusinessConceptKey {
    protected com.amalto.core.webservice.WSDataModelPK wsDataModelPK;
    protected java.lang.String concept;
    
    public WSGetBusinessConceptKey() {
    }
    
    public WSGetBusinessConceptKey(com.amalto.core.webservice.WSDataModelPK wsDataModelPK, java.lang.String concept) {
        this.wsDataModelPK = wsDataModelPK;
        this.concept = concept;
    }
    
    public com.amalto.core.webservice.WSDataModelPK getWsDataModelPK() {
        return wsDataModelPK;
    }
    
    public void setWsDataModelPK(com.amalto.core.webservice.WSDataModelPK wsDataModelPK) {
        this.wsDataModelPK = wsDataModelPK;
    }
    
    public java.lang.String getConcept() {
        return concept;
    }
    
    public void setConcept(java.lang.String concept) {
        this.concept = concept;
    }
}
