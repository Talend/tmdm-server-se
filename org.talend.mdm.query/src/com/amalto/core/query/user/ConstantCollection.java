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

import java.util.Arrays;

/**
 *
 */
public class ConstantCollection implements TypedExpression {

    private final TypedExpression[] values;

    public ConstantCollection(TypedExpression... values) {
        if (values == null) {
            throw new IllegalArgumentException("Values can not be null.");
        }
        if (values.length == 0) {
            throw new IllegalArgumentException("Values can not be empty.");
        }
        this.values = values;
    }

    public Expression[] getValues() {
        return values;
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public Expression normalize() {
        return this;
    }

    @Override
    public boolean cache() {
        return false;
    }

    public String getTypeName() {
        return values[0].getTypeName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConstantCollection)) {
            return false;
        }
        ConstantCollection that = (ConstantCollection) o;
        return Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }
}
