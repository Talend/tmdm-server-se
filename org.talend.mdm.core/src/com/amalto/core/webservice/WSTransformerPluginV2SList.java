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

@XmlType(name="WSTransformerPluginV2SList")
public class WSTransformerPluginV2SList {
    protected com.amalto.core.webservice.WSTransformerPluginV2SListItem[] item;
    
    public WSTransformerPluginV2SList() {
    }
    
    public WSTransformerPluginV2SList(com.amalto.core.webservice.WSTransformerPluginV2SListItem[] item) {
        this.item = item;
    }
    
    public com.amalto.core.webservice.WSTransformerPluginV2SListItem[] getItem() {
        return item;
    }
    
    public void setItem(com.amalto.core.webservice.WSTransformerPluginV2SListItem[] item) {
        this.item = item;
    }
}
