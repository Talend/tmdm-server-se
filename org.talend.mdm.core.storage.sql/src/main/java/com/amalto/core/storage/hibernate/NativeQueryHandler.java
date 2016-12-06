/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import org.talend.mdm.commmon.metadata.*;
import com.amalto.core.query.user.NativeQuery;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

class NativeQueryHandler extends AbstractQueryHandler {

    private static final String SELECT_KEYWORD = "SELECT"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(NativeQueryHandler.class);

    NativeQueryHandler(Storage storage, StorageClassLoader storageClassLoader, Session session, Set<ResultsCallback> callbacks) {
        super(storage, storageClassLoader, session, null, null, callbacks);
    }

    @Override
    public StorageResults visit(NativeQuery nativeQuery) {
        Query query;
        String queryText = nativeQuery.getQueryText().trim();
        String selectPrefix = queryText.substring(0, SELECT_KEYWORD.length());
        if (SELECT_KEYWORD.equalsIgnoreCase(selectPrefix)) {
            // Hibernate support direct SQL
            query = session.createSQLQuery(queryText);
            List list = query.list();
            return new NativeQueryStorageResults(list, callbacks);
        } else {
            // Hibernate support write queries (update / delete) only via Work interface.
            session.doWork(new UpdateQueryWork(nativeQuery));
            return new StorageResults() {
                @Override
                public int getSize() {
                    return 0;
                }

                @Override
                public int getCount() {
                    return 0;
                }

                @Override
                public void close() {
                }

                @Override
                public Iterator<DataRecord> iterator() {
                    return Collections.<DataRecord>emptySet().iterator();
                }
            };
        }
    }

    private static class NativeQueryStorageResults implements StorageResults {

        private final List list;

        private final NativeIterator nativeIterator;

        public NativeQueryStorageResults(List list, Set<ResultsCallback> callbacks) {
            this.list = list;
            this.nativeIterator = new NativeIterator(list.iterator(), callbacks);
        }

        @Override
        public int getSize() {
            return list.size();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public void close() {
            try {
                nativeIterator.close();
            } catch (IOException e) {
                throw new RuntimeException("Unexpected exception during close.", e);
            }
        }

        @Override
        public Iterator<DataRecord> iterator() {
            return nativeIterator;
        }

    }

    private static class NativeIterator implements CloseableIterator<DataRecord> {

        private static final Logger LOGGER = Logger.getLogger(NativeIterator.class);

        private final Iterator iterator;

        private final Set<ResultsCallback> callbacks;

        private boolean firstNextCall = true;

        public NativeIterator(Iterator iterator, Set<ResultsCallback> callbacks) {
            this.iterator = iterator;
            this.callbacks = callbacks;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = iterator.hasNext();
            if (!hasNext) {
                try {
                    close();
                } catch (IOException e) {
                    throw new RuntimeException("Unexpected exception during close.", e);
                }
            }
            return hasNext;
        }

        @Override
        public DataRecord next() {
            if (firstNextCall) {
                for (ResultsCallback callback : callbacks) {
                    callback.onBeginOfResults();
                }
                firstNextCall = false;
            }
            ComplexTypeMetadata explicitProjectionType = new ComplexTypeMetadataImpl(StringUtils.EMPTY, Storage.PROJECTION_TYPE, false);
            DataRecord nativeResult = new DataRecord(explicitProjectionType, UnsupportedDataRecordMetadata.INSTANCE);
            Object next = iterator.next();
            if (next instanceof Object[]) {
                int i = 0;
                Object[] objectArray = (Object[]) next;
                for (Object o : objectArray) {
                    SimpleTypeMetadata fieldType = null ;
                    if (o != null) {
                        fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, MetadataUtils.getType(o.getClass().getName()));
                    } else {
                        fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, MetadataUtils.getType(String.class.getName()));
                    }
                    String name = "col" + i++; //$NON-NLS-1$
                    SimpleTypeFieldMetadata colField = new SimpleTypeFieldMetadata(explicitProjectionType, false, false, false, name, fieldType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY);
                    explicitProjectionType.addField(colField);
                    nativeResult.set(colField, o);
                }
            } else {
                SimpleTypeMetadata fieldType = null ;
                if (next != null) {
                    fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, MetadataUtils.getType(next.getClass().getName()));
                } else {
                    fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, MetadataUtils.getType(String.class.getName()));
                }
                String name = "col0"; //$NON-NLS-1$
                SimpleTypeFieldMetadata colField = new SimpleTypeFieldMetadata(explicitProjectionType, false, false, false, name, fieldType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY);
                explicitProjectionType.addField(colField);
                nativeResult.set(colField, next);
            }
            explicitProjectionType.freeze();
            return nativeResult;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws IOException {
            for (ResultsCallback callback : callbacks) {
                try {
                    callback.onEndOfResults();
                } catch (Throwable t) {
                    // Catch Throwable and log it (to ensure all callbacks get called).
                    LOGGER.error("End of result callback exception", t);
                }
            }
        }
    }

    private class UpdateQueryWork implements Work {
        private final NativeQuery nativeQuery;

        public UpdateQueryWork(NativeQuery nativeQuery) {
            this.nativeQuery = nativeQuery;
        }

        @Override
        public void execute(Connection connection) throws SQLException {
            Statement statement = null;
            Transaction transaction = session.getTransaction();
            try {
                statement = connection.createStatement();
                statement.executeUpdate(nativeQuery.getQueryText());
                transaction.commit(); // Don't forget to commit changes
            } catch (Exception e) {
                transaction.rollback();
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        LOGGER.error("Error on statement close during native query execution.", e);
                    }
                }
            }

        }
    }
}
