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

public class Range implements Condition {

    private final TypedExpression expression;

    private final Expression start;

    private final Expression end;

    public Range(TypedExpression expression, Expression start, Expression end) {
        this.expression = expression;
        this.start = start;
        this.end = end;
    }

    public Range(TypedExpression expression, int start, int end) {
        this(expression, new IntegerConstant(start), new IntegerConstant(end));
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

    public TypedExpression getExpression() {
        return expression;
    }

    public Expression getStart() {
        return start;
    }

    public Expression getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Range)) {
            return false;
        }
        Range range = (Range) o;
        if (end != null ? !end.equals(range.end) : range.end != null) {
            return false;
        }
        if (expression != null ? !expression.equals(range.expression) : range.expression != null) {
            return false;
        }
        if (start != null ? !start.equals(range.start) : range.start != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = expression != null ? expression.hashCode() : 0;
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }
}
