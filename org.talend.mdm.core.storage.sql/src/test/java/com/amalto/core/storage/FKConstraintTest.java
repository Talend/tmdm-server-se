/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;

import junit.framework.TestCase;

/**
 * Test case for FK Constraint
 * created by Pingwen Lin on 2015-7-2 <br>
 * Tables will be: entity_a1, entity_a2, entity_c, x_type_b1, x_type_b1_x_c_id_entity_c, x_type_b2, x_type_b2_x_c_id_entity_c<br>
 * The usage size of Type_B1 is <b>1</b>, the usage size of Type_B2 is <b>2</b>. So: <br>
 * <li>For MASTER, column 'x_type_b1_x_c_id_entity_c.x_c_id_x_c_id' <b>WILL</b> add FK Constraint, column 'x_type_b2_x_c_id_entity_c.x_c_id_x_c_id' <b>WON'T</b> add FK Constraint</li>
 * <li>For STAGING, <b>NONE</b> of them will add FK Constraint</li>
 *
 */
@SuppressWarnings("nls")
public class FKConstraintTest extends TestCase {

    private static Logger LOG = LogManager.getLogger(FKConstraintTest.class);

    private static String ENTITY_A1_EMPTY = "<Entity_A1><A1_Id>A1</A1_Id><A1_Name>A1 Name</A1_Name><B1><B1_Name>B1 Name</B1_Name></B1></Entity_A1>";

    private static String ENTITY_A1_1 = "<Entity_A1><A1_Id>A1</A1_Id><A1_Name>A1 Name</A1_Name><B1><B1_Name>B1 Name</B1_Name><C_Id>[C1]</C_Id><C_Id>[C2]</C_Id></B1></Entity_A1>";

    private static String ENTITY_A1_2 = "<Entity_A1><A1_Id>A1</A1_Id><A1_Name>A1 Name</A1_Name><B1><B1_Name>B1 Name</B1_Name><C_Id>[C3]</C_Id><C_Id>[C4]</C_Id></B1></Entity_A1>";

    private static String ENTITY_A2_1 = "<Entity_A2><A2_Id>A2</A2_Id><A2_Name>A2 Name</A2_Name><B2_1><B2_Name>B2_1</B2_Name><C_Id>[C1]</C_Id></B2_1><B2_2><B2_Name>B2_2</B2_Name><C_Id>[C2]</C_Id></B2_2></Entity_A2>";

    private static String ENTITY_A2_2 = "<Entity_A2><A2_Id>A2</A2_Id><A2_Name>A2 Name</A2_Name><B2_1><B2_Name>B2_1</B2_Name><C_Id>[C3]</C_Id></B2_1><B2_2><B2_Name>B2_2</B2_Name><C_Id>[C4]</C_Id></B2_2></Entity_A2>";

    private static String ENTITY_C1 = "<Entity_C><C_Id>C1</C_Id><C_Name>C1 Name</C_Name></Entity_C>";

    private static String ENTITY_C2 = "<Entity_C><C_Id>C2</C_Id><C_Name>C2 Name</C_Name></Entity_C>";

    private static String ENTITY_C3 = "<Entity_C><C_Id>C3</C_Id><C_Name>C3 Name</C_Name></Entity_C>";

    private static String ENTITY_C4 = "<Entity_C><C_Id>C4</C_Id><C_Name>C4 Name</C_Name></Entity_C>";

