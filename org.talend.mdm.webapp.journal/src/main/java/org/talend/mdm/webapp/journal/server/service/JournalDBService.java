/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.server.service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.io.SAXReader;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.server.util.Constants;
import org.talend.mdm.webapp.base.server.util.DateUtil;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.server.util.CommonUtil;
import org.talend.mdm.webapp.journal.server.LocalLabelTransformer;
import org.talend.mdm.webapp.journal.shared.JournalGridModel;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;
import org.talend.mdm.webapp.journal.shared.JournalTreeModel;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.allen_sauer.gwt.log.client.Log;
import com.amalto.core.util.Util;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSGetItem;
import com.amalto.core.webservice.WSItem;
import com.amalto.core.webservice.WSItemPK;
import com.amalto.core.webservice.WSStringArray;
import com.amalto.core.webservice.WSWhereItem;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.sun.xml.xsom.XSElementDecl;

/**
 * The server side implementation of the RPC service.
 */
public class JournalDBService {

    private static final String LEFT_BRACKET = "[";

    private static final String RIGHT_BRACKET = "]";

    private static final String SPRIT = "/";

    private static final Logger LOG = Logger.getLogger(JournalDBService.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

    private WebService webService;

    public JournalDBService(WebService webService) {
        this.webService = webService;
    }

    public Object[] getResultListByCriteria(JournalSearchCriteria criteria, int start, int limit, String sort, String field)
            throws Exception {
        List<WSWhereItem> conditions = org.talend.mdm.webapp.journal.server.util.Util.buildWhereItems(criteria, webService);

        int totalSize = 0;
        List<JournalGridModel> list = new ArrayList<JournalGridModel>();
        String sortDir = null;
        if (SortDir.ASC.equals(SortDir.findDir(sort))) {
            sortDir = Constants.SEARCH_DIRECTION_ASC;
        } else if (SortDir.DESC.equals(SortDir.findDir(sort))) {
            sortDir = Constants.SEARCH_DIRECTION_DESC;
        }
        WSStringArray resultsArray = webService.getItemsBySort(org.talend.mdm.webapp.journal.server.util.Util.buildGetItemsSort(
                conditions, start, limit, field, sortDir));
        String[] results = resultsArray == null ? new String[0] : resultsArray.getStrings();
        Document document = Util.parse(results[0]);
        totalSize = Integer.parseInt(document.getDocumentElement().getTextContent());

        for (int i = 1; i < results.length; i++) {
            String result = results[i];
            JournalGridModel journalGridModel = parseString2Model(result);
            list.add(journalGridModel);
        }

        Object[] resArr = new Object[2];
        resArr[0] = totalSize;
        resArr[1] = list;
        return resArr;
    }

    public JournalTreeModel getDetailTreeModel(String[] idsArr,EntityModel entityModel, String language) throws Exception {
        WSDataClusterPK wsDataClusterPK = new WSDataClusterPK(XSystemObjects.DC_UPDATE_PREPORT.getName());
        String conceptName = "Update"; //$NON-NLS-1$
        WSGetItem wsGetItem = new WSGetItem(new WSItemPK(wsDataClusterPK, conceptName, idsArr));
        WSItem wsItem = webService.getItem(wsGetItem);
        String content = wsItem.getContent();

        Map<String, String[]> formatMap = CommonUtil.checkDisplayFormat(entityModel, language);

        JournalTreeModel root = new JournalTreeModel("Update"); //$NON-NLS-1$

        if (content == null) {
            return root;
        }

        if (content.length() == 0) {
            return root;
        }

        Document doc = Util.parse(content);
        String concept = Util.getFirstTextNode(doc, "/Update/Concept"); //$NON-NLS-1$
        String dataModel = Util.getFirstTextNode(doc, "/Update/DataModel"); //$NON-NLS-1$
        String dataCluster = Util.getFirstTextNode(doc, "/Update/DataCluster"); //$NON-NLS-1$

        root.add(new JournalTreeModel("UserName:" + checkNull(Util.getFirstTextNode(doc, "/Update/UserName")))); //$NON-NLS-1$ //$NON-NLS-2$
        root.add(new JournalTreeModel("Source:" + checkNull(Util.getFirstTextNode(doc, "/Update/Source")))); //$NON-NLS-1$ //$NON-NLS-2$
        root.add(new JournalTreeModel("TimeInMillis:" + checkNull(Util.getFirstTextNode(doc, "/Update/TimeInMillis")))); //$NON-NLS-1$ //$NON-NLS-2$
        root.add(new JournalTreeModel("OperationType:" + checkNull(Util.getFirstTextNode(doc, "/Update/OperationType")))); //$NON-NLS-1$ //$NON-NLS-2$
        root.add(new JournalTreeModel("Concept:" + checkNull(concept))); //$NON-NLS-1$
        root.add(new JournalTreeModel("DataCluster:" + checkNull(dataCluster))); //$NON-NLS-1$
        root.add(new JournalTreeModel("DataModel:" + checkNull(dataModel))); //$NON-NLS-1$
        root.add(new JournalTreeModel("Key:" + checkNull(Util.getFirstTextNode(doc, "/Update/Key")))); //$NON-NLS-1$ //$NON-NLS-2$                       

        XSElementDecl decl = webService.getXSElementDecl(dataModel, concept);

        // TODO Way better another solution
        boolean isAuth = !com.amalto.webapp.core.util.Util.isElementHiddenForCurrentUser(decl);
        root.setAuth(isAuth);

        if (isAuth) {
            NodeList ls = com.amalto.core.util.Util.getNodeList(doc, "/Update/Item"); //$NON-NLS-1$
            if (ls.getLength() > 0) {
                for (int i = 0; i < ls.getLength(); i++) {
                    List<JournalTreeModel> list = new ArrayList<JournalTreeModel>();
                    String path = Util.getFirstTextNode(doc, "/Update/Item[" + (i + 1) + "]/path"); //$NON-NLS-1$//$NON-NLS-2$

                    String oldValue = checkNull(Util.getFirstTextNode(doc, "/Update/Item[" + (i + 1) + "]/oldValue")); //$NON-NLS-1$//$NON-NLS-2$
                    String newValue = checkNull(Util.getFirstTextNode(doc, "/Update/Item[" + (i + 1) + "]/newValue")); //$NON-NLS-1$ //$NON-NLS-2$
                    oldValue = formatValue(entityModel, formatMap, path, oldValue);
                    newValue = formatValue(entityModel, formatMap, path, newValue);

                    list.add(new JournalTreeModel("path:" + path)); //$NON-NLS-1$
                    list.add(new JournalTreeModel("oldValue:" + oldValue)); //$NON-NLS-1$
                    list.add(new JournalTreeModel("newValue:" + newValue)); //$NON-NLS-1$

                    JournalTreeModel itemModel = new JournalTreeModel("Item", list); //$NON-NLS-1$
                    root.add(itemModel);
                }
            }
        }

        return root;
    }

    private String formatValue(EntityModel entityModel, Map<String, String[]> formatMap, String path, String oldValue) {
        String formatValue = oldValue;
        for (Map.Entry<String, String[]> entry : formatMap.entrySet()) {
            String key = entry.getKey();
            if (!key.contains(path)) {
                continue;
            }

            String[] value = formatMap.get(key);
            TypeModel tm = entityModel.getMetaDataTypes().get(key);

            if (StringUtils.isNoneBlank(oldValue)) {
                if (oldValue.trim().length() != 0) {
                    if (CommonUtil.dateTypeNames.contains(tm.getType().getBaseTypeName())) {
                        SimpleDateFormat sdf = null;
                        if (value[1].equalsIgnoreCase(DataTypeConstants.DATE.getBaseTypeName())) {
                            sdf = new SimpleDateFormat(DateUtil.DATE_FORMAT, java.util.Locale.ENGLISH);
                        } else if (value[1].equalsIgnoreCase(DataTypeConstants.DATETIME.getBaseTypeName())) {
                            sdf = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT, java.util.Locale.ENGLISH);
                        }

                        try {
                            Date date = sdf.parse(oldValue.trim());
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            formatValue = com.amalto.webapp.core.util.Util.formatDate(value[0], calendar);
                        } catch (Exception e) {
                            Log.warn("Format '" + oldValue +"' to date gets error.", e); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    } else if (CommonUtil.numberTypeNames.contains(tm.getType().getBaseTypeName())) {
                        try {
                            NumberFormat nf = NumberFormat.getInstance();
                            Number num = nf.parse(oldValue.trim());
                            if (tm.getType().getBaseTypeName().equals(DataTypeConstants.DOUBLE.getBaseTypeName())) {
                                formatValue = String.format(value[0], num.doubleValue()).trim();
                            } else if (tm.getType().getBaseTypeName().equals(DataTypeConstants.FLOAT.getBaseTypeName())) {
                                formatValue = String.format(value[0], num.floatValue()).trim();
                            } else if (tm.getType().getBaseTypeName().equals(DataTypeConstants.DECIMAL.getBaseTypeName())) {
                                formatValue = String.format(value[0], new BigDecimal(oldValue.trim())).trim();
                            } else {
                                formatValue = String.format(value[0], num).trim();
                            }

                        } catch (Exception e) {
                            Log.warn("Format '" + oldValue +"' to number gets error.", e); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    } else if (DataTypeConstants.STRING.getBaseTypeName().equalsIgnoreCase(tm.getType().getBaseTypeName())) {
                        try {
                            formatValue = String.format(value[0], oldValue).trim();
                        } catch (Exception e) {
                            Log.warn("Format '" + oldValue +"' to string gets error.", e); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
            }
        }
        return formatValue;
    }

    public JournalTreeModel getComparisionTreeModel(String xmlString) {
        JournalTreeModel root = new JournalTreeModel("root", "Document", "root"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (xmlString == null || "".equals(xmlString)) { //$NON-NLS-1$
            return root;
        }

        SAXReader reader = new SAXReader();
        setProtectRule(reader);
        org.dom4j.Document document = null;
        try {
            document = reader.read(new ByteArrayInputStream(xmlString.getBytes("UTF-8"))); //$NON-NLS-1$
            org.dom4j.Element rootElement = document.getRootElement();
            this.retrieveElement(rootElement, root);
        } catch (Exception e) {
            LOG.error("Failed to use the new feature while initializing SAXReader.", e);
        }

        return root;
    }

    private static void setProtectRule(SAXReader reader) {
        try {
            reader.setFeature(MDMXMLUtils.FEATURE_DISALLOW_DOCTYPE, true);
            reader.setFeature(MDMXMLUtils.FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
            reader.setFeature(MDMXMLUtils.FEATURE_EXTERNAL_PARAM_ENTITIES, false);
        } catch (SAXException e) {
            throw new RuntimeException("Unable to initialize SAXReader", e);
        }
    }

    private void retrieveElement(org.dom4j.Element element, JournalTreeModel root) {
        List<?> list = element.elements();
        JournalTreeModel model = this.getModelByElement(element);
        root.add(model);
        if (list.size() == 0) {
            return;
        }

        for (Object obj : list) {
            org.dom4j.Element el = (org.dom4j.Element) obj;
            retrieveElement(el, model);
        }
    }

    private JournalTreeModel getModelByElement(org.dom4j.Element element) {
        JournalTreeModel model = null;
        Attribute idAttr = element.attribute("id"); //$NON-NLS-1$
        String id = null;
        if (idAttr != null) {
            id = idAttr.getValue();
        }

        Attribute clsAttr = element.attribute("cls"); //$NON-NLS-1$
        String cls = null;
        if (clsAttr != null) {
            cls = clsAttr.getValue();
        }

        String value = element.getText();
        if (!value.equalsIgnoreCase("")) { //$NON-NLS-1$
            value = ":" + value; //$NON-NLS-1$
        }

        if (cls == null) {
            model = new JournalTreeModel(id, element.attributeValue(LocalLabelTransformer.LABEL) + value, element.getUniquePath());
        } else {
            model = new JournalTreeModel(id, element.attributeValue(LocalLabelTransformer.LABEL) + value, element.getUniquePath(), cls);
        }
        return model;
    }

    private JournalGridModel parseString2Model(String xmlStr) throws Exception {
        JournalGridModel model = new JournalGridModel();
        Document doc = Util.parse(xmlStr);
        String source = checkNull(Util.getFirstTextNode(doc, "result/Update/Source")); //$NON-NLS-1$
        String timeInMillis = checkNull(Util.getFirstTextNode(doc, "result/Update/TimeInMillis")); //$NON-NLS-1$

        model.setDataContainer(checkNull(Util.getFirstTextNode(doc, "result/Update/DataCluster"))); //$NON-NLS-1$
        model.setDataModel(checkNull(Util.getFirstTextNode(doc, "result/Update/DataModel"))); //$NON-NLS-1$
        model.setEntity(checkNull(Util.getFirstTextNode(doc, "result/Update/Concept"))); //$NON-NLS-1$
        model.setKey(checkNull(Util.getFirstTextNode(doc, "result/Update/Key"))); //$NON-NLS-1$
        model.setOperationType(checkNull(Util.getFirstTextNode(doc, "result/Update/OperationType"))); //$NON-NLS-1$
        model.setOperationTime(timeInMillis);
        model.setOperationDate(sdf.format(new Date(Long.parseLong(timeInMillis))));
        model.setSource(source);
        model.setUserName(checkNull(Util.getFirstTextNode(doc, "result/Update/UserName"))); //$NON-NLS-1$
        model.setIds(Util.joinStrings(new String[] { source, timeInMillis }, ".")); //$NON-NLS-1$

        String[] pathArray = Util.getTextNodes(doc, "result/Update/Item/path"); //$NON-NLS-1$

        List<String> changeNodeList = getChangeNodeList(model.getEntity(), pathArray);

        model.getChangeNodeList().addAll(changeNodeList);
        return model;
    }

    protected List<String> getChangeNodeList(String modelEntityName, String[] pathArray) {
        List<String> result = new ArrayList<String>();
        getChagneNodePath(StringUtils.EMPTY, Arrays.asList(pathArray), result);
        for(int i = 0 ; i < result.size() ; i++){
            result.set(i, SPRIT + modelEntityName + SPRIT + result.get(i));
        }
        return result;
    }

    private List<String> getContainsList(String str, List<String> list) {
        List<String> result = new ArrayList<String>();
        for (String path : list) {
            if (path.startsWith(str)) {
                result.add(path);
            }
        }
        return result;
    }

    private void getChagneNodePath(String prePath, List<String> pathList, List<String> result) {
        for (int i = 0; i < pathList.size(); i++) {
            String path = pathList.get(i);

            if (result.contains(path)) {
                continue;
            }
            if (i == pathList.size() - 1) {
                result.add(path);
                break;
            }

            String newpath = StringUtils.EMPTY; 
            if (prePath.equals(StringUtils.EMPTY)) { 
                newpath = path;
            } else {
                newpath = path.substring(prePath.length() + 1);
            }

            if (newpath.endsWith(LEFT_BRACKET) && !newpath.contains(SPRIT)) {
                result.add(path);
            } else if (!newpath.contains(LEFT_BRACKET) && !newpath.contains(SPRIT)) {
                result.add(path);
            } else if (newpath.contains(LEFT_BRACKET) || newpath.contains(SPRIT)) {
                String firstStr = StringUtils.EMPTY;
                if (newpath.contains(SPRIT)) {
                    firstStr = newpath.substring(0, newpath.indexOf(SPRIT));
                } else {
                    firstStr = newpath;
                }

                if (firstStr.contains(LEFT_BRACKET)) {
                    int firstStrNum = Integer.parseInt(firstStr.substring(firstStr.indexOf(LEFT_BRACKET) + 1, firstStr.indexOf(RIGHT_BRACKET)));
                    for (int j = 1; j <= firstStrNum; j++) {
                        String newStr = firstStr.substring(0, firstStr.indexOf(LEFT_BRACKET)) + LEFT_BRACKET + j + RIGHT_BRACKET;
                        if (i < pathList.size() - 1) {
                            List<String> containsList = getContainsList(prePath.equals(StringUtils.EMPTY) ? newStr : prePath + SPRIT + newStr,
                                    pathList);
                            getChagneNodePath(prePath.equals(StringUtils.EMPTY) ? newStr : prePath + SPRIT + newStr, containsList, result);
                        }
                    }
                } else {
                    List<String> containsList = getContainsList(prePath.equals(StringUtils.EMPTY) ? firstStr : prePath + SPRIT + firstStr,
                            pathList);
                    getChagneNodePath(prePath.equals(StringUtils.EMPTY) ? firstStr : prePath + SPRIT + firstStr, containsList, result);
                }
            }
        }

    }

    private String checkNull(String str) {
        if (str == null) {
            return ""; //$NON-NLS-1$
        }
        if (str.equalsIgnoreCase("null")) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        return str;
    }
}