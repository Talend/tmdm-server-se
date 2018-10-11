/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import static com.amalto.core.query.user.UserQueryBuilder.count;
import static com.amalto.core.query.user.UserQueryBuilder.emptyOrNull;
import static com.amalto.core.query.user.UserQueryBuilder.not;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.*;
import org.talend.mdm.commmon.metadata.compare.AddChange;
import org.talend.mdm.commmon.metadata.compare.Change;
import org.talend.mdm.commmon.metadata.compare.Compare;
import org.talend.mdm.commmon.metadata.compare.HibernateStorageImpactAnalyzer;
import org.talend.mdm.commmon.metadata.compare.ModifyChange;
import org.talend.mdm.commmon.metadata.compare.RemoveChange;

import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.storage.StorageResults;

@SuppressWarnings("nls")
public class HibernateStorageDataAnaylzer extends HibernateStorageImpactAnalyzer {

    private static final Logger LOGGER = Logger.getLogger(HibernateStorageDataAnaylzer.class);

    private HibernateStorage storage;

    public HibernateStorageDataAnaylzer(HibernateStorage storage) {
        this.storage = storage;
    }

    protected final String STRING_DEFAULT_LENGTH = "255";

    @Override
    public Map<Impact, List<Change>> analyzeImpacts(Compare.DiffResults diffResult) {
        // Modify actions
        for (ModifyChange modifyAction : diffResult.getModifyChanges()) {
            MetadataVisitable element = modifyAction.getElement();
            if (element instanceof FieldMetadata) {
                FieldMetadata previous = (FieldMetadata) modifyAction.getPrevious();
                FieldMetadata current = (FieldMetadata) modifyAction.getCurrent();

                if (current.isMandatory() && !previous.isMandatory()) {
                    int count = fetchFieldCountOfNull(previous.getContainingType().getEntity(), previous);
                    modifyAction.setHasNullValue(count > 0);
                }
            }
        }

        Map<RemoveChange, ComplexTypeMetadata> renamedReferenceFieldMap = getRenamedFKFieldMap(diffResult);
        for (Map.Entry<RemoveChange, ComplexTypeMetadata> entry : renamedReferenceFieldMap.entrySet()) {
            RemoveChange removeChange = entry.getKey();
            int count = fetchFieldCountOfNotNull(entry.getValue(), (FieldMetadata) removeChange.getElement());
            removeChange.setContainsData(count > 0);
        }
        return super.analyzeImpacts(diffResult);
    }

    private Map<RemoveChange, ComplexTypeMetadata> getRenamedFKFieldMap(Compare.DiffResults diffResult) {
        Map<FieldMetadata, RemoveChange> removeReferenceFieldMap = new HashMap<>();
        Map<RemoveChange, ComplexTypeMetadata> renamedReferenceFieldMap = new HashMap<>();
        for (RemoveChange removeAction : diffResult.getRemoveChanges()) {
            MetadataVisitable element = removeAction.getElement();
            if (element instanceof ReferenceFieldMetadata) {
                removeReferenceFieldMap.put(((ReferenceFieldMetadata) element).getReferencedField(), removeAction);
            }
        }
        if (removeReferenceFieldMap.isEmpty()) {
            return renamedReferenceFieldMap;
        }
        for (AddChange addAction : diffResult.getAddChanges()) {
            MetadataVisitable element = addAction.getElement();
            if (element instanceof ReferenceFieldMetadata) {
                FieldMetadata referenceField = ((ReferenceFieldMetadata) element).getReferencedField();
                if (removeReferenceFieldMap.containsKey(referenceField)) {
                    renamedReferenceFieldMap.put((removeReferenceFieldMap.get(referenceField)),
                            ((ReferenceFieldMetadata) element).getContainingType().getEntity());
                }
            }
        }
        return renamedReferenceFieldMap;
    }

    private int fetchFieldCountOfNotNull(ComplexTypeMetadata entry, FieldMetadata field) {
        UserQueryBuilder qb = UserQueryBuilder.from(entry).select(count()).where(not(emptyOrNull(field)));
        return fetchFieldCountByUserQuery(qb);
    }

    private int fetchFieldCountOfNull(ComplexTypeMetadata entry, FieldMetadata field) {
        UserQueryBuilder qb = UserQueryBuilder.from(entry).select(count()).where(emptyOrNull(field));
        return fetchFieldCountByUserQuery(qb);
    }

    private int fetchFieldCountByUserQuery(UserQueryBuilder qb) {
        int count = 0;
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            count = results.getCount();
        } finally {
            results.close();
        }
        return count;
    }
}
