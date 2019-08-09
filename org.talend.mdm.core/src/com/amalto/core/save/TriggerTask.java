/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import com.amalto.core.history.Document;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.save.context.SaverSource;

public class TriggerTask implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(TriggerTask.class);

    private final SaverSource saverSource;

    private final List<Document> updateReport;

    private final String longTransactionId;

    private static final int SECONDS_TO_WAIT = 600;

    public TriggerTask(SaverSource saverSource, List<Document> updateReport, String longTransactionId) {
        this.saverSource = saverSource;
        this.updateReport = updateReport;
        this.longTransactionId = longTransactionId;
    }

    private boolean isLongTransactionCommitted(String longTransactionId) {
        String isCommitted = System.getProperty(longTransactionId + ""); //$NON-NLS-1$
        if (isCommitted != null && isCommitted.equalsIgnoreCase("true")) { //$NON-NLS-1$
            return true;
        }
        return false;
    }

    private void routeItems(SaverSource saverSource, List<Document> updateReport) {
        Iterator<Document> iterator = updateReport.iterator();
        while (iterator.hasNext()) {
            MutableDocument document = (MutableDocument) iterator.next();
            String dataCluster = document.getDataCluster();
            String typeName = document.getType().getName();

            Collection<FieldMetadata> keyFields = document.getType().getKeyFields();
            String[] itemsId = new String[keyFields.size()];
            int i = 0;
            for (FieldMetadata keyField : keyFields) {
                itemsId[i++] = document.createAccessor(keyField.getName()).get();
            }
            saverSource.routeItem(dataCluster, typeName, itemsId);
            iterator.remove();
        }
    }

    @Override
    public void run() {
        long endWaitTime = System.currentTimeMillis() + SECONDS_TO_WAIT * 1000;
        boolean isConditionMet = false;
        while (System.currentTimeMillis() < endWaitTime && !isConditionMet) {
            isConditionMet = isLongTransactionCommitted(longTransactionId);
            if (isConditionMet) {
                routeItems(saverSource, updateReport);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Route items after Transaction " + longTransactionId + " was committed."); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}