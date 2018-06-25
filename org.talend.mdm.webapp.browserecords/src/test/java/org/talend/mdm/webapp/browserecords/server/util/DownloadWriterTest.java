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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit3.PowerMockSuite;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.commmon.util.datamodel.management.DataModelID;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.server.actions.BrowseRecordsActionTest;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.DataModelHelper;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.SchemaMockAgent;

import com.amalto.core.webservice.WSGetItem;
import com.amalto.core.webservice.WSGetView;
import com.amalto.core.webservice.WSItem;
import com.amalto.core.webservice.WSStringArray;
import com.amalto.core.webservice.WSView;
import com.amalto.core.webservice.WSViewSearch;
import com.amalto.core.webservice.WSWhereCondition;
import com.amalto.core.webservice.XtentisPort;

import junit.framework.TestCase;
import junit.framework.TestSuite;

@PrepareForTest({ org.talend.mdm.webapp.base.server.util.CommonUtil.class,
        org.talend.mdm.webapp.browserecords.server.util.CommonUtil.class, XtentisPort.class })
@SuppressWarnings("nls")
public class DownloadWriterTest extends TestCase {

    @SuppressWarnings("unchecked")
    public static TestSuite suite() throws Exception {
        return new PowerMockSuite("Unit tests for " + DownloadWriterTest.class.getSimpleName(), DownloadWriterTest.class);
    }

