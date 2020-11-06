/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.save;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import com.amalto.core.storage.transaction.TransactionService;

import com.amalto.core.storage.transaction.Transaction;
import com.amalto.core.storage.transaction.TransactionManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.BaseSecurityCheck;
import com.amalto.core.history.DeleteType;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.save.context.DocumentSaver;
import com.amalto.core.save.context.SaverContextFactory;
import com.amalto.core.save.context.SaverSource;
import com.amalto.core.save.context.StorageDocument;
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;
import com.amalto.core.schema.validation.XmlSchemaValidator;
import com.amalto.core.server.MDMContextAccessor;
import com.amalto.core.server.MockMetadataRepositoryAdmin;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.ServerTest;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import com.amalto.core.util.MDMEhCacheUtil;
import com.amalto.core.util.OutputReport;
import com.amalto.core.util.Util;
import junit.framework.TestCase;
import net.sf.ehcache.CacheManager;

@SuppressWarnings("nls")
public class BulkImportRecordSaveTest extends TestCase {

    public static final boolean USE_STORAGE_OPTIMIZATIONS = true;

    private static final Logger LOG = LogManager.getLogger(BulkImportRecordSaveTest.class);

    private XPath xPath = XPathFactory.newInstance().newXPath();

    private static boolean beanDelegatorContainerFlag = false;

    private static void createBeanDelegatorContainer() {
        if (!beanDelegatorContainerFlag) {
            BeanDelegatorContainer.createInstance();
            beanDelegatorContainerFlag = true;
        }
    }

    @Override
    public void setUp() throws Exception {
        LOG.info("Setting up MDM server environment...");
        ServerContext.INSTANCE.get(new MockServerLifecycle());
        MDMConfiguration.getConfiguration().setProperty("xmlserver.class", "com.amalto.core.storage.DispatchWrapper");
        SaverSession.setDefaultCommitter(new MockCommitter());
        LOG.info("MDM server environment set.");
        XPathFactory xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(new TestNamespaceContext());

        createBeanDelegatorContainer();
        BeanDelegatorContainer.getInstance().setDelegatorInstancePool(
                Collections.<String, Object> singletonMap("SecurityCheck", new MockISecurityCheck()));

        new MDMContextAccessor()
        .setApplicationContext(new ClassPathXmlApplicationContext("classpath:com/amalto/core/server/mdm-context.xml"));

        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(
                MDMEhCacheUtil.MDM_CACHE_MANAGER, EhCacheCacheManager.class);
        mdmEhcache.setCacheManager(CacheManager.newInstance(ServerTest.class.getResourceAsStream("mdm-ehcache.xml")));
    }

    private static class MockISecurityCheck extends BaseSecurityCheck {}

    @Override
    public void tearDown() throws Exception {
        ServerContext.INSTANCE.close();
        MockMetadataRepositoryAdmin.INSTANCE.close();
        XmlSchemaValidator.invalidateCache();
    }

    private Object evaluate(Element committedElement, String path) throws XPathExpressionException {
        return xPath.evaluate(path, committedElement, XPathConstants.STRING);
    }

