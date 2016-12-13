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

public class UnaryLogicOperator implements Condition {

    private Condition condition;

    private final Predicate predicate;

    public UnaryLogicOperator(Condition condition, Predicate predicate) {
        this.condition = condition;
        this.predicate = predicate;
    }

    public Condition getCondition() {
        return condition;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public Expression normalize() {
        if (predicate == Predicate.NOT) {
            return condition.accept(new VisitorAdapter<Condition>() {

                @Override
                public Condition visit(ConstantCondition constantCondition) {
                    if (constantCondition.value()) {
                        return UserQueryHelper.FALSE;
                    } else {
                        return UserQueryHelper.TRUE;
                    }
                }

                @Override
                public Condition visit(IsEmpty isEmpty) {
                    return new NotIsEmpty(isEmpty.getField());
                }

                @Override
                public Condition visit(IsNull isNull) {
                    return new NotIsNull(isNull.getField());
                }

                @Override
                public Condition visit(NotIsEmpty notIsEmpty) {
                    return new IsEmpty(notIsEmpty.getField());
                }

                @Override
                public Condition visit(NotIsNull notIsNull) {
                    return new IsNull(notIsNull.getField());
                }

                @Override
                public Condition visit(Compare condition) {
                    return new UnaryLogicOperator(condition, Predicate.NOT);
                }

                @Override
                public Condition visit(BinaryLogicOperator condition) {
                    return new BinaryLogicOperator(condition.getLeft().accept(this), condition.getPredicate(), condition.getRight().accept(this));
                }
            });
        } else {
            condition = (Condition) condition.normalize();
            if (condition instanceof ConstantCondition) {
                ConstantCondition constantCondition = (ConstantCondition) condition;
                if (constantCondition.value()) {
                    return UserQueryHelper.FALSE;
                } else {
                    return UserQueryHelper.TRUE;
                }
            }
            return this;
        }
    }

    @Override
    public boolean cache() {
        return false;
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UnaryLogicOperator)) {
            return false;
        }
        UnaryLogicOperator that = (UnaryLogicOperator) o;
        if (condition != null ? !condition.equals(that.condition) : that.condition != null) {
            return false;
        }
        if (predicate != null ? !predicate.equals(that.predicate) : that.predicate != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = condition != null ? condition.hashCode() : 0;
        result = 31 * result + (predicate != null ? predicate.hashCode() : 0);
        return result;
    }
}
