/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 *  %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.load;

import com.amalto.core.delegator.BaseSecurityCheck;
import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.MockILocalUser;
import com.amalto.core.load.action.LoadAction;
import com.amalto.core.load.action.OptimizedLoadAction;
import com.amalto.core.server.MockMetadataRepositoryAdmin;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.servlet.LoadServlet;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.util.Util;
import com.amalto.core.util.XSDKey;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("nls")
public class LoadServletForAutoIncrementTest {

    private static final Logger LOG = Logger.getLogger(LoadServletForAutoIncrementTest.class);

    private static boolean beanDelegatorContainerFlag = false;

    private static LoadServlet loadServlet;

    private static class MockISecurityCheck extends BaseSecurityCheck {

    }

    private static void createBeanDelegatorContainer() {
        if (!beanDelegatorContainerFlag) {
            BeanDelegatorContainer.createInstance();
            beanDelegatorContainerFlag = true;
        }
    }

    @BeforeClass
    public static void setUp() {
        LOG.info("Setting up MDM server environment...");
        ServerContext.INSTANCE.get(new MockServerLifecycle());
        MDMConfiguration.getConfiguration().setProperty("xmlserver.class", "com.amalto.core.storage.DispatchWrapper");
        LOG.info("MDM server environment set.");

        Map<String, Object> delegatorInstancePool = new HashMap<>();
        delegatorInstancePool.put("LocalUser", new MockILocalUser());
        delegatorInstancePool.put("SecurityCheck", new MockISecurityCheck());
        createBeanDelegatorContainer();
        BeanDelegatorContainer.getInstance().setDelegatorInstancePool(delegatorInstancePool);

        loadServlet = new LoadServlet();
    }

    @Test
    public void test_01_BulkLoadGeneratePK() throws Exception {
        String dataClusterName = "AutoInc";
        String typeName = "Person";
        String dataModelName = "AutoInc";
        boolean needAutoGenPK = true;
        boolean insertOnly = false;

        MetadataRepository repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata01.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("AutoInc", repository);
        ComplexTypeMetadata type = repository.getComplexType(typeName);

        LoadAction loadAction = new OptimizedLoadAction(dataClusterName, typeName, dataModelName, needAutoGenPK);

        DataRecord.CheckExistence.set(!insertOnly);
        InputStream recordXml = new ByteArrayInputStream(("<Person><Name>T-Shirt</Name></Person>").getBytes(
                StandardCharsets.UTF_8));

        Method getTypeKeyMethod = loadServlet.getClass().getDeclaredMethod("getTypeKey", Collection.class);
        getTypeKeyMethod.setAccessible(true);

        XSDKey keyMetadata = (XSDKey) getTypeKeyMethod.invoke(loadServlet, type.getKeyFields());

        XmlServer server = Util.getXmlServerCtrlLocal();

        Method bulkLoadSaveMethod = loadServlet.getClass()
                .getDeclaredMethod("bulkLoadSave", String.class, String.class, InputStream.class, LoadAction.class, XSDKey.class,
                        XSDKey.class);
        bulkLoadSaveMethod.setAccessible(true);

        bulkLoadSaveMethod.invoke(loadServlet, dataClusterName, dataModelName, recordXml, loadAction, keyMetadata, null);
        String result = server.getDocumentAsString(dataClusterName, dataClusterName + "." + typeName + ".1");
        Document xmlDocument = DocumentHelper.parseText(result);
        assertEquals(2, xmlDocument.getRootElement().element("p").element("Person").elements().size());
        assertEquals(1,
                Integer.parseInt(xmlDocument.getRootElement().element("p").element("Person").element("Id").getText()));
        assertEquals("T-Shirt", xmlDocument.getRootElement().element("p").element("Person").element("Name").getText());
    }

