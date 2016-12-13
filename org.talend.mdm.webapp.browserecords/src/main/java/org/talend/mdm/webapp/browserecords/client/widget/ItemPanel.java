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

import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.browserecords.client.ToolBarFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail.DynamicTreeItem;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;
import org.talend.mdm.webapp.browserecords.shared.VisibleRuleResult;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Frame;

public class ItemPanel extends ContentPanel {

    private TreeDetail tree;

    private ContentPanel smartPanel = new ContentPanel();

    private ItemDetailToolBar toolBar;

    private ViewBean viewBean;

    private ItemBean item;

    private String operation;

    private Map<String, List<String>> initDataMap;

    private boolean isForeignKeyPanel;

    private ContentPanel contenPanel;

    private boolean isStaging;

    public ItemPanel(ItemsDetailPanel itemsDetailPanel) {
        tree = new TreeDetail(itemsDetailPanel);
        itemsDetailPanel.setTreeDetail(tree);
    }

    public ItemPanel(ViewBean viewBean, ItemBean item, String operation, ItemsDetailPanel itemsDetailPanel) {
        this(itemsDetailPanel);
        this.viewBean = viewBean;
        this.item = item;
        this.toolBar = ToolBarFactory.getInstance().createItemDetailToolBar(item, operation, viewBean, itemsDetailPanel);
        this.operation = operation;
        this.initUI(null);
    }

    public ItemPanel(ViewBean viewBean, ItemBean item, String operation, ItemsDetailPanel itemsDetailPanel, boolean openTab) {
        this(itemsDetailPanel);
        this.viewBean = viewBean;
        this.item = item;
        this.toolBar = ToolBarFactory.getInstance().createItemDetailToolBar(item, operation, viewBean, itemsDetailPanel, openTab);
        this.operation = operation;
        this.initUI(null);
    }

    public ItemPanel(ViewBean viewBean, ItemBean item, String operation, ContentPanel contenPanel, DynamicTreeItem root,
            ItemsDetailPanel itemsDetailPanel) {
        tree = new TreeDetail(itemsDetailPanel);
        this.viewBean = viewBean;
        this.item = item;
        this.toolBar = ToolBarFactory.getInstance().createItemDetailToolBar(item, operation, viewBean, itemsDetailPanel);
        this.operation = operation;
        this.isForeignKeyPanel = true;
        this.contenPanel = contenPanel;
        this.initUI(root);
    }

    public ItemPanel(ViewBean viewBean, ItemBean item, String operation, ContentPanel contenPanel, DynamicTreeItem root,
            ItemsDetailPanel itemsDetailPanel, boolean openTab) {
        tree = new TreeDetail(itemsDetailPanel);
        this.viewBean = viewBean;
        this.item = item;
        this.toolBar = ToolBarFactory.getInstance().createItemDetailToolBar(item, operation, viewBean, itemsDetailPanel, openTab);
        this.operation = operation;
        this.isForeignKeyPanel = true;
        this.contenPanel = contenPanel;
        this.initUI(root);
    }

    public ItemPanel(boolean isStaging, ViewBean viewBean, ItemBean item, String operation, ItemsDetailPanel itemsDetailPanel) {
        this(itemsDetailPanel);
        this.viewBean = viewBean;
        this.item = item;
        this.toolBar = ToolBarFactory.getInstance().createItemDetailToolBar(isStaging, item, operation, viewBean,
                itemsDetailPanel);
        this.operation = operation;
        this.isStaging = isStaging;
        this.initUI(null);
    }

    public ItemPanel(boolean isStaging, ViewBean viewBean, ItemBean item, String operation, ItemsDetailPanel itemsDetailPanel,
            boolean openTab) {
        this(itemsDetailPanel);
        this.viewBean = viewBean;
        this.item = item;
        this.toolBar = ToolBarFactory.getInstance().createItemDetailToolBar(isStaging, item, operation, false, viewBean,
                itemsDetailPanel, openTab);
        this.operation = operation;
        this.isStaging = isStaging;
        this.initUI(null);
    }

    public ItemPanel(boolean isStaging, ViewBean viewBean, ItemBean item, String operation, ContentPanel contenPanel,
            DynamicTreeItem root, ItemsDetailPanel itemsDetailPanel) {
        tree = new TreeDetail(itemsDetailPanel);
        this.viewBean = viewBean;
        this.item = item;
        this.toolBar = ToolBarFactory.getInstance().createItemDetailToolBar(isStaging, item, operation, viewBean,
                itemsDetailPanel);
        this.operation = operation;
        this.isForeignKeyPanel = true;
        this.contenPanel = contenPanel;
        this.isStaging = isStaging;
        this.initUI(root);
    }

