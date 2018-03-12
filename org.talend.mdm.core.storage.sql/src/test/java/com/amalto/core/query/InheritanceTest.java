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

import static com.amalto.core.query.user.UserQueryBuilder.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;

import com.amalto.core.query.user.BinaryLogicOperator;
import com.amalto.core.query.user.OrderBy;
import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.query.user.UserQueryHelper;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;

@SuppressWarnings("nls")
public class InheritanceTest extends StorageTestCase {

    private void populateData() {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        allRecords.add(factory.read(repository, b, "<B><id>1</id><textB>TextB</textB></B>"));
        allRecords.add(factory.read(repository, d, "<D><id>2</id><textB>TextBD</textB><textD>TextDD</textD></D>"));
        allRecords.add(factory.read(repository, persons, "<Persons><name>person</name><age>20</age></Persons>"));
        allRecords.add(factory.read(repository, employee,
                "<Employee><name>employee</name><age>21</age><jobTitle>Test</jobTitle></Employee>"));
        allRecords.add(factory.read(repository, employee,
                "<Employee><name>employee2</name><age>22</age><jobTitle>Test2</jobTitle></Employee>"));
        allRecords.add(factory.read(repository, manager,
                "<Manager><name>manager</name><age>25</age><jobTitle>Test</jobTitle><dept>manager</dept></Manager>"));
        allRecords
                .add(factory
                        .read(
                                repository,
                                a,
                                "<A xmlns:tmdm=\"http://www.talend.com/mdm\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>1</id><refB tmdm:type=\"B\">[1]</refB><textA>TextA</textA><nestedB xsi:type=\"Nested\"><text>Text</text></nestedB></A>"));
        allRecords
                .add(factory
                        .read(
                                repository,
                                a,
                                "<A xmlns:tmdm=\"http://www.talend.com/mdm\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>3</id><refB tmdm:type=\"D\">[2]</refB><textA>TextA</textA><nestedB xsi:type=\"Nested\"><text>Text</text></nestedB></A>"));
        allRecords
                .add(factory
                        .read(
                                repository,
                                c,
                                "<C xmlns:tmdm=\"http://www.talend.com/mdm\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>2</id><refB tmdm:type=\"D\">[2]</refB><textA>TextAC</textA><nestedB xsi:type=\"SubNested\"><text>Text</text><subText>SubText</subText></nestedB><textC>TextCC</textC></C>"));
        allRecords
                .add(factory
                        .read(
                                repository,
                                ss,
                                "<SS xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>1</id><nestedB xsi:type=\"Nested\"><text>Text</text></nestedB></SS>"));
        allRecords
                .add(factory
                        .read(
                                repository,
                                ss,
                                "<SS xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>2</id><nestedB xsi:type=\"SubNested\"><text>Text</text><subText>SubText</subText></nestedB></SS>"));

        try {
            storage.begin();
            storage.update(allRecords);
            storage.commit();
        } finally {
            storage.end();
        }
    }

    @Override
    public void setUp() throws Exception {
        populateData();
        super.setUp();
        userSecurity.setActive(false); // Not testing security here
    }

    public void testTypeSubSetOrdering() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(InheritanceTest.class.getResourceAsStream("TypeOrdering.xsd"));
        List<ComplexTypeMetadata> sortedList = MetadataUtils.sortTypes(repository);
        
        // test for Strict 
        // Customer(1,1)==>Address(ComplexType) Address(1-1)==>Country
        boolean hasCountry = false ;
        boolean hasAddress = false ;
        for (ComplexTypeMetadata sortType : sortedList) {
            if("Country".equals(sortType.getName())){
                hasCountry = true ;
            }
            if ("Address".equals(sortType.getName())) {
                assertTrue(hasCountry);
                hasAddress = true;
            }
        }
        assertTrue(hasAddress);
        
        // test for LENIENT 
        sortedList = MetadataUtils.sortTypes(repository,MetadataUtils.SortType.LENIENT);
        boolean hasA = false;
        boolean hasB = false;
        boolean hasC = false;
        boolean hasD = false;
        boolean hasE = false;
        
        boolean hasPerons = false;
        boolean hasEmployee = false;
        boolean hasEmployee1 = false;
        boolean hasManager = false;
        boolean hasManager1 = false;
        boolean hasCompany = false;
        
        boolean hasProduct = false;
        boolean hasProductFamily = false;
        boolean hasSupplier = false;
        boolean hasStore = false;
        hasAddress = false;
        
