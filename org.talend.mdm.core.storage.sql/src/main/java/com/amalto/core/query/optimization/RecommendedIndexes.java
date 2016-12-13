/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.query.optimization;

import com.amalto.core.query.user.*;
import com.amalto.core.query.user.At;
import com.amalto.core.query.user.metadata.*;
import org.talend.mdm.commmon.metadata.*;

import java.util.*;

/**
 * A utility class that allows code to detect what field(s) should be indexed for optimal performance.
 */
public class RecommendedIndexes {

    private static final RecommendedIndexVisitor RECOMMENDED_INDEX_VISITOR = new RecommendedIndexVisitor();

    private RecommendedIndexes() {
    }

    /**
     * Computes all recommended indexes for a query: scans order by, conditions, joins statements and all fields that
     * should be indexed to allow fast execution.
     *
     * @param expression A {@link com.amalto.core.query.user.Expression} that represents a query to perform.
     * @return A {@link java.util.Collection} of {@link org.talend.mdm.commmon.metadata.FieldMetadata} that should be
     *         indexed in order to obtain best performances. If no field needs to be index, this method returns empty
     *         collection.
     * @throws IllegalArgumentException if <code>expression</code> is null.
     */
    public static Collection<FieldMetadata> get(Expression expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Expression cannot be null.");
        }
        return expression.accept(RECOMMENDED_INDEX_VISITOR);
    }

    /**
     * @param expression A {@link com.amalto.core.query.user.Expression} that represents a query to perform.
     * @return A {@link Collection} of {@link ComplexTypeMetadata} types selected in <code>expression</code>.
     * @throws IllegalArgumentException if <code>expression</code> is null.
     */
    public static Collection<ComplexTypeMetadata> getRoots(Expression expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Expression cannot be null.");
        }
        return expression.accept(new VisitorAdapter<Collection<ComplexTypeMetadata>>() {
            @Override
            public Collection<ComplexTypeMetadata> visit(Select select) {
                return select.getTypes();
            }

            @Override
            public Collection<ComplexTypeMetadata> visit(NativeQuery nativeQuery) {
                return Collections.emptySet();
            }
        });
    }

    private static class RecommendedIndexVisitor implements Visitor<Collection<FieldMetadata>> {

        public Collection<FieldMetadata> visit(Select select) {
            Collection<FieldMetadata> fields = new HashSet<FieldMetadata>();
            // Joins
            for (Join join : select.getJoins()) {
                fields.addAll(join.accept(this));
            }
            // Conditions
            Condition condition = select.getCondition();
            if (condition != null) {
                fields.addAll(condition.accept(this));
            }
            // Order by
            for (OrderBy current : select.getOrderBy()) {
                fields.addAll(current.accept(this));
            }
            return fields;
        }

        public Collection<FieldMetadata> visit(NativeQuery nativeQuery) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(Condition condition) {
            return Collections.emptySet();
        }

        @Override
        public Collection<FieldMetadata> visit(Max max) {
            return max.getExpression().accept(this);
        }

        @Override
        public Collection<FieldMetadata> visit(Min min) {
            return min.getExpression().accept(this);
        }

        public Collection<FieldMetadata> visit(Compare condition) {
            Collection<FieldMetadata> fields = new HashSet<FieldMetadata>();
            fields.addAll(condition.getLeft().accept(this));
            fields.addAll(condition.getRight().accept(this));
            return fields;
        }

        public Collection<FieldMetadata> visit(BinaryLogicOperator condition) {
            Collection<FieldMetadata> fields = new HashSet<FieldMetadata>();
            fields.addAll(condition.getLeft().accept(this));
            fields.addAll(condition.getRight().accept(this));
            return fields;
        }

        public Collection<FieldMetadata> visit(UnaryLogicOperator condition) {
            return condition.getCondition().accept(this);
        }

        public Collection<FieldMetadata> visit(Range range) {
            return range.getExpression().accept(this);
        }

        @Override
        public Collection<FieldMetadata> visit(ConstantCondition constantCondition) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(Timestamp timestamp) {
            // TODO Should indicate that timestamp needs index
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(TaskId taskId) {
            // TODO Should indicate that task id needs index
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(Type type) {
            // TODO Should indicate that class name needs index
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(Distinct distinct) {
            return distinct.getExpression().accept(this);
        }

        public Collection<FieldMetadata> visit(StagingStatus stagingStatus) {
            // TODO Should indicate that staging status needs index
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(StagingError stagingError) {
            // TODO Should indicate that staging error needs index
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(StagingSource stagingSource) {
            // TODO Should indicate that staging source needs index
            return Collections.emptySet();
        }

        @Override
        public Collection<FieldMetadata> visit(StagingBlockKey stagingBlockKey) {
            // TODO Should indicate that staging block key needs index
            return Collections.emptySet();
        }

        @Override
        public Collection<FieldMetadata> visit(GroupSize groupSize) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(Join join) {
            List<FieldMetadata> fields = Arrays.asList(join.getLeftField().getFieldMetadata(),
                    join.getRightField().getFieldMetadata());
            return new HashSet<FieldMetadata>(fields);
        }

        public Collection<FieldMetadata> visit(Expression expression) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(Predicate predicate) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(Field field) {
            FieldMetadata fieldMetadata = field.getFieldMetadata();
            return fieldMetadata.accept(new DefaultMetadataVisitor<Collection<FieldMetadata>>() {

                @Override
                public Collection<FieldMetadata> visit(ContainedTypeFieldMetadata containedField) {
                    return Collections.emptySet();
                }

                public Collection<FieldMetadata> visit(FieldMetadata fieldMetadata) {
                    return fieldMetadata.accept(this);
                }

                public Collection<FieldMetadata> visit(SimpleTypeFieldMetadata simpleField) {
                    return Collections.<FieldMetadata>singleton(simpleField);
                }

                public Collection<FieldMetadata> visit(EnumerationFieldMetadata enumField) {
                    return Collections.<FieldMetadata>singleton(enumField);
                }

                public Collection<FieldMetadata> visit(ReferenceFieldMetadata referenceField) {
                    return new HashSet<FieldMetadata>(Arrays.asList(referenceField, referenceField.getReferencedField()));
                }
            });
        }

        public Collection<FieldMetadata> visit(Alias alias) {
            return alias.getTypedExpression().accept(this);
        }

        public Collection<FieldMetadata> visit(Id id) {
            return id.getType().getKeyFields();
        }

        @Override
        public Collection<FieldMetadata> visit(ConstantCollection collection) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(StringConstant constant) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(IntegerConstant constant) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(DateConstant constant) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(DateTimeConstant constant) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(BooleanConstant constant) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(BigDecimalConstant constant) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(TimeConstant constant) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(ShortConstant constant) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(ByteConstant constant) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(LongConstant constant) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(DoubleConstant constant) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(FloatConstant constant) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(IsEmpty isEmpty) {
            return isEmpty.getField().accept(this);
        }

        public Collection<FieldMetadata> visit(NotIsEmpty notIsEmpty) {
            return notIsEmpty.getField().accept(this);
        }

        public Collection<FieldMetadata> visit(IsNull isNull) {
            return isNull.getField().accept(this);
        }

        public Collection<FieldMetadata> visit(NotIsNull notIsNull) {
            return notIsNull.getField().accept(this);
        }

        public Collection<FieldMetadata> visit(OrderBy orderBy) {
            return orderBy.getExpression().accept(this);
        }

        public Collection<FieldMetadata> visit(Paging paging) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(Count count) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(FullText fullText) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(Isa isa) {
            // TODO Index the class name?
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(ComplexTypeExpression expression) {
            return Collections.emptySet();
        }

        public Collection<FieldMetadata> visit(IndexedField indexedField) {
            return Collections.singleton(indexedField.getFieldMetadata());
        }

        @Override
        public Collection<FieldMetadata> visit(FieldFullText fieldFullText) {
            return fieldFullText.getField().accept(this);
        }

        @Override
        public Collection<FieldMetadata> visit(At at) {
            return Collections.emptySet();
        }
    }
}
