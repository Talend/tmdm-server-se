/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.query;

import java.util.List;

import com.amalto.core.query.user.Condition;
import com.amalto.core.query.user.TypedExpression;

import static com.amalto.core.query.user.UserQueryBuilder.eq;

class EqualsProcessor extends BasicConditionProcessor {

    static ConditionProcessor INSTANCE = new EqualsProcessor();

    private EqualsProcessor() {
    }

    @Override
    protected Condition buildCondition(TypedExpression expression, String value) {
        return eq(expression, value);
    }

    @Override
    protected Condition buildCondition(TypedExpression expression, TypedExpression value) {
        return eq(expression, value);
    }

    @Override
    protected String getConditionElement() {
        return "eq"; //$NON-NLS-1
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Condition buildCondition(TypedExpression expression, List value) {
        throw new UnsupportedOperationException();
    }
}
