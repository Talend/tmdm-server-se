/*
 * Copyright (C) 2006-2012 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.util.core.EUUIDCustomType;
import org.w3c.dom.Node;

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.history.accessor.DOMAccessor;
import com.amalto.core.history.action.FieldUpdateAction;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;

/**
 * Generate actions on creation (like setting UUID and AUTO_INCREMENT fields that <b>are not</b> part of the saved
 * entity type).
 * 
 * @see ID for code that sets ID values.
 */
class CreateActions extends DefaultMetadataVisitor<List<Action>> {

    private final Stack<FieldMetadata> path = new Stack<FieldMetadata>();

    private final List<Action> actions = new LinkedList<Action>();

    private final Date date;

    private final String source;

    private final String userName;

    private final String universe;

    private final SaverSource saverSource;

    private final String dataCluster;

    private final Map<String,String> idValueMap = new HashMap<String,String>();

    private final MutableDocument document;

    private boolean hasMetAutoIncrement;

    private String rootTypeName = null;

    private Map<String, String> autoIncrementFieldMap;

    CreateActions(MutableDocument document, Date date, String source, String userName, String dataCluster, String universe,
            SaverSource saverSource, Map<String, String> autoIncrementFieldMap) {
        this.document = document;
        this.date = date;
        this.source = source;
        this.userName = userName;
        this.dataCluster = dataCluster;
        this.universe = universe;
        this.saverSource = saverSource;
        this.autoIncrementFieldMap = autoIncrementFieldMap;
    }

    private String getPath() {
        if (path.isEmpty()) {
            throw new IllegalStateException();
        } else {
            StringBuilder builder = new StringBuilder();
            Iterator<FieldMetadata> pathIterator = path.iterator();
            while (pathIterator.hasNext()) {
                builder.append(pathIterator.next().getName());
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
        path.push(referenceField);
        {
            super.visit(referenceField);
        }
        path.pop();
        return actions;
    }

    @Override
    public List<Action> visit(ContainedTypeFieldMetadata containedField) {
        path.push(containedField);
        {
            super.visit(containedField);
        }
        path.pop();
        return actions;
    }

    @Override
    public List<Action> visit(EnumerationFieldMetadata enumField) {
        path.push(enumField);
        {
            super.visit(enumField);
        }
        path.pop();
        return actions;
    }

    @Override
    public List<Action> visit(SimpleTypeFieldMetadata simpleField) {
        
        // Under some circumstances, do not generate action(s) for UUID/AUTOINCREMENT(see TMDM-4473)
        boolean doCreate = true;
        if (!path.isEmpty()) {
            // Only apply the do-create-check for UUID/AUTO_INCREMENT field to improve the performance
            if (EUUIDCustomType.AUTO_INCREMENT.getName().equalsIgnoreCase(simpleField.getType().getName())
                    || EUUIDCustomType.UUID.getName().equalsIgnoreCase(simpleField.getType().getName())) {
                FieldMetadata parentField = path.peek();
                if (parentField != null) {

                    boolean isParentOptional = !parentField.isMandatory();

                    boolean isEmpty = false;
                    Accessor accessor = document.createAccessor(getPath());
                    Node parentNode = null;
                    if (accessor instanceof DOMAccessor)
                        parentNode = ((DOMAccessor) accessor).getNode();
                    if (parentNode == null)
                        isEmpty = true;
                    else if (parentNode.getTextContent() == null || parentNode.getTextContent().isEmpty())
                        isEmpty = true;

                    if (isParentOptional && isEmpty)
                        doCreate = false;
                }
            }
        }

        path.push(simpleField);
        {
            // Handle UUID and AutoIncrement elements (this code also ensures any previous value is overwritten, see
            // TMDM-3900).
            // Note #2: This code generate values even for non-mandatory fields (but this is expected behavior).
            String currentPath = getPath();
            if (EUUIDCustomType.AUTO_INCREMENT.getName().equalsIgnoreCase(simpleField.getType().getName()) && doCreate) {
                String conceptName = rootTypeName + "." + simpleField.getName().replaceAll("/", "."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                String autoIncrementValue = autoIncrementFieldMap.get(conceptName);
                if (autoIncrementValue == null) {
                    autoIncrementValue = saverSource.nextAutoIncrementId(universe, dataCluster, conceptName);
                    autoIncrementFieldMap.put(conceptName, autoIncrementValue);
                }
                actions.add(new FieldUpdateAction(date, source, userName, currentPath, StringUtils.EMPTY, autoIncrementValue,
                        simpleField));
                if (simpleField.isKey()) {
                    idValueMap.put(currentPath, autoIncrementValue);
                }
                hasMetAutoIncrement = true; // Remembers we've just met an auto increment value
            } else if (EUUIDCustomType.UUID.getName().equalsIgnoreCase(simpleField.getType().getName()) && doCreate) {
                String uuidValue = UUID.randomUUID().toString();
                if (simpleField.isKey()) {
                    idValueMap.put(currentPath, uuidValue);
                }
                int size = document.createAccessor(currentPath).size();
                for (int i = 1; i <= size; i++) {
                    actions.add(new FieldUpdateAction(date, source, userName, currentPath + '[' + i + ']', StringUtils.EMPTY,
                            uuidValue, simpleField));
                    uuidValue = UUID.randomUUID().toString();
                }
            } else {
                if (simpleField.isKey()) {
                    Accessor accessor = document.createAccessor(currentPath);
                    if (!accessor.exist()) {
                        throw new IllegalStateException("Document was expected to contain id information at '" + currentPath
                                + "'");
                    }
                    idValueMap.put(currentPath, accessor.get());
                }
            }
        }
        path.pop();
        return super.visit(simpleField);
    }

    public boolean hasMetAutoIncrement() {
        return hasMetAutoIncrement;
    }

    public Map<String,String> getIdValueMap() {
        return idValueMap;
    }
}
