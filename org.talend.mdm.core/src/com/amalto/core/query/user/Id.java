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


import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

public class Id implements Expression {

    private final ComplexTypeMetadata type;

    private final String id;

    public Id(ComplexTypeMetadata type, String id) {
        this.type = type;
        this.id = id;
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

    public String getId() {
        return id;
    }

    public ComplexTypeMetadata getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Id)) {
            return false;
        }
        Id id1 = (Id) o;
        if (id != null ? !id.equals(id1.id) : id1.id != null) {
            return false;
        }
        if (type != null ? !type.equals(id1.type) : id1.type != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
