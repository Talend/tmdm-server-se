/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.ICoreConstants;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.amalto.core.delegator.IBeanDelegator;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.objects.datamodel.DataModelPOJOPK;
import com.amalto.core.objects.marshalling.MarshallingFactory;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

public class ItemPOJO implements Serializable {

    /**
     * FIXME The newInstance() is deprecated and the newFactory() method should be used instead. However since no
     * changes in behavior are defined by this replacement method, keep deprecated method to ensure there's no
     * class loading issues for now (see TMDM-3604).
     **/
    private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

    private static Pattern pLoad = Pattern.compile(".*?(<c>.*?</taskId>|<c>.*?</t>).*?(<p>(.*)</p>|<p/>).*", Pattern.DOTALL); //$NON-NLS-1$

    private static Map<String, XSystemObjects> DATA_CLUSTER_SYSTEM_OBJECTS = new HashMap<String, XSystemObjects>();

    public static Logger LOG = Logger.getLogger(ItemPOJO.class);

    public static Pattern pathWithoutConditions = Pattern.compile("(.*?)[\\[|/].*");

    private String dataModelName;// used for binding data model

    private String conceptName;

    private DataClusterPOJOPK dataClusterPOJOPK;

    private long insertionTime;

    private String[] itemIds;

    private Element projection;

    private String taskId;

    static {
        DATA_CLUSTER_SYSTEM_OBJECTS = XSystemObjects.getXSystemObjects(XObjectType.DATA_CLUSTER);
    }

    public ItemPOJO() {
    }

    public ItemPOJO(DataClusterPOJOPK clusterPK, String concept, String[] ids, long time, Element projection) {
        this.conceptName = concept;
        this.dataClusterPOJOPK = clusterPK;
        this.insertionTime = time;
        this.itemIds = ids;
        this.projection = projection;
    }

