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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.exception.ParserException;
import org.talend.mdm.webapp.base.client.model.DataType;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.client.model.SimpleCriterion;
import org.talend.mdm.webapp.base.client.model.UserContextModel;
import org.talend.mdm.webapp.base.client.util.Parser;
import org.talend.mdm.webapp.base.client.util.UserContextUtil;
import org.talend.mdm.webapp.base.client.widget.ColumnAlignGrid;
import org.talend.mdm.webapp.base.client.widget.PagingToolBarEx;
import org.talend.mdm.webapp.base.shared.Constants;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsEvents;
import org.talend.mdm.webapp.browserecords.client.BrowseStagingRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.ServiceFactory;
import org.talend.mdm.webapp.browserecords.client.creator.CellEditorCreator;
import org.talend.mdm.webapp.browserecords.client.creator.CellRendererCreator;
import org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.OperatorConstants;
import org.talend.mdm.webapp.browserecords.client.model.QueryModel;
import org.talend.mdm.webapp.browserecords.client.model.RecordStatus;
import org.talend.mdm.webapp.browserecords.client.model.RecordStatusWrapper;
import org.talend.mdm.webapp.browserecords.client.model.RecordsPagingConfig;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.util.StagingConstant;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.util.ViewUtil;
import org.talend.mdm.webapp.browserecords.client.widget.filter.BooleanFilter;
import org.talend.mdm.webapp.browserecords.client.widget.filter.NumericFilter;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.creator.FieldCreator;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.task.StagingConstants;
import com.extjs.gxt.ui.client.Style.HideMode;
import com.extjs.gxt.ui.client.data.BaseFilterPagingLoadConfig;
import com.extjs.gxt.ui.client.data.BaseModelData;
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
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.filters.DateFilter;
import com.extjs.gxt.ui.client.widget.grid.filters.Filter;
import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
import com.extjs.gxt.ui.client.widget.grid.filters.ListFilter;
import com.extjs.gxt.ui.client.widget.grid.filters.StringFilter;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;

public class LineageListPanel extends ContentPanel {

    private Map<Integer, String> errorTitles;

    private QueryModel currentQueryModel;

    private boolean isPagingAccurate;

    private String taskId;

    private String cluster;

    private ViewBean viewBean;

    private EntityModel entityModel;

    private GridFilters filters;

    private ContentPanel gridContainer;

    private ToolBar openTaskToolBar;

    private static LineageListPanel instance;

    private BrowseStagingRecordsServiceAsync browseStagingRecordService = ServiceFactory.getInstance().getStagingService();

