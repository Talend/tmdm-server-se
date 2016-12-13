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

@XmlType(name="WSPutItemByOperatorType")
public class WSPutItemByOperatorType {
    protected com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK;
    protected java.lang.String xmlString;
    protected com.amalto.core.webservice.WSDataModelPK wsDataModelPK;
    protected com.amalto.core.webservice.WSOperatorType operatortype;
    
    public WSPutItemByOperatorType() {
    }
    
    public WSPutItemByOperatorType(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK, java.lang.String xmlString, com.amalto.core.webservice.WSDataModelPK wsDataModelPK, com.amalto.core.webservice.WSOperatorType operatortype) {
        this.wsDataClusterPK = wsDataClusterPK;
        this.xmlString = xmlString;
        this.wsDataModelPK = wsDataModelPK;
        this.operatortype = operatortype;
    }
    
    public com.amalto.core.webservice.WSDataClusterPK getWsDataClusterPK() {
        return wsDataClusterPK;
    }
    
    public void setWsDataClusterPK(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK) {
        this.wsDataClusterPK = wsDataClusterPK;
    }
    
    public java.lang.String getXmlString() {
        return xmlString;
    }
    
    public void setXmlString(java.lang.String xmlString) {
        this.xmlString = xmlString;
    }
    
    public com.amalto.core.webservice.WSDataModelPK getWsDataModelPK() {
        return wsDataModelPK;
    }
    
    public void setWsDataModelPK(com.amalto.core.webservice.WSDataModelPK wsDataModelPK) {
        this.wsDataModelPK = wsDataModelPK;
    }
    
    public com.amalto.core.webservice.WSOperatorType getOperatortype() {
        return operatortype;
    }
    
    public void setOperatortype(com.amalto.core.webservice.WSOperatorType operatortype) {
        this.operatortype = operatortype;
    }
}
