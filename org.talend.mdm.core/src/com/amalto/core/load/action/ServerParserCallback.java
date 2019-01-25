/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.load.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.amalto.core.load.LoadParserCallback;
import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.server.api.XmlServer;

/**
 *
 */
class ServerParserCallback implements LoadParserCallback {

    private static final Logger log = Logger.getLogger(ServerParserCallback.class);

    private final XmlServer server;

    private final String dataClusterName;

    private final boolean needAutoGenPK;

    private final boolean updateReport;

    private int currentCount;

    private Map<String, String> updateReportMap = new HashMap();

    public ServerParserCallback(XmlServer server, String dataClusterName, boolean needAutoGenPK, boolean updateReport) {
        this.server = server;
        this.dataClusterName = dataClusterName;
        this.needAutoGenPK = needAutoGenPK;
        this.updateReport = updateReport;
        currentCount = 0;
    }

    @Override
    public void flushDocument(XMLReader docReader, InputSource input) {
        try {
            if (updateReport) {
                String[] inputList = input.getPublicId().split("\\."); //$NON-NLS-1$
                String id = inputList[2];
                ItemPOJO item = null;
                // If id is not auto generated, need check the record exist or not.
                // If the record exist, operation should be update.
                if (!needAutoGenPK) {
                    ItemPOJOPK pk = new ItemPOJOPK(new DataClusterPOJOPK(inputList[0]), inputList[1], new String[] { id });
                    item = ItemPOJO.load(pk);
                }
                String operation;
                if (item == null) {
                    operation = UpdateReportPOJO.OPERATION_TYPE_CREATE;
                } else {
                    operation = UpdateReportPOJO.OPERATION_TYPE_UPDATE;
                }
                updateReportMap.put(id, operation);
            }
            server.putDocumentFromSAX(dataClusterName, docReader, input);
            currentCount++;
            if (currentCount % 1000 == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Loaded documents: " + (currentCount / 1000) + "K."); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getCount() {
        return currentCount;
    }

    public Map<String, String> getUpdateReportMap() {
        return updateReportMap;
    }
}
