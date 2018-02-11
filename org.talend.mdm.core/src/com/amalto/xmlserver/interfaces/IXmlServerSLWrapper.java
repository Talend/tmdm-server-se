/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.xmlserver.interfaces;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Xtentis performs all calls to the DataBase via this interface<br>
 * which must be implemented by supported DataBases
 * 
 * @author Bruno Grieder
 * 
 */
public interface IXmlServerSLWrapper {

    /** An XML document stored in the DB */
    public final static String TYPE_DOCUMENT = "DOCUMENT"; //$NON-NLS-1$

    /** Sort by ascending order in queries */
    public final static String ORDER_ASCENDING = "ascending"; //$NON-NLS-1$
    
    /** Sort by descending order in queries */
    public final static String ORDER_DESCENDING = "descending"; //$NON-NLS-1$

    
    /**
     * Is the XML Database Server up?
     */
    public boolean isUpAndRunning();

    /**
     * Get all clusters for a particular revision
     * @throws XmlServerException
     */
    public String[] getAllClusters() throws XmlServerException;

    /**
     * Delete a cluster for particular revision
     * @param clusterName
     *          The name of the cluster
     * @return the milliseconds to perform the operation
     * @throws XmlServerException
     * 
     */
    public long deleteCluster(String clusterName) throws XmlServerException;

    /**
     * Delete All clusters for a particular revision
     * @return the milliseconds to perform the operation
     * @throws XmlServerException
     * 
     */
    public long deleteAllClusters() throws XmlServerException;

    /**
     * Create a cluster for a particular revision
     * @param clusterName
     *          The name of the cluster
     * @return the milliseconds to perform the operation
     * @throws XmlServerException
     * 
     */
    public long createCluster(String clusterName) throws XmlServerException;

    /**
     * Reads a document from a file and stores it in the DB 
     * @param revisionID
     *          The ID of the revision, <code>null</code> for the head
     * @param fileName
     *          The full path of the file
     * @param uniqueID
     *          The unique ID of the document
     * @param clusterName
     *          The unique ID of the cluster
     * @return the milliseconds to perform the operation
     * @throws XmlServerException
     */
    public long putDocumentFromFile(String fileName, String uniqueID, String clusterName) throws XmlServerException;

    /**
     * Reads a document from a file and stores it in the DB
     * @param fileName
     *          The full path of the file
     * @param uniqueID
     *          The unique ID of the document
     * @param clusterName
     *          The unique ID of the cluster
     * @param documentType
     *          "DOCUMENT" for and XML document, "BINARY" otherwise
     * @return the milliseconds to perform the operation
     * @throws XmlServerException
     */
    public long putDocumentFromFile(String fileName, String uniqueID, String clusterName, String documentType) throws XmlServerException;

    public boolean existCluster(String cluster)throws XmlServerException;

    /**
     * Read a document from s String an store it in the DB as "DOCUMENT"
     * @param revisionID
     *          The ID of the revision, <code>null</code> for the head
     * @param xmlString
     *          The xml string
     * @param uniqueID
     *          The unique ID of the document
     * @param clusterName
     *          The unique ID of the cluster
     * @return the time to store the document
     * @throws XmlServerException
     */
    public long putDocumentFromString(String xmlString, String uniqueID, String clusterName) throws XmlServerException;

    /**
     * Read a document from a String and store it in the DB
     * @param string
     *          The string to store
     * @param uniqueID
     *          The unique ID of the document
     * @param clusterName
     *          The unique ID of the cluster
     * @param documentType
     *          "DOCUMENT" for and XML document, "BINARY" otherwise
     * @return the time to store the document
     * @throws XmlServerException
     */
    public long putDocumentFromString(
            String string,
            String uniqueID,
            String clusterName,
            String documentType
    ) throws XmlServerException;

    /**
     * Stores a document from its DOM root element
     * @param revisionID
     *          The ID of the revision, <code>null</code> for the head
     * @param root
     *          The DOM root element
     * @param uniqueID
     *          The unique ID of the document
     * @param clusterName
     *          The unique ID of the cluster
     * @return the time to store the document
     * @throws XmlServerException
     */
    public long putDocumentFromDOM(Element root, String uniqueID, String clusterName) throws XmlServerException;

    /**
     * Load a document using a SAX parser.
     *
     * @param dataClusterName The unique ID of the cluster
     * @param docReader A SAX reader
     * @param input A SAX input
     * @throws XmlServerException If anything goes wrong in underlying storage
     */
    public long putDocumentFromSAX(String dataClusterName, XMLReader docReader, InputSource input) throws XmlServerException;

    /**
     * Gets an XML document from the DB<br>
     * The XML instruction will have an encoding specified as UTF-16
     * @param revisionID
     *          The ID of the revision, <code>null</code> for the head
     * @param clusterName
     *          The unique ID of the cluster
     * @param uniqueID
     *          The unique ID of the document
     * @return the document as A string
     * @throws XmlServerException
     */
    public String getDocumentAsString(String clusterName, String uniqueID) throws XmlServerException;

    /**
     * Gets an XML document from the DB<br>
     * The XML instruction will have the encoding specified in the encoding parameter<br>
     * If encoding is null, the document will not have an XML instruction
     * @param revisionID
     *          The ID of the revision, <code>null</code> for the head
     * @param clusterName
     *          The unique ID of the cluster
     * @param uniqueID
     *          The unique ID of the document
     * @param encoding
     *          The encoding of the XML instruction (e.g. UTF-16, UTF-8, etc...).
     * @return the document as A string
     * @throws XmlServerException
     */
    public String getDocumentAsString(String clusterName, String uniqueID, String encoding) throws XmlServerException;

