/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit3.PowerMockSuite;
import org.powermock.reflect.Whitebox;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadataImpl;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.datamodel.management.DataModelID;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.client.model.ItemResult;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.FormatModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.model.QueryModel;
import org.talend.mdm.webapp.browserecords.client.model.RecordsPagingConfig;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.DataModelHelper;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.SchemaMockAgent;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.ViewHelper;
import org.talend.mdm.webapp.browserecords.server.provider.SmartViewProvider;
import org.talend.mdm.webapp.browserecords.server.ruleengine.DisplayRuleEngine;
import org.talend.mdm.webapp.browserecords.server.util.DynamicLabelUtil;
import org.talend.mdm.webapp.browserecords.server.util.SmartViewUtil;
import org.talend.mdm.webapp.browserecords.server.util.TestData;
import org.talend.mdm.webapp.browserecords.shared.SmartViewDescriptions;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;
import org.talend.mdm.webapp.browserecords.shared.VisibleRuleResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.util.CoreException;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import com.amalto.core.webservice.WSBoolean;
import com.amalto.core.webservice.WSByteArray;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSDeleteItemWithReport;
import com.amalto.core.webservice.WSExecuteTransformerV2;
import com.amalto.core.webservice.WSGetItem;
import com.amalto.core.webservice.WSGetTransformerV2PKs;
import com.amalto.core.webservice.WSGetView;
import com.amalto.core.webservice.WSGetViewPKs;
import com.amalto.core.webservice.WSInt;
import com.amalto.core.webservice.WSItem;
import com.amalto.core.webservice.WSString;
import com.amalto.core.webservice.WSStringArray;
import com.amalto.core.webservice.WSTransformerContext;
import com.amalto.core.webservice.WSTransformerContextPipeline;
import com.amalto.core.webservice.WSTransformerContextPipelinePipelineItem;
import com.amalto.core.webservice.WSTransformerV2PK;
import com.amalto.core.webservice.WSTransformerV2PKArray;
import com.amalto.core.webservice.WSTypedContent;
import com.amalto.core.webservice.WSView;
import com.amalto.core.webservice.WSViewPK;
import com.amalto.core.webservice.WSViewPKArray;
import com.amalto.core.webservice.WSViewSearch;
import com.amalto.core.webservice.WSWhereCondition;
import com.amalto.core.webservice.XtentisPort;
import com.amalto.webapp.core.bean.Configuration;
import com.amalto.webapp.core.util.XmlUtil;
import com.extjs.gxt.ui.client.data.ModelData;

@PrepareForTest({ Util.class, org.talend.mdm.webapp.base.server.util.CommonUtil.class, MetadataRepository.class,
        org.talend.mdm.webapp.browserecords.server.util.CommonUtil.class, XtentisPort.class, WSViewSearch.class,
        BrowseRecordsAction.class, SmartViewProvider.class, SmartViewUtil.class, SmartViewDescriptions.class,
        com.amalto.webapp.core.util.Util.class, LocalUser.class, ILocalUser.class, BeanDelegatorContainer.class,
        BrowseRecordsAction.class, Configuration.class })
@SuppressWarnings("nls")
public class BrowseRecordsActionTest extends TestCase {

    private final Messages MESSAGES = MessagesFactory.getMessages(
            "org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages", this.getClass().getClassLoader()); //$NON-NLS-1$

    private BrowseRecordsAction action = new BrowseRecordsAction();

    private String xml = "<Agency><Name>Newark</Name><Name>Newark1</Name><City>Newark</City><State>NJ</State><Zip>07107</Zip><Region>EAST</Region><MoreInfo>Map@@http://maps.google.com/maps?q=40.760667,-74.1879&amp;ll=40.760667,-74.1879&amp;z=9</MoreInfo><Id>NJ01</Id></Agency>"; //$NON-NLS-1$

    @SuppressWarnings("unchecked")
    public static TestSuite suite() throws Exception {
        return new PowerMockSuite("Unit tests for " + BrowseRecordsActionTest.class.getSimpleName(),
                BrowseRecordsActionTest.class);
    }

    public void testDynamicAssembleByResultOrder() throws Exception {
        String xml = "<result><numeroContrat xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>5005007</numeroContrat><xsi:type xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>AP-RE</xsi:type><taskId>11e18f27-2a8c-4ef8-b80f-5ba27d8fd103</taskId></result>";
        ItemBean itemBean = new ItemBean();
        itemBean.setItemXml(xml);
        ViewBean viewBean = new ViewBean();
        viewBean.addViewableXpath("Contrat/numeroContrat");
        viewBean.addViewableXpath("Contrat/detailContrat/@xsi:type");
        EntityModel entityModel = new EntityModel();
        entityModel.setMetaDataTypes(new LinkedHashMap<String, TypeModel>());
        action.dynamicAssembleByResultOrder(itemBean, viewBean, entityModel, new HashMap<String, EntityModel>(), "en");
        assertEquals("5005007", itemBean.get("Contrat/numeroContrat"));
        assertEquals("AP-RE", itemBean.get("Contrat/detailContrat/@xsi:type"));
    }

    public void testMultiOccurenceNode() throws Exception {
        String language = "en"; //$NON-NLS-1$
        ItemNodeModel model = action.getItemNodeModel(getItemBean(), TestData.getEntityModel(), false, language);
        List<ModelData> child = model.getChildren();

        for (int i = 0; i < child.size(); i++) {
            String value = ((ItemNodeModel) child.get(i)).getObjectValue().toString();
            switch (i) {
            case 0: {
                assertEquals("NJ01", value); //$NON-NLS-1$
                break;
            }
            case 1: {
                assertEquals("Newark", value); //$NON-NLS-1$
                break;
            }
            case 2: {
                assertEquals("Newark1", value); //$NON-NLS-1$
                break;
            }
            case 3: {
                assertEquals("Newark", value); //$NON-NLS-1$
                break;
            }
            case 4: {
                assertEquals("NJ", value); //$NON-NLS-1$
                break;
            }
            case 5: {
                assertEquals("07107", value); //$NON-NLS-1$
                break;
            }
            case 6: {
                assertEquals("EAST", value); //$NON-NLS-1$
                break;
            }
            case 7: {
                assertEquals("Map@@http://maps.google.com/maps?q=40.760667,-74.1879&ll=40.760667,-74.1879&z=9", value); //$NON-NLS-1$
                break;
            }
            default: {
                // do nothing
            }
            }

        }
    }

    public void test_TaskIdIsNull() {
        ItemBean itemBean = getItemBean();

        boolean taskIdNotNull = itemBean.getTaskId() != null && itemBean.getTaskId().trim().length() > 0
                && !"null".equalsIgnoreCase(itemBean.getTaskId().trim());
        assertEquals(false, taskIdNotNull);
        // 1 taskId = ""
        itemBean.setTaskId("");
        taskIdNotNull = itemBean.getTaskId() != null && itemBean.getTaskId().trim().length() > 0
                && !"null".equalsIgnoreCase(itemBean.getTaskId().trim());
        assertEquals(false, taskIdNotNull);
        // 2 taskId = " "
        itemBean.setTaskId(" ");
        taskIdNotNull = itemBean.getTaskId() != null && itemBean.getTaskId().trim().length() > 0
                && !"null".equalsIgnoreCase(itemBean.getTaskId().trim());
        assertEquals(false, taskIdNotNull);
        // 3 taskId = "null"
        itemBean.setTaskId("null");
        taskIdNotNull = itemBean.getTaskId() != null && itemBean.getTaskId().trim().length() > 0
                && !"null".equalsIgnoreCase(itemBean.getTaskId().trim());
        assertEquals(false, taskIdNotNull);
        // 4 taksId = "Null";
        itemBean.setTaskId("Null");
        taskIdNotNull = itemBean.getTaskId() != null && itemBean.getTaskId().trim().length() > 0
                && !"null".equalsIgnoreCase(itemBean.getTaskId().trim());
        assertEquals(false, taskIdNotNull);
        // 5 taskId = "null ";
        itemBean.setTaskId("null ");
        taskIdNotNull = itemBean.getTaskId() != null && itemBean.getTaskId().trim().length() > 0
                && !"null".equalsIgnoreCase(itemBean.getTaskId().trim());
        assertEquals(false, taskIdNotNull);
        // 6 taskId = "NULL";
        itemBean.setTaskId("NULL");
        taskIdNotNull = itemBean.getTaskId() != null && itemBean.getTaskId().trim().length() > 0
                && !"null".equalsIgnoreCase(itemBean.getTaskId().trim());
        assertEquals(false, taskIdNotNull);
        // 7 taskId = "123";
        itemBean.setTaskId("123");
        taskIdNotNull = itemBean.getTaskId() != null && itemBean.getTaskId().trim().length() > 0
                && !"null".equalsIgnoreCase(itemBean.getTaskId().trim());
        assertEquals(true, taskIdNotNull);
    }

