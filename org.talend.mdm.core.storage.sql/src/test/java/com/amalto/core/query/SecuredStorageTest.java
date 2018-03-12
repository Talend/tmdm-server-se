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

import java.util.LinkedList;
import java.util.List;

import com.amalto.core.query.user.OrderBy;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;

@SuppressWarnings("nls")
public class SecuredStorageTest extends StorageTestCase {

    private void populateData() {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();

        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        allRecords
                .add(factory
                        .read(
                                repository,
                                person,
                                "<Person><id>1</id><score>130000</score><lastname>Dupond</lastname><middlename>John</middlename><firstname>Julien</firstname><age>10</age><Status>Employee</Status><Available>true</Available></Person>"));
        allRecords
                .add(factory
                        .read(
                                repository,
                                person,
                                "<Person><id>2</id><score>170000</score><lastname>Dupont</lastname><middlename>John</middlename><firstname>Robert-Julien</firstname><age>20</age><Status>Customer</Status><Available>false</Available></Person>"));
        allRecords
                .add(factory
                        .read(
                                repository,
                                person,
                                "<Person><id>3</id><score>200000</score><lastname>Leblanc</lastname><middlename>John</middlename><firstname>Juste</firstname><age>30</age><Status>Friend</Status></Person>"));

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
        userSecurity.setActive(true); // This test is about security...
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        try {
            storage.begin();
            {
                UserQueryBuilder qb = from(person);
                storage.delete(qb.getSelect());
            }
            storage.commit();
        } finally {
            storage.end();
        }

        userSecurity.setActive(true);
    }

    public void testUnsecuredField() throws Exception {
        assertTrue(storage instanceof SecuredStorage);

        UserQueryBuilder qb = from(person).selectId(person).select(person.getField("firstname"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get("id"));
                assertNotNull(result.get("firstname"));
            }
        } finally {
            results.close();
        }
    }

    public void testSecuredField() throws Exception {
        assertTrue(storage instanceof SecuredStorage);

        // With security inactive
        assertTrue(userSecurity.isActive);
        UserQueryBuilder qb = from(person).selectId(person).select(person.getField("firstname"))
                .select(person.getField("Status"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }

        // With security inactive
        userSecurity.setActive(false);
        assertFalse(userSecurity.isActive);
        qb = from(person).selectId(person).select(person.getField("firstname")).select(person.getField("Status"));

        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testSecuredFieldCondition() throws Exception {
        assertTrue(storage instanceof SecuredStorage);

        // With security active
        assertTrue(userSecurity.isActive);
        UserQueryBuilder qb = from(person).selectId(person).select(person.getField("firstname"))
                .where(eq(person.getField("Status"), "Customer"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get("id"));
                assertNotNull(result.get("firstname"));
            }
        } finally {
            results.close();
        }

        // With security inactive
        userSecurity.setActive(false);
        assertFalse(userSecurity.isActive);
        qb = from(person).selectId(person).select(person.getField("firstname")).where(eq(person.getField("Status"), "Customer"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get("id"));
                assertNotNull(result.get("firstname"));
            }
        } finally {
            results.close();
        }
    }

    public void testSecuredFieldMultipleCondition() throws Exception {
        assertTrue(storage instanceof SecuredStorage);

        // With security active
        assertTrue(userSecurity.isActive);
        UserQueryBuilder qb = from(person).selectId(person).select(person.getField("firstname"))
                .where(and(eq(person.getField("Status"), "Customer"), eq(person.getField("middlename"), "John")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get("id"));
                assertNotNull(result.get("firstname"));
            }
        } finally {
            results.close();
        }

        // With security inactive
        userSecurity.setActive(false);
        assertFalse(userSecurity.isActive);
        qb = from(person).selectId(person).select(person.getField("firstname"))
                .where(and(eq(person.getField("Status"), "Customer"), eq(person.getField("middlename"), "John")));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get("id"));
                assertNotNull(result.get("firstname"));
            }
        } finally {
            results.close();
        }
    }

    public void testSecuredFieldOrderBy() throws Exception {
        storage.rollback(); // Cancel transaction begun in setUp();
        assertTrue(storage instanceof SecuredStorage);

        // With security active
        assertTrue(userSecurity.isActive);
        UserQueryBuilder qb = from(person).selectId(person).select(person.getField("firstname"))
                .orderBy(person.getField("Status"), OrderBy.Direction.DESC)
                .orderBy(person.getField("id"), OrderBy.Direction.DESC);

        String[] firstNames = new String[3];
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
            int i = 0;
            for (DataRecord result : results) {
                assertNotNull(result.get("id"));
                assertNotNull(result.get("firstname"));
                firstNames[i++] = String.valueOf(result.get("firstname"));
            }
        } finally {
            results.close();
            storage.commit();
        }

        // With security active
        userSecurity.setActive(false);
        assertFalse(userSecurity.isActive);
        qb = from(person).selectId(person).select(person.getField("firstname"))
                .orderBy(person.getField("Status"), OrderBy.Direction.ASC)
                .orderBy(person.getField("id"), OrderBy.Direction.ASC);
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
            int i = 0;
            boolean hasOrderChanged = false;
            for (DataRecord result : results) {
                assertNotNull(result.get("id"));
                assertNotNull(result.get("firstname"));
                hasOrderChanged |= !firstNames[i++].equals(result.get("firstname"));
            }
            assertTrue(hasOrderChanged);
        } finally {
            results.close();
            storage.commit();
        }
        storage.begin(); // Here for tearDown()
    }

}
