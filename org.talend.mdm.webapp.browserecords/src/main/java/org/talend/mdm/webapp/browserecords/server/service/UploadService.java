/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.base.server.util.CommonUtil;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.server.exception.UploadException;
import org.talend.mdm.webapp.browserecords.server.util.CSVReader;
import org.talend.mdm.webapp.browserecords.shared.Constants;

import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSGetItem;
import com.amalto.core.webservice.WSItem;
import com.amalto.core.webservice.WSItemPK;
import com.amalto.core.webservice.WSPutItem;
import com.amalto.core.webservice.WSPutItemWithReport;
import com.amalto.webapp.core.util.Util;
import com.amalto.webapp.core.util.XmlUtil;
import com.amalto.webapp.core.util.XtentisWebappException;

public class UploadService {

    private static final Logger LOG = Logger.getLogger(UploadService.class);

    private static final Messages MESSAGES = MessagesFactory.getMessages(
            "org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages", UploadService.class.getClassLoader()); //$NON-NLS-1$

    private final String FILE_TYPE_CSV_SUFFIX = "csv"; //$NON-NLS-1$

    private final String FILE_TYPE_EXCEL_SUFFIX = "xls"; //$NON-NLS-1$

    private final String FILE_TYPE_EXCEL2010_SUFFIX = "xlsx"; //$NON-NLS-1$

    private final String File_CSV_SEPARATOR_SEMICOLON = "semicolon"; //$NON-NLS-1$

    private static int defaultMaxImportCount;

    private String fileType = null;

    private boolean isPartialUpdate = false;
    
    private boolean headersOnFirstLine = false;

    private Map<String, Boolean> headerVisibleMap = null;

    private List<String> inheritanceNodePathList = null;

    private String language = null;

    private String multipleValueSeparator = null;

    private String seperator = null;

    private String encoding = null;

    private char textDelimiter;

    private CSVReader csvReader = null;

    private EntityModel entityModel = null;

    private TypeModel typeModel = null;

    private HashMap<String, Integer> xsiTypeMap = null;

    private QName xsiTypeQName = null;

    private Map<String, List<Element>> multiNodeMap;

    static{
        defaultMaxImportCount = Integer.parseInt(
                MDMConfiguration.getConfiguration().getProperty("max.import.browserecord", MDMConfiguration.MAX_IMPORT_COUNT));
    }

    public UploadService(EntityModel entityModel, String fileType, boolean isPartialUpdate, boolean headersOnFirstLine,
            Map<String, Boolean> headerVisibleMap, List<String> inheritanceNodePathList, String multipleValueSeparator,
            String seperator, String encoding, char textDelimiter, String language) {
        this.entityModel = entityModel;
        this.fileType = fileType;
        this.isPartialUpdate = isPartialUpdate;
        this.headersOnFirstLine = headersOnFirstLine;
        this.headerVisibleMap = headerVisibleMap;
        this.inheritanceNodePathList = inheritanceNodePathList;
        this.multipleValueSeparator = multipleValueSeparator;
        this.seperator = seperator;
        this.encoding = encoding;
        this.textDelimiter = textDelimiter;
        this.language = language;
    }

