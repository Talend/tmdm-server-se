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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.DataTypeCustomized;
import org.talend.mdm.webapp.base.client.widget.MultiLanguageField;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.FacetModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.model.OperatorConstants;
import org.talend.mdm.webapp.browserecords.client.util.DateUtil;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.BooleanField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatDateField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatNumberField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatTextAreaField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatTextField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.PictureField;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.FacetEnum;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.junit.client.GWTTestCase;

@SuppressWarnings("nls")
public class TypeFieldCreatorGWTTest extends GWTTestCase {

    private String language = "en";

    private TypeModel nameNodeModel = new SimpleTypeModel("name", DataTypeConstants.STRING);

    private TypeModel descriptionNodeModel = new SimpleTypeModel("description", DataTypeConstants.MLS);

    private TypeModel priceNodeModel = new SimpleTypeModel("price", DataTypeConstants.DOUBLE);

    private TypeModel publishTimeNodeModel = new SimpleTypeModel("publishTime", DataTypeConstants.DATETIME);

    private TypeModel isAvailableNodeModel = new SimpleTypeModel("isAvailable", DataTypeConstants.BOOLEAN);

    private TypeModel durationNodeModel = new SimpleTypeModel("duration", DataTypeConstants.DURATION);

    private TypeModel featureNodeModel = new ComplexTypeModel("feature", new DataTypeCustomized("FeatureType", "anyType"));

