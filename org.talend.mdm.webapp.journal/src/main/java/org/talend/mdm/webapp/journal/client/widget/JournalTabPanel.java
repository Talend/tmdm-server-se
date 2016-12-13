/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client.widget;

import org.talend.mdm.webapp.journal.client.i18n.MessagesFactory;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class JournalTabPanel extends TabPanel {

    private static JournalTabPanel tabPanel;

    private TabItem resultTabItem;

    private TabItem timeLineTabItem;

    private JournalTimelinePanel journalTimelinePanel;
    
    private boolean status;

    public static JournalTabPanel getInstance() {
        if (tabPanel == null) {
            tabPanel = new JournalTabPanel();
        }
        return tabPanel;
    }

    private JournalTabPanel() {
        this.setResizeTabs(true);
        this.setAnimScroll(true);

        resultTabItem = new TabItem(MessagesFactory.getMessages().results_tab());
        resultTabItem.setId("resultTabItem"); //$NON-NLS-1$
        resultTabItem.setLayout(new FitLayout());
        resultTabItem.setClosable(false);
        resultTabItem.add(JournalGridPanel.getInstance());
        resultTabItem.addListener(Events.Select, new Listener<ComponentEvent>() {

            public void handleEvent(ComponentEvent be) {
                journalTimelinePanel.setActive(false);
            }
        });
        this.add(resultTabItem);

        timeLineTabItem = new TabItem(MessagesFactory.getMessages().timeline_tab());
        timeLineTabItem.setId("timeLineTabItem"); //$NON-NLS-1$
        timeLineTabItem.setClosable(false);
        timeLineTabItem.setScrollMode(Scroll.AUTO);

        SimplePanel cp = new SimplePanel();
        cp.getElement().setId("journalTimeLine"); //$NON-NLS-1$
        cp.setStyleName("timeline-default"); //$NON-NLS-1$
        cp.getElement().getStyle().setProperty("height", 398D, Unit.PX); //$NON-NLS-1$

        journalTimelinePanel = new JournalTimelinePanel();
        journalTimelinePanel.setId("journalTimeLine"); //$NON-NLS-1$
        journalTimelinePanel.setStyleName("timeline-default"); //$NON-NLS-1$

        timeLineTabItem.addListener(Events.Resize, new Listener<ComponentEvent>(){
            public void handleEvent(ComponentEvent be) {
                if (journalTimelinePanel.isRendered() && status != JournalSearchPanel.getInstance().isExpanded()) { 
                    timeLineTabItem.fireEvent(Events.Select);
                }
                status = JournalSearchPanel.getInstance().isExpanded();    
            }            
        });

        timeLineTabItem.add(journalTimelinePanel);
        timeLineTabItem.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                journalTimelinePanel.setActive(true);
                journalTimelinePanel.setTimeLinePanelHeight(timeLineTabItem.getHeight());
                journalTimelinePanel.getElement().getStyle().setPropertyPx("height", timeLineTabItem.getHeight()); //$NON-NLS-1$
                journalTimelinePanel.initTimeline(JournalGridPanel.getInstance().getOffset(), JournalGridPanel.getInstance().getLoaderConfigStr());
            }
        });
        this.add(timeLineTabItem);
        this.setSelection(resultTabItem);
    }

    public TabItem getResultTabItem() {
        return resultTabItem;
    }

    public TabItem getTimeLineTabItem() {
        return timeLineTabItem;
    }

    public JournalTimelinePanel getJournalTimelinePanel() {
        return this.journalTimelinePanel;
    }

    public void setSelectionItem() {
        this.setSelection(resultTabItem);
    }
}