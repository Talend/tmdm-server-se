/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.recyclebin.client.mvc;

import org.talend.mdm.webapp.recyclebin.client.RecycleBinEvents;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class RecycleBinController extends Controller {

    private RecycleBinView view;

    public RecycleBinController() {
        registerEventTypes(RecycleBinEvents.InitFrame);
    }

    @Override
    public void initialize() {
        view = new RecycleBinView(this);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type == RecycleBinEvents.InitFrame) {
            forwardToView(view, event);
        }
    }
}
