/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.model;

import com.amalto.core.storage.task.StagingConstants;
import com.google.gwt.user.client.rpc.IsSerializable;

public enum RecordStatus implements IsSerializable {
    
    NEW(Integer.parseInt(StagingConstants.NEW)),
    SUCCESS(Integer.parseInt(StagingConstants.SUCCESS)),
    SUCCESS_IDENTIFIED_CLUSTERS(Integer.parseInt(StagingConstants.SUCCESS_IDENTIFIED_CLUSTERS)),
    SUCCESS_MERGE_CLUSTERS(Integer.parseInt(StagingConstants.SUCCESS_MERGE_CLUSTERS)),
    SUCCESS_MERGE_CLUSTER_TO_RESOLVE(Integer.parseInt(StagingConstants.SUCCESS_MERGED_RECORD_TO_RESOLVE)),
    SUCCESS_MERGED_RECORD(Integer.parseInt(StagingConstants.SUCCESS_MERGED_RECORD)),
    SUCCESS_VALIDATE(Integer.parseInt(StagingConstants.SUCCESS_VALIDATE)),
    SUCCESS_DELETED(Integer.parseInt(StagingConstants.DELETED)),
    FAIL(Integer.parseInt(StagingConstants.FAIL)),
    FAIL_IDENTIFIED_CLUSTERS(Integer.parseInt(StagingConstants.FAIL_IDENTIFIED_CLUSTERS)),
    FAIL_MERGE_CLUSTERS(Integer.parseInt(StagingConstants.FAIL_MERGE_CLUSTERS)),
    FAIL_VALIDATE_VALIDATION(Integer.parseInt(StagingConstants.FAIL_VALIDATE_VALIDATION)),
    FAIL_VALIDATE_CONSTRAINTS(Integer.parseInt(StagingConstants.FAIL_VALIDATE_CONSTRAINTS)),
    FAIL_DELETE_CONSTRAINTS(Integer.parseInt(StagingConstants.FAIL_DELETE_CONSTRAINTS)),
    UNKNOWN(999);
    
    private final int statusCode;

    RecordStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static RecordStatus newStatus(int statusCode) {
        for (RecordStatus recordStatus : RecordStatus.values()) {
            if (recordStatus.getStatusCode() == statusCode) {
                return recordStatus;
            }
        }
        return UNKNOWN;
    }

}
