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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.ToolBarFactory;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ColumnElement;
import org.talend.mdm.webapp.browserecords.client.model.ColumnTreeLayoutModel;
import org.talend.mdm.webapp.browserecords.client.model.ColumnTreeModel;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyModel;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyValidateModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class ForeignKeyTreeDetail extends ContentPanel {

    private ViewBean viewBean;

    private ItemDetailToolBar toolBar;

    private ItemNodeModel model;

    private boolean isCreate;

    private ForeignKeyModel fkModel;

    private ColumnTreeLayoutModel columnLayoutModel;

    private TreeEx tree;

    private TreeItemEx root;

    private ForeignKeyRender fkRender;

    private Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();

    private HashMap<CountMapItem, Integer> occurMap = new HashMap<CountMapItem, Integer>();

    private DynamicTreeItem selectedItem;

    private ItemsDetailPanel itemsDetailPanel;

    private ClickHandler handler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
            if (selectedItem == null) {
                return;
            }
            // final DynamicTreeItem selected = (DynamicTreeItem) tree.getSelectedItem();
            final DynamicTreeItem parentItem = (DynamicTreeItem) selectedItem.getParentItem();

            final ItemNodeModel selectedModel = selectedItem.getItemNodeModel();
            final ItemNodeModel parentModel = (ItemNodeModel) selectedModel.getParent();

            final String xpath = selectedModel.getBindingPath();
            final String typePath = selectedModel.getTypePath();
            final CountMapItem countMapItem = new CountMapItem(xpath, parentModel);
            final int count = occurMap.containsKey(countMapItem) ? occurMap.get(countMapItem) : 0;
            final TypeModel typeModel = viewBean.getBindingEntityModel().getMetaDataTypes().get(typePath);
            if ("Add".equals(arg0.getRelativeElement().getId()) || "Clone".equals(arg0.getRelativeElement().getId())) { //$NON-NLS-1$ //$NON-NLS-2$               
                if (typeModel.getMaxOccurs() < 0 || count < typeModel.getMaxOccurs()) {
                    // clone a new item
                    ItemNodeModel model = selectedModel.clone("Clone".equals(arg0.getRelativeElement().getId()) ? true : false); //$NON-NLS-1$

                    int selectModelIndex = parentModel.indexOf(selectedModel);
                    parentModel.insert(model, selectModelIndex + 1);
                    // if it has default value
                    if (typeModel.getDefaultValue() != null) {
                        model.setObjectValue(typeModel.getDefaultValue());
                    }
                    parentItem.insertItem(buildGWTTree(model, true), parentItem.getChildIndex(selectedItem) + 1);
                    occurMap.put(countMapItem, count + 1);
                } else {
                    MessageBox.alert(MessagesFactory.getMessages().status(), MessagesFactory.getMessages()
                            .multiOccurrence_maximize(count), null);
                }
            } else {
                MessageBox.confirm(MessagesFactory.getMessages().confirm_title(), MessagesFactory.getMessages().delete_confirm(),
                        new Listener<MessageBoxEvent>() {

                            @Override
                            public void handleEvent(MessageBoxEvent be) {
                                if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                                    if (count > 1 && count > typeModel.getMinOccurs()) {
                                        parentItem.removeItem(selectedItem);
                                        parentModel.remove(selectedModel);
                                        occurMap.put(countMapItem, count - 1);
                                        if (parentModel.getChildCount() > 0) {
                                            ItemNodeModel child = (ItemNodeModel) parentModel.getChild(0);
                                            Field<?> field = fieldMap.get(child.getId().toString());
                                            if (field != null) {
                                                TreeDetailGridFieldCreator.updateMandatory(field, child, fieldMap);
                                            }
                                        }
                                    } else {
                                        MessageBox.alert(MessagesFactory.getMessages().status(), MessagesFactory.getMessages()
                                                .multiOccurrence_minimize(count), null);
                                    }
                                }
                            }
                        });
            }
        }
    };

    private ForeignKeyTreeDetail(ItemsDetailPanel itemsDetailPanel) {
        this.itemsDetailPanel = itemsDetailPanel;
        itemsDetailPanel.setTreeDetail(this);
        this.setHeaderVisible(false);
        this.setHeight(Window.getClientHeight() - (60 + 4 * 20));
        this.setScrollMode(Scroll.AUTO);
        this.setFkRender(new ForeignKeyRenderImpl());
        // display ForeignKey detail information,the tabPanel need to be clear. including create link refresh.
        itemsDetailPanel.clearContent();
    }

    public ForeignKeyTreeDetail(ForeignKeyModel fkModel, boolean isCreate, ItemsDetailPanel itemsDetailPanel) {
        this(itemsDetailPanel);
        this.isCreate = isCreate;
        this.fkModel = fkModel;
        this.model = fkModel.getNodeModel();
        this.viewBean = fkModel.getViewBean();
        this.columnLayoutModel = viewBean.getColumnLayoutModel();
        this.toolBar = ToolBarFactory.getInstance().createItemDetailToolBar(fkModel.getItemBean(),
                isCreate ? ItemDetailToolBar.CREATE_OPERATION : ItemDetailToolBar.VIEW_OPERATION, true, viewBean,
                itemsDetailPanel);
        this.setTopComponent(toolBar);
        itemsDetailPanel.clearContent();
        itemsDetailPanel.initBanner(fkModel.getItemBean().getPkInfoList(), fkModel.getItemBean().getDescription());
        // Update breadcrumb
        itemsDetailPanel.appendBreadCrumb(fkModel.getItemBean().getConcept(), fkModel.getItemBean().getLabel(), fkModel
                .getItemBean().getIds(), fkModel.getItemBean().getDisplayPKInfo().equals(fkModel.getItemBean().getLabel()) ? null
                : fkModel.getItemBean().getDisplayPKInfo());
        buildPanel(viewBean);
    }

    public void buildPanel(final ViewBean viewBean) {
        ItemNodeModel rootModel;
        if (this.model == null && this.isCreate) {
            List<ItemNodeModel> models = CommonUtil.getDefaultTreeModel(
                    viewBean.getBindingEntityModel().getMetaDataTypes().get(viewBean.getBindingEntityModel().getConceptName()),
                    Locale.getLanguage(), true, false, false);
            rootModel = models.get(0);
        } else {
            rootModel = this.model;
        }
        renderTree(rootModel);
    }

    private void renderTree(ItemNodeModel rootModel) {
        root = buildGWTTree(rootModel, false);
        tree = new TreeEx();
        tree.addItem(root);
        root.setState(true);
        if (root.getElement().getFirstChildElement() != null) {
            root.getElement().getFirstChildElement().setClassName("rootNode"); //$NON-NLS-1$
        }
        if (this.columnLayoutModel != null) {// TODO if create a new ForeignKey, tree UI can not render according to the
                                             // layout template
            HorizontalPanel hp = new HorizontalPanel();
            for (ColumnTreeModel ctm : columnLayoutModel.getColumnTreeModels()) {
                TreeEx tree = displayGWTTree(ctm);
                hp.add(tree);
                addTreeListener(tree);
            }
            hp.setHeight("570px"); //$NON-NLS-1$
            HorizontalPanel spacehp = new HorizontalPanel();
            spacehp.setHeight("10px"); //$NON-NLS-1$
            add(spacehp);
            add(hp);

        } else {
            add(tree);
            addTreeListener(tree);
        }
        String foreignKeyDeleteMessage = rootModel.get("foreignKeyDeleteMessage"); //$NON-NLS-1$
        if (foreignKeyDeleteMessage != null && foreignKeyDeleteMessage.trim().length() > 0) {
            MessageBox.alert(MessagesFactory.getMessages().warning_title(), foreignKeyDeleteMessage, null).getDialog()
                    .setWidth(600);
        }
    }

    // get selected item in tree
    private void addTreeListener(TreeEx tree) {
        tree.addSelectionHandler(new SelectionHandler<TreeItemEx>() {

            @Override
            public void onSelection(SelectionEvent<TreeItemEx> event) {
                selectedItem = (DynamicTreeItem) event.getSelectedItem();
            }
        });
    }

    public void refreshTree() {
        BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);
        final ItemBean item = fkModel.getItemBean();
        item.set("isRefresh", true); //$NON-NLS-1$
        service.getItemNodeModel(item, viewBean.getBindingEntityModel(), itemsDetailPanel.isStaging(), Locale.getLanguage(),
                new SessionAwareAsyncCallback<ItemNodeModel>() {

                    @Override
                    public void onSuccess(ItemNodeModel nodeModel) {
                        fkModel.setNodeModel(nodeModel);
                        model = nodeModel;
                        ForeignKeyTreeDetail.this.getItem(0).removeFromParent();
                        item.set("time", nodeModel.get("time")); //$NON-NLS-1$ //$NON-NLS-2$
                        itemsDetailPanel.clearChildrenContent();
                        renderTree(nodeModel);
                        ForeignKeyTreeDetail.this.layout();
                    }

                    @Override
                    protected void doOnFailure(Throwable caught) {
                        MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                                .refresh_error(), null);
                    }
                });

    }

    public ViewBean getViewBean() {
        return viewBean;
    }

    public void setViewBean(ViewBean viewBean) {
        this.viewBean = viewBean;
        buildPanel(viewBean);
    }

    private DynamicTreeItem buildGWTTree(final ItemNodeModel itemNode, boolean withDefaultValue) {
        Map<TypeModel, List<ItemNodeModel>> foreighKeyMap = new LinkedHashMap<TypeModel, List<ItemNodeModel>>();
        Map<TypeModel, ItemNodeModel> foreignKeyParentMap = new LinkedHashMap<TypeModel, ItemNodeModel>();
        DynamicTreeItem reuslt = buildGWTTree(itemNode, withDefaultValue, foreighKeyMap, foreignKeyParentMap);
        if (foreighKeyMap.size() > 0) {
            for (TypeModel model : foreighKeyMap.keySet()) {
                fkRender.RenderForeignKey(foreignKeyParentMap.get(model), foreighKeyMap.get(model), model, toolBar, viewBean,
                        this, itemsDetailPanel);
            }
        }
        return reuslt;
    }

    private DynamicTreeItem buildGWTTree(final ItemNodeModel itemNode, boolean withDefaultValue,
            Map<TypeModel, List<ItemNodeModel>> foreighKeyMap, Map<TypeModel, ItemNodeModel> foreignKeyParentMap) {
        DynamicTreeItem item = new DynamicTreeItem();
        item.setItemNodeModel(itemNode);
        item.setWidget(TreeDetailUtil.createWidget(itemNode, viewBean, fieldMap, handler, itemsDetailPanel));
        item.setUserObject(itemNode);

        List<ModelData> itemNodeChildren = itemNode.getChildren();
        if (itemNodeChildren != null && itemNodeChildren.size() > 0) {
            Map<String, TypeModel> metaDataTypes = viewBean.getBindingEntityModel().getMetaDataTypes();
            for (ModelData model : itemNode.getChildren()) {
                ItemNodeModel node = (ItemNodeModel) model;
                TypeModel typeModel = viewBean.getBindingEntityModel().getMetaDataTypes().get(node.getTypePath());
                if (withDefaultValue && typeModel.getDefaultValue() != null
                        && (node.getObjectValue() == null || node.getObjectValue().equals(""))) {
                    node.setObjectValue(typeModel.getDefaultValue());
                }
                if (this.isCreate && this.model != null && node.isKey()) {
                    node.setObjectValue(null); // id
                }

                boolean isFKDisplayedIntoTab = fkRender != null
                        && TreeDetail.isFKDisplayedIntoTab(node, typeModel, metaDataTypes);

                if (isFKDisplayedIntoTab) {
                    if (!foreighKeyMap.containsKey(typeModel)) {
                        foreighKeyMap.put(typeModel, new ArrayList<ItemNodeModel>());
                        foreignKeyParentMap.put(typeModel, itemNode);
                    }
                    foreighKeyMap.get(typeModel).add(node);
                } else {
                    TreeItemEx childItem = buildGWTTree(node, withDefaultValue, foreighKeyMap, foreignKeyParentMap);
                    if (childItem != null) {
                        item.addItem(childItem);
                        int count = 0;
                        CountMapItem countMapItem = new CountMapItem(node.getBindingPath(), itemNode);
                        if (occurMap.containsKey(countMapItem)) {
                            count = occurMap.get(countMapItem);
                        }
                        occurMap.put(countMapItem, count + 1);
                    }
                }
            }

            if (itemNodeChildren.size() > 0 && item.getChildCount() == 0) {
                return null; // All went to FK Tab, in that case return null so the tree item is not added
            }

            item.getElement().getStyle().setPaddingLeft(3.0, Unit.PX);
        }

        return item;
    }

    private TreeEx displayGWTTree(ColumnTreeModel treeModel) {
        TreeEx tree = new TreeEx();
        DynamicTreeItem treeRootNode = new DynamicTreeItem();
        tree.addItem(treeRootNode);

        if (root != null && root.getChildCount() > 0) {
            for (ColumnElement ce : treeModel.getColumnElements()) {
                for (int i = 0; i < root.getChildCount(); i++) {
                    TreeItemEx child = root.getChild(i);
                    ItemNodeModel node = (ItemNodeModel) child.getUserObject();
                    String xpath = node.getBindingPath();
                    String typePath = node.getTypePath();
                    if (("/" + xpath).equals(ce.getxPath())) { //$NON-NLS-1$
                        treeRootNode.addItem(child);
                        TypeModel typeModel = viewBean.getBindingEntityModel().getMetaDataTypes().get(typePath);
                        if (typeModel.getForeignkey() == null && (typeModel.getMaxOccurs() < 0 || typeModel.getMaxOccurs() > 1)) {
                            i--;
                            continue;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        if (treeRootNode.getElement().getFirstChildElement() != null) {
            treeRootNode.getElement().getFirstChildElement().setClassName("rootNode"); //$NON-NLS-1$
        }
        treeRootNode.setState(true);
        return tree;
    }

    public ItemNodeModel getRootModel() {
        return (ItemNodeModel) root.getUserObject();
    }

    public TreeItemEx getRoot() {
        return root;
    }

    public boolean isCreate() {
        return isCreate;
    }

    public ForeignKeyModel getFkModel() {
        return fkModel;
    }

    public void setFkModel(ForeignKeyModel fkModel) {
        this.fkModel = fkModel;
    }

    public static class DynamicTreeItem extends TreeItemEx {

        private ItemNodeModel itemNode;

        public DynamicTreeItem() {
            super();
        }

        private List<TreeItemEx> items = new ArrayList<TreeItemEx>();

        public void insertItem(DynamicTreeItem item, int beforeIndex) {
            int count = this.getChildCount();

            for (int i = 0; i < count; i++) {
                items.add(this.getChild(i));
            }

            items.add(beforeIndex, item);
            this.removeItems();

            for (int j = 0; j < items.size(); j++) {
                this.addItem(items.get(j));
            }
            items.clear();
        }

        public void removeItem(DynamicTreeItem item) {
            super.removeItem(item);
        }

        public void setItemNodeModel(ItemNodeModel treeNode) {
            itemNode = treeNode;
        }

        public ItemNodeModel getItemNodeModel() {
            return itemNode;
        }
    }

    private class CountMapItem {

        private String xpath;

        private ItemNodeModel parentModel;

        public CountMapItem(String xpath, ItemNodeModel parentModel) {
            this.xpath = xpath;
            this.parentModel = parentModel;
        }

        public String getXpath() {
            return this.xpath;
        }

        public ItemNodeModel getParentModel() {
            return this.parentModel;
        }

        @Override
        public int hashCode() {
            return xpath.length();
        }

        @Override
        public boolean equals(Object o) {
            CountMapItem item = (CountMapItem) o;
            return item.getXpath().equals(xpath) && item.getParentModel().equals(parentModel);
        }
    }

    public boolean validateTree() {
        boolean flag = true;
        ItemNodeModel rootNode = getRootModel();
        if (rootNode != null) {
            flag = validateNode(rootNode, flag);
        }
        return flag;
    }

    public boolean validateNode(ItemNodeModel rootNode, boolean flag) {

        if (rootNode.getChildren() != null && rootNode.getChildren().size() > 0) {
            Map<TypeModel, Integer> map = new HashMap<TypeModel, Integer>();
            Map<TypeModel, ForeignKeyValidateModel> fkValidateMap = new HashMap<TypeModel, ForeignKeyValidateModel>();
            for (ModelData model : rootNode.getChildren()) {

                ItemNodeModel node = (ItemNodeModel) model;
                if (!node.isValid() && node.getChildCount() == 0) {
                    TypeModel tm = viewBean.getBindingEntityModel().getMetaDataTypes().get(node.getTypePath());
                    boolean parentIsMayNull = rootNode.getParent() != null && !rootNode.isMandatory();
                    if (tm.getForeignkey() != null) {
                        // fk minOccurs check
                        if (!map.containsKey(tm)) {
                            map.put(tm, 0);
                        } else if (parentIsMayNull && fkValidateMap.get(tm).isNodeValid()) {
                            map.put(tm, map.get(tm) + 1);
                            continue;
                        }
                        map.put(tm, map.get(tm) + 1);
                        ForeignKeyValidateModel fkValidateModel = validateFK(node);
                        flag = fkValidateModel.isNodeValid();
                        fkValidateMap.put(tm, fkValidateModel);
                    } else {
                        MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                                .validation_error(node.getBindingPath()), null);
                        flag = false;
                    }

                }

                if (node.getChildren() != null && node.getChildren().size() > 0) {
                    flag = validateNode(node, flag);
                }

                if (!flag) {
                    break;
                }
            }
            if (flag) {
                for (TypeModel typeModel : fkValidateMap.keySet()) {
                    if (fkValidateMap.get(typeModel).isHaveNodeValue()) {
                        int count = map.get(typeModel);
                        if (typeModel.getMinOccurs() > count) {
                            MessageBox.alert(
                                    MessagesFactory.getMessages().error_title(),
                                    MessagesFactory.getMessages().fk_save_validate(
                                            ForeignKeyUtil.transferXpathToLabel(typeModel, viewBean), typeModel.getMinOccurs()),
                                    null);
                            flag = false;
                            break;
                        }
                    }
                }
            }

        }
        return flag;
    }

    private ForeignKeyValidateModel validateFK(ItemNodeModel node) {
        ItemNodeModel parent = (ItemNodeModel) node.getParent();
        boolean isHaveNodeValue = false;
        boolean isNodeValid = true;
        if (parent != null && parent.getParent() != null && !parent.isMandatory()) {
            for (ModelData model : parent.getChildren()) {
                ItemNodeModel nodeModel = (ItemNodeModel) model;
                if (nodeModel.getObjectValue() != null) {
                    if (nodeModel.getObjectValue() instanceof ForeignKeyBean) {
                        ForeignKeyBean fkBean = (ForeignKeyBean) nodeModel.getObjectValue();
                        if (fkBean.getId() == null || fkBean.getId().trim().length() == 0) {
                            continue;
                        } else {
                            isHaveNodeValue = true;
                            break;
                        }
                    } else if (nodeModel.getObjectValue().toString().trim().length() > 0) {
                        isHaveNodeValue = true;
                        break;
                    }
                }
            }
            if (isHaveNodeValue) {
                for (ModelData model : parent.getChildren()) {
                    ItemNodeModel nodeModel = (ItemNodeModel) model;
                    TypeModel tm = viewBean.getBindingEntityModel().getMetaDataTypes().get(node.getTypePath());
                    if (tm.getForeignkey() != null) {
                        if (!validateFKValue(nodeModel)) {
                            isNodeValid = false;
                            break;
                        }
                    }
                }
            }
        } else {
            isHaveNodeValue = true;
            isNodeValid = validateFKValue(node);
        }
        return new ForeignKeyValidateModel(isHaveNodeValue, isNodeValid);
    }

    private boolean validateFKValue(ItemNodeModel node) {
        if (!node.isValid() && node.getChildCount() == 0) {
            TypeModel tm = viewBean.getBindingEntityModel().getMetaDataTypes().get(node.getTypePath());
            // fk minOccurs check
            if (tm.getMinOccurs() >= 1) {
                // check value
                ForeignKeyBean fkBean = (ForeignKeyBean) node.getObjectValue();
                if (fkBean == null || fkBean.getId() == null) {
                    MessageBox.alert(
                            MessagesFactory.getMessages().error_title(),
                            MessagesFactory.getMessages().fk_save_validate(ForeignKeyUtil.transferXpathToLabel(tm, viewBean),
                                    tm.getMinOccurs()), null);
                    return false;
                }
            }
        }
        return true;
    }

    public ForeignKeyRender getFkRender() {
        return fkRender;
    }

    public void setFkRender(ForeignKeyRender fkRender) {
        this.fkRender = fkRender;
    }

    public Map<String, Field<?>> getFieldMap() {
        return fieldMap;
    }

    public ItemDetailToolBar getToolBar() {
        return toolBar;
    }

}
