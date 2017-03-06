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
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.SubTypeBean;
import org.talend.mdm.webapp.base.shared.AppHeader;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.junit.client.GWTTestCase;

public class MultiOccurrenceChangeItemGWTTest extends GWTTestCase {

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
    }

    public void testMultiOccurrenceFieldLabel() {

        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.setTypePath("T/sub"); //$NON-NLS-1$
        itemNodeModel.setLabel("sub"); //$NON-NLS-1$

        ViewBean viewBean = new ViewBean();
        EntityModel entityModel = new EntityModel();
        LinkedHashMap<String, TypeModel> metaDataTypes = new LinkedHashMap<String, TypeModel>();
        TypeModel typeModel = new SimpleTypeModel("sub", DataTypeConstants.STRING); //$NON-NLS-1$
        Map<String, String> labelMap = new HashMap<String, String>();
        // labelMap.put("en", "sub");
        typeModel.setLabelMap(labelMap);
        typeModel.setMinOccurs(1);
        typeModel.setMaxOccurs(-1);
        typeModel.getType();
        metaDataTypes.put("T/sub", typeModel); //$NON-NLS-1$
        entityModel.setMetaDataTypes(metaDataTypes);
        viewBean.setBindingEntityModel(entityModel);
        Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();

        MultiOccurrenceChangeItem multiOccurrenceChangeItem = new MultiOccurrenceChangeItem(itemNodeModel, viewBean, fieldMap,
                null, null);
        assertEquals("<div class=\"gwt-HTML\">sub<span style=\"color:red\"> *</span></div>", //$NON-NLS-1$
                multiOccurrenceChangeItem.getWidget(0).toString());

        typeModel.setMinOccurs(0);
        multiOccurrenceChangeItem = new MultiOccurrenceChangeItem(itemNodeModel, viewBean, fieldMap, null, null);
        assertEquals("<div class=\"gwt-HTML\">sub</div>", //$NON-NLS-1$
                multiOccurrenceChangeItem.getWidget(0).toString());

        typeModel.setMinOccurs(1);
        typeModel.setMaxOccurs(1);
        multiOccurrenceChangeItem = new MultiOccurrenceChangeItem(itemNodeModel, viewBean, fieldMap, null, null);
        assertEquals("<div class=\"gwt-HTML\">sub<span style=\"color:red\"> *</span></div>", //$NON-NLS-1$
                multiOccurrenceChangeItem.getWidget(0).toString());
    }

    public void testClearMultiOccurrenceItemValue() {
        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.setTypePath("T/sub"); //$NON-NLS-1$
        itemNodeModel.setLabel("sub"); //$NON-NLS-1$
        itemNodeModel.setObjectValue("TestNodeValue"); //$NON-NLS-1$
        ViewBean viewBean = new ViewBean();
        EntityModel entityModel = new EntityModel();
        LinkedHashMap<String, TypeModel> metaDataTypes = new LinkedHashMap<String, TypeModel>();
        TypeModel typeModel = new SimpleTypeModel("sub", DataTypeConstants.STRING); //$NON-NLS-1$
        Map<String, String> labelMap = new HashMap<String, String>();
        typeModel.setLabelMap(labelMap);
        typeModel.setMinOccurs(-1);
        typeModel.setMaxOccurs(-1);
        typeModel.getType();
        metaDataTypes.put("T/sub", typeModel); //$NON-NLS-1$
        entityModel.setMetaDataTypes(metaDataTypes);
        viewBean.setBindingEntityModel(entityModel);
        Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();
        MultiOccurrenceChangeItem multiOccurrenceChangeItem = new MultiOccurrenceChangeItem(itemNodeModel, viewBean, fieldMap,
                null, null);
        multiOccurrenceChangeItem.clearValue();
        assertEquals(null, itemNodeModel.getObjectValue());
        assertEquals(true, itemNodeModel.isChangeValue());
    }

    public void testMassUpdateMultiOccurrenceItem() {
        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.setTypePath("T/sub"); //$NON-NLS-1$
        itemNodeModel.setLabel("sub"); //$NON-NLS-1$
        itemNodeModel.setObjectValue("TestNodeValue"); //$NON-NLS-1$
        ViewBean viewBean = new ViewBean();
        EntityModel entityModel = new EntityModel();
        LinkedHashMap<String, TypeModel> metaDataTypes = new LinkedHashMap<String, TypeModel>();
        TypeModel typeModel = new SimpleTypeModel("sub", DataTypeConstants.STRING); //$NON-NLS-1$
        Map<String, String> labelMap = new HashMap<String, String>();
        typeModel.setLabelMap(labelMap);
        typeModel.setMinOccurs(-1);
        typeModel.setMaxOccurs(-1);
        typeModel.getType();
        typeModel.setParentTypeModel(new SimpleTypeModel("T", DataTypeConstants.STRING)); //$NON-NLS-1$)
        metaDataTypes.put("T/sub", typeModel); //$NON-NLS-1$
        entityModel.setMetaDataTypes(metaDataTypes);
        viewBean.setBindingEntityModel(entityModel);
        Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();
        MultiOccurrenceChangeItem multiOccurrenceChangeItem = new MultiOccurrenceChangeItem(itemNodeModel, viewBean, fieldMap,
                ItemDetailToolBar.BULK_UPDATE_OPERATION, null);
        String result = multiOccurrenceChangeItem.getWidget(4).toString();
        assertTrue(result.contains("src=\"secure/img/genericUI/bulkupdate.png\""));
        assertTrue(result.contains("id=\"Edit\""));
        assertTrue(result.contains("title=\"Bulk Update\""));
        assertEquals(true, itemNodeModel.isMassUpdate());
        assertEquals(false, itemNodeModel.isEdited());

        itemNodeModel.setKey(true);
        multiOccurrenceChangeItem = new MultiOccurrenceChangeItem(itemNodeModel, viewBean, fieldMap,
                ItemDetailToolBar.BULK_UPDATE_OPERATION, null);
        result = multiOccurrenceChangeItem.getWidget(4).toString();
        assertFalse(result.contains("src=\"secure/img/genericUI/bulkupdate.png\""));
        assertEquals(true, itemNodeModel.isMassUpdate());
        assertEquals(true, itemNodeModel.isEdited());

        itemNodeModel.setKey(false);
        typeModel.setReadOnly(true);
        multiOccurrenceChangeItem = new MultiOccurrenceChangeItem(itemNodeModel, viewBean, fieldMap,
                ItemDetailToolBar.BULK_UPDATE_OPERATION, null);
        result = multiOccurrenceChangeItem.getWidget(4).toString();
        assertFalse(result.contains("src=\"secure/img/genericUI/bulkupdate.png\""));
        assertEquals(true, itemNodeModel.isMassUpdate());
        assertEquals(true, itemNodeModel.isEdited());

        itemNodeModel.setKey(false);
        typeModel.setForeignKeyFilter("Product/Name='5'");
        multiOccurrenceChangeItem = new MultiOccurrenceChangeItem(itemNodeModel, viewBean, fieldMap,
                ItemDetailToolBar.BULK_UPDATE_OPERATION, null);
        result = multiOccurrenceChangeItem.getWidget(4).toString();
        assertFalse(result.contains("src=\"secure/img/genericUI/bulkupdate.png\""));
        assertEquals(true, itemNodeModel.isMassUpdate());
        assertEquals(true, itemNodeModel.isEdited());
    }

    public void testMassUpdateMultiOccurrenceItemForComplexType() {
        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.setTypePath("Citizen/identity"); //$NON-NLS-1$
        itemNodeModel.setLabel("identity"); //$NON-NLS-1$
        itemNodeModel.setObjectValue("identity"); //$NON-NLS-1$
        itemNodeModel.setMassUpdate(true);
        itemNodeModel.setParent(new ItemNodeModel("Citizen"));
        ViewBean viewBean = new ViewBean();
        EntityModel entityModel = new EntityModel();
        LinkedHashMap<String, TypeModel> metaDataTypes = new LinkedHashMap<String, TypeModel>();
        TypeModel identityTypeModel = new ComplexTypeModel("identity", DataTypeConstants.ENTITY); //$NON-NLS-1$
        Map<String, String> labelMap = new HashMap<String, String>();
        identityTypeModel.setLabelMap(labelMap);
        identityTypeModel.setMinOccurs(-1);
        identityTypeModel.setMaxOccurs(-1);
        identityTypeModel.getType();
        metaDataTypes.put("Citizen/identity", identityTypeModel); //$NON-NLS-1$
        entityModel.setMetaDataTypes(metaDataTypes);
        viewBean.setBindingEntityModel(entityModel);
        Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();
        MultiOccurrenceChangeItem multiOccurrenceChangeItem = new MultiOccurrenceChangeItem(itemNodeModel, viewBean, fieldMap,
                ItemDetailToolBar.BULK_UPDATE_OPERATION, null);
        String result = multiOccurrenceChangeItem.getWidget(1).toString();
        assertTrue(result.contains("src=\"secure/img/genericUI/add.png\""));
        assertTrue(result.contains("id=\"Add\""));
        assertTrue(result.contains("title=\"Add an occurrence\""));
        result = multiOccurrenceChangeItem.getWidget(2).toString();
        assertTrue(result.contains("src=\"secure/img/genericUI/delete.png\""));
        assertTrue(result.contains("id=\"Remove\""));
        assertTrue(result.contains("title=\"Delete an occurrence\""));
        result = multiOccurrenceChangeItem.getWidget(3).toString();
        assertTrue(result.contains("src=\"secure/img/genericUI/add-group.png\""));
        assertTrue(result.contains("id=\"Clone\""));
        assertTrue(result.contains("title=\"Clone this occurrence\""));
        result = multiOccurrenceChangeItem.getWidget(4).toString();
        assertFalse(result.contains("src=\"secure/img/genericUI/bulkupdate.png\""));
        assertEquals(true, itemNodeModel.isMassUpdate());
        assertEquals(true, itemNodeModel.isEdited());
    }

    public void testMassUpdateMultiOccurrenceItemForReusableComplexType() {
        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.setTypePath("Citizen"); //$NON-NLS-1$
        itemNodeModel.setLabel("Citizen"); //$NON-NLS-1$
        itemNodeModel.setObjectValue("Citizen"); //$NON-NLS-1$
        itemNodeModel.setMassUpdate(true);
        ViewBean viewBean = new ViewBean();
        EntityModel entityModel = new EntityModel();
        LinkedHashMap<String, TypeModel> metaDataTypes = new LinkedHashMap<String, TypeModel>();
        TypeModel citizenTypeModel = new ComplexTypeModel("Citizen", DataTypeConstants.ENTITY); //$NON-NLS-1$
        Map<String, String> labelMap = new HashMap<String, String>();
        citizenTypeModel.setLabelMap(labelMap);
        citizenTypeModel.setMinOccurs(-1);
        citizenTypeModel.setMaxOccurs(-1);
        citizenTypeModel.getType();
        citizenTypeModel.setParentTypeModel(null);
        ArrayList<SubTypeBean> subTypes = new ArrayList<SubTypeBean>();
        subTypes.add(new SubTypeBean());
        citizenTypeModel.setReusableTypes(subTypes);
        metaDataTypes.put("Citizen", citizenTypeModel); //$NON-NLS-1$
        entityModel.setMetaDataTypes(metaDataTypes);
        viewBean.setBindingEntityModel(entityModel);
        Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();
        MultiOccurrenceChangeItem multiOccurrenceChangeItem = new MultiOccurrenceChangeItem(itemNodeModel, viewBean, fieldMap,
                ItemDetailToolBar.BULK_UPDATE_OPERATION, null);
        String result = multiOccurrenceChangeItem.getWidget(1).toString();
        assertTrue(result.contains("src=\"secure/img/genericUI/add.png\""));
        assertTrue(result.contains("id=\"Add\""));
        assertTrue(result.contains("title=\"Add an occurrence\""));
        result = multiOccurrenceChangeItem.getWidget(2).toString();
        assertTrue(result.contains("src=\"secure/img/genericUI/delete.png\""));
        assertTrue(result.contains("id=\"Remove\""));
        assertTrue(result.contains("title=\"Delete an occurrence\""));
        result = multiOccurrenceChangeItem.getWidget(3).toString();
        assertEquals(true, itemNodeModel.isMassUpdate());
        assertEquals(true, itemNodeModel.isEdited());
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }
}
