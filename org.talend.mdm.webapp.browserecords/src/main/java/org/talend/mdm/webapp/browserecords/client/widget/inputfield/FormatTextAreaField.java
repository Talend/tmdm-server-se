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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;


/**
 * DOC Administrator  class global comment. Detailed comment
 */
public class FormatTextAreaField extends FormatTextField {

    private boolean preventScrollbars;

    private Element textFieldDisable;

    public FormatTextAreaField() {
        super();
        setSize(100, 60);
    }

    @Override
    public int getCursorPos() {
        return impl.getTextAreaCursorPos(getInputEl().dom);
    }

    /**
     * Returns true if scroll bars are disabled.
     * 
     * @return the scroll bar state
     */
    public boolean isPreventScrollbars() {
        return preventScrollbars;
    }

    /**
     * True to prevent scrollbars from appearing regardless of how much text is in the field (equivalent to setting
     * overflow: hidden, defaults to false, pre-render).
     * 
     * @param preventScrollbars true to disable scroll bars
     */
    public void setPreventScrollbars(boolean preventScrollbars) {
        this.preventScrollbars = preventScrollbars;
    }

    @Override
    protected void onRender(Element target, int index) {
        if (el() == null) {
            setElement(DOM.createDiv(), target, index);
            if (isEditable()) {
                getElement().appendChild(DOM.createTextArea());
            } else {
                textFieldDisable = DOM.createDiv();
                if (name != null && name.length() > 0) {
                    DOM.setElementAttribute(textFieldDisable, "key", name); //$NON-NLS-1$
                }
                DOM.setElementAttribute(textFieldDisable, "type", "text"); //$NON-NLS-1$//$NON-NLS-2$
                DOM.setElementAttribute(textFieldDisable, "contenteditable", "true"); //$NON-NLS-1$//$NON-NLS-2$
                String elementStyle = "overflow: auto; "; //$NON-NLS-1$
                if (getUserProperties() != null && getUserProperties().size() > 0) {
                    if (getUserProperties().containsKey(KEY_MDM_READ_ONLY_FIELD_STYLE)) {
                        elementStyle = elementStyle + getUserProperties().get(KEY_MDM_READ_ONLY_FIELD_STYLE);
                    }
                }
                DOM.setElementAttribute(textFieldDisable, "style", elementStyle); //$NON-NLS-1$
                getElement().appendChild(textFieldDisable);
            }
            input = el().firstChild();
        }

        getInputEl().dom.setPropertyString("autocomplete", "off"); //$NON-NLS-1$//$NON-NLS-2$

        if (preventScrollbars) {
            getInputEl().setStyleAttribute("overflow", "hidden"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        super.onRender(target, index);

        addInputStyleName("x-form-textarea"); //$NON-NLS-1$
    }

}
