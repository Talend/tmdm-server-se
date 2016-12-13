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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.client.model.MultipleCriteria;
import org.talend.mdm.webapp.base.client.model.SimpleCriterion;
import org.talend.mdm.webapp.base.client.util.CriteriaUtil;
import org.talend.mdm.webapp.base.client.widget.PagingToolBarEx;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsEvents;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.WidgetFactory;
import org.talend.mdm.webapp.browserecords.client.creator.ItemCreator;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.BreadCrumbModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.model.QueryModel;
import org.talend.mdm.webapp.browserecords.client.mvc.BrowseRecordsView;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.rest.ExplainRestServiceHandler;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.util.ViewUtil;
import org.talend.mdm.webapp.browserecords.client.widget.SearchPanel.AdvancedSearchPanel;
import org.talend.mdm.webapp.browserecords.client.widget.SearchPanel.SimpleCriterionPanel;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.ComboBoxField;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.ContainerUpdate;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.DeleteAction;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.DeleteCallback;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.ListRefresh;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.LogicalDeleteAction;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.NoOpPostDeleteAction;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.PostDeleteAction;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailUtil;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;

public class ItemsToolBar extends ToolBar {

    protected Button createButton;

    protected Button deleteButton;

    protected Button simulateMatchButton;

    protected Button uploadButton;

    protected MenuItem importMenu;

    protected MenuItem exportMenu;

    protected Button bulkUpdateButton;

    public final Button searchButton = new Button(MessagesFactory.getMessages().search_btn());

    protected ToggleButton advancedSearchButton;

    protected Button manageBookButton;

    protected Button bookMarkButton;

    private final static int PAGE_SIZE = 10;

    private boolean isSimple;

    private static String userCluster = null;

    private SimpleCriterionPanel<?> simplePanel;

    private AdvancedSearchPanel advancedPanel;

    private ComboBoxField<ItemBaseModel> entityCombo;

