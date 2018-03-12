/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import java.util.Set;
import java.util.Stack;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;

class TypeComparison extends DefaultMetadataVisitor<Set<String>> {

    private final Stack<String> pathPrefix = new Stack<String>();

    private final Set<String> paths;

    private ComplexTypeMetadata type;

    TypeComparison(ComplexTypeMetadata type, Set<String> paths) {
        this.type = type;
        this.paths = paths;
    }

    private String getCurrentPath(FieldMetadata referenceField) {
        StringBuilder builder = new StringBuilder();
        for (String currentPathElement : pathPrefix) {
            builder.append(currentPathElement);
            builder.append('/');
        }
        return builder.append(referenceField.getName()).toString();
    }

    @Override
    public Set<String> visit(ComplexTypeMetadata complexType) {
        super.visit(complexType);
        return paths;
    }

    @Override
    public Set<String> visit(ReferenceFieldMetadata referenceField) {
        String currentPath = getCurrentPath(referenceField);
        try {
            if (!type.hasField(currentPath)) {
                paths.add(currentPath);
            }
        } catch (Exception e) {
            paths.add(currentPath);
        }
        return paths;
    }

    @Override
    public Set<String> visit(ContainedTypeFieldMetadata containedField) {
        String currentPath = getCurrentPath(containedField);
        pathPrefix.push(containedField.getName());
        {
            if (type.hasField(containedField.getName())) {
                type = containedField.getContainedType();
                super.visit(containedField);
                type = containedField.getContainingType();
            } else {
                paths.add(currentPath);
                containedField.getContainedType().accept(this);
            }
        }
        pathPrefix.pop();
        return paths;
    }

    @Override
    public Set<String> visit(FieldMetadata fieldMetadata) {
        String currentPath = getCurrentPath(fieldMetadata);
        try {
            if (!type.hasField(fieldMetadata.getName())) {
                paths.add(currentPath);
            }
        } catch (Exception e) {
            paths.add(currentPath);
        }
        return paths;
    }

    @Override
    public Set<String> visit(SimpleTypeFieldMetadata simpleField) {
        String currentPath = getCurrentPath(simpleField);
        try {
            if (!type.hasField(simpleField.getName())) {
                paths.add(currentPath);
            }
        } catch (Exception e) {
            paths.add(currentPath);
        }
        return paths;
    }

    @Override
    public Set<String> visit(EnumerationFieldMetadata enumField) {
        String currentPath = getCurrentPath(enumField);
        try {
            if (!type.hasField(enumField.getName())) {
                paths.add(currentPath);
            }
        } catch (Exception e) {
            paths.add(currentPath);
        }
        return paths;
    }
}
