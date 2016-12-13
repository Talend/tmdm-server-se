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

public class IntegerConstant implements ConstantExpression<Integer> {

    private final int constant;

    public IntegerConstant(int constant) {
        this.constant = constant;
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
        return constant == that.constant;
    }

    @Override
    public int hashCode() {
        return constant;
    }
}
