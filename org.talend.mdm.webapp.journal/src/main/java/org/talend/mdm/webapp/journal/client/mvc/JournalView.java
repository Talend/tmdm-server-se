/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client.mvc;

import org.talend.mdm.webapp.journal.client.GenerateContainer;
import org.talend.mdm.webapp.journal.client.JournalEvents;
import org.talend.mdm.webapp.journal.client.widget.JournalGridPanel;
import org.talend.mdm.webapp.journal.client.widget.JournalSearchPanel;
import org.talend.mdm.webapp.journal.client.widget.JournalTabPanel;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

/**
 * DOC Administrator  class global comment. Detailled comment
 */
public class JournalView extends View {

    public JournalView(Controller controller) {
        super(controller);
    }

    protected void handleEvent(AppEvent event) {
        if (event.getType() == JournalEvents.InitFrame)
            onInitFrame(event);
        else if (event.getType() == JournalEvents.DoSearch)
            onDoSearch(event);
    }
    
    private void onInitFrame(AppEvent event) {
        if (Log.isInfoEnabled())
            Log.info("Init frame... ");//$NON-NLS-1$

        ContentPanel container = GenerateContainer.getContentPanel();
        container.setHeaderVisible(false);
        BorderLayout layout = new BorderLayout();
        container.setLayout(layout);
        container.setStyleAttribute("height", "100%");//$NON-NLS-1$ //$NON-NLS-2$  
        BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 195);
        northData.setCollapsible(true);
        northData.setSplit(false);
        northData.setFloatable(false);
        northData.setMargins(new Margins(0, 0, 5, 0));
        JournalSearchPanel northPanel = JournalSearchPanel.getInstance();
        container.add(northPanel, northData);
        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
        centerData.setMargins(new Margins(0));
        JournalTabPanel southPanel = JournalTabPanel.getInstance();
        southPanel.setSelectionItem();
        container.add(southPanel, centerData);
    }
    
    private void onDoSearch(AppEvent event) {
        JournalGridPanel.getInstance().refreshGrid();
        if (JournalTabPanel.getInstance().getJournalTimelinePanel().isActive()) {
            JournalTabPanel
                    .getInstance()
                    .getJournalTimelinePanel()
                    .initTimeline(JournalGridPanel.getInstance().getOffset(),
                            JournalGridPanel.getInstance().getLoaderConfigStr());
        }

    }
}