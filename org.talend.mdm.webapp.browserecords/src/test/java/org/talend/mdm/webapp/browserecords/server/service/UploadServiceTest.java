/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit3.PowerMockSuite;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.commmon.util.datamodel.management.DataModelID;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.DataModelHelper;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.SchemaMockAgent;

import com.amalto.core.util.Util;
import com.amalto.core.webservice.WSPutItemWithReport;
import com.amalto.webapp.core.util.XmlUtil;
import com.amalto.webapp.core.util.XtentisWebappException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * created by talend2 on 2013-12-18 Detailled comment
 * 
 */
@PrepareForTest({ Util.class })
public class UploadServiceTest extends TestCase {

    static {
        new UploadServiceTest();
        MDMConfiguration.createConfiguration("", true);
        MDMConfiguration.getConfiguration().setProperty("max.import.browserecord", String.valueOf(10)); //$NON-NLS-1$
    }

    protected String clusterName = null;

    protected String dataModelName = null;

    protected EntityModel entityModel = null;

    protected File file = null;

    protected String fileType = null;

    protected boolean isPartialUpdate = false;
    
    protected boolean headersOnFirstLine = false;

    protected Map<String, Boolean> headerVisibleMap = null;

    protected List<String> inheritanceNodePathList = null;

    protected String language = "en"; //$NON-NLS-1$

    protected String multipleValueSeparator = null;

    protected String seperator = null;

    protected String encoding = "utf-8"; //$NON-NLS-1$

    protected char textDelimiter = '\"';

