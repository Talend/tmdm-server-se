/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import org.hibernate.dialect.Oracle10gDialect;

import java.sql.Types;

public class OracleCustomDialect extends Oracle10gDialect {
    /**
     * Hibernate 3.5.6 incorrectly maps Double JDBC type. This custom implementation fixes this.
     */
    protected void registerNumericTypeMappings() {
        super.registerNumericTypeMappings();
        registerColumnType(Types.DOUBLE, "float"); //$NON-NLS-1$
    }
}