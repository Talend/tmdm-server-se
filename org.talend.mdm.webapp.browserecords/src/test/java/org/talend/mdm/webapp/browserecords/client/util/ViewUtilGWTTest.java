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
import java.util.List;

import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsGWTTest;

@SuppressWarnings("nls")
public class ViewUtilGWTTest extends BrowseRecordsGWTTest {
	
	public void testGetDefaultSmartViewModel() {
		List<ItemBaseModel> list = new ArrayList<ItemBaseModel>();		
		ItemBaseModel defalutModel = new ItemBaseModel();
		defalutModel.set("key", "Smart_view_Product");
		
		ItemBaseModel optionModel1 = new ItemBaseModel();
		optionModel1.set("key", "Smart_view_Product#Bekcham");
		
		ItemBaseModel optionModel2 = new ItemBaseModel();
		optionModel2.set("key", "Smart_view_Product#Owen");
		
		ItemBaseModel modeLang = new ItemBaseModel();
		modeLang.set("key", "Smart_view_Product_en");
		
		list.add(defalutModel);
		list.add(optionModel2);
		list.add(optionModel1);
		list.add(modeLang);
		
		ItemBaseModel val = ViewUtil.getDefaultSmartViewModel(list, "Product");
		assertEquals(val, defalutModel);

		list.clear();
		list.add(optionModel1);
		list.add(optionModel2);
		list.add(modeLang);
		
		val = ViewUtil.getDefaultSmartViewModel(list, "Product");
		assertEquals(val, modeLang);
		
		list.clear();
		list.add(optionModel1);
		list.add(optionModel2);
		
		val = ViewUtil.getDefaultSmartViewModel(list, "Product");
		assertEquals(val, optionModel1);
	}
}
