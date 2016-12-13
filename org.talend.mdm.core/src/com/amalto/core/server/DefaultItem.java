/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server;

import static com.amalto.core.query.user.UserQueryBuilder.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.integrity.FKIntegrityCheckResult;
import com.amalto.core.integrity.FKIntegrityChecker;
import com.amalto.core.objects.DroppedItemPOJOPK;
import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import com.amalto.core.objects.transformers.util.TransformerCallBack;
import com.amalto.core.objects.transformers.util.TransformerContext;
import com.amalto.core.objects.transformers.util.TypedContent;
import com.amalto.core.objects.view.ViewPOJO;
import com.amalto.core.objects.view.ViewPOJOPK;
import com.amalto.core.query.user.OrderBy;
import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.query.user.UserQueryHelper;
import com.amalto.core.server.api.Item;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.storage.DispatchWrapper;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.SystemStorageWrapper;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordDefaultWriter;
import com.amalto.core.storage.record.DataRecordWriter;
import com.amalto.core.util.EntityNotFoundException;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.CustomWhereCondition;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.ItemPKCriteria;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;
import com.amalto.xmlserver.interfaces.WhereOr;
import com.amalto.xmlserver.interfaces.XmlServerException;

public class DefaultItem implements Item {

    private static final String DEFAULT_VARIABLE = "_DEFAULT_"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(DefaultItem.class);

    /**
     * Creates or updates a item
     *
     * @param item The new item, null is not allowed.
     * @param dataModel Null is allowed.
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     * @return A PK to the newly created record.
     */
    @Override
    public ItemPOJOPK putItem(ItemPOJO item, DataModelPOJO dataModel) throws XtentisException {
        String schema = dataModel == null ? null : dataModel.getSchema();
        String dataModelName = dataModel == null ? null : dataModel.getName();
        return BeanDelegatorContainer.getInstance().getItemCtrlDelegator().putItem(item, schema, dataModelName);
    }
    
    /**
     * Updates a item taskId. Is equivalent to {@link #putItem(ItemPOJO, DataModelPOJO)}.
     *
     * @param item The item to update
     * @throws XtentisException In case of error in MDM code.
     * @return A PK to the updated item.
     */
    @Override
    public ItemPOJOPK updateItemMetadata(ItemPOJO item) throws XtentisException {
        return BeanDelegatorContainer.getInstance().getItemCtrlDelegator().putItem(item, null, null);
    }

