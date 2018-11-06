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

import com.amalto.core.query.user.metadata.MetadataField;
import com.amalto.core.query.user.metadata.TaskId;
import com.amalto.core.query.user.metadata.Timestamp;
import com.amalto.core.storage.record.CommonStorageMetaDataUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

public class UserQueryBuilder {

    public static final String ID_FIELD = "../../i"; //$NON-NLS-1$

    public static final String ID_ALIAS = "i"; //$NON-NLS-1$
    
    public static final String ALL_FIELD = "../*";  //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(UserQueryBuilder.class);

    private final Expression expression;

    private UserQueryBuilder(Expression expression) {
        this.expression = expression;
    }

    private Select expressionAsSelect() {
        return ((Select) expression);
    }

    public static Condition and(Condition left, Condition right) {
        assertConditionsArguments(left, right);
        return new BinaryLogicOperator(left, Predicate.AND, right);
    }

    public static Condition not(Condition condition) {
        return new UnaryLogicOperator(condition, Predicate.NOT);
    }

    public static Condition or(Condition left, Condition right) {
        assertConditionsArguments(left, right);
        return new BinaryLogicOperator(left, Predicate.OR, right);
    }

    public static TypedExpression index(FieldMetadata field, int index) {
        return new IndexedField(field, index);
    }

    public static Condition startsWith(FieldMetadata field, String constant) {
        assertValueConditionArguments(field, constant);
        Field userField = new Field(field);
        return startsWith(userField, constant);
    }

    public static Condition startsWith(TypedExpression field, String constant) {
        assertValueConditionArguments(field, constant);
        if (constant.charAt(0) == '^') {
            constant = constant.substring(1);
        }
        return new Compare(field, Predicate.STARTS_WITH, createConstant(field, constant));
    }

    public static TypedExpression max(FieldMetadata field) {
        assertNullField(field);
        Field userField = new Field(field);
        return max(userField);
    }

    public static TypedExpression max(TypedExpression typedExpression) {
        assertNullField(typedExpression);
        return new Alias(new Max(typedExpression), "max"); //$NON-NLS-1$
    }

    public static TypedExpression min(FieldMetadata field) {
        assertNullField(field);
        Field userField = new Field(field);
        return min(userField);
    }

    public static TypedExpression min(TypedExpression typedExpression) {
        assertNullField(typedExpression);
        return new Alias(new Min(typedExpression), "min"); //$NON-NLS-1$
    }

    public static Condition gt(FieldMetadata field, String constant) {
        assertValueConditionArguments(field, constant);
        Field userField = new Field(field);
        return gt(userField, constant);
    }

    public static Condition gt(TypedExpression expression, String constant) {
        assertValueConditionArguments(expression, constant);
        return new Compare(expression, Predicate.GREATER_THAN, createConstant(expression, constant));
    }
    
    public static Condition gt(TypedExpression expression, FieldMetadata field) {
        return new Compare(expression, Predicate.GREATER_THAN, new Field(field));
    }

    public static Condition gte(FieldMetadata field, String constant) {
        assertValueConditionArguments(field, constant);
        Field userField = new Field(field);
        return gte(userField, constant);
    }

    public static Condition gte(TypedExpression expression, String constant) {
        assertValueConditionArguments(expression, constant);
        return new Compare(expression, Predicate.GREATER_THAN_OR_EQUALS, createConstant(expression, constant));
    }

    public static Compare lt(FieldMetadata field, String constant) {
        assertValueConditionArguments(field, constant);
        Field userField = new Field(field);
        return lt(userField, constant);
    }

    public static Compare lt(TypedExpression expression, String constant) {
        return new Compare(expression, Predicate.LOWER_THAN, createConstant(expression, constant));
    }

    public static Compare lte(FieldMetadata field, String constant) {
        assertValueConditionArguments(field, constant);
        Field userField = new Field(field);
        return lte(userField, constant);
    }

    public static Compare lte(TypedExpression expression, String constant) {
        return new Compare(expression, Predicate.LOWER_THAN_OR_EQUALS, createConstant(expression, constant));
    }

    public static Compare lte(FieldMetadata left, FieldMetadata right) {
        return new Compare(new Field(left), Predicate.LOWER_THAN_OR_EQUALS, new Field(right));
    }

    public static Compare eq(FieldMetadata left, String... values) {
        if (values == null) {
            throw new IllegalArgumentException("Values can not be null.");
        }
        Field leftExpression = new Field(left);
        TypedExpression[] constants = new TypedExpression[values.length];
        int i = 0;
        for (String value : values) {
            constants[i++] = createConstant(leftExpression, value);
        }
        return new Compare(leftExpression, Predicate.EQUALS, new ConstantCollection(constants));
    }

    public static Compare eq(FieldMetadata left, FieldMetadata right) {
        return new Compare(new Field(left), Predicate.EQUALS, new Field(right));
    }

