/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.server.service;

import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.dom4j.tree.DefaultElement;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit3.PowerMockSuite;
import org.talend.mdm.webapp.journal.shared.JournalGridModel;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;
import org.talend.mdm.webapp.journal.shared.JournalTreeModel;

import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSDataModelPKArray;
import com.amalto.core.webservice.WSGetConceptsInDataCluster;
import com.amalto.core.webservice.WSRegexDataModelPKs;
import com.amalto.core.webservice.WSStringArray;
import com.amalto.core.webservice.XtentisPort;
import com.amalto.webapp.core.util.Util;
import com.extjs.gxt.ui.client.data.ModelData;
import com.sun.xml.xsom.XSElementDecl;

/**
 * DOC talend2 class global comment. Detailled comment
 */
@SuppressWarnings("nls")
@PrepareForTest({ Util.class, XtentisPort.class, WSDataModelPKArray.class, WSRegexDataModelPKs.class })
public class JournalDBServiceTest extends TestCase {

    private WebServiceMock mock = new WebServiceMock();

    JournalDBService journalDBService = new JournalDBService(mock);

    @SuppressWarnings("unchecked")
    public static TestSuite suite() throws Exception {
        return new PowerMockSuite("Unit tests for " + JournalDBServiceTest.class.getSimpleName(), JournalDBServiceTest.class);
    }

    @SuppressWarnings("unchecked")
    public void testGetResultListByCriteria() throws Exception {
        PowerMockito.mockStatic(com.amalto.webapp.core.util.Util.class);
        XtentisPort port = PowerMockito.mock(XtentisPort.class);
        Mockito.when(com.amalto.webapp.core.util.Util.getPort()).thenReturn(port);
        com.amalto.core.webservice.WSDataModelPK[] wsDataModelsPKs = { new WSDataModelPK("Product") };
        WSDataModelPKArray array = PowerMockito.mock(WSDataModelPKArray.class);
        when(port.getDataModelPKs(Mockito.any(WSRegexDataModelPKs.class))).thenReturn(array);
        when(array.getWsDataModelPKs()).thenReturn(wsDataModelsPKs);
        String[] entities = { "Store", "TestModel" };
        WSStringArray entityArray = new WSStringArray(entities);
        when(port.getConceptsInDataCluster(Mockito.any(WSGetConceptsInDataCluster.class))).thenReturn(entityArray);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        JournalSearchCriteria criteria = new JournalSearchCriteria();
        criteria.setEntity("TestModel");
        criteria.setStartDate(dateFormat.parse("2012-07-01"));
        criteria.setEndDate(dateFormat.parse("2012-09-30"));
        criteria.setKey("1");
        criteria.setOperationType(UpdateReportPOJO.OPERATION_TYPE_CREATE);
        criteria.setSource(UpdateReportPOJO.GENERIC_UI_SOURCE);

        Object[] result = null;
        List<JournalGridModel> journalGridModelList = null;
        JournalGridModel journalGridModel = null;

        result = journalDBService.getResultListByCriteria(criteria, 0, 20, "ASC", "key");
        journalGridModelList = (List<JournalGridModel>) result[1];
        assertEquals(20, journalGridModelList.size());
        assertEquals(20, result[0]);
        journalGridModel = journalGridModelList.get(0);
        assertEquals("Product", journalGridModel.getDataContainer());
        assertEquals("Product", journalGridModel.getDataModel());

        journalGridModel = journalGridModelList.get(2);
        assertEquals("T", journalGridModel.getDataContainer());
        assertEquals("T", journalGridModel.getDataModel());

    }

