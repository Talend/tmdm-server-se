/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingareabrowser.client.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum RecordStatus implements IsSerializable {

    NEW(000),
    SUCCESS(200),
    SUCCESS_IDENTIFIED_CLUSTERS(201),
    SUCCESS_MERGE_CLUSTERS(202),
    SUCCESS_MERGE_CLUSTER_TO_RESOLVE(203),
    SUCCESS_MERGED_RECORD(204),
    SUCCESS_VALIDATE(205),
    SUCCESS_DELETED(206),
    FAIL(400),
    FAIL_IDENTIFIED_CLUSTERS(401),
    FAIL_MERGE_CLUSTERS(402),
    FAIL_VALIDATE_VALIDATION(403),
    FAIL_VALIDATE_CONSTRAINTS(404),
    UNKNOWN(999);

    private int statusCode;

    RecordStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static RecordStatus newStatus(int statusCode) {

        switch (statusCode) {
        case 000:
            return NEW;
        case 200:
            return SUCCESS;
        case 201:
            return SUCCESS_IDENTIFIED_CLUSTERS;
        case 202:
            return SUCCESS_MERGE_CLUSTERS;
        case 203:
            return SUCCESS_MERGE_CLUSTER_TO_RESOLVE;
        case 204:
            return SUCCESS_MERGED_RECORD;
        case 205:
            return SUCCESS_VALIDATE;
        case 206:
            return SUCCESS_DELETED;
        case 400:
            return FAIL;
        case 401:
            return FAIL_IDENTIFIED_CLUSTERS;
        case 402:
            return FAIL_MERGE_CLUSTERS;
        case 403:
            return FAIL_VALIDATE_VALIDATION;
        case 404:
            return FAIL_VALIDATE_CONSTRAINTS;
        default:
            return UNKNOWN;
        }

    }

}
