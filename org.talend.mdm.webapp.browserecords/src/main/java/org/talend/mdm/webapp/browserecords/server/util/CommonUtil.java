/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.talend.mdm.commmon.util.exception.XmlBeanDefinitionException;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.util.FormatUtil;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.server.ForeignKeyHelper;
import org.talend.mdm.webapp.base.server.util.XmlUtil;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.DataModelHelper;
import org.talend.mdm.webapp.browserecords.shared.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.allen_sauer.gwt.log.client.Log;
import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.util.EntityNotFoundException;
import com.amalto.core.util.LocalUser;
import com.amalto.webapp.core.bean.Configuration;
import com.amalto.webapp.core.util.Util;

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class CommonUtil {

    private static final Logger LOG = Logger.getLogger(CommonUtil.class);

    public static final String ORIGINAL_VALUE = "originalValue";

    public static final String FORMATE_VALUE = "formateValue";

    public static final String RESULT = "result";

    public static final List<String> dateTypeNames = Arrays.asList(DataTypeConstants.DATE.getBaseTypeName(),
            DataTypeConstants.DATETIME.getBaseTypeName());

    public static final List<String> numberTypeNames = Arrays.asList(DataTypeConstants.DOUBLE.getBaseTypeName(),
            DataTypeConstants.FLOAT.getBaseTypeName(), DataTypeConstants.DECIMAL.getBaseTypeName(),
            DataTypeConstants.INT.getBaseTypeName(), DataTypeConstants.INTEGER.getBaseTypeName(),
            DataTypeConstants.LONG.getBaseTypeName(), DataTypeConstants.SHORT.getBaseTypeName());

    public static List<ItemNodeModel> getDefaultTreeModel(TypeModel model, boolean isCreate, String language) {
        return org.talend.mdm.webapp.browserecords.client.util.CommonUtil.getDefaultTreeModel(model, language, false, isCreate,
                true);
    }

    public static Document getSubXML(TypeModel typeModel, String realType, Map<String, List<String>> map, String language)
            throws ServiceException {
        try {
            DocumentBuilder builder = MDMXMLUtils.getDocumentBuilder().get();
            Document doc = builder.newDocument();
            List<Element> list = _getDefaultXML(typeModel, null, realType, doc, map, language);
            Element root = list.get(0);
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$//$NON-NLS-2$
            doc.appendChild(root);
            return doc;
        } catch (Exception e) {
            throw new XmlBeanDefinitionException("An unexpected exception occurred.", e);
        }
    }

    public static Document getSubDoc(org.dom4j.Document mainDoc, String contextPath) throws ServiceException {
        org.dom4j.Element el = (org.dom4j.Element) mainDoc.selectSingleNode(contextPath);
        if (el.getParent() != null) {
            el.getParent().remove(el);
        }
        org.dom4j.Document doc = DocumentHelper.createDocument(el);
        return XmlUtil.parseDocument(doc);
    }

    private static List<Element> _getDefaultXML(TypeModel model, TypeModel parentModel, String realType, Document doc,
            Map<String, List<String>> map, String language) {
        List<Element> itemNodes = new ArrayList<Element>();
        if (model.getMinOccurs() > 1) {
            for (int i = 0; i < model.getMinOccurs(); i++) {
                Element el = doc.createElement(model.getName());
                applySimpleTypesDefaultValue(model, parentModel, el);
                itemNodes.add(el);
            }
        } else {
            Element el = doc.createElement(model.getName());
            applySimpleTypesDefaultValue(model, parentModel, el);
            itemNodes.add(el);
        }
        if (model.getForeignkey() != null && model.getForeignkey().trim().length() > 0) {
            if (map != null && map.containsKey(model.getXpath()) && map.get(model.getXpath()).size() > 0) {
                int count = map.get(model.getXpath()).size() - itemNodes.size();
                for (int i = 0; i < count; i++) {
                    Element el = doc.createElement(model.getName());
                    applySimpleTypesDefaultValue(model, parentModel, el);
                    itemNodes.add(el);
                }
            }
        }
        if (!model.isSimpleType()) {
            ComplexTypeModel complexModel = (ComplexTypeModel) model;
            ComplexTypeModel realTypeModel = complexModel.getRealType(realType);
            List<TypeModel> children;
            if (realTypeModel != null) {
                children = realTypeModel.getSubTypes();
            } else {
                children = complexModel.getSubTypes();
            }
            for (Element node : itemNodes) {
                if (realTypeModel != null) {
                    node.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", realType); //$NON-NLS-1$ //$NON-NLS-2$
                } else if (parentModel != null && !model.isAbstract() && model.getType() != null
                        && complexModel.getReusableComplexTypes().size() > 0) {
                    // When create a record, if the node is not root node and it is reusable type(but not abstract
                    // type), it need to record the xsi:type value.
                    node.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", model.getType().getTypeName()); //$NON-NLS-1$ //$NON-NLS-2$
                }
                for (TypeModel typeModel : children) {
                    List<Element> els = _getDefaultXML(typeModel, model, realType, doc, map, language);
                    for (Element el : els) {
                        node.appendChild(el);
                    }
                }
            }
        }
        return itemNodes;
    }

    private static void applySimpleTypesDefaultValue(TypeModel nodeTypeModel, TypeModel parentModel, Element el) {

        // if parent node is not root node, also is non-mandatory, don't apply system default value
        if (parentModel != null && parentModel.getParentTypeModel() != null && parentModel instanceof ComplexTypeModel
                && parentModel.getMinOccurs() == 0) {
            return;
        }

        if (nodeTypeModel != null && el != null) {
            // only assist system default value for mandatory node
            if (nodeTypeModel.isSimpleType() && nodeTypeModel.getMinOccurs() > 0) {
                if (nodeTypeModel.getType().equals(DataTypeConstants.BOOLEAN)) {
                    el.setTextContent("false"); //$NON-NLS-1$
                }
                // TODO is there any more?
            }

        }
    }

    public static Map<String, String> handleProcessMessage(String outputMessage, String language) throws Exception {
        Map<String, String> messageMap = new HashMap<String, String>();
        messageMap.put("typeCode", null); //$NON-NLS-1$
        messageMap.put("message", null); //$NON-NLS-1$

        Document doc = Util.parse(outputMessage);
        String xpath = "//report/message"; //$NON-NLS-1$
        NodeList checkList = com.amalto.core.util.Util.getNodeList(doc, xpath);
        if (checkList != null && checkList.getLength() > 0) {
            Node messageNode = checkList.item(0);
            if (messageNode instanceof Element) {
                Element messageElement = (Element) messageNode;
                messageMap.put("typeCode", messageElement.getAttribute("type")); //$NON-NLS-1$ //$NON-NLS-2$
                NodeList childList = messageElement.getChildNodes();
                if (childList.getLength() == 1) {
                    Node contentNode = childList.item(0);
                    if (contentNode.getNodeType() == Node.TEXT_NODE) {
                        messageMap.put(
                                "message", MultilanguageMessageParser.pickOutISOMessage(contentNode.getTextContent(), language)); //$NON-NLS-1$
                    } else if (contentNode.getNodeType() == Node.ELEMENT_NODE) {
                        if (contentNode.getChildNodes().getLength() == 1
                                && contentNode.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
                            messageMap
                                    .put("message", MultilanguageMessageParser.pickOutISOMessage(contentNode.getTextContent(), language)); //$NON-NLS-1$
                        }
                    }
                }
            }
        }
        return messageMap;
    }

    public static void dynamicAssembleByResultOrder(ItemBean itemBean, List<String> viewableXpaths, EntityModel entityModel,
            Map<String, EntityModel> map, String language, boolean isStaging) throws Exception {

        if (itemBean.getItemXml() != null) {
            org.dom4j.Document docXml = DocumentHelper.parseText(itemBean.getItemXml());
            int i = 0;
            List<?> els = docXml.getRootElement().elements();
            for (String path : viewableXpaths) {
                String leafPath = path.substring(path.lastIndexOf('/') + 1);
                if (leafPath.startsWith("@")) { //$NON-NLS-1$
                    String[] xsiType = leafPath.substring(leafPath.indexOf("@") + 1).split(":"); //$NON-NLS-1$//$NON-NLS-2$
                    itemBean.set(
                            path,
                            docXml.getRootElement()
                                    .element(
                                            new QName(xsiType[1], new Namespace(xsiType[0],
                                                    "http://www.w3.org/2001/XMLSchema-instance"))).getText()); //$NON-NLS-1$
                } else {
                    TypeModel typeModel = entityModel.getMetaDataTypes().get(path);
                    org.dom4j.Element el = (org.dom4j.Element) els.get(i);
                    if (typeModel != null && typeModel.getForeignkey() != null) {
                        String modelType = el
                                .attributeValue(new QName("type", new Namespace("tmdm", "http://www.talend.com/mdm"))); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
                        itemBean.set(path, path + "-" + el.getText()); //$NON-NLS-1$
                        itemBean.setForeignkeyDesc(
                                path + "-" + el.getText(), //$NON-NLS-1$
                                org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getForeignKeyDesc(typeModel,
                                        el.getText(), false, modelType, map.get(typeModel.getXpath()), isStaging, language));
                    } else if (typeModel != null && DataTypeConstants.BOOLEAN.equals(typeModel.getType())) {
                        if (Constants.BOOLEAN_TRUE_DISPLAY_VALUE.equals(el.getText())
                                || Constants.BOOLEAN_TRUE_VALUE.equals(el.getText())) {
                            itemBean.set(path, Constants.BOOLEAN_TRUE_VALUE);
                        } else if (Constants.BOOLEAN_FALSE_DISPLAY_VALUE.equals(el.getText())
                                || Constants.BOOLEAN_FALSE_VALUE.equals(el.getText())) {
                            itemBean.set(path, Constants.BOOLEAN_FALSE_VALUE);
                        } else {
                            itemBean.set(path, el.getText());
                        }
                    } else {
                        itemBean.set(path, el.getText());
                    }
                }
                i++;
            }
        }
    }

    public static ForeignKeyBean getForeignKeyDesc(TypeModel model, String ids, boolean isNeedExceptionMessage, String modelType,
            EntityModel entityModel, boolean isStaging, String language) throws Exception {
        String xpathForeignKey = model.getForeignkey();
        if (xpathForeignKey == null) {
            return null;
        }
        if (ids == null || ids.trim().length() == 0) {
            return null;
        }

        ForeignKeyBean bean = new ForeignKeyBean();
        bean.setId(ids);
        bean.setForeignKeyPath(model.getXpath());
        try {
            if (!model.isRetrieveFKinfos()) {
                return bean;
            } else {
                ItemPOJOPK pk = new ItemPOJOPK();
                String[] itemId = org.talend.mdm.webapp.base.server.util.CommonUtil.extractFKRefValue(ids, language);
                pk.setIds(itemId);
                String conceptName = model.getForeignkey().split("/")[0]; //$NON-NLS-1$
                // get deriveType's conceptName, otherwise getItem() method will throw exception.
                if (modelType != null && modelType.trim().length() > 0) {
                    conceptName = modelType;
                    bean.setConceptName(conceptName);
                }
                pk.setConceptName(conceptName);
                String cluster = getCurrentDataCluster(isStaging);
                pk.setDataClusterPOJOPK(new DataClusterPOJOPK(cluster));
                ItemPOJO item = com.amalto.core.util.Util.getItemCtrl2Local().getItem(pk);

                if (item != null) {
                    org.w3c.dom.Document document = item.getProjection().getOwnerDocument();
                    List<String> foreignKeyInfo = model.getForeignKeyInfo();
                    String formattedId = ""; // Id formatted using foreign key info //$NON-NLS-1$
                    for (String foreignKeyPath : foreignKeyInfo) {
                        NodeList nodes = com.amalto.core.util.Util.getNodeList(document,
                                StringUtils.substringAfter(foreignKeyPath, "/")); //$NON-NLS-1$
                        if (nodes.getLength() == 1) {
                            String value = nodes.item(0).getTextContent();
                            TypeModel typeModel = entityModel.getTypeModel(foreignKeyPath);
                            if (typeModel != null) {
                                if (typeModel.getForeignKeyInfo() != null && typeModel.getForeignKeyInfo().size() > 0
                                        && !"".equals(value)) { //$NON-NLS-1$
                                    value = ForeignKeyHelper.getDisplayValue(value, foreignKeyPath, getCurrentDataCluster(isStaging),
                                            entityModel, language);
                                }

                                if (typeModel.getType().equals(DataTypeConstants.MLS)) {
                                    value = MultilanguageMessageParser.getValueByLanguage(value, language);
                                }
                            }
                            bean.getForeignKeyInfo().put(foreignKeyPath, value);
                            if (formattedId.equals("")) { //$NON-NLS-1$
                                formattedId += value;
                            } else {
                                formattedId += "-" + value; //$NON-NLS-1$
                            }
                        }
                    }

                    if (model.getForeignKeyInfoFormat() != null && model.getForeignKeyInfoFormat().length() > 0) {
                        ForeignKeyHelper.convertFKInfo2DisplayInfo(bean, model);
                    } else {
                        bean.setDisplayInfo(formattedId);
                    }

                    return bean;
                } else {
                    return null;
                }
            }
        } catch (EntityNotFoundException e) {
            if (!isNeedExceptionMessage) {
                return null;
            }
            // fix bug TMDM-2757
            bean.set("foreignKeyDeleteMessage", e.getMessage()); //$NON-NLS-1$
            return bean;
        }
    }

    public static String getPKInfos(List<String> xPathList) {
        StringBuilder gettedValue = new StringBuilder();
        for (String pkInfo : xPathList) {
            if (pkInfo != null) {
                if (gettedValue.length() == 0) {
                    gettedValue.append(pkInfo);
                } else {
                    gettedValue.append("-").append(pkInfo); //$NON-NLS-1$
                }
            }
        }
        return gettedValue.toString();
    }

    public static List<String> getPKInfoList(EntityModel entityModel, TypeModel model, ItemBean itemBean, Document document,
            String language) throws Exception {
        List<String> xpathPKInfos = model.getPrimaryKeyInfo();
        List<String> xPathList = new ArrayList<String>();
        if (xpathPKInfos != null && xpathPKInfos.size() > 0 && itemBean.getIds() != null) {
            for (String pkInfoPath : xpathPKInfos) {
                if (pkInfoPath != null && pkInfoPath.length() > 0) {
                    String pkInfo = com.amalto.core.util.Util.getFirstTextNode(document, pkInfoPath);
                    if (pkInfo != null) {
                        if (entityModel.getTypeModel(pkInfoPath).getType().equals(DataTypeConstants.MLS)) {
                            String value = MultilanguageMessageParser.getValueByLanguage(pkInfo, language);
                            if (value != null) {
                                xPathList.add(value);
                            }
                        } else if (entityModel.getTypeModel(pkInfoPath).getForeignkey() != null) {
                            ForeignKeyBean fkBean = itemBean.getForeignkeyDesc(pkInfoPath + "-" + pkInfo); //$NON-NLS-1$
                            if (fkBean != null) {
                                xPathList.add(fkBean.toString());
                            }
                        } else {
                            xPathList.add(pkInfo);
                        }
                    }
                }
            }
        } else {
            xPathList.add(model.getLabel(language));
        }
        return xPathList;
    }

    public static void migrationMultiLingualFieldValue(ItemBean itemBean, TypeModel typeModel, Node node, String path,
            boolean isMultiOccurence, ItemNodeModel nodeModel) {
        String value = node.getTextContent();
        if (typeModel != null && typeModel.getType().equals(DataTypeConstants.MLS)
                && BrowseRecordsConfiguration.dataMigrationMultiLingualFieldAuto()) {
            if (value != null && value.trim().length() > 0) {
                if (!MultilanguageMessageParser.isExistMultiLanguageFormat(value)) {
                    String defaultLanguage = com.amalto.core.util.Util.getDefaultSystemLocale();
                    String newValue = MultilanguageMessageParser.getFormatValueByDefaultLanguage(value,
                            defaultLanguage != null ? defaultLanguage : "en");//$NON-NLS-1$
                    if (nodeModel == null) {
                        if (isMultiOccurence) {
                            node.setTextContent(newValue);
                        } else {
                            itemBean.set(path, newValue);
                        }
                    } else {
                        nodeModel.setObjectValue(newValue);
                    }
                } else if (nodeModel != null) {
                    nodeModel.setObjectValue(FormatUtil.multiLanguageEncode(value));
                }
            }
        }
    }

    public static EntityModel getEntityModel(String concept, String language) throws Exception {
        // bind entity model
        String model = getCurrentDataModel();
        EntityModel entityModel = new EntityModel();
        DataModelHelper.parseSchema(model, concept, entityModel, LocalUser.getLocalUser().getRoles());
        return entityModel;
    }

    public static String getCurrentDataCluster() throws Exception {
        Configuration config = Configuration.getConfiguration();
        return config.getCluster();
    }

    public static String getCurrentDataCluster(boolean isStaging) throws Exception {
        String cluster = getCurrentDataCluster();
        if (isStaging) {
            if (cluster != null && !cluster.endsWith(StorageAdmin.STAGING_SUFFIX)) {
                cluster += StorageAdmin.STAGING_SUFFIX;
            }
        }
        return cluster;
    }

    public static String getCurrentDataModel() throws Exception {
        Configuration config = Configuration.getConfiguration();
        return config.getModel();
    }

    public static Map<String, Object> formatQuerylValue(Map<String, String[]> formatMap,  org.dom4j.Document doc, EntityModel entityModel, String concept) throws Exception{
        String dateFormat = "yyyy-MM-dd"; //$NON-NLS-1$
        String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss"; //$NON-NLS-1$
        Set<String> keySet = formatMap.keySet();
        Map<String, Object> originalMap = new HashMap<String, Object>();
        Map<String, String> formateValueMap = new HashMap<String, String>();

        Map<String, Object> returnValue = new HashMap<String, Object>();
        returnValue.put(RESULT, doc);
        returnValue.put(FORMATE_VALUE, formateValueMap);
        returnValue.put(ORIGINAL_VALUE, originalMap);

        if (formatMap.isEmpty()) {
            return returnValue;
        }

        for (String key : keySet) {
            String[] value = formatMap.get(key);
            TypeModel tm = entityModel.getMetaDataTypes().get(key);
            String xpath = tm.getXpath();
            String dataText = null;
            org.dom4j.Node node = null;
            if (!key.equals(xpath)) {
                Namespace namespace = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$//$NON-NLS-2$
                List<?> nodeList = doc.selectNodes(xpath);
                if (nodeList != null && nodeList.size() > 0) {
                    for (int i = 0; i < nodeList.size(); i++) {
                        org.dom4j.Element current = (org.dom4j.Element) nodeList.get(i);
                        String realType = current.getParent().attributeValue(new QName("type", namespace, "xsi:type")); //$NON-NLS-1$ //$NON-NLS-2$
                        if (key.replaceAll(":" + realType, "").equals(xpath)) { //$NON-NLS-1$//$NON-NLS-2$
                            node = current;
                            break;
                        }
                    }
                }
            } else {
                node = doc.selectSingleNode(key);
            }

            if (node == null) {
                node = doc.selectSingleNode("result/" + key.substring(key.lastIndexOf('/') + 1));
            }
            if (node != null) {
                dataText = node.getText();
            }
            if (dataText != null) {
                if (dataText.trim().length() != 0) {
                    if (dateTypeNames.contains(tm.getType().getBaseTypeName())) {
                        SimpleDateFormat sdf = null;
                        if (value[1].equalsIgnoreCase(DataTypeConstants.DATE.getBaseTypeName())) {
                            sdf = new SimpleDateFormat(dateFormat, java.util.Locale.ENGLISH);
                        } else if (value[1].equalsIgnoreCase(DataTypeConstants.DATETIME.getBaseTypeName())) {
                            sdf = new SimpleDateFormat(dateTimeFormat, java.util.Locale.ENGLISH);
                        }

                        try {
                            Date date = sdf.parse(dataText.trim());
                            originalMap.put(key, date);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            String formatValue = com.amalto.webapp.core.util.Util.formatDate(value[0], calendar);
                            formateValueMap.put(key, formatValue);
                            node.setText(formatValue);
                        } catch (Exception e) {
                            originalMap.remove(key);
                            formateValueMap.remove(key);
                        }
                    } else if (numberTypeNames.contains(tm.getType().getBaseTypeName())) {
                        try {
                            NumberFormat nf = NumberFormat.getInstance();
                            Number num = nf.parse(dataText.trim());
                            String formatValue = ""; //$NON-NLS-1$
                            if (tm.getType().getBaseTypeName().equals(DataTypeConstants.DOUBLE.getBaseTypeName())) {
                                formatValue = String.format(value[0], num.doubleValue()).trim();
                            } else if (tm.getType().getBaseTypeName().equals(DataTypeConstants.FLOAT.getBaseTypeName())) {
                                formatValue = String.format(value[0], num.floatValue()).trim();
                            } else if (tm.getType().getBaseTypeName().equals(DataTypeConstants.DECIMAL.getBaseTypeName())) {
                                formatValue = String.format(value[0], new BigDecimal(dataText.trim())).trim();
                            } else {
                                formatValue = String.format(value[0], num).trim();
                            }

                            originalMap.put(key, num);
                            formateValueMap.put(key, formatValue);
                            node.setText(formatValue);
                        } catch (Exception e) {
                            Log.info("format has error 111"); //$NON-NLS-1$
                            originalMap.remove(key);
                            formateValueMap.remove(key);
                        }
                    } else if (DataTypeConstants.STRING.getBaseTypeName().equalsIgnoreCase(tm.getType().getBaseTypeName())) {
                        try {
                            String formatValue = String.format(value[0], dataText).trim();
                            originalMap.put(key, dataText);
                            formateValueMap.put(key, formatValue);
                            node.setText(formatValue);
                        } catch (Exception e) {
                            Log.info("format has error 111"); //$NON-NLS-1$
                            originalMap.remove(key);
                            formateValueMap.remove(key);
                        }
                    }
                }
            }
        }
        return returnValue;
    }

    public static Document parseResultDocument(String result, String expectedRootElement) throws Exception {
        Document doc = Util.parse(result);
        Element rootElement = doc.getDocumentElement();
        if (!rootElement.getNodeName().equals(expectedRootElement)) {
            // When there is a null value in fields, the viewable fields sequence is not enclosed by expected element
            // FIXME Better to find out a solution at the underlying stage
            doc.removeChild(rootElement);
            Element resultElement = doc.createElement(expectedRootElement);
            resultElement.appendChild(rootElement);
        }
        return doc;
    }

    public static Map<String, String[]> checkDisplayFormat(EntityModel entityModel, String language) {
        Map<String, TypeModel> metaData = entityModel.getMetaDataTypes();
        Map<String, String[]> formatMap = new HashMap<String, String[]>();
        String languageStr = "format_" + language.toLowerCase(); //$NON-NLS-1$
        if (metaData == null) {
            return formatMap;
        }

        Set<String> keySet = metaData.keySet();
        for (String key : keySet) {
            TypeModel typeModel = metaData.get(key);
            if (CommonUtil.dateTypeNames.contains(typeModel.getType().getBaseTypeName())
                    || CommonUtil.numberTypeNames.contains(typeModel.getType().getBaseTypeName())
                    || DataTypeConstants.STRING.getBaseTypeName().equalsIgnoreCase(typeModel.getType().getBaseTypeName())) {
                if (typeModel.getDisplayFomats() != null && typeModel.getDisplayFomats().size() > 0) {
                    if (typeModel.getDisplayFomats().containsKey(languageStr)) {
                        formatMap.put(key, new String[] { typeModel.getDisplayFomats().get(languageStr),
                                typeModel.getType().getBaseTypeName() });
                    }
                }
            }

        }
        return formatMap;
    }
}
