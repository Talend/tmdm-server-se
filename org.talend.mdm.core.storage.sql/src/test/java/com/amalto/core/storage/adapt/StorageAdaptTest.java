/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.adapt;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.h2.jdbc.JdbcSQLException;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.MetadataUtils.SortType;

import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;

public class StorageAdaptTest extends TestCase {

    protected static final String STORAGE_NAME = "Test";

    @Override
    public void setUp() throws Exception {
        ServerContext.INSTANCE.get(new MockServerLifecycle());
    }

    public void testParameter() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageAdaptTest.class.getResourceAsStream("schema1_1.xsd"));
        storage.prepare(repository, true);
        try {
            storage.adapt(null, true);
            fail("Null is not supposed to be accepted as parameter for adapt()");
        } catch (IllegalArgumentException e) {
            // Expected
        } finally {
            storage.close(true);
        }
    }

    public void testNoUpdate() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageAdaptTest.class.getResourceAsStream("schema1_1.xsd"));
        storage.prepare(repository, true);
        performAssert1(dataSource, true, true);
        storage.adapt(storage.getMetadataRepository(), true);
        performAssert1(dataSource, true, true);
        storage.close(true);
    }

    public void test1() throws Exception {
        // Test preparation
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageAdaptTest.class.getResourceAsStream("schema1_1.xsd"));
        storage.prepare(repository, true);
        // Ensure name column exists
        performAssert1(dataSource, true, true);
        // Actual test
        MetadataRepository newRepository = new MetadataRepository();
        newRepository.load(StorageAdaptTest.class.getResourceAsStream("schema1_2.xsd"));
        storage.adapt(newRepository, true);
        // Test expected schema update
        performAssert1(dataSource, false, false);
        storage.close(true);
    }

    private void performAssert1(DataSourceDefinition dataSource, boolean expectNameColumn, boolean expectSupplierTable)
            throws SQLException {
        DataSource master = dataSource.getMaster();
        assertTrue(master instanceof RDBMSDataSource);
        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) master;
        assertEquals(RDBMSDataSource.DataSourceDialect.H2, rdbmsDataSource.getDialectName());
        Connection connection = DriverManager.getConnection(rdbmsDataSource.getConnectionURL());
        Statement statement = connection.createStatement();
        try {
            // Product asserts
            ResultSet resultSet = statement.executeQuery("SELECT * FROM PRODUCT");
            ResultSetMetaData metaData = resultSet.getMetaData();
            boolean hasName = false;
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                hasName |= "x_name".equalsIgnoreCase(metaData.getColumnName(i));
            }
            assertSame(expectNameColumn, hasName);
            // Supplier asserts
            try {
                statement.executeQuery("SELECT * FROM SUPPLIER");
            } catch (SQLException e) {
                if (!expectSupplierTable) {
                    JdbcSQLException h2Exception = (JdbcSQLException) e;
                    assertEquals(42102, h2Exception.getErrorCode()); // 42102 is "table or view not found" error code
                                                                     // for H2.
                } else {
                    fail("Expected statement to succeed (table exist).");
                }
            }
            File ftDirectory = new File(rdbmsDataSource.getIndexDirectory() + '/' + STORAGE_NAME + "/org.talend.mdm.storage.hibernate.Supplier");
            if (!expectSupplierTable) {
                assertTrue(ftDirectory.list()[0].equalsIgnoreCase("delete.Supplier"));
            } else {
                assertTrue(ftDirectory.exists());
            }
        } finally {
            statement.close();
            connection.close();
        }
    }

    public void test2() throws Exception {
        // Test preparation
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageAdaptTest.class.getResourceAsStream("schema2_1.xsd"));
        storage.prepare(repository, true);
        // Ensure name column exists
        performAssert2(dataSource, true);
        // Actual test
        MetadataRepository newRepository = new MetadataRepository();
        newRepository.load(StorageAdaptTest.class.getResourceAsStream("schema2_2.xsd"));
        storage.adapt(newRepository, true);
        // Test expected schema update
        performAssert2(dataSource, false);
        storage.close(true);
    }

    private void performAssert2(DataSourceDefinition dataSource, boolean roadElement)
            throws SQLException {
        DataSource master = dataSource.getMaster();
        assertTrue(master instanceof RDBMSDataSource);
        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) master;
        assertEquals(RDBMSDataSource.DataSourceDialect.H2, rdbmsDataSource.getDialectName());
        Connection connection = DriverManager.getConnection(rdbmsDataSource.getConnectionURL());
        Statement statement = connection.createStatement();
        try {
            // Chinese address table asserts
            ResultSet resultSet = statement.executeQuery("SELECT * FROM X_ChineseAddress");
            ResultSetMetaData metaData = resultSet.getMetaData();
            boolean hasRoad = false;
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                hasRoad |= "x_road".equalsIgnoreCase(metaData.getColumnName(i));
            }
            assertSame(roadElement, hasRoad);
        } finally {
            statement.close();
            connection.close();
        }
    }
    
    public void test3_DeleteMandatoryField_Nested() throws Exception { 
        // Test preparation
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "TestEntity_Nested" };
        String[] tables = { "testentity_nested", "x_nestedtype2", "x_nestedtype3" };
        String[] columns = { "x_mandatory12", "x_mandatory22", "x_mandatory32" };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema3_1.xsd"));
        storage.prepare(repository1, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {true, true, true});
        String input1 = "<TestEntity_Nested>" +
                       "  <id>1</id>"+
                       "  <mandatory11>m11</mandatory11>"+
                       "  <mandatory12>m12</mandatory12>"+
                       "  <nestedType2>"+
                       "    <mandatory21>m21</mandatory21>"+
                       "    <mandatory22>m22</mandatory22>"+
                       "    <nestedType3>"+
                       "      <mandatory31>m31</mandatory31>"+
                       "      <mandatory32>m32</mandatory32>"+
                       "    </nestedType3>"+
                       "  </nestedType2>"+
                       "</TestEntity_Nested>";
        createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
 
        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema3_2.xsd"));
        storage.adapt(repository2, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, true, true});
        String input2 = "<TestEntity_Nested>" +
                        "  <id>1</id>"+
                        "  <mandatory11>m11</mandatory11>"+
                        "  <nestedType2>"+
                        "    <mandatory21>m21</mandatory21>"+
                        "    <mandatory22>m22</mandatory22>"+
                        "    <nestedType3>"+
                        "      <mandatory31>m31</mandatory31>"+
                        "      <mandatory32>m32</mandatory32>"+
                        "    </nestedType3>"+
                        "  </nestedType2>"+
                        "</TestEntity_Nested>";
        createRecord(storage, factory, repository2, typeNames, new String[] { input2 });
             
        MetadataRepository repository3 = new MetadataRepository();
        repository3.load(StorageAdaptTest.class.getResourceAsStream("schema3_3.xsd"));
        storage.adapt(repository3, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, true});
        String input3 = "<TestEntity_Nested>" +
                        "  <id>1</id>"+
                        "  <mandatory11>m11</mandatory11>"+
                        "  <nestedType2>"+
                        "    <mandatory21>m21</mandatory21>"+
                        "    <nestedType3>"+
                        "      <mandatory31>m31</mandatory31>"+
                        "      <mandatory32>m32</mandatory32>"+
                        "    </nestedType3>"+
                        "  </nestedType2>"+
                        "</TestEntity_Nested>";
        createRecord(storage, factory, repository3, typeNames, new String[] { input3 });
        
        MetadataRepository repository4 = new MetadataRepository();
        repository4.load(StorageAdaptTest.class.getResourceAsStream("schema3_4.xsd"));
        storage.adapt(repository4, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, false});
        String input4 = "<TestEntity_Nested>" +
                        "  <id>1</id>"+
                        "  <mandatory11>m11</mandatory11>"+
                        "  <nestedType2>"+
                        "    <mandatory21>m21</mandatory21>"+
                        "    <nestedType3>"+
                        "      <mandatory31>m31</mandatory31>"+
                        "    </nestedType3>"+
                        "  </nestedType2>"+
                        "</TestEntity_Nested>";
        createRecord(storage, factory, repository4, typeNames, new String[] { input4 });
        
        storage.close(true);
    }
    
    public void test4_DeleteMandatoryField_Extend() throws Exception { 
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "TestEntity1_Extend", "TestEntity2_Extend", "TestEntity3_Extend" };
        String[] tables = { "TestEntity1_Extend", "TestEntity2_Extend", "TestEntity3_Extend" };
        String[] columns = { "x_mandatory12", "x_mandatory22", "x_mandatory32" };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema4_1.xsd"));
        storage.prepare(repository1, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {true, true, true});
        String input1_1 = "<TestEntity1_Extend><id>a</id><mandatory11>a</mandatory11><mandatory12>a</mandatory12></TestEntity1_Extend>";
        String input1_2 = "<TestEntity2_Extend><id>b</id><mandatory11>b</mandatory11><mandatory12>b</mandatory12><mandatory21>b</mandatory21><mandatory22>b</mandatory22></TestEntity2_Extend>";
        String input1_3 = "<TestEntity3_Extend><id>c</id><mandatory11>c</mandatory11><mandatory12>c</mandatory12><mandatory21>c</mandatory21><mandatory22>b</mandatory22><mandatory31>c</mandatory31><mandatory32>c</mandatory32></TestEntity3_Extend>";
        createRecord(storage, factory, repository1, typeNames, new String[] { input1_1, input1_2, input1_3 });
        
        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema4_2.xsd"));
        storage.adapt(repository2, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, true, true});
        String input2_1 = "<TestEntity1_Extend><id>a</id><mandatory11>a</mandatory11></TestEntity1_Extend>";
        String input2_2 = "<TestEntity2_Extend><id>b</id><mandatory11>b</mandatory11><mandatory21>b</mandatory21><mandatory22>b</mandatory22></TestEntity2_Extend>";
        String input2_3 = "<TestEntity3_Extend><id>c</id><mandatory11>c</mandatory11><mandatory21>c</mandatory21><mandatory22>b</mandatory22><mandatory31>c</mandatory31><mandatory32>c</mandatory32></TestEntity3_Extend>";
        createRecord(storage, factory, repository2, typeNames, new String[] { input2_1, input2_2, input2_3 });
        
        MetadataRepository repository3 = new MetadataRepository();
        repository3.load(StorageAdaptTest.class.getResourceAsStream("schema4_3.xsd"));
        storage.adapt(repository3, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, true});
        String input3_1 = "<TestEntity1_Extend><id>a</id><mandatory11>a</mandatory11></TestEntity1_Extend>";
        String input3_2 = "<TestEntity2_Extend><id>b</id><mandatory11>b</mandatory11><mandatory21>b</mandatory21></TestEntity2_Extend>";
        String input3_3 = "<TestEntity3_Extend><id>c</id><mandatory11>c</mandatory11><mandatory21>c</mandatory21><mandatory31>c</mandatory31><mandatory32>c</mandatory32></TestEntity3_Extend>";
        createRecord(storage, factory, repository3, typeNames, new String[] { input3_1, input3_2, input3_3 });
        
        MetadataRepository repository4 = new MetadataRepository();
        repository4.load(StorageAdaptTest.class.getResourceAsStream("schema4_4.xsd"));
        storage.adapt(repository4, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, false});
        String input4_1 = "<TestEntity1_Extend><id>a</id><mandatory11>a</mandatory11></TestEntity1_Extend>";
        String input4_2 = "<TestEntity2_Extend><id>b</id><mandatory11>b</mandatory11><mandatory21>b</mandatory21></TestEntity2_Extend>";
        String input4_3 = "<TestEntity3_Extend><id>c</id><mandatory11>c</mandatory11><mandatory21>c</mandatory21><mandatory31>c</mandatory31></TestEntity3_Extend>";
        createRecord(storage, factory, repository3, typeNames, new String[] { input4_1, input4_2, input4_3 });
        
        storage.close(true);
    }
    
    public void test5_DeleteMandatoryField_Reference() throws Exception { 
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "TestEntity5_Reference", "TestEntity4_Reference", "TestEntity3_Reference", "TestEntity2_Reference", "TestEntity1_Reference" };
        String[] tables = { "testentity1_reference", "testentity2_reference", "testentity3_reference", "testentity4_reference", "testentity5_reference" };
        String[] columns = { "x_mandatory12", "x_mandatory22", "x_mandatory32", "x_mandatory42", "x_mandatory52" };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema5_1.xsd"));
        storage.prepare(repository1, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {true, true, true, true, true});
        String input_5 = "<TestEntity5_Reference><id>5</id><mandatory51>m51</mandatory51><mandatory52>m52</mandatory52></TestEntity5_Reference>";
        String input_4 = "<TestEntity4_Reference><id>4</id><mandatory41>m41</mandatory41><mandatory42>m42</mandatory42></TestEntity4_Reference>";
        String input_3 = "<TestEntity3_Reference><id>3</id><mandatory31>m31</mandatory31><mandatory32>m32</mandatory32><testEntity4>[4]</testEntity4><testEntity5>[5]</testEntity5></TestEntity3_Reference>";
        String input_2 = "<TestEntity2_Reference><id>2</id><mandatory21>m21</mandatory21><mandatory22>m22</mandatory22><testEntity4>[4]</testEntity4><testEntity5>[5]</testEntity5></TestEntity2_Reference>";
        String input_1 = "<TestEntity1_Reference><id>1</id><mandatory11>m11</mandatory11><mandatory12>m12</mandatory12><testEntity2>[2]</testEntity2><testEntity3>[3]</testEntity3></TestEntity1_Reference>";
        createRecord(storage, factory, repository1, typeNames, new String[] { input_5, input_4, input_3, input_2, input_1 });
               
        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema5_2.xsd"));
        storage.adapt(repository2, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, true, true, true, true});
        input_1 = "<TestEntity1_Reference><id>1</id><mandatory11>m11</mandatory11><testEntity2>[2]</testEntity2><testEntity3>[3]</testEntity3></TestEntity1_Reference>";
        createRecord(storage, factory, repository2, typeNames, new String[] { input_5, input_4, input_3, input_2, input_1 });
        
        MetadataRepository repository3 = new MetadataRepository();
        repository3.load(StorageAdaptTest.class.getResourceAsStream("schema5_3.xsd"));
        storage.adapt(repository3, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, true, true, true});
        input_2 = "<TestEntity2_Reference><id>2</id><mandatory21>m21</mandatory21><testEntity4>[4]</testEntity4><testEntity5>[5]</testEntity5></TestEntity2_Reference>";
        createRecord(storage, factory, repository3, typeNames, new String[] { input_5, input_4, input_3, input_2, input_1 });
        
        MetadataRepository repository4 = new MetadataRepository();
        repository4.load(StorageAdaptTest.class.getResourceAsStream("schema5_4.xsd"));
        storage.adapt(repository4, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, false, true, true});
        input_3 = "<TestEntity3_Reference><id>3</id><mandatory31>m31</mandatory31><testEntity4>[4]</testEntity4><testEntity5>[5]</testEntity5></TestEntity3_Reference>";
        createRecord(storage, factory, repository4, typeNames, new String[] { input_5, input_4, input_3, input_2, input_1 });
        
        MetadataRepository repository5 = new MetadataRepository();
        repository5.load(StorageAdaptTest.class.getResourceAsStream("schema5_5.xsd"));
        storage.adapt(repository5, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, false, false, true});
        input_4 = "<TestEntity4_Reference><id>4</id><mandatory41>m41</mandatory41></TestEntity4_Reference>";
        createRecord(storage, factory, repository5, typeNames, new String[] { input_5, input_4, input_3, input_2, input_1 });
        
        MetadataRepository repository6 = new MetadataRepository();
        repository6.load(StorageAdaptTest.class.getResourceAsStream("schema5_6.xsd"));
        storage.adapt(repository6, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, false, false, false});
        input_5 = "<TestEntity5_Reference><id>5</id><mandatory51>m51</mandatory51></TestEntity5_Reference>";
        createRecord(storage, factory, repository6, typeNames, new String[] { input_5, input_4, input_3, input_2, input_1 });
        
        storage.close(true);
    }
    
    // TMDM-9376 Open RCEnt container cause studio freeze. In RCEnt data model, two entities have FK refer to each other
    // and one of the entity has another FK refers to itself.
    public void testFKReferToItself() throws Exception {
        // Test preparation
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage(STORAGE_NAME, StorageType.MASTER);
        storage.init(dataSource);

        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema6_1.xsd"));
        storage.prepare(repository1, true);

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema6_2.xsd"));
        storage.adapt(repository2, true);

        MetadataRepository repository = storage.getMetadataRepository();
        assertEquals(repository2, repository);
        storage.close(true);
    }
    
    // TMDM-9401 redeploy datamodel failed after adding a mandatory field
    public void testAddMandatoryField() throws Exception {
        // Test preparation
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage(STORAGE_NAME, StorageType.MASTER);
        storage.init(dataSource);

        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema7_1.xsd"));
        storage.prepare(repository1, true);

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema7_2.xsd"));
        storage.adapt(repository2, true);

        MetadataRepository repository = storage.getMetadataRepository();
        assertEquals(repository2, repository);
        storage.close(true);
    }

    // TMDM-9644 Recreate tables of entity dose not delete the FK values dependency to it and the journal records are deleted.
    public void testFindTablesToDrop() throws Exception {
        // Test preparation
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage(STORAGE_NAME, StorageType.MASTER);
        storage.init(dataSource);

        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageAdaptTest.class.getResourceAsStream("AOP.xsd"));
        storage.prepare(repository, true);
        // Drop Type Composition
        ComplexTypeMetadata composition = repository.getComplexType("Composition");
        Set<ComplexTypeMetadata> typesToDrop = new HashSet<ComplexTypeMetadata>();
        typesToDrop.add(composition);
        // Find all dependencies of Composition
        Set<ComplexTypeMetadata> allDependencies = new HashSet<ComplexTypeMetadata>();
        allDependencies.addAll(typesToDrop);
        Method findDependentTypesToDelete = storage.getClass().getDeclaredMethod("findDependentTypesToDelete",
                new Class[] { MetadataRepository.class, Set.class, Set.class });
        findDependentTypesToDelete.setAccessible(true);
        Object[] args = { repository, typesToDrop, allDependencies };
        Set<ComplexTypeMetadata> dependentTypesToDrop = (Set<ComplexTypeMetadata>) findDependentTypesToDelete.invoke(storage,
                args);
        typesToDrop.addAll(dependentTypesToDrop);
        // Sort types
        List<ComplexTypeMetadata> sortedTypesToDrop = new ArrayList<ComplexTypeMetadata>(typesToDrop);
        sortedTypesToDrop = MetadataUtils.sortTypes(repository, sortedTypesToDrop, SortType.LENIENT);
        // Find tables to drop
        Method findTablesToDrop = storage.getClass().getDeclaredMethod("findTablesToDrop", new Class[] { List.class });
        findTablesToDrop.setAccessible(true);
        Object[] args1 = { sortedTypesToDrop };
        Set<String> tables = (Set<String>) findTablesToDrop.invoke(storage, args1);
        assertTrue(tables.contains("X_CompatibilityType"));
        storage.close(true);
    }

    private void assertDatabaseChange(DataSourceDefinition dataSource, String[] tables, String[] columns, boolean[] exists)
            throws SQLException {
        DataSource master = dataSource.getMaster();
        assertTrue(master instanceof RDBMSDataSource);
        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) master;
        assertEquals(RDBMSDataSource.DataSourceDialect.H2, rdbmsDataSource.getDialectName());
        Connection connection = DriverManager.getConnection(rdbmsDataSource.getConnectionURL());
        Statement statement = connection.createStatement();
        try {
            for (int i = 0; i < tables.length; i++) {
                ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tables[i]);
                ResultSetMetaData metaData = resultSet.getMetaData();
                boolean hasField = false;
                for (int j = 1; j <= metaData.getColumnCount(); j++) {
                    hasField |= columns[i].equalsIgnoreCase(metaData.getColumnName(j));
                }
                assertSame(exists[i], hasField);
            }
        } finally {
            statement.close();
            connection.close();
        }
    }
    
    private void assertExistTables(DataSourceDefinition dataSource, String[] tables, boolean[] exists)
            throws SQLException {
        DataSource master = dataSource.getMaster();
        assertTrue(master instanceof RDBMSDataSource);
        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) master;
        assertEquals(RDBMSDataSource.DataSourceDialect.H2, rdbmsDataSource.getDialectName());
        Connection connection = DriverManager.getConnection(rdbmsDataSource.getConnectionURL());
        Statement statement = connection.createStatement();
        try {
            for (int i = 0; i < tables.length; i++) {
                try {
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tables[i]);
                    if(exists[i] == false) {
                        fail("Table : '" + tables[i] + "' should not exist anymore.");
                    }
                } catch (Exception e) {
                    if(exists[i]) {
                        fail("Table : '" + tables[i] + "' should still exist.");
                    }
                }
            }
        } finally {
            statement.close();
            connection.close();
        }
    }
    
    private void createRecord(Storage storage, DataRecordReader<String> factory, MetadataRepository repository,  String[] typeNames, String[] inputs){
        List<DataRecord> records = new ArrayList<DataRecord>();
        for (int i = 0; i < typeNames.length; i++) {
            records.add(factory.read(repository, repository.getComplexType(typeNames[i]), inputs[i]));
        }
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }
    }
}