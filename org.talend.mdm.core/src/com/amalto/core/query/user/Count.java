/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.query.user;

import org.talend.mdm.commmon.metadata.Types;

/**
 *
 */
public class Count implements TypedExpression {

    private final TypedExpression expression;

    public Count() {
        expression = null;
    }

    public Count(TypedExpression expression) {
        this.expression = expression;
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

    public String getTypeName() {
        return Types.LONG;
    }

    /**
     * @return The {@link com.amalto.core.query.user.TypedExpression expression} to be counted or <code>null</code>
     * if query request an instance count.
     */
    public TypedExpression getExpression() {
        return expression;
    }
}
