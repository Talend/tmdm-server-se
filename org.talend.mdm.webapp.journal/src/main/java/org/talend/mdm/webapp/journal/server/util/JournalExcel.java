/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.server.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;


/**
 * created by talend2 on 2013-2-19
 * Detailled comment
 *
 */
public class JournalExcel {
    
    private HSSFWorkbook workbook;
    private HSSFSheet sheet;
    private HSSFCellStyle cellStyle;
    private Locale locale;
    private Messages messages;
    private String fileName;
    
    public JournalExcel(String language) {
        this.locale = new Locale(language);
        this.messages = MessagesFactory.getMessages(
                "org.talend.mdm.webapp.journal.client.i18n.JournalMessages", JournalExcel.class.getClassLoader()); //$NON-NLS-1$
        createWorkbook();
        createSheet();
        createCellStyle();
        createHeader();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); //$NON-NLS-1$
        fileName = "Journal_" + dateFormat.format(new Date()) + ".xls"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    private void createWorkbook() {
        workbook = new HSSFWorkbook();
    }
    
    private void createSheet() {
        sheet = workbook.createSheet("Talend MDM"); //$NON-NLS-1$
        sheet.setDefaultColumnWidth((short) 20);
    }
    
    private void createCellStyle() {
        cellStyle = workbook.createCellStyle();
        HSSFFont f = workbook.createFont();
        f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        cellStyle.setFont(f);
    }
    
    private void createHeader() {
        HSSFRow row = sheet.createRow((short) 0);

        row.createCell((short) 0).setCellValue(messages.getMessage(locale, "data_container_label")); //$NON-NLS-1$
        row.getCell((short) 0).setCellStyle(cellStyle);

        row.createCell((short) 1).setCellValue(messages.getMessage(locale, "data_model_label")); //$NON-NLS-1$
        row.getCell((short) 1).setCellStyle(cellStyle);

        row.createCell((short) 2).setCellValue(messages.getMessage(locale, "entity_label")); //$NON-NLS-1$
        row.getCell((short) 2).setCellStyle(cellStyle);

        row.createCell((short) 3).setCellValue(messages.getMessage(locale, "key_label")); //$NON-NLS-1$
        row.getCell((short) 3).setCellStyle(cellStyle);

        row.createCell((short) 4).setCellValue(messages.getMessage(locale, "operation_type_label")); //$NON-NLS-1$
        row.getCell((short) 4).setCellStyle(cellStyle);

        row.createCell((short) 5).setCellValue(messages.getMessage(locale, "operation_time_label")); //$NON-NLS-1$
        row.getCell((short) 5).setCellStyle(cellStyle);

        row.createCell((short) 6).setCellValue(messages.getMessage(locale, "source_label")); //$NON-NLS-1$
        row.getCell((short) 6).setCellStyle(cellStyle);

        row.createCell((short) 7).setCellValue(messages.getMessage(locale, "user_name_label")); //$NON-NLS-1$
        row.getCell((short) 7).setCellStyle(cellStyle);
    }
    
    public HSSFRow createRow(short number) {
        return sheet.createRow(number);
    }
    
    public HSSFWorkbook getWorkbook() {
        return workbook;
    }
    
    public String getFileName() {
        return fileName;
    }
}
