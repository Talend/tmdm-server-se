/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.query;

import com.amalto.core.query.user.Alias;
import com.amalto.core.query.user.At;
import com.amalto.core.query.user.BigDecimalConstant;
import com.amalto.core.query.user.BinaryLogicOperator;
import com.amalto.core.query.user.BooleanConstant;
import com.amalto.core.query.user.ByteConstant;
import com.amalto.core.query.user.Compare;
import com.amalto.core.query.user.ConstantCondition;
import com.amalto.core.query.user.ConstantExpression;
import com.amalto.core.query.user.Count;
import com.amalto.core.query.user.DateConstant;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.query.user.Distinct;
import com.amalto.core.query.user.DoubleConstant;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.Field;
import com.amalto.core.query.user.FieldFullText;
import com.amalto.core.query.user.FloatConstant;
import com.amalto.core.query.user.FullText;
import com.amalto.core.query.user.Id;
import com.amalto.core.query.user.IndexedField;
import com.amalto.core.query.user.IntegerConstant;
import com.amalto.core.query.user.IsEmpty;
import com.amalto.core.query.user.IsNull;
import com.amalto.core.query.user.Isa;
import com.amalto.core.query.user.Join;
import com.amalto.core.query.user.LongConstant;
import com.amalto.core.query.user.Max;
import com.amalto.core.query.user.Min;
import com.amalto.core.query.user.NotIsEmpty;
import com.amalto.core.query.user.NotIsNull;
import com.amalto.core.query.user.OrderBy;
import com.amalto.core.query.user.Paging;
import com.amalto.core.query.user.Predicate;
import com.amalto.core.query.user.Range;
import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.ShortConstant;
import com.amalto.core.query.user.StringConstant;
import com.amalto.core.query.user.TimeConstant;
import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.query.user.UnaryLogicOperator;
import com.amalto.core.query.user.VisitorAdapter;
import com.amalto.core.query.user.metadata.GroupSize;
import com.amalto.core.query.user.metadata.StagingBlockKey;
import com.amalto.core.query.user.metadata.StagingError;
import com.amalto.core.query.user.metadata.StagingSource;
import com.amalto.core.query.user.metadata.StagingStatus;
import com.amalto.core.query.user.metadata.TaskId;
import com.amalto.core.query.user.metadata.Timestamp;
import com.amalto.core.util.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang.NotImplementedException;
import org.talend.mdm.commmon.metadata.FieldMetadata;

/**
 * This {@link com.amalto.core.query.user.Visitor} implementation transforms an {@link com.amalto.core.query.user.Expression}
 * into JSON that can be then processed by a {@link QueryParser}.
 *
 * @see #toJson(Expression)
 */
public class UserQueryJsonSerializer extends VisitorAdapter<JsonElement> {

    /**
     * @see #toJson(Expression)
     */
    private UserQueryJsonSerializer() {
    }

    /**
     * Converts the <code>expression</code> to a JSON that can be used as input for {@link QueryParser}.
     *
     * @param expression A query expressed with a {@link Expression}.
     * @return A String containing a JSON that can be used as input of {@link QueryParser}.
     * @see com.amalto.core.query.user.UserQueryBuilder
     */
    public static String toJson(Expression expression) {
        final JsonElement jsonElement = expression.accept(new UserQueryJsonSerializer());
        return jsonElement.toString();
    }

    private static String toPath(Field field) {
        return field.getFieldMetadata().getContainingType().getName() + '/' + field.getFieldMetadata().getName();
    }

    private static JsonElement toConstant(ConstantExpression constantExpression) {
        final JsonObject valueObject = new JsonObject();
        valueObject.add("value", new JsonPrimitive(String.valueOf(constantExpression.getValue())));
        return valueObject;
    }

