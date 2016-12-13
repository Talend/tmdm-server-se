/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.query.user;

/**
 *
 */
public class NativeQuery implements Expression {

    private final String nativeQuery;

    public NativeQuery(String nativeQuery) {
        this.nativeQuery = nativeQuery;
    }

    public String getQueryText() {
        return nativeQuery;
    }

    @Override
    public Expression normalize() {
        return this;
    }

    @Override
    public boolean cache() {
        return false;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NativeQuery)) {
            return false;
        }
        NativeQuery that = (NativeQuery) o;
        return !(nativeQuery != null ? !nativeQuery.equals(that.nativeQuery) : that.nativeQuery != null);
    }

    @Override
    public int hashCode() {
        return nativeQuery != null ? nativeQuery.hashCode() : 0;
    }
}
