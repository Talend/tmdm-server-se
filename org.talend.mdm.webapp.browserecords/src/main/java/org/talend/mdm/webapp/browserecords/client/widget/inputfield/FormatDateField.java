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

import java.util.Date;
import java.util.HashMap;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.model.FormatModel;
import org.talend.mdm.webapp.browserecords.client.util.Locale;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.PreviewEvent;
import com.extjs.gxt.ui.client.util.BaseEventPreview;
import com.extjs.gxt.ui.client.widget.DatePicker;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.menu.DateMenu;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

public class FormatDateField extends DateField {

    static interface FormatValueCallback {

        void formatValue(String value);
    }

    BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);

    public static final String KEY_MDM_READ_ONLY_FIELD_STYLE = "MDM_READ_ONLY_FIELD_STYLE"; //$NON-NLS-1$

    private String formatPattern;

    private boolean isDateTime = false;

    private boolean validateFlag = true;

    private DateMenu menu;

    private Element textFieldDisable;

    private HashMap<String, String> userProperties;

    public FormatDateField() {
        super();
        validateFlag = BrowseRecords.getSession().getAppHeader().isAutoValidate();
        setUserProperties(BrowseRecords.getSession().getAppHeader().getUserProperties());
    }

    public boolean isDateTime() {
        return isDateTime;
    }

    public void setDateTime(boolean isDateTime) {
        this.isDateTime = isDateTime;
    }

    public String getFormatPattern() {
        return formatPattern;
    }

    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    public String getOjbectValue() {
        if (value != null) {
            return propertyEditor.getStringValue(value);
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public void setValue(Date value) {
        super.setValue(value);
        setFormatedValue(new FormatValueCallback() {

            @Override
            public void formatValue(String value) {
                setRawValue(value);
            }
        });
    }

    @Override
    public String getRawValue() {
        String rawValue = rendered ? getInputEl().getValue() : ""; //$NON-NLS-1$
        if (!isEditable()) {
            rawValue = rendered ? getInputEl().dom.getInnerText() : ""; //$NON-NLS-1$
        }
        if (rawValue == null || "".equals(rawValue) || rawValue.equals(emptyText)) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }

        try {
            Date d = propertyEditor.convertStringValue(rawValue);
            if (d != null) {
                return rawValue;
            }
            return value == null ? "" : propertyEditor.getStringValue(value); //$NON-NLS-1$
        } catch (Exception e) {
            if (!this.validateValue(rawValue)) {
                return rawValue;
            }
            return value == null ? "" : propertyEditor.getStringValue(value); //$NON-NLS-1$
        }
    }

    @Override
    public void setRawValue(String value) {
        String rawValue = value;
        if (formatPattern != null && formatPattern.trim().length() > 0) {
            if (hasFocus) {
                rawValue = this.value == null ? "" : propertyEditor.getStringValue(this.value); //$NON-NLS-1$);
            }
        }

        if (rendered) {
            if (isEditable()) {
                getInputEl().setValue(rawValue == null ? "" : rawValue); //$NON-NLS-1$
            } else {
                getInputEl().dom.setInnerText(rawValue == null ? "" : rawValue); //$NON-NLS-1$
            }
        }
    }

    @Override
    protected void onFocus(ComponentEvent ce) {
        if (formatPattern != null && formatPattern.trim().length() > 0) {
            setRawValue(value == null ? "" : propertyEditor.getStringValue(value)); //$NON-NLS-1$);
        }
        super.onFocus(ce);
    }

    @Override
    protected void onBlur(ComponentEvent ce) {
        if (formatPattern != null && formatPattern.trim().length() > 0) {
            setFormatedValue(new FormatValueCallback() {

                @Override
                public void formatValue(String value) {
                    setRawValue(value);
                }
            });
        }
    }

    private void setFormatedValue(final FormatValueCallback callback) {
        if (formatPattern != null && formatPattern.trim().length() > 0) {
            if (value != null) {
                final FormatModel model = new FormatModel(formatPattern, value, Locale.getLanguage());
                service.formatValue(model, new SessionAwareAsyncCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        if (callback != null) {
                            callback.formatValue(result);
                        }
                    }
                });
            } else {
                if (callback != null) {
                    callback.formatValue(""); //$NON-NLS-1$
                }
            }
        }
    }

    @Override
    public boolean validateValue(String value) {
        if (formatPattern != null && formatPattern.trim().length() > 0 && this.value != null) {
            return super.validateValue(propertyEditor.getStringValue(this.value));
        } else {
            return super.validateValue(value);
        }
    }

    public void setValidateFlag(boolean validateFlag) {
        this.validateFlag = validateFlag;
    }

    @Override
    protected void onRender(Element target, int index) {

        focusEventPreview = new BaseEventPreview() {

            @Override
            protected boolean onAutoHide(final PreviewEvent ce) {
                if (ce.getEventTypeInt() == Event.ONMOUSEDOWN) {
                    mimicBlur(ce, ce.getTarget());
                }
                return false;
            }
        };

        if (el() != null) {
            super.onRender(target, index);
            return;
        }

        setElement(DOM.createDiv(), target, index);

        if (!isPassword()) {
            if (isEditable()) {
                input = new El(DOM.createInputText());
            } else {
                textFieldDisable = DOM.createDiv();
                if (name != null && name.length() > 0) {
                    DOM.setElementAttribute(textFieldDisable, "key", name); //$NON-NLS-1$
                }
                DOM.setElementAttribute(textFieldDisable, "type", "text"); //$NON-NLS-1$//$NON-NLS-2$
                DOM.setElementAttribute(textFieldDisable, "contenteditable", "true"); //$NON-NLS-1$//$NON-NLS-2$
                String elementStyle = "overflow: hidden; whiteSpace: nowrap;"; //$NON-NLS-1$
                if (GXT.isIE) {
                    elementStyle = "overflow: hidden; whiteSpace: nowrap; float: left;"; //$NON-NLS-1$
                }
                if (getUserProperties() != null && getUserProperties().size() > 0) {
                    if (getUserProperties().containsKey(KEY_MDM_READ_ONLY_FIELD_STYLE)) {
                        elementStyle = elementStyle + getUserProperties().get(KEY_MDM_READ_ONLY_FIELD_STYLE);
                    }
                }
                DOM.setElementAttribute(textFieldDisable, "style", elementStyle); //$NON-NLS-1$
                input = new El(textFieldDisable);
            }
        } else {
            input = new El(DOM.createInputPassword());
        }

        addStyleName("x-form-field-wrap"); //$NON-NLS-1$

        input.addStyleName(fieldStyle);

        trigger = new El(GXT.isHighContrastMode ? DOM.createDiv() : DOM.createImg());
        trigger.dom.setClassName("x-form-trigger " + triggerStyle); //$NON-NLS-1$
        trigger.dom.setPropertyString("src", GXT.BLANK_IMAGE_URL); //$NON-NLS-1$
        if (GXT.isAriaEnabled()) {
            trigger.dom.setPropertyString("alt", "Dropdown"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        el().appendChild(input.dom);
        el().appendChild(trigger.dom);

        if (isHideTrigger()) {
            trigger.setVisible(false);
        }

        super.onRender(target, index);
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

    /**
     * Returns the field's date picker.
     * 
     * @return the date picker
     */
    @Override
    public DatePicker getDatePicker() {
        if (menu == null) {
            menu = new DateMenu();

            menu.getDatePicker().addListener(Events.Select, new Listener<ComponentEvent>() {

                @Override
                public void handleEvent(ComponentEvent ce) {
                    focusValue = getValue();
                    setValue(menu.getDate());
                    menu.hide();
                }
            });
            menu.addListener(Events.Hide, new Listener<ComponentEvent>() {

                @Override
                public void handleEvent(ComponentEvent be) {
                    focus();
                }
            });
        }
        return menu.getDatePicker();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void expand() {
        DatePicker picker = getDatePicker();

        Object v = getValue();
        Date d = null;
        if (v instanceof Date) {
            d = (Date) v;
        } else {
            d = new Date();
        }

        picker.setMinDate(super.getMinValue());
        picker.setMaxDate(super.getMaxValue());
        picker.setValue(d, true);

        // handle case when down arrow is opening menu
        DeferredCommand.addCommand(new Command() {

            @Override
            public void execute() {
                menu.show(el().dom, "tl-bl?"); //$NON-NLS-1$
                menu.getDatePicker().focus();
            }
        });
    }

    @Override
    public Date getValue() {
        if (!rendered) {
            return value;
        }
        String v = getRawValue();
        if (emptyText != null && v.equals(emptyText)) {
            return null;
        }
        if (v == null || v.equals("")) {
            return null;
        }
        try {
            return propertyEditor.convertStringValue(v);
        } catch (Exception e) {
            return value;
        }
    }

    public HashMap<String, String> getUserProperties() {
        return this.userProperties;
    }

    public void setUserProperties(HashMap<String, String> userProperties) {
        this.userProperties = userProperties;
    }
}