    /**
     * Gets the bytes of a document from the DB<br>
     * For an xml "DOCUMENT" type, the bytes will be encoded using UTF-16
     * 
     * @param revisionID
     *          The ID of the revision, <code>null</code> for the head
     * @param clusterName
     *          The unique ID of the cluster
     * @param uniqueID
     *          The unique ID of the document
     * @param documentType
     *          "DOCUMENT" for and XML document, "BINARY" otherwise
     * @return the document as A string
     * @throws XmlServerException
     */
    public byte[] getDocumentBytes(String clusterName, String uniqueID, String documentType) throws XmlServerException;

    /**
     * The list of documents unique ids in a cluster of a particular revision
     * 
     * @param clusterName
     *          The unique ID of the cluster
     * @return the list of document unique IDs
     * @throws XmlServerException
     */
    public String[] getAllDocumentsUniqueID(String clusterName) throws XmlServerException;

    /**
     * Delete a document
     * @param revisionID
     *          The ID of the revision, <code>null</code> for the head
     * @param clusterName
     *          The unique ID of the cluster
     * @param uniqueID
     *          The unique ID of the document
     * @return the time to perform the delete
     * @throws XmlServerException
     */
    public long deleteDocument(String clusterName, String uniqueID, String documentType) throws XmlServerException;

    /**
         * Delete Items matching a particular condition<br> 
         * @param conceptPatternsToRevisionID
         *          An ordered map that gives the revision ID of a Concept when matching the first pattern found
         * @param conceptPatternsToClusterName
         *          An ordered map that gives the cluster name of a Concept when matching the first pattern found
         * @param clusterName
         *@param conceptName
         *          The Concept of the items being deleted
         * @param whereItem
     *          The condition   @return the number of items deleted
         * @throws XmlServerException
         */
        public int deleteItems(
                String clusterName,
                String conceptName,
                IWhereItem whereItem
        ) throws XmlServerException;

    /**
     * Move a document between two clusters of particular revision
     * 
     * @param targetRevisionID The ID of the target revision
     * @param sourceclusterName The unique ID of the source cluster
     * @param uniqueID The unique ID of the document
     * @param targetclusterName The unique ID of the target cluster
     * @return the time to perform the move
     * @throws XmlServerException
     */
    public long moveDocumentById(String sourceclusterName, String uniqueID, String targetclusterName) throws XmlServerException;

    /**
         * Count Items based on conditions
         *
         * @param conceptPatternsToRevisionID
         *          A map that gives the revision ID of a pattern matching a concept name Concept (isItemQuery is true) or Xtentis Object (isItemQuery is false)
         * @param conceptPatternsToClusterName
         *          An ordered map that gives the cluster name of a Concept when matching the first pattern found
         * @param clusterName
         *@param conceptName
         *          The name of the concept
         * @param whereItem
     *          The condition to apply   @return the number of items meeting the conditions
         * @throws XmlServerException
         */
        public long countItems(
                String clusterName,
                String conceptName,
                IWhereItem whereItem
        ) throws XmlServerException;

    /**
     * Performs a query on the db with optional parameters<br>
     * The parameters are specified as %n in the query where n is the parameter number starting with 0
     * @param revisionID
     *          The ID of the revision, <code>null</code> to run the query from the head
     * @param clusterName
     *          The unique ID of the cluster, <code>null</code> to run the query from the head
     * @param query
     *          The query in the native language
     * @param parameters
     *          The parameter values to replace the %n in the query before execution
     * @return the result of the Query as a Collection of Strings
     * @throws XmlServerException
     */
    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters) throws XmlServerException;

    public ArrayList<String> runQuery(String clusterName,
                              String query, String[] parameters, final int start, final int limit, final boolean withTotalCount)
    throws XmlServerException ;

    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters, boolean includeNullValue) throws XmlServerException;
    /**
     * 
     * Retrieves a list of PKs using provided criteria
     * @param criteria
     * @return
     * @throws XmlServerException
     */
    public List<String> getItemPKsByCriteria(ItemPKCriteria criteria) throws XmlServerException;

    /**
     * clear the item cache
     */
    public void clearCache();

    public boolean supportTransaction();

    public void start(String dataClusterName) throws XmlServerException;

    public void commit(String dataClusterName) throws XmlServerException;

    public void rollback(String dataClusterName) throws XmlServerException;

    public void end(String dataClusterName) throws XmlServerException;

    public void close() throws XmlServerException;
    
    public List<String> globalSearch(String dataCluster, String keyword, int start, int end) throws XmlServerException;

    public void exportDocuments(String clusterName, int start, int end, boolean includeMetadata, OutputStream outputStream) throws XmlServerException;

    /**
     * Gets many XML document from the DB<br>
     * The XML instruction will have an encoding specified as UTF-16
     * @param revisionID
     *          The ID of the revision, <code>null</code> for the head
     * @param clusterName
     *          The unique ID of the cluster
     * @param uniqueIDs
     *          The unique IDs of the document
     * @return the document as A string
     * @throws XmlServerException
     */
    public String[] getDocumentsAsString(String clusterName, String[] uniqueIDs) throws XmlServerException;
    
    /**
     * Gets many XML document from the DB<br>
     * The XML instruction will have the encoding specified in the encoding parameter<br>
     * If encoding is null, the document will not have an XML instruction
     * @param revisionID
     *          The ID of the revision, <code>null</code> for the head
     * @param clusterName
     *          The unique ID of the cluster
     * @param uniqueIDs
     *          The unique IDs of the document
     * @param encoding
     *          The encoding of the XML instruction (e.g. UTF-16, UTF-8, etc...).
     * @return the document as A string
     * @throws XmlServerException
     */
    public String[] getDocumentsAsString(String clusterName, String[] uniqueIDs, String encoding) throws XmlServerException;
}