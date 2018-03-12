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

package com.amalto.core.storage;

import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.IXmlServerSLWrapper;
import com.amalto.xmlserver.interfaces.ItemPKCriteria;
import com.amalto.xmlserver.interfaces.XmlServerException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.OutputStream;
import java.util.*;

class NoOpWrapper implements IXmlServerSLWrapper{

    private static final Logger LOGGER = Logger.getLogger(NoOpWrapper.class);

    static {
        LOGGER.warn("--- Using No op wrapper! ---"); //$NON-NLS-1$
    }

    public boolean isUpAndRunning() {
        return true;
    }

    public String[] getAllClusters() throws XmlServerException {
        return new String[0];
    }

    public long deleteCluster(String clusterName) throws XmlServerException {
        return 0;  
    }

    public long deleteAllClusters() throws XmlServerException {
        return 0;  
    }

    public long createCluster(String clusterName) throws XmlServerException {
        return 0;  
    }

    public long putDocumentFromFile(String fileName, String uniqueID, String clusterName) throws XmlServerException {
        return 0;  
    }

    public long putDocumentFromFile(String fileName, String uniqueID, String clusterName, String documentType) throws XmlServerException {
        return 0;  
    }

    public boolean existCluster(String cluster) throws XmlServerException {
        return true;
    }

    public long putDocumentFromString(String xmlString, String uniqueID, String clusterName) throws XmlServerException {
        return 0;  
    }

    public long putDocumentFromString(String string, String uniqueID, String clusterName, String documentType) throws XmlServerException {
        return 0;  
    }

    public long putDocumentFromDOM(Element root, String uniqueID, String clusterName) throws XmlServerException {
        return 0;  
    }

    public long putDocumentFromSAX(String dataClusterName, XMLReader docReader, InputSource input) throws XmlServerException {
        return 0;  
    }

    public String getDocumentAsString(String clusterName, String uniqueID) throws XmlServerException {
        return ""; //$NON-NLS-1$
    }

    public String getDocumentAsString(String clusterName, String uniqueID, String encoding) throws XmlServerException {
        return ""; //$NON-NLS-1$
    }
    
    public byte[] getDocumentBytes(String clusterName, String uniqueID, String documentType) throws XmlServerException {
        return new byte[0];
    }

    public String[] getAllDocumentsUniqueID(String clusterName) throws XmlServerException {
        return new String[0];
    }

    public long deleteDocument(String clusterName, String uniqueID, String documentType) throws XmlServerException {
        return 0;  
    }

    public int deleteItems(String clusterName, String conceptName, IWhereItem whereItem) throws XmlServerException {
        return 0;  
    }

    public long moveDocumentById(String sourceClusterName, String uniqueID, String targetClusterName) throws XmlServerException {
        return 0;  
    }

    public long countItems(String clusterName, String conceptName, IWhereItem whereItem) throws XmlServerException {
        return 0;  
    }

    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters) throws XmlServerException {
        return null;  
    }

    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters, boolean includeNullValue) throws XmlServerException {
        return null;
    }

    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters, int start, int limit, boolean withTotalCount) throws XmlServerException {
        return null;  
    }

    public List<String> getItemPKsByCriteria(ItemPKCriteria criteria) throws XmlServerException {
        return null;  
    }

    public void clearCache() {
        
    }

    public boolean supportTransaction() {
        return true;
    }

    public void start(String dataClusterName) throws XmlServerException {
        
    }

    public void commit(String dataClusterName) throws XmlServerException {
        
    }

    public void rollback(String dataClusterName) throws XmlServerException {
        
    }

    public void end(String dataClusterName) throws XmlServerException {
        
    }

    public void close() throws XmlServerException {
        
    }

    public List<String> globalSearch(String dataCluster, String keyword, int start, int end) throws XmlServerException {
        return null;  
    }

    public void exportDocuments(String clusterName, int start, int end, boolean includeMetadata, OutputStream outputStream) throws XmlServerException {
        
    }

    public String[] getDocumentsAsString(String clusterName, String[] uniqueIDs) throws XmlServerException {
        return new String[0];
    }

    public String[] getDocumentsAsString(String clusterName, String[] uniqueIDs, String encoding) throws XmlServerException {
        return new String[0];
    }
}
