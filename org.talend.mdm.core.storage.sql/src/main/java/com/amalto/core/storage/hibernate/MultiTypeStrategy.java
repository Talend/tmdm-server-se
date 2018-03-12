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

package com.amalto.core.storage.hibernate;

import com.amalto.core.query.user.*;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import org.apache.commons.collections.set.ListOrderedSet;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import java.util.*;

public class MultiTypeStrategy extends VisitorAdapter<StorageResults> {

    private final Storage storage;

    public MultiTypeStrategy(Storage storage) {
        this.storage = storage;
    }

    @Override
    public StorageResults visit(Select select) {
        /*
         * Implementation consumes all results before they're sent back to user: current API doesn't allow "long"
         * conversation with storage. Using iterators, the first iterator would close the connection for the remaining
         * ones.
         */
        List<ComplexTypeMetadata> types = select.getTypes();
        if (types.isEmpty()) {
            throw new IllegalArgumentException("Select clause must select one type.");
        }
        int start = 0;
        int limit = Integer.MAX_VALUE;
        Paging paging = select.getPaging();
        if (paging != null) {
            start = paging.getStart();
            limit = select.getPaging().getLimit();
        }
        int currentStart = 0;
        int currentLimit = 0;
        int count = 0;
        final Set<DataRecord> results = new ListOrderedSet();
        for (ComplexTypeMetadata type : types) {
            UserQueryBuilder qb = UserQueryBuilder.from(type);
            qb.where(select.getCondition()); // TODO Would be better to ensure condition applies to type.
            for (OrderBy current : select.getOrderBy()) {
                qb.orderBy(current.getExpression(), current.getDirection());
            }
            StorageResults dataRecords = storage.fetch(qb.getSelect()); // Expects an active transaction here
            try {
                count += dataRecords.getCount();
                for (DataRecord result : dataRecords) {
                    if (currentLimit < limit) {
                        if (currentStart >= start) {
                            if (results.add(result)) { // Avoids duplicates in results (if any).
                                currentLimit++;
                            }
                        }
                        currentStart++;
                    }
                }
            } finally {
                dataRecords.close();
            }
        }
        final int totalCount = count;
        return new StorageResults() {
            @Override
            public int getSize() {
                return results.size();
            }

            @Override
            public int getCount() {
                return totalCount;
            }

            @Override
            public void close() {
            }

            @Override
            public Iterator<DataRecord> iterator() {
                return results.iterator();
            }
        };
    }
}
