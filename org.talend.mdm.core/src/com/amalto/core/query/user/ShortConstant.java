/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.query.user;

import java.util.List;

import org.talend.mdm.commmon.metadata.Types;

public class ShortConstant implements ConstantExpression<Short> {

    private final Short value;

    private List<Short> valueList;

    public ShortConstant(String value) {
        assert value != null;
        this.value = Short.parseShort(value);
        this.valueList = null;
    }

    public ShortConstant(List<Short> valueList) {
        assert valueList != null;
        this.valueList = valueList;
        this.value = null;
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

    public Short getValue() {
        return value;
    }

    @Override
    public List<Short> getValueList() {
        return valueList;
    }

    @Override public boolean isExpressionList() {
        return this.valueList != null;
    }

    public String getTypeName() {
        return Types.SHORT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShortConstant)) {
            return false;
        }
        ShortConstant that = (ShortConstant) o;
        if (this.isExpressionList()) {
            return valueList.equals(that.valueList);
        } else {
            return value.equals(that.value);
        }
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : valueList.isEmpty() ? 0 : valueList.hashCode();
    }

}
