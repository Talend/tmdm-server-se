/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.client.view;

import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import org.talend.mdm.webapp.stagingarea.control.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.stagingarea.control.client.i18n.StagingAreaMessages;

abstract class AbstractView extends Composite {

    final ContentPanel        mainPanel;

    final StagingAreaMessages messages;

    AbstractView() {
        messages = MessagesFactory.getMessages();
        mainPanel = new ContentPanel();
        mainPanel.setHeaderVisible(false);

        initComponents();
        initLayout();
        registerEvent();
        this.initComponent(mainPanel);
    }

    void initComponents() {
    }

    void initLayout() {
    }

    void registerEvent() {
    }

}
