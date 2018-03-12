/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage;

import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource.DataSourceDialect;

public abstract class HibernateStorageUtils {

    public static String convertedDefaultValue(DataSourceDialect dialect, String defaultValueRule, String replexStr) {
        if (defaultValueRule == null) {
            return null;
        }
        
        String covertValue = defaultValueRule;
        if (defaultValueRule.equalsIgnoreCase(MetadataRepository.FN_FALSE)) {
            if (dialect == RDBMSDataSource.DataSourceDialect.SQL_SERVER
                    || dialect == RDBMSDataSource.DataSourceDialect.ORACLE_10G) {
                covertValue = "0"; //$NON-NLS-1$
            } else {
                covertValue = Boolean.FALSE.toString();
            }
        } else if (defaultValueRule.equalsIgnoreCase(MetadataRepository.FN_TRUE)) {
            if (dialect == RDBMSDataSource.DataSourceDialect.SQL_SERVER
                    || dialect == RDBMSDataSource.DataSourceDialect.ORACLE_10G) {
                covertValue = "1"; //$NON-NLS-1$
            } else {
                covertValue = Boolean.TRUE.toString();
            }
        } else if (defaultValueRule.startsWith("\"") && defaultValueRule.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
            covertValue = defaultValueRule.replace("\"", replexStr); //$NON-NLS-1$
        }
        return covertValue;
    }
}
