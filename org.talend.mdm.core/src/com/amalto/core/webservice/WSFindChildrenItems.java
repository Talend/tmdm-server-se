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

@XmlType(name="WSFindChildrenItems")
public class WSFindChildrenItems {
    protected java.lang.String clusterName;
    protected java.lang.String conceptName;
    protected java.lang.String PKName;
    protected java.lang.String PKXpath;
    protected java.lang.String FKXpath;
    protected java.lang.String labelName;
    protected java.lang.String labelXpath;
    protected java.lang.String fatherPK;
    
    public WSFindChildrenItems() {
    }
    
    public WSFindChildrenItems(java.lang.String clusterName, java.lang.String conceptName, java.lang.String PKName, java.lang.String PKXpath, java.lang.String FKXpath, java.lang.String labelName, java.lang.String labelXpath, java.lang.String fatherPK) {
        this.clusterName = clusterName;
        this.conceptName = conceptName;
        this.PKName = PKName;
        this.PKXpath = PKXpath;
        this.FKXpath = FKXpath;
        this.labelName = labelName;
        this.labelXpath = labelXpath;
        this.fatherPK = fatherPK;
    }
    
    public java.lang.String getClusterName() {
        return clusterName;
    }
    
    public void setClusterName(java.lang.String clusterName) {
        this.clusterName = clusterName;
    }
    
    public java.lang.String getConceptName() {
        return conceptName;
    }
    
    public void setConceptName(java.lang.String conceptName) {
        this.conceptName = conceptName;
    }
    
    public java.lang.String getPKName() {
        return PKName;
    }
    
    public void setPKName(java.lang.String PKName) {
        this.PKName = PKName;
    }
    
    public java.lang.String getPKXpath() {
        return PKXpath;
    }
    
    public void setPKXpath(java.lang.String PKXpath) {
        this.PKXpath = PKXpath;
    }
    
    public java.lang.String getFKXpath() {
        return FKXpath;
    }
    
    public void setFKXpath(java.lang.String FKXpath) {
        this.FKXpath = FKXpath;
    }
    
    public java.lang.String getLabelName() {
        return labelName;
    }
    
    public void setLabelName(java.lang.String labelName) {
        this.labelName = labelName;
    }
    
    public java.lang.String getLabelXpath() {
        return labelXpath;
    }
    
    public void setLabelXpath(java.lang.String labelXpath) {
        this.labelXpath = labelXpath;
    }
    
    public java.lang.String getFatherPK() {
        return fatherPK;
    }
    
    public void setFatherPK(java.lang.String fatherPK) {
        this.fatherPK = fatherPK;
    }
}
