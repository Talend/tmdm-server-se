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

@XmlType(name="WSProcessFileUsingTransformer")
public class WSProcessFileUsingTransformer {
    protected java.lang.String fileName;
    protected java.lang.String contentType;
    protected com.amalto.core.webservice.WSTransformerPK wsTransformerPK;
    protected com.amalto.core.webservice.WSOutputDecisionTable wsOutputDecisionTable;
    
    public WSProcessFileUsingTransformer() {
    }
    
    public WSProcessFileUsingTransformer(java.lang.String fileName, java.lang.String contentType, com.amalto.core.webservice.WSTransformerPK wsTransformerPK, com.amalto.core.webservice.WSOutputDecisionTable wsOutputDecisionTable) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.wsTransformerPK = wsTransformerPK;
        this.wsOutputDecisionTable = wsOutputDecisionTable;
    }
    
    public java.lang.String getFileName() {
        return fileName;
    }
    
    public void setFileName(java.lang.String fileName) {
        this.fileName = fileName;
    }
    
    public java.lang.String getContentType() {
        return contentType;
    }
    
    public void setContentType(java.lang.String contentType) {
        this.contentType = contentType;
    }
    
    public com.amalto.core.webservice.WSTransformerPK getWsTransformerPK() {
        return wsTransformerPK;
    }
    
    public void setWsTransformerPK(com.amalto.core.webservice.WSTransformerPK wsTransformerPK) {
        this.wsTransformerPK = wsTransformerPK;
    }
    
    public com.amalto.core.webservice.WSOutputDecisionTable getWsOutputDecisionTable() {
        return wsOutputDecisionTable;
    }
    
    public void setWsOutputDecisionTable(com.amalto.core.webservice.WSOutputDecisionTable wsOutputDecisionTable) {
        this.wsOutputDecisionTable = wsOutputDecisionTable;
    }
}