    @Override
    public JsonElement visit(Select select) {
        JsonObject selectContent = new JsonObject();

        // Generate from clause
        final JsonArray types = new JsonArray();
        select.getTypes().forEach(type -> types.add(new JsonPrimitive(type.getName())));
        selectContent.add("from", types);

        // Generate fields clause
        if (!select.getSelectedFields().isEmpty()) {
            final JsonArray selectedFields = new JsonArray();
            for (TypedExpression selectedField : select.getSelectedFields()) {
                selectedFields.add(selectedField.accept(this));
            }
            selectContent.add("fields", selectedFields);
        }

        // Generate where clause
        if (select.getCondition() != null) {
            selectContent.add("where", select.getCondition().accept(this));
        }

        // Generate join clause
        if (!select.getJoins().isEmpty()) {
            final JsonArray joinContent = new JsonArray();
            for (Join join : select.getJoins()) {
                joinContent.add(join.accept(this));
            }
            selectContent.add("joins", joinContent);
        }

        // Start and limit
        final Paging paging = select.getPaging();
        if (paging != null) {
            if (paging.getStart() != 0) {
                selectContent.add("start", new JsonPrimitive(paging.getStart()));
            }
            if (paging.getLimit() != Integer.MAX_VALUE) {
                selectContent.add("limit", new JsonPrimitive(paging.getLimit()));
            }
        }

        // Order by
        if (!select.getOrderBy().isEmpty()) {
            final JsonArray orderBysContent = new JsonArray();
            for (OrderBy orderBy : select.getOrderBy()) {
                orderBysContent.add(orderBy.accept(this));
            }
            selectContent.add("order_bys", orderBysContent);
        }

        // Cache
        if (select.cache()) {
            selectContent.add("cache", new JsonPrimitive("true"));
        }

        // Generate the top level select
        JsonObject selectObject = new JsonObject();
        selectObject.add("select", selectContent);

        return selectObject;
    }

    @Override
    public JsonElement visit(Max max) {
        JsonObject maxObject = new JsonObject();
        maxObject.add("max", max.getExpression().accept(this));
        return maxObject;
    }

    @Override
    public JsonElement visit(Min min) {
        JsonObject minObject = new JsonObject();
        minObject.add("min", min.getExpression().accept(this));
        return minObject;
    }

    @Override
    public JsonElement visit(Compare condition) {
        final JsonArray compareObjectContent = new JsonArray();
        compareObjectContent.add(condition.getLeft().accept(this));
        compareObjectContent.add(condition.getRight().accept(this));

        final JsonObject compareObject = new JsonObject();
        final Predicate predicate = condition.getPredicate();
        if (predicate == Predicate.CONTAINS) {
            compareObject.add("contains", compareObjectContent);
        } else if (predicate == Predicate.EQUALS) {
            compareObject.add("eq", compareObjectContent);
        } else if (predicate == Predicate.GREATER_THAN) {
            compareObject.add("gt", compareObjectContent);
        } else if (predicate == Predicate.GREATER_THAN_OR_EQUALS) {
            compareObject.add("gte", compareObjectContent);
        } else if (predicate == Predicate.LOWER_THAN) {
            compareObject.add("lt", compareObjectContent);
        } else if (predicate == Predicate.LOWER_THAN_OR_EQUALS) {
            compareObject.add("lte", compareObjectContent);
        } else if (predicate == Predicate.STARTS_WITH) {
            compareObject.add("startsWith", compareObjectContent);
        }

        return compareObject;
    }

    @Override
    public JsonElement visit(BinaryLogicOperator condition) {
        final Predicate predicate = condition.getPredicate();
        final JsonObject object = new JsonObject();

        final JsonArray value = new JsonArray();
        value.add(condition.getLeft().accept(this));
        value.add(condition.getRight().accept(this));
        if (predicate == Predicate.AND) {
            object.add("and", value);
        } else if (predicate == Predicate.OR) {
            object.add("or", value);
        } else {
            throw new NotImplementedException("No support for predicate: " + predicate);
        }

        return object;
    }

    @Override
    public JsonElement visit(UnaryLogicOperator condition) {
        final Predicate predicate = condition.getPredicate();
        final JsonObject object = new JsonObject();
        if (predicate == Predicate.NOT) {
            object.add("not", condition.getCondition().accept(this));
        }

        return object;
    }

    @Override
    public JsonElement visit(Range range) {
        final JsonObject rangeObjectContent = new JsonObject();
        rangeObjectContent.add("field", range.getExpression().accept(this));
        rangeObjectContent.add("start", range.getStart().accept(this));
        rangeObjectContent.add("end", range.getEnd().accept(this));

        final JsonObject rangeObject = new JsonObject();
        rangeObject.add("range", rangeObjectContent);
        return rangeObject;
    }

    @Override
    public JsonElement visit(ConstantCondition constantCondition) {
        return new JsonPrimitive(constantCondition.value());
    }

