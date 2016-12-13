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

@XmlType(name="WSTransformerV2PK")
public class WSTransformerV2PK {
    protected java.lang.String pk;
    
    public WSTransformerV2PK() {
    }
    
    public WSTransformerV2PK(java.lang.String pk) {
        this.pk = pk;
    }
    
    public java.lang.String getPk() {
        return pk;
    }
    
    public void setPk(java.lang.String pk) {
        this.pk = pk;
    }
}
