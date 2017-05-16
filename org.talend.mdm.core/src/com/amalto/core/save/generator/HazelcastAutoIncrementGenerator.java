// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.save.generator;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.locks.Lock;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.server.MDMContextAccessor;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.util.Util;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IMap;

/**
 * This {@link com.amalto.core.save.generator.AutoIdGenerator generator} is a more secure way to generate auto increment
 * values in case of concurrent access to the same underlying database. <br />
 * (It is used to replace the old resolution of StorageAutoIncrementGenerator)
 */
@SuppressWarnings("nls")
public class HazelcastAutoIncrementGenerator implements AutoIdGenerator {

    private static final Logger LOGGER = Logger.getLogger(HazelcastAutoIncrementGenerator.class);

    private static final HazelcastAutoIncrementGenerator INSTANCE = new HazelcastAutoIncrementGenerator();

    protected Lock INIT_LOCK;

    protected IAtomicLong WAS_INIT_CALLED;

    protected IAtomicLong NEED_TO_SAVE;

    protected IMap<String, Long> CONFIGURATION;

    protected HazelcastAutoIncrementGenerator() {
        Config hzConfig = null;
        if (MDMContextAccessor.getApplicationContext().containsBean("hzConfig")) {
            hzConfig = MDMContextAccessor.getApplicationContext().getBean("hzConfig", Config.class);
            String hzGroupName = (String) MDMConfiguration.getConfiguration().get("hz.group.name");
            String hzGroupPassword = (String) MDMConfiguration.getConfiguration().get("hz.group.password");
            hzConfig.setGroupConfig(new GroupConfig(hzGroupName, hzGroupPassword));
        }
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(hzConfig);

        INIT_LOCK = hazelcast.getLock("autoIncrement_initLock");
        WAS_INIT_CALLED = hazelcast.getAtomicLong("autoIncrement_wasInitCalled");
        NEED_TO_SAVE = hazelcast.getAtomicLong("autoIncrement_needToSave");
        CONFIGURATION = hazelcast.getMap("autoIncrement_configuration");
        INIT_LOCK.lock();
        try {
            if (WAS_INIT_CALLED.get() == 0) {
                init();
            }
        } finally {
            INIT_LOCK.unlock();
        }
    }

    public static HazelcastAutoIncrementGenerator getInstance() {
        return INSTANCE;
    }
    
    @Override
    public String generateId(String dataClusterName, String conceptName, String keyElementName) {
        long nextId = 0;
        String key = dataClusterName + "." + AutoIncrementGenerator.getConceptForAutoIncrement(dataClusterName, conceptName) + "." + keyElementName;
        CONFIGURATION.lock(key);
        try {
            Long value = CONFIGURATION.get(key);
            if (value != null) {
                nextId = value.longValue();
            }
            nextId++;
            if (!DataRecord.ValidateRecord.get()) {// don't actually save if for Record Validation
                CONFIGURATION.put(key, nextId);
                NEED_TO_SAVE.set(1);
            }
        } finally {
            CONFIGURATION.unlock(key);
        }
        return String.valueOf(nextId);
    }

    @Override
    public void saveState(XmlServer server) {
        if (NEED_TO_SAVE.getAndSet(0) == 1) {
            try {
                Properties properties = new Properties();
                properties.putAll(CONFIGURATION);
                String xmlString = Util.convertAutoIncrement(properties);
                ItemPOJO pojo = new ItemPOJO(DC, // cluster
                        AUTO_INCREMENT, // concept name
                        IDS, System.currentTimeMillis(), // insertion time
                        xmlString // actual data
                );
                pojo.setDataModelName(XSystemObjects.DM_CONF.getName());
                pojo.store();
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public synchronized void init() {
        CONFIGURATION.evictAll();
        try {
            ItemPOJOPK pk = new ItemPOJOPK(DC, AUTO_INCREMENT, IDS);
            ItemPOJO itempojo = ItemPOJO.load(pk);
            if (itempojo == null) {
                LOGGER.info("Could not load configuration from database, use default configuration.");
            } else {
                String xml = itempojo.getProjectionAsString();
                if (StringUtils.isNotBlank(xml)) {
                    Properties properties = Util.convertAutoIncrement(xml);
                    for (Entry<Object, Object> entry : properties.entrySet()) {
                        String key = entry.getKey().toString();
                        CONFIGURATION.lock(key);
                        try {
                            CONFIGURATION.put(key, Long.valueOf(entry.getValue().toString()));
                        } finally {
                            CONFIGURATION.unlock(key);
                        }
                    }
                }
            }
            WAS_INIT_CALLED.set(1);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

}
