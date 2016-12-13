/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.treedetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.client.model.ItemResult;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.FacetModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.creator.DataTypeCreator;
import org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ColumnTreeLayoutModel;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyDrawer;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyModel;
import org.talend.mdm.webapp.browserecords.client.model.FormatModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.model.QueryModel;
import org.talend.mdm.webapp.browserecords.client.model.Restriction;
import org.talend.mdm.webapp.browserecords.client.model.UpdateItemModel;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel.ForeignKeyHandler;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatTextField;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail.DynamicTreeItem;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail.GhostTreeItem;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.FKIntegrityResult;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;
import org.talend.mdm.webapp.browserecords.shared.VisibleRuleResult;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;

@SuppressWarnings("nls")
public class TreeDetailGWTTest extends GWTTestCase {

    private EntityModel entityModel;

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
        Registry.register(BrowseRecords.BROWSERECORDS_SERVICE, new MockBrowseRecordsServiceAsync());
    }

    public void testValidateNode() {
        Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();
        EntityModel entity = new EntityModel();

        ItemNodeModel root = new ItemNodeModel("Product");
        ItemNodeModel id = new ItemNodeModel("Id");
        id.setKey(true);
        id.setMandatory(true);
        id.setValid(true);
        root.add(id);
        fieldMap.put(id.getId().toString(), new FormatTextField());

        ItemNodeModel feature = new ItemNodeModel("Feature");
        feature.setMandatory(false);
        root.add(feature);

        ItemNodeModel size = new ItemNodeModel("Size");
        size.setMandatory(true);
        size.setValid(true);
        Field<?> field = new FormatTextField();
        field.render(DOM.createElement("Size"));
        fieldMap.put(size.getId().toString(), field);
        feature.add(size);

        ItemNodeModel color = new ItemNodeModel("Color");
        color.setMandatory(true);
        color.setValid(false);
        field = new FormatTextField();
        field.render(DOM.createElement("Color"));
        color.setTypePath("Product/Feature/Color");
        fieldMap.put(color.getId().toString(), field);
        entity.getMetaDataTypes().put(color.getTypePath(), new SimpleTypeModel());
        feature.add(color);

        TreeDetail detail = new TreeDetail(null);
        ViewBean viewBean = new ViewBean();
        viewBean.setBindingEntityModel(entity);
        detail.setViewBean(viewBean);

    }

    public void testIsFKDisplayedIntoTab() {
        Map<String, TypeModel> metaDataTypes = new HashMap<String, TypeModel>();

        ItemNodeModel product = new ItemNodeModel("Produce");
        ComplexTypeModel productType = new ComplexTypeModel();
        productType.setTypePath("Product");
        metaDataTypes.put(productType.getTypePath(), productType);
        product.setTypePath(productType.getTypePath());

        ItemNodeModel picture = new ItemNodeModel("Picture");
        SimpleTypeModel pictureType = new SimpleTypeModel();
        pictureType.setTypePath("Product/Picture");
        picture.setTypePath(pictureType.getTypePath());
        metaDataTypes.put(pictureType.getTypePath(), pictureType);
        product.add(picture);

        ItemNodeModel name = new ItemNodeModel("Name");
        SimpleTypeModel nameType = new SimpleTypeModel();
        nameType.setTypePath("Product/Name");
        name.setTypePath(nameType.getTypePath());
        metaDataTypes.put(nameType.getTypePath(), nameType);
        product.add(name);

        ItemNodeModel description = new ItemNodeModel("Description");
        SimpleTypeModel descriptionType = new SimpleTypeModel();
        descriptionType.setTypePath("Product/Description");
        description.setTypePath(descriptionType.getTypePath());
        metaDataTypes.put(descriptionType.getTypePath(), descriptionType);
        product.add(description);

        ItemNodeModel family = new ItemNodeModel("Family");
        SimpleTypeModel familyType = new SimpleTypeModel();
        familyType.setTypePath("Product/Family");
        familyType.setForeignkey("ProductFamily/Id");
        familyType.setNotSeparateFk(false);
        family.setTypePath(familyType.getTypePath());
        metaDataTypes.put(familyType.getTypePath(), familyType);
        product.add(family);

        ItemNodeModel stores = new ItemNodeModel("Stores");
        ComplexTypeModel storesType = new ComplexTypeModel();
        storesType.setTypePath("Product/Stores");
        stores.setTypePath(storesType.getTypePath());
        metaDataTypes.put(storesType.getTypePath(), storesType);
        product.add(stores);

        SimpleTypeModel storeType = new SimpleTypeModel();
        storeType.setTypePath("Product/Store");
        storeType.setForeignkey("Store/Id");
        storeType.setNotSeparateFk(false);

        ItemNodeModel store1 = new ItemNodeModel("Store");
        store1.setTypePath(storeType.getTypePath());
        stores.add(store1);
        ItemNodeModel store2 = new ItemNodeModel("Store");
        store2.setTypePath(storeType.getTypePath());
        stores.add(store2);
        ItemNodeModel store3 = new ItemNodeModel("Store");
        store3.setTypePath(storeType.getTypePath());
        stores.add(store3);
        metaDataTypes.put(storeType.getTypePath(), storeType);
        storesType.addSubType(storeType);

        ItemNodeModel otherNode = new ItemNodeModel("OtherNode");
        ComplexTypeModel otherNodeType = new ComplexTypeModel();
        otherNodeType.setTypePath("Product/OtherNodeType");
        otherNode.setTypePath(otherNodeType.getTypePath());
        metaDataTypes.put(otherNodeType.getTypePath(), otherNodeType);
        product.add(otherNode);

        ItemNodeModel oNode1 = new ItemNodeModel("O1");
        SimpleTypeModel o1Type = new SimpleTypeModel();
        o1Type.setTypePath("Product/OtherNodeType/O1");
        o1Type.setForeignkey("Other/Id");
        o1Type.setNotSeparateFk(false);
        metaDataTypes.put(o1Type.getTypePath(), o1Type);
        otherNodeType.addSubType(o1Type);
        oNode1.setTypePath(o1Type.getTypePath());
        otherNode.add(oNode1);

        ItemNodeModel oNode2 = new ItemNodeModel("O2");
        SimpleTypeModel o2Type = new SimpleTypeModel();
        o2Type.setTypePath("Product/OtherNodeType/O2");
        metaDataTypes.put(o2Type.getTypePath(), o2Type);
        otherNodeType.addSubType(o2Type);
        oNode2.setTypePath(o2Type.getTypePath());
        otherNode.add(oNode2);

        ItemNodeModel oNode3 = new ItemNodeModel("O3");
        SimpleTypeModel o3Type = new SimpleTypeModel();
        o3Type.setTypePath("Product/OtherNodeType/O3");
        metaDataTypes.put(o3Type.getTypePath(), o3Type);
        otherNodeType.addSubType(o3Type);
        oNode3.setTypePath(o3Type.getTypePath());
        otherNode.add(oNode3);

        assertEquals(TreeDetail.isFKDisplayedIntoTab(product, productType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(picture, pictureType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(name, nameType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(description, descriptionType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(family, familyType, metaDataTypes), true);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(stores, storesType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(store1, storeType, metaDataTypes), true);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(store2, storeType, metaDataTypes), true);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(store3, storeType, metaDataTypes), true);
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(otherNode, otherNodeType, metaDataTypes));
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(oNode1, o1Type, metaDataTypes));
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(oNode2, o2Type, metaDataTypes));
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(oNode3, o3Type, metaDataTypes));

        familyType.setNotSeparateFk(true);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(product, productType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(picture, pictureType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(name, nameType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(description, descriptionType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(family, familyType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(stores, storesType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(store1, storeType, metaDataTypes), true);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(store2, storeType, metaDataTypes), true);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(store3, storeType, metaDataTypes), true);
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(otherNode, otherNodeType, metaDataTypes));
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(oNode1, o1Type, metaDataTypes));
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(oNode2, o2Type, metaDataTypes));
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(oNode3, o3Type, metaDataTypes));

        storeType.setNotSeparateFk(true);

        assertEquals(TreeDetail.isFKDisplayedIntoTab(product, productType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(picture, pictureType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(name, nameType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(description, descriptionType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(family, familyType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(stores, storesType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(store1, storeType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(store2, storeType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(store3, storeType, metaDataTypes), false);
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(otherNode, otherNodeType, metaDataTypes));
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(oNode1, o1Type, metaDataTypes));
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(oNode2, o2Type, metaDataTypes));
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(oNode3, o3Type, metaDataTypes));

        o1Type.setNotSeparateFk(true);

        assertEquals(TreeDetail.isFKDisplayedIntoTab(product, productType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(picture, pictureType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(name, nameType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(description, descriptionType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(family, familyType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(stores, storesType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(store1, storeType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(store2, storeType, metaDataTypes), false);
        assertEquals(TreeDetail.isFKDisplayedIntoTab(store3, storeType, metaDataTypes), false);
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(otherNode, otherNodeType, metaDataTypes));
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(oNode1, o1Type, metaDataTypes));
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(oNode2, o2Type, metaDataTypes));
        assertEquals(false, TreeDetail.isFKDisplayedIntoTab(oNode3, o3Type, metaDataTypes));

    }

    public void testProgressBar() {
        BrowseRecordsMessages msg = MessagesFactory.getMessages();
        // 1. rendering item
        MessageBox progressBar = MessageBox.wait(msg.rendering_title(), msg.render_message(), msg.rendering_progress());
        assertEquals(msg.rendering_title(), progressBar.getTitle());
        assertEquals(msg.render_message(), progressBar.getMessage());
        assertEquals(msg.rendering_progress(), progressBar.getProgressText());
        assertEquals(true, progressBar.isVisible());
        progressBar.close();
        assertEquals(false, progressBar.isVisible());
        // 2. deleting item
        progressBar = MessageBox.wait(msg.delete_item_title(), null, msg.delete_item_progress());
        assertEquals(msg.delete_item_title(), progressBar.getTitle());
        assertEquals(null, progressBar.getMessage());
        assertEquals(msg.delete_item_progress(), progressBar.getProgressText());
        progressBar.close();
    }

    public void testForeignKeyLazyRender() {
        final ItemsDetailPanel itemsDetailPanel = ItemsDetailPanel.newInstance();
        // add PK tabPanel
        ItemPanel pkTabPanel = new ItemPanel(itemsDetailPanel);
        itemsDetailPanel.addTabItem("PKTab", pkTabPanel, ItemsDetailPanel.SINGLETON, "PK");
        // add FK tabPanel (FK lazy render)
        ItemPanel fkTabPanel = new ItemPanel(itemsDetailPanel);
        // add ForeignKeyTablePanel
        final ForeignKeyTablePanel fkPanel = new ForeignKeyTablePanel("FK Lazy render", false, false);
        fkTabPanel.add(fkPanel, new RowData(1, 1));
        itemsDetailPanel.addTabItem("FKTab", fkTabPanel, ItemsDetailPanel.MULTIPLE, "FK");
        // lazy render FK
        itemsDetailPanel.addFkHandler(fkTabPanel, new ForeignKeyHandler() {

            @Override
            public void onSelect() {
                // render FK
                EntityModel entityModel = new EntityModel();
                entityModel.setConceptName("ProductFamily");
                entityModel.setKeys(new String[] { "ProductFamily" });
                LinkedHashMap<String, TypeModel> metaDataTypes = new LinkedHashMap<String, TypeModel>();
                metaDataTypes.put("ProductFamily", new ComplexTypeModel("ProductFamily", DataTypeConstants.STRING));
                metaDataTypes.put("ProductFamily/Id", new SimpleTypeModel("Id", DataTypeConstants.STRING));
                metaDataTypes.put("ProductFamily/Name", new SimpleTypeModel("Name", DataTypeConstants.STRING));
                entityModel.setMetaDataTypes(metaDataTypes);
                ItemNodeModel parent = new ItemNodeModel("Product");
                List<ItemNodeModel> fkModels = new ArrayList<ItemNodeModel>();
                TypeModel fkTypeModel = new SimpleTypeModel("Family", DataTypeConstants.STRING);
                fkTypeModel.setForeignkey("ProductFamily/Id");
                List<String> foreignKeyInfo = new ArrayList<String>();
                foreignKeyInfo.add("ProductFamily/Name");
                fkTypeModel.setForeignKeyInfo(foreignKeyInfo);
                ItemNodeModel fkModel = new ItemNodeModel();
                ForeignKeyBean fkBean = new ForeignKeyBean();
                fkBean.setConceptName("ProductFamily");
                Map<String, String> foreignKeyInfos = new HashMap<String, String>();
                foreignKeyInfos.put("ProductFamily/Name", "TestFkLazyRender");
                fkBean.setForeignKeyInfo(foreignKeyInfos);
                fkBean.setId("1");
                fkModel.setObjectValue(fkBean);
                fkModels.add(fkModel);
                Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();
                fkPanel.initContent(entityModel, parent, fkModels, fkTypeModel, fieldMap, itemsDetailPanel, null);
                itemsDetailPanel.layout(true);
            }
        });
        // before render FK
        assertNotNull(fkPanel.getTopComponent());
        assertNotNull(fkPanel.getBottomComponent());
        assertEquals(0, fkPanel.getItemCount());
        assertEquals(2, itemsDetailPanel.getTabCount());
        // render FK
        itemsDetailPanel.selectTabAtIndex(1);
        // fkTable
        assertEquals(1, fkPanel.getItemCount());
        // fkTable is a grid
        assertTrue(fkPanel.getItem(0) instanceof Grid);
        Grid<?> grid = (Grid<?>) fkPanel.getItem(0);
        // fire attach event
        grid.fireEvent(Events.Attach);
        // store include one record
        assertEquals(1, grid.getStore().getCount());
        // record is ItemNodeModel object
        assertTrue(grid.getStore().getAt(0) instanceof ItemNodeModel);
        ItemNodeModel fkNodeModel = (ItemNodeModel) grid.getStore().getAt(0);
        // nodeModel's objectValue is ForeignKeyBean object
        assertTrue(fkNodeModel.getObjectValue() instanceof ForeignKeyBean);
        ForeignKeyBean fk = (ForeignKeyBean) fkNodeModel.getObjectValue();
        // validate foreignKey info
        assertEquals("1", fk.getId());
        assertEquals("ProductFamily", fk.getConceptName());
        assertEquals(1, fk.getForeignKeyInfo().size());
        assertTrue(fk.getForeignKeyInfo().containsKey("ProductFamily/Name"));
        assertTrue(fk.getForeignKeyInfo().containsValue("TestFkLazyRender"));
    }

    /**
     * Test Model Structure: <br>
     * Root<br>
     * |_id<br>
     * |_name<br>
     * |_cp(autoExpand=false)<br>
     * |_title<br>
     * |_content<br>
     */
    public void testNodeLazyLoading() {
        ViewBean viewBean = getViewBean();
        TreeDetail treeDetail = new TreeDetail(ItemsDetailPanel.newInstance());
        treeDetail.setViewBean(viewBean);
        ItemNodeModel rootNode = builderItemNode();
        DynamicTreeItem item = treeDetail.buildGWTTree(rootNode, null, false, ItemDetailToolBar.VIEW_OPERATION);
        // validate result
        assertNotNull(item);
        assertEquals(3, item.getChildCount());
        // cp node lazy loading
        DynamicTreeItem cpItem = (DynamicTreeItem) item.getChild(2);
        assertNotNull(cpItem);
        assertEquals(1, cpItem.getChildCount());
        assertTrue(cpItem.getChild(0) instanceof GhostTreeItem);
        // render cp node
        cpItem.setState(true);
        assertEquals(2, cpItem.getChildCount());
        assertTrue(((ItemNodeModel) cpItem.getChild(0).getUserObject()).getName().equals("title"));
        assertTrue(((ItemNodeModel) cpItem.getChild(1).getUserObject()).getName().equals("content"));
    }

    /**
     * Model Structure: <br>
     * Test<br>
     * |_id<br>
     * |_name<br>
     * |_oem(visible rule: fn:matches(../name ,"test"))<br>
     * |_oem_type(enumeration:a,b,c)<br>
     * |_a(visible rule: fn:starts-with(../oem_type,"a"))<br>
     * |_b(visible rule: fn:starts-with(../oem_type,"b"))<br>
     * |_c(visible rule: fn:starts-with(../oem_type,"c"))<br>
     */
    public void testRecrusiveSetItems() {
        // 1. Build Parameter(ItemNodeModel testNode)
        ItemNodeModel testNode = new ItemNodeModel("Test");
        testNode.setTypePath("Test");
        ItemNodeModel idNode = new ItemNodeModel("id");
        idNode.setTypePath("Test/id");
        idNode.setKey(true);
        testNode.add(idNode);
        ItemNodeModel nameNode = new ItemNodeModel("name");
        nameNode.setObjectValue("test");
        nameNode.setTypePath("Test/name");
        testNode.add(nameNode);
        ItemNodeModel oemNode = new ItemNodeModel("oem");
        oemNode.setHasVisiblueRule(true);
        oemNode.setTypePath("Test/oem");
        testNode.add(oemNode);
        ItemNodeModel oem_typeNode = new ItemNodeModel("oem_type");
        oem_typeNode.setTypePath("Test/oem/oem_type");
        oem_typeNode.setObjectValue("a");
        oemNode.add(oem_typeNode);
        ItemNodeModel oem_aNode = new ItemNodeModel("a");
        oem_aNode.setHasVisiblueRule(true);
        oem_aNode.setObjectValue("VisibleRule_TestSuccessfully");
        oem_aNode.setTypePath("Test/oem/a");
        oemNode.add(oem_aNode);
        ItemNodeModel oem_bNode = new ItemNodeModel("b");
        oem_bNode.setHasVisiblueRule(true);
        oem_bNode.setTypePath("Test/oem/b");
        oemNode.add(oem_bNode);
        ItemNodeModel oem_cNode = new ItemNodeModel("c");
        oem_cNode.setHasVisiblueRule(true);
        oem_cNode.setTypePath("Test/oem/c");
        oemNode.add(oem_cNode);
        ItemNodeModel documentNode = new ItemNodeModel("doucment");
        documentNode.setHasVisiblueRule(true);
        documentNode.setTypePath("Test/doucment");
        documentNode.setHide(true);
        testNode.add(documentNode);
        ItemNodeModel document_typeNode = new ItemNodeModel("doucment_type");
        document_typeNode.setTypePath("Test/doucment/doucment_type");
        document_typeNode.setObjectValue("type");
        documentNode.add(document_typeNode);
        ItemNodeModel familyNode = new ItemNodeModel("family");
        familyNode.setHasVisiblueRule(true);
        familyNode.setTypePath("Test/family");
        familyNode.setHide(true);
        testNode.add(familyNode);
        ItemNodeModel family_typeNode = new ItemNodeModel("family_type");
        family_typeNode.setTypePath("Test/family/family_type");
        family_typeNode.setObjectValue("type");
        familyNode.add(family_typeNode);
        ItemNodeModel docNode = new ItemNodeModel("doc");
        docNode.setHasVisiblueRule(true);
        docNode.setTypePath("Test/doc");
        testNode.add(docNode);
        ItemNodeModel doc_typeNode = new ItemNodeModel("dou_type");
        document_typeNode.setTypePath("Test/dou/dou_type");
        document_typeNode.setObjectValue("type");
        docNode.add(doc_typeNode);

        // 2. Build Parameter(List<VisibleRuleResult> visibleResults)
        List<VisibleRuleResult> visibleResults = new ArrayList<VisibleRuleResult>();
        VisibleRuleResult oem_VisibleRule = new VisibleRuleResult("Test/oem[1]", true);
        VisibleRuleResult a_VisibleRule = new VisibleRuleResult("Test/oem[1]/a[1]", true);
        VisibleRuleResult b_VisibleRule = new VisibleRuleResult("Test/oem[1]/b[1]", false);
        VisibleRuleResult c_VisibleRule = new VisibleRuleResult("Test/oem[1]/c[1]", false);
        VisibleRuleResult document_VisibleRule = new VisibleRuleResult("Test/doucment[1]", true);
        VisibleRuleResult family_VisibleRule = new VisibleRuleResult("Test/family[1]", false);
        VisibleRuleResult doc_VisibleRule = new VisibleRuleResult("Test/doc[1]", false);
        visibleResults.add(oem_VisibleRule);
        visibleResults.add(a_VisibleRule);
        visibleResults.add(b_VisibleRule);
        visibleResults.add(c_VisibleRule);
        visibleResults.add(document_VisibleRule);
        visibleResults.add(family_VisibleRule);
        visibleResults.add(doc_VisibleRule);
        // 3. Call TreeDetail.recrusiveSetItems(List<VisibleRuleResult>, ItemNodeModel)
        TreeDetail treeDetail = new TreeDetail(ItemsDetailPanel.newInstance());
        treeDetail.recrusiveSetItems(visibleResults, testNode);
        // 4. Validate result
        assertEquals(true, oemNode.isVisible());
        assertEquals(true, oem_aNode.isVisible());
        assertEquals(false, oem_bNode.isVisible());
        assertEquals(false, oem_cNode.isVisible());
        assertEquals(false, documentNode.isVisible());
        assertEquals(false, familyNode.isVisible());
        assertEquals(false, docNode.isVisible());
    }

    public void testRecrusiveSetItemsAndNode() {

        ItemNodeModel productNode = new ItemNodeModel("Product");
        productNode.setTypePath("Product");
        ItemNodeModel pictureNode = new ItemNodeModel("Picture");
        pictureNode.setTypePath("Product/Picture");
        pictureNode.setKey(true);
        productNode.add(pictureNode);
        ItemNodeModel idNode = new ItemNodeModel("Id");
        idNode.setTypePath("Product/Id");
        productNode.add(idNode);
        ItemNodeModel nameNode = new ItemNodeModel("Name");
        nameNode.setTypePath("Product/Name");
        productNode.add(nameNode);
        ItemNodeModel priceNode = new ItemNodeModel("Price");
        priceNode.setTypePath("Product/Price");
        priceNode.setHide(true);
        productNode.add(priceNode);
        ItemNodeModel storesNode = new ItemNodeModel("Stores");
        storesNode.setTypePath("Product/Stores");
        productNode.add(storesNode);
        ItemNodeModel storeNode = new ItemNodeModel("Store");
        storeNode.setTypePath("Product/Stores/Store");
        storesNode.add(storeNode);

        ViewBean viewBean = new ViewBean();
        entityModel = new EntityModel();
        LinkedHashMap<String, TypeModel> metaDataTypes = new LinkedHashMap<String, TypeModel>();
        ComplexTypeModel productType = new ComplexTypeModel("Product", DataTypeCreator.getDataType("Product", "anyType"));
        productType.setTypePath("Product");
        productType.addLabel("en", "product");
        metaDataTypes.put(productType.getTypePath(), productType);
        SimpleTypeModel pictureType = new SimpleTypeModel("Picture", DataTypeCreator.getDataType("string", "anyType"));
        pictureType.setTypePath("Product/Picture");
        pictureType.addLabel("en", "picture");
        pictureType.setAutoExpand(false);
        metaDataTypes.put(pictureType.getTypePath(), pictureType);
        SimpleTypeModel idType = new SimpleTypeModel("Id", DataTypeCreator.getDataType("string", "anyType"));
        idType.setTypePath("Product/Id");
        idType.addLabel("en", "pk");
        idType.setFacets(new ArrayList<FacetModel>());
        metaDataTypes.put(idType.getTypePath(), idType);
        SimpleTypeModel nameType = new SimpleTypeModel("Name", DataTypeCreator.getDataType("string", "anyType"));
        nameType.setTypePath("Product/Name");
        nameType.addLabel("en", "name");
        nameType.setFacets(new ArrayList<FacetModel>());
        metaDataTypes.put(nameType.getTypePath(), nameType);
        SimpleTypeModel priceType = new SimpleTypeModel("Price", DataTypeCreator.getDataType("string", "anyType"));
        priceType.setTypePath("Product/Price");
        priceType.addLabel("en", "price");
        priceType.setAutoExpand(true);
        metaDataTypes.put(priceType.getTypePath(), priceType);
        SimpleTypeModel storeType = new SimpleTypeModel("Store", DataTypeCreator.getDataType("string", "anyType"));
        storeType.setTypePath("Product/Stores/Store");
        storeType.addLabel("en", "store");
        storeType.setForeignkey("Store/Id");
        storeType.setNotSeparateFk(false);
        storeType.setAutoExpand(false);
        metaDataTypes.put(storeType.getTypePath(), storeType);
        ComplexTypeModel storesType = new ComplexTypeModel("Stores", DataTypeCreator.getDataType("string", "anyType"));
        storesType.setTypePath("Product/Stores");
        storesType.addLabel("en", "stores");
        storesType.setAutoExpand(false);
        metaDataTypes.put(storesType.getTypePath(), storesType);
        productType.addSubType(idType);
        productType.addSubType(nameType);
        productType.addSubType(pictureType);
        productType.addSubType(storesType);
        storesType.addSubType(storeType);

        entityModel.setMetaDataTypes(metaDataTypes);
        viewBean.setBindingEntityModel(entityModel);
        TreeDetail treeDetail = new TreeDetail(ItemsDetailPanel.newInstance());
        treeDetail.setViewBean(viewBean);

        DynamicTreeItem item = treeDetail.buildGWTTree(productNode, null, false, ItemDetailToolBar.VIEW_OPERATION);

        List<VisibleRuleResult> visibleResults = new ArrayList<VisibleRuleResult>();

        treeDetail.onExecuteVisibleRule(visibleResults);

        for (int i = 0; i < item.getChildCount(); i++) {
            if (item.getChild(i) instanceof DynamicTreeItem) {
                DynamicTreeItem sonItem = (DynamicTreeItem) item.getChild(i);
                if (sonItem.getItemNodeModel().getName().equals("Price")) {
                    VisibleRuleResult price_VisibleRule = new VisibleRuleResult("Product/Price[1]", false);
                    treeDetail.recrusiveSetItems(price_VisibleRule, sonItem);
                    assertFalse(sonItem.isVisible());
                }
                if (sonItem.getItemNodeModel().getName().equals("Name")) {
                    VisibleRuleResult name_VisibleRule = new VisibleRuleResult("Product/Name[1]", true);
                    treeDetail.recrusiveSetItems(name_VisibleRule, sonItem);
                    assertTrue(sonItem.isVisible());
                }
            }
        }

    }

    private ViewBean getViewBean() {
        ViewBean viewBean = new ViewBean();
        EntityModel bindingEntityModel = new EntityModel();
        LinkedHashMap<String, TypeModel> metaDataTypes = new LinkedHashMap<String, TypeModel>();

        ComplexTypeModel testType = new ComplexTypeModel("Root", DataTypeCreator.getDataType("Root", "anyType"));
        testType.setTypePath("Root");
        testType.addLabel("en", "root");
        metaDataTypes.put(testType.getTypePath(), testType);

        SimpleTypeModel idType = new SimpleTypeModel("id", DataTypeCreator.getDataType("string", "anyType"));
        idType.setTypePath("Root/id");
        idType.addLabel("en", "pk");
        idType.setFacets(new ArrayList<FacetModel>());
        metaDataTypes.put(idType.getTypePath(), idType);

        SimpleTypeModel nameType = new SimpleTypeModel("name", DataTypeCreator.getDataType("string", "anyType"));
        nameType.setTypePath("Root/name");
        nameType.addLabel("en", "name");
        nameType.setFacets(new ArrayList<FacetModel>());
        metaDataTypes.put(nameType.getTypePath(), nameType);

        ComplexTypeModel cpType = new ComplexTypeModel("cp", DataTypeCreator.getDataType("CP", "anyType"));
        cpType.setTypePath("Root/cp");
        cpType.addLabel("en", "complex type");
        cpType.setAutoExpand(false);
        metaDataTypes.put(cpType.getTypePath(), cpType);

        testType.addSubType(idType);
        testType.addSubType(nameType);
        testType.addSubType(cpType);

        SimpleTypeModel cp_titleType = new SimpleTypeModel("title", DataTypeCreator.getDataType("string", "anyType"));
        cp_titleType.setTypePath("Root/cp/title");
        cp_titleType.addLabel("en", "title");
        cp_titleType.setFacets(new ArrayList<FacetModel>());
        metaDataTypes.put(cp_titleType.getTypePath(), cp_titleType);

        SimpleTypeModel cp_contentType = new SimpleTypeModel("content", DataTypeCreator.getDataType("string", "anyType"));
        cp_contentType.setTypePath("Root/cp/content");
        cp_contentType.addLabel("en", "contnet info");
        cp_contentType.setFacets(new ArrayList<FacetModel>());
        metaDataTypes.put(cp_contentType.getTypePath(), cp_contentType);

        cpType.addSubType(cp_titleType);
        cpType.addSubType(cp_contentType);

        bindingEntityModel.setMetaDataTypes(metaDataTypes);
        viewBean.setBindingEntityModel(bindingEntityModel);

        return viewBean;
    }

    private ItemNodeModel builderItemNode() {
        ItemNodeModel testNode = new ItemNodeModel("Root");
        testNode.setTypePath("Root");

        ItemNodeModel idNode = new ItemNodeModel("id");
        idNode.setTypePath("Root/id");
        idNode.setKey(true);
        testNode.add(idNode);

        ItemNodeModel nameNode = new ItemNodeModel("name");
        nameNode.setTypePath("Root/name");
        testNode.add(nameNode);

        ItemNodeModel cpNode = new ItemNodeModel("cp");
        cpNode.setTypePath("Root/cp");
        testNode.add(cpNode);

        ItemNodeModel cpTitleNode = new ItemNodeModel("title");
        cpTitleNode.setTypePath("Root/cp/title");
        cpNode.add(cpTitleNode);

        ItemNodeModel cpContentNode = new ItemNodeModel("content");
        cpContentNode.setTypePath("Root/cp/content");
        cpNode.add(cpContentNode);

        return testNode;
    }

    public void testAutoFillValue4MandatoryBooleanField() {
        boolean enable = true;

        List<ModelData> toUpdateNodes = new ArrayList<ModelData>();
        ItemNodeModel root = new ItemNodeModel();
        root.set("id", 48);
        root.set("name", "testBoolean");
        ItemNodeModel node = new ItemNodeModel();
        node.set("id", 51);
        node.set("name", "complext");
        node.setParent(root);
        ItemNodeModel node1 = new ItemNodeModel();
        node1.set("id", 52);
        node1.set("name", "b2");
        node1.setMandatory(true);
        node1.setParent(node);
        ItemNodeModel node2 = new ItemNodeModel();
        node2.set("id", 53);
        node2.set("name", "e1");
        node2.setObjectValue("test");
        node2.setMandatory(true);
        node2.setParent(node);
        ItemNodeModel node3 = new ItemNodeModel();
        node3.set("id", 54);
        node3.set("name", "b3");
        node3.setParent(node);

        toUpdateNodes.add(node1);
        toUpdateNodes.add(node2);
        toUpdateNodes.add(node3);

        Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();
        fieldMap.put("49", new FormatTextField());
        fieldMap.put("52", new CheckBox());
        fieldMap.put("53", new FormatTextField());
        fieldMap.put("54", new CheckBox());

        assertNull(node1.getObjectValue());
        TreeDetailGridFieldCreator.autoFillValue4MandatoryBooleanField(enable, toUpdateNodes, fieldMap);
        assertEquals(false, node1.getObjectValue());
    }

    public void testRenderFk() {
        // test TMDM-5439 case
        ItemNodeModel productNode = new ItemNodeModel("Product");
        productNode.setTypePath("Product");
        ItemNodeModel pictureNode = new ItemNodeModel("Picture");
        pictureNode.setTypePath("Product/Picture");
        pictureNode.setKey(true);
        productNode.add(pictureNode);
        ItemNodeModel idNode = new ItemNodeModel("Id");
        idNode.setTypePath("Product/Id");
        productNode.add(idNode);
        ItemNodeModel nameNode = new ItemNodeModel("Name");
        nameNode.setTypePath("Product/Name");
        productNode.add(nameNode);
        ItemNodeModel priceNode = new ItemNodeModel("Price");
        priceNode.setTypePath("Product/Price");
        productNode.add(priceNode);
        ItemNodeModel storesNode = new ItemNodeModel("Stores");
        storesNode.setTypePath("Product/Stores");
        productNode.add(storesNode);
        ItemNodeModel storeNode = new ItemNodeModel("Store");
        storeNode.setTypePath("Product/Stores/Store");
        storesNode.add(storeNode);

        ViewBean viewBean = new ViewBean();
        entityModel = new EntityModel();
        LinkedHashMap<String, TypeModel> metaDataTypes = new LinkedHashMap<String, TypeModel>();
        ComplexTypeModel productType = new ComplexTypeModel("Product", DataTypeCreator.getDataType("Product", "anyType"));
        productType.setTypePath("Product");
        productType.addLabel("en", "product");
        metaDataTypes.put(productType.getTypePath(), productType);
        SimpleTypeModel pictureType = new SimpleTypeModel("Picture", DataTypeCreator.getDataType("string", "anyType"));
        pictureType.setTypePath("Product/Picture");
        pictureType.addLabel("en", "picture");
        pictureType.setAutoExpand(false);
        metaDataTypes.put(pictureType.getTypePath(), pictureType);
        SimpleTypeModel idType = new SimpleTypeModel("Id", DataTypeCreator.getDataType("string", "anyType"));
        idType.setTypePath("Product/Id");
        idType.addLabel("en", "pk");
        idType.setFacets(new ArrayList<FacetModel>());
        metaDataTypes.put(idType.getTypePath(), idType);
        SimpleTypeModel nameType = new SimpleTypeModel("Name", DataTypeCreator.getDataType("string", "anyType"));
        nameType.setTypePath("Product/Name");
        nameType.addLabel("en", "name");
        nameType.setFacets(new ArrayList<FacetModel>());
        metaDataTypes.put(nameType.getTypePath(), nameType);
        SimpleTypeModel priceType = new SimpleTypeModel("Price", DataTypeCreator.getDataType("string", "anyType"));
        priceType.setTypePath("Product/Price");
        priceType.addLabel("en", "price");
        priceType.setAutoExpand(true);
        metaDataTypes.put(priceType.getTypePath(), priceType);
        SimpleTypeModel storeType = new SimpleTypeModel("Store", DataTypeCreator.getDataType("string", "anyType"));
        storeType.setTypePath("Product/Stores/Store");
        storeType.addLabel("en", "store");
        storeType.setForeignkey("Store/Id");
        storeType.setNotSeparateFk(false);
        storeType.setAutoExpand(false);
        metaDataTypes.put(storeType.getTypePath(), storeType);
        ComplexTypeModel storesType = new ComplexTypeModel("Stores", DataTypeCreator.getDataType("string", "anyType"));
        storesType.setTypePath("Product/Stores");
        storesType.addLabel("en", "stores");
        storesType.setAutoExpand(false);
        metaDataTypes.put(storesType.getTypePath(), storesType);
        productType.addSubType(idType);
        productType.addSubType(nameType);
        productType.addSubType(pictureType);
        productType.addSubType(storesType);
        storesType.addSubType(storeType);

        entityModel.setMetaDataTypes(metaDataTypes);
        viewBean.setBindingEntityModel(entityModel);
        TreeDetail treeDetail = new TreeDetail(ItemsDetailPanel.newInstance());
        treeDetail.setViewBean(viewBean);

        ItemNodeModel rootNode = builderItemNode();
        DynamicTreeItem item = treeDetail.buildGWTTree(productNode, null, false, ItemDetailToolBar.VIEW_OPERATION);
        assertNotNull(item);
        assertEquals("Product", item.getItemNodeModel().getName());
        // Element 'Stores' shouldn't display in main tab because element 'store' render in foreignkey tab.
        assertEquals(4, item.getChildCount());
        assertNotNull(item.getChild(0));
        DynamicTreeItem child = (DynamicTreeItem) item.getChild(0);
        assertEquals("Picture", child.getItemNodeModel().getName());
        assertNotNull(item.getChild(1));
        child = (DynamicTreeItem) item.getChild(1);
        assertEquals("Id", child.getItemNodeModel().getName());
        assertNotNull(item.getChild(2));
        child = (DynamicTreeItem) item.getChild(2);
        assertEquals("Name", child.getItemNodeModel().getName());
        assertNotNull(item.getChild(3));
        child = (DynamicTreeItem) item.getChild(3);
        assertEquals("Price", child.getItemNodeModel().getName());

        // test TMDM-8801 case
        ItemNodeModel ElementArborescenceNode = new ItemNodeModel("ElementArborescence");
        ElementArborescenceNode.setTypePath("ElementArborescence");
        ItemNodeModel codeNode = new ItemNodeModel("Code");
        codeNode.setTypePath("ElementArborescence/Code");
        codeNode.setKey(true);
        ElementArborescenceNode.add(codeNode);
        ItemNodeModel nomNode = new ItemNodeModel("Nom");
        nomNode.setTypePath("ElementArborescence/Nom");
        ElementArborescenceNode.add(nomNode);
        ItemNodeModel typeElementArborescenceNode = new ItemNodeModel("TypeElementArborescence");
        typeElementArborescenceNode.setTypePath("ElementArborescence/TypeElementArborescence");
        ElementArborescenceNode.add(typeElementArborescenceNode);
        ItemNodeModel elementsListeNode = new ItemNodeModel("ElementsListe");
        elementsListeNode.setTypePath("ElementArborescence/TypeElementArborescence/ElementsListe");
        typeElementArborescenceNode.add(elementsListeNode);
        ItemNodeModel elementNode = new ItemNodeModel("Element");
        elementNode.setTypePath("ElementArborescence/TypeElementArborescence/ElementsListe/Element");
        elementsListeNode.add(elementNode);
        ItemNodeModel SocieteFkNode = new ItemNodeModel("SocieteFk");
        SocieteFkNode.setTypePath("ElementArborescence/TypeElementArborescence/ElementsListe/Element/SocieteFk");
        elementNode.add(SocieteFkNode);

        viewBean = new ViewBean();
        entityModel = new EntityModel();
        metaDataTypes = new LinkedHashMap<String, TypeModel>();
        ComplexTypeModel elementArborescenceType = new ComplexTypeModel("ElementArborescence", DataTypeCreator.getDataType(
                "ElementArborescence", "anyType"));
        elementArborescenceType.setTypePath("ElementArborescence");
        elementArborescenceType.addLabel("en", "ElementArborescence");
        metaDataTypes.put(elementArborescenceType.getTypePath(), elementArborescenceType);
        SimpleTypeModel codeType = new SimpleTypeModel("Code", DataTypeCreator.getDataType("string", "anyType"));
        codeType.setTypePath("ElementArborescence/Code");
        codeType.addLabel("en", "pk");
        codeType.setFacets(new ArrayList<FacetModel>());
        metaDataTypes.put(codeType.getTypePath(), codeType);
        SimpleTypeModel nomType = new SimpleTypeModel("Nom", DataTypeCreator.getDataType("string", "anyType"));
        nomType.setTypePath("ElementArborescence/Nom");
        nomType.addLabel("en", "Nom");
        nomType.setFacets(new ArrayList<FacetModel>());
        metaDataTypes.put(nomType.getTypePath(), nomType);
        ComplexTypeModel typeElementArborescenceType = new ComplexTypeModel("TypeElementArborescence",
                DataTypeCreator.getDataType("string", "anyType"));
        typeElementArborescenceType.setTypePath("ElementArborescence/TypeElementArborescence");
        typeElementArborescenceType.addLabel("en", "TypeElementArborescence");
        typeElementArborescenceType.setAutoExpand(true);
        metaDataTypes.put(typeElementArborescenceType.getTypePath(), typeElementArborescenceType);
        ComplexTypeModel elementsListeType = new ComplexTypeModel("ElementsListe", DataTypeCreator.getDataType("string",
                "anyType"));
        elementsListeType.setTypePath("ElementArborescence/TypeElementArborescence/ElementsListe");
        elementsListeType.addLabel("en", "ElementsListe");
        elementsListeType.setAutoExpand(true);
        metaDataTypes.put(elementsListeType.getTypePath(), elementsListeType);
        ComplexTypeModel elementType = new ComplexTypeModel("Element", DataTypeCreator.getDataType("string", "anyType"));
        elementType.setTypePath("ElementArborescence/TypeElementArborescence/ElementsListe/Element");
        elementType.addLabel("en", "Element");
        elementType.setAutoExpand(false);
        elementType.addComplexReusableTypes(new ComplexTypeModel("ArborescenceEtablissementsListe", null));
        elementType.addComplexReusableTypes(new ComplexTypeModel("ArborescenceSocietesListe", null));
        metaDataTypes.put(elementType.getTypePath(), elementType);
        SimpleTypeModel societeFkType = new SimpleTypeModel("SocieteFk", DataTypeCreator.getDataType("string", "anyType"));
        societeFkType.setTypePath("ElementArborescence/TypeElementArborescence/ElementsListe/Element/SocieteFk");
        societeFkType.addLabel("en", "SocieteFk");
        societeFkType.setForeignkey("Societe/CodeOSMOSE");
        societeFkType.setXpath("ElementArborescence/TypeElementArborescence/ElementsListe/Element/SocieteFk");
        societeFkType.setForeignKeyInfo(new ArrayList<String>());
        societeFkType.setNotSeparateFk(false);
        societeFkType.setAutoExpand(false);
        metaDataTypes.put(societeFkType.getTypePath(), societeFkType);
        elementArborescenceType.addSubType(codeType);
        elementArborescenceType.addSubType(nomType);
        elementArborescenceType.addSubType(typeElementArborescenceType);
        typeElementArborescenceType.addSubType(elementsListeType);
        elementsListeType.addSubType(elementType);
        elementType.addSubType(societeFkType);
        entityModel.setMetaDataTypes(metaDataTypes);
        viewBean.setBindingEntityModel(entityModel);
        treeDetail = new TreeDetail(ItemsDetailPanel.newInstance());
        treeDetail.setViewBean(viewBean);

        item = treeDetail.buildGWTTree(ElementArborescenceNode, null, false, ItemDetailToolBar.VIEW_OPERATION);
        assertNotNull(item);
        assertEquals("ElementArborescence", item.getItemNodeModel().getName());
        assertEquals(3, item.getChildCount());
        assertNotNull(item.getChild(0));
        child = (DynamicTreeItem) item.getChild(0);
        assertEquals("Code", child.getItemNodeModel().getName());
        assertNotNull(item.getChild(1));
        child = (DynamicTreeItem) item.getChild(1);
        assertEquals("Nom", child.getItemNodeModel().getName());
        assertNotNull(item.getChild(2));
        child = (DynamicTreeItem) item.getChild(2);
        assertEquals("TypeElementArborescence", child.getItemNodeModel().getName());
        assertEquals(1, child.getChildCount());
        assertNotNull(child.getChild(0));
        child = (DynamicTreeItem) child.getChild(0);
        assertEquals("ElementsListe", child.getItemNodeModel().getName());
        assertEquals(1, child.getChildCount());
        assertNotNull(child.getChild(0));
        child = (DynamicTreeItem) child.getChild(0);
        assertEquals("Element", child.getItemNodeModel().getName());
        assertEquals(1, child.getChildCount());
        assertNotNull(child.getChild(0));
        child = (DynamicTreeItem) child.getChild(0);
        // element 'SocieteFk' should display in main tab although it don't set property 'render in main tab' to true in
        // studio.
        assertNotNull(child);

        // test TMDM-8920 case
        ItemNodeModel aNode = new ItemNodeModel("A");
        aNode.setTypePath("A");
        ItemNodeModel a_IdNode = new ItemNodeModel("A_Id");
        a_IdNode.setTypePath("A/A_Id");
        a_IdNode.setKey(true);
        aNode.add(a_IdNode);
        ItemNodeModel a_NameNode = new ItemNodeModel("A_Name");
        a_NameNode.setTypePath("A/A_Name");
        aNode.add(a_NameNode);
        ItemNodeModel bCollection1Node = new ItemNodeModel("BCollection1");
        bCollection1Node.setTypePath("A/BCollection1");
        aNode.add(bCollection1Node);
        ItemNodeModel b1Node = new ItemNodeModel("B");
        b1Node.setTypePath("A/BCollection1/B");
        bCollection1Node.add(b1Node);
        ItemNodeModel bCoolection2Node = new ItemNodeModel("BCollection2");
        bCoolection2Node.setTypePath("A/BCollection2");
        aNode.add(bCoolection2Node);
        ItemNodeModel b2Node = new ItemNodeModel("B");
        b2Node.setTypePath("A/BCollection2/B");
        bCoolection2Node.add(b2Node);
        ItemNodeModel cCollection1Node = new ItemNodeModel("CCollection1");
        cCollection1Node.setTypePath("A/CCollection1");
        aNode.add(cCollection1Node);
        ItemNodeModel c1Node = new ItemNodeModel("C");
        c1Node.setTypePath("A/CCollection1/C");
        cCollection1Node.add(c1Node);
        ItemNodeModel cCoolection2Node = new ItemNodeModel("CCollection2");
        cCoolection2Node.setTypePath("A/CCollection2");
        aNode.add(cCoolection2Node);
        ItemNodeModel c2Node = new ItemNodeModel("C");
        c2Node.setTypePath("A/CCollection2/C");
        cCoolection2Node.add(c2Node);

        viewBean = new ViewBean();
        entityModel = new EntityModel();
        metaDataTypes = new LinkedHashMap<String, TypeModel>();
        ComplexTypeModel aType = new ComplexTypeModel("A", DataTypeCreator.getDataType("A", "anyType"));
        aType.setTypePath("A");
        aType.addLabel("en", "A");
        metaDataTypes.put(aType.getTypePath(), aType);
        SimpleTypeModel a_IdType = new SimpleTypeModel("A_Id", DataTypeCreator.getDataType("string", "anyType"));
        a_IdType.setTypePath("A/A_Id");
        a_IdType.addLabel("en", "pk");
        a_IdType.setFacets(new ArrayList<FacetModel>());
        metaDataTypes.put(a_IdType.getTypePath(), a_IdType);
        SimpleTypeModel a_NameType = new SimpleTypeModel("A_Name", DataTypeCreator.getDataType("string", "anyType"));
        a_NameType.setTypePath("A/A_Name");
        a_NameType.addLabel("en", "name");
        a_NameType.setFacets(new ArrayList<FacetModel>());
        metaDataTypes.put(a_NameType.getTypePath(), a_NameType);
        ComplexTypeModel bCollection1Type = new ComplexTypeModel("BCollection1", DataTypeCreator.getDataType("string", "anyType"));
        bCollection1Type.setTypePath("A/BCollection1");
        bCollection1Type.addLabel("en", "bCollection1");
        bCollection1Type.setAutoExpand(false);
        metaDataTypes.put(bCollection1Type.getTypePath(), bCollection1Type);
        SimpleTypeModel b1Type = new SimpleTypeModel("B", DataTypeCreator.getDataType("string", "anyType"));
        b1Type.setTypePath("A/BCollection1/B");
        b1Type.addLabel("en", "b");
        b1Type.setForeignkey("B/B_Id");
        b1Type.setNotSeparateFk(false);
        b1Type.setAutoExpand(false);
        metaDataTypes.put(b1Type.getTypePath(), b1Type);
        ComplexTypeModel bCollection2Type = new ComplexTypeModel("BCollection2", DataTypeCreator.getDataType("string", "anyType"));
        bCollection2Type.setTypePath("A/BCollection2");
        bCollection2Type.addLabel("en", "bCollection2");
        bCollection2Type.setAutoExpand(false);
        metaDataTypes.put(bCollection2Type.getTypePath(), bCollection2Type);
        SimpleTypeModel b2Type = new SimpleTypeModel("B", DataTypeCreator.getDataType("string", "anyType"));
        b2Type.setTypePath("A/BCollection2/B");
        b2Type.addLabel("en", "b");
        b2Type.setForeignkey("B/B_Id");
        b2Type.setNotSeparateFk(false);
        b2Type.setAutoExpand(false);
        metaDataTypes.put(b2Type.getTypePath(), b2Type);
        ComplexTypeModel cCollection1Type = new ComplexTypeModel("CCollection1", DataTypeCreator.getDataType("string", "anyType"));
        cCollection1Type.setTypePath("A/CCollection1");
        cCollection1Type.addLabel("en", "cCollection1");
        cCollection1Type.setAutoExpand(false);
        metaDataTypes.put(cCollection1Type.getTypePath(), cCollection1Type);
        SimpleTypeModel c1Type = new SimpleTypeModel("C", DataTypeCreator.getDataType("string", "anyType"));
        c1Type.setTypePath("A/CCollection1/C");
        c1Type.addLabel("en", "b");
        c1Type.setForeignkey("C/C_Id");
        c1Type.setNotSeparateFk(false);
        c1Type.setAutoExpand(false);
        metaDataTypes.put(c1Type.getTypePath(), c1Type);
        ComplexTypeModel cCollection2Type = new ComplexTypeModel("CCollection2", DataTypeCreator.getDataType("string", "anyType"));
        cCollection2Type.setTypePath("A/CCollection2");
        cCollection2Type.addLabel("en", "cCollection2");
        cCollection2Type.setAutoExpand(false);
        metaDataTypes.put(cCollection2Type.getTypePath(), cCollection2Type);
        SimpleTypeModel c2Type = new SimpleTypeModel("C", DataTypeCreator.getDataType("string", "anyType"));
        c2Type.setTypePath("A/CCollection2/C");
        c2Type.addLabel("en", "b");
        c2Type.setForeignkey("C/C_Id");
        c2Type.setNotSeparateFk(false);
        c2Type.setAutoExpand(false);
        metaDataTypes.put(c2Type.getTypePath(), c2Type);
        aType.addSubType(a_IdType);
        aType.addSubType(a_NameType);
        aType.addSubType(bCollection1Type);
        bCollection1Type.addSubType(b1Type);
        aType.addSubType(bCollection2Type);
        bCollection2Type.addSubType(b2Type);
        aType.addSubType(cCollection1Type);
        cCollection1Type.addSubType(c1Type);
        aType.addSubType(cCollection2Type);
        cCollection2Type.addSubType(c2Type);

        entityModel.setMetaDataTypes(metaDataTypes);
        viewBean.setBindingEntityModel(entityModel);
        treeDetail = new TreeDetail(ItemsDetailPanel.newInstance());
        treeDetail.setViewBean(viewBean);

        item = treeDetail.buildGWTTree(aNode, null, false, ItemDetailToolBar.VIEW_OPERATION);
        assertNotNull(item);
        // Elements 'BCollection1','BCollection2','CCollection1' and 'CCollection2' shouldn't display in main tab
        // because elements 'B' and 'C' render in foreignkey tab.
        assertEquals(2, item.getChildCount());
        assertNotNull(item.getChild(0));
        child = (DynamicTreeItem) item.getChild(0);
        assertEquals("A_Id", child.getItemNodeModel().getName());
        assertNotNull(item.getChild(1));
        child = (DynamicTreeItem) item.getChild(1);
        assertEquals("A_Name", child.getItemNodeModel().getName());
    }

    /**
     * Test Model Structure: <br>
     * Root<br>
     * |_id<br>
     * |_name<br>
     * |_cp(autoExpand=false)<br>
     * |_title<br>
     * |_content<br>
     */
    public void testBulkUpdate() {
        ViewBean viewBean = getViewBean();
        TreeDetail treeDetail = new TreeDetail(ItemsDetailPanel.newInstance());
        treeDetail.setViewBean(viewBean);
        ItemNodeModel rootNode = builderItemNode();
        DynamicTreeItem item = treeDetail.buildGWTTree(rootNode, null, false, ItemDetailToolBar.BULK_UPDATE_OPERATION);
        // validate result
        assertNotNull(item);
        assertEquals(3, item.getChildCount());
        assertTrue(((ItemNodeModel) item.getChild(0).getUserObject()).isMassUpdate());
        assertTrue(((ItemNodeModel) item.getChild(1).getUserObject()).isMassUpdate());
        assertTrue(((ItemNodeModel) item.getChild(2).getUserObject()).isMassUpdate());
        // cp node lazy loading
        DynamicTreeItem cpItem = (DynamicTreeItem) item.getChild(2);
        assertNotNull(cpItem);
        assertEquals(1, cpItem.getChildCount());
        assertTrue(cpItem.getChild(0) instanceof GhostTreeItem);
        // render cp node
        cpItem.setState(true);
        assertEquals(2, cpItem.getChildCount());
        assertTrue(((ItemNodeModel) cpItem.getChild(0).getUserObject()).isMassUpdate());
        assertFalse(((ItemNodeModel) cpItem.getChild(0).getUserObject()).isEdited());
        assertTrue(((ItemNodeModel) cpItem.getChild(0).getUserObject()).isMassUpdate());
        assertFalse(((ItemNodeModel) cpItem.getChild(0).getUserObject()).isEdited());
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords";
    }

    class MockBrowseRecordsServiceAsync implements BrowseRecordsServiceAsync {

        @Override
        public void saveItem(ViewBean viewBean, String ids, String xml, boolean isCreate, String language,
                AsyncCallback<ItemResult> callback) {
            callback.onSuccess(new ItemResult(ItemResult.SUCCESS));
        }

        @Override
        public void getForeignKeyPolymTypeList(String xpathForeignKey, String language, AsyncCallback<List<Restriction>> callback) {
        }

        @Override
        public void switchForeignKeyType(String targetEntityType, String xpathForeignKey, String xpathInfoForeignKey,
                String fkFilter, AsyncCallback<ForeignKeyDrawer> callback) {
        }

        @Override
        public void queryItemBeans(QueryModel config, String language, AsyncCallback<ItemBasePageLoadResult<ItemBean>> callback) {
        }

        @Override
        public void saveItemBean(ItemBean item, String language, AsyncCallback<String> callback) {
        }

        @Override
        public void getItem(ItemBean itemBean, String viewPK, EntityModel entityModel, boolean isStaging, String language,
                AsyncCallback<ItemBean> callback) {
        }

        @Override
        public void getView(String viewPk, String language, AsyncCallback<ViewBean> callback) {
        }

        @Override
        public void deleteItemBeans(List<ItemBean> items, boolean override, String language,
                AsyncCallback<List<ItemResult>> callback) {
            callback.onSuccess(new ArrayList<ItemResult>());
        }

        @Override
        public void checkFKIntegrity(List<ItemBean> selectedItems, AsyncCallback<Map<ItemBean, FKIntegrityResult>> asyncCallback) {
        }

        @Override
        public void logicalDeleteItem(ItemBean item, String path, boolean override, AsyncCallback<Void> callback) {

        }

        @Override
        public void logicalDeleteItems(List<ItemBean> items, String path, boolean override, AsyncCallback<Void> callback) {
        }

        @Override
        public void getViewsList(String language, AsyncCallback<List<ItemBaseModel>> callback) {
        }

        @Override
        public void getCriteriaByBookmark(String bookmark, AsyncCallback<String> callback) {
        }

        @Override
        public void getUserCriterias(String view, AsyncCallback<List<ItemBaseModel>> callback) {
        }

        @Override
        public void getAppHeader(AsyncCallback<AppHeader> callback) {
        }

        @Override
        public void getCurrentDataModel(AsyncCallback<String> callback) {
        }

        @Override
        public void getCurrentDataCluster(AsyncCallback<String> callback) {
        }

        @Override
        public void querySearchTemplates(String view, boolean isShared, BasePagingLoadConfigImpl load,
                AsyncCallback<ItemBasePageLoadResult<ItemBaseModel>> callback) {
        }

        @Override
        public void deleteSearchTemplate(String id, AsyncCallback<Void> callback) {
        }

        @Override
        public void isExistCriteria(String dataObjectLabel, String id, AsyncCallback<Boolean> callback) {
        }

        @Override
        public void saveCriteria(String viewPK, String templateName, boolean isShared, String criteriaString,
                AsyncCallback<Void> callback) {
        }

        @Override
        public void getMandatoryFieldList(String tableName, AsyncCallback<List<String>> callback) {
        }

        @Override
        public void saveItem(String concept, String ids, String xml, boolean isCreate, String language,
                AsyncCallback<ItemResult> callback) {
        }

        @Override
        public void getColumnTreeLayout(String concept, AsyncCallback<ColumnTreeLayoutModel> callback) {
        }

        @Override
        public void getRunnableProcessList(String concept, String language, AsyncCallback<List<ItemBaseModel>> callback) {
        }

        @Override
        public void processItem(String concept, String[] ids, String transformerPK, AsyncCallback<String> callback) {
        }

        @Override
        public void getLineageEntity(String concept, AsyncCallback<List<String>> callback) {
        }

        @Override
        public void getSmartViewList(String regex, AsyncCallback<List<ItemBaseModel>> callback) {
        }

        @Override
        public void getItemBeanById(String concept, String[] ids, String language, AsyncCallback<ItemBean> callback) {

        }

        @Override
        public void executeVisibleRule(ViewBean viewBean, String xml, AsyncCallback<List<VisibleRuleResult>> asyncCallback) {

        }

        @Override
        public void isItemModifiedByOthers(ItemBean itemBean, AsyncCallback<Boolean> callback) {

        }

        @Override
        public void updateItem(String concept, String ids, Map<String, String> changedNodes, String xml, EntityModel entityModel,
                String language, AsyncCallback<ItemResult> callback) {

        }

        @Override
        public void updateItems(List<UpdateItemModel> updateItems, String language, AsyncCallback<List<ItemResult>> callback) {

        }

        @Override
        public void formatValue(FormatModel model, AsyncCallback<String> callback) {

        }

        @Override
        public void getEntityModel(String concept, String language, AsyncCallback<EntityModel> callback) {
            callback.onSuccess(entityModel);
        }

        @Override
        public void createDefaultItemNodeModel(ViewBean viewBean, Map<String, List<String>> initDataMap, String language,
                AsyncCallback<ItemNodeModel> callback) {

        }

        @Override
        public void getForeignKeyValues(String concept, String[] ids, String language,
                AsyncCallback<Map<String, List<String>>> callback) {

        }

        @Override
        public void isExistId(String concept, String[] ids, String language, AsyncCallback<Boolean> callback) {

        }

        @Override
        public void queryItemBeanById(String dataClusterPK, ViewBean viewBean, EntityModel entityModel, String id,
                String language, AsyncCallback<ItemBean> callback) {
        }

        @Override
        public void getGoldenRecordIdByGroupId(String dataClusterPK, String viewPK, String concept, String[] keys,
                String groupId, AsyncCallback<String> callback) {
        }

        @Override
        public void getRecords(String concept, List<String> idsList, AsyncCallback<List<ItemBean>> callback) {

        }

        @Override
        public void getCurrentDataCluster(boolean isStaging, AsyncCallback<String> callback) {

        }

        @Override
        public void getItemNodeModel(ItemBean item, EntityModel entity, boolean isStaging, String language,
                AsyncCallback<ItemNodeModel> callback) {

        }

        @Override
        public void getForeignKeyModel(String concept, String ids, boolean isStaging, String language,
                AsyncCallback<ForeignKeyModel> callback) {

        }

        @Override
        public void createSubItemNodeModel(ViewBean viewBean, String xml, String typePath, String contextPath, String realType,
                boolean isStaging, String language, AsyncCallback<ItemNodeModel> callback) {
        }

        @Override
        public void checkTask(String dataClusterPK, String concept, String groupId, AsyncCallback<Integer> callback) {

        }

        @Override
        public void getForeignKeyBean(String concept, String ids, String xml, String currentXpath, String foreignKey,
                List<String> foreignKeyInfo, String foreignKeyFilter, boolean staging, String language,
                AsyncCallback<ForeignKeyBean> callback) {
        }

        @Override
        public void getItemBeanById(String concept, String ids, String language, AsyncCallback<ItemBean> callback) {

        }

        @Override
        public void getForeignKeyList(BasePagingLoadConfigImpl config, TypeModel model, String foreignKeyFilterValue,
                String dataClusterPK, String language, AsyncCallback<ItemBasePageLoadResult<ForeignKeyBean>> callback) {
        }

        @Override
        public void getForeignKeySuggestion(BasePagingLoadConfigImpl config, TypeModel model, String foreignKeyFilterValue,
                String dataClusterPK, String language, AsyncCallback<List<ForeignKeyBean>> callback) {
        }

        @Override
        public void getExsitedViewName(String concept, AsyncCallback<String> callback) {
        }

        @Override
        public void handleNavigatorNodeLabel(String jsonString, String language, AsyncCallback<String> callback) {
        }

        @Override
        public void bulkUpdateItem(String baseUrl, String concept, String xml, String language, AsyncCallback<String> callback) {
        }
    }
}