    protected BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);

    private List<ItemBaseModel> userCriteriasList;

    private boolean advancedPanelVisible = false;

    private boolean bookmarkShared = false;

    private String bookmarkName = null;

    private ItemBaseModel currentModel = null;

    private int maxExportRecordsCount = -1;

    private int maxImportRecordsCount = -1;

    /*************************************/

    public static interface ItemsToolBarCreator {

        ItemsToolBar newInstance();
    }

    private static ItemsToolBarCreator creator;

    public static void initialize(ItemsToolBarCreator impl) {
        ItemsToolBar.creator = impl;
    }

    private static ItemsToolBar instance;

    public static ItemsToolBar getInstance() {
        if (instance == null) {
            if (creator == null) {
                instance = new ItemsToolBar();
            } else {
                instance = creator.newInstance();
            }
        }
        return instance;
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        instance = null;
    }

    protected ItemsToolBar() {
        // init user saved model
        userCluster = BrowseRecords.getSession().getAppHeader().getDatacluster();

        maxExportRecordsCount = BrowseRecords.getSession().getAppHeader().getExportRecordsDefaultCount();
        maxImportRecordsCount = BrowseRecords.getSession().getAppHeader().getImportRecordsDefaultCount();

        this.setBorders(false);
        this.setId("ItemsToolBar"); //$NON-NLS-1$
        this.setLayout(new ToolBarLayoutEx());

        initToolBar();
    }

    public void setQueryModel(QueryModel qm) {
        qm.setDataClusterPK(userCluster);
        qm.setView(BrowseRecords.getSession().getCurrentView());
        qm.setModel(BrowseRecords.getSession().getCurrentEntityModel());
        UserSession userSession = BrowseRecords.getSession();
        EntityModel entityModel = (EntityModel) userSession.get(UserSession.CURRENT_ENTITY_MODEL);
        if (isSimple || advancedPanel == null) {
            SimpleCriterion simpCriterion = simplePanel.getCriteria();
            MultipleCriteria criteriaStore = (MultipleCriteria) BrowseRecords.getSession().get(
                    UserSession.CUSTOMIZE_CRITERION_STORE);
            if (criteriaStore == null) {
                criteriaStore = new MultipleCriteria();
                criteriaStore.setOperator("AND"); //$NON-NLS-1$
            } else {
                BrowseRecords.getSession().getCustomizeCriterionStore().getChildren().clear();
            }
            criteriaStore.add(simpCriterion);
            BrowseRecords.getSession().put(UserSession.CUSTOMIZE_CRITERION_STORE, criteriaStore);
            qm.setCriteria(simplePanel.getCriteria().toString());
            if (!CommonUtil.validateSearchValue(entityModel.getMetaDataTypes(),
                    simpCriterion.getInputValue() != null ? simpCriterion.getInputValue() : simpCriterion.getValue())) {
                qm.setErrorValue(simpCriterion.getValue());
            }
        } else {
            if (!advancedPanel.isVisible()
                    && BrowseRecords.getSession().get(UserSession.CUSTOMIZE_CRITERION_STORE_ADVANCE) != null) {
                advancedPanel.setVisible(true);
                advancedSearchButton.toggle(true);
                advancedPanelVisible = true;
                ItemsSearchContainer.getInstance().resizeTop(ItemsSearchContainer.SIZE + advancedPanel.getOffsetHeight());
            }
            qm.setCriteria(advancedPanel.getCriteria());
            MultipleCriteria multipleCriteria = (MultipleCriteria) (BrowseRecords.getSession().get(
                    UserSession.CUSTOMIZE_CRITERION_STORE_ADVANCE) == null ? BrowseRecords.getSession().get(
                    UserSession.CUSTOMIZE_CRITERION_STORE) : BrowseRecords.getSession().get(
                    UserSession.CUSTOMIZE_CRITERION_STORE_ADVANCE));
            List<SimpleCriterion> simpleCriterions = CriteriaUtil.getSimpleCriterions(multipleCriteria);
            for (SimpleCriterion criteria : simpleCriterions) {
                if (!CommonUtil.validateSearchValue(entityModel.getMetaDataTypes(),
                        criteria.getInputValue() != null ? criteria.getInputValue() : criteria.getValue())) {
                    qm.setErrorValue(criteria.getValue());
                    break;
                }
            }
        }
    }

    public void updateToolBar(ViewBean viewBean) {
        simplePanel.updateFields(viewBean);
        if (advancedPanel != null) {
            advancedPanel.setView(viewBean);
            advancedPanel.cleanCriteria();
        }
        // reset search results

        ItemsListPanel.getInstance().resetGrid();
        ItemsMainTabPanel.getInstance().removeAll();

        searchButton.setEnabled(true);
        advancedSearchButton.setEnabled(true);
        manageBookButton.setEnabled(true);
        bookMarkButton.setEnabled(true);

        createButton.setEnabled(false);
        uploadButton.setEnabled(true);
        deleteButton.setEnabled(false);
        String concept = ViewUtil.getConceptFromBrowseItemView(entityCombo.getValue().get("value").toString());//$NON-NLS-1$
        if (!viewBean.getBindingEntityModel().getMetaDataTypes().get(concept).isDenyCreatable()) {
            createButton.setEnabled(true);
            if (simulateMatchButton != null) {
                simulateMatchButton.setEnabled(true);
            }
        }
        boolean denyLogicalDelete = viewBean.getBindingEntityModel().getMetaDataTypes().get(concept).isDenyLogicalDeletable();
        boolean denyPhysicalDelete = viewBean.getBindingEntityModel().getMetaDataTypes().get(concept).isDenyPhysicalDeleteable();

        if (denyLogicalDelete && denyPhysicalDelete) {
            deleteButton.setEnabled(false);
        } else {
            deleteButton.setEnabled(true);
            if (deleteButton.getMenu() != null) {
                if (denyPhysicalDelete) {
                    deleteButton.getMenu().getItemByItemId("physicalDelMenuInGrid").setEnabled(false); //$NON-NLS-1$
                } else {
                    deleteButton.getMenu().getItemByItemId("physicalDelMenuInGrid").setEnabled(true); //$NON-NLS-1$
                }
                if (denyLogicalDelete) {
                    deleteButton.getMenu().getItemByItemId("logicalDelMenuInGrid").setEnabled(false); //$NON-NLS-1$
                } else {
                    deleteButton.getMenu().getItemByItemId("logicalDelMenuInGrid").setEnabled(true); //$NON-NLS-1$
                }
            }
        }
        if (bulkUpdateButton != null) {
            bulkUpdateButton.setEnabled(true);
        }
        updateUserCriteriasList();
        this.layout(true);
    }

    public int getSelectItemNumber() {
        int number = 0;
        number = ItemsListPanel.getInstance().getGrid().getSelectionModel().getSelectedItems().size();
        return number;
    }

    private void initToolBar() {
        addCreateButton();
        addDeleteButton();
        if (((AppHeader) BrowseRecords.getSession().get(UserSession.APP_HEADER)).isEnterprise() && isStaging()) {
            addSimulateMatchButton();
        }
        addImportAndExportButton();
        if (((AppHeader) BrowseRecords.getSession().get(UserSession.APP_HEADER)).isEnterprise() && !isStaging()) {
            addBulkUpdateButton();
        }
        add(new FillToolItem());
        addEntityCombo();
        updateEntityCombo();
        addSearchPanel();
        addSearchButton();
        add(new SeparatorToolItem());
        addAdvanceSearchButton();
        add(new SeparatorToolItem());
        addManageBookButton();
        addBookMarkButton();
        initAdvancedPanel();
        setUploadButtonVisible();
    }

    protected void addCreateButton() {
        createButton = new Button(MessagesFactory.getMessages().create_btn());
        createButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Create()));
        createButton.setEnabled(false);
        createButton.setId("BrowseRecords_Create"); //$NON-NLS-1$
        createButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                List<TabItem> tabItemList = ItemsMainTabPanel.getInstance().getItems();
                boolean isChangeCurrentRecord = false;
                String label = "";
                for (int i = 0; i < tabItemList.size(); i++) {
                    TabItem tabItem = tabItemList.get(i);
                    ItemsDetailPanel itemsDetailPanel = (ItemsDetailPanel) tabItem.getWidget(0);
                    if (itemsDetailPanel != null && itemsDetailPanel.getPrimaryKeyTabWidget() != null) {
                        // get tree root node from the primary key tab
                        ItemPanel itemPanel = (ItemPanel) itemsDetailPanel.getPrimaryKeyTabWidget();
                        ItemNodeModel root = itemPanel.getTree().getRootModel();
                        label = root != null ? root.getLabel() : "";
                        isChangeCurrentRecord = root != null ? TreeDetailUtil.isChangeValue(root) : false;
                        if (isChangeCurrentRecord) {
                            ItemsMainTabPanel.getInstance().setSelection(tabItem);
                            break;
                        }
                    }
                }
                if (isChangeCurrentRecord) {
                    MessageBox msgBox = MessageBox.confirm(MessagesFactory.getMessages().confirm_title(), MessagesFactory
                            .getMessages().msg_confirm_close_tab(label), new Listener<MessageBoxEvent>() {

                        @Override
                        public void handleEvent(MessageBoxEvent be) {
                            if (Dialog.YES.equals(be.getButtonClicked().getItemId())) {
                                displayCreatePanel();
                            } else {
                                return;
                            }
                        }
                    });
                    msgBox.getDialog().setWidth(550);
                    return;
                } else {
                    displayCreatePanel();
                }
            }

        });
        add(createButton);
    }

    protected void addDeleteButton() {
        deleteButton = new Button(MessagesFactory.getMessages().delete_btn());
        deleteButton.setEnabled(false);
        deleteButton.setId("BrowseRecords_Delete"); //$NON-NLS-1$
        deleteButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Delete()));
        Menu sub = new Menu();
        MenuItem delMenu = new MenuItem(MessagesFactory.getMessages().delete_btn());
        delMenu.setId("physicalDelMenuInGrid");//$NON-NLS-1$
        delMenu.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Delete()));

        // TODO duplicate with recordToolbar
        delMenu.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                if (ItemsListPanel.getInstance().getGrid() == null) {
                    MessageBox.alert(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages()
                            .select_delete_item_record(), null);
                } else {
                    if (getSelectItemNumber() == 0) {
                        MessageBox.alert(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages()
                                .select_delete_item_record(), null);
                    } else {
                        MessageBox.confirm(MessagesFactory.getMessages().confirm_title(), MessagesFactory.getMessages()
                                .delete_confirm(), new DeleteItemsBoxListener(service));
                    }
                }
            }
        });

        MenuItem trashMenu = new MenuItem(MessagesFactory.getMessages().trash_btn());
        trashMenu.setId("logicalDelMenuInGrid");//$NON-NLS-1$
        trashMenu.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Send_to_trash()));
        trashMenu.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {

                final ItemsListPanel list = ItemsListPanel.getInstance();
                if (list.getGrid() != null) {
                    PostDeleteAction postDeleteAction = new ListRefresh(new ContainerUpdate(NoOpPostDeleteAction.INSTANCE));
                    DeleteAction deleteAction = new LogicalDeleteAction("/"); //$NON-NLS-1$
                    service.checkFKIntegrity(list.getGrid().getSelectionModel().getSelectedItems(), new DeleteCallback(
                            deleteAction, postDeleteAction, service));
                }
            }
        });

        sub.add(trashMenu);
        sub.add(delMenu);
        deleteButton.setMenu(sub);
        add(deleteButton);
    }

    protected void addSimulateMatchButton() {
        simulateMatchButton = new Button(MessagesFactory.getMessages().compare_button());
        simulateMatchButton.setId("compareButton"); //$NON-NLS-1$
        simulateMatchButton.setToolTip(MessagesFactory.getMessages().compare_tip());
        simulateMatchButton.setEnabled(false);
        simulateMatchButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Save()));
        simulateMatchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                ItemsListPanel list = ItemsListPanel.getInstance();
                if (list.getGrid() != null) {
                    final String concept = ViewUtil.getConceptFromBrowseItemView(entityCombo.getValue().get("value").toString());//$NON-NLS-1$
                    StringBuilder ids = new StringBuilder();
                    List<ItemBean> selectedItems = list.getGrid().getSelectionModel().getSelectedItems();
                    if (selectedItems.size() > 1) {
                        ids.append(selectedItems.get(0).getIds());
                        for (int i = 1; i < selectedItems.size(); i++) {
                            ids.append("\n"); //$NON-NLS-1$
                            ids.append(selectedItems.get(i).getIds());
                        }
                        ExplainRestServiceHandler.get().simulateMatch(
                                BrowseRecords.getSession().getAppHeader().getMasterDataCluster(), concept, ids.toString(),
                                new SessionAwareAsyncCallback<BaseTreeModel>() {

                                    @Override
                                    public void onSuccess(BaseTreeModel root) {
                                        showExplainResultTable(root);
                                    }
                                });
                    } else {
                        MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                                .compare_choose_one_warning_message(), null);
                    }
                }
            }
        });
        add(simulateMatchButton);
    }

    protected void addImportAndExportButton() {
        uploadButton = new Button(MessagesFactory.getMessages().itemsBrowser_Import_Export());
        uploadButton.setId("BrowseRecords_UploadMenuInGrid"); //$NON-NLS-1$
        uploadButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Save()));
        uploadButton.setEnabled(false);
        Menu uploadMenu = new Menu();
        importMenu = new MenuItem(MessagesFactory.getMessages().import_btn());
        importMenu.setId("importRecords"); //$NON-NLS-1$
        importMenu.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Save()));
        uploadMenu.add(importMenu);

        importMenu.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                final Window window = new Window();
                window.setSize(480, 300);
                window.setPlain(true);
                window.setModal(true);
                window.setBlinkModal(true);
                window.setHeading(MessagesFactory.getMessages().upload_title());
                window.setLayout(new FitLayout());
                window.setClosable(true);
                ViewBean viewBean = BrowseRecords.getSession().getCurrentView();
                UploadFileFormPanel formPanel = WidgetFactory.getInstance().createUploadFileFormPanel(viewBean, window);
                window.add(formPanel);
                window.show();
            }
        });

        exportMenu = new MenuItem(MessagesFactory.getMessages().export_btn());
        exportMenu.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Save()));
        uploadMenu.add(exportMenu);

        exportMenu.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                final QueryModel queryModel = ItemsListPanel.getInstance().getCurrentQueryModel();
                if (queryModel != null) {
                    final Window window = new Window();
                    window.setSize(480, 220);
                    window.setPlain(true);
                    window.setModal(true);
                    window.setBlinkModal(true);
                    window.setHeading(MessagesFactory.getMessages().export_title());
                    window.setLayout(new FitLayout());
                    window.setClosable(true);
                    ViewBean viewBean = BrowseRecords.getSession().getCurrentView();
                    window.add(WidgetFactory.getInstance().createDownloadFilePanel(viewBean, queryModel, window));
                    window.show();
                }
            }
        });
        uploadButton.setMenu(uploadMenu);
        add(uploadButton);
    }

    protected void addBulkUpdateButton() {
        bulkUpdateButton = new Button(MessagesFactory.getMessages().bulkUpdate_btn());
        bulkUpdateButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.BulkUpdate()));
        bulkUpdateButton.setEnabled(false);
        bulkUpdateButton.setId("BrowseRecords_BulkUpdate"); //$NON-NLS-1$
        bulkUpdateButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                ItemsListPanel list = ItemsListPanel.getInstance();
                if (list.getGrid() != null) {
                    List<Map<String, Object>> keyMapList = new ArrayList<Map<String, Object>>();
                    List<ItemBean> selectedItems = list.getGrid().getSelectionModel().getSelectedItems();
                    if (selectedItems.size() > 0) {
                        UserSession userSession = BrowseRecords.getSession();
                        EntityModel entityModel = (EntityModel) userSession.get(UserSession.CURRENT_ENTITY_MODEL);
                        String[] keys = entityModel.getKeys();
                        for (int i = 0; i < selectedItems.size(); i++) {
                            Map<String, Object> keyMap = new HashMap<String, Object>();
                            for (int j = 0; j < keys.length; j++) {
                                keyMap.put(keys[j], selectedItems.get(i).get(keys[j]));
                            }
                            keyMapList.add(keyMap);
                        }
                        BulkUpdatePanel bulkUpdatePanel = new BulkUpdatePanel(BrowseRecords.getSession().getCurrentEntityModel(),
                                BrowseRecords.getSession().getCurrentView(), keyMapList, isStaging());
                        bulkUpdatePanel.renderPanel();
                        Registry.register(BrowseRecords.BULK_UPDATE_PANEL, bulkUpdatePanel);
                    } else {
                        MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                                .mass_update_choose_warning_message(), null);
                    }
                }
            }
        });
        add(bulkUpdateButton);
    }

    protected void addEntityCombo() {
        // add entity combo
        RpcProxy<List<ItemBaseModel>> entityproxy = new RpcProxy<List<ItemBaseModel>>() {

            @Override
            public void load(Object loadConfig, final AsyncCallback<List<ItemBaseModel>> callback) {
                service.getViewsList(Locale.getLanguage(), new SessionAwareAsyncCallback<List<ItemBaseModel>>() {

                    @Override
                    protected void doOnFailure(Throwable caught) {
                        super.doOnFailure(caught);
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(List<ItemBaseModel> result) {
                        callback.onSuccess(result);
                        entityCombo.setMode("local"); //$NON-NLS-1$
                        entityCombo.setMinChars(0);
                    }
                });
            }
        };

        ListLoader<ListLoadResult<ItemBaseModel>> entityloader = new BaseListLoader<ListLoadResult<ItemBaseModel>>(entityproxy);

        // HorizontalPanel entityPanel = new HorizontalPanel();
        final ListStore<ItemBaseModel> list = new ListStore<ItemBaseModel>(entityloader);
        entityCombo = new ComboBoxField<ItemBaseModel>();
        entityCombo.setAutoWidth(true);
        entityCombo.setEmptyText(MessagesFactory.getMessages().empty_entity());
        entityCombo.setLoadingText(MessagesFactory.getMessages().loading());
        entityCombo.setStore(list);
        entityCombo.setDisplayField("name");//$NON-NLS-1$
        entityCombo.setValueField("value");//$NON-NLS-1$
        entityCombo.setForceSelection(true);
        entityCombo.setTriggerAction(TriggerAction.ALL);
        entityCombo.setId("BrowseRecords_EntityComboBox");//$NON-NLS-1$
        entityCombo.setStyleAttribute("padding-right", "17px"); //$NON-NLS-1$ //$NON-NLS-2$

        entityCombo.addSelectionChangedListener(new SelectionChangedListener<ItemBaseModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<ItemBaseModel> se) {
                String viewPk = se.getSelectedItem().get("value").toString();//$NON-NLS-1$
                Dispatcher.forwardEvent(BrowseRecordsEvents.GetView, viewPk);
            }
        });
        add(entityCombo);
    }

    public void updateEntityCombo() {
        if (entityCombo != null && !GenerateContainer.getDefaultViewPk().equals("")) { //$NON-NLS-1$
            service.getViewsList(Locale.getLanguage(), new SessionAwareAsyncCallback<List<ItemBaseModel>>() {

                @Override
                protected void doOnFailure(Throwable caught) {
                    super.doOnFailure(caught);
                }

                @Override
                public void onSuccess(List<ItemBaseModel> result) {

                    if (!GenerateContainer.getDefaultViewPk().equals("")) { //$NON-NLS-1$
                        boolean isExist = false;
                        String pk = "Browse_items_" + GenerateContainer.getDefaultViewPk(); //$NON-NLS-1$   
                        for (ItemBaseModel bm : result) {
                            if (bm.get("value").equals(pk)) { //$NON-NLS-1$                               
                                entityCombo.setValue(bm);
                                isExist = true;
                                break;
                            }
                        }
                        if (!isExist) {
                            for (ItemBaseModel bm : result) {
                                if (bm.get("value").toString().startsWith(pk + "#")) { //$NON-NLS-1$ //$NON-NLS-2$                               
                                    entityCombo.setValue(bm);
                                    isExist = true;
                                    break;
                                }
                            }
                        }
                        if (!isExist) {
                            MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                                    .find_view_warning(GenerateContainer.getDefaultViewPk()), null);
                        }
                        GenerateContainer.setDefaultViewPk(""); //$NON-NLS-1$
                    }
                }
            });
        }
    }

    protected void addSearchPanel() {
        simplePanel = new SimpleCriterionPanel(null, null, searchButton, isStaging());
        add(simplePanel);
    }

    protected void addSearchButton() {
        searchButton.setEnabled(false);
        searchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                boolean isEmptyValue = false;
                if (!simplePanel.getOperator().equals("EMPTY_NULL") //$NON-NLS-1$
                        && (simplePanel.getValue() == null || simplePanel.getValue().equals(""))) { //$NON-NLS-1$
                    isEmptyValue = true;
                }

                if (simplePanel.getCriteria() != null && !isEmptyValue) {
                    isSimple = true;
                    String viewPk = entityCombo.getValue().get("value");//$NON-NLS-1$
                    AppEvent event = new AppEvent(BrowseRecordsEvents.SearchView, viewPk);
                    event.setData(BrowseRecordsView.IS_STAGING, isStaging());
                    Dispatcher.forwardEvent(event);
                    resizeAfterSearch();
                } else {
                    MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                            .advsearch_lessinfo(), new Listener<MessageBoxEvent>() {

                        @Override
                        public void handleEvent(MessageBoxEvent be) {
                            simplePanel.focusField();
                        }
                    });
                }
            }
        });
        searchButton.setId("BrowseRecords_Search"); //$NON-NLS-1$
        add(searchButton);
    }

    protected void addAdvanceSearchButton() {
        advancedSearchButton = new ToggleButton(MessagesFactory.getMessages().advsearch_btn());
        advancedSearchButton.setId("BrowseRecords_AdvancedSearch"); //$NON-NLS-1$
        advancedSearchButton.setEnabled(false);
        advancedSearchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                // show advanced Search panel
                advancedPanelVisible = !advancedPanelVisible;
                advancedPanel.setVisible(advancedPanelVisible);
                if (advancedPanelVisible) {
                    ItemsSearchContainer.getInstance().resizeTop(ItemsSearchContainer.SIZE + advancedPanel.getOffsetHeight());
                } else {
                    ItemsSearchContainer.getInstance().resizeTop(ItemsSearchContainer.SIZE);
                }
                advancedPanel.getButtonBar().getItemByItemId("updateBookmarkBtn").setVisible(false); //$NON-NLS-1$

                if (ItemsListPanel.getInstance().gridContainer != null) {
                    ItemsListPanel.getInstance().gridContainer.setHeight(ItemsToolBar.this.getParent().getOffsetHeight()
                            - ItemsToolBar.this.getOffsetHeight() - advancedPanel.getOffsetHeight());
                }
                if (isSimple) {
                    MultipleCriteria criteriaStore = (MultipleCriteria) BrowseRecords.getSession().get(
                            UserSession.CUSTOMIZE_CRITERION_STORE);
                    advancedPanel.setCriteria(criteriaStore.toString());
                }
            }
        });
        add(advancedSearchButton);
    }

    protected void addManageBookButton() {
        manageBookButton = new Button();
        manageBookButton.setId("BrowseRecords_ManageBookMark"); //$NON-NLS-1$
        manageBookButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Display()));
        manageBookButton.setTitle(MessagesFactory.getMessages().bookmarkmanagement_heading());
        manageBookButton.setEnabled(false);
        manageBookButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final Window winBookmark = new Window();
                winBookmark.setHeading(MessagesFactory.getMessages().bookmarkmanagement_heading());
                winBookmark.setAutoHeight(true);
                // winBookmark.setAutoWidth(true);
                winBookmark.setWidth(563);
                winBookmark.setModal(true);
                FormPanel content = new FormPanel();
                FormData formData = new FormData("-10");//$NON-NLS-1$
                content.setFrame(false);
                content.setLayout(new FitLayout());
                content.setBodyBorder(false);
                content.setHeaderVisible(false);
                content.setSize(550, 350);
                winBookmark.add(content);

                // display bookmark grid
                RpcProxy<PagingLoadResult<ItemBaseModel>> proxyBookmark = new RpcProxy<PagingLoadResult<ItemBaseModel>>() {

                    @Override
                    public void load(Object loadConfig, final AsyncCallback<PagingLoadResult<ItemBaseModel>> callback) {
                        BasePagingLoadConfigImpl baseConfig = BasePagingLoadConfigImpl
                                .copyPagingLoad((PagingLoadConfig) loadConfig);
                        service.querySearchTemplates(entityCombo.getValue().get("value").toString(), true, //$NON-NLS-1$
                                baseConfig, new SessionAwareAsyncCallback<ItemBasePageLoadResult<ItemBaseModel>>() {

                                    @Override
                                    protected void doOnFailure(Throwable caught) {
                                        super.doOnFailure(caught);
                                        callback.onFailure(caught);
                                    }

                                    @Override
                                    public void onSuccess(ItemBasePageLoadResult<ItemBaseModel> result) {
                                        callback.onSuccess(new BasePagingLoadResult<ItemBaseModel>(result.getData(), result
                                                .getOffset(), result.getTotalLength()));
                                    }
                                });
                    }
                };

                // loader
                final PagingLoader<PagingLoadResult<ItemBaseModel>> loaderBookmark = new BasePagingLoader<PagingLoadResult<ItemBaseModel>>(
                        proxyBookmark);

                ListStore<ItemBaseModel> store = new ListStore<ItemBaseModel>(loaderBookmark);
                store.setDefaultSort("name", SortDir.ASC); //$NON-NLS-1$

                final PagingToolBarEx pagetoolBar = new PagingToolBarEx(PAGE_SIZE);
                pagetoolBar.bind(loaderBookmark);

                List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
                columns.add(new ColumnConfig("name", MessagesFactory.getMessages().bookmark_heading(), 200)); //$NON-NLS-1$
                ColumnConfig colEdit = new ColumnConfig("value", MessagesFactory.getMessages().bookmark_edit(), 100); //$NON-NLS-1$
                colEdit.setRenderer(new GridCellRenderer<ItemBaseModel>() {

                    @Override
                    public Object render(final ItemBaseModel model, String property, ColumnData config, int rowIndex,
                            int colIndex, ListStore<ItemBaseModel> store, Grid<ItemBaseModel> grid) {
                        Image image = new Image();
                        image.setResource(Icons.INSTANCE.Edit());
                        if (!ifManage(model)) {
                            image.addStyleName("x-item-disabled");//$NON-NLS-1$
                        } else {
                            image.addClickHandler(new ClickHandler() {

                                @Override
                                public void onClick(ClickEvent event) {
                                    // edit the bookmark
                                    if (advancedPanel == null) {
                                        advancedPanel = new AdvancedSearchPanel(simplePanel.getView(), null);
                                    }
                                    service.getCriteriaByBookmark(
                                            model.get("value").toString(), new SessionAwareAsyncCallback<String>() { //$NON-NLS-1$

                                                @Override
                                                public void onSuccess(String arg0) {
                                                    // set criteria
                                                    if (arg0 != null) {
                                                        advancedPanel.setCriteria(arg0);
                                                        // advancedPanel.setCriteriaAppearance(arg0, arg0);
                                                    } else {
                                                        advancedPanel.cleanCriteria();
                                                    }
                                                    advancedPanelVisible = true;
                                                    advancedPanel.setVisible(advancedPanelVisible);
                                                    ItemsSearchContainer.getInstance().resizeTop(
                                                            ItemsSearchContainer.SIZE + advancedPanel.getOffsetHeight());
                                                    advancedPanel.getButtonBar().getItemByItemId("updateBookmarkBtn") //$NON-NLS-1$
                                                            .setVisible(true);
                                                    bookmarkName = model.get("value").toString(); //$NON-NLS-1$
                                                    bookmarkShared = Boolean.parseBoolean(model.get("shared").toString()); //$NON-NLS-1$
                                                    if (ItemsListPanel.getInstance().gridContainer != null) {
                                                        ItemsListPanel.getInstance().gridContainer.setHeight(ItemsToolBar.this
                                                                .getParent().getOffsetHeight()
                                                                - ItemsToolBar.this.getOffsetHeight()
                                                                - advancedPanel.getOffsetHeight());
                                                    }
                                                    winBookmark.close();
                                                }

                                            });
                                }

                            });
                        }
                        return image;
                    }

                });
                columns.add(colEdit);

                ColumnConfig colDel = new ColumnConfig("value", MessagesFactory.getMessages().bookmark_del(), 100); //$NON-NLS-1$
                colDel.setRenderer(new GridCellRenderer<ItemBaseModel>() {

                    @Override
                    public Object render(final ItemBaseModel model, String property, ColumnData config, int rowIndex,
                            int colIndex, ListStore<ItemBaseModel> store, Grid<ItemBaseModel> grid) {
                        Image image = new Image();
                        image.setResource(Icons.INSTANCE.Delete());
                        if (!ifManage(model)) {
                            image.addStyleName("x-item-disabled"); //$NON-NLS-1$
                        } else {
                            image.addClickHandler(new ClickHandler() {

                                @Override
                                public void onClick(ClickEvent event) {
                                    MessageBox.confirm(MessagesFactory.getMessages().confirm_title(), MessagesFactory
                                            .getMessages().bookmark_DelMsg(), new Listener<MessageBoxEvent>() {

                                        @Override
                                        public void handleEvent(MessageBoxEvent be) {
                                            if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                                                // delete the bookmark
                                                service.deleteSearchTemplate(model.get("value").toString(), //$NON-NLS-1$
                                                        new SessionAwareAsyncCallback<Void>() {

                                                            @Override
                                                            public void onSuccess(Void arg0) {
                                                                loaderBookmark.load();
                                                            }

                                                        });
                                            }
                                        }
                                    });
                                }

                            });
                        }
                        return image;
                    }

                });

                columns.add(colDel);

                ColumnConfig colSearch = new ColumnConfig("value", MessagesFactory.getMessages().bookmark_search(), 100); //$NON-NLS-1$
                colSearch.setRenderer(new GridCellRenderer<ItemBaseModel>() {

                    @Override
                    public Object render(final ItemBaseModel model, String property, ColumnData config, int rowIndex,
                            int colIndex, ListStore<ItemBaseModel> store, Grid<ItemBaseModel> grid) {
                        Image image = new Image();
                        image.setResource(Icons.INSTANCE.dosearch());
                        image.addClickHandler(new ClickHandler() {

                            @Override
                            public void onClick(ClickEvent event) {
                                doSearch(model, winBookmark);
                            }

                        });
                        return image;
                    }

                });
                columns.add(colSearch);

                ColumnModel cm = new ColumnModel(columns);

                final Grid<ItemBaseModel> bookmarkgrid = new Grid<ItemBaseModel>(store, cm);
                if (cm.getColumnCount() > 0) {
                    bookmarkgrid.setAutoExpandColumn(cm.getColumn(0).getHeader());
                }
                bookmarkgrid.getView().setForceFit(true);
                bookmarkgrid.addListener(Events.Attach, new Listener<GridEvent<ItemBaseModel>>() {

                    @Override
                    public void handleEvent(GridEvent<ItemBaseModel> be) {
                        PagingLoadConfig config = new BasePagingLoadConfig();
                        config.setOffset(0);
                        config.setLimit(PAGE_SIZE);
                        loaderBookmark.load(config);
                    }
                });

                bookmarkgrid.addListener(Events.OnDoubleClick, new Listener<GridEvent<ItemBaseModel>>() {

                    @Override
                    public void handleEvent(final GridEvent<ItemBaseModel> be) {
                        doSearch(be.getModel(), winBookmark);
                    }
                });

                bookmarkgrid.setLoadMask(true);
                bookmarkgrid.setBorders(false);

                content.setBottomComponent(pagetoolBar);
                content.add(bookmarkgrid, formData);

                winBookmark.show();
            }

        });
        add(manageBookButton);
    }

    protected void addBookMarkButton() {
        bookMarkButton = new Button();
        bookMarkButton.setId("BrowseRecords_BookMark"); //$NON-NLS-1$
        bookMarkButton.setTitle(MessagesFactory.getMessages().advsearch_bookmark());
        bookMarkButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Save()));
        bookMarkButton.setEnabled(false);
        bookMarkButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                showBookmarkSavedWin(true);
            }

        });
        add(bookMarkButton);
    }

    private void updateUserCriteriasList() {
        service.getUserCriterias(entityCombo.getValue().get("value").toString(), //$NON-NLS-1$
                new SessionAwareAsyncCallback<List<ItemBaseModel>>() {

                    @Override
                    public void onSuccess(List<ItemBaseModel> list) {
                        userCriteriasList = list;
                    }

                });
    }

    private boolean ifManage(ItemBaseModel model) {
        // only the shared bookmark could be managed
        Iterator<ItemBaseModel> i = userCriteriasList.iterator();
        while (i.hasNext()) {
            if ((i.next()).get("value").equals( //$NON-NLS-1$
                    model.get("value").toString())) { //$NON-NLS-1$    
                return true;
            }
        }

        return false;
    }

    private void doSearch(final ItemBaseModel model, final Window winBookmark) {
        service.getCriteriaByBookmark(model.get("value").toString(), //$NON-NLS-1$
                new SessionAwareAsyncCallback<String>() {

                    @Override
                    public void onSuccess(String arg0) {
                        isSimple = false;
                        if (advancedPanel == null) {
                            advancedPanel = new AdvancedSearchPanel(simplePanel.getView(), null);
                        }

                        MultipleCriteria mutilCriteria = new MultipleCriteria();
                        if (CommonUtil.isSimpleCriteria(arg0)) {
                            SimpleCriterion criteria = buildCriterion(arg0);
                            mutilCriteria.add(criteria);
                        } else if (arg0.indexOf(" AND ") != -1) { //$NON-NLS-1$
                            mutilCriteria.setOperator("AND"); //$NON-NLS-1$
                            String[] xquery = arg0.split("AND"); //$NON-NLS-1$
                            for (int i = 0; i < xquery.length && xquery[i].indexOf(AdvancedSearchPanel.modifiedON) == -1; i++) {
                                mutilCriteria.add(buildCriterion(xquery[i]));
                            }
                        } else if (arg0.indexOf(" OR ") != -1) { //$NON-NLS-1$
                            mutilCriteria.setOperator("OR"); //$NON-NLS-1$
                            String[] xquery = arg0.split("OR"); //$NON-NLS-1$
                            for (int i = 0; i < xquery.length && xquery[i].indexOf(AdvancedSearchPanel.modifiedON) == -1; i++) {
                                mutilCriteria.add(buildCriterion(xquery[i]));
                            }
                        }

                        BrowseRecords.getSession().put(UserSession.CUSTOMIZE_CRITERION_STORE, mutilCriteria);
                        advancedPanel.setCriteria(arg0);
                        ItemsSearchContainer.getInstance().resizeTop(ItemsSearchContainer.SIZE + advancedPanel.getOffsetHeight());
                        String viewPk = entityCombo.getValue().get("value"); //$NON-NLS-1$
                        AppEvent event = new AppEvent(BrowseRecordsEvents.SearchView, viewPk);
                        event.setData(BrowseRecordsView.IS_STAGING, isStaging());
                        Dispatcher.forwardEvent(event);
                        winBookmark.close();
                    }

                });

    }

    private SimpleCriterion buildCriterion(String xquery) {
        String[] query = xquery.trim().split(" "); //$NON-NLS-1$
        SimpleCriterion criteria = null;
        if (query.length == 3) {
            criteria = new SimpleCriterion(query[0].replace("(", ""), query[1], query[2].replace(")", "")); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

        return criteria;
    }

    public FormPanel getAdvancedPanel() {
        return advancedPanel;
    }

    private void resizeAfterSearch() {
        advancedPanelVisible = false;
        advancedPanel.setVisible(advancedPanelVisible);
        ItemsSearchContainer.getInstance().resizeTop(ItemsSearchContainer.SIZE);
        advancedSearchButton.toggle(advancedPanelVisible);
        // resize result grid
        if (ItemsListPanel.getInstance().gridContainer != null) {
            ItemsListPanel.getInstance().gridContainer.setHeight(ItemsToolBar.this.getParent().getOffsetHeight()
                    - ItemsToolBar.this.getOffsetHeight() - advancedPanel.getOffsetHeight());
        }
    }

    private void initAdvancedPanel() {
        if (advancedPanel == null) {
            Button searchBtn = new Button(MessagesFactory.getMessages().search_btn());
            advancedPanel = new AdvancedSearchPanel(simplePanel.getView(), searchBtn, isStaging());
            advancedPanel.setItemId("advancedPanel"); //$NON-NLS-1$
            advancedPanel.setButtonAlign(HorizontalAlignment.CENTER);

            searchBtn.setItemId("searchBtn"); //$NON-NLS-1$
            searchBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    String criteria = advancedPanel.getCriteria();
                    if (criteria == null || criteria.equals("")) { //$NON-NLS-1$
                        MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                                .search_expression_notempty(), null);
                    } else {
                        isSimple = false;
                        String viewPk = entityCombo.getValue().get("value"); //$NON-NLS-1$
                        AppEvent event = new AppEvent(BrowseRecordsEvents.SearchView, viewPk);
                        event.setData(BrowseRecordsView.IS_STAGING, isStaging());
                        Dispatcher.forwardEvent(event);
                        resizeAfterSearch();
                    }
                }

            });
            advancedPanel.addButton(searchBtn);

            Button advancedBookmarkBtn = new Button(MessagesFactory.getMessages().advsearch_bookmark());
            advancedBookmarkBtn.setItemId("advancedBookmarkBtn"); //$NON-NLS-1$
            advancedBookmarkBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    showBookmarkSavedWin(false);
                }

            });
            advancedPanel.addButton(advancedBookmarkBtn);

            Button updateBookmarkBtn = new Button(MessagesFactory.getMessages().bookmark_update());
            updateBookmarkBtn.setItemId("updateBookmarkBtn"); //$NON-NLS-1$
            updateBookmarkBtn.setVisible(false);
            updateBookmarkBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    saveBookmark(bookmarkName, bookmarkShared, advancedPanel.getCriteria(), null);
                }

            });
            advancedPanel.addButton(updateBookmarkBtn);

            Button cancelBtn = new Button(MessagesFactory.getMessages().button_reset());
            cancelBtn.setItemId("cancelBtn"); //$NON-NLS-1$
            cancelBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    advancedPanel.cleanCriteria();
                    BrowseRecords.getSession().put(UserSession.CUSTOMIZE_CRITERION_STORE_ADVANCE, null);
                    advancedPanel.layout(true);
                    ItemsSearchContainer.getInstance().resizeTop(ItemsSearchContainer.SIZE + advancedPanel.getOffsetHeight());

                    if (ItemsListPanel.getInstance().gridContainer != null) {
                        ItemsListPanel.getInstance().gridContainer.setHeight(ItemsToolBar.this.getParent().getOffsetHeight()
                                - ItemsToolBar.this.getOffsetHeight() - advancedPanel.getOffsetHeight());
                    }
                }

            });
            advancedPanel.addButton(cancelBtn);
            advancedPanel.setVisible(false);
        }
    }

    private void showBookmarkSavedWin(final boolean ifSimple) {
        final Window winBookmark = new Window();
        winBookmark.setHeading(MessagesFactory.getMessages().bookmark_heading());
        // winBookmark.setAutoHeight(true);
        // winBookmark.setAutoWidth(true);
        winBookmark.setWidth(355);
        winBookmark.setHeight(191);
        final FormPanel content = new FormPanel();
        content.setFrame(false);
        content.setBodyBorder(false);
        content.setHeaderVisible(false);
        content.setButtonAlign(HorizontalAlignment.CENTER);
        content.setLabelWidth(100);
        content.setFieldWidth(200);
        final CheckBox cb = new CheckBox();
        cb.setFieldLabel(MessagesFactory.getMessages().bookmark_shared());
        content.add(cb);

        final TextField<String> bookmarkfield = new TextField<String>();
        bookmarkfield.setFieldLabel(MessagesFactory.getMessages().bookmark_name());
        Validator validator = new Validator() {

            @Override
            public String validate(Field<?> field, String value) {
                if (field == bookmarkfield) {
                    if (bookmarkfield.getValue() == null || bookmarkfield.getValue().trim().equals("")) { //$NON-NLS-1$
                        return MessagesFactory.getMessages().required_field();
                    }
                }

                return null;
            }
        };
        bookmarkfield.setValidator(validator);
        content.add(bookmarkfield);

        Button btn = new Button(MessagesFactory.getMessages().ok_btn());
        btn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (bookmarkfield.getValue() == null || !content.isValid()) {
                    MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                            .bookmark_nameNotBlank(), null);
                    return;
                } else if (bookmarkfield.getValue().contains("&")) { //$NON-NLS-1$
                    MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                            .bookmark_invalid("&"), null); //$NON-NLS-1$
                    return;
                }
                service.isExistCriteria(entityCombo.getValue().get("value").toString(), bookmarkfield.getValue(), //$NON-NLS-1$
                        new SessionAwareAsyncCallback<Boolean>() {

                            @Override
                            public void onSuccess(Boolean exists) {
                                if (!exists) {
                                    String curCriteria = null;
                                    if (ifSimple) {
                                        curCriteria = simplePanel.getCriteria().toString();
                                    } else {
                                        curCriteria = advancedPanel.getCriteria();
                                    }
                                    saveBookmark(bookmarkfield.getValue().toString(), cb.getValue(), curCriteria, winBookmark);
                                } else {
                                    MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                                            .bookmark_existMsg(), null);
                                }
                            }

                        });
            }
        });
        winBookmark.addButton(btn);

        winBookmark.add(content);
        winBookmark.show();
    }

    private void saveBookmark(String name, boolean shared, String curCriteria, final Window winBookmark) {
        if (curCriteria.contains("&amp;#91;") || curCriteria.contains("&amp;#92;") || curCriteria.contains("&amp;#93;")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            curCriteria = curCriteria.replace("&", "&amp;"); //$NON-NLS-1$//$NON-NLS-2$
        }
        service.saveCriteria(entityCombo.getValue().get("value").toString(), name, shared, curCriteria, //$NON-NLS-1$
                new SessionAwareAsyncCallback<Void>() {

                    @Override
                    protected void doOnFailure(Throwable caught) {
                        MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                                .bookmark_saveFailed(), null);
                    }

                    @Override
                    public void onSuccess(Void arg0) {
                        MessageBox.info(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages()
                                .bookmark_saveSuccess(), null);
                        updateUserCriteriasList();
                        if (winBookmark != null) {
                            winBookmark.close();
                        }
                    }

                });
    }

    private void showExplainResultTable(BaseTreeModel root) {
        SimulateTablePanel simulateTable = new SimulateTablePanel();
        Window explainWindow = new Window();
        explainWindow.setHeading(MessagesFactory.getMessages().compare_result_title());
        explainWindow.setSize(800, 600);
        explainWindow.setLayout(new FitLayout());
        explainWindow.setScrollMode(Scroll.NONE);
        simulateTable.buildTree(root);
        explainWindow.add(simulateTable);
        explainWindow.show();
    }

    private void displayCreatePanel() {
        ItemsMainTabPanel.getInstance().removeAll();
        ItemsListPanel.getInstance().setCreate(true);
        String concept = ViewUtil.getConceptFromBrowseItemView(entityCombo.getValue().get("value").toString());//$NON-NLS-1$

        EntityModel entityModel = BrowseRecords.getSession().getCurrentEntityModel();
        ItemBean itemBean = ItemCreator.createDefaultItemBean(concept, entityModel);

        if (ItemsListPanel.getInstance().getGrid() != null) {
            ItemsListPanel.getInstance().getGrid().getSelectionModel().deselectAll();
        }

        ItemsDetailPanel panel = ItemsDetailPanel.newInstance();
        panel.setStaging(isStaging());
        List<String> pkInfoList = new ArrayList<String>();
        pkInfoList.add(itemBean.getLabel());
        panel.initBanner(pkInfoList, itemBean.getDescription());
        List<BreadCrumbModel> breads = new ArrayList<BreadCrumbModel>();
        if (itemBean != null) {
            breads.add(new BreadCrumbModel("", BreadCrumb.DEFAULTNAME, null, null, false)); //$NON-NLS-1$
            breads.add(new BreadCrumbModel(itemBean.getConcept(), itemBean.getLabel(), null, null, true));
        }
        panel.initBreadCrumb(new BreadCrumb(breads, panel));
        ViewBean viewBean = (ViewBean) BrowseRecords.getSession().get(UserSession.CURRENT_VIEW);
        ItemPanel itemPanel = new ItemPanel(panel);
        panel.addTabItem(itemBean.getLabel(), itemPanel, ItemsDetailPanel.SINGLETON, itemBean.getConcept());
        itemPanel.initTreeDetail(viewBean, itemBean, ItemDetailToolBar.CREATE_OPERATION, isStaging());
        ItemsMainTabPanel.getInstance().addMainTabItem(itemBean.getLabel(), panel, itemBean.getConcept());
        CommonUtil.setCurrentCachedEntity(itemBean.getConcept() + panel.isOutMost(), itemPanel);
    }

    public ItemBaseModel getCurrentModel() {
        return currentModel;
    }

    public void setCurrentModel(ItemBaseModel currentModel) {
        this.currentModel = currentModel;
    }

    public SimpleCriterionPanel<?> getSimplePanel() {
        return simplePanel;
    }

    public void setSimplePanel(SimpleCriterionPanel<?> simplePanel) {
        this.simplePanel = simplePanel;
    }

    protected class DeleteItemsBoxListener implements Listener<MessageBoxEvent> {

        private final ItemsListPanel list;

        private final BrowseRecordsServiceAsync service;

        public DeleteItemsBoxListener(BrowseRecordsServiceAsync service) {
            this.service = service;
            this.list = ItemsListPanel.getInstance();
        }

        @Override
        public void handleEvent(MessageBoxEvent be) {
            if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                if (list.getGrid() != null) {
                    PostDeleteAction postDeleteAction = new ListRefresh(new ContainerUpdate(NoOpPostDeleteAction.INSTANCE));
                    service.checkFKIntegrity(list.getGrid().getSelectionModel().getSelectedItems(), new DeleteCallback(
                            DeleteAction.PHYSICAL, postDeleteAction, service));
                }

            }
        }
    }

    protected boolean isStaging() {
        return false;
    }

    public ComboBoxField<ItemBaseModel> getEntityCombo() {
        return entityCombo;
    }

    protected void setUploadButtonVisible() {
        if (maxImportRecordsCount == 0 && maxExportRecordsCount == 0) {
            uploadButton.setVisible(false);
        }

        if (maxImportRecordsCount == 0) {
            importMenu.setVisible(false);
        }
        if (maxExportRecordsCount == 0) {
            exportMenu.setVisible(false);
        }
    }

    public Button getUploadButton() {
        return uploadButton;
    }

    public MenuItem getImportMenu() {
        return importMenu;
    }

    public MenuItem getExportMenu() {
        return exportMenu;
    }

    protected void openDebugBulkUpdatePanel(BulkUpdatePanel panel) {
        Window window = new Window();
        window.setLayout(new FitLayout());
        window.add(panel);
        window.setSize(1100, 700);
        window.setMaximizable(true);
        window.setModal(false);
        window.show();
    }

    protected native void openBulkUpdatePanel(String id, BulkUpdatePanel bulkUpdatePanel)/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		var panel = tabPanel.getItem(id);
		if (panel == undefined) {
			var panel = @org.talend.mdm.webapp.browserecords.client.widget.ItemsToolBar::convertBulkUpdatePanel(Lorg/talend/mdm/webapp/browserecords/client/widget/BulkUpdatePanel;)(bulkUpdatePanel);
			tabPanel.add(panel);
		}
		tabPanel.setSelection(id);
    }-*/;

    private native static JavaScriptObject convertBulkUpdatePanel(BulkUpdatePanel bulkUpdatePanel)/*-{
		var panel = {
			// imitate extjs's render method, really call gxt code.
			render : function(el) {
				var rootPanel = @com.google.gwt.user.client.ui.RootPanel::get(Ljava/lang/String;)(el.id);
				rootPanel.@com.google.gwt.user.client.ui.RootPanel::add(Lcom/google/gwt/user/client/ui/Widget;)(bulkUpdatePanel);
			},
			// imitate extjs's setSize method, really call gxt code.
			setSize : function(width, height) {
				bulkUpdatePanel.@org.talend.mdm.webapp.browserecords.client.widget.BulkUpdatePanel::setSize(II)(width, height);
			},
			// imitate extjs's getItemId, really return itemId of ContentPanel of GXT.
			getItemId : function() {
				return bulkUpdatePanel.@org.talend.mdm.webapp.browserecords.client.widget.BulkUpdatePanel::getItemId()();
			},
			// imitate El object of extjs
			getEl : function() {
				var el = bulkUpdatePanel.@org.talend.mdm.webapp.browserecords.client.widget.BulkUpdatePanel::getElement()();
				return {
					dom : el
				};
			},
			// imitate extjs's doLayout method, really call gxt code.
			doLayout : function() {
				return bulkUpdatePanel.@org.talend.mdm.webapp.browserecords.client.widget.BulkUpdatePanel::doLayout()();
			},
			title : function() {
				return bulkUpdatePanel.@org.talend.mdm.webapp.browserecords.client.widget.BulkUpdatePanel::getHeading()();
			}
		};
		return panel;
    }-*/;
}