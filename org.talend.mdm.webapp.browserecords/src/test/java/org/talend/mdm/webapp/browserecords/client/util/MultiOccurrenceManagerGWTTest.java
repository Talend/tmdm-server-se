/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail.DynamicTreeItem;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx;

import com.google.gwt.junit.client.GWTTestCase;

public class MultiOccurrenceManagerGWTTest extends GWTTestCase {

    Map<String, TypeModel> metaDataTypes = new HashMap<String, TypeModel>();

    TreeDetail treeDetail = null;

    List<DynamicTreeItem> multiNodes = null;

    public void testAddMultiOccurrenceNode() {
        SimpleTypeModel simpleTypeModel = new SimpleTypeModel();
        simpleTypeModel.setAutoExpand(false);
        simpleTypeModel.setMaxOccurs(5);
        metaDataTypes.put("ThirdEntity/optionalDetails/optionalUbounded3", simpleTypeModel); //$NON-NLS-1$

        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.setName("optionalUbounded3"); //$NON-NLS-1$
        itemNodeModel.setTypePath("ThirdEntity/optionalDetails/optionalUbounded3"); //$NON-NLS-1$

        ItemNodeModel parent = new ItemNodeModel();
        parent.setName("optionalDetails"); //$NON-NLS-1$
        parent.setTypePath("ThirdEntity/optionalDetails"); //$NON-NLS-1$
        parent.add(itemNodeModel);
        itemNodeModel.setParent(parent);

        DynamicTreeItem item = new DynamicTreeItem();
        item.setItemNodeModel(itemNodeModel);

        DynamicTreeItem parentItem = new DynamicTreeItem();
        parentItem.addItem(item);

        MultiOccurrenceManager manager = new MultiOccurrenceManager(metaDataTypes, treeDetail);
        manager.addMultiOccurrenceNode(item);

        multiNodes = _getMultiOccurrence(manager, "optionalDetails/optionalUbounded3"); //$NON-NLS-1$
        assertEquals(1, multiNodes.size());
        assertEquals("optionalUbounded3", multiNodes.get(0).getItemNodeModel().getName()); //$NON-NLS-1$
        manager.addMultiOccurrenceNode(item);

        multiNodes = _getMultiOccurrence(manager, "optionalDetails/optionalUbounded3"); //$NON-NLS-1$
        assertEquals(2, multiNodes.size());
        assertEquals("optionalUbounded3", multiNodes.get(1).getItemNodeModel().getName()); //$NON-NLS-1$
    }

    public void testAddMultiOccurrenceNodeForComplexType() {
        ComplexTypeModel complexTypeModel = new ComplexTypeModel();
        complexTypeModel.setAutoExpand(false);
        complexTypeModel.setMaxOccurs(5);
        metaDataTypes.put("Citizen/identity", complexTypeModel); //$NON-NLS-1$

        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.setName("identity"); //$NON-NLS-1$
        itemNodeModel.setTypePath("Citizen/identity"); //$NON-NLS-1$

        ItemNodeModel parent = new ItemNodeModel();
        parent.setName("Citizen"); //$NON-NLS-1$
        parent.setTypePath("Citizen"); //$NON-NLS-1$
        parent.add(itemNodeModel);
        itemNodeModel.setParent(parent);

        DynamicTreeItem item = new DynamicTreeItem();
        item.setItemNodeModel(itemNodeModel);

        DynamicTreeItem parentItem = new DynamicTreeItem();
        parentItem.addItem(item);

        MultiOccurrenceManager manager = new MultiOccurrenceManager(metaDataTypes, treeDetail);
        manager.addMultiOccurrenceNode(item);

        multiNodes = _getMultiOccurrence(manager, "Citizen/identity"); //$NON-NLS-1$
        assertEquals(1, multiNodes.size());
        ItemNodeModel itemNodeModel1 = multiNodes.get(0).getItemNodeModel();
        assertEquals("identity", itemNodeModel1.getName()); //$NON-NLS-1$
        assertEquals(false, itemNodeModel1.isEdited()); //$NON-NLS-1$
        assertEquals(false, itemNodeModel1.isMassUpdate()); //$NON-NLS-1$
        manager.addMultiOccurrenceNode(item);

        multiNodes = _getMultiOccurrence(manager, "Citizen/identity"); //$NON-NLS-1$
        assertEquals(2, multiNodes.size());
        ItemNodeModel itemNodeModel2 = multiNodes.get(1).getItemNodeModel();
        assertEquals("identity", itemNodeModel2.getName()); //$NON-NLS-1$
        assertEquals(false, itemNodeModel2.isEdited()); //$NON-NLS-1$
        assertEquals(false, itemNodeModel2.isMassUpdate()); //$NON-NLS-1$
    }

