/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.integrity;

import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.LineageListPanel;
import org.talend.mdm.webapp.browserecords.client.widget.LineagePanel;

public class CloseLineageTabPostDeleteAction implements PostDeleteAction {

    private final PostDeleteAction next;

    private final ItemDetailToolBar bar;

    public CloseLineageTabPostDeleteAction(ItemDetailToolBar bar, PostDeleteAction next) {
        this.bar = bar;
        this.next = next;
    }

    @Override
    public void doAction() {
        LineagePanel.getInstance().clearDetailPanel();
        LineageListPanel.getInstance().refresh();
        next.doAction();
    }
}
