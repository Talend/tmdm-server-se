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

@XmlType(name="WSXPathsSearch")
public class WSXPathsSearch {
    protected com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK;
    protected java.lang.String pivotPath;
    protected com.amalto.core.webservice.WSStringArray viewablePaths;
    protected com.amalto.core.webservice.WSWhereItem whereItem;
    protected int spellTreshold;
    protected int skip;
    protected int maxItems;
    protected java.lang.String orderBy;
    protected java.lang.String direction;
    protected java.lang.Boolean returnCount;
    
    public WSXPathsSearch() {
    }
    
    public WSXPathsSearch(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK, java.lang.String pivotPath, com.amalto.core.webservice.WSStringArray viewablePaths, com.amalto.core.webservice.WSWhereItem whereItem, int spellTreshold, int skip, int maxItems, java.lang.String orderBy, java.lang.String direction, java.lang.Boolean returnCount) {
        this.wsDataClusterPK = wsDataClusterPK;
        this.pivotPath = pivotPath;
        this.viewablePaths = viewablePaths;
        this.whereItem = whereItem;
        this.spellTreshold = spellTreshold;
        this.skip = skip;
        this.maxItems = maxItems;
        this.orderBy = orderBy;
        this.direction = direction;
        this.returnCount = returnCount;
    }
    
    public com.amalto.core.webservice.WSDataClusterPK getWsDataClusterPK() {
        return wsDataClusterPK;
    }
    
    public void setWsDataClusterPK(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK) {
        this.wsDataClusterPK = wsDataClusterPK;
    }
    
    public java.lang.String getPivotPath() {
        return pivotPath;
    }
    
    public void setPivotPath(java.lang.String pivotPath) {
        this.pivotPath = pivotPath;
    }
    
    public com.amalto.core.webservice.WSStringArray getViewablePaths() {
        return viewablePaths;
    }
    
    public void setViewablePaths(com.amalto.core.webservice.WSStringArray viewablePaths) {
        this.viewablePaths = viewablePaths;
    }
    
    public com.amalto.core.webservice.WSWhereItem getWhereItem() {
        return whereItem;
    }
    
    public void setWhereItem(com.amalto.core.webservice.WSWhereItem whereItem) {
        this.whereItem = whereItem;
    }
    
    public int getSpellTreshold() {
        return spellTreshold;
    }
    
    public void setSpellTreshold(int spellTreshold) {
        this.spellTreshold = spellTreshold;
    }
    
    public int getSkip() {
        return skip;
    }
    
    public void setSkip(int skip) {
        this.skip = skip;
    }
    
    public int getMaxItems() {
        return maxItems;
    }
    
    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
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
    
    public java.lang.Boolean getReturnCount() {
        return returnCount;
    }
    
    public void setReturnCount(java.lang.Boolean returnCount) {
        this.returnCount = returnCount;
    }
}
