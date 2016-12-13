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

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.talend.mdm.webapp.journal.server.util.JournalExcel;


/**
 * created by talend2 on 2013-2-19
 * Detailled comment
 *
 */
public class JournalExcelTest extends TestCase {

    public void testJournalExcel() {
        JournalExcel journalExcel = new JournalExcel("en"); //$NON-NLS-1$
        assertEquals(true, journalExcel.getFileName().startsWith("Journal_")); //$NON-NLS-1$
        HSSFSheet sheet = journalExcel.getWorkbook().getSheetAt(0);
        HSSFRow row = sheet.getRow(0);
        assertEquals("Data Container", row.getCell(0).getStringCellValue()); //$NON-NLS-1$
        assertEquals("Data Model", row.getCell(1).getStringCellValue()); //$NON-NLS-1$
        assertEquals("Entity", row.getCell(2).getStringCellValue()); //$NON-NLS-1$
        assertEquals("Key", row.getCell(3).getStringCellValue()); //$NON-NLS-1$
        assertEquals("Operation Type", row.getCell(4).getStringCellValue()); //$NON-NLS-1$
        assertEquals("Operation Time", row.getCell(5).getStringCellValue()); //$NON-NLS-1$
        assertEquals("Source", row.getCell(6).getStringCellValue()); //$NON-NLS-1$
        assertEquals("User Name", row.getCell(7).getStringCellValue()); //$NON-NLS-1$
    }
}