    @Override
    public JsonElement visit(Timestamp timestamp) {
        final JsonObject timestampObject = new JsonObject();
        timestampObject.add("metadata", new JsonPrimitive("timestamp"));
        return timestampObject;
    }

    @Override
    public JsonElement visit(TaskId taskId) {
        final JsonObject taskIdObject = new JsonObject();
        taskIdObject.add("metadata", new JsonPrimitive("taskId"));
        return taskIdObject;
    }

    @Override
    public JsonElement visit(Distinct distinct) {
        final JsonObject distinctObject = new JsonObject();
        distinctObject.add("distinct", distinct.getExpression().accept(this));
        return distinctObject;
    }

    @Override
    public JsonElement visit(StagingStatus stagingStatus) {
        final JsonObject stagingStatusObject = new JsonObject();
        stagingStatusObject.add("metadata", new JsonPrimitive("staging_source"));
        return stagingStatusObject;
    }

    @Override
    public JsonElement visit(StagingError stagingError) {
        final JsonObject stagingErrorObject = new JsonObject();
        stagingErrorObject.add("metadata", new JsonPrimitive("staging_error"));
        return stagingErrorObject;
    }

    @Override
    public JsonElement visit(StagingSource stagingSource) {
        final JsonObject stagingSourceObject = new JsonObject();
        stagingSourceObject.add("metadata", new JsonPrimitive("staging_source"));
        return stagingSourceObject;
    }

    @Override
    public JsonElement visit(StagingBlockKey stagingBlockKey) {
        final JsonObject stagingBlockKeyObject = new JsonObject();
        stagingBlockKeyObject.add("metadata", new JsonPrimitive("staging_blockkey"));
        return stagingBlockKeyObject;
    }

    @Override
    public JsonElement visit(GroupSize groupSize) {
        final JsonObject groupSizeObject = new JsonObject();
        groupSizeObject.add("metadata", new JsonPrimitive("group_size"));
        return groupSizeObject;
    }

    @Override
    public JsonElement visit(Join join) {
        JsonObject joinObject = new JsonObject();
        joinObject.add("from", new JsonPrimitive(toPath(join.getLeftField())));
        joinObject.add("on", new JsonPrimitive(toPath(join.getRightField())));
        return joinObject;
    }

    @Override
    public JsonElement visit(Field field) {
        final JsonObject fieldObject = new JsonObject();
        final String path = toPath(field);
        fieldObject.add("field", new JsonPrimitive(path));
        return fieldObject;
    }

    @Override
    public JsonElement visit(Alias alias) {
        final JsonObject aliasObject = new JsonObject();
        final JsonArray aliasObjectContent = new JsonArray();
        final JsonObject aliasDefinition = new JsonObject();
        aliasDefinition.add("name", new JsonPrimitive(alias.getAliasName()));
        aliasObjectContent.add(aliasDefinition);
        aliasObjectContent.add(alias.getTypedExpression().accept(this));

        aliasObject.add("alias", aliasObjectContent);

        return aliasObject;
    }

    @Override
    public JsonElement visit(Id id) {
        final JsonObject fieldObject = new JsonObject();
        final String path = id.getType().getName() + '/' + id.getId();
        fieldObject.add("field", new JsonPrimitive(path));
        return fieldObject;
    }

    @Override
    public JsonElement visit(StringConstant constant) {
        return toConstant(constant);
    }

    @Override
    public JsonElement visit(IntegerConstant constant) {
        return toConstant(constant);
    }

    @Override
    public JsonElement visit(DateConstant constant) {
        return toConstant(constant);
    }

    @Override
    public JsonElement visit(DateTimeConstant constant) {
        return toConstant(constant);
    }

    @Override
    public JsonElement visit(BooleanConstant constant) {
        return toConstant(constant);
    }

    @Override
    public JsonElement visit(BigDecimalConstant constant) {
        return toConstant(constant);
    }

    @Override
    public JsonElement visit(TimeConstant constant) {
        return toConstant(constant);
    }

    @Override
    public JsonElement visit(ShortConstant constant) {
        return toConstant(constant);
    }

    @Override
    public JsonElement visit(ByteConstant constant) {
        return toConstant(constant);
    }

    @Override
    public JsonElement visit(LongConstant constant) {
        return toConstant(constant);
    }

    @Override
    public JsonElement visit(DoubleConstant constant) {
        return toConstant(constant);
    }

