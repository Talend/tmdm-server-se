/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.task;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.compare.Compare.DiffResults;
import org.talend.mdm.commmon.metadata.compare.HibernateStorageImpactAnalyzer;
import org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Document;

import com.amalto.core.history.DeleteType;
import com.amalto.core.query.user.BinaryLogicOperator;
import com.amalto.core.query.user.Compare;
import com.amalto.core.query.user.Condition;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.IntegerConstant;
import com.amalto.core.query.user.LongConstant;
import com.amalto.core.query.user.Predicate;
import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.metadata.StagingStatus;
import com.amalto.core.query.user.metadata.Timestamp;
import com.amalto.core.save.SaverSession;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.transaction.StorageTransaction;

import junit.framework.TestCase;

public class MetadataRepositoryTaskTest extends TestCase {

    private static Logger LOG = Logger.getLogger(MetadataRepositoryTaskTest.class);

    @Override
    public void tearDown() throws Exception {
        ServerContext.INSTANCE.close();
    }

    @Override
    public void setUp() throws Exception {
        ServerContext.INSTANCE.close();
        ServerContext.INSTANCE.get(new MockServerLifecycle());
    }

    public void testValidationFilter() {
        Server server = ServerContext.INSTANCE.get();
        assertNotNull(server);

        String metadataRepositoryId = "../query/metadata.xsd"; //$NON-NLS-1$
        MetadataRepository metadataRepository = server.getMetadataRepositoryAdmin().get(metadataRepositoryId);
        assertNotNull(metadataRepository);

        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);

        Storage staging = storageAdmin.create(metadataRepositoryId, "Storage", StorageType.STAGING, "H2-DS1"); //$NON-NLS-1$//$NON-NLS-2$

        Storage user = storageAdmin.create(metadataRepositoryId, "Storage", StorageType.MASTER, "H2-DS1"); //$NON-NLS-1$//$NON-NLS-2$

        MetadataRepository userRepository = user.getMetadataRepository();

        MetadataRepository stagingRepository = staging.getMetadataRepository();
        assertNotNull(stagingRepository);

        Filter filter = getFilter();

        ComplexTypeMetadata product = userRepository.getComplexType("Product"); //$NON-NLS-1$
        ComplexTypeMetadata productFamily = userRepository.getComplexType("ProductFamily"); //$NON-NLS-1$
        ComplexTypeMetadata store = userRepository.getComplexType("Store"); //$NON-NLS-1$
        ComplexTypeMetadata person = userRepository.getComplexType("Person"); //$NON-NLS-1$

        assertFalse(filter.exclude(product));
        assertFalse(filter.exclude(productFamily));
        assertFalse(filter.exclude(store));
        assertTrue(filter.exclude(person));

        MockStorage mockStorage = new MockStorage();
        MDMValidationTask task = new MDMValidationTask(mockStorage, mockStorage, userRepository, null, new MockCommitter(),
                new ClosureExecutionStats(), filter);

        Select selectProduct = filter.doFilter(task, product);
        Condition cond = selectProduct.getCondition();
        assertTrue(cond instanceof BinaryLogicOperator);
        BinaryLogicOperator root = (BinaryLogicOperator) cond;
        Condition leftc = root.getLeft();
        assertTrue(leftc instanceof BinaryLogicOperator);
        BinaryLogicOperator leftcc = (BinaryLogicOperator) leftc;

        assertTrue(leftcc.getLeft() instanceof Compare);
        Compare leftcc_l_c = (Compare) leftcc.getLeft();
        assertTrue(leftcc_l_c.getLeft() instanceof StagingStatus);
        assertEquals(Predicate.EQUALS, leftcc_l_c.getPredicate());
        assertTrue(leftcc_l_c.getRight() instanceof IntegerConstant);
        assertEquals(Integer.valueOf(204), ((IntegerConstant) leftcc_l_c.getRight()).getValue());

        assertEquals(Predicate.OR, leftcc.getPredicate());

