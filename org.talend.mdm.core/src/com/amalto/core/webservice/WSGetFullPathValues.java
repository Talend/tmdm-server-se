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

@XmlType(name="WSGetFullPathValues")
public class WSGetFullPathValues {
    protected com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK;
    protected java.lang.String fullPath;
    protected com.amalto.core.webservice.WSWhereItem whereItem;
    protected int spellThreshold;
    protected java.lang.String orderBy;
    protected java.lang.String direction;
    
    public WSGetFullPathValues() {
    }
    
    public WSGetFullPathValues(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK, java.lang.String fullPath, com.amalto.core.webservice.WSWhereItem whereItem, int spellThreshold, java.lang.String orderBy, java.lang.String direction) {
        this.wsDataClusterPK = wsDataClusterPK;
        this.fullPath = fullPath;
        this.whereItem = whereItem;
        this.spellThreshold = spellThreshold;
        this.orderBy = orderBy;
        this.direction = direction;
    }
    
    public com.amalto.core.webservice.WSDataClusterPK getWsDataClusterPK() {
        return wsDataClusterPK;
    }
    
    public void setWsDataClusterPK(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK) {
        this.wsDataClusterPK = wsDataClusterPK;
    }
    
    public java.lang.String getFullPath() {
        return fullPath;
    }
    
    public void setFullPath(java.lang.String fullPath) {
        this.fullPath = fullPath;
    }
    
    public com.amalto.core.webservice.WSWhereItem getWhereItem() {
        return whereItem;
    }
    
    public void setWhereItem(com.amalto.core.webservice.WSWhereItem whereItem) {
        this.whereItem = whereItem;
    }
    
    public int getSpellThreshold() {
        return spellThreshold;
    }
    
    public void setSpellThreshold(int spellThreshold) {
        this.spellThreshold = spellThreshold;
    }
    
    public java.lang.String getOrderBy() {
        return orderBy;
    }
    
    public void setOrderBy(java.lang.String orderBy) {
        this.orderBy = orderBy;
    }
    
    public java.lang.String getDirection() {
        return direction;
    }
    
    public void setDirection(java.lang.String direction) {
        this.direction = direction;
    }
}