    @Override
    public JsonElement visit(FloatConstant constant) {
        return toConstant(constant);
    }

    @Override
    public JsonElement visit(IsEmpty isEmpty) {
        JsonObject isEmptyObject = new JsonObject();
        isEmptyObject.add("isEmpty", isEmpty.getField().accept(this));
        return isEmptyObject;
    }

    @Override
    public JsonElement visit(NotIsEmpty notIsEmpty) {
        return new UnaryLogicOperator(new IsEmpty(notIsEmpty.getField()), Predicate.NOT).accept(this);
    }

    @Override
    public JsonElement visit(IsNull isNull) {
        JsonObject isNullObject = new JsonObject();
        isNullObject.add("isNull", isNull.getField().accept(this));
        return isNullObject;
    }

    @Override
    public JsonElement visit(NotIsNull notIsNull) {
        return new UnaryLogicOperator(new IsNull(notIsNull.getField()), Predicate.NOT).accept(this);
    }

    @Override
    public JsonElement visit(OrderBy orderBy) {
        final JsonObject orderByObject = new JsonObject();
        final JsonArray orderByContent = new JsonArray();
        orderByContent.add(orderBy.getExpression().accept(this));
        final JsonObject direction = new JsonObject();
        direction.add("direction", new JsonPrimitive(orderBy.getDirection().toString()));
        orderByContent.add(direction);
        orderByObject.add("order_by", orderByContent);

        return orderByObject;
    }

    @Override
    public JsonElement visit(Count count) {
        final JsonObject countObject = new JsonObject();
        final JsonElement element = count.getExpression() == null ? new JsonObject() : count.getExpression().accept(this);
        countObject.add("count", element);

        return countObject;
    }

    @Override
    public JsonElement visit(FullText fullText) {
        final JsonObject fullTextObject = new JsonObject();
        final JsonArray fullTextObjectContent = new JsonArray();
        final JsonObject valueObject = new JsonObject();
        valueObject.add("value", new JsonPrimitive(fullText.getValue()));
        fullTextObjectContent.add(valueObject);
        fullTextObject.add("full_text", fullTextObjectContent);

        return fullTextObject;
    }

    @Override
    public JsonElement visit(FieldFullText fieldFullText) {
        final JsonObject fullTextObject = new JsonObject();

        final JsonArray fullTextObjectContent = new JsonArray();
        final JsonObject fieldObject = new JsonObject();
        fieldObject.add("field", new JsonPrimitive(toPath(fieldFullText.getField())));
        fullTextObjectContent.add(fieldObject);

        final JsonObject valueObject = new JsonObject();
        fieldObject.add("value", new JsonPrimitive(fieldFullText.getValue()));
        fullTextObjectContent.add(valueObject);

        fullTextObject.add("full_text", fullTextObjectContent);

        return fullTextObject;
    }

    @Override
    public JsonElement visit(At at) {
        final JsonObject atObject = new JsonObject();
        final JsonObject atObjectContent = new JsonObject();
        atObjectContent.add("date", new JsonPrimitive(String.valueOf(at.getDateTime())));
        atObjectContent.add("swing", new JsonPrimitive(at.getSwing().toString().toLowerCase()));
        atObject.add("as_of", atObjectContent);

        return atObject;
    }

    @Override
    public JsonElement visit(Isa isa) {
        final JsonObject isObject = new JsonObject();

        final JsonArray isObjectContent = new JsonArray();
        isObjectContent.add(isa.getExpression().accept(this));
        final JsonObject typeObject = new JsonObject();
        typeObject.add("type", new JsonPrimitive(isa.getType().getName()));
        isObjectContent.add(typeObject);

        return isObject;
    }

    @Override
    public JsonElement visit(IndexedField indexedField) {
        final JsonObject indexFieldObject = new JsonObject();

        final JsonArray indexFieldObjectContent = new JsonArray();
        final FieldMetadata fieldMetadata = indexedField.getFieldMetadata();
        final String path = fieldMetadata.getContainingType().getName() + "/" + fieldMetadata.getName();
        final JsonObject fieldObject = new JsonObject();
        fieldObject.add("field", new JsonPrimitive(path));
        indexFieldObjectContent.add(fieldObject);
        indexFieldObjectContent.add(new JsonPrimitive(indexedField.getPosition()));
        indexFieldObject.add("index", indexFieldObjectContent);

        return indexFieldObject;
    }

}
