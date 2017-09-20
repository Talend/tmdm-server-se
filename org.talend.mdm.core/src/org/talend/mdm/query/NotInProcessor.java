/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import static com.amalto.core.query.user.UserQueryBuilder.notIn;

class NotInProcessor extends BasicConditionProcessor {

    static ConditionProcessor INSTANCE = new NotInProcessor();

    private NotInProcessor() {
    }

    @Override
    protected Condition buildCondition(TypedExpression expression, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Condition buildCondition(TypedExpression expression, TypedExpression value) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getConditionElement() {
        return "notIn"; //$NON-NLS-1
    }

    @Override
    protected Condition buildCondition(TypedExpression expression, List value) {
        return notIn(expression, value);
    }
}
