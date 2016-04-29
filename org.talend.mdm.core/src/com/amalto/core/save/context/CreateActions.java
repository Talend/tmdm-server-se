/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import com.amalto.core.history.accessor.DOMAccessor;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.util.core.EUUIDCustomType;

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.history.action.FieldUpdateAction;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.w3c.dom.Node;

/**
 * Generate actions on creation (like setting UUID and AUTO_INCREMENT fields that <b>are not</b> part of the saved
 * entity type).
 * 
 * @see ID for code that sets ID values.
 */
class CreateActions extends DefaultMetadataVisitor<List<Action>> {

    private final Stack<PathElement> path = new Stack<PathElement>();

    protected final List<Action> actions = new LinkedList<Action>();

    protected final Date date;

    protected final String source;

    protected final String userName;

    protected final MutableDocument document;

    private final SaverSource saverSource;

    private final String dataCluster;

    private final String dataModel;

    private String rootTypeName = null;

    private static class PathElement {

        final FieldMetadata fieldMetadata;

        final Integer index;

        PathElement(FieldMetadata fieldMetadata, Integer index) {
            this.fieldMetadata = fieldMetadata;
            this.index = index;
        }
    }

    CreateActions(MutableDocument document,
                  Date date,
                  String source,
                  String userName,
                  String dataCluster,
                  String dataModel,
                  SaverSource saverSource) {
        this.document = document;
        this.date = date;
        this.source = source;
        this.userName = userName;
        this.dataCluster = dataCluster;
        this.dataModel = dataModel;
        this.saverSource = saverSource;
    }

    private String getRealPath() {
        if (path.isEmpty()) {
            throw new IllegalStateException();
        } else {
            StringBuilder builder = new StringBuilder();
            Iterator<PathElement> pathIterator = path.iterator();
            while (pathIterator.hasNext()) {
                PathElement item = pathIterator.next();
                builder.append(item.fieldMetadata.getName());
                if (item.index != null) {
                    builder.append('[').append(String.valueOf(item.index)).append(']');
                }
                if (pathIterator.hasNext()) {
                    builder.append('/');
                }
            }
            return builder.toString();
        }
    }

    @Override
    public List<Action> visit(ComplexTypeMetadata complexType) {
        if (rootTypeName == null) {
            rootTypeName = complexType.getName();
        }
        super.visit(complexType);
        return actions;
    }

    @Override
    public List<Action> visit(ReferenceFieldMetadata referenceField) {
        path.push(new PathElement(referenceField, null));
        String xpath = getRealPath();
        int size = document.createAccessor(xpath).size();
        path.pop();
        if (referenceField.isMany() && size > 1) {
            for (int i = 1; i <= size; i++) {
                path.push(new PathElement(referenceField, i));
                super.visit(referenceField);
                path.pop();
            }
        } else {
            path.push(new PathElement(referenceField, null));
            super.visit(referenceField);
            path.pop();
        }
        return actions;
    }

    @Override
    public List<Action> visit(ContainedTypeFieldMetadata containedField) {
        path.push(new PathElement(containedField, null));
        String xpath = getRealPath();
        int size = document.createAccessor(xpath).size();
        path.pop();
        if (containedField.isMany() && size > 1) {
            for (int i = 1; i <= size; i++) {
                path.push(new PathElement(containedField, i));
                super.visit(containedField);
                path.pop();
            }
        } else {
            path.push(new PathElement(containedField, null));
            if (!(containedField.getContainingType().getContainer() != null
                    && containedField.getContainedType().getName()
                    .equals(containedField.getContainingType().getContainer().getDeclaringType().getName()))) {
                super.visit(containedField);
            }
            path.pop();
        }
        return actions;
    }

    @Override
    public List<Action> visit(EnumerationFieldMetadata enumField) {
        path.push(new PathElement(enumField, null));
        String xpath = getRealPath();
        int size = document.createAccessor(xpath).size();
        path.pop();
        boolean doCreate = doCreate(enumField);
        if (enumField.isMany() && size > 1) {
            for (int i = 1; i <= size; i++) {
                path.push(new PathElement(enumField, i));
                handleField(enumField, doCreate, xpath);
                path.pop();
            }
        } else {
            path.push(new PathElement(enumField, null));
            handleField(enumField, doCreate, xpath);
            path.pop();
        }
        return actions;
    }

    @Override
    public List<Action> visit(SimpleTypeFieldMetadata simpleField) {
        path.push(new PathElement(simpleField, null));
        String xpath = getRealPath();
        int size = document.createAccessor(xpath).size();
        path.pop();
        boolean doCreate = doCreate(simpleField);
        if (simpleField.isMany() && size > 1) {
            for (int i = 1; i <= size; i++) {
                path.push(new PathElement(simpleField, i));
                handleField(simpleField, doCreate, xpath);
                path.pop();
            }
        } else {
            path.push(new PathElement(simpleField, null));
            handleField(simpleField, doCreate, xpath);
            path.pop();
        }
        return super.visit(simpleField);
    }

    private boolean doCreate(FieldMetadata simpleField) {
        // Under some circumstances, do not generate action(s) for UUID/AUTOINCREMENT (see TMDM-4473)
        boolean doCreate = true;
        if (!path.isEmpty()) {
            // Only apply the do-create-check for UUID/AUTO_INCREMENT field to improve the performance
            String typeName = simpleField.getType().getName();
            if (EUUIDCustomType.AUTO_INCREMENT.getName().equalsIgnoreCase(typeName)
                    || EUUIDCustomType.UUID.getName().equalsIgnoreCase(typeName)) {
                FieldMetadata parentField = path.peek().fieldMetadata;
                if (parentField != null) {
                    boolean isParentOptional = !parentField.isMandatory();
                    boolean isEmpty = false;
                    Accessor accessor = document.createAccessor(getRealPath());
                    Node parentNode = null;
                    if (accessor instanceof DOMAccessor) {
                        parentNode = ((DOMAccessor) accessor).getNode();
                    }
                    if (parentNode == null) {
                        isEmpty = true;
                    } else if (parentNode.getTextContent() == null || parentNode.getTextContent().isEmpty()) {
                        isEmpty = true;
                    }
                    if (isParentOptional && isEmpty) {
                        doCreate = false;
                    }
                }
            }
        }
        return doCreate;
    }

    protected void handleField(FieldMetadata field, boolean doCreate, String currentPath) {
        // Handle UUID and AutoIncrement elements (this code also ensures any previous value is overwritten, see
        // TMDM-3900).
        // Note #2: This code generate values even for non-mandatory fields (but this is expected behavior).
        if (EUUIDCustomType.AUTO_INCREMENT.getName().equalsIgnoreCase(field.getType().getName()) && doCreate) {
            String conceptName = rootTypeName + "." + field.getName().replaceAll("/", "."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            String autoIncrementValue = saverSource.nextAutoIncrementId(dataCluster, dataModel, conceptName);
            actions.add(new FieldUpdateAction(date, source, userName, currentPath, StringUtils.EMPTY, autoIncrementValue,
                    field));
        } else if (EUUIDCustomType.UUID.getName().equalsIgnoreCase(field.getType().getName()) && doCreate) {
            String uuidValue = UUID.randomUUID().toString();
            actions.add(new FieldUpdateAction(date, source, userName, currentPath, StringUtils.EMPTY, uuidValue, field));
        } else {
            if (field.isKey()) {
                Accessor accessor = document.createAccessor(currentPath);
                if (!accessor.exist()) {
                    throw new IllegalStateException("Document was expected to contain id information at '" + currentPath + "'");
                }
            }
        }
    }
}
