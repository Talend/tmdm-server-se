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

@XmlType(name="WSExtractedContent")
public class WSExtractedContent {
    protected com.amalto.core.webservice.WSByteArray wsByteArray;
    protected java.lang.String contentType;
    
    public WSExtractedContent() {
    }
    
    public WSExtractedContent(com.amalto.core.webservice.WSByteArray wsByteArray, java.lang.String contentType) {
        this.wsByteArray = wsByteArray;
        this.contentType = contentType;
    }
    
    public com.amalto.core.webservice.WSByteArray getWsByteArray() {
        return wsByteArray;
    }
    
    public void setWsByteArray(com.amalto.core.webservice.WSByteArray wsByteArray) {
        this.wsByteArray = wsByteArray;
    }
    
    public java.lang.String getContentType() {
        return contentType;
    }
    
    public void setContentType(java.lang.String contentType) {
        this.contentType = contentType;
    }
}
