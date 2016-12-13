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

import com.amalto.core.query.user.Condition;
import com.amalto.core.query.user.TypedExpression;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.talend.mdm.commmon.metadata.MetadataRepository;

abstract class BasicConditionProcessor implements ConditionProcessor {

    public Condition process(JsonObject condition, MetadataRepository repository) {
        JsonArray conditionElement = condition.get(getConditionElement()).getAsJsonArray(); //$NON-NLS-1
        TypedExpression expression = null;
        String value = null;
        TypedExpression valueExpression = null;
        for (int i = 0; i < conditionElement.size(); i++) {
            JsonObject element = conditionElement.get(i).getAsJsonObject();
            if (element.has("value")) { //$NON-NLS-1
                JsonElement valueElement = element.get("value");
                if (valueElement.isJsonPrimitive()) {
                    value = element.getAsJsonPrimitive("value").getAsString(); //$NON-NLS-1
                } else if (valueElement.isJsonObject()) {
                    valueExpression = Deserializer.getTypedExpression(valueElement.getAsJsonObject()).process(valueElement.getAsJsonObject(), repository);
                } else {
                    throw new IllegalArgumentException("Value '" + valueElement + "' is not supported.");
                }
            } else {
                expression = Deserializer.getTypedExpression(element.getAsJsonObject()).process(element.getAsJsonObject(), repository);
            }
        }
        if (expression == null || (value == null && valueExpression == null)) {
            throw new IllegalArgumentException("Missing expression and/or value.");
        }
        if (value != null) {
            return buildCondition(expression, value);
        } else { // valueExpression != null
            return buildCondition(expression, valueExpression);
        }
    }

    protected abstract Condition buildCondition(TypedExpression expression, String value);

    protected abstract Condition buildCondition(TypedExpression expression, TypedExpression value);

    protected abstract String getConditionElement();

}
