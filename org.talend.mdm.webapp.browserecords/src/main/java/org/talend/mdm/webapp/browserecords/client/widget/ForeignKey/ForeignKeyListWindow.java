/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.ForeignKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.client.model.MultiLanguageModel;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.client.widget.PagingToolBarEx;
import org.talend.mdm.webapp.base.shared.Constants;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyDrawer;
import org.talend.mdm.webapp.browserecords.client.model.Restriction;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.util.ViewUtil;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.ComboBoxField;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class ForeignKeyListWindow extends Window {

    private String foreignKeyPath;

    private List<String> foreignKeyInfo;

    private String foreignKeyFilterValue;

    private static final int COLUMN_WIDTH = 100;

    private static final int WINDOW_WIDTH = 530;

    private static final int WINDOW_HEIGH = 310;

    private Grid<ForeignKeyBean> grid;

    private ReturnCriteriaFK returnCriteriaFK;

    private ListStore<BaseModel> typeList = new ListStore<BaseModel>();

    private BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);

    private PagingLoader<PagingLoadResult<ModelData>> loader;

    private String previousFilterText;

    private TextField<String> filter = new TextField<String>();

    private ComboBoxField<BaseModel> typeComboBox;

    private EntityModel entityModel;

    private String dataCluster;

    private ForeignKeyField sourceField;

    private PagingToolBarEx pageToolBar;

    private static final int MARGIN_WIDTH = 20;

    private static final int MARGIN_HEIGHT = 40;

    public ForeignKeyListWindow(String foreignKeyPath, List<String> foreignKeyInfo, String dataCluster, EntityModel entityModel,
            ForeignKeyField foreignKeyField) {
        this.foreignKeyPath = foreignKeyPath;
        this.foreignKeyInfo = foreignKeyInfo;
        this.dataCluster = dataCluster;
        this.entityModel = entityModel;
        this.sourceField = foreignKeyField;
    }

    public void setForeignKeyFilterValue(String foreignKeyFilterValue) {
        this.foreignKeyFilterValue = foreignKeyFilterValue;
    }

    private String getFilterValue() {
        String value = filter.getRawValue();
        if (value == null || value.trim().length() == 0) {
            value = ".*"; //$NON-NLS-1$
        } else {
            value = "'" + filter.getRawValue() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return value;
    }

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
        final PagingLoadConfig config = new BasePagingLoadConfig();
        final TypeModel typeModel = sourceField.getDataType();
        final String originalForeignkey = typeModel.getForeignkey();
        RpcProxy<PagingLoadResult<ForeignKeyBean>> proxy = new RpcProxy<PagingLoadResult<ForeignKeyBean>>() {

            @Override
            public void load(final Object loadConfig, final AsyncCallback<PagingLoadResult<ForeignKeyBean>> callback) {
                PagingLoadConfig config = (PagingLoadConfig) loadConfig;
                if (pageToolBar != null) {
                    config.setLimit(pageToolBar.getPageSize());
                }
                BasePagingLoadConfigImpl baseConfig = BasePagingLoadConfigImpl.copyPagingLoad(config);
                final String currentFilterText = getFilterValue();

                baseConfig.set("language", Locale.getLanguage()); //$NON-NLS-1$

                typeModel.setForeignkey(foreignKeyPath);
                typeModel.setFilterValue(currentFilterText);
                service.getForeignKeyList(baseConfig, typeModel,
                        org.talend.mdm.webapp.base.shared.util.CommonUtil.unescapeXml(foreignKeyFilterValue), dataCluster,
                        Locale.getLanguage(), new SessionAwareAsyncCallback<ItemBasePageLoadResult<ForeignKeyBean>>() {

                            @Override
                            protected void doOnFailure(Throwable caught) {
                                String err = caught.getMessage();
                                if (err != null) {
                                    MessageBox.alert(MessagesFactory.getMessages().error_title(),
                                            MultilanguageMessageParser.pickOutISOMessage(err), null);
                                    callback.onSuccess(new BasePagingLoadResult<ForeignKeyBean>(new ArrayList<ForeignKeyBean>(),
                                            0, 0));
                                } else {
                                    callback.onFailure(caught);
                                }
                                typeModel.setForeignkey(originalForeignkey);
                            }

                            @Override
                            public void onSuccess(ItemBasePageLoadResult<ForeignKeyBean> result) {
                                if (currentFilterText.equals(getFilterValue())) {
                                    callback.onSuccess(new BasePagingLoadResult<ForeignKeyBean>(result.getData(), result
                                            .getOffset(), result.getTotalLength()));
                                }
                                typeModel.setForeignkey(originalForeignkey);
                            }

                        });
            }
        };

        RpcProxy<BaseListLoadResult<BaseModel>> proxy1 = new RpcProxy<BaseListLoadResult<BaseModel>>() {

            @Override
            public void load(final Object loadConfig, final AsyncCallback<BaseListLoadResult<BaseModel>> callback) {
                service.getForeignKeyPolymTypeList(ForeignKeyListWindow.this.foreignKeyPath, Locale.getLanguage(),
                        new SessionAwareAsyncCallback<List<Restriction>>() {

                            @Override
                            protected void doOnFailure(Throwable caught) {
                                callback.onFailure(caught);
                            }

                            @Override
                            public void onSuccess(List<Restriction> result) {
                                List<BaseModel> list = new ArrayList<BaseModel>();
                                for (Restriction re : result) {
                                    BaseModel model = new BaseModel();
                                    model.set("name", re.getName());//$NON-NLS-1$
                                    model.set("value", re.getValue());//$NON-NLS-1$
                                    list.add(model);
                                }
                                callback.onSuccess(new BaseListLoadResult<BaseModel>(list));
                            }
                        });
            }
        };
        BaseListLoader<ListLoadResult<Restriction>> loader1 = new BaseListLoader<ListLoadResult<Restriction>>(proxy1);
        typeList = new ListStore<BaseModel>(loader1);
        loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
        loader.setRemoteSort(true);
        final ListStore<ForeignKeyBean> store = new ListStore<ForeignKeyBean>(loader);

        int usePageSize = Constants.PAGE_SIZE;
        if (Cookies.getCookie(PagingToolBarEx.BROWSERECORD_FOREIGNKEY_PAGESIZE) != null) {
            usePageSize = Integer.parseInt(Cookies.getCookie(PagingToolBarEx.BROWSERECORD_FOREIGNKEY_PAGESIZE));
        }
        pageToolBar = new PagingToolBarEx(usePageSize, PagingToolBarEx.BROWSERECORD_FOREIGNKEY_PAGESIZE) {

            @Override
            protected void onLoad(LoadEvent event) {
                String of_word = MessagesFactory.getMessages().of_word();
                msgs.setDisplayMsg("{0} - {1} " + of_word + " " + "{2}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
                super.onLoad(event);
            }
        };
        pageToolBar.setBrowseRecordsGridCall(true);
        pageToolBar.bind(loader);
        pageToolBar.setEnabled(true);

        FormPanel panel = new FormPanel();
        panel.setFrame(false);
        panel.setLayout(new FitLayout());
        panel.setHeaderVisible(false);
        panel.setSize(WINDOW_WIDTH, WINDOW_HEIGH);
        panel.setHeaderVisible(false);

        filter.addListener(Events.KeyUp, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_UP) {
                    ForeignKeyBean fkBean = grid.getSelectionModel().getSelectedItem();
                    if (fkBean == null) {
                        grid.getSelectionModel().select(store.getCount() - 1, true);
                    } else {
                        grid.getSelectionModel().selectPrevious(false);
                        filter.focus();
                    }
                    return;
                }
                if (be.getKeyCode() == KeyCodes.KEY_DOWN) {
                    ForeignKeyBean fkBean = grid.getSelectionModel().getSelectedItem();
                    if (fkBean == null) {
                        grid.getSelectionModel().select(0, true);
                    } else {
                        grid.getSelectionModel().selectNext(false);
                        filter.focus();
                    }
                    return;
                }
                if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (filter.getRawValue().equals(previousFilterText)) {
                        ForeignKeyBean fkBean = grid.getSelectionModel().getSelectedItem();
                        fkBean.setForeignKeyPath(entityModel.getConceptName());
                        fkBean.setDisplayInfo(fkBean.toString() != null ? fkBean.toString() : fkBean.getId());
                        returnCriteriaFK.setCriteriaFK(fkBean);
                        closeOrHideWindow();
                    } else {
                        previousFilterText = filter.getRawValue();
                        loader.load(0, pageToolBar.getPageSize());
                    }

                }
                if (be.getKeyCode() == KeyCodes.KEY_LEFT || be.getKeyCode() == KeyCodes.KEY_RIGHT) {
                    return;
                }
            }
        });
        filter.setWidth(WINDOW_WIDTH - 80);

        ToolBar toolBar = new ToolBar();

        typeComboBox = new ComboBoxField<BaseModel>();
        typeComboBox.setRawValue(entityModel.getConceptName());
        typeComboBox.setDisplayField("name"); //$NON-NLS-1$
        typeComboBox.setValueField("value"); //$NON-NLS-1$
        typeComboBox.setStore(typeList);
        typeComboBox.setTriggerAction(TriggerAction.ALL);
        typeComboBox.setEmptyText(MessagesFactory.getMessages().label_select_type());
        typeComboBox.setId("DerivedTypeComboBox"); //$NON-NLS-1$

        typeComboBox.addSelectionChangedListener(new SelectionChangedListener<BaseModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<BaseModel> se) {
                String targetEntity = se.getSelectedItem().get("value").toString();//$NON-NLS-1$
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < ForeignKeyListWindow.this.foreignKeyInfo.size(); i++) {
                    sb.append(ForeignKeyListWindow.this.foreignKeyInfo.get(i));
                    if (i < ForeignKeyListWindow.this.foreignKeyInfo.size() - 1 && i >= 0) {
                        sb.append(",");//$NON-NLS-1$
                    }
                }
                String fkInfo = sb.toString();
                ForeignKeyDrawer fkDrawer = CommonUtil.switchForeignKeyEntityType(targetEntity,
                        ForeignKeyListWindow.this.foreignKeyPath, fkInfo);
                ForeignKeyListWindow.this.foreignKeyPath = fkDrawer.getXpathForeignKey();
                List<String> fkinfo = new ArrayList<String>();
                if (fkDrawer.getXpathInfoForeignKey() != null) {
                    String[] foreignKeyList = fkDrawer.getXpathInfoForeignKey().split(","); //$NON-NLS-1$
                    for (String info : foreignKeyList) {
                        fkinfo.add(info);
                    }
                }
                ForeignKeyListWindow.this.foreignKeyInfo = fkinfo;
                loader.load(0, pageToolBar.getPageSize());
                if (entityModel != null && targetEntity != null && !targetEntity.equals(entityModel.getConceptName())) {
                    fkDrawer = CommonUtil.switchForeignKeyEntityType(entityModel.getConceptName(),
                            ForeignKeyListWindow.this.foreignKeyPath, fkInfo);
                    ForeignKeyListWindow.this.foreignKeyPath = fkDrawer.getXpathForeignKey();
                }
            }
        });
        toolBar.add(typeComboBox);

        Button filterBtn = new Button();
        filterBtn.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.funnel()));
        filterBtn.setWidth(30);
        filterBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                loader.load(0, pageToolBar.getPageSize());
            }
        });
        filter.setWidth(200);
        typeComboBox.setWidth(WINDOW_WIDTH - 250);
        toolBar.add(filter);
        toolBar.add(filterBtn);
        panel.setTopComponent(toolBar);

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        // build columns by specify store

        // change label display
        // boolean retrieveFKinfos = typeModel.isRetrieveFKinfos();
        boolean isDisplayKeyInfo = false;
        if (this.foreignKeyInfo.size() > 0) {
            List<String> foreignKeyInfo = this.foreignKeyInfo;
            if (foreignKeyInfo.contains(this.foreignKeyPath)) {
                isDisplayKeyInfo = true;
            }
            Map<String, TypeModel> dataTypes = entityModel.getMetaDataTypes();
            for (String info : foreignKeyInfo) {
                TypeModel metaDataType = dataTypes.get(info);
                String id = CommonUtil.getElementFromXpath(info);
                ColumnConfig columnConfig = new ColumnConfig(id, metaDataType == null ? id : ViewUtil.getViewableLabel(
                        Locale.getLanguage(), metaDataType), COLUMN_WIDTH);
                columns.add(columnConfig);
                if (entityModel.getTypeModel(info).getType().equals(DataTypeConstants.MLS)) {

                    columnConfig.setRenderer(new GridCellRenderer<ForeignKeyBean>() {

                        @Override
                        public Object render(final ForeignKeyBean fkBean, String property, ColumnData config, int rowIndex,
                                int colIndex, ListStore<ForeignKeyBean> store, Grid<ForeignKeyBean> grid) {
                            String multiLanguageString = (String) fkBean.get(property);
                            MultiLanguageModel multiLanguageModel = new MultiLanguageModel(multiLanguageString);
                            return Format.htmlEncode(multiLanguageModel.getValueByLanguage(Locale.getLanguage().toUpperCase()));
                        }
                    });

                }
            }
            if (columns.size() > 0) {
                config.setSortField(columns.get(0).getId());
                config.setSortDir(SortDir.ASC);
            }
        }
        if (columns.size() == 0) {
            columns.add(new ColumnConfig("id", CommonUtil.getElementFromXpath(typeModel.getXpath()), COLUMN_WIDTH)); //$NON-NLS-1$
            isDisplayKeyInfo = true;
        }

        // fix bug TMDM-2829
        if (!isDisplayKeyInfo) {
            ColumnConfig columnConfig = columns.get(0);
            final String fkInfo = this.foreignKeyInfo.get(0);
            columnConfig.setRenderer(new GridCellRenderer<ForeignKeyBean>() {

                @Override
                public Object render(final ForeignKeyBean fkBean, String property, ColumnData config, int rowIndex, int colIndex,
                        ListStore<ForeignKeyBean> store, Grid<ForeignKeyBean> grid) {
                    String result = ""; //$NON-NLS-1$
                    if (fkBean != null) {
                        if (fkBean.get(property) != null && !"".equals(fkBean.get(property))) { //$NON-NLS-1$
                            if (entityModel.getTypeModel(fkInfo).getType().equals(DataTypeConstants.MLS)) {
                                MultiLanguageModel multiLanguageModel = new MultiLanguageModel(fkBean.get(property).toString());
                                result = multiLanguageModel.getValueByLanguage(Locale.getLanguage().toUpperCase()) + "-"; //$NON-NLS-1$
                            } else {
                                if (fkBean.getDisplayInfo() != null && fkBean.getDisplayInfo().length() > 0) {
                                    result = fkBean.getDisplayInfo() + "-"; //$NON-NLS-1$
                                } else {
                                    result = fkBean.get(property) + "-"; //$NON-NLS-1$
                                }
                            }
                        }
                        return result = result + fkBean.getId();
                    }
                    return result;
                }
            });
        }

        ColumnModel cm = new ColumnModel(columns);
        grid = new Grid<ForeignKeyBean>(store, cm);
        grid.getView().setForceFit(true);
        grid.setLoadMask(true);
        grid.setBorders(false);
        grid.setStateful(true);
        grid.setStateId("relatedRecordGrid"); //$NON-NLS-1$
        grid.addListener(Events.Attach, new Listener<GridEvent<ForeignKeyBean>>() {

            @Override
            public void handleEvent(GridEvent<ForeignKeyBean> be) {
                config.setOffset(0);
                config.setLimit(pageToolBar.getPageSize());
                loader.load(config);
            }
        });

        grid.addListener(Events.OnDoubleClick, new Listener<GridEvent<ForeignKeyBean>>() {

            @Override
            public void handleEvent(final GridEvent<ForeignKeyBean> be) {
                ForeignKeyBean fkBean = be.getModel();
                fkBean.setForeignKeyPath(entityModel.getConceptName());
                CommonUtil.setForeignKeyDisplayInfo(fkBean);
                sourceField.setValue(fkBean);
                typeComboBox.setRawValue(null);
                closeOrHideWindow();
            }
        });

        panel.add(grid);
        panel.setBottomComponent(pageToolBar);

        Button cancelBtn = new Button(MessagesFactory.getMessages().cancel_btn());
        cancelBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                closeOrHideWindow();
            }
        });
        addButton(cancelBtn);
        add(panel, new FlowData(5));
        addListener(Events.Resize, new Listener<WindowEvent>() {

            @Override
            public void handleEvent(WindowEvent e) {
                recalculate(e.getHeight(), e.getWidth());
            }
        });
    }

    protected void closeOrHideWindow() {
        hide(null);
    }

    public ComboBoxField<BaseModel> getTypeComboBox() {
        return this.typeComboBox;
    }

    private void recalculate(int height, int width) {
        this.getWidget(0).setHeight(height - MARGIN_HEIGHT + "px"); //$NON-NLS-1$
        this.getWidget(0).setWidth(width - MARGIN_WIDTH + "px"); //$NON-NLS-1$
    }

}
