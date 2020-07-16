/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.transaction;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.history.Document;
import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.save.DOMDocument;
import com.amalto.core.save.SaverSession;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;
import com.amalto.core.util.MDMEhCacheUtil;
import com.amalto.core.util.Util;

public class RouteItemListener implements TransactionListener {

    private static final Logger LOGGER = LogManager.getLogger(RouteItemListener.class);

    @SuppressWarnings("unchecked")
    @Override
    public void transactionCommitted(String longTransactionId) {
        Object object = MDMEhCacheUtil.getCache(MDMEhCacheUtil.UPDATE_REPORT_EVENT_CACHE, longTransactionId);
        try {
            if (object != null) {
                List<String> stringObjects = (List<String>) object;
                List<Document> updateReportList = new ArrayList<>(stringObjects.size());
                StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
                Storage storage = storageAdmin.get(UpdateReportPOJO.DATA_CLUSTER, StorageType.MASTER);
                MetadataRepository repository = storage.getMetadataRepository();
                ComplexTypeMetadata type = repository.getComplexType("Update"); //$NON-NLS-1$
                for (String string : stringObjects) {
                    DOMDocument document = null;
                    try {
                        document = new DOMDocument(Util.parse(string), type, UpdateReportPOJO.DATA_CLUSTER, UpdateReportPOJO.DATA_MODEL);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse update report from cache.", e); //$NON-NLS-1$
                    }
                    updateReportList.add(document);
                }
                SaverSession session = SaverSession.newSession();
                session.routeItems(updateReportList);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to route item as transaction committed.", e); //$NON-NLS-1$
        } finally {
            if (object != null) {
                MDMEhCacheUtil.removeCache(MDMEhCacheUtil.UPDATE_REPORT_EVENT_CACHE, longTransactionId);
            }
        }
    }

    @Override
    public void transactionRollbacked(String longTransactionId) {
        MDMEhCacheUtil.removeCache(MDMEhCacheUtil.UPDATE_REPORT_EVENT_CACHE, longTransactionId);
    }
}
