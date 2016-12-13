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

@XmlType(name="WSRoutingOrderV2SearchCriteria")
public class WSRoutingOrderV2SearchCriteria {
    protected com.amalto.core.webservice.WSRoutingOrderV2Status status;
    protected java.lang.String anyFieldContains;
    protected java.lang.String nameContains;
    protected long timeCreatedMin;
    protected long timeCreatedMax;
    protected long timeScheduledMin;
    protected long timeScheduledMax;
    protected long timeLastRunStartedMin;
    protected long timeLastRunStartedMax;
    protected long timeLastRunCompletedMin;
    protected long timeLastRunCompletedMax;
    protected java.lang.String itemPKConceptContains;
    protected java.lang.String itemPKIDFieldsContain;
    protected java.lang.String serviceJNDIContains;
    protected java.lang.String serviceParametersContain;
    protected java.lang.String messageContain;
    
    public WSRoutingOrderV2SearchCriteria() {
    }
    
    public WSRoutingOrderV2SearchCriteria(com.amalto.core.webservice.WSRoutingOrderV2Status status, java.lang.String anyFieldContains, java.lang.String nameContains, long timeCreatedMin, long timeCreatedMax, long timeScheduledMin, long timeScheduledMax, long timeLastRunStartedMin, long timeLastRunStartedMax, long timeLastRunCompletedMin, long timeLastRunCompletedMax, java.lang.String itemPKConceptContains, java.lang.String itemPKIDFieldsContain, java.lang.String serviceJNDIContains, java.lang.String serviceParametersContain, java.lang.String messageContain) {
        this.status = status;
        this.anyFieldContains = anyFieldContains;
        this.nameContains = nameContains;
        this.timeCreatedMin = timeCreatedMin;
        this.timeCreatedMax = timeCreatedMax;
        this.timeScheduledMin = timeScheduledMin;
        this.timeScheduledMax = timeScheduledMax;
        this.timeLastRunStartedMin = timeLastRunStartedMin;
        this.timeLastRunStartedMax = timeLastRunStartedMax;
        this.timeLastRunCompletedMin = timeLastRunCompletedMin;
        this.timeLastRunCompletedMax = timeLastRunCompletedMax;
        this.itemPKConceptContains = itemPKConceptContains;
        this.itemPKIDFieldsContain = itemPKIDFieldsContain;
        this.serviceJNDIContains = serviceJNDIContains;
        this.serviceParametersContain = serviceParametersContain;
        this.messageContain = messageContain;
    }
    
    public com.amalto.core.webservice.WSRoutingOrderV2Status getStatus() {
        return status;
    }
    
    public void setStatus(com.amalto.core.webservice.WSRoutingOrderV2Status status) {
        this.status = status;
    }
    
    public java.lang.String getAnyFieldContains() {
        return anyFieldContains;
    }
    
    public void setAnyFieldContains(java.lang.String anyFieldContains) {
        this.anyFieldContains = anyFieldContains;
    }
    
    public java.lang.String getNameContains() {
        return nameContains;
    }
    
    public void setNameContains(java.lang.String nameContains) {
        this.nameContains = nameContains;
    }
    
    public long getTimeCreatedMin() {
        return timeCreatedMin;
    }
    
    public void setTimeCreatedMin(long timeCreatedMin) {
        this.timeCreatedMin = timeCreatedMin;
    }
    
    public long getTimeCreatedMax() {
        return timeCreatedMax;
    }
    
    public void setTimeCreatedMax(long timeCreatedMax) {
        this.timeCreatedMax = timeCreatedMax;
    }
    
    public long getTimeScheduledMin() {
        return timeScheduledMin;
    }
    
    public void setTimeScheduledMin(long timeScheduledMin) {
        this.timeScheduledMin = timeScheduledMin;
    }
    
    public long getTimeScheduledMax() {
        return timeScheduledMax;
    }
    
    public void setTimeScheduledMax(long timeScheduledMax) {
        this.timeScheduledMax = timeScheduledMax;
    }
    
    public long getTimeLastRunStartedMin() {
        return timeLastRunStartedMin;
    }
    
    public void setTimeLastRunStartedMin(long timeLastRunStartedMin) {
        this.timeLastRunStartedMin = timeLastRunStartedMin;
    }
    
    public long getTimeLastRunStartedMax() {
        return timeLastRunStartedMax;
    }
    
    public void setTimeLastRunStartedMax(long timeLastRunStartedMax) {
        this.timeLastRunStartedMax = timeLastRunStartedMax;
    }
    
    public long getTimeLastRunCompletedMin() {
        return timeLastRunCompletedMin;
    }
    
    public void setTimeLastRunCompletedMin(long timeLastRunCompletedMin) {
        this.timeLastRunCompletedMin = timeLastRunCompletedMin;
    }
    
    public long getTimeLastRunCompletedMax() {
        return timeLastRunCompletedMax;
    }
    
    public void setTimeLastRunCompletedMax(long timeLastRunCompletedMax) {
        this.timeLastRunCompletedMax = timeLastRunCompletedMax;
    }
    
    public java.lang.String getItemPKConceptContains() {
        return itemPKConceptContains;
    }
    
    public void setItemPKConceptContains(java.lang.String itemPKConceptContains) {
        this.itemPKConceptContains = itemPKConceptContains;
    }
    
    public java.lang.String getItemPKIDFieldsContain() {
        return itemPKIDFieldsContain;
    }
    
    public void setItemPKIDFieldsContain(java.lang.String itemPKIDFieldsContain) {
        this.itemPKIDFieldsContain = itemPKIDFieldsContain;
    }
    
    public java.lang.String getServiceJNDIContains() {
        return serviceJNDIContains;
    }
    
    public void setServiceJNDIContains(java.lang.String serviceJNDIContains) {
        this.serviceJNDIContains = serviceJNDIContains;
    }
    
    public java.lang.String getServiceParametersContain() {
        return serviceParametersContain;
    }
    
    public void setServiceParametersContain(java.lang.String serviceParametersContain) {
        this.serviceParametersContain = serviceParametersContain;
    }
    
    public java.lang.String getMessageContain() {
        return messageContain;
    }
    
    public void setMessageContain(java.lang.String messageContain) {
        this.messageContain = messageContain;
    }
}
