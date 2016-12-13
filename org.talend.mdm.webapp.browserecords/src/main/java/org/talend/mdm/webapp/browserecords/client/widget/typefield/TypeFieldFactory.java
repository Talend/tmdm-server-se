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

import java.io.Serializable;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.widget.MultiLanguageField;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.OperatorConstants;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatTextAreaField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatTextField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.validator.TextFieldValidator;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class TypeFieldFactory implements IsSerializable {

    protected TypeFieldSource source;

    protected TypeFieldCreateContext context;

    protected String baseType;

    protected String displayformatPattern;

    public TypeFieldFactory() {

    }

    public TypeFieldFactory(TypeFieldSource source, TypeFieldCreateContext context) {
        super();
        this.source = source;
        this.context = context;

        // parse context
        baseType = this.context.getDataType().getType().getBaseTypeName();
        if (this.context.getDataType().getDisplayFomats() != null) {
            displayformatPattern = this.context.getDataType().getDisplayFomats().get("format_" + Locale.getLanguage()); //$NON-NLS-1$
        }
    }

    protected boolean isEmpty(String input) {

        return (input == null || input.trim().length() == 0);

    }

    /**
     * DOC Administrator Comment method "getValue".
     */
    protected Serializable getValue() {

        if (context.getNode() == null)
            return null;

        return context.getNode().getObjectValue();

    }

    /**
     * DOC Administrator Comment method "hasValue".
     */
    protected boolean hasValue() {
        boolean has = false;

        if (context.getNode() != null) {
            has = context.getNode().getObjectValue() != null && !"".equals(context.getNode().getObjectValue()); //$NON-NLS-1$
        }

        return has;

    }

    /**
     * DOC Administrator Comment method "genFormatTextField".
     * 
     * @return
     */
    protected Field<?> genFormatTextField() {
        Field<?> field;
        FormatTextField textField = new FormatTextField();

        // auto switch text area
        if (source != null && source.getName().equals(TypeFieldSource.FORM_INPUT)) {
            if (hasValue() && getValue().toString().length() > context.getAutoTextAreaLength()) {
                textField = new FormatTextAreaField();
            }
        }

        if (!isEmpty(displayformatPattern)) {
            textField.setFormatPattern(displayformatPattern);
        }

        textField.setValidator(TextFieldValidator.getInstance());

        // clear message
        if (source != null && source.getName().equals(TypeFieldSource.CELL_EDITOR))
            textField.setMessages(null);

        // set value
        if (context.isWithValue()) {
            textField.setValue(hasValue() ? getValue().toString() : ""); //$NON-NLS-1$
        }

        field = textField;
        return field;
    }

    /**
     * DOC Administrator Comment method "genTextSearchField".
     * 
     * @return
     */
    protected Field<?> genTextSearchField() {
        Field<?> field;
        TextField<String> textField = new TextField<String>();
        if (context.getDataType().getType().equals(DataTypeConstants.MLS)) {
            textField = new MultiLanguageField(false, BrowseRecords.getSession().getAppHeader().getUserProperties());
            source.setOperatorMap(OperatorConstants.multiLanguageOperators);
        } else {
            // TODO Text should use stringOperators
            source.setOperatorMap(OperatorConstants.fullOperators);
        }
        textField.setValue("*");//$NON-NLS-1$
        field = textField;
        return field;
    }

    public abstract Field<?> createField();

    public abstract Field<?> createSearchField();

    public abstract void updateStyle(Field<?> field);

    protected void updateBuiltInTypeFiledsStyle(Field<?> field) {
        Map<String, TypeFieldStyle> styles = context.getTypeFieldStyles();
        if (styles == null)
            return;
        if (styles != null && styles.size() > 0) {
            for (String key : styles.keySet()) {
                TypeFieldStyle typeFieldStyle = styles.get(key);
                if (typeFieldStyle.getScope().equals(TypeFieldStyle.SCOPE_BUILTIN_TYPEFIELD)) {
                    if (key.equals(TypeFieldStyle.ATTRI_WIDTH) && typeFieldStyle.getValue() != null)
                        field.setWidth(Integer.parseInt(typeFieldStyle.getValue()));
                }
            }
        }

    }

}
