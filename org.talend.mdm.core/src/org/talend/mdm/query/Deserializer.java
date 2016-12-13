/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.query;

import com.amalto.core.query.user.*;
import com.google.gson.*;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import java.lang.reflect.Type;
import java.util.Date;

class Deserializer implements JsonDeserializer<Expression> {

    private final MetadataRepository repository;

    private UserQueryBuilder         queryBuilder;

    Deserializer(MetadataRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null.");
        }
        this.repository = repository;
    }

    static TypedExpressionProcessor getTypedExpression(JsonObject object) {
        if (object.has("field")) { //$NON-NLS-1$
            return FieldProcessor.INSTANCE;
        } else if (object.has("alias")) { //$NON-NLS-1$
            return AliasProcessor.INSTANCE;
        } else if (object.has("max")) { //$NON-NLS-1$
            return MaxProcessor.INSTANCE;
        } else if (object.has("min")) { //$NON-NLS-1$
            return MinProcessor.INSTANCE;
        } else if (object.has("index")) { //$NON-NLS-1
            return IndexProcessor.INSTANCE;
        } else if (object.has("metadata")) { //$NON-NLS-1
            return MetadataFieldProcessor.INSTANCE;
        } else if (object.has("distinct")) { //$NON-NLS-1
            return DistinctProcessor.INSTANCE;
        } else if (object.has("count")) { //$NON-NLS-1
            return CountProcessor.INSTANCE;
        } else {
            throw new NotImplementedException("No support for '" + object + "'.");
        }
    }

    static ConditionProcessor getProcessor(JsonObject object) {
        if (object.has("eq")) { //$NON-NLS-1$
            return EqualsProcessor.INSTANCE;
        } else if (object.has("gt")) { //$NON-NLS-1$
            return GreaterThanProcessor.INSTANCE;
        } else if (object.has("gte")) { //$NON-NLS-1$
            return GreaterThanEqualsProcessor.INSTANCE;
        } else if (object.has("lt")) { //$NON-NLS-1$
            return LessThanProcessor.INSTANCE;
        } else if (object.has("lte")) { //$NON-NLS-1$
            return LessThanEqualsProcessor.INSTANCE;
        } else if (object.has("contains")) { //$NON-NLS-1$
            return ContainsProcessor.INSTANCE;
        } else if (object.has("startsWith")) { //$NON-NLS-1$
            return StartsWithProcessor.INSTANCE;
        } else if (object.has("and")) { //$NON-NLS-1$
            return AndProcessor.INSTANCE;
        } else if (object.has("or")) { //$NON-NLS-1$
            return OrProcessor.INSTANCE;
        } else if (object.has("not")) { //$NON-NLS-1$
            return NotProcessor.INSTANCE;
        } else if (object.has("is")) { //$NON-NLS-1$
            return IsProcessor.INSTANCE;
        } else if (object.has("full_text")) { //$NON-NLS-1$
            return FullTextProcessor.INSTANCE;
        } else {
            throw new NotImplementedException("No support for '" + object + "'.");
        }
    }

    static Field getField(MetadataRepository repository, String path) {
        String typeName = StringUtils.substringBefore(path, "/"); //$NON-NLS-1$
        ComplexTypeMetadata type = repository.getComplexType(typeName);
        if (type == null) {
            throw new IllegalArgumentException("Malformed query (type '" + typeName + "' does not exist).");
        }
        return new Field(type.getField(StringUtils.substringAfter(path, "/"))); //$NON-NLS-1$
    }

    @Override
    public Expression deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (!obj.has("select")) { //$NON-NLS-1$
                throw new IllegalArgumentException("Malformed query (has top level object but hasn't 'select' in it).");
            }
            JsonObject select = obj.get("select").getAsJsonObject(); //$NON-NLS-1$
            if (!select.has("from")) {
                throw new IllegalArgumentException("Malformed query (expected 'from' object in 'select').");
            }
            // 'from' clause (selected types in query).
            JsonArray from = select.get("from").getAsJsonArray(); //$NON-NLS-1$
            for (int i = 0; i < from.size(); i++) {
                String typeName = from.get(i).getAsString();
                ComplexTypeMetadata type = repository.getComplexType(typeName);
                if (type == null) {
                    throw new IllegalArgumentException("Malformed query (type '" + typeName + "' does not exist).");
                }
                if (queryBuilder == null) {
                    queryBuilder = UserQueryBuilder.from(type);
                } else {
                    queryBuilder.and(type);
                }
            }
            // Sanity check: query builder should be initialized at this point
            if (queryBuilder == null) {
                throw new IllegalStateException("Expected query builder to be initialized.");
            }
            // Process conditions (conditions are optional)
            if (select.has("where")) { //$NON-NLS-1$
                JsonObject where = select.get("where").getAsJsonObject(); //$NON-NLS-1$
                ConditionProcessor processor = getProcessor(where);
                queryBuilder.where(processor.process(where, repository));
            }
            // Process selected fields (fields are optional)
            if (select.has("fields")) { //$NON-NLS-1$
                JsonArray fields = select.get("fields").getAsJsonArray(); //$NON-NLS-1$
                for (int i = 0; i < fields.size(); i++) {
                    JsonElement element = fields.get(i);
                    if (element != null && element.isJsonObject()) {
                        JsonObject fieldElement = element.getAsJsonObject();
                        TypedExpressionProcessor processor = getTypedExpression(fieldElement);
                        queryBuilder.select(processor.process(fieldElement, repository));
                    }
                }
            }
            // Process joins (joins are optional)
            if (select.has("joins")) { //$NON-NLS-1$
                JsonArray fields = select.get("joins").getAsJsonArray(); //$NON-NLS-1$
                for (int i = 0; i < fields.size(); i++) {
                    JsonElement element = fields.get(i);
                    if (element != null && element.isJsonObject()) {
                        JsonObject fieldElement = element.getAsJsonObject();
                        String joinLeft = fieldElement.get("from").getAsString();
                        String joinRight = fieldElement.get("on").getAsString();
                        FieldMetadata leftField = getField(repository, joinLeft).getFieldMetadata();
                        FieldMetadata rightField = getField(repository, joinRight).getFieldMetadata();
                        queryBuilder.join(leftField, rightField);
                    }
                }
            }
            // Process paging (optional)
            if (select.has("start")) { //$NON-NLS-1$
                queryBuilder.start(select.get("start").getAsInt()); //$NON-NLS-1$
            }
            if (select.has("limit")) { //$NON-NLS-1$
                queryBuilder.limit(select.get("limit").getAsInt()); //$NON-NLS-1$
            }
            // Process history browsing (optional)
            if (select.has("as_of")) { //$NON-NLS-1$
                JsonElement element = select.get("as_of"); //$NON-NLS-1$
                if (element != null && element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();
                    JsonElement date = object.get("date"); //$NON-NLS-1$
                    if (date == null) {
                        throw new IllegalArgumentException("Expected 'date' element in '" + element + "'.");
                    }
                    String dateAsString = date.getAsString();
                    if ("yesterday".equalsIgnoreCase(dateAsString)) { //$NON-NLS-1$
                        Date yesterday = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000));
                        synchronized (DateTimeConstant.DATE_FORMAT) {
                            dateAsString = DateTimeConstant.DATE_FORMAT.format(yesterday);
                        }
                        queryBuilder.at(dateAsString);
                    } else if ("now".equalsIgnoreCase(dateAsString)) { //$NON-NLS-1$
                        Date yesterday = new Date(System.currentTimeMillis());
                        synchronized (DateTimeConstant.DATE_FORMAT) {
                            dateAsString = DateTimeConstant.DATE_FORMAT.format(yesterday);
                        }
                        queryBuilder.at(dateAsString);
                    } else if ("creation".equalsIgnoreCase(dateAsString)) { //$NON-NLS-1$
                        Date minDate = new Date(0);
                        synchronized (DateTimeConstant.DATE_FORMAT) {
                            dateAsString = DateTimeConstant.DATE_FORMAT.format(minDate);
                        }
                        queryBuilder.at(dateAsString);
                        // For creation, move to initial date (no record exists, then swing to the state after).
                        queryBuilder.swing(At.Swing.AFTER.name());
                        if (object.has("swing")) { //$NON-NLS-1$
                            // We *could* support BEFORE as swing, but not AFTER (can't swing twice).
                            throw new IllegalArgumentException("Swing argument is not supported for 'creation' date.");
                        }
                    } else {
                        queryBuilder.at(dateAsString);
                    }
                    // Swing is optional
                    if (object.has("swing")) { //$NON-NLS-1$
                        queryBuilder.swing(object.get("swing").getAsString()); //$NON-NLS-1$
                    }
                }
            }
            // Process order by (optional)
            if (select.has("order_bys")) { //$NON-NLS-1$
                JsonArray orderBys = select.get("order_bys").getAsJsonArray(); //$NON-NLS-1$
                for (int i = 0; i < orderBys.size(); i++) {
                    TypedExpression orderByExpression = null;
                    OrderBy.Direction direction = null;
                    JsonElement orderBy = orderBys.get(i).getAsJsonObject().get("order_by"); //$NON-NLS-1
                    JsonArray array = orderBy.getAsJsonArray();
                    for (int j = 0; j < array.size(); j++) {
                        JsonElement element = array.get(j);
                        if (element != null && element.isJsonObject()) {
                            JsonObject orderByElement = element.getAsJsonObject();
                            boolean isDirection = orderByElement.get("direction") != null; //$NON-NLS-1
                            if (!isDirection) {
                                orderByExpression = getTypedExpression(orderByElement).process(orderByElement, repository);
                            } else {
                                String directionAsString = orderByElement.get("direction").getAsString(); //$NON-NLS-1
                                if ("DESC".equalsIgnoreCase(directionAsString)) { //$NON-NLS-1
                                    direction = OrderBy.Direction.DESC;
                                } else if ("ASC".equalsIgnoreCase(directionAsString)) { //$NON-NLS-1
                                    direction = OrderBy.Direction.ASC;
                                } else {
                                    throw new IllegalArgumentException("Direction '" + directionAsString
                                            + "' is not a valid direction for order by.");
                                }
                            }
                        }
                    }
                    if (orderByExpression == null || direction == null) {
                        throw new IllegalStateException("Missing expression and/or direction for order by.");
                    }
                    queryBuilder.orderBy(orderByExpression, direction);
                }
            }
            // Process cache option (optional)
            if (select.has("cache")) { //$NON-NLS-1
                boolean enableCache = select.get("cache").getAsBoolean(); //$NON-NLS-1
                if (enableCache) {
                    queryBuilder.cache();
                } else {
                    queryBuilder.nocache();
                }
            }
        } else {
            throw new IllegalArgumentException("Malformed query (expected a top level object).");
        }
        return queryBuilder.getExpression();
    }

}
