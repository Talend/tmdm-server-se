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
import java.util.Arrays;
import java.util.Collection;

import org.talend.mdm.commmon.metadata.Types;

public class ShortConstant implements ConstantExpression<Short> {

    private final Short constant;

    private Collection<Short> constantCollection = new ArrayList();

    public ShortConstant(String constant) {
        if (constant.contains(UserQueryBuilder.IN_VALUE_SPLIT)) {
            Collection<String> stringCollection = Arrays.asList(constant.split(UserQueryBuilder.IN_VALUE_SPLIT));
            Collection<Short> resultCollection = new ArrayList();
            for (String value : stringCollection) {
                resultCollection.add(Short.parseShort(value));
            }
            this.constantCollection = resultCollection;
            this.constant = null;
        } else {
            this.constant = Short.parseShort(constant);
        }
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

    public Short getValue() {
        return constant;
    }

    public String getTypeName() {
        return Types.SHORT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShortConstant)) {
            return false;
        }
        ShortConstant that = (ShortConstant) o;
        return constant.equals(that.constant);
    }

    @Override
    public int hashCode() {
        return constant != null ? constant.hashCode() : constantCollection.isEmpty() ? 0 : constantCollection.hashCode();
    }

    @Override
    public Collection<Short> getValueList() {
        return constantCollection;
    }
}
