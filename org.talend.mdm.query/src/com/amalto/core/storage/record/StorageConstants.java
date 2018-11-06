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

public class StorageConstants {
    /**
     * Name of the column where last MDM validation error is stored (for STAGING databases only).
     */
    public final static String METADATA_STAGING_ERROR     = "x_talend_staging_error";   //$NON-NLS-1$
    /**
     * Name of the column where MDM status (validated...) is stored (for STAGING databases only).
     * com.amalto.core.storage.task.StagingConstants
     *
     */
    public final static String METADATA_STAGING_STATUS    = "x_talend_staging_status";  //$NON-NLS-1$

    /**
     * Name of the column where a block key can be stored (for STAGING databases only).
     */
    public final static String METADATA_STAGING_BLOCK_KEY = "x_talend_staging_blockkey"; //$NON-NLS-1$
    /**
     * Name of type for explicit projection (i.e. selection of a field within MDM entity). Declared fields in this type
     * varies from one query to another (if selected fields in query changed).
     */
    public final static String PROJECTION_TYPE            = "$ExplicitProjection$";     //$NON-NLS-1$
    /**
     * Name of the column where MDM has task is stored (for STAGING databases only).
     * com.amalto.core.storage.task.StagingConstants
     *
     */
    public final static String METADATA_STAGING_HAS_TASK    = "x_talend_staging_hastask";  //$NON-NLS-1$

    /**
     * Name of the column where MDM source is stored (for STAGING databases only).
     */
    public final static String METADATA_STAGING_SOURCE    = "x_talend_staging_source";  //$NON-NLS-1$

    /**
     * Indicates storage supports transactions.
     */
    public final static byte  CAP_TRANSACTION            = 1;

    /**
     * Indicates storage supports full text queries.
     */
    public final static byte  CAP_FULL_TEXT              = 2;

    /**
     * Indicate storage supports referential integrity.
     */
    public final static byte  CAP_INTEGRITY              = 4;

    /**
     * Name of the column where MDM timestamp is stored.
     */
    public final static String METADATA_TIMESTAMP         = "x_talend_timestamp";       //$NON-NLS-1$

    /**
     * Name of the column where MDM task id is stored.
     */
    public final static String METADATA_TASK_ID           = "x_talend_task_id";         //$NON-NLS-1$

    /**
     * <p>
     * Name of the column where previous values that built a golden record can be stored (for STAGING databases and
     * golden records only).
     * </p>
     * <p>
     * This columns contains a Base64-encoded ZIP content. To read the value, you first to decode the Base64 content
     * <b>then</b> unzip it:
     *
     * <pre>
     * String value; // Value from database
     * ZipInputStream in = new ZipInputStream(new Base64InputStream(new ByteArrayInputStream(value.getBytes())));
     * in.getNextEntry();
     * ObjectInputStream inputStream = new ObjectInputStream(in);
     * Map o = (Map) inputStream.readObject();
     * </pre>
     *
     * </p>
     */
    public final static String METADATA_STAGING_VALUES    = "x_talend_staging_values";  //$NON-NLS-1$

}
