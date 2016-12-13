/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.xmlserver.interfaces;

import java.io.Serializable;

public class ItemPKCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private String clusterName;

    private String conceptName;

    private String contentKeywords;

    private String keysKeywords;

    private String keys;

    private boolean compoundKeyKeywords;

    private Long fromDate;

    private Long toDate;

    private int skip;

    private int maxItems;

    private boolean useFTSearch;

    public ItemPKCriteria() {
    }

    public ItemPKCriteria(String clusterName, String conceptName, String contentKeywords, String keysKeywords,
                          boolean compoundKeyKeywords, Long fromDate, Long toDate, int skip, int maxItems, boolean useFTSearch) {
        this.clusterName = clusterName;
        this.conceptName = conceptName;
        this.contentKeywords = contentKeywords;
        this.compoundKeyKeywords = compoundKeyKeywords;
        this.keysKeywords = keysKeywords;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.skip = skip;
        this.maxItems = maxItems;
        this.useFTSearch = useFTSearch;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getContentKeywords() {
        return contentKeywords;
    }

    public void setContentKeywords(String contentKeywords) {
        this.contentKeywords = contentKeywords;
    }

    public String getKeysKeywords() {
        return keysKeywords;
    }

    public void setKeysKeywords(String keysKeywords) {
        this.keysKeywords = keysKeywords;
    }

    public String getKeys() {
        return this.keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public boolean isCompoundKeyKeywords() {
        return compoundKeyKeywords;
    }

    public void setCompoundKeyKeywords(boolean compoundKeyKeywords) {
        this.compoundKeyKeywords = compoundKeyKeywords;
    }

    public Long getFromDate() {
        return fromDate;
    }

    public void setFromDate(Long fromDate) {
        this.fromDate = fromDate;
    }

    public Long getToDate() {
        return toDate;
    }

    public void setToDate(Long toDate) {
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

    public boolean isUseFTSearch() {
        return useFTSearch;
    }

    public void setUseFTSearch(boolean useFTSearch) {
        this.useFTSearch = useFTSearch;
    }
}
