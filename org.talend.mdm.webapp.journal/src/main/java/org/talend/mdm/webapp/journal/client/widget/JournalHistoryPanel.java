/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client.widget;

import org.talend.mdm.webapp.journal.client.Journal;
import org.talend.mdm.webapp.journal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.journal.client.util.JournalSearchUtil;
import org.talend.mdm.webapp.journal.shared.JournalGridModel;
import org.talend.mdm.webapp.journal.shared.JournalParameters;
import org.talend.mdm.webapp.journal.shared.JournalTreeModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

/**
 * DOC Administrator  class global comment. Detailled comment
 */
public class JournalHistoryPanel extends ContentPanel {
    
    private boolean isAuth;
    private JournalDataPanel journalDataPanel;
    
    private JournalComparisonPanel beforePanel;
    
    private JournalComparisonPanel afterPanel;
    
    private BorderLayoutData westData;
    
    private BorderLayoutData centerData;
    
    BorderLayoutData northData;
    
    public JournalHistoryPanel(JournalTreeModel root, JournalGridModel gridModel, boolean isAuth, int width) {
        this.isAuth = isAuth;
        this.setFrame(false);
        this.setItemId(gridModel.getIds().concat(getCriteriaString()));
        this.setHeaderVisible(false);
        this.setHeading(MessagesFactory.getMessages().data_change_viewer());
        this.setLayout(new BorderLayout());
        
        northData = new BorderLayoutData(LayoutRegion.NORTH, 200);
        northData.setCollapsible(false);
        northData.setSplit(true);
        northData.setMargins(new Margins(0, 0, 0, 0));        
        
        journalDataPanel = generateJournalDataPanel(root, gridModel);
        journalDataPanel.setJournalHistoryPanel(this);
        this.add(journalDataPanel, northData);        

        westData = new BorderLayoutData(LayoutRegion.WEST, width);
        westData.setCollapsible(false);
        westData.setSplit(true);
        westData.setMargins(new Margins(5, 5, 0, 0));
        
        beforePanel = generateJournalComparisonPanel(MessagesFactory.getMessages().before_label(),
                JournalSearchUtil.buildParameter(gridModel, "before", isAuth),journalDataPanel.getJournalGridModel(),true); //$NON-NLS-1$
        this.add(beforePanel, westData);
        
        centerData = new BorderLayoutData(LayoutRegion.CENTER);
        westData.setCollapsible(false);
        centerData.setMargins(new Margins(5, 0, 0, 0));
        centerData.setSplit(true);
        
        afterPanel = generateJournalComparisonPanel(MessagesFactory.getMessages().after_label(),
                JournalSearchUtil.buildParameter(gridModel, "current", isAuth),journalDataPanel.getJournalGridModel(),false); //$NON-NLS-1$
        this.add(afterPanel, centerData);
        
        beforePanel.setOtherPanel(afterPanel);
        afterPanel.setOtherPanel(beforePanel);
    }    

    public void update() {
        this.remove(afterPanel);
        this.remove(beforePanel);
        JournalGridModel gridModel = journalDataPanel.getJournalGridModel();
        this.setItemId(gridModel.getIds());
        this.beforePanel = generateJournalComparisonPanel(MessagesFactory.getMessages().before_label(),
                JournalSearchUtil.buildParameter(gridModel, "before", this.isAuth),journalDataPanel.getJournalGridModel(),true); //$NON-NLS-1$
        this.add(beforePanel, westData);
        this.afterPanel = generateJournalComparisonPanel(MessagesFactory.getMessages().after_label(),
                JournalSearchUtil.buildParameter(gridModel, "current", isAuth),journalDataPanel.getJournalGridModel(),false); //$NON-NLS-1$
        this.add(afterPanel, centerData);
        
        beforePanel.setOtherPanel(afterPanel);
        afterPanel.setOtherPanel(beforePanel);
        this.getJournalDataPanel().layout();
        this.layout();
    }
    
    public String getCriteriaString() {
        return Registry.get(Journal.SEARCH_CRITERIA).toString();
    }

    public JournalComparisonPanel getBeforePanel() {
        return beforePanel;
    }
  
    public JournalComparisonPanel getAfterPanel() {
        return afterPanel;
    }

    public JournalDataPanel getJournalDataPanel() {
        return journalDataPanel;
    }

    protected JournalDataPanel generateJournalDataPanel(JournalTreeModel root, JournalGridModel gridModel) {
        return new JournalDataPanel(root, gridModel);
    }

    protected JournalComparisonPanel generateJournalComparisonPanel(String title, final JournalParameters parameter,
            final JournalGridModel journalGridModel, final boolean isBeforePanel) {
        return new JournalComparisonPanel(title, parameter, journalGridModel, isBeforePanel);
    }
}
