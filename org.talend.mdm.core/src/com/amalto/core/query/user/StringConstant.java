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

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.commmon.metadata.Types;

/**
 *
 */
public class StringConstant implements ConstantExpression<String> {

    private final String value;

    private List<String> valueList;

    public StringConstant(String value) {
        assert value != null;
        this.value = value;
        this.valueList = null;
    }

    public StringConstant(List<String> valueList) {
        assert valueList != null;
        this.valueList = valueList;
        this.value = null;
    }

    public String getValue() {
        return value;
    }

    @Override
    public List<String> getValueList() {
        return valueList;
    }

    @Override public boolean isExpressionList() {
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
        return Types.STRING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StringConstant)) {
            return false;
        }
        StringConstant that = (StringConstant) o;
        if (value != null && valueList.isEmpty()) {
            return !(value != null ? !value.equals(that.value) : that.value != null);
        } else {
            return valueList.equals(that.valueList);
        }
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : valueList.isEmpty() ? 0 : valueList.hashCode();
    }


}
