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

import com.amalto.core.query.user.Condition;
import com.amalto.core.query.user.Field;
import com.amalto.core.query.user.TypedExpression;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import static com.amalto.core.query.user.UserQueryBuilder.fullText;

class FullTextProcessor implements ConditionProcessor {

    public static final ConditionProcessor INSTANCE = new FullTextProcessor();

    private FullTextProcessor() {
    }

    @Override
    public Condition process(JsonObject condition, MetadataRepository repository) {
        TypedExpression expression = null;
        String value = null;
        JsonArray fullText = condition.get("full_text").getAsJsonArray(); //$NON-NLS-1$
        for (int i = 0; i < fullText.size(); i++) {
            JsonObject jsonElement = fullText.get(i).getAsJsonObject();
            if (jsonElement.has("value")) {
                value = jsonElement.get("value").getAsString();
            } else {
                expression = Deserializer.getTypedExpression(jsonElement).process(jsonElement, repository);
            }
        }
        if (value == null) {
            throw new IllegalArgumentException("Malformed query (no 'value' in FULL_TEXT condition).");
        }
        if (expression != null) {
            if (!(expression instanceof Field)) {
                throw new IllegalArgumentException("Malformed query (expect expression to be a Field in FULL_TEXT condition).");
            }
            return fullText(((Field) expression).getFieldMetadata(), value);
        } else {
            return fullText(value);
        }
    }
}
