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

@XmlType(name="WSUpdateItemArrayMetadata")
public class WSUpdateItemArrayMetadata {
    protected com.amalto.core.webservice.WSUpdateMetadataItem[] wsUpdateMetadataItem;
    
    public WSUpdateItemArrayMetadata() {
    }
    
    public WSUpdateItemArrayMetadata(com.amalto.core.webservice.WSUpdateMetadataItem[] wsUpdateMetadataItem) {
        this.wsUpdateMetadataItem = wsUpdateMetadataItem;
    }
    
    public com.amalto.core.webservice.WSUpdateMetadataItem[] getWsUpdateMetadataItem() {
        return wsUpdateMetadataItem;
    }
    
    public void setWsUpdateMetadataItem(com.amalto.core.webservice.WSUpdateMetadataItem[] wsUpdateMetadataItem) {
        this.wsUpdateMetadataItem = wsUpdateMetadataItem;
    }
}