    private ItemBean getItemBean() {
        ItemBean item = new ItemBean("Agency", "NJ01", xml); //$NON-NLS-1$//$NON-NLS-2$
        return item;
    }

    /**
     * Using the Product Model to query ItemBeans<br>
     * DataModel File : Product.xsd<br>
     * View File : Browse_items_Product.item<br>
     * Data Result File : ProductQueryResult.properties
     * 
     * @throws Exception
     */
    public void test_queryItemBeans() throws Exception {
        // Create QueryModel parameter
        QueryModel config = new QueryModel();
        RecordsPagingConfig pagingConfig = new RecordsPagingConfig();
        pagingConfig.setLimit(10);
        pagingConfig.setSortDir("ASC");
        pagingConfig.setSortField("Product/Id");
        config.setPagingLoadConfig(pagingConfig);
        config.setLanguage("en");
        config.setDataClusterPK("Product");
        String concept = "Product";
        String fileName = "Product.xsd";
        String viewPK = "Browse_items_Product";
        // Create ViewBean
        ViewBean viewBean = getViewBean(concept, fileName);
        viewBean.setViewPK(viewPK);
        String keys[] = new String[] { "Product/Id" };
        viewBean.getBindingEntityModel().setKeys(keys);
        // Set viewable elements and searchable elements
        parseElements(concept, viewBean, getXml("Browse_items_Product.item"));
        // Reference View file: 'Browse_items_Product.item' to check the parsing results
        assertEquals(4, viewBean.getViewables().length);
        assertEquals("Product/Id", viewBean.getViewables()[0]);
        assertEquals("Product/Name", viewBean.getViewables()[1]);
        assertEquals("Product/Price", viewBean.getViewables()[2]);
        assertEquals("Product/Availability", viewBean.getViewables()[3]);
        assertEquals(4, viewBean.getSearchables().size());
        // Set entityModel and viewBean
        config.setModel(viewBean.getBindingEntityModel());
        config.setView(viewBean);
        // Mock get result from server-side
        PowerMockito.mockStatic(org.talend.mdm.webapp.base.server.util.CommonUtil.class);
        XtentisPort port = PowerMockito.mock(XtentisPort.class);
        String[] results = getMockResultsFromServer();
        WSStringArray wsStringArray = new WSStringArray(results);
        Mockito.when(org.talend.mdm.webapp.base.server.util.CommonUtil.getPort()).thenReturn(port);
        Mockito.when(port.viewSearch(Mockito.any(WSViewSearch.class))).thenReturn(wsStringArray);
        Mockito.when(port.isPagingAccurate(Mockito.any(WSInt.class))).thenReturn(new WSBoolean(true));
        // Mock get SmartViewDescriptions
        PowerMockito.mockStatic(SmartViewUtil.class);
        SmartViewDescriptions svd = PowerMockito.mock(SmartViewDescriptions.class);
        Mockito.when(SmartViewUtil.build(Mockito.any(SmartViewProvider.class), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(svd);
        // Mock private method
        BrowseRecordsAction newAction = PowerMockito.spy(action);
        Whitebox.<Boolean> invokeMethod(newAction, "checkSmartViewExistsByLang", Mockito.anyString(), Mockito.anyString());
        Whitebox.<Boolean> invokeMethod(newAction, "checkSmartViewExistsByOpt", Mockito.anyString(), Mockito.anyString());
        // Call queryItemBeans
        ItemBasePageLoadResult<ItemBean> itemBeans = action.queryItemBeans(config, "en");
        // Total record size
        assertEquals(results[0], "<totalCount>" + itemBeans.getTotalLength() + "</totalCount>");
        // Query record size
        assertEquals(results.length - 1, itemBeans.getData().size());
        // First record
        ItemBean firstItemBean = itemBeans.getData().get(0);
        // First record (xml)
        String result = "<result><Id xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">1</Id><Name>Mug</Name><Price>1.99</Price><Availability>false</Availability></result>";
        assertEquals(result, results[1]);
        // First record (property name list size)
        assertEquals(viewBean.getViewables().length, firstItemBean.getPropertyNames().size());
        // First record (property: Id)
        assertEquals("1", firstItemBean.get("Product/Id"));
        // First record (property: Name)
        assertEquals("Mug", firstItemBean.get("Product/Name"));
        // First record (property: Price)
        assertEquals("1.99", firstItemBean.get("Product/Price"));
        // First record (property: Availability)
        assertEquals("false", firstItemBean.get("Product/Availability"));
        // Second record
        ItemBean secondItemBean = itemBeans.getData().get(1);
        // Second record (property name list size)
        assertEquals(viewBean.getViewables().length, secondItemBean.getPropertyNames().size());
        // Second record (property: Id)
        assertEquals("2", secondItemBean.get("Product/Id"));
        // Second record (property: Name)
        assertEquals("Car", secondItemBean.get("Product/Name"));
        // Second record (property: Price)
        assertEquals("2.99", secondItemBean.get("Product/Price"));
        // Second record (property: Availability)
        assertEquals("false", secondItemBean.get("Product/Availability"));
        // Third record
        ItemBean thirdItemBean = itemBeans.getData().get(2);
        // Third record (property name list size)
        assertEquals(viewBean.getViewables().length, thirdItemBean.getPropertyNames().size());
        // Third record (property: Id)
        assertEquals("3", thirdItemBean.get("Product/Id"));
        // Third record (property: Name)
        assertEquals("Talend", thirdItemBean.get("Product/Name"));
        // Third record (property: Price)
        assertEquals("3.99", thirdItemBean.get("Product/Price"));
        // Third record (property: Availability)
        assertEquals("false", thirdItemBean.get("Product/Availability"));
        // Fourth record
        ItemBean fourthItemBean = itemBeans.getData().get(3);
        // Fourth record (property name list size)
        assertEquals(viewBean.getViewables().length, fourthItemBean.getPropertyNames().size());
        // Fourth record (property: Id)
        assertEquals("4", fourthItemBean.get("Product/Id"));
        // Fourth record (property: Name)
        assertEquals("Shirts", fourthItemBean.get("Product/Name"));
        // Fourth record (property: Price)
        assertEquals("4.99", fourthItemBean.get("Product/Price"));
        // Fourth record (property: Availability)
        assertEquals("true", fourthItemBean.get("Product/Availability"));
        // Fifth record
        ItemBean fifthItemBean = itemBeans.getData().get(4);
        // Fifth record (property name list size)
        assertEquals(viewBean.getViewables().length, fifthItemBean.getPropertyNames().size());
        // Fifth record (property: Id)
        assertEquals("5", fifthItemBean.get("Product/Id"));
        // Fifth record (property: Name)
        assertEquals("Cup", fifthItemBean.get("Product/Name"));
        // Fifth record (property: Price)
        assertEquals("5.99", fifthItemBean.get("Product/Price"));
        // Fifth record (property: Availability)
        assertEquals("true", fifthItemBean.get("Product/Availability"));
    }

    public void test_queryItemBeansWithDisplayFormat() throws Exception {
        // Create QueryModel parameter
        QueryModel config = new QueryModel();
        RecordsPagingConfig pagingConfig = new RecordsPagingConfig();
        pagingConfig.setLimit(10);
        pagingConfig.setSortDir("ASC");
        pagingConfig.setSortField("FormatTest/subelement");
        config.setPagingLoadConfig(pagingConfig);
        config.setLanguage("en");
        config.setDataClusterPK("FormatTest");
        String concept = "FormatTest";
        String fileName = "FormatTest.xsd";
        String viewPK = "Browse_items_FormatTest";
        // Create ViewBean
        ViewBean viewBean = getViewBean(concept, fileName);
        viewBean.setViewPK(viewPK);
        String keys[] = new String[] { "FormatTest/subelement" };
        viewBean.getBindingEntityModel().setKeys(keys);
        // Set viewable elements and searchable elements
        parseElements(concept, viewBean, getXml("Browse_items_FormatTest.item"));
        // Reference View file: 'Browse_items_Product.item' to check the parsing results
        assertEquals(5, viewBean.getViewables().length);
        assertEquals("FormatTest/subelement", viewBean.getViewables()[0]);
        assertEquals("FormatTest/name", viewBean.getViewables()[1]);
        assertEquals("FormatTest/d1", viewBean.getViewables()[2]);
        assertEquals("FormatTest/dt1", viewBean.getViewables()[3]);
        assertEquals("FormatTest/num", viewBean.getViewables()[4]);
        assertEquals(5, viewBean.getSearchables().size());
        // Set entityModel and viewBean
        config.setModel(viewBean.getBindingEntityModel());
        config.setView(viewBean);
        // Mock get result from server-side
        PowerMockito.mockStatic(org.talend.mdm.webapp.base.server.util.CommonUtil.class);
        XtentisPort port = PowerMockito.mock(XtentisPort.class);
        String[] results = getMockResultsFromServerForDisplayFormat();
        WSStringArray wsStringArray = new WSStringArray(results);
        Mockito.when(org.talend.mdm.webapp.base.server.util.CommonUtil.getPort()).thenReturn(port);
        Mockito.when(port.viewSearch(Mockito.any(WSViewSearch.class))).thenReturn(wsStringArray);
        Mockito.when(port.isPagingAccurate(Mockito.any(WSInt.class))).thenReturn(new WSBoolean(true));
        // Mock get SmartViewDescriptions
        PowerMockito.mockStatic(SmartViewUtil.class);
        SmartViewDescriptions svd = PowerMockito.mock(SmartViewDescriptions.class);
        Mockito.when(SmartViewUtil.build(Mockito.any(SmartViewProvider.class), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(svd);
        // Mock private method
        BrowseRecordsAction newAction = PowerMockito.spy(action);
        Whitebox.<Boolean> invokeMethod(newAction, "checkSmartViewExistsByLang", Mockito.anyString(), Mockito.anyString());
        Whitebox.<Boolean> invokeMethod(newAction, "checkSmartViewExistsByOpt", Mockito.anyString(), Mockito.anyString());
        // Call queryItemBeans
        ItemBasePageLoadResult<ItemBean> itemBeans = action.queryItemBeans(config, "en");
        // Total record size
        assertEquals(results[0], "<totalCount>" + itemBeans.getTotalLength() + "</totalCount>");
        // Query record size
        assertEquals(results.length - 1, itemBeans.getData().size());
        // First record
        ItemBean firstItemBean = itemBeans.getData().get(0);
        // First record (xml)
        String result = "<result><subelement>1111</subelement><name>qqqqq8</name><d1>2012-11-01</d1><dt1>2012-11-02T12:00:00</dt1><num>55</num></result>";
        assertEquals(result, results[1]);
        // First record (property name list size)
        assertEquals(viewBean.getViewables().length, firstItemBean.getPropertyNames().size());
        // First record (property: subelement)
        assertEquals("1111", firstItemBean.get("FormatTest/subelement"));
        // First record (property: Name)
        assertEquals("qqqqq8", firstItemBean.get("FormatTest/name"));
        // First record (property: d1)

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH);
        Date date = sdf.parse("2012-11-01");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String formatValue = com.amalto.webapp.core.util.Util.formatDate("%tc", calendar);

        assertEquals(formatValue, firstItemBean.get("FormatTest/d1"));
        // First record (property: dt1)

        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.ENGLISH);
        date = sdf.parse("2012-11-02T12:00:00");
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        formatValue = com.amalto.webapp.core.util.Util.formatDate("%tc", calendar);

        assertEquals(formatValue, firstItemBean.get("FormatTest/dt1"));
        // First record (property: num)
        assertEquals("055", firstItemBean.get("FormatTest/num"));
        // Second record
        ItemBean secondItemBean = itemBeans.getData().get(1);
        // Second record (property name list size)
        assertEquals(viewBean.getViewables().length, secondItemBean.getPropertyNames().size());
        // Second record (property: subelement)
        assertEquals("222", secondItemBean.get("FormatTest/subelement"));
        // Second record (property: Name)
        assertEquals("www8", secondItemBean.get("FormatTest/name"));
        // Second record (property: d1)

        sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH);
        date = sdf.parse("2012-11-06");
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        formatValue = com.amalto.webapp.core.util.Util.formatDate("%tc", calendar);

        assertEquals(formatValue, secondItemBean.get("FormatTest/d1"));
        // Second record (property: dt1)

        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.ENGLISH);
        date = sdf.parse("2012-11-30T12:00:00");
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        formatValue = com.amalto.webapp.core.util.Util.formatDate("%tc", calendar);

        assertEquals(formatValue, secondItemBean.get("FormatTest/dt1"));
        // Second record (property: num)
        assertEquals("087", secondItemBean.get("FormatTest/num"));
    }

