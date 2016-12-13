/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.widget;

import java.util.HashMap;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XDOM;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;


public class MultiLanguageTextAreaField extends MultiLanguageField {
    
    private Element textFieldDisable;
    
    public MultiLanguageTextAreaField() {
        super();
        setSize(100, 60);
    }
    
    public MultiLanguageTextAreaField(boolean isFormInput) {
        super(isFormInput);
    }
    
    public MultiLanguageTextAreaField(boolean isFormInput, HashMap<String,String> userProperties) {
        super(isFormInput);
        setUserProperties(userProperties);
    }
    
    @Override
    protected void onRender(Element target, int index) {
        if (isEditable()) {
            super.setSelfRender(false);
            if (isFormInput) {
                El wrap = new El(DOM.createTable());
                Element tbody = DOM.createTBody();
                Element mlstr = DOM.createTR();
                tbody.appendChild(mlstr);
                Element tdInput = DOM.createTD();
                Element tdIcon = DOM.createTD();
                mlstr.appendChild(tdInput);
                mlstr.appendChild(tdIcon);

                wrap.appendChild(tbody);
                wrap.addStyleName("x-form-field-wrap"); //$NON-NLS-1$
                wrap.addStyleName("x-form-file-wrap"); //$NON-NLS-1$

                input = new El(DOM.createTextArea());
                input.setId(XDOM.getUniqueId());
                input.setEnabled(true);

                tdInput.appendChild(input.dom);
                Element buttonDiv = DOM.createTable();
                Element tr = DOM.createTR();
                Element body = DOM.createTBody();

                Element displayTD = DOM.createTD();

                buttonDiv.appendChild(body);
                body.appendChild(tr);
                tr.appendChild(displayTD);

                tdIcon.appendChild(buttonDiv);
                setElement(wrap.dom, target, index);

                displayTD.appendChild(displayMultiLanguageBtn.getElement());
                displayMultiLanguageBtn.getElement().getStyle().setCursor(Cursor.POINTER);
                updateCtrlButton();

                this.setAutoWidth(true);
                this.setStyleAttribute("margin-left", "-2px"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else {
            if (el() == null) {
                setElement(DOM.createDiv(), target, index);
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
                input = el().firstChild();
            }
        }
        
        getInputEl().setStyleAttribute("height", "56px"); //$NON-NLS-1$ //$NON-NLS-2$
        
        super.onRender(target, index);
        
        addInputStyleName("x-form-textarea"); //$NON-NLS-1$
    }
}
