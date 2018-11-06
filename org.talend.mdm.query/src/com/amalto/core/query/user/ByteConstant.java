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

import java.util.List;

import org.talend.mdm.commmon.metadata.Types;

public class ByteConstant implements ConstantExpression<Byte> {

    private final Byte value;

    private List<Byte> valueList;

    public ByteConstant(String value) {
        assert value != null;
        this.value = Byte.parseByte(value);
        this.valueList = null;
    }

    public ByteConstant(List<Byte> valueList) {
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

    public Byte getValue() {
        if (isExpressionList()) {
            throw new IllegalStateException("The property of 'value' is not valid."); //$NON-NLS-1$
        }
        return value;
    }

    @Override
    public List<Byte> getValueList() {
        if (!isExpressionList()) {
            throw new IllegalStateException("The property of 'valueList' is not valid."); //$NON-NLS-1$
        }
        return valueList;
    }

    @Override
    public boolean isExpressionList() {
        return this.valueList != null;
    }

    public String getTypeName() {
        return Types.BYTE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ByteConstant)) {
            return false;
        }
        ByteConstant that = (ByteConstant) o;
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
            return this.value.hashCode();
        }
    }

}
