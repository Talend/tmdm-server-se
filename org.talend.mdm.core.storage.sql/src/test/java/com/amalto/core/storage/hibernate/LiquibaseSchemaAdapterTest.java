package com.amalto.core.storage.hibernate;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import liquibase.change.AbstractChange;
import liquibase.change.core.DropNotNullConstraintChange;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.compare.Compare;
import org.talend.mdm.commmon.metadata.compare.ModifyChange;

import com.amalto.core.metadata.compare.CompareTest;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.adapt.StorageAdaptTest;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.datasource.RDBMSDataSource;


public class LiquibaseSchemaAdapterTest {

    private static LiquibaseSchemaAdapter adapter;

    private static MetadataRepository repository;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty(LiquibaseSchemaAdapter.MDM_ROOT_URL, System.getProperty("user.dir"));
        
        ServerContext.INSTANCE.get(new MockServerLifecycle());
        
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", "Test");
        HibernateStorage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        
        repository = new MetadataRepository();
        repository.load(LiquibaseSchemaAdapterTest.class.getResourceAsStream("liquibaseSchemeAdapt.xsd"));
        storage.prepare(repository, true);

        Set<FieldMetadata> databaseIndexedFields = new HashSet<FieldMetadata>();

        TableResolver tableResolver = new StorageTableResolver(databaseIndexedFields, ((RDBMSDataSource) dataSource.get(storage
                .getType())).getNameMaxLength());

        SessionFactoryImplementor sessionFactoryImplementor = (SessionFactoryImplementor) storage.getCurrentSession()
                .getSessionFactory();

        Dialect dialect = sessionFactoryImplementor.getDialect();
        //Connection connection = sessionFactoryImplementor.getConnectionProvider().getConnection();

