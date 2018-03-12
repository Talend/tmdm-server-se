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

public class FieldFullText extends FullText {

    private final Field field;

    public FieldFullText(Field field, String value) {
        super(value);
        this.field = field;
    }

    public Expression normalize() {
        return this;
    }

    @Override
    public boolean cache() {
        return false;
    }

    public Field getField() {
        return field;
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldFullText)) {
            return false;
        }
        FieldFullText that = (FieldFullText) o;
        return !(field != null ? !field.equals(that.field) : that.field != null) && this.getValue().equals(that.getValue());
    }

    @Override
    public int hashCode() {
        int hashValue = 0;
        if (field != null) {
            hashValue = field.hashCode();
            hashValue = 31 * hashValue + (this.getValue() == null ? 0 : this.getValue().hashCode());
        }
        return hashValue;
    }
}
