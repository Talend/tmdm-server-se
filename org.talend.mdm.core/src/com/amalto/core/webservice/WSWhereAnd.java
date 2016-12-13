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

@XmlType(name="WSWhereAnd")
public class WSWhereAnd {
    protected com.amalto.core.webservice.WSWhereItem[] whereItems;
    
    public WSWhereAnd() {
    }
    
    public WSWhereAnd(com.amalto.core.webservice.WSWhereItem[] whereItems) {
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
        if (!(o instanceof WSWhereAnd))
            return false;

        WSWhereAnd that = (WSWhereAnd) o;

        if (!Arrays.equals(whereItems, that.whereItems))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return whereItems != null ? Arrays.hashCode(whereItems) : 0;
    }
}
