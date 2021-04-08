/*
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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
import com.amalto.core.storage.record.StorageConstants;
import org.talend.mdm.commmon.metadata.Types;

import static com.amalto.core.query.user.UserQueryBuilder.alias;

@SuppressWarnings("nls")
public class StagingOldGroup implements MetadataField {

    public static final StagingOldGroup INSTANCE = new StagingOldGroup();

    public static final String STAGING_OLD_GROUP_ALIAS = "staging_oldgroup"; //$NON-NLS-1$

    private static final String[] STAGING_OLD_GROUP_FIELD = new String[] { "$staging_oldgroup$", "metadata:staging_oldgroup",
            "staging_oldgroup" };

    private final PropertyReader propertyReader = new PropertyReader(StorageConstants.METADATA_STAGING_OLD_GROUP);

    private StagingOldGroup() {
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
        return STAGING_OLD_GROUP_FIELD[1];
    }

    @Override
    public boolean matches(String path) {
        for (String possibleStatus : STAGING_OLD_GROUP_FIELD) {
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
        return alias(UserStagingQueryBuilder.hasTask(), STAGING_OLD_GROUP_ALIAS);
    }

    @Override
    public Reader getReader() {
        return propertyReader;
    }
}
