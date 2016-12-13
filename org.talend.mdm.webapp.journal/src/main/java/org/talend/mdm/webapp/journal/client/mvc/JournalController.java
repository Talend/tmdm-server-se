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

import org.talend.mdm.webapp.journal.client.JournalEvents;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class JournalController extends Controller {

    private JournalView view;

    public JournalController() {
        registerEventTypes(JournalEvents.InitFrame);
        registerEventTypes(JournalEvents.Error);
        registerEventTypes(JournalEvents.DoSearch);
    }

    @Override
    public void initialize() {
        view = new JournalView(this);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type == JournalEvents.InitFrame) {
            forwardToView(view, event);
        } else if (type == JournalEvents.Error) {
            forwardToView(view, event);
        } else if (type == JournalEvents.DoSearch) {
            forwardToView(view, event);
        }
    }
}