    @Test
    public void test_02_BulkLoadNotGeneratePK() throws Exception {
        String dataClusterName = "AutoInc";
        String typeName = "Person";
        String dataModelName = "AutoInc";
        boolean needAutoGenPK = false;
        boolean needAutoGenAutoFields = true;
        boolean insertOnly = false;

        MetadataRepository repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata01.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("AutoInc", repository);
        ComplexTypeMetadata type = repository.getComplexType(typeName);

        LoadAction loadAction = new OptimizedLoadAction(dataClusterName, typeName, dataModelName, needAutoGenPK,
                needAutoGenAutoFields);

        DataRecord.CheckExistence.set(!insertOnly);
        InputStream recordXml = new ByteArrayInputStream(("<Person><Name>T-Shirt</Name></Person>").getBytes(
                StandardCharsets.UTF_8));

        Method getTypeKeyMethod = loadServlet.getClass().getDeclaredMethod("getTypeKey", Collection.class);
        getTypeKeyMethod.setAccessible(true);

        XSDKey keyMetadata = (XSDKey) getTypeKeyMethod.invoke(loadServlet, type.getKeyFields());
        XSDKey autoFieldMetadata = null;
        if (needAutoGenAutoFields) {
            Collection<FieldMetadata> fields = type.getFields();
            Collection<FieldMetadata> autoFields = fields.stream().filter(filed -> (!filed.isKey())).collect(Collectors.toList());
            Method getTypeAutoFieldMethod = loadServlet.getClass().getDeclaredMethod("getTypeAutoField", Collection.class);
            getTypeAutoFieldMethod.setAccessible(true);
            autoFieldMetadata = (XSDKey) getTypeAutoFieldMethod.invoke(loadServlet, autoFields);
        }

        Method bulkLoadSaveMethod = loadServlet.getClass()
                .getDeclaredMethod("bulkLoadSave", String.class, String.class, InputStream.class, LoadAction.class, XSDKey.class,
                        XSDKey.class);
        bulkLoadSaveMethod.setAccessible(true);
        try {
            bulkLoadSaveMethod
                    .invoke(loadServlet, dataClusterName, dataModelName, recordXml, loadAction, keyMetadata, autoFieldMetadata);
            fail("Failed to save the autoincrement field.");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void test_03_BulkLoadGenerateAutoField() throws Exception {
        String dataClusterName = "AutoInc";
        String typeName = "Person";
        String dataModelName = "AutoInc";
        boolean needAutoGenPK = true;
        boolean needAutoGenAutoFields = true;
        boolean insertOnly = false;

        MetadataRepository repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata01.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("AutoInc", repository);
        ComplexTypeMetadata type = repository.getComplexType(typeName);

        LoadAction loadAction = new OptimizedLoadAction(dataClusterName, typeName, dataModelName, needAutoGenPK,
                needAutoGenAutoFields);

        DataRecord.CheckExistence.set(!insertOnly);
        InputStream recordXml = new ByteArrayInputStream(("<Person><Name>T-Shirt</Name></Person>").getBytes(
                StandardCharsets.UTF_8));

        Method getTypeKeyMethod = loadServlet.getClass().getDeclaredMethod("getTypeKey", Collection.class);
        getTypeKeyMethod.setAccessible(true);

        XSDKey keyMetadata = (XSDKey) getTypeKeyMethod.invoke(loadServlet, type.getKeyFields());
        XSDKey autoFieldMetadata = null;
        if (needAutoGenAutoFields) {
            Collection<FieldMetadata> fields = type.getFields();
            Collection<FieldMetadata> autoFields = fields.stream().filter(filed -> (!filed.isKey())).collect(Collectors.toList());
            Method getTypeAutoFieldMethod = loadServlet.getClass().getDeclaredMethod("getTypeAutoField", Collection.class);
            getTypeAutoFieldMethod.setAccessible(true);
            autoFieldMetadata = (XSDKey) getTypeAutoFieldMethod.invoke(loadServlet, autoFields);
        }

        XmlServer server = Util.getXmlServerCtrlLocal();

        Method bulkLoadSaveMethod = loadServlet.getClass()
                .getDeclaredMethod("bulkLoadSave", String.class, String.class, InputStream.class, LoadAction.class, XSDKey.class,
                        XSDKey.class);
        bulkLoadSaveMethod.setAccessible(true);

        bulkLoadSaveMethod
                .invoke(loadServlet, dataClusterName, dataModelName, recordXml, loadAction, keyMetadata, autoFieldMetadata);
        String result = server.getDocumentAsString(dataClusterName, dataClusterName + "." + typeName + ".2");
        Document xmlDocument = DocumentHelper.parseText(result);
        assertEquals(7, xmlDocument.getRootElement().element("p").element("Person").elements().size());
        assertEquals(2,
                Integer.parseInt(xmlDocument.getRootElement().element("p").element("Person").element("Id").getText()));
        assertEquals("T-Shirt", xmlDocument.getRootElement().element("p").element("Person").element("Name").getText());
        assertEquals(1,
                Integer.parseInt(xmlDocument.getRootElement().element("p").element("Person").element("AA").getText()));
        assertEquals(1,
                Integer.parseInt(xmlDocument.getRootElement().element("p").element("Person").element("BB").getText()));
        assertEquals(1,
                Integer.parseInt(xmlDocument.getRootElement().element("p").element("Person").element("CC").getText()));
        assertEquals(36, xmlDocument.getRootElement().element("p").element("Person").element("DD").getText().length());
        assertEquals(36, xmlDocument.getRootElement().element("p").element("Person").element("EE").getText().length());
    }

    @Test
    public void test_04_BulkLoadNotGenerateAutoField() throws Exception {
        String dataClusterName = "AutoInc";
        String typeName = "Person";
        String dataModelName = "AutoInc";
        boolean needAutoGenPK = false;
        boolean needAutoGenAutoFields = false;
        boolean insertOnly = false;

        MetadataRepository repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata01.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("AutoInc", repository);
        ComplexTypeMetadata type = repository.getComplexType(typeName);

        LoadAction loadAction = new OptimizedLoadAction(dataClusterName, typeName, dataModelName, needAutoGenPK,
                needAutoGenAutoFields);

        DataRecord.CheckExistence.set(!insertOnly);
        InputStream recordXml = new ByteArrayInputStream(
                ("<Person><Id>100</Id><Name>T-Shirt</Name><AA>1</AA><BB>1</BB><CC>1</CC></Person>").getBytes(
                        StandardCharsets.UTF_8));

        Method getTypeKeyMethod = loadServlet.getClass().getDeclaredMethod("getTypeKey", Collection.class);
        getTypeKeyMethod.setAccessible(true);

        XSDKey keyMetadata = (XSDKey) getTypeKeyMethod.invoke(loadServlet, type.getKeyFields());

        XmlServer server = Util.getXmlServerCtrlLocal();

        Method bulkLoadSaveMethod = loadServlet.getClass()
                .getDeclaredMethod("bulkLoadSave", String.class, String.class, InputStream.class, LoadAction.class, XSDKey.class,
                        XSDKey.class);
        bulkLoadSaveMethod.setAccessible(true);

        bulkLoadSaveMethod.invoke(loadServlet, dataClusterName, dataModelName, recordXml, loadAction, keyMetadata, null);
        String result = server.getDocumentAsString(dataClusterName, dataClusterName + "." + typeName + ".100");
        Document xmlDocument = DocumentHelper.parseText(result);
        assertEquals(5, xmlDocument.getRootElement().element("p").element("Person").elements().size());
        assertEquals(100, Integer.parseInt(xmlDocument.getRootElement().element("p").element("Person").element("Id").getText()));
        assertEquals("T-Shirt", xmlDocument.getRootElement().element("p").element("Person").element("Name").getText());
        assertEquals(1, Integer.parseInt(xmlDocument.getRootElement().element("p").element("Person").element("AA").getText()));
        assertEquals(1, Integer.parseInt(xmlDocument.getRootElement().element("p").element("Person").element("BB").getText()));
        assertEquals(1, Integer.parseInt(xmlDocument.getRootElement().element("p").element("Person").element("CC").getText()));
    }

    @Test
    public void test_05_BulkLoadNotGenerateAutoField2() throws Exception {
        String dataClusterName = "AutoInc";
        String typeName = "Person";
        String dataModelName = "AutoInc";
        boolean needAutoGenPK = true;
        boolean needAutoGenAutoFields = false;
        boolean insertOnly = false;

        MetadataRepository repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata01.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("AutoInc", repository);
        ComplexTypeMetadata type = repository.getComplexType(typeName);

        LoadAction loadAction = new OptimizedLoadAction(dataClusterName, typeName, dataModelName, needAutoGenPK,
                needAutoGenAutoFields);

        DataRecord.CheckExistence.set(!insertOnly);
        InputStream recordXml = new ByteArrayInputStream(
                ("<Person><Name>T-Shirt</Name><AA>1</AA><BB>1</BB><CC>1</CC></Person>").getBytes(StandardCharsets.UTF_8));

        Method getTypeKeyMethod = loadServlet.getClass().getDeclaredMethod("getTypeKey", Collection.class);
        getTypeKeyMethod.setAccessible(true);

        XSDKey keyMetadata = (XSDKey) getTypeKeyMethod.invoke(loadServlet, type.getKeyFields());

        XmlServer server = Util.getXmlServerCtrlLocal();

        Method bulkLoadSaveMethod = loadServlet.getClass()
                .getDeclaredMethod("bulkLoadSave", String.class, String.class, InputStream.class, LoadAction.class, XSDKey.class,
                        XSDKey.class);
        bulkLoadSaveMethod.setAccessible(true);

        bulkLoadSaveMethod.invoke(loadServlet, dataClusterName, dataModelName, recordXml, loadAction, keyMetadata, null);
        String result = server.getDocumentAsString(dataClusterName, dataClusterName + "." + typeName + ".3");
        Document xmlDocument = DocumentHelper.parseText(result);
        assertEquals(5, xmlDocument.getRootElement().element("p").element("Person").elements().size());
        assertEquals(3,
                Integer.parseInt(xmlDocument.getRootElement().element("p").element("Person").element("Id").getText()));
        assertEquals("T-Shirt", xmlDocument.getRootElement().element("p").element("Person").element("Name").getText());
        assertEquals(1, Integer.parseInt(xmlDocument.getRootElement().element("p").element("Person").element("AA").getText()));
        assertEquals(1, Integer.parseInt(xmlDocument.getRootElement().element("p").element("Person").element("BB").getText()));
        assertEquals(1, Integer.parseInt(xmlDocument.getRootElement().element("p").element("Person").element("CC").getText()));
    }

    @Test
    public void test_06_BulkLoadDefaultLoad() throws Exception {
        String dataClusterName = "Product";
        String typeName = "Product";
        String dataModelName = "Product";
        boolean needAutoGenPK = false;
        boolean needAutoGenAutoFields = false;
        boolean insertOnly = false;

        MetadataRepository repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata02.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register(dataClusterName, repository);
        ComplexTypeMetadata type = repository.getComplexType(typeName);

        LoadAction loadAction = new OptimizedLoadAction(dataClusterName, typeName, dataModelName, needAutoGenPK,
                needAutoGenAutoFields);

        DataRecord.CheckExistence.set(!insertOnly);
        InputStream recordXml = new ByteArrayInputStream(
                ("<Product><Id>1</Id><Name>T-Shirt</Name><Description>Talend T-Shirt</Description><Price>12.3</Price><Support>1</Support></Product>")
                        .getBytes(StandardCharsets.UTF_8));

        Method getTypeKeyMethod = loadServlet.getClass().getDeclaredMethod("getTypeKey", Collection.class);
        getTypeKeyMethod.setAccessible(true);

        XSDKey keyMetadata = (XSDKey) getTypeKeyMethod.invoke(loadServlet, type.getKeyFields());

        XmlServer server = Util.getXmlServerCtrlLocal();

        Method bulkLoadSaveMethod = loadServlet.getClass()
                .getDeclaredMethod("bulkLoadSave", String.class, String.class, InputStream.class, LoadAction.class, XSDKey.class,
                        XSDKey.class);
        bulkLoadSaveMethod.setAccessible(true);

        bulkLoadSaveMethod.invoke(loadServlet, dataClusterName, dataModelName, recordXml, loadAction, keyMetadata, null);
        String result = server.getDocumentAsString(dataClusterName, dataClusterName + "." + typeName + ".1");
        Document xmlDocument = DocumentHelper.parseText(result);
        assertEquals(7, xmlDocument.getRootElement().element("p").element("Product").elements().size());
        assertEquals(1, Integer.parseInt(xmlDocument.getRootElement().element("p").element("Product").element("Id").getText()));
        assertEquals("T-Shirt", xmlDocument.getRootElement().element("p").element("Product").element("Name").getText());
        assertEquals("Talend T-Shirt",
                xmlDocument.getRootElement().element("p").element("Product").element("Description").getText());
        assertEquals("12.30", xmlDocument.getRootElement().element("p").element("Product").element("Price").getText());
        assertEquals("1", xmlDocument.getRootElement().element("p").element("Product").element("Support").getText());
    }

    @Test
    public void test_07_BulkLoadDefaultLoadGenerate() throws Exception {
        String dataClusterName = "Product";
        String typeName = "Product";
        String dataModelName = "Product";
        boolean needAutoGenPK = false;
        boolean needAutoGenAutoFields = true;
        boolean insertOnly = false;

        MetadataRepository repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata02.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register(dataClusterName, repository);
        ComplexTypeMetadata type = repository.getComplexType(typeName);

        LoadAction loadAction = new OptimizedLoadAction(dataClusterName, typeName, dataModelName, needAutoGenPK,
                needAutoGenAutoFields);

        DataRecord.CheckExistence.set(!insertOnly);
        InputStream recordXml = new ByteArrayInputStream(
                ("<Product><Id>2</Id><Name>T-Shirt</Name><Description>Talend T-Shirt</Description><Price>12.3</Price></Product>")
                        .getBytes(StandardCharsets.UTF_8));

        Method getTypeKeyMethod = loadServlet.getClass().getDeclaredMethod("getTypeKey", Collection.class);
        getTypeKeyMethod.setAccessible(true);

        XSDKey keyMetadata = (XSDKey) getTypeKeyMethod.invoke(loadServlet, type.getKeyFields());
        XSDKey autoFieldMetadata = null;
        if (needAutoGenAutoFields) {
            Collection<FieldMetadata> fields = type.getFields();
            Collection<FieldMetadata> autoFields = fields.stream().filter(filed -> (!filed.isKey())).collect(Collectors.toList());
            Method getTypeAutoFieldMethod = loadServlet.getClass().getDeclaredMethod("getTypeAutoField", Collection.class);
            getTypeAutoFieldMethod.setAccessible(true);
            autoFieldMetadata = (XSDKey) getTypeAutoFieldMethod.invoke(loadServlet, autoFields);
        }

        XmlServer server = Util.getXmlServerCtrlLocal();

        Method bulkLoadSaveMethod = loadServlet.getClass()
                .getDeclaredMethod("bulkLoadSave", String.class, String.class, InputStream.class, LoadAction.class, XSDKey.class,
                        XSDKey.class);
        bulkLoadSaveMethod.setAccessible(true);

        bulkLoadSaveMethod
                .invoke(loadServlet, dataClusterName, dataModelName, recordXml, loadAction, keyMetadata, autoFieldMetadata);
        String result = server.getDocumentAsString(dataClusterName, dataClusterName + "." + typeName + ".2");
        Document xmlDocument = DocumentHelper.parseText(result);
        assertEquals(8, xmlDocument.getRootElement().element("p").element("Product").elements().size());
        assertEquals(2, Integer.parseInt(xmlDocument.getRootElement().element("p").element("Product").element("Id").getText()));
        assertEquals("T-Shirt", xmlDocument.getRootElement().element("p").element("Product").element("Name").getText());
        assertEquals("Talend T-Shirt",
                xmlDocument.getRootElement().element("p").element("Product").element("Description").getText());
        assertEquals("12.30", xmlDocument.getRootElement().element("p").element("Product").element("Price").getText());
        assertEquals("1", xmlDocument.getRootElement().element("p").element("Product").element("Support").getText());
        assertEquals(36, xmlDocument.getRootElement().element("p").element("Product").element("Supply").getText().length());
    }

    @Test
    public void test_08_BulkLoadForComplexType() throws Exception {
        String dataClusterName = "Student";
        String typeName = "Student";
        String dataModelName = "Student";
        boolean needAutoGenPK = false;
        boolean needAutoGenAutoFields = false;
        boolean insertOnly = false;

        MetadataRepository repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata03.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register(dataClusterName, repository);
        ComplexTypeMetadata type = repository.getComplexType(typeName);

        LoadAction loadAction = new OptimizedLoadAction(dataClusterName, typeName, dataModelName, needAutoGenPK,
                needAutoGenAutoFields);

        DataRecord.CheckExistence.set(!insertOnly);
        InputStream recordXml = new ByteArrayInputStream(
                ("<Student><Id>1</Id><Name>John</Name><Age>23</Age><Course><Id>English</Id><Teacher>Mike</Teacher></Course></Student>")
                        .getBytes(StandardCharsets.UTF_8));

        Method getTypeKeyMethod = loadServlet.getClass().getDeclaredMethod("getTypeKey", Collection.class);
        getTypeKeyMethod.setAccessible(true);

        XSDKey keyMetadata = (XSDKey) getTypeKeyMethod.invoke(loadServlet, type.getKeyFields());
        XSDKey autoFieldMetadata = null;
        if (needAutoGenAutoFields) {
            Collection<FieldMetadata> fields = type.getFields();
            Collection<FieldMetadata> autoFields = fields.stream().filter(filed -> (!filed.isKey())).collect(Collectors.toList());
            Method getTypeAutoFieldMethod = loadServlet.getClass().getDeclaredMethod("getTypeAutoField", Collection.class);
            getTypeAutoFieldMethod.setAccessible(true);
            autoFieldMetadata = (XSDKey) getTypeAutoFieldMethod.invoke(loadServlet, autoFields);
        }

        XmlServer server = Util.getXmlServerCtrlLocal();

        Method bulkLoadSaveMethod = loadServlet.getClass()
                .getDeclaredMethod("bulkLoadSave", String.class, String.class, InputStream.class, LoadAction.class, XSDKey.class,
                        XSDKey.class);
        bulkLoadSaveMethod.setAccessible(true);

        bulkLoadSaveMethod
                .invoke(loadServlet, dataClusterName, dataModelName, recordXml, loadAction, keyMetadata, autoFieldMetadata);
        String result = server.getDocumentAsString(dataClusterName, dataClusterName + "." + typeName + ".1");
        Document xmlDocument = DocumentHelper.parseText(result);
        Element typeElement = xmlDocument.getRootElement().element("p").element(typeName);
        assertEquals(4, typeElement.elements().size());
        assertEquals(1, Integer.parseInt(typeElement.element("Id").getText()));
        assertEquals("John", typeElement.element("Name").getText());
        assertEquals("23", typeElement.element("Age").getText());
        Element courseElement = typeElement.element("Course");
        assertNotNull(courseElement);
        assertEquals("English", courseElement.element("Id").getText());
        assertEquals("Mike", courseElement.element("Teacher").getText());
    }

    @Test
    public void test_09_BulkLoadForComplexTypeGenerate() throws Exception {
        String dataClusterName = "Student";
        String typeName = "Student";
        String dataModelName = "Student";
        boolean needAutoGenPK = false;
        boolean needAutoGenAutoFields = true;
        boolean insertOnly = false;

        MetadataRepository repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata03.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register(dataClusterName, repository);
        ComplexTypeMetadata type = repository.getComplexType(typeName);

        LoadAction loadAction = new OptimizedLoadAction(dataClusterName, typeName, dataModelName, needAutoGenPK,
                needAutoGenAutoFields);

        DataRecord.CheckExistence.set(!insertOnly);
        InputStream recordXml = new ByteArrayInputStream(
                ("<Student><Id>2</Id><Name>John</Name><Age>23</Age><Course><Id>English</Id><Teacher>Mike</Teacher></Course></Student>")
                        .getBytes(StandardCharsets.UTF_8));

        Method getTypeKeyMethod = loadServlet.getClass().getDeclaredMethod("getTypeKey", Collection.class);
        getTypeKeyMethod.setAccessible(true);

        XSDKey keyMetadata = (XSDKey) getTypeKeyMethod.invoke(loadServlet, type.getKeyFields());
        XSDKey autoFieldMetadata = null;
        if (needAutoGenAutoFields) {
            Collection<FieldMetadata> fields = type.getFields();
            Collection<FieldMetadata> autoFields = fields.stream().filter(filed -> (!filed.isKey())).collect(Collectors.toList());
            Method getTypeAutoFieldMethod = loadServlet.getClass().getDeclaredMethod("getTypeAutoField", Collection.class);
            getTypeAutoFieldMethod.setAccessible(true);
            autoFieldMetadata = (XSDKey) getTypeAutoFieldMethod.invoke(loadServlet, autoFields);
        }

        XmlServer server = Util.getXmlServerCtrlLocal();

        Method bulkLoadSaveMethod = loadServlet.getClass()
                .getDeclaredMethod("bulkLoadSave", String.class, String.class, InputStream.class, LoadAction.class, XSDKey.class,
                        XSDKey.class);
        bulkLoadSaveMethod.setAccessible(true);

        bulkLoadSaveMethod
                .invoke(loadServlet, dataClusterName, dataModelName, recordXml, loadAction, keyMetadata, autoFieldMetadata);
        String result = server.getDocumentAsString(dataClusterName, dataClusterName + "." + typeName + ".2");
        Document xmlDocument = DocumentHelper.parseText(result);
        Element typeElement = xmlDocument.getRootElement().element("p").element(typeName);
        assertEquals(6, typeElement.elements().size());
        assertEquals(2, Integer.parseInt(typeElement.element("Id").getText()));
        assertEquals("John", typeElement.element("Name").getText());
        assertEquals("23", typeElement.element("Age").getText());
        assertEquals(36, typeElement.element("Account").getText().length());
        assertEquals("1", typeElement.element("Site").getText());
        Element courseElement = typeElement.element("Course");
        assertNotNull(courseElement);
        assertEquals(4, courseElement.elements().size());
        assertEquals("English", courseElement.element("Id").getText());
        assertEquals("Mike", courseElement.element("Teacher").getText());
        assertEquals("1", courseElement.element("Score").getText());
        assertEquals(36, courseElement.element("Like").getText().length());
    }

    @Test
    public void test_10_BulkLoadForComplexTypeGeneratePartial() throws Exception {
        String dataClusterName = "Student";
        String typeName = "Student";
        String dataModelName = "Student";
        boolean needAutoGenPK = false;
        boolean needAutoGenAutoFields = true;
        boolean insertOnly = false;

        MetadataRepository repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata03.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register(dataClusterName, repository);
        ComplexTypeMetadata type = repository.getComplexType(typeName);

        LoadAction loadAction = new OptimizedLoadAction(dataClusterName, typeName, dataModelName, needAutoGenPK,
                needAutoGenAutoFields);

        DataRecord.CheckExistence.set(!insertOnly);
        InputStream recordXml = new ByteArrayInputStream(
                ("<Student><Id>3</Id><Name>John</Name><Age>23</Age><Site>20</Site><Course><Id>English</Id><Teacher>Mike</Teacher><Score>10</Score></Course></Student>")
                        .getBytes(StandardCharsets.UTF_8));

        Method getTypeKeyMethod = loadServlet.getClass().getDeclaredMethod("getTypeKey", Collection.class);
        getTypeKeyMethod.setAccessible(true);

        XSDKey keyMetadata = (XSDKey) getTypeKeyMethod.invoke(loadServlet, type.getKeyFields());
        XSDKey autoFieldMetadata = null;
        if (needAutoGenAutoFields) {
            Collection<FieldMetadata> fields = type.getFields();
            Collection<FieldMetadata> autoFields = fields.stream().filter(filed -> (!filed.isKey())).collect(Collectors.toList());
            Method getTypeAutoFieldMethod = loadServlet.getClass().getDeclaredMethod("getTypeAutoField", Collection.class);
            getTypeAutoFieldMethod.setAccessible(true);
            autoFieldMetadata = (XSDKey) getTypeAutoFieldMethod.invoke(loadServlet, autoFields);
        }

        XmlServer server = Util.getXmlServerCtrlLocal();

        Method bulkLoadSaveMethod = loadServlet.getClass()
                .getDeclaredMethod("bulkLoadSave", String.class, String.class, InputStream.class, LoadAction.class, XSDKey.class,
                        XSDKey.class);
        bulkLoadSaveMethod.setAccessible(true);

        bulkLoadSaveMethod
                .invoke(loadServlet, dataClusterName, dataModelName, recordXml, loadAction, keyMetadata, autoFieldMetadata);
        String result = server.getDocumentAsString(dataClusterName, dataClusterName + "." + typeName + ".3");
        Document xmlDocument = DocumentHelper.parseText(result);
        Element typeElement = xmlDocument.getRootElement().element("p").element(typeName);
        assertEquals(6, typeElement.elements().size());
        assertEquals(3, Integer.parseInt(typeElement.element("Id").getText()));
        assertEquals("John", typeElement.element("Name").getText());
        assertEquals("23", typeElement.element("Age").getText());
        assertEquals(36, typeElement.element("Account").getText().length());
        assertEquals("20", typeElement.element("Site").getText());
        Element courseElement = typeElement.element("Course");
        assertNotNull(courseElement);
        assertEquals(4, courseElement.elements().size());
        assertEquals("English", courseElement.element("Id").getText());
        assertEquals("Mike", courseElement.element("Teacher").getText());
        assertEquals("10", courseElement.element("Score").getText());
        assertEquals(36, courseElement.element("Like").getText().length());
    }

    @Test
    public void test_11_BulkLoadForComplexTypeNotGenerated() throws Exception {
        String dataClusterName = "Student";
        String typeName = "Student";
        String dataModelName = "Student";
        boolean needAutoGenPK = false;
        boolean needAutoGenAutoFields = true;
        boolean insertOnly = false;

        MetadataRepository repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata03.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register(dataClusterName, repository);
        ComplexTypeMetadata type = repository.getComplexType(typeName);

        LoadAction loadAction = new OptimizedLoadAction(dataClusterName, typeName, dataModelName, needAutoGenPK,
                needAutoGenAutoFields);

        DataRecord.CheckExistence.set(!insertOnly);
        InputStream recordXml = new ByteArrayInputStream(
                ("<Student><Id>4</Id><Name>John</Name><Age>23</Age><Site>10</Site></Student>")
                        .getBytes(StandardCharsets.UTF_8));

        Method getTypeKeyMethod = loadServlet.getClass().getDeclaredMethod("getTypeKey", Collection.class);
        getTypeKeyMethod.setAccessible(true);

        XSDKey keyMetadata = (XSDKey) getTypeKeyMethod.invoke(loadServlet, type.getKeyFields());
        XSDKey autoFieldMetadata = null;
        if (needAutoGenAutoFields) {
            Collection<FieldMetadata> fields = type.getFields();
            Collection<FieldMetadata> autoFields = fields.stream().filter(filed -> (!filed.isKey())).collect(Collectors.toList());
            Method getTypeAutoFieldMethod = loadServlet.getClass().getDeclaredMethod("getTypeAutoField", Collection.class);
            getTypeAutoFieldMethod.setAccessible(true);
            autoFieldMetadata = (XSDKey) getTypeAutoFieldMethod.invoke(loadServlet, autoFields);
        }

        XmlServer server = Util.getXmlServerCtrlLocal();

        Method bulkLoadSaveMethod = loadServlet.getClass()
                .getDeclaredMethod("bulkLoadSave", String.class, String.class, InputStream.class, LoadAction.class, XSDKey.class,
                        XSDKey.class);
        bulkLoadSaveMethod.setAccessible(true);

        bulkLoadSaveMethod
                .invoke(loadServlet, dataClusterName, dataModelName, recordXml, loadAction, keyMetadata, autoFieldMetadata);
        String result = server.getDocumentAsString(dataClusterName, dataClusterName + "." + typeName + ".4");
        Document xmlDocument = DocumentHelper.parseText(result);
        Element typeElement = xmlDocument.getRootElement().element("p").element(typeName);
        assertEquals(5, typeElement.elements().size());
        assertEquals(4, Integer.parseInt(typeElement.element("Id").getText()));
        assertEquals("John", typeElement.element("Name").getText());
        assertEquals("23", typeElement.element("Age").getText());
        assertEquals(36, typeElement.element("Account").getText().length());
        assertEquals("10", typeElement.element("Site").getText());
        Element courseElement = typeElement.element("Course");
        assertNull(courseElement);
    }

    @Test
    public void testGetTypeAutoField() throws Exception {
        String dataClusterName = "AutoInc";
        String typeName = "Person";
        String dataModelName = "AutoInc";
        MetadataRepository repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata01.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register(dataClusterName, repository);
        ComplexTypeMetadata type = repository.getComplexType(typeName);

        Collection<FieldMetadata> fields = type.getFields();
        Collection<FieldMetadata> autoFields = fields.stream().filter(filed -> (!filed.isKey())).collect(Collectors.toList());
        Method getTypeAutoFieldMethod = loadServlet.getClass().getDeclaredMethod("getTypeAutoField", Collection.class);
        getTypeAutoFieldMethod.setAccessible(true);
        XSDKey autoFieldMetadata = (XSDKey) getTypeAutoFieldMethod.invoke(loadServlet, autoFields);

        assertNotNull(autoFieldMetadata);
        assertEquals(5, autoFieldMetadata.getFields().length);
        assertEquals("AA", autoFieldMetadata.getFields()[0]);
        assertEquals("BB", autoFieldMetadata.getFields()[1]);
        assertEquals("CC", autoFieldMetadata.getFields()[2]);
        assertEquals("DD", autoFieldMetadata.getFields()[3]);
        assertEquals("EE", autoFieldMetadata.getFields()[4]);

        assertEquals(5, autoFieldMetadata.getFieldTypes().length);
        assertEquals("AUTO_INCREMENT", autoFieldMetadata.getFieldTypes()[0]);
        assertEquals("AUTO_INCREMENT", autoFieldMetadata.getFieldTypes()[1]);
        assertEquals("AUTO_INCREMENT", autoFieldMetadata.getFieldTypes()[2]);
        assertEquals("UUID", autoFieldMetadata.getFieldTypes()[3]);
        assertEquals("UUID", autoFieldMetadata.getFieldTypes()[4]);

        dataClusterName = "Product";
        typeName = "Product";
        repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata02.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register(dataClusterName, repository);
        type = repository.getComplexType(typeName);

        fields = type.getFields();
        autoFields = fields.stream().filter(filed -> (!filed.isKey())).collect(Collectors.toList());

        autoFieldMetadata = (XSDKey) getTypeAutoFieldMethod.invoke(loadServlet, autoFields);

        assertNotNull(autoFieldMetadata);
        assertEquals(2, autoFieldMetadata.getFields().length);
        assertEquals("Support", autoFieldMetadata.getFields()[0]);
        assertEquals("Supply", autoFieldMetadata.getFields()[1]);

        assertEquals(2, autoFieldMetadata.getFieldTypes().length);
        assertEquals("AUTO_INCREMENT", autoFieldMetadata.getFieldTypes()[0]);
        assertEquals("UUID", autoFieldMetadata.getFieldTypes()[1]);

        dataClusterName = "Student";
        typeName = "Student";
        repository = new MetadataRepository();
        repository.load(LoadServletForAutoIncrementTest.class.getResourceAsStream("metadata03.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register(dataClusterName, repository);
        type = repository.getComplexType(typeName);

        fields = type.getFields();
        autoFields = fields.stream().filter(filed -> (!filed.isKey())).collect(Collectors.toList());

        autoFieldMetadata = (XSDKey) getTypeAutoFieldMethod.invoke(loadServlet, autoFields);

        assertNotNull(autoFieldMetadata);
        assertEquals(4, autoFieldMetadata.getFields().length);
        assertEquals("Account", autoFieldMetadata.getFields()[0]);
        assertEquals("Site", autoFieldMetadata.getFields()[1]);
        assertEquals("Course/Score", autoFieldMetadata.getFields()[2]);
        assertEquals("Course/Like", autoFieldMetadata.getFields()[3]);

        assertEquals(4, autoFieldMetadata.getFieldTypes().length);
        assertEquals("UUID", autoFieldMetadata.getFieldTypes()[0]);
        assertEquals("AUTO_INCREMENT", autoFieldMetadata.getFieldTypes()[1]);
        assertEquals("AUTO_INCREMENT", autoFieldMetadata.getFieldTypes()[2]);
        assertEquals("UUID", autoFieldMetadata.getFieldTypes()[3]);
    }
}
