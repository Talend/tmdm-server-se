/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import com.amalto.core.storage.datasource.RDBMSDataSource;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import java.util.List;

public class StatelessContext implements MappingCreatorContext {

    private final List<String> prefixes;

    private final RDBMSDataSource.DataSourceDialect dialect;

    public StatelessContext(List<String> prefixes, RDBMSDataSource.DataSourceDialect dialect) {
        this.prefixes = prefixes;
        this.dialect = dialect;
    }

    @Override
    public String getFieldColumn(FieldMetadata field) {
        StringBuilder buffer = new StringBuilder();
        for (String currentPrefix : prefixes) {
            buffer.append(currentPrefix).append('_');
        }
        String name = field.getName();
        // Note #1: Hibernate (starting from 4.0) internally sets a lower case letter as first letter if field starts with a
        // upper case character. To prevent any error due to missing field, lower case the field name.
        // Note #2: Prefix everything with "x_" so there won't be any conflict with database internal type names.
        // Note #3: Having '-' character is bad for Java code generation, so replace it with '_'.
        return "x_" + (buffer.toString().replace('-', '_') + name.replace('-', '_')).toLowerCase(); //$NON-NLS-1$
    }

    @Override
    public String getFieldColumn(String fieldName) {
        return "x_" + fieldName.replace('-', '_').toLowerCase(); //$NON-NLS-1$
    }

    @Override
    public int getTextLimit() {
        return dialect.getTextLimit();
    }
}
