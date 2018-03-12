/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.query.user;

import java.util.List;

import org.talend.mdm.commmon.metadata.Types;

public class BooleanConstant implements ConstantExpression<Boolean> {

    private final Boolean value;

    private List<Boolean> valueList;

    public BooleanConstant(String value) {
        assert value != null;
        this.value = Boolean.valueOf(value);
        this.valueList = null;
    }

    public BooleanConstant(List<Boolean> value) {
        assert value != null;
        this.valueList = value;
        this.value = null;
    }

    public Boolean getValue() {
        if (isExpressionList()) {
            throw new IllegalStateException("The property of 'value' is not valid."); //$NON-NLS-1$
        }
        return value;
    }

    @Override
    public List<Boolean> getValueList() {
        if (!isExpressionList()) {
            throw new IllegalStateException("The property of 'valueList' is not valid."); //$NON-NLS-1$
        }
        return valueList;
    }

    @Override
    public boolean isExpressionList() {
        return this.valueList != null;
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
        return Types.BOOLEAN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BooleanConstant)) {
            return false;
        }
        BooleanConstant that = (BooleanConstant) o;
        if (isExpressionList()) {
            return valueList.equals(that.valueList);
        } else {
            return value.equals(that.value);
        }
    }

    @Override
    public int hashCode() {
        if (isExpressionList()) {
            return this.valueList.hashCode();
        } else {
            return value.hashCode();
        }
    }
}
