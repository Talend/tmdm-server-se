/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.handler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class ItemTreeHandler implements IsSerializable {

    public static final String XMLNS_TMDM = "xmlns:tmdm"; //$NON-NLS-1$

    public static final String XMLNS_TMDM_VALUE = "http://www.talend.com/mdm"; //$NON-NLS-1$

    private EntityModel entityModel;

    private ItemNodeModel nodeModel;

    private ItemBean itemBean;

    private ItemTreeHandlingStatus status;

    private List<Map<String, Object>> keyMapList;

    private boolean simpleTypeOnly = true;

    @Deprecated
    public ItemTreeHandler() {

    }

    public ItemTreeHandler(ItemNodeModel nodeModel, ViewBean viewBean) {
        this(nodeModel, viewBean, ItemTreeHandlingStatus.InUse);
    }

    public ItemTreeHandler(ItemNodeModel nodeModel, EntityModel entityModel) {
        this(nodeModel, entityModel, ItemTreeHandlingStatus.InUse);
    }

    public ItemTreeHandler(ItemNodeModel nodeModel, ViewBean viewBean, ItemTreeHandlingStatus status) {
        super();
        this.nodeModel = nodeModel;
        this.status = status;
        if (viewBean != null) {
            this.entityModel = viewBean.getBindingEntityModel();
        }
        initConfig();
    }

    public ItemTreeHandler(ItemNodeModel nodeModel, ViewBean viewBean, ItemBean itemBean, ItemTreeHandlingStatus status) {
        this(nodeModel, viewBean, status);
        this.itemBean = itemBean;
    }

    public ItemTreeHandler(ItemNodeModel nodeModel, EntityModel entityModel, ItemTreeHandlingStatus status) {
        super();
        this.nodeModel = nodeModel;
        this.entityModel = entityModel;
        this.status = status;
        initConfig();
    }

    public ItemTreeHandler(ItemNodeModel nodeModel, ViewBean viewBean, List<Map<String, Object>> keyMapList,
            ItemTreeHandlingStatus status) {
        super();
        this.nodeModel = nodeModel;
        if (viewBean != null) {
            this.entityModel = viewBean.getBindingEntityModel();
        }
        this.status = status;
        this.keyMapList = keyMapList;
        initConfig();
    }

    private void initConfig() {
        // dependency for external configurations
        // do nothing for now
    }

    public boolean isSimpleTypeOnly() {
        return simpleTypeOnly;
    }

    public void setSimpleTypeOnly(boolean simpleTypeOnly) {
        this.simpleTypeOnly = simpleTypeOnly;
    }

    public EntityModel getEntityModel() {
        return entityModel;
    }

    public void setEntityModel(EntityModel entityModel) {
        this.entityModel = entityModel;
    }

    public ItemNodeModel getNodeModel() {
        return nodeModel;
    }

    public void setNodeModel(ItemNodeModel nodeModel) {
        this.nodeModel = nodeModel;
    }

    public ItemTreeHandlingStatus getStatus() {
        return status;
    }

    public void setStatus(ItemTreeHandlingStatus status) {
        this.status = status;
    }

    public String serializeItem() {
        return serializeItem(true);
    }

    public String serializeItem(boolean sort) {

        if (nodeModel == null) {
            return null;
        }

        // If do not sort, then it should rely on the input order
        if (sort) {
            nodeModel.sort(entityModel);
        }

        Document doc = XMLParser.createDocument();
        Element root;
        if (status.equals(ItemTreeHandlingStatus.BulkUpdate)) {
            root = doc.createElement("records");
            for (int i = 0; i < keyMapList.size(); i++) {
                Map<String, Object> keyMap = keyMapList.get(i);
                root.appendChild(buildXML(doc, nodeModel, keyMap));
            }
        } else {
            root = buildXML(doc, nodeModel, null);
            if (nodeModel.get(XMLNS_TMDM) != null) {
                root.setAttribute(XMLNS_TMDM, XMLNS_TMDM_VALUE);
            }
        }
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$//$NON-NLS-2$
        doc.appendChild(root);
        return doc.toString();

    }

    private Element buildXML(Document doc, ItemNodeModel currentNodeModel, Map<String, Object> keyMap) {
        if (status.equals(ItemTreeHandlingStatus.BulkUpdate) && currentNodeModel.isKey()) {
            currentNodeModel.setObjectValue((Serializable) keyMap.get(currentNodeModel.getTypePath()));
        }
        Element root = doc.createElement(currentNodeModel.getName());
        TypeModel currentTypeModel = entityModel.getMetaDataTypes().get(currentNodeModel.getTypePath());
        if (status.equals(ItemTreeHandlingStatus.ToSave) || status.equals(ItemTreeHandlingStatus.BulkUpdate)) {
            if (currentNodeModel.getParent() != null) {
                if (!currentNodeModel.isKey()) {
                    if (!currentTypeModel.isVisible()) {
                        if (simpleTypeOnly) {
                            if (currentTypeModel.isSimpleType()) {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                    if (currentTypeModel.isReadOnly()) {
                        if (simpleTypeOnly) {
                            if (currentTypeModel.isSimpleType()
                                    && (currentTypeModel.getDefaultValueExpression() == null || currentTypeModel
                                            .getDefaultValueExpression().length() == 0)) {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                }
            }
        }
        Serializable value = currentNodeModel.getObjectValue();
        if (!currentNodeModel.isMassUpdate() || (currentNodeModel.isMassUpdate() && currentNodeModel.isEdited())) {
            if (currentTypeModel.isSimpleType()) {
                if (value != null && currentNodeModel.getParent() != null) {
                    String elValue = null;
                    if (value instanceof ForeignKeyBean) {
                        elValue = getLookUpFieldValue(itemBean, currentNodeModel.getTypePath(), ((ForeignKeyBean) value).getId());
                        currentNodeModel.setTypeName(((ForeignKeyBean) value).getConceptName());
                    } else {
                        elValue = getLookUpFieldValue(itemBean, currentNodeModel.getTypePath(), value.toString());
                    }
                    root.appendChild(doc.createTextNode(elValue));

                }
            }
            if (currentNodeModel.getRealType() != null) {
                root.setAttribute("xsi:type", currentNodeModel.getRealType()); //$NON-NLS-1$
            }
            if (currentNodeModel.getTypeName() != null) {
                root.setAttribute("tmdm:type", currentNodeModel.getTypeName()); //$NON-NLS-1$
                nodeModel.set(XMLNS_TMDM, XMLNS_TMDM_VALUE);
            }
            List<ModelData> children = currentNodeModel.getChildren();
            if (children != null && children.size() > 0) {
                for (ModelData child : children) {
                    ItemNodeModel childNode = (ItemNodeModel) child;
                    if (currentNodeModel.isMassUpdate() == childNode.isMassUpdate()) {
                        Element el = buildXML(doc, childNode, keyMap);
                        if (el != null) {
                            if (status.equals(ItemTreeHandlingStatus.ToSave) || status.equals(ItemTreeHandlingStatus.BulkUpdate)) {
                                if (isRepeatingEl(childNode)) {
                                    List<Element> childrenEls = childrenEl(root);
                                    if (isEmptyValueEl(el)) {
                                        if (childrenEls == null || childrenEls.size() == 0) {
                                            root.appendChild(el);
                                        } else {
                                            TypeModel childTypeModel = entityModel.getMetaDataTypes()
                                                    .get(childNode.getTypePath());
                                            if (childTypeModel.getType().getTypeName()
                                                    .equals(DataTypeConstants.UUID.getTypeName())
                                                    || childTypeModel.getType().getTypeName()
                                                            .equals(DataTypeConstants.AUTO_INCREMENT.getTypeName())) {
                                                root.appendChild(el);
                                            } else {
                                                // for mixture mode
                                                boolean alreadyHasBrother = false;
                                                for (Element myChildEl : childrenEls) {
                                                    if (myChildEl.getNodeName().equals(el.getNodeName())) {
                                                        alreadyHasBrother = true;
                                                        break;
                                                    }
                                                }
                                                if (!alreadyHasBrother) {
                                                    root.appendChild(el);
                                                }
                                            }
                                        }
                                    } else {
                                        if (childrenEls != null && childrenEls.size() > 0) {
                                            for (Element myChildEl : childrenEls) {
                                                if (myChildEl.getNodeName().equals(el.getNodeName()) && isEmptyValueEl(myChildEl)) {
                                                    root.removeChild(myChildEl);// clean up empty node
                                                }
                                            }
                                        }
                                        // append non-empty el
                                        root.appendChild(el);
                                    }
                                } else {
                                    root.appendChild(el);
                                }
                            } else {
                                root.appendChild(el);
                            }
                        }
                    }
                }
            }
            return root;
        } else {
            return null;
        }
    }

    private boolean isRepeatingEl(ItemNodeModel nodeModel) {
        TypeModel typeModel = entityModel.getMetaDataTypes().get(nodeModel.getTypePath());
        if (typeModel.getMaxOccurs() > 1 || typeModel.getMaxOccurs() == -1) {
            return true;
        }
        return false;
    }

    private boolean isEmptyValueEl(Element el) {
        List<Element> childrenEl = childrenEl(el);
        if (childrenEl.size() == 0) {
            Node node = el.getFirstChild();
            return node == null || node.getNodeValue() == null || node.getNodeValue().trim().length() == 0;
        }

        for (Element childEl : childrenEl) {
            if (!isEmptyValueEl(childEl)) {
                return false;
            }
        }
        return true;
    }

    private List<Element> childrenEl(Element el) {
        List<Element> els = new ArrayList<Element>();
        NodeList childNodes = el.getChildNodes();
        if (childNodes != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    els.add((Element) child);
                }
            }
        }
        return els;
    }

    protected String getLookUpFieldValue(ItemBean itemBean, String path, String elValue) {
        if (itemBean != null && itemBean.getOriginalLookupFieldDisplayValueMap() != null
                && itemBean.getOriginalLookupFieldDisplayValueMap().containsKey(path)) {
            List<String> displayValueList = itemBean.getOriginalLookupFieldDisplayValueMap().get(path);
            List<String> valueList = itemBean.getOriginalLookupFieldValueMap().get(path);
            if (displayValueList.size() > 0) {
                String displayValue = displayValueList.get(0);
                displayValueList.remove(0);
                String value = valueList.get(0);
                valueList.remove(0);
                if (elValue.equals(displayValue)) {
                    return value;
                }
            }
        }
        return elValue;
    }
}
