/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.treedetail;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.ItemResult;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsEvents;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.handler.ItemTreeHandler;
import org.talend.mdm.webapp.browserecords.client.handler.ItemTreeHandlingStatus;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.mvc.BrowseRecordsView;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsListPanel;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * DOC peili.liang class global comment. Detailled comment : support the foreignKey's link and create function.
 */
public class ForeignKeyUtil {

    public static void checkChange(final boolean isCreateForeignKey, final String foreignKeyName, final String ids,
            final ItemsDetailPanel itemsDetailPanel) {

        Widget widget = itemsDetailPanel.getFirstTabWidget();
        final ItemNodeModel root;
        if (widget instanceof ItemPanel) {
            root = ((ItemPanel) widget).getTree().getRootModel();
        } else {
            root = ((ForeignKeyTreeDetail) widget).getRootModel();
        }
        if (isChangeValue(root)) {
            MessageBox msgBox = MessageBox.confirm(MessagesFactory.getMessages().confirm_title(), MessagesFactory.getMessages()
                    .msg_confirm_save_tree_detail(root.getLabel()), new Listener<MessageBoxEvent>() {

                @Override
                public void handleEvent(MessageBoxEvent be) {
                    if (Dialog.YES.equals(be.getButtonClicked().getItemId())) {
                        saveItem(root, isCreateForeignKey, foreignKeyName, ids, itemsDetailPanel);
                    } else if (Dialog.NO.equals(be.getButtonClicked().getItemId())) {
                        displayForeignKey(isCreateForeignKey, foreignKeyName, ids, itemsDetailPanel);
                    }
                }
            });
            msgBox.getDialog().setWidth(550);
            msgBox.getDialog().setButtons(MessageBox.YESNOCANCEL);
        } else {
            displayForeignKey(isCreateForeignKey, foreignKeyName, ids, itemsDetailPanel);
        }

    }

    private static void saveItem(ItemNodeModel model, final boolean isCreateForeignKey, final String foreignKeyName,
            final String ids, final ItemsDetailPanel itemsDetailPanel) {
        final Widget widget = itemsDetailPanel.getFirstTabWidget();
        ViewBean viewBean = null;
        ItemBean itemBean = null;
        boolean isCreate = false;
        boolean validateSuccess = false;
        if (widget instanceof ItemPanel) {// save primary key
            viewBean = (ViewBean) BrowseRecords.getSession().get(UserSession.CURRENT_VIEW);
            ItemPanel itemPanel = (ItemPanel) widget;
            validateSuccess = true;
            itemBean = itemPanel.getItem();
            isCreate = itemPanel.getOperation().equals(ItemDetailToolBar.CREATE_OPERATION) ? true : false;
        } else if (widget instanceof ForeignKeyTreeDetail) { // save foreign key
            ForeignKeyTreeDetail fkDetail = (ForeignKeyTreeDetail) widget;
            if (fkDetail.validateTree()) {
                validateSuccess = true;
                itemBean = fkDetail.isCreate() ? new ItemBean(fkDetail.getViewBean().getBindingEntityModel().getConceptName(),
                        "", "") : itemBean; //$NON-NLS-1$ //$NON-NLS-2$
                isCreate = fkDetail.isCreate();
            }
        }
        final boolean isCreated = isCreate;
        if (validateSuccess) {
            BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);
            service.saveItem(viewBean, itemBean.getIds(),
                    (new ItemTreeHandler(model, viewBean, ItemTreeHandlingStatus.ToSave)).serializeItem(), isCreate,
                    Locale.getLanguage(), new SessionAwareAsyncCallback<ItemResult>() {

                        @Override
                        protected void doOnFailure(Throwable caught) {
                            String err = caught.getMessage();
                            if (err != null) {
                                MessageBox.alert(MessagesFactory.getMessages().error_title(),
                                        MultilanguageMessageParser.pickOutISOMessage(err), null);
                            } else {
                                super.doOnFailure(caught);
                            }
                        }

                        @Override
                        public void onSuccess(ItemResult result) {
                            MessageBox.alert(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages()
                                    .save_success(), null);
                            if (widget instanceof ItemPanel && isCreated) {
                                ItemsListPanel.getInstance().lastPage();
                            }
                            displayForeignKey(isCreateForeignKey, foreignKeyName, ids, itemsDetailPanel);
                        }
                    });
        } else {
            MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages().save_error(), null);
        }
    }

    public static void displayForeignKey(boolean isCreateForeignKey, final String foreignKeyName, final String ids,
            ItemsDetailPanel itemsDetailPanel) {
        Dispatcher dispatch = Dispatcher.get();
        AppEvent event = new AppEvent(BrowseRecordsEvents.CreateForeignKeyView, foreignKeyName);
        event.setData(BrowseRecordsView.ITEMS_DETAIL_PANEL, itemsDetailPanel);
        if (!isCreateForeignKey) {
            event = new AppEvent(BrowseRecordsEvents.ViewForeignKey);
            event.setData("ids", ids); //$NON-NLS-1$ 
            event.setData("concept", foreignKeyName); //$NON-NLS-1$
            event.setData(BrowseRecordsView.IS_STAGING, itemsDetailPanel.isStaging());
            event.setData(BrowseRecordsView.ITEMS_DETAIL_PANEL, itemsDetailPanel);
        }
        dispatch.dispatch(event);
    }

    private static boolean isChangeValue(ItemNodeModel model) {
        if (model.isChangeValue()) {
            return true;
        }
        for (ModelData node : model.getChildren()) {
            if (isChangeValue((ItemNodeModel) node)) {
                return true;
            }
        }
        return false;
    }

    public static Set<ItemNodeModel> getAllForeignKeyModelParent(ViewBean viewBean, ItemNodeModel node) {
        Set<ItemNodeModel> set = new HashSet<ItemNodeModel>();
        TypeModel tm = viewBean.getBindingEntityModel().getMetaDataTypes().get(node.getTypePath());
        if (tm.getForeignkey() != null) {
            set.add((ItemNodeModel) node.getParent());
        }
        for (ModelData child : node.getChildren()) {
            ItemNodeModel childModel = (ItemNodeModel) child;
            set.addAll(getAllForeignKeyModelParent(viewBean, childModel));
        }
        return set;
    }

    public static String transferXpathToLabel(TypeModel fkTypeModel, ViewBean pkViewBean) {
        String xp = fkTypeModel.getXpath();
        StringBuffer sb = new StringBuffer();
        // a/b/c/d
        Stack<String> stack = new Stack<String>();
        do {

            TypeModel tm = pkViewBean.getBindingEntityModel().getMetaDataTypes().get(xp);
            if (tm != null) {
                stack.push(tm.getLabel(Locale.getLanguage()));
            }
            xp = xp.substring(0, xp.lastIndexOf("/")); //$NON-NLS-1$

        } while (xp.indexOf("/") != -1); //$NON-NLS-1$
        boolean flag = true;

        while (!stack.isEmpty()) {
            if (flag) {
                flag = false;
            } else {
                sb.append("/"); //$NON-NLS-1$
            }
            sb.append(stack.pop());
        }
        return sb.toString();
    }

    public static String transferXpathToLabel(ItemNodeModel nodeModel) {
        String realXPath = ""; //$NON-NLS-1$
        ItemNodeModel current = nodeModel;
        while (current != null) {
            String name = current.getDynamicLabel() == null ? current.getLabel() : current.getDynamicLabel();
            realXPath = name + "/" + realXPath; //$NON-NLS-1$
            current = (ItemNodeModel) current.getParent();
        }
        return realXPath;
    }
}
