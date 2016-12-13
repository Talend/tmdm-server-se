/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget;

import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class LineageTabPanel extends TabPanel {

    private static LineageTabPanel instance;

    private TabItem listTabItem;

    private TabItem explainTabItem;

    private LineageListPanel lineageListPanel;

    private ExplainTablePanel explainTablePanel;

    public static LineageTabPanel getInstance() {
        if (instance == null) {
            instance = new LineageTabPanel();
        }
        return instance;
    }

    private LineageTabPanel() {
        listTabItem = new TabItem(MessagesFactory.getMessages().lineage_list_tab_title());
        listTabItem.setLayout(new FitLayout());
        lineageListPanel = LineageListPanel.getInstance();
        listTabItem.setClosable(false);
        listTabItem.add(lineageListPanel);
        add(listTabItem);
        explainTabItem = new TabItem(MessagesFactory.getMessages().lineage_explain_tab_title());
        explainTabItem.setLayout(new FitLayout());
        explainTablePanel = new ExplainTablePanel();
        explainTabItem.setClosable(false);
        explainTabItem.add(explainTablePanel);
        add(explainTabItem);
        setSelection(listTabItem);
    }

    public void init() {
        setSelection(listTabItem);
    }

    public LineageListPanel getLineageListPanel() {
        return this.lineageListPanel;
    }

    public ExplainTablePanel getExplainTablePanel() {
        return this.explainTablePanel;
    }
}
