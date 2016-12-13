/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.mvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsEvents;
import org.talend.mdm.webapp.browserecords.client.creator.CellEditorCreator;
import org.talend.mdm.webapp.browserecords.client.creator.CellRendererCreator;
import org.talend.mdm.webapp.browserecords.client.creator.ItemCreator;
import org.talend.mdm.webapp.browserecords.client.model.BreadCrumbModel;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.util.ViewUtil;
import org.talend.mdm.webapp.browserecords.client.widget.BreadCrumb;
import org.talend.mdm.webapp.browserecords.client.widget.GenerateContainer;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsListPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsMainTabPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsSearchContainer;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.LineagePanel;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeyCellField;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeyField;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ReturnCriteriaFK;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.creator.FieldCreator;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailUtil;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;
import org.talend.mdm.webapp.browserecords.shared.VisibleRuleResult;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class BrowseRecordsView extends View {

    public static final String ITEMS_FORM_TARGET = "items_form_target";//$NON-NLS-1$

    public static final String TARGET_IN_NEW_TAB = "target_in_new_tab";//$NON-NLS-1$

    public static final String DEFAULT_ITEMVIEW = "itemView"; //$NON-NLS-1$

    public static final String LINEAGE_ITEMVIEW = "lineageItemView"; //$NON-NLS-1$

    public static final String BULKUPDATE_ITEMVIEW = "bulkUpdateItemView"; //$NON-NLS-1$

    public static final String ITEMS_DETAIL_PANEL = "itemsDetailPanel"; //$NON-NLS-1$

    public static final String FK_SOURCE_WIDGET = "foreignKeySourceWidget"; //$NON-NLS-1$

    public static final String IS_STAGING = "isStaging"; //$NON-NLS-1$

    public static final int LINEAGE_VIEW_CODE = 1;

    public BrowseRecordsView(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        int eventTypeCode = event.getType().getEventCode();
        switch (eventTypeCode) {
        case BrowseRecordsEvents.InitFrameCode:
            onInitFrame(event);
            break;
        case BrowseRecordsEvents.InitSearchContainerCode:
            onInitSearchContainer(event);
            break;
        case BrowseRecordsEvents.GetViewCode:
            onGetView(event);
            break;
        case BrowseRecordsEvents.SearchViewCode:
            onSearchView(event);
            break;
        case BrowseRecordsEvents.CreateForeignKeyViewCode:
            onCreateForeignKeyView(event);
            break;
        case BrowseRecordsEvents.ViewItemCode:
            onViewItem(event);
            break;
        case BrowseRecordsEvents.ViewForeignKeyCode:
            onViewForeignKey(event);
            break;
        case BrowseRecordsEvents.UpdatePolymorphismCode:
            onUpdatePolymorphism(event);
            break;
        case BrowseRecordsEvents.ExecuteVisibleRuleCode:
            onExecuteVisibleRule(event);
            break;
        case BrowseRecordsEvents.ViewLineageItemCode:
            onViewLineageItem(event);
            break;
        case BrowseRecordsEvents.DefaultViewCode:
            setDefaultView(event);
            break;
        default:
            break;
        }
    }

    private void onExecuteVisibleRule(AppEvent event) {
        ItemsDetailPanel detailPanel = event.getData(BrowseRecordsView.ITEMS_DETAIL_PANEL);
        ItemPanel itemPanel = detailPanel.getCurrentItemPanel();
        if (itemPanel != null) {
            itemPanel.onExecuteVisibleRule((List<VisibleRuleResult>) event.getData());
        }
    }

    private void onUpdatePolymorphism(AppEvent event) {
        ItemsDetailPanel detailPanel = event.getData(BrowseRecordsView.ITEMS_DETAIL_PANEL);
        ItemPanel itemPanel = detailPanel.getCurrentItemPanel();
        if (itemPanel != null) {
            itemPanel.onUpdatePolymorphism((ComplexTypeModel) event.getData());
        }
    }

    private void onViewForeignKey(AppEvent event) {
        ForeignKeyModel model = event.getData();
        ItemsDetailPanel detailPanel = event.getData(BrowseRecordsView.ITEMS_DETAIL_PANEL);
        ItemBean fkItemBean = model.getItemBean();
        ViewBean fkViewBean = model.getViewBean();
        ItemPanel panel = detailPanel.getCurrentItemPanel();
        boolean isOutMost = false;
        boolean isHierarchyCall = false;
        if (panel != null) {
            isOutMost = panel.getToolBar().isOutMost();
            isHierarchyCall = panel.getToolBar().isHierarchyCall();
        }
        detailPanel.clearContent();
        detailPanel.clearBanner();
        detailPanel.setOutMost(isOutMost);

        String pkInfo = fkItemBean.getDisplayPKInfo().equals(fkItemBean.getLabel()) ? null : fkItemBean.getDisplayPKInfo();
        detailPanel.appendBreadCrumb(fkItemBean.getConcept(), fkItemBean.getLabel(), fkItemBean.getIds(), pkInfo);
        String operation = getOperation(fkItemBean);
        ItemPanel itemPanel = new ItemPanel(detailPanel.isStaging(), fkViewBean, fkItemBean, operation, detailPanel, !isOutMost);
        itemPanel.getToolBar().setOutMost(isOutMost);
        itemPanel.getToolBar().setHierarchyCall(isHierarchyCall);
        itemPanel.getToolBar().setFkToolBar(true);
        detailPanel.initBanner(fkItemBean.getPkInfoList(), fkItemBean.getDescription());
        detailPanel.addTabItem(fkItemBean.getLabel(), itemPanel, ItemsDetailPanel.SINGLETON, fkItemBean.getIds());
        CommonUtil.setCurrentCachedEntity(fkItemBean.getConcept() + fkItemBean.getIds() + detailPanel.isOutMost(), itemPanel);
    }

    private String getOperation(ItemBean itemBean) {
        String operation;
        String smartViewMode = itemBean.getSmartViewMode();
        if (smartViewMode.equals(ItemBean.PERSOMODE)) {
            operation = ItemDetailToolBar.PERSONALEVIEW_OPERATION;
        } else if (smartViewMode.equals(ItemBean.SMARTMODE)) {
            operation = ItemDetailToolBar.SMARTVIEW_OPERATION;
        } else {
            operation = ItemDetailToolBar.VIEW_OPERATION;
        }
        return operation;
    }

    private void onViewItem(AppEvent event) {
        ItemBean item = (ItemBean) event.getData();
        Boolean isStaging = event.getData("isStaging"); //$NON-NLS-1$
        String itemConcept = null;
        String itemLabel = null;
        String itemDisplayPKInfo = null;
        String itemIds = null;
        List<String> itemPkInfoList = null;
        String itemDescription = null;

        String operation = getOperation(item);

        ViewBean viewBean = (ViewBean) BrowseRecords.getSession().get(UserSession.CURRENT_VIEW);
        List<BreadCrumbModel> breads = new ArrayList<BreadCrumbModel>();

        // show breadcrumb
        // ItemsDetailPanel.getInstance().clearBreadCrumb();
        itemConcept = item.getConcept();
        itemLabel = item.getLabel();
        itemDisplayPKInfo = item.getDisplayPKInfo();
        itemIds = item.getIds();
        itemPkInfoList = item.getPkInfoList();
        itemDescription = item.getDescription();

        breads.add(new BreadCrumbModel("", BreadCrumb.DEFAULTNAME, null, null, false)); //$NON-NLS-1$           
        breads.add(new BreadCrumbModel(itemConcept, itemLabel, itemIds, itemDisplayPKInfo.equals(itemLabel) ? null
                : itemDisplayPKInfo, true));

        ItemsMainTabPanel itemsMainTabPanel = ItemsMainTabPanel.getInstance();

        if (TARGET_IN_NEW_TAB.equals(event.getData(BrowseRecordsView.ITEMS_FORM_TARGET))) {
            // Open in a new tab in ItemsMainTabPanel

            ItemsDetailPanel itemsDetailPanel = this.buildNewItemsDetailPanel(isStaging, viewBean, item, operation, itemIds,
                    itemPkInfoList, itemDescription, itemLabel, breads);

            itemsMainTabPanel.addMainTabItem(itemLabel + " " + itemIds, //$NON-NLS-1$ 
                    itemsDetailPanel, itemIds);

        } else {
            // Open in default tab in ItemsMainTabPanel, which may or may not already exist

            String itemDisplayLabel = itemLabel != null && itemLabel.trim().length() > 0 ? itemLabel : itemConcept;
            TabItem defaultTabItem = itemsMainTabPanel.getItemByItemId(DEFAULT_ITEMVIEW);

            if (defaultTabItem != null) {
                // Default tab exists in ItemsMainTabPanel
                // Reuse the default tab

                defaultTabItem.setText(itemDisplayLabel + " " + itemIds); //$NON-NLS-1$

                ItemsDetailPanel itemsDetailPanel = (ItemsDetailPanel) defaultTabItem.getItemByItemId(DEFAULT_ITEMVIEW);

                if (itemsDetailPanel == null) {
                    // ItemsDetailPanel does not exist in default tab of ItemsMainTabPanel
                    // Create new ItemDetailPanel within the default tab

                    defaultTabItem.removeAll();
                    defaultTabItem.add(this.buildNewItemsDetailPanel(isStaging, viewBean, item, operation, DEFAULT_ITEMVIEW,
                            itemPkInfoList, itemDescription, itemDisplayLabel, breads));
                } else {
                    // ItemsDetailPanel exists in default tab of ItemsMainTabPanel
                    // Reuse the ItemsDetailPanel

                    itemsDetailPanel.clearAll();
                    itemsDetailPanel.initBanner(itemPkInfoList, itemDescription);

                    ItemPanel itemPanel = new ItemPanel(isStaging, viewBean, item, operation, itemsDetailPanel, true);
                    itemsDetailPanel.addTabItem(itemDisplayLabel, itemPanel, ItemsDetailPanel.SINGLETON, DEFAULT_ITEMVIEW);

                    itemsDetailPanel.initBreadCrumb(new BreadCrumb(breads, itemsDetailPanel));
                }

                itemsMainTabPanel.setSelection(defaultTabItem);
            } else {
                // Default tab does not exist in ItemsMainTabPanel, create it

                TabItem tabItem = this.buildNewItemsMainTabPanelTabItem(itemDisplayLabel, itemIds, this.buildNewItemsDetailPanel(
                        isStaging, viewBean, item, operation, DEFAULT_ITEMVIEW, itemPkInfoList, itemDescription,
                        itemDisplayLabel, breads));
                itemsMainTabPanel.insert(tabItem, 0);
                itemsMainTabPanel.setSelection(tabItem);
            }
        }
        ItemsDetailPanel detailPanel = itemsMainTabPanel.getCurrentViewTabItem();
        detailPanel.setOutMost(false);
        ItemPanel itemPanel = (ItemPanel) detailPanel.getPrimaryKeyTabWidget();
        CommonUtil.setCurrentCachedEntity(itemConcept + itemIds + detailPanel.isOutMost(), itemPanel);

    }

    private TabItem buildNewItemsMainTabPanelTabItem(String itemLabel, String itemIds, ItemsDetailPanel itemsDetailPanel) {
        TabItem tabItem = new TabItem(itemLabel + " " + itemIds); //$NON-NLS-1$
        tabItem.setId(DEFAULT_ITEMVIEW);
        tabItem.setClosable(true);
        tabItem.setLayout(new FitLayout());
        tabItem.add(itemsDetailPanel);

        return tabItem;
    }

    private ItemsDetailPanel buildNewItemsDetailPanel(boolean isStaing, ViewBean viewBean, ItemBean item, String operation,
            String itemIds, List<String> itemPkInfoList, String itemDescription, String itemLabel, List<BreadCrumbModel> breads) {
        ItemsDetailPanel itemsDetailPanel = ItemsDetailPanel.newInstance();
        itemsDetailPanel.setStaging(isStaing);
        ItemPanel itemPanel = new ItemPanel(isStaing, viewBean, item, operation, itemsDetailPanel, true);

        itemsDetailPanel.setId(itemIds);

        itemsDetailPanel.initBanner(itemPkInfoList, itemDescription);

        itemsDetailPanel.addTabItem(itemLabel, itemPanel, ItemsDetailPanel.SINGLETON, itemIds);

        itemsDetailPanel.initBreadCrumb(new BreadCrumb(breads, itemsDetailPanel));

        return itemsDetailPanel;
    }

    private void onCreateForeignKeyView(AppEvent event) {
        ViewBean viewBean = event.getData();
        Boolean isStaging = event.getData(BrowseRecordsView.IS_STAGING);
        ItemsDetailPanel detailPanel = event.getData(BrowseRecordsView.ITEMS_DETAIL_PANEL);
        ItemPanel itemPanelWidget = (ItemPanel) detailPanel.getCurrentlySelectedTabWidget();
        String concept = viewBean.getBindingEntityModel().getConceptName();
        EntityModel entityModel = (BrowseRecords.getSession().getCurrentEntityModel() != null ? BrowseRecords.getSession()
                .getCurrentEntityModel() : viewBean.getBindingEntityModel());
        ItemBean itemBean = ItemCreator.createDefaultItemBean(concept, entityModel);
        // set label
        TypeModel typeModel = viewBean.getBindingEntityModel().getMetaDataTypes().get(concept);
        itemBean.setLabel(typeModel.getLabel(Locale.getLanguage()));

        ItemsDetailPanel panel = ItemsDetailPanel.newInstance();
        panel.setStaging(isStaging);
        panel.setOutMost(detailPanel.isOutMost());
        // set banner
        List<String> pkInfoList = new ArrayList<String>();
        pkInfoList.add(itemBean.getLabel());
        panel.initBanner(pkInfoList, itemBean.getDescription());
        // set breadCrumb
        List<BreadCrumbModel> breads = new ArrayList<BreadCrumbModel>();
        if (itemBean != null) {
            breads.add(new BreadCrumbModel("", BreadCrumb.DEFAULTNAME, null, null, false)); //$NON-NLS-1$
            breads.add(new BreadCrumbModel(itemBean.getConcept(), itemBean.getLabel(), null, null, true));
        }
        panel.initBreadCrumb(new BreadCrumb(breads, panel));
        // set itemPanel
        ItemPanel itemPanel = new ItemPanel(panel);
        panel.addTabItem(itemBean.getLabel(), itemPanel, ItemsDetailPanel.SINGLETON, itemBean.getConcept());
        itemPanel.initTreeDetail(viewBean, itemBean, ItemDetailToolBar.CREATE_OPERATION, isStaging);
        itemPanel.getToolBar().setFkToolBar(true);
        itemPanel.getToolBar().setReturnCriteriaFK((ReturnCriteriaFK) event.getData(BrowseRecordsView.FK_SOURCE_WIDGET));
        itemPanel.getToolBar().setOutMost(detailPanel.isLineage() ? true : detailPanel.isOutMost());
        itemPanel.getToolBar().setHierarchyCall(itemPanelWidget.getToolBar().isHierarchyCall());
        itemPanel.getToolBar().setType(ItemDetailToolBar.TYPE_CREATE_FOREIGNKEY_ENTITY);
        if (itemPanel.getToolBar().isOutMost()) {
            panel.setHeading(itemBean.getLabel());
            panel.setItemId(itemBean.getLabel());
            TreeDetailUtil.renderTreeDetailPanel(itemBean.getLabel(), panel);
        } else {
            ItemsMainTabPanel.getInstance().addMainTabItem(itemBean.getLabel(), panel, itemBean.getConcept());
        }
        CommonUtil.setCurrentCachedEntity(itemBean.getConcept() + panel.isOutMost(), itemPanel);
    }

    private void onInitFrame(AppEvent event) {
        if (Log.isInfoEnabled()) {
            Log.info("Init frame... ");//$NON-NLS-1$
        }
        GenerateContainer.getContentPanel().setLayout(new FitLayout());
        GenerateContainer.getContentPanel().setStyleAttribute("height", "100%");//$NON-NLS-1$ //$NON-NLS-2$       
        Dispatcher.forwardEvent(BrowseRecordsEvents.InitSearchContainer);
    }

    protected void onInitSearchContainer(AppEvent ae) {
        // create search panel
        if (Log.isInfoEnabled()) {
            Log.info("Init items-search-container... ");//$NON-NLS-1$
        }
        GenerateContainer.getContentPanel().add(ItemsSearchContainer.getInstance());
        GenerateContainer.getContentPanel().layout(true);
    }

    protected void onGetView(final AppEvent event) {
        ViewBean viewBean = event.getData();
        ItemsToolBar.getInstance().updateToolBar(viewBean);
        ButtonEvent be = new ButtonEvent(ItemsToolBar.getInstance().searchButton);
        ItemsToolBar.getInstance().searchButton.fireEvent(Events.Select, be);
    }

    protected void onSearchView(final AppEvent event) {
        ViewBean viewBean = event.getData();
        Boolean isStaging = event.getData(BrowseRecordsView.IS_STAGING);
        EntityModel entityModel = viewBean.getBindingEntityModel();

        // TODO update columns
        List<ColumnConfig> ccList = new ArrayList<ColumnConfig>();
        CheckBoxSelectionModel<ItemBean> sm = new CheckBoxSelectionModel<ItemBean>();
        sm.setSelectionMode(SelectionMode.MULTI);
        ccList.add(sm.getColumn());
        List<String> viewableXpaths = viewBean.getViewableXpaths();
        Map<String, TypeModel> dataTypes = entityModel.getMetaDataTypes();
        List<String> keys = Arrays.asList(entityModel.getKeys());

        Map<String, ForeignKeyCellField> fkFieldwithFilterMap = new HashMap<String, ForeignKeyCellField>();
        Map<String, Field> fieldMap = new HashMap<String, Field>();
        for (String xpath : viewableXpaths) {
            TypeModel typeModel = dataTypes.get(xpath);

            ColumnConfig cc = new ColumnConfig(xpath, typeModel == null ? xpath : ViewUtil.getViewableLabel(Locale.getLanguage(),
                    typeModel), 200);
            if (typeModel instanceof SimpleTypeModel && !keys.contains(xpath) && !typeModel.isMultiOccurrence()) {
                Field<?> field = FieldCreator.createField((SimpleTypeModel) typeModel, null, false, Locale.getLanguage());
                if (field instanceof ForeignKeyField) {
                    ForeignKeyCellField foreignKeyCellField = new ForeignKeyCellField((ForeignKeyField) field,
                            typeModel.getForeignKeyFilter());
                    foreignKeyCellField.setStaging(isStaging);
                    if (typeModel.getForeignKeyFilter() != null && !typeModel.getForeignKeyFilter().isEmpty()) {
                        fkFieldwithFilterMap.put(xpath, foreignKeyCellField);
                    }
                    field = foreignKeyCellField;
                }
                fieldMap.put(xpath, field);
                CellEditor cellEditor = CellEditorCreator.createCellEditor(field);
                if (cellEditor != null) {
                    cc.setEditor(cellEditor);
                }
            }

            if (typeModel != null) {
                GridCellRenderer<ModelData> renderer = CellRendererCreator.createRenderer(typeModel, xpath);
                if (renderer != null) {
                    cc.setRenderer(renderer);
                }
            }
            if (typeModel == null || typeModel.isVisible()) {
                ccList.add(cc);
            }
        }

        for (String xpath : fkFieldwithFilterMap.keySet()) {
            ForeignKeyCellField foreignKeyCellField = fkFieldwithFilterMap.get(xpath);
            Map<Integer, Field<?>> targetFieldMap = new HashMap<Integer, Field<?>>();
            String[] criterias = org.talend.mdm.webapp.base.shared.util.CommonUtil.getCriteriasByForeignKeyFilter(dataTypes.get(
                    xpath).getForeignKeyFilter());
            for (int i = 0; i < criterias.length; i++) {
                Map<String, String> conditionMap = org.talend.mdm.webapp.base.shared.util.CommonUtil
                        .buildConditionByCriteria(criterias[i]);
                String filterValue = conditionMap.get("Value"); //$NON-NLS-1$
                if (filterValue != null && !filterValue.isEmpty()
                        && !org.talend.mdm.webapp.base.shared.util.CommonUtil.isFilterValue(filterValue)) {
                    String targetPath;
                    if (org.talend.mdm.webapp.base.shared.util.CommonUtil.isRelativePath(filterValue)) {
                        if (filterValue.startsWith(".")) { //$NON-NLS-1$
                            targetPath = xpath + filterValue.substring(filterValue.indexOf(".")); //$NON-NLS-1$
                        } else if (filterValue.startsWith("..")) { //$NON-NLS-1$
                            targetPath = xpath.substring(0, xpath.lastIndexOf("/")) + filterValue.substring(filterValue.indexOf("..")); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    } else {
                        targetPath = filterValue;
                    }
                    if (filterValue != null && entityModel.getConceptName().equals(filterValue.split("/")[0])) { //$NON-NLS-1$
                        targetFieldMap.put(i, fieldMap.get(filterValue));
                    }
                }
            }
            foreignKeyCellField.setTargetField(targetFieldMap);
        }

        ItemsListPanel.getInstance().updateGrid(sm, ccList);
        // itemsSearchContainer.getItemsListPanel().resetShowItemTimes();
        // TODO in the view of ViewItemForm binding

    }

    private void onViewLineageItem(AppEvent event) {
        ItemBean item = (ItemBean) event.getData();
        String operation = getOperation(item);
        ViewBean viewBean = event.getData(BrowseRecords.VIEW_BEAN);

        ItemsDetailPanel lineageDetailPanel = ItemsDetailPanel.newInstance();
        lineageDetailPanel.setStaging(true);
        lineageDetailPanel.setLineage(true);
        ItemPanel itemPanel = new ItemPanel(true, viewBean, item, operation, lineageDetailPanel, true);

        itemPanel.getToolBar().setOutMost(false);
        itemPanel.getToolBar().setFkToolBar(false);
        itemPanel.getToolBar().setHierarchyCall(false);
        itemPanel.getToolBar().setViewCode(LINEAGE_VIEW_CODE);

        List<BreadCrumbModel> breads = new ArrayList<BreadCrumbModel>();
        if (item != null) {
            breads.add(new BreadCrumbModel("", BreadCrumb.DEFAULTNAME, null, null, false)); //$NON-NLS-1$
            breads.add(new BreadCrumbModel(item.getConcept(), item.getLabel(), item.getIds(), item.getDisplayPKInfo().equals(
                    item.getLabel()) ? null : item.getDisplayPKInfo(), true));
        }

        lineageDetailPanel.setId(item.getIds());
        lineageDetailPanel.initBanner(item.getPkInfoList(), item.getDescription());
        lineageDetailPanel.addTabItem(item.getLabel(), itemPanel, ItemsDetailPanel.SINGLETON, item.getIds());
        lineageDetailPanel.initBreadCrumb(new BreadCrumb(breads, lineageDetailPanel, LINEAGE_VIEW_CODE));
        LineagePanel.getInstance().updateDetailPanel(lineageDetailPanel);
        CommonUtil.setCurrentCachedEntity(LINEAGE_ITEMVIEW + item.getConcept() + item.getIds() + lineageDetailPanel.isOutMost(),
                itemPanel);
    }

    protected void setDefaultView(final AppEvent event) {
        ItemsToolBar.getInstance().updateEntityCombo();
    }
}
