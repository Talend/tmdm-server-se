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

public class Distinct implements TypedExpression {

    private final TypedExpression expression;

    public Distinct(TypedExpression expression) {
        this.expression = expression;
    }

    public TypedExpression getExpression() {
        return expression;
    }

    public String getTypeName() {
        return expression.getTypeName();
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Distinct)) {
            return false;
        }
        Distinct type = (Distinct) o;
        return !(expression != null ? !expression.equals(type.expression) : type.expression != null);
    }

    @Override
    public int hashCode() {
        return expression != null ? expression.hashCode() : 0;
    }
}
