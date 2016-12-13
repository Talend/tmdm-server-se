/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.SearchPanel;

import java.util.Date;

import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.util.FormatUtil;

import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class ModifiedOnCriteria extends Composite {

    private LayoutContainer container = new LayoutContainer();

    private DateField startField;;

    private DateField endField;;

    public ModifiedOnCriteria() {
        init();
        this.initComponent(container);
    }

    public void setStartDate(Date value) {
        startField.setValue(value);
    }

    public void setEndDate(Date value) {
        endField.setValue(value);
    }

    public Date getStartDate() {
        return startField.getValue();
    }

    public Date getEndDate() {
        return endField.getValue();
    }

    private void init() {
        container.setLayout(new ColumnLayout());
        LayoutContainer left = new LayoutContainer();
        left.setStyleAttribute("paddingRight", "10px"); //$NON-NLS-1$  //$NON-NLS-2$
        left.setStyleAttribute("paddingTop", "1px"); //$NON-NLS-1$ //$NON-NLS-2$
        FormLayout layout = new FormLayout();
        layout.setLabelWidth(110);
        left.setLayout(layout);
        startField = new DateField();
        startField.setWidth(120);
        startField.setFieldLabel(MessagesFactory.getMessages().search_modifiedon());
        startField.setPropertyEditor(new DateTimePropertyEditor(FormatUtil.defaultDateTimePattern));
        left.add(startField);

        LayoutContainer right = new LayoutContainer();
        right.setStyleAttribute("paddingLeft", "10px"); //$NON-NLS-1$  //$NON-NLS-2$
        right.setStyleAttribute("paddingTop", "1px"); //$NON-NLS-1$ //$NON-NLS-2$
        layout = new FormLayout();
        layout.setLabelWidth(50);
        right.setLayout(layout);
        endField = new DateField();
        endField.setWidth(120);
        endField.setFieldLabel(MessagesFactory.getMessages().search_modifiedto());
        endField.setPropertyEditor(new DateTimePropertyEditor(FormatUtil.defaultDateTimePattern));
        right.add(endField);

        container.add(left, new ColumnData(.5));
        container.add(right, new ColumnData(.5));
    }
}