        assertTrue(leftcc.getRight() instanceof Compare);
        Compare leftcc_r_c = (Compare) leftcc.getRight();

        assertTrue(leftcc_r_c.getLeft() instanceof StagingStatus);
        assertEquals(Predicate.EQUALS, leftcc_r_c.getPredicate());
        assertTrue(leftcc_r_c.getRight() instanceof IntegerConstant);
        assertEquals(Integer.valueOf(404), ((IntegerConstant) leftcc_r_c.getRight()).getValue());

        assertEquals(Predicate.AND, root.getPredicate());
        Condition rightc = root.getRight();
        assertTrue(rightc instanceof BinaryLogicOperator);
        BinaryLogicOperator rightcc = (BinaryLogicOperator) rightc;

        assertTrue(rightcc.getLeft() instanceof Compare);
        Compare rightcc_l_c = (Compare) rightcc.getLeft();

        assertTrue(rightcc_l_c.getLeft() instanceof Timestamp);
        assertEquals(Predicate.GREATER_THAN_OR_EQUALS, rightcc_l_c.getPredicate());
        assertTrue(rightcc_l_c.getRight() instanceof LongConstant);
        assertEquals(Long.valueOf(1377014400000L), ((LongConstant) rightcc_l_c.getRight()).getValue());

        assertEquals(Predicate.AND, rightcc.getPredicate());

        assertTrue(rightcc.getRight() instanceof Compare);
        Compare rightcc_r_c = (Compare) rightcc.getRight();

        assertTrue(rightcc_r_c.getLeft() instanceof Timestamp);
        assertEquals(Predicate.LOWER_THAN, rightcc_r_c.getPredicate());
        assertTrue(rightcc_r_c.getRight() instanceof LongConstant);
        assertEquals(Long.valueOf(1377792000000L), ((LongConstant) rightcc_r_c.getRight()).getValue());

        assertNotNull(selectProduct);
    }

    private Filter getFilter() {
        Filter filter = null;
        try {
            DocumentBuilder documentBuilder = MDMXMLUtils.getDocumentBuilder().get();
            Document doc = documentBuilder.parse(this.getClass().getResourceAsStream("filter.xml")); //$NON-NLS-1$
            filter = new ConfigurableFilter(doc);
        } catch (Exception e) {
            LOG.error("An unexpected exception occurred.", e);
        }
        return filter;
    }

    private static class MockStorage implements Storage {

        @Override
        public int getCapabilities() {
            return 0;
        }

        @Override
        public StorageTransaction newStorageTransaction() {
            return null;
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
            return new StorageResults() {

                @Override
                public Iterator<DataRecord> iterator() {
                    return null;
                }

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
            };
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

        }

        @Override
        public void commit() {

        }

        @Override
        public void rollback() {

        }

        @Override
        public void end() {

        }

        @Override
        public void reindex() {

        }

        @Override
        public Set<String> getFullTextSuggestion(String keyword, FullTextSuggestion mode, int suggestionSize) {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public DataSource getDataSource() {
            return null;
        }

        @Override
        public StorageType getType() {
            return null;
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

        @Override
        public Storage asInternal() {
            return this;
        }

        @Override
        public List<ComplexTypeMetadata> findSortedTypesToDrop(DiffResults diffResults, boolean force) {
            return null;
        }

        @Override
        public Set<String> findTablesToDrop(List<ComplexTypeMetadata> sortedTypesToDrop) {
            return null;
        }
    }

    private static class MockCommitter implements SaverSession.Committer {

        @Override
        public void begin(String dataCluster) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Start on '" + dataCluster + "'");
            }
        }

        @Override
        public void commit(String dataCluster) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Commit on '" + dataCluster + "'");
            }
        }

        @Override
        public void save(com.amalto.core.history.Document item) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(item.exportToString());
            }
        }

        @Override
        public void delete(com.amalto.core.history.Document document, DeleteType deleteType) {

        }

        @Override
        public void rollback(String dataCluster) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Rollback on '" + dataCluster + "'");
            }
        }
    }
}
