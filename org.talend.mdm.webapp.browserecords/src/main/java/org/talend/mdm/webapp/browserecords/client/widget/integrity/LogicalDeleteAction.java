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

import java.util.List;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.widget.CallbackAction;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;

import com.extjs.gxt.ui.client.widget.MessageBox;

/**
 * Wraps a logical delete operation.
 */
public class LogicalDeleteAction implements DeleteAction {

    private String url;
    
    public LogicalDeleteAction(String url) {
        this.url = url;
    }

    public void delete(List<ItemBean> items, BrowseRecordsServiceAsync service, boolean override,
            final PostDeleteAction postDeleteAction) {
        final BrowseRecordsMessages message = MessagesFactory.getMessages();
        final MessageBox progressBar = MessageBox.wait(message.delete_item_title(), null, message.delete_item_progress());
        service.logicalDeleteItems(items, url, override, new SessionAwareAsyncCallback<Void>() {
            public void onSuccess(Void arg0) {
                progressBar.close();
                postDeleteAction.doAction();
                CallbackAction.getInstance().doAction(CallbackAction.HIERARCHY_DELETEITEM_CALLBACK,null,false);
            }

            @Override
            protected void doOnFailure(Throwable caught) {
                progressBar.close();
                super.doOnFailure(caught);
            }
        });
    }

}
