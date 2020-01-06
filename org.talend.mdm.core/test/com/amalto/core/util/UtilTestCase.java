/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.amalto.commons.core.utils.XMLUtils;
import com.amalto.core.delegator.IValidation;
import com.amalto.core.objects.DroppedItemPOJO;
import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import com.amalto.core.objects.transformers.util.TransformerContext;
import com.amalto.core.save.SaveException;
import com.amalto.core.storage.DispatchWrapper;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;
import com.amalto.xmlserver.interfaces.WhereLogicOperator;

/**
 * DOC achen class global comment. Detailled comment
 */
@SuppressWarnings("nls")
public class UtilTestCase extends TestCase {

    IValidation validation = new IValidation();
    
    public void testDefaultValidate() throws IOException, ParserConfigurationException, SAXException {
        // missing mandontory field cvc-complex-type.2.4.b
        InputStream in = UtilTestCase.class.getResourceAsStream("Agency_ME02.xml");
        String xml = getStringFromInputStream(in);
        Element element = Util.parse(xml).getDocumentElement();
        InputStream inxsd = UtilTestCase.class.getResourceAsStream("DStar.xsd");
        String schema = getStringFromInputStream(inxsd);

        try {
            validation.validation(element, schema);
        } catch (Exception e) {
            String str = e.getLocalizedMessage();
            assertTrue(str.contains("cvc-complex-type.2.4.b"));
        }
        // invalid content cvc-complex-type.2.4.

        String invalidXml = "<Agency>aa</Agency>";
        element = Util.parse(invalidXml).getDocumentElement();
        try {
            validation.validation(element, schema);
        } catch (Exception e) {
            String str = e.getLocalizedMessage();
            assertTrue(str.contains("cvc-complex-type.2.4"));
        }

        // correct xmlstring
        String xmlString = "<Agency xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "<Name>Portland</Name>"
                + "<City>Portland</City>" + "<State>ME</State>" + "<Zip>04102</Zip>" + "<Region>EAST</Region>" + "<Id>ME03</Id>"
                + "</Agency>";
        element = Util.parse(xmlString).getDocumentElement();
        try {
            validation.validation(element, schema);
        } catch (Exception e) {
            throw new SAXException(e);
        }

        // non mandontory field without nillable="true" should not contain empty child fields
        // mandontory filed with nillable="true" could contain empty child fields with xsi:nil="1"
        String xml1 = "<Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Picture>htt:aa</Picture><Id>id1</Id><Name>name2</Name><Description>des1</Description>"
                + "<Features><Sizes></Sizes><Colors><Color xsi:nil=\"1\"/></Colors></Features>"
                + "<Availability>false</Availability><Price>0.0</Price><Family></Family><OnlineStore>gg2@d</OnlineStore></Product>";
        element = Util.parse(xml1).getDocumentElement();
        try {
            validation.validation(element, schema);
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    public void testDefaultValidateWithExtEntity() throws IOException, ParserConfigurationException, SAXException {
        InputStream in = UtilTestCase.class.getResourceAsStream("Agency_ME02.xml");
        String xml = getStringFromInputStream(in);
        Element element = Util.parse(xml).getDocumentElement();
        InputStream inxsd = UtilTestCase.class.getResourceAsStream("DStar.xsd");
        String schema = getStringFromInputStream(inxsd);

        // correct xmlstring
        String xmlString = "<Agency xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "<Name>Portland</Name>"
                + "<City>Portland</City>" + "<State>ME</State>" + "<Zip>04102</Zip>" + "<Region>EAST</Region>" + "<Id>ME03</Id>"
                + "</Agency>";
        element = Util.parse(xmlString).getDocumentElement();
        try {
            validation.validation(element, schema);
        } catch (Exception e) {
            throw new SAXException(e);
        }

        inxsd = UtilTestCase.class.getResourceAsStream("DStar2.xsd");
        schema = getStringFromInputStream(inxsd);
        String xml1 = "<Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Picture>htt:aa</Picture><Id>id1</Id><Name>name2</Name><Description>des1</Description>"
                + "<Features><Sizes></Sizes><Colors><Color xsi:nil=\"1\"/></Colors></Features>"
                + "<Availability>false</Availability><Price>0.0</Price><Family></Family><OnlineStore>gg2@d</OnlineStore></Product>";
        element = Util.parse(xml1).getDocumentElement();
        try {
            validation.validation(element, schema);
            fail("External entities are not allowed in Schema.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("External entities are not allowed in Schema."));
        }
    }

    private static String getStringFromInputStream(InputStream in) throws IOException {
        int total = in.available();
        byte[] buf = new byte[total];
        in.read(buf);
        return new String(buf);
    }

    String[] ids = new String[] { "1" };

    String concept = "Product";

    String projection = "<Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>1</Id><Name>name1</Name><Description>desc1</Description><Price>1</Price></Product>";

    DataClusterPOJOPK dcpk = new DataClusterPOJOPK("Product");

    /**
     * Simulate test beforeDeleting DOC Administrator Comment method "testBeforeDeleting".
     * 
     * @throws Exception
     */
    public void testBeforeDeleting() throws Exception {
        // delete item in recyclebin
        ItemPOJO pojo = null;
        String ret = beforeDeleting(pojo);
        assertTrue(ret != null && ret.contains("[EN:ok]"));

        // delete item in data container
        pojo = new ItemPOJO();
        pojo.setDataClusterPK(dcpk);
        pojo.setItemIds(ids);
        pojo.setConceptName(concept);
        pojo.setProjectionAsString(projection);

        ret = beforeDeleting(pojo);

        assertTrue(ret != null && ret.contains("[EN:ok]"));
    }

    /**
     * the simulate droppedItem DOC Administrator Comment method "getDroppedItem".
     * 
     * @return
     */
    private DroppedItemPOJO getDroppedItem() {
        DroppedItemPOJO pojo = new DroppedItemPOJO();
        pojo.setProjection("<ii><c>Product</c><n>Product</n><dmn>Product</dmn><i>1</i><t>1330671403828</t><p>" + projection
                + "</p></ii>");

        return pojo;
    }

    /**
     * the simulate beforeDeleting() according to the Util.beforeDeleting() DOC Administrator Comment method
     * "beforeDeleting".
     * 
     * @param pj
     * @return
     * @throws Exception
     */
    private String beforeDeleting(ItemPOJO pj) throws Exception {
        // check before deleting transformer
        boolean isBeforeDeletingTransformerExist = true;

        if (isBeforeDeletingTransformerExist) {
            try {
                // call before deleting transformer
                // load the item
                ItemPOJO pojo = pj;// ItemPOJO.load(itempk);
                String xml = null;
                if (pojo == null) {// load from recyclebin
                    DroppedItemPOJO dpPojo = getDroppedItem(); // Util.getDroppedItemCtrlLocal().loadDroppedItem(dpitempk);
                    if (dpPojo != null) {
                        xml = dpPojo.getProjection();
                        Document doc = Util.parse(xml);
                        Node item = (Node) XPathFactory.newInstance().newXPath().evaluate("//ii/p", doc, XPathConstants.NODE); //$NON-NLS-1$
                        if (item != null && item instanceof Element) {
                            NodeList list = item.getChildNodes();
                            Node node = null;
                            for (int i = 0; i < list.getLength(); i++) {
                                if (list.item(i) instanceof Element) {
                                    node = list.item(i);
                                    break;
                                }
                            }
                            if (node != null) {
                                xml = Util.nodeToString(node);
                            }
                        }
                    }
                } else {
                    xml = pojo.getProjectionAsString();
                }
                String resultUpdateReport = null;//Util.createUpdateReport(ids, concept, "PHYSICAL_DELETE", null,                         "", dcpk.getUniqueId()); //$NON-NLS-1$
                String exchangeData = Util.mergeExchangeData(xml, resultUpdateReport);
                final String RUNNING = "XtentisWSBean.executeTransformerV2.beforeDeleting.running";
                TransformerContext context = new TransformerContext(new TransformerV2POJOPK("beforeDeleting_Product"));
                context.put(RUNNING, Boolean.TRUE);
                String outputErrorMessage = "<message type=\"info\">[EN:ok]</message>";
                if (xml == null) {
                    outputErrorMessage = null;
                }
                // handle error message
                if (outputErrorMessage != null && outputErrorMessage.length() > 0) {
                    return outputErrorMessage;
                } else {
                    return "<report><message type=\"error\"/></report> "; //$NON-NLS-1$
                }
            } catch (Exception e) {
                Logger.getLogger(Util.class).error(e);
                throw e;
            }
        }
        // TODO Scan the entries - in priority, taka the content of the specific
        // entry
        return null;
    }

    public void testGetException() {
        String str = "Failed";
        Exception e = new Exception(new SaveException(new ValidateException((str))));
        ValidateException ve = Util.getException(e, ValidateException.class);
        assertNotNull(ve);
        assertEquals(str, ve.getMessage());

        e = new Exception(new SaveException(new Exception(str)));
        ve = Util.getException(e, ValidateException.class);
        assertNull(ve);
    }

    public void testIsSystemDC() throws Exception {
        assertFalse(DispatchWrapper.isMDMInternal(null));
        assertTrue(DispatchWrapper.isMDMInternal("PROVISIONING"));
        assertTrue(DispatchWrapper.isMDMInternal("MDMDomainObjects"));
        assertTrue(DispatchWrapper.isMDMInternal("SearchTemplate"));
        assertTrue(DispatchWrapper.isMDMInternal("SearchTemplate/HierarchySearchItem"));
        assertFalse(DispatchWrapper.isMDMInternal("Product"));
        assertFalse(DispatchWrapper.isMDMInternal("Product/Product"));
    }

    @SuppressWarnings("cast")
    public void testUpdateUserPropertyCondition() throws Exception {
        String userXML = "<User><username>administrator</username><password>200ceb26807d6bf99fd6f4f0d1ca54d4</password><givenname>Default</givenname><familyname>Administrator</familyname><company>Company</company><id>1001</id><realemail>admin@company.com</realemail><viewrealemail>no</viewrealemail><registrationdate>1397444277524</registrationdate><lastvisitdate>0</lastvisitdate><enabled>yes</enabled><homepage>Home</homepage><language>en</language><roles><role>System_Admin</role><role>administration</role></roles><properties><property><name>model</name><value>Product</value></property><property><name>location</name><value>bj</value></property><property><name>cluster</name><value>Product</value></property></properties></User>";//$NON-NLS-1$
        String conditionDesc1 = "${user_context.properties['location']}";//$NON-NLS-1$
        String conditionDesc2 = "${user_context.registrationdate}";//$NON-NLS-1$
        String conditionDesc3 = "${user_context.enabled}";//$NON-NLS-1$
        String conditionDesc4 = "${user_context.language}";//$NON-NLS-1$
        String conditionDesc5 = "${user_context.testproperty}";//$NON-NLS-1$

        WhereCondition condition1 = new WhereCondition();
        WhereCondition condition2 = new WhereCondition();
        WhereCondition condition3 = new WhereCondition();
        WhereCondition condition4 = new WhereCondition();
        WhereCondition condition5 = new WhereCondition();

        ArrayList<WhereCondition> conditions = new ArrayList<WhereCondition>();
        condition1.setRightValueOrPath(conditionDesc1);
        conditions.add(condition1);
        condition2.setRightValueOrPath(conditionDesc2);
        conditions.add(condition2);
        condition3.setRightValueOrPath(conditionDesc3);
        conditions.add(condition3);
        condition4.setRightValueOrPath(conditionDesc4);
        conditions.add(condition4);
        condition5.setRightValueOrPath(conditionDesc5);
        conditions.add(condition5);

        Util.updateUserPropertyCondition(conditions, userXML);
        assertTrue(conditions.size() == 4);

        if (conditions.get(0) instanceof WhereCondition) {
            WhereCondition updateCondition = (WhereCondition) conditions.get(0);
            assertTrue("bj".equals(updateCondition.getRightValueOrPath()));//$NON-NLS-1$
        }
        if (conditions.get(1) instanceof WhereCondition) {
            WhereCondition updateCondition = (WhereCondition) conditions.get(1);
            assertTrue("1397444277524".equals(updateCondition.getRightValueOrPath()));//$NON-NLS-1$
        }
        if (conditions.get(2) instanceof WhereCondition) {
            WhereCondition updateCondition = (WhereCondition) conditions.get(2);
            assertTrue("true".equals(updateCondition.getRightValueOrPath()));//$NON-NLS-1$
        }
        if (conditions.get(3) instanceof WhereCondition) {
            WhereCondition updateCondition = (WhereCondition) conditions.get(3);
            assertTrue("en".equals(updateCondition.getRightValueOrPath()));//$NON-NLS-1$
        }
    }

    public void testFixWebConditions() throws Exception {
        String userXML = "<User><username>administrator</username><password>200ceb26807d6bf99fd6f4f0d1ca54d4</password><givenname>Default</givenname><familyname>Administrator</familyname><company>Company</company><id>1001</id><realemail>admin@company.com</realemail><viewrealemail></viewrealemail><registrationdate>1397444277524</registrationdate><lastvisitdate>0</lastvisitdate><enabled>yes</enabled><homepage>Home</homepage><language>en</language><roles><role>System_Admin</role><role>administration</role></roles><properties><property><name>model</name><value>Product</value></property><property><name>location</name><value>bj</value></property><property><name>cluster</name><value>Product</value></property></properties></User>";//$NON-NLS-1$

        List<IWhereItem> whereItems = new ArrayList<IWhereItem>();
        IWhereItem whereItem1 = new WhereCondition("Product/Price", "=", "${user_context.registrationdate}", "NONE");
        IWhereItem whereItem2 = new WhereCondition("Product/Description", "=", "${user_context.enabled}", "NONE");
        IWhereItem whereItem3 = new WhereCondition("Product/Description", "=", "${user_context.viewrealemail}", "NONE");
        IWhereItem whereItem4 = new WhereCondition("Product/Description", "=", "${user_context.nosuchattribute}", "NONE");
        IWhereItem whereItem5 = new WhereCondition("Product/Description", "=", "${user_context.properties['location']}", "NONE");
        IWhereItem whereItem6 = new WhereCondition("Product/Description", "=", "${user_context.properties['error']}", "NONE");
        IWhereItem whereItem7 = new WhereCondition("Product/Description", "=", "${user_context.properties['']}", "NONE");
        IWhereItem whereItem8 = new WhereCondition("Product/Family", "Is Empty Or Null", null, "NONE");

        whereItems.add(whereItem1);
        whereItems.add(whereItem2);
        whereItems.add(whereItem3);
        whereItems.add(whereItem4);
        whereItems.add(whereItem5);
        whereItems.add(whereItem6);
        whereItems.add(whereItem7);
        whereItems.add(whereItem8);
        IWhereItem iWhereAnd = new WhereAnd(whereItems);

        Util.fixWebConditions(iWhereAnd, userXML);

        if (iWhereAnd instanceof WhereLogicOperator) {
            List<IWhereItem> subItems = ((WhereLogicOperator) iWhereAnd).getItems();

            assertTrue(subItems.size() == 5);

            WhereCondition condition1 = (WhereCondition) subItems.get(0);
            WhereCondition condition2 = (WhereCondition) subItems.get(1);
            WhereCondition condition3 = (WhereCondition) subItems.get(2);
            WhereCondition condition4 = (WhereCondition) subItems.get(3);
            WhereCondition condition8 = (WhereCondition) subItems.get(4);

            assertTrue("1397444277524".equals(condition1.getRightValueOrPath()));
            assertTrue("true".equals(condition2.getRightValueOrPath()));
            assertTrue("false".equals(condition3.getRightValueOrPath()));
            assertTrue("bj".equals(condition4.getRightValueOrPath()));
            assertTrue("Is Empty Or Null".equals(condition8.getOperator()));
        }
    }
    
    public void testSetUserPropertyDoesNotExist() throws Exception {
        String xml = "<User><username>A</username><id>B</id><properties><property><name>x</name><value>y</value></property></properties></User>";
        Document user = Util.parse(xml);
        Util.setUserProperty(user, "newprop", "newvalue");
        
        String result = XMLUtils.nodeToString(user, true, false).replaceAll("\r\n", "\n");
        assertEquals("<User><username>A</username><id>B</id><properties><property><name>x</name><value>y</value></property><property><name>newprop</name><value>newvalue</value></property></properties></User>", result);
    }
    
    public void testSetUserPropertyNoProperties() throws Exception {
        String xml = "<User><username>A</username><id>B</id></User>";
        Document user = Util.parse(xml);
        Util.setUserProperty(user, "newprop", "newvalue");
        
        String result = XMLUtils.nodeToString(user, true, false).replaceAll("\r\n", "\n");
        assertEquals("<User><username>A</username><id>B</id><properties><property><name>newprop</name><value>newvalue</value></property></properties></User>", result);
    }
    
    public void testSetUserPropertyOverride() throws Exception {
        String xml = "<User><username>A</username><id>B</id><properties><property><name>x</name><value>y</value></property></properties></User>";
        Document user = Util.parse(xml);
        Util.setUserProperty(user, "x", "z");
        
        String result = XMLUtils.nodeToString(user, true, false).replaceAll("\r\n", "\n");
        assertEquals("<User><username>A</username><id>B</id><properties><property><name>x</name><value>z</value></property></properties></User>", result);
    }
    
    public void testSetUserPropertyNullName() throws Exception {
        String xml = "<User><username>A</username><id>B</id><properties><property><name>x</name><value>y</value></property></properties></User>";
        Document user = Util.parse(xml);
        Util.setUserProperty(user, null, "z");
        
        String result = XMLUtils.nodeToString(user, true, false).replaceAll("\r\n", "\n");
        assertEquals("<User><username>A</username><id>B</id><properties><property><name>x</name><value>y</value></property></properties></User>", result);
    }
    
    public void testMessagesModifyPatternAccordingToArgs() throws Exception {
        Messages messages = new Messages("org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages", new TestClassLoader());
        StringBuffer pattern = new StringBuffer("An error occurred.");
        Object[] args = new Object[]{"error message one", "error message two"};
        messages.ModifyPatternAccordingToArgs(pattern, args);
        MessageFormat formatter = new MessageFormat(pattern.toString());
        assertEquals("An error occurred.error message one", formatter.format(args));
    }

    public void testRemoveBracketWithNumber() throws Exception {
        String expectedResult = "/detail/feature/actor";
        assertEquals(expectedResult, Util.removeBracketWithNumber("/detail/feature/actor[1]"));
        assertEquals(expectedResult, Util.removeBracketWithNumber("/detail/feature[2]/actor"));
        assertEquals(expectedResult, Util.removeBracketWithNumber("/detail[3]/feature/actor"));
        assertEquals(expectedResult, Util.removeBracketWithNumber("/detail[12]/feature/actor[1]"));
        assertEquals(expectedResult, Util.removeBracketWithNumber("/detail/feature[13]/actor[1]"));
        assertEquals(expectedResult, Util.removeBracketWithNumber("/detail[12]/feature[0]/actor[1]"));
        assertEquals("", Util.removeBracketWithNumber(""));

        assertEquals("/detail/feature/actor[s]", Util.removeBracketWithNumber("/detail/feature/actor[s]"));

        assertNull(Util.removeBracketWithNumber(null));
    }

    private class TestClassLoader extends ClassLoader {
    }
}
