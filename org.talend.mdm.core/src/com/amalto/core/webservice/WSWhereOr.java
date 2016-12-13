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
import java.util.Arrays;

@XmlType(name="WSWhereOr")
public class WSWhereOr {
    protected com.amalto.core.webservice.WSWhereItem[] whereItems;
    
    public WSWhereOr() {
    }
    
    public WSWhereOr(com.amalto.core.webservice.WSWhereItem[] whereItems) {
        this.whereItems = whereItems;
    }
    
    public com.amalto.core.webservice.WSWhereItem[] getWhereItems() {
        return whereItems;
    }
    
    public void setWhereItems(com.amalto.core.webservice.WSWhereItem[] whereItems) {
        this.whereItems = whereItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WSWhereOr))
            return false;

        WSWhereOr wsWhereOr = (WSWhereOr) o;

        if (!Arrays.equals(whereItems, wsWhereOr.whereItems))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return whereItems != null ? Arrays.hashCode(whereItems) : 0;
    }
}