    RpcProxy<PagingLoadResult<ItemBean>> proxy = new RpcProxy<PagingLoadResult<ItemBean>>() {

        @Override
        public void load(Object loadConfig, final AsyncCallback<PagingLoadResult<ItemBean>> callback) {
            final QueryModel qm = generateQueryModel();
            qm.setPagingLoadConfig(copyPgLoad((PagingLoadConfig) loadConfig));
            int pageSize = pagingBar.getPageSize();
            qm.getPagingLoadConfig().setLimit(pageSize);
            qm.setLanguage(Locale.getLanguage());

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

            browseStagingRecordService.queryItemBeans(qm, Locale.getLanguage(),
                    new SessionAwareAsyncCallback<ItemBasePageLoadResult<ItemBean>>() {

                        @Override
                        public void onSuccess(ItemBasePageLoadResult<ItemBean> result) {
                            List<ItemBean> data = result.getData();
                            List<ItemBean> sortedData = new ArrayList();
                            sortedData.add(null);
                            for (int i = 0; i < data.size(); i++) {
                                ItemBean itemBean = data.get(i);
                                Object statusObject = itemBean.get(itemBean.getConcept() + StagingConstant.STAGING_STATUS);
                                if (StagingConstants.SUCCESS_VALIDATE.equals(statusObject)
                                        || StagingConstants.SUCCESS_MERGED_RECORD.equals(statusObject)) {
                                    sortedData.set(0, itemBean);
                                } else {
                                    sortedData.add(itemBean);
                                }
                            }
                            isPagingAccurate = result.isPagingAccurate();
                            callback.onSuccess(new BasePagingLoadResult<ItemBean>(sortedData, result.getOffset(), result
                                    .getTotalLength()));
                            if (result.getTotalLength() == 0) {
                                LineagePanel.getInstance().clearDetailPanel();
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

    public static LineageListPanel getInstance() {
        if (instance == null) {
            instance = new LineageListPanel();
        }
        return instance;
    }

    private LineageListPanel() {
        this.cluster = BrowseRecords.getSession().getAppHeader().getDatacluster();
        this.cluster = this.cluster.endsWith(StorageAdmin.STAGING_SUFFIX) ? this.cluster : this.cluster
                + StorageAdmin.STAGING_SUFFIX;
        this.viewBean = BrowseRecords.getSession().getCurrentView();
        this.entityModel = BrowseRecords.getSession().getCurrentEntityModel();

        setLayout(new FitLayout());
        setHeaderVisible(false);

        initErrorTitles();
        ContentPanel gridPanel = generateGrid();
        add(gridPanel);
        layout();
    }

    private void initErrorTitles() {
        UserContextModel ucx = UserContextUtil.getUserContext();
        BrowseRecordsMessages messages = MessagesFactory.getMessages();
        errorTitles = new HashMap<Integer, String>();
        errorTitles.put(RecordStatus.NEW.getStatusCode(), messages.status_000());
        errorTitles.put(RecordStatus.SUCCESS_IDENTIFIED_CLUSTERS.getStatusCode(), messages.status_201(ucx.getDataContainer()));
        errorTitles.put(RecordStatus.SUCCESS_MERGE_CLUSTERS.getStatusCode(), messages.status_202(ucx.getDataContainer()));
        errorTitles.put(RecordStatus.SUCCESS_MERGE_CLUSTER_TO_RESOLVE.getStatusCode(),
                messages.status_203(ucx.getDataContainer()));
        errorTitles.put(RecordStatus.SUCCESS_MERGED_RECORD.getStatusCode(), messages.status_204(ucx.getDataContainer()));
        errorTitles.put(RecordStatus.SUCCESS_VALIDATE.getStatusCode(), messages.status_205());
        errorTitles.put(RecordStatus.SUCCESS_DELETED.getStatusCode(), messages.status_206());
        errorTitles.put(RecordStatus.FAIL_IDENTIFIED_CLUSTERS.getStatusCode(), messages.status_401(ucx.getDataContainer()));
        errorTitles.put(RecordStatus.FAIL_MERGE_CLUSTERS.getStatusCode(), messages.status_402(ucx.getDataContainer()));
        errorTitles.put(RecordStatus.FAIL_VALIDATE_VALIDATION.getStatusCode(), messages.status_403());
        errorTitles.put(RecordStatus.FAIL_VALIDATE_CONSTRAINTS.getStatusCode(), messages.status_404());
        errorTitles.put(RecordStatus.FAIL_DELETE_CONSTRAINTS.getStatusCode(), messages.status_405());
    }

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

    private ContentPanel panel;

    private PagingToolBarEx pagingBar = null;

    public void initPanel(String stagingTaskId) {
        this.taskId = stagingTaskId;
        refresh();
    }

    public void refresh() {
        selectStagingGridPanel();
        browseStagingRecordService.checkTask(cluster, entityModel.getConceptName(), taskId,
                new SessionAwareAsyncCallback<Integer>() {

                    @Override
                    public void onSuccess(Integer result) {
                        int gridContainerHeight = LineageListPanel.this.getHeight();
                        if (result > 1) {
                            openTaskToolBar.setVisible(true);
                        } else {
                            openTaskToolBar.setVisible(false);
                            gridContainerHeight = gridContainerHeight - 1;
                        }
                        PagingLoadConfig config = new BaseFilterPagingLoadConfig();
                        config.setOffset(0);
                        int pageSize = pagingBar.getPageSize();
                        config.setLimit(pageSize);
                        loader.load(config);
                        pagingBar.setVisible(true);
                        gridContainer.setHeight(gridContainerHeight);
                    }
                });
    }

    private List<ColumnConfig> generateColumnList() {
        List<ColumnConfig> columnConfigList = new ArrayList<ColumnConfig>();
        filters = new GridFilters();
        filters.setLocal(true);
        List<String> viewableXpaths = viewBean.getViewableXpaths();
        Map<String, TypeModel> dataTypes = entityModel.getMetaDataTypes();
        List<String> keys = Arrays.asList(entityModel.getKeys());
        for (String xpath : viewableXpaths) {
            TypeModel typeModel = dataTypes.get(xpath);

            ColumnConfig cc = new ColumnConfig(xpath, typeModel == null ? xpath : ViewUtil.getViewableLabel(Locale.getLanguage(),
                    typeModel), 200);
            if (typeModel instanceof SimpleTypeModel && !keys.contains(xpath) && !typeModel.isMultiOccurrence()) {
                Field<?> field = FieldCreator.createField((SimpleTypeModel) typeModel, null, false, Locale.getLanguage());

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
                columnConfigList.add(cc);
            }

            filters.addFilter(createFilter(xpath, typeModel != null ? typeModel.getType() : DataTypeConstants.STRING));
        }

        String taskIdPath = entityModel.getConceptName() + StagingConstant.STAGING_TASKID;
        ColumnConfig groupColumn = new ColumnConfig(taskIdPath, MessagesFactory.getMessages().match_group(), 200);
        columnConfigList.add(groupColumn);
        filters.addFilter(createFilter(taskIdPath, DataTypeConstants.STRING));

        String statusPath = entityModel.getConceptName() + StagingConstant.STAGING_STATUS;
        ColumnConfig statusColumn = new ColumnConfig(statusPath, MessagesFactory.getMessages().status(), 200);
        statusColumn.setRenderer(new GridCellRenderer<ItemBean>() {

            @Override
            public Object render(ItemBean model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<ItemBean> statusListStore, Grid<ItemBean> statusGrid) {
                com.google.gwt.user.client.ui.Grid g = new com.google.gwt.user.client.ui.Grid(1, 2);
                g.setCellPadding(0);
                g.setCellSpacing(0);
                String status = model.get(entityModel.getConceptName() + StagingConstant.STAGING_STATUS);

                if (status == null || status.trim().length() == 0) {
                    status = "0"; //$NON-NLS-1$
                }
                RecordStatusWrapper wrapper = new RecordStatusWrapper(RecordStatus.newStatus(Integer.valueOf(status)));

                String color = wrapper.getColor();

                if (color != null) {
                    g.getElement().getStyle().setColor(color);
                }
                if (wrapper.getIcon() != null) {
                    g.setWidget(0, 0, new Image(wrapper.getIcon()));
                }
                g.setText(0, 1, status);
                g.setTitle(status
                        + ": " + (errorTitles.get(Integer.valueOf(status)) == null ? "" : errorTitles.get(Integer.valueOf(status)))); //$NON-NLS-1$ //$NON-NLS-2$
                return g;
            }
        });
        columnConfigList.add(statusColumn);

        ListStore<ModelData> statusListFilterStore = new ListStore<ModelData>();
        for (RecordStatus recordStatus : RecordStatus.values()) {
            if (recordStatus != RecordStatus.UNKNOWN) { // UNKNOWN state exists only in web UI.
                statusListFilterStore.add(buildFilterItem(statusPath, String.valueOf(recordStatus.getStatusCode())));
            }
        }
        ListFilter statusFilter = new ListFilter(statusPath, statusListFilterStore);
        statusFilter.setDisplayProperty(statusPath);
        filters.addFilter(statusFilter);

        String sourcePath = entityModel.getConceptName() + StagingConstant.STAGING_SOURCE;
        ColumnConfig sourceColumn = new ColumnConfig(sourcePath, MessagesFactory.getMessages().source(), 200);
        columnConfigList.add(sourceColumn);

        ListStore<ModelData> sourceListFilterStore = new ListStore<ModelData>();
        sourceListFilterStore.add(buildFilterItem(sourcePath, StagingConstants.STAGING_MDM_SOURCE));
        ListFilter sourceFilter = new ListFilter(sourcePath, sourceListFilterStore);
        sourceFilter.setDisplayProperty(sourcePath);
        filters.addFilter(sourceFilter);

        for (ColumnConfig cc : columnConfigList) {
            final GridCellRenderer<ModelData> render = cc.getRenderer();

            GridCellRenderer<ModelData> renderProxy = new GridCellRenderer<ModelData>() {

                @Override
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                        ListStore<ModelData> listStore, Grid<ModelData> g) {
                    Object value = null;
                    if (render != null) {
                        value = render.render(model, property, config, rowIndex, colIndex, listStore, g);
                    } else {
                        value = model.get(property);
                    }
                    if (value instanceof String) {
                        ItemBean item = (ItemBean) model;
                        String matchGroup = model.get(item.getConcept() + StagingConstant.STAGING_TASKID);
                        if (matchGroup != null && matchGroup.length() != 0) {
                            String status = model.get(item.getConcept() + StagingConstant.STAGING_STATUS);
                            if ("205".equals(status) || "203".equals(status) || "204".equals(status)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                return "<b>" + value + "</b>"; //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                    return value;
                }
            };
            cc.setRenderer(renderProxy);
        }
        return columnConfigList;
    }

    private ContentPanel generateGrid() {
        gridContainer = new ContentPanel(new FitLayout());

        Button taskButton = new Button();
        taskButton = new Button(MessagesFactory.getMessages().open_task());
        taskButton.setId("openTaskButton"); //$NON-NLS-1$
        taskButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.openTask()));

        taskButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                initDSC(LineageListPanel.this.taskId);
            }
        });
        openTaskToolBar = new ToolBar();
        openTaskToolBar.add(taskButton);
        gridContainer.setTopComponent(openTaskToolBar);

        ColumnModel cm = new ColumnModel(generateColumnList());
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
                    grid.getSelectionModel().select(0, false);
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

        {
            grid.setStateful(true);
            AppHeader header = (AppHeader) BrowseRecords.getSession().get(UserSession.APP_HEADER);
            ViewBean vb = (ViewBean) BrowseRecords.getSession().get(UserSession.CURRENT_VIEW);
            grid.setStateId(header.getDatamodel() + "." + entityModel.getConceptName() + "." + vb.getViewPK()); //$NON-NLS-1$//$NON-NLS-2$
        }

        grid.getView().setForceFit(true);
        if (cm.getColumnCount() > 0) {
            grid.setAutoExpandColumn(cm.getColumn(0).getHeader());
        }

        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<ItemBean>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<ItemBean> se) {
                selectRow(se.getSelectedItem());
            }
        });

        grid.setLoadMask(true);

        grid.getAriaSupport().setDescribedBy("abcdefg"); //$NON-NLS-1$
        grid.getAriaSupport().setLabelledBy(this.getHeader().getId() + "-label"); //$NON-NLS-1$

        grid.addPlugin(filters);
        gridContainer.add(grid);

        return gridContainer;
    }

