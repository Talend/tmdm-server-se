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
package com.amalto.core.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

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
        List<Object[]> parseResult;
        try {
            parseResult = parseResult(item.viewSearch(new DataClusterPOJOPK("DStar"), dtStarViewPOJO, whereItem, null, null, 0, 20));
            assertEquals(1, parseResult.size());
            assertEquals("3", parseResult.get(0)[0]);
            assertEquals("Juste", parseResult.get(0)[1]);
            assertEquals("John", parseResult.get(0)[2]);
            assertEquals("Leblanc_1", parseResult.get(0)[3]);
            assertEquals("my resume", parseResult.get(0)[4]);
        } catch (Exception e) {
            fail("Query failed");
        }

        // 2. one empty condition one result
        conditions = new ArrayList<IWhereItem>();
        conditions.add(new WhereCondition("Person/resume", WhereCondition.EMPTY_NULL, "", "NONE"));
        whereItem = new WhereAnd(conditions);
        try {
            parseResult = parseResult(item.viewSearch(new DataClusterPOJOPK("DStar"), dtStarViewPOJO, whereItem, null, null, 0, 20));
            assertEquals(1, parseResult.size());
            assertEquals("4", parseResult.get(0)[0]);
            assertEquals("Julien", parseResult.get(0)[1]);
            assertEquals("John", parseResult.get(0)[2]);
            assertEquals("Leblanc_2", parseResult.get(0)[3]);
            assertEquals("", parseResult.get(0)[4]);
        } catch (Exception e) {
            fail("Query failed");
        }

        // 3. one euqlas condition() and one empty condition
        conditions.add(new WhereCondition("Person/firstname", WhereCondition.EQUALS, "Juste", "NONE"));
        whereItem = new WhereAnd(conditions);
        try {
            parseResult = parseResult(item.viewSearch(new DataClusterPOJOPK("DStar"), dtStarViewPOJO, whereItem, null, null, 0, 20));
            assertEquals(0, parseResult.size());
        } catch (Exception e) {
            fail("Query failed");
        }

        // 4
        conditions = new ArrayList<IWhereItem>();
        conditions.add(new WhereCondition("Person/resume", WhereCondition.EMPTY_NULL, "", "NONE"));
        conditions.add(new WhereCondition("Person/firstname", WhereCondition.EQUALS, "Julien", "NONE"));
        whereItem = new WhereAnd(conditions);
        try {
            parseResult = parseResult(item.viewSearch(new DataClusterPOJOPK("DStar"), dtStarViewPOJO, whereItem, null, null, 0, 20));
            assertEquals(1, parseResult.size());
            assertEquals("4", parseResult.get(0)[0]);
            assertEquals("Julien", parseResult.get(0)[1]);
            assertEquals("John", parseResult.get(0)[2]);
            assertEquals("Leblanc_2", parseResult.get(0)[3]);
            assertEquals("", parseResult.get(0)[4]);
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
        
        // 6 sort
        allRecords = new LinkedList<DataRecord>();
        allRecords.add(factory.read(repository, person,
                "<Person><id>1</id><score>200000.00</score><lastname>Leblanc_5</lastname><middlename>John1</middlename><firstname>B</firstname><age>30</age><resume>Resume1</resume><Status>Friend</Status></Person>"));
        allRecords.add(factory.read(repository, person,
                "<Person><id>2</id><score>200000.00</score><lastname>Leblanc_4</lastname><middlename>John2</middlename><firstname>D</firstname><age>30</age><resume>Resume2</resume><Status>Friend</Status></Person>"));
        allRecords.add(factory.read(repository, person,
                "<Person><id>3</id><score>200000.00</score><lastname>Leblanc_3</lastname><middlename>John3</middlename><firstname>C</firstname><age>30</age><resume>Resume3</resume><Status>Friend</Status></Person>"));
        allRecords.add(factory.read(repository, person,
                "<Person><id>4</id><score>200000.00</score><lastname>Leblanc_2</lastname><middlename>John4</middlename><firstname>A</firstname><age>30</age><resume>Resume1</resume><Status>Friend</Status></Person>"));
        allRecords.add(factory.read(repository, person,
                "<Person><id>5</id><score>200000.00</score><lastname>Aeblanc_1</lastname><middlename>John5</middlename><firstname>E</firstname><age>30</age><resume>Resume1</resume><Status>Friend</Status></Person>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();
        // 6.1 default sort
        try {
            parseResult = parseResult(item.viewSearch(new DataClusterPOJOPK("DStar"), dtStarViewPOJO, null, null, null, 0, 20));
            assertEquals(5, parseResult.size());
            assertEquals("1", parseResult.get(0)[0]);
            assertEquals("B", parseResult.get(0)[1]);
            assertEquals("Leblanc_5", parseResult.get(0)[3]);
            assertEquals("2", parseResult.get(1)[0]);
            assertEquals("D", parseResult.get(1)[1]);
            assertEquals("Leblanc_4", parseResult.get(1)[3]);
            assertEquals("3", parseResult.get(2)[0]);
            assertEquals("C", parseResult.get(2)[1]);
            assertEquals("Leblanc_3", parseResult.get(2)[3]);
            assertEquals("4", parseResult.get(3)[0]);
            assertEquals("A", parseResult.get(3)[1]);
            assertEquals("Leblanc_2", parseResult.get(3)[3]);
            assertEquals("5", parseResult.get(4)[0]);
            assertEquals("E", parseResult.get(4)[1]);
            assertEquals("Aeblanc_1", parseResult.get(4)[3]);
        } catch (Exception e) {
            fail("Query failed");
        }

        // 6.2 sort by view definition (order by firstname asc)
        try {
            parseResult = parseResult(item.viewSearch(new DataClusterPOJOPK("DStar"), new ViewPOJOPK("Browse_items_Person_"),
                    null, null, null, 0, 20));
            assertEquals(5, parseResult.size());
            assertEquals("4", parseResult.get(0)[0]);
            assertEquals("A", parseResult.get(0)[1]);
            assertEquals("Leblanc_2", parseResult.get(0)[3]);
            assertEquals("1", parseResult.get(1)[0]);
            assertEquals("B", parseResult.get(1)[1]);
            assertEquals("Leblanc_5", parseResult.get(1)[3]);
            assertEquals("3", parseResult.get(2)[0]);
            assertEquals("C", parseResult.get(2)[1]);
            assertEquals("Leblanc_3", parseResult.get(2)[3]);
            assertEquals("2", parseResult.get(3)[0]);
            assertEquals("D", parseResult.get(3)[1]);
            assertEquals("Leblanc_4", parseResult.get(3)[3]);
            assertEquals("5", parseResult.get(4)[0]);
            assertEquals("E", parseResult.get(4)[1]);
            assertEquals("Aeblanc_1", parseResult.get(4)[3]);
        } catch (Exception e) {
            fail("Query failed");
        }

        // 6.3 sort by view definition (order by firstname asc) with filter (lastname CONTAINS 'L')
        try {
            parseResult = parseResult(item.viewSearch(new DataClusterPOJOPK("DStar"),
                    new ViewPOJOPK("Browse_items_Person_Filter"), null, null, null, 0, 20));
            assertEquals(4, parseResult.size());
            assertEquals("4", parseResult.get(0)[0]);
            assertEquals("A", parseResult.get(0)[1]);
            assertEquals("Leblanc_2", parseResult.get(0)[3]);
            assertEquals("1", parseResult.get(1)[0]);
            assertEquals("B", parseResult.get(1)[1]);
            assertEquals("Leblanc_5", parseResult.get(1)[3]);
            assertEquals("3", parseResult.get(2)[0]);
            assertEquals("C", parseResult.get(2)[1]);
            assertEquals("Leblanc_3", parseResult.get(2)[3]);
            assertEquals("2", parseResult.get(3)[0]);
            assertEquals("D", parseResult.get(3)[1]);
            assertEquals("Leblanc_4", parseResult.get(3)[3]);
        } catch (Exception e) {
            fail("Query failed");
        }

        // 6.4 sort by view definition (order by firstname asc) with full text search (Person FULLTEXTSEARCH 'L')
        try {
            parseResult = parseResult(item.viewSearch(new DataClusterPOJOPK("DStar"),
                    new ViewPOJOPK("Browse_items_Person_FullText"), null, null, null, 0, 20));
            assertEquals(4, parseResult.size());
            assertEquals("4", parseResult.get(0)[0]);
            assertEquals("A", parseResult.get(0)[1]);
            assertEquals("Leblanc_2", parseResult.get(0)[3]);
            assertEquals("1", parseResult.get(1)[0]);
            assertEquals("B", parseResult.get(1)[1]);
            assertEquals("Leblanc_5", parseResult.get(1)[3]);
            assertEquals("3", parseResult.get(2)[0]);
            assertEquals("C", parseResult.get(2)[1]);
            assertEquals("Leblanc_3", parseResult.get(2)[3]);
            assertEquals("2", parseResult.get(3)[0]);
            assertEquals("D", parseResult.get(3)[1]);
            assertEquals("Leblanc_4", parseResult.get(3)[3]);
        } catch (Exception e) {
            fail("Query failed");
        }

        // 6.4 sort by ui setting
        try {
            parseResult = parseResult(item.viewSearch(new DataClusterPOJOPK("DStar"),
                    new ViewPOJOPK("Browse_items_Person_FullText"), null, "Person/lastname", "ASC", 0, 20));
            assertEquals(4, parseResult.size());
            assertEquals("4", parseResult.get(0)[0]);
            assertEquals("A", parseResult.get(0)[1]);
            assertEquals("Leblanc_2", parseResult.get(0)[3]);
            assertEquals("3", parseResult.get(1)[0]);
            assertEquals("C", parseResult.get(1)[1]);
            assertEquals("Leblanc_3", parseResult.get(1)[3]);
            assertEquals("2", parseResult.get(2)[0]);
            assertEquals("D", parseResult.get(2)[1]);
            assertEquals("Leblanc_4", parseResult.get(2)[3]);
            assertEquals("1", parseResult.get(3)[0]);
            assertEquals("B", parseResult.get(3)[1]);
            assertEquals("Leblanc_5", parseResult.get(3)[3]);
        } catch (Exception e) {
            fail("Query failed");
        }
    }

    
    /*
     * object[0] = id object[1] = firstname object[2] = middlename obejct[3] = lastname object[4] = resume
     */
    private List<Object[]> parseResult(List<String> resultList) throws Exception {
        List<Object[]> reuslts = new ArrayList<Object[]>();
        for (int i = 1; i < resultList.size(); i++) {
            Object[] parseResult = new Object[5];
            Document doc = Util.parse(resultList.get(i)); //$NON-NLS-1$
            Node nodeItem = (Node) XPathFactory.newInstance().newXPath().evaluate("//result", doc, XPathConstants.NODE); //$NON-NLS-1$
            if (nodeItem != null && nodeItem instanceof Element) {
                NodeList list = nodeItem.getChildNodes();
                Node node = null;
                for (int j = 0; j < list.getLength(); j++) {
                    if (list.item(j) instanceof Element) {
                        node = list.item(j);
                        if (node.getNodeName().equals("id")) {
                            parseResult[0] = node.getTextContent();
                        } else if (node.getNodeName().equals("firstname")) {
                            parseResult[1] = node.getTextContent();
                        } else if (node.getNodeName().equals("middlename")) {
                            parseResult[2] = node.getTextContent();
                        } else if (node.getNodeName().equals("lastname")) {
                            parseResult[3] = node.getTextContent();
                        } else if (node.getNodeName().equals("resume")) {
                            parseResult[4] = node.getTextContent();
                        }
                    }
                }
            }
            reuslts.add(parseResult);
        }
        return reuslts;
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
            if (!viewPOJOPK.toString().endsWith("Person")) {
                viewPOJO.setSortField("Person/firstname");
                viewPOJO.setIsAsc(true);
            }
            ArrayListHolder<String> searchableBusinessElements = new ArrayListHolder<String>();
            searchableBusinessElements.setList(list);
            viewPOJO.setSearchableBusinessElements(searchableBusinessElements);
            viewPOJO.setTransformerPK("");
            viewPOJO.setViewableBusinessElements(searchableBusinessElements);
            ArrayListHolder<IWhereItem> whereConditions = new ArrayListHolder<IWhereItem>();
            if (viewPOJOPK.toString().endsWith("Filter")) {
                WhereCondition whereCondition = new WhereCondition("Person/lastname", "CONTAINS", "L", "NONE");
                whereConditions.getList().add(whereCondition);
            } else if (viewPOJOPK.toString().endsWith("FullText")) {
                WhereCondition whereCondition = new WhereCondition("Person", "FULLTEXTSEARCH", "L", "NONE");
                whereConditions.getList().add(whereCondition);
            }
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
    }
}
