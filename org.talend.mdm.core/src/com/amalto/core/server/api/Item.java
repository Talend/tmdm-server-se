/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server.api;

import com.amalto.core.integrity.FKIntegrityCheckResult;
import com.amalto.core.objects.DroppedItemPOJOPK;
import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import com.amalto.core.objects.view.ViewPOJOPK;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.ItemPKCriteria;

import java.util.ArrayList;
import java.util.List;

public interface Item {

    /**
     * Creates or updates a item
     *
     * @throws com.amalto.core.util.XtentisException
     */
    public ItemPOJOPK putItem(ItemPOJO item, DataModelPOJO datamodel) throws com.amalto.core.util.XtentisException;

    /**
     * Updates a item taskId. Is equivalent to {@link #putItem(ItemPOJO, DataModelPOJO)}.
     *
     */
    public ItemPOJOPK updateItemMetadata(ItemPOJO item) throws XtentisException;
    
    /**
     * Get item
     *
     * @throws com.amalto.core.util.XtentisException
     */
    public ItemPOJO getItem(ItemPOJOPK pk) throws com.amalto.core.util.XtentisException;

    /**
     * Is Item modified by others - no exception is thrown: true|false
     *
     * @throws com.amalto.core.util.XtentisException
     */
    public boolean isItemModifiedByOther(ItemPOJOPK item, long time) throws com.amalto.core.util.XtentisException;

    /**
     * Get an item - no exception is thrown: returns null if not found
     *
     * @throws com.amalto.core.util.XtentisException
     */
    public ItemPOJO existsItem(ItemPOJOPK pk) throws com.amalto.core.util.XtentisException;

    /**
     * Remove an item - returns null if no item was deleted
     *
     * @throws com.amalto.core.util.XtentisException
     */
    public ItemPOJOPK deleteItem(ItemPOJOPK pk, boolean override) throws com.amalto.core.util.XtentisException;

    /**
     * Delete items in a stateless mode: open a connection --> perform delete --> close the connection
     *
     * @throws com.amalto.core.util.XtentisException
     */
    public int deleteItems(DataClusterPOJOPK dataClusterPOJOPK, java.lang.String conceptName,
            com.amalto.xmlserver.interfaces.IWhereItem search, int spellThreshold, boolean override)
            throws com.amalto.core.util.XtentisException;

    /**
     * Drop an item - returns null if no item was dropped
     *
     * @throws com.amalto.core.util.XtentisException
     */
    public DroppedItemPOJOPK dropItem(ItemPOJOPK itemPOJOPK, java.lang.String partPath, boolean override)
            throws com.amalto.core.util.XtentisException;

    /**
     * Search Items thru a view in a cluster and specifying a condition
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param viewPOJOPK The View
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException
     */
    public java.util.ArrayList viewSearch(DataClusterPOJOPK dataClusterPOJOPK, ViewPOJOPK viewPOJOPK,
            com.amalto.xmlserver.interfaces.IWhereItem whereItem, int spellThreshold, int start, int limit)
            throws com.amalto.core.util.XtentisException;

    /**
     * Search ordered Items thru a view in a cluster and specifying a condition
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param viewPOJOPK The View
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of ASC or DESC
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException
     */
    public java.util.ArrayList viewSearch(DataClusterPOJOPK dataClusterPOJOPK, ViewPOJOPK viewPOJOPK,
            com.amalto.xmlserver.interfaces.IWhereItem whereItem, int spellThreshold, java.lang.String orderBy,
            java.lang.String direction, int start, int limit) throws com.amalto.core.util.XtentisException;

    /**
     * Returns an ordered collection of results searched in a cluster and specifying an optional condition<br/>
     * The results are xml objects made of elements constituted by the specified viewablePaths
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param forceMainPivot An optional pivot that will appear first in the list of pivots in the query<br>
     * : This allows forcing cartesian products: for instance Order Header vs Order Line
     * @param viewablePaths The list of elements returned in each result
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @param returnCount <code>true</code> returns total count as first result.
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException
     */
    public java.util.ArrayList xPathsSearch(DataClusterPOJOPK dataClusterPOJOPK, String forceMainPivot,
            ArrayList<String> viewablePaths, IWhereItem whereItem, int spellThreshold, int start, int limit, boolean returnCount)
            throws com.amalto.core.util.XtentisException;

    /**
     * Returns an ordered collection of results searched in a cluster and specifying an optional condition<br/>
     * The results are xml objects made of elements constituted by the specified viewablePaths
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param forceMainPivot An optional pivot that will appear first in the list of pivots in the query<br>
     * : This allows forcing cartesian products: for instance Order Header vs Order Line
     * @param viewablePaths The list of elements returned in each result
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of ASC or DESC
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @param returnCount <code>true</code> returns total count as first result.
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException
     */
    public java.util.ArrayList xPathsSearch(DataClusterPOJOPK dataClusterPOJOPK, String forceMainPivot,
            ArrayList<String> viewablePaths, IWhereItem whereItem, int spellThreshold, String orderBy, String direction,
            int start, int limit, boolean returnCount) throws com.amalto.core.util.XtentisException;

    /**
     * Count the items denoted by concept name meeting the optional condition whereItem
     *
     * @param dataClusterPOJOPK
     * @param conceptName
     * @param whereItem
     * @param spellThreshold
     * @return The number of items found
     * @throws com.amalto.core.util.XtentisException
     */
    public long count(DataClusterPOJOPK dataClusterPOJOPK, java.lang.String conceptName,
            com.amalto.xmlserver.interfaces.IWhereItem whereItem, int spellThreshold)
            throws com.amalto.core.util.XtentisException;