    public static Condition eq(TypedExpression expression, String constant) {
        assertNullField(expression);
        if (expression instanceof Field) {
            return eq(((Field) expression), constant);
        } else {
            if (constant == null) {
                return isNull(expression);
            }
            return new Compare(expression, Predicate.EQUALS, createConstant(expression, constant));
        }
    }

    public static Condition eq(FieldMetadata left, TypedExpression right) {
        return new Compare(new Field(left), Predicate.EQUALS, right);
    }

    public static Condition eq(TypedExpression left, TypedExpression right) {
        return new Compare(left, Predicate.EQUALS, right);
    }

    public static Condition eq(FieldMetadata field, String constant) {
        assertNullField(field);
        Field userField = new Field(field);
        if (CommonStorageMetaDataUtils.isValueAssignable(constant, field)) {
            return eq(userField, constant);
        } else {
            return UserQueryHelper.FALSE;
        }
    }

    public static Condition eq(Field field, String constant) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }
        if (constant == null) {
            return isNull(field);
        }
        assertValueConditionArguments(field, constant);
        if (!CommonStorageMetaDataUtils.isValueAssignable(constant, field.getFieldMetadata())) {
            return UserQueryHelper.FALSE;
        }
        if (field.getFieldMetadata() instanceof ReferenceFieldMetadata) {
            ReferenceFieldMetadata fieldMetadata = (ReferenceFieldMetadata) field.getFieldMetadata();
            return new Compare(field, Predicate.EQUALS, new Id(fieldMetadata.getReferencedType(), constant));
        } else {
            return new Compare(field, Predicate.EQUALS, createConstant(field, constant));
        }
    }

    public static Condition in(TypedExpression left, TypedExpression right) {
        return new Compare(left, Predicate.IN, right);
    }

    public static Condition in(TypedExpression expression, String constant) {
        assertNullField(expression);
        if (expression instanceof Field) {
            return in(((Field) expression), constant);
        } else {
            if (constant == null) {
                return isNull(expression);
            }
            return new Compare(expression, Predicate.IN, createConstant(expression, constant));
        }
    }

    @SuppressWarnings("rawtypes")
    public static Condition in(TypedExpression expression, List constant) {
        assertNullField(expression);
        if (expression instanceof Field) {
            return in(((Field) expression), constant);
        } else {
            if (constant == null) {
                return isNull(expression);
            }
            return new Compare(expression, Predicate.IN, createConstant(expression, constant));
        }
    }

    public static Condition in(FieldMetadata field, String constant) {
        assertNullField(field);
        Field userField = new Field(field);
        if (CommonStorageMetaDataUtils.isValueAssignable(constant, field)) {
            return in(userField, constant);
        } else {
            return UserQueryHelper.FALSE;
        }
    }

    @SuppressWarnings("rawtypes")
    public static Condition in(FieldMetadata field, List constant) {
        assertNullField(field);
        Field userField = new Field(field);
        if (CommonStorageMetaDataUtils.isValueAssignable(constant, field)) {
            return in(userField, constant);
        } else {
            return UserQueryHelper.FALSE;
        }
    }

    public static Condition in(Field field, String constant) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null"); //$NON-NLS-1$
        }
        if (constant == null) {
            return isNull(field);
        }
        assertValueConditionArguments(field, constant);
        if (!CommonStorageMetaDataUtils.isValueAssignable(constant, field.getFieldMetadata())) {
            return UserQueryHelper.FALSE;
        }
        if (field.getFieldMetadata() instanceof ReferenceFieldMetadata) {
            ReferenceFieldMetadata fieldMetadata = (ReferenceFieldMetadata) field.getFieldMetadata();
            return new Compare(field, Predicate.IN, new Id(fieldMetadata.getReferencedType(), constant));
        } else {
            return new Compare(field, Predicate.IN, createConstant(field, constant));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Condition in(Field field, List constant) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null"); //$NON-NLS-1$
        }
        if (constant == null) {
            return isNull(field);
        }
        assertValueConditionArguments(field, constant);
        if (!CommonStorageMetaDataUtils.isValueAssignable(constant, field.getFieldMetadata())) {
            return UserQueryHelper.FALSE;
        }
        if (field.getFieldMetadata() instanceof ReferenceFieldMetadata) {
            ReferenceFieldMetadata fieldMetadata = (ReferenceFieldMetadata) field.getFieldMetadata();
            return new Compare(field, Predicate.IN, new Id(fieldMetadata.getReferencedType(), constant));
        } else {
            return new Compare(field, Predicate.IN, createConstant(field, constant));
        }
    }

    public static Condition isa(FieldMetadata field, ComplexTypeMetadata type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null."); //$NON-NLS-1$
        }
        if (field instanceof ReferenceFieldMetadata) {
            throw new IllegalArgumentException("Cannot perform type check on a foreign key."); //$NON-NLS-1$
        }
        // Get the matching type from the field definition (field uses a contained version of the type).
        ComplexTypeMetadata fieldType = type;
        if (!(fieldType instanceof ContainedComplexTypeMetadata)) {
            fieldType = (ComplexTypeMetadata) field.getType();
            if (!fieldType.getName().equals(type.getName())) {
                for (ComplexTypeMetadata subType : fieldType.getSubTypes()) {
                    if (subType.getName().equals(type.getName())) {
                        fieldType = subType;
                        break;
                    }
                }
            }
        }
        Condition current = new Isa(new Field(field), fieldType);
        if (!type.getSubTypes().isEmpty()) {
            for (ComplexTypeMetadata subType : type.getSubTypes()) {
                current = or(current, isa(field, subType));
            }
        }
        return current;
    }

    public UserQueryBuilder isa(ComplexTypeMetadata type) {
        if (expression == null || expressionAsSelect().getTypes().isEmpty()) {
            throw new IllegalStateException("No type is currently selected.");
        }
        ComplexTypeMetadata mainType = getSelect().getTypes().get(0);
        if (!type.isAssignableFrom(mainType)) {
            throw new IllegalArgumentException("Type '" + type.getName() + "' is not assignable from '" + mainType.getName() + "'.");
        } else if (!mainType.equals(type) || !mainType.getSubTypes().isEmpty()) {
            where(new Isa(new ComplexTypeExpression(mainType), type));
            return this;
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Ignore 'is a' statement of type '" + type.getName() + "' since it is not part of an " +
                        "inheritance tree OR a expression like 'type1 isa type1' was detected.");
            }
            return this;
        }
    }

    public static TypedExpression createConstant(TypedExpression expression, String constant) {
        String fieldTypeName = expression.getTypeName();
        if (Types.INTEGER.equals(fieldTypeName)
                || Types.POSITIVE_INTEGER.equals(fieldTypeName)
                || Types.NEGATIVE_INTEGER.equals(fieldTypeName)
                || Types.NON_POSITIVE_INTEGER.equals(fieldTypeName)
                || Types.NON_NEGATIVE_INTEGER.equals(fieldTypeName)
                || Types.UNSIGNED_INT.equals(fieldTypeName)
                || Types.INT.equals(fieldTypeName)) {
            if (constant.isEmpty()) {
                return new IntegerConstant(String.valueOf(0));
            } else {
                return new IntegerConstant(constant);
            }
        } else if (Types.STRING.equals(fieldTypeName)
                || Types.HEX_BINARY.equals(fieldTypeName)
                || Types.BASE64_BINARY.equals(fieldTypeName)
                || Types.ANY_URI.equals(fieldTypeName)
                || Types.QNAME.equals(fieldTypeName)
                || Types.DURATION.equals(fieldTypeName)) {
            return new StringConstant(constant);
        } else if (Types.DATE.equals(fieldTypeName)) {
            return new DateConstant(constant);
        } else if (Types.DATETIME.equals(fieldTypeName)) {
            return new DateTimeConstant(constant);
        } else if (Types.TIME.equals(fieldTypeName)) {
            return new TimeConstant(constant);
        } else if (Types.BOOLEAN.equals(fieldTypeName)) {
            return new BooleanConstant(constant);
        } else if (Types.DECIMAL.equals(fieldTypeName)) {
            return new BigDecimalConstant(constant);
        } else if (Types.SHORT.equals(fieldTypeName) || Types.UNSIGNED_SHORT.equals(fieldTypeName)) {
            return new ShortConstant(constant);
        } else if (Types.BYTE.equals(fieldTypeName) || Types.UNSIGNED_BYTE.equals(fieldTypeName)) {
            return new ByteConstant(constant);
        } else if (Types.LONG.equals(fieldTypeName) || Types.UNSIGNED_LONG.equals(fieldTypeName)) {
            return new LongConstant(constant);
        } else if (Types.DOUBLE.equals(fieldTypeName)) {
            return new DoubleConstant(constant);
        } else if (Types.FLOAT.equals(fieldTypeName)) {
            return new FloatConstant(constant);
        } else {
            throw new IllegalArgumentException("Cannot create expression constant for expression type '" + expression.getTypeName() + "' (is expression allowed to contain values?)");
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static TypedExpression createConstant(TypedExpression expression, List constant) {
        String fieldTypeName = expression.getTypeName();
        if (Types.INTEGER.equals(fieldTypeName) || Types.POSITIVE_INTEGER.equals(fieldTypeName)
                || Types.NEGATIVE_INTEGER.equals(fieldTypeName) || Types.NON_POSITIVE_INTEGER.equals(fieldTypeName)
                || Types.NON_NEGATIVE_INTEGER.equals(fieldTypeName) || Types.UNSIGNED_INT.equals(fieldTypeName)
                || Types.INT.equals(fieldTypeName)) {
            if (constant.isEmpty()) {
                throw new IllegalArgumentException("in query collection list is empty.");
            } else {
                CollectionUtils.transform(constant, new Transformer() {

                    public java.lang.Object transform(java.lang.Object input) {
                        if (input instanceof String) {
                            return new Integer((String) input);
                        } else {
                            return input;
                        }
                    }
                });
                return new IntegerConstant(constant);
            }
        } else if (Types.STRING.equals(fieldTypeName) || Types.HEX_BINARY.equals(fieldTypeName)
                || Types.BASE64_BINARY.equals(fieldTypeName) || Types.ANY_URI.equals(fieldTypeName)
                || Types.QNAME.equals(fieldTypeName) || Types.DURATION.equals(fieldTypeName)) {
            return new StringConstant(constant);
        } else if (Types.DATE.equals(fieldTypeName)) {
            CollectionUtils.transform(constant, new Transformer() {

                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        try {
                            return DateConstant.DATE_FORMAT.parse((String) input);
                        } catch (ParseException e) {
                            throw new IllegalArgumentException("Value '" + input.toString() + "' is not valid for Date");
                        }
                    } else {
                        return input;
                    }
                }
            });
            return new DateConstant(constant);
        } else if (Types.DATETIME.equals(fieldTypeName)) {
            CollectionUtils.transform(constant, new Transformer() {

                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        try {
                            return DateTimeConstant.DATE_FORMAT.parse((String) input);
                        } catch (ParseException e) {
                            throw new IllegalArgumentException("Value '" + input.toString() + "' is not valid for Date Time");
                        }
                    } else {
                        return input;
                    }

                }
            });
            return new DateTimeConstant(constant);
        } else if (Types.TIME.equals(fieldTypeName)) {
            CollectionUtils.transform(constant, new Transformer() {

                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        try {
                            return TimeConstant.TIME_FORMAT.parse((String) input);
                        } catch (ParseException e) {
                            throw new IllegalArgumentException("Value '" + input.toString() + "' is not valid for Time");
                        }
                    } else {
                        return input;
                    }
                }
            });
            return new TimeConstant(constant);
        } else if (Types.BOOLEAN.equals(fieldTypeName)) {
            CollectionUtils.transform(constant, new Transformer() {

                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        if ("0".equals(input.toString())) { //$NON-NLS-1$
                            return false;
                        } else if ("1".equals(input.toString())) { //$NON-NLS-1$
                            return true;
                        }
                        if (!"false".equalsIgnoreCase(input.toString()) && !"true".equalsIgnoreCase(input.toString())) { //$NON-NLS-1$ //$NON-NLS-2$
                            throw new IllegalArgumentException("Value '" + input.toString() + "' is not valid for boolean");
                        }
                        return Boolean.parseBoolean(input.toString());
                    } else {
                        return input;
                    }
                }
            });
            return new BooleanConstant(constant);
        } else if (Types.DECIMAL.equals(fieldTypeName)) {
            CollectionUtils.transform(constant, new Transformer() {

                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        return new BigDecimal((String) input);
                    } else {
                        return input;
                    }
                }
            });
            return new BigDecimalConstant(constant);
        } else if (Types.SHORT.equals(fieldTypeName) || Types.UNSIGNED_SHORT.equals(fieldTypeName)) {
            CollectionUtils.transform(constant, new Transformer() {

                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        return Short.parseShort((String) input);
                    } else {
                        return input;
                    }
                }
            });
            return new ShortConstant(constant);
        } else if (Types.BYTE.equals(fieldTypeName) || Types.UNSIGNED_BYTE.equals(fieldTypeName)) {
            CollectionUtils.transform(constant, new Transformer() {

                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        return Byte.parseByte((String) input);
                    } else {
                        return input;
                    }
                }
            });
            return new ByteConstant(constant);
        } else if (Types.LONG.equals(fieldTypeName) || Types.UNSIGNED_LONG.equals(fieldTypeName)) {
            CollectionUtils.transform(constant, new Transformer() {

                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        return Long.parseLong((String) input);
                    } else {
                        return input;
                    }
                }
            });
            return new LongConstant(constant);
        } else if (Types.DOUBLE.equals(fieldTypeName)) {
            CollectionUtils.transform(constant, new Transformer() {

                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        return Double.parseDouble((String) input);
                    } else {
                        return input;
                    }
                }
            });
            return new DoubleConstant(constant);
        } else if (Types.FLOAT.equals(fieldTypeName)) {
            CollectionUtils.transform(constant, new Transformer() {

                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        return Float.parseFloat((String) input);
                    } else {
                        return input;
                    }
                }
            });
            return new FloatConstant(constant);
        } else {
            throw new IllegalArgumentException("Cannot create expression constant for expression type '"
                    + expression.getTypeName() + "' (is expression allowed to contain values?)");
        }
    }

    public static Condition emptyOrNull(FieldMetadata field) {
        assertNullField(field);
        // TMDM-7700: IsEmpty on a FK field should be considered as IsNull
        if (!field.isMany() && field instanceof ReferenceFieldMetadata) {
            return new IsNull(new Field(field));
        }
        // Only do a isEmpty operator if field type is string, for all other known cases, isNull is enough.
        if (Types.STRING.equals(field.getType().getName())) {
            return new BinaryLogicOperator(isEmpty(field), Predicate.OR, isNull(field));
        } else {
            return isNull(field);
        }
    }

    public static Condition emptyOrNull(TypedExpression field) {
        assertNullField(field);
        if (field instanceof Type) {
            // TMDM-6831: Consider a "emptyOrNull(type)" as a "isa(field, actual_field_type)" (aka a restriction).
            FieldMetadata testedField = ((Type) field).getField().getFieldMetadata();
            return new Isa(new Field(testedField), ((ContainedTypeFieldMetadata) testedField).getContainedType());
        }
        // TMDM-7700: IsEmpty on a FK field should be considered as IsNull
        if (field instanceof Field) {
            FieldMetadata fieldMetadata = ((Field) field).getFieldMetadata();
            if (!fieldMetadata.isMany() && fieldMetadata instanceof ReferenceFieldMetadata) {
                return new IsNull(new Field(fieldMetadata));
            }
        }                
        // Only do a isEmpty operator if field type is string, for all other known cases, isNull is enough.
        if (Types.STRING.equals(field.getTypeName())) {
            return new BinaryLogicOperator(isEmpty(field), Predicate.OR, isNull(field));
        } else {
            return isNull(field);
        }
    }

    public static Condition isEmpty(FieldMetadata field) {
        assertNullField(field);
        // TMDM-7700: IsEmpty on a FK field should be considered as IsNull
        if (!field.isMany() && field instanceof ReferenceFieldMetadata) {
            return new IsNull(new Field(field));
        }
        // If field is a number field, consider a condition "field equals 0"
        String typeName = MetadataUtils.getSuperConcreteType(field.getType()).getName();
        for (String numberType : Types.NUMBERS) {
            if (numberType.equals(typeName)) {
                return new Compare(new Field(field), Predicate.EQUALS, new IntegerConstant(String.valueOf(0)));
            }
        }
        // For booleans, consider "field equals false"
        if (Types.BOOLEAN.equals(typeName)) {
            return new Compare(new Field(field), Predicate.EQUALS, new BooleanConstant(Boolean.FALSE.toString()));
        }
        // Dates
        for (String dateType : Types.DATES) {
            if (dateType.equals(typeName)) {
                return isNull(field);
            }
        }
        return new IsEmpty(new Field(field));
    }

    public static Condition isEmpty(TypedExpression typedExpression) {
        assertNullField(typedExpression);
        return or(new IsEmpty(typedExpression), isNull(typedExpression));
    }

    public static Condition isNull(FieldMetadata field) {
        assertNullField(field);
        if (field.isMany()) {
            // Consider is null on a repeatable field as "is empty".
            return isEmpty(field);
        } else {
            return new IsNull(new Field(field));
        }
    }

    public static Condition isNull(TypedExpression typedExpression) {
        assertNullField(typedExpression);
        if (typedExpression instanceof Field) {
            return isNull(((Field) typedExpression).getFieldMetadata());
        }
        return new IsNull(typedExpression);
    }

    public static Condition neq(FieldMetadata field, String constant) {
        assertValueConditionArguments(field, constant);
        return new UnaryLogicOperator(eq(field, constant), Predicate.NOT);
    }

    public static Condition neq(TypedExpression field, String constant) {
        assertValueConditionArguments(field, constant);
        return new UnaryLogicOperator(eq(field, constant), Predicate.NOT);
    }

    public static Condition fullText(String constant) {
        return new FullText(constant);
    }

    public static Condition fullText(FieldMetadata field, String constant) {
        if (field.getType() instanceof ComplexTypeMetadata) {
            // TMDM-5126: Full text on contained type -> perform search on all elements defined in type
            List<FieldMetadata> fields = field.getType().accept(new ContainedFieldFullTextSearch());
            if (fields.isEmpty()) {
                return UserQueryHelper.FALSE;
            } else {
                Iterator<FieldMetadata> iterator = fields.iterator();
                Condition condition = new FieldFullText(new Field(iterator.next()), constant);
                while (iterator.hasNext()) {
                    condition = or(condition, new FieldFullText(new Field(iterator.next()), constant));
                }
                return condition;
            }
        } else {
            return new FieldFullText(new Field(field), constant);
        }
    }

    public static TypedExpression count() {
        return new Alias(new Count(), "count"); //$NON-NLS-1$
    }

    public static TypedExpression count(FieldMetadata field) {
        return new Count(new Field(field));
    }

    public static TypedExpression timestamp() {
        return Timestamp.INSTANCE;
    }

    public static TypedExpression taskId() {
        return TaskId.INSTANCE;
    }

    public static UserQueryBuilder from(ComplexTypeMetadata type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        Select select = new Select();
        select.addType(type);
        return new UserQueryBuilder(select);
    }

    // TODO This declaration allows user to set where clauses on a native query: but where clause won't be processed in this case
    public static UserQueryBuilder from(String nativeQuery) {
        NativeQuery select = new NativeQuery(nativeQuery);
        return new UserQueryBuilder(select);
    }

    public UserQueryBuilder forUpdate() {
        expressionAsSelect().setForUpdate(true);
        return this;
    }

    public UserQueryBuilder cache() {
        expressionAsSelect().setCache(true);
        return this;
    }

    public UserQueryBuilder nocache() {
        expressionAsSelect().setCache(false);
        return this;
    }

    public UserQueryBuilder at(String dateTime) {
        // Parse date time
        long dateTimeAsLong;
        try {
            dateTimeAsLong = Long.parseLong(dateTime); // A long?
        } catch (NumberFormatException e) {
            // Try date format parsing
            try {
                Date date = DateTimeConstant.DATE_FORMAT.parse(dateTime); // Or maybe a XML date?
                dateTimeAsLong = date.getTime();
            } catch (ParseException e1) {
                throw new IllegalArgumentException("Date '" + dateTime + "' is neither a long nor a date time that can be parsed.", e1);
            }
        }
        // Select the history navigation information
        expressionAsSelect().setHistory(new At(dateTimeAsLong));
        return this;
    }

    public UserQueryBuilder swing(String swing) {
        // Before calling swing, at() should be called to give a fixed point in record history.
        At history = expressionAsSelect().getHistory();
        if (history == null) {
            throw new IllegalStateException("Can swing in record history: no date pivot was specified.");
        }
        // Set the 'swing' (i.e. where user wants to move in history).
        At.Swing swingValue;
        if (swing != null) {
            try {
                swingValue = At.Swing.valueOf(swing.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                StringBuilder builder = new StringBuilder();
                for (At.Swing allowedValue : At.Swing.values()) {
                    builder.append(allowedValue.name()).append(' ');
                }
                throw new RuntimeException("Value '" + swing + "' is not valid. Only ( " + builder + ") are.");
            }
        } else {
            swingValue = At.Swing.CLOSEST; // Default behavior
        }
        history.setSwing(swingValue);
        return this;
    }

    public UserQueryBuilder select(FieldMetadata... fields) {
        if (fields == null) {
            throw new IllegalArgumentException("Fields cannot be null");
        }
        for (FieldMetadata field : fields) {
            select(field);
        }
        return this;
    }

    public UserQueryBuilder select(FieldMetadata field) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }
        TypedExpression typedExpression;
        if (field instanceof ContainedTypeFieldMetadata) {
            // Selecting a field without value is equivalent to select "" (empty string) with an alias name equals to
            // selected field. (see MSQL-50)
            typedExpression = new Alias(new StringConstant(StringUtils.EMPTY), field.getName());
        } else {
            typedExpression = new Field(field);
        }
        expressionAsSelect().getSelectedFields().add(typedExpression);
        return this;
    }

    public UserQueryBuilder select(ComplexTypeMetadata type, String fieldName) {
        if (fieldName.startsWith("@")) { //$NON-NLS-1$
            fieldName = fieldName.substring(1); // TODO Not convincing: add a "Attribute" search capability?
        }
        if (type.hasField(fieldName)) {
            select(type.getField(fieldName));
        } else {
            MetadataField metadataField = MetadataField.Factory.getMetadataField(fieldName);
            if (metadataField != null) {
                select(metadataField.getProjectionExpression());
            } else if (ID_FIELD.equals(fieldName)) {
                for (FieldMetadata keyField : type.getKeyFields()) {
                    select(alias(keyField, ID_ALIAS));
                }
            } else {
                throw new IllegalArgumentException("Field '" + fieldName + "' is not supported.");
            }
        }
        return this;
    }

    /**
     * Adds a {@link Condition} to the {@link Select} built by this {@link UserQueryBuilder}. If this method has previously
     * been called, a logic "and"/"or" condition (depends on <code>predicate</code> argument) is created between the
     * existing condition {@link com.amalto.core.query.user.Select#getCondition()} and the <code>condition</code> parameter.
     *
     * @param condition A {@link Condition} to be added to the user query.
     * @param predicate Predicate to use to link existing condition with <code>condition</code>.
     * @return Same {@link UserQueryBuilder} for chained method calls.
     * @throws IllegalArgumentException If <code>condition</code> parameter is null.
     */
    public UserQueryBuilder where(Condition condition, Predicate predicate) {
        if (condition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        if (expressionAsSelect().getCondition() == null) {
            expressionAsSelect().setCondition(condition);
        } else {
            if (predicate == Predicate.OR) {
                expressionAsSelect().setCondition(or(expressionAsSelect().getCondition(), condition));
            } else if (predicate == Predicate.AND) {
                expressionAsSelect().setCondition(and(expressionAsSelect().getCondition(), condition));
            } else {
                throw new NotImplementedException("Not implemented: support of " + predicate);
            }
        }

        return this;
    }

    /**
     * <p>
     * Adds a {@link Condition} to the {@link Select} built by this {@link UserQueryBuilder}. If this method has previously
     * been called, a logic "and" condition is created between the existing condition {@link com.amalto.core.query.user.Select#getCondition()}
     * and the <code>condition</code> parameter.
     * </p>
     * <p>
     * This method is equivalent to:<br/>
     * <code>
     * Condition condition = ...<br/>
     * UserQueryBuilder qb = ...<br/>
     * qb.where(condition, Predicate.AND);<br/>
     * </code>
     * </p>
     *
     * @param condition A {@link Condition} to be added to the user query.
     * @return Same {@link UserQueryBuilder} for chained method calls.
     * @throws IllegalArgumentException If <code>condition</code> parameter is null.
     * @see #where(Condition, Predicate)
     */
    public UserQueryBuilder where(Condition condition) {
        where(condition, Predicate.AND);
        return this;
    }

    public UserQueryBuilder orderBy(FieldMetadata field, OrderBy.Direction direction) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }
        if (field instanceof ReferenceFieldMetadata) {
            // Order by a FK field is equivalent to a join on FK + a order by clause on referenced field.
            return join(field)
                    .orderBy(new Field(((ReferenceFieldMetadata) field).getReferencedField()), direction);
        } else {
            expressionAsSelect().addOrderBy(new OrderBy(new Field(field), direction));
        }
        return this;
    }

    public UserQueryBuilder orderBy(TypedExpression expression, OrderBy.Direction direction) {
        if (expression == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }
        if (expression instanceof Field) {
            orderBy(((Field) expression).getFieldMetadata(), direction);
        } else {
            expressionAsSelect().addOrderBy(new OrderBy(expression, direction));
        }
        return this;
    }

    public UserQueryBuilder join(TypedExpression leftField, FieldMetadata rightField) {
        if (!(leftField instanceof Field)) {
            throw new IllegalArgumentException("Can not perform join on a non-user field (was " + leftField.getClass().getName() + ")");
        }
        return join(((Field) leftField).getFieldMetadata(), rightField);
    }

    /**
     * <p>
     * Join a type's field with another.
     * </p>
     * <p>
     * If left field is a FK, use this method for Joins when right field is a simple PK (i.e. joined entity does not
     * have composite id). If this is the case, consider using {@link #join(FieldMetadata)}.
     * </p>
     *
     * @param leftField  The left field for the join operation.
     * @param rightField The right field for the join operation.
     * @return Same {@link UserQueryBuilder} for chained method calls.
     */
    public UserQueryBuilder join(FieldMetadata leftField, FieldMetadata rightField) {
        if (leftField == null) {
            throw new IllegalArgumentException("Left field cannot be null");
        }
        if (rightField == null) {
            throw new IllegalArgumentException("Right field cannot be null");
        }
        if (leftField instanceof ReferenceFieldMetadata) {
            FieldMetadata leftReferencedField = ((ReferenceFieldMetadata) leftField).getReferencedField();
            if (!leftReferencedField.equals(rightField)) {
                throw new IllegalArgumentException("Left field '" + leftReferencedField.getName() + "' is a FK, but right field isn't the one left is referring to.");
            }
        }
        Field leftUserField = new Field(leftField);
        Field rightUserField = new Field(rightField);
        // Implicit select joined type if it isn't already selected
        if (!expressionAsSelect().getTypes().contains(rightField.getContainingType())) {
            expressionAsSelect().addType(rightField.getContainingType());
        }
        JoinType joinType = leftField.isMandatory() ? JoinType.INNER : JoinType.LEFT_OUTER;
        expressionAsSelect().addJoin(new Join(leftUserField, rightUserField, joinType));
        return this;
    }

    /**
     * <p>
     * Join a type's field with another. This method expects field to be a {@link ReferenceFieldMetadata} and automatically
     * creates a Join between <code>field</code> parameter and the field(s) it targets.
     * </p>
     *
     * @param field The left field for the join operation.
     * @return Same {@link UserQueryBuilder} for chained method calls.
     * @throws IllegalArgumentException If <code>field</code> is not a {@link ReferenceFieldMetadata}.
     */
    public UserQueryBuilder join(FieldMetadata field) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }
        if (!(field instanceof ReferenceFieldMetadata)) {
            throw new IllegalArgumentException("Field must be a reference field.");
        }
        return join(field, ((ReferenceFieldMetadata) field).getReferencedField());
    }

    public UserQueryBuilder start(int start) {
        if (start < 0) {
            throw new IllegalArgumentException("Start index must be positive");
        }
        expressionAsSelect().getPaging().setStart(start);
        return this;
    }

    public UserQueryBuilder limit(int limit) {
        if (limit > 0) {
            // Only consider limit > 0 as worthy values.
            expressionAsSelect().getPaging().setLimit(limit);
        }
        return this;
    }

    public Select getSelect() {
        if (expression == null) {
            throw new IllegalStateException("No type has been selected");
        }
        return expressionAsSelect();
    }

    public Expression getExpression() {
        if (expression == null) {
            throw new IllegalStateException("No type has been selected");
        }
        return expression;
    }

    public UserQueryBuilder and(ComplexTypeMetadata type) {
        expressionAsSelect().addType(type);
        return this;
    }

    private static void assertConditionsArguments(Condition left, Condition right) {
        if (left == null) {
            throw new IllegalArgumentException("Left condition cannot be null");
        }
        if (right == null) {
            throw new IllegalArgumentException("Right condition cannot be null");
        }
    }

    private static void assertValueConditionArguments(Object field, String constant) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }
        if (constant == null) {
            throw new IllegalArgumentException("Constant cannot be null");
        }
    }

    @SuppressWarnings("rawtypes")
    private static void assertValueConditionArguments(Object field, Collection constant) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }
        if (constant == null) {
            throw new IllegalArgumentException("Constant cannot be null");
        }

        if (constant.isEmpty()) {
            throw new IllegalArgumentException("Constant cannot be empty");
        }
    }

    private static void assertNullField(Object field) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }
    }

    /**
     * Adds a {@link TypedExpression} the query should return. If the typed expression has already been selected, an
     * {@link Alias} is automatically created.
     *
     * @param expression Expression that represents a value to return in query results.
     * @return This instance for method call chaining.
     */
    public UserQueryBuilder select(TypedExpression expression) {
        Select select = expressionAsSelect();
        List<TypedExpression> selectedFields = select.getSelectedFields();
        if (!selectedFields.contains(expression)) {
            selectedFields.add(expression);
        } else {
            if (expression instanceof Field) {
                // TMDM-5022: Automatic alias if a field with same name was already selected.
                selectedFields.add(alias(expression, ((Field) expression).getFieldMetadata().getName()));
            } else {
                throw new UnsupportedOperationException("Can't select twice a non-field expression.");
            }
        }
        return this;
    }

    public static TypedExpression alias(FieldMetadata field, String alias) {
        return alias(new Field(field), alias);
    }

    public static TypedExpression alias(TypedExpression expression, String alias) {
        return new Alias(expression, alias);
    }

    public UserQueryBuilder selectId(ComplexTypeMetadata typeMetadata) {
        Collection<FieldMetadata> keyFields = typeMetadata.getKeyFields();
        if (keyFields.isEmpty()) {
            LOGGER.warn("Cannot select key field(s) for '" + typeMetadata + "' (no key defined in type).");
        }
        for (FieldMetadata keyField : keyFields) {
            select(keyField);
        }
        return this;
    }

    public UserQueryBuilder select(List<FieldMetadata> viewableFields) {
        if (viewableFields == null) {
            throw new IllegalArgumentException("Viewable fields cannot be null");
        }

        for (FieldMetadata viewableField : viewableFields) {
            select(viewableField);
        }
        return this;
    }

    public static Condition contains(FieldMetadata field, String value) {
        assertValueConditionArguments(field, value);
        if (value.isEmpty()) {
            return UserQueryHelper.TRUE;
        }
        Field userField = new Field(field);
        return contains(userField, value);
    }

    public static Condition contains(TypedExpression field, String value) {
        assertValueConditionArguments(field, value);
        if (!CommonStorageMetaDataUtils.isValueAssignable(value, field.getTypeName())) {
            return UserQueryHelper.FALSE;
        }
        Expression constant = createConstant(field, value);
        if (constant instanceof StringConstant) {
            return new Compare(field, Predicate.CONTAINS, constant);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Change CONTAINS to EQUALS for '" + field + "' (type: " + field.getTypeName() + ").");
            }
            return new Compare(field, Predicate.EQUALS, constant);
        }
    }

    public static TypedExpression type(FieldMetadata field) {
        if (!(field.getType() instanceof ComplexTypeMetadata)) {
            throw new IllegalArgumentException("Expected a complex type for field '" + field.getName() + "'.");
        }
        ComplexTypeMetadata fieldType = (ComplexTypeMetadata) field.getType();
        if (fieldType.getSubTypes().isEmpty()) {
            return new StringConstant(fieldType.getName());
        } else {
            return new Type(new Field(field));
        }
    }

    public static TypedExpression distinct(FieldMetadata field) {
        return distinct(new Field(field));
    }

    public static TypedExpression distinct(TypedExpression expression) {
        if (expression instanceof Field) {
            return new Alias(new Distinct(expression), ((Field) expression).getFieldMetadata().getPath());
        }
        return new Distinct(expression);
    }

    private static class ContainedFieldFullTextSearch extends DefaultMetadataVisitor<List<FieldMetadata>> {

        private final List<FieldMetadata> fields = new ArrayList<FieldMetadata>();

        @Override
        public List<FieldMetadata> visit(ComplexTypeMetadata complexType) {
            super.visit(complexType);
            return fields;
        }

        @Override
        public List<FieldMetadata> visit(ContainedComplexTypeMetadata containedType) {
            super.visit(containedType);
            return fields;
        }

        @Override
        public List<FieldMetadata> visit(ContainedTypeFieldMetadata containedField) {
            super.visit(containedField);
            return fields;
        }

        @Override
        public List<FieldMetadata> visit(ReferenceFieldMetadata referenceField) {
            return fields; // No search on FK fields.
        }

        @Override
        public List<FieldMetadata> visit(SimpleTypeFieldMetadata simpleField) {
            fields.add(simpleField);
            return fields;
        }

        @Override
        public List<FieldMetadata> visit(EnumerationFieldMetadata enumField) {
            fields.add(enumField);
            return fields;
        }

    }
}
