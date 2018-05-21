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

package com.amalto.core.save;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.core.save.context.DocumentSaver;
import com.amalto.core.save.context.SaverContextFactory;
import com.amalto.core.webservice.WSPartialPutItem;
import com.amalto.core.webservice.WSPutItem;

public class SaverHelper {
    public static DocumentSaver saveItem(WSPutItem wsPutItem,
                                         SaverSession session,
                                         String dataClusterName,
                                         String dataModelName) throws UnsupportedEncodingException {
        return saveItem(wsPutItem.getXmlString(), session, !wsPutItem.getIsUpdate(), dataClusterName, dataModelName);
    }

    public static DocumentSaver saveItem(String xmlString,
                                         SaverSession session,
                                         boolean isReplace,
                                         String dataClusterName,
                                         String dataModelName) throws UnsupportedEncodingException {
        SaverContextFactory contextFactory = session.getContextFactory();
        DocumentSaverContext context = contextFactory.create(dataClusterName,
                dataModelName,
                isReplace,
                new ByteArrayInputStream(xmlString.getBytes("UTF-8"))); //$NON-NLS-1$
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        return saver;
    }

    public static DocumentSaver saveItemWithReport(WSPutItem wsPutItem,
                                                   SaverSession session,
                                                   String dataClusterName,
                                                   String dataModelName,
                                                   String changeSource,
                                                   boolean beforeSaving) throws UnsupportedEncodingException {
        return saveItemWithReport(wsPutItem.getXmlString(), session, !wsPutItem.getIsUpdate(), dataClusterName, dataModelName, changeSource, beforeSaving);
    }

    public static DocumentSaver saveItemWithReport(String xmlString,
                                                   SaverSession session,
                                                   boolean isReplace,
                                                   String dataClusterName,
                                                   String dataModelName,
                                                   String changeSource,
                                                   boolean beforeSaving) throws UnsupportedEncodingException {
        SaverContextFactory contextFactory = session.getContextFactory();
        DocumentSaverContext context = contextFactory.create(dataClusterName,
                dataModelName,
                changeSource,
                new ByteArrayInputStream(xmlString.getBytes("UTF-8")), //$NON-NLS-1$
                isReplace,
                true, // Always validate
                true, // Always generate an update report
                beforeSaving, XSystemObjects.DC_PROVISIONING.getName().equals(dataClusterName));
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        return saver;
    }

    public static DocumentSaver saveItem(WSPartialPutItem partialPutItem,
                                         SaverSession session) throws UnsupportedEncodingException {
        SaverContextFactory contextFactory = session.getContextFactory();
        DocumentSaverContext context = contextFactory.createPartialUpdate(partialPutItem.getDatacluster(),
                partialPutItem.getDatamodel(),
                partialPutItem.getSource(),
                new ByteArrayInputStream(partialPutItem.getXml().getBytes("UTF-8")), //$NON-NLS-1$
                true, // Always validate
                partialPutItem.isReport(),
                partialPutItem.getPivot(),
                partialPutItem.getKeyXPath(),
                partialPutItem.getStartingPosition() != null ? partialPutItem.getStartingPosition() : -1,
                partialPutItem.getOverwrite(),
                partialPutItem.isDelete());
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        return saver;
    }

    public static class WarningApprovedBeforeSave {

        private static ThreadLocal<Boolean> threadLocal = new ThreadLocal<Boolean>() {

            public Boolean initialValue() {
                return Boolean.FALSE;
            }
        };

        private WarningApprovedBeforeSave() {
        }

        public static void set(boolean value) {
            threadLocal.set(value);
        }

        public static boolean get() {
            return threadLocal.get();
        }

        public static void remove() {
            threadLocal.remove();
        }
    }
}
