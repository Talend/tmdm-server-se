/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.integrity;

import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.publiclistener.StatusObserver;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsMainTabPanel;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;

/**
 * An implementation of {@link PostDeleteAction} that performs item browser container operations (such as closing tabs
 * if needed...).
 */
public class ContainerUpdate implements PostDeleteAction {

    private final PostDeleteAction next;

    private ItemDetailToolBar bar;

    /**
     * @param next If you don't know what to pass as <code>next</code> argument, check the
     * constant {@link NoOpPostDeleteAction#INSTANCE}.
     */
    public ContainerUpdate(PostDeleteAction next) {
        this.next = next;
    }

    public ContainerUpdate(ItemDetailToolBar bar, PostDeleteAction next) {
        this.bar = bar;
        this.next = next;
    }

    public void doAction() {

        // After item has been deleted, close its view tab.
        TabItem tabItem = ItemsMainTabPanel.getInstance().getSelectedItem();

        if (tabItem != null) {
            tabItem.removeFromParent();
        }
        // TMDM-3361, Hierarchy didn't need to refresh ItemsListPanel
        if (bar != null && bar.isHierarchyCall()) {
            MessageBox messageBox = new MessageBox();
            messageBox.setTitle(MessagesFactory.getMessages().info_title());
            messageBox.setButtons(""); //$NON-NLS-1$
            messageBox.setIcon(MessageBox.INFO);
            messageBox.setMessage(MessagesFactory.getMessages().delete_record_success());
            messageBox.show();
            setTimeout(messageBox, 1000);
            StatusObserver.getInstance().notifyDeleted(null, null, false);
            return;
        }

        next.doAction();
    }

    private native void setTimeout(MessageBox msgBox, int millisecond)/*-{
		$wnd.setTimeout(function() {
			msgBox.@com.extjs.gxt.ui.client.widget.MessageBox::close()();
		}, millisecond);
    }-*/;
}
