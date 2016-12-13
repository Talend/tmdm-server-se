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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.shared.FacetModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatNumberField;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.FacetEnum;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.RootPanel;

@SuppressWarnings("nls")
public class NumberTypeFieldFactoryGWTTest extends GWTTestCase {

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
    }
    
    public void testCreateField(){
        // beacuse GWT getValue method can't use the BigDecimal, all the BigDecimal use the Double to deal with.
        TypeModel typeModel = new SimpleTypeModel("price", DataTypeConstants.DECIMAL);
        TypeFieldCreateContext context = new TypeFieldCreateContext(typeModel);
        context.setWithValue(true);
        context.setNode(getItemNodeModel("price", "12.90", "decimalValue"));
        
        NumberTypeFieldFactory dateTimeTypeFieldFactory = new NumberTypeFieldFactory(new TypeFieldSource(
                TypeFieldSource.FORM_INPUT), context);
        Field<?> field = dateTimeTypeFieldFactory.createField();
        RootPanel.get().add(field);
        assertTrue(field instanceof FormatNumberField);
        FormatNumberField dateField = (FormatNumberField) field;
        assertNotNull(dateField.getValue());
        assertEquals("12.90", dateField.getPropertyEditor().getStringValue(dateField.getValue()));
        assertTrue(field.getValue() instanceof BigDecimal);
        assertEquals(true, dateField.isValid());
        
        
        List<FacetModel> facets = new ArrayList<FacetModel>();
        facets.add(new FacetModel(FacetEnum.TOTAL_DIGITS.getFacetName(), "15"));
        facets.add(new FacetModel(FacetEnum.FRACTION_DIGITS.getFacetName(), "3"));
        ((SimpleTypeModel) typeModel).setFacets(facets);
        dateTimeTypeFieldFactory = new NumberTypeFieldFactory(new TypeFieldSource(
                TypeFieldSource.FORM_INPUT), context);
        field = dateTimeTypeFieldFactory.createField();
        RootPanel.get().add(field);
        assertTrue(field instanceof FormatNumberField);
        dateField = (FormatNumberField) field;
        assertNotNull(dateField.getValue());
        assertEquals("12.90", dateField.getPropertyEditor().getStringValue(dateField.getValue()));
        assertTrue(field.getValue() instanceof BigDecimal);
        assertEquals(true, dateField.isValid());

        typeModel = new SimpleTypeModel("price", DataTypeConstants.FLOAT);
        context = new TypeFieldCreateContext(typeModel);
        context.setWithValue(true);
        context.setNode(getItemNodeModel("price", "12.9", "floatValue"));
        dateTimeTypeFieldFactory = new NumberTypeFieldFactory(new TypeFieldSource(TypeFieldSource.FORM_INPUT), context);
        field = dateTimeTypeFieldFactory.createField();
        RootPanel.get().add(field);
        assertTrue(field instanceof FormatNumberField);
        dateField = (FormatNumberField) field;
        assertNotNull(dateField.getValue());
        assertEquals("12.9", dateField.getPropertyEditor().getStringValue(dateField.getValue()));
        assertEquals(true, dateField.isValid());
        assertTrue(field.getValue() instanceof Float);

        typeModel = new SimpleTypeModel("price", DataTypeConstants.DOUBLE);
        context = new TypeFieldCreateContext(typeModel);
        context.setWithValue(true);
        context.setNode(getItemNodeModel("price", "12.9", "decimalValue"));
        dateTimeTypeFieldFactory = new NumberTypeFieldFactory(new TypeFieldSource(TypeFieldSource.FORM_INPUT), context);
        field = dateTimeTypeFieldFactory.createField();
        RootPanel.get().add(field);
        assertTrue(field instanceof FormatNumberField);
        dateField = (FormatNumberField) field;
        assertNotNull(dateField.getValue());
        assertEquals("12.9", dateField.getPropertyEditor().getStringValue(dateField.getValue()));
        assertEquals(true, dateField.isValid());
        assertTrue(field.getValue() instanceof Double);
    }
    
    private ItemNodeModel getItemNodeModel(String name, String value, String label){
        ItemNodeModel node = new ItemNodeModel(name);
        node.setObjectValue(value);
        node.setLabel(label);
        return node;
    }
    
    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords";
    }

}
