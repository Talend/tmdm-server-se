/*
  * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
  * 
  * This source code is available under agreement available at
  * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
  * 
  * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
  * 92150 Suresnes, France
  */
package com.amalto.core.storage.record;

public interface CommonStorage {
    /**
     * Name of the column where last MDM validation error is stored (for STAGING databases only).
     */
    String METADATA_STAGING_ERROR     = "x_talend_staging_error";   //$NON-NLS-1$
    /**
     * Name of the column where MDM status (validated...) is stored (for STAGING databases only).
     * com.amalto.core.storage.task.StagingConstants
     *
     */
    String METADATA_STAGING_STATUS    = "x_talend_staging_status";  //$NON-NLS-1$

    /**
     * Name of the column where a block key can be stored (for STAGING databases only).
     */
    String METADATA_STAGING_BLOCK_KEY = "x_talend_staging_blockkey"; //$NON-NLS-1$
    /**
     * Name of type for explicit projection (i.e. selection of a field within MDM entity). Declared fields in this type
     * varies from one query to another (if selected fields in query changed).
     */
    String PROJECTION_TYPE            = "$ExplicitProjection$";     //$NON-NLS-1$
    /**
     * Name of the column where MDM has task is stored (for STAGING databases only).
     * com.amalto.core.storage.task.StagingConstants
     *
     */
    String METADATA_STAGING_HAS_TASK    = "x_talend_staging_hastask";  //$NON-NLS-1$

    /**
     * Name of the column where MDM source is stored (for STAGING databases only).
     */
    String METADATA_STAGING_SOURCE    = "x_talend_staging_source";  //$NON-NLS-1$

}
