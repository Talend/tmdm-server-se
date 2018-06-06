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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.webapp.base.server.util.CommonUtil;
import org.talend.mdm.webapp.base.server.util.XmlUtil;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.client.util.LabelUtil;
import org.talend.mdm.webapp.browserecords.shared.Constants;

import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSGetItem;
import com.amalto.core.webservice.WSGetView;
import com.amalto.core.webservice.WSItem;
import com.amalto.core.webservice.WSItemPK;
import com.amalto.core.webservice.WSStringPredicate;
import com.amalto.core.webservice.WSView;
import com.amalto.core.webservice.WSViewPK;
import com.amalto.core.webservice.WSViewSearch;
import com.amalto.core.webservice.WSWhereAnd;
import com.amalto.core.webservice.WSWhereCondition;
import com.amalto.core.webservice.WSWhereItem;
import com.amalto.core.webservice.WSWhereOperator;
import com.amalto.core.webservice.WSWhereOr;

@SuppressWarnings("nls")
public abstract class DownloadWriter {

    private static final int FETCH_SIZE = 100;

    private EntityModel entity;

    private String concept;

    private List<String> idsList;

    private String viewPk;

    protected String[] headerArray;

    protected String[] xpathArray;

    private String criteria;

    private List<String> results;

    private String multipleValueSeparator;

    protected int columnIndex;

    private String fkDisplay;

    private boolean fkResovled;

    private Map<String, String> colFkMap;

    private Map<String, List<String>> fkMap;

    private String language;

    private int defaultMaxExportCount;

    private boolean isStaging;

    public DownloadWriter(String concept, String viewPk, List<String> idsList, String[] headerArray, String[] xpathArray,
            String criteria, String multipleValueSeparator, String fkDisplay, boolean fkResovled, Map<String, String> colFkMap,
            Map<String, List<String>> fkMap, boolean isStaging, String language) {
        this.concept = concept;
        this.viewPk = viewPk;
        this.idsList = idsList;
        this.headerArray = headerArray;
        this.xpathArray = xpathArray;
        this.criteria = criteria;
        this.multipleValueSeparator = multipleValueSeparator;
        this.fkDisplay = fkDisplay;
        this.fkResovled = fkResovled;
        this.colFkMap = colFkMap;
        this.fkMap = fkMap;
        this.isStaging = isStaging;
        this.language = language;
        this.defaultMaxExportCount = Integer.parseInt(
                MDMConfiguration.getConfiguration().getProperty("max.export.browserecord", MDMConfiguration.MAX_EXPORT_COUNT));
    }

    abstract void writeHeader();

    abstract void generateLine() throws Exception;

    abstract void writeValue(String value);

    public abstract void write(OutputStream out) throws IOException;

    public void writeFile() throws Exception {
        generateFile();
        writeHeader();
        generateResult();
        writeContent();
    }

    protected void generateFile() {
    }

    private void generateResult() throws Exception {
        WSViewPK wsViewPK = new WSViewPK(viewPk);
        WSView wsView = CommonUtil.getPort().getView(new WSGetView(wsViewPK));
        results = new LinkedList<String>();
        entity = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getEntityModel(concept, language);
        if (idsList != null && idsList.size() > defaultMaxExportCount) {
            idsList = idsList.subList(0, defaultMaxExportCount);
        }

        String[] result = null;
        if (idsList != null && idsList.size() > FETCH_SIZE) {
            for (int i = 0; i < idsList.size(); i = i + FETCH_SIZE) {
                int toIndex = i + FETCH_SIZE;
                if (toIndex > idsList.size()) {
                    toIndex = idsList.size();
                }
                result = fetchResultWithIdList(wsViewPK, wsView, idsList.subList(i, toIndex));
                if (result.length > 1) {
                    results.addAll(Arrays.asList(Arrays.copyOfRange(result, 1, result.length)));
                }
            }
        } else {
            result = fetchResultWithIdList(wsViewPK, wsView, idsList);
            if (result.length > 1) {
                results = Arrays.asList(Arrays.copyOfRange(result, 1, result.length));
            }
        }
    }

