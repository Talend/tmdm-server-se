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

public class TaskId implements MetadataField {

    public static final TaskId INSTANCE = new TaskId();

    public static final String TASK_ID_ALIAS = "taskId"; //$NON-NLS-1$

    private static final String[] TASK_ID_FIELD = new String[] { "../../taskId", "metadata:task_id", "task_id" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3

    private final Reader reader = new Reader() {

        @Override
        public Object readValue(DataRecord record) {
            return record.getRecordMetadata().getTaskId();
        }
    };

    private TaskId() {
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
        return TASK_ID_FIELD[1];
    }

    @Override
    public boolean matches(String path) {
        for (String possibleTaskId : TASK_ID_FIELD) {
            if (possibleTaskId.equals(path)) {
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
        return alias(UserQueryBuilder.taskId(), TASK_ID_ALIAS);
    }

    @Override
    public Reader getReader() {
        return reader;
    }
}
