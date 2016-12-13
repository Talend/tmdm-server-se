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

import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;

import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class MatchGroupCriteria extends Composite {

    private LayoutContainer container = new LayoutContainer();

    private TextField<String> matchGroupField = new TextField<String>();
    
    private String operator = "EQUALS"; //$NON-NLS-1$

    public MatchGroupCriteria() {
        init();
        this.initComponent(container);
    }

    public void setValue(String taskId) {
        matchGroupField.setValue(taskId);
    }

    public String getValue() {
        return matchGroupField.getValue();
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public String getOperator() {
        return this.operator;
    }
    private void init() {
        FormLayout layout = new FormLayout();
        layout.setLabelWidth(110);
        container.setLayout(layout);
        container.setStyleAttribute("paddingTop", "1px"); //$NON-NLS-1$ //$NON-NLS-2$
        matchGroupField.setFieldLabel(MessagesFactory.getMessages().match_group());
        matchGroupField.setWidth(120);
        container.add(matchGroupField);
    }
}
