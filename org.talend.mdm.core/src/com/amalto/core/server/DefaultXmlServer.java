/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.amalto.core.server.api.XmlServer;
import com.amalto.core.storage.SQLWrapper;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.IXmlServerSLWrapper;
import com.amalto.xmlserver.interfaces.ItemPKCriteria;
import com.amalto.xmlserver.interfaces.XmlServerException;

public class DefaultXmlServer implements XmlServer {

    private final IXmlServerSLWrapper server;

    public DefaultXmlServer() {
        server = new SQLWrapper();
    }

    public String[] getAllClusters() throws XtentisException {
        try {
            return server.getAllClusters();
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public void clearCache() throws XtentisException {
        server.clearCache();
    }

    public long deleteCluster(String clusterName) throws XtentisException {
        try {
            return server.deleteCluster(clusterName);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public long createCluster(String clusterName) throws XtentisException {
        try {
            return server.createCluster(clusterName);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public long putDocumentFromFile(String fileName, String uniqueID, String clusterName, String documentType)
            throws XtentisException {
        try {
            return server.putDocumentFromFile(fileName, uniqueID, clusterName, documentType);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public long putDocumentFromString(String xmlString, String uniqueID, String clusterName) throws XtentisException {
        try {
            return server.putDocumentFromString(xmlString, uniqueID, clusterName);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public long putDocumentFromString(String string, String uniqueID, String clusterName, String documentType)
            throws XtentisException {
        try {
            return server.putDocumentFromString(string, uniqueID, clusterName, documentType);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public long putDocumentFromSAX(String dataClusterName, XMLReader docReader, InputSource input)
            throws com.amalto.core.util.XtentisException {
        try {
            return server.putDocumentFromSAX(dataClusterName, docReader, input);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public String getDocumentAsString(String clusterName, String uniqueID) throws XtentisException {
        try {
            return server.getDocumentAsString(clusterName, uniqueID);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public boolean existCluster(String cluster) throws XtentisException {
        try {
            if (cluster.endsWith(StorageAdmin.STAGING_SUFFIX)) {
                cluster = StringUtils.substringBeforeLast(cluster, "#"); //$NON-NLS-1$
            }
            return server.existCluster(cluster);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public String getDocumentAsString(String clusterName, String uniqueID, String encoding) throws XtentisException {
        try {
            return server.getDocumentAsString(clusterName, uniqueID, encoding);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public byte[] getDocumentBytes(String clusterName, String uniqueID, String documentType) throws XtentisException {
        try {
            return server.getDocumentBytes(clusterName, uniqueID, documentType);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public String[] getAllDocumentsUniqueID(String clusterName) throws XtentisException {
        try {
            return server.getAllDocumentsUniqueID(clusterName);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public long deleteDocument(String clusterName, String uniqueID) throws XtentisException {
        return deleteDocument(clusterName, uniqueID, IXmlServerSLWrapper.TYPE_DOCUMENT);
    }

    public long deleteDocument(String clusterName, String uniqueID, String documentType) throws XtentisException {
        try {
            return server.deleteDocument(clusterName, uniqueID, documentType);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public int deleteItems(String clusterName, String conceptName, IWhereItem whereItem) throws XtentisException {
        try {
            return server.deleteItems(clusterName, conceptName, whereItem);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public long countItems(String clusterName, String conceptName, IWhereItem whereItem) throws XtentisException {
        try {
            return server.countItems(clusterName, conceptName, whereItem);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters) throws XtentisException {
        try {
            return server.runQuery(clusterName, query, parameters);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters, boolean includeNullValue)
            throws XtentisException {
        try {
            return server.runQuery(clusterName, query, parameters, includeNullValue);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public List<String> getItemPKsByCriteria(ItemPKCriteria criteria) throws XtentisException {
        try {
            return server.getItemPKsByCriteria(criteria);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public boolean supportTransaction() {
        return server.supportTransaction();
    }

    public void start(String dataClusterName) throws com.amalto.core.util.XtentisException {
        try {
            server.start(dataClusterName);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public void commit(String dataClusterName) throws com.amalto.core.util.XtentisException {
        try {
            server.commit(dataClusterName);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public void rollback(String dataClusterName) throws com.amalto.core.util.XtentisException {
        try {
            server.rollback(dataClusterName);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public void end(String dataClusterName) throws com.amalto.core.util.XtentisException {
        try {
            server.end(dataClusterName);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public void close() throws com.amalto.core.util.XtentisException {
        try {
            server.close();
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public List<String> globalSearch(String dataCluster, String keyword, int start, int end)
            throws com.amalto.core.util.XtentisException {
        try {
            return server.globalSearch(dataCluster, keyword, start, end);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public void exportDocuments(String clusterName, int start, int end, boolean includeMetadata, OutputStream outputStream)
            throws com.amalto.core.util.XtentisException {
        try {
            server.exportDocuments(clusterName, start, end, includeMetadata, outputStream);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public boolean supportStaging(String dataCluster) {
        if (dataCluster == null || dataCluster.trim().length() == 0) {
            return false;
        }
        Server server = ServerContext.INSTANCE.get();
        return server.getStorageAdmin().supportStaging(dataCluster);
    }

    public String[] getDocumentsAsString(String clusterName, String[] uniqueIDs) throws XtentisException {
        try {
            return server.getDocumentsAsString(clusterName, uniqueIDs);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }

    public String[] getDocumentsAsString(String clusterName, String[] uniqueIDs, String encoding) throws XtentisException {
        try {
            return server.getDocumentsAsString(clusterName, uniqueIDs, encoding);
        } catch (XmlServerException e) {
            throw new XtentisException(e);
        }
    }
}
