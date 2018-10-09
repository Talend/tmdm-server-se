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

public class HibernateStorageDataAnaylzer extends HibernateStorageImpactAnalyzer {

    private static final Logger LOGGER = Logger.getLogger(HibernateStorageDataAnaylzer.class);

    private HibernateStorage storage;

    public HibernateStorageDataAnaylzer(HibernateStorage storage) {
        this.storage = storage;
    }

    protected final String STRING_DEFAULT_LENGTH = "255"; //$NON-NLS-1$

    public Map<Impact, List<Change>> analyzeImpacts(Compare.DiffResults diffResult) {
        // Modify actions
        for (ModifyChange modifyAction : diffResult.getModifyChanges()) {
            MetadataVisitable element = modifyAction.getElement();
            if (element instanceof FieldMetadata) {
                FieldMetadata previous = (FieldMetadata) modifyAction.getPrevious();
                FieldMetadata current = (FieldMetadata) modifyAction.getCurrent();

                if (current.isMandatory() && !previous.isMandatory()) {
                    try {
                        storage.begin();
                        ComplexTypeMetadata objectType = previous.getContainingType().getEntity();
                        UserQueryBuilder qb = UserQueryBuilder.from(objectType).select(count()).where(emptyOrNull(previous));
                        StorageResults results = storage.fetch(qb.getSelect());
                        if (results.getCount() == 0) {
                            modifyAction.setHasNullValue(false);
                        } else {
                            modifyAction.setHasNullValue(true);
                        }
                        results.close();
                    } catch (Exception e) {
                        LOGGER.error("Hibernate Storage query data anaylzer failure", e);
                        storage.rollback();
                    } finally {
                        storage.commit();
                    }
                }
            }
        }

        Map<FieldMetadata, RemoveChange> removeReference = new HashMap<>();
        Map<RemoveChange, ComplexTypeMetadata> renamedReference = new HashMap<>();
        for (RemoveChange removeAction : diffResult.getRemoveChanges()) {
            MetadataVisitable element = removeAction.getElement();
            if (element instanceof ReferenceFieldMetadata) {
                removeReference.put(((ReferenceFieldMetadata) element).getReferencedField(), removeAction);
            }
        }
        for (AddChange removeAction : diffResult.getAddChanges()) {
            MetadataVisitable element = removeAction.getElement();
            if (element instanceof ReferenceFieldMetadata) {
                if (removeReference.containsKey(((ReferenceFieldMetadata) element).getReferencedField())) {
                    renamedReference.put((removeReference.get(((ReferenceFieldMetadata) element).getReferencedField())),
                            ((ReferenceFieldMetadata) element).getContainingType().getEntity());
                }
            }
        }
        if (!renamedReference.isEmpty()) {
            for (Map.Entry<RemoveChange, ComplexTypeMetadata> entry : renamedReference.entrySet()) {
                try {
                    storage.begin();
                    ComplexTypeMetadata objectType = entry.getValue();
                    UserQueryBuilder qb = UserQueryBuilder.from(objectType).select(count())
                            .where(not(emptyOrNull((FieldMetadata) entry.getKey().getElement())));
                    StorageResults results = storage.fetch(qb.getSelect());
                    if (results.getCount() == 0) {
                        entry.getKey().setContainsData(false);
                    } else {
                        entry.getKey().setContainsData(true);
                    }
                    results.close();
                } catch (Exception e) {
                    LOGGER.error("Hibernate Storage query data anaylzer failure", e);
                    storage.rollback();
                } finally {
                    storage.commit();
                }
            }
        }
        return super.analyzeImpacts(diffResult);
    }
}
