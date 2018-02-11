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

package com.amalto.core.storage;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import com.amalto.core.query.user.*;
import com.amalto.core.query.user.metadata.*;

// TODO Instead of removing, it is possible to return arbitrary string value ("???", "###"...).
class SecurityQueryCleaner extends VisitorAdapter<Expression> {

    private final Checker checker;

    private final SecuredStorage.UserDelegator delegator;

    public SecurityQueryCleaner(SecuredStorage.UserDelegator delegator) {
        this.delegator = delegator;
        checker = new Checker(delegator);
    }

    @Override
    public Expression visit(NativeQuery nativeQuery) {
        return nativeQuery;
    }

    @Override
    public Expression visit(Range range) {
        if (!range.getExpression().accept(checker)) {
            return UserQueryHelper.TRUE;
        } else {
            return range;
        }
    }

    @Override
    public Expression visit(Select select) {
        Iterator<TypedExpression> fields = select.getSelectedFields().iterator();
        while (fields.hasNext()) {
            if (!fields.next().accept(checker)) {
                fields.remove();
            }
        }

        Iterator<ComplexTypeMetadata> types = select.getTypes().iterator();
        while (types.hasNext()) {
            if (delegator.hide(types.next())) {
                types.remove();
            }
        }
        if (select.getTypes().isEmpty()) {
            throw new IllegalStateException("Security rules removed all selected types in query.");
        }

        Condition condition = select.getCondition();
        if (condition != null) {
            select.setCondition((Condition) condition.accept(this));
        }

        List<OrderBy> orderBy = select.getOrderBy();
        List<OrderBy> checkedOrderBy = new LinkedList<OrderBy>();
        for (OrderBy current : orderBy) {
            if (current.accept(checker)) {
                checkedOrderBy.add(current);
            }
        }
        select.getOrderBy().clear();
        select.getOrderBy().addAll(checkedOrderBy);

        return select;
    }

    @Override
    public Expression visit(ConstantCondition constantCondition) {
        return constantCondition;
    }

    @Override
    public Expression visit(ConstantCollection collection) {
        return collection;
    }

    @Override
    public Expression visit(Expression expression) {
        return expression;
    }

    @Override
    public Expression visit(ComplexTypeExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(IndexedField indexedField) {
        if (delegator.hide(indexedField.getFieldMetadata())) {
            return UserQueryHelper.TRUE;
        } else {
            return indexedField;
        }
    }

    @Override
    public Expression visit(Isa isa) {
        if(!isa.getExpression().accept(checker)) {
            return UserQueryHelper.TRUE;
        } else {
            return isa;
        }
    }

    @Override
    public Expression visit(NotIsEmpty notIsEmpty) {
        if (!notIsEmpty.getField().accept(checker)) {
            return UserQueryHelper.TRUE;
        } else {
            return notIsEmpty;
        }
    }

    @Override
    public Expression visit(NotIsNull notIsNull) {
        if (!notIsNull.getField().accept(checker)) {
            return UserQueryHelper.TRUE;
        } else {
            return notIsNull;
        }
    }

    @Override
    public Expression visit(IsEmpty isEmpty) {
        if (!isEmpty.getField().accept(checker)) {
            return UserQueryHelper.TRUE;
        } else {
            return isEmpty;
        }
    }

    @Override
    public Expression visit(IsNull isNull) {
        if (!isNull.getField().accept(checker)) {
            return UserQueryHelper.TRUE;
        } else {
            return isNull;
        }
    }

    @Override
    public Expression visit(BinaryLogicOperator condition) {
        if (!condition.getLeft().accept(checker) && !condition.getRight().accept(checker)) {
            return UserQueryHelper.TRUE;
        } else if (!condition.getLeft().accept(checker)) {
            return new BinaryLogicOperator(UserQueryHelper.TRUE, condition.getPredicate(), condition.getRight());
        } else if (!condition.getRight().accept(checker)) {
            return new BinaryLogicOperator(condition.getLeft(), condition.getPredicate(), UserQueryHelper.TRUE);
        } else {
            return condition;
        }
    }

    @Override
    public Expression visit(UnaryLogicOperator condition) {
        if (!condition.getCondition().accept(checker)) {
            return new UnaryLogicOperator(UserQueryHelper.TRUE, condition.getPredicate());
        } else {
            return condition;
        }
    }

    @Override
    public Expression visit(Compare condition) {
        if (!condition.getLeft().accept(checker) || !condition.getRight().accept(checker)) {
            return UserQueryHelper.TRUE;
        } else {
            return condition;
        }
    }

    @Override
    public Expression visit(Condition condition) {
        return condition;
    }

    @Override
    public Expression visit(FullText fullText) {
        // TODO Could maybe restrict full text depending on user rights... but could also be in result creation.
        return fullText;
    }

    @Override
    public Expression visit(FieldFullText fullText) {
        if (!fullText.getField().accept(checker)) {
            return UserQueryHelper.TRUE;
        } else {
            return fullText;
        }
    }

    @Override
    public Expression visit(StagingStatus stagingStatus) {
        return stagingStatus;
    }

    @Override
    public Expression visit(StagingError stagingError) {
        return stagingError;
    }

    @Override
    public Expression visit(StagingSource stagingSource) {
        return stagingSource;
    }

    @Override
    public Expression visit(StagingBlockKey stagingBlockKey) {
        return stagingBlockKey;
    }

    @Override
    public Expression visit(StagingHasTask stagingHasTask) {
        return stagingHasTask;
    }
}
