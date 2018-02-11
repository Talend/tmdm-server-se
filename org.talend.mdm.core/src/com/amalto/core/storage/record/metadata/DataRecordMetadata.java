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

import java.util.Map;

/**
 * Additional metadata record specific. Information provided by this interface vary from one {@link com.amalto.core.storage.record.DataRecord} to another
 * in results (where it's less likely for type metadata such as {@link org.talend.mdm.commmon.metadata.ComplexTypeMetadata}).
 */
public interface DataRecordMetadata {

    /**
     * @return A long that represents the date time of the last modification.
     */
    long getLastModificationTime();

    /**
     * Set the last modification time.
     * @param lastModificationTime New last modification time value.
     */
    void setLastModificationTime(long lastModificationTime);

    /**
     * @return The associated task id to the {@link com.amalto.core.storage.record.DataRecord} or null if no task id is associated to the record.
     */
    String getTaskId();

    /**
     * Set the task id.
     * @param taskId New value for task id.
     */
    void setTaskId(String taskId);
    
    /**
     * @return A set of non-constrained {@link com.amalto.core.storage.record.DataRecord} properties.
     */
    Map<String, String> getRecordProperties();

    /**
     * @return A copy of this {@link DataRecordMetadata} instance ({@link #getRecordProperties()} is also expected
     * to be a copy of this original instance).
     */
    DataRecordMetadata copy();
}
