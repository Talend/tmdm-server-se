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

@XmlType(name="WSViewPK")
public class WSViewPK {
    protected java.lang.String pk;
    
    public WSViewPK() {
    }
    
    public WSViewPK(java.lang.String pk) {
        this.pk = pk;
    }
    
    public java.lang.String getPk() {
        return pk;
    }
    
    public void setPk(java.lang.String pk) {
        this.pk = pk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WSViewPK))
            return false;

        WSViewPK wsViewPK = (WSViewPK) o;

        if (pk != null ? !pk.equals(wsViewPK.pk) : wsViewPK.pk != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return pk != null ? pk.hashCode() : 0;
    }
}
