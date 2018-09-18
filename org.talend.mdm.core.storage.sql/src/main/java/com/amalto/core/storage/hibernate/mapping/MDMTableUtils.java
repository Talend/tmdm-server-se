/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate.mapping;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.mapping.Column;
import org.hibernate.tool.hbm2ddl.ColumnMetadata;

@SuppressWarnings("nls")
public abstract class MDMTableUtils {

    public static final String NO = "NO";

    public static boolean isAlterColumnField(Column newColumn, ColumnMetadata oldColumnInfo, Dialect dialect) {
        if (oldColumnInfo == null) {
            return Boolean.FALSE;
        }
        boolean isNeedToAlter = Boolean.TRUE;
        isNeedToAlter &= isVarcharField(newColumn, oldColumnInfo, dialect)
                && isIncreaseVarcharColumnLength(newColumn, oldColumnInfo, dialect);
        return isNeedToAlter;
    }

    public static boolean isVarcharField(Column newColumn, ColumnMetadata oldColumnInfo, Dialect dialect) {
        boolean isVarcharType = oldColumnInfo.getTypeCode() == java.sql.Types.VARCHAR;
        if (dialect instanceof SQLServerDialect) {
            isVarcharType |= oldColumnInfo.getTypeCode() == java.sql.Types.NVARCHAR
                    && oldColumnInfo.getTypeName().equalsIgnoreCase("nvarchar");
        }
        return isVarcharType;
    }

    public static boolean isIncreaseVarcharColumnLength(Column newColumn, ColumnMetadata oldColumnInfo, Dialect dialect) {
        return newColumn.getLength() > oldColumnInfo.getColumnSize();
    }

    public static boolean isChangedToOptional(Column newColumn, ColumnMetadata oldColumnInfo) {
        if (oldColumnInfo.getNullable().toUpperCase().equals(NO) && newColumn.isNullable()) {
            return true;
        }
        return false;
    }
}
