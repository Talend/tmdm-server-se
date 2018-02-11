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


import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

public class Isa implements Condition {

    private final TypedExpression expression;

    private final ComplexTypeMetadata type;

    public Isa(TypedExpression expression, ComplexTypeMetadata type) {
        this.expression = expression;
        this.type = type;
    }

    public TypedExpression getExpression() {
        return expression;
    }

    public ComplexTypeMetadata getType() {
        return type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Isa)) {
            return false;
        }
        Isa isa = (Isa) o;
        if (expression != null ? !expression.equals(isa.expression) : isa.expression != null) {
            return false;
        }
        if (type != null ? !type.equals(isa.type) : isa.type != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = expression != null ? expression.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
