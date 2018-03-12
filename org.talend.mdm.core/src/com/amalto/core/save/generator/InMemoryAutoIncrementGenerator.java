/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save.generator;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.util.Util;

@SuppressWarnings("nls")
public class InMemoryAutoIncrementGenerator implements AutoIdGenerator {

    private static final Logger LOGGER = Logger.getLogger(InMemoryAutoIncrementGenerator.class);

    private static final InMemoryAutoIncrementGenerator INSTANCE = new InMemoryAutoIncrementGenerator();

    private AtomicBoolean NEED_TO_SAVE = new AtomicBoolean(false);

    private Properties CONFIGURATION;

    private InMemoryAutoIncrementGenerator() {
        init();
    }

    public static InMemoryAutoIncrementGenerator getInstance() {
        return INSTANCE;
    }

    // This method is not secure in clustered environments (when several MDM nodes share same database).
    // See com.amalto.core.save.generator.StorageAutoIncrementGenerator for better concurrency support.
    @Override
    public synchronized String generateId(String dataClusterName, String conceptName, String keyElementName) {
        long nextId = 0;
        String key = dataClusterName + "." + AutoIncrementUtil.getConceptForAutoIncrement(dataClusterName, conceptName) + "." + keyElementName;
        String value = CONFIGURATION.getProperty(key);
        if (value != null) {
            nextId = Long.valueOf(value);
        }
        nextId++;
        if (!DataRecord.ValidateRecord.get()) {// don't actually save if for Record Validation
            CONFIGURATION.setProperty(key, String.valueOf(nextId));
            NEED_TO_SAVE.set(true);
        }
        return String.valueOf(nextId);
    }

    @Override
    public void saveState(XmlServer server) {
        if (NEED_TO_SAVE.getAndSet(false)) {
            try {
                String xmlString = Util.convertAutoIncrement(CONFIGURATION);
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
        CONFIGURATION = new Properties();
        try {
            ItemPOJOPK pk = new ItemPOJOPK(DC, AUTO_INCREMENT, IDS);
            ItemPOJO itempojo = ItemPOJO.load(pk);
            if (itempojo == null) {
                LOGGER.info("Could not load configuration from database, use default configuration.");
            } else {
                String xml = itempojo.getProjectionAsString();
                if (StringUtils.isNotBlank(xml)) {
                    CONFIGURATION = Util.convertAutoIncrement(xml);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }
}