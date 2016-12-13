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

@XmlType(name="WSBackgroundJob")
public class WSBackgroundJob {
    protected java.lang.String id;
    protected java.lang.String description;
    protected com.amalto.core.webservice.BackgroundJobStatusType status;
    protected java.lang.String message;
    protected java.lang.Integer percentage;
    protected java.lang.String timestamp;
    protected com.amalto.core.webservice.WSPipeline pipeline;
    protected byte[] serializedObject;
    
    public WSBackgroundJob() {
    }
    
    public WSBackgroundJob(java.lang.String id, java.lang.String description, com.amalto.core.webservice.BackgroundJobStatusType status, java.lang.String message, java.lang.Integer percentage, java.lang.String timestamp, com.amalto.core.webservice.WSPipeline pipeline, byte[] serializedObject) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.message = message;
        this.percentage = percentage;
        this.timestamp = timestamp;
        this.pipeline = pipeline;
        this.serializedObject = serializedObject;
    }
    
    public java.lang.String getId() {
        return id;
    }
    
    public void setId(java.lang.String id) {
        this.id = id;
    }
    
    public java.lang.String getDescription() {
        return description;
    }
    
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    public com.amalto.core.webservice.BackgroundJobStatusType getStatus() {
        return status;
    }
    
    public void setStatus(com.amalto.core.webservice.BackgroundJobStatusType status) {
        this.status = status;
    }
    
    public java.lang.String getMessage() {
        return message;
    }
    
    public void setMessage(java.lang.String message) {
        this.message = message;
    }
    
    public java.lang.Integer getPercentage() {
        return percentage;
    }
    
    public void setPercentage(java.lang.Integer percentage) {
        this.percentage = percentage;
    }
    
    public java.lang.String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(java.lang.String timestamp) {
        this.timestamp = timestamp;
    }
    
    public com.amalto.core.webservice.WSPipeline getPipeline() {
        return pipeline;
    }
    
    public void setPipeline(com.amalto.core.webservice.WSPipeline pipeline) {
        this.pipeline = pipeline;
    }
    
    public byte[] getSerializedObject() {
        return serializedObject;
    }
    
    public void setSerializedObject(byte[] serializedObject) {
        this.serializedObject = serializedObject;
    }
}
