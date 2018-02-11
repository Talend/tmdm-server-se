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

package com.amalto.core.server.api;

import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.ItemPKCriteria;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.OutputStream;
import java.util.*;

public interface XmlServer {

    /**
     * Get all clusters for a particular revision
     *
     * @return the list of cluster IDs
     */
    String[] getAllClusters() throws XtentisException;

    /**
     * clear the item cache
     */
    void clearCache() throws XtentisException;

    /**
     * Delete a cluster for particular revision
     *
     * @param clusterName The name of the cluster
     * @return the milliseconds to perform the operation
     */
    long deleteCluster(String clusterName) throws XtentisException;

    /**
     * Create a cluster for a particular revision
     *
     * @param clusterName The name of the cluster
     * @return the milliseconds to perform the operation
     */
    long createCluster(String clusterName) throws XtentisException;

    /**
     * Reads a document from a file and stores it in the DB
     *
     * @param fileName                The full path of the file
     * @param uniqueID                The unique ID of the document
     * @param clusterName             The unique ID of the cluster
     * @param documentType ="DOCUMENT"
     * @return the milliseconds to perform the operation
     */
    long putDocumentFromFile(String fileName, String uniqueID, String clusterName, String documentType)
            throws XtentisException;

    /**
     * Read a document from s String an store it in the DB as "DOCUMENT"
     *
     * @param revisionID  The ID of the revision, <code>null</code> for the head
     * @param xmlString   The xml string
     * @param uniqueID    The unique ID of the document
     * @param clusterName The unique ID of the cluster
     * @return the time to store the document
     */
    long putDocumentFromString(String xmlString, String uniqueID, String clusterName)
            throws XtentisException;

    /**
     * Read a document from a String and store it in the DB
     *
     * @param string                  The string to store
     * @param uniqueID                The unique ID of the document
     * @param clusterName             The unique ID of the cluster
     * @param documentType ="DOCUMENT"
     * @return the time to store the document
     */
    long putDocumentFromString(String string, String uniqueID, String clusterName, String documentType)
            throws XtentisException;

    /**
     * Load a document using a SAX parser.
     *
     * @param dataClusterName The unique ID of the cluster
     * @param docReader       A SAX reader
     * @param input           A SAX input
     * @throws XtentisException If anything goes wrong in underlying storage
     */
    long putDocumentFromSAX(String dataClusterName, XMLReader docReader, InputSource input)
            throws XtentisException;

    /**
     * Gets an XML document from the DB<br>
     * The XML instruction will have an encoding specified as UTF-16
     *
     * @param revisionID  The ID of the revision, <code>null</code> for the head
     * @param clusterName The unique ID of the cluster
     * @param uniqueID    The unique ID of the document
     * @return the document as A string
     */
    String getDocumentAsString(String clusterName, String uniqueID) throws XtentisException;

    boolean existCluster(String cluster) throws XtentisException;

    /**
     * Gets an XML document from the DB<br>
     * The XML instruction will have the encoding specified in the encoding parameter<br>
     * If encoding is null, the document will not have an XML instruction
     *
     * @param revisionID  The ID of the revision, <code>null</code> for the head
     * @param clusterName The unique ID of the cluster
     * @param uniqueID    The unique ID of the document
     * @param encoding    The encoding of the XML instruction (e.g. UTF-16, UTF-8, etc...).
     * @return the document as A string
     */
    String getDocumentAsString(String clusterName, String uniqueID, String encoding)
            throws XtentisException;

    /**
     * Gets the bytes of a document from the DB<br>
     * For an xml "DOCUMENT" type, the bytes will be encoded using UTF-16 The XML instruction will have an encoding
     * specified as UTF-16
     *
     * @param revisionID              The ID of the revision, <code>null</code> for the head
     * @param clusterName             The unique ID of the cluster
     * @param uniqueID                The unique ID of the document
     * @param documentType ="DOCUMENT"
     * @return the document as A string
     */
    byte[] getDocumentBytes(String clusterName, String uniqueID, String documentType)
            throws XtentisException;

    /**
     * The list of documents unique ids in a cluster of a particular revision
     *
     * @param clusterName The unique ID of the cluster
     * @return the list of document unique IDs
     */
    String[] getAllDocumentsUniqueID(String clusterName) throws XtentisException;

