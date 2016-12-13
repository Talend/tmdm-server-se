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

@XmlType(name="WSRoutingOrderV2")
public class WSRoutingOrderV2 {
    protected java.lang.String name;
    protected com.amalto.core.webservice.WSRoutingOrderV2Status status;
    protected long timeCreated;
    protected long timeScheduled;
    protected long timeLastRunStarted;
    protected long timeLastRunCompleted;
    protected com.amalto.core.webservice.WSItemPK wsItemPK;
    protected java.lang.String serviceJNDI;
    protected java.lang.String serviceParameters;
    protected java.lang.String message;
    protected java.lang.String bindingUniverseName;
    protected java.lang.String bindingUserToken;
    
    public WSRoutingOrderV2() {
    }
    
    public WSRoutingOrderV2(java.lang.String name, com.amalto.core.webservice.WSRoutingOrderV2Status status, long timeCreated, long timeScheduled, long timeLastRunStarted, long timeLastRunCompleted, com.amalto.core.webservice.WSItemPK wsItemPK, java.lang.String serviceJNDI, java.lang.String serviceParameters, java.lang.String message, java.lang.String bindingUniverseName, java.lang.String bindingUserToken) {
        this.name = name;
        this.status = status;
        this.timeCreated = timeCreated;
        this.timeScheduled = timeScheduled;
        this.timeLastRunStarted = timeLastRunStarted;
        this.timeLastRunCompleted = timeLastRunCompleted;
        this.wsItemPK = wsItemPK;
        this.serviceJNDI = serviceJNDI;
        this.serviceParameters = serviceParameters;
        this.message = message;
        this.bindingUniverseName = bindingUniverseName;
        this.bindingUserToken = bindingUserToken;
    }
    
    public java.lang.String getName() {
        return name;
    }
    
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    public com.amalto.core.webservice.WSRoutingOrderV2Status getStatus() {
        return status;
    }
    
    public void setStatus(com.amalto.core.webservice.WSRoutingOrderV2Status status) {
        this.status = status;
    }
    
    public long getTimeCreated() {
        return timeCreated;
    }
    
    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }
    
    public long getTimeScheduled() {
        return timeScheduled;
    }
    
    public void setTimeScheduled(long timeScheduled) {
        this.timeScheduled = timeScheduled;
    }
    
    public long getTimeLastRunStarted() {
        return timeLastRunStarted;
    }
    
    public void setTimeLastRunStarted(long timeLastRunStarted) {
        this.timeLastRunStarted = timeLastRunStarted;
    }
    
    public long getTimeLastRunCompleted() {
        return timeLastRunCompleted;
    }
    
    public void setTimeLastRunCompleted(long timeLastRunCompleted) {
        this.timeLastRunCompleted = timeLastRunCompleted;
    }
    
    public com.amalto.core.webservice.WSItemPK getWsItemPK() {
        return wsItemPK;
    }
    
    public void setWsItemPK(com.amalto.core.webservice.WSItemPK wsItemPK) {
        this.wsItemPK = wsItemPK;
    }
    
    public java.lang.String getServiceJNDI() {
        return serviceJNDI;
    }
    
    public void setServiceJNDI(java.lang.String serviceJNDI) {
        this.serviceJNDI = serviceJNDI;
    }
    
    public java.lang.String getServiceParameters() {
        return serviceParameters;
    }
    
    public void setServiceParameters(java.lang.String serviceParameters) {
        this.serviceParameters = serviceParameters;
    }
    
    public java.lang.String getMessage() {
        return message;
    }
    
    public void setMessage(java.lang.String message) {
        this.message = message;
    }
    
    public java.lang.String getBindingUniverseName() {
        return bindingUniverseName;
    }
    
    public void setBindingUniverseName(java.lang.String bindingUniverseName) {
        this.bindingUniverseName = bindingUniverseName;
    }
    
    public java.lang.String getBindingUserToken() {
        return bindingUserToken;
    }
    
    public void setBindingUserToken(java.lang.String bindingUserToken) {
        this.bindingUserToken = bindingUserToken;
    }
}
