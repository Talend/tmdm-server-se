/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.query.user;

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.commmon.metadata.Types;

public class IntegerConstant implements ConstantExpression<Integer> {

    private final Integer value;

    private List<Integer> valueList;

    public IntegerConstant(String value) {
        assert value != null;
        this.value = Integer.parseInt(value);
        this.valueList = null;
    }

    public IntegerConstant(List<Integer> valueList) {
        assert valueList != null;
        this.valueList = valueList;
        this.value = null;
    }

    public Expression normalize() {
        return this;
    }

    @Override public boolean cache() {
        return false;
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public Integer getValue() {
        return value;
    }

    @Override public List<Integer> getValueList() {
        return valueList;
    }

    @Override public boolean isExpressionList() {
        return this.valueList != null;
    }

    public String getTypeName() {
        return Types.INTEGER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntegerConstant)) {
            return false;
        }
        IntegerConstant that = (IntegerConstant) o;
        if (this.isExpressionList()) {
            return valueList.equals(that.valueList);
        } else {
            return value.equals(that.value);
        }
    }

    @Override public int hashCode() {
        return value != null ? value : valueList.isEmpty() ? 0 : valueList.hashCode();
    }

}
