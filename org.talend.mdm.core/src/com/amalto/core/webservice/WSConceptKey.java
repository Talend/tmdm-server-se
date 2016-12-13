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

@XmlType(name="WSConceptKey")
public class WSConceptKey {
    protected java.lang.String selector;
    protected java.lang.String[] fields;
    
    public WSConceptKey() {
    }
    
    public WSConceptKey(java.lang.String selector, java.lang.String[] fields) {
        this.selector = selector;
        this.fields = fields;
    }
    
    public java.lang.String getSelector() {
        return selector;
    }
    
    public void setSelector(java.lang.String selector) {
        this.selector = selector;
    }
    
    public java.lang.String[] getFields() {
        return fields;
    }
    
    public void setFields(java.lang.String[] fields) {
        this.fields = fields;
    }
}
