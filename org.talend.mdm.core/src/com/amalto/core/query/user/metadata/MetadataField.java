/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.query.user.metadata;

import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.storage.record.DataRecord;

/**
 * Represents a "metadata" field: mostly a technical field, a metadata field is not user defined but associated with the
 * MDM record.
 * 
 * @see MetadataField.Factory#getMetadataField(String)
 * @see MetadataField#SUPPORTED_METADATA
 */
public interface MetadataField extends TypedExpression {

    /**
     * Lists all metadata fields supported by this MDM.
     */
    MetadataField[] SUPPORTED_METADATA = new MetadataField[] { StagingError.INSTANCE, StagingSource.INSTANCE,
            StagingStatus.INSTANCE, StagingBlockKey.INSTANCE, StagingHasTask.INSTANCE, GroupSize.INSTANCE, TaskId.INSTANCE,
            Timestamp.INSTANCE };

    /**
     * @return A non-null name that represents the metadata field. Each serialization format may alter or reuse as-is
     * this name.
     */
    String getFieldName();

    /**
     * @param path A XPath-like string.
     * @return <code>true</code> if path is used with this metadata field.
     */
    boolean matches(String path);

    /**
     * @return <code>true</code> if metadata field is read only, <code>false</code> otherwise.
     */
    boolean isReadOnly();

    /**
     * @return A {@link TypedExpression} that can be used as a left part of a
     * {@link com.amalto.core.query.user.BinaryLogicOperator} for instance.
     * @see com.amalto.core.query.user.UserQueryBuilder#where(com.amalto.core.query.user.Condition)
     */
    TypedExpression getConditionExpression();

    /**
     * @return A {@link TypedExpression} that can be used in a {@link com.amalto.core.query.user.Select}.
     * @see com.amalto.core.query.user.UserQueryBuilder#select(com.amalto.core.query.user.TypedExpression)
     */
    TypedExpression getProjectionExpression();

    /**
     * @return A {@link Reader reader} that ease metadata value read from a
     * {@link com.amalto.core.storage.record.DataRecord record}.
     * @throws java.lang.UnsupportedOperationException If data can't be read from record.
     */
    Reader getReader();

    class Factory {

        /**
         * Returns the first {@link com.amalto.core.query.user.metadata.MetadataField field} represented by
         * <code>path</code>.
         * 
         * @param path A path (usually a metadata field name).
         * @return The first {@link com.amalto.core.query.user.metadata.MetadataField field} to match the
         * <code>path</code> or <code>null</code> if no candidate was found.
         * @see com.amalto.core.query.user.metadata.MetadataField#matches(String)
         */
        public static MetadataField getMetadataField(String path) {
            for (MetadataField metadataField : SUPPORTED_METADATA) {
                if (metadataField.matches(path)) {
                    return metadataField;
                }
            }
            return null;
        }
    }

    /**
     * Abstract access to the {@link com.amalto.core.storage.record.DataRecord#getRecordMetadata()} content.
     */
    interface Reader {

        /**
         * Returns the metadata field value defined in <code>record</code>.
         * 
         * @param record A {@link DataRecord record}.
         * @return The metadata value in <code>record</code>, <code>null</code> if not defined or not set.
         */
        Object readValue(DataRecord record);
    }

}
