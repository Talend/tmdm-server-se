/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield.creator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.widget.MultiLanguageField;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeyField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.MultipleField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.SimpleComboBoxField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.converter.BooleanConverter;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.converter.DateConverter;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.converter.DateTimeConverter;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.converter.FKConverter;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.converter.MultiLanguageConverter;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.converter.NumberConverter;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailGridFieldCreator;
import org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldCreateContext;
import org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldCreator;
import org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldSource;
import org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldStyle;

import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.binding.SimpleComboBoxFieldBinding;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.google.gwt.user.client.ui.Widget;

public class FieldCreator {

    public static Field<?> createField(SimpleTypeModel dataType, FormBinding formBindings, boolean enableMultiple, String language) {
        Field<?> field;
        if (dataType.isMultiOccurrence() && enableMultiple) {
            MultipleField multipleField = new MultipleField(dataType, language);
            field = multipleField;
        } else if (dataType.getForeignkey() != null) {
            ForeignKeyField foreignKeyField = new ForeignKeyField(dataType);
            field = foreignKeyField;
        } else if (dataType.hasEnumeration()) {
            SimpleComboBoxField<String> comboBox = new SimpleComboBoxField<String>();
            comboBox.setFireChangeEventOnSetValue(true);
            comboBox.setDisplayField("text"); //$NON-NLS-1$
            if (dataType.getMinOccurs() > 0) {
                comboBox.setAllowBlank(false);
            }
            comboBox.setEditable(false);
            comboBox.setForceSelection(true);
            comboBox.setTriggerAction(TriggerAction.ALL);
            comboBox.setTemplate(TreeDetailGridFieldCreator.getTemplate());
            setEnumerationValues(dataType, comboBox);
            field = comboBox;
        } else {
            TypeFieldCreateContext context = new TypeFieldCreateContext(dataType);
            context.setLanguage(language);
            TypeFieldCreator typeFieldCreator = new TypeFieldCreator(new TypeFieldSource(TypeFieldSource.CELL_EDITOR), context);
            Map<String, TypeFieldStyle> sytles = new HashMap<String, TypeFieldStyle>();
            sytles.put(TypeFieldStyle.ATTRI_WIDTH, new TypeFieldStyle(TypeFieldStyle.ATTRI_WIDTH,
                    "400", TypeFieldStyle.SCOPE_BUILTIN_TYPEFIELD)); //$NON-NLS-1$
            field = typeFieldCreator.createFieldWithUpdateStyle(sytles);
        }

        field.setFieldLabel(dataType.getLabel(language));
        field.setName(dataType.getXpath());

        if (formBindings != null) {
            FieldBinding binding = null;
            if (field instanceof SimpleComboBox) {
                binding = new SimpleComboBoxFieldBinding((SimpleComboBox) field, field.getName());
                String baseType = dataType.getType().getBaseTypeName();
                if (DataTypeConstants.BOOLEAN.getTypeName().equals(baseType)) {
                    binding.setConverter(new BooleanConverter());
                }
            } else if (field instanceof DateField) {
                binding = new FieldBinding(field, field.getName());
                binding.setConverter(new DateConverter());
            } else if (field instanceof DateField) {
                binding = new FieldBinding(field, field.getName());
                binding.setConverter(new DateTimeConverter());
            } else if (field instanceof NumberField) {
                binding = new FieldBinding(field, field.getName());
                binding.setConverter(new NumberConverter(field));
            } else if (field instanceof ForeignKeyField) {
                binding = new FieldBinding(field, field.getName());
                binding.setConverter(new FKConverter());
            } else if (field instanceof MultiLanguageField) {
                binding = new FieldBinding(field, field.getName());
                binding.setConverter(new MultiLanguageConverter());
            } else {
                binding = new FieldBinding(field, field.getName());
            }
            formBindings.addFieldBinding(binding);
        }

        field.setReadOnly(dataType.isReadOnly());
        field.setEnabled(!dataType.isReadOnly());

        return field;
    }

    private static void setEnumerationValues(TypeModel typeModel, Widget w) {
        List<String> enumeration = ((SimpleTypeModel) typeModel).getEnumeration();
        if (enumeration != null && enumeration.size() > 0) {
            SimpleComboBox<String> field = (SimpleComboBox<String>) w;
            if (typeModel.getMinOccurs() <= 0) {
                field.getStore().add(new SimpleComboValue<String>() {

                    {
                        this.set("text", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        this.setValue(""); //$NON-NLS-1$
                    }
                });
            }
            for (final String value : enumeration) {
                field.getStore().add(new SimpleComboValue<String>() {

                    {
                        this.set("text", value); //$NON-NLS-1$
                        this.setValue(value);
                    }
                });
            }
        }
    }
}
