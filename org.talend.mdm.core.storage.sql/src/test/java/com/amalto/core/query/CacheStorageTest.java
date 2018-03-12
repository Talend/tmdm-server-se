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

import static com.amalto.core.query.user.UserQueryBuilder.contains;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.storage.CacheStorage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("nls")
public class CacheStorageTest extends StorageTestCase {

    private CacheStorage cacheStorage;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cacheStorage = new CacheStorage(storage);
    }

    @Override
    public void tearDown() throws Exception {
        cacheStorage.begin();
        cacheStorage.delete(from(country).getExpression());
        cacheStorage.commit();
    }

    public void testCache() throws Exception {
        UserQueryBuilder qb = from(person).selectId(person).cache();
        Select select = qb.getSelect();
        assertFalse(cacheStorage.hasCache(select));
        cacheStorage.fetch(select);
        assertTrue(cacheStorage.hasCache(select));
        cacheStorage.fetch(select);
    }

    public void testUsageEviction() throws Exception {
        UserQueryBuilder qb = from(person).selectId(person).cache();
        Select select = qb.getSelect();
        assertFalse(cacheStorage.hasCache(select));
        cacheStorage.fetch(select);
        assertTrue(cacheStorage.hasCache(select));
        for (int i = 0; i < cacheStorage.getMaxCacheEntryUsage() - 1; i++) {
            cacheStorage.fetch(select);
        }
        assertEquals(1, cacheStorage.getCacheEntryUsage(select));
        cacheStorage.fetch(select);
        assertEquals(cacheStorage.getMaxCacheEntryUsage(), cacheStorage.getCacheEntryUsage(select));
    }

    public void testEvictionWithCacheParameter() throws Exception {
        UserQueryBuilder qb = from(person).selectId(person).cache();
        Select select = qb.getSelect();
        assertFalse(cacheStorage.hasCache(select));
        cacheStorage.fetch(select);
        assertTrue(cacheStorage.hasCache(select));
        // Cache = false should invalidate cache
        qb = from(person).selectId(person).nocache();
        cacheStorage.fetch(qb.getSelect());
        assertFalse(cacheStorage.hasCache(select));
        assertEquals(0, cacheStorage.getCacheEntryUsage(select));
    }

    public void testTimeEviction() throws Exception {
        cacheStorage.setMaxCacheEntryLifetime(2000); // Change eviction time to avoid long running tests
        UserQueryBuilder qb = from(person).selectId(person).cache();
        Select select = qb.getSelect();
        assertFalse(cacheStorage.hasCache(select));
        cacheStorage.fetch(select);
        assertTrue(cacheStorage.hasCache(select));
        Thread.sleep(cacheStorage.getMaxCacheEntryLifetime());
        assertFalse(cacheStorage.hasCache(select)); // Cache entry is now too old
        cacheStorage.fetch(select);
        assertTrue(cacheStorage.hasCache(select)); // But redoing a fetch caches a new value
    }

    public void testCapacityEviction() {
        UserQueryBuilder qb = from(person).selectId(person).cache();
        Select initialSelect = qb.getSelect();
        assertFalse(cacheStorage.hasCache(initialSelect));
        cacheStorage.fetch(initialSelect);
        assertTrue(cacheStorage.hasCache(initialSelect));
        for (int i = 0; i < cacheStorage.getMaxCacheCapacity(); i++) {
            qb = from(person).selectId(person).where(contains(person.getField("firstname"), "value" + i)).cache();
            assertFalse(cacheStorage.hasCache(qb.getExpression()));
            cacheStorage.fetch(qb.getExpression());
            assertTrue(cacheStorage.hasCache(qb.getExpression()));
        }
        // Over-sized capacity, initial query should no longer be in cache
        assertFalse(cacheStorage.hasCache(initialSelect));
    }

    public void testCachedResultMaxSize1() {
        cacheStorage.setCachedResultMaxSize(0);
        UserQueryBuilder qb = from(person).selectId(person).cache();
        Select initialSelect = qb.getSelect();
        assertFalse(cacheStorage.hasCache(initialSelect));
        cacheStorage.fetch(initialSelect);
        assertFalse(cacheStorage.hasCache(initialSelect));
    }

    public void testCachedPartialResults1() {
        // Need some additional data
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<>();
        allRecords
                .add(factory
                        .read(
                                repository,
                                country,
                                "<Country><id>1</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France</name></Country>"));
        allRecords
                .add(factory
                        .read(
                                repository,
                                country,
                                "<Country><id>2</id><creationDate>2011-10-10</creationDate><creationTime>2011-10-10T01:01:01</creationTime><name>USA</name></Country>"));
        allRecords
                .add(factory
                        .read(
                                repository,
                                country,
                                "<Country><id>3</id><creationDate>2011-10-10</creationDate><creationTime>2011-10-10T02:01:01</creationTime><name>Italy</name></Country>"));
        cacheStorage.begin();
        cacheStorage.update(allRecords);
        cacheStorage.commit();
        // Now test max result size
        cacheStorage.setCachedResultMaxSize(1);
        UserQueryBuilder qb = from(country).selectId(country).cache();
        Select initialSelect = qb.getSelect();
        assertFalse(cacheStorage.hasCache(initialSelect));
        try {
            cacheStorage.begin();
            StorageResults results = cacheStorage.fetch(initialSelect);
            // Max cached result size is 1 but query yields 3 results: so no cache expected for query *but* results should
            // still contain 3 records.
            assertEquals(3, results.getSize());
            int i = 0;
            try {
                for (DataRecord result : results) {
                    i++;
                }
            } finally {
                results.close();
            }
            assertEquals(3, i);
            assertFalse(cacheStorage.hasCache(initialSelect));
            cacheStorage.commit();
        } catch (Exception e) {
            cacheStorage.rollback();
            throw new RuntimeException(e);
        }
    }

    public void testCachedPartialResults2() {
        // Need some additional data
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<>();
        allRecords
                .add(factory
                        .read(
                                repository,
                                country,
                                "<Country><id>1</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France</name></Country>"));
        allRecords
                .add(factory
                        .read(
                                repository,
                                country,
                                "<Country><id>2</id><creationDate>2011-10-10</creationDate><creationTime>2011-10-10T01:01:01</creationTime><name>USA</name></Country>"));
        allRecords
                .add(factory
                        .read(
                                repository,
                                country,
                                "<Country><id>3</id><creationDate>2011-10-10</creationDate><creationTime>2011-10-10T02:01:01</creationTime><name>Italy</name></Country>"));
        cacheStorage.begin();
        cacheStorage.update(allRecords);
        cacheStorage.commit();
        // Now test max result size
        cacheStorage.setCachedResultMaxSize(4); // Test with a max cache size higher than instances in storage
        UserQueryBuilder qb = from(country).selectId(country).cache();
        Select initialSelect = qb.getSelect();
        assertFalse(cacheStorage.hasCache(initialSelect));
        try {
            cacheStorage.begin();
            StorageResults results = cacheStorage.fetch(initialSelect);
            // Max cached result size is 1 but query yields 3 results: so no cache expected for query *but* results should
            // still contain 3 records.
            assertEquals(3, results.getSize());
            int i = 0;
            try {
                for (DataRecord result : results) {
                    i++;
                }
            } finally {
                results.close();
            }
            assertEquals(3, i);
            assertTrue(cacheStorage.hasCache(initialSelect));
            cacheStorage.commit();
        } catch (Exception e) {
            cacheStorage.rollback();
            throw new RuntimeException(e);
        }
    }



}
