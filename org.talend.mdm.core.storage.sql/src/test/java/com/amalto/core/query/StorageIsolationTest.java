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

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import static com.amalto.core.query.user.UserQueryBuilder.contains;
import static com.amalto.core.query.user.UserQueryBuilder.from;

@SuppressWarnings("nls")
public class StorageIsolationTest extends TestCase {

    private static Logger LOG = Logger.getLogger(StorageIsolationTest.class);

    static {
        LOG.info("Setting up MDM server environment...");
        ServerContext.INSTANCE.get(new MockServerLifecycle());
        LOG.info("MDM server environment set.");
    }

    public void test1() throws Exception {
        MetadataRepository repository1 = prepareMetadata("StorageIsolationTest_1.xsd");
        MetadataRepository repository2 = prepareMetadata("StorageIsolationTest_2.xsd");
        assertNotSame(repository1, repository2);

        ComplexTypeMetadata type1 = repository1.getComplexType("StorageIsolationTest1");
        ComplexTypeMetadata type2 = repository2.getComplexType("StorageIsolationTest1");
        assertNotSame(type1, type2);

        Storage s1 = new HibernateStorage("MDM1", StorageType.MASTER);
        s1.init(ServerContext.INSTANCE.get().getDefinition(StorageTestCase.DATABASE + "-DS1", "MDM"));
        s1.prepare(repository1, true);
        Storage s2 = new HibernateStorage("MDM2", StorageType.MASTER);
        s2.init(ServerContext.INSTANCE.get().getDefinition(StorageTestCase.DATABASE + "-DS2", "MDM"));
        s2.prepare(repository2, true);

        MainTestRunnable runnable1 = new MainTestRunnable(repository1, s1, "StorageIsolationTest1", 300, "ValueMDM1", "ValueMDM2");
        Thread t1 = new Thread(runnable1);

        MainTestRunnable runnable2 = new MainTestRunnable(repository2, s2, "StorageIsolationTest1", 500, "ValueMDM2", "ValueMDM1");
        Thread t2 = new Thread(runnable2);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        LOG.info("Run1: " + runnable1.getActualInstanceNumber());
        LOG.info("Run2: " + runnable2.getActualInstanceNumber());

        assertEquals(300, runnable1.getActualInstanceNumber());
        assertEquals(500, runnable2.getActualInstanceNumber());
        assertEquals(0, runnable1.getActualFullTextResults());
        assertEquals(0, runnable2.getActualFullTextResults());
    }

    private MetadataRepository prepareMetadata(String dataModelFile) {
        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageFullTextTest.class.getResourceAsStream(dataModelFile));
        return repository;
    }

    private class MainTestRunnable implements Runnable {

        private final MetadataRepository repository;

        private final String typeName;

        private final int instanceNumber;

        private final String valueText;

        private final String valueNotToBeFound;

        private int actualInstanceNumber;

        private int actualFullTextResults;

        private final Storage storage;

        private MainTestRunnable(MetadataRepository repository, Storage storage, String typeName, int instanceNumber, String valueText, String valueNotToBeFound) {
            this.repository = repository;
            this.storage = storage;
            this.typeName = typeName;
            this.instanceNumber = instanceNumber;
            this.valueText = valueText;
            this.valueNotToBeFound = valueNotToBeFound;
        }

        @Override
        public void run() {
            ComplexTypeMetadata type = repository.getComplexType(typeName);
            try {
                DataRecordReader<String> factory = new XmlStringDataRecordReader();
                storage.begin();
                for (int i = 0; i < instanceNumber; i++) {
                    DataRecord record = factory.read(repository, type, "<" + typeName + "><Id>" + i + "</Id><field>"
                            + valueText + "</field></" + typeName + ">");
                    storage.update(record);
                }
                storage.commit();
                storage.end();

                UserQueryBuilder qb = from(type).select(type.getField("field"));
                storage.begin();
                {
                    StorageResults results = storage.fetch(qb.getSelect());
                    try {
                        actualInstanceNumber = results.getCount();
                    } finally {
                        results.close();
                    }

                    qb = from(type).where(contains(type.getField("field"), valueNotToBeFound));
                    results = storage.fetch(qb.getSelect());
                    try {
                        actualFullTextResults = results.getCount();
                    } finally {
                        results.close();
                    }
                }
                storage.commit();
            } catch (Exception e) {
                e.printStackTrace();
                actualInstanceNumber = -1;
            } finally {
                storage.close();
            }
        }

        public int getActualInstanceNumber() {
            return actualInstanceNumber;
        }

        public int getActualFullTextResults() {
            return actualFullTextResults;
        }
    }
}
