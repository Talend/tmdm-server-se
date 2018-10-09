/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.adapt;

import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
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

import org.apache.log4j.Logger;
import org.h2.jdbc.JdbcSQLException;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.MetadataUtils.SortType;
import org.talend.mdm.commmon.metadata.compare.Compare;

import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.hibernate.LiquibaseSchemaAdapter;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class StorageAdaptTest extends TestCase {

    protected static final String STORAGE_NAME = "Test";

    private static final Logger LOGGER = Logger.getLogger(StorageAdaptTest.class);

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
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
        } catch (Exception e) {
            assertNull(e);
        }
 
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
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input2 });
        } catch (Exception e) {
            assertNull(e);
        }
             
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
        try {
            createRecord(storage, factory, repository3, typeNames, new String[] { input3 });
        } catch (Exception e) {
            assertNull(e);
        }
        
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
        try {
            createRecord(storage, factory, repository4, typeNames, new String[] { input4 });
        } catch (Exception e) {
            assertNull(e);
        }
        
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
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1_1, input1_2, input1_3 });
        } catch (Exception e) {
            assertNull(e);
        }
        
        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema4_2.xsd"));
        storage.adapt(repository2, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, true, true});
        String input2_1 = "<TestEntity1_Extend><id>a</id><mandatory11>a</mandatory11></TestEntity1_Extend>";
        String input2_2 = "<TestEntity2_Extend><id>b</id><mandatory11>b</mandatory11><mandatory21>b</mandatory21><mandatory22>b</mandatory22></TestEntity2_Extend>";
        String input2_3 = "<TestEntity3_Extend><id>c</id><mandatory11>c</mandatory11><mandatory21>c</mandatory21><mandatory22>b</mandatory22><mandatory31>c</mandatory31><mandatory32>c</mandatory32></TestEntity3_Extend>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input2_1, input2_2, input2_3 });
        } catch (Exception e) {
            assertNull(e);
        }
        
        MetadataRepository repository3 = new MetadataRepository();
        repository3.load(StorageAdaptTest.class.getResourceAsStream("schema4_3.xsd"));
        storage.adapt(repository3, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, true});
        String input3_1 = "<TestEntity1_Extend><id>a</id><mandatory11>a</mandatory11></TestEntity1_Extend>";
        String input3_2 = "<TestEntity2_Extend><id>b</id><mandatory11>b</mandatory11><mandatory21>b</mandatory21></TestEntity2_Extend>";
        String input3_3 = "<TestEntity3_Extend><id>c</id><mandatory11>c</mandatory11><mandatory21>c</mandatory21><mandatory31>c</mandatory31><mandatory32>c</mandatory32></TestEntity3_Extend>";
        try {
            createRecord(storage, factory, repository3, typeNames, new String[] { input3_1, input3_2, input3_3 });
        } catch (Exception e) {
            assertNull(e);
        }
        
        MetadataRepository repository4 = new MetadataRepository();
        repository4.load(StorageAdaptTest.class.getResourceAsStream("schema4_4.xsd"));
        storage.adapt(repository4, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, false});
        String input4_1 = "<TestEntity1_Extend><id>a</id><mandatory11>a</mandatory11></TestEntity1_Extend>";
        String input4_2 = "<TestEntity2_Extend><id>b</id><mandatory11>b</mandatory11><mandatory21>b</mandatory21></TestEntity2_Extend>";
        String input4_3 = "<TestEntity3_Extend><id>c</id><mandatory11>c</mandatory11><mandatory21>c</mandatory21><mandatory31>c</mandatory31></TestEntity3_Extend>";
        try {
            createRecord(storage, factory, repository3, typeNames, new String[] { input4_1, input4_2, input4_3 });
        } catch (Exception e) {
            assertNull(e);
        }
        
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
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input_5, input_4, input_3, input_2, input_1 });
        } catch (Exception e) {
            assertNull(e);
        }
        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema5_2.xsd"));
        storage.adapt(repository2, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, true, true, true, true});
        input_1 = "<TestEntity1_Reference><id>1</id><mandatory11>m11</mandatory11><testEntity2>[2]</testEntity2><testEntity3>[3]</testEntity3></TestEntity1_Reference>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input_5, input_4, input_3, input_2, input_1 });
        } catch (Exception e) {
            assertNull(e);
        }
        
        MetadataRepository repository3 = new MetadataRepository();
        repository3.load(StorageAdaptTest.class.getResourceAsStream("schema5_3.xsd"));
        storage.adapt(repository3, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, true, true, true});
        input_2 = "<TestEntity2_Reference><id>2</id><mandatory21>m21</mandatory21><testEntity4>[4]</testEntity4><testEntity5>[5]</testEntity5></TestEntity2_Reference>";
        try {
            createRecord(storage, factory, repository3, typeNames, new String[] { input_5, input_4, input_3, input_2, input_1 });
        } catch (Exception e) {
            assertNull(e);
        }
        
        MetadataRepository repository4 = new MetadataRepository();
        repository4.load(StorageAdaptTest.class.getResourceAsStream("schema5_4.xsd"));
        storage.adapt(repository4, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, false, true, true});
        input_3 = "<TestEntity3_Reference><id>3</id><mandatory31>m31</mandatory31><testEntity4>[4]</testEntity4><testEntity5>[5]</testEntity5></TestEntity3_Reference>";
        try {
            createRecord(storage, factory, repository4, typeNames, new String[] { input_5, input_4, input_3, input_2, input_1 });
        } catch (Exception e) {
            assertNull(e);
        }
        
        MetadataRepository repository5 = new MetadataRepository();
        repository5.load(StorageAdaptTest.class.getResourceAsStream("schema5_5.xsd"));
        storage.adapt(repository5, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, false, false, true});
        input_4 = "<TestEntity4_Reference><id>4</id><mandatory41>m41</mandatory41></TestEntity4_Reference>";
        try {
            createRecord(storage, factory, repository5, typeNames, new String[] { input_5, input_4, input_3, input_2, input_1 });
        } catch (Exception e) {
            assertNull(e);
        }
        
        MetadataRepository repository6 = new MetadataRepository();
        repository6.load(StorageAdaptTest.class.getResourceAsStream("schema5_6.xsd"));
        storage.adapt(repository6, true);
        assertDatabaseChange(dataSource, tables, columns, new boolean[] {false, false, false, false, false});
        input_5 = "<TestEntity5_Reference><id>5</id><mandatory51>m51</mandatory51></TestEntity5_Reference>";
        try {
            createRecord(storage, factory, repository6, typeNames, new String[] { input_5, input_4, input_3, input_2, input_1 });
        } catch (Exception e) {
            assertNull(e);
        }
        
        storage.close(true);
    }
    
    // TMDM-9376 Open RCEnt container cause studio freeze. In RCEnt data model, two entities have FK refer to each other
    // and one of the entity has another FK refers to itself.
    public void testFKReferToItself() throws Exception {
        // Test preparation
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage(STORAGE_NAME, StorageType.MASTER);
        storage.init(dataSource);

        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema6_1.xsd"));
        storage.prepare(repository1, true);

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema6_2.xsd"));

        // TMDM-9469 Impact analysis: list entities that will be dropped
        Compare.DiffResults diffResults = Compare.compare(repository1, repository2);
        List<ComplexTypeMetadata> sortedTypesToDrop = storage.findSortedTypesToDrop(diffResults, true);
        assertEquals(3, sortedTypesToDrop.size());
        assertEquals("OrganisationType", sortedTypesToDrop.get(0).getName());
        assertEquals("MST_Organisation", sortedTypesToDrop.get(1).getName());
        assertEquals("MST_Notice", sortedTypesToDrop.get(2).getName());

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

        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema7_1.xsd"));
        storage.prepare(repository1, true);

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema7_2.xsd"));

        // TMDM-9469 Impact analysis: list entities that will be dropped
        Compare.DiffResults diffResults = Compare.compare(repository1, repository2);
        List<ComplexTypeMetadata> sortedTypesToDrop = storage.findSortedTypesToDrop(diffResults, true);
        assertEquals(5, sortedTypesToDrop.size());
        assertEquals("TypeComptes", sortedTypesToDrop.get(0).getName());
        assertEquals("TypeEtablissements", sortedTypesToDrop.get(1).getName());
        assertEquals("TieTiers", sortedTypesToDrop.get(2).getName());
        assertEquals("TieEtablissements", sortedTypesToDrop.get(3).getName());
        assertEquals("TieComptes", sortedTypesToDrop.get(4).getName());

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

    // TMDM-9941 Recreate table fields when there are reusable types changes
    public void testFindReusableTablesToDrop() throws Exception {
        // Test preparation
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage(STORAGE_NAME, StorageType.MASTER);
        storage.init(dataSource);

        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageAdaptTest.class.getResourceAsStream("TMDM-9941.xsd"));
        storage.prepare(repository, true);
        // Drop Type e2
        ComplexTypeMetadata composition = repository.getComplexType("e2");
        Set<ComplexTypeMetadata> typesToDrop = new HashSet<ComplexTypeMetadata>();
        typesToDrop.add(composition);
        // Find all dependencies of e2
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
        assertTrue(tables.contains("e2"));
        assertTrue(tables.contains("e1"));
        assertTrue(tables.contains("X_share"));
        storage.close(true);
    }

    // TMDM-9099 Increase the length of a string element should be low impact
    public void test9_IncreaseStringFieldLength() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("MyStr", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "MyStr" };
        String[] tables = { "MyStr" };
        String[] columns = { "X_ID", "X_MYSTR", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema9_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        try {
            assertColumnLengthChange(dataSource, "MyStr", "X_MYSTR", 10);
        } catch (SQLException e) {
            assertNull(e);
        }

        String input1 = "<MyStr><Id>id-1</Id><MyStr>str-1</MyStr></MyStr>";
        String input2 = "<MyStr><Id>id-2</Id><MyStr>str-1-1-1-1-1-1-1-1-1-1-1</MyStr></MyStr>";
        String input3 = "<MyStr><Id>id-3</Id><MyStr>str123456789123456789123456789123</MyStr></MyStr>";
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
        } catch (Exception e2) {
            assertNull(e2);
        }

        storage.begin();
        ComplexTypeMetadata MyStr = repository1.getComplexType("MyStr");//$NON-NLS-1$
        UserQueryBuilder qb = from(MyStr);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("id-1", result.get("Id"));
                assertEquals("str-1", result.get("MyStr"));
            }
        } finally {
            results.close();
        }
        storage.end();

        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input2 });
            fail("could not insert into the input2 data");
        } catch (Exception e1) {
            assertTrue(e1 instanceof RuntimeException);
        }
        storage.begin();
        MyStr = repository1.getComplexType("MyStr");//$NON-NLS-1$
        qb = from(MyStr);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("id-1", result.get("Id"));
                assertEquals("str-1", result.get("MyStr"));
            }
        } finally {
            results.close();
        }
        storage.end();

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema9_2.xsd"));
        storage.adapt(repository2, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        try {
            assertColumnLengthChange(dataSource, "MyStr", "X_MYSTR", 30);
        } catch (SQLException e) {
            assertNull(e);
        }

        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input2 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        MyStr = repository2.getComplexType("MyStr");//$NON-NLS-1$
        qb = from(MyStr);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            for (DataRecord result : results) {
                if (result.get("Id").equals("id-1")) {
                    assertEquals("str-1", result.get("MyStr"));
                }
                if (result.get("Id").equals("id-2")) {
                    assertEquals("str-1-1-1-1-1-1-1-1-1-1-1", result.get("MyStr"));
                }

            }
        } finally {
            results.close();
        }
        storage.end();

        MetadataRepository repository3 = new MetadataRepository();
        repository3.load(StorageAdaptTest.class.getResourceAsStream("schema10_1.xsd"));
        storage.adapt(repository3, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        try {
            assertColumnLengthChange(dataSource, "MyStr", "X_MYSTR", 35);
        } catch (SQLException e) {
            assertNull(e);
        }

        try {
            createRecord(storage, factory, repository3, typeNames, new String[] { input3 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        MyStr = repository3.getComplexType("MyStr");//$NON-NLS-1$
        qb = from(MyStr);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
            for (DataRecord result : results) {
                if (result.get("Id").equals("id-1")) {
                    assertEquals("str-1", result.get("MyStr"));
                }
                if (result.get("Id").equals("id-2")) {
                    assertEquals("str-1-1-1-1-1-1-1-1-1-1-1", result.get("MyStr"));
                }
                if (result.get("Id").equals("id-3")) {
                    assertEquals("str123456789123456789123456789123", result.get("MyStr"));
                }

            }
        } finally {
            results.close();
        }
        storage.end();

        MetadataRepository repository4 = new MetadataRepository();
        repository4.load(StorageAdaptTest.class.getResourceAsStream("schema9_3.xsd"));
        storage.adapt(repository4, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        try {
            assertColumnLengthChange(dataSource, "MyStr", "X_MYSTR", 5);
        } catch (SQLException e) {
            assertNull(e);
        }

        storage.begin();
        MyStr = repository4.getComplexType("MyStr");//$NON-NLS-1$
        qb = from(MyStr);
        results = storage.fetch(qb.getSelect());
        assertEquals(0, results.getCount());
    }

    public void test10_UseSuperTypeMaxLengthForInherit() {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("MyStr", StorageType.MASTER);
        storage.init(dataSource);
        String[] tables = { "MyStr" };
        String[] columns = { "X_ID", "X_MYSTR", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema10_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        try {
            assertColumnLengthChange(dataSource, "MyStr", "X_MYSTR", 35);
        } catch (SQLException e) {
            assertNull(e);
        }
    }

    // TMDM-9086 test for add mandatory field with default value
    public void test11_addMandatoryFiledWithDefaultValue() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Person" };
        String[] tables = { "Person" };
        String[] columns = { "X_ID", "X_NAME", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema11_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        String input1 = "<Person><Id>id-1</Id><name>name-1</name></Person>";
        String input2 = "<Person><Id>id-2</Id><name>name-2</name><lastname>Alice</lastname><age>20</age><weight>81.1</weight><sex>false</sex><name_2>abbc</name_2></Person>";
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository1.getComplexType("Person");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("id-1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.end();

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema11_2.xsd"));
        storage.adapt(repository2, false);
        
        String[] updatedColumns = { "X_ID", "X_NAME", "X_LASTNAME", "X_AGE", "X_WEIGHT", "X_SEX", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        try {
            assertDatabaseChange(dataSource, tables, updatedColumns, new boolean[] { true });
            String[] name2Table = { "PERSON_X_NAME_2" };
            assertExistTables(dataSource, name2Table, new boolean[] { true });
            String[] updatedColumnsForName2 = { "X_ID", "VALUE", "POS"};
            assertDatabaseChange(dataSource, name2Table, updatedColumnsForName2, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("id-1", result.get("Id"));
                assertEquals("name-1", result.get("name"));
                assertEquals(6, result.get("age"));
                assertEquals(12.6, result.get("weight"));
                assertEquals(Boolean.TRUE, result.get("sex"));
            }
        } finally {
            results.close();
        }
        storage.end();

        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input2 });
        } catch (Exception e) {
            assertNull(e);
        }

        storage.begin();
        objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        qb = from(objectType).where(eq(objectType.getField("Id"), "id-2")); //$NON-NLS-1$ //$NON-NLS-2$
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("Alice", result.get("lastname"));
                assertEquals(20, result.get("age"));
                assertEquals(81.1, result.get("weight"));
                assertEquals(Boolean.FALSE, result.get("sex"));
                assertEquals(1, ((List)result.get("name_2")).size());
                assertEquals("abbc", ((List)result.get("name_2")).get(0));
            }
        } finally {
            results.close();
        }
        storage.end();
    }

    private static void setMDMRootURL() {
        URL targetDir = StorageAdaptTest.class.getClassLoader().getResource(".");
        LOGGER.info("Using MDM ROOT: " + targetDir.getFile());
        System.setProperty(LiquibaseSchemaAdapter.MDM_ROOT, targetDir.getFile());
    }

    // TMDM-10525 [Impact Analysis] Move a simple field from optional to mandatory 1
    public void test12_MoveFieldFromOptionalToMandatory_ForNoValueInDB() throws Exception {
        /*
         * all the filed will change to mandatory from optional first name type is string, have no the default value
         * second name type is string, contains the default value full name type is string, have no the default value
         * age type is int, have no the default value married type is boolean contains the default value birthday type
         * is data, have no default value
         */
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        HibernateStorage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Person" };
        String[] tables = { "Person" };
        String[] columns = { "X_ID", "X_FIRST_NAME", "X_SECOND_NAME", "X_FULL_NAME", "X_AGE", "X_MARRIED", "X_BIRTHDAY",
                "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema12_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema12_2.xsd"));
        storage.adapt(repository2, false);

        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        // create one record(all field have value) success
        String input1 = "<Person><Id>1</Id><first_name>Jack</first_name><second_name>Chen</second_name><full_name>Jack Chen</full_name><age>11</age><married>true</married><birthday>2017-02-04</birthday></Person>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNull(e1);
        }
        storage.begin();
        ComplexTypeMetadata objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        // create one record(birthday have no value ) failed
        String input2 = "<Person><Id>1</Id><first_name>Jack</first_name><second_name>Chen</second_name><full_name>Jack Chen</full_name><age>11</age><married>true</married></Person>";
        boolean createFail = false;
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input2 });
        } catch (Exception e) {
            createFail = true;
            assertTrue(e != null);
        }
        assertTrue(createFail);

        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();

        deleteLiquibaseChangeLogFile();

    }

    // TMDM-10525 [Impact Analysis] Move a simple field from optional to mandatory 2
    public void test12_MoveFieldFromOptionalToMandatory_ForAllFieldContainsDataInDB() throws Exception {
        /*
         * all the filed will change to mandatory from optional first name type is string, have no the default value
         * second name type is string, contains the default value full name type is string, have no the default value
         * age type is int, have no the default value married type is boolean contains the default value birthday type
         * is data, have no default value
         */

        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        HibernateStorage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Person" };
        String[] tables = { "Person" };
        String[] columns = { "X_ID", "X_FIRST_NAME", "X_SECOND_NAME", "X_FULL_NAME", "X_AGE", "X_MARRIED", "X_BIRTHDAY",
                "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema12_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        // create record before change, second_name, married is null
        String input1 = "<Person><Id>1</Id><first_name>Jack</first_name><second_name>Chen</second_name><full_name>Jack Chen</full_name><age>11</age><married>true</married><birthday>2017-02-04</birthday></Person>";
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository1.getComplexType("Person");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema12_2.xsd"));
        storage.adapt(repository2, true);

        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        // data had changed for have default value field
        storage.begin();
        objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
                assertEquals("Jack", result.get("first_name"));
                assertEquals("Chen", result.get("second_name"));
                assertEquals("Jack Chen", result.get("full_name"));
                assertEquals(11, result.get("age"));
                assertEquals(Boolean.TRUE, result.get("married"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();

        deleteLiquibaseChangeLogFile();
    }

    // TMDM-10525 [Impact Analysis] Move a simple field from optional to mandatory 3
    public void test12_MoveFieldFromOptionalToMandatory_ForContainsDefaultValueFieldNoDataInDB() throws Exception {
        /*
         * all the filed will change to mandatory from optional first name type is string, have no the default value
         * second name type is string, contains the default value full name type is string, have no the default value
         * age type is int, have no the default value married type is boolean contains the default value birthday type
         * is data, have no default value
         */
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Person" };
        String[] tables = { "Person" };
        String[] columns = { "X_ID", "X_FIRST_NAME", "X_SECOND_NAME", "X_FULL_NAME", "X_AGE", "X_MARRIED", "X_BIRTHDAY",
                "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema12_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        // create record before change, second_name, married is null
        String input1 = "<Person><Id>1</Id><first_name>Jack</first_name><full_name>Jack Chen</full_name><age>11</age><birthday>2017-02-04</birthday></Person>";
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository1.getComplexType("Person");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema12_2.xsd"));
        storage.adapt(repository2, false);

        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        // data contains in DB
        storage.begin();
        objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
                assertEquals("Jack", result.get("first_name"));
                assertEquals("Chen", result.get("second_name"));
                assertEquals("Jack Chen", result.get("full_name"));
                assertEquals(11, result.get("age"));
                assertEquals(Boolean.FALSE, result.get("married"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();

        deleteLiquibaseChangeLogFile();
    }

    // TMDM-10525 [Impact Analysis] Move a simple field from optional to mandatory 4
    public void test12_MoveFieldFromOptionalToMandatory_ForNoDefaultValueFieldNoDataInDB() throws Exception {
        /*
         * all the filed will change to mandatory from optional first name type is string, have no the default value
         * second name type is string, contains the default value full name type is string, have no the default value
         * age type is int, have no the default value married type is boolean contains the default value birthday type
         * is data, have no default value
         */
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Person" };
        String[] tables = { "Person" };
        String[] columns = { "X_ID", "X_FIRST_NAME", "X_SECOND_NAME", "X_FULL_NAME", "X_AGE", "X_MARRIED", "X_BIRTHDAY",
                "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema12_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        // create record before change, second_name, married is null
        String input1 = "<Person><Id>1</Id><second_name>Chen</second_name><full_name>Jack Chen</full_name><age>11</age><married>true</married><birthday>2017-02-04</birthday></Person>";
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository1.getComplexType("Person");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema12_2.xsd"));
        storage.adapt(repository2, true);

        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        // data can't contains in DB
        storage.begin();
        objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();

        deleteLiquibaseChangeLogFile();
    }

    // TMDM-10529 [Impact Analysis] Delete an optional simple field, then recreate the same optional field with another
    // type
    public void test13_DeleteOptionField_ForNoData() throws Exception {
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Person" };
        String[] tables = { "Person" };
        String[] columns = { "X_ID", "X_FIRST_NAME", "X_SECOND_NAME", "X_FULL_NAME", "X_AGE", "X_MARRIED", "X_BIRTHDAY",
                "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema12_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema13_1.xsd"));
        storage.adapt(repository2, false);

        // tabale had changed
        String[] updatedColumns = { "X_ID", "X_FULL_NAME", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        try {
            assertDatabaseChange(dataSource, tables, updatedColumns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        String input1 = "<Person><Id>1</Id><second_name>Chen</second_name><full_name>Jack Chen</full_name><age>11</age><married>true</married><birthday>2017-02-04</birthday></Person>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNotNull(e1);
        }

        // create record before change, second_name, married is null
        String input2 = "<Person><Id>1</Id><full_name>Jack Chen</full_name></Person>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input2 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();

        deleteLiquibaseChangeLogFile();
    }

    // TMDM-10529 [Impact Analysis] Delete an optional simple field, then recreate the same optional field with another
    // type
    public void test13_DeleteOptionField_ForWithData() throws Exception {
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Person" };
        String[] tables = { "Person" };
        String[] columns = { "X_ID", "X_FIRST_NAME", "X_SECOND_NAME", "X_FULL_NAME", "X_AGE", "X_MARRIED", "X_BIRTHDAY",
                "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema12_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        // create record before change, second_name, married is null
        String input1 = "<Person><Id>1</Id><second_name>Chen</second_name><full_name>Jack Chen</full_name><age>11</age><married>true</married><birthday>2017-02-04</birthday></Person>";
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository1.getComplexType("Person");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema13_1.xsd"));
        storage.adapt(repository2, false);

        // tabale had changed
        String[] updatedColumns = { "X_ID", "X_FULL_NAME", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        try {
            assertDatabaseChange(dataSource, tables, updatedColumns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        storage.begin();
        objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(2, result.getSetFields().size());
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        String input2 = "<Person><Id>1</Id><second_name>Chen</second_name><full_name>Jack Chen</full_name><age>11</age><married>true</married><birthday>2017-02-04</birthday></Person>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input2 });
        } catch (Exception e1) {
            assertNotNull(e1);
        }

        // create record before change, second_name, married is null
        String input3 = "<Person><Id>2</Id><full_name>Jack Chen</full_name></Person>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input3 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();

        deleteLiquibaseChangeLogFile();
    }

    // TMDM-10531: [Impact Analysis] Move a simple field from mandatory to optional
    public void test14_MoveSimpleFieldFormMandatoryToOption_ForNoData() throws Exception {
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Person" };
        String[] tables = { "Person" };
        String[][] columns = { { "", "X_ID", "X_FIRST_NAME", "X_SECOND_NAME", "X_FULL_NAME", "X_AGE", "X_MARRIED", "X_BIRTHDAY",
                "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" } };

        int[][] isNullable = { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 } };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema14_1.xsd"));
        storage.prepare(repository1, true);

        try {
            assertColumnNullAble(dataSource, tables, columns, isNullable);
        } catch (SQLException e) {
            assertNull(e);
        }

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema14_2.xsd"));
        storage.adapt(repository2, false);

        int[][] isNullableUpdated = { { 0, 0, 1, 1, 1, 1, 1, 1, 0, 1 } };
        try {
            assertColumnNullAble(dataSource, tables, columns, isNullableUpdated);
        } catch (SQLException e) {
            assertNull(e);
        }

        // create record before change, second_name, married is null
        String input1 = "<Person><Id>1</Id></Person>";
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository1.getComplexType("Person");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();

        deleteLiquibaseChangeLogFile();
    }

    // TMDM-10531: [Impact Analysis] Move a simple field from mandatory to optional
    public void test14_MoveSimpleFieldFormMandatoryToOption_WithData() throws Exception {
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Person" };
        String[] tables = { "Person" };
        String[][] columns = { { "", "X_ID", "X_FIRST_NAME", "X_SECOND_NAME", "X_FULL_NAME", "X_AGE", "X_MARRIED", "X_BIRTHDAY",
                "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" } };

        int[][] isNullable = { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 } };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema14_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertColumnNullAble(dataSource, tables, columns, isNullable);
        } catch (SQLException e) {
            assertNull(e);
        }

        // create record before change, second_name, married is null
        String input1 = "<Person><Id>1</Id><first_name>Jack</first_name><second_name>Chen</second_name><full_name>Jack Chen</full_name><age>11</age><married>true</married><birthday>2017-02-04</birthday></Person>";
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository1.getComplexType("Person");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema14_2.xsd"));
        storage.adapt(repository2, false);

        int[][] isNullableUpdated = { { 0, 0, 1, 1, 1, 1, 1, 1, 0, 1 } };
        try {
            assertColumnNullAble(dataSource, tables, columns, isNullableUpdated);
        } catch (SQLException e) {
            assertNull(e);
        }

        storage.begin();
        objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(7, result.getSetFields().size());
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        String input2 = "<Person><Id>2</Id></Person>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input2 });
        } catch (Exception e1) {
            assertNotNull(e1);
        }

        storage.begin();
        objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();

        deleteLiquibaseChangeLogFile();
    }

    // TMDM-10533: [Impact Analysis] Deleting a mandatory field
    public void test15_DeleteMandatoryField_ForNoData() throws Exception {
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Person" };
        String[] tables = { "Person" };
        String[] columns = { "X_ID", "X_FIRST_NAME", "X_SECOND_NAME", "X_FULL_NAME", "X_AGE", "X_MARRIED", "X_BIRTHDAY",
                "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema12_2.xsd"));
        storage.prepare(repository1, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema13_1.xsd"));
        storage.adapt(repository2, false);

        // tabale had changed
        String[] updatedColumns = { "X_ID", "X_FULL_NAME", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        try {
            assertDatabaseChange(dataSource, tables, updatedColumns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        String input1 = "<Person><Id>1</Id><first_name>Jack</first_name><second_name>Chen</second_name><full_name>Jack Chen</full_name><age>11</age><married>true</married><birthday>2017-02-04</birthday></Person>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNotNull(e1);
        }

        // create record before change, second_name, married is null
        String input2 = "<Person><Id>1</Id><full_name>Jack Chen</full_name></Person>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input2 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();

        deleteLiquibaseChangeLogFile();
    }

    // TMDM-10533: [Impact Analysis] Deleting a mandatory field
    public void test15_DeleteMandatoryField_ForWithData() throws Exception {
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Person" };
        String[] tables = { "Person" };
        String[] columns = { "X_ID", "X_FIRST_NAME", "X_SECOND_NAME", "X_FULL_NAME", "X_AGE", "X_MARRIED", "X_BIRTHDAY",
                "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema12_2.xsd"));
        storage.prepare(repository1, true);
        try {
            assertDatabaseChange(dataSource, tables, columns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        // create record before change, second_name, married is null
        String input1 = "<Person><Id>1</Id><first_name>Jack</first_name><second_name>Chen</second_name><full_name>Jack Chen</full_name><age>11</age><married>true</married><birthday>2017-02-04</birthday></Person>";
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository1.getComplexType("Person");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema13_1.xsd"));
        storage.adapt(repository2, false);

        // tabale had changed
        String[] updatedColumns = { "X_ID", "X_FULL_NAME", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" };
        try {
            assertDatabaseChange(dataSource, tables, updatedColumns, new boolean[] { true });
        } catch (SQLException e) {
            assertNull(e);
        }

        storage.begin();
        objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(2, result.getSetFields().size());
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        String input2 = "<Person><Id>1</Id><first_name>Jack</first_name><second_name>Chen</second_name><full_name>Jack Chen</full_name><age>11</age><married>true</married><birthday>2017-02-04</birthday></Person>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input2 });
        } catch (Exception e1) {
            assertNotNull(e1);
        }

        // create record before change, second_name, married is null
        String input3 = "<Person><Id>2</Id><full_name>Jack Chen</full_name></Person>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input3 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();

        deleteLiquibaseChangeLogFile();
    }

    // TMDM-10534：[Impact Analysis] Move a complex mandatory field (containing a simple optional field) to optional
    public void test16_MoveComplexFieldFromMandatoryToOptional() throws Exception {
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Person" };
        String[] tables = { "Person" };
        String[][] columns = { { "", "X_ID", "X_NAME", "X_BOY_X_TALEND_ID", "X_GIRL_X_TALEND_ID", "X_STATUS", "X_UU_X_TALEND_ID",
                "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" } };

        int[][] isNullable = { { 0, 0, 0, 0, 1, 0, 0, 0, 1 } };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema15_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertColumnNullAble(dataSource, tables, columns, isNullable);
        } catch (SQLException e) {
            assertNull(e);
        }

        String[] e2Tables = { "E2", "X_ANONYMOUS0" };
        String[][] e2Columns = { { "", "X_GRADE", "X_NAME", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" },
                { "", "X_TALEND_ID", "X_SUBELEMENT" } };
        int[][] e2IsNullable = { { 0, 0, 0, 0, 1 }, { 0, 0, 0 } };
        try {
            assertColumnNullAble(dataSource, e2Tables, e2Columns, e2IsNullable);
        } catch (SQLException e) {
            assertNull(e);
        }

        // create record before change, second_name, married is null
        String input1 = "<Person><Id>1</Id><name>person-name</name><boy><name>boy_name</name><grade>1</grade></boy><girl><name>girl_name</name><grade>1</grade></girl><status>Approved</status><uu><subelement>uu-sub</subelement></uu></Person>";
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        String[] e2TypeNames = { "E2" };
        String input1_2 = "<E2><name>e2-name</name><grade>1</grade></E2>";
        try {
            createRecord(storage, factory, repository1, e2TypeNames, new String[] { input1_2 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository1.getComplexType("Person");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(6, result.getSetFields().size());
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        ComplexTypeMetadata e2Type = repository1.getComplexType("E2");//$NON-NLS-1$
        qb = from(e2Type);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(2, result.getSetFields().size());
                assertEquals("e2-name", result.get("name"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema15_2.xsd"));
        try {
            storage.adapt(repository2, false);
        } catch (Exception e2) {
            assertNull(e2);
        }

        int[][] isNullableUpdated = { { 0, 0, 1, 1, 1, 1, 1, 0, 1 } };
        try {
            assertColumnNullAble(dataSource, tables, columns, isNullableUpdated);
        } catch (SQLException e) {
            assertNull(e);
        }

        String[] e2TablesUpdated = { "E2", "X_ANONYMOUS0" };
        String[][] e2ColumnsUpdated = { { "", "X_GRADE", "X_NAME", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" },
                { "", "X_TALEND_ID", "X_SUBELEMENT" } };
        int[][] e2IsNullableUpdated = { { 0, 0, 1, 0, 1 }, { 0, 0, 1 } };
        try {
            assertColumnNullAble(dataSource, e2TablesUpdated, e2ColumnsUpdated, e2IsNullableUpdated);
        } catch (SQLException e) {
            assertNull(e);
        }

        storage.begin();
        objectType = repository2.getComplexType("Person");//$NON-NLS-1$
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(6, result.getSetFields().size());
            }
        } finally {
            results.close();
        }
        storage.commit();

        storage.begin();
        e2Type = repository2.getComplexType("E2");//$NON-NLS-1$
        qb = from(e2Type);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(2, result.getSetFields().size());
            }
        } finally {
            results.close();
        }
        storage.commit();

        String input2 = "<Person><Id>2</Id><girl><name>girl_name</name><grade>1</grade></girl></Person>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input2 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        String input2_2 = "<E2><grade>2</grade></E2>";
        try {
            createRecord(storage, factory, repository2, e2TypeNames, new String[] { input2_2 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();

        storage.begin();
        qb = from(e2Type);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();

        storage.begin();
        storage.delete(qb.getSelect());
        qb = from(objectType);
        storage.delete(qb.getSelect());
        storage.commit();

        deleteLiquibaseChangeLogFile();
    }

    public void test17_forRepeatable() throws Exception {
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] tables = { "Person" };
        String[][] columns = { { "", "X_ID", "X_BB_X_TALEND_ID", "X_EE", "X_UU_X_TALEND_ID", "X_ADDRESS_X_ADDRESSID",
                "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" } };

        int[][] isNullable = { { 0, 0, 0, 0, 0, 1, 0, 1 } };
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("../hibernate/schema2_1.xsd"));
        storage.prepare(repository1, true);

        try {
            assertColumnNullAble(dataSource, tables, columns, isNullable);
        } catch (SQLException e) {
            assertNull(e);
        }

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("../hibernate/schema2_2.xsd"));
        try {
            storage.adapt(repository2, false);
        } catch (Exception e2) {
            assertNull(e2);
        }

        String[][] columnsUpdated = {
                { "", "X_ID", "X_BB_X_TALEND_ID", "X_EE", "X_UU_X_TALEND_ID", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" } };
        int[][] isNullableUpdated = { { 0, 0, 1, 1, 1, 0, 1 } };
        try {
            assertColumnNullAble(dataSource, tables, columnsUpdated, isNullableUpdated);
        } catch (SQLException e) {
            assertNull(e);
        }
    }

    // TMDM-10993
    public void test18_for_duplicated_fk() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);

        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema18_1.xsd"));
        storage.prepare(repository1, true);

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema18_2.xsd"));
        try {
            storage.adapt(repository2, true);
        } catch (Exception e2) {
            assertNull(e2);
        }
    }

    // TMDM-10613
    public void test19_addOptionalComplexField() throws Exception {
        /*
         * Test                                                 Test
         *   |__id (SimpleField) (1-1)                           |__id (SimpleField) (1-1)
         *   |__name (SimpleField) (1-1)                         |__name (SimpleField) (1-1)
         *   |__desc (SimpleField) (1-1)     ===========>            |__desc (SimpleField) (1-1)
         *                                                       |__do (ComplexField) (non-anonymous of: aa) (0-1)
         *                                                           |__subelement (SimpleField) (1-1)
         *                                                       |__do1 (ComplexField) (non-anonymous of: aa) (0-many)
         *                                                           |__subelement (SimpleField) (1-1)
         *                                                       |__do2 (ComplexField) (anonymous) (0-1)
         *                                                           |__subelement (SimpleField) (1-1)
         *                                                           |__second (SimpleField) (0-1)
         *                                                       |__do3 (ComplexField) (anonymous) (0-many)
         *                                                           |__subelement (SimpleField) (1-1)
         *                                                           |__third (SimpleField) (1-1)
         */
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Test" };
        String[] tables = { "Test" };
        String[][] columns = { { "", "X_ID", "X_NAME", "X_DESC", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" } };

        int[][] isNullable = { { 0, 0, 0, 0, 0, 1 } };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema19_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertColumnNullAble(dataSource, tables, columns, isNullable);
        } catch (SQLException e) {
            assertNull(e);
        }

        // create record before change, second_name, married is null
        String input1 = "<Test><id>1</id><name>person-name</name><desc>desc</desc></Test>";
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository1.getComplexType("Test");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(3, result.getSetFields().size());
                assertEquals("1", result.get("id"));
                assertEquals("person-name", result.get("name"));
                assertEquals("desc", result.get("desc"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema19_2.xsd"));
        try {
            storage.adapt(repository2, false);
        } catch (Exception e2) {
            assertNull(e2);
        }

        int[][] isNullableUpdated = { { 0, 0, 0, 0, 0, 1, 1, 1 } };
        String[][] updatedColumns = { { "", "X_ID", "X_NAME", "X_DESC", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID",
                "X_DO_X_TALEND_ID", "X_DO2_X_TALEND_ID" } };
        try {
            assertColumnNullAble(dataSource, tables, updatedColumns, isNullableUpdated);
        } catch (SQLException e) {
            assertNull(e);
        }

        String[] aaTablesUpdated = { "X_AA", "X_ANONYMOUS0", "X_ANONYMOUS1", "TEST_X_DO2_X_ANONYMOUS1" };
        String[][] aaColumnsUpdated = { { "", "X_TALEND_ID", "X_SUBELEMENT" }, { "", "X_TALEND_ID", "X_SUBELEMENT", "X_SECOND" },
                { "", "X_TALEND_ID", "X_SUBELEMENT", "X_THIRD" }, { "", "X_ID", "X_DO2_X_TALEND_ID", "POS" } };
        int[][] aaIsNullableUpdated = { { 0, 0, 1, 0, 0, 1, 1, 1 }, { 0, 0, 0 }, { 0, 0, 1, 1 }, { 0, 0, 1, 0 } };
        try {
            assertColumnNullAble(dataSource, aaTablesUpdated, aaColumnsUpdated, aaIsNullableUpdated);
        } catch (SQLException e) {
            assertNull(e);
        }

        storage.begin();
        objectType = repository2.getComplexType("Test");//$NON-NLS-1$
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(3, result.getSetFields().size());
            }
        } finally {
            results.close();
        }
        storage.commit();

        String input2 = "<Test><id>2</id><name>name2</name><desc>desc2</desc><do><subelement>do-sub</subelement></do><do1><subelement>do1-sub</subelement></do1><do2><subelement>do2-sub</subelement><second>do2-second</second></do2><do3><subelement>do3-sub</subelement><third>do3-third</third></do3></Test>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input2 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            int i = 0;
            for (DataRecord result : results) {
                // assertEquals(7, result.getSetFields().size());
                if (i == 0) {
                    assertEquals("1", result.get("id"));
                    assertEquals("person-name", result.get("name"));
                    assertEquals("desc", result.get("desc"));
                } else if (i == 1) {
                    assertEquals("2", result.get("id"));
                    assertEquals("name2", result.get("name"));
                    assertEquals("desc2", result.get("desc"));
                    assertEquals("do-sub", result.get("do/subelement"));
                    assertEquals("do1-sub", result.get("do1/subelement"));
                    assertEquals("do2-sub", result.get("do2/subelement"));
                    assertEquals("do2-second", result.get("do2/second"));
                    assertEquals("do3-sub", result.get("do3/subelement"));
                    assertEquals("do3-third", result.get("do3/third"));
                }
                i++;
            }
        } finally {
            results.close();
        }
        storage.commit();
    }

    public void test20_ModifyMandatoryToOptionalOfComplexTypeField() throws Exception {
        /*
         * Test                                                  Test
         *   |__id (SimpleField) (1-1)                             |__id (SimpleField) (1-1)
         *   |__name (SimpleField) (1-1)                           |__name (SimpleField) (1-1)
         *   |__address (ComplexField) (0-1)                        |__address(ComplexField) (0-1)
         *        |__city (SimpleField) (1-1)   ==========>             |__city (SimpleField) (1-1)
         *        |__detail (ComplexField) (1-1)                         |__detail(ComplexField) (0-1)
         *              |__comment (SimpleField) (0-1)                        |__comment (SimpleField) (0-1)
         */
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Test" };
        String[] tables = { "Test", "X_COMPLEX1", "X_ANONYMOUS0" };
        String[][] columns = { { "", "X_ID", "X_NAME", "X_ADDRESS_X_TALEND_ID", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" },
                { "", "X_TALEND_ID", "X_CITY", "X_DETAIL_X_TALEND_ID" }, { "", "X_TALEND_ID", "X_COMMENT" } };
        int[][] isNullable = { { 0, 0, 0, 1, 0, 1 }, { 0, 0, 0, 0 }, { 0, 0, 0 } };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema20_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertColumnNullAble(dataSource, tables, columns, isNullable);
        } catch (SQLException e) {
            assertNull(e);
        }

        // create record before change, second_name, married is null
        String input1 = "<Test><id>1</id><name>John</name><address><city>Beijing</city><detail><comment>Beijing Address</comment></detail></address></Test>";
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository1.getComplexType("Test");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("id"));
                assertEquals("John", result.get("name"));
                assertEquals("Beijing", result.get("address/city"));
                assertEquals("Beijing Address", result.get("address/detail/comment"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema20_2.xsd"));
        try {
            storage.adapt(repository2, false);
        } catch (Exception e2) {
            assertNull(e2);
        }

        isNullable[1][2] = 1;
        isNullable[1][3] = 1;
        isNullable[2][2] = 1;
        try {
            assertColumnNullAble(dataSource, tables, columns, isNullable);
        } catch (SQLException e) {
            assertNull(e);
        }

        storage.begin();
        objectType = repository2.getComplexType("Test");
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(3, result.getSetFields().size());
            }
        } finally {
            results.close();
        }
        storage.commit();

        String input2 = "<Test><id>2</id><name>Jack</name></Test>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input2 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            int i = 0;
            for (DataRecord result : results) {
                // assertEquals(7, result.getSetFields().size());
                if (i == 0) {
                    assertEquals("1", result.get("id"));
                    assertEquals("John", result.get("name"));
                    assertEquals("Beijing", result.get("address/city"));
                    assertEquals("Beijing Address", result.get("address/detail/comment"));
                } else if (i == 1) {
                    assertEquals("2", result.get("id"));
                    assertEquals("Jack", result.get("name"));
                }
                i++;
            }
        } finally {
            results.close();
        }
        storage.commit();
    }

    public void test21_RenamedFKName() throws Exception {
        setMDMRootURL();

        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        Storage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        String[] typeNames = { "Product" };
        String[] tables = { "Product", "PRODUCTFAMILY" };
        String[][] columns = { { "", "X_ID", "X_NAME", "X_FAMILY_X_ID", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" },
                { "", "X_ID", "X_NAME", "X_CHANGESTATUS", "X_TALEND_TIMESTAMP", "X_TALEND_TASK_ID" } };
        int[][] isNullable = { { 0, 0, 1, 1, 0, 1 }, { 0, 0, 0, 1, 0, 1 } };
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        MetadataRepository repository1 = new MetadataRepository();
        repository1.load(StorageAdaptTest.class.getResourceAsStream("schema21_1.xsd"));
        storage.prepare(repository1, true);
        try {
            assertColumnNullAble(dataSource, tables, columns, isNullable);
        } catch (SQLException e) {
            assertNull(e);
        }

        // create record before change, second_name, married is null
        String input1 = "<Product><Id>1</Id><Name>John</Name></Product>";
        String input2 = "<ProductFamily><Id>1</Id><Name>Product family #1</Name></ProductFamily>";
        try {
            createRecord(storage, factory, repository1, typeNames, new String[] { input1 });
            createRecord(storage, factory, repository1, new String[] { "ProductFamily" }, new String[] { input2 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        ComplexTypeMetadata objectType = repository1.getComplexType("Product");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
                assertEquals("John", result.get("Name"));
            }
        } finally {
            results.close();
        }
        storage.commit();

        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(StorageAdaptTest.class.getResourceAsStream("schema21_2.xsd"));
        try {
            storage.adapt(repository2, false);
        } catch (Exception e2) {
            assertNull(e2);
        }

        columns[0][3] = "X_TALEND_TIMESTAMP";
        columns[0][4] = "X_TALEND_TASK_ID";
        columns[0][5] = "X_FAMILY_NAME_X_ID";
        isNullable[0][3] = 0;
        isNullable[0][4] = 1;
        isNullable[0][5] = 1;
        try {
            assertColumnNullAble(dataSource, tables, columns, isNullable);
        } catch (SQLException e) {
            assertNull(e);
        }

        storage.begin();
        objectType = repository2.getComplexType("Product");
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();

        String input3 = input1 = "<Product><Id>2</Id><Name>John</Name><Family_Name>1</Family_Name></Product>";
        try {
            createRecord(storage, factory, repository2, typeNames, new String[] { input3 });
        } catch (Exception e1) {
            assertNull(e1);
        }

        storage.begin();
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();

        MetadataRepository repository3 = new MetadataRepository();
        repository3.load(StorageAdaptTest.class.getResourceAsStream("schema21_1.xsd"));
        try {
            storage.adapt(repository3, true);
        } catch (Exception e2) {
            assertNull(e2);
        }

        columns[0][3] = "X_FAMILY_X_ID";
        columns[0][4] = "X_TALEND_TIMESTAMP";
        columns[0][5] = "X_TALEND_TASK_ID";
        isNullable[0][3] = 1;
        isNullable[0][4] = 0;
        isNullable[0][5] = 1;
        try {
            assertColumnNullAble(dataSource, tables, columns, isNullable);
        } catch (SQLException e) {
            assertNull(e);
        }

        storage.begin();
        objectType = repository2.getComplexType("Product");
        qb = from(objectType);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();
    }

    private void assertColumnLengthChange(DataSourceDefinition dataSource, String tables, String columns, int expectedLength)
            throws SQLException {
        DataSource master = dataSource.getMaster();
        assertTrue(master instanceof RDBMSDataSource);
        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) master;
        assertEquals(RDBMSDataSource.DataSourceDialect.H2, rdbmsDataSource.getDialectName());
        Connection connection = DriverManager.getConnection(rdbmsDataSource.getConnectionURL());
        Statement statement = connection.createStatement();
        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tables);
            ResultSetMetaData metaData = resultSet.getMetaData();
            boolean hasField = false;
            for (int j = 1; j <= metaData.getColumnCount(); j++) {
                if (columns.equals(metaData.getColumnName(j))) {
                    assertEquals(expectedLength, metaData.getColumnDisplaySize(j));
                    hasField = true;
                }
            }
            assertTrue(hasField);
        } finally {
            statement.close();
            connection.close();
        }
    }

    private void assertDatabaseChange(DataSourceDefinition dataSource, String[] tables, String[] columns, boolean[] exists)
            throws SQLException {
        DataSource master = dataSource.getMaster();
        assertTrue(master instanceof RDBMSDataSource);
        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) master;
        assertEquals(RDBMSDataSource.DataSourceDialect.H2, rdbmsDataSource.getDialectName());
        Connection connection = DriverManager.getConnection(rdbmsDataSource.getConnectionURL(), rdbmsDataSource.getUserName(), rdbmsDataSource.getPassword());
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

    private void assertColumnNullAble(DataSourceDefinition dataSource, String[] tables, String[][] columns, int[][] isNullAble)
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
                assertEquals(columns[i].length - 1, metaData.getColumnCount());
                for (int j = 1; j <= metaData.getColumnCount(); j++) {
                    assertEquals(columns[i][j], metaData.getColumnName(j));
                    assertEquals(isNullAble[i][j], metaData.isNullable(j));
                }
            }
        } catch(Exception e){
            assertNotNull(e);
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

    private void createRecord(Storage storage, DataRecordReader<String> factory, MetadataRepository repository,
            String[] typeNames, String[] inputs) throws Exception {
        List<DataRecord> records = new ArrayList<DataRecord>();
        for (int i = 0; i < typeNames.length; i++) {
            records.add(factory.read(repository, repository.getComplexType(typeNames[i]), inputs[i]));
        }
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
            storage.end();
        } catch (Exception e) {
            storage.rollback();
            throw new RuntimeException(e);
        }
    }

    private void deleteLiquibaseChangeLogFile() {
        // do nothing
    }
}