    public void testDeleteFKTable() {
        ServerContext.INSTANCE.get(new MockServerLifecycle());

        Storage storage = new HibernateStorage("MDM", StorageType.MASTER);
        MetadataRepository repository = new MetadataRepository();
        repository.load(FKConstraintTest.class.getResourceAsStream("FKConstraintTest.xsd"));
        storage.init(ServerContext.INSTANCE.get().getDefinition("H2-DS1", "MDM"));
        storage.prepare(repository, true);

        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        ComplexTypeMetadata entityA1 = repository.getComplexType("Entity_A1");
        ComplexTypeMetadata entityA2 = repository.getComplexType("Entity_A2");
        ComplexTypeMetadata entityC = repository.getComplexType("Entity_C");

        List<DataRecord> recordCs = new LinkedList<DataRecord>();
        recordCs.add(factory.read(repository, entityC, ENTITY_C1));
        recordCs.add(factory.read(repository, entityC, ENTITY_C2));
        recordCs.add(factory.read(repository, entityC, ENTITY_C3));
        try {
            storage.begin();
            storage.update(recordCs);
            storage.commit();
        } finally {
            storage.end();
        }

        try {
            storage.begin();
            storage.update(factory.read(repository, entityA1, ENTITY_A1_EMPTY));
            storage.commit();
        } finally {
            storage.end();
        }

        UserQueryBuilder qb = from(entityA1);
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());

        try {
            storage.begin();
            storage.update(factory.read(repository, entityA2, ENTITY_A2_1));
            storage.commit();
        } finally {
            storage.end();
        }

