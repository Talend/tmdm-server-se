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

@XmlType(name="WSCount")
public class WSCount {
    protected com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK;
    protected java.lang.String countPath;
    protected com.amalto.core.webservice.WSWhereItem whereItem;
    protected int spellTreshold;
    
    public WSCount() {
    }
    
    public WSCount(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK, java.lang.String countPath, com.amalto.core.webservice.WSWhereItem whereItem, int spellTreshold) {
        this.wsDataClusterPK = wsDataClusterPK;
        this.countPath = countPath;
        this.whereItem = whereItem;
        this.spellTreshold = spellTreshold;
    }
    
    public com.amalto.core.webservice.WSDataClusterPK getWsDataClusterPK() {
        return wsDataClusterPK;
    }
    
    public void setWsDataClusterPK(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK) {
        this.wsDataClusterPK = wsDataClusterPK;
    }
    
    public java.lang.String getCountPath() {
        return countPath;
    }
    
    public void setCountPath(java.lang.String countPath) {
        this.countPath = countPath;
    }
    
    public com.amalto.core.webservice.WSWhereItem getWhereItem() {
        return whereItem;
    }
    
    public void setWhereItem(com.amalto.core.webservice.WSWhereItem whereItem) {
        this.whereItem = whereItem;
    }
    
    public int getSpellTreshold() {
        return spellTreshold;
    }
    
    public void setSpellTreshold(int spellTreshold) {
        this.spellTreshold = spellTreshold;
    }
}
