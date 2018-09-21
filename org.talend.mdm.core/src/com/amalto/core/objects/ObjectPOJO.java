/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.ICoreConstants;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;

import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.objects.backgroundjob.BackgroundJobPOJO;
import com.amalto.core.objects.configurationinfo.ConfigurationInfoPOJO;
import com.amalto.core.objects.customform.CustomFormPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.objects.marshalling.MarshallingFactory;
import com.amalto.core.objects.menu.MenuPOJO;
import com.amalto.core.objects.role.RolePOJO;
import com.amalto.core.objects.routing.CompletedRoutingOrderV2POJO;
import com.amalto.core.objects.routing.FailedRoutingOrderV2POJO;
import com.amalto.core.objects.routing.RoutingEngineV2POJO;
import com.amalto.core.objects.routing.RoutingRulePOJO;
import com.amalto.core.objects.storedprocedure.StoredProcedurePOJO;
import com.amalto.core.objects.transformers.TransformerPluginV2POJO;
import com.amalto.core.objects.transformers.TransformerV2POJO;
import com.amalto.core.objects.view.ViewPOJO;
import com.amalto.core.server.api.Item;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.BAMLogger;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.MDMEhCacheUtil;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.IXmlServerSLWrapper;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;

public abstract class ObjectPOJO implements Serializable {

    // Don't change this id, it forces compatibility with pre-5.3 versions (even if structure slightly changed).
    public static final long serialVersionUID = 3157316606545297572l;

    private static final Map<Class<?>, String> OBJECTS_CLASSES_TO_NAMES_MAP = new HashMap<Class<?>, String>();

    private static final Map<String, Class<? extends ObjectPOJO>> OBJECTS_NAMES_TO_CLASSES_MAP = new HashMap<String, Class<? extends ObjectPOJO>>();

    private static final Map<String, String> OBJECTS_NAMES_TO_ROOT_NAMES_MAP = new HashMap<String, String>();

    public static Object[][] OBJECT_TYPES = new Object[][] { { "Data Cluster", DataClusterPOJO.class }, //$NON-NLS-1$
            { "Data Model", DataModelPOJO.class }, //$NON-NLS-1$
            { "Role", RolePOJO.class }, //$NON-NLS-1$
            { "Routing Rule", RoutingRulePOJO.class }, //$NON-NLS-1$
            { "Service", Service.class }, //$NON-NLS-1$
            { "Stored Procedure", StoredProcedurePOJO.class }, //$NON-NLS-1$
            { "Transformer Plugin V2", TransformerPluginV2POJO.class }, //$NON-NLS-1$
            { "Transformer V2", TransformerV2POJO.class }, //$NON-NLS-1$
            { "View", ViewPOJO.class }, //$NON-NLS-1$
            { "Menu", MenuPOJO.class }, //$NON-NLS-1$
            { "Background Job", BackgroundJobPOJO.class }, //$NON-NLS-1$
            { "Configuration Info", ConfigurationInfoPOJO.class }, //$NON-NLS-1$
            { "Routing Order V2 Failed", FailedRoutingOrderV2POJO.class }, //$NON-NLS-1$
            { "Routing Order V2 Completed", CompletedRoutingOrderV2POJO.class }, //$NON-NLS-1$
            { "Routing Engine V2", RoutingEngineV2POJO.class }, //$NON-NLS-1$
            { "Custom Layout", CustomFormPOJO.class }, //$NON-NLS-1$
            { "Item", ItemPOJO.class } //$NON-NLS-1$
    };

    private static Logger LOG = Logger.getLogger(ObjectPOJO.class);

