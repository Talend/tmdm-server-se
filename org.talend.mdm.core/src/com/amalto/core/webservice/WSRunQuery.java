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

@XmlType(name="WSRunQuery")
public class WSRunQuery {
    protected com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK;
    protected java.lang.String query;
    protected java.lang.String[] parameters;
    
    public WSRunQuery() {
    }
    
    public WSRunQuery(WSDataClusterPK wsDataClusterPK, String query, String[] parameters) {
        this.wsDataClusterPK = wsDataClusterPK;
        this.query = query;
        this.parameters = parameters;
    }
    
    public com.amalto.core.webservice.WSDataClusterPK getWsDataClusterPK() {
        return wsDataClusterPK;
    }
    
    public void setWsDataClusterPK(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK) {
        this.wsDataClusterPK = wsDataClusterPK;
    }
    
    public java.lang.String getQuery() {
        return query;
    }
    
    public void setQuery(java.lang.String query) {
        this.query = query;
    }
    
    public java.lang.String[] getParameters() {
        return parameters;
    }
    
    public void setParameters(java.lang.String[] parameters) {
        this.parameters = parameters;
    }
}
