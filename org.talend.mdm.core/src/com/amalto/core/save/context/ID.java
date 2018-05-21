/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.util.core.EUUIDCustomType;

import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.save.DOMDocument;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.SaverSession;
import com.amalto.core.save.UserAction;

class ID implements DocumentSaver {

    private final DocumentSaver next;

    private final List<String> ids = new LinkedList<String>();

    private String savedTypeName;

    ID(DocumentSaver next) {
        this.next = next;
    }

    public void save(SaverSession session, DocumentSaverContext context) {
        ComplexTypeMetadata type = context.getUserDocument().getType();
        if (type == null) {
            throw new IllegalStateException("Type of record being saved is expected to be set at this step.");
        }
        Collection<FieldMetadata> keyFields = type.getKeyFields();
        SaverSource database = session.getSaverSource();
        String dataCluster = context.getDataCluster();
        String dataModelName = context.getDataModelName();
        MutableDocument userDocument = context.getUserDocument();
        String typeName = type.getName();
        for (FieldMetadata keyField : keyFields) {
            String keyFieldTypeName = keyField.getType().getName();
            Accessor userAccessor = userDocument.createAccessor(keyField.getName());
            // Get ids.
            String currentIdValue;
            if (isServerProvidedValue(keyFieldTypeName)) {
                if (userAccessor.exist()) { // Ignore cases where id is not there (will usually mean this is a creation).
                    String value = userAccessor.get();
                    if (!value.trim().isEmpty()) {
                        currentIdValue = value;
                    } else {
                        currentIdValue = null;  // Value is empty, don't consider this as an ID value.
                    }
                } else {
                    currentIdValue = null; // Element does not exist, don't consider this as an ID value.
                }
            } else {
                // Not a value to generate, first ensure value is correctly set.
                if (!userAccessor.exist()) {
                    throw new IllegalArgumentException("Expected id '" + keyField.getName() + "' to be set.");
                }
                currentIdValue = userAccessor.get();
            }
            if (currentIdValue != null && !StringUtils.EMPTY.equals(currentIdValue)) {
                ids.add(currentIdValue);
            } else if(!isServerProvidedValue(keyFieldTypeName)){
                throw new IllegalArgumentException("Expected id '" + keyField.getName() + "' to be set.");
            }
        }
        // now has an id, so load database document
        String[] xmlDocumentId = ids.toArray(new String[ids.size()]);
        if (xmlDocumentId.length > 0 && database.exist(dataCluster, dataModelName, typeName, xmlDocumentId)) {
            if (context.getUserAction() == UserAction.AUTO || context.getUserAction() == UserAction.AUTO_STRICT) {
                context.setUserAction(UserAction.UPDATE);
            }
            context.setId(xmlDocumentId);
            context.setDatabaseDocument(database.get(dataCluster, dataModelName, typeName, xmlDocumentId));
        } else {
            // Throw an exception if trying to update a document that does not exist.
            switch (context.getUserAction()) {
                case AUTO:
                case AUTO_STRICT:
                case CREATE:
                case CREATE_STRICT:
                case REPLACE:
                    break;
                case UPDATE:
                case PARTIAL_UPDATE:
                    StringBuilder builder = new StringBuilder();
                    for (String idElement : xmlDocumentId) {
                        builder.append('[').append(idElement).append(']');
                    }
                    throw new IllegalStateException("Can not update document '" + type.getName() + "' with id '" + builder.toString() + "' because it does not exist.");
            }
            // Creation... so mark context
            switch (context.getUserAction()) {
                case AUTO_STRICT:
                    // In case of AUTO_STRICT, stick to CREATE_STRICT
                    context.setUserAction(UserAction.CREATE_STRICT);
                    break;
                default:
                    context.setUserAction(UserAction.CREATE);
            }
            context.setDatabaseDocument(new DOMDocument(SaverContextFactory.DOCUMENT_BUILDER.newDocument(), type, dataCluster, context.getDataModelName()));
        }
        // Continue save
        savedTypeName = type.getName();
        next.save(session, context);
    }

    private static boolean isServerProvidedValue(String keyFieldTypeName) {
        return EUUIDCustomType.UUID.getName().equalsIgnoreCase(keyFieldTypeName)
                || EUUIDCustomType.AUTO_INCREMENT.getName().equalsIgnoreCase(keyFieldTypeName);
    }

    public String[] getSavedId() {
        return next.getSavedId();
    }

    public String getSavedConceptName() {
        return savedTypeName;
    }

    public String getBeforeSavingMessage() {
        return next.getBeforeSavingMessage();
    }

    public String getBeforeSavingMessageType() {
        return next.getBeforeSavingMessageType();
    }
}
