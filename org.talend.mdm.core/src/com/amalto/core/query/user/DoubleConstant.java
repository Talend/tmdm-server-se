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

public class DoubleConstant implements ConstantExpression<Double> {

    private final Double constant;

    private Collection<Double> constantCollection = new ArrayList();

    public DoubleConstant(String constant) {
        if (constant.contains(UserQueryBuilder.IN_VALUE_SPLIT)) {
            Collection<String> stringCollection = Arrays.asList(constant.split(UserQueryBuilder.IN_VALUE_SPLIT));
            Collection<Double> resultCollection = new ArrayList();
            for (String tmp : stringCollection) {
                resultCollection.add(Double.parseDouble(tmp));
            }
            this.constantCollection = resultCollection;
            this.constant = null;
        } else {
            this.constant = Double.parseDouble(constant);
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

    public Double getValue() {
        return constant;
    }

    public String getTypeName() {
        return Types.DOUBLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DoubleConstant)) {
            return false;
        }
        DoubleConstant that = (DoubleConstant) o;
        return !(constant != null ? !constant.equals(that.constant) : that.constant != null);
    }

    @Override
    public int hashCode() {
        return constant != null ? constant.hashCode() : constantCollection != null ? constantCollection.hashCode(): 0;
    }

    @Override
    public String getStringValue() {
        if (constant != null) {
            return String.valueOf(constant);
        } else {
            StringBuilder sb = new StringBuilder();
            for (Double value : constantCollection) {
                sb.append(value);
                sb.append(UserQueryBuilder.IN_VALUE_SPLIT);
            }
            return sb.toString().substring(0, sb.toString().length() - UserQueryBuilder.IN_VALUE_SPLIT.length());
        }
    }

    @Override
    public Collection<Double> getValueList() {
        return constantCollection;
    }

}