    private void writeContent() throws Exception {
        for (int i = 0; i < results.size(); i++) {
            Document document = XmlUtil.parseText(results.get(i));
            generateLine();
            writeLine(document);
        }
    }

    private void writeLine(Document document) throws Exception {
        columnIndex = 0;
        Map<String, EntityModel> entityMaps = new HashMap<String, EntityModel>();
        for (String xpath : xpathArray) {
            String cellValue = null;
            String joinEntityCluster = xpath.substring(0, xpath.indexOf("/"));
            if (DownloadUtil.isJoinField(xpath, concept)) {
                cellValue = getNodeValue(document, xpath);
                if (fkResovled) {
                    cellValue = wrapFkResovledValue(xpath, cellValue);
                }

                if (!entityMaps.containsKey(joinEntityCluster)) {
                    entityMaps.put(joinEntityCluster, entity);
                }
            } else {
                cellValue = DownloadUtil.getJoinFieldValue(document, xpath, columnIndex);
                if (!entityMaps.containsKey(joinEntityCluster)) {
                    entityMaps.put(joinEntityCluster, org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                            .getEntityModel(joinEntityCluster, language));
                }
            }

            if (cellValue != null) {
                cellValue = cellValue.trim();
                cellValue = cellValue.replaceAll("__h", "");
                cellValue = cellValue.replaceAll("h__", "");
            } else {
                cellValue = ""; //$NON-NLS-1$
            }

            if (entity == null || entity.getTypeModel(xpath) == null) {
                return;
            }
            if (entity.getTypeModel(xpath).getMaxOccurs() != 1 && StringUtils.isNotEmpty(cellValue)
                    && multipleValueSeparator != null) {
                cellValue = cellValue.replace(",", multipleValueSeparator);
            }
            writeValue(cellValue);
            columnIndex++;
        }
    }

    private String[] fetchResultWithIdList(WSViewPK wsViewPK, WSView wsView, List<String> idsList) throws Exception {
        WSWhereCondition[] conditions = wsView.getWhereConditions();
        WSWhereItem wi = new WSWhereItem();
        WSWhereAnd whereAnd = new WSWhereAnd();
        List<WSWhereItem> itemArray = new ArrayList<WSWhereItem>();
        for (WSWhereCondition whereCondition : conditions) {
            WSWhereItem andWhereItem = new WSWhereItem();
            andWhereItem.setWhereCondition(whereCondition);
            itemArray.add(andWhereItem);
        }

        // This blank line is for excel file header
        if (idsList != null) {
            WSWhereItem idsWhereItem = new WSWhereItem();
            WSWhereOr idWhereOr = new WSWhereOr();
            List<WSWhereItem> idWhereItemArray = new ArrayList<WSWhereItem>();

            for (String ids : idsList) {
                WSWhereItem idWhereItem = new WSWhereItem();

                // if the composite primary key
                if (entity.getKeys().length > 1) {
                    WSWhereItem compositeIdWhereItems = new WSWhereItem();
                    WSWhereAnd compositeIdWhereand = new WSWhereAnd();
                    List<WSWhereItem> compositeIdWhereItemArray = new ArrayList<WSWhereItem>();
                    int i = 0;
                    for (String primaryKey : entity.getKeys()) {
                        WSWhereItem compositeIdWhereItem = new WSWhereItem();
                        compositeIdWhereItem.setWhereCondition(new WSWhereCondition(primaryKey, WSWhereOperator.EQUALS,
                                ids.split("\\.")[i++], WSStringPredicate.NONE, false));
                        compositeIdWhereItemArray.add(compositeIdWhereItem);
                    }
                    compositeIdWhereand.setWhereItems(
                            (WSWhereItem[]) compositeIdWhereItemArray.toArray(new WSWhereItem[compositeIdWhereItemArray.size()]));
                    compositeIdWhereItems.setWhereAnd(compositeIdWhereand);

                    idWhereItemArray.add(compositeIdWhereItems);
                } else {
                    idWhereItem.setWhereCondition(new WSWhereCondition(entity.getKeys()[0], WSWhereOperator.EQUALS, ids,
                            WSStringPredicate.NONE, false));
                    idWhereItemArray.add(idWhereItem);
                }

            }
            idWhereOr.setWhereItems((WSWhereItem[]) idWhereItemArray.toArray(new WSWhereItem[idWhereItemArray.size()]));
            idsWhereItem.setWhereOr(idWhereOr);

            itemArray.add(idsWhereItem);
            whereAnd.setWhereItems((WSWhereItem[]) itemArray.toArray(new WSWhereItem[itemArray.size()]));
            wi.setWhereAnd(whereAnd);
        } else {
            WSWhereItem criteriaWhereItem = criteria != null ? CommonUtil.buildWhereItems(criteria) : null;
            if (criteriaWhereItem != null) {
                itemArray.add(criteriaWhereItem);
            }
            whereAnd.setWhereItems((WSWhereItem[]) itemArray.toArray(new WSWhereItem[itemArray.size()]));
            wi.setWhereAnd(whereAnd);
        }

        String[] result = CommonUtil.getPort().viewSearch(new WSViewSearch(new WSDataClusterPK(getCurrentDataCluster()), wsViewPK,
                wi, -1, 0, defaultMaxExportCount, null, null)).getStrings();
        return result;
    }

