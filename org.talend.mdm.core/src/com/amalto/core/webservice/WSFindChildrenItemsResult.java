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

@XmlType(name="WSFindChildrenItemsResult")
public class WSFindChildrenItemsResult {
    protected com.amalto.core.webservice.WSStringArray wsStringArray;
    
    public WSFindChildrenItemsResult() {
    }
    
    public WSFindChildrenItemsResult(com.amalto.core.webservice.WSStringArray wsStringArray) {
        this.wsStringArray = wsStringArray;
    }
    
    public com.amalto.core.webservice.WSStringArray getWsStringArray() {
        return wsStringArray;
    }
    
    public void setWsStringArray(com.amalto.core.webservice.WSStringArray wsStringArray) {
        this.wsStringArray = wsStringArray;
    }
}
