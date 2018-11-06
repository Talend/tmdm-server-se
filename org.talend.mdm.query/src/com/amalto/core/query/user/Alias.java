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

public class Alias implements TypedExpression {

    private final TypedExpression typedExpression;

    private final String alias;

    public Alias(TypedExpression typedExpression, String alias) {
        this.typedExpression = typedExpression;
        this.alias = alias;
    }

    public String getAliasName() {
        return alias;
    }

    public TypedExpression getTypedExpression() {
        return typedExpression;
    }

    public String getTypeName() {
        return typedExpression.getTypeName();
    }

    public Expression normalize() {
        if (typedExpression instanceof Alias) {
            return new Alias(((Alias) typedExpression).getTypedExpression(), alias);
        }
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
        if (!(o instanceof Alias)) {
            return false;
        }
        Alias alias1 = (Alias) o;
        if (alias != null ? !alias.equals(alias1.alias) : alias1.alias != null) {
            return false;
        }
        if (typedExpression != null ? !typedExpression.equals(alias1.typedExpression) : alias1.typedExpression != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = typedExpression != null ? typedExpression.hashCode() : 0;
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        return result;
    }
}
