/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save.generator;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.util.Util;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import java.util.Properties;

@SuppressWarnings("nls")
class InMemoryAutoIncrementGenerator implements AutoIdGenerator {

    private static final Logger LOGGER = Logger.getLogger(InMemoryAutoIncrementGenerator.class);

    private static final DataClusterPOJOPK DC = new DataClusterPOJOPK(XSystemObjects.DC_CONF.getName());

    private static final String AUTO_INCREMENT = "AutoIncrement";

    private static final String[] IDS = new String[] { AUTO_INCREMENT }; 

    private static Properties CONFIGURATION = null;

    private boolean wasInitCalled = false;

    // This method is not secure in clustered environments (when several MDM nodes share same database).
    // See com.amalto.core.save.generator.StorageAutoIncrementGenerator for better concurrency support.
    @Override
    public synchronized String generateId(String dataClusterName, String conceptName, String keyElementName) {
        if (!wasInitCalled) {
            init();
        }
        String key = dataClusterName + "." + AutoIncrementGenerator.getConceptForAutoIncrement(dataClusterName, conceptName) + "." + keyElementName;
        long num;
        String n = CONFIGURATION.getProperty(key);
        if (n == null) {
            num = 0;
        } else {
            num = Long.valueOf(n);
        }
        num++;
        if (!DataRecord.ValidateRecord.get()) {// don't actually save if for Record Validation
            CONFIGURATION.setProperty(key, String.valueOf(num));
        }
        return String.valueOf(num);
    }

    @Override
    public synchronized void saveState(XmlServer server) {
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

    @Override
    public synchronized boolean isInitialized() {
        return wasInitCalled;
    }
    
    @Override
    public synchronized void init() {
        // first try Current path
        CONFIGURATION = new Properties();
        try {
            ItemPOJOPK pk = new ItemPOJOPK(DC, AUTO_INCREMENT, IDS);
            ItemPOJO itempojo = ItemPOJO.load(pk);
            if (itempojo == null) {
                LOGGER.info("Could not load configuration from database, use default configuration."); 
            } else {
                String xml = itempojo.getProjectionAsString();
                if (xml != null && xml.trim().length() > 0) {
                    CONFIGURATION = Util.convertAutoIncrement(xml);
                }
            }
            wasInitCalled = true;
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }
}