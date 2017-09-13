/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.integrity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.i18n.BaseMessagesFactory;
import org.talend.mdm.webapp.base.client.model.ItemResult;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.client.widget.CallbackAction;
import org.talend.mdm.webapp.base.client.widget.OperationMessageWindow;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.shared.FKIntegrityResult;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.MessageBox;

/**
 * Wraps a physical delete operation.
 */
public class PhysicalDeleteAction implements DeleteAction {

    private final int FAIL = 2;
    
    private final int ERROR = 3;
    
    @Override
    public void delete(final Map<ItemBean, FKIntegrityResult> items, BrowseRecordsServiceAsync service, boolean override,
            final PostDeleteAction postDeleteAction) {
        final BrowseRecordsMessages message = MessagesFactory.getMessages();
        final MessageBox progressBar = MessageBox.wait(message.delete_item_title(), null, message.delete_item_progress());
        final List<ItemResult> fkIntegrityMsgs = new ArrayList<ItemResult>();
        List<ItemBean> itemBeans = new ArrayList<ItemBean>();
        CommonUtil.setDeleteItemInfo(items, fkIntegrityMsgs, itemBeans);
        service.deleteItemBeans(itemBeans, override, Locale.getLanguage(),
                new SessionAwareAsyncCallback<List<ItemResult>>() {

            @Override
            public void onSuccess(List<ItemResult> msgs) {
                progressBar.close();

                if (fkIntegrityMsgs != null && fkIntegrityMsgs.size() > 0) {
                    msgs.addAll(fkIntegrityMsgs);
                }
                if (msgs != null && msgs.size() > 0) {
                    CommonUtil.displayMsgBoxWindow(new OperationMessageWindow(), msgs);
                } else {
                    MessageBox msgBox = new MessageBox();
                    msgBox.setTitle(MessagesFactory.getMessages().info_title());
                    msgBox.setButtons(""); //$NON-NLS-1$
                    msgBox.setIcon(MessageBox.INFO);
                    msgBox.setMessage(MessagesFactory.getMessages().delete_item_record_success(items.size()));
                    msgBox.show();
                    setTimeout(msgBox, 1000);
                }
                postDeleteAction.doAction();
                CallbackAction.getInstance().doAction(CallbackAction.HIERARCHY_DELETEITEM_CALLBACK, null,false);
            }

            @Override
            protected void doOnFailure(Throwable caught) {
                progressBar.close();
                String errorMsg = caught.getLocalizedMessage();
                if (errorMsg == null) {
                    if (Log.isDebugEnabled()) {
                        errorMsg = caught.toString(); // for debugging purpose
                    } else {
                        errorMsg = BaseMessagesFactory.getMessages().unknown_error();
                    }
                }
                MessageBox.alert(message.error_title(),
                        MultilanguageMessageParser.pickOutISOMessage(errorMsg), null);
            }
        });
    }

    private native void setTimeout(MessageBox msgBox, int millisecond)/*-{
		$wnd.setTimeout(function() {
			msgBox.@com.extjs.gxt.ui.client.widget.MessageBox::close()();
		}, millisecond);
    }-*/;
}
