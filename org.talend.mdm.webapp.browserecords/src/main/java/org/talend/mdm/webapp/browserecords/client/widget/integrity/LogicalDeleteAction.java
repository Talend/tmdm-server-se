package org.talend.mdm.webapp.browserecords.client.widget.integrity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.ItemResult;
import org.talend.mdm.webapp.base.client.widget.CallbackAction;
import org.talend.mdm.webapp.base.client.widget.OperationMessageWindow;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.shared.FKIntegrityResult;

import com.extjs.gxt.ui.client.widget.MessageBox;

/**
 * Wraps a logical delete operation.
 */
public class LogicalDeleteAction implements DeleteAction {
    
    private String url;
    
    public LogicalDeleteAction(String url) {
        this.url = url;
    }

    public void delete(Map<ItemBean, FKIntegrityResult> items, BrowseRecordsServiceAsync service, boolean override,
            final PostDeleteAction postDeleteAction) {
        final BrowseRecordsMessages message = MessagesFactory.getMessages();
        final MessageBox progressBar = MessageBox.wait(message.delete_item_title(), null, message.delete_item_progress());
        final List<ItemResult> fkIntegrityMsgs = new ArrayList<ItemResult>();
        List<ItemBean> itemBeans = new ArrayList<ItemBean>();
        CommonUtil.setDeleteItemInfo(items, fkIntegrityMsgs, itemBeans);
        
        service.logicalDeleteItems(itemBeans, url, override, new SessionAwareAsyncCallback<Void>() {
            public void onSuccess(Void arg0) {
                progressBar.close();
                if (fkIntegrityMsgs != null && fkIntegrityMsgs.size() > 0) {
                    CommonUtil.displayMsgBoxWindow(new OperationMessageWindow(), fkIntegrityMsgs);
                }
                postDeleteAction.doAction();
                CallbackAction.getInstance().doAction(CallbackAction.HIERARCHY_DELETEITEM_CALLBACK, null, false);
            }

            @Override
            protected void doOnFailure(Throwable caught) {
                progressBar.close();
                super.doOnFailure(caught);
            }
        });
    }

}
