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
import static com.amalto.core.query.user.UserQueryBuilder.fullText;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StoragePrepareTest;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class IndexAssociatedEntityQueryTest extends TestCase {

    protected static MockUserDelegator userSecurity = new MockUserDelegator();

    protected static final String STORAGE_NAME = "Test";

    public IndexAssociatedEntityQueryTest() {
        ServerContext.INSTANCE.get(new MockServerLifecycle());
    }

    public void test1() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS2", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("ProductAttribute.xsd"));
        storage.prepare(repository, true);

        // test table had been created
        String[] tables = { "PRODUCT", "X_ATTRIBUTESLIST_T", "X_ATTRIBUTEITEM_T" };
        String[][] columns = {
                { "", "X_PRODUCTID", "X_NAME", "X_CATEGORYCODE", "X_ATTRIBUTESLIST_X_TALEND_ID", "X_TALEND_TIMESTAMP",
                        "X_TALEND_TASK_ID" },
                { "", "X_TALEND_ID" }, { "", "X_TALEND_ID ", "X_NAME", "X_VALUE ", "X_ATTRIBUTEITEM_T_X_TALEND_ID", "POS" } };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true, true, true });
        } catch (SQLException e) {
            assertNull(e);
        }
        ComplexTypeMetadata product = repository.getComplexType("product");
        // test data had been added
        List<DataRecord> records = new ArrayList<DataRecord>();
        records.add(factory.read(repository, product,
                "<product><productId>id1</productId><name>name1</name><categoryCode>234</categoryCode><attributesList><attrItem><name>111</name><value>1111</value></attrItem><attrItem><name>222</name><value>2222</value></attrItem></attributesList></product>"));
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } catch (Exception e) {
            fail("Faield to insert data");
        } finally {
            storage.end();
        }
        // test query data
        UserQueryBuilder qb = from(product);
        qb.getSelect().getPaging().setLimit(10);
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("name1", result.get("name"));
            }
        } finally {
            results.close();
        }

        UserQueryBuilder qb1 = from(product).select(product.getField("attributesList/attrItem/name")).where(fullText("111"));
        StorageResults results1 = storage.fetch(qb1.getSelect());
        try {
            assertEquals(1, results1.getCount());
            for (DataRecord result : results) {
                assertEquals("name1", result.get("name"));
            }
        } finally {
            results.close();
        }
        storage.end();
    }

    private void assertDatabaseChange(DataSourceDefinition dataSource, String[] tables, String[][] columns, boolean[] exists)
            throws SQLException {
        DataSource master = dataSource.getMaster();
        assertTrue(master instanceof RDBMSDataSource);
        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) master;
        assertEquals(RDBMSDataSource.DataSourceDialect.H2, rdbmsDataSource.getDialectName());
        Connection connection = DriverManager.getConnection(rdbmsDataSource.getConnectionURL(), rdbmsDataSource.getUserName(),
                rdbmsDataSource.getPassword());
        Statement statement = connection.createStatement();
        try {
            for (int i = 0; i < tables.length; i++) {
                ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tables[i]);
                ResultSetMetaData metaData = resultSet.getMetaData();
                boolean hasField = false;
                for (int j = 1; j <= metaData.getColumnCount(); j++) {
                    hasField |= columns[i][j].equalsIgnoreCase(metaData.getColumnName(j));
                }
                assertSame(exists[i], hasField);
            }
        } finally {
            statement.close();
            connection.close();
        }
    }

    protected static DataSourceDefinition getDatasource(String dataSourceName) {
        return ServerContext.INSTANCE.get().getDefinition(dataSourceName, "MDM");
    }

    protected static class MockUserDelegator implements SecuredStorage.UserDelegator {

        boolean isActive = true;

        public void setActive(boolean active) {
            isActive = active;
        }

        @Override
        public boolean hide(FieldMetadata field) {
            return isActive && field.getHideUsers().contains("System_Users");
        }

        @Override
        public boolean hide(ComplexTypeMetadata type) {
            return isActive && type.getHideUsers().contains("System_Users");
        }
    }

    @SuppressWarnings("unchecked")
    public void testForCyclicDependencyEntites() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS2", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("ProductAttribute_2.xsd"));
        storage.prepare(repository, true);

        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        ComplexTypeMetadata product = repository.getComplexType("Product");
        ComplexTypeMetadata storeItem = repository.getComplexType("StoreItem");
        List<DataRecord> records = new ArrayList<DataRecord>();
        records.add(factory.read(repository, product, "<Product><Id>11</Id><Name>name11</Name></Product>"));
        records.add(factory.read(repository, storeItem,
                "<StoreItem><StoreItemId>22</StoreItemId><StoreItemName>name22</StoreItemName><ProductId>[11]</ProductId></StoreItem>"));
        records.add(factory.read(repository, storeItem,
                "<StoreItem><StoreItemId>33</StoreItemId><StoreItemName>name33</StoreItemName><ProductId>[11]</ProductId></StoreItem>"));
        records.add(factory.read(repository, product,
                "<Product><Id>11</Id><Name>name11</Name><Items>[22]</Items><Items>[33]</Items></Product>"));
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } catch (Exception e) {
            fail("Faield to insert data");
        } finally {
            storage.end();
        }
        // test query data
        UserQueryBuilder qb = from(product).where(fullText("11"));
        qb.getSelect().getPaging().setLimit(10);
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("name11", result.get("Name"));
                assertEquals("22", ((List<DataRecord>) result.get("Items")).get(0).get("StoreItemId"));
                assertEquals("33", ((List<DataRecord>) result.get("Items")).get(1).get("StoreItemId"));
            }
        } finally {
            results.close();
        }

        UserQueryBuilder qb1 = from(storeItem).select(storeItem.getField("StoreItemName")).where(fullText("name"));
        StorageResults results1 = storage.fetch(qb1.getSelect());
        try {
            assertEquals(2, results1.getCount());
            Spliterator<DataRecord> splitResults = results1.spliterator();
            splitResults.forEachRemaining((DataRecord record) -> assertNotNull(record.get("StoreItemName")));
        } finally {
            results.close();
        }
        storage.end();
    }

    public void testForCyclicDependencyComplexType1() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS2", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository repository = new MetadataRepository();
        try {
            repository.load(StoragePrepareTest.class.getResourceAsStream("CyclicComplexType_1.xsd"));
            fail();
        } catch (Exception e) {
        } finally {
            storage.end();
        }
    }

    public void testForCyclicDependencyComplexType2() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS2", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository repository = new MetadataRepository();
        try {
            repository.load(StoragePrepareTest.class.getResourceAsStream("CyclicComplexType_2.xsd"));
            fail();
        } catch (Exception e) {
        } finally {
            storage.end();
        }
    }
}