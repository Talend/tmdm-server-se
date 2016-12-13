/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.typefield;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatDateField;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.RootPanel;

@SuppressWarnings("nls")
public class DateTimeTypeFieldFactoryGWTTest extends GWTTestCase{
	
    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
    }
    
	public void testCreateFormatDateField() {
		
		// 1. TypeModel DataType = DataTypeConstants.DATE, value is valid
		boolean isDateTime = false;
		TypeModel typeModel = new SimpleTypeModel("date", DataTypeConstants.DATE);
		TypeFieldCreateContext context = new TypeFieldCreateContext(typeModel);
		context.setWithValue(true);
		context.setNode(getItemNodeModel("date", "2012-06-13", "date"));
		DateTimeTypeFieldFactory dateTimeTypeFieldFactory = new DateTimeTypeFieldFactory(new TypeFieldSource(TypeFieldSource.FORM_INPUT), context);
		Field<?> field = dateTimeTypeFieldFactory.createFormatDateField(isDateTime);
		RootPanel.get().add(field);
		assertTrue(field instanceof FormatDateField);
		FormatDateField dateField = (FormatDateField) field;
		assertNotNull(dateField.getValue());
		assertEquals("2012-06-13", dateField.getPropertyEditor().getStringValue(dateField.getValue()));
		assertEquals(true, dateField.isValid());
		
		// 2. TypeModel DataType = DataTypeConstants.DATE, value is invalid
		context.setNode(getItemNodeModel("date", "2012/06/13", "date"));
		dateTimeTypeFieldFactory = new DateTimeTypeFieldFactory(new TypeFieldSource(TypeFieldSource.FORM_INPUT), context);
		field = dateTimeTypeFieldFactory.createFormatDateField(isDateTime);
		RootPanel.get().add(field);
		assertTrue(field instanceof FormatDateField);
		dateField = (FormatDateField) field;
		assertNull(dateField.getValue());
        assertEquals(true, dateField.isValid());
		
		// 3. TypeModel DataType = DataTypeConstants.DATETIME, value is valid
		isDateTime = true;
		typeModel = new SimpleTypeModel("dateTime", DataTypeConstants.DATETIME);
		context = new TypeFieldCreateContext(typeModel);
		context.setWithValue(true);
		context.setNode(getItemNodeModel("dateTime", "2012-06-13T10:08:08", "dateTime"));
		dateTimeTypeFieldFactory = new DateTimeTypeFieldFactory(new TypeFieldSource(TypeFieldSource.FORM_INPUT), context);
		field = dateTimeTypeFieldFactory.createFormatDateField(isDateTime);
		RootPanel.get().add(field);
		assertTrue(field instanceof FormatDateField);
		dateField = (FormatDateField) field;
		assertNotNull(dateField.getValue());
		assertEquals("2012-06-13T10:08:08", dateField.getPropertyEditor().getStringValue(dateField.getValue()));
		assertEquals(true, dateField.isValid());
		
		// 4. TypeModel DataType = DataTypeConstants.DATETIME, value is invalid
		isDateTime = true;
		context.setNode(getItemNodeModel("dateTime", "2012/06/13T10:08:08", "dateTime"));
		dateTimeTypeFieldFactory = new DateTimeTypeFieldFactory(new TypeFieldSource(TypeFieldSource.FORM_INPUT), context);
		field = dateTimeTypeFieldFactory.createFormatDateField(isDateTime);
		RootPanel.get().add(field);
		assertTrue(field instanceof FormatDateField);
		dateField = (FormatDateField) field;
        assertNull(dateField.getValue());
        assertEquals(true, dateField.isValid());

        // 5. customer type
        DataTypeConstants.UNKNOW.setBaseTypeName("date");
        typeModel = new SimpleTypeModel("date", DataTypeConstants.UNKNOW);
        assertEquals("unknow", typeModel.getType().getTypeName());
        assertEquals("date", typeModel.getType().getBaseTypeName());
        context.setNode(getItemNodeModel("date", "2012/06/13", "date"));
        dateTimeTypeFieldFactory = new DateTimeTypeFieldFactory(new TypeFieldSource(TypeFieldSource.FORM_INPUT), context);
        field = dateTimeTypeFieldFactory.createField();
        assertNotNull(field);
	}
	
	private ItemNodeModel getItemNodeModel(String name, String value, String label){
		ItemNodeModel node = new ItemNodeModel(name);
		node.setObjectValue(value);
		node.setLabel(label);
		return node;
	}
	
	public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords";
    }
}
