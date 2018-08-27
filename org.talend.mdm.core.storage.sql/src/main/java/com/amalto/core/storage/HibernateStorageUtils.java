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

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource.DataSourceDialect;
import com.amalto.core.storage.hibernate.TypeMapping;

@SuppressWarnings({ "nls"})
public abstract class HibernateStorageUtils {

    private static final String FALSE = Boolean.toString(false);

    private static final String TRUE = Boolean.toString(true);

    public static String convertedDefaultValue(String fieldType, DataSourceDialect dialect, String defaultValueRule,
            String replexStr) {
        if (defaultValueRule == null) {
            return null;
        }

        String covertValue = defaultValueRule;
        if (isBooleanDefaultValue(fieldType, defaultValueRule)) {
            if (defaultValueRule.equalsIgnoreCase(MetadataRepository.FN_FALSE) || defaultValueRule.contains(FALSE)) {
                if (isSQLServer(dialect) || isOracle(dialect) || isMySQL(dialect) || isDB2(dialect)) {
                    covertValue = "0";
                } else {
                    covertValue = Boolean.FALSE.toString();
                }
            } else if (defaultValueRule.equalsIgnoreCase(MetadataRepository.FN_TRUE) || defaultValueRule.contains(TRUE)) {
                if (isSQLServer(dialect) || isOracle(dialect) || isMySQL(dialect) || isDB2(dialect)) {
                    covertValue = "1";
                } else {
                    covertValue = Boolean.TRUE.toString();
                }
            }
        } else if (defaultValueRule.startsWith("\"") && defaultValueRule.endsWith("\"")) {
            covertValue = defaultValueRule.replace("\"", replexStr);
        }
        return covertValue;
    }

    public static boolean isBooleanDefaultValue(String fieldType, String defaultValue) {
        if (!fieldType.equals(TypeMapping.SQL_TYPE_BOOLEAN)) {
            return false;
        }

        return isBooleanDefaultValue(defaultValue);
    }

    public static boolean isBooleanDefaultValue(String defaultValue) {
        if (defaultValue.equalsIgnoreCase(MetadataRepository.FN_FALSE)
                || defaultValue.equalsIgnoreCase(MetadataRepository.FN_TRUE)) {
            return true;
        }

        String[] searchStrings = new String[] { "\"", "'" };
        if (StringUtils.startsWithAny(defaultValue, searchStrings) && StringUtils.endsWithAny(defaultValue, searchStrings)) {
            String lowerCaseDefaultValue = defaultValue.replaceAll("\"|'", "").toLowerCase();
            if (lowerCaseDefaultValue.equals(TRUE) || lowerCaseDefaultValue.equals(FALSE)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isOracle(DataSourceDialect dialect) {
        return dialect == RDBMSDataSource.DataSourceDialect.ORACLE_10G;
    }

    public static boolean isSQLServer(DataSourceDialect dialect) {
        return dialect == RDBMSDataSource.DataSourceDialect.SQL_SERVER;
    }

    public static boolean isMySQL(DataSourceDialect dialect) {
        return dialect == RDBMSDataSource.DataSourceDialect.MYSQL;
    }

    public static boolean isH2(DataSourceDialect dialect) {
        return dialect == RDBMSDataSource.DataSourceDialect.H2;
    }

    public static boolean isPostgres(DataSourceDialect dialect) {
        return dialect == RDBMSDataSource.DataSourceDialect.POSTGRES;
    }

    public static boolean isDB2(DataSourceDialect dialect) {
        return dialect == RDBMSDataSource.DataSourceDialect.DB2;
    }
}
