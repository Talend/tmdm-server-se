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

import static com.amalto.core.query.user.UserQueryBuilder.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import com.amalto.core.query.user.metadata.*;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;

/**
 * A utility to slice a query into time slots (based on timestamp).
 */
public class TimeSlicer {

    private TimeSlicer() {
    }

    /**
     * <p>
     * "Slices" the <code>expression</code> into smaller queries depending on last modification timestamp. Each slice
     * contains all records for which last modification time was within same slice. For example:
     * </p>
     * <code>
     *     TimeSlicer.slice(expression, storage, 10, UserQueryBuilder.timestamp())
     * </code>
     * <p>
     * returns a {@link java.util.Iterator iterator} of 10 {@link com.amalto.core.query.user.Expression expressions}.
     * Each expression navigate through the minimum of timestamp to its maximum. In this example, each slice covers a
     * time of ((max(timestamp) - min(timestamp)) / 10) milliseconds.
     * </p>
     * 
     * @param expression The original expression to slice into queries on timestamp ranges.
     * @param storage Use to get the last modified record time (upper bound for timestamp) and the less modified record
     * (lower bound for timestamp), <b>IF</b> lower bounds or upper bounds were not in <code>expression</code>.
     * @param maxSlice The maximum number of slices to expect in the result. If lower of equals to 0, behaves as if 0
     * was passed as value, meaning no slice returned.
     * @param timestamp The field to be used for time slicing (must store times in milliseconds).
     * @return A {@link java.util.Iterator iterator} with <b>at most</b> <code>maxSlice</code> slices
     * {@link com.amalto.core.query.user.TimeSlicer.Slice slices} created based on <code>expression</code>.
     */
    public static Iterator<Slice> slice(Expression expression, Storage storage, int maxSlice, TypedExpression timestamp) {
        if (expression == null) {
            return null;
        }
        if (storage == null) {
            throw new IllegalArgumentException("Storage cannot be null");
        }
        if (maxSlice <= 0) {
            return Collections.<Slice>emptySet().iterator();
        }
        if (!(expression instanceof Select)) {
            Slice slice = new Slice(Long.MAX_VALUE, 0, expression);
            return Collections.singletonList(slice).iterator();
        }
        // Get the main type
        Select select = (Select) expression;
        ComplexTypeMetadata mainType = select.getTypes().get(0);
        Condition condition = select.getCondition();
        // Try to get upper and lower bound from query
        Long upperBound = getUpperBound(storage, timestamp, mainType, condition);
        Long lowerBound = getLowerBound(storage, timestamp, mainType, condition);
        // Create the slice iterator
        long step = (upperBound - lowerBound + maxSlice) / maxSlice;
        return getSliceIterator(step, TimeUnit.MILLISECONDS, timestamp, select, lowerBound, upperBound);
    }

    /**
     * <p>
     * "Slices" the <code>expression</code> into smaller queries depending on last modification timestamp. Each slice
     * contains all records for which last modification time was within <code>step</code>. For example:
     * </p>
     * <code>
     *     TimeSlicer.slice(expression, storage, 1, TimeUnit.HOURS)
     * </code>
     * <p>
     * returns a {@link java.util.Iterator iterator} of {@link com.amalto.core.query.user.Expression expressions} that
     * groups modifications done within the same hour.
     * </p>
     * <p>
     * This method uses the default timestamp property {@link UserQueryBuilder#timestamp()}.
     * </p>
     * 
     * @param expression The original expression to slice into queries on timestamp ranges.
     * @param storage Use to get the last modified record time (upper bound for timestamp) and the less modified record
     * (lower bound for timestamp).
     * @param step Indicates how long a slice can be (this is a maximum, last slice might be smaller). Parameter is
     * evaluated with <code>unit</code>.
     * @param unit The {@link java.util.concurrent.TimeUnit unit} for the <code>step</code>.
     * @return A {@link java.util.Iterator iterator} to all {@link com.amalto.core.query.user.TimeSlicer.Slice slices}
     * created based on <code>expression</code>.
     */
    public static Iterator<Slice> slice(Expression expression, Storage storage, long step, TimeUnit unit) {
        return slice(expression, storage, step, unit, timestamp());
    }

