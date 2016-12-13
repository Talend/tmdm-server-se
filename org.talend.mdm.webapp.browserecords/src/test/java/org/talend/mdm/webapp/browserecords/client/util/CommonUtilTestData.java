/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.creator.DataTypeCreator;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;

import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

@SuppressWarnings("nls")
public class CommonUtilTestData {

    public static EntityModel getEntityModel(String modelXml) {
        Element root = XMLParser.parse(modelXml).getDocumentElement();
        EntityModel entity = new EntityModel();
        List<String> ids = new ArrayList<String>();
        entity.setConceptName(root.getAttribute("concept"));

        NodeList childNodes = root.getChildNodes();
        if (childNodes != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element childEl = (Element) child;
                    boolean isSimple = childEl.getAttribute("isSimple") == null ? true : Boolean.parseBoolean(childEl
                            .getAttribute("isSimple"));
                    boolean isPk = childEl.getAttribute("isKey") == null ? false : Boolean.parseBoolean(childEl
                            .getAttribute("isKey"));
                    boolean isFk = childEl.getAttribute("isFk") == null ? false : Boolean.parseBoolean(childEl
                            .getAttribute("isFk"));
                    boolean isReadOnly = childEl.getAttribute("isReadOnly") == null ? false : Boolean.parseBoolean(childEl
                            .getAttribute("isReadOnly"));
                    boolean isVisible = childEl.getAttribute("isVisible") == null ? true : Boolean.parseBoolean(childEl
                            .getAttribute("isVisible"));
                    int minOccurs = childEl.getAttribute("minOccurs") == null ? 0 : Integer.parseInt(childEl
                            .getAttribute("minOccurs"));
                    int maxOccurs = childEl.getAttribute("maxOccurs") == null ? 1 : Integer.parseInt(childEl
                            .getAttribute("maxOccurs"));
                    String typePath = childEl.getAttribute("typePath");
                    String dataType = childEl.getAttribute("dataType");
                    String name = childEl.getAttribute("name");
                    String defaultValueExpression = childEl.getAttribute("defaultValueExpression");
                    TypeModel tm = null;

                    if (isSimple) {
                        SimpleTypeModel stm = new SimpleTypeModel(name, DataTypeCreator.getDataType(dataType, ""));
                        stm.setForeignkey(isFk ? "fk/fk" : null);
                        stm.setXpath(typePath.replaceAll(":\\w+", ""));
                        tm = stm;
                    } else {
                        ComplexTypeModel ctm = new ComplexTypeModel(name, DataTypeCreator.getDataType(dataType, ""));
                        ctm.setXpath(typePath.replaceAll(":\\w+", ""));
                        tm = ctm;
                    }

                    if (isPk) {
                        ids.add(typePath);
                    }
                    tm.setReadOnly(isReadOnly);
                    tm.setVisible(isVisible);
                    tm.setTypePath(typePath);
                    tm.setMinOccurs(minOccurs);
                    tm.setMaxOccurs(maxOccurs);
                    if (defaultValueExpression != null && defaultValueExpression.length() > 0) {
                        tm.setDefaultValueExpression(defaultValueExpression);
                    }
                    entity.getMetaDataTypes().put(typePath, tm);
                }
            }
        }
        if (ids.size() > 0) {
            entity.setKeys(ids.toArray(new String[ids.size()]));
        }

        return entity;
    }

    public static ItemNodeModel getItemNodeModel(String recordXml, EntityModel entityModel) {
        Element root = XMLParser.parse(recordXml).getDocumentElement();
        return builderItemNodeModel(root, entityModel);
    }

    private static ItemNodeModel builderItemNodeModel(Element el, EntityModel entityModel) {
        ItemNodeModel node = new ItemNodeModel(el.getNodeName());
        node.setTypePath(getElTypePath(el));
        TypeModel tm = entityModel.getMetaDataTypes().get(node.getTypePath());
        if (tm == null) {
            throw new RuntimeException(node.getTypePath() + " does not exist! ");
        }
        if (tm.isSimpleType()) {
            if (tm.getForeignkey() != null && tm.getForeignkey().trim().length() > 0) {
                if (el.getFirstChild().getNodeType() == Node.TEXT_NODE && el.getFirstChild().getNodeValue() != null
                        && el.getFirstChild().getNodeValue().trim().length() > 0) {
                    ForeignKeyBean fkBean = new ForeignKeyBean();
                    String entityType = el.getAttribute("tmdm:type"); //$NON-NLS-1$
                    if (entityType != null && entityType.trim().length() > 0) {
                        fkBean.setConceptName(entityType);
                    }
                    fkBean.setId(el.getFirstChild().getNodeValue());
                    node.setObjectValue(fkBean);
                } else {
                    node.setObjectValue(null);
                }
            } else {
                if (el.getFirstChild() != null) {
                    node.setObjectValue(el.getFirstChild().getNodeValue());
                } else {
                    node.setObjectValue(null);
                }
            }
        }

        if (entityModel.getKeys() != null) {
            for (String key : entityModel.getKeys()) {
                if (key.equals(node.getTypePath())) {
                    node.setKey(true);
                }
            }
        }

        NodeList childNodes = el.getChildNodes();
        if (childNodes != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element childEl = (Element) child;
                    ItemNodeModel childNode = builderItemNodeModel(childEl, entityModel);
                    node.add(childNode);
                }
            }
        }

        return node;
    }

    private static String getElTypePath(Element el) {
        String typePath = "";
        Node current = el;
        boolean isFirst = true;
        while (current != null && current instanceof Element) {
            if (isFirst) {
                typePath = getElTypeName((Element) current) + typePath;
                isFirst = false;
            } else {
                typePath = getElTypeName((Element) current) + "/" + typePath;
            }
            current = current.getParentNode();
        }
        return typePath;
    }

    private static String getElTypeName(Element el) {
        String type = el.getAttribute("xsi:type");
        if (type != null && type.trim().length() > 0) {
            return el.getNodeName() + ":" + type;
        }
        return el.getNodeName();
    }

}
