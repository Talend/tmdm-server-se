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

import com.google.gson.JsonElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class BigDecimalConstant implements ConstantExpression<BigDecimal> {

    private final BigDecimal constant;

    private Collection<BigDecimal> constantCollection = new ArrayList();

    public BigDecimalConstant(String constant) {
        if(constant.contains(UserQueryBuilder.IN_VALUE_SPLIT)){
            Collection<String> stringCollection = Arrays.asList(constant.split(UserQueryBuilder.IN_VALUE_SPLIT));
            Collection<BigDecimal> resultCollection = new ArrayList();
            for(String tmp: stringCollection){
                resultCollection.add(new BigDecimal(tmp));
            }
            this.constantCollection = resultCollection;
            this.constant = null ;
        }else{
            this.constant = new BigDecimal(constant);
        }
    }

    public BigDecimalConstant(Collection<BigDecimal> constantCollection) {
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

    public Collection<BigDecimal> getValueList() {
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
        return constant.equals(that.constant);
    }

    @Override
    public int hashCode() {
        return constant == null ? constantCollection.hashCode() : constant.hashCode();
    }
}
