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

package com.amalto.core.storage.hibernate;

import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.query.user.*;
import com.amalto.core.query.user.metadata.*;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.CompoundFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.Types;

import java.util.*;

class MappingExpressionTransformer extends VisitorAdapter<Expression> {

    private final MappingRepository mappingRepository;

    private UserQueryBuilder builder;

    private TypedExpression currentField;

    public MappingExpressionTransformer(MappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    private ComplexTypeMetadata getMapping(ComplexTypeMetadata type) {
        TypeMapping mapping = mappingRepository.getMappingFromUser(type);
        if (mapping != null) {
            return mapping.getDatabase();
        } else {
            throw new IllegalStateException("Type '" + type.getName() + "' does not have a mapping.");
        }
    }

    private FieldMetadata getMapping(FieldMetadata fieldMetadata) {
        TypeMapping mapping = mappingRepository.getMappingFromUser(fieldMetadata.getContainingType());
        if (fieldMetadata instanceof CompoundFieldMetadata) {
            FieldMetadata[] fields = ((CompoundFieldMetadata) fieldMetadata).getFields();
            FieldMetadata[] mappedFields = new FieldMetadata[fields.length];
            int i = 0;
            for (FieldMetadata field : fields) {
                mappedFields[i++] = getMapping(field);
            }
            return new CompoundFieldMetadata(mappedFields);
        } else {
            if (mapping != null) {
                ComplexTypeMetadata databaseType = mapping.getDatabase();
                FieldMetadata databaseField = mapping.getDatabase(fieldMetadata);
                if (databaseField == null) {
                    // Look if database type contains field (in case a database field was used in user query).
                    if (databaseType.hasField(fieldMetadata.getName())) {
                        databaseField = fieldMetadata;
                    } else {
                        // Null means there's no mapping and may be used to indicate fields that shouldn't be taken into
                        // account.
                        return null;
                    }
                }
                return databaseField;
            } else {
                throw new IllegalStateException("Field '" + fieldMetadata + "' is located in a type without mapping.");
            }
        }
    }

    @Override
    public Expression visit(Select select) {
        for (ComplexTypeMetadata type : select.getTypes()) {
            ComplexTypeMetadata mappedType = getMapping(type);
            if (builder == null) {
                builder = UserQueryBuilder.from(mappedType);
            } else {
                builder = builder.and(mappedType);
            }
        }
        for (Join join : select.getJoins()) {
            builder.getSelect().getJoins().add((Join) join.accept(this));
        }
        Condition condition = select.getCondition();
        if (condition != null) {
            builder.where((Condition) condition.accept(this));
        }
        for (OrderBy current : select.getOrderBy()) {
            builder.orderBy((TypedExpression) current.getExpression().accept(this), current.getDirection());
        }
        for (TypedExpression selectedField : select.getSelectedFields()) {
            builder.select((TypedExpression) selectedField.accept(this));
        }
        if (select.getPaging() != null) {
            select.getPaging().accept(this);
        }
        if (select.forUpdate()) {
            builder.forUpdate();
        }
        return builder.getExpression();
    }

    @Override
    public Expression visit(ConstantCondition constantCondition) {
        return constantCondition;
    }

    @Override
    public Expression visit(NativeQuery nativeQuery) {
        return nativeQuery;
    }

    @Override
    public Expression visit(Max max) {
        return new Max((TypedExpression) max.getExpression().accept(this));
    }

    @Override
    public Expression visit(Min min) {
        return new Min((TypedExpression) min.getExpression().accept(this));
    }

    @Override
    public Expression visit(Compare condition) {
        Expression previousLeft = condition.getLeft();
        Expression left = previousLeft.accept(this);
        Predicate predicate = condition.getPredicate();
        if (previousLeft instanceof TypedExpression && left instanceof TypedExpression) {
            TypedExpression previousTypedExpression = (TypedExpression) previousLeft;
            TypedExpression typedExpression = (TypedExpression) left;
            if (Types.STRING.equals(previousTypedExpression.getTypeName())
                    && predicate == Predicate.CONTAINS
                    && !previousTypedExpression.getTypeName().equals(typedExpression.getTypeName())) {
                predicate = Predicate.EQUALS;
            }
        }
        Expression constant = condition.getRight().accept(this);
        if (constant != null) {
            return new Compare(left,
                    predicate,
                    constant);
        } else {
            return UserQueryHelper.TRUE;
        }
    }

    @Override
    public Expression visit(BinaryLogicOperator condition) {
        return new BinaryLogicOperator(((Condition) condition.getLeft().accept(this)),
                condition.getPredicate(),
                ((Condition) condition.getRight().accept(this)));
    }

    @Override
    public Expression visit(UnaryLogicOperator condition) {
        return new UnaryLogicOperator((Condition) condition.getCondition().accept(this), condition.getPredicate());
    }

    @Override
    public Expression visit(Range range) {
        return new Range((TypedExpression) range.getExpression().accept(this), range.getStart(), range.getEnd());
    }

    @Override
    public Expression visit(Timestamp timestamp) {
        currentField = timestamp;
        return timestamp;
    }

    @Override
    public Expression visit(TaskId taskId) {
        currentField = taskId;
        return taskId;
    }

    @Override
    public Expression visit(Type type) {
        Type mappedType = new Type((Field) type.getField().accept(this));
        currentField = mappedType;
        return mappedType;
    }

    @Override
    public Expression visit(StagingStatus stagingStatus) {
        currentField = stagingStatus;
        return stagingStatus;
    }

    @Override
    public Expression visit(StagingError stagingError) {
        currentField = stagingError;
        return stagingError;
    }

    @Override
    public Expression visit(StagingSource stagingSource) {
        currentField = stagingSource;
        return stagingSource;
    }

    @Override
    public Expression visit(StagingBlockKey stagingBlockKey) {
        currentField = stagingBlockKey;
        return stagingBlockKey;
    }

    @Override
    public Expression visit(StagingHasTask stagingHasTask) {
        currentField = stagingHasTask;
        return stagingHasTask;
    }

    @Override
    public Expression visit(GroupSize groupSize) {
        currentField = groupSize;
        return groupSize;
    }

    @Override
    public Expression visit(Join join) {
        return new Join(((Field) join.getLeftField().accept(this)),
                (Field) join.getRightField().accept(this),
                join.getJoinType());
    }

    @Override
    public Expression visit(Expression expression) {
        return builder.getExpression();
    }

    @Override
    public Expression visit(Predicate predicate) {
        return builder.getExpression();
    }

    @Override
    public Expression visit(Field field) {
        FieldMetadata fieldMetadata = field.getFieldMetadata();
        FieldMetadata mappedFieldMetadata = getMapping(fieldMetadata);
        // Compute the path from entity to field
        ComplexTypeMetadata entity = fieldMetadata.getContainingType().getEntity();
        // Field is in entity (could be a nested field).
        List<FieldMetadata> path = StorageMetadataUtils.path(entity, fieldMetadata);
        List<FieldMetadata> mappingPath = new ArrayList<FieldMetadata>(path.size());
        for (FieldMetadata pathElement : path) {
            FieldMetadata mappedField = getMapping(pathElement);
            if (mappedField != null) {
                mappingPath.add(mappedField);
            }
        }
        // Create the new field (target is the mapping in database, and contains the path to field if any needs to be set).
        Field mappedField = new Field(mappedFieldMetadata);
        mappedField.setPath(mappingPath);
        currentField = mappedField;
        return mappedField;
    }

    @Override
    public Expression visit(Alias alias) {
        currentField = alias.getTypedExpression();
        return new Alias((TypedExpression) alias.getTypedExpression().accept(this), alias.getAliasName());
    }

    @Override
    public Expression visit(Distinct distinct) {
        return new Distinct((TypedExpression) distinct.getExpression().accept(this));
    }

    @Override
    public Expression visit(Id id) {
        if (id.isExpressionList()) {
            return new Id(getMapping(id.getType()), id.getIdList());
        } else {
            return new Id(getMapping(id.getType()), id.getId());
        }
    }

    @Override
    public Expression visit(ConstantCollection collection) {
        TypedExpression[] constantExpressions = new TypedExpression[collection.getValues().length];
        int i = 0;
        for (Expression expression : collection.getValues()) {
            constantExpressions[i++] = (TypedExpression) expression.accept(this);
        }
        return new ConstantCollection(constantExpressions);
    }

    @Override
    public Expression visit(StringConstant constant) {
        return getConstant(constant.getValueObject());
    }

    @Override
    public Expression visit(IntegerConstant constant) {
        return getConstant(constant.getValueObject());
    }

    @Override
    public Expression visit(DateConstant constant) {
        if (constant.isExpressionList()) {
            return getConstant(constant.getValueList());
        } else {
            return getConstant(DateConstant.DATE_FORMAT.format(constant.getValue()));
        }
    }

    @Override
    public Expression visit(DateTimeConstant constant) {
        if (constant.isExpressionList()) {
            return getConstant(constant.getValueList());
        } else {
            return getConstant(DateTimeConstant.DATE_FORMAT.format(constant.getValue()));
        }
    }

    @Override
    public Expression visit(BooleanConstant constant) {
        return getConstant(constant.getValueObject());
    }

    @Override
    public Expression visit(BigDecimalConstant constant) {
        return getConstant(constant.getValueObject());
    }

    @Override
    public Expression visit(TimeConstant constant) {
        if (constant.isExpressionList()) {
            return getConstant(constant.getValueList());
        } else {
            return getConstant(TimeConstant.TIME_FORMAT.format(constant.getValue()));
        }
    }

    @Override
    public Expression visit(ShortConstant constant) {
        return getConstant(constant.getValueObject());
    }

    @Override
    public Expression visit(ByteConstant constant) {
        return getConstant(constant.getValueObject());
    }

    @Override
    public Expression visit(LongConstant constant) {
        return getConstant(constant.getValueObject());
    }

    @Override
    public Expression visit(DoubleConstant constant) {
        return getConstant(constant.getValueObject());
    }

    @Override
    public Expression visit(FloatConstant constant) {
        return getConstant(constant.getValueObject());
    }

    @Override
    public Expression visit(IsEmpty isEmpty) {
        return new IsEmpty((TypedExpression) isEmpty.getField().accept(this));
    }

    @Override
    public Expression visit(NotIsEmpty notIsEmpty) {
        return new NotIsEmpty((TypedExpression) notIsEmpty.getField().accept(this));
    }

    @Override
    public Expression visit(IsNull isNull) {
        return new IsNull((TypedExpression) isNull.getField().accept(this));
    }

    @Override
    public Expression visit(NotIsNull notIsNull) {
        return new NotIsNull((TypedExpression) notIsNull.getField().accept(this));
    }

    @Override
    public Expression visit(OrderBy orderBy) {
        return new OrderBy((TypedExpression) orderBy.getExpression().accept(this), orderBy.getDirection());
    }

    @Override
    public Expression visit(Paging paging) {
        builder.start(paging.getStart());
        builder.limit(paging.getLimit());
        return builder.getExpression();
    }

    @Override
    public Expression visit(Count count) {
        if (count.getExpression() != null) {
            return new Count((TypedExpression) count.getExpression().accept(this));
        }
        return count;
    }

    @Override
    public Expression visit(FullText fullText) {
        return fullText;
    }

    @Override
    public Expression visit(FieldFullText fullText) {
        fullText.getField().accept(this);
        if (currentField == null) {
            throw new IllegalStateException("Could not get field information from full text expression.");
        }
        if (!(currentField instanceof Field)) {
            throw new IllegalStateException("Expected full text expression to contains a field not '" + currentField.getClass() + "'.");
        }
        return new FieldFullText((Field) currentField, fullText.getValue());
    }

    @Override
    public Expression visit(Isa isa) {
        return new Isa((TypedExpression) isa.getExpression().accept(this), getMapping(isa.getType()));
    }

    @Override
    public Expression visit(ComplexTypeExpression expression) {
        return new ComplexTypeExpression(getMapping(expression.getType()));
    }

    @Override
    public Expression visit(IndexedField indexedField) {
        IndexedField mappedField = new IndexedField(getMapping(indexedField.getFieldMetadata()), indexedField.getPosition());
        currentField = mappedField;
        return mappedField;
    }

    @SuppressWarnings("rawtypes")
    private Expression getConstant(Object data) {
        if (data instanceof List) {
            return getConstant((List) data);
        } else {
            if (StorageMetadataUtils.isValueAssignable(String.valueOf(data), currentField.getTypeName())) {
                return UserQueryBuilder.createConstant(currentField, String.valueOf(data));
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private Expression getConstant(List data) {
        if (StorageMetadataUtils.isValueAssignable(data, currentField.getTypeName())) {
            return UserQueryBuilder.createConstant(currentField, data);
        } else {
            return null;
        }
    }
}
