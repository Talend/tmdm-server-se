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
import org.talend.mdm.webapp.browserecords.client.model.OperatorConstants;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.BooleanField;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;


public class MiscTypeFieldFactory extends TypeFieldFactory {

    public MiscTypeFieldFactory() {

    }

    public MiscTypeFieldFactory(TypeFieldSource source, TypeFieldCreateContext context) {
        super(source, context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.mdm.webapp.base.client.widget.typefield.TypeFieldFactory#createField()
     */
    @Override
    public Field<?> createField() {
        Field<?> field = null;

        if (DataTypeConstants.BOOLEAN.getTypeName().equals(baseType)) {

            if (source.getName().equals(TypeFieldSource.FORM_INPUT)) {

                CheckBox checkBox = new CheckBox();
                if (context.isWithValue()) {
                    checkBox.setValue(hasValue() ? ((getValue().toString().equals("true") || getValue().equals(true)) ? true : false) : null); //$NON-NLS-1$
                }

                field = checkBox;

            } else if (source.getName().equals(TypeFieldSource.CELL_EDITOR)) {

                BooleanField booleanField = new BooleanField();
                booleanField.setDisplayField("text");//$NON-NLS-1$
                booleanField.getStore().add(new SimpleComboValue<Boolean>() {

                    {
                        this.setValue(true);
                        this.set("text", "TRUE");}});//$NON-NLS-1$ //$NON-NLS-2$
                booleanField.getStore().add(new SimpleComboValue<Boolean>() {

                    {
                        this.setValue(false);
                        this.set("text", "FALSE");}});//$NON-NLS-1$ //$NON-NLS-2$

                field = booleanField;

            }

        } else if (DataTypeConstants.BASE64BINARY.getTypeName().equals(baseType)
                || DataTypeConstants.HEXBINARY.getTypeName().equals(baseType)) {

            field = genFormatTextField();

        }

        return field;
    }

    @Override
    public Field<?> createSearchField() {
        Field<?> field = null;
        if (baseType.equals(DataTypeConstants.BOOLEAN.getBaseTypeName())) {
            final CheckBox checkBox = new CheckBox();
            checkBox.setBoxLabel("False"); //$NON-NLS-1$
            checkBox.addListener(Events.Change, new Listener<BaseEvent>() {

                public void handleEvent(BaseEvent be) {
                    checkBox.setBoxLabel(checkBox.getValue() ? " True" : "False"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            });
            field = checkBox;
            source.setOperatorMap(OperatorConstants.booleanOperators);
        } else {
            field = genTextSearchField();
        }
        return field;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldFactory#updateStyle()
     */
    @Override
    public void updateStyle(Field<?> field) {
        updateBuiltInTypeFiledsStyle(field);
    }
}
