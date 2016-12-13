/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecordsinstaging.client.widget;

import org.talend.mdm.webapp.browserecordsinstaging.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsListPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsToolBar;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class ItemsToolBar4Staging extends ItemsToolBar {

    public ItemsToolBar4Staging() {
        super();
    }

    @Override
    protected void addDeleteButton() {
        deleteButton = new Button(MessagesFactory.getMessages().mark_as_deleted());
        deleteButton.setEnabled(false);
        deleteButton.setId("BrowseRecords_Delete"); //$NON-NLS-1$
        deleteButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Delete()));
        deleteButton.setMenu(null);
        deleteButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {

                if (ItemsListPanel.getInstance().getGrid() == null) {
                    MessageBox.alert(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages()
                            .select_mark_item_record(), null);
                } else {
                    if (getSelectItemNumber() == 0) {
                        MessageBox.alert(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages()
                                .select_mark_item_record(), null);
                    } else {
                        MessageBox.confirm(MessagesFactory.getMessages().confirm_title(), MessagesFactory.getMessages()
                                .mark_deleted_confirm(), new DeleteItemsBoxListener(service));
                    }
                }
            }
        });
        add(deleteButton);
    }

    @Override
    protected boolean isStaging() {
        return true;
    }
}
