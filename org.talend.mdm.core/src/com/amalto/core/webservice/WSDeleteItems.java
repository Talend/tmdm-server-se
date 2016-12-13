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

@XmlType(name="WSDeleteItems")
public class WSDeleteItems {
    protected com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK;
    protected java.lang.String conceptName;
    protected com.amalto.core.webservice.WSWhereItem wsWhereItem;
    protected int spellTreshold;
    protected java.lang.Boolean override;
    
    public WSDeleteItems() {
    }
    
    public WSDeleteItems(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK, java.lang.String conceptName, com.amalto.core.webservice.WSWhereItem wsWhereItem, int spellTreshold, java.lang.Boolean override) {
        this.wsDataClusterPK = wsDataClusterPK;
        this.conceptName = conceptName;
        this.wsWhereItem = wsWhereItem;
        this.spellTreshold = spellTreshold;
        this.override = override;
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
    
    public com.amalto.core.webservice.WSWhereItem getWsWhereItem() {
        return wsWhereItem;
    }
    
    public void setWsWhereItem(com.amalto.core.webservice.WSWhereItem wsWhereItem) {
        this.wsWhereItem = wsWhereItem;
    }
    
    public int getSpellTreshold() {
        return spellTreshold;
    }
    
    public void setSpellTreshold(int spellTreshold) {
        this.spellTreshold = spellTreshold;
    }
    
    public java.lang.Boolean getOverride() {
        return override;
    }
    
    public void setOverride(java.lang.Boolean override) {
        this.override = override;
    }
}
