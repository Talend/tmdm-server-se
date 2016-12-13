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

import com.amalto.core.storage.CloseableIterator;
import com.amalto.core.storage.record.DataRecord;

import java.io.IOException;

/**
*
*/
public class EmptyIterator implements CloseableIterator<DataRecord> {
    public static final CloseableIterator<DataRecord> INSTANCE = new EmptyIterator();

    private EmptyIterator() {
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public DataRecord next() {
        return null;
    }

    @Override
    public void remove() {
    }
}