        qb = from(entityA2);
        results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());

        // ENTITY_A1_EMPTY has no FK record, so delete this table will success
        Exception e_a11 = null;
        qb = from(entityA1);
        try {
            storage.begin();
            storage.delete(qb.getSelect());
            storage.commit();
        } catch (Exception e) {
            e_a11 = e;
        } finally {
            storage.end();
        }
        assertNull(e_a11);

        qb = from(entityA1);
        results = storage.fetch(qb.getSelect());
        assertEquals(0, results.getCount());

        // ENTITY_C1 is FK of entity A2, so delete this table will fail
        qb = from(entityC).where(eq(entityC.getField("C_Id"), "C1"));
        try {
            storage.begin();
            storage.delete(qb.getSelect());
            storage.commit();
        } catch (RuntimeException e) {
            e_a11 = e;
        } finally {
            storage.end();
        }
        assertNotNull(e_a11);

        results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
    }

    public void testMaster(){
        LOG.info("Setting up MDM server environment...");
        ServerContext.INSTANCE.get(new MockServerLifecycle());
        LOG.info("MDM server environment set.");

        LOG.info("Preparing storage for Master tests...");
        Storage storage = new HibernateStorage("MDM", StorageType.MASTER);
        MetadataRepository repository = new MetadataRepository();
        repository.load(FKConstraintTest.class.getResourceAsStream("FKConstraintTest.xsd"));
        storage.init(ServerContext.INSTANCE.get().getDefinition("H2-DS1", "MDM"));
        storage.prepare(repository, true);

        LOG.info("Preparing datas for Master tests...");
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        ComplexTypeMetadata entityA1 = repository.getComplexType("Entity_A1");
        ComplexTypeMetadata entityA2 = repository.getComplexType("Entity_A2");
        ComplexTypeMetadata entityC = repository.getComplexType("Entity_C");

        List<DataRecord> recordCs = new LinkedList<DataRecord>();
        recordCs.add(factory.read(repository, entityC, ENTITY_C1));
        recordCs.add(factory.read(repository, entityC, ENTITY_C2));
        recordCs.add(factory.read(repository, entityC, ENTITY_C3));
        try {
            storage.begin();
            storage.update(recordCs);
            storage.commit();
        } finally {
            storage.end();
        }

        // Test insert with FK, Type_B1's usage size=1, MASTER will create FK, so ENTITY_A1_1 will success, ENTITY_A1_2 will fail
        Exception e_a11 = null;
        try {
            storage.begin();
            storage.update(factory.read(repository, entityA1, ENTITY_A1_1));
            storage.commit();
        } catch(Exception e){
            e_a11 = e;
        } finally {
            storage.end();
        }
        assertNull(e_a11);

        Exception e_a12 = null;
        try {
            storage.begin();
            storage.update(factory.read(repository, entityA1, ENTITY_A1_2));
            storage.commit();
        } catch(Exception e){
            e_a12 = e;
        } finally {
            storage.end();
        }
        storage.commit();
        assertNotNull(e_a12);

        // Test insert without FK, Type_B2's usage size=2, won't create FK, so ENTITY_A2_1 will success, ENTITY_A2_2 will success too
        try {
            storage.begin();
            storage.update(factory.read(repository, entityC, ENTITY_C4));
            storage.commit();
        } finally {
            storage.end();
        }

        storage.begin();
        UserQueryBuilder qb = from(entityC);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }

        Exception e_a21 = null;
        try {
            storage.begin();
            storage.update(factory.read(repository, entityA2, ENTITY_A2_1));
            storage.commit();
        } catch(Exception e){
            e_a21 = e;
        } finally {
            storage.end();
        }
        assertNull(e_a21);

        Exception e_a22 = null;
        try {
            storage.begin();
            storage.update(factory.read(repository, entityA2, ENTITY_A2_2));
            storage.commit();
        } catch(Exception e){
            e_a22 = e;
        } finally {
            storage.end();
        }
        assertNull(e_a22);
    }

    public void testStaging(){
        LOG.info("Setting up MDM server environment...");
        ServerContext.INSTANCE.get(new MockServerLifecycle());
        LOG.info("MDM server environment set.");

        LOG.info("Preparing storage for Staging tests...");
        Storage storage = new HibernateStorage("MDM", StorageType.STAGING);
        MetadataRepository repository = new MetadataRepository();
        repository.load(FKConstraintTest.class.getResourceAsStream("FKConstraintTest.xsd"));
        storage.init(ServerContext.INSTANCE.get().getDefinition("H2-DS1", "MDM"));
        storage.prepare(repository, true);

        LOG.info("Preparing datas for Staging tests...");
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        ComplexTypeMetadata entityA1 = repository.getComplexType("Entity_A1");
        ComplexTypeMetadata entityA2 = repository.getComplexType("Entity_A2");
        ComplexTypeMetadata entityC = repository.getComplexType("Entity_C");

        List<DataRecord> recordCs = new LinkedList<DataRecord>();
        recordCs.add(factory.read(repository, entityC, ENTITY_C1));
        recordCs.add(factory.read(repository, entityC, ENTITY_C2));
        try {
            storage.begin();
            storage.update(recordCs);
            storage.commit();
        } finally {
            storage.end();
        }

        // Test insert without FK, Type_B1's usage size=1, but STAGING won't create FK, so ENTITY_A1_1 will success, ENTITY_A1_2 will success too
        Exception e_a11 = null;
        try {
            storage.begin();
            storage.update(factory.read(repository, entityA1, ENTITY_A1_1));
            storage.commit();
        } catch(Exception e){
            e_a11 = e;
        } finally {
            storage.end();
        }
        assertNull(e_a11);

        Exception e_a12 = null;
        try {
            storage.begin();
            storage.update(factory.read(repository, entityA1, ENTITY_A1_2));
            storage.commit();
        } catch(Exception e){
            e_a12 = e;
        } finally {
            storage.end();
        }
        assertNull(e_a12);

        // Test insert without FK, Type_B2's usage size=2, STAGING won't create FK, so ENTITY_A2_1 will success, ENTITY_A2_2 will success too
        Exception e_a21 = null;
        try {
            storage.begin();
            storage.update(factory.read(repository, entityA2, ENTITY_A2_1));
            storage.commit();
        } catch(Exception e){
            e_a21 = e;
        } finally {
            storage.end();
        }
        assertNull(e_a21);

        Exception e_a22 = null;
        try {
            storage.begin();
            storage.update(factory.read(repository, entityA2, ENTITY_A2_2));
            storage.commit();
        } catch(Exception e){
            e_a22 = e;
        } finally {
            storage.end();
        }
        assertNull(e_a22);
    }
}