    public void testCreate() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata22.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);
        SaverSource source = new TestSaverSource(repository, false, "", "metadata22.xsd");
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test33.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);
        Element committedElement = committer.getCommittedElement();
        assertEquals("Talend Cat", evaluate(committedElement, "/Product/Name"));
        assertEquals("[11]", evaluate(committedElement, "/Product/Stores/Store"));
    }

    public void testLongTransactionRollback() throws Exception {
        TransactionService service = new TransactionService();

        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        Transaction transaction = transactionManager.create(Transaction.Lifetime.LONG);
        transactionManager.associate(transaction);
        String transactionId = transaction.getId();

        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata22.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);
        TestSaverSource source = new TestSaverSource(repository, false, "", "metadata22.xsd");
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test33.xml");
        DocumentSaverContext context =
                session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true, true, false);
        DocumentSaver saver = context.createSaver();

        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        service.rollback(transactionId);
        assertNull(MDMEhCacheUtil.getCache(MDMEhCacheUtil.UPDATE_REPORT_EVENT_CACHE, transactionId));
    }

    public void testLongTransactionCommit() throws Exception {
        TransactionService service = new TransactionService();

        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        Transaction transaction = transactionManager.create(Transaction.Lifetime.LONG);
        transactionManager.associate(transaction);
        String transactionId = transaction.getId();

        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata22.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);
        TestSaverSource source = new TestSaverSource(repository, false, "", "metadata22.xsd");
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test33.xml");
        DocumentSaverContext context =
                session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true, true, false);
        DocumentSaver saver = context.createSaver();

        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertNotNull(MDMEhCacheUtil.getCache(MDMEhCacheUtil.UPDATE_REPORT_EVENT_CACHE, transactionId));
        service.commit(transactionId);
        assertNull(MDMEhCacheUtil.getCache(MDMEhCacheUtil.UPDATE_REPORT_EVENT_CACHE, transactionId));
     }

    private static class MockCommitter implements SaverSession.Committer {

        private MutableDocument lastSaved;

        private boolean hasSaved = false;

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
            if (!item.getType().getName().equals("Update") || !hasSaved) { // when update UpdateReport directly
                hasSaved = true;
                lastSaved = (MutableDocument) item;
                if (LOG.isDebugEnabled()) {
                    LOG.debug(item.exportToString());
                }
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

        public Element getCommittedElement() {
            return lastSaved.asDOM().getDocumentElement();
        }

        public boolean hasSaved() {
            return hasSaved;
        }
    }

    private static class TestSaverSource implements SaverSource {

        private final MetadataRepository repository;

        private final boolean exist;

        private final String originalDocumentFileName;

        private MetadataRepository updateReportRepository;

        private String userName = "User";

        private String lastInvalidatedTypeCache;

        private final String schemaFileName;

        private boolean hasSavedAutoIncrement;

        private boolean hasCalledInitAutoIncrement;

        private final Map<String, String> schemasAsString = new HashMap<String, String>();

        private final Map<String, Integer> AUTO_INCREMENT_ID_MAP = new HashMap<String, Integer>();

        public TestSaverSource(MetadataRepository repository, boolean exist, String originalDocumentFileName,
                String schemaFileName) {
            this.repository = repository;
            this.exist = exist;
            this.originalDocumentFileName = originalDocumentFileName;
            this.schemaFileName = schemaFileName;
        }

        public TestSaverSource(MetadataRepository repository, boolean exist, String originalDocumentFileName,
                String schemaFileName, String userName) {
            this(repository, exist, originalDocumentFileName, schemaFileName);
            this.userName = userName;
        }

        @Override
        public MutableDocument get(String dataClusterName, String dataModelName, String typeName, String[] key) {
            try {
                ComplexTypeMetadata type = repository.getComplexType(typeName);
                DocumentBuilder documentBuilder;
                documentBuilder = new SkipAttributeDocumentBuilder(SaverContextFactory.DOCUMENT_BUILDER, false);
                Document databaseDomDocument = documentBuilder
                        .parse(DocumentSaveTest.class.getResourceAsStream(originalDocumentFileName));
                Element userXmlElement = getUserXmlElement(databaseDomDocument);
                if (USE_STORAGE_OPTIMIZATIONS) {
                    DataRecordReader<String> reader = new XmlStringDataRecordReader();
                    DataRecord dataRecord = reader.read(repository, type, Util.nodeToString(userXmlElement));
                    return new StorageDocument(dataClusterName, repository, dataRecord);
                } else {
                    return new DOMDocument(userXmlElement, type, dataClusterName, dataClusterName);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static Element getUserXmlElement(Document databaseDomDocument) {
            NodeList userXmlPayloadElement = databaseDomDocument.getElementsByTagName("p"); //$NON-NLS-1$
            if (userXmlPayloadElement.getLength() > 1) {
                throw new IllegalStateException("Document has multiple payload elements.");
            }
            Node current = userXmlPayloadElement.item(0).getFirstChild();
            while (current != null) {
                if (current instanceof Element) {
                    return (Element) current;
                }
                current = current.getNextSibling();
            }
            throw new IllegalStateException("Element 'p' is expected to have an XML element as child.");
        }

        @Override
        public boolean exist(String dataCluster, String dataModelName, String typeName, String[] key) {
            return exist;
        }

        @Override
        public synchronized MetadataRepository getMetadataRepository(String dataModelName) {
            if ("UpdateReport".equals(dataModelName)) {
                if (updateReportRepository == null) {
                    updateReportRepository = new MetadataRepository();
                    updateReportRepository.load(DocumentSaveTest.class.getResourceAsStream("updateReport.xsd"));
                }
                return updateReportRepository;
            }
            return repository;
        }

        @Override
        public InputStream getSchema(String dataModelName) {
            if (schemasAsString.get(dataModelName) == null) {
                String schemaAsString = "testdata";
                schemasAsString.put(dataModelName, schemaAsString);
            }
            return DocumentSaveTest.class.getResourceAsStream(schemaFileName);
        }

        @Override
        public OutputReport invokeBeforeSaving(DocumentSaverContext context, MutableDocument updateReportDocument) {
            String message = "<report><message type=\"info\">change the value successfully!</message></report>";
            return new OutputReport(message, null, false);
        }

        @Override
        public Set<String> getCurrentUserRoles() {
            if ("User".equals(userName)) {
                return Collections.singleton("User");
            } else {
                return Collections.singleton("Demo_Manager");
            }
        }

        @Override
        public String getUserName() {
            return userName;
        }

        @Override
        public String getLegitimateUser() {
            return getUserName();
        }

        @Override
        public boolean existCluster(String dataClusterName) {
            return true;
        }

        @Override
        public void resetLocalUsers() {
            // nothing to do
        }

        @Override
        public void initAutoIncrement() {
            hasCalledInitAutoIncrement = true;
        }

        @Override
        public void routeItem(String dataCluster, String typeName, String[] id) {
            // nothing to do
        }

        @Override
        public void invalidateTypeCache(String dataModelName) {
            lastInvalidatedTypeCache = dataModelName;
            schemasAsString.remove(dataModelName);
        }

        @Override
        public void saveAutoIncrement() {
            hasSavedAutoIncrement = true;
        }

        @Override
        public String nextAutoIncrementId(String dataCluster, String dataModel, String conceptName) {
            if (!hasCalledInitAutoIncrement) {
                initAutoIncrement();
            }
            int id = 0;
            if (AUTO_INCREMENT_ID_MAP.containsKey(conceptName)) {
                id = AUTO_INCREMENT_ID_MAP.get(conceptName);
            }
            id++;
            AUTO_INCREMENT_ID_MAP.put(conceptName, id);
            return String.valueOf(id);
        }
    }

    private static class TestNamespaceContext implements NamespaceContext {

        private Map<String, String> declaredPrefix = new HashMap<String, String>();

        private TestNamespaceContext() {
            declaredPrefix.put("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            declaredPrefix.put("tmdm", SkipAttributeDocumentBuilder.TALEND_NAMESPACE);
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return declaredPrefix.get(prefix);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            Set<Map.Entry<String, String>> entries = declaredPrefix.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                if (entry.getValue().equals(namespaceURI)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Iterator getPrefixes(String namespaceURI) {
            return declaredPrefix.keySet().iterator();
        }
    }
}
