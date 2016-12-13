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

@XmlType(name="WSBoolean")
public class WSBoolean {
    protected boolean _true;
    
    public WSBoolean() {
    }
    
    public WSBoolean(boolean _true) {
        this._true = _true;
    }
    
    public boolean is_true() {
        return _true;
    }
    
    public void set_true(boolean _true) {
        this._true = _true;
    }
}
