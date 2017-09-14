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
import java.util.Collection;

import org.talend.mdm.commmon.metadata.Types;

/**
 *
 */
public class BooleanConstant implements ConstantExpression<Boolean> {

    private final boolean value;

    private Collection<Boolean> valueCollection = new ArrayList();

    public BooleanConstant(boolean value) {
        this.value = value;
    }

    public Boolean getValue() {
        return value;
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
        return Types.BOOLEAN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BooleanConstant)) {
            return false;
        }
        BooleanConstant that = (BooleanConstant) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return (value ? 1 : 0);
    }

    @Override
    public String getStringValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Boolean> getValueList() {
        // TODO Auto-generated method stub
        return null;
    }

}
