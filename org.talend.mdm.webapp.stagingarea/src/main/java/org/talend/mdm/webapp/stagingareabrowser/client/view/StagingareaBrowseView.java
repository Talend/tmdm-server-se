/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingareabrowser.client.view;

import org.talend.mdm.webapp.stagingareabrowser.client.controller.ControllerContainer;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;


public class StagingareaBrowseView extends AbstractView {

    private SearchView searchView;

    private ResultsView resultsView;

    @Override
    protected void initComponents() {
        searchView = new SearchView();
        resultsView = new ResultsView();
        ControllerContainer.initController(searchView, resultsView);
    }

    @Override
    protected void initLayout() {
        mainPanel.setHeight(600);
        mainPanel.setLayout(new RowLayout());
        mainPanel.add(searchView, new RowData(1, -1, new Margins(5, 5, 5, 5)));
        mainPanel.add(resultsView, new RowData(1, 1, new Margins(5, 5, 5, 5)));
    }
}
