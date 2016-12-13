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

@XmlType(name="WSGetItemPKsByCriteria")
public class WSGetItemPKsByCriteria {

    protected com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK;

    protected java.lang.String conceptName;

    protected java.lang.String contentKeywords;

    protected java.lang.String keys;

    protected java.lang.String keysKeywords;

    protected java.lang.Long fromDate;

    protected java.lang.Long toDate;

    protected int skip;

    protected int maxItems;

    public WSGetItemPKsByCriteria() {
    }

    public WSGetItemPKsByCriteria(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK, java.lang.String conceptName,
            java.lang.String contentKeywords, java.lang.String keysKeywords, java.lang.String keys, java.lang.Long fromDate,
            java.lang.Long toDate, int skip, int maxItems) {
        this.wsDataClusterPK = wsDataClusterPK;
        this.conceptName = conceptName;
        this.contentKeywords = contentKeywords;
        this.keysKeywords = keysKeywords;
        this.keys = keys;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.skip = skip;
        this.maxItems = maxItems;
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

    public java.lang.String getContentKeywords() {
        return contentKeywords;
    }

    public void setContentKeywords(java.lang.String contentKeywords) {
        this.contentKeywords = contentKeywords;
    }

    public java.lang.String getKeysKeywords() {
        return keysKeywords;
    }

    public void setKeysKeywords(java.lang.String keysKeywords) {
        this.keysKeywords = keysKeywords;
    }

    public java.lang.String getKeys() {
        return this.keys;
    }

    public void setKeys(java.lang.String keys) {
        this.keys = keys;
    }

    public java.lang.Long getFromDate() {
        return fromDate;
    }

    public void setFromDate(java.lang.Long fromDate) {
        this.fromDate = fromDate;
    }

    public java.lang.Long getToDate() {
        return toDate;
    }

    public void setToDate(java.lang.Long toDate) {
        this.toDate = toDate;
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
}
