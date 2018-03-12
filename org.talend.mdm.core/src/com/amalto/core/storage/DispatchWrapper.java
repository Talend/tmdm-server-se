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

import java.io.OutputStream;
import java.util.*;

import com.amalto.core.server.StorageAdmin;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.IXmlServerSLWrapper;
import com.amalto.xmlserver.interfaces.ItemPKCriteria;
import com.amalto.xmlserver.interfaces.XmlServerException;

public class DispatchWrapper implements IXmlServerSLWrapper {
    
    private static final Logger LOGGER = Logger.getLogger(DispatchWrapper.class);
    
    private static IXmlServerSLWrapper mdmInternalWrapper;

    private static IXmlServerSLWrapper userStorageWrapper;

    private boolean userWrapperUp;

    private boolean internalWrapperUp;

    private static Set<String> internalClusterNames;

    public DispatchWrapper() {
        String mdmInternalWrapperClass = (String) MDMConfiguration.getConfiguration().get("mdm.internal.wrapper"); //$NON-NLS-1$
        String userWrapperClass = (String) MDMConfiguration.getConfiguration().get("user.wrapper");  //$NON-NLS-1$
        try {
            IXmlServerSLWrapper internal = (IXmlServerSLWrapper) Class.forName(mdmInternalWrapperClass).newInstance();
            IXmlServerSLWrapper user = (IXmlServerSLWrapper) Class.forName(userWrapperClass).newInstance();
            init(internal, user);
        } catch (Throwable e) {
            LOGGER.error("Initialization error", e); //$NON-NLS-1$
            throw new RuntimeException(e);
        }
    }

    protected DispatchWrapper(IXmlServerSLWrapper internal, IXmlServerSLWrapper user) {
        init(internal, user);
    }