    private Pattern removeFormatPattern = Pattern.compile("\t|\r|\n"); //$NON-NLS-1$

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        isPartialUpdate = false;
        headersOnFirstLine = true;
    }

    @SuppressWarnings("unchecked")
    public static TestSuite suite() throws Exception {
        return new PowerMockSuite("Unit tests for " + UploadServiceTest.class.getSimpleName(), UploadServiceTest.class);
    }

    public void testUploadModel_Polymorphism() throws Exception {
        headerVisibleMap = new HashMap<String, Boolean>();
        headerVisibleMap.put("UploadTestModel_Polymorphism/id", true); //$NON-NLS-1$
        headerVisibleMap.put("UploadTestModel_Polymorphism/info/@xsi:type", true); //$NON-NLS-1$
        inheritanceNodePathList = new LinkedList<String>();
        inheritanceNodePathList.add("UploadTestModel_Polymorphism/info"); //$NON-NLS-1$
        String[] keys = { "UploadTestModel_Polymorphism/id" }; //$NON-NLS-1$
        entityModel = getEntityModel("UploadTestModel.xsd", "UploadTestModel", "UploadTestModel_Polymorphism", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        String record1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<UploadTestModel_Polymorphism xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>1</id><info xsi:type=\"SuperInfoType\"><name>1</name></info></UploadTestModel_Polymorphism>"; //$NON-NLS-1$
        String record2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<UploadTestModel_Polymorphism xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>2</id><info xsi:type=\"SubInfoType\"><name>2</name></info></UploadTestModel_Polymorphism>"; //$NON-NLS-1$
        String record3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<UploadTestModel_Polymorphism xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>3</id><info xsi:type=\"SuperInfoType\"><name/></info></UploadTestModel_Polymorphism>"; //$NON-NLS-1$
        String record4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<UploadTestModel_Polymorphism xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>4</id><info xsi:type=\"SubInfoType\"><name>4</name></info></UploadTestModel_Polymorphism>"; //$NON-NLS-1$
        String record5 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<UploadTestModel_Polymorphism xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>5</id><info xsi:type=\"SubInfoType\"><name/></info></UploadTestModel_Polymorphism>"; //$NON-NLS-1$

        // test upload excel file
        fileType = "xls"; //$NON-NLS-1$
        multipleValueSeparator = "|"; //$NON-NLS-1$
        file = new File(this.getClass().getResource("UploadTestModel_Polymorphism.xls").getFile()); //$NON-NLS-1$
        UploadService service = new TestUploadService(entityModel, fileType, isPartialUpdate, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);

        List<WSPutItemWithReport> wsPutItemWithReportList = service.readUploadFile(file);
        assertEquals(record1, wsPutItemWithReportList.get(0).getWsPutItem().getXmlString());
        assertEquals(record2, wsPutItemWithReportList.get(1).getWsPutItem().getXmlString());
        assertEquals(record3, wsPutItemWithReportList.get(2).getWsPutItem().getXmlString());
        assertEquals(record4, wsPutItemWithReportList.get(3).getWsPutItem().getXmlString());
        assertEquals(record5, wsPutItemWithReportList.get(4).getWsPutItem().getXmlString());

        // test upload csv file
        fileType = "csv"; //$NON-NLS-1$
        file = new File(this.getClass().getResource("UploadTestModel_Polymorphism.csv").getFile()); //$NON-NLS-1$
        service = new TestUploadService(entityModel, fileType, isPartialUpdate, headersOnFirstLine, headerVisibleMap, inheritanceNodePathList,
                multipleValueSeparator, seperator, encoding, textDelimiter, language);
        wsPutItemWithReportList = service.readUploadFile(file);
        assertEquals(record1, wsPutItemWithReportList.get(0).getWsPutItem().getXmlString());
        assertEquals(record2, wsPutItemWithReportList.get(1).getWsPutItem().getXmlString());
        assertEquals(record3, wsPutItemWithReportList.get(2).getWsPutItem().getXmlString());
        assertEquals(record4, wsPutItemWithReportList.get(3).getWsPutItem().getXmlString());
        assertEquals(record5, wsPutItemWithReportList.get(4).getWsPutItem().getXmlString());
    }

    public void testMultiNode1() throws Exception {
        String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Entity xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><field1>1</field1><field2><atr1>1</atr1><atr2>2</atr2><atr3>3</atr3></field2><field2><atr1>11</atr1><atr2>22</atr2><atr3>33</atr3></field2></Entity>"; //$NON-NLS-1$
        // test upload excel file
        fileType = "xls"; //$NON-NLS-1$
        headerVisibleMap = new HashMap<String, Boolean>();
        headerVisibleMap.put("Entity/field1", true); //$NON-NLS-1$
        headerVisibleMap.put("Entity/field2/atr1", true); //$NON-NLS-1$
        headerVisibleMap.put("Entity/field2/atr2", true); //$NON-NLS-1$
        headerVisibleMap.put("Entity/field2/atr3", true); //$NON-NLS-1$
        multipleValueSeparator = "|"; //$NON-NLS-1$
        file = new File(this.getClass().getResource("UploadTestModel_MultiNode1.xls").getFile()); //$NON-NLS-1$
        String[] keys = { "Entity/field1" }; //$NON-NLS-1$
        entityModel = getEntityModel("UploadTestModel_MultiNode1.xsd", "Entity", "Entity", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        UploadService service = new TestUploadService(entityModel, fileType, isPartialUpdate, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        List<WSPutItemWithReport> wsPutItemWithReportList = service.readUploadFile(file);
        assertEquals(expectedResult, removeFormatPattern.matcher(wsPutItemWithReportList.get(0).getWsPutItem().getXmlString())
                .replaceAll("")); //$NON-NLS-1$

        expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Entity xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><field1>1</field1><field2><atr1>1</atr1><atr2>2</atr2><atr3>3</atr3></field2></Entity>"; //$NON-NLS-1$
        file = new File(this.getClass().getResource("UploadTestModel_MultiNode12.xls").getFile()); //$NON-NLS-1$
        wsPutItemWithReportList = service.readUploadFile(file);
        assertEquals(expectedResult, removeFormatPattern.matcher(wsPutItemWithReportList.get(0).getWsPutItem().getXmlString())
                .replaceAll("")); //$NON-NLS-1$
    }

    public void testMultiNode2() throws Exception {
        String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Picture/><Id>1</Id><Name>1</Name><Description>1</Description><Features><Sizes><Size>Medium</Size><Size>Small</Size></Sizes><Colors><Color>White</Color><Color>Light Blue</Color><Color>Lemon</Color></Colors></Features><Availability/><Price>1.00</Price><Family/><OnlineStore/><Stores><Store/></Stores></Product>"; //$NON-NLS-1$
        // test upload excel file
        fileType = "xls"; //$NON-NLS-1$
        headerVisibleMap = new HashMap<String, Boolean>();
        headerVisibleMap.put("Product/Id", true); //$NON-NLS-1$
        headerVisibleMap.put("Product/Name", true); //$NON-NLS-1$
        headerVisibleMap.put("Product/Price", true); //$NON-NLS-1$
        headerVisibleMap.put("Product/Description", true); //$NON-NLS-1$
        headerVisibleMap.put("Product/Availability", true); //$NON-NLS-1$
        headerVisibleMap.put("Product/Features/Sizes/Size", true); //$NON-NLS-1$
        headerVisibleMap.put("Product/Features/Colors/Color", true); //$NON-NLS-1$"
        headerVisibleMap.put("Product/Family", true); //$NON-NLS-1$"
        multipleValueSeparator = "|"; //$NON-NLS-1$
        file = new File(this.getClass().getResource("Product.xls").getFile()); //$NON-NLS-1$
        String[] keys = { "Product/Id" }; //$NON-NLS-1$
        entityModel = getEntityModel("Product.xsd", "Product", "Product", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        UploadService service = new TestUploadService(entityModel, fileType, isPartialUpdate, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        List<WSPutItemWithReport> wsPutItemWithReportList = service.readUploadFile(file);
        assertEquals(expectedResult, removeFormatPattern.matcher(wsPutItemWithReportList.get(0).getWsPutItem().getXmlString())
                .replaceAll("")); //$NON-NLS-1$

        expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Picture/><Id>1</Id><Name>1</Name><Description>1</Description><Features><Sizes><Size/></Sizes><Colors><Color/></Colors></Features><Availability/><Price>1.00</Price><Family/><OnlineStore/><Stores><Store/></Stores></Product>"; //$NON-NLS-1$
        headerVisibleMap = new HashMap<String, Boolean>();
        headerVisibleMap.put("Product/Id", true); //$NON-NLS-1$
        headerVisibleMap.put("Product/Name", true); //$NON-NLS-1$
        headerVisibleMap.put("Product/Price", true); //$NON-NLS-1$
        headerVisibleMap.put("Product/Description", true); //$NON-NLS-1$
        headerVisibleMap.put("Product/Availability", true); //$NON-NLS-1$
        headerVisibleMap.put("Product/Family", true); //$NON-NLS-1$"
        file = new File(this.getClass().getResource("Product2.xls").getFile()); //$NON-NLS-1$
        service = new TestUploadService(entityModel, fileType, isPartialUpdate, headersOnFirstLine, headerVisibleMap, inheritanceNodePathList,
                multipleValueSeparator, seperator, encoding, textDelimiter, language);
        wsPutItemWithReportList = service.readUploadFile(file);
        assertEquals(expectedResult, removeFormatPattern.matcher(wsPutItemWithReportList.get(0).getWsPutItem().getXmlString())
                .replaceAll("")); //$NON-NLS-1$
    }

    public void testGetExcelFieldValue() throws Exception {
        boolean partialUpdateFlag = true;
        String[] keys = { "EntityA/EntityAId" }; //$NON-NLS-1$
        headerVisibleMap = new HashMap<String, Boolean>();
        headerVisibleMap.put("EntityA/EntityAId", true); //$NON-NLS-1$
        headerVisibleMap.put("EntityA/Name", true); //$NON-NLS-1$
        headerVisibleMap.put("EntityA/Age", true); //$NON-NLS-1$
        multipleValueSeparator = "|"; //$NON-NLS-1$
        EntityModel em = getEntityModel("PartialUpdateModel.xsd", "PartialUpdateModel", "EntityA", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        UploadService service = new TestUploadService(em, fileType, partialUpdateFlag, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        String[] importHeader = new String[]{"EntityA/EntityAId"}; //$NON-NLS-1$
        try {
            service.validateKeyFieldExist(importHeader);
        } catch(Exception e) {
            fail("Key Field is not Exist"); //$NON-NLS-1$
        }
        
        file = new File(this.getClass().getResource("UploadTestModel_PartialUpdate.xls").getFile()); //$NON-NLS-1$
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            POIFSFileSystem poiFSFile = new POIFSFileSystem(fileInputStream);
            Workbook workBook = new HSSFWorkbook(poiFSFile);
            Sheet sheet = workBook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            int rowNumber = 0;
            while (rowIterator.hasNext()) {
                rowNumber++;
                Row row = rowIterator.next();
                if (rowNumber == 1) {
                    continue;
                } else if (rowNumber == 2) {
                    assertEquals("22", service.getExcelFieldValue(row.getCell(0))); //$NON-NLS-1$
                    assertEquals("updatedName2", service.getExcelFieldValue(row.getCell(1))); //$NON-NLS-1$
                    assertEquals("2222", service.getExcelFieldValue(row.getCell(2))); //$NON-NLS-1$
                } else if (rowNumber == 3) {
                    assertEquals("33", service.getExcelFieldValue(row.getCell(0))); //$NON-NLS-1$
                    assertEquals("updatedName3", service.getExcelFieldValue(row.getCell(1))); //$NON-NLS-1$
                    assertEquals("3333", service.getExcelFieldValue(row.getCell(2))); //$NON-NLS-1$
                } else if (rowNumber == 4) {
                    assertEquals("44", service.getExcelFieldValue(row.getCell(0))); //$NON-NLS-1$
                    assertEquals("updatedName4", service.getExcelFieldValue(row.getCell(1))); //$NON-NLS-1$
                    assertEquals("4444", service.getExcelFieldValue(row.getCell(2))); //$NON-NLS-1$
                }
            }
        } catch (Exception exception) {
            fail("get excel file field value failed."); //$NON-NLS-1$
        } 
    }

    public void testImportWithDefaultImportCount() throws Exception {
        fileType = "xls"; //$NON-NLS-1$
        multipleValueSeparator = "|"; //$NON-NLS-1$
        file = new File(this.getClass().getResource("Product_defalutImportCount.xls").getFile()); //$NON-NLS-1$
        String[] keys = { "Product/Id" }; //$NON-NLS-1$
        entityModel = getEntityModel("Product.xsd", "Product", "Product", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        UploadService service = new TestUploadService(entityModel, fileType, isPartialUpdate, headersOnFirstLine,
                headerVisibleMap, inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        List<WSPutItemWithReport> wsPutItemWithReportList = service.readUploadFile(file);

        FileInputStream fileInputStream = new FileInputStream(file);
        POIFSFileSystem poiFSFile = new POIFSFileSystem(fileInputStream);
        Workbook workBook = new HSSFWorkbook(poiFSFile);
        Sheet sheet = workBook.getSheetAt(0);

        assertEquals(15, sheet.getLastRowNum());
        assertEquals(10, wsPutItemWithReportList.size());

        fileType = "csv"; //$NON-NLS-1$
        file = new File(this.getClass().getResource("Product_defalutImportCount.csv").getFile()); //$NON-NLS-1$
        service = new TestUploadService(entityModel, fileType, isPartialUpdate, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        wsPutItemWithReportList = service.readUploadFile(file);

        assertEquals(10, wsPutItemWithReportList.size());
    }

    public void testImportForPartial() throws Exception {
        fileType = "xls"; //$NON-NLS-1$
        multipleValueSeparator = "|"; //$NON-NLS-1$
        file = new File(this.getClass().getResource("Product_defalutImportCount_ForPartial.xls").getFile()); //$NON-NLS-1$
        String[] keys = { "Product/Id" }; //$NON-NLS-1$
        entityModel = getEntityModel("Product.xsd", "Product", "Product", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        UploadService service = new TestUploadService(entityModel, fileType, true, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        List<WSPutItemWithReport> wsPutItemWithReportList = service.readUploadFile(file);

        assertEquals(10, wsPutItemWithReportList.size());

        String exceptResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Picture/><Id>11.0</Id><Name>1</Name><Description>11</Description><Features><Sizes><Size>Medium</Size><Size>Small</Size></Sizes><Colors><Color>White</Color><Color>Light Blue</Color><Color>Lemon</Color></Colors></Features><Availability></Availability><Price>1.10</Price><Family></Family><OnlineStore/><Stores><Store/></Stores></Product>";
        assertEquals(exceptResult, removeFormatPattern.matcher(wsPutItemWithReportList.get(9).getWsPutItem().getXmlString())
                .replaceAll(""));

        fileType = "csv"; //$NON-NLS-1$
        multipleValueSeparator = "|"; //$NON-NLS-1$
        file = new File(this.getClass().getResource("Product_defalutImportCount_ForPartial.csv").getFile()); //$NON-NLS-1$
        entityModel = getEntityModel("Product.xsd", "Product", "Product", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        service = new TestUploadService(entityModel, fileType, true, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        wsPutItemWithReportList = service.readUploadFile(file);

        assertEquals(10, wsPutItemWithReportList.size());

        exceptResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Picture/><Id>11</Id><Name>1</Name><Description>11</Description><Features><Sizes><Size>Medium</Size><Size>Small</Size></Sizes><Colors><Color>White</Color><Color>Light Blue</Color><Color>Lemon</Color></Colors></Features><Availability></Availability><Price>1.10</Price><Family></Family><OnlineStore/><Stores><Store/></Stores></Product>";
        assertEquals(exceptResult, removeFormatPattern.matcher(wsPutItemWithReportList.get(9).getWsPutItem().getXmlString())
                .replaceAll(""));
    }
    
    public void testUploadCSVFileWithoutHeader() throws Exception {
        headerVisibleMap = new HashMap<String, Boolean>();
        headerVisibleMap.put("Contact/ContactId", true); //$NON-NLS-1$
        headerVisibleMap.put("Contact/name", true); //$NON-NLS-1$
        headerVisibleMap.put("Contact/firstname", true); //$NON-NLS-1$
        inheritanceNodePathList = new LinkedList<String>();
        
        String[] keys = { "Contact/ContactId" }; //$NON-NLS-1$
        entityModel = getEntityModel("UploadContactTestModel.xsd", "Contact", "Contact", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        // test upload csv file
        fileType = "csv"; //$NON-NLS-1$
        multipleValueSeparator = "|"; //$NON-NLS-1$
        UploadService service = new TestUploadService(entityModel, fileType, isPartialUpdate, false, headerVisibleMap, inheritanceNodePathList,
                multipleValueSeparator, seperator, encoding, textDelimiter, language);
        
        file = new File(this.getClass().getResource("UploadContactWithoutHeader.csv").getFile()); //$NON-NLS-1$
        List<WSPutItemWithReport> wsPutItemWithReportList = service.readUploadFile(file);
        
        wsPutItemWithReportList = service.readUploadFile(file);
        assertEquals(2, wsPutItemWithReportList.size());
        
        String exceptResult1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Contact xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><ContactId/><name>id1</name><firstname>name1</firstname><emailsList><email><adress/><adresscategory/></email></emailsList></Contact>"; //$NON-NLS-1$
        String exceptResult2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Contact xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><ContactId/><name>id2</name><firstname>name2</firstname><emailsList><email><adress/><adresscategory/></email></emailsList></Contact>"; //$NON-NLS-1$
        assertEquals(exceptResult1, removeFormatPattern.matcher(wsPutItemWithReportList.get(0).getWsPutItem().getXmlString()).replaceAll("")); //$NON-NLS-1$
        assertEquals(exceptResult2, removeFormatPattern.matcher(wsPutItemWithReportList.get(1).getWsPutItem().getXmlString()).replaceAll("")); //$NON-NLS-1$
    }

    public void testGetForeignKeyVlaue() throws Exception {
        Document document;
        boolean partialUpdateFlag = true;
        String[] keys = { "TestFK/Id" }; //$NON-NLS-1$
        headerVisibleMap = new HashMap<String, Boolean>();
        headerVisibleMap.put("TestFK/Id", true); //$NON-NLS-1$
        headerVisibleMap.put("TestFK/Name", true); //$NON-NLS-1$
        headerVisibleMap.put("TestFK/FK", true); //$NON-NLS-1$
        multipleValueSeparator = "|"; //$NON-NLS-1$
        EntityModel entityModel = getEntityModel("UploadTestForeignKey.xsd", "UploadTestForeignKey", "TestFK", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        TestUploadService service = new TestUploadService(entityModel, fileType, partialUpdateFlag, headersOnFirstLine,
                headerVisibleMap, inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);

        file = new File(this.getClass().getResource("TestFK.xls").getFile()); //$NON-NLS-1$
        FileInputStream fileInputStream = new FileInputStream(file);
        POIFSFileSystem poiFSFile = new POIFSFileSystem(fileInputStream);
        Workbook workBook = new HSSFWorkbook(poiFSFile);
        Sheet sheet = workBook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        int rowNumber = 0;
        while (rowIterator.hasNext()) {
            rowNumber++;
            Row row = rowIterator.next();
            if (rowNumber == 1) {
                continue;
            } else if (rowNumber == 2) {
                // case 1 Test foreign key value with bracket like [FK1]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFK/FK", service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK></TestFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 3) {
                // case 2 Test foreign key value without bracket like [FK1-AAA]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFK/FK", service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AAA]</FK></TestFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 4) {
                // case 3 Test foreign key value without bracket like [FK1-AAA-BBB]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFK/FK", service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AAA-BBB]</FK></TestFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 5) {
                // case 4 Test foreign key value without bracket like FK1
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFK/FK", service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK></TestFK>", //$NON-NLS-1$
                        document.asXML());
            }
        }

        entityModel = getEntityModel("UploadTestForeignKey.xsd", "UploadTestForeignKey", "TestFKWithInfo", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        service = new TestUploadService(entityModel, fileType, partialUpdateFlag, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        file = new File(this.getClass().getResource("TestFKWithInfo.xls").getFile()); //$NON-NLS-1$
        fileInputStream = new FileInputStream(file);
        poiFSFile = new POIFSFileSystem(fileInputStream);
        workBook = new HSSFWorkbook(poiFSFile);
        sheet = workBook.getSheetAt(0);
        rowIterator = sheet.rowIterator();
        rowNumber = 0;
        while (rowIterator.hasNext()) {
            rowNumber++;
            Row row = rowIterator.next();
            if (rowNumber == 1) {
                continue;
            } else if (rowNumber == 2) {
                // case 4 Test foreign key value with info and bracket
                // [FK1]|FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 3) {
                // case 5 Test foreign key value with info and bracket
                // [FK1|AAA]|FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 4) {
                // case 5 Test foreign key value with info and bracket
                // [FK1|AAA|BBB]|FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA|BBB]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 5) {
                // case 6 Test foreign key value with info and bracket
                // [FK1]-FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 6) {
                // case 7 Test foreign key value with info and bracket
                // [FK1-AAA]-FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AAA]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 7) {
                // case 7 Test foreign key value with info and bracket
                // [FK1-AAA-BBB]-FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AAA-BBB]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 8) {
                // case 8 Test foreign key value with info and bracket
                // [FK1]|FKInfo|FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 9) {
                // case 9 Test foreign key value with info and bracket
                // [FK1|AAA]|FKInfo|FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 10) {
                // case 9 Test foreign key value with info and bracket
                // [FK1|AAA|BBB]|FKInfo|FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA|BBB]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 11) {
                // case 10 Test foreign key value with info and bracket
                // [FK1]-FKInfo-FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 12) {
                // case 11 Test foreign key value with info and bracket
                // [FK1-AAA]-FKInfo-FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AAA]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 13) {
                // case 11 Test foreign key value with info and bracket
                // [FK1-AAA-BBB]-FKInfo-FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AAA-BBB]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 14) {
                // case 12 Test foreign key value with info without bracket
                // FK1|FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 15) {
                // case 13 Test foreign key value with info without bracket
                // FK1-FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 16) {
                // case 14 Test foreign key value with info without bracket
                // FK1|FKInfo|FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 17) {
                // case 15 Test foreign key value with info without bracket
                // FK1-FKInfo-FKInfo
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK></TestFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            }
        }

        entityModel = getEntityModel("UploadTestForeignKey.xsd", "UploadTestForeignKey", "TestMultipleFK", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        service = new TestUploadService(entityModel, fileType, partialUpdateFlag, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        file = new File(this.getClass().getResource("TestMultipleFK.xls").getFile()); //$NON-NLS-1$
        fileInputStream = new FileInputStream(file);
        poiFSFile = new POIFSFileSystem(fileInputStream);
        workBook = new HSSFWorkbook(poiFSFile);
        sheet = workBook.getSheetAt(0);
        rowIterator = sheet.rowIterator();
        rowNumber = 0;
        while (rowIterator.hasNext()) {
            rowNumber++;
            Row row = rowIterator.next();
            if (rowNumber == 1) {
                continue;
            } else if (rowNumber == 2) {
                // case 16 Test multiple foreign key value with bracket like [FK1]|[FK2]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFK/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK><FK>[FK2]</FK></TestMultipleFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 3) {
                // case 17 Test multiple foreign key value with bracket like [FK1|AAA]|[FK2|AAA]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFK/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA]</FK><FK>[FK2|AAA]</FK></TestMultipleFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 4) {
                // case 18 Test multiple foreign key value with bracket like [FK1AAA]|[FK2|AAA]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFK/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1AAA]</FK><FK>[FK2|AAA]</FK></TestMultipleFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 5) {
                // case 19 Test multiple foreign key value with bracket like [FK1|AAA]|[FK2AAA]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFK/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA]</FK><FK>[FK2AAA]</FK></TestMultipleFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 6) {
                // case 19 Test multiple foreign key value with bracket like [FK1|AAA|BBB]|[FK2|AAA]|[FK3AAA]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFK/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA|BBB]</FK><FK>[FK2|AAA]</FK><FK>[FK3AAA]</FK></TestMultipleFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 7) {
                // case 19 Test multiple foreign key value with bracket like [FK1|AAA|BBB]|[FK2AAA]|[FK3|AAA|BBB]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFK/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA|BBB]</FK><FK>[FK2AAA]</FK><FK>[FK3|AAA|BBB]</FK></TestMultipleFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 8) {
                // case 20 Test multiple foreign key value without bracket like FK1|FK2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFK/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK><FK>[FK2]</FK></TestMultipleFK>", //$NON-NLS-1$
                        document.asXML());
            }
        }

        entityModel = getEntityModel("UploadTestForeignKey.xsd", "UploadTestForeignKey", "TestMultipleFKWithInfo", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        service = new TestUploadService(entityModel, fileType, partialUpdateFlag, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        file = new File(this.getClass().getResource("TestMultipleFKWithInfo.xls").getFile()); //$NON-NLS-1$
        fileInputStream = new FileInputStream(file);
        poiFSFile = new POIFSFileSystem(fileInputStream);
        workBook = new HSSFWorkbook(poiFSFile);
        sheet = workBook.getSheetAt(0);
        rowIterator = sheet.rowIterator();
        rowNumber = 0;
        while (rowIterator.hasNext()) {
            rowNumber++;
            Row row = rowIterator.next();
            if (rowNumber == 1) {
                continue;
            } else if (rowNumber == 2) {
                // case 21 Test multiple foreign key value with info with bracket
                // [FK1]|FKInfo1|[FK2]|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK><FK>[FK2]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 3) {
                // case 22 Test multiple foreign key value with info with bracket
                // [FK1|AAA]|FKInfo1|[FK2|AAA]|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA]</FK><FK>[FK2|AAA]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 4) {
                // case 23 Test multiple foreign key value with info with bracket
                // [FK1|AAA|BBB]|FKInfo1|[FK2|AAA|BBB]|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA|BBB]</FK><FK>[FK2|AAA|BBB]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 5) {
                // case 23 Test multiple foreign key value with info with bracket
                // [FK1|AAA|BBB]|FKInfo1|[FK2|AAA|BBB]|FKInfo2|[FK3|AAA|BBB]|FKInfo3
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA|BBB]</FK><FK>[FK2|AAA|BBB]</FK><FK>[FK3|AAA|BBB]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 6) {
                // case 23 Test multiple foreign key value with info with bracket
                // [FK1AAA]|FKInfo1|[FK2|AAA]|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1AAA]</FK><FK>[FK2|AAA]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 7) {
                // case 24 Test multiple foreign key value with info with bracket
                // [FK1|AAA]|FKInfo1|[FK2AAA]|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA]</FK><FK>[FK2AAA]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 8) {
                // case 25 Test multiple foreign key value with info with bracket
                // [FK1]|FKInfo1|FKInfo2|[FK2]|FKInfo1|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK><FK>[FK2]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 9) {
                // case 26 Test multiple foreign key value with info with bracket
                // [FK1|AAA]|FKInfo1|FKInfo2|[FK2|AAA]|FKInfo1|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA]</FK><FK>[FK2|AAA]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 10) {
                // case 27 Test multiple foreign key value with info with bracket
                // [FK1AAA]|FKInfo1|FKInfo2|[FK2|AAA]|FKInfo1|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1AAA]</FK><FK>[FK2|AAA]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 11) {
                // case 28 Test multiple foreign key value with info with bracket
                // [FK1|AAA]|FKInfo1|FKInfo2|[FK2AAA]|FKInfo1|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA]</FK><FK>[FK2AAA]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 12) {
                // case 29 Test multiple foreign key value with info with bracket
                // [FK1]-FKInfo1|[FK2]-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK><FK>[FK2]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 13) {
                // case 30 Test multiple foreign key value with info with bracket
                // [FK1-AAA]-FKInfo1|[FK2-AAA]-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AAA]</FK><FK>[FK2-AAA]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 14) {
                // case 30 Test multiple foreign key value with info with bracket
                // [FK1-AAA]-FKInfo1|[FK2-BBB]-FKInfo2|[FK3-CCC]-FKInfo3
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AAA]</FK><FK>[FK2-BBB]</FK><FK>[FK3-CCC]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 15) {
                // case 30 Test multiple foreign key value with info with bracket
                // [FK1-AA-AAA]-FKInfo1|[FK2-BB-BBB]-FKInfo2|[FK2-CC-CCC]-FKInfo3
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AA-AAA]</FK><FK>[FK2-BB-BBB]</FK><FK>[FK3-CC-CCC]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 16) {
                // case 31 Test multiple foreign key value with info with bracket
                // [FK1AAA]-FKInfo1|[FK2-AAA]-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1AAA]</FK><FK>[FK2-AAA]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 17) {
                // case 32 Test multiple foreign key value with info with bracket
                // [FK1-AAA]-FKInfo1|[FK2AAA]-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AAA]</FK><FK>[FK2AAA]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 18) {
                // case 33 Test multiple foreign key value with info with bracket
                // [FK1]-FKInfo1-FKInfo2|[FK2]-FKInfo1-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1]</FK><FK>[FK2]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 19) {
                // case 34 Test multiple foreign key value with info with bracket
                // [FK1-AAA]-FKInfo1-FKInfo2|[FK2-AAA]-FKInfo1-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AAA]</FK><FK>[FK2-AAA]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 20) {
                // case 34 Test multiple foreign key value with info with bracket
                // [FK1AAA]-FKInfo1-FKInfo2|[FK2-AAA]-FKInfo1-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1AAA]</FK><FK>[FK2-AAA]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 21) {
                // case 35 Test multiple foreign key value with info with bracket
                // [FK1-AAA]-FKInfo1-FKInfo2|[FK2AAA]-FKInfo1-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AAA]</FK><FK>[FK2AAA]</FK></TestMultipleFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            }
        }

        entityModel = getEntityModel("UploadTestForeignKey.xsd", "UploadTestForeignKey", "TestCompositeFK", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        service = new TestUploadService(entityModel, fileType, partialUpdateFlag, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        file = new File(this.getClass().getResource("TestCompositeFK.xls").getFile()); //$NON-NLS-1$
        fileInputStream = new FileInputStream(file);
        poiFSFile = new POIFSFileSystem(fileInputStream);
        workBook = new HSSFWorkbook(poiFSFile);
        sheet = workBook.getSheetAt(0);
        rowIterator = sheet.rowIterator();
        rowNumber = 0;
        while (rowIterator.hasNext()) {
            rowNumber++;
            Row row = rowIterator.next();
            if (rowNumber == 1) {
                continue;
            } else if (rowNumber == 2) {
                // case 36 Test composite foreign key value with bracket like [FK11][FK12]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11][FK12]</FK></TestCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 3) {
                // case 37 Test composite foreign key value with bracket like [FK11|AAA][FK12|AAA]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11|AAA][FK12|AAA]</FK></TestCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 4) {
                // case 38 Test composite foreign key value with bracket like [FK11-AAA][FK12-AAA]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11-AAA][FK12-AAA]</FK></TestCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 5) {
                // case 39 Test composite foreign key value with bracket like [FK11-AAA][FK12-AAA]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)), row,
                        null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK></FK></TestCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            }
        }

        entityModel = getEntityModel("UploadTestForeignKey.xsd", "UploadTestForeignKey", "TestCompositeFKWithInfo", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        service = new TestUploadService(entityModel, fileType, partialUpdateFlag, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        file = new File(this.getClass().getResource("TestCompositeFKWithInfo.xls").getFile()); //$NON-NLS-1$
        fileInputStream = new FileInputStream(file);
        poiFSFile = new POIFSFileSystem(fileInputStream);
        workBook = new HSSFWorkbook(poiFSFile);
        sheet = workBook.getSheetAt(0);
        rowIterator = sheet.rowIterator();
        rowNumber = 0;
        while (rowIterator.hasNext()) {
            rowNumber++;
            Row row = rowIterator.next();
            if (rowNumber == 1) {
                continue;
            } else if (rowNumber == 2) {
                // case 40 Test composite foreign key value with info with bracket
                // 1 [FK1][FK2]|FKInfo1
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestCompositeFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1][FK2]</FK></TestCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 3) {
                // case 41 Test composite foreign key value with info with bracket
                // 1 [FK1|AAA][FK2|AAA]|FKInfo1
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestCompositeFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA][FK2|AAA]</FK></TestCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 4) {
                // case 42 Test composite foreign key value with info with bracket
                // 2 [FK1][FK2]|FKInfo1|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestCompositeFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1][FK2]</FK></TestCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 5) {
                // case 43 Test composite foreign key value with info with bracket
                // 2 [FK1|AAA][FK2|AAA]|FKInfo1|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestCompositeFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1|AAA][FK2|AAA]</FK></TestCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 6) {
                // case 44 Test composite foreign key value with info with bracket
                // 3 [FK1][FK2]-FKInfo1
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestCompositeFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1][FK2]</FK></TestCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 7) {
                // case 45 Test composite foreign key value with info with bracket
                // 3 [FK1-AAA][FK2-AAA]-FKInfo1
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestCompositeFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AAA][FK2-AAA]</FK></TestCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 8) {
                // case 46 Test composite foreign key value with info with bracket
                // 4 [FK1][FK2]-FKInfo1-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestCompositeFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1][FK2]</FK></TestCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 9) {
                // case 47 Test composite foreign key value with info with bracket
                // 4 [FK1-AAA][FK2-AAA]-FKInfo1-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestCompositeFKWithInfo/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK1-AAA][FK2-AAA]</FK></TestCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            }
        }

        entityModel = getEntityModel("UploadTestForeignKey.xsd", "UploadTestForeignKey", "TestMultipleCompositeFK", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        service = new TestUploadService(entityModel, fileType, partialUpdateFlag, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        file = new File(this.getClass().getResource("TestMultipleCompositeFK.xls").getFile()); //$NON-NLS-1$
        fileInputStream = new FileInputStream(file);
        poiFSFile = new POIFSFileSystem(fileInputStream);
        workBook = new HSSFWorkbook(poiFSFile);
        sheet = workBook.getSheetAt(0);
        rowIterator = sheet.rowIterator();
        rowNumber = 0;
        while (rowIterator.hasNext()) {
            rowNumber++;
            Row row = rowIterator.next();
            if (rowNumber == 1) {
                continue;
            } else if (rowNumber == 2) {
                // case 48 Test multiple composite foreign key value with bracket like [FK11][FK12]|[FK21][FK22]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11][FK12]</FK><FK>[FK21][FK22]</FK></TestMultipleCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 3) {
                // case 49 Test multiple composite foreign key value with bracket like [FK11|AAA][FK12]|[FK21][FK22]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11|AAA][FK12]</FK><FK>[FK21][FK22]</FK></TestMultipleCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 4) {
                // case 50 Test multiple composite foreign key value with bracket like [FK11][FK12|AAA]|[FK21][FK22]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11][FK12|AAA]</FK><FK>[FK21][FK22]</FK></TestMultipleCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 5) {
                // case 51 Test multiple composite foreign key value with bracket like [FK11][FK12]|[FK21|AAA][FK22]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11][FK12]</FK><FK>[FK21|AAA][FK22]</FK></TestMultipleCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 6) {
                // case 52 Test multiple composite foreign key value with bracket like [FK11][FK12]|[FK21][FK22|AAA]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11][FK12]</FK><FK>[FK21][FK22|AAA]</FK></TestMultipleCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 7) {
                // case 53 Test multiple composite foreign key value with bracket like
                // [FK11|AAA][FK12|AAA]|[FK21|AAA][FK22|AAA]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11|AAA][FK12|AAA]</FK><FK>[FK21|AAA][FK22|AAA]</FK></TestMultipleCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 8) {
                // case 54 Test multiple composite foreign key value with bracket like [FK11-AAA][FK12-AAA]|[FK21][FK22]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11-AAA][FK12-AAA]</FK><FK>[FK21][FK22]</FK></TestMultipleCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 9) {
                // case 55 Test multiple composite foreign key value with bracket like [FK11][FK12-AAA]|[FK21-AAA][FK22]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11][FK12-AAA]</FK><FK>[FK21-AAA][FK22]</FK></TestMultipleCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 10) {
                // case 56 Test multiple composite foreign key value with bracket like [FK11][FK12]|[FK21-AAA][FK22-AAA]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11][FK12]</FK><FK>[FK21-AAA][FK22-AAA]</FK></TestMultipleCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 11) {
                // case 57 Test multiple composite foreign key value with bracket like [FK11-AAA][FK12]|[FK21][FK22-AAA]
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFK"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFK/FK", service.getExcelFieldValue(row.getCell(2)),
                        row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFK xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11-AAA][FK12]</FK><FK>[FK21][FK22-AAA]</FK></TestMultipleCompositeFK>", //$NON-NLS-1$
                        document.asXML());
            }
        }

        entityModel = getEntityModel("UploadTestForeignKey.xsd", "UploadTestForeignKey", "TestMultipleCompositeFKWithInfo", keys); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        service = new TestUploadService(entityModel, fileType, partialUpdateFlag, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        file = new File(this.getClass().getResource("TestMultipleCompositeFKWithInfo.xls").getFile()); //$NON-NLS-1$
        fileInputStream = new FileInputStream(file);
        poiFSFile = new POIFSFileSystem(fileInputStream);
        workBook = new HSSFWorkbook(poiFSFile);
        sheet = workBook.getSheetAt(0);
        rowIterator = sheet.rowIterator();
        rowNumber = 0;
        while (rowIterator.hasNext()) {
            rowNumber++;
            Row row = rowIterator.next();
            if (rowNumber == 1) {
                continue;
            } else if (rowNumber == 2) {
                // case 58 Test multiple composite foreign key value with info without bracket like
                // [FK11][FK12]|FKInfo1|[FK21][FK22]|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFKWithInfo/FK",
                        service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11][FK12]</FK><FK>[FK21][FK22]</FK></TestMultipleCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 3) {
                // case 59 Test multiple composite foreign key value with info without bracket like
                // [FK11|AAA][FK12]|FKInfo1|[FK21][FK22|AAA]|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFKWithInfo/FK",
                        service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11|AAA][FK12]</FK><FK>[FK21][FK22|AAA]</FK></TestMultipleCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 4) {
                // case 60 Test multiple composite foreign key value with info without bracket like
                // [FK11][FK12|AAA]|FKInfo1|[FK21|AAA][FK22]|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFKWithInfo/FK",
                        service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11][FK12|AAA]</FK><FK>[FK21|AAA][FK22]</FK></TestMultipleCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 5) {
                // case 61 Test multiple composite foreign key value with info without bracket like
                // [FK11][FK12]|FKInfo1|FKInfo2|[FK21][FK22]|FKInfo1|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFKWithInfo/FK",
                        service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11][FK12]</FK><FK>[FK21][FK22]</FK></TestMultipleCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 6) {
                // case 62 Test multiple composite foreign key value with info without bracket like
                // [FK11|AAA][FK12]|FKInfo1|FKInfo2|[FK21][FK22|AAA]|FKInfo1|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFKWithInfo/FK",
                        service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11|AAA][FK12]</FK><FK>[FK21][FK22|AAA]</FK></TestMultipleCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 7) {
                // case 63 Test multiple composite foreign key value with info without bracket like
                // [FK11|AAA][FK12]|FKInfo1|FKInfo2|[FK21|AAA][FK22]|FKInfo1|FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFKWithInfo/FK",
                        service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11|AAA][FK12]</FK><FK>[FK21|AAA][FK22]</FK></TestMultipleCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 8) {
                // case 64 Test multiple composite foreign key value with info without bracket like
                // [FK11][FK12]-FKInfo1|[FK21][FK22]-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFKWithInfo/FK",
                        service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11][FK12]</FK><FK>[FK21][FK22]</FK></TestMultipleCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 9) {
                // case 65 Test multiple composite foreign key value with info without bracket like
                // [FK11][FK12-AAA]-FKInfo1|[FK21][FK22-AAA]-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFKWithInfo/FK",
                        service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11][FK12-AAA]</FK><FK>[FK21][FK22-AAA]</FK></TestMultipleCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 10) {
                // case 66 Test multiple composite foreign key value with info without bracket like
                // [FK11-AAA][FK12]-FKInfo1|[FK21-AAA][FK22]-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFKWithInfo/FK",
                        service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11-AAA][FK12]</FK><FK>[FK21-AAA][FK22]</FK></TestMultipleCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 11) {
                // case 67 Test multiple composite foreign key value with info without bracket like
                // [FK11][FK12]-FKInfo1-FKInfo2|[FK21][FK22]-FKInfo1-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFKWithInfo/FK",
                        service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11][FK12]</FK><FK>[FK21][FK22]</FK></TestMultipleCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 12) {
                // case 68 Test multiple composite foreign key value with info without bracket like
                // [FK11-AAA][FK12]-FKInfo1-FKInfo2|[FK21][FK22-AAA]-FKInfo1-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFKWithInfo/FK",
                        service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11-AAA][FK12]</FK><FK>[FK21][FK22-AAA]</FK></TestMultipleCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            } else if (rowNumber == 13) {
                // case 69 Test multiple composite foreign key value with info without bracket like
                // [FK11-AAA][FK12-AAA]-FKInfo1-FKInfo2|[FK21][FK22]-FKInfo1-FKInfo2
                document = XmlUtil.parseDocument(org.talend.mdm.webapp.browserecords.server.util.CommonUtil
                        .getSubXML(entityModel.getTypeModel("TestMultipleCompositeFKWithInfo"), null, null, "en"));
                Element currentElement = document.getRootElement();
                service.resetMultiNodeMap();
                service.fillFieldValue(currentElement, "TestMultipleCompositeFKWithInfo/FK",
                        service.getExcelFieldValue(row.getCell(2)), row, null);
                assertEquals(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TestMultipleCompositeFKWithInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name/><FK>[FK11-AAA][FK12-AAA]</FK><FK>[FK21][FK22]</FK></TestMultipleCompositeFKWithInfo>", //$NON-NLS-1$
                        document.asXML());
            }
        }
    }

    protected EntityModel getEntityModel(String xsdFileName, String dataModel, String concept, String[] keys) throws Exception {
        String xsd = getFileContent(xsdFileName);
        String[] roles = { "System_Admin", "administration" }; //$NON-NLS-1$//$NON-NLS-2$
        entityModel = new EntityModel();
        PowerMockito.mockStatic(Util.class);
        Mockito.when(Util.isEnterprise()).thenReturn(false);
        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(dataModel)));
        DataModelHelper.parseSchema(dataModel, concept, DataModelHelper.convertXsd2ElDecl(concept, xsd), keys, entityModel,
                Arrays.asList(roles));
        return entityModel;
    }

    private String getFileContent(String fileName) throws IOException {
        InputStream resourceAsStream = this.getClass().getResourceAsStream(fileName);
        BufferedReader in = new BufferedReader(new InputStreamReader(resourceAsStream));
        StringBuffer buffer = new StringBuffer();
        String line = ""; //$NON-NLS-1$
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

    public class TestUploadService extends UploadService {

        public TestUploadService(EntityModel entityModel, String fileType, boolean isPartialUpdate, boolean headersOnFirstLine,
                Map<String, Boolean> headerVisibleMap, List<String> inheritanceNodePathList, String multipleValueSeparator,
                String seperator, String encoding, char textDelimiter, String language) {
            super(entityModel, fileType, isPartialUpdate, headersOnFirstLine, headerVisibleMap, inheritanceNodePathList, multipleValueSeparator,
                    seperator, encoding, textDelimiter, language);
        }

        @Override
        protected String getCurrentDataCluster() throws Exception {
            return "UploadTestModel"; //$NON-NLS-1$
        }

        @Override
        protected String getCurrentDataModel() throws Exception {
            return "UploadTestModel"; //$NON-NLS-1$
        }

        @Override
        protected Document getItemForPartialUpdate(EntityModel model, String[] keys, int rowNumber) throws RemoteException, XtentisWebappException, Exception {
            String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Picture/><Id>i</Id><Name>1</Name><Description>1</Description><Features><Sizes><Size>Medium</Size><Size>Small</Size></Sizes><Colors><Color>White</Color><Color>Light Blue</Color><Color>Lemon</Color></Colors></Features><Availability/><Price>1.00</Price><Family/><OnlineStore/><Stores><Store/></Stores></Product>"; //$NON-NLS-1$
            Document document = DocumentHelper.parseText(expectedResult);
            return document;
        }

        public void resetMultiNodeMap() {
            this.multiNodeMap = new HashMap<String, List<Element>>();
        }
    }
}
