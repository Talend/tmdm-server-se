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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit3.PowerMockSuite;
import org.talend.mdm.webapp.base.server.util.Constants;
import org.talend.mdm.webapp.journal.server.service.WebServiceMock;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;

import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSDataModelPKArray;
import com.amalto.core.webservice.WSGetConceptsInDataCluster;
import com.amalto.core.webservice.WSGetItems;
import com.amalto.core.webservice.WSGetItemsSort;
import com.amalto.core.webservice.WSRegexDataModelPKs;
import com.amalto.core.webservice.WSStringArray;
import com.amalto.core.webservice.WSWhereCondition;
import com.amalto.core.webservice.WSWhereItem;
import com.amalto.core.webservice.WSWhereOperator;
import com.amalto.core.webservice.XtentisPort;
import com.extjs.gxt.ui.client.Style.SortDir;

import static org.mockito.Mockito.when;

/**
 * created by talend2 on 2013-1-29 Detailled comment
 * 
 */
@PrepareForTest({ XtentisPort.class, WSDataModelPKArray.class, WSRegexDataModelPKs.class, com.amalto.webapp.core.util.Util.class })
@SuppressWarnings("nls")
public class UtilTest extends TestCase {
    
    private JournalSearchCriteria criteria;   

    private WebServiceMock mock = new WebServiceMock();

