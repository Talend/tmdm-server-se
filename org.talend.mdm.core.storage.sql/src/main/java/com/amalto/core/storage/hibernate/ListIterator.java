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

import com.amalto.core.storage.CloseableIterator;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.ObjectDataRecordReader;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class ListIterator implements CloseableIterator<DataRecord> {

    private final static Logger LOGGER = Logger.getLogger(ListIterator.class);

    private final static Map<ComplexTypeMetadata, ObjectDataRecordReader> typeToReader = new HashMap<ComplexTypeMetadata, ObjectDataRecordReader>();

    private final MappingRepository storageRepository;

    private final StorageClassLoader storageClassLoader;

    private final Iterator iterator;

    private final Set<ResultsCallback> callbacks;

    private boolean firstNextCall = true;

    public ListIterator(MappingRepository storageRepository, StorageClassLoader storageClassLoader, Iterator iterator, Set<ResultsCallback> callbacks) {
        this.storageRepository = storageRepository;
        this.storageClassLoader = storageClassLoader;
        this.iterator = iterator;
        this.callbacks = callbacks;
    }

    public boolean hasNext() {
        boolean hasNext;
        try {
            hasNext = iterator.hasNext();
            if (!hasNext) {
                notifyCallbacks();
            }
        } catch (HibernateException e) {
            notifyCallbacks(); // In case of exception, notify the callbacks so statelessSession can be closed.
            throw e;
        }
        return hasNext;
    }

    public DataRecord next() {
        if (firstNextCall) {
            for (ResultsCallback callback : callbacks) {
                callback.onBeginOfResults();
            }
            firstNextCall = false;
        }
        Object next;
        try {
            next = iterator.next();
        } catch (HibernateException e) {
            notifyCallbacks(); // In case of exception, notify the callbacks so statelessSession can be closed.
            throw e;
        }

        ComplexTypeMetadata type = storageClassLoader.getTypeFromClass(next.getClass());
        ObjectDataRecordReader reader = getReader(type);
        if (!(next instanceof Wrapper)) {
            throw new IllegalArgumentException("Result object is not an instance of " + Wrapper.class.getName());
        }
        return reader.read(storageRepository.getMappingFromDatabase(type), (Wrapper) next);
    }

    // Cache type readers
    private static ObjectDataRecordReader getReader(ComplexTypeMetadata type) {
        synchronized (typeToReader) {
            ObjectDataRecordReader reader = typeToReader.get(type);
            if (reader == null) {
                reader = new ObjectDataRecordReader();
                typeToReader.put(type, reader);
            }
            return reader;
        }
    }

    public static void resetTypeReaders() {
        synchronized (typeToReader) {
            typeToReader.clear();
        }
    }

    public void remove() {
        iterator.remove();
    }

    private void notifyCallbacks() {
        for (ResultsCallback callback : callbacks) {
            try {
                callback.onEndOfResults();
            } catch (Throwable t) {
                // Catch Throwable and log it (to ensure all callbacks get called).
                LOGGER.error("End of result callback exception", t);
            }
        }
    }

    public void close() throws IOException {
        notifyCallbacks();
    }
}
