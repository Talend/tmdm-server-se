/*
 * Copyright (C) 2006-2011 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.query.user;

import com.amalto.core.metadata.FieldMetadata;
import com.amalto.core.metadata.TypeMetadata;

/**
 *
 */
public class Field implements TypedExpression {

    private final FieldMetadata fieldMetadata;

    public Field(FieldMetadata fieldMetadata) {
        this.fieldMetadata = fieldMetadata;
    }

    public FieldMetadata getFieldMetadata() {
        return fieldMetadata;
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public Expression normalize() {
        return this;
    }

    public String getTypeName() {
        TypeMetadata type = fieldMetadata.getType();
        // Move up the inheritance tree to find the "most generic" type (used when simple types inherits from XSD types,
        // in this case, the XSD type is interesting, not the custom one).
        while (!type.getSuperTypes().isEmpty()) {
            type = type.getSuperTypes().iterator().next();
        }
        return type.getName();
    }
}
