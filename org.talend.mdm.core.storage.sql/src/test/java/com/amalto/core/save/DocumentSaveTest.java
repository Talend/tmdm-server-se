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

import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.amalto.core.delegator.MockILocalUser;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.commons.core.utils.XMLUtils;
import com.amalto.core.delegator.BaseSecurityCheck;
import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.history.DeleteType;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.save.context.DocumentSaver;
import com.amalto.core.save.context.SaverContextFactory;
import com.amalto.core.save.context.SaverSource;
import com.amalto.core.save.context.StorageDocument;
import com.amalto.core.save.context.StorageSaverSource;
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;
import com.amalto.core.schema.validation.Validator;
import com.amalto.core.schema.validation.XmlSchemaValidator;
import com.amalto.core.server.MDMContextAccessor;
import com.amalto.core.server.MockMetadataRepositoryAdmin;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.MockStorageAdmin;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.ServerTest;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import com.amalto.core.storage.transaction.TransactionManager;
import com.amalto.core.util.MDMEhCacheUtil;
import com.amalto.core.util.OutputReport;
import com.amalto.core.util.Util;
import com.amalto.core.util.ValidateException;

import junit.framework.TestCase;
import net.sf.ehcache.CacheManager;

@SuppressWarnings("nls")
public class DocumentSaveTest extends TestCase {

    public static final boolean USE_STORAGE_OPTIMIZATIONS = true;

    private static final Logger LOG = LogManager.getLogger(DocumentSaveTest.class);

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
        
        Map<String, Object> delegatorInstancePool = new HashMap<>();
        delegatorInstancePool.put("LocalUser", new MockILocalUser()); //$NON-NLS-1$
        delegatorInstancePool.put("SecurityCheck", new MockISecurityCheck()); //$NON-NLS-1$
        createBeanDelegatorContainer();
        BeanDelegatorContainer.getInstance().setDelegatorInstancePool(delegatorInstancePool);

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/amalto/core/server/mdm-context.xml");
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

