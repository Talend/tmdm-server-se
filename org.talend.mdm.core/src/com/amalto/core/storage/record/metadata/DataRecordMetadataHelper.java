/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.record.metadata;

import com.amalto.core.query.user.metadata.StagingBlockKey;
import com.amalto.core.query.user.metadata.StagingError;
import com.amalto.core.query.user.metadata.StagingSource;
import com.amalto.core.query.user.metadata.StagingStatus;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.record.DataRecordReader;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 *
 */
public class DataRecordMetadataHelper {

    private static final Logger LOGGER = Logger.getLogger(DataRecordMetadataHelper.class);

    private DataRecordMetadataHelper() {
    }

    public static void setMetadataValue(DataRecordMetadata metadata, String metadataProperty, String value) {
        Map<String, String> properties = metadata.getRecordProperties();
        if (StagingError.STAGING_ERROR_ALIAS.equals(metadataProperty)) {
            properties.put(Storage.METADATA_STAGING_ERROR, value);
        } else if (StagingSource.STAGING_SOURCE_ALIAS.equals(metadataProperty)) {
            properties.put(Storage.METADATA_STAGING_SOURCE, value);
        } else if (StagingStatus.STAGING_STATUS_ALIAS.equals(metadataProperty)) {
            properties.put(Storage.METADATA_STAGING_STATUS, value);
        } else if (StagingBlockKey.STAGING_BLOCK_ALIAS.equals(metadataProperty)) {
            properties.put(Storage.METADATA_STAGING_BLOCK_KEY, value);
        } else if (DataRecordReader.TASK_ID.equals(metadataProperty)) {
            metadata.setTaskId(value);
        } else if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Metadata parameter '" + metadataProperty + "' is not supported.");
        }
    }
}
