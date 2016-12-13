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

import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.query.user.metadata.MetadataField;
import com.google.gson.JsonObject;
import org.talend.mdm.commmon.metadata.MetadataRepository;

class MetadataFieldProcessor implements TypedExpressionProcessor {

    public static final TypedExpressionProcessor INSTANCE = new MetadataFieldProcessor();

    private MetadataFieldProcessor() {
    }

    @Override
    public TypedExpression process(JsonObject element, MetadataRepository repository) {
        String metadataFieldName = element.get("metadata").getAsString(); //$NON-NLS-1$ //$NON-NLS-2$
        MetadataField metadataField = MetadataField.Factory.getMetadataField(metadataFieldName);
        if (metadataField == null) {
            throw new IllegalArgumentException("Metadata field '" + metadataFieldName + "' does not exist.");
        }
        return metadataField;
    }
}
