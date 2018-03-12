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

public class FullText implements Condition {

    private final String value;

    public FullText(String value) {
        this.value = value;
    }

    public Expression normalize() {
        return this;
    }

    @Override
    public boolean cache() {
        return false;
    }

    public String getValue() {
        return value;
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FullText)) {
            return false;
        }
        FullText fullText = (FullText) o;
        return !(value != null ? !value.equals(fullText.value) : fullText.value != null);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
