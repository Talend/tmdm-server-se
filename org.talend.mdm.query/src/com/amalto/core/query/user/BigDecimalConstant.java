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

import org.talend.mdm.commmon.metadata.Types;

import java.math.BigDecimal;
import java.util.List;

public class BigDecimalConstant implements ConstantExpression<BigDecimal> {

    private final BigDecimal value;

    private List<BigDecimal> valueList;

    public BigDecimalConstant(String value) {
        assert value != null;
        this.value = new BigDecimal(value);
        this.valueList = null;
    }

    public BigDecimalConstant(List<BigDecimal> valueList) {
        assert valueList != null;
        this.value = null;
        this.valueList = valueList;
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

    public BigDecimal getValue() {
        if (isExpressionList()) {
            throw new IllegalStateException("The property of 'value' is not valid."); //$NON-NLS-1$
        }
        return value;
    }

    public List<BigDecimal> getValueList() {
        if (!isExpressionList()) {
            throw new IllegalStateException("The property of 'valueList' is not valid."); //$NON-NLS-1$
        }
        return valueList;
    }

    @Override public boolean isExpressionList() {
        return this.valueList != null;
    }

    public String getTypeName() {
        return Types.DECIMAL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BigDecimalConstant)) {
            return false;
        }
        BigDecimalConstant that = (BigDecimalConstant) o;
        if (this.isExpressionList()) {
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
