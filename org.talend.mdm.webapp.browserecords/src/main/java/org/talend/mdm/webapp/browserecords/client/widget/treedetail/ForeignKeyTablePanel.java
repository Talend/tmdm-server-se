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
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.MultiLanguageModel;
import org.talend.mdm.webapp.base.client.widget.PagingToolBarEx;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.base.shared.XpathUtil;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsEvents;
import org.talend.mdm.webapp.browserecords.client.ServiceFactory;
import org.talend.mdm.webapp.browserecords.client.handler.ItemTreeHandler;
import org.talend.mdm.webapp.browserecords.client.handler.ItemTreeHandlingStatus;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.mvc.BrowseRecordsView;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKeyRowEditor;
import org.talend.mdm.webapp.browserecords.client.widget.ItemPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeySelector;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ReturnCriteriaFK;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.celleditor.FKKeyCellEditor;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.celleditor.ForeignKeyCellEditor;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.creator.FieldCreator;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Style.HideMode;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.state.StateManager;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ForeignKeyTablePanel extends ContentPanel implements ReturnCriteriaFK {

    private static final int COLUMN_WIDTH = 100;

    private int PAGE_SIZE = 10;

    private String panelName;

    private PagingToolBarEx pagingBar = null;

    Grid<ItemNodeModel> grid;

    ToolBar toolBar = new ToolBar();

    HTML statusBar = new HTML();

    Button addFkButton = new Button(MessagesFactory.getMessages().add_btn(), AbstractImagePrototype.create(Icons.INSTANCE
            .Create()));

    Button removeFkButton = new Button(MessagesFactory.getMessages().remove_btn(), AbstractImagePrototype.create(Icons.INSTANCE
            .Delete()));

    Button createFkButton = new Button(MessagesFactory.getMessages().create_btn(), AbstractImagePrototype.create(Icons.INSTANCE
            .Create()));

    Button editFkButton = new Button(MessagesFactory.getMessages().edit_btn(), AbstractImagePrototype.create(Icons.INSTANCE
            .BulkUpdate()));

    TypeModel fkTypeModel;

    List<ItemNodeModel> fkModels;

    PagingModelMemoryProxy proxy;

    PagingLoader<PagingLoadResult<ModelData>> loader;

    ListStore<ItemNodeModel> store;

    ItemNodeModel parent;

    EntityModel entityModel;

    ItemNodeModel currentNodeModel;

    ItemNodeModel lastFkModel;

    int lastFkIndex;

    Map<String, Field<?>> fieldMap;

    private ItemsDetailPanel itemsDetailPanel;

    private VerticalPanel bottomPanel = new VerticalPanel();

    private boolean staging;

    private FKKeyCellEditor keyCellEditor;

    private ForeignKeySelector foreignKeySelector;

    private boolean isBulkUpdate = false;

    private boolean enable = false;

    public ForeignKeyTablePanel(String panelName, boolean staging, boolean isBulkUpdate) {
        super();
        this.setHeaderVisible(false);
        this.setLayout(new FitLayout());
        this.setAutoWidth(true);
        this.setBodyBorder(false);
        this.panelName = panelName;
        this.staging = staging;
        this.isBulkUpdate = isBulkUpdate;
        initBaseComponent();
    }

    public ForeignKeyTablePanel(final EntityModel entityModel, ItemNodeModel parent, final List<ItemNodeModel> fkModels,
            final TypeModel fkTypeModel, Map<String, Field<?>> fieldMap, ItemsDetailPanel itemsDetailPanel) {
        this(entityModel, parent, fkModels, fkTypeModel, fieldMap, itemsDetailPanel, null);
    }

    public ForeignKeyTablePanel(final EntityModel entityModel, ItemNodeModel parent, final List<ItemNodeModel> fkModels,
            final TypeModel fkTypeModel, Map<String, Field<?>> fieldMap, ItemsDetailPanel itemsDetailPanel,
            ViewBean originalViewBean) {
        initContent(entityModel, parent, fkModels, fkTypeModel, fieldMap, itemsDetailPanel, originalViewBean);
    }

    private void initBaseComponent() {
        // topComponent

        if (isBulkUpdate) {
            addFkButton.setEnabled(false);
            toolBar.add(addFkButton);
            toolBar.add(new SeparatorToolItem());
            removeFkButton.setEnabled(false);
            toolBar.add(removeFkButton);
            toolBar.add(new SeparatorToolItem());
            createFkButton.setEnabled(false);
            toolBar.add(createFkButton);
            toolBar.add(new SeparatorToolItem());
            toolBar.add(editFkButton);
            toolBar.add(new SeparatorToolItem());
        } else {
            toolBar.add(addFkButton);
            toolBar.add(new SeparatorToolItem());
            toolBar.add(removeFkButton);
            toolBar.add(new SeparatorToolItem());
            toolBar.add(createFkButton);
            toolBar.add(new SeparatorToolItem());
        }
        this.setTopComponent(toolBar);
        // bottomComponent
        bottomPanel.setWidth("100%"); //$NON-NLS-1$
        statusBar.getElement().getStyle().setFontSize(12D, Unit.PX);
        statusBar.setHeight("16px"); //$NON-NLS-1$
        bottomPanel.add(statusBar);
        if (StateManager.get().get(panelName) != null) {
            PAGE_SIZE = Integer.valueOf(((Map<?, ?>) StateManager.get().get(panelName)).get("limit").toString()); //$NON-NLS-1$
        }
        pagingBar = new PagingToolBarEx(PAGE_SIZE);
        pagingBar.setHideMode(HideMode.VISIBILITY);
        pagingBar.getMessages().setDisplayMsg(MessagesFactory.getMessages().page_displaying_records());
        bottomPanel.add(pagingBar);
        this.setBottomComponent(new WidgetComponent(bottomPanel));
    }

    public void initContent(final EntityModel entityModel, ItemNodeModel parent, final List<ItemNodeModel> fkModels,
            final TypeModel fkTypeModel, Map<String, Field<?>> fieldMap, ItemsDetailPanel itemsDetailPanel,
            ViewBean originalViewBean) {
        this.itemsDetailPanel = itemsDetailPanel;
        this.parent = parent;
        this.entityModel = entityModel;
        this.fkTypeModel = fkTypeModel;
        this.fkModels = fkModels;
        this.fieldMap = fieldMap;

        addListener();

        if (isBulkUpdate) {
            for (int i = 0; i < fkModels.size(); i++) {
                fkModels.get(i).setMassUpdate(isBulkUpdate);
            }
        }
        if (fkTypeModel.getForeignKeyFilter() != null && isBulkUpdate) {
            editFkButton.setEnabled(false);
            MessageBox.alert(MessagesFactory.getMessages().warning_title(),
                    MessagesFactory.getMessages().bulkUpdate_foreignkey_warning(), null).setIcon(MessageBox.WARNING);
        }

        proxy = new PagingModelMemoryProxy(this.fkModels);
        loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
        store = new ListStore<ItemNodeModel>(loader);

        List<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
        final CheckBoxSelectionModel<ItemNodeModel> sm = new CheckBoxSelectionModel<ItemNodeModel>();
        columnConfigs.add(sm.getColumn());

        foreignKeySelector = new ForeignKeySelector(fkTypeModel, itemsDetailPanel, parent);
        foreignKeySelector.setUsageField("ForeignKeyTablePanel"); //$NON-NLS-1$
        foreignKeySelector.setShowAddButton(false);
        foreignKeySelector.setShowCleanButton(false);
        foreignKeySelector.setShowRelationButton(false);

        final ColumnConfig keyColumn = new ColumnConfig("objectValue", convertKeys(entityModel.getKeys()), COLUMN_WIDTH); //$NON-NLS-1$

        keyCellEditor = new FKKeyCellEditor(foreignKeySelector, fkTypeModel, this);
        keyColumn.setEditor(keyCellEditor);

        keyColumn.setRenderer(new GridCellRenderer<ItemNodeModel>() {

            @Override
            public Object render(final ItemNodeModel model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<ItemNodeModel> store, Grid<ItemNodeModel> grid) {
                foreignKeySelector.setItemNode(model);
                currentNodeModel = model;
                model.setValid(false);
                ForeignKeyBean fkBean = (ForeignKeyBean) model.getObjectValue();
                final String value = fkBean != null ? fkBean.getId() : ""; //$NON-NLS-1$
                return value;

            }
        });
        columnConfigs.add(keyColumn);

        List<String> foreignKeyInfo = fkTypeModel.getForeignKeyInfo();
        for (final String info : foreignKeyInfo) {
            final String fkInfo = info.startsWith(".") ? XpathUtil.convertAbsolutePath(fkTypeModel.getForeignkey(), info) : info; //$NON-NLS-1$
            if (fkInfo.equals(fkTypeModel.getForeignkey())) {
                continue;
            }
            final ColumnConfig column = new ColumnConfig(info, entityModel.getTypeModel(fkInfo).getLabel(Locale.getLanguage()),
                    COLUMN_WIDTH); // using the label
                                   // to display table
                                   // header
            column.setDataIndex("objectValue"); //$NON-NLS-1$
            column.setRenderer(new GridCellRenderer<ItemNodeModel>() {

                @Override
                public Object render(ItemNodeModel model, String property, ColumnData config, int rowIndex, int colIndex,
                        ListStore<ItemNodeModel> store, Grid<ItemNodeModel> grid) {
                    ForeignKeyBean fkBean = (ForeignKeyBean) model.getObjectValue();
                    String value = fkBean != null && fkBean.getForeignKeyInfo().containsKey(fkInfo) ? fkBean.getForeignKeyInfo()
                            .get(fkInfo) : ""; //$NON-NLS-1$
                    if (entityModel.getTypeModel(fkInfo).getType().equals(DataTypeConstants.MLS)) {
                        MultiLanguageModel multiLanguageModel = new MultiLanguageModel(value);
                        value = multiLanguageModel.getValueByLanguage(Locale.getLanguage().toUpperCase());
                    } else if (fkBean != null && "".equals(value)) { //$NON-NLS-1$
                        String propertyName = fkInfo.substring(fkInfo.lastIndexOf("/") + 1); //$NON-NLS-1$
                        if (fkBean.getForeignKeyInfo().get(info) != null) {
                            value = fkBean.getForeignKeyInfo().get(info).toString();
                        } else if (fkBean.get(propertyName) != null) {
                            value = fkBean.get(propertyName).toString();
                        }
                    }
                    return value;
                }

            });

            TypeModel typeModel = entityModel.getMetaDataTypes().get(fkInfo);
            Field<?> field = FieldCreator.createField((SimpleTypeModel) typeModel, null, false, Locale.getLanguage());
            field.setEnabled(disabled);
            CellEditor cellEditor = new ForeignKeyCellEditor(field, typeModel);
            if (cellEditor != null) {
                column.setEditor(cellEditor);
            }
            columnConfigs.add(column);
        }

        ColumnConfig columnOpt = new ColumnConfig("", "", COLUMN_WIDTH); //$NON-NLS-1$ //$NON-NLS-2$
        columnOpt.setFixed(true);
        columnOpt.setWidth(60);
        columnOpt.setRenderer(optRender);
        columnConfigs.add(columnOpt);

        ColumnModel cm = new ColumnModel(columnConfigs);
        grid = new Grid<ItemNodeModel>(store, cm);
        grid.getView().setForceFit(true);
        if (cm.getColumnCount() > 0) {
            grid.setAutoExpandColumn(cm.getColumn(0).getHeader());
        }

        grid.addListener(Events.Attach, new Listener<GridEvent<ItemNodeModel>>() {

            @Override
            public void handleEvent(GridEvent<ItemNodeModel> be) {
                PagingLoadConfig config = new BasePagingLoadConfig();
                config.setOffset(0);
                int pageSize = pagingBar.getPageSize();
                config.setLimit(pageSize);
                loader.load(config);
            }
        });

        if (parent.getParent() != null && !parent.isMandatory()) {
            grid.getView().addListener(Events.Refresh, new Listener<BaseEvent>() {

                @Override
                public void handleEvent(BaseEvent be) {
                    updateSiblingNodes();
                };
            });
        }
        final ForeignKeyRowEditor re = new ForeignKeyRowEditor(fkTypeModel, staging);
        grid.setSelectionModel(sm);
        grid.addPlugin(sm);
        grid.setStateId(panelName);
        grid.setStateful(true);
        hookContextMenu(re);
        if (!isReadonly(entityModel)) {
            grid.addPlugin(re);
        }

        // TMDM-3202 open FK in new tab
        grid.addListener(Events.OnDoubleClick, new Listener<GridEvent<ItemNodeModel>>() {

            @Override
            public void handleEvent(GridEvent<ItemNodeModel> be) {
                int rowIndex = be.getRowIndex();
                if (rowIndex != -1) {
                    ItemNodeModel model = grid.getStore().getAt(rowIndex);
                    openForeignKey(model);
                }
            }
        });

        grid.addListener(Events.CellClick, new Listener<GridEvent<ItemNodeModel>>() {

            @Override
            public void handleEvent(GridEvent<ItemNodeModel> be) {

                int rowIndex = be.getRowIndex();
                if (be.getColIndex() == 1) {
                    ItemNodeModel m = be.getModel();
                    if (m == null || m.getObjectValue() == null) {
                        return;
                    }
                    if (rowIndex != -1) {
                        currentNodeModel = m;
                        re.startEditing(rowIndex, true);
                    }
                }
            }
        });

        // grid.setWidth(Window.getClientWidth() - ItemsListPanel.getInstance().getInnerWidth());
        grid.setBorders(false);
        this.removeAll();
        this.add(grid);

        pagingBar.bind(loader);
        loader.setRemoteSort(true);

        updateMandatory();
        if (isBulkUpdate) {
            grid.setEnabled(false);
        }
    }

    private boolean isReadonly(final EntityModel model) {
        if (model.getMetaDataTypes() != null) {
            TypeModel fkType = model.getMetaDataTypes().get(model.getConceptName());
            if (fkType != null) {
                return fkType.isReadOnly();
            }
        }
        return true;
    }

    private void updateSiblingNodes() {
        if (parent.getChildCount() > 0) {
            ItemNodeModel child = (ItemNodeModel) parent.getChild(0);
            Field<?> field = fieldMap.get(child.getId().toString());
            if (field != null) {
                TreeDetailGridFieldCreator.updateMandatory(field, child, fieldMap);
            }
        }
    }

    private String convertKeys(String[] keys) {
        if (keys.length == 1) {
            // using the label to display table header
            return entityModel.getTypeModel(keys[0]).getLabel(Locale.getLanguage());
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < keys.length; i++) {
            if (i != 0) {
                sb.append("-"); //$NON-NLS-1$
            }
            // using the label to display table header
            sb.append(entityModel.getTypeModel(keys[i]).getLabel(Locale.getLanguage()));
        }
        return sb.toString();
    }

    private void addListener() {
        addFkButton.removeAllListeners();
        removeFkButton.removeAllListeners();
        createFkButton.removeAllListeners();
        addFkButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (!addFk()) {
                    // maxOccurs tip
                    MessageBox.alert(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages()
                            .fk_validate_max_occurence(fkTypeModel.getLabel(Locale.getLanguage()), fkTypeModel.getMaxOccurs()),
                            null);
                }
            }
        });
        removeFkButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                List<ItemNodeModel> selectedFkModelList = grid.getSelectionModel().getSelectedItems();
                if (selectedFkModelList != null && selectedFkModelList.size() > 0) {
                    boolean allSelected = (fkModels.size() == selectedFkModelList.size());
                    int endIndex = allSelected ? 1 : 0;
                    for (int i = selectedFkModelList.size() - 1; i >= endIndex; i--) {
                        ItemNodeModel itemNodeModel = selectedFkModelList.get(i);
                        delFk(itemNodeModel);
                    }
                    if (allSelected) {
                        if (grid.getStore().getCount() == 1) {
                            ItemNodeModel itemNodeModel = grid.getStore().getAt(0);
                            if (itemNodeModel != null) {
                                itemNodeModel.setObjectValue(null);
                                itemNodeModel.setChangeValue(true);
                            }
                        }
                    }

                    updateMandatory();
                    pagingBar.refresh();
                }
            }
        });
        createFkButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                boolean autoSetFk = false;
                if (fkModels != null && fkModels.size() > 0) {
                    currentNodeModel = fkModels.get(fkModels.size() - 1);
                    if (currentNodeModel.getObjectValue() != null) {
                        if (addFk()) {
                            currentNodeModel = fkModels.get(fkModels.size() - 1);
                            autoSetFk = true;
                        }
                    } else {
                        autoSetFk = true;
                    }
                }
                Dispatcher dispatch = Dispatcher.get();
                AppEvent event = new AppEvent(BrowseRecordsEvents.CreateForeignKeyView, entityModel.getConceptName());
                event.setData(BrowseRecordsView.ITEMS_DETAIL_PANEL, itemsDetailPanel);
                if (autoSetFk) {
                    event.setData(BrowseRecordsView.FK_SOURCE_WIDGET, ForeignKeyTablePanel.this);
                }
                event.setData(BrowseRecordsView.IS_STAGING, staging);
                dispatch.dispatch(event);
            }
        });

        createFkButton.setEnabled(!entityModel.getTypeModel(entityModel.getConceptName()).isDenyCreatable() && !isBulkUpdate);

        editFkButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                enable = !enable;
                if (enable) {
                    addFkButton.setEnabled(true);
                    removeFkButton.setEnabled(true);
                    createFkButton.setEnabled(!entityModel.getTypeModel(entityModel.getConceptName()).isDenyCreatable());
                    grid.setEnabled(true);
                    for (int i = 0; i < fkModels.size(); i++) {
                        fkModels.get(i).setEdited(enable);
                    }
                } else {
                    addFkButton.setEnabled(false);
                    removeFkButton.setEnabled(false);
                    createFkButton.setEnabled(false);
                    foreignKeySelector.setEnabled(false);
                    grid.setEnabled(false);

                    if (fkModels.size() > 1) {
                        for (int i = fkModels.size() - 1; i >= 1; i--) {
                            delFk(fkModels.get(i));
                        }
                    }
                    ItemNodeModel itemNodeModel = grid.getStore().getAt(0);
                    if (itemNodeModel != null) {
                        itemNodeModel.setObjectValue(null);
                        itemNodeModel.setChangeValue(false);
                        itemNodeModel.setEdited(enable);
                    }
                    updateMandatory();
                    grid.getView().layout();
                    pagingBar.refresh();
                }
            }
        });

        if (fkTypeModel.isReadOnly()) {
            addFkButton.setEnabled(false);
            removeFkButton.setEnabled(false);
            editFkButton.setEnabled(false);
        }
    }

    private boolean addFk() {
        int min = fkTypeModel.getMinOccurs();
        int max = fkTypeModel.getMaxOccurs();
        int count = 1;
        if (min >= 0 && max > min) {
            count = max;
        }
        if (fkModels.size() < count || max == -1) {
            ItemNodeModel lastRowModel;
            int index;
            if (fkModels.size() > 0 && fkModels.get(fkModels.size() - 1) != null) {
                lastRowModel = fkModels.get(fkModels.size() - 1);
                index = parent.indexOf(lastRowModel);
            } else {
                lastRowModel = lastFkModel;
                index = lastFkIndex;
            }
            ItemNodeModel newFkModel = lastRowModel.clone(false);
            newFkModel.setMassUpdate(isBulkUpdate);
            newFkModel.setEdited(enable);
            parent.insert(newFkModel, index + 1);
            newFkModel.setParent(parent);
            fkModels.add(newFkModel);
            parent.setChangeValue(true);
            grid.getView().layout();
            pagingBar.refresh();
            return true;
        } else {
            return false;
        }
    }

    private void delFk(ItemNodeModel currentFkModel) {
        lastFkModel = currentFkModel;
        lastFkIndex = parent.indexOf(currentFkModel) - 1;
        int min = fkTypeModel.getMinOccurs();
        int count = 1;
        if (min > 0) {
            count = min;
        } else {
            count = 0;
        }
        if (fkModels.size() > count) {
            fkModels.remove(currentFkModel);
            grid.getStore().remove(currentFkModel);
            TreeModel parent = currentFkModel.getParent();
            parent.remove(currentFkModel);
            ((ItemNodeModel) parent).setChangeValue(true);
        }
    }

    GridCellRenderer<ItemNodeModel> optRender = new GridCellRenderer<ItemNodeModel>() {

        @Override
        public Object render(final ItemNodeModel model, String property, ColumnData config, final int rowIndex, int colIndex,
                final ListStore<ItemNodeModel> store, Grid<ItemNodeModel> grid) {
            ForeignKeySelector foreignKeySelector = new ForeignKeySelector(fkTypeModel, itemsDetailPanel, model);
            foreignKeySelector.setStaging(staging);
            foreignKeySelector.setShowInput(false);
            foreignKeySelector.setShowAddButton(false);
            foreignKeySelector.setShowCleanButton(false);
            foreignKeySelector.setShowRelationButton(false);
            foreignKeySelector.addListener(Events.Change, new Listener<FieldEvent>() {

                @Override
                public void handleEvent(FieldEvent be) {
                    currentNodeModel = model;
                    ForeignKeyTablePanel.this.setCriteriaFK((ForeignKeyBean) be.getField().getValue());
                }
            });

            Image linkFKBtn = new Image(Icons.INSTANCE.link_go());
            linkFKBtn.setTitle(MessagesFactory.getMessages().fk_open_title());
            linkFKBtn.getElement().getStyle().setCursor(Cursor.POINTER);
            linkFKBtn.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    openForeignKey(model);
                }
            });

            com.google.gwt.user.client.ui.Grid optGrid = new com.google.gwt.user.client.ui.Grid(1, 2);
            optGrid.setCellPadding(0);
            optGrid.setCellSpacing(0);
            optGrid.setWidget(0, 0, foreignKeySelector);
            if (!isBulkUpdate) {
                optGrid.setWidget(0, 1, linkFKBtn);
            }
            return optGrid;
        }
    };

    private void openForeignKey(ItemNodeModel model) {
        if (model == null) {
            return;
        }
        ForeignKeyBean fkBean = (ForeignKeyBean) model.getObjectValue();
        if (fkBean == null || fkBean.getId() == null || "".equals(fkBean.getId())) { //$NON-NLS-1$
            return;
        }
        String ids = fkBean.getId().replaceAll("^\\[|\\]$", "").replace("][", "."); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
        ForeignKeyUtil.checkChange(false, fkBean.getConceptName() != null ? fkBean.getConceptName() : fkTypeModel.getForeignkey()
                .split("/")[0], ids, itemsDetailPanel); //$NON-NLS-1$
    }

    public void setCriteriaFK(final String foreignKeyIds) {
        ItemPanel itemPanel = (ItemPanel) itemsDetailPanel.getFirstTabWidget();
        ItemNodeModel root = itemPanel.getTree().getRootModel();
        String xml = (new ItemTreeHandler(root, itemPanel.getViewBean(), ItemTreeHandlingStatus.BeforeLoad)).serializeItem();
        ServiceFactory
                .getInstance()
                .getService(staging)
                .getForeignKeyBean(
                        fkTypeModel.getForeignkey().split("/")[0], foreignKeyIds, xml, fkTypeModel.getXpath(), fkTypeModel.getForeignkey(), fkTypeModel.getForeignKeyInfo(), //$NON-NLS-1$
                        foreignKeySelector.parseForeignKeyFilter(), staging, Locale.getLanguage(),
                        new SessionAwareAsyncCallback<ForeignKeyBean>() {

                            @Override
                            public void onSuccess(ForeignKeyBean foreignKeyBean) {
                                if (foreignKeyBean != null) {
                                    setCriteriaFK(foreignKeyBean);
                                } else {
                                    MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                                            .foreignkey_filter_warning(), null);
                                }
                            }
                        });
    }

    @Override
    public void setCriteriaFK(ForeignKeyBean fk) {
        // TODO check fk exist
        if (currentNodeModel != null) {
            if (currentNodeModel.getObjectValue() != null) {
                ForeignKeyBean currentForeignKeyBean = (ForeignKeyBean) currentNodeModel.getObjectValue();
                if (!currentForeignKeyBean.getId().equals(fk.getId()) || !currentForeignKeyBean.toString().equals(fk.toString())) {
                    changeCriteriaFK(fk);
                }
            } else {
                changeCriteriaFK(fk);
            }
        }
    }

    private void changeCriteriaFK(ForeignKeyBean fk) {
        currentNodeModel.setObjectValue(fk);
        currentNodeModel.setTypeName(fk.getConceptName());
        currentNodeModel.setChangeValue(true);
        grid.getView().refresh(false);
        updateMandatory();
    }

    private void updateMandatory() {
        if (this.fkModels.size() > 0) {
            ItemNodeModel fk = fkModels.get(0);
            int fkValueCount = 0;
            for (ItemNodeModel fkModel : fkModels) {
                if (fkModel.getObjectValue() != null) {
                    fkValueCount++;
                }
            }
            if (fkValueCount < fkTypeModel.getMinOccurs()) {

                setStatusHtml("<b style=\"color: red;\">" + MessagesFactory.getMessages().multiOccurrence_minimize_title(fkTypeModel.getMinOccurs(), fk.getName()) + "</b>"); //$NON-NLS-1$//$NON-NLS-2$
            } else {
                setStatusHtml(null);
            }
        }
    }

    public void setStatusHtml(String html) {
        statusBar.setHTML(html);
    }

    private void hookContextMenu(final ForeignKeyRowEditor re) {
        Menu contextMenu = new Menu();
        MenuItem editRow = new MenuItem();
        editRow.setText(MessagesFactory.getMessages().edititem());
        editRow.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Edit()));
        editRow.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                ItemNodeModel m = grid.getSelectionModel().getSelectedItem();
                if (m == null) {
                    MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                            .grid_record_select(), null);
                    return;
                }
                if (m.getObjectValue() == null) {
                    MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                            .fk_edit_failure(), null);
                    return;
                }
                int rowIndex = grid.getStore().indexOf(m);
                re.startEditing(rowIndex, true);
            }
        });

        editRow.setEnabled(!isReadonly(entityModel));
        contextMenu.add(editRow);
        grid.setContextMenu(contextMenu);
    }

    public TypeModel getFkTypeModel() {
        return this.fkTypeModel;
    }
}