    @SuppressWarnings("nls")
    public void testAddMultiOccurrenceNode_CumtomLayout() {
        SimpleTypeModel simpleTypeModel = new SimpleTypeModel();
        simpleTypeModel.setAutoExpand(false);
        simpleTypeModel.setMaxOccurs(-1);
        metaDataTypes.put("CO_Contrat/RefSpecificiteContractuelle", simpleTypeModel); //$NON-NLS-1$

        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.setName("RefSpecificiteContractuelle"); //$NON-NLS-1$
        itemNodeModel.setTypePath("CO_Contrat/RefSpecificiteContractuelle"); //$NON-NLS-1$        

        ItemNodeModel parent = new ItemNodeModel();
        parent.setName("CO_Contrat"); //$NON-NLS-1$
        parent.setTypePath("CO_Contrat"); //$NON-NLS-1$
        parent.add(itemNodeModel);
        itemNodeModel.setParent(parent);

        DynamicTreeItem item = new DynamicTreeItem();
        item.setItemNodeModel(itemNodeModel);

        DynamicTreeItem parentItem = new DynamicTreeItem();
        TreeItemEx treeItemEx = new TreeItemEx();
        treeItemEx
                .addItem("<div style=\"padding: 3px 3px 3px 23px; margin-left: 16px;\">"
                        + "<div id=\"gwt-uid-185\" role=\"treeitem\" class=\"gwt-TreeItem\" style=\"display: inline;\">"
                        + "<div class=\"gwt-HTML\"><p style=\"background-color: #C0D72D; font-size: 18px; text-transform: uppercase; font-weight: bold; padding-left: 5px\">"
                        + "Description générale du Contrat</p></div></div></div>");
        parentItem.addItem(treeItemEx);
        parentItem.addItem(item);

        try {
            MultiOccurrenceManager manager = new MultiOccurrenceManager(metaDataTypes, treeDetail);
            manager.addMultiOccurrenceNode(item);
            multiNodes = _getMultiOccurrence(manager, "CO_Contrat/RefSpecificiteContractuelle"); //$NON-NLS-1$
            assertEquals(1, multiNodes.size());
        } catch (Exception e) {
            fail();
        }
    }

    public void testAddMultiOccurrenceNodeValid() {
        SimpleTypeModel simpleTypeModel = new SimpleTypeModel();
        simpleTypeModel.setAutoExpand(false);
        simpleTypeModel.setMaxOccurs(5);
        metaDataTypes.put("ThirdEntity/optionalDetails/optionalUbounded3", simpleTypeModel); //$NON-NLS-1$

        ComplexTypeModel complexTypeModel = new ComplexTypeModel();
        complexTypeModel.setAutoExpand(false);
        complexTypeModel.setMinOccurs(0);
        metaDataTypes.put("ThirdEntity/optionalDetails", complexTypeModel); //$NON-NLS-1$

        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.setName("optionalUbounded3"); //$NON-NLS-1$
        itemNodeModel.setTypePath("ThirdEntity/optionalDetails/optionalUbounded3"); //$NON-NLS-1$
        itemNodeModel.setValid(true);
        itemNodeModel.setBlankValid(true);
        itemNodeModel.setMandatory(false);
        itemNodeModel.setObjectValue("2017-02-02");

        ItemNodeModel itemNodeModel2 = new ItemNodeModel();
        itemNodeModel2.setName("optionalUbounded3"); //$NON-NLS-1$
        itemNodeModel2.setTypePath("ThirdEntity/optionalDetails/optionalUbounded3"); //$NON-NLS-1$
        itemNodeModel2.setValid(false);
        itemNodeModel2.setBlankValid(true);
        itemNodeModel2.setMandatory(false);
        itemNodeModel2.setObjectValue("2017-02-33");

        ItemNodeModel parent = new ItemNodeModel();
        parent.setName("optionalDetails"); //$NON-NLS-1$
        parent.setTypePath("ThirdEntity/optionalDetails"); //$NON-NLS-1$
        parent.add(itemNodeModel);
        itemNodeModel.setParent(parent);
        itemNodeModel2.setParent(parent);

        ItemNodeModel father = new ItemNodeModel();
        father.setName("ThirdEntity"); //$NON-NLS-1$
        father.setTypePath("ThirdEntity"); //$NON-NLS-1$
        father.add(parent);
        parent.setParent(father);

        DynamicTreeItem item = new DynamicTreeItem();
        item.setItemNodeModel(itemNodeModel);

        DynamicTreeItem item2 = new DynamicTreeItem();
        item2.setItemNodeModel(itemNodeModel2);

        DynamicTreeItem parentItem = new DynamicTreeItem();
        parentItem.addItem(item);
        parentItem.addItem(item2);

        DynamicTreeItem fatherItem = new DynamicTreeItem();
        fatherItem.addItem(parentItem);

        MultiOccurrenceManager manager = new MultiOccurrenceManager(metaDataTypes, treeDetail);
        manager.addMultiOccurrenceNode(item);

        multiNodes = _getMultiOccurrence(manager, "ThirdEntity/optionalDetails[1]/optionalUbounded3"); //$NON-NLS-1$
        assertEquals(1, multiNodes.size());
        assertEquals("optionalUbounded3", multiNodes.get(0).getItemNodeModel().getName()); //$NON-NLS-1$
        assertTrue(multiNodes.get(0).getItemNodeModel().isValid()); //$NON-NLS-1$
        manager.addMultiOccurrenceNode(item2);

        multiNodes = _getMultiOccurrence(manager, "ThirdEntity/optionalDetails[1]/optionalUbounded3"); //$NON-NLS-1$
        assertEquals(2, multiNodes.size());
        assertFalse(multiNodes.get(1).getItemNodeModel().isValid()); //$NON-NLS-1$

        manager.warningBrothers(multiNodes.get(0).getItemNodeModel());
        assertTrue(multiNodes.get(0).getItemNodeModel().isValid()); //$NON-NLS-1$
        assertFalse(multiNodes.get(1).getItemNodeModel().isValid()); //$NON-NLS-1$
    }

    private native List<DynamicTreeItem> _getMultiOccurrence(MultiOccurrenceManager manager, String xpath)/*-{
		return manager.@org.talend.mdm.webapp.browserecords.client.util.MultiOccurrenceManager::getBrothersGroup(Ljava/lang/String;)(xpath);
    }-*/;

    @Override
    public String getModuleName() {
        // GWTTestCase Required
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }
}
