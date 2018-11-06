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

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

public class Id implements Expression {

    private final ComplexTypeMetadata type;

    private final String id;

    private List<String> idList;

    public Id(ComplexTypeMetadata type, String id) {
        assert id != null;
        this.type = type;
        this.id = id;
        this.idList = null;
    }

    public Id(ComplexTypeMetadata type, List<String> idList) {
        assert idList != null;
        this.type = type;
        this.idList = idList;
        this.id = null;
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
        if (isExpressionList()) {
            throw new IllegalStateException("The property of 'value' is not valid."); //$NON-NLS-1$
        }
        return id;
    }

    public List<String> getIdList() {
        if (!isExpressionList()) {
            throw new IllegalStateException("The property of 'valueList' is not valid."); //$NON-NLS-1$
        }
        return idList;
    }

    public boolean isExpressionList() {
        return this.idList != null;
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
        if (isExpressionList()) {
            return this.idList.equals(id1.idList);
        } else {
            if (id != null ? !id.equals(id1.id) : id1.id != null) {
                return false;
            }
        }

        if (type != null ? !type.equals(id1.type) : id1.type != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        if (isExpressionList()) {
            result = 31 * result + idList.hashCode();
        } else {
            result = 31 * result + (id != null ? id.hashCode() : 0);
        }
        return result;
    }
}
