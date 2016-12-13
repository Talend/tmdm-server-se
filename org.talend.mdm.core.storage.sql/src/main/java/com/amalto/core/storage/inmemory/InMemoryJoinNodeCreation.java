/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.inmemory;

import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.query.user.*;
import org.apache.commons.lang.NotImplementedException;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import java.util.List;

/**
*
*/
class InMemoryJoinNodeCreation extends VisitorAdapter<Void> {

    private final InMemoryJoinNode root;

    private FieldMetadata fieldMetadata;

    public InMemoryJoinNodeCreation(InMemoryJoinNode root) {
        this.root = root;
    }

    private static void buildExecutionTree(InMemoryJoinNode root, FieldMetadata field, Condition condition) {
        // Build nodes (based on path)
        InMemoryJoinNode current = root;
        List<FieldMetadata> path = StorageMetadataUtils.path(root.type, field);
        for (FieldMetadata fieldMetadata : path) {
            InMemoryJoinNode node = new InMemoryJoinNode();
            node.name = fieldMetadata.getName();
            node.type = fieldMetadata.getContainingType();
            node.childProperty = fieldMetadata;
            if (!current.children.containsKey(node)) {
                current.children.put(node, node);
                current = node;
            } else {
                current = current.children.get(node);
            }
        }
        current.expression = UserQueryBuilder.from(current.type)
                .selectId(current.type)
                .where(condition)
                .getSelect();
    }

    @Override
    public Void visit(Compare condition) {
        FieldMetadata previous = fieldMetadata;
        condition.getLeft().accept(this);
        if (fieldMetadata != previous) {
            buildExecutionTree(root, fieldMetadata, condition);
        }
        return null;
    }

    @Override
    public Void visit(BinaryLogicOperator condition) {
        condition.getLeft().accept(this);
        FieldMetadata leftField = fieldMetadata;
        condition.getRight().accept(this);
        FieldMetadata rightField = fieldMetadata;
        // Union and Intersection
        List<FieldMetadata> leftPath = StorageMetadataUtils.path(root.type, leftField);
        List<FieldMetadata> rightPath = StorageMetadataUtils.path(root.type, rightField);
        InMemoryJoinNode lastCommonNode = root;
        for (int i = 0; i < leftPath.size(); i++) {
            if (!rightPath.get(i).equals(leftPath.get(i))) {
                break;
            }
            InMemoryJoinNode node = new InMemoryJoinNode();
            node.name = leftPath.get(i).getName();
            lastCommonNode = root.children.get(node);
        }
        if (condition.getPredicate() == Predicate.AND) {
            lastCommonNode.merge = InMemoryJoinNode.Merge.INTERSECTION;
        } else if (condition.getPredicate() == Predicate.OR) {
            lastCommonNode.merge = InMemoryJoinNode.Merge.UNION;
        } else {
            throw new NotImplementedException("Not implemented: " + condition.getPredicate());
        }
        return null;
    }

    @Override
    public Void visit(Field field) {
        fieldMetadata = field.getFieldMetadata();
        return null;
    }

    @Override
    public Void visit(UnaryLogicOperator condition) {
        FieldMetadata previous = fieldMetadata;
        condition.getCondition().accept(this);
        if (fieldMetadata != previous) {
            buildExecutionTree(root, fieldMetadata, condition);
        }
        return null;
    }

    @Override
    public Void visit(IsEmpty isEmpty) {
        FieldMetadata previous = fieldMetadata;
        isEmpty.getField().accept(this);
        if (fieldMetadata != previous) {
            buildExecutionTree(root, fieldMetadata, isEmpty);
        }
        return null;
    }

    @Override
    public Void visit(NotIsEmpty notIsEmpty) {
        FieldMetadata previous = fieldMetadata;
        notIsEmpty.getField().accept(this);
        if (fieldMetadata != previous) {
            buildExecutionTree(root, fieldMetadata, notIsEmpty);
        }
        return null;
    }

    @Override
    public Void visit(IsNull isNull) {
        FieldMetadata previous = fieldMetadata;
        isNull.getField().accept(this);
        if (fieldMetadata != previous) {
            buildExecutionTree(root, fieldMetadata, isNull);
        }
        return null;
    }

    @Override
    public Void visit(NotIsNull notIsNull) {
        FieldMetadata previous = fieldMetadata;
        notIsNull.getField().accept(this);
        if (fieldMetadata != previous) {
            buildExecutionTree(root, fieldMetadata, notIsNull);
        }
        return null;
    }

    @Override
    public Void visit(IndexedField indexedField) {
        fieldMetadata = indexedField.getFieldMetadata();
        return null;
    }
}
