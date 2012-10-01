package com.amalto.core.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.amalto.core.metadata.ComplexTypeMetadata;
import com.amalto.core.metadata.FieldMetadata;
import com.amalto.core.metadata.MetadataRepository;
import com.amalto.core.query.user.*;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordWriter;
import com.amalto.xmlserver.interfaces.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.CommonUtil;
import org.talend.mdm.commmon.util.core.EDBType;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.ejb.local.XmlServerSLWrapperLocal;
import com.amalto.core.integrity.FKIntegrityCheckResult;
import com.amalto.core.integrity.FKIntegrityChecker;
import com.amalto.core.objects.datacluster.ejb.DataClusterPOJO;
import com.amalto.core.objects.datacluster.ejb.DataClusterPOJOPK;
import com.amalto.core.objects.datamodel.ejb.DataModelPOJO;
import com.amalto.core.objects.transformers.v2.ejb.TransformerV2POJOPK;
import com.amalto.core.objects.transformers.v2.util.TransformerCallBack;
import com.amalto.core.objects.transformers.v2.util.TransformerContext;
import com.amalto.core.objects.transformers.v2.util.TypedContent;
import com.amalto.core.objects.universe.ejb.UniversePOJO;
import com.amalto.core.objects.view.ejb.ViewPOJO;
import com.amalto.core.objects.view.ejb.ViewPOJOPK;
import com.amalto.core.util.EntityNotFoundException;
import com.amalto.core.util.JazzyConfiguration;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

import java.io.*;
import java.util.*;

import static com.amalto.core.query.user.UserQueryBuilder.alias;
import static com.amalto.core.query.user.UserQueryBuilder.count;
import static com.amalto.core.query.user.UserQueryBuilder.from;

/**
 * @author Bruno Grieder
 *
 * @ejb.bean name="ItemCtrl2" display-name="Name for ItemCtrl2" description="Description for ItemCtrl2"
 * jndi-name="amalto/remote/core/itemctrl2" local-jndi-name = "amalto/local/core/itemctrl2" type="Stateless"
 * view-type="both"
 *
 * @ejb.remote-facade
 *
 * @ejb.permission view-type = "remote" role-name = "administration"
 * @ejb.permission view-type = "local" unchecked = "true"
 *
 */

@SuppressWarnings("deprecation")
public class ItemCtrl2Bean implements SessionBean {

    public static final String DEFAULT_VARIABLE = "_DEFAULT_";

    public static final long serialVersionUID = 200;

    private static final Logger LOGGER = Logger.getLogger(ItemCtrl2Bean.class);

    private static final JazzyConfiguration DEFAULT_JAZZY_CONFIGURATION = new JazzyConfiguration();

    /**
     * ItemCtrlBean.java Constructor
     *
     */
    public ItemCtrl2Bean() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() throws EJBException, RemoteException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() throws EJBException, RemoteException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() throws EJBException, RemoteException {
    }

    /**
     * Create method
     *
     * @ejb.create-method view-type = "local"
     * @throws javax.ejb.CreateException Thrown by J2EE container.
     */
    public void ejbCreate() throws javax.ejb.CreateException {
    }

    /**
     * Post Create method
     * @throws javax.ejb.CreateException Thrown by J2EE container.
     */
    public void ejbPostCreate() throws javax.ejb.CreateException {
    }

