/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client.widget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.journal.client.Journal;
import org.talend.mdm.webapp.journal.client.JournalServiceAsync;
import org.talend.mdm.webapp.journal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.journal.client.resources.icon.Icons;
import org.talend.mdm.webapp.journal.shared.JournalGridModel;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;
import org.talend.mdm.webapp.journal.shared.JournalTreeModel;

import com.amalto.core.objects.UpdateReportPOJO;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class JournalDataPanel extends FormPanel {

    private JournalHistoryPanel journalHistoryPanel;

    private JournalServiceAsync service = Registry.get(Journal.JOURNAL_SERVICE);

    private ListStore<JournalGridModel> gridStore = JournalGridPanel.getInstance().getStore();

    private PagingLoadConfig localPagingLoadConfig;

    private PagingLoader<PagingLoadResult<JournalGridModel>> localLoader;

    private int total;

    private Button openRecordButton;

    private Button viewUpdateReportButton;

    private List<JournalGridModel> journalNavigateList;

    private boolean naviToPrevious;

    private List<JournalGridModel> currentDataList;

    private Set<String> deletedKeys;

    private boolean turnPage = false;

    private boolean backupPage = false;

    private Button prevUpdateReportButton;

    private Button nextUpdateReportButton;

    private TreePanel<JournalTreeModel> tree;

    private Window treeWindow;

    private TreeStore<JournalTreeModel> treeStore;

    private JournalTreeModel root;

    private JournalGridModel journalGridModel;

    private LayoutContainer main;

    private LabelField entityField;

    private LabelField sourceField;

    private LabelField dataContainerField;

    private LabelField dataModelField;

    private LabelField keyField;

    private LabelField operationTypeField;

    private LabelField oeprationTimeField;

    private SelectionListener<ButtonEvent> updateReportListener;

    private SelectionListener<ButtonEvent> openRecordListener;

    public JournalDataPanel(final JournalTreeModel root, final JournalGridModel journalGridModel) {
        this.setFrame(false);
        this.setHeight(-1);
        this.setItemId(journalGridModel.getIds());
        this.journalGridModel = journalGridModel;
        this.root = root;
        this.setHeading(MessagesFactory.getMessages().update_report_detail_label());
        this.setBodyBorder(false);
        this.setLayout(new FitLayout());
        initializeTreeWindow();
        openRecordButton = new Button(MessagesFactory.getMessages().open_record_button());
        if (UpdateReportPOJO.OPERATION_TYPE_LOGICAL_DELETE.equals(journalGridModel.getOperationType())
                || UpdateReportPOJO.OPERATION_TYPE_PHYSICAL_DELETE.equals(journalGridModel.getOperationType())) {
            openRecordButton.disable();
        }
        openRecordButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.browse()));
        this.openRecordListener = createOpenRecordListener();
        openRecordButton.addSelectionListener(this.openRecordListener);

        viewUpdateReportButton = new Button(MessagesFactory.getMessages().view_updatereport_button());
        viewUpdateReportButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.view()));

        this.updateReportListener = createUpdateReportListener();
        viewUpdateReportButton.addSelectionListener(this.updateReportListener);

        prevUpdateReportButton = new Button(MessagesFactory.getMessages().prev_updatereport_button());
        prevUpdateReportButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.prev()));
        prevUpdateReportButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                naviToPrevious = true;
                JournalGridModel preGridModel = journalNavigateList.get(0);
                if (preGridModel != null) {
                    JournalDataPanel.this.updateTabPanel(preGridModel);
                    toggleOpenRecordButton(preGridModel);
                } else {
                    retrieveNeighbourJournalInOtherPages();
                }
            }
        });

        nextUpdateReportButton = new Button(MessagesFactory.getMessages().next_updatereport_button());
        nextUpdateReportButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.next()));
        nextUpdateReportButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                naviToPrevious = false;
                JournalGridModel nextGridModel = journalNavigateList.get(1);
                if (nextGridModel != null) {
                    JournalDataPanel.this.updateTabPanel(nextGridModel);
                    toggleOpenRecordButton(nextGridModel);
                } else {
                    retrieveNeighbourJournalInOtherPages();
                }
            }
        });

        this.initLoadConfig();

        this.addButton(viewUpdateReportButton);
        this.addButton(prevUpdateReportButton);
        this.addButton(nextUpdateReportButton);
        this.addButton(openRecordButton);

        this.setHeading(MessagesFactory.getMessages().change_properties());
        this.setButtonAlign(HorizontalAlignment.RIGHT);

        initializeMain();
        FormData formData = new FormData("100%"); //$NON-NLS-1$
        this.add(main, formData);

        final JournalSearchCriteria criteria = Registry.get(Journal.SEARCH_CRITERIA);
        RpcProxy<PagingLoadResult<JournalGridModel>> proxy = new RpcProxy<PagingLoadResult<JournalGridModel>>() {

            @Override
            protected void load(Object loadConfig, final AsyncCallback<PagingLoadResult<JournalGridModel>> callback) {
                localPagingLoadConfig = (PagingLoadConfig) loadConfig;

                service.getJournalList(criteria, BasePagingLoadConfigImpl.copyPagingLoad(localPagingLoadConfig),
                        new SessionAwareAsyncCallback<ItemBasePageLoadResult<JournalGridModel>>() {

                            @Override
                            public void onSuccess(ItemBasePageLoadResult<JournalGridModel> result) {
                                total = result.getTotalLength();
                                callback.onSuccess(new BasePagingLoadResult<JournalGridModel>(result.getData(), result
                                        .getOffset(), result.getTotalLength()));
                            }

                            @Override
                            protected void doOnFailure(Throwable caught) {
                                total = 0;
                                super.doOnFailure(caught);
                                callback.onSuccess(new BasePagingLoadResult<JournalGridModel>(new ArrayList<JournalGridModel>(),
                                        0, 0));
                            }
                        });
            }
        };

        localLoader = new BasePagingLoader<PagingLoadResult<JournalGridModel>>(proxy);
        localLoader.setRemoteSort(true);

        localLoader.addLoadListener(new LoadListener() {

            @Override
            public void loaderLoad(LoadEvent le) {
                currentDataList = ((BasePagingLoadResult<JournalGridModel>) le.getData()).getData();

                if (backupPage) {
                    backupPage = false;
                    return;
                }
                if (turnPage) {
                    JournalGridModel targetGridModel;
                    // here need to check current journal(iso prev/next one) has its data record exists(then open the
                    // journal), thus need to use adjusted index value
                    if (naviToPrevious) {
                        targetGridModel = getPrevJournalWithExistentRecord(currentDataList.size());
                    } else {
                        targetGridModel = getNextJournalWithExistentRecord(-1);
                    }

                    if (targetGridModel != null) {
                        JournalDataPanel.this.updateTabPanel(targetGridModel);
                        toggleOpenRecordButton(targetGridModel);
                    } else {
                        backupToPreviousPage();
                    }
                    turnPage = false;
                } else {// updateJournalNavigationList only called from here when the JournalDataPanel opened from
                        // gridPanel, other calls to this fuction is issued from update()
                    JournalSearchCriteria criteriaForPhysicalDeleted = new JournalSearchCriteria();
                    criteriaForPhysicalDeleted.setOperationType(UpdateReportPOJO.OPERATION_TYPE_PHYSICAL_DELETE);
                    PagingLoadConfig loadConfig = new BasePagingLoadConfig();
                    loadConfig.setOffset(0);
                    loadConfig.setLimit(0);
                    service.getJournalList(criteriaForPhysicalDeleted, BasePagingLoadConfigImpl.copyPagingLoad(loadConfig),
                            new SessionAwareAsyncCallback<ItemBasePageLoadResult<JournalGridModel>>() {

                                @Override
                                public void onSuccess(ItemBasePageLoadResult<JournalGridModel> result) {
                                    if (JournalDataPanel.this.deletedKeys == null) {
                                        JournalDataPanel.this.deletedKeys = new HashSet<String>();
                                        for (JournalGridModel model : result.getData()) {
                                            deletedKeys.add(model.getKey());
                                        }
                                    }
                                    updateJournalNavigationList();
                                }

                            });

                }
            }
        });

        localLoader.load(localPagingLoadConfig);
    }

    protected void toggleOpenRecordButton(JournalGridModel journalGridModel) {
        if (UpdateReportPOJO.OPERATION_TYPE_LOGICAL_DELETE.equals(journalGridModel.getOperationType())
                || UpdateReportPOJO.OPERATION_TYPE_PHYSICAL_DELETE.equals(journalGridModel.getOperationType())) {
            openRecordButton.disable();
        } else {
            openRecordButton.enable();
        }
    }

    private SelectionListener<ButtonEvent> createUpdateReportListener() {
        return new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                treeWindow.show();
                tree.setExpanded(JournalDataPanel.this.root, true);
            }
        };
    }

    private SelectionListener<ButtonEvent> createOpenRecordListener() {
        return new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                service.checkDCAndDM(journalGridModel.getDataContainer(), journalGridModel.getDataModel(),
                        new SessionAwareAsyncCallback<Boolean>() {

                            @Override
                            public void onSuccess(Boolean result) {
                                if (result) {
                                    if (journalGridModel.getDataContainer().endsWith("#STAGING")) { //$NON-NLS-1$
                                        JournalDataPanel.this.openBrowseRecordPanel4Staging(MessagesFactory.getMessages()
                                                .journal_label(), journalGridModel.getKey(), journalGridModel.getEntity());
                                    } else {
                                        JournalDataPanel.this.openBrowseRecordPanel(
                                                MessagesFactory.getMessages().journal_label(), journalGridModel.getKey(),
                                                journalGridModel.getEntity());
                                    }
                                } else {
                                    MessageBox.alert(MessagesFactory.getMessages().error_level(), MessagesFactory.getMessages()
                                            .select_contain_model_msg(), null);
                                }
                            }
                        });
            }
        };
    }

    private void initializeMain() {
        main = new LayoutContainer();
        main.setLayout(new ColumnLayout());

        LayoutContainer left = new LayoutContainer();
        left.setStyleAttribute("paddingRight", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
        FormLayout layout = new FormLayout();
        layout.setLabelAlign(LabelAlign.LEFT);
        layout.setLabelWidth(150);
        left.setWidth(350);
        left.setLayout(layout);

        entityField = new LabelField();
        entityField.setFieldLabel(MessagesFactory.getMessages().entity_label() + " : "); //$NON-NLS-1$     
        entityField.setValue(this.journalGridModel.getEntity());
        left.add(entityField);
        sourceField = new LabelField();
        sourceField.setFieldLabel(MessagesFactory.getMessages().source_label() + " : "); //$NON-NLS-1$
        sourceField.setValue(this.journalGridModel.getSource());
        left.add(sourceField);
        dataContainerField = new LabelField();
        dataContainerField.setFieldLabel(MessagesFactory.getMessages().data_container_label() + " : "); //$NON-NLS-1$
        dataContainerField.setValue(this.journalGridModel.getDataContainer());
        left.add(dataContainerField);
        dataModelField = new LabelField();
        dataModelField.setFieldLabel(MessagesFactory.getMessages().data_model_label() + " : "); //$NON-NLS-1$
        dataModelField.setValue(this.journalGridModel.getDataModel());
        left.add(dataModelField);

        LayoutContainer right = new LayoutContainer();
        right.setStyleAttribute("paddingLeft", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
        layout = new FormLayout();
        layout.setLabelAlign(LabelAlign.LEFT);
        layout.setLabelWidth(150);
        right.setWidth(350);
        right.setLayout(layout);

        keyField = new LabelField();
        keyField.setFieldLabel(MessagesFactory.getMessages().key_label() + " : "); //$NON-NLS-1$
        keyField.setValue(this.journalGridModel.getKey());
        right.add(keyField);
        operationTypeField = new LabelField();
        operationTypeField.setFieldLabel(MessagesFactory.getMessages().operation_type_label() + " : "); //$NON-NLS-1$
        operationTypeField.setValue(this.journalGridModel.getOperationType());
        right.add(operationTypeField);
        oeprationTimeField = new LabelField();
        oeprationTimeField.setFieldLabel(MessagesFactory.getMessages().operation_time_label() + " : "); //$NON-NLS-1$
        oeprationTimeField.setValue(this.journalGridModel.getOperationDate());
        right.add(oeprationTimeField);

        main.add(left, new ColumnData(.5));
        main.add(right, new ColumnData(.5));
    }

    private void initializeTreeWindow() {
        treeWindow = new Window();
        treeWindow.setHeading(MessagesFactory.getMessages().updatereport_label());
        treeWindow.setWidth(400);
        treeWindow.setHeight(450);
        treeWindow.setLayout(new FitLayout());
        treeWindow.setScrollMode(Scroll.NONE);

        treeStore = new TreeStore<JournalTreeModel>();
        treeStore.add(this.root, true);
        tree = new TreePanel<JournalTreeModel>(treeStore);
        tree.setDisplayProperty("name"); //$NON-NLS-1$
        tree.getStyle().setLeafIcon(AbstractImagePrototype.create(Icons.INSTANCE.leaf()));

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeaderVisible(false);
        contentPanel.setScrollMode(Scroll.AUTO);
        contentPanel.setLayout(new FitLayout());
        contentPanel.add(tree);
        treeWindow.add(contentPanel);
    }

    public TreePanel<JournalTreeModel> getTree() {
        return tree;
    }

    public JournalGridModel getJournalGridModel() {
        return this.journalGridModel;
    }

    public List<JournalGridModel> getJournalNavigateList() {
        return this.journalNavigateList;
    }

    public String getHeadingString() {
        return MessagesFactory.getMessages().data_change_viewer();
    }

    public void setJournalHistoryPanel(JournalHistoryPanel journalHistoryPanel) {
        this.journalHistoryPanel = journalHistoryPanel;
    }

    public void updateTabPanel(final JournalGridModel gridModel) {
        service.getDetailTreeModel(gridModel.getIds(), new SessionAwareAsyncCallback<JournalTreeModel>() {

            @Override
            public void onSuccess(final JournalTreeModel newRoot) {
                service.isEnterpriseVersion(new SessionAwareAsyncCallback<Boolean>() {

                    @Override
                    public void onSuccess(Boolean isEnterprise) {
                        if (GWT.isScript()) {
                            JournalDataPanel.this.updateGWTPanel(isEnterprise, gridModel, newRoot);
                        } else {
                            JournalDataPanel.this.updateDebugPanel(isEnterprise, gridModel, newRoot);
                        }
                    }
                });
            }
        });
    }

    private void updateGWTPanel(Boolean isEnterprise, JournalGridModel gridModel, JournalTreeModel newRoot) {
        this.update(gridModel, newRoot);
        if (isEnterprise) {
            this.setItemId(this.journalGridModel.getIds());
            this.journalHistoryPanel.update();
        }
    }

    private void updateDebugPanel(Boolean isEnterprise, JournalGridModel gridModel, JournalTreeModel newRoot) {
        this.update(gridModel, newRoot);
        this.journalHistoryPanel.update();
        if (isEnterprise) {
            Window window = new Window();
            window.setLayout(new FitLayout());
            window.add(this.journalHistoryPanel);
            window.setSize(1100, 700);
            window.setMaximizable(true);
            window.setModal(false);
            window.show();
        } else {
            Window window = new Window();
            window.setLayout(new FitLayout());
            window.add(this);
            window.setSize(1100, 700);
            window.setMaximizable(true);
            window.setModal(false);
            window.show();
        }
    }

    private void initLoadConfig() {
        PagingLoadConfig pagingLoadConfig = (PagingLoadConfig) gridStore.getLoadConfig();

        localPagingLoadConfig = new BasePagingLoadConfig();
        localPagingLoadConfig.setOffset(pagingLoadConfig.getOffset());
        localPagingLoadConfig.setLimit(pagingLoadConfig.getLimit());
    }

    private void backupToPreviousPage() {
        turnPage = false;
        backupPage = true;
        int curOffset = localPagingLoadConfig.getOffset();
        int limit = localPagingLoadConfig.getLimit();

        int nextOffSet = 0;
        if (naviToPrevious) {
            nextOffSet = curOffset + limit;
        } else {
            nextOffSet = curOffset - limit;
        }
        localPagingLoadConfig.setOffset(nextOffSet);
        localLoader.load(localPagingLoadConfig);
    }

    protected void updateJournalNavigationList() {
        this.turnPage = false;

        int currOffSet = localPagingLoadConfig.getOffset();
        int currLimit = localPagingLoadConfig.getLimit();

        int index = 0;
        for (int i = 0; i < currentDataList.size(); i++) {
            if (this.journalGridModel.getOperationTime().equals(currentDataList.get(i).getOperationTime())) {
                index = i;
                break;
            }
        }

        journalNavigateList = new ArrayList<JournalGridModel>(2);
        journalNavigateList.add(index == 0 ? null : getPrevJournalWithExistentRecord(index));
        journalNavigateList.add(index == currentDataList.size() - 1 ? null : getNextJournalWithExistentRecord(index));

        if (currOffSet == 0 && journalNavigateList.get(0) == null) {
            prevUpdateReportButton.setEnabled(false);
        } else {
            prevUpdateReportButton.setEnabled(true);
        }

        if ((currOffSet + currLimit) >= total && journalNavigateList.get(1) == null) {
            nextUpdateReportButton.setEnabled(false);
        } else {
            nextUpdateReportButton.setEnabled(true);
        }
    }

    private JournalGridModel getNextJournalWithExistentRecord(int startFrom) {
        int curOffset = localPagingLoadConfig.getOffset();
        int limit = localPagingLoadConfig.getLimit();
        int start = startFrom;
        String key;
        boolean exists = false;
        while (++start < limit && start < currentDataList.size() && start < total) {
            key = currentDataList.get(start).getKey();
            if (!deletedKeys.contains(key)) {
                exists = true;
                break;
            }
        }

        if (!exists && (curOffset + limit) >= total) {
            nextUpdateReportButton.setEnabled(false);
        }
        return !exists ? null : currentDataList.get(start);
    }

    private JournalGridModel getPrevJournalWithExistentRecord(int startFrom) {
        int curOffset = localPagingLoadConfig.getOffset();
        int start = startFrom;
        String key;
        boolean exists = false;
        while (--start >= 0) {
            key = currentDataList.get(start).getKey();
            if (!deletedKeys.contains(key)) {
                exists = true;
                break;
            }
        }

        if (!exists && curOffset == 0) {
            prevUpdateReportButton.setEnabled(false);
        }
        return !exists ? null : currentDataList.get(start);
    }

    private void retrieveNeighbourJournalInOtherPages() {
        this.turnPage = true;
        int curOffset = localPagingLoadConfig.getOffset();
        int limit = localPagingLoadConfig.getLimit();
        int nextOffSet = 0;
        if (naviToPrevious) {
            nextOffSet = (curOffset >= limit) ? (curOffset - limit) : curOffset;
            if (nextOffSet == curOffset) {
                prevUpdateReportButton.setEnabled(false);
                return;
            }
        } else {
            nextOffSet = ((curOffset + limit) <= total) ? (curOffset + limit) : curOffset;
            if (nextOffSet == curOffset) {
                nextUpdateReportButton.setEnabled(false);
                return;
            }
        }
        localPagingLoadConfig.setOffset(nextOffSet);
        localLoader.load(localPagingLoadConfig);
    }

    private void update(JournalGridModel gridModel, JournalTreeModel newRoot) {
        this.journalGridModel = gridModel;
        this.root = newRoot;

        updateUpdateReport();

        updateMainFieldValues(gridModel);

        updateOpenRecord();

        updateJournalNavigationList();

        this.layout();
    }

    private void updateUpdateReport() {
        treeStore.removeAll();
        treeStore.add(this.root, true);
        updateUpdateReportListener();
    }

    private void updateOpenRecord() {
        this.openRecordButton.removeSelectionListener(openRecordListener);
        this.openRecordListener = createOpenRecordListener();
        this.openRecordButton.addSelectionListener(openRecordListener);
    }

    private void updateUpdateReportListener() {
        this.viewUpdateReportButton.removeSelectionListener(this.updateReportListener);
        this.updateReportListener = createUpdateReportListener();
        this.viewUpdateReportButton.addSelectionListener(this.updateReportListener);
    }

    private void updateMainFieldValues(JournalGridModel gridModel) {
        this.entityField.setValue(gridModel.getEntity());
        this.sourceField.setValue(gridModel.getSource());
        this.dataContainerField.setValue(gridModel.getDataContainer());
        this.dataModelField.setValue(gridModel.getDataModel());
        this.keyField.setValue(gridModel.getKey());
        this.operationTypeField.setValue(gridModel.getOperationType());
        this.oeprationTimeField.setValue(gridModel.getOperationDate());
    }

    private native void openBrowseRecordPanel(String title, String key, String concept)/*-{
		var arr = key.split("\.");
		$wnd.amalto.itemsbrowser.ItemsBrowser.editItemDetails(title, arr,
				concept, function() {
				});
    }-*/;

    private native void openBrowseRecordPanel4Staging(String title, String key, String concept)/*-{
		var arr = key.split("\.");
		$wnd.amalto.itemsbrowser.ItemsBrowser.editItemDetails4Staging(title,
				arr, concept, function() {
				});
    }-*/;
}