    /**
     * Search ordered Items thru a view in a cluster and specifying a condition
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param viewPOJOPK The View
     * @param searchValue The value/sentenced searched
     * @param matchAllWords If <code>true</code>, the items must match all the words in the sentence
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of ASC or DESC.
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException
     */
    public java.util.ArrayList quickSearch(DataClusterPOJOPK dataClusterPOJOPK, ViewPOJOPK viewPOJOPK,
            java.lang.String searchValue, boolean matchAllWords, int spellThreshold, java.lang.String orderBy,
            java.lang.String direction, int start, int limit) throws com.amalto.core.util.XtentisException;

    /**
     * Get the possible value for the business Element Path, optionally filtered by a condition
     *
     * @param dataClusterPOJOPK The data cluster where to run the query
     * @param businessElementPath The business element path. Must be of the form
     * <code>ConceptName/[optional sub elements]/element</code>
     * @param whereItem The optional condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of ASC or DESC.
     * @return The list of values
     * @throws com.amalto.core.util.XtentisException
     */
    public java.util.ArrayList getFullPathValues(DataClusterPOJOPK dataClusterPOJOPK, java.lang.String businessElementPath,
            com.amalto.xmlserver.interfaces.IWhereItem whereItem, int spellThreshold, java.lang.String orderBy,
            java.lang.String direction) throws com.amalto.core.util.XtentisException;

    /**
     * Extract results thru a view and transform them using a transformer<br/>
     * This call is asynchronous and results will be pushed via the passed
     * {@link com.amalto.core.objects.transformers.util.TransformerCallBack}
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param context The {@link com.amalto.core.objects.transformers.util.TransformerContext} containi the inital
     * context and the transformer name
     * @param globalCallBack The callback function called by the transformer when it completes a step
     * @param viewPOJOPK A filtering view
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of ASC or DESC.
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     */
    public void extractUsingTransformerThroughView(DataClusterPOJOPK dataClusterPOJOPK,
            com.amalto.core.objects.transformers.util.TransformerContext context,
            com.amalto.core.objects.transformers.util.TransformerCallBack globalCallBack, ViewPOJOPK viewPOJOPK,
            com.amalto.xmlserver.interfaces.IWhereItem whereItem, int spellThreshold, java.lang.String orderBy,
            java.lang.String direction, int start, int limit) throws com.amalto.core.util.XtentisException;

    /**
     * Extract results thru a view and transform them using a transformer<br/>
     * This call is asynchronous and results will be pushed via the passed
     * {@link com.amalto.core.objects.transformers.util.TransformerCallBack}
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param transformerPOJOPK The transformer to use
     * @param viewPOJOPK A filtering view
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of ASC or DESC.
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     */
    public com.amalto.core.objects.transformers.util.TransformerContext extractUsingTransformerThroughView(
            DataClusterPOJOPK dataClusterPOJOPK, TransformerV2POJOPK transformerPOJOPK, ViewPOJOPK viewPOJOPK,
            com.amalto.xmlserver.interfaces.IWhereItem whereItem, int spellThreshold, java.lang.String orderBy,
            java.lang.String direction, int start, int limit) throws com.amalto.core.util.XtentisException;

    public java.util.ArrayList runQuery(DataClusterPOJOPK dataClusterPOJOPK, String query, String[] parameters)
            throws com.amalto.core.util.XtentisException;

    /**
     * Returns a map with keys being the concepts found in the Data Cluster and as value the revisionID
     *
     * @param dataClusterPOJOPK
     * @return A {@link java.util.Map} of concept names to revision IDs
     * @throws com.amalto.core.util.XtentisException
     */
    public List<String> getConceptsInDataCluster(DataClusterPOJOPK dataClusterPOJOPK)
            throws com.amalto.core.util.XtentisException;

    public long countItemsByCustomFKFilters(DataClusterPOJOPK dataClusterPOJOPK, java.lang.String conceptName,
            java.lang.String injectedXpath) throws com.amalto.core.util.XtentisException;

    public java.util.ArrayList getItemsByCustomFKFilters(DataClusterPOJOPK dataClusterPOJOPK, ArrayList<String> viewablePaths,
            String customXPath, IWhereItem whereItem, int start, int limit, String orderBy, String direction, boolean returnCount)
            throws com.amalto.core.util.XtentisException;

    /**
     * Get unordered items of a Concept using an optional where condition
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param conceptName The name of the concept
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException
     */
    public java.util.ArrayList getItems(DataClusterPOJOPK dataClusterPOJOPK, java.lang.String conceptName,
            com.amalto.xmlserver.interfaces.IWhereItem whereItem, int spellThreshold, int start, int limit,
            boolean totalCountOnFirstRow) throws com.amalto.core.util.XtentisException;

    /**
     * Get potentially ordered items of a Concept using an optional where condition
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param conceptName The name of the concept
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of ASC or DESC.
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException
     */
    public java.util.ArrayList getItems(DataClusterPOJOPK dataClusterPOJOPK, java.lang.String conceptName,
            com.amalto.xmlserver.interfaces.IWhereItem whereItem, int spellThreshold, java.lang.String orderBy,
            java.lang.String direction, int start, int limit, boolean totalCountOnFirstRow)
            throws com.amalto.core.util.XtentisException;

    /**
     * @param dataClusterPK
     * @param concept
     * @param ids
     * @return
     * @throws com.amalto.core.util.XtentisException
     */
    public FKIntegrityCheckResult checkFKIntegrity(String dataClusterPK, String concept, String[] ids)
            throws com.amalto.core.util.XtentisException;

    List<String> getItemPKsByCriteria(ItemPKCriteria criteria) throws XtentisException;
}
