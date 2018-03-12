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
import com.google.gson.JsonObject;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import static com.amalto.core.query.user.UserQueryBuilder.isa;

class IsProcessor implements ConditionProcessor {

    public static final ConditionProcessor INSTANCE = new IsProcessor();

    private IsProcessor() {
    }

    @Override
    public Condition process(JsonObject condition, MetadataRepository repository) {
        TypedExpression expression = null;
        ComplexTypeMetadata type = null;
        JsonArray element = condition.get("is").getAsJsonArray(); //$NON-NLS-1$
        for (int i = 0; i < element.size(); i++) {
            JsonObject currentElement = element.get(i).getAsJsonObject();
            if (currentElement.has("type")) { //$NON-NLS-1$
                String typeName = currentElement.get("type").getAsString(); //$NON-NLS-1$
                type = (ComplexTypeMetadata) repository.getNonInstantiableType(repository.getUserNamespace(), typeName);
            } else {
                expression = Deserializer.getTypedExpression(currentElement).process(currentElement, repository);
                if (!(expression instanceof Field)) {
                    throw new IllegalArgumentException("Malformed query (expect expression to be a Field in IS condition).");
                }
            }
        }
        if (type == null) {
            throw new IllegalArgumentException("Malformed query (no 'type' in IS condition).");
        }
        if (expression == null) {
            throw new IllegalArgumentException("Malformed query (no 'field' in IS condition).");
        }
        return isa(((Field) expression).getFieldMetadata(), type);
    }
}