    private TypeModel pictureNodeModel = new SimpleTypeModel("picture", DataTypeConstants.PICTURE);

    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);

    }

    public void testInputArguments() {
        try {
            TypeFieldCreator typeFieldCreator = new TypeFieldCreator();
            typeFieldCreator.createField();
        } catch (IllegalArgumentException iae) {
            assertTrue(true);
        }
    }

    public void testCreateField4Form() {

        Field<?> createdField = null;

        String nameValue = "IPad2";
        String descriptionValue = "[EN:123][FR:456]";
        double priceValue = 3499.99;
        String publishTimeValue = "2010-01-01T00:00:00";
        boolean isAvaliableValue = true;
        String durationValue = "-P120D";

        ItemNodeModel nameNode = new ItemNodeModel("Name");
        nameNode.setKey(true);
        nameNode.setObjectValue(nameValue);

        ItemNodeModel descriptionNode = new ItemNodeModel("Description");
        descriptionNode.setObjectValue(descriptionValue);

        ItemNodeModel priceNode = new ItemNodeModel("Price");
        priceNode.setObjectValue(priceValue);

        ItemNodeModel publishTimeNode = new ItemNodeModel("publishTime");
        publishTimeNode.setObjectValue(publishTimeValue);

        ItemNodeModel isAvailableNode = new ItemNodeModel("isAvailable");
        isAvailableNode.setObjectValue(isAvaliableValue);

        ItemNodeModel durationNode = new ItemNodeModel("duration");
        durationNode.setObjectValue(durationValue);

        TypeFieldCreateContext context = new TypeFieldCreateContext();
        context.setLanguage(language);
        context.setAutoTextAreaLength(30);
        TypeFieldCreator typeFieldCreator = new TypeFieldCreator(new TypeFieldSource(TypeFieldSource.FORM_INPUT), context);
        Map<String, TypeFieldStyle> sytles = new HashMap<String, TypeFieldStyle>();
        sytles.put(TypeFieldStyle.ATTRI_WIDTH, new TypeFieldStyle(TypeFieldStyle.ATTRI_WIDTH, "400",
                TypeFieldStyle.SCOPE_BUILTIN_TYPEFIELD)); //$NON-NLS-1$
        context.setDataType(nameNodeModel);
        createdField = typeFieldCreator.createFieldWithValueAndUpdateStyle(nameNode, sytles);
        assertNotNull(createdField);
        assertTrue(createdField instanceof FormatTextField);
        assertEquals(nameValue, createdField.getValue());
        
        nameNode.setObjectValue(makeRandomString(71));
        createdField = typeFieldCreator.createFieldWithValueAndUpdateStyle(nameNode, sytles);
        assertNotNull(createdField);
        assertTrue(createdField instanceof FormatTextAreaField);

        context.setDataType(descriptionNodeModel);
        createdField = typeFieldCreator.createFieldWithValueAndUpdateStyle(descriptionNode, sytles);
        assertNotNull(createdField);
        assertTrue(createdField instanceof MultiLanguageField);
        assertEquals(descriptionValue, ((MultiLanguageField) createdField).getMultiLanguageStringValue());

        context.setDataType(priceNodeModel);
        createdField = typeFieldCreator.createFieldWithValueAndUpdateStyle(priceNode, sytles);
        assertNotNull(createdField);
        assertTrue(createdField instanceof FormatNumberField);
        assertEquals(priceValue, createdField.getValue());

        context.setDataType(publishTimeNodeModel);
        createdField = typeFieldCreator.createFieldWithValueAndUpdateStyle(publishTimeNode, sytles);
        assertNotNull(createdField);
        assertTrue(createdField instanceof FormatDateField);
        assertEquals(publishTimeValue, DateUtil.convertDateToString((Date) createdField.getValue()));

        context.setDataType(isAvailableNodeModel);
        createdField = typeFieldCreator.createFieldWithValueAndUpdateStyle(isAvailableNode, sytles);
        assertNotNull(createdField);
        assertTrue(createdField instanceof CheckBox);
        assertEquals(isAvaliableValue, createdField.getValue());

        context.setDataType(durationNodeModel);
        createdField = typeFieldCreator.createFieldWithValueAndUpdateStyle(durationNode, sytles);
        assertNotNull(createdField);
        assertTrue(createdField instanceof FormatTextField);
        assertEquals(durationValue, createdField.getValue());

        context.setDataType(featureNodeModel);
        createdField = typeFieldCreator.createFieldWithValueAndUpdateStyle(new ItemNodeModel("feature"), sytles);
        assertNull(createdField);

        context.setDataType(pictureNodeModel);
        createdField = typeFieldCreator.createFieldWithValueAndUpdateStyle(null, sytles);
        assertNotNull(createdField);
        assertTrue(createdField instanceof PictureField);
        assertEquals("", createdField.getValue());

    }

    public void testCreateField4Cell() {

        Field<?> createdField = null;

        TypeFieldCreateContext context = new TypeFieldCreateContext(isAvailableNodeModel);
        context.setLanguage(language);
        TypeFieldCreator typeFieldCreator = new TypeFieldCreator(new TypeFieldSource(TypeFieldSource.CELL_EDITOR), context);
        createdField = typeFieldCreator.createFieldWithUpdateStyle(null);

        assertNotNull(createdField);
        assertTrue(createdField instanceof BooleanField);
        assertEquals(createdField.getValue(), null);

        List<FacetModel> facets = new ArrayList<FacetModel>();
        FacetModel facet = new FacetModel("maxInclusive", "10000");
        facets.add(facet);
        ((SimpleTypeModel) priceNodeModel).setFacets(facets);
        context.setDataType(priceNodeModel);
        Map<String, TypeFieldStyle> sytles = new HashMap<String, TypeFieldStyle>();
        sytles.put(TypeFieldStyle.ATTRI_WIDTH, new TypeFieldStyle(TypeFieldStyle.ATTRI_WIDTH, "400",
                TypeFieldStyle.SCOPE_BUILTIN_TYPEFIELD));
        createdField = typeFieldCreator.createFieldWithUpdateStyle(sytles);
        assertNotNull(createdField);
        assertTrue(createdField instanceof FormatNumberField);
        assertEquals("400px", getWidthFromField(createdField));
        assertEquals("10000", ((String) createdField.getData(FacetEnum.MAX_INCLUSIVE.getFacetName())));

    }

    public void testCreateField4Search() {

        Field<?> createdField = null;

        TypeFieldCreateContext context = new TypeFieldCreateContext(nameNodeModel);
        TypeFieldSource typeFieldSource = new TypeFieldSource(TypeFieldSource.SEARCH_EDITOR);
        TypeFieldCreator typeFieldCreator = new TypeFieldCreator(typeFieldSource, context);
        createdField = typeFieldCreator.createField();
        assertNotNull(createdField);
        assertTrue(createdField instanceof TextField);
        assertEquals(createdField.getValue(), "*");
        assertEquals(typeFieldSource.getOperatorMap(), OperatorConstants.fullOperators);

        context.setDataType(priceNodeModel);
        createdField = typeFieldCreator.createField();
        assertNotNull(createdField);
        assertTrue(createdField instanceof TextField);
        assertEquals(createdField.getValue(), "*");
        assertEquals(typeFieldSource.getOperatorMap(), OperatorConstants.numOperators);

        context.setDataType(pictureNodeModel);
        createdField = typeFieldCreator.createField();
        assertNotNull(createdField);
        assertTrue(createdField instanceof TextField);
        assertEquals(typeFieldSource.getOperatorMap(), OperatorConstants.fullOperators);

    }

    public static String makeRandomString(int length) {
        // a-zA-Z0-9
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rand = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = rand.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    private native String getWidthFromField(Field<?> field)/*-{
        return field.@com.extjs.gxt.ui.client.widget.BoxComponent::width;
    }-*/;

}
