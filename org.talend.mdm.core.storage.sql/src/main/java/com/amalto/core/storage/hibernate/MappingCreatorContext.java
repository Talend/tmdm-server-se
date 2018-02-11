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

import org.talend.mdm.commmon.metadata.FieldMetadata;

/**
 * A context interface used by {@link FlatTypeMappingCreator} or
 * {@link com.amalto.core.storage.hibernate.ScatteredMappingCreator} instances to get database column names.
 */
public interface MappingCreatorContext {
    /**
     * @param field A field from a type.
     * @return The column name for the <code>field</code>.
     */
    String getFieldColumn(FieldMetadata field);

    /**
     * @param fieldName A field name (as returned by {@link org.talend.mdm.commmon.metadata.FieldMetadata#getName()}).
     * @return The column name for the <code>fieldName</code>.
     */
    String getFieldColumn(String fieldName);

    /**
     * @return A positive integer that indicates a threshold for using clob/text field on field max length.
     * @see com.amalto.core.storage.datasource.RDBMSDataSource.DataSourceDialect#getTextLimit()
     */
    int getTextLimit();
}
