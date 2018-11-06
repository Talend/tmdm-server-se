/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.query.user.metadata;

import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.query.user.UserStagingQueryBuilder;
import com.amalto.core.query.user.Visitor;
import com.amalto.core.storage.record.CommonStorage;
import org.talend.mdm.commmon.metadata.Types;

import static com.amalto.core.query.user.UserQueryBuilder.alias;

public class StagingError implements MetadataField {

    public static final StagingError INSTANCE = new StagingError();

    public static final String STAGING_ERROR_ALIAS = "staging_error"; //$NON-NLS-1$

    private static final String[] STAGING_ERROR_FIELD = new String[] { "$staging_error$", "metadata:staging_error", "staging_error" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3

    private final PropertyReader propertyReader = new PropertyReader(CommonStorage.METADATA_STAGING_ERROR);

    private StagingError() {
    }

    public String getTypeName() {
        return Types.STRING;
    }

    public Expression normalize() {
        return this;
    }

    @Override
    public boolean cache() {
        return false;
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getFieldName() {
        return STAGING_ERROR_FIELD[1];
    }

    @Override
    public boolean matches(String path) {
        for (String possibleError : STAGING_ERROR_FIELD) {
            if (possibleError.equals(path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public TypedExpression getConditionExpression() {
        return this;
    }

    @Override
    public TypedExpression getProjectionExpression() {
        return alias(UserStagingQueryBuilder.error(), STAGING_ERROR_ALIAS);
    }

    @Override
    public Reader getReader() {
        return propertyReader;
    }
}
