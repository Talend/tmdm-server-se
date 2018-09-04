/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield.creator;

import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.OperatorConstants;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeyField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.SimpleComboBoxField;
import org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldCreateContext;
import org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldCreator;
import org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldSource;

import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.Widget;

public class SearchFieldCreator {

    public static Map<String, String> cons;

    public static Field<?> createField(TypeModel typeModel, EntityModel entityModel) {
        Field<?> field = null;
        // when the search condition is no element of entity,the typeModel is null.For example search element/@xsi:type
        // equals something
        if (typeModel == null) {
            TextField<String> textField = new TextField<String>();
            textField.setValue("*");//$NON-NLS-1$
            field = textField;
            cons = OperatorConstants.stringOperators;
        } else if (typeModel.getForeignkey() != null && entityModel != null) {
            boolean isCompositeKey = entityModel.getKeys().length > 1;
            if (isCompositeKey) {
                field = createForeignKeyField(typeModel);
            } else {
                String keyPath = entityModel.getKeys()[0];
                TypeModel keyTypeModel = entityModel.getTypeModel(keyPath);
                boolean isString = DataTypeConstants.STRING.getTypeName().equals(keyTypeModel.getType().getTypeName());
                boolean isAutoIncrement = DataTypeConstants.AUTO_INCREMENT.getTypeName()
                        .equals(keyTypeModel.getType().getTypeName());
                boolean isUUID = DataTypeConstants.UUID.getTypeName().equals(keyTypeModel.getType().getTypeName());
                if (isString || isAutoIncrement || isUUID) {
                    TextField<String> textField = new TextField<String>();
                    textField.setValue("*");//$NON-NLS-1$
                    field = textField;
                    cons = OperatorConstants.stringOperators;
                } else {
                    field = createForeignKeyField(typeModel);
                }
            }
        } else if (typeModel.hasEnumeration()) {
            SimpleComboBoxField<String> comboBox = new SimpleComboBoxField<String>();
            comboBox.setFireChangeEventOnSetValue(true);
            if (typeModel.getMinOccurs() > 0) {
                comboBox.setAllowBlank(false);
            }
            comboBox.setEditable(false);
            comboBox.setForceSelection(true);
            comboBox.setTriggerAction(TriggerAction.ALL);
            setEnumerationValues(typeModel, comboBox);
            field = comboBox;
            cons = OperatorConstants.enumOperators;
        } else if (typeModel instanceof ComplexTypeModel) {
            TextField<String> textField = new TextField<String>();
            textField.setValue("*");//$NON-NLS-1$
            field = textField;
            cons = OperatorConstants.fulltextOperators;
        } else {
            TypeFieldCreateContext context = new TypeFieldCreateContext(typeModel);
            TypeFieldSource typeFieldSource = new TypeFieldSource(TypeFieldSource.SEARCH_EDITOR);
            TypeFieldCreator typeFieldCreator = new TypeFieldCreator(typeFieldSource, context);
            field = typeFieldCreator.createField();
            cons = typeFieldSource.getOperatorMap();
        }
        return field;
    }

    private static Field createForeignKeyField(TypeModel typeModel) {
        ForeignKeyField fkField = new ForeignKeyField(typeModel);
        fkField.setUsageField("SearchFieldCreator"); //$NON-NLS-1$
        cons = OperatorConstants.foreignKeyOperators;
        return fkField;
    }

    private static void setEnumerationValues(TypeModel typeModel, Widget w) {
        List<String> enumeration = ((SimpleTypeModel) typeModel).getEnumeration();
        if (enumeration != null && enumeration.size() > 0) {
            SimpleComboBox<String> field = (SimpleComboBox<String>) w;
            for (String value : enumeration) {
                field.add(value);
            }
        }
    }

}
