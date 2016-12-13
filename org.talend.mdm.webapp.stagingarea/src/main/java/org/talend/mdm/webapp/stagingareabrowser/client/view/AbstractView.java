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

import org.talend.mdm.webapp.stagingareabrowser.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.stagingareabrowser.client.i18n.StagingareaBrowseMessages;

import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;

public class AbstractView extends Composite {

    protected final ContentPanel mainPanel;

    protected final StagingareaBrowseMessages messages;

    public AbstractView() {
        messages = MessagesFactory.getMessages();
        mainPanel = new ContentPanel();
        mainPanel.setHeaderVisible(false);

        initComponents();
        initLayout();
        registerEvent();
        this.initComponent(mainPanel);
    }

    protected void initComponents() {

    }

    protected void initLayout() {

    }

    protected void registerEvent() {

    }

}