    private static void init(IXmlServerSLWrapper internal, IXmlServerSLWrapper user) {
        if (mdmInternalWrapper == null || userStorageWrapper == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("--- Using dispatch wrapper ---"); //$NON-NLS-1$
            }
            mdmInternalWrapper = internal;
            userStorageWrapper = user;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("MDM internal storage: " + internal.getClass().getName()); //$NON-NLS-1$
                LOGGER.debug("User data storage: " + user.getClass().getName()); //$NON-NLS-1$
            }
        }
    }

    public boolean isUpAndRunning() {
        if (!internalWrapperUp) {
            internalWrapperUp = mdmInternalWrapper.isUpAndRunning();
        }
        if (!userWrapperUp) {
            userWrapperUp = userStorageWrapper.isUpAndRunning();
        }
        return userWrapperUp && internalWrapperUp;
    }

    private static String[] joinArrays(String[] array1, String[] array2) {
        String[] joinedArray = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static boolean isMDMInternal(String clusterName) {
        if(clusterName != null) {
            String pureClusterName = StorageWrapper.getPureClusterName(clusterName);
            for (String internalClusterName : getInternalClusterNames()) {
                if (StringUtils.equalsIgnoreCase(internalClusterName, pureClusterName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Set<String> getInternalClusterNames() {
        if (internalClusterNames == null) {
            synchronized (DispatchWrapper.class) {
                if (internalClusterNames == null) { // Re-check in case of concurrent wait/initialization
                    internalClusterNames = new HashSet<String>();
                    internalClusterNames.add(StringUtils.EMPTY); // Consider an empty cluster name as internal
                    internalClusterNames.add(StorageAdmin.SYSTEM_STORAGE);
                    Map<String, XSystemObjects> systemObjects = XSystemObjects.getXSystemObjects(XObjectType.DATA_CLUSTER);
                    for (Map.Entry<String, XSystemObjects> entry : systemObjects.entrySet()) {
                        // Note #1: TMDM-4507: Update report now stored in SQL storage (user space).
                        // Note #2: Cross referencing is also user space.
                        if (!XSystemObjects.DC_CROSSREFERENCING.getName().equals(entry.getKey())
                                && !XSystemObjects.DC_UPDATE_PREPORT.getName().equals(entry.getKey())) {
                            internalClusterNames.add(entry.getKey());
                        }
                    }
                    internalClusterNames.add("MDMDomainObjects"); //$NON-NLS-1$
                    internalClusterNames.add("MDMItemsTrash"); //$NON-NLS-1$
                    // Adds amalto containers
                    String[] amaltoContainers = new String[] { "amaltoOBJECTSTransformerV2", //$NON-NLS-1$
                            "amaltoOBJECTSFailedRoutingOrderV2", //$NON-NLS-1$
                            "amaltoOBJECTSCompletedRoutingOrderV2", //$NON-NLS-1$
                            "amaltoOBJECTSCustomForm", //$NON-NLS-1$
                            "amaltoOBJECTSjcaadapters", //$NON-NLS-1$
                            "amaltoOBJECTSRoutingEngineV2", //$NON-NLS-1$
                            "amaltoOBJECTSRoutingRule", //$NON-NLS-1$
                            "amaltoOBJECTSSynchronizationItem", //$NON-NLS-1$
                            "amaltoOBJECTSSynchronizationPlan", //$NON-NLS-1$
                            "amaltoOBJECTSservices", //$NON-NLS-1$
                            "amaltoOBJECTSTransformerPluginV2", //$NON-NLS-1$
                            "amaltoOBJECTSroutingorders", //$NON-NLS-1$
                            "amaltoOBJECTSUniverse", //$NON-NLS-1$
                            "amaltoOBJECTSVersioningSystem", //$NON-NLS-1$
                            "amaltoOBJECTSroutingqueues", //$NON-NLS-1$
                            "amaltoOBJECTSroutingservices", //$NON-NLS-1$
                            "amaltoOBJECTSStoredProcedure", //$NON-NLS-1$
                            "amaltoOBJECTSSynchronizationObject", //$NON-NLS-1$
                            "amaltoOBJECTSMatchRule", //$NON-NLS-1$
                            "amaltoOBJECTSVersionSystem", //$NON-NLS-1$
                            "amaltoOBJECTSMenu", //$NON-NLS-1$
                            "amaltoOBJECTSActiveRoutingOrderV2", //$NON-NLS-1$
                            "amaltoOBJECTSDataCluster", //$NON-NLS-1$
                            "amaltoOBJECTSRole", //$NON-NLS-1$
                            "amaltoOBJECTSDataModel", //$NON-NLS-1$
                            "amaltoOBJECTSBackgroundJob", //$NON-NLS-1$
                            "amaltoOBJECTSView", //$NON-NLS-1$
                            "amaltoOBJECTSConfigurationinfo" }; //$NON-NLS-1$
                    internalClusterNames.addAll(Arrays.asList(amaltoContainers));
                }
            }
        }
        return internalClusterNames;
    }

    public String[] getAllClusters() throws XmlServerException {
        String[] internalClusters = mdmInternalWrapper.getAllClusters();
        String[] userClusters = userStorageWrapper.getAllClusters();
        return joinArrays(internalClusters, userClusters);
    }

    public long deleteCluster(String clusterName) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.deleteCluster(clusterName);
        } else {
            return userStorageWrapper.deleteCluster(clusterName);
        }
    }

    public long deleteAllClusters() throws XmlServerException {
        long start = System.currentTimeMillis();
        {
            mdmInternalWrapper.deleteAllClusters();
            userStorageWrapper.deleteAllClusters();
        }
        return System.currentTimeMillis() - start;
    }

    public long createCluster(String clusterName) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.createCluster(clusterName);
        } else {
            return userStorageWrapper.createCluster(clusterName);
        }
    }

    public long putDocumentFromFile(String fileName, String uniqueID, String clusterName) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.putDocumentFromFile(fileName, uniqueID, clusterName);
        } else {
            return userStorageWrapper.putDocumentFromFile(fileName, uniqueID, clusterName);
        }
    }

    public long putDocumentFromFile(String fileName, String uniqueID, String clusterName, String documentType) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.putDocumentFromFile(fileName, uniqueID, clusterName, documentType);
        } else {
            return userStorageWrapper.putDocumentFromFile(fileName, uniqueID, clusterName, documentType);
        }
    }

    public boolean existCluster(String cluster) throws XmlServerException {
        if (isMDMInternal(cluster)) {
            return mdmInternalWrapper.existCluster(cluster);
        } else {
            return userStorageWrapper.existCluster(cluster);
        }
    }

    public long putDocumentFromString(String xmlString, String uniqueID, String clusterName) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.putDocumentFromString(xmlString, uniqueID, clusterName);
        } else {
            return userStorageWrapper.putDocumentFromString(xmlString, uniqueID, clusterName);
        }
    }

    public long putDocumentFromString(String string, String uniqueID, String clusterName, String documentType) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.putDocumentFromString(string, uniqueID, clusterName, documentType);
        } else {
            return userStorageWrapper.putDocumentFromString(string, uniqueID, clusterName, documentType);
        }
    }

    public long putDocumentFromDOM(Element root, String uniqueID, String clusterName) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.putDocumentFromDOM(root, uniqueID, clusterName);
        } else {
            return userStorageWrapper.putDocumentFromDOM(root, uniqueID, clusterName);
        }
    }

    public long putDocumentFromSAX(String dataClusterName, XMLReader docReader, InputSource input) throws XmlServerException {
        if (isMDMInternal(dataClusterName)) {
            return mdmInternalWrapper.putDocumentFromSAX(dataClusterName, docReader, input);
        } else {
            return userStorageWrapper.putDocumentFromSAX(dataClusterName, docReader, input);
        }
    }

    public String getDocumentAsString(String clusterName, String uniqueID) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.getDocumentAsString(clusterName, uniqueID);
        } else {
            return userStorageWrapper.getDocumentAsString(clusterName, uniqueID);
        }
    }

    public String getDocumentAsString(String clusterName, String uniqueID, String encoding) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.getDocumentAsString(clusterName, uniqueID, encoding);
        } else {
            return userStorageWrapper.getDocumentAsString(clusterName, uniqueID, encoding);
        }
    }

    public byte[] getDocumentBytes(String clusterName, String uniqueID, String documentType) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.getDocumentBytes(clusterName, uniqueID, documentType);
        } else {
            return userStorageWrapper.getDocumentBytes(clusterName, uniqueID, documentType);
        }
    }

    public String[] getAllDocumentsUniqueID(String clusterName) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.getAllDocumentsUniqueID(clusterName);
        } else {
            return userStorageWrapper.getAllDocumentsUniqueID(clusterName);
        }
    }

    public long deleteDocument(String clusterName, String uniqueID, String documentType) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.deleteDocument(clusterName, uniqueID, documentType);
        } else {
            return userStorageWrapper.deleteDocument(clusterName, uniqueID, documentType);
        }
    }

    public int deleteItems(String clusterName, String conceptName, IWhereItem whereItem) throws XmlServerException {
        return userStorageWrapper.deleteItems(clusterName, conceptName, whereItem);
    }

    public long moveDocumentById(String sourceClusterName, String uniqueID, String targetClusterName) throws XmlServerException {
        if (isMDMInternal(sourceClusterName)) {
            if (!isMDMInternal(targetClusterName)) {
                throw new IllegalArgumentException("Cannot copy to user data cluster '" + targetClusterName + "'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return mdmInternalWrapper.moveDocumentById(sourceClusterName, uniqueID, targetClusterName);
        } else {
            if (isMDMInternal(targetClusterName)) {
                throw new IllegalArgumentException("Cannot copy to internal data cluster '" + targetClusterName + "'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return userStorageWrapper.moveDocumentById(sourceClusterName, uniqueID, targetClusterName);
        }
    }

    public long countItems(String clusterName, String conceptName, IWhereItem whereItem) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.countItems(clusterName, conceptName, whereItem);
        } else {
            return userStorageWrapper.countItems(clusterName, conceptName, whereItem);
        }
    }

    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.runQuery(clusterName, query, parameters);
        } else {
            return userStorageWrapper.runQuery(clusterName, query, parameters);
        }
    }

    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters, boolean includeNullValue)
            throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.runQuery(clusterName, query, parameters, includeNullValue);
        } else {
            return userStorageWrapper.runQuery(clusterName, query, parameters, includeNullValue);
        }
    }

    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters, int start, int limit,
            boolean withTotalCount) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.runQuery(clusterName, query, parameters, start, limit, withTotalCount);
        } else {
            return userStorageWrapper.runQuery(clusterName, query, parameters, start, limit, withTotalCount);
        }
    }

    public List<String> getItemPKsByCriteria(ItemPKCriteria criteria) throws XmlServerException {
        if (isMDMInternal(criteria.getClusterName())) {
            return mdmInternalWrapper.getItemPKsByCriteria(criteria);
        } else {
            return userStorageWrapper.getItemPKsByCriteria(criteria);
        }
    }

    public void clearCache() {
        mdmInternalWrapper.clearCache();
        userStorageWrapper.clearCache();
    }

    public boolean supportTransaction() {
        return mdmInternalWrapper.supportTransaction() || userStorageWrapper.supportTransaction();
    }

    public void start(String dataClusterName) throws XmlServerException {
        if (isMDMInternal(dataClusterName)) {
            if (mdmInternalWrapper.supportTransaction()) {
                mdmInternalWrapper.start(dataClusterName);
            }
        } else {
            if (userStorageWrapper.supportTransaction()) {
                userStorageWrapper.start(dataClusterName);
            }
        }
    }

    public void commit(String dataClusterName) throws XmlServerException {
        if (isMDMInternal(dataClusterName)) {
            if (mdmInternalWrapper.supportTransaction()) {
                mdmInternalWrapper.commit(dataClusterName);
            }
        } else {
            if (userStorageWrapper.supportTransaction()) {
                userStorageWrapper.commit(dataClusterName);
            }
        }
    }

    public void rollback(String dataClusterName) throws XmlServerException {
        if (isMDMInternal(dataClusterName)) {
            if (mdmInternalWrapper.supportTransaction()) {
                mdmInternalWrapper.rollback(dataClusterName);
            }
        } else {
            if (userStorageWrapper.supportTransaction()) {
                userStorageWrapper.rollback(dataClusterName);
            }
        }
    }

    public void end(String dataClusterName) throws XmlServerException {
        if (isMDMInternal(dataClusterName)) {
            if (mdmInternalWrapper.supportTransaction()) {
                mdmInternalWrapper.end(dataClusterName);
            }
        } else {
            if (userStorageWrapper.supportTransaction()) {
                userStorageWrapper.end(dataClusterName);
            }
        }
    }

    public void close() throws XmlServerException {
        mdmInternalWrapper.close();
        userStorageWrapper.close();
    }

    public List<String> globalSearch(String dataCluster, String keyword, int start, int end) throws XmlServerException {
        if (isMDMInternal(dataCluster)) {
            return mdmInternalWrapper.globalSearch(dataCluster, keyword, start, end);
        } else {
            return userStorageWrapper.globalSearch(dataCluster, keyword, start, end);
        }
    }

    public void exportDocuments(String clusterName, int start, int end, boolean includeMetadata, OutputStream outputStream) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            mdmInternalWrapper.exportDocuments(clusterName, start, end, includeMetadata, outputStream);
        } else {
            userStorageWrapper.exportDocuments(clusterName, start, end, includeMetadata, outputStream);
        }
    }

    public String[] getDocumentsAsString(String clusterName, String[] uniqueIDs) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.getDocumentsAsString(clusterName, uniqueIDs);
        } else {
            return userStorageWrapper.getDocumentsAsString(clusterName, uniqueIDs);
        }
    }

    public String[] getDocumentsAsString(String clusterName, String[] uniqueIDs, String encoding) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.getDocumentsAsString(clusterName, uniqueIDs, encoding);
        } else {
            return userStorageWrapper.getDocumentsAsString(clusterName, uniqueIDs, encoding);
        }
    }
}
