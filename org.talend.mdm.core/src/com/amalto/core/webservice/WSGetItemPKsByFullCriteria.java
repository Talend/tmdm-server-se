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

@XmlType(name="WSGetItemPKsByFullCriteria")
public class WSGetItemPKsByFullCriteria {
    protected com.amalto.core.webservice.WSGetItemPKsByCriteria wsGetItemPKsByCriteria;
    protected boolean useFTSearch;
    
    public WSGetItemPKsByFullCriteria() {
    }
    
    public WSGetItemPKsByFullCriteria(com.amalto.core.webservice.WSGetItemPKsByCriteria wsGetItemPKsByCriteria, boolean useFTSearch) {
        this.wsGetItemPKsByCriteria = wsGetItemPKsByCriteria;
        this.useFTSearch = useFTSearch;
    }
    
    public com.amalto.core.webservice.WSGetItemPKsByCriteria getWsGetItemPKsByCriteria() {
        return wsGetItemPKsByCriteria;
    }
    
    public void setWsGetItemPKsByCriteria(com.amalto.core.webservice.WSGetItemPKsByCriteria wsGetItemPKsByCriteria) {
        this.wsGetItemPKsByCriteria = wsGetItemPKsByCriteria;
    }
    
    public boolean isUseFTSearch() {
        return useFTSearch;
    }
    
    public void setUseFTSearch(boolean useFTSearch) {
        this.useFTSearch = useFTSearch;
    }
}
