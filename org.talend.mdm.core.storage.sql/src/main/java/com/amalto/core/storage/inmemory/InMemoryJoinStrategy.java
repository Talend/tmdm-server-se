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

package com.amalto.core.storage.inmemory;

import com.amalto.core.query.user.*;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.hibernate.MappingRepository;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import java.util.*;

public class InMemoryJoinStrategy extends VisitorAdapter<StorageResults> {

    private final Storage storage;

    private final MappingRepository mappings;

    public InMemoryJoinStrategy(Storage storage, MappingRepository mappings) {
        this.storage = storage;
        this.mappings = mappings;
    }

    @Override
    public StorageResults visit(Select select) {
        // Get main type
        List<ComplexTypeMetadata> types = select.getTypes();
        if (types.isEmpty()) {
            throw new IllegalArgumentException("Select clause must select one type.");
        }
        if (types.size() > 1) {
            throw new IllegalArgumentException("Select clause must select only one type (was " + types.size() + ").");
        }
        // Create root (selected type)
        ComplexTypeMetadata type = types.get(0);
        InMemoryJoinNode root = new InMemoryJoinNode();
        root.name = type.getName();
        root.type = type;
        // Get conditions paths
        if (select.getCondition() != null) {
            InMemoryJoinNodeCreation inMemoryJoinNodeCreation = new InMemoryJoinNodeCreation(root);
            select.getCondition().accept(inMemoryJoinNodeCreation);
        }
        // Return Storage Results
        Select selectCopy = select.copy();
        selectCopy.setCondition(null);
        root.expression = selectCopy;
        return new InMemoryJoinResults(storage, mappings, root);
    }

}
