/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield;

import java.util.HashMap;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.model.FormatModel;
import org.talend.mdm.webapp.browserecords.client.util.Locale;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

public class FormatTextField extends TextField<String> {

    BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);

    public static final String KEY_MDM_READ_ONLY_FIELD_STYLE = "MDM_READ_ONLY_FIELD_STYLE"; //$NON-NLS-1$

    private String formatPattern;

    private String ojbectValue;

    private String diplayValue;

    private boolean validateFlag = true;

    private Boolean editable = true;

    private Element textFieldDisable;

    private HashMap<String, String> userProperties;

    public FormatTextField() {
        super();
        validateFlag = BrowseRecords.getSession().getAppHeader().isAutoValidate();
        setUserProperties(BrowseRecords.getSession().getAppHeader().getUserProperties());
    }

    public String getFormatPattern() {
        return formatPattern;
    }

    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    public String getOjbectValue() {
        return ojbectValue;
    }

    public void setOjbectValue(String ojbectValue) {
        this.ojbectValue = ojbectValue;
    }

    public String getDiplayValue() {
        return diplayValue;
    }

    public void setDiplayValue(String diplayValue) {
        this.diplayValue = diplayValue;
    }

    @SuppressWarnings("hiding")
    @Override
    public boolean validateValue(String value) {
        if (value.equals(this.getDiplayValue())) {
            this.fireEvent(Events.Change);
            return true;
        } else {
            if (!validateFlag) {
                return true;
            }
            boolean result = super.validateValue(value);
            if (!result) {
                return false;
            }
            int length = value.length();
            if (value.length() < 1 || value.equals("")) { //$NON-NLS-1$
                this.setOjbectValue(null);
                if (super.getAllowBlank()) {
                    clearInvalid();
                    return true;
                } else {
                    markInvalid(getMessages().getBlankText());
                    return false;
                }
            }
            if (length < super.getMinLength()) {
                String error = ""; //$NON-NLS-1$
                if (getMessages().getMinLengthText() == null) {
                    error = GXT.MESSAGES.textField_minLengthText(super.getMinLength());
                } else {
                    error = Format.substitute(getMessages().getMinLengthText(), super.getMinLength());
                }
                markInvalid(error);
                return false;
            }

            if (length > super.getMaxLength()) {
                String error = ""; //$NON-NLS-1$
                if (getMessages().getMaxLengthText() == null) {
                    error = GXT.MESSAGES.textField_maxLengthText(super.getMaxLength());
                } else {
                    error = Format.substitute(getMessages().getMaxLengthText(), super.getMaxLength());
                }
                markInvalid(error);
                return false;
            }

            if (validator != null) {
                String msg = validator.validate(this, value);
                if (msg != null) {
                    markInvalid(msg);
                    return false;
                }
            }

            if (super.getRegex() != null && !value.matches(super.getRegex())) {
                markInvalid(getMessages().getRegexText());
                return false;
            }

            this.setOjbectValue(value);

            if (formatPattern != null) {
                FormatModel model = new FormatModel(formatPattern, value, Locale.getLanguage());
                service.formatValue(model, new SessionAwareAsyncCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        setDiplayValue(result);
                        setRawValue(result);
                    }
                });
            }

            return true;
        }
    }

    public void setValidateFlag(boolean validateFlag) {
        this.validateFlag = validateFlag;
    }

    @Override
    public void setRawValue(String value) {
        if (rendered) {
            if (isEditable()) {
                getInputEl().setValue(value == null ? "" : value); //$NON-NLS-1$
            } else {
                getInputEl().dom.setInnerText(value == null ? "" : value); //$NON-NLS-1$
            }
        }
    }

    @Override
    public String getRawValue() {
        String v = rendered ? getInputEl().getValue() : ""; //$NON-NLS-1$
        if (!isEditable()) {
            v = rendered ? getInputEl().dom.getInnerText() : ""; //$NON-NLS-1$
        }
        if (v == null || v.equals(emptyText)) {
            return ""; //$NON-NLS-1$
        }
        return v;
    }

    @Override
    protected void onRender(Element target, int index) {
        if (isEditable()) {
            super.onRender(target, index);
        } else {
            if (el() == null) {
                setElement(DOM.createDiv(), target, index);
                getElement().setAttribute("role", "presentation"); //$NON-NLS-1$//$NON-NLS-2$

                textFieldDisable = DOM.createDiv();
                if (name != null && name.length() > 0) {
                    DOM.setElementAttribute(textFieldDisable, "key", name); //$NON-NLS-1$
                }
                DOM.setElementAttribute(textFieldDisable, "type", "text"); //$NON-NLS-1$//$NON-NLS-2$
                DOM.setElementAttribute(textFieldDisable, "contenteditable", "true"); //$NON-NLS-1$//$NON-NLS-2$
                String elementStyle = "overflow: hidden; whiteSpace: nowrap;"; //$NON-NLS-1$
                if (getUserProperties() != null && getUserProperties().size() > 0) {
                    if (getUserProperties().containsKey(KEY_MDM_READ_ONLY_FIELD_STYLE)) {
                        elementStyle = elementStyle + getUserProperties().get(KEY_MDM_READ_ONLY_FIELD_STYLE);
                    }
                }
                DOM.setElementAttribute(textFieldDisable, "style", elementStyle); //$NON-NLS-1$
                getElement().appendChild(textFieldDisable);
                input = el().firstChild();
            }

            addStyleName("x-form-field-wrap"); //$NON-NLS-1$
            getInputEl().addStyleName(fieldStyle);

            getInputEl().setId(getId() + "-input"); //$NON-NLS-1$

            super.onRender(target, index);
            removeStyleName(fieldStyle);

            if (GXT.isAriaEnabled()) {
                if (!getAllowBlank()) {
                    setAriaState("aria-required", "true"); //$NON-NLS-1$//$NON-NLS-2$
                }
            }

            applyEmptyText();
            input.dom.removeAttribute("tabIndex"); //$NON-NLS-1$
        }
    }

    @Override
    public void disable() {
        super.disable();
        setEditable(false);
        if (input != null) {
            input.dom.setAttribute("contenteditable", "false"); //$NON-NLS-1$//$NON-NLS-2$
            input.dom.removeAttribute("tabIndex"); //$NON-NLS-1$
        }
    }

    @Override
    public void enable() {
        super.enable();
        setEditable(true);
        if (input != null) {
            input.dom.setAttribute("contenteditable", "true"); //$NON-NLS-1$//$NON-NLS-2$
            input.dom.removeAttribute("tabIndex"); //$NON-NLS-1$
        }
    }

    @Override
    protected void onKeyDown(FieldEvent fe) {
        if (fe.getKeyCode() == 13 && !isEditable()) {
            fe.stopEvent();
            return;
        }
        super.onKeyDown(fe);
    }

    @Override
    public void onDisable() {
        addStyleName(disabledStyle);
    }

    public Boolean isEditable() {
        return this.editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public HashMap<String, String> getUserProperties() {
        return this.userProperties;
    }

    public void setUserProperties(HashMap<String, String> userProperties) {
        this.userProperties = userProperties;
    }

}
