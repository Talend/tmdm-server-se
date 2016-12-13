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

import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailUtil;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;


public class TabItemListener implements Listener<TabPanelEvent> {

    private TabItem item;

    public boolean isConfirmedTabClose = false;

    public TabItemListener(TabItem _item) {
        this.item = _item;
    }

    public void handleEvent(TabPanelEvent be) {
        if (isConfirmedTabClose)
            isConfirmedTabClose = false;
        else {
            be.setCancelled(true);
            TreeDetailUtil.checkRecord(item, (ItemsDetailPanel) item.getWidget(0), this, null);
        }
    }
}