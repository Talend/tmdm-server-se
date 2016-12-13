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
import org.talend.mdm.webapp.base.client.model.Criteria;
import org.talend.mdm.webapp.base.client.model.DataType;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.client.model.MultipleCriteria;
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
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.ServiceFactory;
import org.talend.mdm.webapp.browserecords.client.creator.CellEditorCreator;
import org.talend.mdm.webapp.browserecords.client.creator.CellRendererCreator;
import org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.OperatorConstants;
import org.talend.mdm.webapp.browserecords.client.model.QueryModel;
import org.talend.mdm.webapp.browserecords.client.model.RecordStatus;
import org.talend.mdm.webapp.browserecords.client.model.RecordsPagingConfig;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.util.ViewUtil;
import org.talend.mdm.webapp.browserecords.client.widget.filter.BooleanFilter;
import org.talend.mdm.webapp.browserecords.client.widget.filter.NumericFilter;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.creator.FieldCreator;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

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
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.filters.DateFilter;
import com.extjs.gxt.ui.client.widget.grid.filters.Filter;
import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
import com.extjs.gxt.ui.client.widget.grid.filters.StringFilter;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class BulkUpdateListPanel extends ContentPanel {

    private Map<Integer, String> errorTitles;

    private QueryModel currentQueryModel;

    private boolean isPagingAccurate;

    private String[] ids;

    private String cluster;

    private ViewBean viewBean;

    private EntityModel entityModel;

    private GridFilters filters;

    private ContentPanel gridContainer;

    private ToolBar openTaskToolBar;

    private static BulkUpdateListPanel instance;

    private BrowseRecordsServiceAsync browseRecordService = ServiceFactory.getInstance().getMasterService();

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
                Parser.parse(qm.getCriteria());
            } catch (ParserException e) {
                MessageBox.alert(MessagesFactory.getMessages().error_title(), e.getMessage(), null);
                callback.onSuccess(new BasePagingLoadResult<ItemBean>(new ArrayList<ItemBean>(), 0, 0));
                return;
            }

            browseRecordService.queryItemBeans(qm, Locale.getLanguage(),
                    new SessionAwareAsyncCallback<ItemBasePageLoadResult<ItemBean>>() {

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

    public static BulkUpdateListPanel getInstance() {
        if (instance == null) {
            instance = new BulkUpdateListPanel();
        }
        return instance;
    }

    private BulkUpdateListPanel() {
        this.cluster = BrowseRecords.getSession().getAppHeader().getDatacluster();
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

    public void initPanel(String[] ids) {
        this.ids = ids;
        refresh();
    }

    public void refresh() {
        selectBulkUpdateGridPanel();
        PagingLoadConfig config = new BaseFilterPagingLoadConfig();
        config.setOffset(0);
        int pageSize = pagingBar.getPageSize();
        config.setLimit(pageSize);
        loader.load(config);
        pagingBar.setVisible(true);
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
        return columnConfigList;
    }

    private ContentPanel generateGrid() {
        gridContainer = new ContentPanel(new FitLayout());

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
        String[] keys = entityModel.getKeys();
        Criteria criteria;
        if (keys.length == 1) {
            if (ids.length == 1) {
                criteria = new SimpleCriterion(keys[0], OperatorConstants.EQUALS_OPERATOR, ids[0]);
            } else {
                MultipleCriteria multipleCriteria = new MultipleCriteria();
                multipleCriteria.setOperator(OperatorConstants.OR_OPERATOR);
                for (int i = 0; i < ids.length; i++) {
                    multipleCriteria.getChildren().add(new SimpleCriterion(keys[0], OperatorConstants.EQUALS_OPERATOR, ids[i]));
                }
                criteria = multipleCriteria;
            }
        } else {
            criteria = new SimpleCriterion(keys[0], OperatorConstants.EQUALS_OPERATOR, ids[0]);
        }
        QueryModel queryModel = new QueryModel();
        queryModel.setDataClusterPK(cluster);
        queryModel.setView(viewBean);
        queryModel.setModel(entityModel);
        queryModel.setCriteria(criteria.toString());
        return queryModel;
    }

    private Filter createFilter(String xpath, DataType type) {
        Filter filter;
        if (DataTypeConstants.BOOLEAN.equals(type) || DataTypeConstants.HEXBINARY.equals(type)
                || DataTypeConstants.BASE64BINARY.equals(type)) {
            filter = new BooleanFilter(xpath);
        } else if (DataTypeConstants.DURATION.equals(type) || DataTypeConstants.DATETIME.equals(type)
                || DataTypeConstants.TIME.equals(type) || DataTypeConstants.DATE.equals(type)
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
                }
            });
        }
    }

    private native void selectBulkUpdateGridPanel()/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		var panel = tabPanel.getItem("Bulk Update");
		if (panel != undefined) {
			tabPanel.setSelection(panel.getItemId());
		}
    }-*/;
}
