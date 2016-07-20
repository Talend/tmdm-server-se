/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server;

import com.amalto.core.query.user.Expression;
import com.amalto.core.server.MDMTransactionManager;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.transaction.StorageTransaction;
import com.amalto.core.storage.transaction.Transaction;
import com.amalto.core.storage.transaction.TransactionManager;
import junit.framework.TestCase;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.compare.Compare.DiffResults;
import org.talend.mdm.commmon.metadata.compare.HibernateStorageImpactAnalyzer;
import org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TransactionTest extends TestCase {

    private TransactionManager manager;

    @Override
    public void setUp() throws Exception {
        manager = new MDMTransactionManager();
        manager.init();
    }
    
    @Override
    public void tearDown() throws Exception {
        if(manager!=null){
            manager.close();
        }
    }

    public void testArguments() throws Exception {
        try {
            manager.create(null);
            fail("Null is not a correct argument");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            manager.associate(null);
            fail("Null is not a correct argument");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            manager.dissociate(null);
            fail("Null is not a correct argument");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            manager.get(null);
            fail("Null is not a correct argument");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            manager.remove(null);
            fail("Null is not a correct argument");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void testCreate() throws Exception {
        Transaction transaction = manager.create(Transaction.Lifetime.AD_HOC);
        assertNotNull(transaction);
        assertNotNull(transaction.getId());

        transaction = manager.create(Transaction.Lifetime.LONG);
        assertNotNull(transaction);
        assertNotNull(transaction.getId());
    }

    public void testAssociate() throws Exception {
        Transaction transaction = manager.create(Transaction.Lifetime.AD_HOC);
        assertTrue(manager.hasTransaction());
        manager.associate(transaction);
        assertTrue(manager.hasTransaction());
        manager.dissociate(transaction);
        assertFalse(manager.hasTransaction());

        transaction = manager.create(Transaction.Lifetime.LONG);
        assertTrue(manager.hasTransaction());
        manager.associate(transaction);
        assertTrue(manager.hasTransaction());
        manager.dissociate(transaction);
        assertFalse(manager.hasTransaction());
    }

    public void testGet() throws Exception {
        Transaction transaction = manager.create(Transaction.Lifetime.AD_HOC);
        assertNotNull(transaction);
        assertNotNull(transaction.getId());
        assertSame(transaction, manager.get(transaction.getId()));

        transaction = manager.create(Transaction.Lifetime.LONG);
        assertNotNull(transaction);
        assertNotNull(transaction.getId());
        assertSame(transaction, manager.get(transaction.getId()));
    }

    public void testRemove() throws Exception {
        Transaction transaction = manager.create(Transaction.Lifetime.AD_HOC);
        assertNotNull(transaction);
        assertNotNull(manager.get(transaction.getId()));
        manager.remove(transaction);
        assertNull(manager.get(transaction.getId()));

        transaction = manager.create(Transaction.Lifetime.AD_HOC);
        assertNotNull(transaction);
        assertNotNull(manager.get(transaction.getId()));
        manager.remove(transaction);
        assertNull(manager.get(transaction.getId()));
    }

    public void testAdHocTransactionCommit() throws Exception {
        Transaction transaction = manager.create(Transaction.Lifetime.AD_HOC);
        Storage storage = new MockStorage(true);
        transaction.include(storage);
        storage.commit();
        assertNull(manager.get(transaction.getId()));

        transaction.commit(); // Useless but should not fail anyway.
    }

    public void testLongTransactionCommit() throws Exception {
        Transaction transaction = manager.create(Transaction.Lifetime.LONG);
        Storage storage = new MockStorage(true);
        transaction.include(storage);
        storage.commit();
        assertNotNull(manager.get(transaction.getId()));

        transaction.commit();
        assertNull(manager.get(transaction.getId()));
    }

    public void testAdHocTransactionRollback() throws Exception {
        Transaction transaction = manager.create(Transaction.Lifetime.AD_HOC);
        Storage storage = new MockStorage(true);
        transaction.include(storage);
        storage.rollback();
        assertNull(manager.get(transaction.getId()));

        transaction.rollback(); // Useless but should not fail anyway.
    }

    public void testLongTransactionRollback() throws Exception {
        Transaction transaction = manager.create(Transaction.Lifetime.LONG);
        Storage storage = new MockStorage(true);
        transaction.include(storage);
        storage.rollback();
        assertNotNull(manager.get(transaction.getId()));

        transaction.rollback();
        assertNull(manager.get(transaction.getId()));
    }

    public void testNonTransactionalStorage() throws Exception {
        Transaction transaction = manager.create(Transaction.Lifetime.AD_HOC);
        Storage storage = new MockStorage(false);
        try {
            transaction.include(storage);
            fail("Expected to fail: storage does not support TRANSACTION.");
        } catch (IllegalArgumentException e) {
            // Expected.
        }
    }

    class MockStorage implements Storage {

        private final int capabilities;

        MockStorage(boolean supportTransactions) {
            if (supportTransactions) {
                capabilities = CAP_TRANSACTION | CAP_FULL_TEXT | CAP_INTEGRITY;
            } else {
                capabilities = CAP_FULL_TEXT | CAP_INTEGRITY;
            }
        }

        @Override
        public Storage asInternal() {
            return this;
        }

        @Override
        public int getCapabilities() {
            return capabilities;
        }

        @Override
        public StorageTransaction newStorageTransaction() {
            return new MockStorageTransaction();
        }

        @Override
        public void init(DataSourceDefinition dataSource) {
        }

        @Override
        public void prepare(MetadataRepository repository, Set<Expression> optimizedExpressions, boolean force,
                boolean dropExistingData) {
        }

        @Override
        public void prepare(MetadataRepository repository, boolean dropExistingData) {
        }

        @Override
        public MetadataRepository getMetadataRepository() {
            return null;
        }

        @Override
        public StorageResults fetch(Expression userQuery) {
            return null;
        }

        @Override
        public void update(DataRecord record) {
        }

        @Override
        public void update(Iterable<DataRecord> records) {
        }

        @Override
        public void delete(Expression userQuery) {
        }

        @Override
        public void delete(DataRecord record) {
        }

        @Override
        public void close() {
        }

        @Override
        public void close(boolean dropExistingData) {
        }

        @Override
        public void begin() {
            manager.currentTransaction().include(this).begin();
        }

        @Override
        public void commit() {
            manager.currentTransaction().include(this).commit();
        }

        @Override
        public void rollback() {
            manager.currentTransaction().include(this).rollback();
        }

        @Override
        public void end() {
        }

        @Override
        public void reindex() {
        }

        @Override
        public Set<String> getFullTextSuggestion(String keyword, FullTextSuggestion mode, int suggestionSize) {
            return Collections.emptySet();
        }

        @Override
        public String getName() {
            return "Mock storage";
        }

        @Override
        public DataSource getDataSource() {
            return null;
        }

        @Override
        public StorageType getType() {
            return StorageType.MASTER;
        }

        @Override
        public ImpactAnalyzer getImpactAnalyzer() {
            return new HibernateStorageImpactAnalyzer();
        }

        @Override
        public void adapt(MetadataRepository newRepository, boolean force) {
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        private class MockStorageTransaction extends StorageTransaction {

            @Override
            public Storage getStorage() {
                return MockStorage.this;
            }

            @Override
            public void begin() {
            }

            @Override
            public boolean hasFailed() {
                return false;
            }
        }

        @Override
        public List<ComplexTypeMetadata> findSortedTypesToDrop(DiffResults diffResults, boolean force) {
            return null;
        }
    }
}
