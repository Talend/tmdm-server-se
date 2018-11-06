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

public class StagingBlockKey implements MetadataField {

    public static final StagingBlockKey INSTANCE = new StagingBlockKey();

    public static final String STAGING_BLOCK_ALIAS = "staging_blockkey"; //$NON-NLS-1$

    private static final String[] STAGING_BLOCK_FIELD = new String[] { "metadata:staging_blockkey", "staging_blockkey" }; //$NON-NLS-1$ //$NON-NLS-2

    private final PropertyReader propertyReader = new PropertyReader(CommonStorage.METADATA_STAGING_BLOCK_KEY);

    private StagingBlockKey() {
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
        return STAGING_BLOCK_FIELD[0];
    }

    @Override
    public boolean matches(String path) {
        for (String possibleStatus : STAGING_BLOCK_FIELD) {
            if (possibleStatus.equals(path)) {
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
        return alias(UserStagingQueryBuilder.status(), STAGING_BLOCK_ALIAS);
    }

    @Override
    public Reader getReader() {
        return propertyReader;
    }

}
