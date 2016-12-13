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

@XmlType(name="WSItemPKsByCriteriaResponse")
public class WSItemPKsByCriteriaResponse {
    protected com.amalto.core.webservice.WSItemPKsByCriteriaResponseResults[] results;
    
    public WSItemPKsByCriteriaResponse() {
    }
    
    public WSItemPKsByCriteriaResponse(com.amalto.core.webservice.WSItemPKsByCriteriaResponseResults[] results) {
        this.results = results;
    }
    
    public com.amalto.core.webservice.WSItemPKsByCriteriaResponseResults[] getResults() {
        return results;
    }
    
    public void setResults(com.amalto.core.webservice.WSItemPKsByCriteriaResponseResults[] results) {
        this.results = results;
    }
}
