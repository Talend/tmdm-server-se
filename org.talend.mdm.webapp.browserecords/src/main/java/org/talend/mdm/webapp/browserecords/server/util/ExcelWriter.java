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
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.talend.mdm.webapp.browserecords.shared.Constants;

@SuppressWarnings("nls")
public class ExcelWriter extends DownloadWriter {

    private final static String SHEET_LABEL = "Talend MDM";

    private XSSFWorkbook workbook = null;

    private XSSFCellStyle cs = null;

    private XSSFSheet sheet = null;

    private XSSFRow row = null;

    private int rowNumber = 1;

    public ExcelWriter(String concept, String viewPk, List<String> idsList, String[] headerArray, String[] xpathArray,
            String criteria, String multipleValueSeparator, String fkDisplay, boolean fkResovled, Map<String, String> colFkMap,
            Map<String, List<String>> fkMap, boolean isStaging, String language) {
        super(concept, viewPk, idsList, headerArray, xpathArray, criteria, multipleValueSeparator, fkDisplay, fkResovled,
                colFkMap, fkMap, isStaging, language);
    }

    @Override
    public void generateFile() {
        workbook = new XSSFWorkbook();
        cs = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        cs.setFont(font);
        sheet = workbook.createSheet(SHEET_LABEL);
        sheet.setDefaultColumnWidth((short) 20);
        row = sheet.createRow((short) 0);
        writeHeader();
    }

    @Override
    public void writeHeader() {
        for (int i = 0; i < headerArray.length; i++) {
            XSSFCell cell = row.createCell((short) i);
            cell.setCellValue(headerArray[i]);
            cell.setCellStyle(cs);
        }
    }

    @Override
    public void generateLine() throws Exception {
        row = sheet.createRow((short) rowNumber);
        rowNumber++;
    }

    @Override
    public void writeValue(String value) {
        row.createCell((short) columnIndex).setCellValue(value);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        workbook.write(out);
    }

    @Override
    public String generateFileName(String name) {
        return super.generateFileName(name) + "." + Constants.FILE_TYPE_EXCEL;
    }
}
