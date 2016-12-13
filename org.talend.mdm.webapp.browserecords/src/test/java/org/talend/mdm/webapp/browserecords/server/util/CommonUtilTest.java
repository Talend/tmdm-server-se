/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.dom4j.Node;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit3.PowerMockSuite;
import org.talend.mdm.commmon.util.datamodel.management.DataModelID;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.DataModelHelper;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.SchemaMockAgent;
import org.talend.mdm.webapp.browserecords.server.ruleengine.DisplayRuleEngine;
import org.talend.mdm.webapp.browserecords.shared.VisibleRuleResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.amalto.core.util.Util;
import com.amalto.webapp.core.util.XmlUtil;

@PrepareForTest({ Util.class })
@SuppressWarnings("nls")
public class CommonUtilTest extends TestCase {

    private static final Logger LOG = Logger.getLogger(CommonUtilTest.class);

    @SuppressWarnings("unchecked")
    public static TestSuite suite() throws Exception {
        return new PowerMockSuite("Unit tests for " + CommonUtilTest.class.getSimpleName(), CommonUtilTest.class);
    }

    public void testGetSubXML() {

        Map<String, TypeModel> metaDataTypes = new HashMap<String, TypeModel>();
        ComplexTypeModel edaType = new ComplexTypeModel("Eda", null); //$NON-NLS-1
        edaType.setTypePath("Eda"); //$NON-NLS-1
        metaDataTypes.put(edaType.getTypePath(), edaType);

        SimpleTypeModel idEdaType = new SimpleTypeModel("idEda", null); //$NON-NLS-1
        idEdaType.setTypePath("Eda/idEda"); //$NON-NLS-1
        metaDataTypes.put(idEdaType.getTypePath(), idEdaType);
        edaType.addSubType(idEdaType);

        SimpleTypeModel statutEdaType = new SimpleTypeModel("statutEda", DataTypeConstants.STRING); //$NON-NLS-1
        statutEdaType.setTypePath("Eda/statutEda"); //$NON-NLS-1
        statutEdaType.setDefaultValueExpression("\"Brouillon\""); //$NON-NLS-1
        metaDataTypes.put(statutEdaType.getTypePath(), statutEdaType);
        edaType.addSubType(statutEdaType);

        SimpleTypeModel demandeValidationType = new SimpleTypeModel("demandeValidation", null); //$NON-NLS-1
        demandeValidationType.setTypePath("Eda/demandeValidation"); //$NON-NLS-1
        demandeValidationType.setVisibleExpression("../statutEda = \"Brouillon\""); //$NON-NLS-1
        metaDataTypes.put(demandeValidationType.getTypePath(), demandeValidationType);
        edaType.addSubType(demandeValidationType);

        SimpleTypeModel demandeDiffusionType = new SimpleTypeModel("demandeDiffusion", null); //$NON-NLS-1
        demandeDiffusionType.setTypePath("Eda/demandeDiffusion"); //$NON-NLS-1
        demandeDiffusionType.setVisibleExpression("../statutEda = \"Validée\""); //$NON-NLS-1
        metaDataTypes.put(demandeDiffusionType.getTypePath(), demandeDiffusionType);
        edaType.addSubType(demandeDiffusionType);

        Document doc = null;
        try {
            doc = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getSubXML(edaType, null, null, "en"); //$NON-NLS-1
        } catch (ServiceException e) {
            LOG.debug("assert no exception, please check CommonUtil.getSubXML(TypeModek, String, String)", e); //$NON-NLS-1
        }
        assertNotNull(doc);
        Element root = doc.getDocumentElement();
        assertEquals("http://www.w3.org/2001/XMLSchema-instance", root.getAttribute("xmlns:xsi")); //$NON-NLS-1 //$NON-NLS-2
        assertNotNull(doc);
        DisplayRuleEngine ruleEngine = new DisplayRuleEngine(metaDataTypes, "Eda"); //$NON-NLS-1
        org.dom4j.Document doc4j = null;
        try {
            doc4j = XmlUtil.parseDocument(doc);
        } catch (Exception e) {
            LOG.debug("assert no exception, please check XmlUtil.parseDocument(Document)", e); //$NON-NLS-1
        }
        assertNotNull(doc4j);
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<Eda xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><idEda/><statutEda/><demandeValidation/><demandeDiffusion/></Eda>", doc4j.asXML()); //$NON-NLS-1
        ruleEngine.execDefaultValueRule(doc4j);
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<Eda xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><idEda/><statutEda>Brouillon</statutEda><demandeValidation/><demandeDiffusion/></Eda>", doc4j.asXML()); //$NON-NLS-1

        List<VisibleRuleResult> visibleItems = ruleEngine.execVisibleRule(doc4j);
        assertNotNull(visibleItems);
        assertEquals(2, visibleItems.size());
        assertVisibleItemsContainValue(visibleItems, "Eda/demandeValidation[1]", true); //$NON-NLS-1
        assertVisibleItemsContainValue(visibleItems, "Eda/demandeDiffusion[1]", false); //$NON-NLS-1

        Node node = doc4j.selectSingleNode("Eda/statutEda[1]"); //$NON-NLS-1
        assertNotNull(node);
        node.setText("Validée"); //$NON-NLS-1

        visibleItems = ruleEngine.execVisibleRule(doc4j);

        assertVisibleItemsContainValue(visibleItems, "Eda/demandeValidation[1]", false); //$NON-NLS-1
        assertVisibleItemsContainValue(visibleItems, "Eda/demandeDiffusion[1]", true); //$NON-NLS-1

        node = doc4j.selectSingleNode("Eda/statutEda[1]"); //$NON-NLS-1
        assertNotNull(node);
        node.setText("A valider"); //$NON-NLS-1

        visibleItems = ruleEngine.execVisibleRule(doc4j);

        assertVisibleItemsContainValue(visibleItems, "Eda/demandeValidation[1]", false); //$NON-NLS-1
        assertVisibleItemsContainValue(visibleItems, "Eda/demandeDiffusion[1]", false); //$NON-NLS-1

    }