    /**
     * Creates or updates a item
     *
     * @param item The new item, null is not allowed.
     * @param dataModel Null is allowed.
     * @throws XtentisException In case of error in MDM code.
     * @return A PK to the newly created record.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public ItemPOJOPK putItem(ItemPOJO item, DataModelPOJO dataModel) throws XtentisException {
        return putItem(item, (dataModel == null ? null : dataModel.getSchema()), (dataModel == null ? null : dataModel.getName()));
    }

    protected ItemPOJOPK putItem(ItemPOJO item, String schema, String dataModelName) throws XtentisException {
        return BeanDelegatorContainer.getUniqueInstance().getItemCtrlDelegator().putItem(item, schema, dataModelName);
    }

    /**
     * updates a item taskId. Is equivalent to {@link #putItem(ItemPOJO, String, String)}.
     *
     * @param item The item to update
     * @throws XtentisException In case of error in MDM code.
     * @return A PK to the updated item.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public ItemPOJOPK updateItemMetadata(ItemPOJO item) throws XtentisException {
        return BeanDelegatorContainer.getUniqueInstance().getItemCtrlDelegator().putItem(item, null, null);
    }

    /**
     * Get item
     *
     * @param pk The item PK.
     * @return The MDM record for the provided PK.
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public ItemPOJO getItem(ItemPOJOPK pk) throws XtentisException {

        try {
            ItemPOJO pojo = ItemPOJO.load(pk);
            if (pojo == null) {
                String err = "The item '" + pk.getUniqueID() + "' cannot be found.";
                LOGGER.error(err);
                throw new EntityNotFoundException(err);
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
     * Get item with revisionID
     *
     * @param revisionID The item revision
     * @param pk The item PK
     * @throws XtentisException In case of error in MDM code.
     * @return The MDM record for the PK.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public ItemPOJO getItem(String revisionID, ItemPOJOPK pk) throws XtentisException {
        try {
            ItemPOJO pojo = ItemPOJO.load(revisionID, pk);
            if (pojo == null) {
                String err = "The item '" + pk.getUniqueID() + "' cannot be found.";
                LOGGER.error(err);
                throw new XtentisException(err);
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
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public boolean isItemModifiedByOther(ItemPOJOPK item, long time) throws XtentisException {
        ItemPOJO pojo = ItemPOJO.adminLoad(item);
        return pojo == null || time != pojo.getInsertionTime();
    }

    /**
     * Get an item - no exception is thrown: returns null if not found
     *
     * @param pk MDM record PK
     * @return True if item with PK exists in database.
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
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
     * @param pk PK of the item to be deleted.
     * @param override Override FK integrity when deleting instance. Please note that this parameter is only taken into
     * account if the data model allows override.
     * @return The PK of the deleted item.
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public ItemPOJOPK deleteItem(ItemPOJOPK pk,boolean override ) throws XtentisException {
        String dataClusterName = pk.getDataClusterPOJOPK().getUniqueId();
        String conceptName = pk.getConceptName();
        String[] ids = pk.getIds();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Deleting " + dataClusterName + "." + Util.joinStrings(ids, "."));
        }
        boolean allowDelete = FKIntegrityChecker.getInstance().allowDelete(dataClusterName, conceptName, ids, override);
        if (!allowDelete) {
            throw new RuntimeException("Cannot delete instance '" + pk.getUniqueID() + "' (concept name: " + conceptName + ") due to FK integrity constraints.");
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
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    // TODO override is not taken into account here?
    public int deleteItems(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem search, int spellThreshold, boolean override)
            throws XtentisException {

        // get the universe and revision ID
        UniversePOJO universe = LocalUser.getLocalUser().getUniverse();
        if (universe == null) {
            String err = "ERROR: no Universe set for user '" + LocalUser.getLocalUser().getUsername() + "'";
            LOGGER.error(err);
            throw new XtentisException(err);
        }

        // build the patterns to revision ID map
        LinkedHashMap<String, String> patternsToRevisionID = new LinkedHashMap<String, String>(universe.getItemsRevisionIDs());
        if (universe.getDefaultItemRevisionID() != null)
            patternsToRevisionID.put(".*", universe.getDefaultItemRevisionID());

        // build the patterns to cluster map - only one cluster at this stage
        LinkedHashMap<String, String> patternsToClusterName = new LinkedHashMap<String, String>();
        patternsToClusterName.put(".*", dataClusterPOJOPK.getUniqueId());

        XmlServerSLWrapperLocal server = Util.getXmlServerCtrlLocal();
        try {
            return server.deleteItems(patternsToRevisionID, patternsToClusterName, conceptName, search);
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
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public DroppedItemPOJOPK dropItem(ItemPOJOPK itemPOJOPK, String partPath, boolean override) throws XtentisException {
        String dataClusterName = itemPOJOPK.getDataClusterPOJOPK().getUniqueId();
        String conceptName = itemPOJOPK.getConceptName();
        String[] ids = itemPOJOPK.getIds();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Dropping " + dataClusterName + "." + Util.joinStrings(ids, "."));
        }
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
     * Search Items thru a view in a cluster and specifying a condition
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param viewPOJOPK The View
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @return The ordered list of results
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public ArrayList<String> viewSearch(DataClusterPOJOPK dataClusterPOJOPK, ViewPOJOPK viewPOJOPK, IWhereItem whereItem,
            int spellThreshold, int start, int limit) throws XtentisException {
        return viewSearch(dataClusterPOJOPK, viewPOJOPK, whereItem, spellThreshold, null, null, start, limit);
    }

    /**
     * Search ordered Items thru a view in a cluster and specifying a condition
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param viewPOJOPK The View
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of {@link IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @return The ordered list of results
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public ArrayList<String> viewSearch(DataClusterPOJOPK dataClusterPOJOPK, ViewPOJOPK viewPOJOPK, IWhereItem whereItem,
            int spellThreshold, String orderBy, String direction, int start, int limit) throws XtentisException {
        return BeanDelegatorContainer.getUniqueInstance().getItemCtrlDelegator()
                .viewSearch(dataClusterPOJOPK, viewPOJOPK, whereItem, spellThreshold, orderBy, direction, start, limit);

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
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
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
     * @param direction One of {@link IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @param returnCount True if total search count should be returned as first result.
     * @return The ordered list of results
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
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
            if ("admin".equals(user.getUsername()) || LocalUser.UNAUTHENTICATED_USER.equals(user.getUsername())) { //$NON-NLS-1$
                authorized = true;
            } else if (user.userCanRead(DataClusterPOJO.class, dataClusterPOJOPK.getUniqueId())) {
                authorized = true;
            }
            if (!authorized) {
                throw new XtentisException("Unauthorized read access on data cluster '" + dataClusterPOJOPK.getUniqueId() + "' by user '"
                        + user.getUsername() + "'");
            }

            // get the universe and revision ID
            UniversePOJO universe = LocalUser.getLocalUser().getUniverse();
            if (universe == null) {
                String err = "ERROR: no Universe set for user '" + LocalUser.getLocalUser().getUsername() + "'";
                LOGGER.error(err);
                throw new XtentisException(err);
            }

            Server server = ServerContext.INSTANCE.get();
            Storage storage = server.getStorageAdmin().get(dataClusterPOJOPK.getUniqueId());

            if (storage == null) {
                // build the patterns to revision ID map
                LinkedHashMap<String, String> conceptPatternsToRevisionID = new LinkedHashMap<String, String>(
                        universe.getItemsRevisionIDs());
                if (universe.getDefaultItemRevisionID() != null) {
                    conceptPatternsToRevisionID.put(".*", universe.getDefaultItemRevisionID());
                }

                // build the patterns to cluster map - only one cluster at this stage
                LinkedHashMap<String, String> conceptPatternsToClusterName = new LinkedHashMap<String, String>();
                conceptPatternsToClusterName.put(".*", dataClusterPOJOPK.getUniqueId());

                XmlServerSLWrapperLocal xmlServer = Util.getXmlServerCtrlLocal();

                String query = xmlServer.getItemsQuery(conceptPatternsToRevisionID, conceptPatternsToClusterName, forceMainPivot,
                        viewablePaths, whereItem, orderBy, direction, start, limit, spellThreshold, returnCount, Collections.emptyMap());

                return xmlServer.runQuery(null, null, query, null);
            } else {
                MetadataRepository repository = server.getMetadataRepositoryAdmin().get(dataClusterPOJOPK.getUniqueId());
                String typeName = StringUtils.substringBefore(viewablePaths.get(0), "/"); //$NON-NLS-1$
                ComplexTypeMetadata type = repository.getComplexType(typeName);
                UserQueryBuilder qb = from(type);
                qb.where(UserQueryHelper.buildCondition(qb, whereItem, repository));
                qb.start(start);
                qb.limit(limit);
                if (orderBy != null) {
                    TypedExpression field = UserQueryHelper.getField(repository, typeName, StringUtils.substringAfter(orderBy, "/")); //$NON-NLS-1$
                    if (field == null) {
                        throw new IllegalArgumentException("Field '" + orderBy + "' does not exist.");
                    }
                    OrderBy.Direction queryDirection;
                    if ("ascending".equals(direction)) { //$NON-NLS-1$
                        queryDirection = OrderBy.Direction.ASC;
                    } else {
                        queryDirection = OrderBy.Direction.DESC;
                    }
                    qb.orderBy(field, queryDirection);
                }

                // Select fields
                for (String viewablePath : viewablePaths) {
                    String viewableTypeName = StringUtils.substringBefore(viewablePath, "/"); //$NON-NLS-1$
                    String viewableFieldName = StringUtils.substringAfter(viewablePath, "/"); //$NON-NLS-1$
                    if (!viewableFieldName.isEmpty()) {
                        qb.select(repository.getComplexType(viewableTypeName), viewableFieldName);
                    } else {
                        qb.selectId(repository.getComplexType(viewableTypeName)); // Select id if xPath is 'typeName' and and 'typeName/field'
                    }
                }

                StorageResults results;
                ArrayList<String> resultsAsString = new ArrayList<String>();
                if (returnCount) {
                    results = storage.fetch(qb.getSelect());
                    resultsAsString.add("<totalCount>" + results.getCount() + "</totalCount>"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                results = storage.fetch(qb.getSelect());
                DataRecordWriter writer = new DataRecordWriter() {
                    public void write(DataRecord record, OutputStream output) throws IOException {
                        Writer out = new BufferedWriter(new OutputStreamWriter(output, "UTF-8")); //$NON-NLS-1$
                        write(record, out);
                    }

                    public void write(DataRecord record, Writer writer) throws IOException {
                        writer.write("<result>\n"); //$NON-NLS-1$
                        for (FieldMetadata fieldMetadata : record.getSetFields()) {
                            Object value = record.get(fieldMetadata);
                            if (value != null) {
                                writer.append("\t<").append(fieldMetadata.getName()).append(">");
                                writer.append(StringEscapeUtils.escapeXml(String.valueOf(value)));
                                writer.append("</").append(fieldMetadata.getName()).append(">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            }
                        }
                        writer.append("</result>"); //$NON-NLS-1$
                        writer.flush();
                    }
                };

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
                return resultsAsString;
            }
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to single search: " + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get items hierarchical tree according to pivots
     *
     * @param clusterName The Data Cluster where to run the query
     * @param mainPivotName The main Business Concept name
     * @param pivotWithKeys The pivots with their IDs which selected to be the catalog of the hierarchical tree
     * @param indexPaths The title as the content of each leaf node of the hierarchical tree
     * @param whereItem The condition
     * @param pivotDirections One of {@link IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param indexDirections One of {@link IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @return The ordered list of results
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public ArrayList<String> getItemsPivotIndex(String clusterName, String mainPivotName,
            LinkedHashMap<String, String[]> pivotWithKeys, String[] indexPaths, IWhereItem whereItem, String[] pivotDirections,
            String[] indexDirections, int start, int limit) throws XtentisException {
        return BeanDelegatorContainer
                .getUniqueInstance()
                .getItemCtrlDelegator()
                .getItemsPivotIndex(clusterName, mainPivotName, pivotWithKeys, indexPaths, whereItem, pivotDirections,
                        indexDirections, start, limit);
    }

    /**
     * @param clusterName A data cluster name.
     * @param conceptName A concept name
     * @param PKXpaths
     * @param FKXpath
     * @param labelXpath
     * @param fatherPK
     * @param whereItem
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     *
     * @return
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public ArrayList<String> getChildrenItems(String clusterName, String conceptName, String PKXpaths[], String FKXpath,
            String labelXpath, String fatherPK, IWhereItem whereItem, int start, int limit) throws XtentisException {
        return BeanDelegatorContainer.getUniqueInstance().getItemCtrlDelegator()
                .getChildrenItems(clusterName, conceptName, PKXpaths, FKXpath, labelXpath, fatherPK, whereItem, start, limit);
    }

    /**
     * Count the items denoted by concept name meeting the optional condition whereItem
     *
     * @param dataClusterPOJOPK A data cluster PK.
     * @param conceptName A concept name.
     * @param whereItem A condition on returned count.
     * @param spellThreshold Unused parameter.
     * @return The number of items found
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public long count(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem whereItem, int spellThreshold)
            throws XtentisException {
        try {
            Server mdmServer = ServerContext.INSTANCE.get();
            Storage storage = mdmServer.getStorageAdmin().get(dataClusterPOJOPK.getUniqueId());

            if (storage == null) {
                // get the universe and revision ID
                UniversePOJO universe = LocalUser.getLocalUser().getUniverse();
                if (universe == null) {
                    String err = "ERROR: no Universe set for user '" + LocalUser.getLocalUser().getUsername() + "'";
                    LOGGER.error(err);
                    throw new XtentisException(err);
                }

                // build the patterns to revision ID map
                LinkedHashMap<String, String> conceptPatternsToRevisionID = new LinkedHashMap<String, String>(
                        universe.getItemsRevisionIDs());
                if (universe.getDefaultItemRevisionID() != null)
                    conceptPatternsToRevisionID.put(".*", universe.getDefaultItemRevisionID());

                // build the patterns to cluster map - only one cluster at this stage
                LinkedHashMap<String, String> conceptPatternsToClusterName = new LinkedHashMap<String, String>();
                conceptPatternsToClusterName.put(".*", dataClusterPOJOPK.getUniqueId());

                XmlServerSLWrapperLocal server = Util.getXmlServerCtrlLocal();
                return server.countItems(conceptPatternsToRevisionID, conceptPatternsToClusterName, conceptName, whereItem);
            } else {
                MetadataRepository repository = mdmServer.getMetadataRepositoryAdmin().get(dataClusterPOJOPK.getUniqueId());

                Collection<ComplexTypeMetadata> types;
                if ("*".equals(conceptName)) {
                    types = repository.getUserComplexTypes();
                } else {
                    types = Collections.singletonList(repository.getComplexType(conceptName));
                }

                long count = 0;
                for (ComplexTypeMetadata type : types) {
                    if (!type.getKeyFields().isEmpty()) { // Don't try to count types that don't have any PK.
                        UserQueryBuilder qb = from(type)
                                .select(alias(UserQueryBuilder.count(), "count"));

                        StorageResults countResult = storage.fetch(qb.getSelect());
                        Iterator<DataRecord> resultsIterator = countResult.iterator();
                        if (resultsIterator.hasNext()) {
                            Object countObjectValue = resultsIterator.next().get("count");
                            count += Long.parseLong(String.valueOf(countObjectValue));
                        } else {
                            throw new IllegalStateException("Count returned no result for type '" + type.getName() + "'.");
                        }
                    }
                }
                return count;
            }
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to single search: " + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Search ordered Items thru a view in a cluster and specifying a condition
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
     * @param direction One of {@link IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @return The ordered list of results
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public ArrayList<String> quickSearch(DataClusterPOJOPK dataClusterPOJOPK, ViewPOJOPK viewPOJOPK, String searchValue,
            boolean matchWholeSentence, int spellThreshold, String orderBy, String direction, int start, int limit)
            throws XtentisException {
        try {
            // check if there actually is a search value
            if ((searchValue == null) || "".equals(searchValue) || "*".equals(searchValue)) { // $NON-NLS-1$ // $NON-NLS-2$
                return viewSearch(dataClusterPOJOPK, viewPOJOPK, null, spellThreshold, orderBy, direction, start, limit);
            } else {
                boolean isSpellCheck = (spellThreshold >= DEFAULT_JAZZY_CONFIGURATION.getMinTreshold());
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
                            WhereCondition nestedCondition = new WhereCondition(fieldName, WhereCondition.CONTAINS, keyword.trim(), WhereCondition.PRE_OR, isSpellCheck);
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
     * @param direction One of {@link IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link IXmlServerSLWrapper#ORDER_DESCENDING}
     * @return The list of values
     * @throws XtentisException In case of error in MDM code.
     *
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
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
                if (m.matches())
                    res.add(StringEscapeUtils.unescapeXml(m.group(1)));
                else
                    throw new XtentisException("Result values were not understood for business element: " + conceptName);
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
     * Extract results thru a view and transform them using a transformer<br/>
     * This call is asynchronous and results will be pushed via the passed {@link TransformerCallBack}
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param context The {@link TransformerContext} contains the initial context and the transformer name
     * @param globalCallBack The callback function called by the transformer when it completes a step
     * @param viewPOJOPK A filtering view
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of {@link IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @throws com.amalto.core.util.XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public void extractUsingTransformerThroughView(DataClusterPOJOPK dataClusterPOJOPK, TransformerContext context,
            TransformerCallBack globalCallBack, ViewPOJOPK viewPOJOPK, IWhereItem whereItem, int spellThreshold, String orderBy,
            String direction, int start, int limit) throws XtentisException {

        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("extractUsingTransformerThroughView() ");
            }

            context.put("com.amalto.core.ejb.itemctrl.globalCallBack", globalCallBack);
            context.put("com.amalto.core.ejb.itemctrl.count", 0);

            // perform search
            ArrayList<String> raws = viewSearch(dataClusterPOJOPK, viewPOJOPK, whereItem, spellThreshold, orderBy, direction,
                    start, limit);

            // transform
            for (String raw : raws) {
                Util.getTransformerV2CtrlLocal().execute(context,
                        new TypedContent(raw.getBytes("utf-8"), "text/xml; charset=\"utf-8\""), new TransformerCallBack() {

                    public void contentIsReady(TransformerContext context) throws XtentisException {
                        // add numbered content to the pipeline
                        TypedContent content = context.getFromPipeline(DEFAULT_VARIABLE);
                        int count = (Integer) context.get("com.amalto.core.ejb.itemctrl.count") + 1;
                        context.putInPipeline("com.amalto.core.extract." + count, content);
                        // context.put(TransformerCtrlBean.CTX_PIPELINE, pipeline);
                        context.put("com.amalto.core.ejb.itemctrl.count", count);
                        TransformerCallBack globalCallBack = (TransformerCallBack) context
                                .get("com.amalto.core.ejb.itemctrl.globalCallBack");
                        globalCallBack.contentIsReady(context);
                    }

                    public void done(TransformerContext context) throws XtentisException {
                        // do not notify
                    }
                });// execute
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
     * Extract results thru a view and transform them using a transformer<br/>
     * This call is asynchronous and results will be pushed via the passed {@link TransformerCallBack}
     *
     * @param dataClusterPOJOPK The Data Cluster where to run the query
     * @param transformerPOJOPK The transformer to use
     * @param viewPOJOPK A filtering view
     * @param whereItem The condition
     * @param spellThreshold The condition spell checking threshold. A negative value de-activates spell
     * @param orderBy The full path of the item user to order
     * @param direction One of {@link IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public TransformerContext extractUsingTransformerThroughView(DataClusterPOJOPK dataClusterPOJOPK,
            TransformerV2POJOPK transformerPOJOPK, ViewPOJOPK viewPOJOPK, IWhereItem whereItem, int spellThreshold,
            String orderBy, String direction, int start, int limit) throws XtentisException {

        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("extractUsingTransformerThroughView() ");
            }

            TransformerContext context = new TransformerContext(transformerPOJOPK);
            ArrayList<TypedContent> content = new ArrayList<TypedContent>();
            context.put("com.amalto.core.itemctrl2.content", content);
            context.put("com.amalto.core.itemctrl2.ready", new Boolean(false));
            TransformerCallBack globalCallBack = new TransformerCallBack() {

                public void contentIsReady(TransformerContext context) throws XtentisException {
                }

                public void done(TransformerContext context) throws XtentisException {
                    context.put("com.amalto.core.itemctrl2.ready", new Boolean(true));
                }
            };

            extractUsingTransformerThroughView(dataClusterPOJOPK, context, globalCallBack, viewPOJOPK, whereItem, spellThreshold,
                    orderBy, direction, start, limit);

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

    /*****************************************************************
     * D I R E C T Q U E R Y
     *****************************************************************/

