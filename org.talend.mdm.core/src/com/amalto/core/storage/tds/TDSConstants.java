// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.storage.tds;

/**
 * Utility class for TDS constants.
 */
@SuppressWarnings("nls")
public class TDSConstants {

    public static final String SCHEMAS_NAMESPACE = "org.talend.schema";

    public static final String AUTHORIZATION = "Authorization";

    public static final String ACCEPT = "Accept";

    public static final String APPLICATION_JSON = "application/json";

    public static final String CLIENT_APP = "STUDIO";

    public static final String TASK_TYPE_MERGING = "MERGING";

    public static final String TASK_STATE_NEW = "New";

    public static final String TASK_STATE_RESOLVED = "Resolved";

    public static final String STEWARD_ROLE = "Steward";

    public static final String ACCESS_RIGHT_READ_ONLY = "READ_ONLY";

    public static final long MS_PER_DAY = 1000 * 3600 * 24;

    public static final int TASK_BATCH_SIZE = 50;

    private TDSConstants() {
        throw new IllegalAccessError("Utility class");
    }
}
