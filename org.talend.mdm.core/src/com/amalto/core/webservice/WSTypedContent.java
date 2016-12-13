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

@XmlType(name="WSTypedContent")
public class WSTypedContent {
    protected java.lang.String url;
    protected com.amalto.core.webservice.WSByteArray wsBytes;
    protected java.lang.String contentType;
    
    public WSTypedContent() {
    }
    
    public WSTypedContent(java.lang.String url, com.amalto.core.webservice.WSByteArray wsBytes, java.lang.String contentType) {
        this.url = url;
        this.wsBytes = wsBytes;
        this.contentType = contentType;
    }
    
    public java.lang.String getUrl() {
        return url;
    }
    
    public void setUrl(java.lang.String url) {
        this.url = url;
    }
    
    public com.amalto.core.webservice.WSByteArray getWsBytes() {
        return wsBytes;
    }
    
    public void setWsBytes(com.amalto.core.webservice.WSByteArray wsBytes) {
        this.wsBytes = wsBytes;
    }
    
    public java.lang.String getContentType() {
        return contentType;
    }
    
    public void setContentType(java.lang.String contentType) {
        this.contentType = contentType;
    }
}
