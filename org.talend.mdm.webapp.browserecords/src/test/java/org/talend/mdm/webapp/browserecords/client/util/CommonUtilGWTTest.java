/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.junit.client.GWTTestCase;

public class CommonUtilGWTTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }

    public void testGetCountOfBrotherOfTheSameName() {
        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelG());
        viewBean.setBindingEntityModel(entity);
        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordG(), entity);

        assertEquals(CommonUtil.getCountOfBrotherOfTheSameName(nodeModel), 1);

        ItemNodeModel subElement = (ItemNodeModel) nodeModel.getChild(0);
        assertEquals(CommonUtil.getCountOfBrotherOfTheSameName(subElement), 1);

        ItemNodeModel name = (ItemNodeModel) nodeModel.getChild(1);
        assertEquals(CommonUtil.getCountOfBrotherOfTheSameName(name), 1);

        ItemNodeModel age = (ItemNodeModel) nodeModel.getChild(2);
        assertEquals(CommonUtil.getCountOfBrotherOfTheSameName(age), 1);

        ItemNodeModel memo1 = (ItemNodeModel) nodeModel.getChild(3);
        assertEquals(CommonUtil.getCountOfBrotherOfTheSameName(memo1), 4);

        ItemNodeModel memo2 = (ItemNodeModel) nodeModel.getChild(4);
        assertEquals(CommonUtil.getCountOfBrotherOfTheSameName(memo2), 4);

        ItemNodeModel memo3 = (ItemNodeModel) nodeModel.getChild(5);
        assertEquals(CommonUtil.getCountOfBrotherOfTheSameName(memo3), 4);

        ItemNodeModel memo4 = (ItemNodeModel) nodeModel.getChild(6);
        assertEquals(CommonUtil.getCountOfBrotherOfTheSameName(memo4), 4);

        ItemNodeModel cp = (ItemNodeModel) nodeModel.getChild(7);
        assertEquals(CommonUtil.getCountOfBrotherOfTheSameName(cp), 1);

        ItemNodeModel title = (ItemNodeModel) cp.getChild(0);
        assertEquals(CommonUtil.getCountOfBrotherOfTheSameName(title), 1);

        ItemNodeModel address1 = (ItemNodeModel) cp.getChild(1);
        assertEquals(CommonUtil.getCountOfBrotherOfTheSameName(address1), 3);

        ItemNodeModel address2 = (ItemNodeModel) cp.getChild(2);
        assertEquals(CommonUtil.getCountOfBrotherOfTheSameName(address2), 3);

        ItemNodeModel address3 = (ItemNodeModel) cp.getChild(3);
        assertEquals(CommonUtil.getCountOfBrotherOfTheSameName(address3), 3);
    }

    public void testHasChildrenValue() {
        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelG());
        viewBean.setBindingEntityModel(entity);
        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordG(), entity);

        assertEquals(CommonUtil.hasChildrenValue(nodeModel), true);

        ItemNodeModel subElement = (ItemNodeModel) nodeModel.getChild(0);
        assertEquals(CommonUtil.hasChildrenValue(subElement), true);

        ItemNodeModel name = (ItemNodeModel) nodeModel.getChild(1);
        assertEquals(CommonUtil.hasChildrenValue(name), true);

        ItemNodeModel age = (ItemNodeModel) nodeModel.getChild(2);
        assertEquals(CommonUtil.hasChildrenValue(age), false);

        ItemNodeModel memo1 = (ItemNodeModel) nodeModel.getChild(3);
        assertEquals(CommonUtil.hasChildrenValue(memo1), false);

        ItemNodeModel memo2 = (ItemNodeModel) nodeModel.getChild(4);
        assertEquals(CommonUtil.hasChildrenValue(memo2), true);

        ItemNodeModel memo3 = (ItemNodeModel) nodeModel.getChild(5);
        assertEquals(CommonUtil.hasChildrenValue(memo3), false);

        ItemNodeModel memo4 = (ItemNodeModel) nodeModel.getChild(6);
        assertEquals(CommonUtil.hasChildrenValue(memo4), true);

        ItemNodeModel cp = (ItemNodeModel) nodeModel.getChild(7);
        assertEquals(CommonUtil.hasChildrenValue(cp), false);

        ItemNodeModel title = (ItemNodeModel) cp.getChild(0);
        assertEquals(CommonUtil.hasChildrenValue(title), false);

        ItemNodeModel address1 = (ItemNodeModel) cp.getChild(1);
        assertEquals(CommonUtil.hasChildrenValue(address1), false);

        ItemNodeModel address2 = (ItemNodeModel) cp.getChild(2);
        assertEquals(CommonUtil.hasChildrenValue(address2), false);

        ItemNodeModel address3 = (ItemNodeModel) cp.getChild(3);
        assertEquals(CommonUtil.hasChildrenValue(address3), false);
    }

    public void testGetRealXpathWithout() {
        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelG());
        viewBean.setBindingEntityModel(entity);
        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordG(), entity);

        assertEquals(CommonUtil.getRealXPath(nodeModel), "Test"); //$NON-NLS-1$

        ItemNodeModel subElement = (ItemNodeModel) nodeModel.getChild(0);
        assertEquals(CommonUtil.getRealXPath(subElement), "Test/subelement[1]"); //$NON-NLS-1$
        assertEquals(CommonUtil.getRealXpathWithoutLastIndex(subElement), "Test/subelement"); //$NON-NLS-1$

        ItemNodeModel name = (ItemNodeModel) nodeModel.getChild(1);
        assertEquals(CommonUtil.getRealXPath(name), "Test/name[1]"); //$NON-NLS-1$
        assertEquals(CommonUtil.getRealXpathWithoutLastIndex(name), "Test/name"); //$NON-NLS-1$

        ItemNodeModel age = (ItemNodeModel) nodeModel.getChild(2);
        assertEquals(CommonUtil.getRealXPath(age), "Test/age[1]"); //$NON-NLS-1$
        assertEquals(CommonUtil.getRealXpathWithoutLastIndex(age), "Test/age"); //$NON-NLS-1$

        ItemNodeModel memo1 = (ItemNodeModel) nodeModel.getChild(3);
        assertEquals(CommonUtil.getRealXPath(memo1), "Test/memo[1]"); //$NON-NLS-1$
        assertEquals(CommonUtil.getRealXpathWithoutLastIndex(memo1), "Test/memo"); //$NON-NLS-1$

        ItemNodeModel memo2 = (ItemNodeModel) nodeModel.getChild(4);
        assertEquals(CommonUtil.getRealXPath(memo2), "Test/memo[2]"); //$NON-NLS-1$
        assertEquals(CommonUtil.getRealXpathWithoutLastIndex(memo2), "Test/memo"); //$NON-NLS-1$

        ItemNodeModel memo3 = (ItemNodeModel) nodeModel.getChild(5);
        assertEquals(CommonUtil.getRealXPath(memo3), "Test/memo[3]"); //$NON-NLS-1$
        assertEquals(CommonUtil.getRealXpathWithoutLastIndex(memo3), "Test/memo"); //$NON-NLS-1$

        ItemNodeModel memo4 = (ItemNodeModel) nodeModel.getChild(6);
        assertEquals(CommonUtil.getRealXPath(memo4), "Test/memo[4]"); //$NON-NLS-1$
        assertEquals(CommonUtil.getRealXpathWithoutLastIndex(memo4), "Test/memo"); //$NON-NLS-1$

        ItemNodeModel cp = (ItemNodeModel) nodeModel.getChild(7);
        assertEquals(CommonUtil.getRealXPath(cp), "Test/cp[1]"); //$NON-NLS-1$
        assertEquals(CommonUtil.getRealXpathWithoutLastIndex(cp), "Test/cp"); //$NON-NLS-1$

        ItemNodeModel title = (ItemNodeModel) cp.getChild(0);
        assertEquals(CommonUtil.getRealXPath(title), "Test/cp[1]/title[1]"); //$NON-NLS-1$
        assertEquals(CommonUtil.getRealXpathWithoutLastIndex(title), "Test/cp[1]/title"); //$NON-NLS-1$

        ItemNodeModel address1 = (ItemNodeModel) cp.getChild(1);
        assertEquals(CommonUtil.getRealXPath(address1), "Test/cp[1]/address[1]"); //$NON-NLS-1$
        assertEquals(CommonUtil.getRealXpathWithoutLastIndex(address1), "Test/cp[1]/address"); //$NON-NLS-1$

        ItemNodeModel address2 = (ItemNodeModel) cp.getChild(2);
        assertEquals(CommonUtil.getRealXPath(address2), "Test/cp[1]/address[2]"); //$NON-NLS-1$
        assertEquals(CommonUtil.getRealXpathWithoutLastIndex(address2), "Test/cp[1]/address"); //$NON-NLS-1$

        ItemNodeModel address3 = (ItemNodeModel) cp.getChild(3);
        assertEquals(CommonUtil.getRealXPath(address3), "Test/cp[1]/address[3]"); //$NON-NLS-1$
        assertEquals(CommonUtil.getRealXpathWithoutLastIndex(address3), "Test/cp[1]/address"); //$NON-NLS-1$
    }

    @SuppressWarnings("nls")
    public void testGetDefaultTreeModel() {
        // 1. build TypeModel
        ComplexTypeModel oem = new ComplexTypeModel("oem", DataTypeConstants.STRING);
        oem.setTypePath("oem");
        oem.setLabelMap(new HashMap<String, String>());
        SimpleTypeModel oem_a = new SimpleTypeModel("oem_a", DataTypeConstants.STRING);
        oem_a.setTypePath("oem/oem_a");
        oem_a.setHide(true) ;
        oem_a.setVisible(false) ;
        oem_a.setHasVisibleRule(true) ;
        oem_a.setLabelMap(new HashMap<String, String>());
        oem.addSubType(oem_a);
        SimpleTypeModel oem_b = new SimpleTypeModel("oem_b", DataTypeConstants.INTEGER);
        oem_b.setTypePath("oem/oem_b");
        oem_b.setLabelMap(new HashMap<String, String>());
        oem_b.setHide(false) ;
        oem_b.setVisible(true) ;
        oem_b.setHasVisibleRule(true) ;
        oem.addSubType(oem_b);
        SimpleTypeModel oem_c = new SimpleTypeModel("oem_c", DataTypeConstants.BOOLEAN);
        oem_c.setTypePath("oem/oem_c");
        oem_c.setLabelMap(new HashMap<String, String>());
        oem_c.setHide(false) ;
        oem_c.setVisible(true) ;
        oem_c.setHasVisibleRule(false) ;
        oem.addSubType(oem_c);
        SimpleTypeModel oem_d = new SimpleTypeModel("oem_d", DataTypeConstants.DATE);
        oem_d.setTypePath("oem/oem_d");
        oem_d.setLabelMap(new HashMap<String, String>());
        oem.addSubType(oem_d);
        SimpleTypeModel oem_e = new SimpleTypeModel("oem_e", DataTypeConstants.DOUBLE);
        oem_e.setTypePath("oem/oem_e");
        oem_e.setLabelMap(new HashMap<String, String>());
        oem.addSubType(oem_e);
        DataTypeConstants.UNKNOW.setBaseTypeName("date");
        SimpleTypeModel oem_f = new SimpleTypeModel("oem_f", DataTypeConstants.UNKNOW);
        oem_f.setTypePath("oem/oem_f");
        oem_f.setLabelMap(new HashMap<String, String>());
        oem.addSubType(oem_f);
        // 2. Test defaultValue = true
        List<ItemNodeModel> list = CommonUtil.getDefaultTreeModel(oem, "en", true, false, false);
        assertNotNull(list);
        ItemNodeModel oemNodeModel = list.get(0);
        assertNotNull(oemNodeModel);
        assertEquals(6, oemNodeModel.getChildCount());
        ItemNodeModel oem_a_node = (ItemNodeModel) oemNodeModel.getChild(0);
        assertEquals(DataTypeConstants.STRING.getDefaultValue(), oem_a_node.getObjectValue());
        ItemNodeModel oem_b_node = (ItemNodeModel) oemNodeModel.getChild(1);
        assertEquals(DataTypeConstants.INTEGER.getDefaultValue(), oem_b_node.getObjectValue());
        ItemNodeModel oem_c_node = (ItemNodeModel) oemNodeModel.getChild(2);
        assertEquals(DataTypeConstants.BOOLEAN.getDefaultValue(), oem_c_node.getObjectValue());
        ItemNodeModel oem_d_node = (ItemNodeModel) oemNodeModel.getChild(3);
        assertTrue(oem_d_node.getObjectValue() != null);
        assertTrue(oem_d_node.getObjectValue() instanceof String);
        ItemNodeModel oem_e_node = (ItemNodeModel) oemNodeModel.getChild(4);
        assertEquals(DataTypeConstants.DOUBLE.getDefaultValue(), oem_e_node.getObjectValue());
        ItemNodeModel oem_f_node = (ItemNodeModel) oemNodeModel.getChild(5);
        assertEquals(DateUtil.getDate((Date) DataTypeConstants.DATE.getDefaultValue()), oem_f_node.getObjectValue());
        // 3. Test defaultValue = false
        list = CommonUtil.getDefaultTreeModel(oem, "en", false, false, false);
        assertNotNull(list);
        oemNodeModel = list.get(0);
        assertNotNull(oemNodeModel);
        assertEquals(6, oemNodeModel.getChildCount());
        for (ModelData modelData : oemNodeModel.getChildren()) {
            ItemNodeModel itemNodeModel = (ItemNodeModel) modelData;
            assertTrue(itemNodeModel.getObjectValue() == null);
        }
        // 4. Test visible, hide, and hasVisibleRule
        list = CommonUtil.getDefaultTreeModel(oem, "en", false, false, false);
        assertNotNull(list);
        oemNodeModel = list.get(0);
        assertNotNull(oemNodeModel);
        assertEquals(6, oemNodeModel.getChildCount());
        for (ModelData modelData : oemNodeModel.getChildren()) {
            ItemNodeModel itemNodeModel = (ItemNodeModel) modelData;
            if(itemNodeModel.getName().equals("oem_a")){
                assertTrue(itemNodeModel.isHide()) ;
                assertTrue(itemNodeModel.isHasVisiblueRule()) ;
                assertFalse(itemNodeModel.isVisible()) ;
            }
            if(itemNodeModel.getName().equals("oem_b")){
                assertFalse(itemNodeModel.isHide()) ;
                assertTrue(itemNodeModel.isHasVisiblueRule()) ;
                assertTrue(itemNodeModel.isVisible()) ;
            }
            if(itemNodeModel.getName().equals("oem_c")){
                assertFalse(itemNodeModel.isHide()) ;
                assertFalse(itemNodeModel.isHasVisiblueRule()) ;
                assertTrue(itemNodeModel.isVisible()) ;
            }
        }
    }

    public void testConvertList2Xml() {
        List<String> list = new ArrayList<String>();
        list.add("Id"); //$NON-NLS-1$
        list.add("Name"); //$NON-NLS-1$
        list.add("Family"); //$NON-NLS-1$
        list.add("Price"); //$NON-NLS-1$
        list.add("Availability"); //$NON-NLS-1$
        String xml = "<header><item>Id</item><item>Name</item><item>Family</item><item>Price</item><item>Availability</item></header>"; //$NON-NLS-1$        
        assertEquals(xml,CommonUtil.convertList2Xml(list, "header")); //$NON-NLS-1$
    }
}
