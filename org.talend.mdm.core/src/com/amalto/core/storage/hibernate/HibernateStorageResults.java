/*
 * Copyright (C) 2006-2012 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import com.amalto.core.metadata.FieldMetadata;
import com.amalto.core.query.user.*;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class HibernateStorageResults implements StorageResults {

    private final int size;

    private final CloseableIterator<DataRecord> iterator;

    private final Storage storage;

    private final Select select;

    public HibernateStorageResults(Storage storage, Select select, CloseableIterator<DataRecord> iterator) {
        this.storage = storage;
        this.select = select;
        this.size = select.getPaging().getLimit();
        this.iterator = iterator;
    }

    public int getSize() {
        // Size = Integer.MAX_VALUE means no paging, so results size is then the count.
        if (size == Integer.MAX_VALUE) {
            return getCount();
        }
        return size;
    }

    public int getCount() {
        try {
            Select countSelect = select.copy();
            List<TypedExpression> selectedFields = countSelect.getSelectedFields();
            Paging paging = countSelect.getPaging();
            selectedFields.clear();
            selectedFields.add(UserQueryBuilder.count());
            paging.setLimit(1);
            paging.setStart(0);
            countSelect.setProjection(true);
            countSelect.setOrderBy(null);

            StorageResults countResult = storage.fetch(countSelect);
            Iterator<DataRecord> resultIterator = countResult.iterator();
            DataRecord count = resultIterator.next();

            Set<FieldMetadata> setFields = count.getSetFields();
            String countAsString = null;
            for (FieldMetadata setField : setFields) {
                countAsString = String.valueOf(count.get(setField));
            }
            if (countAsString == null) {
                throw new RuntimeException("Count returned no result");
            }
            return Integer.parseInt(countAsString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Iterator<DataRecord> iterator() {
        return iterator;
    }

    public void close() {
        try {
            iterator.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
