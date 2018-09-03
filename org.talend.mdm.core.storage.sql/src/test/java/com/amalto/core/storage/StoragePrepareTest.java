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

import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.query.StorageTestCase;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.MockMetadataRepositoryAdmin;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.MockStorageAdmin;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.adapt.StorageAdaptTest;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;

public class StoragePrepareTest extends TestCase {
    
    protected static MockUserDelegator userSecurity = new MockUserDelegator();
    protected static final String STORAGE_NAME = "Test";
    
    public StoragePrepareTest() {
        ServerContext.INSTANCE.get(new MockServerLifecycle());
    }
    
    private MetadataRepository prepareMetadata(String xsd) {
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream(xsd));
        return repository;
    }

    private Storage prepareStorage(String name, MetadataRepository repository) {
        Storage storage = new HibernateStorage(name);
        storage.init(ServerContext.INSTANCE.get().getDefinition(StorageTestCase.DATABASE + "-Default", "MDM")); //$NON-NLS-1$//$NON-NLS-2$
        storage.prepare(repository, true);
        return storage;
    }
    
    public void test1_CompositeIdAndContainedType() {
        String[] userKeys = { "NumeroBdd", "BddSource", "NomApplication", "IdMDM" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        String[] dbKeys = { "x_numerobdd", "x_bddsource", "x_nomapplication", "x_idmdm" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        MetadataRepository repository = prepareMetadata("StoragePrepare_1.xsd"); //$NON-NLS-1$
        ComplexTypeMetadata userType = repository.getComplexType("XrefAgence"); //$NON-NLS-1$
        // assert user type
        assertNotNull(userType);
        assertEquals(4, userType.getKeyFields().size());
        int i = 0;
        for (FieldMetadata keyField : userType.getKeyFields()) {
            assertEquals(userKeys[i++], keyField.getName());
        }
        // assert database type
        HibernateStorage storage = (HibernateStorage) prepareStorage("Test1", repository); //$NON-NLS-1$
        ComplexTypeMetadata dbType = storage.getTypeEnhancer().getMappings().getMappingFromUser(userType).getDatabase();
        assertNotNull(dbType);
        assertEquals(4, dbType.getKeyFields().size());
        int j = 0;
        for (FieldMetadata keyField : dbType.getKeyFields()) {
            assertEquals(dbKeys[j++], keyField.getName());
        }
    }
    
    // TMDM-8022 custom decimal type totalDigits/fractionDigits is not considered while mapping to RDBMS db
    public void testDecimalComplexType() {
        Storage storage = new SecuredStorage(new HibernateStorage("Goods", StorageType.MASTER), userSecurity);//$NON-NLS-1$
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("GoodsDecimal.xml"));//$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register("Goods", repository);//$NON-NLS-1$

        storage.init(getDatasource("H2-DS3"));//    //$NON-NLS-1$
        storage.prepare(repository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        storage.begin();
        ComplexTypeMetadata goods = repository.getComplexType("Goods");//$NON-NLS-1$
        UserQueryBuilder qb = from(goods);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.end();

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        //scale is less than define, expect result will be enlarge to define scale length
        records.add(factory.read(repository, goods, "<Goods><Id>1</Id><Price>12.00</Price></Goods>")); //$NON-NLS-1$
        //scale is greater than define, expect result will be round to define scale length
        records.add(factory.read(repository, goods, "<Goods><Id>2</Id><Price>12.3588</Price></Goods>")); //$NON-NLS-1$
        records.add(factory.read(repository, goods, "<Goods><Id>3</Id><Price>12.3584</Price></Goods>")); //$NON-NLS-1$
        //for the number,scale is greater than define, expect result will be round to define scale length
        records.add(factory.read(repository, goods, "<Goods><Id>4</Id><Price>-2.02365</Price></Goods>")); //$NON-NLS-1$
        records.add(factory.read(repository, goods, "<Goods><Id>5</Id><Price>-2.0232</Price></Goods>")); //$NON-NLS-1$
        //for the max number and min number, will be insert into correctly.
        records.add(factory.read(repository, goods, "<Goods><Id>6</Id><Price>999999999999.999</Price></Goods>")); //$NON-NLS-1$
        records.add(factory.read(repository, goods, "<Goods><Id>7</Id><Price>-999999999999.999</Price></Goods>")); //$NON-NLS-1$

        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }

        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(7, results.getCount());
            for (DataRecord result : results) {
                if (result.get("Id").equals("1")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("12.000").doubleValue(), ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ //$NON-NLS-2$
                }
                if (result.get("Id").equals("2")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("12.359").doubleValue(), ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ //$NON-NLS-2$
                }
                if (result.get("Id").equals("3")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("12.358").doubleValue(), ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ //$NON-NLS-2$
                }
                if (result.get("Id").equals("4")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("-2.024").doubleValue(), ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ //$NON-NLS-2$
                }
                if (result.get("Id").equals("5")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("-2.023").doubleValue(), ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ //$NON-NLS-2$
                }
                if (result.get("Id").equals("6")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("999999999999.999").doubleValue(),//$NON-NLS-1$ 
                            ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ 
                }
                if (result.get("Id").equals("7")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("-999999999999.999").doubleValue(),//$NON-NLS-1$ 
                            ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ 
                }
            }

        } finally {
            results.close();
        }
        storage.end();

        try {
            storage.begin();
            {
                qb = from(goods);
                storage.delete(qb.getSelect());
            }
            storage.commit();
        } finally {
            storage.end();
        }

        //greater than the max number, failed to insert.
        records.add(factory.read(repository, goods, "<Goods><Id>8</Id><Price>1000000000000.000</Price></Goods>"));//$NON-NLS-1$ 
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
            fail("could not execute batch");//$NON-NLS-1$ 
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        } finally {
            storage.end();
        }

        //less than the min numnber, failed to insert.
        records.add(factory.read(repository, goods, "<Goods><Id>9</Id><Price>-1000000000000.000</Price></Goods>"));//$NON-NLS-1$ 
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
            fail("could not execute batch");//$NON-NLS-1$ 
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        } finally {
            storage.end();
        }

        storage.close();
    }

    public void testStorageAfterFetchClassLoader() {
        Storage storage = new SecuredStorage(new HibernateStorage("Goods", StorageType.MASTER), userSecurity);//$NON-NLS-1$
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("GoodsDecimal.xml"));//$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register("Goods", repository);//$NON-NLS-1$

        storage.init(getDatasource("H2-DS3"));//    //$NON-NLS-1$
        storage.prepare(repository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        storage.begin();
        ComplexTypeMetadata goods = repository.getComplexType("Goods");//$NON-NLS-1$
        UserQueryBuilder qb = from(goods);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
            for (DataRecord result : results) {
                break;
            }
        } finally {
            results.close();
        }
        storage.end();
        ClassLoader StorageClassLoader = (ClassLoader) Thread.currentThread().getContextClassLoader();

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        records.add(factory.read(repository, goods, "<Goods><Id>1</Id><Price>12.00</Price></Goods>")); //$NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }

        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                break;
            }

        } finally {
            results.close();
        }
        storage.end();
        ClassLoader StorageClassLoader2 = (ClassLoader) Thread.currentThread().getContextClassLoader();
        assertEquals(StorageClassLoader, StorageClassLoader2);
    }

    // TMDM-10222
    public void testInClauseHandle() {
        Storage storage = new SecuredStorage(new HibernateStorage("Features", StorageType.MASTER), userSecurity);//$NON-NLS-1$
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("Features.xsd"));//$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register("Features", repository);//$NON-NLS-1$

        storage.init(getDatasource("H2-DS3"));//    //$NON-NLS-1$
        storage.prepare(repository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        ComplexTypeMetadata factFacturesStg = repository.getComplexType("FactFacturesStg");//$NON-NLS-1$
        ComplexTypeMetadata Factures = repository.getComplexType("Factures");//$NON-NLS-1$

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        // scale is less than define, expect result will be enlarge to define scale length
        records.add(factory
                .read(repository,
                        factFacturesStg,
                        "<FactFacturesStg><CodeEntiteFact>1</CodeEntiteFact><CodeServiceFact>1</CodeServiceFact><NoDocument>1</NoDocument><DateDocument>2016-07-20</DateDocument><CycleDocument>1</CycleDocument><OrigineDocument>1</OrigineDocument><FluxDocument>1</FluxDocument><RefTiersPrestation>1</RefTiersPrestation><LignesFacturesStg><IdLigne>1</IdLigne></LignesFacturesStg><TvaFacturesStg><IdLigneTva>1</IdLigneTva></TvaFacturesStg></FactFacturesStg>")); //$NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }

        UserQueryBuilder qb = from(factFacturesStg).where(eq(factFacturesStg.getField("CodeEntiteFact"), "1"));
        qb.getSelect().getPaging().setLimit(10);
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("CodeServiceFact"));//$NON-NLS-1$ //$NON-NLS-2$
            }

        } finally {
            results.close();
        }
        storage.end();

        qb = from(factFacturesStg);
        try {
            storage.delete(qb.getSelect());
            storage.commit();
        } finally {
            storage.close();
        }
    }

    // TMDM-10861
    public void testStagingHasTask() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS2", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.STAGING);
        storage.init(dataSource);
        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageAdaptTest.class.getResourceAsStream("schema1_1.xsd"));
        storage.prepare(repository, true);

        DataSource staging = dataSource.getStaging();
        assertTrue(staging instanceof RDBMSDataSource);
        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) staging;
        assertEquals(RDBMSDataSource.DataSourceDialect.H2, rdbmsDataSource.getDialectName());
        Connection connection = DriverManager.getConnection(rdbmsDataSource.getConnectionURL());
        Statement statement = connection.createStatement();
        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM PRODUCT");
            ResultSetMetaData metaData = resultSet.getMetaData();
            boolean hasTask = false;
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                hasTask |= "x_talend_staging_hastask".equalsIgnoreCase(metaData.getColumnName(i));
            }
            assertTrue(hasTask);
        } finally {
            statement.close();
            connection.close();
        }
    }

    public void testBooleanDefaultValuePrepare() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS2", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("BooleanType.xsd"));
        storage.prepare(repository, true);
        // test table had been created
        String[] tables = { "BooleanType" };
        String[][] columns = {
                { "", "X_ID", "X_A1", "X_A2", "X_A3", "X_A4", "X_A5", "X_A6", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" } };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }
        ComplexTypeMetadata booleanType = repository.getComplexType("BooleanType");//$NON-NLS-1$
        // test data had been added
        List<DataRecord> records = new ArrayList<DataRecord>();
        records.add(factory.read(repository, booleanType, "<BooleanType><Id>123</Id><a2>true</a2><a6>false</a6></BooleanType>")); // $NON-NLS-1$
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
        UserQueryBuilder qb = from(booleanType);
        qb.getSelect().getPaging().setLimit(10);
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("123", result.get("Id"));//$NON-NLS-1$ //$NON-NLS-2$
                assertEquals(true, result.get("a1"));//$NON-NLS-1$ //$NON-NLS-2$
                assertEquals(true, result.get("a2"));//$NON-NLS-1$ //$NON-NLS-2$
                assertEquals(false, result.get("a3"));//$NON-NLS-1$ //$NON-NLS-2$
                assertEquals(true, result.get("a4"));//$NON-NLS-1$ //$NON-NLS-2$
                assertEquals(null, result.get("a5"));//$NON-NLS-1$ //$NON-NLS-2$
                assertEquals(false, result.get("a6"));//$NON-NLS-1$ //$NON-NLS-2$
            }
        } finally {
            results.close();
        }
        storage.end();
        // delte data
        try {
            storage.begin();
            {
                qb = from(booleanType);
                storage.delete(qb.getSelect());
            }
            storage.commit();
        } finally {
            storage.end();
        }
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
}
