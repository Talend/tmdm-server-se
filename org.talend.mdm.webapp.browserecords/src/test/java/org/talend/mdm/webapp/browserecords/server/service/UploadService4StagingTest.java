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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestSuite;

import org.powermock.modules.junit3.PowerMockSuite;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecordsinstaging.server.service.UploadService4Staging;

import com.amalto.core.webservice.WSPutItemWithReport;

public class UploadService4StagingTest extends UploadServiceTest {

    @SuppressWarnings("unchecked")
    public static TestSuite suite() throws Exception {
        return new PowerMockSuite("Unit tests for " + UploadService4StagingTest.class.getSimpleName(),
                UploadService4StagingTest.class);
    }

    @Override
    public void testUploadModel_Polymorphism() throws Exception {
        // Set test parameter value
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
        file = new File(this.getClass().getResource("UploadTestModel_Polymorphism-Staging.xls").getFile()); //$NON-NLS-1$
        UploadService service = new TestUploadService4Staging(entityModel, fileType, isPartialUpdate, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        List<WSPutItemWithReport> wsPutItemWithReportList = service.readUploadFile(file);
        assertEquals(record1, wsPutItemWithReportList.get(0).getWsPutItem().getXmlString());
        assertEquals(record2, wsPutItemWithReportList.get(1).getWsPutItem().getXmlString());
        assertEquals(record3, wsPutItemWithReportList.get(2).getWsPutItem().getXmlString());
        assertEquals(record4, wsPutItemWithReportList.get(3).getWsPutItem().getXmlString());
        assertEquals(record5, wsPutItemWithReportList.get(4).getWsPutItem().getXmlString());

        // test upload csv file
        fileType = "csv"; //$NON-NLS-1$
        file = new File(this.getClass().getResource("UploadTestModel_Polymorphism-Staging.csv").getFile()); //$NON-NLS-1$
        service = new TestUploadService4Staging(entityModel, fileType, isPartialUpdate, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
        wsPutItemWithReportList = service.readUploadFile(file);
        assertEquals(record1, wsPutItemWithReportList.get(0).getWsPutItem().getXmlString());
        assertEquals(record2, wsPutItemWithReportList.get(1).getWsPutItem().getXmlString());
        assertEquals(record3, wsPutItemWithReportList.get(2).getWsPutItem().getXmlString());
        assertEquals(record4, wsPutItemWithReportList.get(3).getWsPutItem().getXmlString());
        assertEquals(record5, wsPutItemWithReportList.get(4).getWsPutItem().getXmlString());
    }

    public class TestUploadService4Staging extends UploadService4Staging {

        public TestUploadService4Staging(EntityModel entityModel, String fileType, boolean isPartialUpdate, boolean headersOnFirstLine,
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

    }

}
