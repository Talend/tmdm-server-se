/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.record.metadata;

import com.amalto.core.storage.Storage;

import java.util.HashMap;
import java.util.Map;

public class DataRecordMetadataImpl implements DataRecordMetadata {

    private long lastModificationTime;

    private String taskId;

    private Map<String,String> recordProperties;

    public DataRecordMetadataImpl() {

    }
    
    public DataRecordMetadataImpl(long lastModificationTime, String taskId) {
        this.lastModificationTime = lastModificationTime;
        this.taskId = taskId;
    }

    public long getLastModificationTime() {
        return lastModificationTime;
    }

    public void setLastModificationTime(long lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
        if (recordProperties != null) {
            recordProperties.put(Storage.METADATA_TIMESTAMP, String.valueOf(lastModificationTime)); // Overrides timestamp in record properties (in case it was there).
        }
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
        if (recordProperties != null) {
            recordProperties.put(Storage.METADATA_TASK_ID, taskId); // Overrides task id in record properties (in case it was there).
        }
    }

    public Map<String, String> getRecordProperties() {
        if (recordProperties == null) {
            recordProperties = new HashMap<String, String>();
        }
        return recordProperties;
    }

    @Override
    public DataRecordMetadata copy() {
        DataRecordMetadataImpl copy = new DataRecordMetadataImpl(lastModificationTime, taskId);
        if (recordProperties != null) {
            copy.recordProperties = new HashMap<String, String>(this.recordProperties);
        }
        return copy;
    }
}
