/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

public class BigDecimalConstant implements ConstantExpression<BigDecimal> {

    private final BigDecimal constant;

    public BigDecimalConstant(String constant) {
        this.constant = new BigDecimal(constant);
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
        return constant;
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
        return constant.equals(that.constant);
    }

    @Override
    public int hashCode() {
        return constant.hashCode();
    }
}
