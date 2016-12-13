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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.handler.ItemTreeHandler;
import org.talend.mdm.webapp.browserecords.client.handler.ItemTreeHandlingStatus;
import org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ColumnTreeLayoutModel;
import org.talend.mdm.webapp.browserecords.client.model.ColumnTreeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.util.MultiOccurrenceManager;
import org.talend.mdm.webapp.browserecords.client.util.ViewUtil;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeyField;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeySelector;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatDateField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatNumberField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatTextField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.PictureField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.UrlField;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;
import org.talend.mdm.webapp.browserecords.shared.VisibleRuleResult;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TreeDetail extends ContentPanel {

    private int ONE_SECOND = 1000;

    private ViewBean viewBean;

    private TreeEx tree = new TreeEx();

    private DynamicTreeItem root;

    private List<TreeEx> columnTrees = new ArrayList<TreeEx>();

    private Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();

    private final ForeignKeyRender fkRender = new ForeignKeyRenderImpl();

    private ItemDetailToolBar toolBar;

    private DynamicTreeItem selectedItem;

    private int renderedCounter = 0;

    private MessageBox progressBar;

    private List<RenderCompleteCallBack> renderCompleteCallBackList = new ArrayList<RenderCompleteCallBack>();

    private boolean isStaing;

    public ForeignKeyRender getFkRender() {
        return fkRender;
    }

    public void beginRender() {
        BrowseRecordsMessages msg = MessagesFactory.getMessages();
        progressBar = MessageBox.wait(msg.rendering_title(), msg.render_message(), msg.rendering_progress());
        Timer timer = new Timer() {

            @Override
            public void run() {
                closeProgressBar();
                Log.info("render detail timeout!"); //$NON-NLS-1$
            }
        };
        timer.schedule(ONE_SECOND * 300);
    }

    public void stepRenderCounter() {
        renderedCounter++;
    }

    public void resetRenderCounter() {
        renderedCounter = 0;
        closeProgressBar();
    }

    public void closeProgressBar() {
        if (progressBar != null) {
            progressBar.close();
        }
    }

    public void endRender() {
        if (renderedCounter > 0) {
            renderedCounter--;
        }
        if (renderedCounter == 0) {
            Iterator<RenderCompleteCallBack> it = renderCompleteCallBackList.iterator();
            while (it.hasNext()) {
                it.next().onSuccess();
                it.remove();
            }
            closeProgressBar();
        }
    }

    MultiOccurrenceManager multiManager;

    public DynamicTreeItem getSelectedItem() {
        return selectedItem;
    }

    public MultiOccurrenceManager getMultiManager() {
        return multiManager;
    }

    private ItemsDetailPanel itemsDetailPanel;

    public TreeDetail(ItemsDetailPanel itemsDetailPanel) {
        this.setHeaderVisible(false);
        this.setLayout(new FitLayout());
        this.setBorders(false);
        this.setBodyBorder(false);
        this.itemsDetailPanel = itemsDetailPanel;
        this.addListener(Events.Resize, new Listener<BoxComponentEvent>() {

            @Override
            public void handleEvent(BoxComponentEvent be) {
                if (root == null) {
                    return;
                }
                TreeDetail td = (TreeDetail) be.getSource();
                int width = be.getWidth();
                if (td.getWidget(0) instanceof TreeEx && columnTrees.size() == 0) {
                    td.setFiledWidth(root, width, 400, 0);
                } else if (td.getWidget(0) instanceof FlexTable && columnTrees.size() > 0) {
                    int columnWidth = width / columnTrees.size();
                    for (TreeEx columnTree : columnTrees) {
                        td.setFiledWidth(columnTree.getItem(0), columnWidth, 300, 0);
                    }
                    if (td.bwrap != null) {
                        td.bwrap.repaint();
                    }
                }
            }
        });
    }

    public void initTree(ViewBean viewBean, ItemBean itemBean, boolean isStaging) {
        initTree(viewBean, itemBean, null, null, isStaging);
    }

    public void initTree(final ViewBean viewBean, ItemBean itemBean, Map<String, List<String>> initDataMap,
            final String operation, boolean isStaging) {
        this.isStaing = isStaging;
        this.viewBean = viewBean;
        if (itemBean == null) {
            buildPanel(operation, initDataMap);
        } else {
            final BrowseRecordsServiceAsync itemService = getItemService();
            BrowseRecordsMessages msg = MessagesFactory.getMessages();
            final MessageBox loadProgress = MessageBox.wait(msg.load_title(), msg.load_message(), msg.load_progress());
            itemService.getItemNodeModel(itemBean, viewBean.getBindingEntityModel(), isStaging, Locale.getLanguage(),
                    new SessionAwareAsyncCallback<ItemNodeModel>() {

                        @Override
                        public void onSuccess(final ItemNodeModel node) {

                            itemService.executeVisibleRule(viewBean, (new ItemTreeHandler(node, TreeDetail.this.viewBean,
                                    ItemTreeHandlingStatus.InUse)).serializeItem(),
                                    new SessionAwareAsyncCallback<List<VisibleRuleResult>>() {

                                        @Override
                                        public void onSuccess(List<VisibleRuleResult> visibleResults) {
                                            if (visibleResults != null) {
                                                recrusiveSetItems(visibleResults, node);
                                            }
                                            loadProgress.close();
                                            renderTree(node, operation);
                                        }
                                    });
                        }

                        @Override
                        protected void doOnFailure(Throwable caught) {
                            loadProgress.close();
                            super.doOnFailure(caught);
                        }
                    });
        }
    }

    private void buildPanel(final String operation, Map<String, List<String>> initDataMap) {
        getItemService().createDefaultItemNodeModel(viewBean, initDataMap, Locale.getLanguage(),
                new SessionAwareAsyncCallback<ItemNodeModel>() {

                    @Override
                    public void onSuccess(final ItemNodeModel result) {
                        getItemService().executeVisibleRule(viewBean,
                                (new ItemTreeHandler(result, viewBean, ItemTreeHandlingStatus.InUse)).serializeItem(),
                                new SessionAwareAsyncCallback<List<VisibleRuleResult>>() {

                                    @Override
                                    public void onSuccess(List<VisibleRuleResult> visibleResults) {
                                        if (visibleResults != null) {
                                            recrusiveSetItems(visibleResults, result);
                                        }
                                        renderTree(result, operation);
                                    }
                                });
                    }
                });
    }

    private boolean isFirstKey = true;

    public DynamicTreeItem buildGWTTree(final ItemNodeModel itemNode, DynamicTreeItem item, final boolean withDefaultValue,
            final String operation) {
        if (item == null) {
            item = new DynamicTreeItem();
            item.setItemNodeModel(itemNode);
            if (ItemDetailToolBar.DUPLICATE_OPERATION.equals(operation)) {
                if (itemNode.isKey()) {
                    if (isFirstKey) {
                        itemNode.setObjectValue(null);
                        isFirstKey = false;
                    }
                }
            }

            MultiOccurrenceChangeItem itemWidget = TreeDetailUtil.createWidget(itemNode, viewBean, fieldMap, null, operation,
                    itemsDetailPanel);
            itemWidget.setTreeDetail(this);
            item.setWidget(itemWidget);
        }

        List<ModelData> itemNodeChildren = itemNode.getChildren();
        TypeModel typeModel = viewBean.getBindingEntityModel().getMetaDataTypes().get(itemNode.getTypePath());
        boolean isAutoExpand = typeModel.isAutoExpand();
        if (itemNodeChildren != null && itemNodeChildren.size() > 0) {
            if (isAutoExpand || itemNode.getParent() == null) {
                renderChildren(itemNode, item, withDefaultValue, operation);
            } else {
                List<TypeModel> typeModels = ((ComplexTypeModel) typeModel).getSubTypes();
                if (itemNode.getRealType() != null) {
                    typeModels = ((ComplexTypeModel) typeModel).getRealType(itemNode.getRealType()).getSubTypes();
                }

                final DynamicTreeItem parentItem = item;
                if (typeModels.size() == 1) {
                    TypeModel fkType = typeModels.get(0);
                    if (fkType.getForeignkey() != null && fkType.getForeignkey().trim().length() > 0 && !fkType.isNotSeparateFk()) {
                        // For case TMDM-5439,TMDM-8801 and TMDM-8920
                        // when element model type is Polymorphism and it contains FK,this FK must render in main tab.
                        boolean isRenderInMain = false;
                        if (typeModel instanceof ComplexTypeModel) {
                            List<ComplexTypeModel> reusableTypes = ((ComplexTypeModel) typeModel).getReusableComplexTypes();
                            if (reusableTypes != null && reusableTypes.size() > 0) {
                                isRenderInMain = true;
                            }
                        }
                        item.getElement().setPropertyBoolean("EmptyFkContainer", !isRenderInMain); //$NON-NLS-1$
                    }
                }

                if (item.getElement().getPropertyBoolean("EmptyFkContainer")) { //$NON-NLS-1$
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        @Override
                        public void execute() {
                            renderChildren(itemNode, parentItem, withDefaultValue, operation);
                        }
                    });
                } else {
                    item.addItem(new GhostTreeItem());
                    item.setAutoExpandHandler(new AutoExpandHandler() {

                        @Override
                        public void autoExpand() {
                            BrowseRecordsMessages msg = MessagesFactory.getMessages();
                            progressBar = MessageBox.wait(msg.rendering_title(), msg.render_message(), msg.rendering_progress());
                            renderChildren(itemNode, parentItem, withDefaultValue, operation);
                            renderCompleteCallBackList.add(new RenderCompleteCallBack() {

                                @Override
                                public void onSuccess() {
                                    multiManager.addMultiOccurrenceNode(parentItem);
                                    multiManager.warningAllItems();
                                    multiManager.handleOptIcons();
                                }
                            });
                        }
                    });
                }
            }
        }
        item.setUserObject(itemNode);
        item.setVisible(itemNode.isVisible());
        item.setState(isAutoExpand);
        item.getElement().getStyle().setPaddingLeft(3.0, Unit.PX);

        return item;
    }

    private void renderChildren(ItemNodeModel itemNode, DynamicTreeItem item, boolean withDefaultValue, String operation) {
        IncrementalBuildTree incCommand = new IncrementalBuildTree(this, itemNode, viewBean, withDefaultValue, operation, item);
        if (itemNode.getParent() == null) {
            addCommand(incCommand, true);
        } else {
            addCommand(incCommand, false);
        }
    }

    public static void addCommand(IncrementalBuildTree command, boolean sync) {
        if (sync || command.getChildCount() < IncrementalBuildTree.GROUP_SIZE) {
            while (command.execute()) {
                ;
            }
        } else {
            DeferredCommand.addCommand(command);
        }
    }

    static boolean isFKDisplayedIntoTab(ItemNodeModel node, TypeModel typeModel, Map<String, TypeModel> metaDataTypes) {
        String typeModelFK = typeModel.getForeignkey();
        if (typeModelFK == null) {
            return false; // Not a FK
        }

        ItemNodeModel parentNode = (ItemNodeModel) node.getParent();
        if (parentNode == null) {
            return false; // It is root
        }
        if (parentNode.getParent() == null) {
            return !typeModel.isNotSeparateFk();
        }

        TypeModel parentType = metaDataTypes.get(parentNode.getTypePath());
        assert parentType instanceof ComplexTypeModel : "any node's parent type must be ComplexTypeModel"; //$NON-NLS-1$

        List<TypeModel> subTypes = ((ComplexTypeModel) parentType).getSubTypes();
        if (subTypes != null && subTypes.size() == 1) {
            return !typeModel.isNotSeparateFk();
        }
        return false;
    }

    public void onUpdatePolymorphism(final ComplexTypeModel typeModel, final String operation) {
        // DynamicTreeItem item = (DynamicTreeItem) tree.getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        selectedItem.setState(true);
        final ItemNodeModel treeNode = selectedItem.getItemNodeModel();

        Set<ItemNodeModel> fkContainers = ForeignKeyUtil.getAllForeignKeyModelParent(viewBean, treeNode);
        for (ItemNodeModel fkContainer : fkContainers) {
            fkRender.removeRelationFkPanel(fkContainer);
        }

        treeNode.setRealType(typeModel.getName());
        multiManager.removeMultiOccurrenceNode(selectedItem);
        selectedItem.removeItems();

        String contextPath = CommonUtil.getRealXPath(treeNode);
        String typePath = CommonUtil.getRealTypePath(treeNode);
        typePath = typePath.replaceAll(":" + treeNode.getRealType() + "$", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ItemNodeModel rootNode = (ItemNodeModel) root.getUserObject();
        treeNode.removeAll();
        String xml = (new ItemTreeHandler(rootNode, viewBean, ItemTreeHandlingStatus.InUse)).serializeItem();

        getItemService().createSubItemNodeModel(viewBean, xml, typePath, contextPath, treeNode.getRealType(), isStaing,
                UrlUtil.getLanguage(), new SessionAwareAsyncCallback<ItemNodeModel>() {

                    @Override
                    public void onSuccess(ItemNodeModel result) {
                        ModelData[] children = result.getChildren().toArray(new ModelData[0]);
                        for (ModelData child : children) {
                            treeNode.add(child);
                        }
                        buildGWTTree(treeNode, selectedItem, false, operation);
                        if (typeModel.isAutoExpand()) {
                            renderCompleteCallBackList.add(new RenderCompleteCallBack() {

                                @Override
                                public void onSuccess() {
                                    multiManager.addMultiOccurrenceNode(selectedItem);
                                    multiManager.warningAllItems();
                                    multiManager.handleOptIcons();
                                }
                            });
                        }
                        if (selectedItem.getWidget() instanceof MultiOccurrenceChangeItem
                                && ((MultiOccurrenceChangeItem) selectedItem.getWidget()).isAddRemoveHandlerNull()) {
                            multiManager.addMultiOccurrenceNode(selectedItem);
                            multiManager.warningAllItems();
                            multiManager.handleOptIcons();
                        }
                    }
                });
    }

    public void onExecuteVisibleRule(List<VisibleRuleResult> visibleResults) {
        for (VisibleRuleResult visibleResult : visibleResults) {
            if (columnTrees.size() > 0) {
                for (TreeEx columnTree : columnTrees) {
                    recrusiveSetItems(visibleResult, (DynamicTreeItem) columnTree.getItem(0));
                }
            } else {
                DynamicTreeItem rootItem = (DynamicTreeItem) tree.getItem(0);
                recrusiveSetItems(visibleResult, rootItem);
            }
            fkRender.recrusiveSetItems(visibleResult);
        }
    }

    private void renderTree(ItemNodeModel rootModel) {
        renderTree(rootModel, null);
    }

    private void renderTree(ItemNodeModel rootModel, String operation) {
        multiManager = new MultiOccurrenceManager(viewBean.getBindingEntityModel().getMetaDataTypes(), this);
        beginRender();
        renderCompleteCallBackList.add(new RenderCompleteCallBack() {

            @Override
            public void onSuccess() {
                if (columnTrees.size() > 0) {
                    for (TreeEx columnTree : columnTrees) {
                        DynamicTreeItem rootItem = (DynamicTreeItem) columnTree.getItem(0);
                        multiManager.addMultiOccurrenceNode(rootItem);
                    }
                } else {
                    multiManager.addMultiOccurrenceNode(root);
                }
                multiManager.warningAllItems();
                multiManager.handleOptIcons();
            }
        });

        ColumnTreeLayoutModel columnLayoutModel = viewBean.getColumnLayoutModel();
        ItemNodeModel customModel = rootModel;
        if (columnLayoutModel != null && columnLayoutModel.getColumnTreeModels().size() > 0) {
            customModel = ViewUtil.transformToCustomLayoutModel(rootModel, columnLayoutModel.getColumnTreeModels());
        }
        root = buildGWTTree(customModel, null, false, operation);
        isFirstKey = true;
        root.setState(true);
        if (root.getElement().getFirstChildElement() != null) {
            root.getElement().getFirstChildElement().setClassName("rootNode"); //$NON-NLS-1$
        }

        if (columnLayoutModel != null) {// TODO if create a new PrimaryKey, tree UI should not render according to the
                                        // layout template
            FlexTable htable = new FlexTable();
            htable.setHeight("100%"); //$NON-NLS-1$
            columnTrees.clear();
            int columnWidth = this.getWidth() / columnLayoutModel.getColumnTreeModels().size();
            int columnNum = 0;
            for (ColumnTreeModel ctm : columnLayoutModel.getColumnTreeModels()) {
                // Tree columnTree = displayGWTTree(ctm);
                TreeEx columnTree = ViewUtil.transformToCustomLayout(root, ctm, viewBean);
                columnTree.getElement().getStyle().setOverflow(Overflow.HIDDEN);
                if (columnWidth > 500) {
                    this.setFiledWidth(columnTree.getItem(0), columnWidth, 300, 0);
                }
                columnTrees.add(columnTree);
                htable.setWidget(0, columnNum, columnTree);
                ViewUtil.setStyleAttribute(htable.getCellFormatter().getElement(0, columnNum), ctm.getStyle());
                htable.getCellFormatter().getElement(0, columnNum).setAttribute("vAlign", "top"); //$NON-NLS-1$//$NON-NLS-2$
                columnNum++;
                addTreeListener(columnTree);
            }
            add(htable);
            if (this.bwrap != null && this.bwrap.getChildElement(0) != null) {
                this.bwrap.getChildElement(0).getStyle().setOverflow(Overflow.AUTO);
            }
        } else {
            tree = new TreeEx();
            tree.getElement().setId("TreeDetail-tree"); //$NON-NLS-1$ 
            tree.addItem(root);
            this.setFiledWidth(root, this.getWidth(), 400, 0);
            add(tree);
            addTreeListener(tree);
        }
        this.layout(true);
        String foreignKeyDeleteMessage = rootModel.get("foreignKeyDeleteMessage"); //$NON-NLS-1$
        if (foreignKeyDeleteMessage != null && foreignKeyDeleteMessage.trim().length() > 0) {
            MessageBox.alert(MessagesFactory.getMessages().warning_title(), foreignKeyDeleteMessage, null).getDialog()
                    .setWidth(600);
        }
    }

    private int getLevel(TreeItemEx item) {
        int level = -1;
        if (item == null) {
            return level;
        }

        TreeItemEx current = item;
        while (current != null) {
            level++;
            current = current.getParentItem();
        }
        return level;
    }

    public void adjustFieldWidget(TreeItemEx item) {
        int level = getLevel(item);
        ColumnTreeLayoutModel columnLayoutModel = viewBean.getColumnLayoutModel();
        if (columnLayoutModel != null) {
            int columnWidth = this.getWidth() / columnLayoutModel.getColumnTreeModels().size();
            if (columnWidth > 500) {
                setFiledWidth(item, columnWidth, 300, level);
            }
        } else {
            setFiledWidth(item, this.getWidth(), 400, level);
        }
    }

    private void setFiledWidth(TreeItemEx item, int width, int offset, int level) {
        if (item.getWidget() instanceof HorizontalPanel) {
            HorizontalPanel hp = (HorizontalPanel) item.getWidget();
            if (hp.getWidgetCount() > 1) {
                Widget field = hp.getWidget(1);
                int size = width - (offset + 19 * level);
                if (field instanceof TextField) {
                    ((TextField<?>) field).setWidth(size > 200 ? size : 200);
                } else if (field instanceof SimpleComboBox) {
                    ((SimpleComboBox<?>) field).setWidth(size > 200 ? size : 200);
                } else if (field instanceof FormatNumberField) {
                    ((FormatNumberField) field).setWidth(size > 200 ? size : 200);
                } else if (field instanceof FormatDateField) {
                    ((FormatDateField) field).setWidth(size > 200 ? size : 200);
                } else if (field instanceof ForeignKeyField) {
                    ((ForeignKeyField) field).setWidth(size > 200 ? size : 200);
                }
            }
        }

        for (int i = 0; i < item.getChildCount(); i++) {
            TreeItemEx subItem = item.getChild(i);
            setFiledWidth(subItem, width, offset, level + 1);
        }
    }

    // get selected item in tree
    private void addTreeListener(TreeEx tree) {
        tree.addSelectionHandler(new SelectionHandler<TreeItemEx>() {

            @Override
            public void onSelection(SelectionEvent<TreeItemEx> event) {
                if (event.getSelectedItem() instanceof DynamicTreeItem) {
                    selectedItem = (DynamicTreeItem) event.getSelectedItem();
                }
            }
        });
    }

    public void recrusiveSetItems(List<VisibleRuleResult> visibleResults, ItemNodeModel itemNode) {
        if (itemNode != null) {
            String realPath = CommonUtil.getRealXPath(itemNode);

            boolean maybeUse = false;
            Iterator<VisibleRuleResult> iter = visibleResults.iterator();

            while (iter.hasNext()) {
                VisibleRuleResult vr = iter.next();
                if (vr.getXpath().equals(realPath)) {
                    itemNode.setVisible(vr.isVisible() && !itemNode.isHide());
                    iter.remove();
                }
                if (vr.getXpath().startsWith(realPath)) {
                    maybeUse = true;
                    break;
                }
            }
            if (!maybeUse) {
                return;
            }
        }

        if (visibleResults.size() == 0) {
            return;
        }

        for (int i = 0; i < itemNode.getChildCount(); i++) {
            ItemNodeModel childNode = (ItemNodeModel) itemNode.getChild(i);
            recrusiveSetItems(visibleResults, childNode);
            if (visibleResults.size() == 0) {
                break;
            }
        }
    }

    protected void recrusiveSetItems(VisibleRuleResult visibleResult, DynamicTreeItem rootItem) {
        if (rootItem.getItemNodeModel() != null) {
            String realPath = CommonUtil.getRealXPath(rootItem.getItemNodeModel());
            if (realPath.equals(visibleResult.getXpath())) {
                rootItem.setVisible(visibleResult.isVisible() && !rootItem.getItemNodeModel().isHide());
                return;
            }

            if (!visibleResult.getXpath().startsWith(realPath)) {
                return;
            }
        }

        if (rootItem.getChildCount() == 0) {
            return;
        }

        for (int i = 0; i < rootItem.getChildCount(); i++) {
            if (rootItem.getChild(i) instanceof DynamicTreeItem) {
                DynamicTreeItem item = (DynamicTreeItem) rootItem.getChild(i);
                recrusiveSetItems(visibleResult, item);
            }
        }
    }

    interface AutoExpandHandler {

        void autoExpand();
    }

    public class GhostTreeItem extends DynamicTreeItem {
    }

    public static class DynamicTreeItem extends TreeItemEx {

        private ItemNodeModel itemNode;

        private AutoExpandHandler autoExpandHandler;

        public DynamicTreeItem() {
            super();
            if (GXT.isIE) {
                getContentElement().getStyle().setDisplay(Display.BLOCK);
            }
            this.getElement().getStyle().setPaddingTop(0D, Unit.PX);
            this.getElement().getStyle().setPaddingBottom(0D, Unit.PX);
        }

        public void setAutoExpandHandler(AutoExpandHandler autoExpandHandler) {
            this.autoExpandHandler = autoExpandHandler;
        }

        private native Element getContentElement()/*-{
			return this.@org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx::contentElem;
        }-*/;

        public void insertItem(DynamicTreeItem item, int beforeIndex) {
            // Detach item from existing parent.
            if ((item.getParentItem() != null) || (item.getTree() != null)) {
                item.remove();
            }

            if (_getChildren() == null) {
                _initChildren();
            }

            // Logical attach.
            item._setParentItem(this);

            _getChildren().add(beforeIndex, item);

            DOM.appendChild(_getChildSpanElem(), item.getElement());
            Element beforeEl = DOM.getChild(_getChildSpanElem(), beforeIndex);
            DOM.insertBefore(_getChildSpanElem(), item.getElement(), beforeEl);
            // Adopt.
            item._setTree(this.getTree());

            if (_getChildren().size() == 1) {
                _updateState(false, false);

            }
        }

        private native ArrayList<TreeItemEx> _getChildren()/*-{
			return this.@org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx::children;
        }-*/;

        private native void _initChildren() /*-{
			this
					.@org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx::initChildren()
        }-*/;

        private native void _setParentItem(TreeItemEx parent)/*-{
			this.@org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx::setParentItem(Lorg/talend/mdm/webapp/browserecords/client/widget/treedetail/TreeItemEx;)(parent);
        }-*/;

        private native void _updateState(boolean animate, boolean updateTreeSelection)/*-{
			this.@org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx::updateState(ZZ)(animate, updateTreeSelection);
        }-*/;

        private native void _setTree(TreeEx tree)/*-{
			this.@org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx::setTree(Lorg/talend/mdm/webapp/browserecords/client/widget/treedetail/TreeEx;)(tree);
        }-*/;

        private native Element _getChildSpanElem()/*-{
			return this.@org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx::childSpanElem;
        }-*/;

        public void removeItem(DynamicTreeItem item) {
            super.removeItem(item);
        }

        @Override
        public void setState(boolean open, boolean fireEvents) {
            // Only do the physical update if it changes
            if (this.getOpen() != open) {
                setOpen(open);
                __updateState(true, true);
                if (fireEvents && this.getTree() != null) {
                    fireTreeStateChanged(this.getTree(), this, open);
                }
            }
            if (open && autoExpandHandler != null) {
                this.removeItems();
                autoExpandHandler.autoExpand();
                autoExpandHandler = null;
            }

        }

        private native boolean getOpen()/*-{
			return this.@org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx::open;
        }-*/;

        private native void setOpen(boolean open)/*-{
			this.@org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx::open = open;
        }-*/;

        private native void __updateState(boolean animate, boolean updateTreeSelection)/*-{
			this.@org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx::updateState(ZZ)(animate, updateTreeSelection);
        }-*/;

        private native void fireTreeStateChanged(TreeEx tree, TreeItemEx item, boolean open)/*-{
			tree.@org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeEx::fireStateChanged(Lorg/talend/mdm/webapp/browserecords/client/widget/treedetail/TreeItemEx;Z)(item,open);
        }-*/;

        public void setItemNodeModel(ItemNodeModel treeNode) {
            itemNode = treeNode;
        }

        public ItemNodeModel getItemNodeModel() {
            return itemNode;
        }
    }

    interface RenderCompleteCallBack {

        void onSuccess();
    }

    protected BrowseRecordsServiceAsync getItemService() {

        BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);
        return service;

    }

    public TreeEx getTree() {
        return tree;
    }

    public void refreshTree(final ItemBean item) {
        item.set("isRefresh", true); //$NON-NLS-1$
        getItemService().getItemNodeModel(item, viewBean.getBindingEntityModel(), isStaing, Locale.getLanguage(),
                new SessionAwareAsyncCallback<ItemNodeModel>() {

                    @Override
                    public void onSuccess(final ItemNodeModel node) {
                        TreeDetail.this.removeAll();
                        item.setLastUpdateTime(node);
                        itemsDetailPanel.clearChildrenContent();

                        if (node.isHasVisiblueRule()) {
                            getItemService().executeVisibleRule(
                                    viewBean,
                                    (new ItemTreeHandler(node, TreeDetail.this.viewBean, ItemTreeHandlingStatus.InUse))
                                            .serializeItem(), new SessionAwareAsyncCallback<List<VisibleRuleResult>>() {

                                        @Override
                                        public void onSuccess(List<VisibleRuleResult> visibleResults) {
                                            if (visibleResults != null) {
                                                recrusiveSetItems(visibleResults, node);
                                            }
                                            renderTree(node);
                                        }
                                    });
                        } else {
                            renderTree(node);
                        }
                    }

                    @Override
                    protected void doOnFailure(Throwable caught) {
                        MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                                .refresh_error(), null);
                    }
                });
    }

    public class CountMapItem {

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

    private boolean hasVisibleRule(TypeModel typeModel) {
        if (typeModel.isHasVisibleRule()) {
            return true;
        }

        if (!typeModel.isSimpleType()) {
            ComplexTypeModel complexModel = (ComplexTypeModel) typeModel;
            List<TypeModel> children = complexModel.getSubTypes();

            for (TypeModel model : children) {
                boolean childVisibleRule = hasVisibleRule(model);

                if (childVisibleRule) {
                    return true;
                }
            }
        }

        return false;
    }

    public void setToolBar(ItemDetailToolBar toolBar) {
        this.toolBar = toolBar;
    }

    public ItemDetailToolBar getToolBar() {
        return toolBar;
    }

    public Map<String, Field<?>> getFieldMap() {
        return fieldMap;
    }

    public void setRoot(DynamicTreeItem root) {
        this.root = root;
        if (root != null && root.getElement() != null) {
            root.getElement().setId("TreeDetail-root"); //$NON-NLS-1$
        }
    }

    public ItemNodeModel getRootModel() {
        return (ItemNodeModel) root.getUserObject();
    }

    public TreeItemEx getRoot() {
        return root;
    }

    public void setViewBean(ViewBean viewBean) {
        this.viewBean = viewBean;
    }

    public ViewBean getViewBean() {
        return viewBean;
    }

    public ItemsDetailPanel getItemsDetailPanel() {
        return itemsDetailPanel;
    }

    public void makeWarning(TreeItemEx item) {
        ItemNodeModel itemNodeModel = ((DynamicTreeItem) item).getItemNodeModel();
        if (itemNodeModel != null) {
            if (!isRoot(itemNodeModel)) {
                ItemNodeModel parent = (ItemNodeModel) itemNodeModel.getParent();
                if (item.getWidget() instanceof HorizontalPanel) {
                    HorizontalPanel hp = (HorizontalPanel) item.getWidget();
                    if (hp.getWidgetCount() > 1) {
                        Widget field = hp.getWidget(1);
                        if (itemNodeModel.isMandatory()) {
                            if (isRoot(parent)) {
                                makeWarning4Mandatory(field, true, itemNodeModel);
                            } else {
                                if (parent.isMandatory()) {
                                    makeWarning4Mandatory(field, true, itemNodeModel);
                                } else {
                                    makeWarning4Mandatory(field, false, itemNodeModel);
                                }
                            }
                        } else {
                            if (field instanceof FormatTextField) {
                                FormatTextField ftf = (FormatTextField) field;
                                String value = ftf.getValue();
                                if (value != null) {
                                    ftf.setValidateFlag(true);
                                    ftf.validateValue(value);
                                    ftf.setValidateFlag(false);
                                }

                            } else if (field instanceof FormatNumberField) {
                                FormatNumberField fnf = (FormatNumberField) field;
                                Number value = fnf.getValue();
                                if (value != null) {
                                    fnf.setValidateFlag(true);
                                    fnf.validateValue(value.toString());
                                    fnf.setValidateFlag(false);
                                }

                            } else if (field instanceof FormatDateField) {
                                FormatDateField fdf = (FormatDateField) field;
                                Date value = fdf.getValue();
                                if (value != null) {
                                    fdf.setValidateFlag(true);
                                    fdf.validateValue(itemNodeModel.getObjectValue().toString());
                                    fdf.setValidateFlag(false);
                                }

                            } else if (field instanceof ForeignKeySelector) {
                                ForeignKeySelector foreignKeySelector = (ForeignKeySelector) field;
                                ForeignKeyBean value = foreignKeySelector.getValue();
                                if (value != null) {
                                    foreignKeySelector.setValidateFlag(true);
                                    foreignKeySelector.validateValue(value.getId());
                                    foreignKeySelector.setValidateFlag(false);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < item.getChildCount(); i++) {
            TreeItemEx subItem = item.getChild(i);
            makeWarning(subItem);
        }
    }

    private void makeWarning4Mandatory(Widget field, boolean isParentMandatory, ItemNodeModel itemNodeModel) {
        if (field instanceof PictureField) {
            if (((PictureField) field).getImageURL().equals(PictureField.DefaultImage)) {
                if (isParentMandatory) {
                    ((Field<?>) field).markInvalid(null);
                } else {
                    if (checkSameLevelNode((ItemNodeModel) itemNodeModel.getParent())) {
                        ((Field<?>) field).markInvalid(null);
                    }
                }
            }

        } else if (field instanceof SimpleComboBox) {
            SimpleComboBox<?> scb = (SimpleComboBox<?>) field;
            ModelData value = scb.getValue();
            if (value == null) {
                if (isParentMandatory) {
                    scb.markInvalid(scb.getMessages().getBlankText());
                } else {
                    if (checkSameLevelNode((ItemNodeModel) itemNodeModel.getParent())) {
                        scb.markInvalid(scb.getMessages().getBlankText());
                    }
                }
            }
        } else if (field instanceof UrlField) {
            UrlField uf = (UrlField) field;
            String value = uf.getValue();
            if (value == null) {
                if (isParentMandatory) {
                    uf.markInvalid(uf.getMessages().getInvalidText());
                } else {
                    if (checkSameLevelNode((ItemNodeModel) itemNodeModel.getParent())) {
                        uf.markInvalid(uf.getMessages().getInvalidText());
                    }
                }
            }

        } else if (field instanceof FormatDateField) {
            FormatDateField fdf = (FormatDateField) field;
            Date value = fdf.getValue();
            if (value == null) {
                if (isParentMandatory) {
                    fdf.markInvalid(fdf.getMessages().getBlankText());
                } else {
                    if (checkSameLevelNode((ItemNodeModel) itemNodeModel.getParent())) {
                        fdf.markInvalid(fdf.getMessages().getBlankText());
                    }
                }
            } else {
                fdf.setValidateFlag(true);
                fdf.validateValue(itemNodeModel.getObjectValue().toString());
                fdf.setValidateFlag(false);
            }

        } else if (field instanceof ForeignKeySelector) {
            ForeignKeySelector foreignKeySelector = (ForeignKeySelector) field;
            ForeignKeyBean value = foreignKeySelector.getValue();
            if (value == null) {
                if (isParentMandatory) {
                    foreignKeySelector.markInvalid(foreignKeySelector.getMessages().getBlankText());
                } else {
                    if (checkSameLevelNode((ItemNodeModel) itemNodeModel.getParent())) {
                        foreignKeySelector.markInvalid(foreignKeySelector.getMessages().getBlankText());
                    }
                }
            } else {
                foreignKeySelector.setValidateFlag(true);
                foreignKeySelector.validateValue(value.getId());
                foreignKeySelector.setValidateFlag(false);
            }

        } else if (field instanceof FormatTextField) {
            FormatTextField ftf = (FormatTextField) field;
            String value = ftf.getValue();
            if (value == null || value.equals("")) { //$NON-NLS-1$
                if (isParentMandatory) {
                    ftf.markInvalid(ftf.getMessages().getBlankText());
                } else {
                    if (checkSameLevelNode((ItemNodeModel) itemNodeModel.getParent())) {
                        ftf.markInvalid(ftf.getMessages().getBlankText());
                    }
                }
            } else {
                ftf.setValidateFlag(true);
                ftf.validateValue(value);
                ftf.setValidateFlag(false);
            }

        } else if (field instanceof FormatNumberField) {
            FormatNumberField fnf = (FormatNumberField) field;
            Number value = fnf.getValue();
            if (value == null) {
                if (isParentMandatory) {
                    fnf.markInvalid(fnf.getMessages().getBlankText());
                } else {
                    if (checkSameLevelNode((ItemNodeModel) itemNodeModel.getParent())) {
                        fnf.markInvalid(fnf.getMessages().getBlankText());
                    }
                }
            } else {
                fnf.setValidateFlag(true);
                fnf.validateValue(value.toString());
                fnf.setValidateFlag(false);
            }
        }
    }

    private boolean checkSameLevelNode(ItemNodeModel model) {
        List<ModelData> modelList = model.getChildren();
        for (ModelData data : modelList) {
            ItemNodeModel itemNodeModel = (ItemNodeModel) data;
            if (itemNodeModel.getObjectValue() != null && !itemNodeModel.getObjectValue().equals("")) {
                return true;
            }
        }
        return false;
    }

    private boolean isRoot(ItemNodeModel itemNodeModel) {
        if (itemNodeModel.getParent() != null) {
            return false;
        }
        return true;
    }
}