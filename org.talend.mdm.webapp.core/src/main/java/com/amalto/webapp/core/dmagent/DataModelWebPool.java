/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.dmagent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.talend.mdm.commmon.util.datamodel.management.DataModelBean;
import org.talend.mdm.commmon.util.datamodel.management.DataModelID;

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class DataModelWebPool {

    private static final int DEFAULT_POOL_SIZE = 1000;

    private static final float hashTableLoadFactor = 0.75f;

    private LinkedHashMap<DataModelID, DataModelBean> map;

    /** unique instance */
    private static DataModelWebPool sInstance = null;

    private int poolSize = 0;

    /**
     * Private constuctor
     */
    private DataModelWebPool() {
        super();
        poolSize = DEFAULT_POOL_SIZE;
        int hashTableCapacity = (int) Math.ceil(poolSize / hashTableLoadFactor) + 1;
        map = new LinkedHashMap<DataModelID, DataModelBean>(hashTableCapacity, hashTableLoadFactor, true) {

            // (an anonymous inner class)
            private static final long serialVersionUID = 1;

            @Override
            protected boolean removeEldestEntry(Map.Entry<DataModelID, DataModelBean> eldest) {
                return size() > DataModelWebPool.this.poolSize;
            }
        };

    }

    /**
     * Get the unique instance of this class.
     */
    public static synchronized DataModelWebPool getUniqueInstance() {

        if (sInstance == null) {
            sInstance = new DataModelWebPool();
        }

        return sInstance;

    }

    public synchronized DataModelBean get(DataModelID dataModelID) {
        return map.get(dataModelID);
    }

    public synchronized void put(DataModelID dataModelID, DataModelBean dataModelBean) {
        map.put(dataModelID, dataModelBean);
    }

    public synchronized DataModelBean remove(DataModelID dataModelID) {
        return map.remove(dataModelID);
    }

}
