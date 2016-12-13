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

@XmlType(name="WSPutItem")
public class WSPutItem {
    protected com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK;
    protected java.lang.String xmlString;
    protected com.amalto.core.webservice.WSDataModelPK wsDataModelPK;
    protected java.lang.Boolean isUpdate;

    public WSPutItem() {
    }
    
    public WSPutItem(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK, java.lang.String xmlString, com.amalto.core.webservice.WSDataModelPK wsDataModelPK, java.lang.Boolean isUpdate) {
        this.wsDataClusterPK = wsDataClusterPK;
        this.xmlString = xmlString;
        this.wsDataModelPK = wsDataModelPK;
        this.isUpdate = isUpdate;
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
    
    public java.lang.Boolean getIsUpdate() {
        return isUpdate;
    }
    
    public void setIsUpdate(java.lang.Boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

}
