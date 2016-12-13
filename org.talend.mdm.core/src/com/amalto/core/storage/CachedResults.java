/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;

import com.amalto.core.storage.record.DataRecord;

public class CachedResults implements StorageResults {

    private final List<DataRecord> records;

    public CachedResults(List<DataRecord> records) {
        this.records = records;
    }

    public static StorageResults from(StorageResults otherResults) {
        return new CachedResults(IteratorUtils.toList(otherResults.iterator()));
    }

    @Override
    public int getSize() {
        return records.size();
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public void close() {
    }

    @Override
    public Iterator<DataRecord> iterator() {
        return records.iterator();
    }
}
