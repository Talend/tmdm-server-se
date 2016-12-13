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

import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.FacetModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.creator.DataTypeCreator;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail.DynamicTreeItem;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.junit.client.GWTTestCase;

public class IncrementalBuildTreeGWTTest extends GWTTestCase {

    ItemsDetailPanel mockItemsDetailPanel;

    TreeDetail mockTreeDetail;

    IncrementalBuildTree incCommand;

    Map<TypeModel, ItemNodeModel> foreignKeyParentMap;

    Map<TypeModel, List<ItemNodeModel>> foreighKeyMap;

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        mockItemsDetailPanel = ItemsDetailPanel.newInstance();
        mockTreeDetail = new TreeDetail(mockItemsDetailPanel);

        foreignKeyParentMap = new HashMap<TypeModel, ItemNodeModel>();
        foreighKeyMap = new HashMap<TypeModel, List<ItemNodeModel>>();
        initJsEnv();
        
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
    }

    private native void initJsEnv()/*-{
        $wnd.language = "en";  //$NON-NLS-1$
    }-*/;

    Map<String, String> genMap() {
        return new HashMap<String, String>();
    }

    private void initTypeModel(TypeModel tm) {
        if (tm.isSimpleType()) {
            ((SimpleTypeModel) tm).setFacets(new ArrayList<FacetModel>());
        }
        __initTypeModel(tm);
    }

    private native void __initTypeModel(TypeModel tm)/*-{
        var map = this.@org.talend.mdm.webapp.browserecords.client.widget.treedetail.IncrementalBuildTreeGWTTest::genMap()();
        tm.@org.talend.mdm.webapp.base.shared.TypeModel::labelMap = map;
    }-*/;

    public void testIncrementalBuildTree() {
        DynamicTreeItem item = new DynamicTreeItem();
        ViewBean viewBean = getViewBean();
        mockTreeDetail.setViewBean(viewBean);
        ItemNodeModel testNode = builderItemNode();

        incCommand = new IncrementalBuildTree(mockTreeDetail, testNode, viewBean, false,
                ItemDetailToolBar.VIEW_OPERATION, item);

        while (incCommand.execute());
        assertNotNull(item);
        assertEquals(128, item.getChildCount());
        assertEquals(1, incCommand.getForeighKeyMap().size());
    }

    private ViewBean getViewBean() {
        ViewBean viewBean = new ViewBean();
        EntityModel bindingEntityModel = new EntityModel();
        LinkedHashMap<String, TypeModel> metaDataTypes = new LinkedHashMap<String, TypeModel>();

        ComplexTypeModel testType = new ComplexTypeModel("Test", DataTypeCreator.getDataType("Test", "anyType")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        testType.setTypePath("Test"); //$NON-NLS-1$
        initTypeModel(testType);
        testType.addLabel("en", "test root node"); //$NON-NLS-1$ //$NON-NLS-2$
        metaDataTypes.put(testType.getTypePath(), testType);

        SimpleTypeModel subelementType = new SimpleTypeModel("subelement", DataTypeCreator.getDataType("string", "anyType")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        subelementType.setTypePath("Test/subelement"); //$NON-NLS-1$
        initTypeModel(subelementType);
        subelementType.addLabel("en", "this is pk"); //$NON-NLS-1$ //$NON-NLS-2$
        metaDataTypes.put(subelementType.getTypePath(), subelementType);

        SimpleTypeModel nameType = new SimpleTypeModel("name", DataTypeCreator.getDataType("string", "anyType")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        nameType.setTypePath("Test/name"); //$NON-NLS-1$
        initTypeModel(nameType);
        nameType.addLabel("en", "name field"); //$NON-NLS-1$ //$NON-NLS-2$
        metaDataTypes.put(nameType.getTypePath(), nameType);

        SimpleTypeModel addressType = new SimpleTypeModel("address", DataTypeCreator.getDataType("string", "anyType")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        addressType.setTypePath("Test/address"); //$NON-NLS-1$
        initTypeModel(addressType);
        addressType.addLabel("en", "address field"); //$NON-NLS-1$//$NON-NLS-2$
        metaDataTypes.put(addressType.getTypePath(), addressType);

        ComplexTypeModel cpType = new ComplexTypeModel("cp", DataTypeCreator.getDataType("CP", "anyType")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        cpType.setTypePath("Test/cp"); //$NON-NLS-1$
        initTypeModel(cpType);
        cpType.addLabel("en", "this is complex type"); //$NON-NLS-1$ //$NON-NLS-2$
        metaDataTypes.put(cpType.getTypePath(), cpType);

        testType.addSubType(subelementType);
        testType.addSubType(nameType);
        testType.addSubType(cpType);

        SimpleTypeModel cp_titleType = new SimpleTypeModel("title", DataTypeCreator.getDataType("string", "anyType")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        cp_titleType.setTypePath("Test/cp/title"); //$NON-NLS-1$
        initTypeModel(cp_titleType);
        cp_titleType.addLabel("en", "title"); //$NON-NLS-1$ //$NON-NLS-2$
        metaDataTypes.put(cp_titleType.getTypePath(), cp_titleType);

        ComplexTypeModel detailType = new ComplexTypeModel("detail", DataTypeCreator.getDataType("Detail", "anyType")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        detailType.setTypePath("Test/cp/detail"); //$NON-NLS-1$
        initTypeModel(detailType);
        detailType.addLabel("en", "detail info"); //$NON-NLS-1$ //$NON-NLS-2$
        metaDataTypes.put(detailType.getTypePath(), detailType);

        cpType.addSubType(cp_titleType);
        cpType.addSubType(detailType);

        SimpleTypeModel detail_titleType = new SimpleTypeModel("title", DataTypeCreator.getDataType("string", "anyType")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        detail_titleType.setTypePath("Test/cp/detail/title"); //$NON-NLS-1$
        initTypeModel(detail_titleType);
        detail_titleType.addLabel("en", "detail title"); //$NON-NLS-1$ //$NON-NLS-2$
        metaDataTypes.put(detail_titleType.getTypePath(), detail_titleType);

        SimpleTypeModel contentType = new SimpleTypeModel("content", DataTypeCreator.getDataType("string", "anyType")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        contentType.setTypePath("Test/cp/detail/content"); //$NON-NLS-1$
        initTypeModel(contentType);
        contentType.addLabel("en", "contnet info"); //$NON-NLS-1$ //$NON-NLS-2$
        metaDataTypes.put(contentType.getTypePath(), contentType);

        detailType.addSubType(detail_titleType);
        detailType.addSubType(contentType);

        SimpleTypeModel fkOpenType = new SimpleTypeModel("open", DataTypeCreator.getDataType("string", "anyType")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        fkOpenType.setTypePath("Test/open"); //$NON-NLS-1$
        fkOpenType.setForeignkey("open"); //$NON-NLS-1$
        fkOpenType.setNotSeparateFk(false);
        initTypeModel(fkOpenType);
        fkOpenType.addLabel("en", "open field"); //$NON-NLS-1$//$NON-NLS-2$
        metaDataTypes.put(fkOpenType.getTypePath(), fkOpenType);

        SimpleTypeModel fkCloseType = new SimpleTypeModel("close", DataTypeCreator.getDataType("string", "anyType")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        fkCloseType.setTypePath("Test/close"); //$NON-NLS-1$
        fkCloseType.setForeignkey("close"); //$NON-NLS-1$
        fkCloseType.setNotSeparateFk(false);
        initTypeModel(fkCloseType);
        addressType.addLabel("en", "close field"); //$NON-NLS-1$//$NON-NLS-2$
        metaDataTypes.put(fkCloseType.getTypePath(), fkCloseType);
        
        bindingEntityModel.setMetaDataTypes(metaDataTypes);
        viewBean.setBindingEntityModel(bindingEntityModel);

        return viewBean;
    }

    private ItemNodeModel builderItemNode() {
        ItemNodeModel testNode = new ItemNodeModel("Test"); //$NON-NLS-1$
        testNode.setTypePath("Test"); //$NON-NLS-1$

        ItemNodeModel subelementNode = new ItemNodeModel("subelement"); //$NON-NLS-1$
        subelementNode.setTypePath("Test/subelement"); //$NON-NLS-1$
        subelementNode.setKey(true);
        testNode.add(subelementNode);

        ItemNodeModel nameNode = new ItemNodeModel("name"); //$NON-NLS-1$
        nameNode.setTypePath("Test/name"); //$NON-NLS-1$
        testNode.add(nameNode);
        
        for (int j = 0; j < 125; j++) {
            ItemNodeModel addressNode = new ItemNodeModel("address"); //$NON-NLS-1$
            addressNode.setTypePath("Test/address"); //$NON-NLS-1$
            testNode.add(addressNode);
        }

        ItemNodeModel cpNode = new ItemNodeModel("cp"); //$NON-NLS-1$
        cpNode.setTypePath("Test/cp"); //$NON-NLS-1$
        testNode.add(cpNode);

        ItemNodeModel cpTitleNode = new ItemNodeModel("title"); //$NON-NLS-1$
        cpTitleNode.setTypePath("Test/cp/title"); //$NON-NLS-1$
        cpNode.add(cpTitleNode);

        for (int i = 0; i < 15; i++) {
            ItemNodeModel detailNode = new ItemNodeModel("detail"); //$NON-NLS-1$
            detailNode.setTypePath("Test/cp/detail"); //$NON-NLS-1$
            ItemNodeModel detailTitleNode = new ItemNodeModel("title"); //$NON-NLS-1$
            detailTitleNode.setTypePath("Test/cp/detail/title"); //$NON-NLS-1$
            ItemNodeModel contentNode = new ItemNodeModel("content"); //$NON-NLS-1$
            contentNode.setTypePath("Test/cp/detail/content"); //$NON-NLS-1$
            detailNode.add(detailTitleNode);
            detailNode.add(contentNode);
            cpNode.add(detailNode);
        }
        
        ItemNodeModel openNode = new ItemNodeModel("open"); //$NON-NLS-1$
        openNode.setTypePath("Test/open"); //$NON-NLS-1$
        openNode.setHide(false);
        testNode.add(openNode);

        ItemNodeModel closeNode = new ItemNodeModel("close"); //$NON-NLS-1$
        closeNode.setTypePath("Test/close"); //$NON-NLS-1$
        closeNode.setHide(true);
        testNode.add(closeNode);
        
        return testNode;
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }

}
