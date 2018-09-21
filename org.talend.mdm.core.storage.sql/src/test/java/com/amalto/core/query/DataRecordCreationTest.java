/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.query;

import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.amalto.core.load.io.ResettableStringWriter;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.ItemPKCriteriaResultsWriter;
import com.amalto.core.storage.StagingStorage;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.DataRecordWriter;
import com.amalto.core.storage.record.XmlDOMDataRecordReader;
import com.amalto.core.storage.record.XmlSAXDataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import com.amalto.core.storage.record.metadata.DataRecordMetadata;
import com.amalto.core.util.Util;
import com.amalto.xmlserver.interfaces.XmlServerException;

@SuppressWarnings("nls")
public class DataRecordCreationTest extends StorageTestCase {

    private static final Logger LOGGER = Logger.getLogger(DataRecordCreationTest.class);

    @Override
    public void tearDown() throws Exception {
        try {
            storage.begin();
            {
                UserQueryBuilder qb = from(company);
                try {
                    storage.delete(qb.getSelect());
                } catch (Exception e) {
                    // Ignored
                }
            }
            storage.commit();
        } finally {
            storage.end();
        }
    }

    public void __testCompositeKeyAndFK() {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DataRecordCreationTest.class.getResourceAsStream("metadata17.xsd"));

        Storage hibernateStorage = new HibernateStorage("H2-DS1", StorageType.STAGING); //$NON-NLS-1$
        hibernateStorage.init(ServerContext.INSTANCE.get().getDefinition("H2-DS1", "MDM")); //$NON-NLS-1$//$NON-NLS-2$
        hibernateStorage.prepare(repository, true);
        Storage storage = new StagingStorage(hibernateStorage);
        DataRecordReader<String> factory = new XmlStringDataRecordReader();

        List<DataRecord> records = new LinkedList<DataRecord>();
        records.add(factory.read(repository, repository.getComplexType("MyType"),
                "<MyType><subelement>22</subelement><myDatetime>2014-04-17T12:00:00</myDatetime><myDate>2014-04-17</myDate></MyType>"));
        storage.begin();
        storage.update(records);
        storage.commit();

