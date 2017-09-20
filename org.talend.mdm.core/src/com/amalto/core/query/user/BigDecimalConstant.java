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

import org.talend.mdm.commmon.metadata.Types;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BigDecimalConstant implements ConstantExpression<BigDecimal> {

    private final BigDecimal constant;

    private List<BigDecimal> constantCollection = new ArrayList();

    public BigDecimalConstant(String constant) {
        this.constant = new BigDecimal(constant);
    }

    public BigDecimalConstant(List<BigDecimal> constantCollection) {
        this.constant = null;
        this.constantCollection = constantCollection;
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

    public List<BigDecimal> getValueList() {
        return constantCollection;
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
        if (constant != null && constantCollection.isEmpty()) {
            return constant.equals(that.constant);
        } else {
            return constantCollection.equals(that.constantCollection);
        }
    }

    @Override
    public int hashCode() {
        return constant == null ? constantCollection.hashCode() : constant.hashCode();
    }
}
