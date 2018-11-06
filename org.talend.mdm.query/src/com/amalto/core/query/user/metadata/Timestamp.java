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
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.query.user.Visitor;
import com.amalto.core.storage.record.DataRecord;

public class Timestamp implements MetadataField {

    public static final Timestamp INSTANCE = new Timestamp();

    public static final String TIMESTAMP_ALIAS = "timestamp"; //$NON-NLS-1$

    private static final String[] TIMESTAMP_FIELD = new String[] { "../../t", "metadata:timestamp", "timestamp" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3

    private final Reader reader = new Reader() {

        @Override
        public Object readValue(DataRecord record) {
            return record.getRecordMetadata().getLastModificationTime();
        }
    };

    private Timestamp() {
    }

    public String getTypeName() {
        return Types.LONG;
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
        return TIMESTAMP_FIELD[1];
    }

    @Override
    public boolean matches(String path) {
        for (String possibleTaskId : TIMESTAMP_FIELD) {
            if (possibleTaskId.equals(path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return true; // Don't allow external users to change timestamp.
    }

    @Override
    public TypedExpression getConditionExpression() {
        return this;
    }

    @Override
    public TypedExpression getProjectionExpression() {
        return alias(UserQueryBuilder.timestamp(), TIMESTAMP_ALIAS);
    }

    @Override
    public Reader getReader() {
        return reader;
    }
}
