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

@XmlType(name="WSWhereItem")
public class WSWhereItem {
    protected com.amalto.core.webservice.WSWhereCondition whereCondition;
    protected com.amalto.core.webservice.WSWhereAnd whereAnd;
    protected com.amalto.core.webservice.WSWhereOr whereOr;
    
    public WSWhereItem() {
    }
    
    public WSWhereItem(com.amalto.core.webservice.WSWhereCondition whereCondition, com.amalto.core.webservice.WSWhereAnd whereAnd, com.amalto.core.webservice.WSWhereOr whereOr) {
        this.whereCondition = whereCondition;
        this.whereAnd = whereAnd;
        this.whereOr = whereOr;
    }
    
    public com.amalto.core.webservice.WSWhereCondition getWhereCondition() {
        return whereCondition;
    }
    
    public void setWhereCondition(com.amalto.core.webservice.WSWhereCondition whereCondition) {
        this.whereCondition = whereCondition;
    }
    
    public com.amalto.core.webservice.WSWhereAnd getWhereAnd() {
        return whereAnd;
    }
    
    public void setWhereAnd(com.amalto.core.webservice.WSWhereAnd whereAnd) {
        this.whereAnd = whereAnd;
    }
    
    public com.amalto.core.webservice.WSWhereOr getWhereOr() {
        return whereOr;
    }
    
    public void setWhereOr(com.amalto.core.webservice.WSWhereOr whereOr) {
        this.whereOr = whereOr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WSWhereItem))
            return false;

        WSWhereItem that = (WSWhereItem) o;

        if (whereAnd != null ? !whereAnd.equals(that.whereAnd) : that.whereAnd != null)
            return false;
        if (whereCondition != null ? !whereCondition.equals(that.whereCondition) : that.whereCondition != null)
            return false;
        if (whereOr != null ? !whereOr.equals(that.whereOr) : that.whereOr != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = whereCondition != null ? whereCondition.hashCode() : 0;
        result = 31 * result + (whereAnd != null ? whereAnd.hashCode() : 0);
        result = 31 * result + (whereOr != null ? whereOr.hashCode() : 0);
        return result;
    }
}
