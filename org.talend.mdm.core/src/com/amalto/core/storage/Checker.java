/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage;

import com.amalto.core.query.user.*;
import com.amalto.core.query.user.metadata.*;

class Checker extends VisitorAdapter<Boolean> {

    private final SecuredStorage.UserDelegator delegator;

    public Checker(SecuredStorage.UserDelegator delegator) {
        this.delegator = delegator;
    }

    @Override
    public Boolean visit(GroupSize groupSize) {
        // Always allow group size operator.
        return true;
    }

    @Override
    public Boolean visit(Count count) {
        // Always allow count operation.
        return true;
    }

    @Override
    public Boolean visit(ConstantCondition constantCondition) {
        // Always allow constant conditions.
        return true;
    }

    @Override
    public Boolean visit(Expression expression) {
        return true;
    }

    @Override
    public Boolean visit(IndexedField indexedField) {
        return !delegator.hide(indexedField.getFieldMetadata());
    }

    @Override
    public Boolean visit(Id id) {
        return !delegator.hide(id.getType());
    }

    @Override
    public Boolean visit(NotIsEmpty notIsEmpty) {
        return notIsEmpty.getField().accept(this);
    }

    @Override
    public Boolean visit(NotIsNull notIsNull) {
        return notIsNull.getField().accept(this);
    }

    @Override
    public Boolean visit(IsEmpty isEmpty) {
        return isEmpty.getField().accept(this);
    }

    @Override
    public Boolean visit(IsNull isNull) {
        return isNull.getField().accept(this);
    }

    @Override
    public Boolean visit(Max max) {
        return max.getExpression().accept(this);
    }

    @Override
    public Boolean visit(Min min) {
        return min.getExpression().accept(this);
    }

    @Override
    public Boolean visit(Distinct distinct) {
        return distinct.getExpression().accept(this);
    }

    @Override
    public Boolean visit(Alias alias) {
        return alias.getTypedExpression().accept(this);
    }

    @Override
    public Boolean visit(Type type) {
        return type.getField().accept(this);
    }

    @Override
    public Boolean visit(Timestamp timestamp) {
        return true;
    }

    @Override
    public Boolean visit(TaskId taskId) {
        return true;
    }

    @Override
    public Boolean visit(OrderBy orderBy) {
        return orderBy.getExpression().accept(this);
    }

    @Override
    public Boolean visit(BinaryLogicOperator condition) {
        return condition.getLeft().accept(this) && condition.getRight().accept(this);
    }

    @Override
    public Boolean visit(UnaryLogicOperator condition) {
        return condition.getCondition().accept(this);
    }

    @Override
    public Boolean visit(Compare condition) {
        return condition.getLeft().accept(this) && condition.getRight().accept(this);
    }

    @Override
    public Boolean visit(ConstantCollection collection) {
        boolean ret = true;
        for (Expression expression : collection.getValues()) {
            ret &= expression.accept(this);
        }
        return ret;
    }

    @Override
    public Boolean visit(StringConstant constant) {
        return true;
    }

    @Override
    public Boolean visit(IntegerConstant constant) {
        return true;
    }

    @Override
    public Boolean visit(DateConstant constant) {
        return true;
    }

    @Override
    public Boolean visit(DateTimeConstant constant) {
        return true;
    }

    @Override
    public Boolean visit(BooleanConstant constant) {
        return true;
    }

    @Override
    public Boolean visit(BigDecimalConstant constant) {
        return true;
    }

    @Override
    public Boolean visit(TimeConstant constant) {
        return true;
    }

    @Override
    public Boolean visit(ShortConstant constant) {
        return true;
    }

    @Override
    public Boolean visit(ByteConstant constant) {
        return true;
    }

    @Override
    public Boolean visit(LongConstant constant) {
        return true;
    }

    @Override
    public Boolean visit(DoubleConstant constant) {
        return true;
    }

    @Override
    public Boolean visit(FloatConstant constant) {
        return true;
    }

    @Override
    public Boolean visit(Field field) {
        return !delegator.hide(field.getFieldMetadata().getContainingType()) && !delegator.hide(field.getFieldMetadata());
    }

    @Override
    public Boolean visit(Condition condition) {
        return true;
    }

    @Override
    public Boolean visit(FullText fullText) {
        return true;
    }

    @Override
    public Boolean visit(FieldFullText fullText) {
        return true;
    }

    @Override
    public Boolean visit(StagingStatus stagingStatus) {
        return true;
    }

    @Override
    public Boolean visit(StagingError stagingError) {
        return true;
    }

    @Override
    public Boolean visit(StagingSource stagingSource) {
        return true;
    }

    @Override
    public Boolean visit(StagingBlockKey stagingBlockKey) {
        return true;
    }

    @Override
    public Boolean visit(StagingHasTask stagingHasTask) {
        return true;
    }

    @Override
    public Boolean visit(Range range) {
        return range.getExpression().accept(this);
    }

    @Override
    public Boolean visit(Isa isa) {
        return isa.getExpression().accept(this);
    }

    @Override
    public Boolean visit(ComplexTypeExpression expression) {
        return true;
    }
}