        adapter = new LiquibaseSchemaAdapter(tableResolver, dialect,
                (RDBMSDataSource) storage.getDataSource(), storage.getType());
        
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        String mdmRootLocation = System.getProperty(LiquibaseSchemaAdapter.MDM_ROOT_URL).replace("file:/", "");
        String filePath = mdmRootLocation + "/data/liqubase-changelog/";
        File file = new File(filePath);
        file.deleteOnExit();
    }

    @Test
    public void testFieldTypeMetadata() throws Exception{
        
        ComplexTypeMetadata person = repository.getComplexType("Person");
        ComplexTypeMetadata e2 = repository.getComplexType("E2");

        FieldMetadata personBoyNameType = ((ContainedTypeFieldMetadata)person.getField("boy")).getContainedType().getField("name");
        FieldMetadata personBoyType = person.getField("boy");
        FieldMetadata e2BoyNameType = e2.getField("name");
        FieldMetadata personName = person.getField("name");
        
        assertTrue(adapter.isContainedComplexFieldTypeMetadata(personBoyNameType));
        assertFalse(adapter.isSimpleTypeFieldMetadata(personBoyNameType));
        assertFalse(adapter.isContainedComplexType(personBoyNameType));
        
        assertFalse(adapter.isContainedComplexFieldTypeMetadata(personBoyType));
        assertFalse(adapter.isSimpleTypeFieldMetadata(personBoyType));
        assertTrue(adapter.isContainedComplexType(personBoyType));
        
        assertFalse(adapter.isContainedComplexFieldTypeMetadata(e2BoyNameType));
        assertTrue(adapter.isSimpleTypeFieldMetadata(e2BoyNameType));
        assertFalse(adapter.isContainedComplexType(e2BoyNameType));
        
        assertFalse(adapter.isContainedComplexFieldTypeMetadata(personName));
        assertTrue(adapter.isSimpleTypeFieldMetadata(personName));
        assertFalse(adapter.isContainedComplexType(personName));
    }

    @Test
    public void testGetColumnType() throws Exception {
        ComplexTypeMetadata person = repository.getComplexType("Person");
        ComplexTypeMetadata allType = repository.getComplexType("allType");

        assertEquals("varchar(255)", adapter.getColumnTypeName(allType.getField("strField")));
        assertEquals("boolean", adapter.getColumnTypeName(allType.getField("booleanField")));
        assertEquals("smallint", adapter.getColumnTypeName(allType.getField("shortField")));
        assertEquals("integer", adapter.getColumnTypeName(allType.getField("intField")));
        assertEquals("bigint", adapter.getColumnTypeName(allType.getField("longField")));
        assertEquals("integer", adapter.getColumnTypeName(allType.getField("integerField")));
        assertEquals("float", adapter.getColumnTypeName(allType.getField("floatField")));
        assertEquals("double", adapter.getColumnTypeName(allType.getField("doubleField")));
        assertEquals("decimal(19,2)", adapter.getColumnTypeName(allType.getField("decimalField")));
        assertEquals("timestamp", adapter.getColumnTypeName(allType.getField("dateField")));
        assertEquals("timestamp", adapter.getColumnTypeName(allType.getField("datetimeField")));
        assertEquals("timestamp", adapter.getColumnTypeName(allType.getField("timeField")));
        assertEquals("varchar(50)", adapter.getColumnTypeName(allType.getField("my_def_str")));
        assertEquals("varchar(50)", adapter.getColumnTypeName(allType.getField("my_def_str_son")));
        assertEquals("decimal(5,2)", adapter.getColumnTypeName(allType.getField("my_def_decimal")));
        assertEquals("varchar(255)", adapter.getColumnTypeName(person.getField("status")));
        assertEquals("varchar(255)", adapter.getColumnTypeName(person.getField("boy")));
    }

    @Test
    public void testIsChangeMinOccursFormZeroToOneWithNMaxOccurs() throws Exception {
        MetadataRepository repository2 = new MetadataRepository();
        repository2.load(LiquibaseSchemaAdapterTest.class.getResourceAsStream("schema1.xsd"));

        ComplexTypeMetadata person = repository2.getComplexType("Person");

        assertFalse(adapter
                .isChangeMinOccursFormZeroToOneWithNMaxOccurs(person.getField("aa_0_1_1"), person.getField("aa_0_1_2")));
        assertFalse(adapter
                .isChangeMinOccursFormZeroToOneWithNMaxOccurs(person.getField("aa_0_1_1"), person.getField("aa_0_1_3")));
        assertFalse(adapter
                .isChangeMinOccursFormZeroToOneWithNMaxOccurs(person.getField("bb_1_1_1"), person.getField("bb_1_1_2")));
        assertFalse(adapter
                .isChangeMinOccursFormZeroToOneWithNMaxOccurs(person.getField("bb_1_1_1"), person.getField("bb_1_1_2")));
        assertFalse(adapter
                .isChangeMinOccursFormZeroToOneWithNMaxOccurs(person.getField("cc_0_n_1"), person.getField("cc_0_n_2")));
        assertFalse(adapter
                .isChangeMinOccursFormZeroToOneWithNMaxOccurs(person.getField("cc_0_n_1"), person.getField("cc_0_n_3")));
        assertTrue(adapter.isChangeMinOccursFormZeroToOneWithNMaxOccurs(person.getField("cc_0_n_1"), person.getField("cc_0_n_4")));
        assertFalse(adapter
                .isChangeMinOccursFormZeroToOneWithNMaxOccurs(person.getField("dd_1_n_1"), person.getField("dd_1_n_2")));
        assertFalse(adapter
                .isChangeMinOccursFormZeroToOneWithNMaxOccurs(person.getField("dd_1_n_1"), person.getField("dd_1_n_3")));
        assertTrue(adapter.isChangeMinOccursFormZeroToOneWithNMaxOccurs(person.getField("dd_1_n_1"), person.getField("dd_1_n_4")));
    }

    @Test
    public void testAnalyzeModifyChange_Complex() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(LiquibaseSchemaAdapterTest.class.getResourceAsStream("schema2_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(LiquibaseSchemaAdapterTest.class.getResourceAsStream("schema2_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);

        assertEquals(7, diffResults.getActions().size());
        assertEquals(7, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        List<AbstractChange> changeList = adapter.analyzeModifyChange(diffResults);

        assertEquals(3, changeList.size());
        assertEquals("liquibase.change.core.DropNotNullConstraintChange", changeList.get(0).getClass().getName());
        assertEquals("liquibase.change.core.DropNotNullConstraintChange", changeList.get(1).getClass().getName());
        assertEquals("liquibase.change.core.DropNotNullConstraintChange", changeList.get(2).getClass().getName());

        assertEquals("x_bb_x_talend_id", ((DropNotNullConstraintChange) changeList.get(0)).getColumnName());
        assertEquals("x_ee", ((DropNotNullConstraintChange) changeList.get(1)).getColumnName());
        assertEquals("x_uu_x_talend_id", ((DropNotNullConstraintChange) changeList.get(2)).getColumnName());
    }
}
