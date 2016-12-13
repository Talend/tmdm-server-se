/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.Element;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.webapp.base.server.util.CommonUtil;
import org.talend.mdm.webapp.base.server.util.XmlUtil;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.client.util.LabelUtil;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.ViewHelper;
import org.talend.mdm.webapp.browserecords.server.util.DownloadUtil;
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
import com.amalto.webapp.core.util.Util;

public class DownloadData extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(DownloadData.class);

    private static final long serialVersionUID = 1L;

    private int defaultMaxExportCount;

    private final String SHEET_LABEL = "Talend MDM"; //$NON-NLS-1$

    protected final String DOWNLOADFILE_EXTEND_NAME = ".xlsx"; //$NON-NLS-1$

    protected String fileName = ""; //$NON-NLS-1$

    private String multipleValueSeparator = null;

    protected String concept = ""; //$NON-NLS-1$

    private String viewPk;

    private boolean fkResovled = false;

    private String criteria = ""; //$NON-NLS-1$

    private String language = "en"; //$NON-NLS-1$

    private String fkDisplay = ""; //$NON-NLS-1$

    protected EntityModel entity = null;

    private String[] headerArray = null;

    protected String[] xpathArray = null;

    private List<String> idsList = null;

    private Map<String, String> colFkMap = null;

    private Map<String, List<String>> fkMap = null;

    protected int columnIndex = 0;

    protected XSSFCellStyle cs = null;

    @Override
    public void init() throws ServletException {
        super.init();
        defaultMaxExportCount = Integer.parseInt(MDMConfiguration.getConfiguration().getProperty("max.export.browserecord",
                MDMConfiguration.MAX_EXPORT_COUNT));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        XSSFWorkbook workbook = new XSSFWorkbook();
        cs = workbook.createCellStyle();
        XSSFFont f = workbook.createFont();
        f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        cs.setFont(f);
        XSSFSheet sheet = workbook.createSheet(SHEET_LABEL);
        sheet.setDefaultColumnWidth((short) 20);
        XSSFRow row = sheet.createRow((short) 0);
        try {
            setParameter(request);
            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); //$NON-NLS-1$
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            fillHeader(row);
            fillSheet(sheet);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        OutputStream out = response.getOutputStream();
        workbook.write(out);
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    private void setParameter(HttpServletRequest request) throws Exception {
        viewPk = request.getParameter("tableName"); //$NON-NLS-1$
        generateFileName(new String(request.getParameter("fileName").getBytes("iso-8859-1"), "UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String header = new String(request.getParameter("header").getBytes("iso-8859-1"), "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$        
        String xpath = request.getParameter("xpath"); //$NON-NLS-1$
        fkResovled = Boolean.valueOf(request.getParameter("fkResovled")); //$NON-NLS-1$
        if (fkResovled) {
            colFkMap = new HashMap<String, String>();
            fkMap = new HashMap<String, List<String>>();
            String fkColXPath = request.getParameter("fkColXPath"); //$NON-NLS-1$
            String fkInfo = request.getParameter("fkInfo"); //$NON-NLS-1$
            DownloadUtil.assembleFkMap(colFkMap, fkMap, fkColXPath, fkInfo);
        }
        concept = ViewHelper.getConceptFromDefaultViewName(viewPk);
        headerArray = DownloadUtil.convertXml2Array(header, "item"); //$NON-NLS-1$
        xpathArray = DownloadUtil.convertXml2Array(xpath, "item"); //$NON-NLS-1$
        criteria = request.getParameter("criteria"); //$NON-NLS-1$
        language = request.getParameter("language"); //$NON-NLS-1$
        fkDisplay = request.getParameter("fkDisplay"); //$NON-NLS-1$
        multipleValueSeparator = request.getParameter("multipleValueSeparator"); //$NON-NLS-1$

        if (request.getParameter("itemIdsListString") != null && !request.getParameter("itemIdsListString").isEmpty()) { //$NON-NLS-1$ //$NON-NLS-2$
            idsList = org.talend.mdm.webapp.base.shared.util.CommonUtil.convertStrigToList(
                    request.getParameter("itemIdsListString"), Constants.FILE_EXPORT_IMPORT_SEPARATOR); //$NON-NLS-1$
        } else {
            idsList = null;
        }
    }

    protected void fillHeader(XSSFRow row) {

        for (int i = 0; i < headerArray.length; i++) {
            XSSFCell cell = row.createCell((short) i);
            cell.setCellValue(headerArray[i]);
            cell.setCellStyle(cs);
        }
    }

    protected void fillSheet(XSSFSheet sheet) throws Exception {
        entity = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getEntityModel(concept, language);
        List<String> results = new LinkedList<String>();

        WSViewPK wsViewPK = new WSViewPK(viewPk);
        WSView wsView = CommonUtil.getPort().getView(new WSGetView(wsViewPK));

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
            if (idsList.size() > defaultMaxExportCount) {
                idsList.subList(0, defaultMaxExportCount);
            }

            for (String ids : idsList) {
                WSWhereItem idWhereItem = new WSWhereItem();

                //if the composite primary key
                if (entity.getKeys().length > 1) {
                    WSWhereItem compositeIdWhereItems = new WSWhereItem();
                    WSWhereAnd compositeIdWhereand = new WSWhereAnd();
                    List<WSWhereItem> compositeIdWhereItemArray = new ArrayList<WSWhereItem>();
                    int i = 0;
                    for (String primaryKey : entity.getKeys()) {
                        WSWhereItem compositeIdWhereItem = new WSWhereItem();
                        compositeIdWhereItem.setWhereCondition(new WSWhereCondition(primaryKey, WSWhereOperator.EQUALS, ids
                                .split("\\.")[i++], WSStringPredicate.NONE, false));
                        compositeIdWhereItemArray.add(compositeIdWhereItem);
                    }
                    compositeIdWhereand.setWhereItems((WSWhereItem[]) compositeIdWhereItemArray
                            .toArray(new WSWhereItem[compositeIdWhereItemArray.size()]));
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

        String[] result = CommonUtil
                .getPort()
                .viewSearch(
                        new WSViewSearch(new WSDataClusterPK(getCurrentDataCluster()), wsViewPK, wi, -1, 0, defaultMaxExportCount, null,
                                null)).getStrings();
        if (result.length > 1) {
            results = Arrays.asList(Arrays.copyOfRange(result, 1, result.length));
        }
        for (int i = 0; i < results.size(); i++) {
            Document document = XmlUtil.parseText(results.get(i));
            XSSFRow row = sheet.createRow(i + 1);
            fillRow(row, document);
        }
    }


    protected void fillRow(XSSFRow row, Document document) throws Exception {
        columnIndex = 0;
        for (String xpath : xpathArray) {
            String tmp = null;
            if (DownloadUtil.isJoinField(xpath, concept)) {
                tmp = getNodeValue(document, xpath);
                if (fkResovled) {
                    if (colFkMap.containsKey(xpath)) {
                        List<String> fkinfoList = fkMap.get(xpath);
                        if (!fkinfoList.get(0).trim().equalsIgnoreCase("") && !tmp.equalsIgnoreCase("")) { //$NON-NLS-1$ //$NON-NLS-2$
                            List<String> infoList = getFKInfo(colFkMap.get(xpath), fkinfoList, tmp);
                            if (fkDisplay.equalsIgnoreCase("Id-FKInfo")) { //$NON-NLS-1$
                                infoList.add(0, tmp);
                            }
                            if (multipleValueSeparator != null && multipleValueSeparator.length() > 0) {
                                tmp = LabelUtil.convertList2String(infoList, multipleValueSeparator);
                            } else {
                                tmp = LabelUtil.convertList2String(infoList, "-"); //$NON-NLS-1$
                            }
                        }
                    }
                }
            } else {
                tmp = DownloadUtil.getJoinFieldValue(document, xpath, columnIndex);
            }

            if (tmp != null) {
                tmp = tmp.trim();
                tmp = tmp.replaceAll("__h", ""); //$NON-NLS-1$ //$NON-NLS-2$
                tmp = tmp.replaceAll("h__", ""); //$NON-NLS-1$//$NON-NLS-2$
            } else {
                tmp = ""; //$NON-NLS-1$
            }
            if (entity != null && entity.getTypeModel(xpath) != null) {
                if (entity.getTypeModel(xpath).getMaxOccurs() != 1 && StringUtils.isNotEmpty(tmp) && multipleValueSeparator != null) {
                    row.createCell((short) columnIndex).setCellValue(tmp.replace(",", multipleValueSeparator)); //$NON-NLS-1$
                } else {
                    row.createCell((short) columnIndex).setCellValue(tmp);
                }
                columnIndex++;
            } else {
                continue;
            }
        }
    }

    protected String getNodeValue(Document document, String xpath) {
        List<?> selectNodes = null;
        Map<String, String> namespaceMap = new HashMap<String, String>();
        namespaceMap.put(Constants.XSI_PREFIX, Constants.XSI_URI);
        List<String> valueList = null;
        selectNodes = document.selectNodes(xpath);

        if(selectNodes == null || selectNodes.isEmpty()){
            String str = xpath.substring(xpath.lastIndexOf("/") + 1, xpath.length()); //$NON-NLS-1$
            if(str.startsWith(Constants.FILE_EXPORT_IMPORT_SEPARATOR)){
                str = str.replace(Constants.FILE_EXPORT_IMPORT_SEPARATOR, ""); //$NON-NLS-1$
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
            return ""; //$NON-NLS-1$
        } else {
            if (valueList.size() > 1) {
                return CommonUtil.joinStrings(valueList, multipleValueSeparator);
            } else {
                return valueList.get(0);
            }
        }
    }

    private List<String> getFKInfo(String fk, List<String> fkInfoList, String fkValue) throws Exception {
        List<String> infoList = new ArrayList<String>();
        String conceptName = fk.substring(0, fk.indexOf("/")); //$NON-NLS-1$
        String value = LabelUtil.removeBrackets(fkValue);
        String ids[] = { value };
        WSItem wsItem = Util.getPort().getItem(
                new WSGetItem(new WSItemPK(new WSDataClusterPK(getCurrentDataCluster()), conceptName, ids)));
        Document doc = XmlUtil.parseText(wsItem.getContent());
        for (String xpath : fkInfoList) {
            infoList.add(XmlUtil.queryNodeText(doc, xpath));
        }
        return infoList;
    }

    protected void generateFileName(String name) {
        fileName = name + DOWNLOADFILE_EXTEND_NAME;
    }

    protected String getCurrentDataCluster() throws Exception {
        return org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getCurrentDataCluster(false);
    }
}
