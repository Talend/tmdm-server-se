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


import java.util.Collections;
import java.util.List;

import org.talend.mdm.commmon.metadata.CompoundFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;

public class Field implements TypedExpression {

    private final FieldMetadata fieldMetadata;

    private List<FieldMetadata> path = Collections.emptyList();

    public Field(FieldMetadata fieldMetadata) {
        if (fieldMetadata == null) {
            throw new IllegalArgumentException("Field can not be null.");
        }
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

    @Override
    public boolean cache() {
        return false;
    }

    public String getTypeName() {
        TypeMetadata fieldType;
        if (fieldMetadata instanceof ReferenceFieldMetadata) {
            fieldType = ((ReferenceFieldMetadata) fieldMetadata).getReferencedField().getType();
        } else {
            fieldType = fieldMetadata.getType();
        }
        TypeMetadata type = MetadataUtils.getSuperConcreteType(fieldType);
        return type.getName();
    }

    public void setPath(List<FieldMetadata> path) {
        this.path = path;
    }

    public List<FieldMetadata> getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Field)) {
            return false;
        }
        Field field = (Field) o;
        if (field.fieldMetadata instanceof ReferenceFieldMetadata) {
            if (fieldMetadata.getContainingType() != null && field.fieldMetadata.getContainingType() != null) {
                if (!fieldMetadata.getContainingType().toString().equals(field.fieldMetadata.getContainingType().toString())) {
                    return false;
                }
            }
        }
        if (field.getPath() != null && this.getPath() != null && field.getPath().size() > 0) {
            for (FieldMetadata fm : field.getPath()) {
                if (!getPath().contains(fm)) {
                    return false;
                }
            }
        }
        return fieldMetadata.getName().equals(field.fieldMetadata.getName());
    }

    @Override
    public int hashCode() {
        if (fieldMetadata instanceof CompoundFieldMetadata) {
             int hashCode = 0;
             FieldMetadata[] fields = ((CompoundFieldMetadata) fieldMetadata).getFields();
             for (FieldMetadata field : fields) {
                 hashCode = hashCode + field.getName().hashCode();
             }
             return hashCode;
        } else {
                 return fieldMetadata.getName().hashCode();
        }
    }
}
