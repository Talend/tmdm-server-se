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

package com.amalto.core.query.user;

public class Min implements TypedExpression {

    private final TypedExpression expression;

    public Min(TypedExpression expression) {
        this.expression = expression;
    }

    public TypedExpression getExpression() {
        return expression;
    }

    @Override
    public Expression normalize() {
        return this;
    }

    @Override
    public boolean cache() {
        return false;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getTypeName() {
        return expression.getTypeName();
    }
}
