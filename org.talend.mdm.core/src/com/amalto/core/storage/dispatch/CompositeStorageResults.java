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

package com.amalto.core.storage.dispatch;

import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import org.apache.commons.collections.iterators.IteratorChain;

import java.util.Iterator;
import java.util.List;

class CompositeStorageResults implements StorageResults {
    private final List<StorageResults> results;

    public CompositeStorageResults(List<StorageResults> results) {
        this.results = results;
    }

    @Override
    public int getSize() {
        int size = 0;
        for (StorageResults result : results) {
            size += result.getSize();
        }
        return size;
    }

    @Override
    public int getCount() {
        int count = 0;
        for (StorageResults result : results) {
            count += result.getCount();
        }
        return count;
    }

    @Override
    public void close() {
        for (StorageResults result : results) {
            result.close();
        }
    }

    @Override
    public Iterator<DataRecord> iterator() {
        IteratorChain chain = new IteratorChain();
        for (StorageResults result : results) {
            chain.addIterator(result.iterator());
        }
        return chain;
    }
}
