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

@SuppressWarnings("nls")
public class StagingHasTask implements MetadataField {

    public static final StagingHasTask INSTANCE = new StagingHasTask();

    public static final String STAGING_HAS_TASK_ALIAS = "staging_hastask"; //$NON-NLS-1$

    private static final String[] STAGING_HAS_TASK_FIELD = new String[] { "$staging_hastask$", "metadata:staging_hastask", "staging_hastask" };

    private final PropertyReader propertyReader = new PropertyReader(CommonStorage.METADATA_STAGING_HAS_TASK);

    private StagingHasTask() {
    }

    public String getTypeName() {
        return Types.BOOLEAN;
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
        return STAGING_HAS_TASK_FIELD[1];
    }

    @Override
    public boolean matches(String path) {
        for (String possibleStatus : STAGING_HAS_TASK_FIELD) {
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
        return alias(UserStagingQueryBuilder.hasTask(), STAGING_HAS_TASK_ALIAS);
    }

    @Override
    public Reader getReader() {
        return propertyReader;
    }

}