    private QueryModel generateQueryModel() {
        SimpleCriterion criterion = new SimpleCriterion(StagingConstant.STAGING_TASKID, OperatorConstants.EQUALS_OPERATOR, taskId);
        QueryModel queryModel = new QueryModel();
        queryModel.setDataClusterPK(cluster);
        queryModel.setView(viewBean);
        queryModel.setModel(entityModel);
        queryModel.setCriteria(criterion.toString());
        return queryModel;
    }

    private Filter createFilter(String xpath, DataType type) {
        Filter filter;
        if (DataTypeConstants.BOOLEAN.equals(type) || DataTypeConstants.HEXBINARY.equals(type)
                || DataTypeConstants.BASE64BINARY.equals(type)) {
            filter = new BooleanFilter(xpath);
        } else if (DataTypeConstants.DURATION.equals(type) || DataTypeConstants.DATETIME.equals(type)
                || DataTypeConstants.TIME.equals(type) || DataTypeConstants.DATE.equals(type)
                || DataTypeConstants.TIME.getTypeName().equals(type.getBaseTypeName())
                || DataTypeConstants.DATE.getTypeName().equals(type.getBaseTypeName())
                || DataTypeConstants.DATETIME.getTypeName().equals(type.getBaseTypeName())
                || DataTypeConstants.GYEARMONTH.equals(type) || DataTypeConstants.GYEAR.equals(type)
                || DataTypeConstants.GDAY.equals(type) || DataTypeConstants.GMONTH.equals(type)) {
            filter = new DateFilter(xpath);
        } else if (DataTypeConstants.INTEGER.equals(type) || DataTypeConstants.NONPOSITIVEINTEGER.equals(type)
                || DataTypeConstants.NEGATIVEINTEGER.equals(type) || DataTypeConstants.NONNEGATIVEINTEGER.equals(type)
                || DataTypeConstants.LONG.equals(type) || DataTypeConstants.INT.equals(type)
                || DataTypeConstants.SHORT.equals(type) || DataTypeConstants.UNSIGNEDLONG.equals(type)
                || DataTypeConstants.UNSIGNEDINT.equals(type) || DataTypeConstants.UNSIGNEDSHORT.equals(type)
                || DataTypeConstants.POSITIVEINTEGER.equals(type) || DataTypeConstants.BYTE.equals(type)
                || DataTypeConstants.UNSIGNEDBYTE.equals(type) || DataTypeConstants.DECIMAL.equals(type)
                || DataTypeConstants.FLOAT.equals(type) || DataTypeConstants.DOUBLE.equals(type)) {
            filter = new NumericFilter(xpath);
        } else {
            filter = new StringFilter(xpath);
        }
        return filter;
    }

    private ModelData buildFilterItem(String xpath, String value) {
        ModelData model = new BaseModelData();
        model.set(xpath, value);
        return model;
    }

    private void selectRow(final ItemBean item) {
        if (item != null) {
            DeferredCommand.addCommand(new Command() {

                @Override
                public void execute() {
                    AppEvent event = new AppEvent(BrowseRecordsEvents.ViewLineageItem, item);
                    event.setData(BrowseRecords.ENTITY_MODEL, entityModel);
                    event.setData(BrowseRecords.VIEW_BEAN, viewBean);
                    Dispatcher.forwardEvent(event);
                }
            });
        }
    }

    private native void selectStagingGridPanel()/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		var panel = tabPanel.getItem("Staging Data Viewer");
		if (panel != undefined) {
			tabPanel.setSelection(panel.getItemId());
		}
    }-*/;

    private native boolean initDSC(String taskId)/*-{
		$wnd.amalto.datastewardship.Datastewardship.taskItem(taskId);
		return true;
    }-*/;
}