    public void testCSVWriter() throws Exception {
        MDMConfiguration.createConfiguration("", true);
        EntityModel entity = new EntityModel();
        String[] roles = { "Demo_Manager", "System_Admin", "authenticated", "administration" };
        InputStream stream = BrowseRecordsActionTest.class.getResourceAsStream("../../ProductDemo.xsd");
        String xsd = inputStream2String(stream);
        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID("Product")));
        DataModelHelper.parseSchema("Product", "Product", DataModelHelper.convertXsd2ElDecl("Product", xsd), new String[] {},
                entity, Arrays.asList(roles));
        entity.setKeys(new String[] { "Product/Id" });

        WSView wsView = new WSView();
        wsView.setDescription("[FR:Produit][EN:Product]");
        wsView.setName("Browse_items_Product");
        wsView.setWhereConditions(new WSWhereCondition[] {});
        wsView.setViewableBusinessElements(new String[] { "Product/Id", "Product/Name", "Product/Family", "Product/Price",
                "Product/Availability", "Product/Description", "Product/Features", "Product/Features/Sizes",
                "Product/Features/Colors", "Product/Features/Sizes/Size", "Product/Features/Colors/Color" });
        wsView.setSearchableBusinessElements(
                new String[] { "Product/Id", "Product/Name", "Product/Description", "Product/Price", "Product/Name" });

        WSItem wsItem = new WSItem();
        wsItem.setContent("<ProductFamily><Id>7</Id><Name>F33</Name></ProductFamily>");

        String concept = "Product";
        String viewPk = "Browse_items_Product";
        List<String> idsList = new ArrayList<String>();
        idsList.add("1");
        String[] headerArray = { "Id", "Name", "Family", "Price", "Availability", "Description", "Features", "Features/Sizes",
                "Features/Colors", "Features/Sizes/Size", "Features/Colors/Color" };
        String[] xpathArray = { "Product/Id", "Product/Name", "Product/Family", "Product/Price", "Product/Availability",
                "Product/Description", "Product/Features", "Product/Features/Sizes", "Product/Features/Colors",
                "Product/Features/Sizes/Size", "Product/Features/Colors/Color" };
        String criteria = "Product/Id CONTAINS *";
        String multipleValueSeparator = "|";
        String fkDisplay = "Id-FKInfo";
        boolean fkResovled = true;
        Map<String, String> colFkMap = new HashMap<String, String>();
        Map<String, List<String>> fkMap = new HashMap<String, List<String>>();
        String language = "en";
        DownloadUtil.assembleFkMap(colFkMap, fkMap, "<fkColXPath><item>Product/Family,ProductFamily/Id</item></fkColXPath>",
                "<fkInfo><item>ProductFamily/Name</item></fkInfo>");
        String[] stringArray = { "<totalCount>1</totalCount>",
                "<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>1</Id><Name>2,2\"</Name><Family>[7]</Family><Price>1.00</Price><Availability>true</Availability><Description>1</Description><Features></Features><Sizes></Sizes><Colors></Colors><Size>Small,Medium,Large</Size><Color>White,Light Blue,Light Pink</Color><taskId/></result>" };
        WSStringArray wsStringArray = new WSStringArray(stringArray);

        XtentisPort port = PowerMockito.mock(XtentisPort.class);
        PowerMockito.mockStatic(org.talend.mdm.webapp.base.server.util.CommonUtil.class);
        PowerMockito.mockStatic(org.talend.mdm.webapp.browserecords.server.util.CommonUtil.class);

        Mockito.when(org.talend.mdm.webapp.base.server.util.CommonUtil.getPort()).thenReturn(port);
        Mockito.when(org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getEntityModel(Mockito.anyString(),
                Mockito.anyString())).thenReturn(entity);
        Mockito.when(org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getCurrentDataCluster(Mockito.anyBoolean()))
                .thenReturn("Product");
        Mockito.when(port.getView(Mockito.any(WSGetView.class))).thenReturn(wsView);
        Mockito.when(port.getItem(Mockito.any(WSGetItem.class))).thenReturn(wsItem);
        Mockito.when(port.viewSearch(Mockito.any(WSViewSearch.class))).thenReturn(wsStringArray);

        String expectedResult = "Id,Name,Family,Price,Availability,Description,Features,Features/Sizes,Features/Colors,Features/Sizes/Size,Features/Colors/Color"
                + System.getProperty("line.separator")
                + "1,2&#44;2&#34;,[7]|F33,1.00,true,1,,,,Small|Medium|Large,White|Light Blue|Light Pink";
        DownloadWriter writer = new CSVWriter(concept, viewPk, idsList, headerArray, xpathArray, criteria, multipleValueSeparator,
                fkDisplay, fkResovled, colFkMap, fkMap, false, language);
        writer.writeFile();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.write(out);
        assertEquals(expectedResult, out.toString());

        assertEquals("Product.csv", writer.generateFileName("Product"));
        writer = new CSVWriter(concept, viewPk, idsList, headerArray, xpathArray, criteria, multipleValueSeparator, fkDisplay,
                fkResovled, colFkMap, fkMap, true, language);
        assertEquals("Product-Staging.csv", writer.generateFileName("Product"));

        writer = new ExcelWriter(concept, viewPk, idsList, headerArray, xpathArray, criteria, multipleValueSeparator, fkDisplay,
                fkResovled, colFkMap, fkMap, false, language);
        writer.writeFile();
        File file = new File(this.getClass().getResource("TestDownload.xlsx").getFile());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        writer.write(fileOutputStream);
        fileOutputStream.close();
        FileInputStream fileInputStream = new FileInputStream(file);
        Workbook workBook = new XSSFWorkbook(fileInputStream);
        Sheet sheet = workBook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        int rowNumber = 0;
        while (rowIterator.hasNext()) {
            rowNumber++;
            Row row = rowIterator.next();
            if (rowNumber == 1) {
                continue;
            } else if (rowNumber == 2) {
                assertEquals("1", row.getCell(0).getRichStringCellValue().getString());
                assertEquals("2,2\"", row.getCell(1).getRichStringCellValue().getString());
                assertEquals("[7]|F33", row.getCell(2).getRichStringCellValue().getString());
                assertEquals("1.00", row.getCell(3).getRichStringCellValue().getString());
                assertEquals("true", row.getCell(4).getRichStringCellValue().getString());
                assertEquals("1", row.getCell(5).getRichStringCellValue().getString());
                assertEquals("", row.getCell(6).getRichStringCellValue().getString());
                assertEquals("", row.getCell(7).getRichStringCellValue().getString());
                assertEquals("", row.getCell(8).getRichStringCellValue().getString());
                assertEquals("Small|Medium|Large", row.getCell(9).getRichStringCellValue().getString());
                assertEquals("White|Light Blue|Light Pink", row.getCell(10).getRichStringCellValue().getString());
            }
        }

        assertEquals("Product.xlsx", writer.generateFileName("Product"));
        writer = new ExcelWriter(concept, viewPk, idsList, headerArray, xpathArray, criteria, multipleValueSeparator, fkDisplay,
                fkResovled, colFkMap, fkMap, true, language);
        assertEquals("Product-Staging.xlsx", writer.generateFileName("Product"));

        // Test export CSV File that contains title @xsi:type
        entity = new EntityModel();
        stream = BrowseRecordsActionTest.class.getResourceAsStream("../../ContractInheritance.xsd");
        xsd = inputStream2String(stream);
        DataModelHelper.parseSchema("Contract", "Contract", DataModelHelper.convertXsd2ElDecl("Contract", xsd), new String[] {},
                entity, Arrays.asList(roles));
        entity.setKeys(new String[] { "Contract/id" });
        concept = "Contract";
        viewPk = "Browse_items_Contract";
        String[] headerArray2 = { "id", "comment", "detail/@xsi:type", "enumEle", "detail/code" };
        String[] xpathArray2 = { "Contract/id", "Contract/comment", "Contract/detail/@xsi:type", "Contract/enumEle",
                "Contract/detail/code" };
        criteria = "Contract/id CONTAINS *";
        fkResovled = false;
        colFkMap = null;
        fkMap = null;
        String[] stringArray2 = { "<totalCount>2</totalCount>",
                "<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>2</id><comment>2</comment><xsi:type>ContractDetailSubType</xsi:type><enumEle>pending</enumEle><code>2</code><taskId/></result>",
                "<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>1</id><comment>1</comment><xsi:type>ContractDetailType</xsi:type><enumEle>pending</enumEle><code>1</code><taskId/></result>" };
        wsStringArray = new WSStringArray(stringArray2);

        Mockito.when(port.viewSearch(Mockito.any(WSViewSearch.class))).thenReturn(wsStringArray);
        Mockito.when(org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getEntityModel(Mockito.anyString(),
                Mockito.anyString())).thenReturn(entity);

        expectedResult = "id,comment,detail/@xsi:type,enumEle,detail/code" + System.getProperty("line.separator")
                + "2,2,,pending,2" + System.getProperty("line.separator") + "1,1,,pending,1";
        writer = new CSVWriter(concept, viewPk, idsList, headerArray2, xpathArray2, criteria, multipleValueSeparator, fkDisplay,
                fkResovled, colFkMap, fkMap, false, language);
        writer.writeFile();
        out = new ByteArrayOutputStream();
        writer.write(out);
        assertEquals(expectedResult, out.toString());
    }

    private String inputStream2String(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }
}
