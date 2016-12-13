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

@XmlType(name="WSGetItemsPivotIndexPivotWithKeysTypedContentEntry")
public class WSGetItemsPivotIndexPivotWithKeysTypedContentEntry {
    protected java.lang.String key;
    protected com.amalto.core.webservice.WSStringArray value;
    
    public WSGetItemsPivotIndexPivotWithKeysTypedContentEntry() {
    }
    
    public WSGetItemsPivotIndexPivotWithKeysTypedContentEntry(java.lang.String key, com.amalto.core.webservice.WSStringArray value) {
        this.key = key;
        this.value = value;
    }
    
    public java.lang.String getKey() {
        return key;
    }
    
    public void setKey(java.lang.String key) {
        this.key = key;
    }
    
    public com.amalto.core.webservice.WSStringArray getValue() {
        return value;
    }
    
    public void setValue(com.amalto.core.webservice.WSStringArray value) {
        this.value = value;
    }
}