    public List<WSPutItemWithReport> readUploadFile(File file) throws Exception {
        List<WSPutItemWithReport> wsPutItemWithReportList = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            typeModel = entityModel.getTypeModel(entityModel.getConceptName());
            xsiTypeMap = new HashMap<String, Integer>();

            if (FILE_TYPE_EXCEL_SUFFIX.equals(fileType.toLowerCase())
                    || FILE_TYPE_EXCEL2010_SUFFIX.equals(fileType.toLowerCase())) {
                wsPutItemWithReportList = readExcelFile(fileInputStream);
            } else if (FILE_TYPE_CSV_SUFFIX.equals(fileType.toLowerCase())) {
                wsPutItemWithReportList = readCsvFile(fileInputStream);
            }
            return wsPutItemWithReportList;
        } catch (Exception exception) {
            LOG.equals(exception);
            throw exception;
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (csvReader != null) {
                csvReader.close();
            }
        }
    }

    private List<WSPutItemWithReport> readExcelFile(FileInputStream fileInputStream) throws ServiceException, Exception {
        List<WSPutItemWithReport> wSPutItemWithReportList = new LinkedList<WSPutItemWithReport>();
        String[] importHeader = null;
        Workbook workBook = null;
        if (FILE_TYPE_EXCEL_SUFFIX.equals(fileType.toLowerCase())) {
            POIFSFileSystem poiFSFile = new POIFSFileSystem(fileInputStream);
            workBook = new HSSFWorkbook(poiFSFile);
        } else {
            workBook = new XSSFWorkbook(fileInputStream);
        }
        Sheet sheet = workBook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        int rowNumber = 0;
        boolean dataLine;
        while (rowIterator.hasNext()) {
            dataLine = false;
            rowNumber++;
            if ((rowNumber - 1) > defaultMaxImportCount) {
                break;
            }
            Row row = rowIterator.next();
            if (rowNumber == 1) {
                importHeader = readHeader(row, null);
                if (importHeader != null && importHeader.length > 0 && entityModel != null) {
                    validateKeyFieldExist(importHeader);
                }
                if (headersOnFirstLine) {
                    continue;
                }
            }
            multiNodeMap = new HashMap<String, List<Element>>();
            if (importHeader != null) {
                Document document;
                if (isPartialUpdate) {
                    Boolean keyContainsEmpty = false;
                    String[] keys = new String[entityModel.getKeys().length];
                    for (int k=0; k < entityModel.getKeys().length; k++) {
                        for(String header : importHeader) {
                            if(header.equals(entityModel.getKeys()[k]) && row.getCell(k) != null){
                                keys[k] = getExcelFieldValue(row.getCell(k));
                                if (keys[k].isEmpty()) {
                                    keyContainsEmpty = true;
                                }
                            } else if (header.equals(entityModel.getKeys()[k]) && row.getCell(k) == null) {
                                keyContainsEmpty = true;
                            }
                        }
                    }
                    if (keyContainsEmpty) {
                        if (isEmptyRecordInExcel(row, importHeader)) {
                            rowNumber--;
                            continue;
                        }
                        throw new UploadException(
                                MESSAGES.getMessage(new Locale(language), "save_error") + " " //$NON-NLS-1$ //$NON-NLS-2$
                                + MESSAGES.getMessage(new Locale(language), "save_row_count", rowNumber)  //$NON-NLS-1$
                                + MESSAGES.getMessage(new Locale(language), "error_missing_key_field")); //$NON-NLS-1$
                    }
                    document = getItemForPartialUpdate(entityModel, keys, rowNumber);
                } else {
                    if (isEmptyRecordInExcel(row, importHeader)) {
                        rowNumber--;
                        continue;
                    }
                    document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getSubXML(
                            typeModel, null, null, language));
                }
                Element currentElement = document.getRootElement();
                for (int i = 0; i < importHeader.length; i++) {
                    if (row.getCell(i) != null) {
                        String fieldValue = getExcelFieldValue(row.getCell(i));
                        if (fieldValue != null && !fieldValue.isEmpty()) {
                            dataLine = true;
                            fillFieldValue(currentElement, importHeader[i], fieldValue, row, null);
                        } else {
                            if(isPartialUpdate){
                                dataLine = true;
                                fillFieldValue(currentElement, importHeader[i], "", row, null); //$NON-NLS-1$
                            }
                        }
                    } else {
                        if(isPartialUpdate){
                            dataLine = true;
                            fillFieldValue(currentElement, importHeader[i], "", row, null); //$NON-NLS-1$
                        }
                    }
                }
                if (dataLine) {
                    wSPutItemWithReportList.add(buildWSPutItemWithReport(document));
                }
            }
        }
        return wSPutItemWithReportList;
    }

    private List<WSPutItemWithReport> readCsvFile(FileInputStream fileInputStream) throws ServiceException, Exception {
        List<WSPutItemWithReport> wSPutItemWithReportList = new LinkedList<WSPutItemWithReport>();
        String[] importHeader = null;
        char separator = File_CSV_SEPARATOR_SEMICOLON.equals(seperator) ? ';' : ',';
        csvReader = new CSVReader(new InputStreamReader(fileInputStream, encoding), separator, textDelimiter);
        List<String[]> records = csvReader.readAll();
        boolean dataLine;
        int rowNumber = 0;
        for (int i = 0; i < records.size(); i++) {
            rowNumber++;
            if ((rowNumber - 1) > defaultMaxImportCount) {
                break;
            }
            String[] record = records.get(i);
            dataLine = false;
            if (i == 0) {
                importHeader = readHeader(null, record);
                if (importHeader != null && importHeader.length > 0 && entityModel != null) {
                    validateKeyFieldExist(importHeader);
                }
                if (headersOnFirstLine) {
                    continue;
                }
            }
            multiNodeMap = new HashMap<String, List<Element>>();
            if (importHeader != null) {
                Document document;
                if(isPartialUpdate){
                    Boolean keyContainsEmpty = false;
                    String[] keys = new String[entityModel.getKeys().length];
                    for (int k=0; k < entityModel.getKeys().length; k++) {
                        for(String header : importHeader) {
                            if(header.equals(entityModel.getKeys()[k])){
                                keys[k] = record[k];
                                if (keys[k].isEmpty()) {
                                    keyContainsEmpty = true;
                                }
                            }
                        }
                    }
                    if (keyContainsEmpty) {
                        if (isEmptyRecordInCSV(record, importHeader)) {
                            rowNumber--;
                            continue;
                        }
                        throw new UploadException(
                                MESSAGES.getMessage(new Locale(language), "save_error") + " " //$NON-NLS-1$ //$NON-NLS-2$
                                + MESSAGES.getMessage(new Locale(language), "save_row_count", i + 1)  //$NON-NLS-1$
                                + MESSAGES.getMessage(new Locale(language), "error_missing_key_field")); //$NON-NLS-1$
                    }
                    document = getItemForPartialUpdate(entityModel, keys, i + 1);
                } else {
                    if (isEmptyRecordInCSV(record, importHeader)) {
                        rowNumber--;
                        continue;
                    }
                    document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getSubXML(
                            typeModel, null, null, language));
                }
                Element currentElement = document.getRootElement();
                if (record.length > 0) {
                    int validLength = record.length > importHeader.length ? importHeader.length : record.length;
                    for (int j = 0; j < validLength; j++) {
                        String fieldValue = record[j];
                        if (fieldValue != null && !fieldValue.isEmpty()) {
                            dataLine = true;
                            fillFieldValue(currentElement, importHeader[j], fieldValue, null, record);
                        } else {
                            if(isPartialUpdate){
                                dataLine = true;
                                fillFieldValue(currentElement, importHeader[j], "", null, record); //$NON-NLS-1$
                            }
                        }
                    }
                }
                if (dataLine) {
                    wSPutItemWithReportList.add(buildWSPutItemWithReport(document));
                }
            }
        }
        return wSPutItemWithReportList;
    }

    protected WSPutItemWithReport buildWSPutItemWithReport(Document document) throws Exception {
        return new WSPutItemWithReport(new WSPutItem(new WSDataClusterPK(getCurrentDataCluster()), document.asXML(),
                new WSDataModelPK(getCurrentDataModel()), false), UpdateReportPOJO.GENERIC_UI_SOURCE, true); 
    }

    /*
     * Returns a string corresponding to the double value given in parameter Exponent is removed and "0" are added at
     * the end of the string if necessary This method is useful when you import long itemid that you don't want to see
     * modified by importation method.
     */
    private String getStringRepresentation(double value) {
        String result = ""; //$NON-NLS-1$

        result = Double.toString(value);

        int index = result.indexOf("E");//$NON-NLS-1$

        String base = result;

        if (index > 0) {
            try {
                base = result.substring(0, index);
                String puissance = result.substring(index + 1);

                int puissanceValue = Integer.parseInt(puissance);

                int indexPoint = base.indexOf(".");//$NON-NLS-1$

                if (indexPoint > 0) {
                    String beforePoint = base.substring(0, indexPoint);
                    String afterPoint = base.substring(indexPoint + 1);

                    if (puissanceValue >= afterPoint.length()) {
                        base = beforePoint + "" + afterPoint; //$NON-NLS-1$
                        puissanceValue -= afterPoint.length();
                    } else {
                        String newBeforePoint = beforePoint + "" + afterPoint.substring(0, puissanceValue);//$NON-NLS-1$
                        String newAfterPoint = afterPoint.substring(puissanceValue);
                        base = newBeforePoint + "." + newAfterPoint;//$NON-NLS-1$
                        puissanceValue = 0;
                    }
                }

                for (int j = 0; j < puissanceValue; j++) {
                    base += "0"; //$NON-NLS-1$
                }

                result = base;

            } catch (NumberFormatException e) {
            }
        }
        return result;
    }

    protected String[] readHeader(Row headerRow, String[] headerRecord) throws UploadException {
        List<String> headers = new LinkedList<String>();
        String header;
        int index = 0;
        if (headersOnFirstLine) {
            if (FILE_TYPE_EXCEL_SUFFIX.equals(fileType.toLowerCase())
                    || FILE_TYPE_EXCEL2010_SUFFIX.equals(fileType.toLowerCase())) {
                Iterator<Cell> headerIterator = headerRow.cellIterator();
                while (headerIterator.hasNext()) {
                    Cell cell = headerIterator.next();
                    if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                        header = cell.getRichStringCellValue().getString();
                        headers.add(handleHeader(header, index));
                    }
                    index++;
                }
            } else if (FILE_TYPE_CSV_SUFFIX.equals(fileType.toLowerCase())) {
                for (String element : headerRecord) {
                    headers.add(handleHeader(element, index));
                    index++;
                }
            }
        } else {
            Iterator<String> headerIterator = headerVisibleMap.keySet().iterator();
            while (headerIterator.hasNext()) {
                header = headerIterator.next();
                registXsiType(header, index);
                headers.add(header);
                index++;
            }
        }
        return headers.toArray(new String[headers.size()]);
    }
    
    protected void validateKeyFieldExist(String[] importHeaders) throws UploadException {
        String[] keys = entityModel.getKeys();
        for(String key : keys){
            Boolean exist = false;
            for(String header : importHeaders){
                if (key.equals(header)) {
                    exist = true;
                    break;
                }
            }
            if(!exist){
                throw new UploadException(MESSAGES.getMessage(new Locale(language), "error_missing_key_field")); //$NON-NLS-1$
            }
        }
    }
    
    protected Boolean isEmptyRecordInExcel(Row row, String[] importHeader) throws Exception  {
        if (row != null && importHeader != null && importHeader.length > 0) {
            for (int i = 0; i < importHeader.length; i++) {
                if (row.getCell(i) != null) {
                    String fieldValue = getExcelFieldValue(row.getCell(i));
                    if (fieldValue != null && !fieldValue.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    protected Boolean isEmptyRecordInCSV(String[] record, String[] importHeader) throws Exception  {
        if (record != null && importHeader != null && importHeader.length > 0) {
            for (int i = 0; i < importHeader.length; i++) {
                if (record[i] != null && !record[i].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    protected String handleHeader(String headerName, int index) throws UploadException {
        String concept = entityModel.getConceptName();
        String headerPath = ""; //$NON-NLS-1$
        if (!headerName.startsWith(concept + "/")) { //$NON-NLS-1$
            headerPath = concept + "/" + headerName; //$NON-NLS-1$
        } else {
            headerPath = headerName;
        }
        if (!headerPath.endsWith(Constants.XSI_TYPE_QUALIFIED_NAME)) {
            if (entityModel.getTypeModel(headerPath) == null) {
                throw new UploadException(MESSAGES.getMessage(new Locale(language), "error_column_header", headerName, concept)); //$NON-NLS-1$
            }
        } else {
            registXsiType(headerPath, index);
        }

        return headerPath;
    }

    private void registXsiType(String headerName, int index) throws UploadException {
        if (headerName.endsWith(Constants.XSI_TYPE_QUALIFIED_NAME)) {
            if (entityModel.getTypeModel(headerName.substring(0, headerName.indexOf("/@" + Constants.XSI_TYPE_QUALIFIED_NAME))) != null) { //$NON-NLS-1$
                xsiTypeMap.put(headerName, index);
            } else {
                throw new UploadException(MESSAGES.getMessage(new Locale(language),
                        "error_column_header", headerName, entityModel.getConceptName())); //$NON-NLS-1$
            }
        }
    }
    
    private List<String> splitString(String valueString, String separator) {
        List<String> valueList = new ArrayList<String>();
        if (valueString == null || valueString.isEmpty()) {
            valueList.add(""); //$NON-NLS-1$
        } else {
            String[] valueArray = valueString.split(separator, -1);
            for (String value : valueArray) {
                valueList.add(value);
            }
        }
        return valueList;
    }

    protected void fillFieldValue(Element currentElement, String fieldPath, String fieldValue, Row row, String[] record)
            throws Exception {
        String parentPath = null;
        boolean isAttribute = false;
        List<String> valueList = null;
        List<Element> valueNodeList = new ArrayList<Element>();
        if (fieldPath.endsWith(Constants.XSI_TYPE_QUALIFIED_NAME)) {
            isAttribute = true;
            String field[] = fieldPath.split(Constants.FILE_EXPORT_IMPORT_SEPARATOR);
            fieldPath = field[0];
        }
        String[] xpathPartArray = fieldPath.split("/"); //$NON-NLS-1$
        String xpath = xpathPartArray[0];
        if (!isAttribute) {
            if (multipleValueSeparator != null && !multipleValueSeparator.isEmpty()) {
                valueList = splitString(fieldValue, "\\" + String.valueOf(multipleValueSeparator.charAt(0))); //$NON-NLS-1$
            }
        }
        for (int i = 1; i < xpathPartArray.length; i++) {
            if (currentElement != null) {
                parentPath = xpath;
                xpath = xpath + "/" + xpathPartArray[i]; //$NON-NLS-1$
                if (entityModel.getTypeModel(xpath).isMultiOccurrence() && multiNodeMap.get(xpath) == null) {
                    List<Element> multiNodeList = new ArrayList<Element>();
                    if (valueList != null) {
                        if (isPartialUpdate) {
                            if (currentElement.element(xpathPartArray[i]) == null) {
                                currentElement.addElement(xpathPartArray[i]);
                            }
                            List<Element> elementList = currentElement.elements(xpathPartArray[i]);
                            for (Element e : elementList) {
                                multiNodeList.add(e);
                            }
                            if (elementList.size() < valueList.size()) {
                                for (int j = 1; j <= valueList.size() - elementList.size(); j++) {
                                    currentElement.addElement(xpathPartArray[i]);
                                    multiNodeList.add((Element)currentElement.content().get(currentElement.content().size() - 1));
                                }
                            }
                        } else {
                            for (int j = 0; j < valueList.size(); j++) {
                                Element element = currentElement.element(xpathPartArray[i]);
                                int index = currentElement.content().indexOf(element);
                                if (index + j >= currentElement.content().size()
                                        || currentElement.content().get(currentElement.content().indexOf(element) + j) != element) {
                                    Element createCopy = element.createCopy();
                                    currentElement.content().add(createCopy);
                                    multiNodeList.add(createCopy);
                                } else {
                                    multiNodeList.add(element);
                                }
                            }
                        }
                    }
                    multiNodeMap.put(xpath, multiNodeList);
                    if (multiNodeList.size() > 0) {
                        currentElement = multiNodeList.get(multiNodeList.size() - 1);
                    }
                } else if (multiNodeMap.get(parentPath) != null) {
                    List<Element> parentlist = multiNodeMap.get(parentPath);
                    for (int j = 0; j < parentlist.size(); j++) {
                        Element parentElement = parentlist.get(j);
                        Element element = parentElement.element(xpathPartArray[i]);
                        if (element == null) {
                            element = parentElement.addElement(xpathPartArray[i]);
                        }
                        valueNodeList.add(element);
                    }
                    if (valueNodeList.size() > 0) {
                        currentElement = valueNodeList.get(valueNodeList.size() - 1);
                    }
                } else {
                    if (currentElement.element(xpathPartArray[i]) != null) {
                        currentElement = currentElement.element(xpathPartArray[i]);
                    } else {
                        currentElement = currentElement.addElement(xpathPartArray[i]);
                    }
                }
                if (i == xpathPartArray.length - 1) {
                    if (isAttribute) {
                        setAttributeValue(currentElement, fieldValue);
                    } else {
                        if (valueNodeList.size() > 0) {
                            for (int j = 0; j < valueList.size(); j++) {
                                setFieldValue(valueNodeList.get(j), valueList.get(j));
                            }
                        } else if (multiNodeMap.get(xpath) != null) {
                            List<Element> multiNodeList = multiNodeMap.get(xpath);
                            for (int j = 0; j < valueList.size(); j++) {
                                setFieldValue(multiNodeList.get(j), valueList.get(j));
                            }
                        } else {
                            setFieldValue(currentElement, fieldValue);
                        }
                    }
                } else {
                    String currentElemntPath = currentElement.getPath().substring(1);
                    if (inheritanceNodePathList != null && inheritanceNodePathList.contains(currentElemntPath)) {
                        Integer xsiTypeIndex = xsiTypeMap.get(currentElemntPath + "/@" + Constants.XSI_TYPE_QUALIFIED_NAME); //$NON-NLS-1$
                        if (xsiTypeIndex != null) {
                            String xsiTypeValue = ""; //$NON-NLS-1$
                            if (FILE_TYPE_EXCEL_SUFFIX.equals(fileType.toLowerCase())
                                    || FILE_TYPE_EXCEL2010_SUFFIX.equals(fileType.toLowerCase())) {
                                xsiTypeValue = row.getCell(xsiTypeIndex).getRichStringCellValue().getString();
                            } else if (FILE_TYPE_CSV_SUFFIX.equals(fileType.toLowerCase())) {
                                xsiTypeValue = record[i];
                            }
                            setAttributeValue(currentElement, xsiTypeValue);
                        } else {
                            throw new UploadException(MESSAGES.getMessage(new Locale(currentElemntPath),
                                    "missing_attribute", currentElemntPath + "/@" + Constants.XSI_TYPE_QUALIFIED_NAME)); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
            }
        }
    }
    
    protected Document getItemForPartialUpdate(EntityModel model, String[] keys, int rowNumber) throws RemoteException, XtentisWebappException, Exception {
        try {
            WSItem wsItem = CommonUtil.getPort().getItem(new WSGetItem(new WSItemPK(new WSDataClusterPK(org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getCurrentDataModel()), model.getConceptName(), keys)));
            return org.talend.mdm.webapp.base.server.util.XmlUtil.parseText(wsItem.getContent());
        } catch (Exception e) {
            throw new UploadException(MESSAGES.getMessage("save_error") + " " + MESSAGES.getMessage("save_row_count", rowNumber) + e.getCause().getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }
    

    protected String getExcelFieldValue(Cell cell) throws Exception {
        String fieldValue = null;
        int cellType = cell.getCellType();
        switch (cellType) {
            case Cell.CELL_TYPE_NUMERIC: {
                double tmp = cell.getNumericCellValue();
                fieldValue = getStringRepresentation(tmp);
                break;
            }
            case Cell.CELL_TYPE_STRING: {
                fieldValue = cell.getRichStringCellValue().getString();
                int result = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getFKFormatType(fieldValue,
                        multipleValueSeparator);
                if (result > 0) {
                    fieldValue = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getForeignKeyId(
                            fieldValue, result, multipleValueSeparator);
                }
                break;
            }
            case Cell.CELL_TYPE_BOOLEAN: {
                boolean tmp = cell.getBooleanCellValue();
                if (tmp) {
                    fieldValue = "true"; //$NON-NLS-1$
                } else {
                    fieldValue = "false";//$NON-NLS-1$
                }
                break;
            }
            case Cell.CELL_TYPE_FORMULA: {
                fieldValue = cell.getCellFormula();
                break;
            }
            case Cell.CELL_TYPE_ERROR: {
                break;
            }
            case Cell.CELL_TYPE_BLANK: {
                fieldValue = ""; //$NON-NLS-1$
            }
            default: {
            }
        }
        return fieldValue;
    }

    private void setFieldValue(Element currentElement, String value) throws Exception {
        if (currentElement.elements() != null && currentElement.elements().size() > 0) {
            Element complexeElement = XmlUtil.parseDocument(Util.parse(StringEscapeUtils.unescapeXml(value))).getRootElement();
            List<Element> contentList = currentElement.getParent().content();
            int index = contentList.indexOf(currentElement);
            contentList.remove(currentElement);
            contentList.add(index, complexeElement);
        } else {
            currentElement.setText(value);
        }
    }

    private void setAttributeValue(Element currentElement, String value) {
        if (xsiTypeQName == null) {
            xsiTypeQName = new QName(Constants.XSI_TYPE_NAME, new Namespace(Constants.XSI_PREFIX, Constants.XSI_URI),
                    Constants.XSI_TYPE_QUALIFIED_NAME);
        }
        if (!value.equals(currentElement.attributeValue(xsiTypeQName))) {
            currentElement.setAttributeValue(xsiTypeQName, value);
        }
    }

    protected String getCurrentDataCluster() throws Exception {
        return org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getCurrentDataCluster(false);
    }

    protected String getCurrentDataModel() throws Exception {
        return org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getCurrentDataModel();
    }
}
