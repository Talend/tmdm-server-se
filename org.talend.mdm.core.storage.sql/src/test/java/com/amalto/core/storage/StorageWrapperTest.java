/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage;

import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.load.io.ResettableStringWriter;
import com.amalto.core.query.StorageTestCase;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.DataRecordWriter;
import com.amalto.core.storage.record.DataRecordXmlWriter;
import com.amalto.core.storage.record.ViewSearchResultsWriter;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.ItemPKCriteria;
import com.amalto.xmlserver.interfaces.XmlServerException;

import junit.framework.TestCase;

public class StorageWrapperTest extends TestCase {
    
    public static String[] XMLS_PRODUCT = {
        "<ii><c>Product</c><n>Product</n><dmn>Product</dmn><i>333</i><t>1372654669313</t><taskId></taskId><p><Product><Id>333</Id><Name>333</Name><Description>333</Description><Price>333</Price></Product></p></ii>", //$NON-NLS-1$
        "<ii><c>Product</c><n>Product</n><dmn>Product</dmn><i>33&amp;44</i><t>1372654669313</t><taskId></taskId><p><Product><Id>33&amp;44</Id><Name>333</Name><Description>333</Description><Price>333</Price></Product></p></ii>", //$NON-NLS-1$
        "<ii><c>Product</c><n>Product</n><dmn>Product</dmn><i>&quot;555&lt;666&gt;444&quot;</i><t>1372654669313</t><taskId></taskId><p><Product><Id>&quot;555&lt;666&gt;444&quot;</Id><Name>333</Name><Description>333</Description><Price>333</Price></Product></p></ii>", //$NON-NLS-1$
        "<ii><c>Product</c><dmn>Product</dmn><dmr/><sp/><t>1442298182088</t><taskId>null</taskId><i>1</i><p><ProductFamily><Id>1</Id><Name>1</Name><ChangeStatus>Approved</ChangeStatus></ProductFamily></p></ii>", //$NON-NLS-1$
        "<ii><c>Product</c><dmn>Product</dmn><dmr/><sp/><t>1442298185640</t><taskId>null</taskId><i>1</i><p><Store><Id>1</Id><Address>1</Address><Lat>1.0</Lat><Long>1.0</Long></Store></p></ii>" //$NON-NLS-1$
       };
    public static String[] IDS_PRODUCT = {"Product.Product.333", "Product.Product.33&44", "Product.Product.\"555<666>444\"", "Product.ProductFamily.1", "Product.Store.1"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    public static String[] XMLS_INHERIT = { "<ii><c>InheritTest</c><n>InheritEntity</n><dmn>InheritTest</dmn><i>123</i><t>1372654669313</t><taskId></taskId><p><InheritEntity><id>123</id><field1>a</field1><field2>b</field2></InheritEntity></p></ii>" }; //$NON-NLS-1$
    
    public static String[] IDS_INHERIT = { "InheritTest.InheritEntity.1" }; //$NON-NLS-1$
    
    public StorageWrapperTest() {
        ServerContext.INSTANCE.get(new MockServerLifecycle());
    }
    
    public void testGetAllDocumentsUniqueID() throws Exception {
        StorageWrapper wrapper = prepareWrapper("Product", "Product.xsd", XMLS_PRODUCT, IDS_PRODUCT); //$NON-NLS-1$ //$NON-NLS-2$
        List<String> uniqueIDs = Arrays.asList(new String[]{"Product.Product.333", "Product.Product.33&44", "Product.Product.\"555<666>444\"", "Product.ProductFamily.1", "Product.Store.1"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        List<String> uniqueIDs_1 = Arrays.asList(wrapper.getAllDocumentsUniqueID("Product")); //$NON-NLS-1$
        assertTrue(uniqueIDs.containsAll(uniqueIDs_1));
        List<String> uniqueIDs_2 = Arrays.asList(wrapper.getAllDocumentsUniqueID("Product/Product")); //$NON-NLS-1$
        assertTrue(uniqueIDs.containsAll(uniqueIDs_2));
    }

    public void testGetDocumentAsString() throws Exception {
        BeanDelegatorContainer.createInstance().setDelegatorInstancePool(Collections.<String, Object> singletonMap("LocalUser", new MockUser())); //$NON-NLS-1$
        StorageWrapper wrapper = prepareWrapper("Product", "Product.xsd", XMLS_PRODUCT, IDS_PRODUCT); //$NON-NLS-1$ //$NON-NLS-2$
        String item = wrapper.getDocumentAsString("Product", "Product.Product.333", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertNotNull(item);
        assertTrue(item.contains("<i>333</i>")); //$NON-NLS-1$
        item = wrapper.getDocumentAsString("Product", "Product.Product.33&44", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertNotNull(item);
        assertTrue(item.contains("<i>33&amp;44</i>")); //$NON-NLS-1$
        item = wrapper.getDocumentAsString("Product", "Product.Product.\"555<666>444\"", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertNotNull(item);
        assertTrue(item.contains("<i>&quot;555&lt;666&gt;444&quot;</i>")); //$NON-NLS-1$      
        // bellow test inherit entity
        wrapper = prepareWrapper("InheritTest", "InheritEntityTest.xsd", XMLS_INHERIT, IDS_INHERIT); //$NON-NLS-1$ //$NON-NLS-2$
        item = wrapper.getDocumentAsString("InheritTest", "InheritTest.InheritEntity.123", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertNotNull(item);
        assertTrue(item.contains("<i>123</i>")); //$NON-NLS-1$
        assertTrue(item.contains("<InheritEntity><id>123</id><field1>a</field1><field2>b</field2></InheritEntity>")); //$NON-NLS-1$
        
        //get documents as string
        wrapper = prepareWrapper("Product", "Product.xsd", XMLS_PRODUCT, IDS_PRODUCT); //$NON-NLS-1$ //$NON-NLS-2$
        String[] uniqueIDs = {"Product.Product.333", "Product.ProductFamily.1", "Product.Store.1"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String[] xmls = wrapper.getDocumentsAsString("Product", uniqueIDs, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
        assertNotNull(xmls[0]);
        assertTrue(xmls[0].contains("<i>333</i>")); //$NON-NLS-1$
        assertNotNull(xmls[1]);
        //check getting xml according access permission
        assertTrue(xmls[1].contains("<p><ProductFamily><Id>1</Id><Name>1</Name></ProductFamily></p>")); //$NON-NLS-1$
        assertNotNull(xmls[2]);
        assertTrue(xmls[2].contains("<Store><Id>1</Id>")); //$NON-NLS-1$
           
    }

    public void testMultipleOccurrenceComplex() throws IOException {
        final MetadataRepository repository = prepareMetadata("MultipleOccurrenceComplex.xsd"); //$NON-NLS-1$
        final Storage storage = prepareStorage("ABCD", repository); //$NON-NLS-1$
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        String recordXml = "<C><subelement>1</subelement><A xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"B\"><a>7</a><B>6</B></A><A xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"A\"><a>5</a></A><A xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"B\"><a>4</a><B>3</B></A><Aa xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"B\"><a>2</a><B>1</B></Aa></C>"; //$NON-NLS-1$
        List<DataRecord> records = new LinkedList<DataRecord>();
        records.add(factory.read(repository, repository.getComplexType("C"), recordXml)); //$NON-NLS-1$
        storage.begin();
        storage.update(records);
        storage.commit();

        storage.begin();
        ComplexTypeMetadata c = repository.getComplexType("C"); //$NON-NLS-1$
        UserQueryBuilder qb = from(c);
        qb.start(0);
        qb.limit(1);
        StorageResults results = storage.fetch(qb.getSelect());
        DataRecord result = results.iterator().next();
        DataRecordXmlWriter writer = new DataRecordXmlWriter(c);
        ResettableStringWriter stringWriter = new ResettableStringWriter();
        writer.write(result, stringWriter);
        assertEquals(recordXml, stringWriter.toString());
    }

    public void testDateTimeAsBaseType() throws XmlServerException, ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {
        final MetadataRepository repository = prepareMetadata("CustomDateTime.xsd"); //$NON-NLS-1$
        final Storage storage = prepareStorage("CustomDateTime", repository); //$NON-NLS-1$
        DataRecordReader<String> factory = new XmlStringDataRecordReader();

        List<DataRecord> records = new LinkedList<DataRecord>();
        records.add(factory.read(repository, repository.getComplexType("Employee"), //$NON-NLS-1$
                "<Employee><Id>22</Id><Holiday>2014-04-17T12:00:00</Holiday><birthday>2014-04-16T12:00:00</birthday></Employee>")); //$NON-NLS-1$
        storage.begin();
        storage.update(records);
        storage.commit();

        // Query saved data
        storage.begin();
        ComplexTypeMetadata employee = repository.getComplexType("Employee"); //$NON-NLS-1$
        UserQueryBuilder qb = from(employee);
        qb.start(0);
        qb.limit(1);
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        DataRecord result = results.iterator().next();

        DataRecordXmlWriter writer = new DataRecordXmlWriter(employee);
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
        NodeList holiday = (NodeList) xpath.evaluate("./Holiday", r, XPathConstants.NODESET); //$NON-NLS-1$
        List<String> datetimeStrValues = new ArrayList<String>();
        for (int j = 0; j < holiday.getLength(); j++) {
            datetimeStrValues.add(holiday.item(j).getFirstChild() == null ? "" : holiday.item(j).getFirstChild().getNodeValue()); //$NON-NLS-1$
        }
        assertTrue(datetimeStrValues.contains("2014-04-17T12:00:00")); //$NON-NLS-1$
        stringWriter.reset();

        DataRecordWriter viewWriter = new ViewSearchResultsWriter();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            viewWriter.write(result, output);
        } catch (IOException e) {
            throw new XmlServerException(e);
        }
        String resultsAsString = new String(output.toByteArray(), Charset.forName("UTF-8")); //$NON-NLS-1$
        output.reset();

        xpath = XPathFactory.newInstance().newXPath();
        r = documentBuilder.parse(new InputSource(new StringReader(resultsAsString))).getDocumentElement();
        NodeList birthday = (NodeList) xpath.evaluate("./birthday", r, XPathConstants.NODESET); //$NON-NLS-1$
        datetimeStrValues = new ArrayList<String>();
        for (int j = 0; j < birthday.getLength(); j++) {
            datetimeStrValues
                    .add(birthday.item(j).getFirstChild() == null ? "" : birthday.item(j).getFirstChild().getNodeValue()); //$NON-NLS-1$
        }
        assertTrue(datetimeStrValues.contains("2014-04-16T12:00:00")); //$NON-NLS-1$

        storage.commit();

        final MetadataRepository repository2 = prepareMetadata("CustomDate.xsd"); //$NON-NLS-1$
        final Storage storage2 = prepareStorage("CustomDate", repository2); //$NON-NLS-1$

        factory = new XmlStringDataRecordReader();

        records.clear();
        records.add(factory.read(repository2, repository2.getComplexType("CustomDate"), //$NON-NLS-1$
                "<CustomDate><Id>22</Id><MyDate>2014-04-17</MyDate></CustomDate>")); //$NON-NLS-1$
        storage2.begin();
        storage2.update(records);
        storage2.commit();

        // Query saved data
        storage2.begin();
        ComplexTypeMetadata mydate = repository2.getComplexType("CustomDate"); //$NON-NLS-1$
        qb = from(mydate);
        qb.start(0);
        qb.limit(1);
        results = storage2.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        result = results.iterator().next();
        writer = new DataRecordXmlWriter(mydate);
        try {
            writer.write(result, stringWriter);
        } catch (IOException e) {
            throw new XmlServerException(e);
        }
        recordStringValue = stringWriter.toString();
        xpath = XPathFactory.newInstance().newXPath();

        r = documentBuilder.parse(new InputSource(new StringReader(recordStringValue))).getDocumentElement();
        holiday = (NodeList) xpath.evaluate("./MyDate", r, XPathConstants.NODESET); //$NON-NLS-1$
        List<String> dateStrValues = new ArrayList<String>();
        for (int j = 0; j < holiday.getLength(); j++) {
            dateStrValues.add(holiday.item(j).getFirstChild() == null ? "" : holiday.item(j).getFirstChild().getNodeValue()); //$NON-NLS-1$
        }
        assertTrue(dateStrValues.contains("2014-04-17")); //$NON-NLS-1$
        stringWriter.reset();

        storage2.commit();

    }
    
    // TMDM-10201 Exported file of related entity is empty when entity is inheritance type
    public void testGetItemPKsByCriteria() throws XmlServerException {
        final MetadataRepository repository = prepareMetadata("InheritFKTest.xsd"); //$NON-NLS-1$
        final Storage storage = prepareStorage("InheritFKTest", repository); //$NON-NLS-1$
        DataRecordReader<String> factory = new XmlStringDataRecordReader();

        List<DataRecord> records = new LinkedList<DataRecord>();
        records.add(factory.read(repository, repository.getComplexType("EnumValue"), //$NON-NLS-1$
                "<EnumValue><EnumValID>1</EnumValID><EnumType>aaaa</EnumType><Value>bbbb</Value></EnumValue>")); //$NON-NLS-1$
        records.add(factory.read(repository, repository.getComplexType("Feature"), //$NON-NLS-1$
                "<Feature><FeatureCode>1111</FeatureCode><Name>cccc</Name><Value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"EnumValueType\"><EnumType>aaaa</EnumType><EnumFK>[1]</EnumFK></Value></Feature>")); //$NON-NLS-1$
        storage.begin();
        storage.update(records);
        storage.commit();
        
        
        ItemPKCriteria criteria = new ItemPKCriteria();
        criteria.setClusterName("InheritFKTest"); //$NON-NLS-1$
        criteria.setConceptName("Feature"); //$NON-NLS-1$
        criteria.setContentKeywords(""); //$NON-NLS-1$
        criteria.setKeysKeywords("$Feature/Value/EnumFK,EnumValueType/EnumFK$[1]"); //$NON-NLS-1$
        criteria.setKeys(""); //$NON-NLS-1$
        criteria.setCompoundKeyKeywords(false);
        criteria.setFromDate(-1L);
        criteria.setToDate(-1L);
        criteria.setMaxItems(Integer.MAX_VALUE);
        criteria.setSkip(0);
        criteria.setUseFTSearch(false);
        
        StorageWrapper wrapper = new StorageWrapper() {

            @Override
            protected Storage getStorage(String dataClusterName) {
                return storage;
            }

        };
        List<String> result = wrapper.getItemPKsByCriteria(criteria);
        assertEquals("<totalCount>1</totalCount>", result.get(0)); //$NON-NLS-1$
        assertTrue(result.get(1).contains("<n>Feature</n><ids><i>1111</i>")); //$NON-NLS-1$
    }

    private MetadataRepository prepareMetadata(String xsd) {
        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageWrapperTest.class.getResourceAsStream(xsd));
        return repository;
    }

    private Storage prepareStorage(String name, MetadataRepository repository) {
        Storage storage = new HibernateStorage(name);
        storage.init(ServerContext.INSTANCE.get().getDefinition(StorageTestCase.DATABASE + "-Default", "MDM")); //$NON-NLS-1$//$NON-NLS-2$
        storage.prepare(repository, true);
        return storage;
    }
    
    private StorageWrapper prepareWrapper(String name, String xsd, String[] xmls, String[] ids) throws XmlServerException{
        final MetadataRepository repository = prepareMetadata(xsd);
        final Storage storage = prepareStorage(name, repository);

        StorageWrapper wrapper = new StorageWrapper() {

            @Override
            protected Storage getStorage(String dataClusterName) {
                return storage;
            }
        };
        
        wrapper.start(name);
        {
            for (int i = 0; i < xmls.length; i++) {
                wrapper.putDocumentFromString(xmls[i], ids[i], name, null); 
            }
        }
        wrapper.commit(name);
        return wrapper;
    }
    
    protected static class MockUser extends ILocalUser {

        @Override
        public ILocalUser getILocalUser() throws XtentisException {
            return this;
        }

        @Override
        public HashSet<String> getRoles() {
            HashSet<String> roleSet = new HashSet<String>();
            roleSet.add("Demo_User");
            return roleSet;
        }
    }
}