    /**
     * Using the ContractInheritance.xsd to test getItemNodeModel<br>
     * Data File: ContractSampleTwo.xml
     * 
     * @throws Exception
     */
    public void test_getItemNodeModel() throws Exception {
        String language = "en";
        String concept = "Contract";
        String ids = "1";
        ViewBean viewBean = getViewBean(concept, "ContractInheritance.xsd");
        ItemBean item = new ItemBean(concept, ids, getXml("ContractSampleTwo.xml"));
        // call getItemNodeModel
        ItemNodeModel root = action.getItemNodeModel(item, viewBean.getBindingEntityModel(), false, language);
        assertEquals(4, root.getChildCount());
        // the first node
        ItemNodeModel firstNode = (ItemNodeModel) root.getChild(0);
        assertEquals("id", firstNode.getName());
        assertEquals("1", firstNode.getObjectValue().toString());
        assertEquals("Contract/id", firstNode.getTypePath());
        // the second node
        ItemNodeModel secondNode = (ItemNodeModel) root.getChild(1);
        assertEquals("comment", secondNode.getName());
        assertEquals("1", secondNode.getObjectValue().toString());
        assertEquals("Contract/comment", secondNode.getTypePath());
        // the third node
        ItemNodeModel thirdNode = (ItemNodeModel) root.getChild(2);
        assertEquals("detail", thirdNode.getName());
        assertEquals("ContractDetailSubType", thirdNode.getRealType());
        assertEquals("Contract/detail", thirdNode.getTypePath());
        assertEquals(2, thirdNode.getChildCount());
        assertEquals("code", ((ItemNodeModel) thirdNode.getChild(0)).getName());
        assertEquals("subType", ((ItemNodeModel) thirdNode.getChild(1)).getName());
        // detail child node : subType node
        ItemNodeModel detail_subType_node = (ItemNodeModel) thirdNode.getChild(1);
        assertEquals(2, detail_subType_node.getChildCount());
        assertEquals("ContractDetailSubTypeTwo", detail_subType_node.getRealType());
        assertEquals("Contract/detail:ContractDetailSubType/subType", detail_subType_node.getTypePath());
        assertEquals("Contract/detail:ContractDetailSubType/subType:ContractDetailSubTypeTwo/type",
                ((ItemNodeModel) detail_subType_node.getChild(0)).getTypePath());
        assertEquals("Contract/detail:ContractDetailSubType/subType:ContractDetailSubTypeTwo/description",
                ((ItemNodeModel) detail_subType_node.getChild(1)).getTypePath());
        // the fourth node
        ItemNodeModel fourthNode = (ItemNodeModel) root.getChild(3);
        assertEquals("enumEle", fourthNode.getName());
        assertEquals("pending", fourthNode.getObjectValue().toString());
        assertEquals("Contract/enumEle", fourthNode.getTypePath());
    }
    
