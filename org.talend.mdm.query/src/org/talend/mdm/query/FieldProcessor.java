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

import com.amalto.core.query.user.TypedExpression;
import com.google.gson.JsonObject;
import org.talend.mdm.commmon.metadata.MetadataRepository;

class FieldProcessor implements TypedExpressionProcessor {

    public static final TypedExpressionProcessor INSTANCE = new FieldProcessor();

    private FieldProcessor() {
    }

    @Override
    public TypedExpression process(JsonObject element, MetadataRepository repository) {
        String fieldName = element.get("field").getAsString(); //$NON-NLS-1$ //$NON-NLS-2$
        return Deserializer.getField(repository, fieldName);
    }
}
