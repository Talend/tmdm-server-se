/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.Types;

public class StringConstant implements ConstantExpression<String> {

    private final String value;

    private List<String> valueList;

    public StringConstant(String value) {
        assert value != null;
        // For String input value, if it was wrapped with quotes, then we should remove it
        // As the value will be used as String and wrapped with quotes.
        if (StringUtils.startsWith(value, "&quot;") &&
            StringUtils.endsWith(value, "&quot;")) {
            value = StringUtils.substringBetween(value, "&quot;");
        }
        this.value = value;
        this.valueList = null;
    }

    public StringConstant(List<String> valueList) {
        assert valueList != null;
        this.valueList = valueList;
        this.value = null;
    }

    public String getValue() {
        if (isExpressionList()) {
            throw new IllegalStateException("The property of 'value' is not valid."); //$NON-NLS-1$
        }
        return value;
    }

    @Override
    public List<String> getValueList() {
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
        if (isExpressionList()) {
            return valueList.equals(that.valueList);
        } else {
            return value.equals(that.value);
        }
    }

    @Override
    public int hashCode() {
        if (isExpressionList()) {
            return valueList.hashCode();
        } else {
            return value.hashCode();
        }
    }

}