    /**
     * <p>
     * "Slices" the <code>expression</code> into smaller queries depending on last modification timestamp. Each slice
     * contains all records for which last modification time was within <code>step</code>. For example:
     * </p>
     * <code>
     *     TimeSlicer.slice(expression, storage, 1, TimeUnit.HOURS, timestamp())
     * </code>
     * <p>
     * returns a {@link java.util.Iterator iterator} of {@link com.amalto.core.query.user.Expression expressions} that
     * groups modifications done within the same hour, using the built-in timestamp value.
     * </p>
     * 
     * @param expression The original expression to slice into queries on timestamp ranges.
     * @param storage Use to get the last modified record time (upper bound for timestamp) and the less modified record
     * (lower bound for timestamp).
     * @param step Indicates how long a slice can be (this is a maximum, last slice might be smaller). Parameter is
     * evaluated with <code>unit</code>.
     * @param unit The {@link java.util.concurrent.TimeUnit unit} for the <code>step</code>.
     * @param timestamp The field to be used for time slicing (must store times in milliseconds).
     * @return A {@link java.util.Iterator iterator} to all {@link com.amalto.core.query.user.TimeSlicer.Slice slices}
     * created based on <code>expression</code>.
     */
    public static Iterator<Slice> slice(Expression expression, Storage storage, long step, TimeUnit unit,
            TypedExpression timestamp) {
        if (expression == null) {
            return Collections.<Slice>emptySet().iterator();
        }
        if(storage == null) {
            throw new IllegalArgumentException("Storage cannot be null");
        }
        if (!(expression instanceof Select)) {
            Slice slice = new Slice(Long.MAX_VALUE, 0, expression);
            return Collections.singletonList(slice).iterator();
        }
        // Get the main type
        Select select = (Select) expression;
        ComplexTypeMetadata mainType = select.getTypes().get(0);
        Condition condition = select.getCondition();
        // Try to get upper and lower bound from query
        Long upperBound = getUpperBound(storage, timestamp, mainType, condition);
        Long lowerBound = getLowerBound(storage, timestamp, mainType, condition);
        // Create the slice iterator
        return getSliceIterator(step, unit, timestamp, select, lowerBound, upperBound);
    }

    private static Iterator<Slice> getSliceIterator(long step, TimeUnit unit, TypedExpression timestamp, Select select,
            Long lowerBound, Long upperBound) {
        if (upperBound - lowerBound <= step) {
            // Means there's only one slice of result (no need to iterate over ranges).
            Slice singleSlice = new Slice(lowerBound, upperBound, select);
            return Collections.singleton(singleSlice).iterator();
        } else {
            // There's at least 2 slices to iterate over.
            return new SliceIterator(lowerBound, upperBound, unit.toMillis(step), select, timestamp);
        }
    }

    private static Long getLowerBound(Storage storage, TypedExpression timestamp, ComplexTypeMetadata mainType,
            Condition condition) {
        Long lowerBound = null;
        // Read lowerBound from query
        if (condition != null) {
            lowerBound = condition.accept(new TimestampBoundFinder(timestamp, TimestampBoundFinder.Type.LOWER));
        }
        // Read lowerBound from storage
        if (lowerBound == null) {
            UserQueryBuilder lowerBoundQuery = from(mainType).select(min(timestamp)).limit(1);
            if (condition != null) {
                lowerBoundQuery.where(condition);
            }
            StorageResults lowerBoundResult = storage.fetch(lowerBoundQuery.getSelect());
            try {
                Long min = (Long) lowerBoundResult.iterator().next().get("min"); //$NON-NLS-1$
                lowerBound = min == null ? 0 : min;
            } finally {
                lowerBoundResult.close();
            }
        }
        return lowerBound;
    }

    private static Long getUpperBound(Storage storage, TypedExpression timestamp, ComplexTypeMetadata mainType,
            Condition condition) {
        Long upperBound = null;
        // Read upperBound from storage
        if (condition != null) {
            upperBound = condition.accept(new TimestampBoundFinder(timestamp, TimestampBoundFinder.Type.UPPER));
        }
        // Read upperBound from storage
        if (upperBound == null) {
            UserQueryBuilder upperBoundQuery = from(mainType).select(max(timestamp)).limit(1);
            if (condition != null) {
                upperBoundQuery.where(condition);
            }
            StorageResults upperBoundResult = storage.fetch(upperBoundQuery.getSelect());
            try {
                Long max = (Long) upperBoundResult.iterator().next().get("max"); //$NON-NLS-1$
                upperBound = max == null ? 0 : max;
            } finally {
                upperBoundResult.close();
            }
        }
        return upperBound;
    }

    /**
     * Represents a slice of the original query: the <code>expression</code> to retrieve the records, the lower and
     * upper bound for the slice.
     */
    public static class Slice {

        private final Expression expression;

        private final long sliceLowerBound;

        private final long sliceUpperBound;

        public Slice(long sliceLowerBound, long sliceUpperBound, Expression expression) {
            this.sliceUpperBound = sliceUpperBound;
            this.sliceLowerBound = sliceLowerBound;
            this.expression = expression;
        }

        public Expression getExpression() {
            return expression;
        }

        public long getLowerBound() {
            return sliceLowerBound;
        }

        public long getUpperBound() {
            return sliceUpperBound;
        }
    }