    /**
     * Cache the records to improve performance: this is a <b>READ</b> cache only -> only records read from underlying
     * storage should be cached (don't cache what user provide to {@link #store()} for instance).
     */
    private static Object[][] OBJECT_ROOT_ELEMENT_NAMES = new Object[][] { { "Data Cluster", "data-cluster-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Data Model", "data-model-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Role", "role-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Routing Rule", "routing-rule-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Service", "service" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Stored Procedure", "stored-procedure-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Transformer Plugin V2", "transformer-plugin-v2-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Transformer V2", "transformer-v2-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "View", "view-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Menu", "menu-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Background Job", "background-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Configuration Info", "configuration-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Routing Order V2 Active", "active-routing-order-v2-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Routing Order V2 Failed", "failed-routing-order-v2-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Routing Order V2 Completed", "completed-routing-order-v2-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Routing Engine V2", "routing-engine-v2-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Universe", "universe-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Synchronization Plan", "synchronization-plan-pOJO" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Synchronization Conflict", "synchronization-item-pOJO" } //$NON-NLS-1$ //$NON-NLS-2$
    };

    private String digest;

    public ObjectPOJO() {
    }

    public static String getCluster(Class<? extends ObjectPOJO> objectClass) {
        return getCluster(objectClass.getName());
    }

    public static String getCluster(String objectClassName) {
        String[] names = objectClassName.split("\\."); //$NON-NLS-1$
        return "amaltoOBJECTS" + names[names.length - 1].replaceAll("POJO.*", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private static void initObjectsClasses2NamesMap() {
        for (Object[] OBJECT_TYPE : OBJECT_TYPES) {
            String objectName = (String) OBJECT_TYPE[0];
            Class<?> objectClass = (Class<?>) OBJECT_TYPE[1];
            OBJECTS_CLASSES_TO_NAMES_MAP.put(objectClass, objectName);
        }
    }

    public static HashMap<Class<?>, String> getObjectsClasses2NamesMap() {
        if (OBJECTS_CLASSES_TO_NAMES_MAP.size() == 0) {
            initObjectsClasses2NamesMap();
        }
        return new HashMap<Class<?>, String>(OBJECTS_CLASSES_TO_NAMES_MAP);
    }

    public static String getObjectName(Class<?> clazz) {
        if (OBJECTS_CLASSES_TO_NAMES_MAP.size() == 0) {
            initObjectsClasses2NamesMap();
        }
        return OBJECTS_CLASSES_TO_NAMES_MAP.get(clazz);
    }

    private static void initObjectsNames2ClassesMap() {
        for (Object[] OBJECT_TYPE : OBJECT_TYPES) {
            String objectName = (String) OBJECT_TYPE[0];
            Class<? extends ObjectPOJO> objectClass = (Class<? extends ObjectPOJO>) OBJECT_TYPE[1];
            OBJECTS_NAMES_TO_CLASSES_MAP.put(objectName, objectClass);
        }
    }

    public static HashMap<String, Class<?>> getObjectsNames2ClassesMap() {
        if (OBJECTS_NAMES_TO_CLASSES_MAP.size() == 0) {
            initObjectsNames2ClassesMap();
        }
        return new HashMap<String, Class<?>>(OBJECTS_NAMES_TO_CLASSES_MAP);
    }

    public static Class<? extends ObjectPOJO> getObjectClass(String name) throws XtentisException {
        if (OBJECTS_NAMES_TO_CLASSES_MAP.size() == 0) {
            initObjectsNames2ClassesMap();
        }
        try {
            return OBJECTS_NAMES_TO_CLASSES_MAP.get(name);
        } catch (Exception e) {
            String err = "No class found for Object " + name;
            LOG.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    private static void initObjectsNames2RootNamesMap() {
        for (Object[] OBJECT_ROOT_ELEMENT_NAME : OBJECT_ROOT_ELEMENT_NAMES) {
            String objectName = (String) OBJECT_ROOT_ELEMENT_NAME[0];
            String rootElementName = (String) OBJECT_ROOT_ELEMENT_NAME[1];
            OBJECTS_NAMES_TO_ROOT_NAMES_MAP.put(objectName, rootElementName);
        }
    }

    public static String getObjectRootElementName(String name) {
        if (OBJECTS_NAMES_TO_ROOT_NAMES_MAP.size() == 0) {
            initObjectsNames2RootNamesMap();
        }
        return OBJECTS_NAMES_TO_ROOT_NAMES_MAP.get(name);
    }

    /**
     * Loads an object of a particular revision ID<br/>
     * NO Check is done on user rights
     * 
     * @return the instance of the object
     * @throws XtentisException
     */
    public static <T extends ObjectPOJO> T load(Class<T> objectClass, ObjectPOJOPK objectPOJOPK) throws XtentisException {
        String uniqueId = objectPOJOPK.getUniqueId();
        if (LOG.isTraceEnabled()) {
            LOG.trace("load() " + getObjectName(objectClass) + " [" + uniqueId + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        T value = getFromCache(objectClass, uniqueId);
        if (value == null) {
            try {
                // retrieve the item
                XmlServer server = Util.getXmlServerCtrlLocal();
                value = unmarshal(objectClass, server.getDocumentAsString(getCluster(objectClass), uniqueId, null));
                addToCache(objectClass, uniqueId, value);
            } catch (Exception e) {
                String err = "Unable to load the Object  " + getObjectName(objectClass) + "[" + uniqueId
                        + "] in Container " + getCluster(objectClass) + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
                LOG.error(err, e);
                throw new XtentisException(err, e);
            }
        }
        return value;
    }

    /**
     * Remove the item from the DB
     * 
     * @return the Primary Key of the object removed
     * @throws XtentisException
     */
    public static ObjectPOJOPK remove(Class<? extends ObjectPOJO> objectClass, ObjectPOJOPK objectPOJOPK) throws XtentisException {
        if (objectPOJOPK == null) {
            return null;
        }
        String uniqueId = objectPOJOPK.getUniqueId();
        if (LOG.isTraceEnabled()) {
            LOG.trace("remove() " + getObjectName(objectClass) + " [" + uniqueId + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        try {
            // for delete we need to be admin, or have a role of admin , or role of write on instance
            boolean authorized = false;
            ILocalUser user = LocalUser.getLocalUser();
            if (user.getUsername() == null)
                return null;
            if (LocalUser.isAdminUser(user.getUsername())) {
                authorized = true;
            } else if (user.isAdmin(objectClass)) {
                authorized = true;
            } else if (user.userCanWrite(objectClass, uniqueId)) {
                authorized = true;
            }
            if (!authorized) {
                String err = "Unauthorized access on delete for " + "user " + user.getUsername() + " of object "
                        + ObjectPOJO.getObjectName(objectClass) + " [" + uniqueId + "] ";
                LOG.error(err);
                throw new XtentisException(err);
            }
            // get the xml server wrapper
            XmlServer server = Util.getXmlServerCtrlLocal();
            // remove the doc
            long res = server.deleteDocument(getCluster(objectClass), uniqueId);
            if (res == -1) {
                return null;
            }
            clearCaches(objectClass);
            return objectPOJOPK;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to remove the Object  " + uniqueId + " from Cluster "
                    + getCluster(objectClass) + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOG.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Retrieve all the PKs - will fetch only the PKs for which the user is authorized
     * 
     * @return a Collection of ObjectPOJOPK
     * @throws XtentisException
     */
    public static ArrayList<ObjectPOJOPK> findAllPKs(Class<? extends ObjectPOJO> objectClass, String regex)
            throws XtentisException {
        try {
            if (LOG.isTraceEnabled()) {
                LOG.trace("findAllPKs() " + getObjectName(objectClass) +  " using regex [" + regex + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            if ("".equals(regex) || "*".equals(regex) || ".*".equals(regex)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                regex = null;
            }
            String[] ids = getPKListFromCache(objectClass);
            if (ids == null) {
                // get the xml server wrapper
                XmlServer server = Util.getXmlServerCtrlLocal();

                String cluster = getCluster(objectClass);
                // retrieve the item
                ids = server.getAllDocumentsUniqueID(cluster);
                // see 0013859
                if (ids == null || ids.length == 0) {
                    ids = new String[0];
                }
                addPKListToCache(objectClass, ids);
            }

            // build PKs list based on the user rights
            ILocalUser user = LocalUser.getLocalUser();
            ArrayList<ObjectPOJOPK> list = new ArrayList<ObjectPOJOPK>();
            int numItems = 0;

            for (String id : ids) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Does " + getObjectName(objectClass) + " using regex [" + regex + "] match " + id + " ?"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                boolean match = true;
                if (regex != null) {
                    match = id.matches(regex);
                }
                if (match) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace(getObjectName(objectClass) + " using regex [" + regex + "] matches with " + id); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    if (user.userCanRead(objectClass, id)) {
                        list.add(new ObjectPOJOPK(id));
                        numItems++;
                    } else {
                        match = false;
                    }
                }
                if (LOG.isTraceEnabled()) {
                    if(match) {
                        LOG.trace(getObjectName(objectClass) + " [" + id + "] available for user [" + user.getUsername() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    } else {
                        LOG.trace(getObjectName(objectClass) + " [" + id + "] NOT available for user [" + user.getUsername() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
            }

            if (BAMLogger.log) {
                BAMLogger
                        .log("DATA MANAGER", user.getUsername(), "find all", objectClass, new ObjectPOJOPK(numItems + " Items"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                numItems > 0);
            }
            return list;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to find all the Object identifiers for object " + getObjectName(objectClass) + " using regex "
                    + regex + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOG.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Retrieve all PKs of an object type unsynchronized against a particular plan<br/>
     * The user must have the "administration" role to perform this task
     * 
     * @return a Collection of ObjectPOJOPK
     * @throws XtentisException
     */
    public static ArrayList<String> findAllUnsynchronizedPKs(String objectName, String instancePattern,
            String synchronizationPlanName) throws XtentisException {
        try {
            // check if we are admin
            ILocalUser user = LocalUser.getLocalUser();
            if (!user.getRoles().contains(ICoreConstants.ADMIN_PERMISSION)) {
                String err = "Only an user with the 'administration' role can call the synchronization methods";
                LOG.error(err);
                throw new XtentisException(err);
            }
            // get the xml server wrapper
            String clusterName = getCluster(getObjectClass(objectName));
            String rootElementName = getObjectRootElementName(objectName);

            Item itemCtrl2Bean = Util.getItemCtrl2Local();
            List<IWhereItem> conditions = new LinkedList<IWhereItem>();
            if (instancePattern != null && !".*".equals(instancePattern)) {
                WhereCondition idCondition = new WhereCondition(rootElementName + "/../../i", //$NON-NLS-1$
                        WhereCondition.CONTAINS, instancePattern, WhereCondition.PRE_AND);
                conditions.add(idCondition);
            }
            if (synchronizationPlanName != null && !synchronizationPlanName.isEmpty()) {
                WhereCondition planCondition = new WhereCondition(rootElementName + "/last-synch", //$NON-NLS-1$
                        WhereCondition.EQUALS, synchronizationPlanName, WhereCondition.PRE_NOT);
                conditions.add(planCondition);
            }
            IWhereItem whereItem = new WhereAnd(conditions);
            ArrayList<String> elements = new ArrayList<String>();
            elements.add(rootElementName + "/../../i"); //$NON-NLS-1$
            return itemCtrl2Bean.xPathsSearch(new DataClusterPOJOPK(clusterName), null, elements, whereItem, -1, 0, -1, false);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Error Finding All Unsynchronized PKs" + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOG.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get the records for which the user is authorized and matching certain conditions
     * 
     * @param objectClass The class of the XtentisObject
     * @param idsPaths The full path (starting with the object element root name) of the ids
     * @param whereItem The condition
     * @param orderBy An option full path to order by
     * @param direction The direction if orderBy is not <code>null</code>. One of
     * {@link IXmlServerSLWrapper#ORDER_ASCENDING}, {@link IXmlServerSLWrapper#ORDER_DESCENDING}
     * @param start The first item index (starts at zero)
     * @param limit The maximum number of items to return
     * @param withTotalCount If true, return total search count as first result.
     * @return The list of results
     * @throws XtentisException
     */
    public static Collection<ObjectPOJOPK> findPKsByCriteriaWithPaging(Class<? extends ObjectPOJO> objectClass,
            String[] idsPaths, IWhereItem whereItem, String orderBy, String direction, int start, int limit,
            boolean withTotalCount) throws XtentisException {
        try {
            // check if we are admin
            ILocalUser user = LocalUser.getLocalUser();
            boolean isAdmin = false;
            if (LocalUser.isAdminUser(user.getUsername())) {
                isAdmin = true;
            } else if (user.isAdmin(objectClass)) {
                isAdmin = true;
            }
            // Get the values from databases
            Item itemCtrl = Util.getItemCtrl2Local();
            DataClusterPOJOPK dataCluster = new DataClusterPOJOPK(ObjectPOJO.getCluster(objectClass));
            ArrayList<String> xPaths = new ArrayList<String>(Arrays.asList(idsPaths));
            ArrayList<String> results = itemCtrl.xPathsSearch(dataCluster, null, xPaths, whereItem, -1, orderBy, direction,
                    start, limit, withTotalCount);
            // no result --> we are done
            if (results == null) {
                return Collections.emptyList();
            }
            // Log
            if (LOG.isTraceEnabled()) {
                LOG.trace("findAllPKsByCriteriaWithPaging() Results size: " + results.size()); //$NON-NLS-1$  //$NON-NLS-2$
            }
            List<ObjectPOJOPK> pojoPks = new LinkedList<ObjectPOJOPK>();
            for (String result : results) {
                XMLEventReader reader = MDMXMLUtils.createXMLEventReader(new StringReader(result));
                XMLEvent xmlEvent = reader.nextEvent();
                String[] idValues = new String[idsPaths.length];
                int idIndex = 0;
                while (xmlEvent.getEventType() != XMLEvent.END_DOCUMENT) {
                    switch (xmlEvent.getEventType()) {
                    case XMLEvent.CHARACTERS:
                        String data = xmlEvent.asCharacters().getData().trim();
                        if (!data.isEmpty()) {
                            idValues[idIndex++] = data;
                        }
                        break;
                    }
                    xmlEvent = reader.nextEvent();
                }
                // check authorizations
                if (isAdmin || user.userCanRead(objectClass, result)) {
                    pojoPks.add(new ObjectPOJOPK(idValues));
                }
            }
            return pojoPks;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to find all the Object identifiers Using Criteria for object " + getObjectName(objectClass) //$NON-NLS-1$
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage(); //$NON-NLS-1$ //$NON-NLS-2$
            LOG.error(err, e);
            throw new XtentisException(err, e);
        }

    }

    /**
     * Unmarshals an Object POJO to the Original Object
     * 
     * @return The Object instance
     * @throws XtentisException
     */
    public static <T extends ObjectPOJO> T unmarshal(Class<T> objectClass, String marshaledItem) throws XtentisException {
        try {
            if (marshaledItem == null) {
                return null;
            }
            return MarshallingFactory.getInstance().getUnmarshaller(objectClass).unmarshal(new StringReader(marshaledItem));
        } catch (Throwable t) {
            String err = "Unable to unmarshal the object of class '" + objectClass + "' from \n" + marshaledItem;
            LOG.error(err, t);
            throw new XtentisException(err, t);
        }
    }
    
    public String getDigest() {
        return this.digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    /**
     * The PK
     * 
     * @return the pk, null if undefined
     */
    public abstract ObjectPOJOPK getPK();

    /**
     * Store the current item in the DB
     * 
     * @return the pk of the item
     * @throws XtentisException
     */
    public ObjectPOJOPK store() throws XtentisException {
        String uniqueId = getPK().getUniqueId();
        if (LOG.isTraceEnabled()) {
            LOG.trace("store() " + getObjectName(this.getClass()) + " [" + uniqueId + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        try {
            // for storing we need to be admin, or have a role of admin , or role of write on instance
            boolean authorized = false;
            ILocalUser user = LocalUser.getLocalUser();
            if (LocalUser.isAdminUser(user.getUsername())) {
                authorized = true;
            } else if (user.userCanWrite(this.getClass(), uniqueId)) {
                authorized = true;
            }
            if (!authorized) {
                String err = "Unauthorized write access by " + "user " + user.getUsername() + " on object "
                        + ObjectPOJO.getObjectName(this.getClass()) + " [" + uniqueId + "] ";
                LOG.error(err);
                throw new XtentisException(err);
            }
            try {
                // get the xml server wrapper
                XmlServer server = Util.getXmlServerCtrlLocal();
                // Marshal
                StringWriter sw = new StringWriter();
                MarshallingFactory.getInstance().getMarshaller(this.getClass()).marshal(this, sw);
                // store
                String dataClusterName = getCluster(this.getClass());
                server.start(dataClusterName);
                if (-1 == server.putDocumentFromString(sw.toString(), getPK().getUniqueId(), dataClusterName)) {
                    server.rollback(dataClusterName);
                    return null;
                }
                server.commit(dataClusterName);
                clearCaches(this.getClass());
                return getPK();
            } catch (XtentisException e) {
                throw (e);
            } catch (Exception e) {
                String err = "Unable to store the Object " + this.getClass().getName() + " --> " + getPK().getUniqueId() + ": "
                        + e.getClass().getName() + ": " + e.getLocalizedMessage();
                LOG.error(err, e);
                throw new XtentisException(err, e);
            }
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to store the Object  " + getPK().getUniqueId() + " in Cluster " + getCluster(this.getClass())
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOG.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Returns a marshaled version of the object<br/>
     * Identical to calling {@link #marshal()} but does not throw an Exception, returns <code>null</code> instead
     */
    @Override
    public String toString() {
        try {
            return marshal();
        } catch (XtentisException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Returns a marshaled version of the object<br/>
     * 
     * @return The marshaled object
     * @throws XtentisException
     */
    public String marshal() throws XtentisException {
        // Marshal
        StringWriter sw = new StringWriter();
        try {
            MarshallingFactory.getInstance().getMarshaller(this.getClass()).marshal(this, sw);
        } catch (Throwable t) {
            String err = "Unable to marshal '" + this.getPK().getUniqueId() + "'";
            LOG.error(err, t);
            throw new XtentisException(err, t);
        }
        return sw.toString();
    }

    private static <T extends ObjectPOJO> String getCacheName(Class<T> objectClass) {
        String cacheName;
        if(objectClass == TransformerV2POJO.class) {
            cacheName = MDMEhCacheUtil.TRANSFORMER_CACHE_NAME;
        } else if(objectClass == RoutingRulePOJO.class) {
            cacheName = MDMEhCacheUtil.ROUTING_RULE_CACHE_NAME;
        } else if(objectClass == DataClusterPOJO.class) {
            cacheName = MDMEhCacheUtil.DATA_CLUSTER_CACHE_NAME;
        } else {
            cacheName = null;
        }
        return cacheName;
    }

    private static <T extends ObjectPOJO> String getPKListCacheName(Class<T> objectClass) {
        String cacheName;
        if(objectClass == TransformerV2POJO.class) {
            cacheName = MDMEhCacheUtil.TRANSFORMER_PKS_CACHE_NAME;
        } else if(objectClass == RoutingRulePOJO.class) {
            cacheName = MDMEhCacheUtil.ROUTING_RULE_PK_CACHE_NAME;
        } else {
            cacheName = null;
        }
        return cacheName;
    }

    private static <T extends ObjectPOJO> T getFromCache(Class<T> objectClass, String uniqueId) {
        String cacheName = getCacheName(objectClass);
        if(cacheName == null) {
            return null;
        } else {
            T value = MDMEhCacheUtil.getCache(cacheName, uniqueId);
            if (value != null && LOG.isTraceEnabled()) {
                LOG.trace(getObjectName(objectClass) + " [" + uniqueId + "] found in cache"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return value;
        }
    }

    private static <T extends ObjectPOJO> void addToCache(Class<T> objectClass, String uniqueId, T value) {
        String cacheName = getCacheName(objectClass);
        if(cacheName != null) {
            MDMEhCacheUtil.addCache(cacheName, uniqueId, value);
        }
    }

    private static <T extends ObjectPOJO> String[] getPKListFromCache(Class<T> objectClass) {
        String cacheName = getPKListCacheName(objectClass);
        if(cacheName == null) {
            return null;
        } else {
            String[] ids = MDMEhCacheUtil.getCache(cacheName, ""); //$NON-NLS-1$
            if (ids != null && LOG.isTraceEnabled()) {
                LOG.trace(getObjectName(objectClass) + " found in cache"); //$NON-NLS-1$
            }
            return ids;
        }
    }

    private static <T extends ObjectPOJO> void addPKListToCache(Class<T> objectClass, String[] ids) {
        String cacheName = getPKListCacheName(objectClass);
        if(cacheName != null) {
            MDMEhCacheUtil.addCache(cacheName, "", ids); //$NON-NLS-1$
        }
    }

    private static <T extends ObjectPOJO> void clearCaches(Class<T> objectClass) {
        String cacheName = getCacheName(objectClass);
        if(cacheName != null) {
            MDMEhCacheUtil.clearCache(cacheName);
        }
        cacheName = getPKListCacheName(objectClass);
        if(cacheName != null) {
            MDMEhCacheUtil.clearCache(cacheName);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(getObjectName(objectClass) + " caches cleared" ); //$NON-NLS-1$
        }
    }
}