    private void assertVisibleItemsContainValue(List<VisibleRuleResult> visibleItems, String xpath, boolean expectedValue) {
        VisibleRuleResult visibleItem = null;
        for (VisibleRuleResult item : visibleItems) {
            if (item.getXpath().equals(xpath)) {
                visibleItem = item;
                break;
            }
        }
        assertNotNull(visibleItem);
        assertEquals(expectedValue, visibleItem.isVisible());
    }

    public void testHandleProcessMessage() {
        String outputMessage = "<report><message/></report>";
        String language = "en";
        Map<String, String> map = null;
        try {
            map = CommonUtil.handleProcessMessage(outputMessage, language);
        } catch (Exception e) {
            fail();
        }
        assertNotNull(map);
        assertNotNull(map.get("typeCode"));
        assertTrue(map.get("typeCode").equalsIgnoreCase(""));
        assertNull(map.get("message"));

        map.clear();
        outputMessage = "<report><message type=\"error\" /></report>";
        language = "en";
        try {
            map = CommonUtil.handleProcessMessage(outputMessage, language);
        } catch (Exception e) {
            fail();
        }
        assertNotNull(map);
        assertNotNull(map.get("typeCode"));
        assertEquals("error", map.get("typeCode"));
        assertNull(map.get("message"));

        map.clear();
        outputMessage = "<report><message type=\"info\" >[en:message_en][fr:message_fr]</message></report>";
        language = "en";
        try {
            map = CommonUtil.handleProcessMessage(outputMessage, language);
        } catch (Exception e) {
            fail();
        }
        assertNotNull(map);
        assertNotNull(map.get("typeCode"));
        assertEquals("info", map.get("typeCode"));
        assertNotNull(map.get("message"));
        assertEquals("message_en", map.get("message"));

        map.clear();
        outputMessage = "<report><message type=\"info\" >Test_Message</message></report>";
        language = "en";
        try {
            map = CommonUtil.handleProcessMessage(outputMessage, language);
        } catch (Exception e) {
            fail();
        }
        assertNotNull(map);
        assertNotNull(map.get("typeCode"));
        assertEquals("info", map.get("typeCode"));
        assertNotNull(map.get("message"));
        assertEquals("Test_Message", map.get("message"));

        map.clear();
        outputMessage = "<report><message type=\"info\" ><subMessage></subMessage></message></report>";
        language = "en";
        try {
            map = CommonUtil.handleProcessMessage(outputMessage, language);
        } catch (Exception e) {
            fail();
        }
        assertNotNull(map);
        assertNotNull(map.get("typeCode"));
        assertEquals("info", map.get("typeCode"));
        assertNull(map.get("message"));

        map.clear();
        outputMessage = "<report><message type=\"info\" ><subMessage>test_subMessage</subMessage></message></report>";
        language = "en";
        try {
            map = CommonUtil.handleProcessMessage(outputMessage, language);
        } catch (Exception e) {
            fail();
        }
        assertNotNull(map);
        assertNotNull(map.get("typeCode"));
        assertEquals("info", map.get("typeCode"));
        assertNotNull(map.get("message"));
        assertEquals("test_subMessage", map.get("message"));

        map.clear();
        outputMessage = "<report><message type=\"info\" ><subMessage><subsub>subsub</subsub></subMessage></message></report>";
        language = "en";
        try {
            map = CommonUtil.handleProcessMessage(outputMessage, language);
        } catch (Exception e) {
            fail();
        }
        assertNotNull(map);
        assertNotNull(map.get("typeCode"));
        assertEquals("info", map.get("typeCode"));
        assertNull(map.get("message"));
    }

