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

import static com.amalto.core.query.user.UserQueryBuilder.alias;

import org.talend.mdm.commmon.metadata.Types;

import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.query.user.UserStagingQueryBuilder;
import com.amalto.core.query.user.Visitor;

public class GroupSize implements MetadataField {

    public static final GroupSize INSTANCE = new GroupSize();

    public static final String GROUP_SIZE_ALIAS = "staging_group_size"; //$NON-NLS-1$

    private static final String[] GROUP_SIZE_FIELD = new String[] { "metadata:staging_group_size", "group_size" }; //$NON-NLS-1$ //$NON-NLS-2

    private GroupSize() {
    }

    public String getTypeName() {
        return Types.INT;
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
        return GROUP_SIZE_FIELD[0];
    }

    @Override
    public boolean matches(String path) {
        for (String possibleStatus : GROUP_SIZE_FIELD) {
            if (possibleStatus.equals(path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public TypedExpression getConditionExpression() {
        return this;
    }

    @Override
    public TypedExpression getProjectionExpression() {
        return alias(UserStagingQueryBuilder.groupSize(), GROUP_SIZE_ALIAS);
    }

    @Override
    public Reader getReader() {
        throw new UnsupportedOperationException();
    }
}
