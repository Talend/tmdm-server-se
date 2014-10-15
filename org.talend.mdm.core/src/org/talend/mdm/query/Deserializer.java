package org.talend.mdm.query;

import java.lang.reflect.Type;

import com.amalto.core.query.user.*;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.google.gson.*;

class Deserializer implements JsonDeserializer<Expression> {

    private final MetadataRepository repository;

    private UserQueryBuilder queryBuilder;

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
        } else if (object.has("index")) {
            return IndexProcessor.INSTANCE;
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
                    JsonObject fieldElement = fields.get(i).getAsJsonObject();
                    TypedExpressionProcessor processor = getTypedExpression(fieldElement);
                    queryBuilder.select(processor.process(fieldElement, repository));
                }
            }
            // Process joins (joins are optional)
            if (select.has("joins")) { //$NON-NLS-1$
                JsonArray fields = select.get("joins").getAsJsonArray(); //$NON-NLS-1$
                for (int i = 0; i < fields.size(); i++) {
                    JsonObject fieldElement = fields.get(i).getAsJsonObject();
                    String joinLeft = fieldElement.get("from").getAsString();
                    String joinRight = fieldElement.get("on").getAsString();
                    FieldMetadata leftField = getField(repository, joinLeft).getFieldMetadata();
                    FieldMetadata rightField = getField(repository, joinRight).getFieldMetadata();
                    queryBuilder.join(leftField, rightField);
                }
            }
            // Process paging (optional)
            if (select.has("start")) { //$NON-NLS-1$
                queryBuilder.start(select.get("start").getAsInt()); //$NON-NLS-1$
            }
            if (select.has("limit")) { //$NON-NLS-1$
                queryBuilder.limit(select.get("limit").getAsInt()); //$NON-NLS-1$
            }
        } else {
            throw new IllegalArgumentException("Malformed query (expected a top level object).");
        }
        return queryBuilder.getExpression();
    }

}
