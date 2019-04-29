/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate.mapping;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.mapping.Column;
import org.hibernate.tool.hbm2ddl.ColumnMetadata;

import java.sql.Types;

@SuppressWarnings("nls")
public abstract class MDMTableUtils {

    public static final String NVARCHAR_MAX_TYPE = "nvarchar(max)";

    private static final String NO = "NO";

    public static boolean isAlterColumnField(Column newColumn, ColumnMetadata oldColumnInfo, Dialect dialect) {
        if (oldColumnInfo == null) {
            return Boolean.FALSE;
        }
        return isVarcharField(oldColumnInfo, dialect) && isIncreaseVarcharColumnLength(newColumn, oldColumnInfo, dialect)
                || isVarcharTypeChanged(newColumn, oldColumnInfo, dialect);
    }

    private static boolean isVarcharTypeChanged(Column newColumn, ColumnMetadata oldColumnInfo, Dialect dialect) {
        if(!isVarcharField(oldColumnInfo, dialect)){
            return false;
        }
        if (dialect instanceof PostgreSQLDialect) {
            return oldColumnInfo.getTypeName().toLowerCase().startsWith("varchar") && newColumn.getSqlType().equalsIgnoreCase("text");
        }
        if (dialect instanceof SQLServerDialect) {
            String newColumnType = newColumn.getSqlType();
            String oldColumnType = oldColumnInfo.getTypeName();

            // oldColumnType=nvarchar and the oldColumnInfo length=Integer.MAX_VALUE, oldColumnType changed to nvarchar(max)
            if (oldColumnType.equals("nvarchar") && oldColumnInfo.getColumnSize() == Integer.MAX_VALUE) {
                oldColumnType = NVARCHAR_MAX_TYPE;
            }

            // newColumnType=nvarchar(200), newColumnType changed to nvarchar
            if (!newColumnType.equals(NVARCHAR_MAX_TYPE) && newColumnType.contains("(")) {
                newColumnType = newColumnType.substring(0, newColumnType.indexOf('('));
            }
            // oldColumnType=nvarchar(200), oldColumnType changed to nvarchar
            if (!oldColumnType.equals(NVARCHAR_MAX_TYPE) && oldColumnType.contains("(")) {
                oldColumnType = oldColumnType.substring(0, oldColumnType.indexOf('('));
            }
            return !newColumnType.equals(oldColumnType);
        }

        return (oldColumnInfo.getTypeCode() == Types.VARCHAR || oldColumnInfo.getTypeCode() == Types.NVARCHAR) && (
                newColumn.getSqlTypeCode() == Types.LONGVARCHAR || newColumn.getSqlTypeCode() == Types.CLOB);
    }


    private static boolean isVarcharField(ColumnMetadata oldColumnInfo, Dialect dialect) {
        boolean isVarcharType = oldColumnInfo.getTypeCode() == Types.VARCHAR;
        if (dialect instanceof SQLServerDialect) {
            isVarcharType |= oldColumnInfo.getTypeCode() == Types.NVARCHAR
                    && oldColumnInfo.getTypeName().equalsIgnoreCase("nvarchar");
        }
        return isVarcharType;
    }

    private static boolean isIncreaseVarcharColumnLength(Column newColumn, ColumnMetadata oldColumnInfo, Dialect dialect) {
        if (dialect instanceof SQLServerDialect) {
            return newColumn.getLength() > oldColumnInfo.getColumnSize() && (oldColumnInfo.getTypeCode() == Types.NVARCHAR
                    || oldColumnInfo.getTypeCode() == Types.VARCHAR) && (newColumn.getSqlTypeCode() == Types.NVARCHAR
                    || newColumn.getSqlTypeCode() == Types.VARCHAR);
        }
        return newColumn.getLength() > oldColumnInfo.getColumnSize() && (newColumn.getSqlTypeCode() == oldColumnInfo
                .getTypeCode());
    }

    public static boolean isChangedToOptional(Column newColumn, ColumnMetadata oldColumnInfo) {
        return oldColumnInfo.getNullable().toUpperCase().equals(NO) && newColumn.isNullable();
    }
}
