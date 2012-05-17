// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.browserecords.client.widget.typefield;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.PictureField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.UrlField;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;

public class CustomTypeFieldFactory extends TypeFieldFactory {

    private boolean isMandatory;
    
    public CustomTypeFieldFactory() {

    }

    public CustomTypeFieldFactory(TypeFieldSource source, TypeFieldCreateContext context, boolean isMandatory) {
        super(source, context);
        this.isMandatory = isMandatory;
    }

    public CustomTypeFieldFactory(TypeFieldSource source, TypeFieldCreateContext context) {
        this(source, context, false);
    }

    
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.mdm.webapp.base.client.widget.typefield.TypeFieldFactory#createField()
     */
    @Override
    public Field<?> createField() {
        Field<?> field = null;
        if (context.getDataType().getType().equals(DataTypeConstants.UUID)) {
            TextField<String> uuidField = new TextField<String>();
            uuidField.setEnabled(false);
            uuidField.setReadOnly(true);
            if (context.isWithValue() && hasValue())
                uuidField.setValue(getValue().toString());
            field = uuidField;
        } else if (context.getDataType().getType().equals(DataTypeConstants.AUTO_INCREMENT)) {
            TextField<String> autoIncrementField = new TextField<String>();
            autoIncrementField.setEnabled(false);
            autoIncrementField.setReadOnly(true);
            if (context.isWithValue() && hasValue()) {
                autoIncrementField.setValue(getValue().toString());
            } else {
                autoIncrementField.setValue(MessagesFactory.getMessages().auto());
            }
            field = autoIncrementField;
        } else if (context.getDataType().getType().equals(DataTypeConstants.PICTURE)) {
            PictureField pictureField = new PictureField(isMandatory);
            if (context.isWithValue())
                pictureField.setValue(hasValue() ? getValue().toString() : ""); //$NON-NLS-1$
            field = pictureField;
        } else if (context.getDataType().getType().equals(DataTypeConstants.URL)) {
            UrlField urlField = new UrlField();
            urlField.setFieldLabel(context.getDataType().getLabel(context.getLanguage()));
            if (context.isWithValue())
                urlField.setValue(hasValue() ? getValue().toString() : ""); //$NON-NLS-1$
            field = urlField;
        }
        return field;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldFactory#createSearchField()
     */
    @Override
    public Field<?> createSearchField() {
        return genTextSearchField();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldFactory#updateStyle()
     */
    @Override
    public void updateStyle(Field<?> field) {
        // do nothing
    }

}
