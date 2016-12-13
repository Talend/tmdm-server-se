/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.task;

/**
 * <ul>
 * <li>000: New</li>
 * <li>201: Processed by 'identify clusters' successfully.</li>
 * <li>202: Processed by 'merge clusters' successfully.</li>
 * <li>203: Processed by 'merge clusters' but group is bigger than 1 -> manual merge required.</li>
 * <li>204: Processed by 'merge clusters' and this record is the "golden" record to be used in validation.</li>
 * <li>205: Processed by 'validate & save'.</li>
 * <li>401: Processed by 'identify clusters' but failed.</li>
 * <li>402: Processed by 'merge clusters' but failed.</li>
 * <li>403: Processed by 'validate & save' but failed due to validation issue.</li>
 * <li>404: Processed by 'validate & save' but failed due to database constraint issue.</li>
 * </ul>
 */
public interface StagingConstants {
    /**
     * NEW: Newly created (or updated) record.
     */
    String NEW = "000"; //$NON-NLS-1$
    /**
     * Generic success status
     */
    String SUCCESS = "200"; //$NON-NLS-1$
    /**
     * Passed successfully match cluster identification (groups similar records).
     */
    String SUCCESS_IDENTIFIED_CLUSTERS = "201"; //$NON-NLS-1$
    /**
     * Passed successfully merge cluster (a golden record was successfully created).
     * @see #SUCCESS_MERGED_RECORD
     * @see #SUCCESS_MERGED_RECORD_TO_RESOLVE
     */
    String SUCCESS_MERGE_CLUSTERS = "202"; //$NON-NLS-1$
    /**
     * Passed successfully merge cluster (created a golden record, but confidence value does not allow automatic
     * creation in master database).
     * @see #SUCCESS_MERGED_RECORD
     */
    String SUCCESS_MERGED_RECORD_TO_RESOLVE = "203"; //$NON-NLS-1$
    /**
     * Passed successfully merge cluster (created a golden record, and confidence value allowed automatic
     * creation in master database).
     * @see
     *
     *
     * #SUCCESS_MERGED_RECORD_TO_RESOLVE
     */
    String SUCCESS_MERGED_RECORD = "204"; //$NON-NLS-1$
    /**
     * Passed successfully MDM validation (XML schema validation, business rules, before saving processes...).
     */
    String SUCCESS_VALIDATE = "205"; //$NON-NLS-1$
    /**
     * Indicates a deleted record.
     */
    String DELETED = "206"; //$NON-NLS-1$
    /**
     * Indicates record merged using with a DSC task resolution.
     */
    String TASK_RESOLVED_RECORD = "207"; //$NON-NLS-1$
    /**
     * Indicates a deleted record.
     */
    String NEED_REMATCH = "208"; //$NON-NLS-1$
    /**
     * Generic fail status.
     */
    String FAIL = "400"; //$NON-NLS-1$
    /**
     * Failed match cluster identification (groups similar records).
     */
    String FAIL_IDENTIFIED_CLUSTERS = "401"; //$NON-NLS-1$
    /**
     * Failed merge cluster (a golden record was not successfully created).
     */
    String FAIL_MERGE_CLUSTERS = "402"; //$NON-NLS-1$
    /**
     * Failed MDM validation.
     */
    String FAIL_VALIDATE_VALIDATION = "403"; //$NON-NLS-1$
    /**
     * Failed MDM validation due to constraints issues.
     */
    String FAIL_VALIDATE_CONSTRAINTS = "404"; //$NON-NLS-1$
    /**
     * Failed MDM delete due to constraints issues.
     */
    String FAIL_DELETE_CONSTRAINTS = "405"; //$NON-NLS-1$
    /**
     * Constant to indicate a record source when created by MDM.
     */
    String STAGING_MDM_SOURCE = "MDM"; //$NON-NLS-1$
}
