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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsGWTTest;
import org.talend.mdm.webapp.browserecords.client.handler.ItemTreeHandler;
import org.talend.mdm.webapp.browserecords.client.handler.ItemTreeHandlingStatus;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatDateField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatNumberField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatTextField;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailGridFieldCreator;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailUtil;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.DOM;

@SuppressWarnings("nls")
public class ItemNodeModelGWTTest extends BrowseRecordsGWTTest {

    private String name = "Test";

    private String description = "Test";

    private String dynamicLabel = "TestDynamicLabel";

    private String label = "TestLabel";

    private String typePath = "Product/Name";

    private String objectValue = "TestClone";

    private boolean mandatory;

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
    }
    
    public void testClone() {

        ItemNodeModel nodeModel = new ItemNodeModel(name);
        nodeModel.setDescription(description);
        nodeModel.setDynamicLabel(dynamicLabel);
        nodeModel.setLabel(label);
        nodeModel.setTypePath(typePath);
        nodeModel.setObjectValue(objectValue);
        nodeModel.setMandatory(mandatory);

        // 1. withValue = true;
        ItemNodeModel clonedModel = nodeModel.clone(true);
        assertEquals(name, clonedModel.getName());
        assertEquals(description, clonedModel.getDescription());
        assertEquals(dynamicLabel, clonedModel.getDynamicLabel());
        assertEquals(label, clonedModel.getLabel());
        assertEquals(typePath, clonedModel.getTypePath());
        assertEquals(objectValue, clonedModel.getObjectValue());
        assertEquals(mandatory, clonedModel.isMandatory());

        // 2. withValue = false, mandatory = true
        nodeModel.setMandatory(true);
        clonedModel = nodeModel.clone(false);
        assertEquals(name, clonedModel.getName());
        assertEquals(description, clonedModel.getDescription());
        assertEquals(dynamicLabel, clonedModel.getDynamicLabel());
        assertEquals(label, clonedModel.getLabel());
        assertEquals(typePath, clonedModel.getTypePath());
        assertEquals(null, clonedModel.getObjectValue());
        assertEquals(true, clonedModel.isMandatory());

        // 3. set children
        ItemNodeModel childOne = new ItemNodeModel("childOne");
        ItemNodeModel childTwo = new ItemNodeModel("childTwo");
        ItemNodeModel childThree = new ItemNodeModel("childThree");
        nodeModel.add(childOne);
        nodeModel.add(childTwo);
        nodeModel.add(childThree);
        clonedModel = nodeModel.clone(true);
        assertEquals(3, clonedModel.getChildCount());
        assertEquals("childOne", clonedModel.getChild(0).get("name"));

    }

    /**
     * test node mandatory <br>
     * example: <br>
     * --root <br>
     * ---|_Id(1...1)<br>
     * ---|_ComplexType(0...many)<br>
     * ----------|_field1(1...1)<br>
     * ----------|_field2(1...1)<br>
     * ----------|_field3(0...1)
     */
    public void testNodeMandatory() {
        Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();

        ItemNodeModel root = new ItemNodeModel("root");
        ItemNodeModel id = new ItemNodeModel("Id");
        id.setKey(true);
        id.setMandatory(true);
        root.add(id);
        fieldMap.put(id.getId().toString(), new FormatTextField());

        ItemNodeModel complexType = new ItemNodeModel("ComplexType");
        root.add(complexType);

        ItemNodeModel field1 = new ItemNodeModel("field1");
        field1.setMandatory(true);
        Field<?> field = new FormatTextField();
        field.render(DOM.createElement("field1"));
        fieldMap.put(field1.getId().toString(), field);
        addFieldListener(field, field1, fieldMap);

        ItemNodeModel field2 = new ItemNodeModel("field2");
        field2.setMandatory(true);
        field = new FormatTextField();
        field.render(DOM.createElement("field2"));
        fieldMap.put(field2.getId().toString(), field);
        addFieldListener(field, field2, fieldMap);

        ItemNodeModel field3 = new ItemNodeModel("field3");
        field3.setValid(true);
        field = new FormatTextField();
        field.render(DOM.createElement("field3"));
        fieldMap.put(field3.getId().toString(), field);
        addFieldListener(field, field3, fieldMap);

        complexType.add(field1);
        complexType.add(field2);
        complexType.add(field3);

        // 1. field1 = null, field2 = null, field3 = null
        TreeDetailGridFieldCreator.updateMandatory(fieldMap.get(field1.getId().toString()), field1, fieldMap);
        assertEquals(true, field1.isValid());
        assertEquals(true, field2.isValid());
        assertEquals(true, field3.isValid());

        // 2. field1 is not null, field2 = null, field3 = null
        field1.setObjectValue("field1");
        field = fieldMap.get(field1.getId().toString());
        field.setRawValue(field1.getObjectValue().toString());
        FieldEvent fe = new FieldEvent(field);
        field.fireEvent(Events.Change, fe);
        field.fireEvent(Events.Blur);
        assertEquals(true, field1.isValid());
        assertEquals(false, field2.isValid());
        assertEquals(true, field3.isValid());
        assertEquals("field1", field1.getObjectValue());

        // 3. field2 is not null, field1 = null, field3 = null
        field1.setObjectValue(null);
        field.setValue(null);
        field2.setObjectValue("field2");
        field = fieldMap.get(field2.getId().toString());
        field.setRawValue(field2.getObjectValue().toString());
        field.fireEvent(Events.Change);
        field.fireEvent(Events.Blur);
        assertEquals(false, field1.isValid());
        assertEquals(true, field2.isValid());
        assertEquals(true, field3.isValid());
        assertEquals("field2", field2.getObjectValue());

        // 4. field3 is not null, field1 = null, field2 = null
        field2.setObjectValue(null);
        field3.setObjectValue("field3");
        field.setValue(null);
        field = fieldMap.get(field3.getId().toString());
        field.setRawValue(field3.getObjectValue().toString());
        field.validate();
        field.fireEvent(Events.Change);
        field.fireEvent(Events.Blur);
        assertEquals(false, field1.isValid());
        assertEquals(false, field2.isValid());
        assertEquals(true, field3.isValid());
        assertEquals("field3", field3.getObjectValue());

    }

    public void test_isChangeValue() {
        ComplexTypeModel root = new ComplexTypeModel("root", DataTypeConstants.STRING);
        root.addDescription("en", "root");
        TypeModel idModel = new SimpleTypeModel("id", DataTypeConstants.LONG);
        idModel.addDescription("en", "id");
        root.addSubType(idModel);
        TypeModel nameModel = new SimpleTypeModel("name", DataTypeConstants.STRING);
        nameModel.addDescription("en", "name");
        nameModel.setDefaultValueExpression("test");
        nameModel.setDefaultValue("Hello");
        root.addSubType(nameModel);
        List<ItemNodeModel> list = CommonUtil.getDefaultTreeModel(root, "en", true, false, false);
        ItemNodeModel rootNode = list.get(0);
        assertTrue(TreeDetailUtil.isChangeValue(rootNode));
    }

    private static void addFieldListener(final Field<?> field, final ItemNodeModel node, final Map<String, Field<?>> fieldMap) {

        field.addListener(Events.Change, new Listener<FieldEvent>() {

            public void handleEvent(FieldEvent fe) {
                node.setChangeValue(true);
                TreeDetailGridFieldCreator.updateMandatory(field, node, fieldMap);
            }
        });

        field.addListener(Events.Blur, new Listener<FieldEvent>() {

            public void handleEvent(FieldEvent fe) {
                // TMDM-3353 only when node is valid, call setObjectValue(); otherwise objectValue is changed to
                // original value
                if (node.isValid())
                    if (fe.getField() instanceof FormatTextField) {
                        node.setObjectValue(((FormatTextField) fe.getField()).getOjbectValue());
                    } else if (fe.getField() instanceof FormatNumberField) {
                        node.setObjectValue(((FormatNumberField) fe.getField()).getOjbectValue());
                    } else if (fe.getField() instanceof FormatDateField) {
                        node.setObjectValue(((FormatDateField) fe.getField()).getOjbectValue());
                    }
            }
        });
    }

    public void test_getCurrentConceptName() {

        // 1. Selected Item is Product
        String currentXpath = "Product/parent";
        assertEquals("Product", currentXpath.split("/")[0]);

        // 2. Selected Item is Family
        currentXpath = "ProductFamily/parent";
        assertEquals("ProductFamily", currentXpath.split("/")[0]);

        // 3. Selected Item is Product/Features/parent
        currentXpath = "Product/Features/parent";
        assertEquals("Product", currentXpath.split("/")[0]);

    }

    public void testClearNodeValue() {
        ViewBean viewBean = new ViewBean();
        EntityModel entity = CommonUtilTestData.getEntityModel(ClientResourceData.getModelH());
        viewBean.setBindingEntityModel(entity);
        ItemNodeModel nodeModel = CommonUtilTestData.getItemNodeModel(ClientResourceData.getRecordH(), entity);
        ItemTreeHandler itemHandler = new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave);
        String xml = itemHandler.serializeItem();
        assertEquals(
                "<Test xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>1</id><name>test</name><cp><title>Hello</title><address>Beijing</address><fk>North Five Loop</fk><cpChild><tel>13800000000</tel></cpChild></cp></Test>", xml); //$NON-NLS-1$
        ItemNodeModel cpModel = (ItemNodeModel) nodeModel.getChild(2);
        assertEquals("cp", cpModel.getName());
        // Clear cpModel all of leaf nodes value
        cpModel.clearNodeValue();
        itemHandler = new ItemTreeHandler(nodeModel, viewBean, ItemTreeHandlingStatus.ToSave);
        xml = itemHandler.serializeItem();
        assertEquals(
                "<Test xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>1</id><name>test</name><cp><title/><address/><fk/><cpChild><tel/></cpChild></cp></Test>", xml); //$NON-NLS-1$
    }

}