    public void testGetDefaultTreeModel() throws SAXException {
        String datamodelName = "Polymorphism";
        String concept = "Assignment";
        String[] ids = { "" };
        String[] roles = { "System_Admin" };
        InputStream stream = getClass().getResourceAsStream("Polymorphism.xsd");
        String language = "en";
        String xsd = inputStream2String(stream);

        EntityModel employeeModel = new EntityModel();

        PowerMockito.mockStatic(Util.class);
        Mockito.when(Util.isEnterprise()).thenReturn(false);
        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(datamodelName)));
        DataModelHelper.parseSchema(datamodelName, concept, DataModelHelper.convertXsd2ElDecl(concept, xsd), ids, employeeModel,
                Arrays.asList(roles));

        Map<String, TypeModel> types = employeeModel.getMetaDataTypes();
        TypeModel addressType = types.get("Assignment/AddressType");
        assertNotNull(addressType);
        List<ItemNodeModel> list = CommonUtil.getDefaultTreeModel(addressType, true, language);
        ItemNodeModel addressNodeModel = list.get(0);
        assertEquals("", addressNodeModel.getRealType());
        assertEquals(0, addressNodeModel.getChildCount());

    }

    public void testGetDefaultXML() throws Exception {
        String datamodelName = "Test";
        String concept = "e";
        String[] ids = { "" };
        String[] roles = { "System_Admin" };
        InputStream stream = getClass().getResourceAsStream("TestReusableType.xsd");
        String language = "en";
        String xsd = inputStream2String(stream);

        EntityModel testModel = new EntityModel();

        PowerMockito.mockStatic(Util.class);
        Mockito.when(Util.isEnterprise()).thenReturn(false);
        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(datamodelName)));
        DataModelHelper.parseSchema(datamodelName, concept, DataModelHelper.convertXsd2ElDecl(concept, xsd), ids, testModel,
                Arrays.asList(roles));

        Map<String, TypeModel> types = testModel.getMetaDataTypes();
        TypeModel rootModel = types.get("e");
        assertNotNull(rootModel);
        Document doc = CommonUtil.getSubXML(rootModel, null, null, language);
        assertNotNull(doc);

        org.dom4j.Document doc4j = XmlUtil.parseDocument(doc);
        assertNotNull(doc4j);
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<e xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id/><b xsi:type=\"a2\"><subelement/></b></e>";
        String actualXML = doc4j.asXML();
        assertEquals(expectedXML, actualXML);
    }

    public void testDynamicAssembleByResultOrder() throws Exception {
        ItemBean itemBean = new ItemBean(
                "Contract",
                "1",
                "<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>1</id><comment>comment1</comment><detail/><code>code1</code><xsi:type>ContractDetailType</xsi:type><enumEle>pending</enumEle></result>");
        List<String> viewableXpaths = new ArrayList<String>();
        viewableXpaths.add("Contract/id");
        viewableXpaths.add("Contract/comment");
        viewableXpaths.add("Contract/detail");
        viewableXpaths.add("Contract/detail/code");
        viewableXpaths.add("Contract/detail/@xsi:type");
        viewableXpaths.add("Contract/enumEle");
        EntityModel entityModel = new EntityModel();
        entityModel.setMetaDataTypes(new LinkedHashMap<String, TypeModel>());
        CommonUtil.dynamicAssembleByResultOrder(itemBean, viewableXpaths, entityModel, new HashMap<String, EntityModel>(), "en",
                false);
        assertEquals("1", itemBean.get("Contract/id"));
        assertEquals("comment1", itemBean.get("Contract/comment"));
        assertEquals("", itemBean.get("Contract/detail"));
        assertEquals("code1", itemBean.get("Contract/detail/code"));
        assertEquals("ContractDetailType", itemBean.get("Contract/detail/@xsi:type"));
        assertEquals("pending", itemBean.get("Contract/enumEle"));
    }

    private String inputStream2String(InputStream is) {

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            fail();
        }
        return buffer.toString();

    }
}