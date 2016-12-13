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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.exception.ParserException;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.client.model.ItemResult;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.client.util.Parser;
import org.talend.mdm.webapp.base.client.widget.ColumnAlignGrid;
import org.talend.mdm.webapp.base.client.widget.PagingToolBarEx;
import org.talend.mdm.webapp.base.shared.Constants;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsEvents;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.handler.ItemTreeHandler;
import org.talend.mdm.webapp.browserecords.client.handler.ItemTreeHandlingStatus;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.model.QueryModel;
import org.talend.mdm.webapp.browserecords.client.model.RecordsPagingConfig;
import org.talend.mdm.webapp.browserecords.client.mvc.BrowseRecordsView;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.util.DateUtil;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.ForeignKeyTreeDetail;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailUtil;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HideMode;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.RowEditorEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.RowEditor;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;

public class ItemsListPanel extends ContentPanel {

    public interface ReLoadData {

        void onReLoadData();
    }

    ReLoadData reLoad;

    List<ItemBean> selectedItems = null;

    private boolean isCreate = false;

    private boolean isCheckbox;

    public boolean isCreate() {
        return isCreate;
    }

    public void setCreate(boolean isCreate) {
        this.isCreate = isCreate;
    }

    private QueryModel currentQueryModel;

    private ItemBean createItemBean = null;

    private PagingLoadConfig pagingLoadConfig;

    private boolean isPagingAccurate;

    BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);

    RpcProxy<PagingLoadResult<ItemBean>> proxy = new RpcProxy<PagingLoadResult<ItemBean>>() {

        @Override
        public void load(Object loadConfig, final AsyncCallback<PagingLoadResult<ItemBean>> callback) {
            pagingLoadConfig = (PagingLoadConfig) loadConfig;
            final QueryModel qm = new QueryModel();
            ItemsToolBar.getInstance().setQueryModel(qm);
            qm.setPagingLoadConfig(copyPgLoad(pagingLoadConfig));
            int pageSize = pagingBar.getPageSize();
            qm.getPagingLoadConfig().setLimit(pageSize);
            qm.setLanguage(Locale.getLanguage());

            // validate criteria on client-side first
            try {
                if (qm.getCriteria() == null) {
                    return;
                } else if (qm.getErrorValue() != null && !"".equals(qm.getErrorValue())) { //$NON-NLS-1$
                    MessageBox.alert(MessagesFactory.getMessages().search_field_error_title(), MessagesFactory.getMessages()
                            .search_field_error_info(qm.getErrorValue()), null);
                    callback.onSuccess(new BasePagingLoadResult<ItemBean>(new ArrayList<ItemBean>(), 0, 0));
                    return;
                }
                if (BrowseRecords.getStagingArea() != null) {
                    JavaScriptObject stagingArea = BrowseRecords.getStagingArea();
                    qm.setDataClusterPK(getDataContainer(stagingArea));
                    qm.setCriteria(getCriteria(stagingArea));
                }
                Parser.parse(qm.getCriteria());
            } catch (ParserException e) {
                MessageBox.alert(MessagesFactory.getMessages().error_title(), e.getMessage(), null);
                callback.onSuccess(new BasePagingLoadResult<ItemBean>(new ArrayList<ItemBean>(), 0, 0));
                return;
            }

            service.queryItemBeans(qm, Locale.getLanguage(), new SessionAwareAsyncCallback<ItemBasePageLoadResult<ItemBean>>() {

                @Override
                public void onSuccess(ItemBasePageLoadResult<ItemBean> result) {
                    isPagingAccurate = result.isPagingAccurate();
                    callback.onSuccess(new BasePagingLoadResult<ItemBean>(result.getData(), result.getOffset(), result
                            .getTotalLength()));
                    if (result.getTotalLength() == 0) {
                        ItemsMainTabPanel.getInstance().removeAll();
                    }
                    currentQueryModel = qm;
                }

                @Override
                protected void doOnFailure(Throwable caught) {
                    super.doOnFailure(caught);
                    callback.onSuccess(new BasePagingLoadResult<ItemBean>(new ArrayList<ItemBean>(), 0, 0));
                    currentQueryModel = null;
                }
            });
        }
    };

    private native String getDataContainer(JavaScriptObject stagingAreaConfig)/*-{
		return stagingAreaConfig.dataContainer;
    }-*/;

    private native String getCriteria(JavaScriptObject stagingAreaConfig)/*-{
		return stagingAreaConfig.criteria;
    }-*/;

    private RecordsPagingConfig copyPgLoad(PagingLoadConfig pconfig) {
        RecordsPagingConfig rpConfig = new RecordsPagingConfig();
        rpConfig.setLimit(pconfig.getLimit());
        rpConfig.setOffset(pconfig.getOffset());
        rpConfig.setSortDir(pconfig.getSortDir() == null ? "NONE" : pconfig.getSortDir().toString()); //$NON-NLS-1$
        rpConfig.setSortField(pconfig.getSortField());
        return rpConfig;
    }

    public QueryModel getCurrentQueryModel() {
        return currentQueryModel;
    }

    ModelKeyProvider<ItemBean> keyProvidernew = new ModelKeyProvider<ItemBean>() {

        @Override
        public String getKey(ItemBean model) {
            return model.getIds();
        }
    };

    PagingLoader<PagingLoadResult<ModelData>> loader;

    ListStore<ItemBean> store;

    protected Grid<ItemBean> grid;

    private RowEditor<ItemBean> re;

    ContentPanel gridContainer;

    private ContentPanel panel;

    private PagingToolBarEx pagingBar = null;

    private Boolean gridUpdateLock = Boolean.FALSE;

    private ItemDetailToolBar toolBar;

    private ItemNodeModel root;

    private boolean defaultSelectionModel = true;

    private boolean saveCurrentChangeBeforeSwitching;

    private String changedRecordId;

    private static ItemsListPanel instance;

    public static void initialize(ItemsListPanel instanceImpl) {
        ItemsListPanel.instance = instanceImpl;
    }

    public static ItemsListPanel getInstance() {
        if (instance == null) {
            instance = new ItemsListPanel();
        }
        return instance;
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        instance = null;
    }

    protected ItemsListPanel() {
        setLayout(new FitLayout());
        setHeaderVisible(false);
        initPanel();
        this.layout();
    }

    private void initPanel() {
        panel = new ContentPanel();
        Html promptMsg = new Html("<div class=\"promptMsg\">" + MessagesFactory.getMessages().search_initMsg() + "</div>");//$NON-NLS-1$ //$NON-NLS-2$
        panel.setBodyBorder(false);
        panel.setBorders(false);
        panel.setHeaderVisible(false);
        panel.add(promptMsg);
        add(panel);
    }

    public void updateGrid(CheckBoxSelectionModel<ItemBean> sm, List<ColumnConfig> columnConfigList) {
        // toolBar.searchBut.setEnabled(false);
        if (gridContainer != null && this.findItem(gridContainer.getElement()) != null) {
            remove(gridContainer);
        }
        if (panel != null && this.findItem(panel.getElement()) != null) {
            remove(panel);
        }

        ColumnModel cm = new ColumnModel(columnConfigList);
        gridContainer = new ContentPanel(new FitLayout());
        gridContainer.setBodyBorder(false);
        gridContainer.setHeaderVisible(false);

        loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
        loader.setRemoteSort(true);

        store = new ListStore<ItemBean>(loader);
        store.setKeyProvider(keyProvidernew);

        loader.addLoadListener(new LoadListener() {

            @Override
            public void loaderLoad(LoadEvent le) {
                if (store.getModels().size() > 0) {
                    if (selectedItems == null) {
                        // search and create
                        if (isCreate && createItemBean != null) {
                            ItemBean findModel = grid.getStore().findModel(createItemBean.getIds());
                            if (findModel != null) {
                                grid.getSelectionModel().select(findModel, true);
                            } else {
                                grid.getSelectionModel().select(-1, false);
                            }
                        } else {
                            grid.getSelectionModel().select(0, false);
                        }
                        isCreate = false;
                        createItemBean = null;
                    }
                } else {
                    ItemsToolBar.getInstance().searchButton.setEnabled(true);
                    // ItemsMainTabPanel.getInstance().getCurrentViewTabItem().clearAll();
                }
                if (reLoad != null) {
                    reLoad.onReLoadData();
                }
            }
        });

        int usePageSize = Constants.PAGE_SIZE;
        if (Cookies.getCookie(PagingToolBarEx.BROWSERECORD_PAGESIZE) != null) {
            usePageSize = Integer.parseInt(Cookies.getCookie(PagingToolBarEx.BROWSERECORD_PAGESIZE));
        }

        pagingBar = new PagingToolBarEx(usePageSize) {

            @Override
            protected void onLoad(LoadEvent event) {
                String of_word = MessagesFactory.getMessages().of_word();
                msgs.setDisplayMsg("{0} - {1} " + of_word + " " + (isPagingAccurate ? "" : "~") + "{2}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                super.onLoad(event);
            }
        };
        pagingBar.setBrowseRecordsGridCall(true);
        pagingBar.setHideMode(HideMode.VISIBILITY);
        pagingBar.getMessages().setDisplayMsg(MessagesFactory.getMessages().page_displaying_records());

        pagingBar.setVisible(false);
        pagingBar.bind(loader);
        gridContainer.setBottomComponent(pagingBar);
        grid = new ColumnAlignGrid<ItemBean>(store, cm);
        grid.setSelectionModel(sm);

        {
            grid.setStateful(true);
            AppHeader header = (AppHeader) BrowseRecords.getSession().get(UserSession.APP_HEADER);
            ViewBean vb = (ViewBean) BrowseRecords.getSession().get(UserSession.CURRENT_VIEW);
            EntityModel em = vb.getBindingEntityModel();
            grid.setStateId(header.getDatamodel() + "." + em.getConceptName() + "." + vb.getViewPK()); //$NON-NLS-1$//$NON-NLS-2$
        }

        re = new SaveRowEditor();
        grid.getView().setForceFit(true);
        if (cm.getColumnCount() > 0) {
            grid.setAutoExpandColumn(cm.getColumn(0).getHeader());
        }

        grid.addListener(Events.HeaderClick, new Listener<GridEvent<?>>() {

            @Override
            public void handleEvent(GridEvent<?> be) {
                if (be.getColIndex() == 0) {
                    ItemsListPanel.this.isCheckbox = true;
                } else {
                    ItemsListPanel.this.isCheckbox = false;
                }
            }

        });

        grid.addListener(Events.OnMouseOver, new Listener<GridEvent<ItemBean>>() {

            @Override
            public void handleEvent(GridEvent<ItemBean> ge) {
                int rowIndex = ge.getRowIndex();
                if (rowIndex != -1) {
                    if (ge.getColIndex() == 0) {
                        ItemsListPanel.this.isCheckbox = true;
                    } else {
                        ItemsListPanel.this.isCheckbox = false;
                    }

                    ItemBean item = grid.getStore().getAt(rowIndex);
                    grid.getView().getRow(item).getStyle().setCursor(Style.Cursor.POINTER);
                }
            }
        });

        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<ItemBean>() {

            @Override
            public void selectionChanged(final SelectionChangedEvent<ItemBean> se) {
                if (se.getSelectedItem() != null) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        @Override
                        public void execute() {
                            if (isCurrentRecordChange()) {
                                MessageBox msgBox = MessageBox.confirm(MessagesFactory.getMessages().confirm_title(),
                                        MessagesFactory.getMessages().msg_confirm_save_tree_detail(root.getLabel()),
                                        new Listener<MessageBoxEvent>() {

                                            @Override
                                            public void handleEvent(MessageBoxEvent be) {
                                                if (Dialog.YES.equals(be.getButtonClicked().getItemId())) {
                                                    saveCurrentChangeBeforeSwitching = true;
                                                    changedRecordId = toolBar.getItemBean().getIds();
                                                    toolBar.saveItemAndClose(true);
                                                }
                                                selectRow(se);
                                            }
                                        });
                                msgBox.getDialog().setWidth(550);
                            } else {
                                selectRow(se);
                            }

                        }
                    });
                }

            }
        });

        grid.addListener(Events.Attach, new Listener<GridEvent<ItemBean>>() {

            @Override
            public void handleEvent(GridEvent<ItemBean> be) {
                PagingLoadConfig config = new BasePagingLoadConfig();
                config.setOffset(0);
                int pageSize = pagingBar.getPageSize();
                config.setLimit(pageSize);
                loader.load(config);
                pagingBar.setVisible(true);
            }
        });

        grid.setLoadMask(true);
        grid.addPlugin(re);
        grid.addPlugin(sm);

        grid.getAriaSupport().setDescribedBy("abcdefg"); //$NON-NLS-1$
        grid.getAriaSupport().setLabelledBy(this.getHeader().getId() + "-label"); //$NON-NLS-1$

        gridContainer.add(grid);
        gridContainer.setHeight(this.getHeight() - ItemsToolBar.getInstance().getHeight()
                - ItemsToolBar.getInstance().getAdvancedPanel().getHeight());
        hookContextMenu();

        re.addListener(Events.AfterEdit, new Listener<RowEditorEvent>() {

            @Override
            public void handleEvent(RowEditorEvent be) {
                Map<String, Object> changes = be.getChanges();
                Iterator<String> iterator = changes.keySet().iterator();
                Map<String, String> changedField = new HashMap<String, String>(changes.size());

                EntityModel entityModel = (EntityModel) BrowseRecords.getSession().get(UserSession.CURRENT_ENTITY_MODEL);
                final ItemBean itemBean = grid.getSelectionModel().getSelectedItem();
                Map<String, Object> originalMap = itemBean.getOriginalMap();

                while (iterator.hasNext()) {
                    String path = iterator.next();
                    TypeModel tm = entityModel.getMetaDataTypes().get(path);
                    String value;
                    if (tm.getForeignkey() != null) {
                        if (changes.get(path) == null) {
                            value = ""; //$NON-NLS-1$
                        } else {
                            value = changes.get(path).toString();
                        }
                        ForeignKeyBean fkBean = itemBean.getForeignkeyDesc(value);
                        if (fkBean != null) {
                            changedField.put(path, fkBean.getId());
                        } else {
                            changedField.put(path, ""); //$NON-NLS-1$
                        }
                    } else {
                        if (changes.get(path) == null) {
                            continue;
                        }
                        value = changes.get(path).toString();
                        if (originalMap.containsKey(path)) {
                            Object data = originalMap.get(path);
                            if (DataTypeConstants.DATE.getTypeName().equals(tm.getType().getBaseTypeName())) {
                                value = DateUtil.getDate((Date) data);
                            } else if (DataTypeConstants.DATETIME.getTypeName().equals(tm.getType().getBaseTypeName())) {
                                value = DateUtil.getDateTime((Date) data);
                            } else {
                                value = String.valueOf(data);
                            }
                        }
                        changedField.put(path, value != null ? value : ""); //$NON-NLS-1$                        
                    }
                }

                ItemsDetailPanel detailPanel = ItemsMainTabPanel.getInstance().getDefaultViewTabItem();

                Widget widget = detailPanel.getFirstTabWidget();

                ItemNodeModel model = null;
                if (widget instanceof ItemPanel) {// save primary key
                    ItemPanel itemPanel = (ItemPanel) widget;
                    model = itemPanel.getTree().getRootModel();
                } else if (widget instanceof ForeignKeyTreeDetail) { // save foreign key
                    ForeignKeyTreeDetail fkDetail = (ForeignKeyTreeDetail) widget;
                    model = fkDetail.getRootModel();
                }
                ViewBean viewBean = (ViewBean) BrowseRecords.getSession().get(UserSession.CURRENT_VIEW);
                String xml = (new ItemTreeHandler(model, viewBean, ItemTreeHandlingStatus.ToSave)).serializeItem();

                service.updateItem(itemBean.getConcept(), itemBean.getIds(), changedField, xml, entityModel,
                        Locale.getLanguage(), new SessionAwareAsyncCallback<ItemResult>() {

                            @Override
                            protected void doOnFailure(Throwable caught) {
                                Record record;
                                Store<ItemBean> store = grid.getStore();
                                if (store != null) {
                                    record = store.getRecord(itemBean);
                                } else {
                                    record = null;
                                }

                                if (record != null) {
                                    record.reject(false);
                                }

                                String err = caught.getLocalizedMessage();
                                if (err != null) {
                                    MessageBox.alert(MessagesFactory.getMessages().error_title(),
                                            MultilanguageMessageParser.pickOutISOMessage(err), null);
                                } else {
                                    super.doOnFailure(caught);
                                }
                            }

                            @Override
                            public void onSuccess(ItemResult result) {
                                Record record;
                                Store<ItemBean> store = grid.getStore();
                                if (store != null) {
                                    record = store.getRecord(itemBean);
                                } else {
                                    record = null;
                                }

                                if (record != null) {
                                    record.commit(false);
                                }
                                final MessageBox msgBox = new MessageBox();
                                String msg = MultilanguageMessageParser.pickOutISOMessage(result.getDescription());
                                if (result.getStatus() == ItemResult.FAILURE) {
                                    msgBox.setTitle(MessagesFactory.getMessages().error_title());
                                    msgBox.setButtons(MessageBox.OK);
                                    msgBox.setIcon(MessageBox.ERROR);
                                    msgBox.setMessage(msg == null || msg.isEmpty() ? MessagesFactory.getMessages()
                                            .output_report_null() : msg);
                                    msgBox.addCallback(new Listener<MessageBoxEvent>() {

                                        @Override
                                        public void handleEvent(MessageBoxEvent be) {
                                            refreshOnSaveCompleted(itemBean);
                                        }
                                    });
                                    msgBox.show();
                                } else {
                                    msgBox.setTitle(MessagesFactory.getMessages().info_title());
                                    msgBox.setButtons(""); //$NON-NLS-1$
                                    msgBox.setIcon(MessageBox.INFO);
                                    msgBox.setMessage(msg == null || msg.isEmpty() ? MessagesFactory.getMessages().save_success()
                                            : msg);
                                    msgBox.show();
                                    Timer timer = new Timer() {

                                        public void run() {
                                            msgBox.close();
                                            refreshOnSaveCompleted(itemBean);
                                        }
                                    };
                                    timer.schedule(1000);
                                }
                            }
                        });
            }

        });

        add(gridContainer);
        this.syncSize();
        this.doLayout();
    }

    private void refreshOnSaveCompleted(ItemBean itemBean) {
        if (itemBean != null) {
            if (gridUpdateLock) {
                return;
            }
            gridUpdateLock = true;
            refresh(itemBean.getIds(), false);
            AppEvent event = new AppEvent(BrowseRecordsEvents.ViewItem, itemBean);
            event.setData("isStaging", isStaging()); //$NON-NLS-1$
            Dispatcher.forwardEvent(event);
            gridUpdateLock = false;
        }
    }

    private void hookContextMenu() {
        Menu contextMenu = new Menu();
        MenuItem editRow = new MenuItem();
        editRow.setText(MessagesFactory.getMessages().edititem());
        editRow.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Edit()));
        editRow.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                ItemBean m = grid.getSelectionModel().getSelectedItem();
                if (m == null) {
                    MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                            .grid_record_select(), null);
                    return;
                }
                int rowIndex = grid.getStore().indexOf(m);
                re.startEditing(rowIndex, true);
            }
        });

        contextMenu.add(editRow);

        MenuItem openInTab = new MenuItem();
        openInTab.setText(MessagesFactory.getMessages().openitem_tab());
        openInTab.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.openTab()));
        openInTab.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                ItemBean m = grid.getSelectionModel().getSelectedItem();
                if (m == null) {
                    MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                            .grid_record_select(), null);
                    return;
                }
                // TMDM-3202 open in a top-level tab
                ItemBean itemBean = grid.getSelectionModel().getSelectedItem();
                String smartViewMode = itemBean.getSmartViewMode();
                String opt = ItemDetailToolBar.VIEW_OPERATION;
                if (smartViewMode.equals(ItemBean.PERSOMODE)) {
                    opt = ItemDetailToolBar.PERSONALEVIEW_OPERATION;
                } else if (smartViewMode.equals(ItemBean.SMARTMODE)) {
                    opt = ItemDetailToolBar.SMARTVIEW_OPERATION;
                }

                TreeDetailUtil.initItemsDetailPanelById("", m.getIds(), m.getConcept(), false, false, opt, isStaging()); //$NON-NLS-1$
            }
        });
        contextMenu.add(openInTab);

        grid.setContextMenu(contextMenu);

    }

    public void layoutGrid() {
        this.layout(true);
        if (gridContainer != null) {
            Element parent = DOM.getParent(gridContainer.getElement());
            gridContainer.setSize(parent.getOffsetWidth(), parent.getOffsetHeight());
        }
    }

    public void reload(ReLoadData reLoad) {
        this.reLoad = reLoad;
        loader.load();
    }

    public ListStore<ItemBean> getStore() {
        return store;
    }

    public Grid<ItemBean> getGrid() {
        return grid;
    }

    public void setEnabledGridSearchButton(boolean enabled) {
        gridContainer.setEnabled(enabled);
        ItemsToolBar.getInstance().searchButton.setEnabled(enabled);
    }

    public void refreshGrid() {
        if (gridContainer != null) {// refresh when grid is not empty
            if (pagingBar != null && pagingBar.getItemCount() > 0) {
                if (grid.getSelectionModel().getSelectedItem() != null) {
                    if (saveCurrentChangeBeforeSwitching) {
                        refresh(changedRecordId, false);
                    } else {
                        String ids = grid.getSelectionModel().getSelectedItem().getIds();
                        refresh(ids, true);
                        pagingBar.refresh();
                    }

                } else {
                    pagingBar.last();
                }
                return;
            }
        }
        if (ItemsToolBar.getInstance().getSimplePanel() != null
                && ItemsToolBar.getInstance().getSimplePanel().getCriteria() != null) {
            ButtonEvent be = new ButtonEvent(ItemsToolBar.getInstance().searchButton);
            ItemsToolBar.getInstance().searchButton.fireEvent(Events.Select, be);
        }
    }

    public void lastPage() {
        if (pagingBar != null && pagingBar.getItemCount() > 0) {
            pagingBar.last();
        }
    }

    public void resetGrid() {
        if (pagingBar != null) {
            grid.removeFromParent();
            pagingBar.removeAll();
        }
    }

    public void refresh(String ids, final boolean refreshItemForm) {
        if (grid != null) {
            final ListStore<ItemBean> store = grid.getStore();
            final ItemBean itemBean = store.findModel(ids);
            if (itemBean != null) {
                String dataCluster = BrowseRecords.getSession().getAppHeader().getDatacluster();
                EntityModel entityModel = (EntityModel) BrowseRecords.getSession().get(UserSession.CURRENT_ENTITY_MODEL);
                ViewBean viewbean = (ViewBean) BrowseRecords.getSession().get(UserSession.CURRENT_VIEW);
                service.queryItemBeanById(dataCluster, viewbean, entityModel, ids, Locale.getLanguage(),
                        new SessionAwareAsyncCallback<ItemBean>() {

                            @Override
                            public void onSuccess(ItemBean result) {
                                if (result == null) {
                                    ItemsMainTabPanel.getInstance().removeAll();
                                    if (store.getCount() > 1) {
                                        pagingBar.refresh();
                                    } else {
                                        pagingBar.last();
                                    }
                                    return;
                                }
                                Record record = store.getRecord(itemBean);
                                itemBean.copy(result);
                                Map<String, String> formateMap = result.getFormateMap();
                                Set<String> fomateKeySet = formateMap.keySet();
                                Iterator<String> fomatekeyIt = fomateKeySet.iterator();

                                while (fomatekeyIt.hasNext()) {
                                    String key = fomatekeyIt.next();
                                    record.set(key, formateMap.get(key));

                                }
                                record.commit(false);

                                // TMDM-3349 button 'save and close' function
                                if (!defaultSelectionModel) {
                                    if (saveCurrentChangeBeforeSwitching) {
                                        saveCurrentChangeBeforeSwitching = false;
                                        defaultSelectionModel = true;
                                        changedRecordId = null;
                                    } else {
                                        grid.getSelectionModel().select(itemBean, false);
                                    }
                                    return;
                                }

                                if (refreshItemForm) {
                                    AppEvent event = new AppEvent(BrowseRecordsEvents.ViewItem, itemBean);
                                    event.setData("isStaging", isStaging()); //$NON-NLS-1$
                                    Dispatcher.forwardEvent(event);
                                }
                            }
                        });
            } else {
                pagingBar.first();
            }
        } else {
            ButtonEvent be = new ButtonEvent(ItemsToolBar.getInstance().searchButton);
            ItemsToolBar.getInstance().searchButton.fireEvent(Events.Select, be);
        }
    }

    private boolean isCurrentRecordChange() {
        TabItem tabItem = ItemsMainTabPanel.getInstance().getItemByItemId(BrowseRecordsView.DEFAULT_ITEMVIEW);
        if (tabItem != null) {
            ItemsDetailPanel itemsDetailPanel = (ItemsDetailPanel) tabItem.getItemByItemId(BrowseRecordsView.DEFAULT_ITEMVIEW);
            if (itemsDetailPanel != null && itemsDetailPanel.getFirstTabWidget() != null) {
                Widget widget = itemsDetailPanel.getFirstTabWidget();
                if (widget instanceof ForeignKeyTreeDetail) {
                    final ForeignKeyTreeDetail fkTree = (ForeignKeyTreeDetail) widget;
                    toolBar = fkTree.getToolBar();
                    root = fkTree.getRootModel();
                } else {
                    ItemPanel itemPanel = (ItemPanel) widget;
                    toolBar = itemPanel.getToolBar();
                    root = itemPanel.getTree().getRootModel();
                }
                return root != null ? TreeDetailUtil.isChangeValue(root) : false;
            }

        }
        return false;
    }

    private void selectRow(SelectionChangedEvent<ItemBean> se) {
        ItemBean item = se.getSelectedItem();

        // TMDM-3349 button 'save and close' function
        if (!defaultSelectionModel) {
            defaultSelectionModel = true;
            if (ItemsMainTabPanel.getInstance().getSelectedItem() != null) {
                String ids = ItemsMainTabPanel.getInstance().getDefaultViewTabItem().getCurrentItemPanel().getItem().getIds();
                ItemBean selectedItem = grid.getStore().findModel(ids);
                if (item != null && item != selectedItem) {
                    grid.getSelectionModel().deselect(se.getSelectedItem());
                    grid.getSelectionModel().select(selectedItem, true);
                    return;
                }
            } else {
                grid.getSelectionModel().select(-1, false);
                return;
            }
        }

        if (item != null) {
            if (gridUpdateLock) {
                return;
            }

            if (ItemsMainTabPanel.getInstance().getDefaultViewTabItem() != null) {
                if (ItemsListPanel.this.isCheckbox) {
                    return;
                }
            }

            gridUpdateLock = true;
            BrowseRecords.getSession().put(UserSession.CURRENT_CACHED_ENTITY, null);
            BrowseRecords.getSession().put(UserSession.CURRENT_CACHED_FKTABS, null);
            AppEvent event = new AppEvent(BrowseRecordsEvents.ViewItem, item);
            event.setData("isStaging", isStaging()); //$NON-NLS-1$
            Dispatcher.forwardEvent(event);
            gridUpdateLock = false;
        }
    }

    public boolean isDefaultSelectionModel() {
        return defaultSelectionModel;
    }

    public void setDefaultSelectionModel(boolean defaultSelectionModel) {
        this.defaultSelectionModel = defaultSelectionModel;
    }

    public boolean isSaveCurrentChangeBeforeSwitching() {
        return saveCurrentChangeBeforeSwitching;
    }

    public void setSaveCurrentChangeBeforeSwitching(boolean saveCurrentChangeBeforeSwitching) {
        this.saveCurrentChangeBeforeSwitching = saveCurrentChangeBeforeSwitching;
    }

    public String getChangedRecordId() {
        return changedRecordId;
    }

    public void setChangedRecordId(String changedRecordId) {
        this.changedRecordId = changedRecordId;
    }

    public void deSelectCurrentItem() {
        if (grid != null && grid.getSelectionModel() != null) {
            grid.getSelectionModel().deselect(grid.getSelectionModel().getSelectedItem());
        }
    }

    public void initSpecialVariable() {
        if (saveCurrentChangeBeforeSwitching) {
            saveCurrentChangeBeforeSwitching = false;
            changedRecordId = null;
        }
    }

    public void refreshGrid(final ItemBean itemBean) {
        if (gridContainer != null) {// refresh when grid is not empty
            if (pagingBar != null && pagingBar.getItemCount() > 0) {
                if (grid.getSelectionModel().getSelectedItem() != null) {
                    if (saveCurrentChangeBeforeSwitching) {
                        refresh(changedRecordId, false);
                    } else {
                        String ids = grid.getSelectionModel().getSelectedItem().getIds();
                        refresh(ids, true);
                    }
                } else {
                    if (itemBean != null) {
                        String ids[] = { itemBean.getIds() };
                        service.getItemBeanById(itemBean.getConcept(), ids, Locale.getLanguage(), new AsyncCallback<ItemBean>() {

                            @Override
                            public void onSuccess(ItemBean result) {
                                if (!"NONE".equals(pagingLoadConfig.getSortField())) { //$NON-NLS-1$
                                    createItemBean = result;
                                    pagingBar.refresh();
                                } else {
                                    createItemBean = result;
                                    pagingBar.lastAfterCreate();
                                }
                            }

                            @Override
                            public void onFailure(Throwable exception) {
                                pagingBar.last();
                            }
                        });
                    }
                }
                return;
            }
        }
        if (ItemsToolBar.getInstance().getSimplePanel() != null
                && ItemsToolBar.getInstance().getSimplePanel().getCriteria() != null) {
            ButtonEvent be = new ButtonEvent(ItemsToolBar.getInstance().searchButton);
            ItemsToolBar.getInstance().searchButton.fireEvent(Events.Select, be);
        }
    }

    protected Boolean isStaging() {
        return false;
    }
}