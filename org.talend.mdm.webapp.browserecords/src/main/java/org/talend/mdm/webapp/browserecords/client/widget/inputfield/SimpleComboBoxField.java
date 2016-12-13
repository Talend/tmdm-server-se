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

import org.talend.mdm.webapp.browserecords.client.BrowseRecords;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.PreviewEvent;
import com.extjs.gxt.ui.client.util.BaseEventPreview;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

public class SimpleComboBoxField<T> extends SimpleComboBox<String> {

    public static final String KEY_MDM_READ_ONLY_FIELD_STYLE = "MDM_READ_ONLY_FIELD_STYLE"; //$NON-NLS-1$

    private Element textFieldDisable;

    private HashMap<String, String> userProperties;

    public SimpleComboBoxField() {
        super();
        setUserProperties(BrowseRecords.getSession().getAppHeader().getUserProperties());
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
    public void setRawValue(String text) {
        String rawValue = text;
        if (rendered) {
            if (rawValue == null) {
                String msg = getMessages().getValueNoutFoundText();
                rawValue = msg != null ? msg : ""; //$NON-NLS-1$
            }
            if (isEditable()) {
                getInputEl().setValue(rawValue);
            } else {
                getInputEl().dom.setInnerText(rawValue);
            }
        }
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
            trigger.dom.setPropertyString("alt", "Dropdown"); //$NON-NLS-1$//$NON-NLS-2$
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

    public HashMap<String, String> getUserProperties() {
        return this.userProperties;
    }

    public void setUserProperties(HashMap<String, String> hashMap) {
        this.userProperties = hashMap;
    }

}
