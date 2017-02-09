/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import java.util.List;
import java.util.Map;

import org.talend.mdm.commmon.metadata.*;
import org.talend.mdm.commmon.metadata.compare.Change;
import org.talend.mdm.commmon.metadata.compare.Compare;
import org.talend.mdm.commmon.metadata.compare.HibernateStorageImpactAnalyzer;
import org.talend.mdm.commmon.metadata.compare.ModifyChange;

import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.storage.StorageResults;

public class HibernateStorageFetchDataAnalyzer extends HibernateStorageImpactAnalyzer {

    private HibernateStorage storage;

    public HibernateStorageFetchDataAnalyzer(HibernateStorage storage) {
        this.storage = storage;
    }

    protected final String STRING_DEFAULT_LENGTH = "255"; //$NON-NLS-1$

    public Map<Impact, List<Change>> analyzeImpacts(Compare.DiffResults diffResult) {
        // Modify actions
        for (ModifyChange modifyAction : diffResult.getModifyChanges()) {
            MetadataVisitable element = modifyAction.getElement();
            if (element instanceof ComplexTypeMetadata) {
                // Type modifications may include many things (inheritance changes for instance).
                // impactSort.get(Impact.HIGH).add(modifyAction);
            } else if (element instanceof FieldMetadata) {
                FieldMetadata previous = (FieldMetadata) modifyAction.getPrevious();
                FieldMetadata current = (FieldMetadata) modifyAction.getCurrent();

                if (current.isMandatory() && !previous.isMandatory()) {
                    storage.begin();
                    ComplexTypeMetadata objectType = previous.getContainingType().getEntity();
                    UserQueryBuilder qb = UserQueryBuilder.from(objectType).select(count()).where(emptyOrNull(previous));
                    StorageResults results = storage.fetch(qb.getSelect());
                    if (results.getCount() == 0) {
                        modifyAction.setHasNullValue(false);
                    } else {
                        modifyAction.setHasNullValue(true);
                    }
                }
            }
        }
        return super.analyzeImpacts(diffResult);
    }
}
