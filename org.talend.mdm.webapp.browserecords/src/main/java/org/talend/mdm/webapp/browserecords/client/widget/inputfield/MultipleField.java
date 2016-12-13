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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.creator.FieldCreator;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.plugin.FieldPlugin;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MultipleField extends TextField<List<Object>> {

    VerticalPanel vp = new VerticalPanel();

    Listener<BaseEvent> changeListener = new Listener<BaseEvent>() {

        public void handleEvent(BaseEvent be) {
            TextField field = (TextField) be.getSource();
            int index = vp.getWidgetIndex(field);
            value.set(index, (Serializable) field.getValue());
            MultipleField.this.fireChangeEvent(null, value);
        }
    };

    int min, max;

    TypeModel dataType;

    String language;

    public MultipleField(TypeModel dataType, String language) {
        this.dataType = dataType;
        this.language = language;
        int[] range = dataType.getRange();
        min = range[0];
        max = range[1];
        value = new ArrayList<Object>();
        for (int i = 0; i < min; i++) {
            value.add("");//$NON-NLS-1$
        }
    }

    private boolean validateRange(int size) {
        if (size >= min && size <= max) {
            return true;
        }
        return false;
    }

    protected void onRender(Element target, int index) {
        if (el() == null) {
            vp.setSpacing(5);
            setElement(vp.getElement(), target, index);
        }
        super.onRender(target, index);
        removeStyleName(fieldStyle);
    }

    protected void doAttachChildren() {
        super.doAttachChildren();
        ComponentHelper.doAttach(vp);
    }

    protected void doDetachChildren() {
        super.doDetachChildren();
        ComponentHelper.doDetach(vp);
    }

    private void buildFields() {
        if (value != null) {
            vp.clear();
            for (Object item : value) {
                Field field = createField(item);
                vp.add(field);
            }
        } else {
            vp.clear();
        }
    }

    private void buildValue() {
        value.clear();
        Iterator<Widget> iter = vp.iterator();
        while (iter.hasNext()) {
            Field field = (Field) iter.next();
            value.add((Serializable) field.getValue());
        }
    }

    private Field createField(Object value) {
        Field field = (Field) FieldCreator.createField((SimpleTypeModel) dataType, null, false, language);
        field.addPlugin(createFp());
        field.setValue(value);
        field.addListener(Events.Change, changeListener);
        return field;
    }

    public void setValue(List<Object> value) {
        this.value = value;
        if (rendered) {
            buildFields();
        }
    }

    public List<Object> getValue() {
        return value;
    }

    public FieldPlugin createFp() {
        FieldPlugin fp = new FieldPlugin();
        fp.setListener(new FieldPlugin.Add_Remove_Listener() {

            public void onRemove(Field field) {
                if (!validateRange(value.size() - 1)) {
                    MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                            .multiOccurrence_minimize(min), null);
                    return;
                }
                field.removeListener(Events.Change, changeListener);
                field.removeFromParent();
                buildValue();
                MultipleField.this.fireChangeEvent(null, value);
            }

            public void onAdd(Field field) {
                if (!validateRange(value.size() + 1)) {
                    MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                            .multiOccurrence_maximize(max), null);
                    return;
                }
                Field newField = createField(dataType.getType().getDefaultValue());
                int index = vp.getWidgetIndex(field);
                vp.insert(newField, index + 1);
                buildValue();
                MultipleField.this.fireChangeEvent(null, value);
            }
        });
        return fp;
    }
}