    /**
     * Delete an XML document
     *
     * @param revisionID  The ID of the revision, <code>null</code> for the head
     * @param clusterName The unique ID of the cluster
     * @param uniqueID    The unique ID of the document
     * @return the time to perform the delete
     */
    long deleteDocument(String clusterName, String uniqueID) throws XtentisException;

    /**
     * Delete a document
     *
     * @param revisionID  The ID of the revision, <code>null</code> for the head
     * @param clusterName The unique ID of the cluster
     * @param uniqueID    The unique ID of the document
     * @return the time to perform the delete
     */
    long deleteDocument(String clusterName, String uniqueID, String documentType)
            throws XtentisException;

    /**
     * Delete Items matching a particular condition<br>
     *
     * @param conceptPatternsToRevisionID  An ordered map that gives the revision ID of a Concept when matching the first
     *                                     pattern found
     * @param conceptPatternsToClusterName An ordered map that gives the cluster name of a Concept when matching the
     *                                     first pattern found
     * @param clusterName
     *@param conceptName                  The Concept of the items being deleted
     * @param whereItem                    The condition   @return the number of items deleted
     */
    int deleteItems(String clusterName,
                    String conceptName,
                    IWhereItem whereItem) throws XtentisException;

    /**
     * Count Items based on conditions
     *
     * @param conceptPatternsToRevisionID  A map that gives the revision ID of a pattern matching a concept name Concept
     *                                     (isItemQuery is true) or Xtentis Object (isItemQuery is false)
     * @param conceptPatternsToClusterName An ordered map that gives the cluster name of a Concept when matching the
     *                                     first pattern found
     * @param clusterName
     *@param conceptName                  The name of the concept
     * @param whereItem                    The condition to apply   @return the number of items meeting the conditions
     * @throws com.amalto.core.util.XtentisException
     *
     */
    long countItems(String clusterName,
                    String conceptName,
                    IWhereItem whereItem) throws XtentisException;

    /**
     * Performs a query on the db with optional parameters<br>
     * The parameters are specified as %n in the query where n is the parameter number starting with 0
     *
     * @param revisionID  The ID of the revision, <code>null</code> to run from the head
     * @param clusterName The unique ID of the cluster, <code>null</code> to run from the head of the revision ID
     * @param query       The query in the native language
     * @param parameters  The parameter values to replace the %n in the query before execution
     * @return the result of the Query as a Collection of Strings
     */
    ArrayList runQuery(String clusterName, String query, String[] parameters) throws XtentisException;

    ArrayList runQuery(String clusterName, String query, String[] parameters, boolean includeNullValue) throws XtentisException;

    List<String> getItemPKsByCriteria(ItemPKCriteria criteria) throws XtentisException;

    boolean supportTransaction();

    void start(String dataClusterName) throws XtentisException;

    void commit(String dataClusterName) throws XtentisException;

    void rollback(String dataClusterName) throws XtentisException;

    void end(String dataClusterName) throws XtentisException;

    void close() throws XtentisException;

    void exportDocuments(String clusterName, int start, int end, boolean includeMetadata, OutputStream outputStream) throws XtentisException;
    
    List<String> globalSearch(String dataCluster, String keyword, int start, int end) throws XtentisException;

    boolean supportStaging(String dataCluster) throws XtentisException;

    /**
     * Gets many XML documents from the DB<br>
     * The XML instruction will have an encoding specified as UTF-16
     *
     * @param revisionID  The ID of the revision, <code>null</code> for the head
     * @param clusterName The unique ID of the cluster
     * @param uniqueIDs    The unique IDs of the document
     * @return the document as A string
     */
    String[] getDocumentsAsString(String clusterName, String[] uniqueIDs) throws XtentisException;

    /**
     * Gets many XML documents from the DB<br>
     * The XML instruction will have the encoding specified in the encoding parameter<br>
     * If encoding is null, the document will not have an XML instruction
     *
     * @param revisionID  The ID of the revision, <code>null</code> for the head
     * @param clusterName The unique ID of the cluster
     * @param uniqueIDs    The unique IDs of the document
     * @param encoding    The encoding of the XML instruction (e.g. UTF-16, UTF-8, etc...).
     * @return the document as A string
     */
    String[] getDocumentsAsString(String clusterName, String[] uniqueIDs, String encoding)
            throws XtentisException;
}
  