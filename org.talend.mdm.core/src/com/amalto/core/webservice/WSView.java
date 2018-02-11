/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.webservice;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="WSView")
public class WSView {
    protected java.lang.String name;
    protected java.lang.String description;
    protected java.lang.String[] viewableBusinessElements;
    protected com.amalto.core.webservice.WSWhereCondition[] whereConditions;
    protected java.lang.String[] searchableBusinessElements;
    protected java.lang.String transformerPK;
    protected com.amalto.core.webservice.WSBoolean isTransformerActive;
    protected com.amalto.core.webservice.WSBoolean isAsc;
    protected java.lang.String sortField;
    protected java.lang.String customForm;
    
    public WSView() {
    }
    
    public WSView(java.lang.String name, java.lang.String description, java.lang.String[] viewableBusinessElements,
            com.amalto.core.webservice.WSWhereCondition[] whereConditions, java.lang.String[] searchableBusinessElements,
            java.lang.String transformerPK, com.amalto.core.webservice.WSBoolean isTransformerActive, com.amalto.core.webservice.WSBoolean isAsc,
            java.lang.String sortField, java.lang.String customForm) {
        this.name = name;
        this.description = description;
        this.viewableBusinessElements = viewableBusinessElements;
        this.whereConditions = whereConditions;
        this.searchableBusinessElements = searchableBusinessElements;
        this.transformerPK = transformerPK;
        this.isTransformerActive = isTransformerActive;
        this.isAsc = isAsc;
        this.sortField = sortField;
        this.customForm = customForm;
    }
    
    public java.lang.String getName() {
        return name;
    }
    
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    public java.lang.String getDescription() {
        return description;
    }
    
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    public java.lang.String[] getViewableBusinessElements() {
        return viewableBusinessElements;
    }
    
    public void setViewableBusinessElements(java.lang.String[] viewableBusinessElements) {
        this.viewableBusinessElements = viewableBusinessElements;
    }
    
    public com.amalto.core.webservice.WSWhereCondition[] getWhereConditions() {
        return whereConditions;
    }
    
    public void setWhereConditions(com.amalto.core.webservice.WSWhereCondition[] whereConditions) {
        this.whereConditions = whereConditions;
    }
    
    public java.lang.String[] getSearchableBusinessElements() {
        return searchableBusinessElements;
    }
    
    public void setSearchableBusinessElements(java.lang.String[] searchableBusinessElements) {
        this.searchableBusinessElements = searchableBusinessElements;
    }
    
    public java.lang.String getTransformerPK() {
        return transformerPK;
    }
    
    public void setTransformerPK(java.lang.String transformerPK) {
        this.transformerPK = transformerPK;
    }
    
    public com.amalto.core.webservice.WSBoolean getIsTransformerActive() {
        return isTransformerActive;
    }
    
    public void setIsTransformerActive(com.amalto.core.webservice.WSBoolean isTransformerActive) {
        this.isTransformerActive = isTransformerActive;
    }
    
    public com.amalto.core.webservice.WSBoolean getIsAsc() {
        return isAsc;
    }
    
    public void setIsAsc(com.amalto.core.webservice.WSBoolean isAsc) {
        this.isAsc = isAsc;
    }
    
    public java.lang.String getSortField() {
        return sortField;
    }
    
    public void setSortField(java.lang.String sortField) {
        this.sortField = sortField;
    }

    public java.lang.String getCustomForm() {
        return customForm;
    }

    public void setCustomForm(java.lang.String customForm) {
        this.customForm = customForm;
    }

}
