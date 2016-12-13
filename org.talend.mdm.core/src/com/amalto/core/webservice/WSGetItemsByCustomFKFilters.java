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

@XmlType(name="WSGetItemsByCustomFKFilters")
public class WSGetItemsByCustomFKFilters {
    protected com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK;
    protected java.lang.String conceptName;
    protected com.amalto.core.webservice.WSStringArray viewablePaths;
    protected java.lang.String injectedXpath;
    protected int skip;
    protected int maxItems;
    protected java.lang.String orderBy;
    protected java.lang.String direction;
    protected boolean returnCount;
    protected WSWhereItem whereItem;

    public WSGetItemsByCustomFKFilters() {
    }
    
    public WSGetItemsByCustomFKFilters(WSDataClusterPK wsDataClusterPK, String conceptName, WSStringArray viewablePaths, String injectedXpath, int skip, int maxItems, String orderBy, String direction, boolean returnCount, WSWhereItem whereItem) {
        this.wsDataClusterPK = wsDataClusterPK;
        this.conceptName = conceptName;
        this.viewablePaths = viewablePaths;
        this.injectedXpath = injectedXpath;
        this.skip = skip;
        this.maxItems = maxItems;
        this.orderBy = orderBy;
        this.direction = direction;
        this.returnCount = returnCount;
        this.whereItem = whereItem;
    }
    
    public com.amalto.core.webservice.WSDataClusterPK getWsDataClusterPK() {
        return wsDataClusterPK;
    }
    
    public void setWsDataClusterPK(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK) {
        this.wsDataClusterPK = wsDataClusterPK;
    }
    
    public java.lang.String getConceptName() {
        return conceptName;
    }
    
    public void setConceptName(java.lang.String conceptName) {
        this.conceptName = conceptName;
    }
    
    public com.amalto.core.webservice.WSStringArray getViewablePaths() {
        return viewablePaths;
    }
    
    public void setViewablePaths(com.amalto.core.webservice.WSStringArray viewablePaths) {
        this.viewablePaths = viewablePaths;
    }
    
    public java.lang.String getInjectedXpath() {
        return injectedXpath;
    }
    
    public void setInjectedXpath(java.lang.String injectedXpath) {
        this.injectedXpath = injectedXpath;
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

    public boolean getReturnCount() {
        return returnCount;
    }

    public void setReturnCount(boolean returnCount) {
        this.returnCount = returnCount;
    }

    public WSWhereItem getWhereItem() {
        return whereItem;
    }

    public void setWhereItem(WSWhereItem whereItem) {
        this.whereItem = whereItem;
    }
}