    /**
     * @param revisionID The ID of the revision, <code>null</code> to run from the head
     * @param dataClusterPOJOPK The unique ID of the cluster, <code>null</code> to run from the head of the revision ID
     * @param query The query in the native language
     * @param parameters Optional parameter values to replace the %n in the query before execution
     * @return Query results as list of String.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public ArrayList<String> runQuery(String revisionID, DataClusterPOJOPK dataClusterPOJOPK, String query, String[] parameters)
            throws XtentisException {

        XmlServerSLWrapperLocal server = Util.getXmlServerCtrlLocal();
        try {
            return server.runQuery(revisionID,
                    (dataClusterPOJOPK == null ? null : dataClusterPOJOPK.getUniqueId()), query, parameters);
        } catch (Exception e) {
            String err = "Unable to perform a direct query: " + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }


    public List<String> getItemPKsByCriteria(ItemPKCriteria criteria) throws XtentisException {
        try {
            XmlServerSLWrapperLocal server = Util.getXmlServerCtrlLocal();
            return server.getItemPKsByCriteria(criteria);
        } catch (XtentisException xe) {
            throw xe;
        } catch (Exception e) {
            throw new XtentisException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Returns a map with keys being the concepts found in the Data Cluster and as value the revisionID
     *
     * @param dataClusterPOJOPK A data cluster PK.
     * @return A {@link TreeMap} of concept names to revision IDs
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public TreeMap<String, String> getConceptsInDataCluster(DataClusterPOJOPK dataClusterPOJOPK) throws XtentisException {
        return getConceptsInDataCluster(dataClusterPOJOPK, null);
    }

    /**
     * Returns a map with keys being the concepts found in the Data Cluster and as value the revisionID
     *
     * @param dataClusterPOJOPK A data cluster PK.
     * @param universe Universe
     * @return A {@link TreeMap} of concept names to revision IDs
     * @throws XtentisException
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public TreeMap<String, String> getConceptsInDataCluster(DataClusterPOJOPK dataClusterPOJOPK, UniversePOJO universe)
            throws XtentisException {
        try {
            TreeMap<String, String> concepts = new TreeMap<String, String>();

            Server mdmServer = ServerContext.INSTANCE.get();
            Storage storage = mdmServer.getStorageAdmin().get(dataClusterPOJOPK.getUniqueId());

            if (storage == null) {
                ILocalUser user = LocalUser.getLocalUser();
                boolean authorized = false;
                if ("admin".equals(user.getUsername()) || LocalUser.UNAUTHENTICATED_USER.equals(user.getUsername())) {
                    authorized = true;
                } else if (user.userCanRead(DataClusterPOJO.class, dataClusterPOJOPK.getUniqueId())) {
                    authorized = true;
                }
                if (!authorized) {
                    throw new RemoteException("Unauthorized read access on data cluster " + dataClusterPOJOPK.getUniqueId()
                            + " by user " + user.getUsername());
                }

                // This should be moved to ItemCtrl
                // get the universe
                if (universe == null)
                    universe = user.getUniverse();

                // make sure we do not check a revision twice
                Set<String> revisionsChecked = new HashSet<String>();

                // First go through every revision
                String query;
                Set<String> patterns = universe.getItemsRevisionIDs().keySet();
                for (String pattern : patterns) {
                    String revisionID = universe.getConceptRevisionID(pattern);
                    String revisionKey = (revisionID == null) || "".equals(revisionID) ? "__$DEFAULT$__" : revisionID;
                    if (revisionsChecked.contains(revisionKey)) {
                        continue;
                    } else {
                        revisionsChecked.add(revisionKey);
                    }
                    // fetch all the concepts
                    String collectionpath = CommonUtil.getPath(revisionID, dataClusterPOJOPK.getUniqueId());
                    query = "distinct-values(collection(\"" + collectionpath + "\")/ii/n/text())";
                    if (EDBType.ORACLE.getName().equals(MDMConfiguration.getDBType().getName())) {
                        query = "for $pivot0 in collection(\"" + collectionpath + "\")/ii/n/text()return <result>{$pivot0}</result>";
                    }
                    ArrayList<String> conceptsFound = runQuery(revisionID, dataClusterPOJOPK, query, null);
                    // validate the concepts found
                    for (String concept : conceptsFound) {
                        if (concept.matches(pattern) && (concepts.get(concept) == null)) {
                            concepts.put(concept, revisionID == null ? "" : revisionID);
                        }
                    }
                }

                // Then validate the concepts found in the default revision
                String revisionID = universe.getDefaultItemRevisionID();
                String revisionKey = (revisionID == null) || "".equals(revisionID) ? "__$DEFAULT$__" : revisionID;

                if (!revisionsChecked.contains(revisionKey)) {
                    // fetch all the concepts
                    String collectionpath = CommonUtil.getPath(revisionID, dataClusterPOJOPK.getUniqueId());
                    query = "distinct-values(collection(\"" + collectionpath + "\")/ii/n/text())";
                    if (EDBType.ORACLE.getName().equals(MDMConfiguration.getDBType().getName())) {
                        query = "for $pivot0 in collection(\"" + collectionpath + "\")/ii/n/text()return <result>{$pivot0}</result>";
                    }

                    ArrayList<String> conceptsFound = runQuery(revisionID, dataClusterPOJOPK, query, null);
                    // validate the concepts found
                    for (String concept : conceptsFound) {
                        if (concepts.get(concept) == null) {
                            concepts.put(concept, revisionID == null ? "" : revisionID);
                        }
                    }
                }
            } else {
                MetadataRepository repository = mdmServer.getMetadataRepositoryAdmin().get(dataClusterPOJOPK.getUniqueId());
                Collection<ComplexTypeMetadata> types = repository.getUserComplexTypes();
                for (ComplexTypeMetadata type : types) {
                    concepts.put(type.getName(), "");
                }
            }

            return concepts;
        } catch (Exception e) {
            String err = "Unable to search for concept names in the data cluster '" + dataClusterPOJOPK.getUniqueId() + "'";
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * @param dataClusterPOJOPK
     * @param conceptName
     * @param injectedXpath
     * @return
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
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
     * @return The equivalent of a {@link #xPathsSearch(com.amalto.core.objects.datacluster.ejb.DataClusterPOJOPK, String, java.util.ArrayList, com.amalto.xmlserver.interfaces.IWhereItem, int, String, String, int, int, boolean)} using a
     *         custom XPath as additional condition.
     * @throws XtentisException In case of MDM server error.
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
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
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     *
     */
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
     * @param direction One of {@link IXmlServerSLWrapper#ORDER_ASCENDING} or
     * {@link IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @param totalCountOnFirstRow If true, return total search count as first result.
     * @return The ordered list of results
     * @throws XtentisException In case of error in MDM code.
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     *
     */
    public ArrayList<String> getItems(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem whereItem,
            int spellThreshold, String orderBy, String direction, int start, int limit, boolean totalCountOnFirstRow)
            throws XtentisException {

    	return BeanDelegatorContainer.getUniqueInstance().getItemCtrlDelegator().getItems(dataClusterPOJOPK, conceptName, whereItem, spellThreshold, orderBy, direction, start, limit, totalCountOnFirstRow);
    }

    public FKIntegrityCheckResult checkFKIntegrity(String dataCluster, String concept, String[] ids) throws XtentisException {
        return FKIntegrityChecker.getInstance().getFKIntegrityPolicy(dataCluster, concept, ids);
    }

}
