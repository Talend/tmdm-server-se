// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.browserecords.client.widget.treedetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
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
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatTextField;
import org.talend.mdm.webapp.browserecords.shared.ComplexTypeModel;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;
import org.talend.mdm.webapp.browserecords.shared.VisibleRuleResult;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class TreeDetail extends ContentPanel {

	private int ONE_SECOND = 1000;
	
    private ViewBean viewBean;

    private TreeEx tree = new TreeEx();

    private DynamicTreeItem root;

    private List<Tree> columnTrees = new ArrayList<Tree>();

    private Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();

    private final ForeignKeyRender fkRender = new ForeignKeyRenderImpl();

    private ItemDetailToolBar toolBar;

    private DynamicTreeItem selectedItem;

    private int renderedCounter = 0;

    private MessageBox progressBar;

    private List<RenderCompleteCallBack> renderCompleteCallBackList = new ArrayList<RenderCompleteCallBack>();
    
    public ForeignKeyRender getFkRender(){
    	return fkRender;
    }
    
    public void beginRender(){
    	BrowseRecordsMessages msg = MessagesFactory.getMessages();
        progressBar = MessageBox.wait(msg.rendering_title(), msg.render_message(), msg.rendering_progress());
    	Timer timer = new Timer(){
			public void run() {
                closeProgressBar();
				Log.info("render detail timeout!"); //$NON-NLS-1$
			}
    	};
    	timer.schedule(ONE_SECOND * 300);
    }
    
    public void stepRenderCounter(){
    	renderedCounter++;
    }
    
    public void resetRenderCounter(){
    	renderedCounter= 0;
        closeProgressBar();
    }

    public void closeProgressBar() {
        if (progressBar != null)
            progressBar.close();
    }
    
    public void endRender(){
    	if (renderedCounter > 0){
    		renderedCounter--;
    	}
    	if (renderedCounter == 0){
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
        // this.setAutoWidth(true);
        this.setLayout(new FlowLayout());
        this.setScrollMode(Scroll.AUTO);
        this.setBorders(false);
        this.setBodyBorder(false);
        this.itemsDetailPanel = itemsDetailPanel;
        this.addListener(Events.Resize, new Listener<BoxComponentEvent>() {

            public void handleEvent(BoxComponentEvent be) {
                if (root == null)
                    return;
                TreeDetail td = (TreeDetail) be.getSource();
                int width = be.getWidth();
                if (td.getWidget(0) instanceof TreeEx && columnTrees.size() == 0) {
                    td.setFiledWidth(root, width, 400, 0);
                } else if (td.getWidget(0) instanceof FlexTable && columnTrees.size() > 0) {
                    int columnWidth = width / columnTrees.size();
                    for(Tree columnTree : columnTrees)
                        td.setFiledWidth(columnTree.getItem(0), columnWidth, 300, 0);
                }
            }     
        });
    }

    public void initTree(ViewBean viewBean, ItemBean itemBean) {
        initTree(viewBean, itemBean, null, null);
    }

    public void initTree(final ViewBean viewBean, ItemBean itemBean, Map<String, List<String>> initDataMap, final String operation) {
        this.viewBean = viewBean;
        if (itemBean == null) {
            buildPanel(operation, initDataMap);
        } else {
            final BrowseRecordsServiceAsync itemService = getItemService();
            BrowseRecordsMessages msg = MessagesFactory.getMessages();
            final MessageBox loadProgress = MessageBox.wait(msg.load_title(), msg.load_message(), msg.load_progress());
            itemService.getItemNodeModel(itemBean, viewBean.getBindingEntityModel(), Locale.getLanguage(),
                    new SessionAwareAsyncCallback<ItemNodeModel>() {

                        public void onSuccess(final ItemNodeModel node) {

                            itemService.executeVisibleRule(viewBean, CommonUtil.toXML(node, TreeDetail.this.viewBean, true),
                                    new SessionAwareAsyncCallback<List<VisibleRuleResult>>() {

                                        public void onSuccess(List<VisibleRuleResult> visibleResults) {
                                            if (visibleResults != null) {
                                                recrusiveSetItems(visibleResults, node);
                                            }
                                            loadProgress.close();
                                            renderTree(node, operation);
                                        }
                                    });
                        }

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
            public void onSuccess(final ItemNodeModel result) {
                getItemService().executeVisibleRule(viewBean, CommonUtil.toXML(result, viewBean, true),
                new SessionAwareAsyncCallback<List<VisibleRuleResult>>() {

                    public void onSuccess(List<VisibleRuleResult> visibleResults) {
                        if (visibleResults != null){
                            recrusiveSetItems(visibleResults, result);
                        }
                        renderTree(result, operation);
                    }
                });
            }
        });
    }

    private boolean isFirstKey = true;

    public DynamicTreeItem buildGWTTree(final ItemNodeModel itemNode, DynamicTreeItem item, boolean withDefaultValue,
            String operation) {
        if (item == null) {
            item = new DynamicTreeItem();
            item.setItemNodeModel(itemNode);
            String itemRealType = itemNode.getRealType();
            if (itemRealType != null && itemRealType.trim().length() > 0) {
                item.setState(true);
            }
            if (ItemDetailToolBar.DUPLICATE_OPERATION.equals(operation)) {
                if (itemNode.isKey()) {
                    if (isFirstKey) {
                        itemNode.setObjectValue(null);
                        isFirstKey = false;
                    }
                }
            }
            
            MultiOccurrenceChangeItem itemWidget = TreeDetailUtil.createWidget(itemNode, viewBean, fieldMap, null, operation, itemsDetailPanel);
            itemWidget.setTreeDetail(this);
            item.setWidget(itemWidget);
        }

        List<ModelData> itemNodeChildren = itemNode.getChildren();

        if (itemNodeChildren != null && itemNodeChildren.size() > 0) {
            IncrementalBuildTree incCommand = new IncrementalBuildTree(this, itemNode, viewBean, withDefaultValue, operation,
                    item);
            if (itemNode.getParent() == null) {
                addCommand(incCommand, true);
            } else {
                addCommand(incCommand, false);
            }

            item.getElement().getStyle().setPaddingLeft(3.0, Unit.PX);
        }

        item.setUserObject(itemNode);
        item.setVisible(itemNode.isVisible());
        item.setState(viewBean.getBindingEntityModel().getMetaDataTypes().get(itemNode.getTypePath()).isAutoExpand());

        return item;
    }

    public static void addCommand(IncrementalBuildTree command, boolean sync) {
        if (sync || command.getChildCount() < IncrementalBuildTree.GROUP_SIZE) {
            while (command.execute())
                ;
        } else {
            DeferredCommand.addCommand(command);
        }
    }

    static boolean isFKDisplayedIntoTab(ItemNodeModel node, TypeModel typeModel, Map<String, TypeModel> metaDataTypes) {
        String typeModelFK = typeModel.getForeignkey();
        if (typeModelFK == null)
            return false; // Not a FK

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

    public void onUpdatePolymorphism(ComplexTypeModel typeModel) {
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
        String xml = CommonUtil.toXML(rootNode, viewBean);

        getItemService().createSubItemNodeModel(viewBean, xml, typePath, contextPath, treeNode.getRealType(), UrlUtil.getLanguage(),
                new SessionAwareAsyncCallback<ItemNodeModel>() {


            public void onSuccess(ItemNodeModel result) {
                ModelData[] children = result.getChildren().toArray(new ModelData[0]);
                for (ModelData child : children) {
                    treeNode.add(child);
                }
                renderCompleteCallBackList.add(new RenderCompleteCallBack() {
                    public void onSuccess() {
                        multiManager.addMultiOccurrenceNode(selectedItem);
                        multiManager.warningAllItems();
                        multiManager.handleOptIcons();                        
                    }
                });
                buildGWTTree(treeNode, selectedItem, false, null);
            }
        });
    }

    public void onExecuteVisibleRule(List<VisibleRuleResult> visibleResults) {
        for (VisibleRuleResult visibleResult : visibleResults) {
            if (columnTrees.size() > 0) {
                for (Tree columnTree : columnTrees) {
                    recrusiveSetItems(visibleResult, (DynamicTreeItem) columnTree.getItem(0));
                }
            } else {
                DynamicTreeItem rootItem = (DynamicTreeItem) tree.getItem(0);
                recrusiveSetItems(visibleResult, rootItem);
            }
        }
    }

    private void renderTree(ItemNodeModel rootModel) {
        renderTree(rootModel, null);
    }

    private void renderTree(ItemNodeModel rootModel, String operation) {
        multiManager = new MultiOccurrenceManager(viewBean.getBindingEntityModel().getMetaDataTypes(), this);
        beginRender();
        renderCompleteCallBackList.add(new RenderCompleteCallBack() {
            public void onSuccess() {
                multiManager.addMultiOccurrenceNode((DynamicTreeItem) root);
                multiManager.warningAllItems();
                multiManager.handleOptIcons();
            }
        });
        root = buildGWTTree(rootModel, null, false, operation);
        isFirstKey = true;
        root.setState(true);
        if (root.getElement().getFirstChildElement() != null)
            root.getElement().getFirstChildElement().setClassName("rootNode"); //$NON-NLS-1$
        
        ColumnTreeLayoutModel columnLayoutModel = viewBean.getColumnLayoutModel();
        if (columnLayoutModel != null) {// TODO if create a new PrimaryKey, tree UI should not render according to the
                                        // layout template
            FlexTable htable = new FlexTable();
            htable.setHeight("100%"); //$NON-NLS-1$
            columnTrees.clear();
            int columnWidth = this.getWidth() / columnLayoutModel.getColumnTreeModels().size();
            int columnNum = 0;
            for (ColumnTreeModel ctm : columnLayoutModel.getColumnTreeModels()) {
                // Tree columnTree = displayGWTTree(ctm);
                Tree columnTree = ViewUtil.transformToCustomLayout(root, ctm, viewBean);
                if(columnWidth > 500)
                    this.setFiledWidth(columnTree.getItem(0), columnWidth, 300, 0);
                columnTrees.add(columnTree);
                htable.setWidget(0, columnNum, columnTree);
                ViewUtil.setStyleAttribute(htable.getCellFormatter().getElement(0, columnNum), ctm.getStyle());
                htable.getCellFormatter().getElement(0, columnNum).setAttribute("vAlign", "top"); //$NON-NLS-1$//$NON-NLS-2$
                columnNum++;
                addTreeListener(columnTree);
            }
            //            hp.setHeight("570px"); //$NON-NLS-1$
            // HorizontalPanel spacehp = new HorizontalPanel();
            //            spacehp.setHeight("10px"); //$NON-NLS-1$
            // add(spacehp);
            add(htable);

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
        if (foreignKeyDeleteMessage != null && foreignKeyDeleteMessage.trim().length() > 0)
            MessageBox.alert(MessagesFactory.getMessages().warning_title(), foreignKeyDeleteMessage, null).getDialog()
                    .setWidth(600);
    }

    private int getLevel(TreeItem item) {
        int level = -1;
        if (item == null) {
            return level;
        }

        TreeItem current = item;
        while (current != null) {
            level++;
            current = current.getParentItem();
        }
        return level;
    }

    public void adjustFieldWidget(TreeItem item) {
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

    private void setFiledWidth(TreeItem item, int width, int offset, int level) {
        if (item.getWidget() instanceof HorizontalPanel) {
            HorizontalPanel hp = (HorizontalPanel) item.getWidget();
            if (hp.getWidgetCount() > 1) {
                Widget field = hp.getWidget(1);
                int size = width - (offset + 19 * level);
                if (field instanceof FormatTextField)
                    ((FormatTextField) field).setWidth(size > 200 ? size : 200);
                else if (field instanceof SimpleComboBox)
                    ((SimpleComboBox<?>) field).setWidth(size > 200 ? size : 200);
            }
        }
        for (int i = 0; i < item.getChildCount(); i++) {
            TreeItem subItem = item.getChild(i);
            setFiledWidth(subItem, width, offset, level + 1);
        }
    }
    
    // get selected item in tree
    private void addTreeListener(Tree tree) {
        tree.addSelectionHandler(new SelectionHandler<TreeItem>() {

            public void onSelection(SelectionEvent<TreeItem> event) {
                if (event.getSelectedItem() instanceof DynamicTreeItem) {
                    selectedItem = (DynamicTreeItem) event.getSelectedItem();
                }
            }
        });
    }

    private void recrusiveSetItems(List<VisibleRuleResult> visibleResults, ItemNodeModel itemNode) {
        if (itemNode != null) {
            String realPath = CommonUtil.getRealXPath(itemNode);

            boolean maybeUse = false;
            Iterator<VisibleRuleResult> iter = visibleResults.iterator();

            while (iter.hasNext()) {
                VisibleRuleResult vr = iter.next();
                if (vr.getXpath().equals(realPath)) {
                    itemNode.setVisible(vr.isVisible());
                    iter.remove();
                    return;
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
        }
    }

    private void recrusiveSetItems(VisibleRuleResult visibleResult, DynamicTreeItem rootItem) {
        if (rootItem.getItemNodeModel() != null) {
            String realPath = CommonUtil.getRealXPath(rootItem.getItemNodeModel());
            if (realPath.equals(visibleResult.getXpath())) {
                rootItem.setVisible(visibleResult.isVisible());
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

    public static class DynamicTreeItem extends TreeItem {

        private ItemNodeModel itemNode;

        public DynamicTreeItem() {
            super();
            if (GXT.isIE) {
                getContentElement().getStyle().setDisplay(Display.BLOCK);
            }
            this.getElement().getStyle().setPaddingTop(0D, Unit.PX);
            this.getElement().getStyle().setPaddingBottom(0D, Unit.PX);
        }

        private native Element getContentElement()/*-{
            return this.@com.google.gwt.user.client.ui.TreeItem::contentElem;
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

        private native ArrayList<TreeItem> _getChildren()/*-{
            return this.@com.google.gwt.user.client.ui.TreeItem::children;
        }-*/;

        private native void _initChildren() /*-{
            this.@com.google.gwt.user.client.ui.TreeItem::initChildren()
        }-*/;

        private native void _setParentItem(TreeItem parent)/*-{
            this.@com.google.gwt.user.client.ui.TreeItem::setParentItem(Lcom/google/gwt/user/client/ui/TreeItem;)(parent);
        }-*/;

        private native void _updateState(boolean animate, boolean updateTreeSelection)/*-{
            this.@com.google.gwt.user.client.ui.TreeItem::updateState(ZZ)(animate, updateTreeSelection);
        }-*/;

        private native void _setTree(Tree tree)/*-{
            this.@com.google.gwt.user.client.ui.TreeItem::setTree(Lcom/google/gwt/user/client/ui/Tree;)(tree);
        }-*/;

        private native Element _getChildSpanElem()/*-{
            return this.@com.google.gwt.user.client.ui.TreeItem::childSpanElem;
        }-*/;

        public void removeItem(DynamicTreeItem item) {
            super.removeItem(item);
        }

        public void setState(boolean open, boolean fireEvents) { 
            // Only do the physical update if it changes 
            if (this.getOpen() != open) { 
                setOpen(open); 
                __updateState(true, true); 
                if (fireEvents && this.getTree() != null) {
                    fireTreeStateChanged(this.getTree(), this, open);
                }
            }
        }

        private native boolean getOpen()/*-{
            return this.@com.google.gwt.user.client.ui.TreeItem::open;
        }-*/;

        private native void setOpen(boolean open)/*-{
            this.@com.google.gwt.user.client.ui.TreeItem::open = open;
        }-*/;

        private native void __updateState(boolean animate, boolean updateTreeSelection)/*-{
            this.@com.google.gwt.user.client.ui.TreeItem::updateState(ZZ)(animate, updateTreeSelection);
        }-*/;

        private native void fireTreeStateChanged(Tree tree, TreeItem item, boolean open)/*-{
            tree.@com.google.gwt.user.client.ui.Tree::fireStateChanged(Lcom/google/gwt/user/client/ui/TreeItem;Z)(item, open);
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
    
    private static BrowseRecordsServiceAsync getItemService() {

        BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);
        return service;

    }

    public Tree getTree() {
        return tree;
    }

    public void refreshTree(final ItemBean item) {
        item.set("isRefresh", true); //$NON-NLS-1$
        getItemService().getItemNodeModel(item, viewBean.getBindingEntityModel(), Locale.getLanguage(),
                new SessionAwareAsyncCallback<ItemNodeModel>() {

                    public void onSuccess(final ItemNodeModel node) {
                        TreeDetail.this.removeAll();
                        item.setLastUpdateTime(node);
                        itemsDetailPanel.clearChildrenContent();

                        if (node.isHasVisiblueRule()) {
                            getItemService().executeVisibleRule(viewBean, CommonUtil.toXML(node, TreeDetail.this.viewBean, true),
                                    new SessionAwareAsyncCallback<List<VisibleRuleResult>>() {

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

    public TreeItem getRoot(){
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

}