        for (ComplexTypeMetadata sortType : sortedList) {
            // A(0,1)==>B, A(0,1)==>A, C-->A, D-->B, E-->B
            if ("A".equals(sortType.getName())) {
                hasA = true;
            }
            if ("B".equals(sortType.getName())) {
                hasB = true;
            }
            if ("C".equals(sortType.getName())) {
                assertTrue(hasA);
                hasC = true;
            }
            if ("D".equals(sortType.getName())) {
                assertTrue(hasB);
                hasD = true;
            }
            if ("E".equals(sortType.getName())) {
                assertTrue(hasB);
                hasE = true;
            }
            
            // Employee-->Persons, Manager-->Employee, Employee1(0,1) ==> Manager1,
            if ("Persons".equals(sortType.getName())) {
                hasPerons = true;
            }
            if ("Employee".equals(sortType.getName())) {
                assertTrue(hasPerons);
                hasEmployee = true;
            }
            if ("Manager".equals(sortType.getName())) {
                assertTrue(hasEmployee);
                hasManager = true;
            }
            if ("Company".equals(sortType.getName())) {
                hasCompany = true;
            }
            if ("Employee1".equals(sortType.getName())) {
                assertTrue(hasManager1);
                hasEmployee1 = true;
            }
            if ("Manager1".equals(sortType.getName())) {
                hasManager1 = true;
            }
            
            // Product(0,1) ==> ProductFamily, Product(0,unbounded) ==> Supplier, Product(0,1) ==> Store,
            // Supplier(0,1) ==> Address, Address (1,1)=> Country
            if ("Product".equals(sortType.getName())) {
                assertTrue(hasProductFamily);
                assertTrue(hasSupplier);
                assertTrue(hasStore);
                hasProduct = true;
            }
            if ("ProductFamily".equals(sortType.getName())) {
                hasProductFamily = true;
            }
            if ("Store".equals(sortType.getName())) {
                hasStore = true;
            }
            if ("Supplier".equals(sortType.getName())) {
                assertTrue(hasAddress);
                hasSupplier = true;
            }

            if ("Country".equals(sortType.getName())) {
                hasCountry = true;
            }
            if ("Address".equals(sortType.getName())) {
                assertTrue(hasCountry);
                hasAddress = true;
            }
        }
        assertTrue(hasC);
        assertTrue(hasD);
        assertTrue(hasE);
        assertTrue(hasEmployee1);
        assertTrue(hasManager);
        assertTrue(hasCompany);
        assertTrue(hasProduct);
    }

    public void testInheritanceQuery() throws Exception {
        // SuperType Query (Persons exist subtype)
        UserQueryBuilder qb = UserQueryBuilder.from(persons);
        qb.isa(persons);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            int actualCount = 0;
            for (DataRecord result : results) {
                actualCount++;
            }
            assertEquals(1, actualCount);
        } finally {
            results.close();
        }

        // SubType Query (employee exist super type and subtype)
        qb = UserQueryBuilder.from(employee);
        qb.select(employee.getField("name"));
        qb.isa(employee);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            int actualCount = 0;
            for (DataRecord result : results) {
                actualCount++;
            }
            assertEquals(2, actualCount);
        } finally {
            results.close();
        }

        // SubType Query (manager exist super type, no subtype)
        qb = UserQueryBuilder.from(manager);
        qb.isa(manager);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testIsAForInheritance() throws Exception {
        // SuperType Query (Persons exist subtype)
        UserQueryBuilder qb = UserQueryBuilder.from(persons);
        qb.isa(persons);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            int actualCount = 0;
            for (DataRecord result : results) {
                actualCount++;
            }
            assertEquals(1, actualCount);
        } finally {
            results.close();
        }

        qb = UserQueryBuilder.from(persons);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
            int actualCount = 0;
            for (DataRecord result : results) {
                actualCount++;
            }
            assertEquals(4, actualCount);
        } finally {
            results.close();
        }

        // SubType Query (employee exist super type and subtype)
        qb = UserQueryBuilder.from(employee);
        qb.select(employee.getField("name"));
        qb.isa(employee);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            int actualCount = 0;
            for (DataRecord result : results) {
                actualCount++;
            }
            assertEquals(2, actualCount);
        } finally {
            results.close();
        }

        qb = UserQueryBuilder.from(employee);
        qb.select(employee.getField("name"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
            int actualCount = 0;
            for (DataRecord result : results) {
                actualCount++;
            }
            assertEquals(3, actualCount);
        } finally {
            results.close();
        }

        // SubType Query (manager exist super type, no subtype)
        qb = UserQueryBuilder.from(manager);
        qb.isa(manager);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = UserQueryBuilder.from(manager);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testNonAssignableIsa() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(a);
        try {
            qb.isa(persons);
            fail("Persons is not assignable from type A. Expected an exception.");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void testSimpleQuery() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(d);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = UserQueryBuilder.from(c);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testSimpleInheritanceQuery() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(b);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }

        qb = UserQueryBuilder.from(a);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testQueryWithInstanceCheck() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(b).isa(d);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testDefaultFKType() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(a);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
            for (DataRecord result : results) {
                Object value = result.get("refB");
                assertTrue(value instanceof DataRecord);
                DataRecord dataValue = (DataRecord) value;
                String refType;
                if ("A".equals(result.getType().getName())) {
                    refType = dataValue.getType().getName();
                    assertTrue(result.get("refB") instanceof DataRecord);
                    if ("B".equals(refType)) {
                        assertEquals("1", ((DataRecord) result.get("refB")).get("id"));
                    } else if ("D".equals(refType)) {
                        assertEquals("2", ((DataRecord) result.get("refB")).get("id"));
                    }
                } else if ("C".equals(result.getType().getName())) {
                    assertEquals("D", dataValue.getType().getName());
                    assertTrue(result.get("refB") instanceof DataRecord);
                    assertEquals("2", ((DataRecord) result.get("refB")).get("id"));
                }
            }
        } finally {
            results.close();
        }
    }

    public void testFKType() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(c);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                Object value = result.get("refB");
                assertTrue(value instanceof DataRecord);
                assertEquals("D", ((DataRecord) value).getType().getName());
            }
        } finally {
            results.close();
        }
    }

    public void testDefaultNestedType() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(a);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
            for (DataRecord result : results) {
                Object value = result.get("nestedB");
                assertTrue(value instanceof DataRecord);
                if ("A".equals(result.getType().getName())) {
                    assertEquals("Nested", ((DataRecord) value).getType().getName());
                } else if ("C".equals(result.getType().getName())) {
                    assertEquals("SubNested", ((DataRecord) value).getType().getName());
                }
            }
        } finally {
            results.close();
        }
    }

    public void testNestedType() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(c);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                Object value = result.get("nestedB");
                assertTrue(value instanceof DataRecord);
                assertEquals("SubNested", ((DataRecord) value).getType().getName());
            }
        } finally {
            results.close();
        }
    }

    public void testJoin() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(c).and(b).select(c.getField("textC")).select(b.getField("textB"))
                .join(c.getField("refB"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("TextBD", result.get("textB"));
                assertEquals("TextCC", result.get("textC"));
            }
        } finally {
            results.close();
        }
    }

    public void testJoinWithInheritance() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(a).and(b).select(a.getField("textA")).select(b.getField("textB"))
                .join(a.getField("refB"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
            for (DataRecord result : results) {
                assertTrue("TextA".equals(result.get("textA")) || "TextAC".equals(result.get("textA")));
                assertTrue("TextB".equals(result.get("textB")) || "TextBD".equals(result.get("textB")));
            }
        } finally {
            results.close();
        }
    }

    public void testIsa() throws Exception {
        ComplexTypeMetadata subNested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "SubNested");
        assertNotNull(subNested);
        ComplexTypeMetadata nested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "Nested");
        assertNotNull(nested);
        // Test 1
        UserQueryBuilder qb = UserQueryBuilder.from(a).where(isa(a.getField("nestedB"), subNested));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(c, result.getType());
            }
        } finally {
            results.close();
        }
        // Test 2
        qb = UserQueryBuilder.from(a).where(or(isa(a.getField("nestedB"), nested), isa(a.getField("nestedB"), subNested)));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }
        // Test 3
        qb = UserQueryBuilder.from(a).where(isa(a.getField("nestedB"), nested));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testXsiTypeProjection() throws Exception {
        ComplexTypeMetadata subNested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "SubNested");
        assertNotNull(subNested);
        ComplexTypeMetadata nested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "Nested");
        assertNotNull(nested);
        // Test 1
        UserQueryBuilder qb = UserQueryBuilder.from(a).select(alias(type(a.getField("nestedB")), "type"))
                .where(isa(a.getField("nestedB"), subNested));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(subNested.getName(), result.get("type"));
            }
        } finally {
            results.close();
        }
    }

    public void testXsiTypeProjectionWithContains() throws Exception {
        ComplexTypeMetadata subNested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "SubNested");
        assertNotNull(subNested);
        ComplexTypeMetadata nested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "Nested");
        assertNotNull(nested);
        // Test 1
        UserQueryBuilder qb = UserQueryBuilder.from(a).select(alias(type(a.getField("nestedB")), "type"))
                .where(contains(a.getField("textA"), "TextAC"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(subNested.getName(), result.get("type"));
            }
        } finally {
            results.close();
        }
        // Test 2
        qb = UserQueryBuilder.from(a).select(alias(type(a.getField("nestedB")), "type"))
                .where(contains(a.getField("textA"), "TextAC")).start(0).limit(20);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(subNested.getName(), result.get("type"));
            }
        } finally {
            results.close();
        }
    }

    public void testXsiTypeProjectionWithOrderBy() throws Exception {
        ComplexTypeMetadata subNested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "SubNested");
        assertNotNull(subNested);
        ComplexTypeMetadata nested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "Nested");
        assertNotNull(nested);
        // Test 1
        UserQueryBuilder qb = UserQueryBuilder.from(a).select(alias(type(a.getField("nestedB")), "type"))
                .orderBy(type(a.getField("nestedB")), OrderBy.Direction.ASC);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
            String[] expected = new String[] { "Nested", "Nested", "SubNested" };
            int i = 0;
            for (DataRecord result : results) {
                assertEquals(expected[i++], result.get("type"));
            }
        } finally {
            results.close();
        }
        // Test 2
        qb = UserQueryBuilder.from(a).select(alias(type(a.getField("nestedB")), "type"))
                .orderBy(type(a.getField("nestedB")), OrderBy.Direction.DESC);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
            String[] expected = new String[] { "SubNested", "Nested", "Nested" };
            int i = 0;
            for (DataRecord result : results) {
                assertEquals(expected[i++], result.get("type"));
            }
        } finally {
            results.close();
        }
        // Test 3
        qb = UserQueryBuilder.from(a).select(alias(type(a.getField("nestedB")), "type"));
        List<TypedExpression> fields = UserQueryHelper.getFields(a, "nestedB/@xsi:type");
        for (TypedExpression field : fields) {
            qb.orderBy(field, OrderBy.Direction.ASC);
        }
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
            String[] expected = new String[] { "Nested", "Nested", "SubNested" };
            int i = 0;
            for (DataRecord result : results) {
                assertEquals(expected[i++], result.get("type"));
            }
        } finally {
            results.close();
        }
    }

    public void testXsiTypeProjectionWithIdFilter() throws Exception {
        ComplexTypeMetadata subNested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "SubNested");
        assertNotNull(subNested);
        ComplexTypeMetadata nested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "Nested");
        assertNotNull(nested);
        // Test 1
        UserQueryBuilder qb = UserQueryBuilder.from(a).select(alias(type(a.getField("nestedB")), "type"))
                .where(and(isa(a.getField("nestedB"), nested), eq(a.getField("id"), "1")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(nested.getName(), result.get("type"));
            }
        } finally {
            results.close();
        }
        // Test 2
        qb = UserQueryBuilder.from(a).select(alias(type(a.getField("nestedB")), "type")).where(eq(a.getField("id"), "1"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(nested.getName(), result.get("type"));
            }
        } finally {
            results.close();
        }
    }

    public void testIsaFromWhereItem() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(a);
        IWhereItem item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition("A/nestedB/xsi:type", WhereCondition.EQUALS,
                "SubNested", WhereCondition.NO_OPERATOR)));
        qb.getSelect().setCondition(UserQueryHelper.buildCondition(qb, item, repository));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(c, result.getType());
            }
        } finally {
            results.close();
        }
    }

    public void testIsaOnFK() throws Exception {
        try {
            UserQueryBuilder.from(a).where(isa(a.getField("refB"), d));
            fail("Expected exception: can perform 'is a' on a FK");
        } catch (Exception e) {
            // Expected.
        }
    }

    public void testIsaOnFKFromWhereItem() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(a);
        IWhereItem item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition("A/refB/tmdm:type", WhereCondition.EQUALS,
                "D", WhereCondition.NO_OPERATOR)));
        try {
            qb.getSelect().setCondition(UserQueryHelper.buildCondition(qb, item, repository));
            fail("Expected exception: can perform 'is a' on a FK");
        } catch (Exception e) {
            // Expected.
        }
    }

    public void testXsiIsEmptyOrNull() throws Exception {
        ComplexTypeMetadata subNested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "SubNested");
        assertNotNull(subNested);
        ComplexTypeMetadata nested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "Nested");
        assertNotNull(nested);
        // Test 1
        UserQueryBuilder qb = UserQueryBuilder.from(ss).where(emptyOrNull(type(ss.getField("nestedB"))));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                DataRecord value = (DataRecord) result.get("nestedB");
                assertEquals("Nested", value.getType().getName());
            }
        } finally {
            results.close();
        }
        // Test 2
        qb = from(ss);
        List<IWhereItem> conditions = new ArrayList<IWhereItem>();
        conditions.add(new WhereCondition("SS/nestedB/@xsi:type", WhereCondition.EMPTY_NULL, "", "&"));
        IWhereItem fullWhere = new WhereAnd(conditions);
        BinaryLogicOperator condition = (BinaryLogicOperator) UserQueryHelper.buildCondition(qb, fullWhere, repository);
        assertNotNull(condition);
        qb.where(condition);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testXsiTypeOrderBy() throws Exception {
        ComplexTypeMetadata subNested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "SubNested");
        assertNotNull(subNested);
        ComplexTypeMetadata nested = (ComplexTypeMetadata) repository.getNonInstantiableType("", "Nested");
        assertNotNull(nested);
        // Test 1
        UserQueryBuilder qb = UserQueryBuilder.from(ss).select(alias(type(ss.getField("nestedB")), "type"))
                .orderBy(type(ss.getField("nestedB")), OrderBy.Direction.ASC);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            String[] expected = new String[] { "Nested", "SubNested" };
            int i = 0;
            for (DataRecord result : results) {
                assertEquals(expected[i++], result.get("type"));
            }
        } finally {
            results.close();
        }
        // Test 2
        qb = UserQueryBuilder.from(ss).select(alias(type(ss.getField("nestedB")), "type"))
                .orderBy(type(ss.getField("nestedB")), OrderBy.Direction.DESC);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            String[] expected = new String[] { "SubNested", "Nested" };
            int i = 0;
            for (DataRecord result : results) {
                assertEquals(expected[i++], result.get("type"));
            }
        } finally {
            results.close();
        }
        // Test 3
        qb = UserQueryBuilder.from(ss).select(alias(type(ss.getField("nestedB")), "type"));
        List<TypedExpression> fields = UserQueryHelper.getFields(ss, "nestedB/@xsi:type");
        for (TypedExpression field : fields) {
            qb.orderBy(field, OrderBy.Direction.ASC);
        }
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            String[] expected = new String[] { "Nested", "SubNested" };
            int i = 0;
            for (DataRecord result : results) {
                assertEquals(expected[i++], result.get("type"));
            }
        } finally {
            results.close();
        }
    }

    public void testInheritanceCount() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(employee);
        StorageResults results = storage.fetch(qb.getSelect());
        int i = 0;
        try {
            for (DataRecord result : results) {
                i++;
            }
        } finally {
            results.close();
        }

        qb = UserQueryBuilder.from(employee);
        results = storage.fetch(qb.getSelect());
        assertEquals(i, results.getCount());

        qb = UserQueryBuilder.from(persons);
        results = storage.fetch(qb.getSelect());
        assertTrue(i < results.getCount());
    }
    
    public void testInheritanceCountClassLoader() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(employee);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
            }
        } finally {
            results.close();
        }

        ClassLoader StorageClassLoader1 = (ClassLoader) Thread.currentThread().getContextClassLoader();

        qb = UserQueryBuilder.from(employee);
        results = storage.fetch(qb.getSelect());
        results.getCount();

        ClassLoader StorageClassLoader2 = (ClassLoader) Thread.currentThread().getContextClassLoader();

        qb = UserQueryBuilder.from(persons);
        results = storage.fetch(qb.getSelect());
        results.getCount();

        ClassLoader StorageClassLoader3 = (ClassLoader) Thread.currentThread().getContextClassLoader();

        assertEquals(StorageClassLoader1, StorageClassLoader2);
        assertEquals(StorageClassLoader1, StorageClassLoader3);
    }
}