    /**
     * Get item
     *
     * @param pk The item PK.
     * @return The MDM record for the provided PK.
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public ItemPOJO getItem(ItemPOJOPK pk) throws XtentisException {
        try {
            ItemPOJO pojo = ItemPOJO.load(pk);
            if (pojo == null) {
                String err = "The item '" + pk.getUniqueID() + "' cannot be found.";
                LOGGER.error(err);
                throw new EntityNotFoundException(pk);
            }
            return pojo;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the item " + pk.toString() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Is Item modified by others - no exception is thrown: true|false.
     *
     * @param item A record PK.
     * @param time Time of modification.
     * @return True is last modification of record is after time, false otherwise.
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public boolean isItemModifiedByOther(ItemPOJOPK item, long time) throws XtentisException {
        ItemPOJO pojo = ItemPOJO.load(item);
        return pojo == null || time != pojo.getInsertionTime();
    }

    /**
     * Get an item - no exception is thrown: returns null if not found
     *
     * @param pk MDM record PK
     * @return True if item with PK exists in database.
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public ItemPOJO existsItem(ItemPOJOPK pk) throws XtentisException {
        try {
            return ItemPOJO.load(pk);
        } catch (XtentisException e) {
            return null;
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                String info = "Could not check whether this item exists:  " + pk.toString() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
                LOGGER.debug(info, e);
            }
            return null;
        }
    }

    /**
     * Remove an item - returns null if no item was deleted
     *
     * @param pk       PK of the item to be deleted.
     * @param override Override FK integrity when deleting instance. Please note that this parameter is only taken into
     *                 account if the data model allows override.
     * @return The PK of the deleted item.
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public ItemPOJOPK deleteItem(ItemPOJOPK pk, boolean override) throws XtentisException {
        String dataClusterName = pk.getDataClusterPOJOPK().getUniqueId();
        String conceptName = pk.getConceptName();
        String[] ids = pk.getIds();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Deleting " + dataClusterName + "." + Util.joinStrings(ids, "."));
        }
        BeanDelegatorContainer.getInstance().getItemCtrlDelegator()
                .allowDelete(dataClusterName, conceptName, ComplexTypeMetadata.DeleteType.PHYSICAL);
        if (!pk.getDataClusterPOJOPK().getUniqueId().endsWith(StorageAdmin.STAGING_SUFFIX)) {
            boolean allowDelete = FKIntegrityChecker.getInstance().allowDelete(dataClusterName, conceptName, ids, override);
            if (!allowDelete) {
                throw new RuntimeException("Cannot delete instance '" + pk.getUniqueID() + "' (concept name: " + conceptName + ") due to FK integrity constraints.");
            }
        }
        try {
            return ItemPOJO.remove(pk);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to remove the item " + pk.toString() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Delete items in a stateless mode: open a connection --> perform delete --> close the connection
     *
     * @param dataClusterPOJOPK Data cluster where items will be deleted.
     * @param conceptName Concept name of the soon-to-be-deleted items.
     * @param search A condition for items to be deleted.
     * @param spellThreshold Unused parameter.
     * @param override Override FK integrity when deleting instance. Please note that this parameter is only taken into
     * account if the data model allows override.
     * @return Number of deleted items.
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    // TODO override is not taken into account here?
    @Override
    public int deleteItems(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem search, int spellThreshold, boolean override)
            throws XtentisException {
        BeanDelegatorContainer.getInstance().getItemCtrlDelegator()
                .allowDelete(dataClusterPOJOPK.getUniqueId(), conceptName, ComplexTypeMetadata.DeleteType.PHYSICAL);
        // build the patterns to cluster map - only one cluster at this stage
        XmlServer server = Util.getXmlServerCtrlLocal();
        try {
            return server.deleteItems(dataClusterPOJOPK.getUniqueId(), conceptName, search);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to delete the items: " + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Drop an item - returns null if no item was dropped. This is logical delete (i.e. send to trash)
     *
     * @param itemPOJOPK PK of item to be sent to trash.
     * @param partPath Use this parameter too only drop a part of the document (a XPath evaluated from the document's root).
     * @param override Override FK integrity when deleting instance. Please note that this parameter is only taken into
     * account if the data model allows override.
     * @return A PK to the item in the MDM trash.
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public DroppedItemPOJOPK dropItem(ItemPOJOPK itemPOJOPK, String partPath, boolean override) throws XtentisException {
        String dataClusterName = itemPOJOPK.getDataClusterPOJOPK().getUniqueId();
        String conceptName = itemPOJOPK.getConceptName();
        String[] ids = itemPOJOPK.getIds();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Dropping " + dataClusterName + "." + Util.joinStrings(ids, "."));
        }
        BeanDelegatorContainer.getInstance().getItemCtrlDelegator()
                .allowDelete(dataClusterName, conceptName, ComplexTypeMetadata.DeleteType.LOGICAL);
        boolean allowDelete = FKIntegrityChecker.getInstance().allowDelete(dataClusterName, conceptName, ids, override);
        if (!allowDelete) {
            throw new RuntimeException("Cannot delete instance '" + itemPOJOPK.getUniqueID() + "' (concept name: " + conceptName + ") due to FK integrity constraints.");
        }
        try {
            return ItemPOJO.drop(itemPOJOPK, partPath);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to drop the item " + itemPOJOPK.toString() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Search Items through a view in a cluster and specifying a condition
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param viewPOJOPK The View
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public ArrayList<String> viewSearch(DataClusterPOJOPK dataClusterPOJOPK, ViewPOJOPK viewPOJOPK, IWhereItem whereItem,
            int spellThreshold, int start, int limit) throws XtentisException {
        return viewSearch(dataClusterPOJOPK, viewPOJOPK, whereItem, spellThreshold, null, null, start, limit);
    }

    /**
     * Search ordered Items through a view in a cluster and specifying a condition
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param viewPOJOPK The View
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public ArrayList<String> viewSearch(DataClusterPOJOPK dataClusterPOJOPK, ViewPOJOPK viewPOJOPK, IWhereItem whereItem,
            int spellThreshold, String orderBy, String direction, int start, int limit) throws XtentisException {
        return BeanDelegatorContainer.getInstance().getItemCtrlDelegator()
                .viewSearch(dataClusterPOJOPK, viewPOJOPK, whereItem, orderBy, direction, start, limit);

    }

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
     * @param returnCount True if total search count should be returned as first result.
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public ArrayList<String> xPathsSearch(DataClusterPOJOPK dataClusterPOJOPK, String forceMainPivot,
            ArrayList<String> viewablePaths, IWhereItem whereItem, int spellThreshold, int start, int limit, boolean returnCount)
            throws XtentisException {
        return xPathsSearch(dataClusterPOJOPK, forceMainPivot, viewablePaths, whereItem, spellThreshold, null, null, start, limit, returnCount);
    }

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
     * @param direction One of {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @param returnCount True if total search count should be returned as first result.
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public ArrayList<String> xPathsSearch(DataClusterPOJOPK dataClusterPOJOPK, String forceMainPivot,
            ArrayList<String> viewablePaths, IWhereItem whereItem, int spellThreshold, String orderBy, String direction,
            int start, int limit, boolean returnCount) throws XtentisException {
        try {
            if (viewablePaths.size() == 0) {
                String err = "The list of viewable xPaths must contain at least one element";
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            // Check if user is allowed to read the cluster
            ILocalUser user = LocalUser.getLocalUser();
            boolean authorized = false;
            String dataModelName = dataClusterPOJOPK.getUniqueId();
            if (MDMConfiguration.getAdminUser().equals(user.getUsername())) { 
                authorized = true;
            } else if (user.userCanRead(DataClusterPOJO.class, dataModelName)) {
                authorized = true;
            }
            if (!authorized) {
                throw new XtentisException("Unauthorized read access on data cluster '" + dataModelName + "' by user '"
                        + user.getUsername() + "'");
            }
            Server server = ServerContext.INSTANCE.get();
            String typeName = StringUtils.substringBefore(viewablePaths.get(0), "/"); //$NON-NLS-1$
            StorageAdmin storageAdmin = server.getStorageAdmin();
            Storage storage = storageAdmin.get(dataModelName, storageAdmin.getType(dataModelName));
            MetadataRepository repository = storage.getMetadataRepository();
            ComplexTypeMetadata type = repository.getComplexType(typeName);
            UserQueryBuilder qb = from(type);
            qb.where(UserQueryHelper.buildCondition(qb, whereItem, repository));
            qb.start(start);
            qb.limit(limit);
            if (orderBy != null) {
                List<TypedExpression> fields = UserQueryHelper.getFields(type, StringUtils.substringAfter(orderBy, "/")); //$NON-NLS-1$
                if (fields == null) {
                    throw new IllegalArgumentException("Field '" + orderBy + "' does not exist.");
                }
                OrderBy.Direction queryDirection;
                if ("ascending".equals(direction)) { //$NON-NLS-1$
                    queryDirection = OrderBy.Direction.ASC;
                } else {
                    queryDirection = OrderBy.Direction.DESC;
                }
                for (TypedExpression field : fields) {
                    qb.orderBy(field, queryDirection);
                }
            }
            // Select fields
            for (String viewablePath : viewablePaths) {
                String viewableTypeName = StringUtils.substringBefore(viewablePath, "/"); //$NON-NLS-1$
                String viewableFieldName = StringUtils.substringAfter(viewablePath, "/"); //$NON-NLS-1$
                if (!viewableFieldName.isEmpty()) {
                    qb.select(repository.getComplexType(viewableTypeName), viewableFieldName);
                } else {
                    qb.selectId(repository.getComplexType(viewableTypeName)); // Select id if xPath is 'typeName' and not 'typeName/field'
                }
            }
            ArrayList<String> resultsAsString = new ArrayList<String>();
            StorageResults results;
            try {
                storage.begin();
                if (returnCount) {
                    results = storage.fetch(qb.getSelect());
                    resultsAsString.add("<totalCount>" + results.getCount() + "</totalCount>"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                results = storage.fetch(qb.getSelect());
                DataRecordWriter writer = new DataRecordDefaultWriter();
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                for (DataRecord result : results) {
                    try {
                        writer.write(result, output);
                    } catch (IOException e) {
                        throw new XmlServerException(e);
                    }
                    String document = new String(output.toByteArray());
                    resultsAsString.add(document);
                    output.reset();
                }
                storage.commit();
            } catch (Exception e) {
                storage.rollback();
                throw new XmlServerException(e);
            }
            return resultsAsString;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to single search: " + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Count the items denoted by concept name meeting the optional condition whereItem
     *
     * @param dataClusterPOJOPK A data cluster PK.
     * @param conceptName A concept name.
     * @param whereItem A condition on returned count.
     * @param spellThreshold Unused parameter.
     * @return The number of items found
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public long count(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem whereItem, int spellThreshold)
            throws XtentisException {
        try {
            Server server = ServerContext.INSTANCE.get();
            String dataModelName = dataClusterPOJOPK.getUniqueId();
            StorageAdmin storageAdmin = server.getStorageAdmin();
            Storage storage = storageAdmin.get(dataModelName, storageAdmin.getType(dataModelName));
            MetadataRepository repository = storage.getMetadataRepository();
            Collection<ComplexTypeMetadata> types;
            if ("*".equals(conceptName)) {
                types = repository.getUserComplexTypes();
            } else {
                types = Collections.singletonList(repository.getComplexType(conceptName));
            }
            long count = 0;
            try {
                storage.begin();
                for (ComplexTypeMetadata type : types) {
                    if (!type.getKeyFields().isEmpty()) { // Don't try to count types that don't have any PK.
                        UserQueryBuilder qb = from(type).select(UserQueryBuilder.count());
                        qb.where(UserQueryHelper.buildCondition(qb, whereItem, repository));
                        StorageResults results = storage.fetch(qb.getSelect());
                        try {
                            for (DataRecord result : results) {
                                count += (Long)result.get("count");
                            }
                        } finally {
                            results.close();
                        }
                    }
                }
                storage.commit();
            } catch (Exception e) {
                storage.rollback();
                throw new XtentisException(e);
            }
            return count;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to single search: " + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Search ordered Items through a view in a cluster and specifying a condition
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param viewPOJOPK The View
     * @param searchValue The value searched. If empty, null or equals to "*", this method is equivalent to a view search
     * with no filter.
     * @param matchWholeSentence If <code>false</code>, the searchValue is separated into keywords using " " (white space) as
     * separator. Match will be done with a OR condition on each field. If <code>true</code>, the keyword is considered
     * as a whole sentence and matching is done on the whole sentence (not each word).
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy An optional full path of the item used to order results.
     * @param direction One of {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public ArrayList<String> quickSearch(DataClusterPOJOPK dataClusterPOJOPK, ViewPOJOPK viewPOJOPK, String searchValue,
            boolean matchWholeSentence, int spellThreshold, String orderBy, String direction, int start, int limit)
            throws XtentisException {
        try {
            // check if there actually is a search value
            if ((searchValue == null) || "".equals(searchValue) || "*".equals(searchValue)) { // $NON-NLS-1$ // $NON-NLS-2$
                return viewSearch(dataClusterPOJOPK, viewPOJOPK, null, spellThreshold, orderBy, direction, start, limit);
            } else {
                ViewPOJO view = Util.getViewCtrlLocal().getView(viewPOJOPK);
                ArrayList<String> searchableFields = view.getSearchableBusinessElements().getList();
                Iterator<String> iterator = searchableFields.iterator();
                while (iterator.hasNext()) {
                    String searchableField = iterator.next();
                    // Exclude searchable elements that don't include a '/' since we are generating XPath expressions
                    // (exclude 'Entity' elements but keep 'Entity/Id').
                    if (!searchableField.contains("/")) {
                        iterator.remove();
                    }
                }

                List<String> keywords;
                if (!matchWholeSentence) { // Match on each word.
                    keywords = new ArrayList<String>();
                    String[] allKeywords = searchValue.split("\\p{Space}+");
                    Collections.addAll(keywords, allKeywords);
                } else { // Match on whole sentence
                    keywords = Collections.singletonList(searchValue);
                }
                IWhereItem searchItem;
                if (searchableFields.isEmpty()) {
                    return new ArrayList<String>(0);
                } else {
                    WhereOr whereOr = new WhereOr();
                    for (String fieldName : searchableFields) {
                        WhereOr nestedOr = new WhereOr();
                        for (String keyword : keywords) {
                            WhereCondition nestedCondition = new WhereCondition(fieldName, WhereCondition.CONTAINS, keyword.trim(), WhereCondition.PRE_OR, false);
                            nestedOr.add(nestedCondition);
                        }
                        whereOr.add(nestedOr);
                    }
                    searchItem = whereOr;
                }

                return viewSearch(dataClusterPOJOPK, viewPOJOPK, searchItem, spellThreshold, orderBy, direction, start, limit);
            }
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to quick search  " + searchValue + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get the possible value for the business Element Path, optionally filtered by a condition
     *
     * @param dataClusterPOJOPK The data cluster where to run the query
     * @param businessElementPath The business element path. Must be of the form
     * <code>ConceptName/[optional sub elements]/element</code>
     * @param whereItem The optional condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_DESCENDING}
     * @return The list of values
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public ArrayList<String> getFullPathValues(DataClusterPOJOPK dataClusterPOJOPK,
            String businessElementPath, IWhereItem whereItem, int spellThreshold, String orderBy, String direction)
            throws XtentisException {

        ArrayList<String> res = new ArrayList<String>();
        try {
            // find the conceptName
            String conceptName = ItemPOJO.getConceptFromPath(businessElementPath);
            if (conceptName == null) {
                String err = "Unable to recover the concept from business Element path '" + businessElementPath + "'";
                LOGGER.error(err);
                throw new XtentisException(err);
            }

            ArrayList<String> col = xPathsSearch(dataClusterPOJOPK, null,
                    new ArrayList<String>(Arrays.asList(businessElementPath)), whereItem, spellThreshold,
                    orderBy, direction, 0, -1, false);

            Pattern p = Pattern.compile("<.*>(.*?)</.*>", Pattern.DOTALL);
            for (String li : col) {
                Matcher m = p.matcher(li);
                if (m.matches()) {
                    res.add(StringEscapeUtils.unescapeXml(m.group(1)));
                } else {
                    throw new XtentisException("Result values were not understood for business element: " + conceptName);
                }
            }

            return res;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get values for the Business Element \"" + businessElementPath + "\"";
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Extract results through a view and transform them using a transformer<br/>
     * This call is asynchronous and results will be pushed via the passed {@link com.amalto.core.objects.transformers.util.TransformerCallBack}
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param context The {@link com.amalto.core.objects.transformers.util.TransformerContext} contains the initial context and the transformer name
     * @param globalCallBack The callback function called by the transformer when it completes a step
     * @param viewPOJOPK A filtering view
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public void extractUsingTransformerThroughView(DataClusterPOJOPK dataClusterPOJOPK, TransformerContext context,
            TransformerCallBack globalCallBack, ViewPOJOPK viewPOJOPK, IWhereItem whereItem, int spellThreshold, String orderBy,
            String direction, int start, int limit) throws XtentisException {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("extractUsingTransformerThroughView() ");
            }
            context.put("com.amalto.core.ejb.itemctrl.globalCallBack", globalCallBack); //$NON-NLS-1$
            context.put("com.amalto.core.ejb.itemctrl.count", 0); //$NON-NLS-1$
            // perform search
            ArrayList<String> rows = viewSearch(dataClusterPOJOPK,
                    viewPOJOPK,
                    whereItem,
                    spellThreshold,
                    orderBy,
                    direction,
                    start,
                    limit);
            // transform
            for (String raw : rows) {
                Util.getTransformerV2CtrlLocal().execute(context,
                        new TypedContent(raw.getBytes("utf-8"), "text/xml; charset=\"utf-8\""), //$NON-NLS-1$ //$NON-NLS-2$
                        new TransformerCallBack() {
                            @Override
                            public void contentIsReady(TransformerContext context) throws XtentisException {
                                // add numbered content to the pipeline
                                TypedContent content = context.getFromPipeline(DEFAULT_VARIABLE);
                                int count = (Integer) context.get("com.amalto.core.ejb.itemctrl.count") + 1; //$NON-NLS-1$
                                context.putInPipeline("com.amalto.core.extract." + count, content); //$NON-NLS-1$
                                // context.put(TransformerCtrlBean.CTX_PIPELINE, pipeline);
                                context.put("com.amalto.core.ejb.itemctrl.count", count); //$NON-NLS-1$
                                TransformerCallBack globalCallBack = (TransformerCallBack) context
                                        .get("com.amalto.core.ejb.itemctrl.globalCallBack"); //$NON-NLS-1$
                                globalCallBack.contentIsReady(context);
                            }

                            @Override
                            public void done(TransformerContext context) throws XtentisException {
                                // do not notify
                            }
                        });
            }
            // notify that it is the end
            globalCallBack.done(context);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to extract items using transformer " + context.getTransformerV2POJOPK().getUniqueId()
                    + " through view " + viewPOJOPK.getUniqueId() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Extract results through a view and transform them using a transformer<br/>
     * This call is asynchronous and results will be pushed via the passed {@link com.amalto.core.objects.transformers.util.TransformerCallBack}
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param transformerPOJOPK The transformer to use
     * @param viewPOJOPK A filtering view
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     */
    @Override
    public TransformerContext extractUsingTransformerThroughView(DataClusterPOJOPK dataClusterPOJOPK,
            TransformerV2POJOPK transformerPOJOPK, ViewPOJOPK viewPOJOPK, IWhereItem whereItem, int spellThreshold,
            String orderBy, String direction, int start, int limit) throws XtentisException {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("extractUsingTransformerThroughView() ");
            }
            TransformerContext context = new TransformerContext(transformerPOJOPK);
            ArrayList<TypedContent> content = new ArrayList<TypedContent>();
            context.put("com.amalto.core.itemctrl2.content", content); //$NON-NLS-1$
            context.put("com.amalto.core.itemctrl2.ready", false); //$NON-NLS-1$
            TransformerCallBack globalCallBack = new TransformerCallBack() {
                @Override
                public void contentIsReady(TransformerContext context) throws XtentisException {
                }

                @Override
                public void done(TransformerContext context) throws XtentisException {
                    context.put("com.amalto.core.itemctrl2.ready", true); //$NON-NLS-1$
                }
            };
            extractUsingTransformerThroughView(dataClusterPOJOPK,
                    context,
                    globalCallBack,
                    viewPOJOPK,
                    whereItem,
                    spellThreshold,
                    orderBy,
                    direction,
                    start,
                    limit);
            while (!(Boolean) context.get("com.amalto.core.itemctrl2.ready")) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    LOGGER.error("Error while waiting for transformer's end", e);
                }
            }
            return context;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to extract items using transformer " + transformerPOJOPK.getUniqueId() + " through view "
                    + viewPOJOPK.getUniqueId() + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * @param revisionID The ID of the revision, <code>null</code> to run from the head
     * @param dataClusterPOJOPK The unique ID of the cluster, <code>null</code> to run from the head of the revision ID
     * @param query The query in the native language
     * @param parameters Optional parameter values to replace the %n in the query before execution
     * @return Query results as list of String.
     */
    @Override
    public ArrayList<String> runQuery(DataClusterPOJOPK dataClusterPOJOPK, String query, String[] parameters)
            throws XtentisException {
        XmlServer server = Util.getXmlServerCtrlLocal();
        try {
            return server.runQuery(
                    (dataClusterPOJOPK == null ? null : dataClusterPOJOPK.getUniqueId()), query, parameters);
        } catch (Exception e) {
            String err = "Unable to perform a direct query: " + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    @Override
    public List<String> getItemPKsByCriteria(ItemPKCriteria criteria) throws XtentisException {
        try {
            XmlServer server = Util.getXmlServerCtrlLocal();
            return server.getItemPKsByCriteria(criteria);
        } catch (XtentisException xe) {
            throw xe;
        } catch (Exception e) {
            throw new XtentisException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public List<String> getConceptsInDataCluster(DataClusterPOJOPK dataClusterPOJOPK) throws XtentisException {
        String dataModelName = dataClusterPOJOPK.getUniqueId();
        try {
            List<String> concepts = new ArrayList<String>();
            Server server = ServerContext.INSTANCE.get();
            StorageAdmin storageAdmin = server.getStorageAdmin();
            Storage storage = storageAdmin.get(dataModelName, storageAdmin.getType(dataModelName));
            ILocalUser user = LocalUser.getLocalUser();
            boolean authorized = false;
            if (MDMConfiguration.getAdminUser().equals(user.getUsername())) {
                authorized = true;
            } else if (user.userCanRead(DataClusterPOJO.class, dataModelName)) {
                authorized = true;
            }
            if (!authorized) {
                throw new RemoteException("Unauthorized read access on data cluster " + dataModelName
                        + " by user " + user.getUsername());
            }
            // This should be moved to ItemCtrl
            MetadataRepository repository = storage.getMetadataRepository();
            Collection<ComplexTypeMetadata> types;
            if (DispatchWrapper.isMDMInternal(dataClusterPOJOPK.getUniqueId())) {
                types = SystemStorageWrapper.filter(repository, dataModelName);
            } else {
                types = MetadataUtils.sortTypes(repository, MetadataUtils.SortType.LENIENT);
            }
            for (ComplexTypeMetadata type : types) {
                concepts.add(type.getName());
            }
            return concepts;
        } catch (Exception e) {
            String err = "Unable to search for concept names in the data cluster '" + dataModelName + "'";
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    @Override
    public long countItemsByCustomFKFilters(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, String injectedXpath)
            throws XtentisException {
        try {
            IWhereItem whereItem = new WhereAnd(Arrays.<IWhereItem>asList(new CustomWhereCondition(injectedXpath)));
            return count(dataClusterPOJOPK, conceptName, whereItem, 0);
        } catch (Exception e) {
            String err = "Unable to count the elements! "; //$NON-NLS-1$
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * @param dataClusterPOJOPK A data cluster name
     * @param viewablePaths     Viewable paths in the result
     * @param customXPath       A custom XPath-based condition to be added as-is to the XQuery (no validation)
     * @param whereItem         A addition where condition
     * @param start             A start position for paging results
     * @param limit             Size of results page.
     * @param orderBy           A optional order by
     * @param direction         Direction for the order by.
     * @param returnCount       If true, returns total match count as first result.
     * @return The equivalent of a {@link #xPathsSearch(com.amalto.core.objects.datacluster.DataClusterPOJOPK, String, java.util.ArrayList, com.amalto.xmlserver.interfaces.IWhereItem, int, String, String, int, int, boolean)} using a
     *         custom XPath as additional condition.
     * @throws com.amalto.core.util.XtentisException In case of MDM server error.
     */
    @Override
    public ArrayList<String> getItemsByCustomFKFilters(DataClusterPOJOPK dataClusterPOJOPK, ArrayList<String> viewablePaths,
                                                       String customXPath, IWhereItem whereItem, int start, int limit,
                                                       String orderBy, String direction, boolean returnCount)
            throws XtentisException {
        IWhereItem customWhereCondition = new CustomWhereCondition(customXPath);
        IWhereItem xPathSearchCondition;
        if (whereItem != null) {
            xPathSearchCondition = new WhereAnd(Arrays.asList(whereItem, customWhereCondition));
        } else {
            xPathSearchCondition = customWhereCondition;
        }
        return xPathsSearch(dataClusterPOJOPK, null, viewablePaths, xPathSearchCondition, 0, orderBy, direction, start, limit, returnCount);
    }

    public ArrayList<String> getItems(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem whereItem,
            int spellThreshold, int start, int limit) throws XtentisException {
        return getItems(dataClusterPOJOPK, conceptName, whereItem, spellThreshold, start, limit, false);
    }

    public ArrayList<String> getItems(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem whereItem,
            int spellThreshold, String orderBy, String direction, int start, int limit) throws XtentisException {
        return getItems(dataClusterPOJOPK, conceptName, whereItem, spellThreshold, orderBy, direction, start, limit, false);
    }

    /**
     * Get unordered items of a Concept using an optional where condition
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param conceptName The name of the concept
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @param totalCountOnFirstRow If true, return total search count as first result.
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public ArrayList<String> getItems(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem whereItem,
            int spellThreshold, int start, int limit, boolean totalCountOnFirstRow) throws XtentisException {
        return getItems(dataClusterPOJOPK, conceptName, whereItem, spellThreshold, null, null, start, limit, totalCountOnFirstRow);
    }

    /**
     * Get potentially ordered items of a Concept using an optional where condition
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param conceptName The name of the concept
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link com.amalto.xmlserver.interfaces.IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @param totalCountOnFirstRow If true, return total search count as first result.
     * @return The ordered list of results
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     */
    @Override
    public ArrayList<String> getItems(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem whereItem,
            int spellThreshold, String orderBy, String direction, int start, int limit, boolean totalCountOnFirstRow)
            throws XtentisException {
    	return BeanDelegatorContainer.getInstance().getItemCtrlDelegator().getItems(dataClusterPOJOPK, conceptName, whereItem, spellThreshold, orderBy, direction, start, limit, totalCountOnFirstRow);
    }

    @Override
    public FKIntegrityCheckResult checkFKIntegrity(String dataCluster, String concept, String[] ids) throws XtentisException {
        return FKIntegrityChecker.getInstance().getFKIntegrityPolicy(dataCluster, concept, ids);
    }
}
