/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import org.hibernate.dialect.SQLServer2008Dialect;
import org.hibernate.type.StandardBasicTypes;

import java.sql.Types;

public class SQLServerCustomDialect extends SQLServer2008Dialect {
    /**
     * CLOBs and VARCHAR in MDM usually expects value to be stored to UTF-8, this dialect
     * ensures they're stored in a UTF-8 friendly type.
     */
    public SQLServerCustomDialect() {
        registerColumnType(Types.LONGVARCHAR, "nvarchar(max)"); //$NON-NLS-1$
        registerColumnType(Types.LONGNVARCHAR, "nvarchar(max)"); //$NON-NLS-1$
        registerColumnType(Types.CLOB, "nvarchar(max)"); //$NON-NLS-1$
        registerColumnType(Types.VARCHAR, 4000, "nvarchar($l)"); //$NON-NLS-1$
        registerColumnType(Types.CHAR, "nchar(1)"); //$NON-NLS-1$
        registerColumnType(Types.NVARCHAR, 4000, "nvarchar($l)"); //$NON-NLS-1$
        registerColumnType(Types.NVARCHAR, "nvarchar(max)"); //$NON-NLS-1$
        
        registerHibernateType(Types.NVARCHAR, StandardBasicTypes.STRING.getName());
        registerHibernateType(Types.LONGNVARCHAR, StandardBasicTypes.TEXT.getName());
    }
}
