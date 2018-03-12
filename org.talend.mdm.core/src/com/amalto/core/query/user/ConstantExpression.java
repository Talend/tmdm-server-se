/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.query.user;

import java.util.List;

/**
 *
 */
public interface ConstantExpression<T extends Comparable> extends TypedExpression {

    public T getValue();

    public List<T> getValueList();

    public boolean isExpressionList();

    default Object getValueObject() {
        if (isExpressionList()) {
            return getValueList();
        } else {
            return getValue();
        }
    }
}
