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

import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.mapping.Column;
import org.hibernate.tool.hbm2ddl.ColumnMetadata;

import java.sql.Types;

@SuppressWarnings("nls")
public abstract class MDMTableUtils {

    public static final String NO = "NO";

    public static boolean isAlterColumnField(Column newColumn, ColumnMetadata oldColumnInfo, Dialect dialect) {
        if (oldColumnInfo == null) {
            return Boolean.FALSE;
        }
        return isVarcharField(oldColumnInfo, dialect) && isIncreaseVarcharColumnLength(newColumn, oldColumnInfo)
                || isStringTypeChanged(newColumn, oldColumnInfo, dialect);
    }

    public static boolean isStringTypeChanged(Column newColumn, ColumnMetadata oldColumnInfo, Dialect dialect) {
        if (dialect instanceof DB2Dialect) {
            if (oldColumnInfo.getTypeCode() == Types.VARCHAR && newColumn.getSqlTypeCode() == Types.CLOB) {
                return false;
            }
        }
        return oldColumnInfo.getTypeCode() == Types.VARCHAR && newColumn.getSqlTypeCode() == Types.LONGVARCHAR;
    }


    public static boolean isVarcharField(ColumnMetadata oldColumnInfo, Dialect dialect) {
        boolean isVarcharType = oldColumnInfo.getTypeCode() == Types.VARCHAR;
        if (dialect instanceof SQLServerDialect) {
            isVarcharType |= oldColumnInfo.getTypeCode() == Types.NVARCHAR
                    && oldColumnInfo.getTypeName().equalsIgnoreCase("nvarchar");
        }
        return isVarcharType;
    }

    public static boolean isIncreaseVarcharColumnLength(Column newColumn, ColumnMetadata oldColumnInfo) {
        return newColumn.getLength() > oldColumnInfo.getColumnSize() && (newColumn.getSqlTypeCode() == oldColumnInfo.getTypeCode());
    }

    public static boolean isChangedToOptional(Column newColumn, ColumnMetadata oldColumnInfo) {
        return oldColumnInfo.getNullable().toUpperCase().equals(NO) && newColumn.isNullable();
    }
}