    @SuppressWarnings("unchecked")
    public static TestSuite suite() throws Exception {
        return new PowerMockSuite("Unit tests for " + UtilTest.class.getSimpleName(), UtilTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        criteria = new JournalSearchCriteria();
        criteria.setEntity("TestEntity");
        criteria.setStartDate(new Date());
        criteria.setKey("1");
        criteria.setOperationType(UpdateReportPOJO.OPERATION_TYPE_CREATE);
        criteria.setSource("genericUI");
        criteria.setStrict(true);

        PowerMockito.mockStatic(com.amalto.webapp.core.util.Util.class);
        XtentisPort port = PowerMockito.mock(XtentisPort.class);
        Mockito.when(com.amalto.webapp.core.util.Util.getPort()).thenReturn(port);
        com.amalto.core.webservice.WSDataModelPK[] wsDataModelsPKs = { new WSDataModelPK("Product"), new WSDataModelPK("Test") };
        WSDataModelPKArray array = PowerMockito.mock(WSDataModelPKArray.class);
        when(port.getDataModelPKs(Mockito.any(WSRegexDataModelPKs.class))).thenReturn(array);
        when(array.getWsDataModelPKs()).thenReturn(wsDataModelsPKs);
        String[] entities = { "Store", "TestEntity" };
        WSStringArray entityArray = new WSStringArray(entities);
        when(port.getConceptsInDataCluster(Mockito.any(WSGetConceptsInDataCluster.class))).thenReturn(entityArray);
    }

    public void testBuildWhereItems() throws Exception {
        Boolean conceptFlag = false;
        Boolean modelFlag = false;
        mock.setForbiddenconceptName("Store");
        mock.setEnterpriseVersion(true);

        List<WSWhereItem> conditions = Util.buildWhereItems(criteria, mock);
        for (WSWhereItem whereItem : conditions) {
            WSWhereCondition condition = whereItem.getWhereCondition();
            if (condition == null) {
                condition = whereItem.getWhereOr().getWhereItems()[0].getWhereCondition();
                assertEquals(condition.getLeftPath(), "DataModel");
                assertEquals(WSWhereOperator.EQUALS, condition.getOperator());
                assertEquals(condition.getRightValueOrPath(), "Product");
                condition = whereItem.getWhereOr().getWhereItems()[1].getWhereCondition();
                assertEquals(condition.getRightValueOrPath(), "Test");
                modelFlag = true;
            }
            if ("Concept".equals(condition.getLeftPath())) {
                if (condition.getRightValueOrPath().equals("TestEntity")) {
                    assertEquals(WSWhereOperator.EQUALS, condition.getOperator());
                } else if (condition.getRightValueOrPath().equals("Store")) {
                    conceptFlag = true;
                    assertEquals(WSWhereOperator.NOT_EQUALS, condition.getOperator());
                }
            } else if ("Key".equals(condition.getLeftPath())) {
                assertEquals(WSWhereOperator.EQUALS, condition.getOperator());
                assertEquals(condition.getRightValueOrPath(), "1");
            } else if ("Source".equals(condition.getLeftPath())) {
                assertEquals(WSWhereOperator.EQUALS, condition.getOperator());
                assertEquals(condition.getRightValueOrPath(), "genericUI");
            } else if ("OperationType".equals(condition.getLeftPath())) {
                assertEquals(condition.getRightValueOrPath(), UpdateReportPOJO.OPERATION_TYPE_CREATE);
            } else if ("TimeInMillis".equals(condition.getLeftPath())) {
                assertEquals(WSWhereOperator.GREATER_THAN_OR_EQUAL, condition.getOperator());
            }
        }
        assertTrue(conceptFlag);
        assertTrue(modelFlag);

        conceptFlag = false;
        modelFlag = false;
        criteria.setDataModel("Product");
        mock.setEnterpriseVersion(false);
        criteria.setStrict(false);
        conditions = Util.buildWhereItems(criteria, mock);
        for (WSWhereItem whereItem : conditions) {
            WSWhereCondition condition = whereItem.getWhereCondition();
            if ("Concept".equals(condition.getLeftPath())) {
                if (condition.getRightValueOrPath().equals("TestModel")) {
                    assertEquals(WSWhereOperator.CONTAINS, condition.getOperator());
                } else if (condition.getRightValueOrPath().equals("Store")) {
                    assertEquals(WSWhereOperator.NOT_EQUALS, condition.getOperator());
                    conceptFlag = true;
                }
            } else if ("Key".equals(condition.getLeftPath())) {
                assertEquals(WSWhereOperator.CONTAINS, condition.getOperator());
                assertEquals(condition.getRightValueOrPath(), "1");
            } else if ("Source".equals(condition.getLeftPath())) {
                assertEquals(WSWhereOperator.CONTAINS, condition.getOperator());
                assertEquals(condition.getRightValueOrPath(), "genericUI");
            } else if ("OperationType".equals(condition.getLeftPath())) {
                assertEquals(condition.getRightValueOrPath(), UpdateReportPOJO.OPERATION_TYPE_CREATE);
            } else if ("TimeInMillis".equals(condition.getLeftPath())) {
                assertEquals(WSWhereOperator.GREATER_THAN_OR_EQUAL, condition.getOperator());
            } else if ("DataModel".equals(condition.getLeftPath())) {
                assertEquals(WSWhereOperator.EQUALS, condition.getOperator());
                assertEquals(condition.getRightValueOrPath(), "Product");
                modelFlag = true;
            }
        }
        assertFalse(conceptFlag);
        assertTrue(modelFlag);
    }
    
    public void testBuildGetItem() throws Exception {
        List<WSWhereItem> conditions = Util.buildWhereItems(criteria, mock);
        WSGetItems item = Util.buildGetItem(conditions, 0, 20);
        assertEquals("Update", item.getConceptName());
        assertNotNull(item.getWhereItem());
        assertEquals(true, item.getTotalCountOnFirstResult().booleanValue());
        assertEquals(0, item.getSkip());
        assertEquals(20, item.getMaxItems());
    }
    
    public void testBuildGetItemsSort() throws Exception {
        List<WSWhereItem> conditions = Util.buildWhereItems(criteria, mock);
        String sort = "ASC";
        if (SortDir.ASC.equals(SortDir.findDir(sort))) {
            sort = Constants.SEARCH_DIRECTION_ASC;
        } else if (SortDir.DESC.equals(SortDir.findDir(sort))) {
            sort = Constants.SEARCH_DIRECTION_DESC;
        }
        assertEquals(Constants.SEARCH_DIRECTION_ASC, sort);
        WSGetItemsSort itemSort = Util.buildGetItemsSort(conditions, 0, 20, "key", sort);
        assertNotNull(itemSort.getConceptName());
        assertEquals(true, itemSort.getTotalCountOnFirstResult().booleanValue());
        assertEquals(0, itemSort.getSkip());
        assertEquals(20, itemSort.getMaxItems());
        assertEquals(Constants.SEARCH_DIRECTION_ASC,itemSort.getDir());
        assertEquals("Update/Key",itemSort.getSort());
    }
    
    public void testGetOrderXPath() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = Util.class.getDeclaredMethod("getOrderXPath", String.class);
        method.setAccessible(true);
        Object returnValue = method.invoke(null, new Object[] { "dataContainer" });
        assertEquals("Update/DataCluster", returnValue);
        returnValue = method.invoke(null, new Object[] { "dataModel" });
        assertEquals("Update/DataModel", returnValue);
        returnValue = method.invoke(null, new Object[] { "entity" });
        assertEquals("Update/Concept", returnValue);
        returnValue = method.invoke(null, new Object[] { "key" });
        assertEquals("Update/Key", returnValue);
        returnValue = method.invoke(null, new Object[] { "operationType" });
        assertEquals("Update/OperationType", returnValue);
        returnValue = method.invoke(null, new Object[] { "operationTime" });
        assertEquals("Update/TimeInMillis", returnValue);
        returnValue = method.invoke(null, new Object[] { "source" });
        assertEquals("Update/Source", returnValue);
        returnValue = method.invoke(null, new Object[] { "userName" });
        assertEquals("Update/UserName", returnValue);
        returnValue = method.invoke(null, new Object[] { "otherField1" });
        assertEquals("Update/otherField1", returnValue);
        returnValue = method.invoke(null, new Object[] { "otherField2" });
        assertEquals("Update/otherField2", returnValue);
        returnValue = method.invoke(null, new Object[] { "" });
        assertNull(returnValue);
        returnValue = method.invoke(null, new Object[] { null });
        assertNull(returnValue);
    }
    
}
