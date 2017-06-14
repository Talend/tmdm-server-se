/*
 * Copyright (C) 2006-2014 Talend Inc. - www.talend.com
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

    private static IXmlServerSLWrapper mdmInternalWrapper;

    private static IXmlServerSLWrapper userStorageWrapper;

    private final static Logger LOGGER = Logger.getLogger(DispatchWrapper.class);

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
            LOGGER.error("Initialization error", e);
            throw new RuntimeException(e);
        }
    }

    protected DispatchWrapper(IXmlServerSLWrapper internal, IXmlServerSLWrapper user) {
        init(internal, user);
    }

    private static void init(IXmlServerSLWrapper internal, IXmlServerSLWrapper user) {
        if (mdmInternalWrapper == null || userStorageWrapper == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("--- Using dispatch wrapper ---");
            }
            mdmInternalWrapper = internal;
            userStorageWrapper = user;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("MDM internal storage: " + internal.getClass().getName());
                LOGGER.debug("User data storage: " + user.getClass().getName());
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
        for (String internalClusterName : getInternalClusterNames()) {
            if (StringUtils.equalsIgnoreCase(internalClusterName, clusterName)) {
                return true;
            }
        }
        return false;
    }

    public static synchronized Set<String> getInternalClusterNames() {
        if (internalClusterNames == null) {
            internalClusterNames = new HashSet<String>();
            internalClusterNames.add(null);
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
            internalClusterNames.add("MDMItemsTrash");
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
                    "amaltoOBJECTSLicense", //$NON-NLS-1$
                    "amaltoOBJECTSRole", //$NON-NLS-1$
                    "amaltoOBJECTSDataModel", //$NON-NLS-1$
                    "amaltoOBJECTSBackgroundJob", //$NON-NLS-1$
                    "amaltoOBJECTSView", //$NON-NLS-1$
                    "amaltoOBJECTSConfigurationinfo" }; //$NON-NLS-1$
            internalClusterNames.addAll(Arrays.asList(amaltoContainers));
        }
        return internalClusterNames;
    }

    private static String getClusterName(Map<String, String> conceptPatternsToClusterName, String conceptName) {
        String clusterName = null;
        for (Map.Entry<String, String> conceptPattern : conceptPatternsToClusterName.entrySet()) {
            if (conceptName.matches(conceptPattern.getKey())) {
                clusterName = conceptPattern.getValue();
            }
        }
        if (clusterName == null) {
            throw new IllegalArgumentException("Type '" + conceptName + "' did not match any pattern.");
        }
        return clusterName;
    }

    public String[] getAllClusters(String revisionID) throws XmlServerException {
        String[] internalClusters = mdmInternalWrapper.getAllClusters(revisionID);
        String[] userClusters = userStorageWrapper.getAllClusters(revisionID);
        return joinArrays(internalClusters, userClusters);
    }

    public long deleteCluster(String revisionID, String clusterName) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.deleteCluster(revisionID, clusterName);
        } else {
            return userStorageWrapper.deleteCluster(revisionID, clusterName);
        }
    }

    public long deleteAllClusters(String revisionID) throws XmlServerException {
        long start = System.currentTimeMillis();
        {
            mdmInternalWrapper.deleteAllClusters(revisionID);
            userStorageWrapper.deleteAllClusters(revisionID);
        }
        return System.currentTimeMillis() - start;
    }

    public long createCluster(String revisionID, String clusterName) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.createCluster(revisionID, clusterName);
        } else {
            return userStorageWrapper.createCluster(revisionID, clusterName);
        }
    }

    public long putDocumentFromFile(String fileName, String uniqueID, String clusterName, String revisionID) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.putDocumentFromFile(fileName, uniqueID, clusterName, revisionID);
        } else {
            return userStorageWrapper.putDocumentFromFile(fileName, uniqueID, clusterName, revisionID);
        }
    }

    public long putDocumentFromFile(String fileName, String uniqueID, String clusterName, String revisionID, String documentType) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.putDocumentFromFile(fileName, uniqueID, clusterName, revisionID, documentType);
        } else {
            return userStorageWrapper.putDocumentFromFile(fileName, uniqueID, clusterName, revisionID, documentType);
        }
    }

    public boolean existCluster(String revision, String cluster) throws XmlServerException {
        if (isMDMInternal(cluster)) {
            return mdmInternalWrapper.existCluster(revision, cluster);
        } else {
            return userStorageWrapper.existCluster(revision, cluster);
        }
    }

    public long putDocumentFromString(String xmlString, String uniqueID, String clusterName, String revisionID) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.putDocumentFromString(xmlString, uniqueID, clusterName, revisionID);
        } else {
            return userStorageWrapper.putDocumentFromString(xmlString, uniqueID, clusterName, revisionID);
        }
    }

    public long putDocumentFromString(String string, String uniqueID, String clusterName, String revisionID, String documentType) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.putDocumentFromString(string, uniqueID, clusterName, revisionID, documentType);
        } else {
            return userStorageWrapper.putDocumentFromString(string, uniqueID, clusterName, revisionID, documentType);
        }
    }

    public long putDocumentFromDOM(Element root, String uniqueID, String clusterName, String revisionID) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.putDocumentFromDOM(root, uniqueID, clusterName, revisionID);
        } else {
            return userStorageWrapper.putDocumentFromDOM(root, uniqueID, clusterName, revisionID);
        }
    }

    public long putDocumentFromSAX(String dataClusterName, XMLReader docReader, InputSource input, String revisionId) throws XmlServerException {
        if (isMDMInternal(dataClusterName)) {
            return mdmInternalWrapper.putDocumentFromSAX(dataClusterName, docReader, input, revisionId);
        } else {
            return userStorageWrapper.putDocumentFromSAX(dataClusterName, docReader, input, revisionId);
        }
    }

    public String getDocumentAsString(String revisionID, String clusterName, String uniqueID) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.getDocumentAsString(revisionID, clusterName, uniqueID);
        } else {
            return userStorageWrapper.getDocumentAsString(revisionID, clusterName, uniqueID);
        }
    }

    public String getDocumentAsString(String revisionID, String clusterName, String uniqueID, String encoding) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.getDocumentAsString(revisionID, clusterName, uniqueID, encoding);
        } else {
            return userStorageWrapper.getDocumentAsString(revisionID, clusterName, uniqueID, encoding);
        }
    }

    public byte[] getDocumentBytes(String revisionID, String clusterName, String uniqueID, String documentType) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.getDocumentBytes(revisionID, clusterName, uniqueID, documentType);
        } else {
            return userStorageWrapper.getDocumentBytes(revisionID, clusterName, uniqueID, documentType);
        }
    }

    public String[] getAllDocumentsUniqueID(String revisionID, String clusterName) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.getAllDocumentsUniqueID(revisionID, clusterName);
        } else {
            return userStorageWrapper.getAllDocumentsUniqueID(revisionID, clusterName);
        }
    }

    public long deleteDocument(String revisionID, String clusterName, String uniqueID, String documentType) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.deleteDocument(revisionID, clusterName, uniqueID, documentType);
        } else {
            return userStorageWrapper.deleteDocument(revisionID, clusterName, uniqueID, documentType);
        }
    }

    public int deleteXtentisObjects(HashMap<String, String> objectRootElementNameToRevisionID, HashMap<String, String> objectRootElementNameToClusterName, String objectRootElementName, IWhereItem whereItem) throws XmlServerException {
        return mdmInternalWrapper.deleteXtentisObjects(objectRootElementNameToRevisionID, objectRootElementNameToClusterName, objectRootElementName, whereItem);
    }

    public int deleteItems(LinkedHashMap<String, String> conceptPatternsToRevisionID, LinkedHashMap<String, String> conceptPatternsToClusterName, String conceptName, IWhereItem whereItem) throws XmlServerException {
        return userStorageWrapper.deleteItems(conceptPatternsToRevisionID, conceptPatternsToClusterName, conceptName, whereItem);
    }

    public long moveDocumentById(String sourceRevisionID, String sourceClusterName, String uniqueID, String targetRevisionID, String targetClusterName) throws XmlServerException {
        if (isMDMInternal(sourceClusterName)) {
            if (!isMDMInternal(targetClusterName)) {
                throw new IllegalArgumentException("Cannot copy to user data cluster '" + targetClusterName + "'");
            }
            return mdmInternalWrapper.moveDocumentById(sourceRevisionID, sourceClusterName, uniqueID, targetRevisionID, targetClusterName);
        } else {
            if (isMDMInternal(targetClusterName)) {
                throw new IllegalArgumentException("Cannot copy to internal data cluster '" + targetClusterName + "'");
            }
            return userStorageWrapper.moveDocumentById(sourceRevisionID, sourceClusterName, uniqueID, targetRevisionID, targetClusterName);
        }
    }

    public long countItems(Map<String, String> conceptPatternsToRevisionID, Map<String, String> conceptPatternsToClusterName, String conceptName, IWhereItem whereItem) throws XmlServerException {
        if (isMDMInternal(getClusterName(conceptPatternsToClusterName, conceptName))) {
            return mdmInternalWrapper.countItems(conceptPatternsToRevisionID, conceptPatternsToClusterName, conceptName, whereItem);
        } else {
            return userStorageWrapper.countItems(conceptPatternsToRevisionID, conceptPatternsToClusterName, conceptName, whereItem);
        }
    }

    public long countXtentisObjects(HashMap<String, String> objectRootElementNameToRevisionID, HashMap<String, String> objectRootElementNameToClusterName, String mainObjectRootElementName, IWhereItem whereItem) throws XmlServerException {
        if (isMDMInternal(getClusterName(objectRootElementNameToClusterName, mainObjectRootElementName))) {
            return mdmInternalWrapper.countItems(objectRootElementNameToRevisionID, objectRootElementNameToClusterName, mainObjectRootElementName, whereItem);
        } else {
            return userStorageWrapper.countItems(objectRootElementNameToRevisionID, objectRootElementNameToClusterName, mainObjectRootElementName, whereItem);
        }
    }

    public String getItemsQuery(Map<String, String> conceptPatternsToRevisionID, Map<String, String> conceptPatternsToClusterName, String forceMainPivot, ArrayList<String> viewableFullPaths, IWhereItem whereItem, String orderBy, String direction, int start, int limit) throws XmlServerException {
        return mdmInternalWrapper.getItemsQuery(conceptPatternsToRevisionID, conceptPatternsToClusterName, forceMainPivot, viewableFullPaths, whereItem, orderBy, direction, start, limit);
    }

    public String getItemsQuery(Map<String, String> conceptPatternsToRevisionID, Map<String, String> conceptPatternsToClusterName, String forceMainPivot, ArrayList<String> viewableFullPaths, IWhereItem whereItem, String orderBy, String direction, int start, long limit, boolean totalCountOnFirstRow, Map<String, ArrayList<String>> metaDataTypes) throws XmlServerException {
        return mdmInternalWrapper.getItemsQuery(conceptPatternsToRevisionID, conceptPatternsToClusterName, forceMainPivot, viewableFullPaths, whereItem, orderBy, direction, start, limit, totalCountOnFirstRow, metaDataTypes);
    }

    public String getXtentisObjectsQuery(HashMap<String, String> objectRootElementNameToRevisionID, HashMap<String, String> objectRootElementNameToClusterName, String mainObjectRootElementName, ArrayList<String> viewableFullPaths, IWhereItem whereItem, String orderBy, String direction, int start, int limit) throws XmlServerException {
        return mdmInternalWrapper.getXtentisObjectsQuery(objectRootElementNameToRevisionID, objectRootElementNameToClusterName, mainObjectRootElementName, viewableFullPaths, whereItem, orderBy, direction, start, limit);
    }

    public String getXtentisObjectsQuery(LinkedHashMap<String, String> objectRootElementNameToRevisionID, LinkedHashMap<String, String> objectRootElementNameToClusterName, String mainObjectRootElementName, ArrayList<String> viewableFullPaths, IWhereItem whereItem, String orderBy, String direction, int start, long limit, boolean totalCountOnFirstRow) throws XmlServerException {
        return mdmInternalWrapper.getXtentisObjectsQuery(objectRootElementNameToRevisionID, objectRootElementNameToClusterName, mainObjectRootElementName, viewableFullPaths, whereItem, orderBy, direction, start, limit, totalCountOnFirstRow);
    }

    public String getPivotIndexQuery(String clusterName, String mainPivotName, LinkedHashMap<String, String[]> pivotWithKeys, LinkedHashMap<String, String> itemsRevisionIDs, String defaultRevisionID, String[] indexPaths, IWhereItem whereItem, String[] pivotDirections, String[] indexDirections, int start, int limit) throws XmlServerException {
        return mdmInternalWrapper.getPivotIndexQuery(clusterName, mainPivotName, pivotWithKeys, itemsRevisionIDs, defaultRevisionID, indexPaths, whereItem, pivotDirections, indexDirections, start, limit);
    }

    public String getChildrenItemsQuery(String clusterName, String conceptName, String[] PKXPaths, String FKXpath, String labelXpath, String fatherPK, LinkedHashMap<String, String> itemsRevisionIDs, String defaultRevisionID, IWhereItem whereItem, int start, int limit) throws XmlServerException {
        return mdmInternalWrapper.getChildrenItemsQuery(clusterName, conceptName, PKXPaths, FKXpath, labelXpath, fatherPK, itemsRevisionIDs, defaultRevisionID, whereItem, start, limit);
    }

    public ArrayList<String> runQuery(String revisionID, String clusterName, String query, String[] parameters) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.runQuery(revisionID, clusterName, query, parameters);
        } else {
            return userStorageWrapper.runQuery(revisionID, clusterName, query, parameters);
        }
    }

    public ArrayList<String> runQuery(String revisionID, String clusterName, String query, String[] parameters, int start, int limit, boolean withTotalCount) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            return mdmInternalWrapper.runQuery(revisionID, clusterName, query, parameters, start, limit, withTotalCount);
        } else {
            return userStorageWrapper.runQuery(revisionID, clusterName, query, parameters, start, limit, withTotalCount);
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

    public void exportDocuments(String revisionId, String clusterName, int start, int end, boolean includeMetadata, OutputStream outputStream) throws XmlServerException {
        if (isMDMInternal(clusterName)) {
            mdmInternalWrapper.exportDocuments(revisionId, clusterName, start, end, includeMetadata, outputStream);
        } else {
            userStorageWrapper.exportDocuments(revisionId, clusterName, start, end, includeMetadata, outputStream);
        }
    }
}
