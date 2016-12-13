/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.ClientResourceData;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtilTestData;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.junit.client.GWTTestCase;

@SuppressWarnings("nls")
public class ItemTreeHandlerGWTTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }

    public void testSerializeItemWhenBeforeLoad() {

        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelProduct());
        viewBean.setBindingEntityModel(entity);

        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordProduct1(), entity);

        ItemTreeHandler itemHandler = new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.BeforeLoad);
        String actualXml = itemHandler.serializeItem();
        String expectedXml = "<Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>1</Id><Name>Test product 1</Name><Availability>false</Availability><Price>10</Price><OnlineStore>@@http://</OnlineStore><Picture>http://www.talendforge.org/img/style/talendforge.jpg</Picture></Product>";
        assertEquals(expectedXml, actualXml);

    }

    public void testSerializeItemWhenInUse() {

        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelProduct());
        viewBean.setBindingEntityModel(entity);

        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordProduct1(), entity);

        assertNotNull(nodeModel);
        assertTrue(nodeModel.getChildren().size() > 0);

        for (ModelData eachNodeModel : nodeModel.getChildren()) {
            ItemNodeModel myNodeModel = (ItemNodeModel) eachNodeModel;
            if (myNodeModel.getTypePath().equals("Product/Price")) {
                myNodeModel.setObjectValue("150");// update
            } else if (myNodeModel.getTypePath().equals("Product/Picture")) {
                myNodeModel.setObjectValue(null);// delete
            }
        }

        // add
        ItemNodeModel featuresNodeModel = new ItemNodeModel("Features");
        featuresNodeModel.setTypePath("Product/Features");

        ItemNodeModel colorsNodeModel = new ItemNodeModel("Colors");
        colorsNodeModel.setTypePath("Product/Features/Colors");

        ItemNodeModel colorNodeModel = new ItemNodeModel("Color");
        colorNodeModel.setTypePath("Product/Features/Colors/Color");
        colorNodeModel.setObjectValue("White");

        featuresNodeModel.add(colorsNodeModel);
        colorsNodeModel.add(colorNodeModel);

        nodeModel.add(featuresNodeModel);

        ItemTreeHandler itemHandler = new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.InUse);
        String actualXml = itemHandler.serializeItem();

        String expectedXml = "<Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>1</Id><Name>Test product 1</Name><Features><Colors><Color>White</Color></Colors></Features><Availability>false</Availability><Price>150</Price><OnlineStore>@@http://</OnlineStore><Picture/></Product>";
        assertEquals(expectedXml, actualXml);

    }

    public void testSerializeItemContainsLookupField() {
        ItemBean itemBean = new ItemBean();
        itemBean.set("Product/Name", "lookupValue");
        itemBean.setOriginalLookupFieldDisplayValueMap(new HashMap<String, List<String>>());
        itemBean.setOriginalLookupFieldValueMap(new HashMap<String, List<String>>());
        itemBean.getOriginalLookupFieldDisplayValueMap().put("Product/Name", new ArrayList<String>());
        itemBean.getOriginalLookupFieldValueMap().put("Product/Name", new ArrayList<String>());
        itemBean.getOriginalLookupFieldDisplayValueMap().get("Product/Name").add("lookupValue");
        itemBean.getOriginalLookupFieldValueMap().get("Product/Name").add("originalValue");
        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelProduct());
        viewBean.setBindingEntityModel(entity);

        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordProduct1(), entity);
        assertNotNull(nodeModel);
        assertTrue(nodeModel.getChildren().size() > 0);

        for (ModelData eachNodeModel : nodeModel.getChildren()) {
            ItemNodeModel myNodeModel = (ItemNodeModel) eachNodeModel;
            if (myNodeModel.getTypePath().equals("Product/Price")) {
                myNodeModel.setObjectValue("150");// update
            } else if (myNodeModel.getTypePath().equals("Product/Picture")) {
                myNodeModel.setObjectValue(null);// delete
            } else if (myNodeModel.getTypePath().equals("Product/Name")) {
                myNodeModel.setObjectValue("lookupValue");
            }
        }

        ItemNodeModel featuresNodeModel = new ItemNodeModel("Features");
        featuresNodeModel.setTypePath("Product/Features");
        ItemNodeModel colorsNodeModel = new ItemNodeModel("Colors");
        colorsNodeModel.setTypePath("Product/Features/Colors");
        ItemNodeModel colorNodeModel = new ItemNodeModel("Color");
        colorNodeModel.setTypePath("Product/Features/Colors/Color");
        colorNodeModel.setObjectValue("White");
        featuresNodeModel.add(colorsNodeModel);
        colorsNodeModel.add(colorNodeModel);
        nodeModel.add(featuresNodeModel);

        ItemTreeHandler itemHandler = new ItemTreeHandler(nodeModel, viewBean, itemBean, ItemTreeHandlingStatus.ToSave);
        String actualXml = itemHandler.serializeItem();
        String expectedXml = "<Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>1</Id><Name>originalValue</Name><Features><Colors><Color>White</Color></Colors></Features><Availability>false</Availability><Price>150</Price><OnlineStore>@@http://</OnlineStore><Picture/></Product>";
        assertEquals(expectedXml, actualXml);

        for (ModelData eachNodeModel : nodeModel.getChildren()) {
            ItemNodeModel myNodeModel = (ItemNodeModel) eachNodeModel;
            if (myNodeModel.getTypePath().equals("Product/Name")) {
                myNodeModel.setObjectValue("newValue");
            }
        }

        itemHandler = new ItemTreeHandler(nodeModel, viewBean, itemBean, ItemTreeHandlingStatus.ToSave);
        actualXml = itemHandler.serializeItem();
        expectedXml = "<Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>1</Id><Name>newValue</Name><Features><Colors><Color>White</Color></Colors></Features><Availability>false</Availability><Price>150</Price><OnlineStore>@@http://</OnlineStore><Picture/></Product>";
        assertEquals(expectedXml, actualXml);

        itemBean = new ItemBean();
        itemBean.set("Product/Name", "Talend");
        itemBean.setOriginalLookupFieldDisplayValueMap(new HashMap<String, List<String>>());
        itemBean.setOriginalLookupFieldValueMap(new HashMap<String, List<String>>());
        itemBean.getOriginalLookupFieldDisplayValueMap().put("Product/Features/Colors/Color", new ArrayList<String>());
        itemBean.getOriginalLookupFieldValueMap().put("Product/Features/Colors/Color", new ArrayList<String>());
        itemBean.getOriginalLookupFieldDisplayValueMap().get("Product/Features/Colors/Color").add("Blue");
        itemBean.getOriginalLookupFieldValueMap().get("Product/Features/Colors/Color").add("Yellow");
        itemBean.getOriginalLookupFieldDisplayValueMap().get("Product/Features/Colors/Color").add("Blue");
        itemBean.getOriginalLookupFieldValueMap().get("Product/Features/Colors/Color").add("Yellow");
        viewBean = new ViewBean();
        entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelProduct());
        viewBean.setBindingEntityModel(entity);

        nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordProduct1(), entity);

        assertNotNull(nodeModel);
        assertTrue(nodeModel.getChildren().size() > 0);

        for (ModelData eachNodeModel : nodeModel.getChildren()) {
            ItemNodeModel myNodeModel = (ItemNodeModel) eachNodeModel;
            if (myNodeModel.getTypePath().equals("Product/Price")) {
                myNodeModel.setObjectValue("150");// update
            } else if (myNodeModel.getTypePath().equals("Product/Picture")) {
                myNodeModel.setObjectValue(null);// delete
            } else if (myNodeModel.getTypePath().equals("Product/Name")) {
                myNodeModel.setObjectValue("Talend");
            }
        }

        featuresNodeModel = new ItemNodeModel("Features");
        featuresNodeModel.setTypePath("Product/Features");
        colorsNodeModel = new ItemNodeModel("Colors");
        colorsNodeModel.setTypePath("Product/Features/Colors");
        colorNodeModel = new ItemNodeModel("Color");
        colorNodeModel.setTypePath("Product/Features/Colors/Color");
        colorNodeModel.setObjectValue("Blue");
        ItemNodeModel colorNodeModel2 = new ItemNodeModel("Color");
        colorNodeModel2.setTypePath("Product/Features/Colors/Color");
        colorNodeModel2.setObjectValue("Blue");
        featuresNodeModel.add(colorsNodeModel);
        colorsNodeModel.add(colorNodeModel);
        colorsNodeModel.add(colorNodeModel2);

        nodeModel.add(featuresNodeModel);
        itemHandler = new ItemTreeHandler(nodeModel, viewBean, itemBean, ItemTreeHandlingStatus.ToSave);
        actualXml = itemHandler.serializeItem();
        expectedXml = "<Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>1</Id><Name>Talend</Name><Features><Colors><Color>Yellow</Color><Color>Yellow</Color></Colors></Features><Availability>false</Availability><Price>150</Price><OnlineStore>@@http://</OnlineStore><Picture/></Product>";
        assertEquals(expectedXml, actualXml);

        itemBean = new ItemBean();
        itemBean.set("Product/Name", "Talend");
        itemBean.setOriginalLookupFieldDisplayValueMap(new HashMap<String, List<String>>());
        itemBean.setOriginalLookupFieldValueMap(new HashMap<String, List<String>>());
        itemBean.getOriginalLookupFieldDisplayValueMap().put("Product/Features/Colors/Color", new ArrayList<String>());
        itemBean.getOriginalLookupFieldValueMap().put("Product/Features/Colors/Color", new ArrayList<String>());
        itemBean.getOriginalLookupFieldDisplayValueMap().get("Product/Features/Colors/Color").add("Blue");
        itemBean.getOriginalLookupFieldValueMap().get("Product/Features/Colors/Color").add("Yellow");
        itemBean.getOriginalLookupFieldDisplayValueMap().get("Product/Features/Colors/Color").add("Blue");
        itemBean.getOriginalLookupFieldValueMap().get("Product/Features/Colors/Color").add("Yellow");
        colorNodeModel.setObjectValue("Red");
        colorNodeModel2.setObjectValue("Red");
        itemHandler = new ItemTreeHandler(nodeModel, viewBean, itemBean, ItemTreeHandlingStatus.ToSave);
        actualXml = itemHandler.serializeItem();
        expectedXml = "<Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>1</Id><Name>Talend</Name><Features><Colors><Color>Red</Color><Color>Red</Color></Colors></Features><Availability>false</Availability><Price>150</Price><OnlineStore>@@http://</OnlineStore><Picture/></Product>";
        assertEquals(expectedXml, actualXml);

    }

    public void testSerializeItemWhenSaveForHiddenElements() {

        // element "Description" is hidden
        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelProduct());
        viewBean.setBindingEntityModel(entity);

        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordProduct3(), entity);

        ItemTreeHandler itemHandler = new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.BeforeLoad);
        String actualXml = itemHandler.serializeItem();
        assertTrue(!actualXml.contains("<Description>"));

        // add "Description"
        ItemNodeModel descNodeModel = new ItemNodeModel("Description");
        descNodeModel.setTypePath("Product/Description");
        descNodeModel.setObjectValue("This is a Product");
        nodeModel.add(descNodeModel);

        itemHandler = new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave);
        actualXml = itemHandler.serializeItem();
        assertTrue(!actualXml.contains("<Description>"));
        assertTrue(actualXml.contains("<Size>Large</Size>"));

        itemHandler.setSimpleTypeOnly(false);
        actualXml = itemHandler.serializeItem();
        assertTrue(!actualXml.contains("<Size>Large</Size>"));

    }

    public void testSerializeItemWhenSaveForEmptyElements() {

        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelProduct());
        viewBean.setBindingEntityModel(entity);

        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordProduct2(), entity);

        ItemTreeHandler itemHandler = new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.BeforeLoad);
        String expectedXml = "<Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>231035935</Id><Name>Talend Golf Shirt</Name><Description>Golf-style, collared t-shirt</Description><Features><Sizes><Size/><Size>Medium</Size><Size/></Sizes><Colors><Color/></Colors></Features><Availability>true</Availability><Price>16.99</Price><Family>[1]</Family><OnlineStore>https://unknownhost/test</OnlineStore><Picture>/imageserver/upload/TalendShop/golf_shirt.jpg</Picture></Product>";
        String actualXml = itemHandler.serializeItem();
        assertEquals(expectedXml, actualXml);

        for (ModelData eachNodeModel : nodeModel.getChildren()) {
            ItemNodeModel myNodeModel = (ItemNodeModel) eachNodeModel;
            if (myNodeModel.getTypePath().equals("Product/OnlineStore")) {
                myNodeModel.setObjectValue(null);// reset OnlineStore value
            }
        }

        itemHandler = new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave);
        expectedXml = "<Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>231035935</Id><Name>Talend Golf Shirt</Name><Features><Sizes><Size>Medium</Size></Sizes><Colors><Color/></Colors></Features><Availability>true</Availability><Price>16.99</Price><Family>[1]</Family><OnlineStore/><Picture>/imageserver/upload/TalendShop/golf_shirt.jpg</Picture></Product>";
        actualXml = itemHandler.serializeItem();
        assertEquals(expectedXml, actualXml);

    }

    public void testSerializeItemWhenSaveForPKs() {

        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelCustomer());
        viewBean.setBindingEntityModel(entity);

        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordCustomer1(), entity);
        ItemTreeHandler itemHandler = new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave);
        String expectedXml = "<Customer xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><firstname>Mike</firstname><lastname>Burn</lastname><description>VIP Customer</description></Customer>";
        String actualXml = itemHandler.serializeItem();
        assertEquals(expectedXml, actualXml);

    }

    public void testSerializeItemWhenSaveForReadonlyElements() {

        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelContract());
        viewBean.setBindingEntityModel(entity);

        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordContract1(), entity);
        ItemTreeHandler itemHandler = new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave);

        String expectedXml = "<Contract xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>001</id><description>VIP Customer</description><partyNotes><firstPartyNote>First party agree</firstPartyNote></partyNotes></Contract>";
        String actualXml = itemHandler.serializeItem();
        assertEquals(expectedXml, actualXml);

        itemHandler.setSimpleTypeOnly(false);
        expectedXml = "<Contract xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>001</id><description>VIP Customer</description></Contract>";
        actualXml = itemHandler.serializeItem();
        assertEquals(expectedXml, actualXml);

    }

    public void testSerializeItemWhenSaveUseBasicTypes() {

        // Test Model A
        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelA());
        viewBean.setBindingEntityModel(entity);
        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordA(), entity);
        String xml = (new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave)).serializeItem();
        assertEquals(
                "<Test xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subelement>111</subelement><name>zhang</name><age>25</age><memo>hello, morning</memo></Test>",
                xml);

        // Test Model B
        entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelB());
        viewBean.setBindingEntityModel(entity);
        nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordB(), entity);
        xml = (new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave)).serializeItem();
        assertEquals(
                "<Test xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subelement>111</subelement><name/><age>25</age><memo/></Test>",
                xml);

        // Test Model C
        entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelC());
        viewBean.setBindingEntityModel(entity);
        nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordC(), entity);
        xml = (new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave)).serializeItem();
        assertEquals("<Test xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subelement>111</subelement></Test>", xml);

        // Test Model D
        entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelD());
        viewBean.setBindingEntityModel(entity);
        nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordD(), entity);
        xml = (new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave)).serializeItem();
        assertEquals(
                "<Test xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subelement>111</subelement><name>zhang</name></Test>",
                xml);

        // Test Model E
        entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelE());
        viewBean.setBindingEntityModel(entity);
        nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordE(), entity);
        xml = (new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave)).serializeItem();
        assertEquals(
                "<Test xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subelement>111</subelement><name>zhang</name><age>25</age><memo/></Test>",
                xml);

        // Test Model F
        entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelF());
        viewBean.setBindingEntityModel(entity);
        nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordF(), entity);
        xml = (new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave)).serializeItem();
        assertEquals(
                "<Test xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><subelement>111</subelement><name>zhang</name><age>25</age><memo/><memo>hello</memo><memo/><memo>bye</memo></Test>",
                xml);

    }

    /**
     * Test Model Structure:<br>
     * Product<br>
     * |_Id<br>
     * |_Name<br>
     * |_Family(0...1)<br>
     * |_Stores(ComplexType)<br>
     * |_Store(0...Many)
     */
    public void testSerializeItemWhenSaveForRepeatingElementWithoutSiblings() {

        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelProductWithStore());
        viewBean.setBindingEntityModel(entity);

        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordProductWithStore(), entity);
        ItemTreeHandler itemHandler = new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave);
        String expectedXml = "<Product xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>2000</Id><Name>Talend Golf Shirt</Name><Family>[1]</Family><Stores><Store>[1]</Store><Store>[2]</Store><Store>[3]</Store></Stores></Product>";
        String actualXml = itemHandler.serializeItem();
        assertEquals(expectedXml, actualXml);

    }

    public void testSerializeItemWhenSaveForPolymorphismForeignKey() {
        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelProductWithSupplier());
        viewBean.setBindingEntityModel(entity);

        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordProductWithSupplier(), entity);
        ItemTreeHandler itemHandler = new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave);
        String expectedXml = "<Product xmlns:tmdm=\"http://www.talend.com/mdm\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>1</Id><Name>Talend MDM</Name><Family>[1]</Family><Supplier tmdm:type=\"Company\">[1]</Supplier></Product>";
        String actualXml = itemHandler.serializeItem();
        assertEquals(expectedXml, actualXml);
    }

    public void testSerializeItemWhenSaveForModelContainsDefaultValueExpression() {
        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelProductFamily());
        viewBean.setBindingEntityModel(entity);

        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordProductFamily(), entity);
        ItemTreeHandler itemHandler = new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave);
        String expectedXml = "<ProductFamily xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id/><Name>name</Name></ProductFamily>";
        String actualXml = itemHandler.serializeItem();
        assertEquals(expectedXml, actualXml);
    }

    public void testSerializeItemWithBulkUpdate() {
        List<Map<String, Object>> keyMapList = new ArrayList<Map<String, Object>>();
        Map<String, Object> keyMap1 = new HashMap<String, Object>();
        keyMap1.put("Product/Id", 1);
        keyMapList.add(keyMap1);
        Map<String, Object> keyMap2 = new HashMap<String, Object>();
        keyMap2.put("Product/Id", 2);
        keyMapList.add(keyMap2);

        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelProduct());
        viewBean.setBindingEntityModel(entity);
        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordProduct1(), entity);
        assertNotNull(nodeModel);
        assertTrue(nodeModel.getChildren().size() > 0);
        nodeModel.setMassUpdate(true);
        nodeModel.setEdited(true);
        for (ModelData eachNodeModel : nodeModel.getChildren()) {
            ItemNodeModel myNodeModel = (ItemNodeModel) eachNodeModel;
            myNodeModel.setMassUpdate(true);
            myNodeModel.setEdited(false);
            if (myNodeModel.getTypePath().equals("Product/Id")) {
                myNodeModel.setEdited(true);
            } else if (myNodeModel.getTypePath().equals("Product/Price")) {
                myNodeModel.setObjectValue("999");
                myNodeModel.setEdited(true);
            }
        }
        ItemTreeHandler itemHandler = new ItemTreeHandler(nodeModel, viewBean, keyMapList, ItemTreeHandlingStatus.BulkUpdate);
        String actualXml = itemHandler.serializeItem();
        String expectedXml = "<records xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Product><Id>1</Id><Price>999</Price></Product><Product><Id>2</Id><Price>999</Price></Product></records>";
        assertEquals(expectedXml, actualXml);

        nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordProduct1(), entity);
        assertNotNull(nodeModel);
        assertTrue(nodeModel.getChildren().size() > 0);
        nodeModel.setMassUpdate(true);
        nodeModel.setEdited(true);
        for (ModelData eachNodeModel : nodeModel.getChildren()) {
            ItemNodeModel myNodeModel = (ItemNodeModel) eachNodeModel;
            myNodeModel.setMassUpdate(true);
            myNodeModel.setEdited(false);
            if (myNodeModel.getTypePath().equals("Product/Id")) {
                myNodeModel.setEdited(true);
            }
        }
        ItemNodeModel featuresNodeModel = new ItemNodeModel("Features");
        featuresNodeModel.setTypePath("Product/Features");
        featuresNodeModel.setMassUpdate(true);
        featuresNodeModel.setEdited(true);
        ItemNodeModel sizesNodeModel = new ItemNodeModel("Sizes");
        sizesNodeModel.setTypePath("Product/Features/Sizes");
        sizesNodeModel.setMassUpdate(true);
        sizesNodeModel.setEdited(true);
        ItemNodeModel sizeNodeModel = new ItemNodeModel("Size");
        sizeNodeModel.setTypePath("Product/Features/Sizes/Size");
        sizeNodeModel.setObjectValue("Small");
        sizeNodeModel.setMassUpdate(true);
        sizeNodeModel.setEdited(true);
        ItemNodeModel sizeNodeModel2 = new ItemNodeModel("Size");
        sizeNodeModel2.setTypePath("Product/Features/Sizes/Size");
        sizeNodeModel2.setObjectValue("Middle");
        sizeNodeModel2.setMassUpdate(true);
        sizeNodeModel2.setEdited(true);
        featuresNodeModel.add(sizesNodeModel);
        sizesNodeModel.add(sizeNodeModel);
        sizesNodeModel.add(sizeNodeModel2);
        ItemNodeModel colorsNodeModel = new ItemNodeModel("Colors");
        colorsNodeModel.setTypePath("Product/Features/Colors");
        colorsNodeModel.setMassUpdate(true);
        colorsNodeModel.setEdited(true);
        ItemNodeModel colorNodeModel = new ItemNodeModel("Color");
        colorNodeModel.setTypePath("Product/Features/Colors/Color");
        colorNodeModel.setObjectValue("Red");
        colorNodeModel.setMassUpdate(true);
        colorNodeModel.setEdited(true);
        ItemNodeModel colorNodeModel2 = new ItemNodeModel("Color");
        colorNodeModel2.setTypePath("Product/Features/Colors/Color");
        colorNodeModel2.setObjectValue("Blue");
        colorNodeModel2.setMassUpdate(true);
        colorNodeModel2.setEdited(true);
        colorsNodeModel.add(colorNodeModel);
        colorsNodeModel.add(colorNodeModel2);
        featuresNodeModel.add(colorsNodeModel);
        nodeModel.add(featuresNodeModel);
        itemHandler = new ItemTreeHandler(nodeModel, viewBean, keyMapList, ItemTreeHandlingStatus.BulkUpdate);
        actualXml = itemHandler.serializeItem();
        expectedXml = "<records xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Product><Id>1</Id><Features><Sizes><Size>Small</Size><Size>Middle</Size></Sizes><Colors><Color>Red</Color><Color>Blue</Color></Colors></Features></Product><Product><Id>2</Id><Features><Sizes><Size>Small</Size><Size>Middle</Size></Sizes><Colors><Color>Red</Color><Color>Blue</Color></Colors></Features></Product></records>";
        assertEquals(expectedXml, actualXml);

        Map<String, Object> keyMap3 = new HashMap<String, Object>();
        keyMap3.put("Product/Id", 3);
        keyMapList.add(keyMap3);
        Map<String, Object> keyMap4 = new HashMap<String, Object>();
        keyMap4.put("Product/Id", 4);
        keyMapList.add(keyMap4);
        nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordProduct1(), entity);
        assertNotNull(nodeModel);
        assertTrue(nodeModel.getChildren().size() > 0);
        nodeModel.setMassUpdate(true);
        nodeModel.setEdited(true);
        for (ModelData eachNodeModel : nodeModel.getChildren()) {
            ItemNodeModel myNodeModel = (ItemNodeModel) eachNodeModel;
            myNodeModel.setMassUpdate(true);
            myNodeModel.setEdited(false);
            if (myNodeModel.getTypePath().equals("Product/Id")) {
                myNodeModel.setEdited(true);
            } else if (myNodeModel.getTypePath().equals("Product/Name")) {
                myNodeModel.setObjectValue("newName");
                myNodeModel.setEdited(true);
            } else if (myNodeModel.getTypePath().equals("Product/Description")) {
                myNodeModel.setObjectValue("newDescription");
                myNodeModel.setEdited(true);
            } else if (myNodeModel.getTypePath().equals("Product/Price")) {
                myNodeModel.setObjectValue("999");
                myNodeModel.setEdited(true);
            } else if (myNodeModel.getTypePath().equals("Product/OnlineStore")) {
                myNodeModel.setObjectValue("Talend@@Wangfujing");
                myNodeModel.setEdited(true);
            }
        }
        nodeModel.add(featuresNodeModel);
        itemHandler = new ItemTreeHandler(nodeModel, viewBean, keyMapList, ItemTreeHandlingStatus.BulkUpdate);
        actualXml = itemHandler.serializeItem();
        expectedXml = "<records xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Product><Id>1</Id><Name>newName</Name><Features><Sizes><Size>Small</Size><Size>Middle</Size></Sizes><Colors><Color>Red</Color><Color>Blue</Color></Colors></Features><Price>999</Price><OnlineStore>Talend@@Wangfujing</OnlineStore></Product><Product><Id>2</Id><Name>newName</Name><Features><Sizes><Size>Small</Size><Size>Middle</Size></Sizes><Colors><Color>Red</Color><Color>Blue</Color></Colors></Features><Price>999</Price><OnlineStore>Talend@@Wangfujing</OnlineStore></Product><Product><Id>3</Id><Name>newName</Name><Features><Sizes><Size>Small</Size><Size>Middle</Size></Sizes><Colors><Color>Red</Color><Color>Blue</Color></Colors></Features><Price>999</Price><OnlineStore>Talend@@Wangfujing</OnlineStore></Product><Product><Id>4</Id><Name>newName</Name><Features><Sizes><Size>Small</Size><Size>Middle</Size></Sizes><Colors><Color>Red</Color><Color>Blue</Color></Colors></Features><Price>999</Price><OnlineStore>Talend@@Wangfujing</OnlineStore></Product></records>";
        assertEquals(expectedXml, actualXml);

        ForeignKeyBean foreignKeyBean = new ForeignKeyBean();
        foreignKeyBean.setId("1");
        keyMapList = new ArrayList<Map<String, Object>>();
        keyMap1 = new HashMap<String, Object>();
        keyMap1.put("Product/Id", 1);
        keyMapList.add(keyMap1);
        keyMap2 = new HashMap<String, Object>();
        keyMap2.put("Product/Id", 2);
        keyMapList.add(keyMap2);
        nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordProduct1(), entity);
        assertNotNull(nodeModel);
        assertTrue(nodeModel.getChildren().size() > 0);
        nodeModel.setMassUpdate(true);
        nodeModel.setEdited(true);
        for (ModelData eachNodeModel : nodeModel.getChildren()) {
            ItemNodeModel myNodeModel = (ItemNodeModel) eachNodeModel;
            myNodeModel.setMassUpdate(true);
            myNodeModel.setEdited(false);
            if (myNodeModel.getTypePath().equals("Product/Id")) {
                myNodeModel.setEdited(true);
            } else if (myNodeModel.getTypePath().equals("Product/Name")) {
                myNodeModel.setObjectValue("newName");
                myNodeModel.setEdited(true);
            }
        }
        ItemNodeModel familyNodeModel = new ItemNodeModel("Family");
        familyNodeModel.setTypePath("Product/Family");
        familyNodeModel.setObjectValue(foreignKeyBean);
        familyNodeModel.setMassUpdate(true);
        familyNodeModel.setEdited(true);
        nodeModel.add(familyNodeModel);
        itemHandler = new ItemTreeHandler(nodeModel, viewBean, keyMapList, ItemTreeHandlingStatus.BulkUpdate);
        actualXml = itemHandler.serializeItem();
        expectedXml = "<records xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Product><Id>1</Id><Name>newName</Name><Family>1</Family></Product><Product><Id>2</Id><Name>newName</Name><Family>1</Family></Product></records>";
        assertEquals(expectedXml, actualXml);

        familyNodeModel.setEdited(false);
        itemHandler = new ItemTreeHandler(nodeModel, viewBean, keyMapList, ItemTreeHandlingStatus.BulkUpdate);
        actualXml = itemHandler.serializeItem();
        expectedXml = "<records xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Product><Id>1</Id><Name>newName</Name></Product><Product><Id>2</Id><Name>newName</Name></Product></records>";
        assertEquals(expectedXml, actualXml);
    }

    public void testSerializeCompositeKeyItemWithBulkUpdate() {
        List<Map<String, Object>> keyMapList = new ArrayList<Map<String, Object>>();
        Map<String, Object> keyMap1 = new HashMap<String, Object>();
        keyMap1.put("Customer/firstname", "Chen");
        keyMap1.put("Customer/lastname", "Jack");
        keyMapList.add(keyMap1);
        Map<String, Object> keyMap2 = new HashMap<String, Object>();
        keyMap2.put("Customer/firstname", "Lisa");
        keyMap2.put("Customer/lastname", "Ann");
        keyMapList.add(keyMap2);

        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelCustomer());
        viewBean.setBindingEntityModel(entity);
        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordCustomer1(), entity);
        assertNotNull(nodeModel);
        assertTrue(nodeModel.getChildren().size() > 0);
        nodeModel.setMassUpdate(true);
        nodeModel.setEdited(true);
        for (ModelData eachNodeModel : nodeModel.getChildren()) {
            ItemNodeModel myNodeModel = (ItemNodeModel) eachNodeModel;
            myNodeModel.setMassUpdate(true);
            myNodeModel.setEdited(false);
            if (myNodeModel.getTypePath().equals("Customer/firstname")) {
                myNodeModel.setEdited(true);
            } else if (myNodeModel.getTypePath().equals("Customer/lastname")) {
                myNodeModel.setEdited(true);
            } else if (myNodeModel.getTypePath().equals("Customer/description")) {
                myNodeModel.setObjectValue("VIP");
                myNodeModel.setEdited(true);
            }

        }
        ItemTreeHandler itemHandler = new ItemTreeHandler(nodeModel, viewBean, keyMapList, ItemTreeHandlingStatus.BulkUpdate);
        String actualXml = itemHandler.serializeItem();
        String expectedXml = "<records xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Customer><firstname>Chen</firstname><lastname>Jack</lastname><description>VIP</description></Customer><Customer><firstname>Lisa</firstname><lastname>Ann</lastname><description>VIP</description></Customer></records>";
        assertEquals(expectedXml, actualXml);
    }
}
