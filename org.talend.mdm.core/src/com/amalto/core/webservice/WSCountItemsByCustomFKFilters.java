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

@XmlType(name="WSCountItemsByCustomFKFilters")
public class WSCountItemsByCustomFKFilters {
    protected com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK;
    protected java.lang.String conceptName;
    protected java.lang.String injectedXpath;
    
    public WSCountItemsByCustomFKFilters() {
    }
    
    public WSCountItemsByCustomFKFilters(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK, java.lang.String conceptName, java.lang.String injectedXpath) {
        this.wsDataClusterPK = wsDataClusterPK;
        this.conceptName = conceptName;
        this.injectedXpath = injectedXpath;
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
    
    public java.lang.String getInjectedXpath() {
        return injectedXpath;
    }
    
    public void setInjectedXpath(java.lang.String injectedXpath) {
        this.injectedXpath = injectedXpath;
    }
}