 // TMDM-9501: Display Format applied onto numeric fields is not correct
    public void testGetItemForNumberFormate() throws Exception {
        String language = "en";
        String concept = "Goods";
        String ids = "1";
        ViewBean viewBean = getViewBean(concept, "GoodsWithNumberField.xsd");
        ItemBean item = new ItemBean(concept, ids, getXml("GoodsWithNumberFieldSample.xml"));
        if(item.getOriginalMap() == null){
            item.setOriginalMap( new HashMap<String, Object>()) ;
        }
        if(item.getFormateMap() == null){
            item.setFormateMap(new HashMap<String, String>()) ;
        }
            
        com.amalto.core.delegator.BeanDelegatorContainer beanDelegatorContainer = PowerMockito.spy(com.amalto.core.delegator.BeanDelegatorContainer.createInstance());
        Whitebox.<BeanDelegatorContainer> invokeMethod(beanDelegatorContainer, "getInstance");
        BeanDelegatorContainer.getInstance().setDelegatorInstancePool(Collections.<String, Object> singletonMap("LocalUser", new MockILocalUser()));
        //LocalUser localUser = PowerMockito.spy(new LocalUser());
        Whitebox.<BeanDelegatorContainer> invokeMethod(beanDelegatorContainer, "setDelegatorInstancePool", Collections.<String, Object> singletonMap("LocalUser", new MockILocalUser()));
        Whitebox.<LocalUser> invokeMethod(beanDelegatorContainer, "getLocalUserDelegator");
       
        PowerMockito.when(LocalUser.getLocalUser()).thenReturn(new MockILocalUser()) ;
        String[] roles = { "Demo_Manager", "System_Admin", "authenticated", "administration" };
        HashSet<String> rolesSet = new HashSet<String>() ;
        rolesSet.addAll(Arrays.asList(roles)) ;

        Constructor<Configuration> constructor = Configuration.class.getDeclaredConstructor(String.class, String.class);
        constructor.setAccessible(true);
        Configuration config = constructor.newInstance(concept, concept);

        PowerMockito.mockStatic(Configuration.class);
        Mockito.when(Configuration.getConfiguration()).thenReturn(config);
        
        XtentisPort port = PowerMockito.mock(XtentisPort.class);
        PowerMockito.spy(org.talend.mdm.webapp.base.server.util.CommonUtil.class);
        PowerMockito.doReturn(port).when(org.talend.mdm.webapp.base.server.util.CommonUtil.class, "getPort");
        WSItem wsItem = getWsItem(concept, concept, ids, getXml("GoodsWithNumberFieldSample.xml"));
        Mockito.when(port.getItem(Mockito.any(WSGetItem.class))).thenReturn(wsItem);
        
        ItemBean itemBean = action.getItem(item, null, viewBean.getBindingEntityModel(), false, language);
       
        Map<String, Object> originalMap = itemBean.getOriginalMap();
        Map<String, String> formateMap = itemBean.getFormateMap();
        
        assertEquals(4, originalMap.size());
        assertEquals(4, formateMap.size());

        assertEquals("5.5000", formateMap.get("Goods/doubleValue").toString());
        assertEquals("1.50000000", formateMap.get("Goods/decimalValue").toString());
        assertEquals("4.5000", formateMap.get("Goods/simpleDecimalValue").toString());
        assertEquals("2.0000", formateMap.get("Goods/floatValue").toString());

        assertEquals("5.5", originalMap.get("Goods/doubleValue").toString());
        assertEquals("1.5", originalMap.get("Goods/decimalValue").toString());
        assertEquals("4.5", originalMap.get("Goods/simpleDecimalValue").toString());
        assertEquals("2", originalMap.get("Goods/floatValue").toString());
    }

