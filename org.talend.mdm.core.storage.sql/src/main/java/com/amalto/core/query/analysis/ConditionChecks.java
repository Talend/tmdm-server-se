/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.query.analysis;

import com.amalto.core.query.user.*;
import com.amalto.core.query.user.metadata.*;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.CompoundFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;

public class ConditionChecks extends VisitorAdapter<Result> {

    private final Select select;

    private FieldMetadata keyField;

    public ConditionChecks(Select select) {
        this.select = select;
    }

    @Override
    public Result visit(Max max) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(Min min) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(Isa isa) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(IsEmpty isEmpty) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(NotIsEmpty notIsEmpty) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(UnaryLogicOperator condition) {
        // TMDM-5319: Using a 'not' predicate, don't do a get by id.
        if (condition.getPredicate() == Predicate.NOT) {
            Result fieldResult = new Result();
            fieldResult.id = false;
            return fieldResult;
        } else {
            return condition.getCondition().accept(this);
        }
    }

    @Override
    public Result visit(Condition condition) {
        Result conditionResult = new Result();
        conditionResult.id = condition != UserQueryHelper.TRUE;
        return conditionResult;
    }

    @Override
    public Result visit(BinaryLogicOperator condition) {
        Result conditionResult = new Result();
        Result leftResult = condition.getLeft().accept(this);
        Result rightResult = condition.getRight().accept(this);
        conditionResult.id = leftResult.id && rightResult.id;
        conditionResult.limitJoins = leftResult.limitJoins && rightResult.limitJoins;
        return conditionResult;
    }

    @Override
    public Result visit(TaskId taskId) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(StagingStatus stagingStatus) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(StagingError stagingError) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(StagingSource stagingSource) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(StagingBlockKey stagingBlockKey) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(StagingHasTask stagingHasTask) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(GroupSize groupSize) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(IndexedField indexedField) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(Compare condition) {
        Result conditionResult = new Result();
        Result result = condition.getLeft().accept(this);
        if (!(condition.getRight() instanceof ConstantExpression)) {
            result.id = false;
        } else {
            conditionResult.id = result.id && condition.getPredicate() == Predicate.EQUALS;
            conditionResult.limitJoins = result.limitJoins;
        }
        return conditionResult;
    }

    @Override
    public Result visit(Id id) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(Timestamp timestamp) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(IsNull isNull) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(NotIsNull notIsNull) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(FullText fullText) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(Range range) {
        Result fieldResult = new Result();
        fieldResult.id = false;
        return fieldResult;
    }

    @Override
    public Result visit(Field field) {
        Result result = new Result();
        FieldMetadata fieldMetadata = field.getFieldMetadata();
        // Limit join for contained fields
        if (!result.limitJoins) {
            int level = StringUtils.countMatches(fieldMetadata.getPath(), "/"); //$NON-NLS-1$
            if (level > 2 || !select.getTypes().contains(fieldMetadata.getContainingType())) {
                result.limitJoins = true;
            }
        }
        if (fieldMetadata.getContainingType().getKeyFields().size() == 1) {
            if (fieldMetadata.isKey() && !(fieldMetadata instanceof CompoundFieldMetadata)) {
                if (keyField != null) {
                    // At least twice an Id field means different Id values
                    // TODO Support for "entity/id = n AND entity/id = n" (could simplified to "entity/id = n").
                    result.id = false;
                } else {
                    keyField = fieldMetadata;
                    result.id = true;
                }
            }
        } // TODO Support compound key field.
        return result;
    }
}
