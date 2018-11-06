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

public class ComplexTypeExpression implements TypedExpression {

    private final ComplexTypeMetadata type;

    public ComplexTypeExpression(ComplexTypeMetadata type) {
        this.type = type;
    }

    public ComplexTypeMetadata getType() {
        return type;
    }

    public String getTypeName() {
        return type.getName();
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
        if (!(o instanceof ComplexTypeExpression)) {
            return false;
        }
        ComplexTypeExpression that = (ComplexTypeExpression) o;
        return !(type != null ? !type.equals(that.type) : that.type != null);
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
