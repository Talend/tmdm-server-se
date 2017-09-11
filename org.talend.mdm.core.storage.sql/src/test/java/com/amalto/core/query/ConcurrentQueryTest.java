/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.query;

import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.amalto.core.query.user.UserQueryBuilder.eq;

public class ConcurrentQueryTest extends StorageTestCase {

    private static final Logger LOGGER = Logger.getLogger(ConcurrentQueryTest.class);

    private final static int COUNT = 100;

    private static final Object lock = new Object();

    @Override
    public void setUp() throws Exception {
    }

    @Override
    public void tearDown() throws Exception {
    }

    public void testConcurrentGetById() throws Exception {
        // Test env creation
        AtomicBoolean doWrite = new AtomicBoolean(true);
        ComplexTypeMetadata concurrent = repository.getComplexType("Concurrent");
        FieldMetadata valueField = concurrent.getField("Value");
        UserQueryBuilder qb = UserQueryBuilder.from(concurrent).where(eq(concurrent.getField("Id"), "1"));
        Select getByIdSelect = qb.getSelect();
        // Initial record creation
        storage.begin();
        {
            DataRecordReader<String> reader = new XmlStringDataRecordReader();
            DataRecord initialRecord = reader.read(repository, concurrent, "<Concurrent><Id>1</Id><Value>0</Value></Concurrent>");
            storage.update(initialRecord);
        }
        storage.commit();
        // Assert initial records is correctly created
        storage.begin();
        {
            StorageResults results = storage.fetch(getByIdSelect);
            try {
                assertEquals(1, results.getCount());
            } finally {
                results.close();
            }
        }
        storage.commit();
        // Create writers and updaters
        WriterRunnable writerRunnable = new WriterRunnable(doWrite, getByIdSelect, valueField);
        ReaderRunnable readerRunnable = new ReaderRunnable(doWrite, getByIdSelect, valueField);
        Thread writerThread = new Thread(writerRunnable);
        Thread readerThread = new Thread(readerRunnable);
        // Starts everything...
        writerThread.start();
        readerThread.start();
        writerThread.join();
        readerThread.join();
        // Asserts
        assertEquals(0, readerRunnable.getReadErrorCount());
        assertEquals(0, readerRunnable.getExistenceErrorCount());
    }

    private static class ReaderRunnable implements Runnable {

        private final AtomicBoolean doWrite;

        private final Select getByIdSelect;

        private final FieldMetadata valueField;

        private int readErrorCount = 0;

        private int existenceErrorCount = 0;

        public ReaderRunnable(AtomicBoolean doWrite, Select getByIdSelect, FieldMetadata valueField) {
            this.doWrite = doWrite;
            this.getByIdSelect = getByIdSelect;
            this.valueField = valueField;
        }

        public int getReadErrorCount() {
            return readErrorCount;
        }

        public int getExistenceErrorCount() {
            return existenceErrorCount;
        }

        @Override
        public void run() {
            int previousValue = 0;
            int i = 0;
            while (i < COUNT) {
                synchronized (lock) {
                    if (!doWrite.get()) {
                        try {
                            LOGGER.debug("**************************************");
                            LOGGER.debug("************* READ BEGIN *************");
                            LOGGER.debug("**************************************");
                            storage.begin();
                            StorageResults records = storage.fetch(getByIdSelect);
                            if (!records.iterator().hasNext()) {
                                LOGGER.debug("Got a non existing record!");
                                existenceErrorCount++;
                            }
                            for (DataRecord record : records) {
                                int value = (Integer) record.get(valueField);
                                int diff = value - previousValue;
                                if (diff != 1) {
                                    LOGGER.debug("Update is missing (diff=" + diff + ")");
                                    readErrorCount++;
                                }
                                previousValue = value;
                            }
                            i++;
                            storage.commit();
                            doWrite.getAndSet(true);
                            lock.notifyAll();
                        } finally {
                            LOGGER.debug("************************************");
                            LOGGER.debug("************* READ END *************");
                            LOGGER.debug("************************************");
                        }
                    } else {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    private static class WriterRunnable implements Runnable {

        private final AtomicBoolean doWrite;

        private final Select getByIdSelect;

        private final FieldMetadata valueField;

        public WriterRunnable(AtomicBoolean doWrite, Select getByIdSelect, FieldMetadata valueField) {
            this.doWrite = doWrite;
            this.getByIdSelect = getByIdSelect;
            this.valueField = valueField;
        }

        @Override
        public void run() {
            int i = 0;
            while (i < COUNT) {
                synchronized (lock) {
                    if (doWrite.get()) {
                        try {
                            LOGGER.debug("***************************************");
                            LOGGER.debug("************* WRITE BEGIN *************");
                            LOGGER.debug("***************************************");
                            storage.begin();
                            StorageResults records = storage.fetch(getByIdSelect);
                            for (DataRecord record : records) {
                                int value = (Integer) record.get(valueField);
                                record.set(valueField, value + 1);
                                storage.update(record);
                            }
                            storage.commit();
                            doWrite.getAndSet(false);
                            lock.notifyAll();
                            i++;
                        } finally {
                            LOGGER.debug("*************************************");
                            LOGGER.debug("************* WRITE END *************");
                            LOGGER.debug("*************************************");
                        }
                    } else {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
