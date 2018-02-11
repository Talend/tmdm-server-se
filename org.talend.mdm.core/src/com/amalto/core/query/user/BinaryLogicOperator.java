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

public class BinaryLogicOperator implements Condition {

    private Condition left;

    private final Predicate predicate;

    private Condition right;

    public BinaryLogicOperator(Condition left, Predicate predicate, Condition right) {
        this.left = left;
        this.predicate = predicate;
        this.right = right;
    }

    public Condition getLeft() {
        return left;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public Condition getRight() {
        return right;
    }

    public Expression normalize() {
        left = (Condition) left.normalize();
        right = (Condition) right.normalize();
        // If left and right are same (cond1 OR cond1 = cond1 / cond1 AND cond1 = cond1), return one of the side
        if (left.equals(right)) {
            return left;
        }
        // If right or left is a constant condition, simplify a bit the query.
        if (right instanceof ConstantCondition && left instanceof ConstantCondition) {
            ConstantCondition constantLeft = (ConstantCondition) left;
            ConstantCondition constantRight = (ConstantCondition) right;
            if (predicate == Predicate.AND) {
                if (constantLeft.value() && constantRight.value()) {
                    return UserQueryHelper.TRUE;
                } else {
                    return UserQueryHelper.FALSE;
                }
            } else if (predicate == Predicate.OR) {
                if (!constantLeft.value() && !constantRight.value()) {
                    return UserQueryHelper.FALSE; 
                } else {
                    return UserQueryHelper.TRUE;
                }
            }
        } else if (left instanceof ConstantCondition) { // only left is a constant condition
            ConstantCondition constantLeft = (ConstantCondition) left;
            if(constantLeft.value()) {
                if (predicate == Predicate.OR) {
                    return UserQueryHelper.TRUE;
                } else if (predicate == Predicate.AND) {
                    return right;
                }
            } else {
                if (predicate == Predicate.OR) {
                    return right;
                } else if (predicate == Predicate.AND) {
                    return UserQueryHelper.FALSE;
                }
            }
        } else if (right instanceof ConstantCondition) { // only right is a constant condition
            ConstantCondition constantRight = (ConstantCondition) right;
            if (constantRight.value()) {
                if (predicate == Predicate.OR) {
                    return UserQueryHelper.TRUE;
                } else if (predicate == Predicate.AND) {
                    return left;
                }
            } else {
                if (predicate == Predicate.OR) {
                    return left;
                } else if (predicate == Predicate.AND) {
                    return UserQueryHelper.FALSE;
                }
            }
        }
        return this;
    }

    @Override
    public boolean cache() {
        return false;
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public void setLeft(Condition condition) {
        this.left = condition;
    }

    public void setRight(Condition condition) {
        this.right = condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BinaryLogicOperator)) {
            return false;
        }
        BinaryLogicOperator that = (BinaryLogicOperator) o;
        if (left != null ? !left.equals(that.left) : that.left != null) {
            return false;
        }
        if (predicate != null ? !predicate.equals(that.predicate) : that.predicate != null) {
            return false;
        }
        if (right != null ? !right.equals(that.right) : that.right != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (predicate != null ? predicate.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