    public void testTypeCacheInvalidate() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test1_original.xml", "metadata1.xsd");
        assertNull(source.getLastInvalidatedTypeCache());

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);
        assertTrue(committer.hasSaved());

        session = SaverSession.newSession(source);
        session.invalidateTypeCache("DStar");
        session.end(committer);
        assertEquals("DStar", source.getLastInvalidatedTypeCache());

        String conceptName = "crossreferencing";
        session.invalidateTypeCache(conceptName);
        assertNull(source.getSchemasAsString().get(conceptName));
        session.getSaverSource().getSchema(conceptName);
        assertEquals("testdata", source.getSchemasAsString().get(conceptName));
    }

    
    public void testValidationWithXSINamespace() throws Exception {
        InputStream contractXML = DocumentSaveTest.class.getResourceAsStream("contract.xml");
        Document contract = new SkipAttributeDocumentBuilder(MDMXMLUtils.getDocumentBuilderWithNamespace().get(), true)
                .parse(contractXML);

        XmlSchemaValidator validator = new XmlSchemaValidator("", DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"),
                Validator.NO_OP_VALIDATOR);
        validator.validate(contract.getDocumentElement());
    }

    public void testCreate() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, false, "test1_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Chicago", evaluate(committedElement, "/Agency/Name"));
        assertEquals("Chicago", evaluate(committedElement, "/Agency/City"));
    }

    public void testCreateEntityByStandaloneProcessCallWorkflow() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, false, "", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test57.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "workflow", recordXml, false, true,
                true, true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Chicago", evaluate(committedElement, "/Agency/Name"));
        assertEquals("Chicago", evaluate(committedElement, "/Agency/City"));
    }

    public void testUpdateEntityByStandaloneProcessCallWorkflow() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test57_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test57.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "workflow", recordXml, false, true,
                true, true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Chicago", evaluate(committedElement, "/Agency/Name"));
        assertEquals("Chicago", evaluate(committedElement, "/Agency/City"));
    }

    public void testCreateWithInheritanceType() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contract", repository);

        SaverSource source = new TestSaverSource(repository, false, "test22_original.xml", "metadata3.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test22.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contract", "Source", recordXml, true, true,
                true, true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("", evaluate(committedElement, "/Contract/detail[1]/@xsi:type"));
        assertEquals("", evaluate(committedElement, "/Contract/detail[1]/code"));
    }

    public void testCreateSecurity() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, false, "", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("04102", evaluate(committedElement, "/Agency/Zip"));
        assertEquals("", evaluate(committedElement, "/Agency/State"));
    }

    public void testCreateWithUUIDOverwrite() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, false, "", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test23.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertNotSame("100", evaluate(committedElement, "/Agency/Id")); // Id is expected to be overwritten in case of
        // creation
        assertEquals(1, saver.getSavedId().length);
        assertNotSame("100", saver.getSavedId()[0]);
    }

    public void testCreateWithUUIDAndAUTOINCIgnore() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("Personne.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Vinci", repository);

        SaverSource source = new TestSaverSource(repository, false, "", "Personne.xsd");
        ((TestSaverSource) source).setUserName("Demo_Manager");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("Personne.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Vinci", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertNull(Util.getFirstTextNode(committedElement, "/Personne/Contextes/Contexte/IdContexte"));
        assertEquals("[2]", evaluate(committedElement, "/Personne/TypePersonneFk"));

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("Personne2.xml");
        context = session.getContextFactory().create("MDM", "Vinci", "Source", recordXml, true, true, true, true, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertNull(Util.getFirstTextNode(committedElement, "/Personne/Contextes/Contexte/IdContexte"));
        assertEquals("[1]", evaluate(committedElement, "/Personne/TypePersonneFk"));

    }

    public void testCreateWithMultiOccurrenceUUID() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("MultiOccurrenceUUID.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("MultiOccurrenceUUID", repository);

        TestSaverSource source = new TestSaverSource(repository, false, "", "MultiOccurrenceUUID.xsd");
        source.setUserName("Demo_Manager");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("NewMultiOccurrenceUUID.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "MultiOccurrenceUUID", "Source", recordXml,
                true, true, true, true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());

        Element committedElement = committer.getCommittedElement();

        assertEquals("111", evaluate(committedElement, "/EntityA/Id"));

        assertEquals("lab1", evaluate(committedElement, "/EntityA/nodes/node[1]/label"));
        assertNotNull(evaluate(committedElement, "/EntityA/nodes/node[1]/uuid"));
        assertTrue(evaluate(committedElement, "/EntityA/nodes/node[1]/uuid").toString().length() > 0);

        assertEquals("lab2", evaluate(committedElement, "/EntityA/nodes/node[2]/label"));
        assertNotNull(evaluate(committedElement, "/EntityA/nodes/node[2]/uuid"));
        assertTrue(evaluate(committedElement, "/EntityA/nodes/node[2]/uuid").toString().length() > 0);

        assertEquals("lab3", evaluate(committedElement, "/EntityA/nodes/node[3]/label"));
        assertNotNull(evaluate(committedElement, "/EntityA/nodes/node[3]/uuid"));
        assertTrue(evaluate(committedElement, "/EntityA/nodes/node[3]/uuid").toString().length() > 0);
    }

    public void testReplaceWithUUIDOverwrite() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test23_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test23.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        // Id is expected to be overwritten in case of creation
        assertEquals("100", evaluate(committedElement, "/Agency/Id"));
        // Id is expected to be overwritten in case of creation
        assertEquals("http://www.newSite2.org", evaluate(committedElement, "/Agency/Information/MoreInfo[2]"));
        assertEquals(1, saver.getSavedId().length);
        assertEquals("100", saver.getSavedId()[0]);
    }

    public void testCreateWithAutoIncrementOverwrite() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, false, "", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test24.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        assertFalse(source.hasSavedAutoIncrement());
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        // Id is expected to be overwritten in case of creation
        assertNotSame("100", evaluate(committedElement, "/ProductFamily/Id"));
    }

    public void testUpdateWithAutoIncrement() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("personWithAddressOfAutoIncrement.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Vinci", repository);

        SaverSource source = new TestSaverSource(repository, false, "", "personWithAddressOfAutoIncrement.xsd");
        ((TestSaverSource) source).setUserName("Demo_Manager");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("personWithAddressOfAutoIncrement_1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Vinci", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/idAddress"));

        assertEquals("swissMailAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/type"));
        assertEquals("swissHQAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/type"));
        assertEquals("foreignMailAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/type"));

        String idAddress = Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/idAddress");
        String idAddressTwo = Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/idAddress");
        String idAddressThree = Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/idAddress");

        assertEquals(1, Integer.valueOf(idAddress).intValue());
        assertEquals(2, Integer.valueOf(idAddressTwo).intValue());
        assertEquals(3, Integer.valueOf(idAddressThree).intValue());

        source = new TestSaverSource(repository, true, "personWithAddressOfAutoIncrement_origin.xml",
                "personWithAddressOfAutoIncrement.xsd");
        ((TestSaverSource) source).setUserName("Demo_Manager");

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("personWithAddressOfAutoIncrement_2.xml");
        context = session.getContextFactory().create("MDM", "Vinci", "Source", recordXml, false, false, true, true, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[5]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[5]/idAddress"));
        assertEquals("swissMailAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/type"));
        assertEquals("swissHQAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/type"));
        assertEquals("foreignMailAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/type"));
        assertEquals("foreignHQAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/type"));
        assertEquals("pccSignBoard", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[5]/type"));
        idAddress = Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/idAddress");
        idAddressTwo = Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/idAddress");
        idAddressThree = Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/idAddress");
        String idAddressFour = Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/idAddress");
        String idAddressFive = Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[5]/idAddress");

        // these from the record file
        assertEquals(1, Integer.valueOf(idAddress).intValue());
        assertEquals(2, Integer.valueOf(idAddressTwo).intValue());
        assertEquals(3, Integer.valueOf(idAddressThree).intValue());
        // these two from the mock TestSaverSource
        assertEquals(2, Integer.valueOf(idAddressFour).intValue());
        assertEquals(1, Integer.valueOf(idAddressFive).intValue());

        source = new TestSaverSource(repository, true, "personWithAddressOfAutoIncrement_origin.xml",
                "personWithAddressOfAutoIncrement.xsd");
        ((TestSaverSource) source).setUserName("Demo_Manager");

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("personWithAddressOfAutoIncrement_3.xml");
        context = session.getContextFactory().create("MDM", "Vinci", "Source", recordXml, false, false, true, true, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/idAddress"));
        assertEquals("swissMailAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/type"));
        assertEquals("pccSignBoard", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/type"));
        assertEquals("swissHQAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/type"));
        assertEquals("foreignMailAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/type"));
        idAddress = Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/idAddress");
        idAddressTwo = Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/idAddress");
        idAddressThree = Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/idAddress");
        idAddressFour = Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/idAddress");

        // these from the record file
        assertEquals(1, Integer.valueOf(idAddress).intValue());
        // these two from the mock TestSaverSource
        assertEquals(1, Integer.valueOf(idAddressTwo).intValue());

        assertEquals(2, Integer.valueOf(idAddressThree).intValue());
        // these two from the mock TestSaverSource
        assertEquals(3, Integer.valueOf(idAddressFour).intValue());
    }

    public void testAutoIncrementForNormalField() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata24.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("PersonAddress", repository);

        TestSaverSource source = new TestSaverSource(repository, false, "", "metadata24.xsd");

        SaverSession session = SaverSession.newSession(source);
        // 1. Entity Person: normal field is autoincrement
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test79.xml");
        DocumentSaverContext context = session.getContextFactory()
                .create("MDM", "PersonAddress", "Source", recordXml, true, true, true, true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        assertFalse(source.hasSavedAutoIncrement());
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        // Id is expected to be overwritten in case of creation
        assertEquals("1", evaluate(committedElement, "/Person/N_Index"));

        // 2. Entity Address, key field Id and normal field, Port is autoincrement, Create
        source = new TestSaverSource(repository, false, "", "metadata24.xsd");
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test80.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true, true, false);
        saver = context.createSaver();
        saver.save(session, context);
        assertFalse(source.hasSavedAutoIncrement());
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/Address/Id"));
        assertEquals("1", evaluate(committedElement, "/Address/Port"));

        // 3. Entity Address, key field Id and normal field, Port is autoincrement, Update
        source = new TestSaverSource(repository, false, "test81_original.xml", "metadata24.xsd");
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test81.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true, true, false);
        saver = context.createSaver();
        saver.save(session, context);
        assertFalse(source.hasSavedAutoIncrement());
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/Person/N_Index"));

        // 4. Entity Address, key field Id and normal field, Port is autoincrement, Update
        source = new TestSaverSource(repository, false, "test82_original.xml", "metadata24.xsd");
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test82.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true, true, false);
        saver = context.createSaver();
        saver.save(session, context);
        assertFalse(source.hasSavedAutoIncrement());
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/Address/Id"));
        assertEquals("1", evaluate(committedElement, "/Address/Port"));

        //5, for Entity Person, normal field is N_Index, and autoIncrement2 in the complex type Create
        source = new TestSaverSource(repository, false, "", "metadata24.xsd");
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test83.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true, true, false);
        saver = context.createSaver();
        saver.save(session, context);
        assertFalse(source.hasSavedAutoIncrement());
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/Person/N_Index"));
        assertEquals("1", evaluate(committedElement, "/Person/complex/autoIncrement2"));

        //6, for Entity Person, normal field is N_Index, and autoIncrement2 in the complex type Update
        source = new TestSaverSource(repository, false, "test83_original.xml", "metadata24.xsd");
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test83.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true, true, false);
        saver = context.createSaver();
        saver.save(session, context);
        assertFalse(source.hasSavedAutoIncrement());
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/Person/N_Index"));
        assertEquals("1", evaluate(committedElement, "/Person/complex/autoIncrement2"));
    }

    /**
     * TMDM-14507: SOAP/REST API work unexpected when provide value for non-PK autoincrement fields
     */
    public void testNormalFieldAutoIncrementValueProvided() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata24.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("PersonAddress", repository);

        TestSaverSource source = new TestSaverSource(repository, false, "", "metadata24.xsd");

        SaverSession session = SaverSession.newSession(source);
        // 1. Entity Person: normal field N_Index is AutoIncrement, Create
        // Provided value for AutoIncrement normal field
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test79_1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml,
                true, true, true, true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        assertFalse(source.hasSavedAutoIncrement());
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("n_index", evaluate(committedElement, "/Person/N_Index"));

        // Not provided value for AutoIncrement normal field
        recordXml = DocumentSaveTest.class.getResourceAsStream("test79.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true,
                true, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/Person/N_Index"));

        // 2. Entity Person: normal field N_Index is AutoIncrement, Update
        // Provided value for AutoIncrement normal field
        source = new TestSaverSource(repository, false, "test81_original.xml", "metadata24.xsd");
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test79_1.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true,
                true, false);
        saver = context.createSaver();
        saver.save(session, context);
        assertFalse(source.hasSavedAutoIncrement());
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("n_index", evaluate(committedElement, "/Person/N_Index"));

        // Not provided value for AutoIncrement normal field
        recordXml = DocumentSaveTest.class.getResourceAsStream("test79.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true,
                true, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/Person/N_Index"));

        // 3. Entity Address, PK Id and normal field Port is AutoIncrement, Create
        // Provided value for AutoIncrement normal field
        source = new TestSaverSource(repository, false, "", "metadata24.xsd");
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test80_1.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true,
                true, false);
        saver = context.createSaver();
        saver.save(session, context);
        assertFalse(source.hasSavedAutoIncrement());
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/Address/Id"));
        assertEquals("port", evaluate(committedElement, "/Address/Port"));

        // Not provided value for AutoIncrement normal field
        recordXml = DocumentSaveTest.class.getResourceAsStream("test80.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true,
                true, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        committedElement = committer.getCommittedElement();
        assertEquals("2", evaluate(committedElement, "/Address/Id"));
        assertEquals("1", evaluate(committedElement, "/Address/Port"));

        // 4. Entity Address, PK Id and normal field Port is AutoIncrement, Update
        // Provided value for AutoIncrement normal field
        source = new TestSaverSource(repository, false, "test82_original.xml", "metadata24.xsd");
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test80_1.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true,
                true, false);
        saver = context.createSaver();
        saver.save(session, context);
        assertFalse(source.hasSavedAutoIncrement());
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/Address/Id"));
        assertEquals("port", evaluate(committedElement, "/Address/Port"));

        // Not provided value for AutoIncrement normal field
        recordXml = DocumentSaveTest.class.getResourceAsStream("test80.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true,
                true, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        committedElement = committer.getCommittedElement();
        assertEquals("2", evaluate(committedElement, "/Address/Id"));
        assertEquals("1", evaluate(committedElement, "/Address/Port"));

        // 5, Entity Person, normal field N_Index and autoIncrement2 in complex type, Create
        // Provided value for AutoIncrement normal field
        source = new TestSaverSource(repository, false, "", "metadata24.xsd");
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test83_1.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true,
                true, false);
        saver = context.createSaver();
        saver.save(session, context);
        assertFalse(source.hasSavedAutoIncrement());
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/Person/N_Index"));
        assertEquals("autoIncrement2", evaluate(committedElement, "/Person/complex/autoIncrement2"));

        // Not provided value for AutoIncrement normal field
        recordXml = DocumentSaveTest.class.getResourceAsStream("test83.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true,
                true, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        committedElement = committer.getCommittedElement();
        assertEquals("2", evaluate(committedElement, "/Person/N_Index"));
        assertEquals("1", evaluate(committedElement, "/Person/complex/autoIncrement2"));

        // 6, Entity Person, normal field N_Index and autoIncrement2 in complex type, Update
        // Provided value for AutoIncrement normal field
        source = new TestSaverSource(repository, false, "test83_original.xml", "metadata24.xsd");
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test83_1.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true,
                true, false);
        saver = context.createSaver();
        saver.save(session, context);
        assertFalse(source.hasSavedAutoIncrement());
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/Person/N_Index"));
        assertEquals("autoIncrement2", evaluate(committedElement, "/Person/complex/autoIncrement2"));

        // Not provided value for AutoIncrement normal field
        recordXml = DocumentSaveTest.class.getResourceAsStream("test83.xml");
        context = session.getContextFactory().create("MDM", "PersonAddress", "Source", recordXml, true, true, true,
                true, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        committedElement = committer.getCommittedElement();
        assertEquals("2", evaluate(committedElement, "/Person/N_Index"));
        assertEquals("1", evaluate(committedElement, "/Person/complex/autoIncrement2"));
    }

    public void testUpdateWithUUID() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("personWithAddressOfUUID.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Vinci", repository);

        SaverSource source = new TestSaverSource(repository, false, "", "personWithAddressOfUUID.xsd");
        ((TestSaverSource) source).setUserName("Demo_Manager");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("personWithAddressOfUUID_1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Vinci", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/idAddress"));

        assertEquals("swissMailAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/type"));

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("personWithAddressOfUUID_2.xml");
        context = session.getContextFactory().create("MDM", "Vinci", "Source", recordXml, true, true, true, true, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[5]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[5]/idAddress"));
        assertEquals("swissMailAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/type"));
        assertEquals("swissHQAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/type"));
        assertEquals("foreignMailAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/type"));
        assertEquals("foreignHQAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/type"));
        assertEquals("pccSignBoard", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[5]/type"));

        source = new TestSaverSource(repository, true, "personWithAddressOfUUID_origin.xml", "personWithAddressOfUUID.xsd");
        ((TestSaverSource) source).setUserName("Demo_Manager");

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("personWithAddressOfUUID_2.xml");
        context = session.getContextFactory().create("MDM", "Vinci", "Source", recordXml, false, false, true, true, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/idAddress"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[5]/type"));
        assertNotNull(Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[5]/idAddress"));
        assertEquals("swissMailAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[1]/type"));
        assertEquals("swissHQAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[2]/type"));
        assertEquals("foreignMailAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[3]/type"));
        assertEquals("foreignHQAddress", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[4]/type"));
        assertEquals("pccSignBoard", Util.getFirstTextNode(committedElement, "/person/dwellingAddresses/address[5]/type"));
    }

    public void testCreateFailure() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, false, "", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test10.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        try {
            saver.save(session, context);
            fail();
        } catch (SaveException e) {
            assertEquals("Expected id 'Id' to be set.", e.getBeforeSavingMessage());
        }
    }

    public void testCreateWithForeignKeyType() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata9.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, false, "", "metadata9.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test26.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("16.99", evaluate(committedElement, "/Product/Price"));
        assertEquals("ProductFamily", evaluate(committedElement, "/Product/Family/@tmdm:type"));
    }

    public void testReplaceWithForeignKeyType() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata9.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test26_original.xml", "metadata9.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test26.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("16.99", evaluate(committedElement, "/Product/Price"));
    }

    public void testUpdateCompositeFK() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("TestCompositeFK.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("TestCompositeFK", repository);

        SaverSource source = new TestSaverSource(repository, true, "testCompositeFK_original.xml", "TestCompositeFK.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = new ByteArrayInputStream(
                "<TestMultipleCompositeFK><Id>1</Id><Name>1</Name><FK>[Id2_c][Id1_c]</FK></TestMultipleCompositeFK>"
                        .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().create("MDM", "TestCompositeFK", "Source", recordXml, true,
                true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        System.out.print(evaluate(committedElement, "/TestMultipleCompositeFK/FK"));
        assertEquals("[Id2_c][Id1_c]", evaluate(committedElement, "/TestMultipleCompositeFK/FK"));
    }

    // PartialUpdateSaverContext, UserAction=PARTIAL_UPDATE
    public void testUpdateReportPartialUpdate() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test1_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream(
                "<Agency><Id>5258f292-5670-473b-bc01-8b63434682f3</Id><Information><MoreInfo>http://www.mynewsite.fr</MoreInfo></Information></Agency>"
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source", partialUpdateContent, true, true, "/Agency/Information/MoreInfo", "", -1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("http://www.mynewsite.fr", evaluate(committedElement, "/Agency/Information/MoreInfo[1]"));

        // Primary Key Info (UserAction.PARTIAL_UPDATE)
        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();
        String pkinfo = (String) evaluate(doc.getDocumentElement(), "PrimaryKeyInfo");
        assertEquals("name1-city1", pkinfo);
    }

    // PartialUpdateSaverContext, UserAction=UPDATE
    public void testUpdateReportPartialUpdate2() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test1_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream(
                "<Agency><Id>5258f292-5670-473b-bc01-8b63434682f3</Id><City>city2</City><Zip>04103</Zip></Agency>"
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source", partialUpdateContent, true, true, null, "", -1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("city2", evaluate(committedElement, "/Agency/City"));
        assertEquals("04103", evaluate(committedElement, "/Agency/Zip"));

        // Primary Key Info (UserAction.UPDATE)
        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();
        String pkinfo = (String) evaluate(doc.getDocumentElement(), "PrimaryKeyInfo");
        assertEquals("name1-city2", pkinfo);
    }

    public void testPartialUpdateWithOverwrite() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test1_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream(
                "<Agency><Id>5258f292-5670-473b-bc01-8b63434682f3</Id><Information><MoreInfo>http://www.mynewsite.fr</MoreInfo></Information></Agency>"
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source", partialUpdateContent, true, true, "/", "/", -1, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("http://www.mynewsite.fr", evaluate(committedElement, "/Agency/Information/MoreInfo[1]"));
        assertEquals("", evaluate(committedElement, "/Agency/Information/MoreInfo[2]"));

        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();
        assertUpdateReportAction(doc, 1, "Information/MoreInfo[1]", "", "http://www.mynewsite.fr");
    }

    public void testPartialUpdateWithOverwriteEqFalseOneToThree() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test1_0_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream(
                "<Agency><Id>5258f292-5670-473b-bc01-8b63434682f4</Id><Information><MoreInfo>http://www.mynewsite.fr</MoreInfo><MoreInfo>http://www.mynewsite.com</MoreInfo><MoreInfo>http://www.mynewsite.cn</MoreInfo></Information></Agency>"
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source", partialUpdateContent, true, true, "/", "/", -1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("www.a.com", evaluate(committedElement, "/Agency/Information/MoreInfo[1]"));
        assertEquals("http://www.mynewsite.fr", evaluate(committedElement, "/Agency/Information/MoreInfo[2]"));
        assertEquals("http://www.mynewsite.com", evaluate(committedElement, "/Agency/Information/MoreInfo[3]"));
        assertEquals("http://www.mynewsite.cn", evaluate(committedElement, "/Agency/Information/MoreInfo[4]"));

        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();
        assertUpdateReportAction(doc, 1, "Information/MoreInfo[4]", "", "http://www.mynewsite.cn");
        assertUpdateReportAction(doc, 2, "Information/MoreInfo[3]", "", "http://www.mynewsite.com");
        assertUpdateReportAction(doc, 3, "Information/MoreInfo[2]", "", "http://www.mynewsite.fr");
    }

    public void testPartialUpdateSameCountWithOverwriteFalse() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test1_1_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream(
                "<Agency><Id>5258f292-5670-473b-bc01-8b63434682f4</Id><Information><MoreInfo>http://www.mynewsite.fr</MoreInfo><MoreInfo>http://www.mynewsite.com</MoreInfo></Information></Agency>"
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source", partialUpdateContent, true, true, "/", "/", -1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        //two to two
        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("www.a.com", evaluate(committedElement, "/Agency/Information/MoreInfo[1]"));
        assertEquals("www.b.com", evaluate(committedElement, "/Agency/Information/MoreInfo[2]"));
        assertEquals("http://www.mynewsite.fr", evaluate(committedElement, "/Agency/Information/MoreInfo[3]"));
        assertEquals("http://www.mynewsite.com", evaluate(committedElement, "/Agency/Information/MoreInfo[4]"));

        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();
        assertUpdateReportAction(doc, 1, "Information/MoreInfo[4]", "", "http://www.mynewsite.com");
        assertUpdateReportAction(doc, 2, "Information/MoreInfo[3]", "", "http://www.mynewsite.fr");
    }

    public void testPartialUpdateLessCountWithOverwriteFalse() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test1_2_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream(
                "<Agency><Id>5258f292-5670-473b-bc01-8b63434682f4</Id><Information><MoreInfo>http://www.mynewsite.fr</MoreInfo><MoreInfo>http://www.mynewsite.com</MoreInfo></Information></Agency>"
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source", partialUpdateContent, true, true, "/", "/", -1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        //Three to One
        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("www.a.com", evaluate(committedElement, "/Agency/Information/MoreInfo[1]"));
        assertEquals("www.b.com", evaluate(committedElement, "/Agency/Information/MoreInfo[2]"));
        assertEquals("www.c.com", evaluate(committedElement, "/Agency/Information/MoreInfo[3]"));
        assertEquals("http://www.mynewsite.fr", evaluate(committedElement, "/Agency/Information/MoreInfo[4]"));
        assertEquals("http://www.mynewsite.com", evaluate(committedElement, "/Agency/Information/MoreInfo[5]"));

        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();
        assertUpdateReportAction(doc, 1, "Information/MoreInfo[5]", "", "http://www.mynewsite.com");
        assertUpdateReportAction(doc, 2, "Information/MoreInfo[4]", "", "http://www.mynewsite.fr");
    }

    public void testPartialUpdateWithOverwriteEqualsTrue() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test1_1_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream(
                "<Agency><Id>5258f292-5670-473b-bc01-8b63434682f4</Id><Information><MoreInfo>http://www.mynewsite.fr</MoreInfo></Information></Agency>"
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source", partialUpdateContent, true, true, "/", "/", -1, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("http://www.mynewsite.fr", evaluate(committedElement, "/Agency/Information/MoreInfo[1]"));
        assertEquals("", evaluate(committedElement, "/Agency/Information/MoreInfo[2]"));
    }

    // Non-PartialUpdateSaverContext, CREATE, different data types
    public void testUpdateReportPrimaryKeyInfo() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("PrimaryKeyInfo.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("PKInfo", repository);

        @SuppressWarnings("serial")
        List<Map<String, String>> testDatas = new ArrayList<Map<String, String>>() {
            {
                add(new HashMap<String, String>() {// string, boolean
                    {
                        put("type", "EntityOth");
                        put("document", "PrimaryKeyInfo_1.xml");
                        put("pkinfo", "strfield-true");
                    }
                });
                add(new HashMap<String, String>() {// long, integer, int, short
                    {
                        put("type", "EntityNum1");
                        put("document", "PrimaryKeyInfo_2.xml");
                        put("pkinfo", "11-22-33-44");
                    }
                });
                add(new HashMap<String, String>() {// double, decimal, float
                    {
                        put("type", "EntityNum2");
                        put("document", "PrimaryKeyInfo_3.xml");
                        put("pkinfo", "2.2-3.30-4.4");
                    }
                });
                add(new HashMap<String, String>() {// date, datetime, time
                    {
                        put("type", "EntityDate");
                        put("document", "PrimaryKeyInfo_4.xml");
                        put("pkinfo", "2019-07-30-2019-07-31T12:00:00-12:12:12");
                    }
                });
                add(new HashMap<String, String>() {// subelement
                    {
                        put("type", "EntityComp");
                        put("document", "PrimaryKeyInfo_5.xml");
                        put("pkinfo", "subelement");
                    }
                });
                add(new HashMap<String, String>() {// FK
                    {
                        put("type", "EntityFK");
                        put("document", "PrimaryKeyInfo_6.xml");
                        put("pkinfo", "[1]");
                    }
                });
                add(new HashMap<String, String>() {// string, boolean=null
                    {
                        put("type", "EntityOth");
                        put("document", "PrimaryKeyInfo_7.xml");
                        put("pkinfo", "strfield");
                    }
                });
                add(new HashMap<String, String>() {// double=2, decimal=3.3, float=4
                    {
                        put("type", "EntityNum2");
                        put("document", "PrimaryKeyInfo_8.xml");
                        put("pkinfo", "2-3.3-4");
                    }
                });
             }
         };

         for (Map<String, String> data : testDatas) {
             MockStorageSaverSource source = new MockStorageSaverSource(repository, "PrimaryKeyInfo.xsd");

             SaverSession session = SaverSession.newSession(source);
             InputStream recordXml = DocumentSaveTest.class.getResourceAsStream(data.get("document"));
             DocumentSaverContext context = session.getContextFactory().create("PKInfo", "PKInfo", "Source", recordXml, true, true, true, false, false);
             DocumentSaver saver = context.createSaver();
             saver.save(session, context);
             MockCommitter committer = new MockCommitter();
             session.end(committer);

             MutableDocument updateReportDocument = context.getUpdateReportDocument();
             assertNotNull(updateReportDocument);
             Document doc = updateReportDocument.asDOM();
             String pkinfo = (String) evaluate(doc.getDocumentElement(), "PrimaryKeyInfo");
             assertEquals(data.get("pkinfo"), pkinfo);
         }
    }

    // Non-PartialUpdateSaverContext, UPDATE
    public void testUpdateReportPrimaryKeyInfo2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("PrimaryKeyInfo.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("PKInfo", repository);

        SaverSource source = new TestSaverSource(repository, true, "PrimaryKeyInfo_9.xml", "PrimaryKeyInfo.xsd");
        SaverSession session = SaverSession.newSession(source);
        InputStream updateContent = new ByteArrayInputStream(
                "<EntityOth><EntityOthId>1</EntityOthId><StrField>strfield2</StrField><BoolField>false</BoolField></EntityOth>"
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().create("PKInfo", "PKInfo", "Source", updateContent, false, true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        // Primary Key Info (UserAction.UPDATE)
        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();
        String pkinfo = (String) evaluate(doc.getDocumentElement(), "PrimaryKeyInfo");
        assertEquals("strfield2-false", pkinfo);
    }

    // PartialUpdateSaverContext, UserAction=PARTIAL_DELETE
    public void testUpdateReportPartialDelete() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("PartialDelete.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        @SuppressWarnings("serial")
        List<Map<String, String>> testDatas = new ArrayList<Map<String, String>>() {
            {
                add(new HashMap<String, String>() {// foreign key
                    {
                        put("pivot", "Person/Houses/House");
                        put("key", ".");
                        put("document", "PartialDelete_1.xml");
                        put("report", "report1");
                    }
                });
                add(new HashMap<String, String>() {// complex type
                    {
                        put("pivot", "Person/Kids/Kid");
                        put("key", "/Name");
                        put("document", "PartialDelete_2.xml");
                        put("report", "report2");
                    }
                });
                add(new HashMap<String, String>() {// simple type
                    {
                        put("pivot", "Person/Habits/Habit");
                        put("key", "");
                        put("document", "PartialDelete_3.xml");
                        put("report", "report3");
                    }
                });
                add(new HashMap<String, String>() {// simple type
                    {
                        put("pivot", "Person/Kids/Kid[1]/Habits/Habit");
                        put("key", "");
                        put("document", "PartialDelete_4.xml");
                        put("report", "report4");
                    }
                });
                add(new HashMap<String, String>() {// simple type
                    {
                        put("pivot", "Person/Kids/Kid[2]/Habits/Habit");
                        put("key", "");
                        put("document", "PartialDelete_4.xml");
                        put("report", "report5");
                    }
                });
                add(new HashMap<String, String>() {// simple type
                    {
                        put("pivot", "Person/Pets");
                        put("key", "/Pet");
                        put("document", "PartialDelete_5.xml");
                        put("report", "report6");
                    }
                });
                add(new HashMap<String, String>() {// two same label name in document
                    {
                        put("pivot", "Person/phones/PHONE_NUMBER");
                        put("key", "/PHONE_NUMBER");
                        put("document", "PartialDelete_8.xml");
                        put("report", "report7");
                    }
                });
            }
        };

        @SuppressWarnings("serial")
        Map<String, List<String[]>> reportDatas = new HashMap<String, List<String[]>>() {
            {
                put("report1", new ArrayList<String[]>() {
                    {
                        add(new String[] { "Houses/House[3]", "[3]", "" });
                        add(new String[] { "Houses/House[2]", "[2]", "" });
                        add(new String[] { "Houses/House[1]", "[1]", "[3]" });
                    }
                });
                put("report2", new ArrayList<String[]>() {
                    {
                        add(new String[] { "Kids/Kid[3]/Name", "k3", "" });
                        add(new String[] { "Kids/Kid[3]/Age", "3", "" });
                        add(new String[] { "Kids/Kid[2]/Name", "k2", "" });
                        add(new String[] { "Kids/Kid[2]/Age", "2", "" });
                        add(new String[] { "Kids/Kid[2]/Habits/Habit[3]", "Boxing", "" });
                        add(new String[] { "Kids/Kid[2]/Habits/Habit[2]", "Football", "" });
                        add(new String[] { "Kids/Kid[2]/Habits/Habit[1]", "Basketball", "" });
                    }
                });
                put("report3", new ArrayList<String[]>() {
                    {
                        add(new String[] { "Habits/Habit[4]", "Boxing", "" });
                        add(new String[] { "Habits/Habit[3]", "Tennis", "" });
                        add(new String[] { "Habits/Habit[2]", "Football", "" });
                        add(new String[] { "Habits/Habit[1]", "Basketball", "Tennis" });
                    }
                });
                put("report4", new ArrayList<String[]>() {
                    {
                        add(new String[] { "Kids/Kid[1]/Habits/Habit[3]", "Tennis", "" });
                        add(new String[] { "Kids/Kid[1]/Habits/Habit[2]", "Football", "" });
                        add(new String[] { "Kids/Kid[1]/Habits/Habit[1]", "Basketball", "Tennis" });
                    }
                });
                put("report5", new ArrayList<String[]>() {
                    {
                        add(new String[] { "Kids/Kid[2]/Habits/Habit[3]", "Boxing", "" });
                        add(new String[] { "Kids/Kid[2]/Habits/Habit[2]", "Football", "" });
                        add(new String[] { "Kids/Kid[2]/Habits/Habit[1]", "Basketball", "Boxing" });
                    }
                });
                put("report6", new ArrayList<String[]>() {
                    {
                        add(new String[] { "Pets[4]/Pet", "Cow", "" });
                        add(new String[] { "Pets[3]/Pet", "Pig", "" });
                        add(new String[] { "Pets[2]/Pet", "Dog", "Cow" });
                        add(new String[] { "Pets[1]/Pet", "Cat", "Dog" });
                    }
                });
                put("report7", new ArrayList<String[]>() {
                    {
                        add(new String[] { "phones[2]/PHONE_NUMBER[1]/PHONE_NUMBER", "456", "" });
                    }
                });
            }
        };

        for (Map<String, String> data : testDatas) {
            TestSaverSource source = new TestSaverSource(repository, true, "PartialDelete_original_1.xml", "PartialDelete.xsd");
            source.setUserName("admin");

            SaverSession session = SaverSession.newSession(source);
            InputStream recordXml = DocumentSaveTest.class.getResourceAsStream(data.get("document"));
            DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source", recordXml,
                    true, true, data.get("pivot"), data.get("key"), -1, true, true);
            DocumentSaver saver = context.createSaver();
            saver.save(session, context);
            MockCommitter committer = new MockCommitter();
            session.end(committer);

            MutableDocument updateReportDocument = context.getUpdateReportDocument();
            assertNotNull(updateReportDocument);
            Document doc = updateReportDocument.asDOM();
            List<String[]> reportData = reportDatas.get(data.get("report"));
            for (int i = 0; i < reportData.size(); i++) {
                String[] value = reportData.get(i);
                String path = (String) evaluate(doc.getDocumentElement(), "Item[" + (i + 1) + "]/path");
                String oldValue = (String) evaluate(doc.getDocumentElement(), "Item[" + (i + 1) + "]/oldValue");
                String newValue = (String) evaluate(doc.getDocumentElement(), "Item[" + (i + 1) + "]/newValue");
                assertEquals(value[0], path);
                assertEquals(value[1], oldValue);
                assertEquals(value[2], newValue);
            }
            // Primary Key Info (UserAction.PARTIAL_DELETE)
            String primaryKeyInfo = (String) evaluate(doc.getDocumentElement(), "PrimaryKeyInfo");
            assertEquals("1-p1", primaryKeyInfo);
        }
    }

    public void testPartialDelete() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("PartialDelete.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        @SuppressWarnings("serial")
        List<Map<String, String>> testDatas = new ArrayList<Map<String, String>>() {

            {
                add(new HashMap<String, String>() {// foreign key

                    {
                        put("original", "PartialDelete_original_1.xml");
                        put("pivot", "Person/Houses/House");
                        put("key", ".");
                        put("document", "PartialDelete_1.xml");
                        put("asssertPath", "/Person/Houses/House[1]");
                        put("asssertValue", "[3]");
                    }
                });
                add(new HashMap<String, String>() {// complex type

                    {
                        put("original", "PartialDelete_original_1.xml");
                        put("pivot", "Person/Kids/Kid");
                        put("key", "/Name");
                        put("document", "PartialDelete_2.xml");
                        put("asssertPath", "/Person/Kids/Kid[1]/Name");
                        put("asssertValue", "k1");
                    }
                });
                add(new HashMap<String, String>() {// simple type

                    {
                        put("original", "PartialDelete_original_1.xml");
                        put("pivot", "Person/Habits/Habit");
                        put("key", "");
                        put("document", "PartialDelete_3.xml");
                        put("asssertPath", "/Person/Habits/Habit[1]");
                        put("asssertValue", "Tennis");
                    }
                });
                add(new HashMap<String, String>() {// simple type

                    {
                        put("original", "PartialDelete_original_1.xml");
                        put("pivot", "Person/Kids/Kid[1]/Habits/Habit");
                        put("key", "");
                        put("document", "PartialDelete_4.xml");
                        put("asssertPath", "/Person/Kids/Kid[1]/Habits/Habit[1]");
                        put("asssertValue", "Tennis");
                    }
                });
                add(new HashMap<String, String>() {// simple type

                    {
                        put("original", "PartialDelete_original_1.xml");
                        put("pivot", "Person/Kids/Kid[2]/Habits/Habit");
                        put("key", "");
                        put("document", "PartialDelete_4.xml");
                        put("asssertPath", "/Person/Kids/Kid[2]/Habits/Habit[1]");
                        put("asssertValue", "Boxing");
                    }
                });
                add(new HashMap<String, String>() {// simple type

                    {
                        put("original", "PartialDelete_original_1.xml");
                        put("pivot", "Person/Pets");
                        put("key", "/Pet");
                        put("document", "PartialDelete_5.xml");
                        put("asssertPath", "/Person/Pets[1]/Pet");
                        put("asssertValue", "Dog");
                    }
                });
                add(new HashMap<String, String>() {// delete all children

                    {
                        put("original", "PartialDelete_original_1.xml");
                        put("pivot", "Person/Kids/Kid");
                        put("key", "/Name");
                        put("document", "PartialDelete_6.xml");
                        put("asssertPath", "/Person/Kids");
                        put("asssertValue", "");
                    }
                });
                add(new HashMap<String, String>() {// delete last child

                    {
                        put("original", "PartialDelete_original_2.xml");
                        put("pivot", "Person/Kids/Kid");
                        put("key", "/Name");
                        put("document", "PartialDelete_7.xml");
                        put("asssertPath", "/Person/Kids");
                        put("asssertValue", "");
                    }
                });
            }
        };

        for (Map<String, String> data : testDatas) {
            TestSaverSource source = new TestSaverSource(repository, true, data.get("original"), "PartialDelete.xsd");
            source.setUserName("admin");

            SaverSession session = SaverSession.newSession(source);
            InputStream recordXml = DocumentSaveTest.class.getResourceAsStream(data.get("document"));
            DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source", recordXml,
                    true, true, data.get("pivot"), data.get("key"), -1, true, true);
            DocumentSaver saver = context.createSaver();
            saver.save(session, context);
            MockCommitter committer = new MockCommitter();
            session.end(committer);

            assertTrue(committer.hasSaved());
            Element committedElement = committer.getCommittedElement();
            assertEquals(data.get("asssertValue"), evaluate(committedElement, data.get("asssertPath")));
        }
    }

    public void testUpdate() throws Exception {
        // TODO Test for modification of id (this test modifies id but this is intentional).
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test1_original.xml", "metadata1.xsd");
        source.setUserName("Demo_Manager");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Chicago", evaluate(committedElement, "/Agency/Name"));
        assertEquals("Chicago", evaluate(committedElement, "/Agency/City"));
        assertEquals("", evaluate(committedElement, "/Agency/State"));
    }

    public void testUpdateOnNonExisting() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, false, "", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        try {
            saver.save(session, context);
            fail("Expected an exception (update on a record that does not exist.");
        } catch (Exception e) {
            // Expected
        }
    }

    public void testUpdateConf() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test1_original.xml", "metadata1.xsd");
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertFalse(source.hasCalledInitAutoIncrement);

        XmlSchemaValidator.invalidateCache();
        repository.load(DocumentSaveTest.class.getResourceAsStream("CONF.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("CONF", repository);

        source = new TestSaverSource(repository, true, "test_conf_original.xml", "CONF.xsd");
        source.setUserName("admin");

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test_conf.xml");
        context = session.getContextFactory().create("MDM", "CONF", false, recordXml);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        Element committedElement = committer.getCommittedElement();
        assertEquals("<AutoIncrement>" + "<id>AutoIncrement</id>" + "<entry>"
                + "<key>[HEAD].CoreTestsContainer.auto_increment.auto_increment</key>" + "<value>1</value>" + "</entry>"
                + "<entry>" + "<key>[HEAD].Product.ProductFamily.Id</key>" + "<value>30</value>" + "</entry>" + "<entry>"
                + "<key>[HEAD].CoreTestsContainer.auto_increment1.auto_increment1</key>" + "<value>1</value>" + "</entry>"
                + "</AutoIncrement>", XMLUtils.nodeToString(committedElement, true, false).replaceAll("\r\n", "\n"));
        assertTrue(source.hasCalledInitAutoIncrement);

    }

    public void testWithClone() throws Exception {
        // this is the add one ContractDetailSubType, need to add the update report test
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contract", repository);

        SaverSource source = new TestSaverSource(repository, true, "test13_original.xml", "metadata3.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test13.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contract", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("ContractDetailSubType", evaluate(committedElement, "/Contract/detail[1]/@xsi:type"));
        assertEquals("ContractDetailSubType", evaluate(committedElement, "/Contract/detail[2]/@xsi:type"));
        assertEquals("sdfsdf", evaluate(committedElement, "/Contract/detail[2]/code"));
        assertEquals("sdfsdf", evaluate(committedElement, "/Contract/detail[2]/features/actor"));

        // this is the add one ContractDetailSubType, need to add the update report test
        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();
        String path = (String) evaluate(doc.getDocumentElement(), "Item[1]/path");
        String oldValue = (String) evaluate(doc.getDocumentElement(), "Item[1]/oldValue");
        String newValue = (String) evaluate(doc.getDocumentElement(), "Item[1]/newValue");
        assertEquals("detail[2]/@xsi:type", path);
        assertEquals("", oldValue);
        assertEquals("ContractDetailSubType", newValue);
        assertUpdateReportAction(doc, 1, "detail[2]/@xsi:type", "", "ContractDetailSubType");
        assertUpdateReportAction(doc, 2, "detail[2]/code", "", "sdfsdf");
        assertUpdateReportAction(doc, 3, "detail[2]/features/actor", "", "sdfsdf");
    }



    public void testWithCloneSuperType() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contract", repository);

        SaverSource source = new TestSaverSource(repository, true, "test77_original.xml", "metadata3.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test77.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contract", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("ContractDetailType", evaluate(committedElement, "/Contract/detail[1]/@xsi:type"));
        assertEquals("ContractDetailType", evaluate(committedElement, "/Contract/detail[2]/@xsi:type"));
        assertEquals("code-1", evaluate(committedElement, "/Contract/detail[2]/code"));
        assertEquals("code-1", evaluate(committedElement, "/Contract/detail[1]/code"));

        // test update report
        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();
        assertUpdateReportAction(doc, 1, "detail[2]/code", "", "code-1");
    }

    public void testWithChangeSubTypeToSuperType() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contract", repository);

        SaverSource source = new TestSaverSource(repository, true, "test78_original.xml", "metadata3.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test78.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contract", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("ContractDetailType", evaluate(committedElement, "/Contract/detail[1]/@xsi:type"));
        assertEquals("sdfsdf", evaluate(committedElement, "/Contract/detail[1]/code"));

        // test update report
        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();
        assertUpdateReportAction(doc, 1, "detail[1]/features/vendor", "[vendor-1-1]", "");
        assertUpdateReportAction(doc, 2, "detail[1]/features/boolValue", "true", "");
        assertUpdateReportAction(doc, 3, "detail[1]/features/actor", "actor-1", "");
        assertUpdateReportAction(doc, 4, "detail[1]/features", "", "");
        assertUpdateReportAction(doc, 5, "detail[1]/ReadOnlyEle", "[1]", "");
        assertUpdateReportAction(doc, 6, "detail[1]/@xsi:type", "ContractDetailSubType", "ContractDetailType");
        assertUpdateReportAction(doc, 7, "detail[1]/code", "code-1", "sdfsdf");
    }

    public void testSubclassTypeChange() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contract", repository);

        SaverSource source = new TestSaverSource(repository, true, "test14_original.xml", "metadata3.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test14.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contract", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("ContractDetailType", evaluate(committedElement, "/Contract/detail[1]/@xsi:type"));
        assertEquals("sdfsdf", evaluate(committedElement, "/Contract/detail[1]/code"));
    }

    public void testSubclassTypeChange2() throws Exception {
        // this is change detail from ContractDetailType to ContractDetailSubType
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contract", repository);

        SaverSource source = new TestSaverSource(repository, true, "test15_original.xml", "metadata3.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test15.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contract", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("ContractDetailSubType", evaluate(committedElement, "/Contract/detail[1]/@xsi:type"));
        assertEquals("cccccc", evaluate(committedElement, "/Contract/detail[1]/code"));
        assertEquals("sdfsdf", evaluate(committedElement, "/Contract/detail[1]/features/actor"));

        // this is change detail from ContractDetailType to ContractDetailSubType
        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();
        assertUpdateReportAction(doc, 1, "detail[1]/@xsi:type", "ContractDetailType", "ContractDetailSubType");
        assertUpdateReportAction(doc, 2, "detail[1]/code", "sdfsdf", "cccccc");
        assertUpdateReportAction(doc, 3, "detail[1]/features/actor", "", "sdfsdf");
        assertUpdateReportAction(doc, 4, "detail[1]/features/vendor[1]", "", "sdf");
        assertUpdateReportAction(doc, 5, "detail[1]/features/boolValue", "", "true");
        assertUpdateReportAction(doc, 6, "detail[1]/ReadOnlyEle[1]", "", "sdf");
        assertUpdateReportAction(doc, 7, "detail[1]/boolTest", "", "true");
    }

    public void testUpdateSequence() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contract", repository);

        SaverSource source = new TestSaverSource(repository, true, "test16_original.xml", "metadata3.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test16.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contract", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
    }

    public void testSubclassTypeChange3() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contract", repository);

        SaverSource source = new TestSaverSource(repository, true, "test16_original.xml", "metadata3.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test16.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contract", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("ContractDetailSubType2", evaluate(committedElement, "/Contract/detail[1]/@xsi:type"));
        assertEquals("sdfsdf", evaluate(committedElement, "/Contract/detail[1]/code"));
    }

    // TMDM-14910 Can't save record after adding occurrence using attached customer's datamodel
    public void testMultiLevelPolymType() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("annuaireGroupeExpress_1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Societe", repository);

        SaverSource source = new TestSaverSource(repository, true, "societe_2_original.xml", "annuaireGroupeExpress_1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("societe_2.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Societe", "Source", recordXml, false, true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("PersonneMoraleGroupe", evaluate(committedElement, "/Societe/Actionnaires/Actionnaire/TypeMembre/@xsi:type"));
        assertEquals("myMembreName2", evaluate(committedElement, "/Societe/Actionnaires/Actionnaire/TypeMembre/TypeMembreName"));
        assertEquals("44", evaluate(committedElement, "/Societe/Actionnaires/Actionnaire/TypeMembre/Groupe"));
    }

    public void testUpdateSecurity() throws Exception {
        // TODO Test for modification of id (this test modifies id but this is intentional).
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test8_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test8.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);
        Element committedElement = committer.getCommittedElement();
        assertEquals("10001", evaluate(committedElement, "/Agency/Zip"));
        assertEquals("NY", evaluate(committedElement, "/Agency/State"));

        // Test with replace.
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test8.xml");
        context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        session.end(committer);
        committedElement = committer.getCommittedElement();
        assertEquals("10001", evaluate(committedElement, "/Agency/Zip"));
        assertEquals("NY", evaluate(committedElement, "/Agency/State"));

        // Test changing user name (and user's roles).
        source.setUserName("admin");
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test8.xml");
        context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        session.end(committer);
        committedElement = committer.getCommittedElement();
        assertEquals("10001", evaluate(committedElement, "/Agency/Zip"));
        assertEquals("NY", evaluate(committedElement, "/Agency/State"));
    }

    public void testReplaceSecurity() throws Exception {
        // TODO Test for modification of id (this test modifies id but this is intentional).
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test8_original.xml", "metadata1.xsd");

        // Test with replace.
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test8.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);
        Element committedElement = committer.getCommittedElement();
        assertEquals("10001", evaluate(committedElement, "/Agency/Zip"));
        assertEquals("NY", evaluate(committedElement, "/Agency/State"));
    }

    public void testNoUpdate() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test4_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test4.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertFalse(committer.hasSaved());
    }

    public void testLegacyUpdate() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test5_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test5.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Description", evaluate(committedElement, "/Product/Description"));
        assertEquals("60", evaluate(committedElement, "/Product/Price"));
        assertEquals("Lemon", evaluate(committedElement, "/Product/Features/Colors/Color[1]"));
        assertEquals("Light Blue", evaluate(committedElement, "/Product/Features/Colors/Color[2]"));
    }

    public void testLegacyUpdate2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test6_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test6.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Description", evaluate(committedElement, "/Product/Description"));
        assertEquals("60", evaluate(committedElement, "/Product/Price"));
        assertEquals("Lemon", evaluate(committedElement, "/Product/Features/Colors/Color[1]"));
        assertEquals("Light Blue", evaluate(committedElement, "/Product/Features/Colors/Color[2]"));
    }

    public void testLegacyUpdate3() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test7_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test7.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("60", evaluate(committedElement, "/Product/Price"));
    }

    public void testSchematronValidation() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test9_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test9.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("JohDo", evaluate(committedElement, "/Agent/Id"));
        assertEquals("John", evaluate(committedElement, "/Agent/Firstname"));
        assertEquals("Doe", evaluate(committedElement, "/Agent/Lastname"));
        assertEquals("1", evaluate(committedElement, "/Agent/CommissionCode"));
        assertEquals("2010-01-01", evaluate(committedElement, "/Agent/StartDate"));
    }

    public void testSystemUpdate() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("updateReport.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("UpdateReport", repository);

        SaverSource source = new TestSaverSource(repository, true, "test3_original.xml", "updateReport.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test3.xml");
        DocumentSaverContext context = session.getContextFactory().create("UpdateReport", "UpdateReport", "Source", recordXml,
                false, true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals(UpdateReportPOJO.OPERATION_TYPE_LOGICAL_DELETE, evaluate(committedElement, "/Update/OperationType"));
    }

    public void testSystemCreate() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("updateReport.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("UpdateReport", repository);

        SaverSource source = new TestSaverSource(repository, false, "", "updateReport.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test3.xml");
        DocumentSaverContext context = session.getContextFactory().create("UpdateReport", "UpdateReport", "Source", recordXml,
                true, true, false, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals(UpdateReportPOJO.OPERATION_TYPE_LOGICAL_DELETE, evaluate(committedElement, "/Update/OperationType"));
    }

    public void testUpdateReportChangeToSubType() throws Exception {
        // Change detail from ContractDetailType to ContractDetailSubType
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contract", repository);

        SaverSource source = new TestSaverSource(repository, true, "test63_original.xml", "metadata3.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test63.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contract", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("ContractDetailSubType", evaluate(committedElement, "/Contract/detail[1]/@xsi:type"));
        assertEquals("code-new", evaluate(committedElement, "/Contract/detail[1]/code"));

        // test update report
        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();
        assertUpdateReportAction(doc, 1, "comment[1]", "comment-original", "comment-new");
        assertUpdateReportAction(doc, 2, "detail[1]/@xsi:type", "ContractDetailType", "ContractDetailSubType");
        assertUpdateReportAction(doc, 3, "detail[1]/code", "code-original", "code-new");
        assertUpdateReportAction(doc, 4, "detail[1]/features/actor", "", "actor-new");
    }

    public void testUpdateReportForContractRemoveOneDetail() throws Exception {
        // two detail(ContractDetailSubType), remove one
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contract", repository);

        SaverSource source = new TestSaverSource(repository, true, "test76_original.xml", "metadata3.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test76.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contract", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("ContractDetailSubType", evaluate(committedElement, "/Contract/detail[1]/@xsi:type"));
        assertEquals("aa-1", evaluate(committedElement, "/Contract/detail[1]/code"));

        // test update report
        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();
        assertUpdateReportAction(doc, 1, "comment[1]", "comment-original", "comment-new");
        assertUpdateReportAction(doc, 2, "detail[2]/code", "bb-1", "");
        assertUpdateReportAction(doc, 3, "detail[2]/features/actor", "bb-2", "");
        assertUpdateReportAction(doc, 4, "detail[2]/features/vendor[1]", "bb-3", "");
        assertUpdateReportAction(doc, 5, "detail[2]/features/boolValue", "true", "");
        assertUpdateReportAction(doc, 6, "detail[2]/features", "", "");
        assertUpdateReportAction(doc, 7, "detail[2]/ReadOnlyEle[1]", "readOnlyEle-two", "");
        assertUpdateReportAction(doc, 8, "detail[2]/boolTest", "true", "");
        assertUpdateReportAction(doc, 9, "detail[2]/@xsi:type", "ContractDetailSubType", "");
    }

    public void testUpdateReportChangeToSuperTypeAction() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contract", repository);

        SaverSource source = new TestSaverSource(repository, true, "test64_original.xml", "metadata3.xsd");

        SaverSession session = SaverSession.newSession(source);

        InputStream recordXml2 = DocumentSaveTest.class.getResourceAsStream("test64.xml");
        DocumentSaverContext context2 = session.getContextFactory().create("MDM", "Contract", "Source", recordXml2, false, true,
                true, false, false);
        DocumentSaver saver2 = context2.createSaver();
        saver2.save(session, context2);
        MockCommitter committer2 = new MockCommitter();
        session.end(committer2);

        assertTrue(committer2.hasSaved());
        Element committedElement2 = committer2.getCommittedElement();
        assertEquals("ContractDetailType", evaluate(committedElement2, "/Contract/detail[1]/@xsi:type"));
        assertEquals("code-new", evaluate(committedElement2, "/Contract/detail[1]/code"));

        // test update report
        MutableDocument updateReportDocument2 = context2.getUpdateReportDocument();
        assertNotNull(updateReportDocument2);
        Document doc = updateReportDocument2.asDOM();
        assertUpdateReportAction(doc, 1, "comment[1]", "comment-original", "comment-new");
        assertUpdateReportAction(doc, 2, "detail[1]/features/vendor", "[vendor-original]", "");
        assertUpdateReportAction(doc, 3, "detail[1]/features/boolValue", "true", "");
        assertUpdateReportAction(doc, 4, "detail[1]/features/actor", "actor-original", "");
        assertUpdateReportAction(doc, 5, "detail[1]/features", "", "");
        assertUpdateReportAction(doc, 6, "detail[1]/boolTest", "true", "");
        assertUpdateReportAction(doc, 7, "detail[1]/ReadOnlyEle", "[readOnlyEle-original]", "");
        assertUpdateReportAction(doc, 8, "detail[1]/@xsi:type", "ContractDetailSubType", "ContractDetailType");
        assertUpdateReportAction(doc, 9, "detail[1]/code", "code-original", "code-new");
    }

    public void testUpdateFKToSuperType() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata18.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("FKChangeTest", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test66_original.xml", "metadata18.xsd");
        source.setUserName("administrator");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test66.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "FKChangeTest", "Source", recordXml, false,
                true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
    }

    public void testProductUpdate() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test2_original.xml", "metadata1.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test2.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("60", evaluate(committedElement, "/Product/Price"));
        assertEquals("TT", evaluate(committedElement, "/Product/Description"));
        assertEquals("New product name", evaluate(committedElement, "/Product/Name"));
    }

    public void testProductUpdate2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test11_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test11.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("222", evaluate(committedElement, "/Product/Price"));
        assertEquals("Small", evaluate(committedElement, "/Product/Features/Sizes/Size[1]"));
        assertEquals("", evaluate(committedElement, "/Product/Features/Sizes/Size[2]"));
        assertEquals("", evaluate(committedElement, "/Product/Features/Sizes/Size[3]"));
        assertEquals("White", evaluate(committedElement, "/Product/Features/Colors/Color[1]"));
        assertEquals("", evaluate(committedElement, "/Product/Features/Colors/Color[2]"));
        assertEquals("", evaluate(committedElement, "/Product/Features/Colors/Color[3]"));
    }

    public void testProductPartialUpdate() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test11_original.xml", "metadata1.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream(
                "<Product><Id>1</Id><Features><Colors><Color>Light Pink</Color></Colors></Features></Product>"
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source",
                partialUpdateContent, true, false, "/Product/Features/Colors/Color", "", -1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals("Light Pink", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[4]"));
    }

    public void testProductPartialUpdateWithIndex() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test11_original.xml", "metadata1.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream(("<Product>\n" + "    <Id>1</Id>\n" + "    <Features>\n"
                + "        <Colors>" + "           <Color>Light Pink</Color>\n" + "        </Colors>\n" + "    </Features>\n"
                + "</Product>\n").getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source",
                partialUpdateContent, true, false, "/Product/Features/Colors/Color", "", 1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals("Light Pink", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[1]"));
        assertEquals("Khaki", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[4]"));
    }

    public void testProductPartialUpdateOverwriteTrue() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test11_original.xml", "metadata1.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream((
                "<Product><Id>11</Id><Features><Colors><Color>Light Blue</Color></Colors></Features></Product>\n")
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source",
                partialUpdateContent, true, false, "/Product/Features/Colors", "/Color", 1, true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals("Light Blue", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[1]"));
        assertEquals("White", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[2]"));
        assertEquals("Light Blue", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[3]"));
        assertEquals("Khaki", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[4]"));
    }

    public void testProductPartialUpdateWithIndexOutOfRange() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test11_original.xml", "metadata1.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream((
                "<Product><Id>11</Id><Features><Colors><Color>Light Blue</Color></Colors></Features></Product>\n")
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source",
                partialUpdateContent, true, false, "/Product/Features/Colors", "/Color", 10000, true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals("White", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[1]"));
        assertEquals("Light Blue", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[2]"));
        assertEquals("Khaki", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[3]"));
        assertEquals("Light Blue", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[4]"));
    }

    public void testProductPartialUpdateDeleteTrue() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("PartialDelete.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "PartialDelete_original_2.xml", "PartialDelete.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream((
                "<Person><Id>1</Id><Kids><Kid><Name>k1</Name></Kid></Kids></Person>")
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source",
                partialUpdateContent, true, false, "/Person/Kids/Kid", "/Name", 1, false, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);
        assertTrue(committer.hasSaved());
        assertEquals("", evaluate(committer.getCommittedElement(), "/Person/Kids"));
    }

    public void testProductPartialUpdateOverwriteFalse() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test11_original.xml", "metadata1.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream((
                "<Product><Id>1221</Id><Features><Colors><Color>Light Blue</Color></Colors></Features></Product>")
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source",
                partialUpdateContent, true, false, "/Product/Features/Colors", "/Color", 1, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals("Light Blue", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[1]"));
        assertEquals("White", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[2]"));
        assertEquals("Light Blue", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[3]"));
        assertEquals("Khaki", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[4]"));
    }

    public void testProductPartialUpdate2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test26_original.xml", "metadata1.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream partialUpdateContent = new ByteArrayInputStream((
                "<Product><Id>1</Id><Features><Colors><Color>Light Pink</Color></Colors></Features></Product>")
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "DStar", "Source",
                partialUpdateContent, true, true, "/Product/Features/Colors/Color", "", -1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals("Light Pink", evaluate(committer.getCommittedElement(), "/Product/Features/Colors/Color[1]"));
    }

    public void testInheritance() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata2.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test12_original.xml", "metadata2.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test12.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertFalse(committer.hasSaved()); // Should be true once there are actual changes in test12.xml
    }

    public void testBeforeSavingWithAlterRecord() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        // create record
        boolean isOK = true;
        boolean newOutput = true;
        SaverSource source = new AlterRecordTestSaverSource(repository, false, "test1_original.xml", isOK, newOutput);

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        assertEquals("change the value successfully!", saver.getBeforeSavingMessage());
        MockCommitter committer = new MockCommitter();
        session.end(committer);
        assertTrue(committer.hasSaved());

        assertEquals("beforeSaving_Agency", evaluate(committer.getCommittedElement(), "/Agency/Name"));
        assertEquals("Chicago", evaluate(committer.getCommittedElement(), "/Agency/City"));
        assertEquals("http://www.newSite.org", evaluate(committer.getCommittedElement(), "/Agency/Information/MoreInfo"));

        // alter data record
        isOK = true;
        newOutput = true;
        source = new AlterRecordTestSaverSource(repository, true, "test1_original.xml", isOK, newOutput, "System_Admin");

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true, true, false);
        saver = context.createSaver();
        saver.save(session, context);
        assertEquals("change the value successfully!", saver.getBeforeSavingMessage());
        committer = new MockCommitter();
        session.end(committer);
        assertTrue(committer.hasSaved());

        assertEquals("beforeSaving_Agency", evaluate(committer.getCommittedElement(), "/Agency/Name"));
        assertEquals("Chicago", evaluate(committer.getCommittedElement(), "/Agency/City"));
        assertEquals("", evaluate(committer.getCommittedElement(), "/Agency/State"));
        assertEquals("04102", evaluate(committer.getCommittedElement(), "/Agency/Zip"));
        assertEquals("EAST", evaluate(committer.getCommittedElement(), "/Agency/Region"));
        assertEquals("http://www.newSite.org", evaluate(committer.getCommittedElement(), "/Agency/Information/MoreInfo"));

        //
        isOK = false;
        newOutput = true;
        source = new AlterRecordTestSaverSource(repository, false, "test1_original.xml", isOK, newOutput);

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true, true, false);
        saver = context.createSaver();
        try {
            saver.save(session, context);
            fail("Expected an exception.");
        } catch (SaveException e) {
            // Expected
            assertEquals("change the value failed!", e.getBeforeSavingMessage());
        }
        committer = new MockCommitter();
        session.end(committer);
        assertFalse(committer.hasSaved());

        //
        isOK = false;
        newOutput = false;
        source = new AlterRecordTestSaverSource(repository, false, "test1_original.xml", isOK, newOutput);

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true, true, false);
        saver = context.createSaver();
        try {
            saver.save(session, context);
            fail("Expected an exception.");
        } catch (SaveException e) {
            // Expected
            assertEquals("change the value failed!", e.getBeforeSavingMessage());
        }
        committer = new MockCommitter();
        session.end(committer);
        assertFalse(committer.hasSaved());

        //
        isOK = true;
        newOutput = false;
        source = new AlterRecordTestSaverSource(repository, false, "test1_original.xml", isOK, newOutput);

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true, true, false);
        saver = context.createSaver();
        saver.save(session, context);
        assertEquals("change the value successfully!", saver.getBeforeSavingMessage());
        committer = new MockCommitter();
        session.end(committer);
        assertTrue(committer.hasSaved());

    }

    public void testUpdateReportAfterBeforeSavingProcess() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        // Test updateReport
        boolean isOK = true;
        boolean newOutput = true;
        SaverSource source = new AlterRecordTestSaverSource(repository, true, "test1_original.xml", isOK, newOutput);

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        assertEquals("change the value successfully!", saver.getBeforeSavingMessage());

        String lineSeparator = System.getProperty("line.separator");
        StringBuilder expectedUserXmlBuilder = new StringBuilder(
                "<Agency xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
        expectedUserXmlBuilder.append(lineSeparator);
        expectedUserXmlBuilder.append("<Id>5258f292-5670-473b-bc01-8b63434682f3</Id>");
        expectedUserXmlBuilder.append(lineSeparator);
        expectedUserXmlBuilder.append("<Name>beforeSaving_Agency</Name>");
        expectedUserXmlBuilder.append(lineSeparator);
        expectedUserXmlBuilder.append("<City>Chicago</City>");
        expectedUserXmlBuilder.append(lineSeparator);
        expectedUserXmlBuilder.append("<State/>");
        expectedUserXmlBuilder.append(lineSeparator);
        expectedUserXmlBuilder.append("<Zip>04102</Zip>");
        expectedUserXmlBuilder.append(lineSeparator);
        expectedUserXmlBuilder.append("<Region>EAST</Region>");
        expectedUserXmlBuilder.append(lineSeparator);
        expectedUserXmlBuilder.append("<Information>");
        expectedUserXmlBuilder.append(lineSeparator);
        expectedUserXmlBuilder.append("<MoreInfo>http://www.newSite.org</MoreInfo>");
        expectedUserXmlBuilder.append(lineSeparator);
        expectedUserXmlBuilder.append("<MoreInfo>http://www.newSite2.org</MoreInfo>");
        expectedUserXmlBuilder.append(lineSeparator);
        expectedUserXmlBuilder.append("</Information>");
        expectedUserXmlBuilder.append(lineSeparator);
        expectedUserXmlBuilder.append("</Agency>");
        expectedUserXmlBuilder.append(lineSeparator);
        String expectedUserXml = expectedUserXmlBuilder.toString();

        assertEquals(expectedUserXml, context.getUserDocument().exportToString());
        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        assertNotNull(updateReportDocument);
        Document doc = updateReportDocument.asDOM();

        assertUpdateReportAction(doc, 1, "Name", "name1", "beforeSaving_Agency");
        assertUpdateReportAction(doc, 2, "City", "city1", "Chicago");
        assertUpdateReportAction(doc, 3, "State", "ME", "");
        assertUpdateReportAction(doc, 4, "Information/MoreInfo[2]", "", "http://www.newSite2.org");

        String path = (String) evaluate(doc.getDocumentElement(), "OperationType");
        assertEquals("UPDATE", path);

        MockCommitter committer = new MockCommitter();
        session.end(committer);
        assertTrue(committer.hasSaved());
    }

    public void testBeforeSavingWithAutoIncrementPkRecord() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        boolean isOK = true;
        boolean newOutput = true;
        TestSaverSource source = new TestSaverSourceWithOutputReportItem(repository, false, "", isOK, newOutput);

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("autoIncrementPK.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        assertEquals("Save the value successfully!", saver.getBeforeSavingMessage());
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("0", evaluate(committedElement, "/ProductFamily/Id"));
        assertEquals("1", context.getId()[0]);
        assertEquals("testAutoIncrementPK", evaluate(committedElement, "/ProductFamily/Name"));
    }

    public void testBeforeSavingByModifyPK() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);
        
        boolean isOK = true;
        boolean newOutput = true;
        TestSaverSource source = new TestSaverSourceWithProductOutputReportItem(repository, false, "", isOK, newOutput);
        
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test_product_original.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        assertEquals("Save the value successfully!", saver.getBeforeSavingMessage());
        MockCommitter committer = new MockCommitter();
        session.end(committer);
        
        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("id2", evaluate(committedElement, "/Product/Id"));
        assertEquals("id2", context.getId()[0]);
        assertEquals("Product Name1", evaluate(committedElement, "/Product/Name"));
    }
    
    public void testUpdateAutoIncrementRecord() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);
        TestSaverSource source = new TestSaverSource(repository, false, "", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("autoIncrementPK.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Product Family #1", evaluate(committedElement, "/ProductFamily/Name"));
    }

    /**
     * test for TMDM-9804 AUTO_INCREMENT issue in cluster enviroment
     */
    public void testUpdateAutoIncrementRecordForLongTransactionInCluster() throws Exception {
        MDMConfiguration.getConfiguration().setProperty("system.cluster", Boolean.TRUE.toString());
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);
        TestSaverSource source = new TestSaverSource(repository, false, "", "metadata1.xsd");
        TransactionManager manager = ServerContext.INSTANCE.get().getTransactionManager();
        manager.create(com.amalto.core.storage.transaction.Transaction.Lifetime.LONG);
        manager.currentTransaction().begin();
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("autoIncrementPK.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);
        manager.currentTransaction().commit();

        assertTrue(source.hasSavedAutoIncrement());
        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Product Family #1", evaluate(committedElement, "/ProductFamily/Name"));
    }

    public void testCreatePerformance() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, false, "test1_original.xml", "metadata1.xsd");
        source.setUserName("admin");
        {
            SaverSession session = SaverSession.newSession(source);
            {
                for (int i = 0; i < 10; i++) {
                    InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
                    DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true,
                            true, true, false, false);
                    DocumentSaver saver = context.createSaver();
                    saver.save(session, context);
                }
            }
            session.end(new MockCommitter());
        }

        long saveTime = System.nanoTime();
        {
            SaverSession session = SaverSession.newSession(source);
            {
                for (int i = 0; i < 200; i++) {
                    InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
                    DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true,
                            true, true, false, false);
                    DocumentSaver saver = context.createSaver();
                    saver.save(session, context);
                }
            }
            session.end(new MockCommitter());
        }
        LOG.info("Time (mean): " + (System.nanoTime() - saveTime) / 200f / 1000f / 1000f + " ms.");
    }

    public void testUpdatePerformance() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test1_original.xml", "metadata1.xsd");
        source.setUserName("admin");
        {
            SaverSession session = SaverSession.newSession(source);
            {
                for (int i = 0; i < 10; i++) {
                    InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
                    DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false,
                            true, true, false, false);
                    DocumentSaver saver = context.createSaver();
                    saver.save(session, context);
                }
            }
            session.end(new MockCommitter());
        }

        long saveTime = System.currentTimeMillis();
        long max = 0;
        long min = Long.MAX_VALUE;
        {
            SaverSession session = SaverSession.newSession(source);
            {
                for (int i = 0; i < 200; i++) {
                    long singleExecTime = System.currentTimeMillis();
                    {
                        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
                        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml,
                                false, true, true, false, false);
                        DocumentSaver saver = context.createSaver();
                        saver.save(session, context);
                    }
                    singleExecTime = (System.currentTimeMillis() - singleExecTime);
                    if (singleExecTime > max) {
                        max = singleExecTime;
                    }
                    if (singleExecTime < min) {
                        min = singleExecTime;
                    }
                }
            }
            session.end(new MockCommitter());
        }
        LOG.info("Time (mean): " + (System.currentTimeMillis() - saveTime) / 200f + " ms.");
        LOG.info("Time (min): " + min);
        LOG.info("Time (max): " + max);
    }

    public void testConcurrentUpdates() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        SaverSource source = new TestSaverSource(repository, true, "test1_original.xml", "metadata1.xsd");
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        Server server = ServerContext.INSTANCE.get();
        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);
        // create & register storage
        storageAdmin.create("MDM", "MDM", StorageType.MASTER, "H2-Default"); //$NON-NLS-1$//$NON-NLS-2$

        Set<UpdateRunnable> updateRunnables = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            updateRunnables.add(new UpdateRunnable(source));
        }
        Set<Thread> updateThreads = new HashSet<>();
        for (Runnable updateRunnable : updateRunnables) {
            updateThreads.add(new Thread(updateRunnable));
        }
        for (Thread updateThread : updateThreads) {
            updateThread.start();
        }
        for (Thread updateThread : updateThreads) {
            updateThread.join();
        }

        for (UpdateRunnable updateRunnable : updateRunnables) {
            assertTrue(updateRunnable.isSuccess());
        }
    }

    public void test18() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, false, "", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test18.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
    }

    public void test19() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contract", repository);

        SaverSource source = new TestSaverSource(repository, true, "test19_original.xml", "metadata3.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test19.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contract", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("my code", evaluate(committedElement, "/Contract/detail/code"));
    }

    public void testAddComplexElementToSequence() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata4.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Artikel", repository);

        SaverSource source = new TestSaverSource(repository, true, "test20_original.xml", "metadata4.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test20.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Artikel", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);
        Element committedElement = committer.getCommittedElement();
        assertEquals("444", evaluate(committedElement, "/testImport/KeyMapping/Keys[4]/Key"));
        assertEquals("444", evaluate(committedElement, "/testImport/KeyMapping/Keys[4]/System"));
        assertEquals("333", evaluate(committedElement, "/testImport/KeyMapping/Keys[3]/Key"));
        assertEquals("333", evaluate(committedElement, "/testImport/KeyMapping/Keys[3]/System"));
        assertEquals("222", evaluate(committedElement, "/testImport/KeyMapping/Keys[2]/Key"));
        assertEquals("222", evaluate(committedElement, "/testImport/KeyMapping/Keys[2]/System"));
        assertEquals("111", evaluate(committedElement, "/testImport/KeyMapping/Keys[1]/Key"));
        assertEquals("111", evaluate(committedElement, "/testImport/KeyMapping/Keys[1]/System"));
    }

    public void testRemoveComplexElementFromSequence() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata4.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Artikel", repository);

        SaverSource source = new TestSaverSource(repository, true, "test21_original.xml", "metadata4.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test21.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Artikel", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);
        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("", evaluate(committedElement, "/testImport/KeyMapping/Keys[3]/Key"));
        assertEquals("", evaluate(committedElement, "/testImport/KeyMapping/Keys[3]/System"));
        assertEquals("222", evaluate(committedElement, "/testImport/KeyMapping/Keys[2]/Key"));
        assertEquals("222", evaluate(committedElement, "/testImport/KeyMapping/Keys[2]/System"));
        assertEquals("111", evaluate(committedElement, "/testImport/KeyMapping/Keys[1]/Key"));
        assertEquals("111", evaluate(committedElement, "/testImport/KeyMapping/Keys[1]/System"));
    }

    public void test25() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata3.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contract", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test25_original.xml", "metadata3.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test25.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contract", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
    }

    public void test27() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata6.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("OM5", repository);

        SaverSource source = new TestSaverSource(repository, true, "test27_original.xml", "metadata6.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test27.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "OM5", "BusinessFunction", recordXml, false,
                true, false, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
    }

    public void test28() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test28", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test28_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test28.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "Test28", "admin", recordXml, true,
                false, "/Organisation/Contacts/Contact", // Loop (Pivot)
                "SpecialisationContactType/NatureLocalisationFk", // Key
                -1, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals(
                "40-142",
                evaluate(committedElement,
                        "/Organisation/Contacts/Contact[1]/SpecialisationContactType/AdressePostale/CodePostal"));
        assertEquals("Test",
                evaluate(committedElement, "/Organisation/Contacts/Contact[1]/SpecialisationContactType/AdressePostale/Ville"));
        assertEquals("Test",
                evaluate(committedElement, "/Organisation/Contacts/Contact[1]/SpecialisationContactType/AdressePostale/Region"));
        assertEquals(
                "40-143",
                evaluate(committedElement,
                        "/Organisation/Contacts/Contact[2]/SpecialisationContactType/AdressePostale/CodePostal"));
        assertEquals("Katowice1",
                evaluate(committedElement, "/Organisation/Contacts/Contact[2]/SpecialisationContactType/AdressePostale/Ville"));
        assertEquals("Slaskie1",
                evaluate(committedElement, "/Organisation/Contacts/Contact[2]/SpecialisationContactType/AdressePostale/Region"));
    }

    public void test29() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        // there's a small change in this model that differs from QA: ThirdEntity is a xsd:sequence (not a xsd:all)
        // so order of elements will be tested during XSD validation.
        repository.load(DocumentSaveTest.class.getResourceAsStream("CoreTestsModel.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test29_original.xml", "CoreTestsModel.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test29.xml");
        DocumentSaverContext context = session.getContextFactory().create("Product", "Test", "Source", recordXml, false, true,
                false, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Test", evaluate(committedElement, "/ThirdEntity/optionalDetails/mandatory1"));
        assertEquals("123", evaluate(committedElement, "/ThirdEntity/mandatoryDetails/mandatoryUbounded4"));
    }

    public void test30() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test28", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test30_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test30.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "Test28", "admin", recordXml, true,
                false, "/Organisation/Contacts/Contact", "SpecialisationContactType/NatureLocalisationFk", -1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals(
                "[6]",
                evaluate(committer.getCommittedElement(),
                        "/Organisation/Contacts/Contact[1]/SpecialisationContactType/NatureLocalisationFk"));
        assertEquals(
                "[7]",
                evaluate(committer.getCommittedElement(),
                        "/Organisation/Contacts/Contact[2]/SpecialisationContactType/NatureLocalisationFk"));
    }

    public void test31() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test31", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test31_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test31.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "Test31", "admin", recordXml, true,
                true, "/Societe/Contacts/Contact/", "/StatutContactFk", -1, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals(
                "[5]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[1]/SpecialisationContactType/NatureTelephoneFk"));
        assertEquals("", evaluate(committer.getCommittedElement(), "/Societe/Contacts/Contact[2]"));
    }

    public void test32() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test32", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test32_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test32.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "Test32", "admin", recordXml, true,
                false, "/Societe/Contacts/Contact", "SpecialisationContactType/NatureLocalisationFk", -1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals(
                "[1]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[1]/SpecialisationContactType/NatureTelephoneFk"));
        assertEquals(
                "[1]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[2]/SpecialisationContactType/NatureTelephoneFk"));
        assertEquals(
                "[3]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[3]/SpecialisationContactType/NatureLocalisationFk"));
    }

    public void test33() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test33", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test33_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test32.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "Test33", "admin", recordXml, true,
                false, "/Societe/Contacts/Contact", "SpecialisationContactType/NatureLocalisationFk", -1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals(
                "[1]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[1]/SpecialisationContactType/NatureTelephoneFk"));
        assertEquals(
                "[1]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[2]/SpecialisationContactType/NatureTelephoneFk"));
        assertEquals(
                "[3]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[3]/SpecialisationContactType/NatureLocalisationFk"));
        assertEquals(
                "[3]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[4]/SpecialisationContactType/NatureLocalisationFk"));
    }

    public void test34() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test34", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test34_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test34.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "Test34", "admin", recordXml, true,
                true, "/Organisation/Contacts/Contact", "SpecialisationContactType/NatureLocalisationFk", -1, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals(
                "[5]",
                evaluate(committer.getCommittedElement(),
                        "/Organisation/Contacts/Contact[1]/SpecialisationContactType/NatureLocalisationFk"));
        assertEquals(
                "Apartado 111 - Abrunheira",
                evaluate(committer.getCommittedElement(),
                        "/Organisation/Contacts/Contact[1]/SpecialisationContactType/AdressePostale/Adresse1"));
    }

    public void test36() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test36", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test36_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test36.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "Test36", "admin", recordXml, true,
                false, "/Societe/Contacts/Contact", "SpecialisationContactType/NatureLocalisationFk", -1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals(
                "[1]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[1]/SpecialisationContactType/NatureTelephoneFk"));
        assertEquals(
                "[3]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[2]/SpecialisationContactType/NatureLocalisationFk"));
        assertEquals(
                "[4]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[3]/SpecialisationContactType/NatureLocalisationFk"));
    }

    public void test37() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test37", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test37_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test37.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "Test37", "admin", recordXml, true,
                true, "/Societe/Contacts/Contact", "SpecialisationContactType/NatureLocalisationFk", -1, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        // overwrite = true
        assertEquals(
                "[3]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[1]/SpecialisationContactType/NatureLocalisationFk"));
        assertEquals(
                "92501",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[1]/SpecialisationContactType/AdressePostale/CodePostal"));
        assertEquals(
                "[4]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[2]/SpecialisationContactType/NatureLocalisationFk"));

        // overwrite = false
        source = new TestSaverSource(repository, true, "test37_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test37.xml");
        context = session.getContextFactory().createPartialUpdate("MDM", "Test37", "admin", recordXml, true, false,
                "/Societe/Contacts/Contact", "SpecialisationContactType/NatureLocalisationFk", -1, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals(
                "[3]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[1]/SpecialisationContactType/NatureLocalisationFk"));
        assertEquals(
                "92500",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[1]/SpecialisationContactType/AdressePostale/CodePostal"));
        assertEquals(
                "[3]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[2]/SpecialisationContactType/NatureLocalisationFk"));
        assertEquals(
                "92501",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[2]/SpecialisationContactType/AdressePostale/CodePostal"));
        assertEquals(
                "[4]",
                evaluate(committer.getCommittedElement(),
                        "/Societe/Contacts/Contact[3]/SpecialisationContactType/NatureLocalisationFk"));
    }

    public void test38() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test38", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test38_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test38.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "Test38", "admin", recordXml, true,
                false, "/Societe/Contacts/Contact", "/SpecialisationContactType/NatureTelephoneFk", -1, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals("", evaluate(committer.getCommittedElement(), "/Societe/Contacts/Contact[8]"));
        assertEquals("+33 0 00 00 00 00",
                evaluate(committer.getCommittedElement(), "/Societe/Contacts/Contact[7]/SpecialisationContactType/Numero"));
        assertEquals("+33 1 47 16 02 03",
                evaluate(committer.getCommittedElement(), "/Societe/Contacts/Contact[5]/SpecialisationContactType/Numero"));
    }

    public void test39() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("CoreTestsModel.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("test39", repository);

        TestSaverSource source = new TestSaverSource(repository, false, "", "CoreTestsModel.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test39.xml");
        DocumentSaverContext context = session.getContextFactory().create("Product", "test39", "Source", recordXml, true, true,
                true, true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertNotNull(evaluate(committedElement, "/ComplexTypes/son1"));
        assertNotNull(evaluate(committedElement, "/ComplexTypes/father1"));
    }

    public void test40() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("CoreTestsModel.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("test40", repository);

        TestSaverSource source = new TestSaverSource(repository, false, "test40_original.xml", "CoreTestsModel.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test40.xml");
        DocumentSaverContext context = session.getContextFactory().create("Product", "test40", "Source", recordXml, true, true,
                false, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/ComplexTypes/son/Name"));
    }

    public void test42() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("test42", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test42_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test42.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("Product", "test42", "Source", recordXml,
                true, false, "Personne/Contextes/Contexte", "", -1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("[0000]", evaluate(committedElement, "/Personne/Contextes/Contexte[2]/OrganisationFk"));
    }

    public void test43() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("test43", repository);

        TestSaverSource source = new TestSaverSource(repository, false, "", "metadata1.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test43.xml");
        DocumentSaverContext context = session.getContextFactory().create("Product", "test43", "Source", recordXml, true, true,
                false, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("", evaluate(committedElement, "/Product/Features/Colors/Color"));
    }

    public void test44() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata8.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("test44", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test44_original.xml", "metadata8.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test44.xml");
        DocumentSaverContext context = session.getContextFactory().create("Product", "test44", "Source", recordXml, false, true,
                false, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("bob", evaluate(committedElement, "/Create_Supplier/Supplier_Name"));
        assertEquals("123456789", evaluate(committedElement, "/Create_Supplier/Company_RegNbr"));
    }

    public void test45() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata8.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("test45", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test45_original.xml", "metadata8.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test45.xml");
        DocumentSaverContext context = session.getContextFactory().create("Product", "test45", "Source", recordXml, false, true,
                false, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Purchasing", evaluate(committedElement, "/Create_Supplier/Contact_Details/contact_role"));
        assertEquals("Test", evaluate(committedElement, "/Create_Supplier/Supplier_Address/postal_code"));
    }

    public void testPolymorphismForeignKey() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata10.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Product", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test46_original.xml", "metadata10.xsd");
        source.setUserName("Demo_Manager");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test46.xml");
        DocumentSaverContext context = session.getContextFactory().create("TestFK", "Product", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Company", evaluate(committedElement, "/Product/supplier/@tmdm:type"));
        assertEquals("[company]", evaluate(committedElement, "/Product/supplier"));
    }

    public void test46() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata11.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("test47", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test47_original.xml", "metadata11.xsd");
        source.setUserName("administrator");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test47.xml");
        DocumentSaverContext context = session.getContextFactory().create("Eda", "test47", "Source", recordXml, false, true,
                false, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
    }

    public void test47() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata12.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("test48", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test48_original.xml", "metadata12.xsd");
        source.setUserName("administrator");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test48.xml");
        DocumentSaverContext context = session.getContextFactory().create("Product", "test48", "Source", recordXml, false, true,
                false, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
    }

    public void testPolymorphismForeignKeys() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata10.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Product", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test49_original.xml", "metadata10.xsd");
        source.setUserName("Demo_Manager");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test49.xml");
        DocumentSaverContext context = session.getContextFactory().create("TestFK", "Product", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Company", evaluate(committedElement, "/Product/supplier/@tmdm:type"));
        assertEquals("[companyEntity]", evaluate(committedElement, "/Product/supplier"));
    }

    public void testPolymorphismCache() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata10.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Individual", repository);

        TestSaverSource source = new TestSaverSource(repository, false, "", "metadata10.xsd");
        source.setUserName("Demo_User");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test51.xml");
        DocumentSaverContext context = session.getContextFactory().create("TestFK", "Individual", "Source", recordXml, true,
                true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
    }

    public void testPartialUpdateWithEmptyString() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test50", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test50_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test50.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "Test50", "Source", recordXml,
                true, false, "Personne/Contextes/Contexte", "IdContexte", -1, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals("", evaluate(committer.getCommittedElement(), "/Personne/Contextes/Contexte[2]/DateFinContexte"));
        assertEquals("[1]", evaluate(committer.getCommittedElement(), "/Personne/Contextes/Contexte[2]/OrganisationFk"));
    }

    public void testSaveWithPolymorphismNode() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("Personne.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Vinci", repository);

        TestSaverSource source = new TestSaverSource(repository, false, "", "Personne.xsd");
        source.setUserName("Demo_Manager");

        // 1. /Personne/Contextes/Contexte all of nodes are empty(including Polymorphism node:
        // SpecialisationContactType)
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("Personne.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Vinci", "Source", recordXml, true, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertNull(Util.getFirstTextNode(committedElement,
                "/Personne/Contextes/Contexte/Contacts/Contact/SpecialisationContactType"));
        assertNotNull(evaluate(committedElement, "/Personne/IdMDM"));
        assertEquals("[2]", evaluate(committedElement, "/Personne/TypePersonneFk"));
        assertEquals("3", evaluate(committedElement, "/Personne/NomUsuel"));
        assertEquals("3", evaluate(committedElement, "/Personne/PrenomUsuel"));

        // 2. /Personne/Contextes/Contexte all of nodes are empty(excluding Polymorphism node:
        // SpecialisationContactType=SpecialisationContactEmail)
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("Personne3.xml");
        context = session.getContextFactory().create("MDM", "Vinci", "Source", recordXml, true, true, true, true, false);
        saver = context.createSaver();
        try {
            saver.save(session, context);
            fail("mandatory nodes must be filled");
        } catch (Exception e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof ValidateException);
        }

        // 3. /Personne/Contextes/Contexte all of mandatory nodes are filled(including Polymorphism node:
        // SpecialisationContactType=SpecialisationContactEmail)
        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("Personne4.xml");
        context = session.getContextFactory().create("MDM", "Vinci", "Source", recordXml, true, true, true, true, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals(
                "SpecialisationContactEmail",
                evaluate(committedElement,
                        "/Personne/Contextes/Contexte[1]/Contacts/Contact[1]/SpecialisationContactType/@xsi:type"));
        assertEquals("[1]", Util.getFirstTextNode(committedElement,
                "/Personne/Contextes/Contexte/Contacts/Contact/SpecialisationContactType/NatureEmailFk"));
        assertEquals("[1]",
                Util.getFirstTextNode(committedElement, "/Personne/Contextes/Contexte/Contacts/Contact/StatutContactFk"));
        assertEquals("[1]", Util.getFirstTextNode(committedElement, "/Personne/Contextes/Contexte/StatutContexteFk"));
        assertNotNull(evaluate(committedElement, "/Personne/Contextes/Contexte/IdContexte"));
        assertNotNull(evaluate(committedElement, "/Personne/IdMDM"));
        assertEquals("[2]", evaluate(committedElement, "/Personne/TypePersonneFk"));
        assertEquals("3", evaluate(committedElement, "/Personne/NomUsuel"));
        assertEquals("3", evaluate(committedElement, "/Personne/PrenomUsuel"));
    }

    public void test50() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test50", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test50_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test50.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Test50", "Source", recordXml, false, false,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
    }

    public void test53() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata12.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test53", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test53_original.xml", "metadata12.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test53.xml");
        DocumentSaverContext context = session.getContextFactory().create("Product", "Test53", "Source", recordXml, false, false,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertNotNull(committer.getCommittedElement());
    }

    public void test58() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata14.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test58", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test58_original.xml", "metadata14.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test58.xml");
        DocumentSaverContext context = session.getContextFactory().create("Product", "Test58", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertNotNull(committedElement);
        assertEquals("", evaluate(committedElement, "/Territory/countries/country_relation[2]/country_id/@tmdm:type"));
    }

    public void test59() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test59", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test59_original.xml", "metadata7.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test59.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("MDM", "Test59", "admin", recordXml, true,
                false, "/Personne/Contextes/Contexte", // Loop (Pivot)
                "IdContexte", // Key
                -1, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("[7]", evaluate(committedElement, "/Personne/Contextes/Contexte/TypeContexteFk"));
        assertEquals("[8]", evaluate(committedElement, "/Personne/Contextes/Contexte/StatutContexteFk"));
        assertEquals("[9]", evaluate(committedElement, "/Personne/Contextes/Contexte/Contacts/Contact/StatutContactFk"));
        assertEquals(
                "[10]",
                evaluate(committedElement,
                        "/Personne/Contextes/Contexte/Contacts/Contact/SpecialisationContactType/NatureEmailFk"));
    }

    public void test60() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7_vinci.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test60", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test60_original.xml", "metadata7_vinci.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test60.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("Vinci", "Test60",
                UpdateReportPOJO.GENERIC_UI_SOURCE, recordXml, true, false, "/Societe/ListeEtablissements/CodeOSMOSE", // Loop
                                                                                                                       // (Pivot)
                null, // Key
                -1, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("[10702E0031]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[1]"));
        assertEquals("[10702E0032]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[2]"));
        assertEquals("[10702E0033]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[3]"));
        assertEquals("[10702E0034]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[4]"));
        assertEquals("[10702E0035]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[5]"));

    }

    public void test60Ex1() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7_vinci.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test60Ex1", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test60_originalEx1.xml", "metadata7_vinci.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test60Ex1.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("Vinci", "Test60Ex1",
                UpdateReportPOJO.GENERIC_UI_SOURCE, recordXml, true, false, "/Societe/ListeEtablissements/CodeOSMOSE", // Loop
                                                                                                                       // (Pivot)
                null, // Key
                -1, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);
        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();

        assertEquals("[10702E0035]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[1]"));
        assertEquals("[10702E0032]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[2]"));
        assertEquals("[10702E0033]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[3]"));
        assertEquals("[10702E0034]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[4]"));
    }

    public void test60Ex2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata7_vinci.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Test60Ex2", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test60_originalEx2.xml", "metadata7_vinci.xsd");
        source.setUserName("admin");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test60Ex2.xml");
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("Vinci", "Test60Ex2",
                UpdateReportPOJO.GENERIC_UI_SOURCE, recordXml, true, false, "/Societe/ListeEtablissements/CodeOSMOSE", // Loop
                                                                                                                       // (Pivot)
                null, // Key
                -1, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);
        assertTrue(committer.hasSaved());

        Element committedElement = committer.getCommittedElement();
        assertEquals("[10702E0035]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[1]"));
        assertEquals("[10702E0032]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[2]"));
        assertEquals("[10702E0033]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[3]"));
        assertEquals("[10702E0033]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[4]"));
        assertEquals("[10702E0034]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[5]"));
        assertEquals("[10702E0035]", evaluate(committedElement, "/Societe/ListeEtablissements/CodeOSMOSE[6]"));
    }

    public void testRemoveSimpleTypeNodeWithOccurrence() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test52_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test52.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Description", evaluate(committedElement, "/Product/Description"));
        assertEquals("60", evaluate(committedElement, "/Product/Price"));
        assertEquals("Lemon", evaluate(committedElement, "/Product/Features/Colors/Color[1]"));
        assertEquals("", evaluate(committedElement, "/Product/Features/Colors/Color[2]"));
    }

    public void testUpdateShouldNotResetMultiOccurrenceFieldsDefinedInAnnonymousTypes() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test68_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test68.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Description value", evaluate(committedElement, "/Product/Description"));
        assertEquals("60", evaluate(committedElement, "/Product/Price"));
        assertEquals("Small", evaluate(committedElement, "/Product/Features/Sizes/Size[1]"));
        assertEquals("X-Large", evaluate(committedElement, "/Product/Features/Sizes/Size[2]"));
        assertEquals("Lemon", evaluate(committedElement, "/Product/Features/Colors/Color[1]"));
        assertEquals("Light Blue", evaluate(committedElement, "/Product/Features/Colors/Color[2]"));
    }

    public void testDeleteMultiOccurrenceFieldDefinedInReusableTypes() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata20.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("metadata20.xsd", repository);

        SaverSource source = new TestSaverSource(repository, true, "test69_original.xml", "metadata20.xsd");
        ((TestSaverSource) source).setUserName("Demo_Manager");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test69.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "metadata20.xsd", "Source", recordXml, false,
                true, true, true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("", evaluate(committedElement, "/E/a[2]"));
    }

    public void testRemoveComplexTypeNodeWithOccurrence() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata13.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test55_original.xml", "metadata13.xsd");
        ((TestSaverSource) source).setUserName("Demo_Manager");
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test55.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, false,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Doggie t-shirt from American Apparel", evaluate(committedElement, "/Product/Description"));
        assertEquals("16.99", evaluate(committedElement, "/Product/Price"));
        assertEquals("Small", evaluate(committedElement, "/Product/Features/Sizes/Size[1]"));
        assertEquals("1", evaluate(committedElement, "/Product/yu[1]/subelement"));
        assertEquals("2", evaluate(committedElement, "/Product/yu[2]/subelement"));
        assertEquals("", evaluate(committedElement, "/Product/yu[3]/subelement"));
    }

    public void testProductFamilyUpdate() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test54_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test54.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Description", evaluate(committedElement, "/Product/Description"));
        assertEquals("60", evaluate(committedElement, "/Product/Price"));
        assertEquals("[2]", evaluate(committedElement, "/Product/Family"));
    }

    public void testUserPartialUpdate() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("PROVISIONING.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("PROVISIONING", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test56_original.xml", "PROVISIONING.xsd");
        source.setUserName("admin");

        final MockCommitter committer = new MockCommitter();
        SaverSession session = new SaverSession(source) {

            @Override
            protected Committer getDefaultCommitter() {
                return committer;
            }
        };
        InputStream partialUpdateContent = new ByteArrayInputStream(
                "<User><username>user</username><roles><role>System_Interactive</role><role>Demo_User</role></roles></User>"
                .getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate("PROVISIONING", "PROVISIONING", "Source",
                partialUpdateContent, true, false, "/User/roles/role", "", -1, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        session.end(committer);

        assertTrue(committer.hasSaved());
        assertEquals("System_Interactive", evaluate(committer.getCommittedElement(), "/User/roles/role[1]"));
        assertEquals("Demo_User", evaluate(committer.getCommittedElement(), "/User/roles/role[2]"));
    }

    public void test61() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata15.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("test61", repository);

        SaverSource source = new TestSaverSource(repository, false, null, "metadata15.xsd");
        ((TestSaverSource) source).setUserName("Demo_Manager");
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test61_1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "test61", "Source", recordXml, true, true, true,
                false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/Individual/PartyPK"));

        recordXml = DocumentSaveTest.class.getResourceAsStream("test61_2.xml");
        context = session.getContextFactory().create("MDM", "test61", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/Company/PartyPK"));

    }

    public void test62() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata11.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contrat", repository);

        SaverSource source = new TestSaverSource(repository, true, "test62_original.xml", "metadata11.xsd");
        ((TestSaverSource) source).setUserName("administrator");
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test62.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contrat", "Source", recordXml, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("[40]", evaluate(committedElement, "/Contrat/detailContrat/Perimetre/entitesPresentes/EDAs/EDA/eda"));
    }

    public void test65() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata11.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Contrat", repository);

        SaverSource source = new TestSaverSource(repository, false, "", "metadata11.xsd");
        ((TestSaverSource) source).setUserName("administrator");
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test65.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "Contrat", "Source", recordXml, true, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("", evaluate(committedElement, "/Contrat/detailContrat[@xsi:type]"));
    }

    public void test67_update_multiOccurrenceField_SubType() throws Exception {
        // TMDM-7407: when Subtype has field of Annonymous Type
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata19.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("test", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test67_original.xml", "metadata19.xsd");
        source.setUserName("administrator");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test67.xml");
        DocumentSaverContext context = session.getContextFactory().create("test", "test", UpdateReportPOJO.GENERIC_UI_SOURCE,
                recordXml, false, true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
    }

    public void test68() throws Exception {
        // TMDM-7765: Test for null FK during element's type change.
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata21.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("metadata21.xsd", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test70_original.xml", "metadata21.xsd");
        source.setUserName("administrator");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test70.xml");
        DocumentSaverContext context = session.getContextFactory().create("metadata21.xsd", "metadata21.xsd",
                UpdateReportPOJO.GENERIC_UI_SOURCE, recordXml, false, true, false, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Format_Date", evaluate(committedElement, "/EntiteA/format/@xsi:type"));
    }

    public void test69() throws Exception {
        // TMDM-7460: test for FK update during element's type change
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata21.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("metadata21.xsd", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test71_original.xml", "metadata21.xsd");
        source.setUserName("administrator");
        SaverSession session = SaverSession.newSession(source);

        InputStream recordXml2 = DocumentSaveTest.class.getResourceAsStream("test71.xml");
        DocumentSaverContext context2 = session.getContextFactory().create("metadata21.xsd", "metadata21.xsd",
                UpdateReportPOJO.GENERIC_UI_SOURCE, recordXml2, false, false, false, false, false);
        DocumentSaver saver2 = context2.createSaver();
        saver2.save(session, context2);
        MockCommitter committer2 = new MockCommitter();
        session.end(committer2);

        assertTrue(committer2.hasSaved());
        Element committedElement2 = committer2.getCommittedElement();
        assertEquals("Format_Entier", evaluate(committedElement2, "/EntiteA/format/@xsi:type"));
        assertEquals("[e1]", evaluate(committedElement2, "/EntiteA/format/CodeUniteMesure"));
        String datarecordXml = context2.getDatabaseDocument().exportToString();
        assertEquals(
                datarecordXml,
                "<EntiteA><codeA>a1</codeA><format xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"Format_Entier\"><CodeUniteMesure>[e1]</CodeUniteMesure></format></EntiteA>");
    }

    public void test73_extended_type() throws Exception {
        // TMDM-8780: Extended types issue
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("test73.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("test", repository);

        TestSaverSource source = new TestSaverSource(repository, true, "test73_original.xml", "test73.xsd");
        source.setUserName("administrator");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test73.xml");
        DocumentSaverContext context = session.getContextFactory().create("test", "test", UpdateReportPOJO.GENERIC_UI_SOURCE,
                recordXml, false, true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
    }

    /**
     * TMDM-8674 checks no regression on updating from XML source
     */
    public void testRemoveFieldContentFromXml() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test72_original.xml", "metadata1.xsd");
        SaverSession session = SaverSession.newSession(source);

        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source",
                DocumentSaveTest.class.getResourceAsStream("test72.xml"), false, true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertFalse(committer.hasSaved());
    }

    /**
     * TMDM-8674 checks when a source is provided by storage, removing a field content actually removes it.
     */
    public void testRemoveFieldContentFromStorage() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test72_original.xml", "metadata1.xsd");
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        DataRecord updatedRecord = factory.read(repository, repository.getComplexType("Product"),
                IOUtils.toString(DocumentSaveTest.class.getResourceAsStream("test72.xml")));

        StorageDocument updatedDocument = new StorageDocument("DStar", repository, updatedRecord);

        SaverSession session = SaverSession.newSession(source);

        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", updatedDocument, false, true,
                true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Description", evaluate(committedElement, "/Product/Description"));
        assertEquals("", evaluate(committedElement, "/Product/OnlineStore"));
    }

    /**
     * TMDM-8876: TalendMDMContextConnector doesn't work in workflow (don't need to parse time or save update record if
     * there is no change)
     */
    public void testUpdateReportWithNoChange() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        // Test updateReport
        boolean isOK = true;
        boolean newOutput = true;
        SaverSource source = new NoChangeTestSaverSource(repository, true, "test1_original.xml", isOK, newOutput);
        String xmlString = "<Agency>" + "<Id>5258f292-5670-473b-bc01-8b63434682f3</Id>" + "<Name>Portland</Name>"
                + "<City>Portland</City>" + "<State>ME</State>" + "<Zip>04102</Zip>" + "<Region>EAST</Region>" + "</Agency>";
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false, true, true,
                true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        assertEquals("no change update successfully!", saver.getBeforeSavingMessage());
    }

    /**
     * TMDM-9192: issue with deletion of complex optional field
     */
    public void testDeleteOptionalComplexFieldWithManatoryField() throws Exception {
        String orgString = "<Test1><id>a</id><name>a</name><desc>a</desc><complx><e1>a</e1><e2>a</e2></complx></Test1>";
        String updString = "<Test1><id>a</id><name>a</name><desc/><complx><e1/><e2/></complx></Test1>";

        ServerContext.INSTANCE.get(new MockServerLifecycle());
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("OptionalComplexField.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        ComplexTypeMetadata test1 = repository.getComplexType("Test1");
        Storage storage = new SecuredStorage(new HibernateStorage("DStar"), new TestUserDelegator());
        storage.init(ServerContext.INSTANCE.get().getDefinition("H2-Default", "DStar"));
        storage.prepare(repository, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        DataRecord orgRecord = factory.read(repository, test1, orgString);
        try {
            storage.begin();
            storage.update(orgRecord);
            storage.commit();
        } finally {
            storage.end();
        }

        SaverSource source = new MockStorageSaverSource(repository, "OptionalComplexField.xsd");
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = new ByteArrayInputStream(updString.getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().create("DStar", "DStar", "Source", recordXml, false, true,
                false, false, false);
        DocumentSaver saver = context.createSaver();
        session.begin("DStar");
        saver.save(session, context);

        session.end(new DefaultCommitter());

        UserQueryBuilder qb = from(test1).where(eq(test1.getField("id"), "a"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                assertEquals("a", result.get("name"));
                assertNull(result.get("desc"));
                try {
                    assertNull(result.get("complx[1]"));//modify code to return null instead of throw exception
                } catch (IllegalArgumentException e) {
                    // This exception is excepted
                }
            }
        } finally {
            results.close();
        }
    }

    public void testInvalidCharacterId() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        SaverSource source = new TestSaverSource(repository, true, "test57_original.xml", "metadata1.xsd");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("InvalidCharacterId.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "workflow", recordXml, false, true,
                true, true, false);
        DocumentSaver saver = context.createSaver();
        try {
            saver.save(session, context);
            fail();
        } catch (SaveException e) {
        	assertFalse(e.getCause().getLocalizedMessage().isEmpty());
        }
    }
    
    public void testGetCauseMessage() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        SaverSource source = new TestSaverSource(repository, true, "", "");
        SaverSession session = SaverSession.newSession(source);
        
        RuntimeException runtimeException = new RuntimeException("RuntimeException Cause");
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("SaveException Cause", runtimeException);
        Exception exception = new Exception("Exception Cause", illegalArgumentException);
        assertEquals("RuntimeException Cause", session.getCauseMessage(exception));
    }

    // TMDM-8400 Can't save a record with datamodel has composite key,and one is auto increament
    public void testSaveCompositeAutoPK() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("TMDM-8400.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("COMP_AUTO_PK", repository);
        SaverSource source = new TestSaverSource(repository, false, "", "TMDM-8400.xsd");
        SaverSession session = SaverSession.newSession(source);
        String xmlString = "<Compte><Level>Compte SF</Level><Code></Code><Label></Label></Compte>";
        InputStream recordXml = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().create("MDM", "COMP_AUTO_PK", "Source", recordXml, true, true,
                true, true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("Compte SF", evaluate(committedElement, "/Compte/Level"));
    }

    // TMDM-10616 Can't delete Inheritance entity records
    public void testDeleteInheritanceReocrds() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("InheritanceDataModel.xsd"));

        Storage storage = new SecuredStorage(new HibernateStorage("Test"), new TestUserDelegator());
        storage.init(ServerContext.INSTANCE.get().getDefinition("H2-Default", "Test"));
        storage.prepare(repository, true);

        ComplexTypeMetadata objectType = repository.getComplexType("Person");
        List<DataRecord> records = new ArrayList<>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        records.add(factory.read(repository, repository.getComplexType("Person"), "<Person><id>1</id><name>Jack</name></Person>"));
        records.add(factory.read(repository, repository.getComplexType("Employee"), "<Employee><id>2</id><name>Employee</name><role>Employee</role></Employee>"));
        records.add(factory.read(repository, repository.getComplexType("Manager"), "<Manager><id>3</id><name>Manager</name><title>Manager</title></Manager>"));
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }

        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
            for (DataRecord result : results) {
                if ("1".equals(result.get("id"))) {
                    assertEquals("Jack", result.get("name"));
                } else if ("2".equals(result.get("id"))) {
                    assertEquals("Employee", result.get("name"));
                } else if ("3".equals(result.get("id"))) {
                    assertEquals("Manager", result.get("name"));
                }
            }
        } finally {
            results.close();
        }
        storage.commit();

        FieldMetadata field = repository.getComplexType("Person").getField("id");
        storage.begin();
        qb.getSelect().setCondition(UserQueryBuilder.eq(field, "1"));
        storage.delete(qb.getSelect());
        storage.commit();

        qb.getSelect().setCondition(UserQueryBuilder.eq(field, "2"));
        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();

        qb.getSelect().setCondition(UserQueryBuilder.eq(field, "3"));
        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();

        storage.begin();
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        assertEquals(0, results.getCount());
        storage.commit();
    }

    public void testCreateWithOwnPK() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("MyProduct.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("MyProduct", repository);
        SaverSource source = new TestSaverSource(repository, false, "MyProduct_original.xml", "MyProduct.xsd");

        //Case 1 PeopleGroupSize create record with submitted id id1, create success
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = new ByteArrayInputStream(("<PeopleGroupSize><Id>id1</Id><Name>Product Name1</Name><Description>desc1</Description></PeopleGroupSize>").getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().create("MyProduct", "MyProduct", "Source", recordXml, true, true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("id1", evaluate(committedElement, "/PeopleGroupSize/Id"));
        assertEquals("Product Name1", evaluate(committedElement, "/PeopleGroupSize/Name"));
        assertEquals("desc1", evaluate(committedElement, "/PeopleGroupSize/Description"));

        //Case 2 PeopleGroupSize create record without id, create fail
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<PeopleGroupSize><Id></Id><Name>Product Name1</Name><Description>desc1</Description></PeopleGroupSize>").getBytes("UTF-8"));
        context = session.getContextFactory().create("MyProduct", "MyProduct", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        try {
            saver.save(session, context);
            fail("Expected Id to be set");
        } catch (Exception e) {
        }
        committer = new MockCommitter();
        session.end(committer);

        //Case 3 AutoIDEntity create record with submitted id 1, create success
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<AutoIDEntity><Id>1</Id><Name>name1</Name></AutoIDEntity>").getBytes("UTF-8"));
        context = session.getContextFactory().create("MyProduct", "MyProduct", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/AutoIDEntity/Id"));
        assertEquals("name1", evaluate(committedElement, "/AutoIDEntity/Name"));

        //Case 4 AutoIDEntity create record with submitted empty id, create success
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<AutoIDEntity><Id></Id><Name>name2</Name></AutoIDEntity>").getBytes("UTF-8"));
        context = session.getContextFactory().create("MyProduct", "MyProduct", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertNotNull(evaluate(committedElement, "/AutoIDEntity/Id"));
        assertEquals("name2", evaluate(committedElement, "/AutoIDEntity/Name"));

        //Case 5 AutoIDEntity create record with empty id element for request body, create success
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<AutoIDEntity><Name>name3</Name></AutoIDEntity>").getBytes("UTF-8"));
        context = session.getContextFactory().create("MyProduct", "MyProduct", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertNotNull(evaluate(committedElement, "/AutoIDEntity/Id"));
        assertEquals("name3", evaluate(committedElement, "/AutoIDEntity/Name"));

        //Case 6 AutoIDEntity create record with existing id 1 for request body, create success
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<AutoIDEntity><Id>1</Id><Name>name4</Name></AutoIDEntity>").getBytes("UTF-8"));
        context = session.getContextFactory().create("MyProduct", "MyProduct", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertNotNull(evaluate(committedElement, "/AutoIDEntity/Id"));
        assertEquals("name4", evaluate(committedElement, "/AutoIDEntity/Name"));

        //Case 7 UUIDEntity create record with submitted id 1 for request body, create success
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<UUIDEntity><Id>1</Id><Name>name1</Name></UUIDEntity>").getBytes("UTF-8"));
        context = session.getContextFactory().create("MyProduct", "MyProduct", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/UUIDEntity/Id"));
        assertEquals("name1", evaluate(committedElement, "/UUIDEntity/Name"));

        //Case 8 UUIDEntity create record with with submitted empty id, create success
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<UUIDEntity><Id></Id><Name>name2</Name></UUIDEntity>").getBytes("UTF-8"));
        context = session.getContextFactory().create("MyProduct", "MyProduct", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertNotNull(evaluate(committedElement, "/AutoIDEntity/Id"));
        assertEquals("name2", evaluate(committedElement, "/UUIDEntity/Name"));

        //Case 9 UUIDEntity create record with empty id element for request body, create success
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<UUIDEntity><Name>name3</Name></UUIDEntity>").getBytes("UTF-8"));
        context = session.getContextFactory().create("MyProduct", "MyProduct", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertNotNull(evaluate(committedElement, "/AutoIDEntity/Id"));
        assertEquals("name3", evaluate(committedElement, "/UUIDEntity/Name"));

        //Case 10 UUIDEntity create record with existing id 1 for request body, create success
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<UUIDEntity><Id>1</Id><Name>name4</Name></UUIDEntity>").getBytes("UTF-8"));
        context = session.getContextFactory().create("MyProduct", "MyProduct", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/UUIDEntity/Id"));
        assertEquals("name4", evaluate(committedElement, "/UUIDEntity/Name"));
    }

    public void test74_UpdateForeignKeyForMany() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("test74.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("modelisationRefad", repository);
        SaverSource source = new TestSaverSource(repository, true, "test74_1_original.xml", "test74.xsd");

        //Case 1 add fkService in organisation entity, saved success.
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test74_1.xml");
        DocumentSaverContext context = session.getContextFactory()
                .create("modelisationRefad", "modelisationRefad", "Source", recordXml, false, true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("2", evaluate(committedElement, "/organisation/organisationId"));
        assertEquals("[2]", evaluate(committedElement, "/organisation/fkServices[1]"));
    }

    public void test74_UpdateForeignKeyForOne() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("test75.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("modelisationRefad", repository);
        SaverSource source = new TestSaverSource(repository, true, "test75_1_original.xml", "test75.xsd");

        //Case 1 add fkService in organisation entity, saved success.
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test75_1.xml");
        DocumentSaverContext context = session.getContextFactory()
                .create("modelisationRefad", "modelisationRefad", "Source", recordXml, false, true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("2", evaluate(committedElement, "/organisation/organisationId"));
        assertEquals("[2]", evaluate(committedElement, "/organisation/fkServices[1]"));
    }

    public void test13587_SaveForeignKeyForReusableType() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata23.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("testab", repository);
        SaverSource source = new TestSaverSource(repository, false, "", "metadata23.xsd");

        //Case 1 add record for entity TestA as fk using in below entity TestB and TestC.
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = new ByteArrayInputStream(("<TestA><Id>11</Id></TestA>").getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().create("testab", "testab", "Source", recordXml, true, true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);

        //case 2, add record for entity TestB with reusable type BaseType
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<TestB><Id>1</Id><DocterField><BaseField xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"BaseType\"><TestA_FK>[11]</TestA_FK></BaseField><Basename>b1</Basename></DocterField></TestB>").getBytes("UTF-8"));
        context = session.getContextFactory().create("testab", "testab", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/TestB/Id"));
        assertEquals("[11]", evaluate(committedElement, "/TestB/DocterField/BaseField/TestA_FK"));

        //case 3, add record for entity TestB with reusable type FirstType
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<TestB><Id>2</Id><DocterField><BaseField xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"FirstType\"><TestA_FK>[11]</TestA_FK></BaseField><Basename>b2</Basename></DocterField></TestB>").getBytes("UTF-8"));
        context = session.getContextFactory().create("testab", "testab", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("2", evaluate(committedElement, "/TestB/Id"));
        assertEquals("[11]", evaluate(committedElement, "/TestB/DocterField/BaseField/TestA_FK"));

        //case 4, add record for entity TestB with reusable type SecondType
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<TestB><Id>3</Id><DocterField><BaseField xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"SecondType\"><TestA_FK>[11]</TestA_FK></BaseField><Basename>b3</Basename></DocterField></TestB>").getBytes("UTF-8"));
        context = session.getContextFactory().create("testab", "testab", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("3", evaluate(committedElement, "/TestB/Id"));
        assertEquals("[11]", evaluate(committedElement, "/TestB/DocterField/BaseField/TestA_FK"));

        //case 5, add record for entity TestC with reusable type BaseType
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<TestC><Id>1</Id><DocterField><BaseField xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"BaseType\"><TestA_FK>[11]</TestA_FK></BaseField><Basename>b1</Basename></DocterField></TestC>").getBytes("UTF-8"));
        context = session.getContextFactory().create("testab", "testab", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("1", evaluate(committedElement, "/TestC/Id"));
        assertEquals("[11]", evaluate(committedElement, "/TestC/DocterField/BaseField/TestA_FK"));

        //case 6, add record for entity TestC with reusable type FirstType
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<TestC><Id>2</Id><DocterField><BaseField xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"FirstType\"><TestA_FK>[11]</TestA_FK></BaseField><Basename>b2</Basename></DocterField></TestC>").getBytes("UTF-8"));
        context = session.getContextFactory().create("testab", "testab", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("2", evaluate(committedElement, "/TestC/Id"));
        assertEquals("[11]", evaluate(committedElement, "/TestC/DocterField/BaseField/TestA_FK"));

        //case 7, add record for entity TestC with reusable type SecondType
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<TestC><Id>3</Id><DocterField><BaseField xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"SecondType\"><TestA_FK>[11]</TestA_FK></BaseField><Basename>b3</Basename></DocterField></TestC>").getBytes("UTF-8"));
        context = session.getContextFactory().create("testab", "testab", "Source", recordXml, true, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);

        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("3", evaluate(committedElement, "/TestC/Id"));
        assertEquals("[11]", evaluate(committedElement, "/TestC/DocterField/BaseField/TestA_FK"));
    }
    
    public void test_UpdateSpecialFK() throws Exception {
    	MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("testSpecialFK.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("testSpecialFK", repository);
        SaverSource source = new TestSaverSource(repository, true, "testSpecialFK_original.xml", "testSpecialFK.xsd");

        // Case 1, update foreign key refer to itself
        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = new ByteArrayInputStream(("<OrgActivity><idOrgActivity>id1</idOrgActivity><idOrgActivityMere>[id1]</idOrgActivityMere><FK1>[1]</FK1><FK2>[2]</FK2></OrgActivity>").getBytes("UTF-8"));
        DocumentSaverContext context = session.getContextFactory().create("testSpecialFK", "testSpecialFK", "Source", recordXml, false, true, true, false, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        MockCommitter committer = new MockCommitter();
        session.end(committer);
        
        assertTrue(committer.hasSaved());
        Element committedElement = committer.getCommittedElement();
        assertEquals("[id1]", evaluate(committedElement, "/OrgActivity/idOrgActivityMere"));
        
        // Case 2, update foreign key FK2 that refer to the same type with FK1.
        session = SaverSession.newSession(source);
        recordXml = new ByteArrayInputStream(("<OrgActivity><idOrgActivity>id1</idOrgActivity><idOrgActivityMere>[id1]</idOrgActivityMere><FK1>[1]</FK1><FK2>[1]</FK2></OrgActivity>").getBytes("UTF-8"));
        context = session.getContextFactory().create("testSpecialFK", "testSpecialFK", "Source", recordXml, false, true, true, false, false);
        saver = context.createSaver();
        saver.save(session, context);
        committer = new MockCommitter();
        session.end(committer);
        
        assertTrue(committer.hasSaved());
        committedElement = committer.getCommittedElement();
        assertEquals("[1]", evaluate(committedElement, "/OrgActivity/FK2"));
    }

    private void assertUpdateReportAction(Document doc, int index, String expectedPath, String expectedOldValue,
            String expectedNewValue) throws Exception {
        String path = (String) evaluate(doc.getDocumentElement(), "Item[" + index + "]/path");
        String oldValue = (String) evaluate(doc.getDocumentElement(), "Item[" + index + "]/oldValue");
        String newValue = (String) evaluate(doc.getDocumentElement(), "Item[" + index + "]/newValue");
        assertEquals(expectedPath, path);
        assertEquals(expectedOldValue, oldValue);
        assertEquals(expectedNewValue, newValue);
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
            // TODO
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

    private static class MockStorageSaverSource extends StorageSaverSource {

        private MetadataRepository updateReportRepository;

        private final MetadataRepository repository;

        private final String schemaFileName;

        public MockStorageSaverSource(MetadataRepository repository, String schemaFileName) {
            this.repository = repository;
            this.schemaFileName = schemaFileName;
        }

        @Override
        public String getUserName() {
            return "Admin";
        }

        @Override
        public Set<String> getCurrentUserRoles() {
            return Collections.singleton("Demo_Manager");
        }

        @Override
        public InputStream getSchema(String dataModelName) {
            return DocumentSaveTest.class.getResourceAsStream(schemaFileName);
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
        public void routeItem(String dataCluster, String typeName, String[] id) {
            // nothing to do
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

        private final Map<String, String> schemasAsString = new HashMap<>();

        private final Map<String, Integer> AUTO_INCREMENT_ID_MAP = new HashMap<>();

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

        public boolean hasSavedAutoIncrement() {
            return hasSavedAutoIncrement;
        }

        public String getLastInvalidatedTypeCache() {
            return lastInvalidatedTypeCache;
        }

        public Map<String, String> getSchemasAsString() {
            return schemasAsString;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

    }

    private static class AlterRecordTestSaverSource extends DocumentSaveTest.TestSaverSource {

        private final boolean isOK;

        private final boolean newOutput;

        public AlterRecordTestSaverSource(MetadataRepository repository, boolean exist, String fileName, boolean isOK,
                boolean newOutput) {
            super(repository, exist, fileName, "metadata1.xsd");
            this.isOK = isOK;
            this.newOutput = newOutput;
        }

        public AlterRecordTestSaverSource(MetadataRepository repository, boolean exist, String fileName, boolean isOK,
                boolean newOutput, String userName) {
            super(repository, exist, fileName, "metadata1.xsd", userName);
            this.isOK = isOK;
            this.newOutput = newOutput;
        }

        @Override
        public OutputReport invokeBeforeSaving(DocumentSaverContext context, MutableDocument updateReportDocument) {
            String message = "<report><message type=\"info\">change the value successfully!</message></report>";
            if (!isOK) {
                message = "<report><message type=\"error\">change the value failed!</message></report>";
            }
            String item = null;
            OutputReport report = new OutputReport(message, item, false);

            if (newOutput) {
                item = "<exchange><item>"
                        + "<Agency><Id>5258f292-5670-473b-bc01-8b63434682f3</Id><Name>beforeSaving_Agency</Name>"
                        + "<City>Chicago</City><State/><Zip>04102</Zip><Region>EAST</Region><Information>"
                        + "<MoreInfo>http://www.newSite.org</MoreInfo><MoreInfo>http://www.newSite2.org</MoreInfo>"
                        + "</Information></Agency></item></exchange>";
                report.setItem(item);
            }
            return report;
        }
    }

    private static class TestSaverSourceWithOutputReportItem extends DocumentSaveTest.TestSaverSource {

        private final boolean OK;

        private final boolean newOutput;

        public TestSaverSourceWithOutputReportItem(MetadataRepository repository, boolean exist, String fileName, boolean OK,
                boolean newOutput) {
            super(repository, exist, fileName, "metadata1.xsd");
            this.OK = OK;
            this.newOutput = newOutput;
        }

        @Override
        public OutputReport invokeBeforeSaving(DocumentSaverContext context, MutableDocument updateReportDocument) {
            String message = "<report><message type=\"info\">Save the value successfully!</message></report>";
            if (!OK) {
                message = "<report><message type=\"error\">Save the value failed!</message></report>";
            }
            String item = null;
            OutputReport report = new OutputReport(message, item, false);

            if (newOutput) {
                item = "<exchange><item>"
                        + "<ProductFamily><Id>0</Id><Name>testAutoIncrementPK</Name></ProductFamily></item></exchange>";
                report.setItem(item);
            }
            return report;
        }
    }
    
    private static class TestSaverSourceWithProductOutputReportItem extends DocumentSaveTest.TestSaverSource {

        private final boolean OK;

        private final boolean newOutput;

        public TestSaverSourceWithProductOutputReportItem(MetadataRepository repository, boolean exist, String fileName, boolean OK,
                boolean newOutput) {
            super(repository, exist, fileName, "metadata1.xsd");
            this.OK = OK;
            this.newOutput = newOutput;
        }

        @Override
        public OutputReport invokeBeforeSaving(DocumentSaverContext context, MutableDocument updateReportDocument) {
            String message = "<report><message type=\"info\">Save the value successfully!</message></report>";
            if (!OK) {
                message = "<report><message type=\"error\">Save the value failed!</message></report>";
            }
            String item = null;
            OutputReport report = new OutputReport(message, item, false);

            if (newOutput) {
                item = "<exchange><item>"
                        + "<Product><Id>id2</Id><Name>Product Name1</Name><Description>Product Description1</Description><Price>11</Price></Product></item></exchange>";
                report.setItem(item);
            }
            return report;
        }
    }

    private static class UpdateRunnable implements Runnable {

        private final SaverSource source;

        private boolean success = false;

        public UpdateRunnable(SaverSource source) {
            this.source = source;
        }

        @Override
        public void run() {
            SaverSession session = SaverSession.newSession(source);
            {
                for (int i = 0; i < 100; i++) {
                    InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
                    DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, false,
                            true, true, false, false);
                    DocumentSaver saver = context.createSaver();
                    saver.save(session, context);
                }
            }
            session.end(new MockCommitter());
            success = true;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    private static class TestNamespaceContext implements NamespaceContext {

        private Map<String, String> declaredPrefix = new HashMap<>();

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

    private static class TestUserDelegator implements SecuredStorage.UserDelegator {

        boolean isActive = true;

        @Override
        public boolean hide(FieldMetadata field) {
            return isActive && field.getHideUsers().contains("System_Users");
        }

        @Override
        public boolean hide(ComplexTypeMetadata type) {
            return isActive && type.getHideUsers().contains("System_Users");
        }
    }

    private static class NoChangeTestSaverSource extends DocumentSaveTest.TestSaverSource {

        private final boolean OK;

        private final boolean newOutput;

        public NoChangeTestSaverSource(MetadataRepository repository, boolean exist, String fileName, boolean OK,
                boolean newOutput) {
            super(repository, exist, fileName, "metadata1.xsd");
            this.OK = OK;
            this.newOutput = newOutput;
        }

        @Override
        public OutputReport invokeBeforeSaving(DocumentSaverContext context, MutableDocument updateReportDocument) {
            String message = "<report><message type=\"info\">no change update successfully!</message></report>";
            if (!OK) {
                message = "<report><message type=\"error\">no change update failed!</message></report>";
            }
            String item = null;
            OutputReport report = new OutputReport(message, item, false);

            if (newOutput) {
                item = "<exchange><item>" + "<Agency>" + "<Id>5258f292-5670-473b-bc01-8b63434682f3</Id>"
                        + "<Name>Portland</Name>" + "<City>Portland</City>" + "<State>ME</State>" + "<Zip>04102</Zip>"
                        + "<Region>EAST</Region>" + "</Agency>" + "</item></exchange>";
                report.setItem(item);
            }
            return report;
        }
    }
}
