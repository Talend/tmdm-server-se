/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.configurationinfo;

import com.amalto.core.server.api.XmlServer;
import org.apache.log4j.Logger;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

public class ConfigurationHelper {

    private static final Logger logger = Logger.getLogger(ConfigurationHelper.class);

    private static XmlServer server = null;// Do not use this field directly

    public static XmlServer getServer() throws XtentisException {
        if (server == null) {
            try {
                server = Util.getXmlServerCtrlLocal();
            } catch (Exception e) {
                String err = "Unable to access the XML Server wrapper";
                logger.error(err, e);
                throw new XtentisException(err, e);
            }
        }
        return server;
    }

    public static void createCluster(Class<? extends ObjectPOJO> objectClass) throws XtentisException {
        createCluster(ObjectPOJO.getCluster(objectClass));
    }

    public static void createCluster(String clusterName) throws XtentisException {
        try {
            boolean exist = getServer().existCluster(clusterName);
            if (!exist) {
                getServer().createCluster(clusterName);
                logger.info("Created a new data cluster " + clusterName);
            }
        } catch (Exception e) {
            throw new XtentisException(e);
        }

    }

    public static void removeCluster(String clusterName) throws XtentisException {
        try {
            boolean exist = getServer().existCluster(clusterName);
            if (exist) {
                getServer().deleteCluster(clusterName);
                logger.info("Deleted a data cluster " + clusterName);
            }
        } catch (Exception e) {
            throw new XtentisException(e);
        }
    }

    public static void putDocument(String dataCluster, String xmlString, String uniqueID) throws XtentisException {
        XmlServer server = getServer();
        if (server.getDocumentAsString(dataCluster, uniqueID) == null) {
            server.start(dataCluster);
            try {
                server.putDocumentFromString(xmlString, uniqueID, dataCluster);
                server.commit(dataCluster);
            } catch (Exception e) {
                server.rollback(dataCluster);
                throw new XtentisException(e);
            }
            logger.info("Inserted document " + uniqueID + " to data cluster " + dataCluster);
        }
    }

    public static void deleteDocument(String clusterName, String uniqueID) throws XtentisException {
        XmlServer server = getServer();
        if (server.getDocumentAsString(clusterName, uniqueID) != null) {
            try {
                server.deleteDocument(clusterName, uniqueID);
            } catch (Exception e) {
                throw new XtentisException(e);
            }
        }
    }
}
