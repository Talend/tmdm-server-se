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

import com.amalto.core.query.user.Field;
import com.amalto.core.query.user.TypedExpression;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import static com.amalto.core.query.user.UserQueryBuilder.index;

class IndexProcessor implements TypedExpressionProcessor {

    public static final TypedExpressionProcessor INSTANCE = new IndexProcessor();

    private IndexProcessor() {
    }

    @Override
    public TypedExpression process(JsonObject element, MetadataRepository repository) {
        JsonArray index = element.get("index").getAsJsonArray(); //$NON-NLS-1$
        TypedExpression expression = null;
        int indexValue = -1;
        for (int j = 0; j < index.size(); j++) {
            JsonElement jsonElement = index.get(j);
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                expression = Deserializer.getTypedExpression(jsonObject).process(jsonObject, repository);
            } else if (jsonElement.isJsonPrimitive()) {
                indexValue = jsonElement.getAsInt();
            }
        }
        if (expression == null) {
            throw new IllegalArgumentException("Malformed query (expected expression in '" + index + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (indexValue < 0) {
            throw new IllegalArgumentException("Malformed query (expected 'index' in '" + index + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!(expression instanceof Field)) {
            throw new IllegalArgumentException("Malformed query (expected a field in expression)"); //$NON-NLS-1$
        }
        return index(((Field) expression).getFieldMetadata(), indexValue);
    }
}