    private String getNodeValue(Document document, String xpath) {
        List<?> selectNodes = null;
        Map<String, String> namespaceMap = new HashMap<String, String>();
        namespaceMap.put(Constants.XSI_PREFIX, Constants.XSI_URI);
        List<String> valueList = null;
        selectNodes = document.selectNodes(xpath);

        if (selectNodes == null || selectNodes.isEmpty()) {
            String str = xpath.substring(xpath.lastIndexOf("/") + 1, xpath.length());
            if (str.startsWith(Constants.FILE_EXPORT_IMPORT_SEPARATOR)) {
                str = str.replace(Constants.FILE_EXPORT_IMPORT_SEPARATOR, "");
            }
            selectNodes = document.getRootElement().selectNodes(str);
        }
        if (selectNodes != null) {
            valueList = new LinkedList<String>();
            for (Object object : selectNodes) {
                Element element = (Element) object;
                if (element.elements().size() > 0) {
                    valueList.add(element.asXML());
                } else {
                    valueList.add(element.getText());
                }
            }
        }

        if (valueList == null || valueList.size() == 0) {
            return "";
        }

        if (valueList.size() > 1) {
            return CommonUtil.joinStrings(valueList, multipleValueSeparator);
        } else {
            return valueList.get(0);
        }
    }

    private String wrapFkResovledValue(String xpath, String value) throws Exception {
        if (colFkMap.containsKey(xpath)) {
            List<String> fkinfoList = fkMap.get(xpath);
            if (!fkinfoList.get(0).trim().equalsIgnoreCase("") && !value.equalsIgnoreCase("")) {
                List<String> infoList = getFKInfo(colFkMap.get(xpath), fkinfoList, value);
                if (fkDisplay.equalsIgnoreCase("Id-FKInfo")) {
                    infoList.add(0, value);
                }
                if (multipleValueSeparator != null && multipleValueSeparator.length() > 0) {
                    value = LabelUtil.convertList2String(infoList, multipleValueSeparator);
                } else {
                    value = LabelUtil.convertList2String(infoList, "-");
                }
            }
        }
        return value;
    }

    private List<String> getFKInfo(String fk, List<String> fkInfoList, String fkValue) throws Exception {
        List<String> infoList = new ArrayList<String>();
        String conceptName = fk.substring(0, fk.indexOf("/"));
        String value = LabelUtil.removeBrackets(fkValue);
        String ids[] = { value };
        WSItem wsItem = CommonUtil.getPort()
                .getItem(new WSGetItem(new WSItemPK(new WSDataClusterPK(getCurrentDataCluster()), conceptName, ids)));
        Document doc = XmlUtil.parseText(wsItem.getContent());
        for (String xpath : fkInfoList) {
            infoList.add(XmlUtil.queryNodeText(doc, xpath));
        }
        return infoList;
    }

    public String generateFileName(String name) {
        return name + (isStaging ? Constants.STAGING_SUFFIX_NAME : "");
    }

    protected String getCurrentDataCluster() throws Exception {
        return org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getCurrentDataCluster(isStaging);
    }
}
