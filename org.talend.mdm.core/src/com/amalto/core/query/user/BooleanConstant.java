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

import com.amalto.core.util.Util;

/**
 *
 */
public class BooleanConstant implements ConstantExpression<Boolean> {

    private final Boolean value;

    private List<Boolean> valueCollection = new ArrayList();

    public BooleanConstant(String value) {
        this.value = Boolean.valueOf(value);
    }

    public BooleanConstant(List<Boolean> value) {
        this.valueCollection = value;
        this.value = null;
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
        if (value != null && valueCollection.isEmpty()) {
            return value == that.value;
        } else {
            return valueCollection.equals(that.valueCollection);
        }
    }

    @Override
    public int hashCode() {
        return value != null ? 1 : valueCollection.isEmpty() ? 0: valueCollection.hashCode();
    }

    @Override
    public List<Boolean> getValueList() {
        return valueCollection;
    }

}
