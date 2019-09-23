/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.treedetail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.ItemResult;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.base.shared.util.CommonUtil;
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
import org.talend.mdm.webapp.browserecords.client.util.MessageUtil;
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
                        saveItem(root, isCreateForeignKey, foreignKeyName, ids, itemsDetailPanel, false);
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

    private static void saveItem(final ItemNodeModel model, final boolean isCreateForeignKey, final String foreignKeyName,
            final String ids, final ItemsDetailPanel itemsDetailPanel, final boolean isWarningApprovedBeforeSave) {
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
                    isWarningApprovedBeforeSave, Locale.getLanguage(), new SessionAwareAsyncCallback<ItemResult>() {

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
                            MessageBox messageBox = MessageUtil.generateMessageBox(result);
                            if (ItemResult.WARNING == result.getStatus()) {
                                messageBox.addCallback(new Listener<MessageBoxEvent>() {

                                    @Override
                                    public void handleEvent(MessageBoxEvent event) {
                                        if (event.getButtonClicked().getItemId().equals(Dialog.OK)) {
                                            saveItem(model, isCreateForeignKey, foreignKeyName, ids, itemsDetailPanel, true);
                                        }
                                    }
                                });
                                messageBox.show();
                                return;
                            } else {
                                MessageBox.alert(MessagesFactory.getMessages().info_title(),
                                        MessagesFactory.getMessages().save_success(), null);
                                if (widget instanceof ItemPanel && isCreated) {
                                    ItemsListPanel.getInstance().lastPage();
                                }
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

    /**
     * Return the true xpath of the relative xpath
     * @param xpath current fk field xpath
     * @param filterValue the current fk field's filter relative path
     * @return true xpath of the releative xpath
     */
    public static String findTargetRelativePathForCellFK(String xpath, String filterValue) {
        String[] rightPathArray = filterValue.split("/"); //$NON-NLS-1$
        String relativeMark = rightPathArray[0];
        if (".".equals(relativeMark)) { //$NON-NLS-1$
            return xpath + filterValue.substring(filterValue.indexOf(".")); //$NON-NLS-1$
        } else if ("..".equals(relativeMark)) { //$NON-NLS-1$
            return xpath.substring(0, xpath.lastIndexOf("/")) + filterValue  //$NON-NLS-1$
                    .substring(filterValue.indexOf("..") + 2); //$NON-NLS-1$
        }
        return CommonUtil.EMPTY;
    }

    /**
     * Find the xpath value from ItemNodeModel
     * @param filterValue fk filter's value(include the xpath)
     * @param currentPath current fk field xpath
     * @param itemNode current fk field's ItemNodeModel
     * @return xpath value from ItemNodeModel
     */
    public static String getXpathValue(String filterValue, String currentPath, ItemNodeModel itemNode) {
        String[] rightValueOrPathArray = filterValue.split("/"); //$NON-NLS-1$
        if (rightValueOrPathArray.length > 0) {
            String rightConcept = rightValueOrPathArray[0];
            if (rightConcept.equals(currentPath.split("/")[0])) { //$NON-NLS-1$
                List<String> duplicatedPathList = new ArrayList<String>();
                List<String> leftPathNodeList = new ArrayList<String>();
                List<String> rightPathNodeList = Arrays.asList(filterValue.split("/")); //$NON-NLS-1$
                String[] leftValueOrPathArray = currentPath.split("/"); //$NON-NLS-1$
                Collections.addAll(leftPathNodeList, leftValueOrPathArray);
                for (int i = 0; i < leftPathNodeList.size(); i++) {
                    if (i < rightPathNodeList.size() && leftPathNodeList.get(i).equals(rightPathNodeList.get(i))) {
                        duplicatedPathList.add(rightPathNodeList.get(i));
                    } else {
                        break;
                    }
                }
                leftPathNodeList.removeAll(duplicatedPathList);
                ItemNodeModel parentNode = itemNode;
                for (int i = 0; i < leftPathNodeList.size(); i++) {
                    parentNode = (ItemNodeModel) parentNode.getParent();
                }
                ItemNodeModel targetNode = findTarget(filterValue, parentNode);
                filterValue = getFilterValueFromTargetNode(targetNode);
            }
        }
        return filterValue;
    }

    private static String getFilterValueFromTargetNode(ItemNodeModel targetNode) {
        String filterValue;
        if (targetNode != null && targetNode.getObjectValue() != null) {
            Object targetValue = targetNode.getObjectValue();
            if (targetValue instanceof ForeignKeyBean) {
                ForeignKeyBean targetForeignKeyBean = (ForeignKeyBean) targetValue;
                filterValue = org.talend.mdm.webapp.base.shared.util.CommonUtil.unwrapFkValue(targetForeignKeyBean.getId());
            } else {
                filterValue = targetNode.getObjectValue().toString();
            }
        } else {
            filterValue = ""; //$NON-NLS-1$
        }
        return filterValue;
    }

    /**
     * Find the value of relative path 'relaterValue''s corresponding ItemNodeModel
     * @param filterValue the filter value is a relative path
     * @param filterOfXPath current filter corresponding xpath content
     * @param currentPath current field path
     * @param itemNode current field corresponding ItemNodeModel
     * @return the relative path's value
     */
    public static String findRelativePathValueForSelectFK(String filterValue, String filterOfXPath, String currentPath, ItemNodeModel itemNode) {
        if (filterOfXPath != null && filterOfXPath.split("/").length > 0 //$NON-NLS-1$
                && currentPath.split("/")[0].equals(filterOfXPath.split("/")[0])) { //$NON-NLS-1$//$NON-NLS-2$
            String[] rightPathArray = filterValue.split("/"); //$NON-NLS-1$
            String relativeMark = rightPathArray[0];
            String targetPath = itemNode.getTypePath();
            ItemNodeModel parentNode = itemNode;
            if (".".equals(relativeMark)) { //$NON-NLS-1$
                targetPath = targetPath + filterValue.substring(filterValue.indexOf("/")); //$NON-NLS-1$
            } else if ("..".equals(relativeMark)) { //$NON-NLS-1$
                parentNode = (ItemNodeModel) parentNode.getParent();
                targetPath = targetPath.substring(0, targetPath.lastIndexOf("/")); //$NON-NLS-1$
                targetPath = targetPath + filterValue.substring(filterValue.indexOf("/")); //$NON-NLS-1$
            }
            ItemNodeModel targetNode = ForeignKeyUtil.findTarget(targetPath, parentNode);
            filterValue = getFilterValueFromTargetNode(targetNode);
        }
        return filterValue;
    }

    /**
     * Find the corresponding ItemNodeModel according to the target path
     * eg: for Product Entity,
     *    findTarget("Product/Name", ProductItemNodeModel) ==> the Product/Name's ItemNodeModel.
     * @param targetPath the path need to find out
     * @param node current Entity's ItemNodeModel
     * @return the corresponding ItemNodeModel
     */
    public static ItemNodeModel findTarget(String targetPath, ItemNodeModel node) {
        List<ModelData> childrenList = node.getChildren();
        if (childrenList != null && childrenList.size() > 0) {
            for (ModelData modelData : childrenList) {
                ItemNodeModel child = (ItemNodeModel) modelData;
                String typePath = child.getTypePath();
                if (typePath.contains(":")) { //$NON-NLS-1$
                    String[] pathArray = typePath.split("/"); //$NON-NLS-1$
                    for (int j = 0; j < pathArray.length; j++) {
                        String nodePath = pathArray[j];
                        if (nodePath.contains(":")) { //$NON-NLS-1$
                            String[] nodePathArray = nodePath.split(":");
                            if (targetPath.contains("xsi:type")) {
                                pathArray[j] = nodePathArray[0] + "[@xsi:type=\"" + nodePathArray[1]
                                        + "\"]"; //$NON-NLS-1$  //$NON-NLS-2$
                            } else {
                                pathArray[j] = nodePathArray[0];
                            }
                        }
                    }
                    typePath = transformPath(pathArray);
                }
                if (targetPath.contains(typePath)) {
                    if (targetPath.equals(typePath)) {
                        return child;
                    } else {
                        return findTarget(targetPath, child);
                    }
                }
            }
        }
        return null;
    }

    public static String transformPath(String[] pathArray) {
        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 0; i < pathArray.length; i++) {
            pathBuilder.append(pathArray[i]);
            if (i < pathArray.length - 1) {
                pathBuilder.append("/"); //$NON-NLS-1$
            }
        }
        return pathBuilder.toString();
    }
}