        // Query saved data
        storage.begin();
        ComplexTypeMetadata dateInKey = repository.getComplexType("MyType"); //$NON-NLS-1$
        UserQueryBuilder qb = from(dateInKey);
        qb.start(0);
        qb.limit(1);
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        DataRecord result = results.iterator().next();
    }

    public void __testDateTypeInForeignKey() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DataRecordCreationTest.class.getResourceAsStream("metadata16_1.xsd"));

        Storage storage = new HibernateStorage("H2-Default"); //$NON-NLS-1$
        storage.init(ServerContext.INSTANCE.get().getDefinition("H2-Default", "MDM")); //$NON-NLS-1$//$NON-NLS-2$
        storage.prepare(repository, true);
        DataRecordReader<String> factory = new XmlStringDataRecordReader();

        List<DataRecord> records = new LinkedList<DataRecord>();
        records.add(factory.read(
                repository,
                repository.getComplexType("EOR"),
                "<EOR>  <UG_EOR>1</UG_EOR>  <TYP_EOR>1</TYP_EOR>  <L_TYP_EOR>1</L_TYP_EOR>  <CAT_TYP_EOR>1</CAT_TYP_EOR>  <D_DEB_EOR>2014-04-21</D_DEB_EOR>  <UG_EOR_FILLES/>  <UG_EOR_MERES/>  <GARES>    <GARE>[1][2]</GARE>  </GARES> </EOR>"));
        records.add(factory.read(repository, repository.getComplexType("GARE"),
                "<GARE>  <IFE>1</IFE>  <ETFE>2</ETFE>  <UG_EOR>[1][1][2014-04-21]</UG_EOR> </GARE>"));
        storage.begin();
        storage.update(records);
        storage.commit();

        // Query saved data
        storage.begin();
        ComplexTypeMetadata dateInKey = repository.getComplexType("GARE"); //$NON-NLS-1$
        UserQueryBuilder qb = from(dateInKey);
        qb.start(0);
        qb.limit(1);
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        DataRecord result = results.iterator().next();
        assertEquals("[1][1][2014-04-21]",
                StorageMetadataUtils.toString(result.get("UG_EOR"), result.getType().getField("UG_EOR")));
    }

    public void __testDateTypeInKey() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DataRecordCreationTest.class.getResourceAsStream("metadata16.xsd"));

        Storage storage = new HibernateStorage("H2-Default"); //$NON-NLS-1$
        storage.init(ServerContext.INSTANCE.get().getDefinition("H2-Default", "MDM")); //$NON-NLS-1$//$NON-NLS-2$
        storage.prepare(repository, true);
        DataRecordReader<String> factory = new XmlStringDataRecordReader();

        List<DataRecord> records = new LinkedList<DataRecord>();
        records.add(factory.read(repository, repository.getComplexType("DateInKey"),
                "<DateInKey><id>22</id><name>22</name><date1>2014-04-17</date1></DateInKey>"));
        records.add(factory.read(repository, repository.getComplexType("DateTimeInKey"),
                "<DateTimeInKey><code>22</code><db1>2014-04-17T12:00:00</db1><aaa>aaa</aaa></DateTimeInKey>"));
        storage.begin();
        storage.update(records);
        storage.commit();

        // Query saved data
        storage.begin();
        ComplexTypeMetadata dateInKey = repository.getComplexType("DateInKey"); //$NON-NLS-1$
        UserQueryBuilder qb = from(dateInKey);
        qb.start(0);
        qb.limit(1);
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        DataRecord result = results.iterator().next();
        assertEquals("2014-04-17", StorageMetadataUtils.toString(result.get("date1"), result.getType().getField("date1")));

        DataRecordWriter writer = new ItemPKCriteriaResultsWriter(dateInKey.getName(), dateInKey);
        ResettableStringWriter stringWriter = new ResettableStringWriter();
        try {
            writer.write(result, stringWriter);
        } catch (IOException e) {
            throw new XmlServerException(e);
        }
        String recordStringValue = stringWriter.toString();
        XPath xpath = XPathFactory.newInstance().newXPath();

        DocumentBuilder documentBuilder = MDMXMLUtils.getDocumentBuilder().get();
        Element r = documentBuilder.parse(new InputSource(new StringReader(recordStringValue))).getDocumentElement();
        NodeList idsList = (NodeList) xpath.evaluate("./ids/i", r, XPathConstants.NODESET); //$NON-NLS-1$
        List<String> keyStrValues = new ArrayList<String>();
        for (int j = 0; j < idsList.getLength(); j++) {
            keyStrValues.add(idsList.item(j).getFirstChild() == null ? "" : idsList.item(j).getFirstChild().getNodeValue()); //$NON-NLS-1$
        }
        assertTrue(keyStrValues.contains("2014-04-17"));
        assertTrue(keyStrValues.contains("22"));
        stringWriter.reset();

        dateInKey = repository.getComplexType("DateTimeInKey"); //$NON-NLS-1$
        qb = from(dateInKey);
        qb.start(0);
        qb.limit(1);
        results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        result = results.iterator().next();
        assertEquals("2014-04-17T12:00:00", StorageMetadataUtils.toString(result.get("db1"), result.getType().getField("db1")));
        writer = new ItemPKCriteriaResultsWriter(dateInKey.getName(), dateInKey);
        try {
            writer.write(result, stringWriter);
        } catch (IOException e) {
            throw new XmlServerException(e);
        }
        recordStringValue = stringWriter.toString();
        r = documentBuilder.parse(new InputSource(new StringReader(recordStringValue))).getDocumentElement();
        idsList = (NodeList) xpath.evaluate("./ids/i", r, XPathConstants.NODESET); //$NON-NLS-1$
        keyStrValues.clear();
        for (int j = 0; j < idsList.getLength(); j++) {
            keyStrValues.add(idsList.item(j).getFirstChild() == null ? "" : idsList.item(j).getFirstChild().getNodeValue()); //$NON-NLS-1$
        }
        assertTrue(keyStrValues.contains("2014-04-17T12:00:00"));
        assertTrue(keyStrValues.contains("22"));
        stringWriter.reset();
    }

    public void testCreationFromXMLString() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(this.getClass().getResourceAsStream("metadata.xsd"));

        ComplexTypeMetadata product = repository.getComplexType("Product");
        assertNotNull(product);

        StringBuilder builder = new StringBuilder();
        InputStream testResource = this.getClass().getResourceAsStream("DataRecordCreationTest_1.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(testResource));
        String current;
        while ((current = reader.readLine()) != null) {
            builder.append(current);
        }

        DataRecordReader<String> dataRecordReader = new XmlStringDataRecordReader();
        DataRecord dataRecord = dataRecordReader.read(repository, product, builder.toString());

        performAsserts(dataRecord);
    }

    public void testCreationFromXMLStringWithMetadata() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(this.getClass().getResourceAsStream("metadata.xsd"));

        ComplexTypeMetadata product = repository.getComplexType("Product");
        assertNotNull(product);

        StringBuilder builder = new StringBuilder();
        InputStream testResource = this.getClass().getResourceAsStream("DataRecordCreationTest_3.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(testResource));
        String current;
        while ((current = reader.readLine()) != null) {
            builder.append(current);
        }

        DataRecordReader<String> dataRecordReader = new XmlStringDataRecordReader();
        DataRecord dataRecord = dataRecordReader.read(repository, product, builder.toString());

        performAsserts(dataRecord);
        performMetadataAsserts(dataRecord);
    }

    private void performMetadataAsserts(DataRecord dataRecord) {
        DataRecordMetadata recordMetadata = dataRecord.getRecordMetadata();
        assertEquals("1234", recordMetadata.getTaskId());
        Map<String, String> recordProperties = recordMetadata.getRecordProperties();
        assertEquals("My Source", recordProperties.get(Storage.METADATA_STAGING_SOURCE));
        assertEquals("My Error", recordProperties.get(Storage.METADATA_STAGING_ERROR));
        assertEquals("999", recordProperties.get(Storage.METADATA_STAGING_STATUS));
        assertEquals("5678", recordProperties.get(Storage.METADATA_STAGING_BLOCK_KEY));
    }

    public void testCreationFromXMLStringWithInheritance() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(this.getClass().getResourceAsStream("metadata.xsd"));

        ComplexTypeMetadata c = repository.getComplexType("C");
        assertNotNull(c);

        StringBuilder builder = new StringBuilder();
        InputStream testResource = this.getClass().getResourceAsStream("DataRecordCreationTest_2.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(testResource));
        String current;
        while ((current = reader.readLine()) != null) {
            builder.append(current);
        }

        DataRecordReader<String> dataRecordReader = new XmlStringDataRecordReader();
        DataRecord dataRecord = dataRecordReader.read(repository, c, builder.toString());

        performInheritanceAsserts(dataRecord);
    }


    public void testCreationFromSAX() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(this.getClass().getResourceAsStream("metadata.xsd"));

        ComplexTypeMetadata product = repository.getComplexType("Product");
        assertNotNull(product);

        XMLReader xmlReader = MDMXMLUtils.getXMLReader();
        DataRecordReader<XmlSAXDataRecordReader.Input> dataRecordReader = new XmlSAXDataRecordReader();
        XmlSAXDataRecordReader.Input input = new XmlSAXDataRecordReader.Input(xmlReader, new InputSource(this.getClass()
                .getResourceAsStream("DataRecordCreationTest_1.xml")));
        DataRecord dataRecord = dataRecordReader.read(repository, product, input);

        performAsserts(dataRecord);
    }

    public void testCreationFromSAXWithMetadata() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(this.getClass().getResourceAsStream("metadata.xsd"));

        ComplexTypeMetadata product = repository.getComplexType("Product");
        assertNotNull(product);
        XMLReader xmlReader = MDMXMLUtils.getXMLReader();

        DataRecordReader<XmlSAXDataRecordReader.Input> dataRecordReader = new XmlSAXDataRecordReader();
        XmlSAXDataRecordReader.Input input = new XmlSAXDataRecordReader.Input(xmlReader, new InputSource(this.getClass()
                .getResourceAsStream("DataRecordCreationTest_3.xml")));
        DataRecord dataRecord = dataRecordReader.read(repository, product, input);

        performAsserts(dataRecord);
        performMetadataAsserts(dataRecord);
    }

    public void testCreationFromSAXWithInheritance() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(this.getClass().getResourceAsStream("metadata.xsd"));
        ComplexTypeMetadata c = repository.getComplexType("C");
        assertNotNull(c);
        XMLReader xmlReader = MDMXMLUtils.getXMLReader();

        DataRecordReader<XmlSAXDataRecordReader.Input> dataRecordReader = new XmlSAXDataRecordReader();
        XmlSAXDataRecordReader.Input input = new XmlSAXDataRecordReader.Input(xmlReader, new InputSource(this.getClass()
                .getResourceAsStream("DataRecordCreationTest_2.xml")));
        DataRecord dataRecord = dataRecordReader.read(repository, c, input);
        performInheritanceAsserts(dataRecord);
    }

    public void testCreationFromSAXWithReusableTypeFieldUnregistered() throws Exception {
        try {
            List<DataRecord> records = getDataRecords("DataRecordCreationTest_4.xml", company);
            Storage storage = new HibernateStorage("H2-Default"); //$NON-NLS-1$
            storage.init(ServerContext.INSTANCE.get().getDefinition("H2-Default", "MDM")); //$NON-NLS-1$//$NON-NLS-2$
            storage.prepare(repository, true);
            for (DataRecord record : records) {
                performCreationFromSAXWithReusableTypeFieldUnregisteredAsserts(storage, record);
            }
        } finally {
            storage.end();
        }
    }

    private List<DataRecord> getDataRecords(String resourceName, ComplexTypeMetadata recordType) throws Exception {
        List<DataRecord> records = new LinkedList<DataRecord>();
        // Read using SAX
        XMLReader xmlReader = MDMXMLUtils.getXMLReader();

        DataRecordReader<XmlSAXDataRecordReader.Input> dataRecordReader = new XmlSAXDataRecordReader();
        XmlSAXDataRecordReader.Input input = new XmlSAXDataRecordReader.Input(xmlReader, new InputSource(this.getClass()
                .getResourceAsStream(resourceName)));
        DataRecord dataRecord = dataRecordReader.read(repository, recordType, input);
        records.add(dataRecord);
        // Read using DOM
        DataRecordReader<Element> documentDataRecordReader = new XmlDOMDataRecordReader();
        Document document = Util.parse(IOUtils.toString(this.getClass().getResourceAsStream(resourceName)));
        records.add(documentDataRecordReader.read(repository, company, document.getDocumentElement()));
        // Read using String
        DataRecordReader<String> stringDataRecordReader = new XmlStringDataRecordReader();
        DataRecord record = stringDataRecordReader.read(repository, company, IOUtils.toString(this.getClass().getResourceAsStream(resourceName)));
        records.add(record);
        return records;
    }

    private static void performCreationFromSAXWithReusableTypeFieldUnregisteredAsserts(Storage storage, DataRecord dataRecord) {
        assertNotNull(dataRecord);
        assertEquals("Company", dataRecord.getType().getName());
        assertEquals("1", dataRecord.get("subelement"));
        Object staff = dataRecord.get("staff");
        assertNotNull(staff);
        assertTrue(staff instanceof DataRecord);
        assertEquals("PersonType", ((DataRecord) staff).getType().getName());
        assertEquals("John", dataRecord.get("staff/name"));
        assertEquals(30, dataRecord.get("staff/age"));
        // Test storage update
        storage.begin();
        storage.update(dataRecord);
        storage.commit();
    }

    public void testCreationFromSAXWithReusableTypeNoMapping() throws Exception {
        Storage storage = new HibernateStorage("H2-Default"); //$NON-NLS-1$
        storage.init(ServerContext.INSTANCE.get().getDefinition("H2-Default", "MDM")); //$NON-NLS-1$//$NON-NLS-2$
        storage.prepare(repository, true);
        List<DataRecord> records = getDataRecords("DataRecordCreationTest_5.xml", company);
        for (DataRecord record : records) {
            performCreationFromSAXWithReusableTypeNoMappingAsserts(storage, record);
        }
    }

    private void performCreationFromSAXWithReusableTypeNoMappingAsserts(Storage storage, DataRecord dataRecord) {
        assertNotNull(dataRecord);
        assertEquals("Company", dataRecord.getType().getName());
        assertEquals("1", dataRecord.get("subelement"));
        Object companyType = dataRecord.get("type");
        assertNotNull(companyType);
        assertTrue(companyType instanceof DataRecord);
        assertEquals("CompanyType", ((DataRecord) companyType).getType().getName());
        assertEquals("Non-Profit", dataRecord.get("type/profitable"));
        try {
            storage.begin();
            storage.update(dataRecord);
            storage.commit();

            storage.begin();
            UserQueryBuilder qb = from(company);
            StorageResults results = storage.fetch(qb.getSelect());

            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result);
                assertTrue("1".equals(result.get("subelement")));
                assertTrue("Non-Profit".equals(result.get("type/profitable")));
            }
            results.close();
            storage.commit();
        } finally {
            storage.end();
        }
    }

    public void testCreationFromDOM() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(this.getClass().getResourceAsStream("metadata.xsd"));

        ComplexTypeMetadata product = repository.getComplexType("Product");
        assertNotNull(product);

        Document document = MDMXMLUtils.getDocumentBuilderWithNamespace().get()
                .parse(this.getClass().getResourceAsStream("DataRecordCreationTest_1.xml"));
        DataRecordReader<Element> dataRecordReader = new XmlDOMDataRecordReader();
        DataRecord dataRecord = dataRecordReader.read(repository, product, document.getDocumentElement());

        performAsserts(dataRecord);
    }

    public void testCreationFromDOMWithMetadata() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(this.getClass().getResourceAsStream("metadata.xsd"));

        ComplexTypeMetadata product = repository.getComplexType("Product");
        assertNotNull(product);

        DocumentBuilder documentBuilder = MDMXMLUtils.getDocumentBuilderWithNamespace().get();
        InputStream stream = this.getClass().getResourceAsStream("DataRecordCreationTest_3.xml");
        Document document = documentBuilder.parse(stream);
        DataRecordReader<Element> dataRecordReader = new XmlDOMDataRecordReader();
        DataRecord dataRecord = dataRecordReader.read(repository, product, document.getDocumentElement());

        performAsserts(dataRecord);
        performMetadataAsserts(dataRecord);
    }

    public void testCreationFromDOMWithInheritance() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(this.getClass().getResourceAsStream("metadata.xsd"));

        ComplexTypeMetadata c = repository.getComplexType("C");
        assertNotNull(c);

        DocumentBuilder documentBuilder = MDMXMLUtils.getDocumentBuilderWithNamespace().get();
        Document document = documentBuilder.parse(this.getClass().getResourceAsStream("DataRecordCreationTest_2.xml"));
        DataRecordReader<Element> dataRecordReader = new XmlDOMDataRecordReader();
        DataRecord dataRecord = dataRecordReader.read(repository, c, document.getDocumentElement());

        performInheritanceAsserts(dataRecord);
    }

    private void performAsserts(DataRecord dataRecord) {
        assertNotNull(dataRecord);
        assertEquals("Product", dataRecord.getType().getName());

        ComplexTypeMetadata product = dataRecord.getType();
        assertEquals("1", dataRecord.get("Id"));
        assertEquals("1", dataRecord.get(product.getField("Id")));
        assertEquals("Test23", dataRecord.get("Name"));
        assertEquals("Test23", dataRecord.get(product.getField("Name")));
        assertNull(dataRecord.get("LongDescription"));
        assertNull(dataRecord.get(product.getField("LongDescription")));
        assertEquals("Pending", dataRecord.get("Status"));
        assertEquals("Pending", dataRecord.get(product.getField("Status")));

        Object o = dataRecord.get("Features/Sizes/Size");
        assertNotNull(o);
        assertTrue(o instanceof List);
        List list = (List) o;
        assertEquals(2, list.size());
        assertEquals("Small", list.get(0));
        assertEquals("Large", list.get(1));

        o = dataRecord.get("Features/Colors/Color");
        assertNotNull(o);
        assertTrue(o instanceof List);
        list = (List) o;
        assertEquals(1, list.size());
        assertEquals("Blue", list.get(0));

        assertNotNull(dataRecord.get("Product"));

        o = dataRecord.get("Supplier");
        assertNotNull(o);
        assertTrue(o instanceof List);
        assertEquals(1, ((List) o).size());
        assertTrue(((List) o).get(0) instanceof DataRecord);
        assertEquals("Supplier", ((DataRecord) ((List) o).get(0)).getType().getName());
        assertEquals("1", ((DataRecord) ((List) o).get(0)).get("Id"));

        o = dataRecord.get("Family");
        assertNotNull(o);
        assertTrue(o instanceof DataRecord);
        assertEquals("ProductFamily", ((DataRecord) o).getType().getName());
        assertEquals("2", ((DataRecord) o).get("Id"));

        // Metadata asserts
        DataRecordMetadata metadata = dataRecord.getRecordMetadata();
        assertNotNull(metadata);

        assertEquals(1328544306381l, metadata.getLastModificationTime());
        assertNotNull(metadata.getRecordProperties());
    }

    private void performInheritanceAsserts(DataRecord dataRecord) {
        assertNotNull(dataRecord);
        assertEquals("C", dataRecord.getType().getName());

        assertEquals("2", dataRecord.get("id"));
        assertEquals("TextAC", dataRecord.get("textA"));
        assertEquals("TextCC", dataRecord.get("textC"));

        Object o = dataRecord.get("refB");
        assertNotNull(o);
        assertTrue(o instanceof DataRecord);
        assertEquals("D", ((DataRecord) o).getType().getName());

        o = dataRecord.get("nestedB");
        assertNotNull(o);
        assertTrue(o instanceof DataRecord);
        assertEquals("SubNested", ((DataRecord) o).getType().getName());

        assertEquals("Text", dataRecord.get("nestedB/text"));
        assertEquals("SubText", dataRecord.get("nestedB/subText"));
    }

    public void testUserXmlData() {
        DataRecordReader<String> xmlReader = new XmlStringDataRecordReader();
        DataRecord r1 = xmlReader.read(repository, b, "<B><id>1</id><textB>TextB</textB></B>");
        DataRecord r2 = xmlReader.read(repository, d, "<D><id>2</id><textB>TextBD</textB><textD>TextDD</textD></D>");
        DataRecord r3 = xmlReader.read(repository, persons, "<Persons><name>person</name><age>20</age></Persons>");
        DataRecord r4 = xmlReader.read(repository, employee,
                "<Employee><name>employee</name><age>21</age><jobTitle>Test</jobTitle></Employee>");
        DataRecord r5 = xmlReader.read(repository, manager,
                "<Manager><name>manager</name><age>25</age><jobTitle>Test</jobTitle><dept>manager</dept></Manager>");

        assertNotNull(r1);
        assertEquals("1", r1.get(b.getField("id")));
        assertEquals("TextB", r1.get(b.getField("textB")));

        assertNotNull(r2);
        assertEquals("2", r2.get(d.getField("id")));
        assertEquals("TextBD", r2.get(d.getField("textB")));
        assertEquals("TextDD", r2.get(d.getField("textD")));

        assertNotNull(r3);
        assertEquals("person", r3.get(persons.getField("name")));
        assertEquals(20, r3.get(persons.getField("age")));

        assertNotNull(r4);
        assertEquals("employee", r4.get(employee.getField("name")));
        assertEquals(21, r4.get(employee.getField("age")));
        assertEquals("Test", r4.get(employee.getField("jobTitle")));

        assertNotNull(r5);
        assertEquals("manager", r5.get(manager.getField("name")));
        assertEquals(25, r5.get(manager.getField("age")));
        assertEquals("Test", r5.get(manager.getField("jobTitle")));
        assertEquals("manager", r5.get(manager.getField("dept")));

    }

    public void testUserXmlDataWithInnerProperties() {
        DataRecordReader<String> xmlReader = new XmlStringDataRecordReader();
        DataRecord r1 = xmlReader.read(repository, b,
                "<ii><t>1365488764093</t><taskId>123456</taskId><p><B><id>1</id><textB>TextB</textB></B></p></ii>");
        DataRecord r2 = xmlReader
                .read(repository, d,
                        "<ii><t>1365488764093</t><taskId>123456</taskId><p><D><id>2</id><textB>TextBD</textB><textD>TextDD</textD></D></p></ii>");
        DataRecord r3 = xmlReader.read(repository, persons,
                "<ii><t>1365488764093</t><taskId>123456</taskId><p><Persons><name>person</name><age>20</age></Persons></p></ii>");
        DataRecord r4 = xmlReader
                .read(
                        repository,
                        employee,
                        "<ii><t>1365488764093</t><taskId>123456</taskId><p><Employee><name>employee</name><age>21</age><jobTitle>Test</jobTitle></Employee></p></ii>");
        DataRecord r5 = xmlReader
                .read(
                        repository,
                        manager,
                        "<ii><t>1365488764093</t><taskId>123456</taskId><p><Manager><name>manager</name><age>25</age><jobTitle>Test</jobTitle><dept>manager</dept></Manager></p></ii>");

        assertNotNull(r1);
        assertEquals("1", r1.get(b.getField("id")));
        assertEquals("TextB", r1.get(b.getField("textB")));
        DataRecordMetadata metadata = r2.getRecordMetadata();
        assertEquals(1365488764093L, metadata.getLastModificationTime());
        assertEquals("123456", metadata.getTaskId());

        assertNotNull(r2);
        assertEquals("2", r2.get(d.getField("id")));
        assertEquals("TextBD", r2.get(d.getField("textB")));
        assertEquals("TextDD", r2.get(d.getField("textD")));
        metadata = r2.getRecordMetadata();
        assertEquals(1365488764093L, metadata.getLastModificationTime());
        assertEquals("123456", metadata.getTaskId());

        assertNotNull(r3);
        assertEquals("person", r3.get(persons.getField("name")));
        assertEquals(20, r3.get(persons.getField("age")));
        metadata = r2.getRecordMetadata();
        assertEquals(1365488764093L, metadata.getLastModificationTime());
        assertEquals("123456", metadata.getTaskId());

        assertNotNull(r4);
        assertEquals("employee", r4.get(employee.getField("name")));
        assertEquals(21, r4.get(employee.getField("age")));
        assertEquals("Test", r4.get(employee.getField("jobTitle")));
        metadata = r2.getRecordMetadata();
        assertEquals(1365488764093L, metadata.getLastModificationTime());
        assertEquals("123456", metadata.getTaskId());

        assertNotNull(r5);
        assertEquals("manager", r5.get(manager.getField("name")));
        assertEquals(25, r5.get(manager.getField("age")));
        assertEquals("Test", r5.get(manager.getField("jobTitle")));
        assertEquals("manager", r5.get(manager.getField("dept")));
        metadata = r2.getRecordMetadata();
        assertEquals(1365488764093L, metadata.getLastModificationTime());
        assertEquals("123456", metadata.getTaskId());

    }

    // TMDM-9651 could not find item err on oracle db
    public void testScatteredSetValue() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DataRecordCreationTest.class.getResourceAsStream("DataRecordCreationTest_6.xsd"));

        Storage storage = new HibernateStorage("H2-Default"); //$NON-NLS-1$
        storage.init(ServerContext.INSTANCE.get().getDefinition("H2-Default", "MDM")); //$NON-NLS-1$//$NON-NLS-2$
        storage.prepare(repository, true);
        DataRecordReader<String> factory = new XmlStringDataRecordReader();

        List<DataRecord> records = new LinkedList<DataRecord>();
        records.add(factory.read(repository, repository.getComplexType("PartyCompany"),
                "<PartyCompany><code>1</code><name>a</name></PartyCompany>"));
        records.add(factory.read(repository, repository.getComplexType("PartyProduct"), "<PartyProduct><id>1</id><name>a</name>"
                + "<supplier>[1]</supplier></PartyProduct>"));
        storage.begin();
        storage.update(records);
        storage.commit();

        // Query saved data
        storage.begin();
        ComplexTypeMetadata dateInKey = repository.getComplexType("PartyProduct"); //$NON-NLS-1$
        UserQueryBuilder qb = from(dateInKey);
        qb.start(0);
        qb.limit(1);
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        DataRecord result = results.iterator().next();
        assertEquals("[1]", StorageMetadataUtils.toString(result.get("supplier"), result.getType().getField("supplier")));
    }
}
