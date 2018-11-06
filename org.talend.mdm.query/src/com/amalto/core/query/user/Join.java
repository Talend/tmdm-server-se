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

/**
 *
 */
public class Join implements Expression {

    private final Field leftField;

    private final Field rightField;

    private final JoinType joinType;

    public Join(Field leftField, Field rightField, JoinType joinType) {
        this.leftField = leftField;
        this.rightField = rightField;
        this.joinType = joinType;
    }

    public Expression normalize() {
        return this;
    }

    @Override
    public boolean cache() {
        return false;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public Field getLeftField() {
        return leftField;
    }

    public Field getRightField() {
        return rightField;
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Join)) {
            return false;
        }
        Join join = (Join) o;
        if (joinType != join.joinType) {
            return false;
        }
        if (leftField != null ? !leftField.equals(join.leftField) : join.leftField != null) {
            return false;
        }
        if (rightField != null ? !rightField.equals(join.rightField) : join.rightField != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = leftField != null ? leftField.hashCode() : 0;
        result = 31 * result + (rightField != null ? rightField.hashCode() : 0);
        result = 31 * result + (joinType != null ? joinType.hashCode() : 0);
        return result;
    }
}
