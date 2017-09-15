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

/**
 *
 */
public class StringConstant implements ConstantExpression<String> {

    private final String value;

    private Collection<String> constantCollection = new ArrayList();

    public StringConstant(String value) {
        if (value.contains(UserQueryBuilder.IN_VALUE_SPLIT)) {
            Collection<String> stringCollection = Arrays.asList(value.split(UserQueryBuilder.IN_VALUE_SPLIT));
            Collection<String> resultCollection = new ArrayList();
            for (String tmp : stringCollection) {
                resultCollection.add(tmp);
            }
            this.constantCollection = resultCollection;
            this.value = null;
        } else {
            this.value = value;
        }
    }

    public String getValue() {
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
        return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : constantCollection.isEmpty() ? 0 : constantCollection.hashCode();
    }

    @Override
    public Collection<String> getValueList() {
        // TODO Auto-generated method stub
        return constantCollection;
    }
}
