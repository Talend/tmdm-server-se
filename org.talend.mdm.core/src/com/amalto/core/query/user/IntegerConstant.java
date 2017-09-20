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

    private final Integer constant;

    private List<Integer> constantCollection = new ArrayList();

    public IntegerConstant(String constant) {
        this.constant = Integer.parseInt(constant);
    }

    public IntegerConstant(List<Integer> constant) {
        this.constantCollection = constant;
        this.constant = null;
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

    public Integer getValue() {
        return constant;
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
        if (constant != null && constantCollection.isEmpty()) {
            return constant == that.constant;
        } else {
            return constantCollection.equals(that.constantCollection);
        }
    }

    @Override
    public int hashCode() {
        return constant != null ? constant : constantCollection.isEmpty() ? 0: constantCollection.hashCode();
    }

    @Override
    public List<Integer> getValueList() {
        return constantCollection;
    }
}
