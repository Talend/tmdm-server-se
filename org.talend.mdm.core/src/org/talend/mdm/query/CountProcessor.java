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

import com.amalto.core.query.user.Field;
import com.amalto.core.query.user.TypedExpression;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import static com.amalto.core.query.user.UserQueryBuilder.count;

class CountProcessor implements TypedExpressionProcessor {

    public static final TypedExpressionProcessor INSTANCE = new CountProcessor();

    private CountProcessor() {
    }

    @Override
    public TypedExpression process(JsonObject element, MetadataRepository repository) {
        JsonElement countElement = element.get("count"); //$NON-NLS-1
        JsonObject count = countElement.getAsJsonObject();
        if (count.entrySet().size() > 0) {
            TypedExpression field = Deserializer.getTypedExpression(count).process(count, repository);
            if (!(field instanceof Field)) {
                throw new IllegalArgumentException("Can only count field occurrences.");
            }
            return count(((Field) field).getFieldMetadata());
        } else {
            return count();
        }
    }
}