    public ItemPOJO(DataClusterPOJOPK clusterPK, String concept, String[] ids, long time, String projectionAsString) {
        this.conceptName = concept;
        this.dataClusterPOJOPK = clusterPK;
        this.insertionTime = time;
        this.itemIds = ids;
        this.projectionString = projectionAsString;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getDataModelName() {
        return dataModelName;
    }

    public void setDataModelName(String dataModelName) {
        this.dataModelName = dataModelName;
    }

    /**
     * @return Returns the conceptName.
     */
    public String getConceptName() {
        return conceptName;
    }

    /**
     * @param conceptName The conceptName to set.
     */
    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    /**
     * @return Returns the dataClusterPK.
     */
    public DataClusterPOJOPK getDataClusterPOJOPK() {
        return dataClusterPOJOPK;
    }

    /**
     * @param dataClusterPOJOPK The dataClusterPK to set.
     */
    public void setDataClusterPK(DataClusterPOJOPK dataClusterPOJOPK) {
        this.dataClusterPOJOPK = dataClusterPOJOPK;
    }

    /**
     * @return Returns the insertionTime.
     */
    public long getInsertionTime() {
        return insertionTime;
    }

    /**
     * @param insertionTime The insertionTime to set.
     */
    public void setInsertionTime(long insertionTime) {
        this.insertionTime = insertionTime;
    }

    /**
     * @return Returns the itemIds.
     */
    public String[] getItemIds() {
        return itemIds;
    }

    /**
     * @param itemIds The itemIds to set.
     */
    public void setItemIds(String[] itemIds) {
        this.itemIds = itemIds;
    }

    private String projectionString = null;

    /**
     * 
     * @return The projection as a String
     * @throws XtentisException
     */
    public String getProjectionAsString() throws XtentisException {
        if (projectionString == null) {
            try {
                projectionString = Util.nodeToString(projection);
            } catch (Exception e) {
                String err = "Unable to serialize the Item " + this.getItemPOJOPK().getUniqueID() + ": "
                        + e.getLocalizedMessage();
                LOG.error(err, e);
                throw new XtentisException(err);
            }
        }
        return projectionString.replaceAll("<\\?xml.*?\\?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @param str The projection to set.
     */
    public void setProjectionAsString(String str) throws XtentisException {
        this.projectionString = str;
        try {
            if (str != null && str.length() > 0) {
                projection = Util.parse(this.projectionString).getDocumentElement();
            }
        } catch (Exception e) {
            String err = "Unable to parse the Item " + this.getItemPOJOPK().getUniqueID() + ". " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOG.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * @return Returns the projection.
     */
    public Element getProjection() throws XtentisException {
        if (projection == null) {
            try {
                projection = Util.parse(this.projectionString).getDocumentElement();
            } catch (Exception e) {
                String err = "Unable to parse the Item " + this.getItemPOJOPK().getUniqueID() + ". " + e.getClass().getName()
                        + ": " + e.getLocalizedMessage();
                LOG.error(err, e);
                throw new XtentisException(err);
            }
        }
        return projection;
    }

    /**
     * @param projection The projection to set.
     */
    public void setProjection(Element projection) {
        this.projection = projection;
        projectionString = null;
    }

    /**
     * The PK
     * 
     * @return the pk, null if undefined
     */
    public ItemPOJOPK getItemPOJOPK() {
        if (getDataClusterPOJOPK() == null || getConceptName() == null || getItemIds() == null) {
            return null;
        }
        return new ItemPOJOPK(getDataClusterPOJOPK(), getConceptName(), getItemIds());
    }

    /**
     * Loads an Item<br/>
     * @return the {@link ItemPOJO}
     */
    public static ItemPOJO load(ItemPOJOPK itemPOJOPK) throws XtentisException {
        checkAccess(LocalUser.getLocalUser(), itemPOJOPK, false, "read"); //$NON-NLS-1$
        return loadItem(itemPOJOPK);
    }

    public static ItemPOJO loadItem(ItemPOJOPK itemPOJOPK) throws XtentisException {
        XmlServer server = Util.getXmlServerCtrlLocal();
        ILocalUser user = LocalUser.getLocalUser();
        try {
            // retrieve the item
            String id = itemPOJOPK.getUniqueID();
            String item = server.getDocumentAsString(itemPOJOPK.getDataClusterPOJOPK().getUniqueId(), id);
            if (item == null) {
                return null;
            }
            ItemPOJO newItem = new ItemPOJO();
            // Build the result
            newItem.setDataClusterPK(itemPOJOPK.getDataClusterPOJOPK());
            newItem.setConceptName(itemPOJOPK.getConceptName());
            newItem.setItemIds(itemPOJOPK.getIds());
            Matcher m = pLoad.matcher(item);
            if (m.matches()) {
                String h = "<header>" + m.group(1) + "</header>"; //$NON-NLS-1$ //$NON-NLS-2$
                Element header = Util.parse(h).getDocumentElement();
                // used for binding data model
                String dm = Util.getFirstTextNode(header, "dmn");//$NON-NLS-1$
                if (dm == null) {
                    dm = Util.getFirstTextNode(header, "dm");//$NON-NLS-1$
                }
                newItem.setDataModelName(dm); //$NON-NLS-1$
                //if <t> after <taskId> then see 0021697
                String time = null;
                if (!m.group(1).contains("<t>")) { //$NON-NLS-1$
                    Pattern tp = Pattern.compile("<t>(.*?)</t>"); //$NON-NLS-1$
                    Matcher tm = tp.matcher(item);
                    if (tm.find()) {
                        time = tm.group(1);
                    }
                } else {
                    time = Util.getFirstTextNode(header, "t"); //$NON-NLS-1$
                }
                if (time != null) {
                    try {
                        newItem.setInsertionTime(Long.parseLong(time)); //$NON-NLS-1$
                    } catch (Exception e) {
                        LOG.error(e);
                    }
                }
                String taskId = Util.getFirstTextNode(header, "taskId"); //$NON-NLS-1$
                if (taskId != null) {
                    newItem.setTaskId(taskId);
                } else {
                    newItem.setTaskId(""); //$NON-NLS-1$
                }
                if (m.group(2) == null || m.group(2).equals("<p/>")) { //$NON-NLS-1$
                    newItem.setProjectionAsString(""); //$NON-NLS-1$
                } else {
                    newItem.setProjectionAsString(m.group(3));
                }
            } else {
                newItem.setProjectionAsString(item);
            }
            return newItem;
        } catch (Exception e) {
            String err = "Unable to load the item  " + itemPOJOPK.getUniqueID() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOG.error(err, e);
            throw new RuntimeException(err, e);
        }
    
    }

    /**
     * Removes an item
     * @return The {@link ItemPOJOPK} of the item removed
     */
    public static ItemPOJOPK remove(ItemPOJOPK itemPOJOPK) throws XtentisException {
        if (itemPOJOPK == null) {
            return null;
        }
        ILocalUser user = LocalUser.getLocalUser();
        checkAccess(user, itemPOJOPK, true, "delete"); //$NON-NLS-1$
        // get the universe and revision ID
        XmlServer server = Util.getXmlServerCtrlLocal();
        try {
            // remove the doc
            String clusterName = itemPOJOPK.getDataClusterPOJOPK().getUniqueId();
            server.deleteDocument(clusterName, itemPOJOPK.getUniqueID());
            return itemPOJOPK;
        } catch (Exception e) {
            String err = "Unable to remove the item " + itemPOJOPK.getUniqueID() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOG.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * drop an item to items-trash
     */
    public static DroppedItemPOJOPK drop(ItemPOJOPK itemPOJOPK, String partPath) throws XtentisException {
        // validate input
        if (itemPOJOPK == null) {
            return null;
        }
        if (partPath == null || partPath.length() == 0) {
            partPath = "/"; //$NON-NLS-1$
        }
        ILocalUser user = LocalUser.getLocalUser();
        checkAccess(user, itemPOJOPK, true, "drop"); //$NON-NLS-1$
        // get XmlServerSLWrapperLocal
        XmlServer server = Util.getXmlServerCtrlLocal();
        try {
            // init MDMItemsTrash Cluster
            if (ObjectPOJO.load(DataClusterPOJO.class, new DataClusterPOJOPK("MDMItemsTrash")) == null) { //$NON-NLS-1$
                // create record
                DataClusterPOJO dataCluster = new DataClusterPOJO("MDMItemsTrash", "Holds logical deleted items", null);
                ObjectPOJOPK pk = dataCluster.store();
                if (pk == null) {
                    throw new XtentisException("Unable to create the Data Cluster. Please check the XML Server logs");
                }
                // create cluster
                boolean exist = server.existCluster(pk.getUniqueId());
                if (!exist) {
                    server.createCluster(pk.getUniqueId());
                }
                // log
                LOG.info("Init MDMItemsTrash Cluster");
            }
            String dataClusterName = itemPOJOPK.getDataClusterPOJOPK().getUniqueId();
            String uniqueID = itemPOJOPK.getUniqueID();
            StringBuilder xmlDocument = new StringBuilder();
            Document sourceDoc = null;
            NodeList toDeleteNodeList = null;
            String xml = server.getDocumentAsString(dataClusterName, uniqueID, null);
            if (xml == null) {
                return null;
            }
            // get to delete item content
            if (partPath.equals("/")) {  //$NON-NLS-1$
                xmlDocument.append(xml);
            } else {
                String xPath = "/ii/p" + partPath; //$NON-NLS-1$
                sourceDoc = Util.parse(xml);
                toDeleteNodeList = Util.getNodeList(sourceDoc, xPath);
                if (toDeleteNodeList.getLength() == 0) {
                    throw new XtentisException("\nThe target content is not exist or have been deleted already.");
                }
                for (int i = 0; i < toDeleteNodeList.getLength(); i++) {
                    Node node = toDeleteNodeList.item(i);
                    xmlDocument.append(Util.nodeToString(node));
                }
            }
            // make source left doc && validate
            if (!partPath.equals("/")) { //$NON-NLS-1$
                if (toDeleteNodeList != null) {
                    Node lastParentNode;
                    Node formatSiblingNode;
                    for (int i = 0; i < toDeleteNodeList.getLength(); i++) {
                        Node node = toDeleteNodeList.item(i);
                        lastParentNode = node.getParentNode();
                        formatSiblingNode = node.getNextSibling();
                        if (lastParentNode != null) {
                            lastParentNode.removeChild(node);
                        }
                        if (formatSiblingNode != null && formatSiblingNode.getNodeValue() != null
                                && formatSiblingNode.getNodeValue().matches("\\s+")) {
                            lastParentNode.removeChild(formatSiblingNode);
                        }
                    }
                }
                // validate
                String leftSourceDoc = Util.nodeToString(sourceDoc);
                ItemPOJO itemPOJO = parse(leftSourceDoc);
                if (itemPOJO.getDataModelName() != null) {
                    DataModelPOJO dataModelPOJO = ObjectPOJO.load(DataModelPOJO.class,
                            new DataModelPOJOPK(itemPOJO.getDataModelName()));
                    if (dataModelPOJO != null) {
                        Element projection;
                        try {
                            projection = itemPOJO.getProjection();
                        } catch (Exception e) {
                            throw new XtentisException("\nThe remaining item can not be empty!");
                        }

                        if (projection != null)
                            Util.validate(itemPOJO.getProjection(), dataModelPOJO.getSchema());
                    }
                }
            }
            // str 2 pojo
            DroppedItemPOJO droppedItemPOJO = new DroppedItemPOJO(
                    itemPOJOPK.getDataClusterPOJOPK(),
                    uniqueID,
                    itemPOJOPK.getConceptName(),
                    itemPOJOPK.getIds(),
                    partPath,
                    xmlDocument.toString(),
                    user.getUsername(),
                    System.currentTimeMillis());
            // Marshal
            StringWriter sw = new StringWriter();
            MarshallingFactory.getInstance().getMarshaller(droppedItemPOJO.getClass()).marshal(droppedItemPOJO, sw);
            // copy item content
            server.start("MDMItemsTrash"); //$NON-NLS-1$
            long res = server.putDocumentFromString(sw.toString(), droppedItemPOJO.obtainDroppedItemPK().getUniquePK(),
                    "MDMItemsTrash"); //$NON-NLS-1$
            server.commit("MDMItemsTrash"); //$NON-NLS-1$
            if (res == -1) {
                server.rollback("MDMItemsTrash"); //$NON-NLS-1$
                return null;
            }
            // delete source item
            try {
                if (partPath.equals("/")) { //$NON-NLS-1$
                    server.deleteDocument(dataClusterName, uniqueID);
                } else {
                    String xmlString = Util.nodeToString(sourceDoc);
                    server.start(dataClusterName);
                    server.putDocumentFromString(xmlString, uniqueID, dataClusterName);
                    server.commit(dataClusterName);
                }
            } catch (Exception e) {
                server.deleteDocument("MDMItemsTrash", droppedItemPOJO.obtainDroppedItemPK().getUniquePK()); //$NON-NLS-1$
                throw new XtentisException(e);
            }
            return droppedItemPOJO.obtainDroppedItemPK();
        } catch (SAXException e) {
            String err = "The remaining item did not obey the rules of data model.\nYou can modify the data model, and try it again.\n\n"
                    + e.getLocalizedMessage();
            throw new XtentisException(err);
        } catch (Exception e) {
            String err = "Unable to drop the item " + itemPOJOPK.getUniqueID() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOG.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    public ItemPOJOPK store(boolean putInCache) throws XtentisException {
        ItemPOJOPK itemPK = getItemPOJOPK();
        if (itemPK == null) {
            return null;
        }
        ILocalUser user = LocalUser.getLocalUser();
        checkAccess(user,getItemPOJOPK(), true, "write"); //$NON-NLS-1$
        return store();
    }

    /**
     * Stores the item in DB.<br/>
     * Users rights will NOT be checked
     * 
     * @return The {@link ItemPOJOPK} of the stored item
     * @throws XtentisException In case of internal exception.
     */
    public ItemPOJOPK store() throws XtentisException {
        ItemPOJOPK itemPK = getItemPOJOPK();
        try {
            String xml = serialize();
            if(LOG.isTraceEnabled()) {
                LOG.trace("store() " + itemPK.getUniqueID() + "\n" + xml); //$NON-NLS-1$ //$NON-NLS-2$
            }
            String uniqueId = itemPK.getUniqueID();
            String clusterId = getDataClusterPOJOPK().getUniqueId();
            XmlServer server = Util.getXmlServerCtrlLocal();
            if (-1 == server.putDocumentFromString(xml,uniqueId ,clusterId)) {
                return null;
            }
            return itemPK;
        } catch (Exception e) {
            String err = "Unable to store the item " + itemPK.getUniqueID() + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOG.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Parses a marshaled item back into an {@link ItemPOJO}
     * @return the {@link ItemPOJO}
     */
    public static ItemPOJO parse(String marshaledItem) throws XtentisException {
        try {
            Pattern p = Pattern.compile(".*?(<c>.*?)(<p>(.*)</p>|<p/>).*", Pattern.DOTALL); //$NON-NLS-1$
            ItemPOJO newItem = new ItemPOJO();
            Matcher m = p.matcher(marshaledItem);
            if (m.matches()) {
                String h = "<header>" + m.group(1) + "</header>"; //$NON-NLS-1$ //$NON-NLS-2$
                Element header = Util.parse(h).getDocumentElement();
                newItem.setConceptName(Util.getFirstTextNode(header, "n")); //$NON-NLS-1$
                // used for binding data model
                if (Util.getFirstTextNode(header, "dmn") != null) { //$NON-NLS-1$
                    newItem.setDataModelName(Util.getFirstTextNode(header, "dmn")); //$NON-NLS-1$
                }
                newItem.setDataClusterPK(new DataClusterPOJOPK(Util.getFirstTextNode(header, "c"))); //$NON-NLS-1$
                newItem.setItemIds(Util.getTextNodes(header, "i")); //$NON-NLS-1$
                newItem.setInsertionTime(Long.parseLong(Util.getFirstTextNode(header, "t"))); //$NON-NLS-1$
                if (m.group(2) == null || m.group(2).equals("<p/>")) { //$NON-NLS-1$
                    newItem.setProjectionAsString(""); //$NON-NLS-1$
                } else {
                    newItem.setProjectionAsString(m.group(3));
                }
                return newItem;
            } else {
                throw new XtentisException("Cannot parse item read from XML Server");
            }
        } catch (Exception e) {
            String err = "Unable to parse the item \n" + marshaledItem;
            LOG.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Serializes the object to an xml string
     * 
     * @return the xml string
     * 
     * Note: dmn & dmr tags are used for binding data model
     * @throws com.amalto.core.util.XtentisException In case of serialization exception.
     */
    @SuppressWarnings("nls")
    public String serialize() throws XtentisException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter streamWriter = null;
        try {
            streamWriter = xmlOutputFactory.createXMLStreamWriter(stringWriter);
            streamWriter.writeStartElement("ii"); //$NON-NLS-1$
            {
                streamWriter.writeStartElement("c"); //$NON-NLS-1$
                streamWriter.writeCharacters(dataClusterPOJOPK.getUniqueId());
                streamWriter.writeEndElement();
                streamWriter.writeStartElement("n"); //$NON-NLS-1$
                streamWriter.writeCharacters(conceptName);
                streamWriter.writeEndElement();
                if (dataModelName != null) {
                    streamWriter.writeStartElement("dmn"); //$NON-NLS-1$
                    streamWriter.writeCharacters(dataModelName);
                    streamWriter.writeEndElement();
                }
                String[] ids = getItemIds();
                for (String id : ids) {
                    if (id != null) {
                        streamWriter.writeStartElement("i"); //$NON-NLS-1$
                        streamWriter.writeCharacters(id.trim());
                        streamWriter.writeEndElement();
                    }
                }
                streamWriter.writeStartElement("t"); //$NON-NLS-1$
                streamWriter.writeCharacters(String.valueOf(insertionTime));
                streamWriter.writeEndElement();
                if (taskId != null) {
                    streamWriter.writeStartElement("taskId"); //$NON-NLS-1$
                    streamWriter.writeCharacters(taskId);
                    streamWriter.writeEndElement();
                }
                streamWriter.writeStartElement("p");//$NON-NLS-1$
                {
                    streamWriter.writeCharacters(" "); //$NON-NLS-1$
                    streamWriter.flush();
                    String xml = getProjectionAsString();
                    stringWriter.append(xml);
                }
                streamWriter.writeEndElement();
            }
            streamWriter.writeEndElement();
            streamWriter.flush();
        } catch (XMLStreamException e) {
            throw new XtentisException(e);
        } finally {
            try {
                if (streamWriter != null) {
                    streamWriter.close();
                }
            } catch (XMLStreamException e) {
                LOG.error("Error during xml writer close.", e);
            }
        }
        return stringWriter.toString();
    }

    @Override
    public String toString() {
        try {
            return serialize();
        } catch (XtentisException e) {
            String err = "Unable to serialize the item: " + e.getMessage();
            LOG.error("ERROR SYSTRACE toString() " + err, e);
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Returns the first part - eg. the concept - from the path
     * @return the Concept
     */
    public static String getConceptFromPath(String path) {
        if (!path.endsWith("/")) {
            path += "/"; //$NON-NLS-1$
        }
        Matcher m = pathWithoutConditions.matcher(path);
        if (m.matches()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    private static void checkAccess(ILocalUser user, ItemPOJOPK itemPOJOPK, boolean mutableAccess, String accessLabel) throws XtentisException {
        assert user != null;
        boolean authorizedAccess;
        String username = user.getUsername();

        boolean isSystemObject = XSystemObjects.isXSystemObject(DATA_CLUSTER_SYSTEM_OBJECTS, itemPOJOPK.getDataClusterPOJOPK().getIds()[0]);

        // admin has all rights, so bypass security checks
        boolean bypassCheckAccess = MDMConfiguration.getAdminUser().equals(username)
                || user.getRoles().contains(ICoreConstants.ADMIN_PERMISSION) || isSystemObject;

        if (bypassCheckAccess) {
            return;
        }

        if(itemPOJOPK.getDataClusterPOJOPK() != null && !StorageAdmin.SYSTEM_STORAGE.equals(itemPOJOPK.getDataClusterPOJOPK().getUniqueId()) && Util.isEnterprise()){
            isExistDataCluster(itemPOJOPK.getDataClusterPOJOPK());
        }

        if(user.isAdmin(ItemPOJO.class)) {
            authorizedAccess = true;
        } else {
            ItemPOJO itemPOJO = loadItem(itemPOJOPK);
            if(mutableAccess) {
                authorizedAccess = user.userItemCanWrite(itemPOJO, itemPOJOPK.getDataClusterPOJOPK().getUniqueId(), itemPOJOPK.getConceptName());
            } else {
                authorizedAccess = user.userItemCanRead(itemPOJO);
            }
        }
        if (!authorizedAccess) {
            String err = "Unauthorized " + accessLabel + " access by " + "user " + username + " on Item '" + itemPOJOPK.getUniqueID() + "'";
            LOG.error(err);
            throw new XtentisException(err);
        }
    }
    
    public static boolean isExistDataCluster(DataClusterPOJOPK dataClusterPOJOPK) throws XtentisException {
        try {
            if (Util.getDataClusterCtrlLocal().existsDataCluster(dataClusterPOJOPK) == null) {
                throw new IllegalArgumentException("Data Cluster '" + dataClusterPOJOPK.getUniqueId() + "' does not exist."); //$NON-NLS-1$//$NON-NLS-2$
            }
        } catch (Exception e) {
            throw new XtentisException("Unable to get the Data Cluster '" + dataClusterPOJOPK.getUniqueId() + "'", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return true;
    }
}