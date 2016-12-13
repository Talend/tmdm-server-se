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

@XmlType(name="WSItemPKsByCriteriaResponseResults")
public class WSItemPKsByCriteriaResponseResults {
    protected long date;
    protected com.amalto.core.webservice.WSItemPK wsItemPK;
    protected java.lang.String taskId;
    
    public WSItemPKsByCriteriaResponseResults() {
    }
    
    public WSItemPKsByCriteriaResponseResults(long date, com.amalto.core.webservice.WSItemPK wsItemPK, java.lang.String taskId) {
        this.date = date;
        this.wsItemPK = wsItemPK;
        this.taskId = taskId;
    }
    
    public long getDate() {
        return date;
    }
    
    public void setDate(long date) {
        this.date = date;
    }
    
    public com.amalto.core.webservice.WSItemPK getWsItemPK() {
        return wsItemPK;
    }
    
    public void setWsItemPK(com.amalto.core.webservice.WSItemPK wsItemPK) {
        this.wsItemPK = wsItemPK;
    }
    
    public java.lang.String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(java.lang.String taskId) {
        this.taskId = taskId;
    }
}
