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

@XmlType(name="WSGetChildrenItems")
public class WSGetChildrenItems {
    protected java.lang.String clusterName;
    protected java.lang.String conceptName;
    protected com.amalto.core.webservice.WSStringArray PKXpaths;
    protected java.lang.String FKXpath;
    protected java.lang.String labelXpath;
    protected java.lang.String fatherPK;
    protected com.amalto.core.webservice.WSWhereItem whereItem;
    protected int start;
    protected int limit;
    
    public WSGetChildrenItems() {
    }
    
    public WSGetChildrenItems(java.lang.String clusterName, java.lang.String conceptName, com.amalto.core.webservice.WSStringArray PKXpaths, java.lang.String FKXpath, java.lang.String labelXpath, java.lang.String fatherPK, com.amalto.core.webservice.WSWhereItem whereItem, int start, int limit) {
        this.clusterName = clusterName;
        this.conceptName = conceptName;
        this.PKXpaths = PKXpaths;
        this.FKXpath = FKXpath;
        this.labelXpath = labelXpath;
        this.fatherPK = fatherPK;
        this.whereItem = whereItem;
        this.start = start;
        this.limit = limit;
    }
    
    public java.lang.String getClusterName() {
        return clusterName;
    }
    
    public void setClusterName(java.lang.String clusterName) {
        this.clusterName = clusterName;
    }
    
    public java.lang.String getConceptName() {
        return conceptName;
    }
    
    public void setConceptName(java.lang.String conceptName) {
        this.conceptName = conceptName;
    }
    
    public com.amalto.core.webservice.WSStringArray getPKXpaths() {
        return PKXpaths;
    }
    
    public void setPKXpaths(com.amalto.core.webservice.WSStringArray PKXpaths) {
        this.PKXpaths = PKXpaths;
    }
    
    public java.lang.String getFKXpath() {
        return FKXpath;
    }
    
    public void setFKXpath(java.lang.String FKXpath) {
        this.FKXpath = FKXpath;
    }
    
    public java.lang.String getLabelXpath() {
        return labelXpath;
    }
    
    public void setLabelXpath(java.lang.String labelXpath) {
        this.labelXpath = labelXpath;
    }
    
    public java.lang.String getFatherPK() {
        return fatherPK;
    }
    
    public void setFatherPK(java.lang.String fatherPK) {
        this.fatherPK = fatherPK;
    }
    
    public com.amalto.core.webservice.WSWhereItem getWhereItem() {
        return whereItem;
    }
    
    public void setWhereItem(com.amalto.core.webservice.WSWhereItem whereItem) {
        this.whereItem = whereItem;
    }
    
    public int getStart() {
        return start;
    }
    
    public void setStart(int start) {
        this.start = start;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
}