    public ItemPanel(boolean isStaging, ViewBean viewBean, ItemBean item, String operation, ContentPanel contenPanel,
            DynamicTreeItem root, ItemsDetailPanel itemsDetailPanel, boolean openTab) {
        tree = new TreeDetail(itemsDetailPanel);
        this.viewBean = viewBean;
        this.item = item;
        this.toolBar = ToolBarFactory.getInstance().createItemDetailToolBar(isStaging, item, operation, true, viewBean,
                itemsDetailPanel, openTab);
        this.operation = operation;
        this.isForeignKeyPanel = true;
        this.contenPanel = contenPanel;
        this.isStaging = isStaging;
        this.initUI(root);
    }

    private void initUI(DynamicTreeItem root) {
        this.setBodyBorder(false);
        this.setHeaderVisible(false);
        this.setTopComponent(toolBar);
        this.setScrollMode(Scroll.NONE);
        this.setLayout(new RowLayout(Orientation.VERTICAL));
        if (!isForeignKeyPanel) {
            tree.setToolBar(toolBar);
            if (ItemDetailToolBar.CREATE_OPERATION.equals(operation)
                    || operation.equalsIgnoreCase(ItemDetailToolBar.BULK_UPDATE_OPERATION)) {
                tree.initTree(viewBean, null, initDataMap, operation, isStaging);
            } else if (ItemDetailToolBar.VIEW_OPERATION.equals(operation)) {
                tree.initTree(viewBean, item, isStaging);
            } else if (ItemDetailToolBar.DUPLICATE_OPERATION.equals(operation)) {
                tree.initTree(viewBean, item, initDataMap, operation, isStaging);
            } else if (ItemDetailToolBar.PERSONALEVIEW_OPERATION.equals(operation)
                    || ItemDetailToolBar.SMARTVIEW_OPERATION.equals(operation)) {
                tree.initTree(viewBean, item, isStaging);
            } else {
                tree.initTree(viewBean, null, isStaging);
            }
            tree.expand();
            this.add(tree, new RowData(1, 1));
        } else {
            tree.setRoot(root);
            tree.setViewBean(viewBean);
            this.add(contenPanel, new RowData(1, 1));
        }

        smartPanel.setVisible(false);
        smartPanel.setHeaderVisible(false);
        this.add(smartPanel, new RowData(1, 1));

        if (ItemDetailToolBar.SMARTVIEW_OPERATION.equals(operation)) {
            smartPanel.setVisible(true);
            tree.setVisible(false);
        } else if (ItemDetailToolBar.PERSONALEVIEW_OPERATION.equals(operation)) {
            smartPanel.setVisible(false);
            tree.setVisible(true);
        }

    }

    public void onUpdatePolymorphism(ComplexTypeModel typeModel) {
        tree.onUpdatePolymorphism(typeModel, operation);
    }

    public void onExecuteVisibleRule(List<VisibleRuleResult> visibleResults) {
        tree.onExecuteVisibleRule(visibleResults);
    }

    public ItemBean getItem() {
        return item;
    }

    public void setItem(ItemBean item) {
        this.item = item;
    }

    public TreeDetail getTree() {
        return tree;
    }

    public String getOperation() {
        return operation;
    }

    public void refreshTree() {
        tree.refreshTree(item);
        if (smartPanel.getWidget(0) != null && smartPanel.getWidget(0) instanceof Frame) {
            String url = ((Frame) smartPanel.getWidget(0)).getUrl();
            ((Frame) smartPanel.getWidget(0)).setUrl(url + "&" + Math.random()); //$NON-NLS-1$
            smartPanel.layout(true);
        }
    }

    public ContentPanel getSmartPanel() {
        return smartPanel;
    }

    public ItemDetailToolBar getToolBar() {
        return toolBar;
    }

    public ViewBean getViewBean() {
        return viewBean;
    }

    public void initTreeDetail(ViewBean viewBean, ItemBean item, String operation, boolean isStaging) {
        this.viewBean = viewBean;
        this.item = item;
        this.toolBar = ToolBarFactory.getInstance().createItemDetailToolBar(isStaging, item, operation, viewBean,
                tree.getItemsDetailPanel());
        this.operation = operation;
        this.isStaging = isStaging;
        this.initUI(null);
    }

    public void initTreeDetail(ViewBean viewBean, ItemBean item, Map<String, List<String>> initDataMap, String operation,
            boolean isStaging) {
        this.initDataMap = initDataMap;
        initTreeDetail(viewBean, item, operation, isStaging);

    }
}