    public void testDeleteItemBeans() throws Exception {
        List<ItemBean> items = new ArrayList<ItemBean>();
        ItemBean itemBean1 = new ItemBean();
        itemBean1.setConcept("Test1");
        itemBean1.setIds("1");
        items.add(itemBean1);
        ItemBean itemBean2 = new ItemBean();
        itemBean2.setConcept("Test1");
        itemBean1.setIds("1");
        items.add(itemBean2);
        ItemBean itemBean3 = new ItemBean();
        itemBean1.setConcept("Test1");
        itemBean1.setIds("1");
        items.add(itemBean3);
        ItemBean itemBean4 = new ItemBean();
        itemBean1.setConcept("Test1");
        itemBean1.setIds("2");
        items.add(itemBean4);
        ItemBean itemBean5 = new ItemBean();
        itemBean1.setConcept("Test1");
        itemBean1.setIds("2");
        items.add(itemBean5);
        ItemBean itemBean6 = new ItemBean();
        itemBean1.setConcept("Test1");
        itemBean1.setIds("2");
        items.add(itemBean6);
        ItemBean itemBean7 = new ItemBean();
        itemBean1.setConcept("Test1");
        itemBean1.setIds("3");
        items.add(itemBean7);
        ItemBean itemBean8 = new ItemBean();
        itemBean1.setConcept("Test1");
        itemBean1.setIds("3");
        items.add(itemBean8);
        ItemBean itemBean9 = new ItemBean();
        itemBean1.setConcept("Test1");
        itemBean1.setIds("3");
        items.add(itemBean9);
        
        String cluster = "Test";
        PowerMockito.mockStatic(org.talend.mdm.webapp.browserecords.server.util.CommonUtil.class);
        Mockito.when(org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getCurrentDataCluster()).thenReturn(cluster);
        ComplexTypeMetadata complexTypeMetadata = new ComplexTypeMetadataImpl("Test", "Test1", true);
        MetadataRepository repository = PowerMockito.mock(MetadataRepository.class);
        Mockito.when(repository.getComplexType(Mockito.any(String.class))).thenReturn(complexTypeMetadata);
        PowerMockito.mockStatic(org.talend.mdm.webapp.base.server.util.CommonUtil.class);
        Mockito.when(org.talend.mdm.webapp.base.server.util.CommonUtil.getCurrentRepository()).thenReturn(repository);
        Mockito.when(
                org.talend.mdm.webapp.base.server.util.CommonUtil.extractIdWithDots(Mockito.any(String[].class),
                        Mockito.any(String.class))).thenReturn(new String[0]);
        XtentisPort port = PowerMockito.mock(XtentisPort.class);
        Mockito.when(org.talend.mdm.webapp.base.server.util.CommonUtil.getPort()).thenReturn(port);
        WSDeleteItemWithReport wsDeleteItemWithReport = PowerMockito.mock(WSDeleteItemWithReport.class);
        PowerMockito
                .whenNew(WSDeleteItemWithReport.class)
                .withArguments(Mockito.any(com.amalto.core.webservice.WSItemPK.class), Mockito.any(String.class),
                        Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class),
                        Mockito.any(Boolean.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))
                .thenReturn(wsDeleteItemWithReport);
        Mockito.when(wsDeleteItemWithReport.getSource()).thenReturn(BrowseRecordsAction.INFO_KEYWORD);
        WSString deleteMessage = new WSString();
        Mockito.when(port.deleteItemWithReport(Mockito.any(WSDeleteItemWithReport.class))).thenReturn(deleteMessage);
        List<ItemResult> resut = action.deleteItemBeans(items, true, "en");
        assertEquals(3, resut.size());
    }

    private String[] getMockResultsFromServer() throws IOException {
        List<String> results = new ArrayList<String>();
        InputStream is = BrowseRecordsActionTest.class.getResourceAsStream("../../ProductQueryResult.properties");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String ln = br.readLine();
        while (ln != null) {
            results.add(ln);
            ln = br.readLine();
        }
        return results.toArray(new String[results.size()]);
    }

    private String[] getMockResultsFromServerForDisplayFormat() throws IOException {
        List<String> results = new ArrayList<String>();
        InputStream is = BrowseRecordsActionTest.class.getResourceAsStream("../../FormatTestQueryResult.properties");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String ln = br.readLine();
        while (ln != null) {
            results.add(ln);
            ln = br.readLine();
        }
        return results.toArray(new String[results.size()]);
    }

    private void parseElements(String concept, ViewBean viewBean, String xml) throws Exception {
        Document doc = com.amalto.webapp.core.util.Util.parse(xml);
        String viewableXpath = "viewableBusinessElements";
        String searchableXpath = "searchableBusinessElements";
        NodeList viewList = com.amalto.core.util.Util.getNodeList(doc.getDocumentElement(), viewableXpath);
        String viewables[] = new String[viewList.getLength()];
        for (int k = 0; k < viewList.getLength(); k++) {
            Node node = viewList.item(k);
            viewables[k] = node.getTextContent();
            viewBean.addViewableXpath(node.getTextContent());
        }
        viewBean.setViewables(viewables);
        NodeList searchList = com.amalto.core.util.Util.getNodeList(doc.getDocumentElement(), searchableXpath);
        WSView wsView = new WSView();
        String searchableElements[] = new String[searchList.getLength()];
        for (int i = 0; i < searchList.getLength(); i++) {
            Node node = searchList.item(i);
            searchableElements[i] = node.getTextContent();
        }
        wsView.setSearchableBusinessElements(searchableElements);
        wsView.setName("Browse_items_Product");
        viewBean.setSearchables(ViewHelper.getSearchables(wsView, concept, "en", viewBean.getBindingEntityModel()));
    }

    /**
     * Test polymorphism type change, It will get the correct typeModel
     * 
     * @throws Exception
     */
    public void test_createSubItemNodeModel() throws Exception {
        String language = "en";
        String concept = "Contract";
        String ids = "1";
        ViewBean viewBean = getViewBean(concept, "ContractInheritance.xsd");
        ItemBean item = new ItemBean(concept, ids, getXml("ContractSampleOne.xml"));
        ItemNodeModel root = action.getItemNodeModel(item, viewBean.getBindingEntityModel(), false, language);
        assertEquals(4, root.getChildCount());

        ItemNodeModel detailModel = (ItemNodeModel) root.getChild(2);
        assertEquals("ContractDetailSubType", detailModel.getRealType());
        assertEquals("Contract/detail", detailModel.getTypePath());
        assertEquals(2, detailModel.getChildCount());

        ItemNodeModel subTypeModel = (ItemNodeModel) detailModel.getChild(1);

        assertEquals("ContractDetailSubTypeOne", subTypeModel.getRealType());
        assertEquals("Contract/detail:ContractDetailSubType/subType", subTypeModel.getTypePath());
        assertEquals(1, subTypeModel.getChildCount());
        assertEquals("Contract/detail:ContractDetailSubType/subType/type",
                ((ItemNodeModel) subTypeModel.getChild(0)).getTypePath());

        // mock UI behavior: change subType(ContractDetailSubTypeOne) to subType(ContractDetailSubTypeTwo)
        subTypeModel.setRealType("ContractDetailSubTypeTwo");
        String contextPath = CommonUtil.getRealXPath(subTypeModel);
        String typePath = CommonUtil.getRealTypePath(subTypeModel);
        typePath = typePath.replaceAll(":" + subTypeModel.getRealType() + "$", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String xml = getXml("ContractSampleTwo.xml");

        ItemNodeModel newSubTypeModel = action.createSubItemNodeModel(viewBean, xml, typePath, contextPath,
                subTypeModel.getRealType(), false, language);

        assertEquals("ContractDetailSubTypeTwo", newSubTypeModel.getRealType());
        assertEquals("Contract/detail:ContractDetailSubType/subType", newSubTypeModel.getTypePath());
        assertEquals(2, newSubTypeModel.getChildCount());
        assertEquals("Contract/detail:ContractDetailSubType/subType:ContractDetailSubTypeTwo/type",
                ((ItemNodeModel) newSubTypeModel.getChild(0)).getTypePath());
        assertEquals("Contract/detail:ContractDetailSubType/subType:ContractDetailSubTypeTwo/description",
                ((ItemNodeModel) newSubTypeModel.getChild(1)).getTypePath());
    }

    public void test_getForeignKeyValues() throws Exception {
        String[] ids = new String[] { "1" };
        String language = "en";
        Map<ViewBean, Map<String, List<String>>> map = mock_getForeignKeyValues("Product", ids, language);
        assertNotNull(map);
        ViewBean viewBean = map.keySet().iterator().next();
        Map<String, List<String>> fkValues = map.get(viewBean);
        assertNotNull(viewBean);
        assertNotNull(fkValues);
        assertEquals(16, viewBean.getBindingEntityModel().getMetaDataTypes().size());
        assertEquals(2, fkValues.size());
        List<String> families = fkValues.get("Product/Family");
        assertEquals(5, families.size());
        assertEquals("[1]", families.get(0));
        assertEquals("[2]", families.get(1));
        assertEquals("[3]", families.get(2));
        assertEquals("[4]", families.get(3));
        assertEquals("[5]", families.get(4));
        assertTrue(fkValues.get("Product/Parent").size() == 0);
    }

    public void test_createDefaultItemNodeModel() throws Exception {
        String language = "en";
        String concept = "Product";
        String[] ids = new String[] { "1" };
        Map<ViewBean, Map<String, List<String>>> map = mock_getForeignKeyValues(concept, ids, language);
        assertNotNull(map);
        ViewBean viewBean = map.keySet().iterator().next();
        Map<String, List<String>> initDataMap = map.get(viewBean);
        ItemNodeModel root = mock_createDefaultItemNodeModel(viewBean, initDataMap, language);
        assertNotNull(root);
        assertEquals(15, root.getChildren().size());
        ItemNodeModel id = (ItemNodeModel) root.getChild(0);
        assertEquals("", id.getObjectValue());
        ItemNodeModel familyOne = (ItemNodeModel) root.getChild(7);
        assertTrue(familyOne.getObjectValue() instanceof ForeignKeyBean);
        assertEquals("[1]", ((ForeignKeyBean) familyOne.getObjectValue()).getId());
        ItemNodeModel familyTwo = (ItemNodeModel) root.getChild(8);
        assertTrue(familyTwo.getObjectValue() instanceof ForeignKeyBean);
        assertEquals("[2]", ((ForeignKeyBean) familyTwo.getObjectValue()).getId());
        ItemNodeModel familyThree = (ItemNodeModel) root.getChild(9);
        assertTrue(familyThree.getObjectValue() instanceof ForeignKeyBean);
        assertEquals("[3]", ((ForeignKeyBean) familyThree.getObjectValue()).getId());
        ItemNodeModel familyFour = (ItemNodeModel) root.getChild(10);
        assertTrue(familyFour.getObjectValue() instanceof ForeignKeyBean);
        assertEquals("[4]", ((ForeignKeyBean) familyFour.getObjectValue()).getId());
        ItemNodeModel familyFive = (ItemNodeModel) root.getChild(11);
        assertTrue(familyFive.getObjectValue() instanceof ForeignKeyBean);
        assertEquals("[5]", ((ForeignKeyBean) familyFive.getObjectValue()).getId());
    }

    public ItemNodeModel mock_createDefaultItemNodeModel(ViewBean viewBean, Map<String, List<String>> initDataMap, String language)
            throws ServiceException {
        String concept = viewBean.getBindingEntityModel().getConceptName();

        EntityModel entity = viewBean.getBindingEntityModel();
        Map<String, TypeModel> metaDataTypes = entity.getMetaDataTypes();
        ItemNodeModel itemModel = null;
        try {
            DisplayRuleEngine ruleEngine = new DisplayRuleEngine(metaDataTypes, concept);

            TypeModel typeModel = metaDataTypes.get(concept);
            Document doc = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getSubXML(typeModel, null, initDataMap,
                    language);

            org.dom4j.Document doc4j = XmlUtil.parseDocument(doc);

            ruleEngine.execDefaultValueRule(doc4j);

            if (initDataMap != null) {
                Set<String> paths = initDataMap.keySet();
                for (String path : paths) {
                    List<?> nodeList = doc4j.selectNodes(path);
                    List<String> values = initDataMap.get(path);
                    if (nodeList != null && nodeList.size() > 0 && values != null && values.size() > 0) {
                        for (int i = 0; i < nodeList.size(); i++) {
                            org.dom4j.Element current = (org.dom4j.Element) nodeList.get(i);
                            current.setText(values.get(i));
                        }

                    }
                }
            }

            Document resultDoc = org.talend.mdm.webapp.base.server.util.XmlUtil.parseDocument(doc4j);
            Map<String, Integer> multiNodeIndex = new HashMap<String, Integer>();
            StringBuffer foreignKeyDeleteMessage = new StringBuffer();
            Element root = resultDoc.getDocumentElement();
            itemModel = builderNode(multiNodeIndex, root, entity, "", "", false, foreignKeyDeleteMessage, true, language); //$NON-NLS-1$ //$NON-NLS-2$
            DynamicLabelUtil.getDynamicLabel(doc4j, "", itemModel, metaDataTypes, language); //$NON-NLS-1$
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
        return itemModel;
    }

    public void test_getNodeValue() throws Exception {
        String conceptName = "TaxonomyCategory";
        String xml = "<TaxonomyCategory><Id>1</Id><ZthesId>1</ZthesId><CrcCode>1</CrcCode><TaxonomyCategory>1</TaxonomyCategory><X-BusinessType><Id>2</Id></X-BusinessType></TaxonomyCategory>";
        Document docXml = Util.parse(xml);
        String xpath = "TaxonomyCategory/Id";
        assertEquals("1", parsingNodeValue(docXml, xpath, conceptName));
        xpath = "TaxonomyCategory/X-BusinessType/Id";
        assertEquals("2", parsingNodeValue(docXml, xpath, conceptName));
    }

    public void testGetErrorMessageFromWebCoreException() throws Exception {
        RuntimeException runtimeException = new RuntimeException("RuntimeException");
        CoreException coreException = new CoreException("default_remote_error_message", runtimeException);
        Method[] methods = BrowseRecordsAction.class.getDeclaredMethods();
        for (Method method : methods) {
            if ("getErrorMessageFromWebCoreException".equals(method.getName())) {
                method.setAccessible(true);
                Object para1[] = { coreException, "TestModel", "1", new Locale("en") };
                Object result1 = method.invoke(action, para1);
                String expectedMsg = "An error occurred.TestModel.1,RuntimeException";
                assertEquals(expectedMsg, result1);
                
                Object para2[] = { coreException, "", "1", new Locale("en") };
                Object result2 = method.invoke(action, para2);
                expectedMsg = "An error occurred.RuntimeException";
                assertEquals(expectedMsg, result2);
                
                Object para3[] = { coreException, "TestModel", "", new Locale("en") };
                Object result3 = method.invoke(action, para3);
                expectedMsg = "An error occurred.RuntimeException";
                assertEquals(expectedMsg, result3);
                
                Object para4[] = { coreException, "", "", new Locale("en") };
                Object result4 = method.invoke(action, para4);
                expectedMsg = "An error occurred.RuntimeException";
                assertEquals(expectedMsg, result4);
                break;
            }
        }
    }

    public void testFormatValue() throws Exception {
        String result = "";
        FormatModel formatModel = new FormatModel();

        formatModel.setObject(new Date());
        formatModel.setFormat("%tD");
        result = action.formatValue(formatModel);
        assertTrue(result.indexOf("/") != -1);

        formatModel.setLanguage("en");
        formatModel.setObject(1);
        formatModel.setFormat("%03d");
        result = action.formatValue(formatModel);
        assertEquals(result, "001");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dstr = "2013-01-15";
        formatModel.setLanguage("zh");
        formatModel.setObject(sdf.parse(dstr));
        formatModel.setFormat("%1$td/%1$tm/%1$ty");
        result = action.formatValue(formatModel);
        assertEquals(result, "15/01/13");

        formatModel.setLanguage("en");
        formatModel.setObject(new String("Hello"));
        formatModel.setFormat("%s World!");
        result = action.formatValue(formatModel);
        assertEquals(result, "Hello World!");
    }

    /**
     * test the getViewsListOrderedByLabels method checking if the list is well ordered
     */
    public void testGetViewsListOrderedByLabels() {
        Map<String, String> nameLabelMap = new HashMap<String, String>();
        nameLabelMap.put("Browse_items_Product", "produit");
        nameLabelMap.put("Browse_items_Product#AndFamily", "produit avec familly");
        nameLabelMap.put("Browse_items_Product#Unavailable", "produits non disponible");
        nameLabelMap.put("Browse_items_Product#Stores", "produit avec magasins");
        nameLabelMap.put("Browse_items_ProductFamily", "famille produit");
        nameLabelMap.put("Browse_items_Store", "magasin");
        nameLabelMap.put("Browse_items_shipped", "envoyés");

        List<ItemBaseModel> listRes = BrowseRecordsAction.getViewsListOrderedByLabels(nameLabelMap);

        assertEquals(7, listRes.size());
        assertEquals("envoyés", listRes.get(0).get("name"));
        assertEquals("famille produit", listRes.get(1).get("name"));
        assertEquals("magasin", listRes.get(2).get("name"));
        assertEquals("produit", listRes.get(3).get("name"));
        assertEquals("produit avec familly", listRes.get(4).get("name"));
        assertEquals("produit avec magasins", listRes.get(5).get("name"));
        assertEquals("produits non disponible", listRes.get(6).get("name"));

        // test with same value
        nameLabelMap = new HashMap<String, String>();
        nameLabelMap.put("Browse_items_Product", "produit");
        nameLabelMap.put("Browse_items_Product#AndFamily", "produit avec familly");
        nameLabelMap.put("Browse_items_Product#Unavailable", "produits non disponible");
        nameLabelMap.put("Browse_items_Product#Stores", "produit avec magasins");
        nameLabelMap.put("Browse_items_ProductFamily", "famille produit");
        nameLabelMap.put("Browse_items_Store", "magasin");
        nameLabelMap.put("Browse_items_Store2", "magasin");
        nameLabelMap.put("Browse_items_shipped", "envoyés");

        listRes = BrowseRecordsAction.getViewsListOrderedByLabels(nameLabelMap);

        assertEquals(8, listRes.size());
        assertEquals("envoyés", listRes.get(0).get("name"));
        assertEquals("famille produit", listRes.get(1).get("name"));
        assertEquals("magasin", listRes.get(2).get("name"));
        assertEquals("magasin", listRes.get(3).get("name"));
        assertEquals("produit", listRes.get(4).get("name"));
        assertEquals("produit avec familly", listRes.get(5).get("name"));
        assertEquals("produit avec magasins", listRes.get(6).get("name"));
        assertEquals("produits non disponible", listRes.get(7).get("name"));

    }

    public void testGetExsitedViewName() throws Exception {
        PowerMockito.mockStatic(org.talend.mdm.webapp.base.server.util.CommonUtil.class);
        XtentisPort port = PowerMockito.mock(XtentisPort.class);
        List<WSViewPK> viewPKs = new ArrayList<WSViewPK>();
        viewPKs.add(new WSViewPK("Browse_items_Product#Store"));
        viewPKs.add(new WSViewPK("Browse_items_Product#LiveAvailability"));
        viewPKs.add(new WSViewPK("Browse_items_Product#AndFamily"));
        WSViewPKArray viewPkArray = new WSViewPKArray(viewPKs.toArray(new WSViewPK[viewPKs.size()]));
        Mockito.when(org.talend.mdm.webapp.base.server.util.CommonUtil.getPort()).thenReturn(port);
        Mockito.when(port.getViewPKs(Mockito.any(WSGetViewPKs.class))).thenReturn(viewPkArray);
        assertEquals("Browse_items_Product#AndFamily", action.getExsitedViewName("Product"));

        viewPKs.add(new WSViewPK("Browse_items_Product"));
        viewPkArray = new WSViewPKArray(viewPKs.toArray(new WSViewPK[viewPKs.size()]));
        Mockito.when(port.getViewPKs(Mockito.any(WSGetViewPKs.class))).thenReturn(viewPkArray);
        assertEquals("Browse_items_Product", action.getExsitedViewName("Product"));
    }

    public void testSetDefaultValueByExpression() throws Exception {
        EntityModel entityModel = getViewBean("Product", "ProductDemo.xsd").getBindingEntityModel();
        entityModel = action.setDefaultValueByExpression(entityModel, "Product", "en");

        TypeModel name = entityModel.getTypeModel("Product/Name");
        assertEquals("\"MDM Product\"", name.getDefaultValueExpression());
        assertEquals("MDM Product", name.getDefaultValue());

        TypeModel price = entityModel.getTypeModel("Product/Price");
        assertEquals("fn:round(100.15)", price.getDefaultValueExpression());
        assertEquals("100", price.getDefaultValue());

        TypeModel color = entityModel.getTypeModel("Product/Features/Colors/Color");
        assertEquals("\"White\"", color.getDefaultValueExpression());
        assertEquals("White", color.getDefaultValue());
    }

    public void testExecuteVisibleRule() throws Exception {
        ViewBean viewBean = getViewBean("Product", "ProductDemo.xsd");
        String xml = getXml("ProductDemo.xml");
        List<VisibleRuleResult> results = action.executeVisibleRule(viewBean, xml);
        assertEquals(4, results.size());
        for (VisibleRuleResult result : results) {
            if (result.getXpath().contains("Name")) {
                assertEquals(true, result.isVisible());
                break;
            }
            if (result.getXpath().contains("Price")) {
                assertEquals(false, result.isVisible());
                break;
            }
            if (result.getXpath().contains("Description")) {
                assertEquals(false, result.isVisible());
                break;
            }
            if (result.getXpath().contains("Availability")) {
                assertEquals(true, result.isVisible());
                break;
            }
        }
    }
    
    private String parsingNodeValue(Document docXml, String xpath, String conceptName) throws Exception {
        NodeList nodes = Util.getNodeList(docXml, xpath.replaceFirst(conceptName + "/", "./"));
        if (nodes.getLength() > 0) {
            if (nodes.item(0) instanceof Element) {
                Element value = (Element) nodes.item(0);
                return value.getTextContent();
            }
        }
        return null;
    }

    /**
     * test the extractUsingTransformerThroughView method checking if wsItem's xml is covered by job's xml value
     * TMDM-9241 'Running the view result through a Process' doesn't work on occurence field
     */
    public void testExtractUsingTransformerThroughView() throws Exception {
        Method[] methods = BrowseRecordsAction.class.getDeclaredMethods();
        String[] ids = { "1" };
        Date insertDate = new Date();
        String content = "<Agency><AgencyId>1</AgencyId><Name>1</Name>"
                + "<Etablissement><IdEtablissement>1</IdEtablissement><Adresse>1</Adresse></Etablissement>"
                + "<Etablissement><IdEtablissement>2</IdEtablissement><Adresse>2</Adresse></Etablissement>"
                + "<Etablissement><IdEtablissement>3</IdEtablissement><Adresse>3</Adresse></Etablissement><Region>1</Region></Agency>";
        WSItem wsItem = new WSItem(new WSDataClusterPK("ModelTest"), "ModelTest", "Agency", ids, insertDate.getTime(), "1",
                content);
        getViewBean("Agency", "ModelTest.xsd");
        String[] businessElements = { "Agency/AgencyId", "Agency/Name", "Agency/Etablissement", "Agency/Region" };
        WSWhereCondition[] conditions = {};
        WSView view = new WSView("Browse_items_Agency", "[EN:Agency]", businessElements, conditions, businessElements,
                "LookupLocation", new WSBoolean(true));
        XtentisPort port = PowerMockito.mock(XtentisPort.class);
        PowerMockito.spy(com.amalto.webapp.core.util.Util.class);
        PowerMockito.doReturn(port).when(com.amalto.webapp.core.util.Util.class, "getPort");
        Mockito.when(port.getView(Mockito.any(WSGetView.class))).thenReturn(view);
        WSTransformerV2PK[] wst = { new WSTransformerV2PK("LookupLocation") };
        WSTransformerV2PKArray array = new WSTransformerV2PKArray();
        array.setWsTransformerV2PK(wst);
        Mockito.when(port.getTransformerV2PKs(Mockito.any(WSGetTransformerV2PKs.class))).thenReturn(array);
        String result = "<results>\n<Agency>\n<Region>Lausanne</Region>"
                + "\n<Etablissement><Adresse>Paris</Adresse></Etablissement>"
                + "\n<Etablissement><Adresse>Ville1</Adresse></Etablissement>" + "\n</Agency>\n</results>";
        WSByteArray byteArray = new WSByteArray(result.getBytes("utf-8"));
        WSTransformerContextPipelinePipelineItem[] entries = { new WSTransformerContextPipelinePipelineItem("output",
                new WSTypedContent(null, byteArray, "text/xml; charset=utf-8")) };
        WSTransformerContextPipeline line = new WSTransformerContextPipeline(entries);
        WSTransformerContext context = new WSTransformerContext(null, line, null);
        Mockito.when(port.executeTransformerV2(Mockito.any(WSExecuteTransformerV2.class))).thenReturn(context);
        ItemBean itemBean = new ItemBean();
        itemBean.setItemXml(content);

        for (Method method : methods) {
            if ("extractUsingTransformerThroughView".equals(method.getName())) {
                method.setAccessible(true);
                Object para[] = { "Agency", "Browse_items_Agency", ids, "ModelTest", "ModelTest", DataModelHelper.getEleDecl(),
                		itemBean };
                method.invoke(action, para);
                String expectedMsg = "<Agency><AgencyId>1</AgencyId><Name>1</Name>"
                        + "<Etablissement><IdEtablissement>1</IdEtablissement><Adresse>Paris</Adresse></Etablissement>"
                        + "<Etablissement><IdEtablissement>2</IdEtablissement><Adresse>Ville1</Adresse></Etablissement>"
                        + "<Etablissement><IdEtablissement>3</IdEtablissement><Adresse>3</Adresse></Etablissement>"
                        + "<Region>Lausanne</Region></Agency>";
                assertEquals(expectedMsg, itemBean.getItemXml().replaceAll("\r", "").replaceAll("\n", ""));
                break;
            }
        }
    }

    /**
     * the code comes from
     * org.talend.mdm.webapp.browserecords.server.actions.BrowseRecordsAction.builderNode(Map<String, Integer>, Element,
     * EntityModel, String, String, boolean, StringBuffer, boolean, String) <li>change findTypeModelByTypePath to
     * DataModelHelper.findTypeModelByTypePath(metaDataTypes, typePath) <li>change getForeignKeyDesc to
     * mock_getForeignKeyDesc
     */
    private ItemNodeModel builderNode(Map<String, Integer> multiNodeIndex, Element el, EntityModel entity, String baseXpath,
            String xpath, boolean isPolyType, StringBuffer foreignKeyDeleteMessage, boolean isCreate, String language)
            throws Exception {
        Map<String, TypeModel> metaDataTypes = entity.getMetaDataTypes();
        String realType = el.getAttribute("xsi:type"); //$NON-NLS-1$
        if (isPolyType) {
            xpath += ("".equals(xpath) ? el.getNodeName() : "/" + el.getNodeName()); //$NON-NLS-1$//$NON-NLS-2$
            if (realType != null && realType.trim().length() > 0) {
                xpath += ":" + realType; //$NON-NLS-1$
            }
        } else {
            xpath += ("".equals(xpath) ? el.getNodeName() : "/" + el.getNodeName()); //$NON-NLS-1$//$NON-NLS-2$
        }
        String typePath;
        if ("".equals(baseXpath)) { //$NON-NLS-1$
            typePath = xpath.replaceAll("\\[\\d+\\]", ""); //$NON-NLS-1$//$NON-NLS-2$
        } else {
            typePath = (baseXpath + "/" + xpath).replaceAll("\\[\\d+\\]", ""); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        }
        typePath = typePath.replaceAll(":" + realType + "$", ""); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        ItemNodeModel nodeModel = new ItemNodeModel(el.getNodeName());

        TypeModel model = DataModelHelper.findTypeModelByTypePath(metaDataTypes, typePath);
        nodeModel.setTypePath(model.getTypePath());
        nodeModel.setHasVisiblueRule(model.isHasVisibleRule());
        String realXPath = xpath;
        if (isPolyType) {
            realXPath = realXPath.replaceAll(":\\w+", ""); //$NON-NLS-1$//$NON-NLS-2$
        }

        if (model.getMaxOccurs() > 1 || model.getMaxOccurs() == -1) {

            Integer index = multiNodeIndex.get(realXPath);
            if (index == null) {
                nodeModel.setIndex(1);
                multiNodeIndex.put(realXPath, new Integer(1));
            } else {
                nodeModel.setIndex(index + 1);
                multiNodeIndex.put(realXPath, nodeModel.getIndex());
            }
        }

        if (realType != null && realType.trim().length() > 0) {
            nodeModel.setRealType(el.getAttribute("xsi:type")); //$NON-NLS-1$
        }
        nodeModel.setLabel(model.getLabel(language));
        nodeModel.setDescription(model.getDescriptionMap().get(language));
        nodeModel.setName(el.getNodeName());
        if (model.getMinOccurs() == 1 && model.getMaxOccurs() == 1) {
            nodeModel.setMandatory(true);
        }
        String foreignKey = model.getForeignkey();
        if (foreignKey != null && foreignKey.trim().length() > 0) {
            // set foreignKeyBean
            model.setRetrieveFKinfos(true);
            String modelType = el.getAttribute("tmdm:type"); //$NON-NLS-1$
            if (modelType != null && modelType.trim().length() > 0) {
                nodeModel.setTypeName(modelType);
            }
            ForeignKeyBean fkBean = mock_getForeignKeyDesc(model, el.getTextContent(), true, modelType);
            if (fkBean != null) {
                String fkNotFoundMessage = fkBean.get("foreignKeyDeleteMessage"); //$NON-NLS-1$
                if (fkNotFoundMessage != null) {// fix bug TMDM-2757
                    if (foreignKeyDeleteMessage.indexOf(fkNotFoundMessage) == -1) {
                        foreignKeyDeleteMessage.append(fkNotFoundMessage + "\r\n"); //$NON-NLS-1$
                    }
                    return nodeModel;
                }
                nodeModel.setObjectValue(fkBean);
            }
        } else if (model.isSimpleType()) {
            nodeModel.setObjectValue(el.getTextContent());
        }
        if (isCreate && model.getDefaultValueExpression() != null && model.getDefaultValueExpression().trim().length() > 0) {
            nodeModel.setChangeValue(true);
        }

        NodeList children = el.getChildNodes();
        if (children != null && !model.isSimpleType()) {
            List<TypeModel> childModels = null;
            if (nodeModel.getRealType() != null && nodeModel.getRealType().trim().length() > 0) {
                childModels = ((ComplexTypeModel) model).getRealType(nodeModel.getRealType()).getSubTypes();
            } else {
                childModels = ((ComplexTypeModel) model).getSubTypes();
            }
            for (TypeModel typeModel : childModels) { // display tree node according to the studio default configuration
                boolean existNodeFlag = false;
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        String tem_typePath;
                        if (realType != null && realType.trim().length() > 0) {
                            tem_typePath = typePath + ":" + realType + "/" + child.getNodeName(); //$NON-NLS-1$ //$NON-NLS-2$
                        } else {
                            tem_typePath = typePath + "/" + child.getNodeName(); //$NON-NLS-1$
                        }

                        if (typeModel.getTypePath().equals(tem_typePath)
                                || (typeModel.getTypePathObject() != null
                                        && typeModel.getTypePathObject().getAllAliasXpaths() != null && typeModel
                                        .getTypePathObject().getAllAliasXpaths().contains(tem_typePath))) {
                            ItemNodeModel childNode = builderNode(multiNodeIndex, (Element) child, entity, baseXpath, xpath,
                                    isPolyType, foreignKeyDeleteMessage, isCreate, language);
                            nodeModel.add(childNode);
                            existNodeFlag = true;
                            if (typeModel.getMaxOccurs() < 0 || typeModel.getMaxOccurs() > 1) {
                                continue;
                            } else {
                                break;
                            }
                        }
                    }
                }
                if (!existNodeFlag) { // add default tree node when the node has not been saved in DB.
                    nodeModel.add(org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getDefaultTreeModel(typeModel,
                            isCreate, language).get(0));
                }
            }

        }
        for (String key : entity.getKeys()) {
            if (key.equals(realXPath)) {
                nodeModel.setKey(true);
            }
        }
        return nodeModel;

    }

    private ForeignKeyBean mock_getForeignKeyDesc(TypeModel model, String ids, boolean isNeedExceptionMessage, String modelType)
            throws Exception {
        ForeignKeyBean bean = new ForeignKeyBean();
        bean.setId(ids);
        return bean;
    }

    private Map<ViewBean, Map<String, List<String>>> mock_getForeignKeyValues(String concept, String[] ids, String language)
            throws Exception {
        Map<ViewBean, Map<String, List<String>>> map = new HashMap<ViewBean, Map<String, List<String>>>();
        // 1. mock getView
        ViewBean viewBean = getViewBean(concept, "ProductDemo.xsd");
        Map<String, List<String>> fkValues = new HashMap<String, List<String>>();
        // 2. mock getItem
        WSItem wsItem = new WSItem();
        wsItem.setContent(getXml("ProductDemo.xml"));
        org.dom4j.Document doc = org.talend.mdm.webapp.base.server.util.XmlUtil.parseText(wsItem.getContent());
        EntityModel entityModel = viewBean.getBindingEntityModel();
        Map<String, TypeModel> metaData = entityModel.getMetaDataTypes();
        // 3. getAllFKValues
        for (String key : metaData.keySet()) {
            TypeModel typeModel = metaData.get(key);
            if (typeModel.getForeignkey() != null && typeModel.getForeignkey().trim().length() > 0) {
                fkValues.put(typeModel.getXpath(), new ArrayList<String>());
                List<?> nodeList = doc.selectNodes(typeModel.getXpath());
                if (nodeList != null && nodeList.size() > 0) {
                    for (int i = 0; i < nodeList.size(); i++) {
                        org.dom4j.Element current = (org.dom4j.Element) nodeList.get(i);
                        fkValues.get(typeModel.getXpath()).add(current.getText());
                    }
                }
            }
        }
        // 4. construct map
        map.put(viewBean, fkValues);
        return map;
    }

    private String getXml(String fileName) throws IOException {
        InputStream stream = BrowseRecordsActionTest.class.getResourceAsStream("../../" + fileName);
        return inputStream2String(stream);
    }

    private ViewBean getViewBean(String name, String fileName) throws IOException, SAXException {

        EntityModel entityModel = new EntityModel();

        String datamodelName = name;
        String concept = name;

        String[] ids = { "" };
        String[] roles = { "Demo_Manager", "System_Admin", "authenticated", "administration" };
        InputStream stream = BrowseRecordsActionTest.class.getResourceAsStream("../../" + fileName);
        String xsd = inputStream2String(stream);

        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(datamodelName)));
        DataModelHelper.parseSchema(datamodelName, concept, DataModelHelper.convertXsd2ElDecl(concept, xsd), ids, entityModel,
                Arrays.asList(roles));
        ViewBean viewBean = new ViewBean();
        viewBean.setBindingEntityModel(entityModel);
        return viewBean;
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
    
    private WSItem getWsItem(String cluster, String concept, String pk, String content) {
        WSItem item = new WSItem();
        item.setConceptName(concept);
        item.setContent(content);
        item.setDataModelName(cluster);
        if (org.apache.commons.lang.StringUtils.isNotEmpty(pk)) {
            item.setIds(pk.split("\\."));
        }
        item.setWsDataClusterPK(new WSDataClusterPK(cluster));
        return item;
    }
    
    private static class MockILocalUser extends ILocalUser {

        @Override
        public ILocalUser getILocalUser() throws XtentisException {
            return this;
        }

        @Override
        public HashSet<String> getRoles() {
            HashSet<String> roleSet = new HashSet<String>();
            roleSet.add("Demo_Manager");
            roleSet.add("System_Admin");
            roleSet.add("authenticated");
            roleSet.add("administration");
            return roleSet;
        }

        @Override
        public String getUsername() {
            return "Admin";
        }

        @Override
        public boolean isAdmin(Class<?> objectTypeClass) throws XtentisException {
            return true;
        }
    }

}