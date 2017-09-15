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

import java.util.Collection;

/**
 *
 */
public interface ConstantExpression<T extends Comparable> extends TypedExpression {

    public T getValue();

    public Collection<T> getValueList();
    
    default String getStringValue(){
        if (getValue() != null) {
            return String.valueOf(getValue());
        } else {
            StringBuilder sb = new StringBuilder();
            for (T value : getValueList()) {
                sb.append(value);
                sb.append(UserQueryBuilder.IN_VALUE_SPLIT);
            }
            return sb.toString().substring(0, sb.toString().length() - UserQueryBuilder.IN_VALUE_SPLIT.length());
        }
    }

    default Object getConstant(){
        if (getValue() != null && getValueList().isEmpty()) {
            return getValue();
        } else {
            return getValueList();
        }
    }
}
