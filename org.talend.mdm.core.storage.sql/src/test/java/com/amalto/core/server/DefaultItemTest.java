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
package com.amalto.core.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.core.delegator.IItemCtrlDelegator;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.view.ViewPOJO;
import com.amalto.core.objects.view.ViewPOJOPK;
import com.amalto.core.server.api.Item;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import com.amalto.core.util.ArrayListHolder;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;


/**
 * created by John on Jan 7, 2016
 * Detailled comment
 *
 */
@SuppressWarnings("nls")
public class DefaultItemTest extends TestCase {
    
    @Override
    public void tearDown() throws Exception {
        ServerContext.INSTANCE.close();
    }

    @Override
    public void setUp() throws Exception {
        ServerContext.INSTANCE.get(new MockServerLifecycle());
    }
    
    public void testCount() throws Exception {
        Server server = ServerContext.INSTANCE.get();
        assertNotNull(server);

        MetadataRepository repository = new MetadataRepository();
        repository.load(DefaultItemTest.class.getResourceAsStream("../query/metadata.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);
                
        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);
        Storage storage = storageAdmin.create("DStar", "DStar", StorageType.MASTER, "H2-DS1");
        assertNotNull(storage);

        ComplexTypeMetadata person = repository.getComplexType("Person");
        assertNotNull(person);
        
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        
        allRecords.add(factory.read(repository, person,
                        "<Person><id>3</id><score>200000.00</score><lastname>Leblanc</lastname><middlename>John</middlename><firstname>Juste</firstname><age>30</age><Status>Friend</Status></Person>"));
        allRecords.add(factory.read(repository, person,
                        "<Person><id>4</id><score>200000.00</score><lastname>Leblanc</lastname><middlename>John</middlename><firstname>Julien</firstname><age>30</age><Status>Friend</Status></Person>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();

        Item item = new DefaultItem();
        long count = item.count(new DataClusterPOJOPK("DStar"), "Person", null, 0);
        assertTrue(count == 2);
        
        ArrayList<IWhereItem> conditions = new ArrayList<IWhereItem>();
        conditions.add(new WhereCondition("Person/firstname",
                WhereCondition.EQUALS, "Juste", "NONE"));
        conditions.add(new WhereCondition("Person/age",
                WhereCondition.EQUALS, "30", "NONE"));
        IWhereItem whereItem = new WhereAnd(conditions);
        count = item.count(new DataClusterPOJOPK("DStar"), "Person", whereItem, 0);
        assertTrue(count == 1);
    }

    public void testViewSearch() {
        Server server = ServerContext.INSTANCE.get();
        assertNotNull(server);

        MetadataRepository repository = new MetadataRepository();
        repository.load(DefaultItemTest.class.getResourceAsStream("../query/metadata.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("DStar", repository);

        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);
        Storage storage = storageAdmin.create("DStar", "DStar", StorageType.MASTER, "H2-DS1");
        assertNotNull(storage);

        ComplexTypeMetadata person = repository.getComplexType("Person");
        assertNotNull(person);

        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();

        allRecords.add(factory.read(repository, person,
                        "<Person><id>3</id><score>200000.00</score><lastname>Leblanc_1</lastname><middlename>John</middlename><firstname>Juste</firstname><age>30</age><resume>my resume</resume><Status>Friend</Status></Person>"));
        allRecords.add(factory.read(repository, person,
                        "<Person><id>4</id><score>200000.00</score><lastname>Leblanc_2</lastname><middlename>John</middlename><firstname>Julien</firstname><age>30</age><Status>Friend</Status></Person>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();

        ViewPOJOPK dtStarViewPOJO = new ViewPOJOPK("Browse_items_Person");

        MockDefaultItemCtrlDelegator item = new MockDefaultItemCtrlDelegator();

        ArrayList<IWhereItem> conditions = new ArrayList<IWhereItem>();
        conditions.add(new WhereCondition("Person/firstname", WhereCondition.EQUALS, "Juste", "NONE"));

        // 1, one equals condition
        IWhereItem whereItem = new WhereAnd(conditions);
        List<String> queryResult;
        Object[] parseResult;
        try {
            queryResult = item.viewSearch(new DataClusterPOJOPK("DStar"), dtStarViewPOJO, whereItem, null, null, 0, 20);
            parseResult = parseResult(queryResult.toArray(new String[queryResult.size()]));
            assertEquals(1, parseResult[0]);
            assertEquals("3", parseResult[1]);
            assertEquals("Juste", parseResult[2]);
            assertEquals("John", parseResult[3]);
            assertEquals("Leblanc_1", parseResult[4]);
            assertEquals("my resume", parseResult[5]);
        } catch (Exception e) {
            fail("Query failed");
        }

        // 2. one empty condition one result
        conditions = new ArrayList<IWhereItem>();
        conditions.add(new WhereCondition("Person/resume", WhereCondition.EMPTY_NULL, "", "NONE"));

        whereItem = new WhereAnd(conditions);
        try {
            queryResult = item.viewSearch(new DataClusterPOJOPK("DStar"), dtStarViewPOJO, whereItem, null, null, 0, 20);
            parseResult = parseResult(queryResult.toArray(new String[queryResult.size()]));
            assertEquals(1, parseResult[0]);
            assertEquals("4", parseResult[1]);
            assertEquals("Julien", parseResult[2]);
            assertEquals("John", parseResult[3]);
            assertEquals("Leblanc_2", parseResult[4]);
            assertEquals("", parseResult[5]);
        } catch (Exception e) {
            fail("Query failed");
        }

        // 3. one euqlas condition() and one empty condition
        conditions.add(new WhereCondition("Person/firstname", WhereCondition.EQUALS, "Juste", "NONE"));
        whereItem = new WhereAnd(conditions);
        try {
            queryResult = item.viewSearch(new DataClusterPOJOPK("DStar"), dtStarViewPOJO, whereItem, null, null, 0, 20);
            parseResult = parseResult(queryResult.toArray(new String[queryResult.size()]));
            assertEquals(0, parseResult[0]);
        } catch (Exception e) {
            fail("Query failed");
        }

        // 4
        conditions = new ArrayList<IWhereItem>();
        conditions.add(new WhereCondition("Person/resume", WhereCondition.EMPTY_NULL, "", "NONE"));
        conditions.add(new WhereCondition("Person/firstname", WhereCondition.EQUALS, "Julien", "NONE"));
        whereItem = new WhereAnd(conditions);
        try {
            queryResult = item.viewSearch(new DataClusterPOJOPK("DStar"), dtStarViewPOJO, whereItem, null, null, 0, 20);
            parseResult = parseResult(queryResult.toArray(new String[queryResult.size()]));
            assertEquals(1, parseResult[0]);
            assertEquals("4", parseResult[1]);
            assertEquals("Julien", parseResult[2]);
            assertEquals("John", parseResult[3]);
            assertEquals("Leblanc_2", parseResult[4]);
            assertEquals("", parseResult[5]);
        } catch (Exception e) {
            fail("Query failed");
        }

        // 5 full text search
        conditions = new ArrayList<IWhereItem>();
        conditions.add(new WhereCondition("Person/resume", WhereCondition.EMPTY_NULL, "", "NONE"));
        conditions.add(new WhereCondition("Person/firstname", WhereCondition.FULLTEXTSEARCH, "Julien", "NONE"));
        whereItem = new WhereAnd(conditions);
        Throwable t = null;
        try {
            queryResult = item.viewSearch(new DataClusterPOJOPK("DStar"), dtStarViewPOJO, whereItem, null, null, 0, 20);
            fail("Can't together use the full text search and Empty Null to search.");
        } catch (Exception e) {
            t = e;
        }
        assertNotNull(t);
        assertTrue(t instanceof com.amalto.core.util.XtentisException);
        assertTrue(t.getMessage().startsWith("Unable to single search:"));
    }

    /*
     * object[0] = total Size object[1] = id object[2] = firstname object[3] = middlename obejct[4] = lastname object[5]
     * = resume
     */
    private Object[] parseResult(String[] results) throws Exception {
        Object[] parseResult = new Object[6];
        for (int i = 0; i < results.length; i++) {
            if (i == 0) {
                parseResult[0] = Integer.parseInt(Util.parse(results[i].toString()).getDocumentElement().getTextContent());
                continue;
            }
            Document doc = Util.parse(results[i]); //$NON-NLS-1$

            Node nodeItem = (Node) XPathFactory.newInstance().newXPath().evaluate("//result", doc, XPathConstants.NODE); //$NON-NLS-1$
            if (nodeItem != null && nodeItem instanceof Element) {
                NodeList list = nodeItem.getChildNodes();
                Node node = null;
                for (int j = 0; j < list.getLength(); j++) {
                    if (list.item(j) instanceof Element) {
                        node = list.item(j);
                        if (node.getNodeName().equals("id")) {
                            parseResult[1] = node.getTextContent();
                        } else if (node.getNodeName().equals("firstname")) {
                            parseResult[2] = node.getTextContent();
                        } else if (node.getNodeName().equals("middlename")) {
                            parseResult[3] = node.getTextContent();
                        } else if (node.getNodeName().equals("lastname")) {
                            parseResult[4] = node.getTextContent();
                        } else if (node.getNodeName().equals("resume")) {
                            parseResult[5] = node.getTextContent();
                        }
                    }
                }
            }
        }
        return parseResult;
    }

    class MockDefaultItemCtrlDelegator extends IItemCtrlDelegator {

        @Override
        public ViewPOJO getViewPOJO(ViewPOJOPK viewPOJOPK) throws Exception {
            ViewPOJO viewPOJO = new ViewPOJO("Browse_items_Person");
            viewPOJO.setDescription("[EN:Person]");
            viewPOJO.setDigest("450c0ab04bf5a390fbe72c045ecec8ac");
            viewPOJO.setTransformerActive(false);
            viewPOJO.setName("Browse_items_Person");
            ArrayList<String> list = new ArrayList<String>();
            list.add("Person/id");
            list.add("Person/firstname");
            list.add("Person/middlename");
            list.add("Person/lastname");
            list.add("Person/resume");
            ArrayListHolder<String> searchableBusinessElements = new ArrayListHolder<String>();
            searchableBusinessElements.setList(list);
            viewPOJO.setSearchableBusinessElements(searchableBusinessElements);
            viewPOJO.setTransformerPK("");
            viewPOJO.setViewableBusinessElements(searchableBusinessElements);
            ArrayListHolder<IWhereItem> whereConditions = new ArrayListHolder<IWhereItem>();
            viewPOJO.setWhereConditions(whereConditions);
            return viewPOJO;
        }

        @Override
        public ILocalUser getLocalUser() throws XtentisException {
            return new MockAdmin();
        }
    }

    protected static class MockAdmin extends ILocalUser {

        @Override
        public ILocalUser getILocalUser() throws XtentisException {
            return this;
        }

        @Override
        public HashSet<String> getRoles() {
            HashSet<String> roleSet = new HashSet<String>();
            // roleSet.add("System_Admin");
            // roleSet.add("administration");
            return roleSet;
        }

        @Override
        public String getUsername() {
            return "administrator";
        }

        @Override
        public boolean isAdmin(Class<?> objectTypeClass) throws XtentisException {
            return true;
        }

        @Override
        public String getUserXML() {
            return StringUtils.EMPTY;
        }
    }
}
