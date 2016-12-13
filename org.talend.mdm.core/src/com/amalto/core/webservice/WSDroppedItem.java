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

@XmlType(name="WSDroppedItem")
public class WSDroppedItem {
    protected com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK;
    protected java.lang.String uniqueId;
    protected java.lang.String conceptName;
    protected java.lang.String[] ids;
    protected java.lang.String partPath;
    protected java.lang.String insertionUserName;
    protected java.lang.Long insertionTime;
    protected java.lang.String projection;
    
    public WSDroppedItem() {
    }
    
    public WSDroppedItem(WSDataClusterPK wsDataClusterPK, String uniqueId, String conceptName, String[] ids, String partPath, String insertionUserName, Long insertionTime, String projection) {
        this.wsDataClusterPK = wsDataClusterPK;
        this.uniqueId = uniqueId;
        this.conceptName = conceptName;
        this.ids = ids;
        this.partPath = partPath;
        this.insertionUserName = insertionUserName;
        this.insertionTime = insertionTime;
        this.projection = projection;
    }

    public com.amalto.core.webservice.WSDataClusterPK getWsDataClusterPK() {
        return wsDataClusterPK;
    }
    
    public void setWsDataClusterPK(com.amalto.core.webservice.WSDataClusterPK wsDataClusterPK) {
        this.wsDataClusterPK = wsDataClusterPK;
    }
    
    public java.lang.String getUniqueId() {
        return uniqueId;
    }
    
    public void setUniqueId(java.lang.String uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    public java.lang.String getConceptName() {
        return conceptName;
    }
    
    public void setConceptName(java.lang.String conceptName) {
        this.conceptName = conceptName;
    }
    
    public java.lang.String[] getIds() {
        return ids;
    }
    
    public void setIds(java.lang.String[] ids) {
        this.ids = ids;
    }
    
    public java.lang.String getPartPath() {
        return partPath;
    }
    
    public void setPartPath(java.lang.String partPath) {
        this.partPath = partPath;
    }
    
    public java.lang.String getInsertionUserName() {
        return insertionUserName;
    }
    
    public void setInsertionUserName(java.lang.String insertionUserName) {
        this.insertionUserName = insertionUserName;
    }
    
    public java.lang.Long getInsertionTime() {
        return insertionTime;
    }
    
    public void setInsertionTime(java.lang.Long insertionTime) {
        this.insertionTime = insertionTime;
    }
    
    public java.lang.String getProjection() {
        return projection;
    }
    
    public void setProjection(java.lang.String projection) {
        this.projection = projection;
    }
}