    public void testGetDetailTreeModel() throws Exception {
        PowerMockito.mockStatic(Util.class);
        Mockito.when(Util.isElementHiddenForCurrentUser(Mockito.any(XSElementDecl.class))).thenReturn(false);

        String[] ids = { "genericUI", "1360140140037" };
        JournalTreeModel journalTreeModel = journalDBService.getDetailTreeModel(ids);
        assertEquals("Update", journalTreeModel.getName());
        assertEquals(9, journalTreeModel.getChildCount());
        JournalTreeModel childModel = (JournalTreeModel) journalTreeModel.getChild(0);
        assertEquals("UserName:administrator", childModel.getName());
        childModel = (JournalTreeModel) journalTreeModel.getChild(1);
        assertEquals("Source:genericUI", childModel.getName());
        childModel = (JournalTreeModel) journalTreeModel.getChild(2);
        assertEquals("TimeInMillis:1361153957282", childModel.getName());
        childModel = (JournalTreeModel) journalTreeModel.getChild(3);
        assertEquals("OperationType:UPDATE", childModel.getName());
        childModel = (JournalTreeModel) journalTreeModel.getChild(4);
        assertEquals("Concept:Product", childModel.getName());
        childModel = (JournalTreeModel) journalTreeModel.getChild(5);
        assertEquals("DataCluster:Product", childModel.getName());
        childModel = (JournalTreeModel) journalTreeModel.getChild(6);
        assertEquals("DataModel:Product", childModel.getName());
        childModel = (JournalTreeModel) journalTreeModel.getChild(7);
        assertEquals("Key:1", childModel.getName());
        childModel = (JournalTreeModel) journalTreeModel.getChild(8);
        assertEquals("path:Name", ((JournalTreeModel) childModel.getChild(0)).getName());
        assertEquals("oldValue:1", ((JournalTreeModel) childModel.getChild(1)).getName());
        assertEquals("newValue:123", ((JournalTreeModel) childModel.getChild(2)).getName());
    }

    public void testGetComparisionTreeModel() throws NoSuchMethodException, InvocationTargetException, IllegalArgumentException,
            IllegalAccessException {
        String xmlString = "<result label=\"result\"><Update label=\"Update\"><UserName label=\"UserName\">Jennifer</UserName><Source label=\"Source\">genericUI</Source><TimeInMillis label=\"TimeInMillis\">1360032633336</TimeInMillis><OperationType label=\"OperationType\">UPDATE</OperationType><DataCluster label=\"DataCluster\">DStar</DataCluster><DataModel label=\"DataModel\">DStar</DataModel><Concept label=\"Concept\">Agency</Concept><Key label=\"Key\">2</Key><Item label=\"Item\"><path label=\"path\">Name</path><oldValue label=\"oldValue\">23456</oldValue><newValue label=\"newValue\">34567</newValue></Item></Update></result>";
        JournalTreeModel returnValue = journalDBService.getComparisionTreeModel(xmlString);
        assertEquals("root", returnValue.getId());
        assertEquals("Document", returnValue.getName());
        JournalTreeModel journalTreeModel = (JournalTreeModel) returnValue.getChild(0);
        assertEquals("result", journalTreeModel.getName());
        journalTreeModel = (JournalTreeModel) journalTreeModel.getChild(0);
        assertEquals("Update", journalTreeModel.getName());
        List<ModelData> journalTreeModelList = journalTreeModel.getChildren();
        assertEquals("UserName:Jennifer", journalTreeModelList.get(0).get("name"));
        assertEquals("Source:genericUI", journalTreeModelList.get(1).get("name"));
        assertEquals("TimeInMillis:1360032633336", journalTreeModelList.get(2).get("name"));
        assertEquals("OperationType:UPDATE", journalTreeModelList.get(3).get("name"));
        assertEquals("DataCluster:DStar", journalTreeModelList.get(4).get("name"));
        assertEquals("DataModel:DStar", journalTreeModelList.get(5).get("name"));
        assertEquals("Concept:Agency", journalTreeModelList.get(6).get("name"));
        assertEquals("Key:2", journalTreeModelList.get(7).get("name"));
        journalTreeModel = (JournalTreeModel) journalTreeModelList.get(8);
        journalTreeModelList = journalTreeModel.getChildren();
        assertEquals("oldValue:23456", journalTreeModelList.get(1).get("name"));
        assertEquals("newValue:34567", journalTreeModelList.get(2).get("name"));

        returnValue = journalDBService.getComparisionTreeModel("");
        assertEquals("root", returnValue.getId());
        assertEquals("Document", returnValue.getName());
        assertEquals("root", returnValue.getPath());
        assertEquals(0, returnValue.getChildCount());
    }

