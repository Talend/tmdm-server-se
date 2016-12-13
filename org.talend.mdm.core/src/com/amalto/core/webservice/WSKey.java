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

@XmlType(name="WSKey")
public class WSKey {
    protected java.lang.String selectorpath;
    protected java.lang.String[] fieldpath;
    
    public WSKey() {
    }
    
    public WSKey(java.lang.String selectorpath, java.lang.String[] fieldpath) {
        this.selectorpath = selectorpath;
        this.fieldpath = fieldpath;
    }
    
    public java.lang.String getSelectorpath() {
        return selectorpath;
    }
    
    public void setSelectorpath(java.lang.String selectorpath) {
        this.selectorpath = selectorpath;
    }
    
    public java.lang.String[] getFieldpath() {
        return fieldpath;
    }
    
    public void setFieldpath(java.lang.String[] fieldpath) {
        this.fieldpath = fieldpath;
    }
}
