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

import java.util.ArrayList;
import java.util.List;

import com.amalto.core.query.user.Condition;
import com.amalto.core.query.user.TypedExpression;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.talend.mdm.commmon.metadata.MetadataRepository;

abstract class BasicConditionProcessor implements ConditionProcessor {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Condition process(JsonObject condition, MetadataRepository repository) {
        JsonArray conditionElement = condition.get(getConditionElement()).getAsJsonArray(); //$NON-NLS-1
        TypedExpression expression = null;
        String value = null;
        List valueList = new ArrayList();
        TypedExpression valueExpression = null;
        for (int i = 0; i < conditionElement.size(); i++) {
            JsonObject element = conditionElement.get(i).getAsJsonObject();
            if (element.has("value")) { //$NON-NLS-1
                JsonElement valueElement = element.get("value"); //$NON-NLS-1
                if (valueElement.isJsonPrimitive()) {
                    value = element.getAsJsonPrimitive("value").getAsString(); //$NON-NLS-1
                } else if (valueElement.isJsonObject()) {
                    valueExpression = Deserializer.getTypedExpression(valueElement.getAsJsonObject()).process(valueElement.getAsJsonObject(), repository);
                } else if (valueElement instanceof JsonArray) {
                    JsonArray array = (JsonArray) valueElement;
                    for (int j = 0; j < array.size(); j++) {
                        JsonElement jsonElement = array.get(j);
                        if (jsonElement.isJsonPrimitive()) {
                            valueList.add(jsonElement.getAsJsonPrimitive().getAsString());
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Value '" + valueElement + "' is not supported."); //$NON-NLS-1 //$NON-NLS-2
                }
            } else {
                final TypedExpression process = Deserializer.getTypedExpression(element.getAsJsonObject()).process(element.getAsJsonObject(), repository);
                if (expression != null) { // we already have a field expression, keep new value as a value expression
                    valueExpression = process;
                } else {
                    expression = process;
                }
            }
        }
        if (expression == null || (value == null && valueExpression == null && valueList.isEmpty())) {
            throw new IllegalArgumentException("Missing expression and/or value."); //$NON-NLS-1
        }

        if (value != null && valueList.isEmpty()) {
            return buildCondition(expression, value);
        } else if (value == null && !valueList.isEmpty()) {
            return buildCondition(expression, valueList);
        } else { // valueExpression != null
            return buildCondition(expression, valueExpression);
        }
    }

    protected abstract Condition buildCondition(TypedExpression expression, String value);

    @SuppressWarnings("rawtypes")
    protected abstract Condition buildCondition(TypedExpression expression, List value);

    protected abstract Condition buildCondition(TypedExpression expression, TypedExpression value);

    protected abstract String getConditionElement();

}