    public void testGetModelByElement() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        Method method = journalDBService.getClass().getDeclaredMethod("getModelByElement", org.dom4j.Element.class);
        method.setAccessible(true);

        org.dom4j.Element element = new DefaultElement("Product");
        element.addAttribute("id", "18-Product-1");
        element.addAttribute("cls", "tree-node-update");
        element.setText("text_value");
        element.addAttribute("label", "Product");

        Object returnValue = method.invoke(journalDBService, new Object[] { element });
        method.setAccessible(false);
        JournalTreeModel journalTreeModel = (JournalTreeModel) returnValue;
        assertEquals("18-Product-1", journalTreeModel.getId());
        assertEquals("Product:text_value", journalTreeModel.getName());
        assertEquals("/Product", journalTreeModel.getPath());
        assertEquals("tree-node-update", journalTreeModel.getCls());
    }

    public void testParseString2Model() throws NoSuchMethodException, InvocationTargetException, IllegalArgumentException,
            IllegalAccessException {
        String xmlString = "<result><Update><UserName>Jennifer</UserName><Source>genericUI</Source><TimeInMillis>1360032633336</TimeInMillis><OperationType>UPDATE</OperationType><DataCluster>DStar</DataCluster><DataModel>DStar</DataModel><Concept>Agency</Concept><Key>2</Key><Item><path>Name</path><oldValue>23456</oldValue><newValue>34567</newValue><path>Feautres/Sizes/Size[3]</path><oldValue>ccc</oldValue><newValue>333</newValue><path>Feautres/Sizes/Size[2]</path><oldValue>bbb</oldValue><newValue>222</newValue><path>Feautres/Sizes/Size[1]</path><oldValue>aaa</oldValue><newValue>111</newValue></Item></Update></result>";
        Method method = journalDBService.getClass().getDeclaredMethod("parseString2Model", String.class);
        method.setAccessible(true);
        JournalGridModel returnValue = (JournalGridModel) method.invoke(journalDBService, new Object[] { xmlString });
        method.setAccessible(false);
        assertEquals("DStar", returnValue.getDataContainer());
        assertEquals("DStar", returnValue.getDataModel());
        assertEquals("Agency", returnValue.getEntity());
        assertEquals("genericUI.1360032633336", returnValue.getIds());
        assertEquals("2", returnValue.getKey());
        assertEquals(UpdateReportPOJO.OPERATION_TYPE_UPDATE, returnValue.getOperationType());
        assertEquals("genericUI", returnValue.getSource());
        assertEquals("1360032633336", returnValue.getOperationTime());
        assertEquals("Jennifer", returnValue.getUserName());
        assertEquals("/Agency/Name", returnValue.getChangeNodeList().get(0));
        assertEquals("/Agency/Feautres/Sizes/Size[1]", returnValue.getChangeNodeList().get(1));
        assertEquals("/Agency/Feautres/Sizes/Size[2]", returnValue.getChangeNodeList().get(2));
        assertEquals("/Agency/Feautres/Sizes/Size[3]", returnValue.getChangeNodeList().get(3));
    }

    public void testCheckNull() throws NoSuchMethodException, InvocationTargetException, IllegalArgumentException,
            IllegalAccessException {
        Method method = journalDBService.getClass().getDeclaredMethod("checkNull", String.class);
        method.setAccessible(true);
        Object returnValue = method.invoke(journalDBService, new Object[] { "genericUI" });
        assertEquals("genericUI", returnValue);
        returnValue = method.invoke(journalDBService, new Object[] { "null" });
        assertEquals("", returnValue);
        returnValue = method.invoke(journalDBService, new Object[] { null });
        assertEquals("", returnValue);
        method.setAccessible(false);
    }
}
