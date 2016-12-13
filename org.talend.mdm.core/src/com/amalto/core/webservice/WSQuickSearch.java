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

@XmlType(name="WSQuickSearch")
public class WSQuickSearch {
    protected com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK;
    protected com.amalto.core.webservice.WSViewPK wsViewPK;
    protected java.lang.String searchedValue;
    protected int maxItems;
    protected int skip;
    protected int spellTreshold;
    protected boolean matchAllWords;
    protected java.lang.String orderBy;
    protected java.lang.String direction;
    
    public WSQuickSearch() {
    }
    
    public WSQuickSearch(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK, com.amalto.core.webservice.WSViewPK wsViewPK, java.lang.String searchedValue, int maxItems, int skip, int spellTreshold, boolean matchAllWords, java.lang.String orderBy, java.lang.String direction) {
        this.wsDataClusterPK = wsDataClusterPK;
        this.wsViewPK = wsViewPK;
        this.searchedValue = searchedValue;
        this.maxItems = maxItems;
        this.skip = skip;
        this.spellTreshold = spellTreshold;
        this.matchAllWords = matchAllWords;
        this.orderBy = orderBy;
        this.direction = direction;
    }
    
    public com.amalto.core.webservice.WSDataClusterPK getWsDataClusterPK() {
        return wsDataClusterPK;
    }
    
    public void setWsDataClusterPK(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK) {
        this.wsDataClusterPK = wsDataClusterPK;
    }
    
    public com.amalto.core.webservice.WSViewPK getWsViewPK() {
        return wsViewPK;
    }
    
    public void setWsViewPK(com.amalto.core.webservice.WSViewPK wsViewPK) {
        this.wsViewPK = wsViewPK;
    }
    
    public java.lang.String getSearchedValue() {
        return searchedValue;
    }
    
    public void setSearchedValue(java.lang.String searchedValue) {
        this.searchedValue = searchedValue;
    }
    
    public int getMaxItems() {
        return maxItems;
    }
    
    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
    }
    
    public int getSkip() {
        return skip;
    }
    
    public void setSkip(int skip) {
        this.skip = skip;
    }
    
    public int getSpellTreshold() {
        return spellTreshold;
    }
    
    public void setSpellTreshold(int spellTreshold) {
        this.spellTreshold = spellTreshold;
    }
    
    public boolean isMatchAllWords() {
        return matchAllWords;
    }
    
    public void setMatchAllWords(boolean matchAllWords) {
        this.matchAllWords = matchAllWords;
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