    private static class SliceIterator implements Iterator<Slice> {

        private final long upperBound;

        private final Select select;

        private final TypedExpression timestamp;

        private final long step;

        private long currentTimeStamp = 0;

        public SliceIterator(long lowerBound, long upperBound, long step, Select select, TypedExpression timestamp) {
            this.currentTimeStamp = lowerBound;
            this.upperBound = upperBound;
            this.step = step;
            this.select = select;
            this.timestamp = timestamp;
        }

        @Override
        public boolean hasNext() {
            return currentTimeStamp < upperBound;
        }

        @Override
        public Slice next() {
            if (currentTimeStamp > upperBound) {
                throw new NoSuchElementException("No more step for timestamp.");
            }
            Slice slice;
            try {
                Select copy = select.copy();
                Condition previousCondition = copy.getCondition();
                long sliceUpperBound = currentTimeStamp + step;
                Condition rangeCondition = and(gte(timestamp, String.valueOf(currentTimeStamp)),
                        lt(timestamp, String.valueOf(sliceUpperBound)));
                if (previousCondition == null) {
                    copy.setCondition(rangeCondition);
                } else {
                    copy.setCondition(and(previousCondition, rangeCondition));
                }
                slice = new Slice(currentTimeStamp, sliceUpperBound - 1, copy);
            } finally {
                currentTimeStamp += step;
            }
            return slice;
        }

        @Override
        public void remove() {
            currentTimeStamp += step;
        }
    }

    private static class TimestampBoundFinder extends VisitorAdapter<Long> {

        private final TypedExpression timestamp;

        private final Type type;

        private boolean isTimestamp = false;

        public TimestampBoundFinder(TypedExpression timestamp, Type type) {
            this.timestamp = timestamp;
            this.type = type;
        }

        @Override
        public Long visit(ConstantCondition constantCondition) {
            return null;
        }

        @Override
        public Long visit(Range range) {
            return null;
        }

        @Override
        public Long visit(GroupSize groupSize) {
            return null;
        }

        @Override
        public Long visit(IsEmpty isEmpty) {
            return null;
        }

        @Override
        public Long visit(NotIsEmpty notIsEmpty) {
            return null;
        }

        @Override
        public Long visit(IsNull isNull) {
            return null;
        }

        @Override
        public Long visit(NotIsNull notIsNull) {
            return null;
        }

        @Override
        public Long visit(FullText fullText) {
            return null;
        }

        @Override
        public Long visit(FieldFullText fieldFullText) {
            return null;
        }

        @Override
        public Long visit(Isa isa) {
            return null;
        }

        @Override
        public Long visit(StagingStatus stagingStatus) {
            return null;
        }

        @Override
        public Long visit(StagingError stagingError) {
            return null;
        }

        @Override
        public Long visit(StagingSource stagingSource) {
            return null;
        }

        @Override
        public Long visit(StagingBlockKey stagingBlockKey) {
            return null;
        }

        @Override
        public Long visit(StagingHasTask stagingHasTask) {
            return null;
        }

        @Override
        public Long visit(Field field) {
            if (field.equals(timestamp)) {
                isTimestamp = true;
            }
            return null;
        }

        @Override
        public Long visit(Timestamp timestamp) {
            if (timestamp.equals(this.timestamp)) {
                isTimestamp = true;
            }
            return null;
        }

        @Override
        public Long visit(Compare condition) {
            condition.getLeft().accept(this);
            if (isTimestamp) {
                Expression right = condition.getRight();
                if (right instanceof ConstantExpression) {
                    Object valueObject = ((ConstantExpression) right).getValue();
                    Long value;
                    try {
                        value = Long.parseLong(String.valueOf(valueObject));
                    } catch (NumberFormatException e) {
                        // Ignore: value is not a long after all.
                        return null;
                    }
                    switch (type) {
                    case LOWER:
                        if (condition.getPredicate() == Predicate.GREATER_THAN_OR_EQUALS
                                || condition.getPredicate() == Predicate.GREATER_THAN) {
                            return value;
                        }
                        break;
                    case UPPER:
                        if (condition.getPredicate() == Predicate.LOWER_THAN_OR_EQUALS
                                || condition.getPredicate() == Predicate.LOWER_THAN) {
                            return value;
                        }
                        break;
                    }
                    return null;
                }
            }
            return null;
        }

        @Override
        public Long visit(BinaryLogicOperator condition) {
            Long value = condition.getLeft().accept(this);
            if (value != null) {
                return value;
            } else {
                return condition.getRight().accept(this);
            }
        }

        @Override
        public Long visit(UnaryLogicOperator condition) {
            return condition.getCondition().accept(this);
        }

        static enum Type {
            LOWER,
            UPPER
        }
    }
}
