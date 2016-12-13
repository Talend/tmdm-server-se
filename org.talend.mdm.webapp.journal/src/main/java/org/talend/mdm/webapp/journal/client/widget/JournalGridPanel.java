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
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.i18n.BaseMessagesFactory;
import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.client.widget.PagingToolBarEx;
import org.talend.mdm.webapp.journal.client.Journal;
import org.talend.mdm.webapp.journal.client.JournalServiceAsync;
import org.talend.mdm.webapp.journal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.journal.client.resources.icon.Icons;
import org.talend.mdm.webapp.journal.client.util.JournalSearchUtil;
import org.talend.mdm.webapp.journal.shared.JournalGridModel;
import org.talend.mdm.webapp.journal.shared.JournalParameters;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;
import org.talend.mdm.webapp.journal.shared.JournalTreeModel;

import com.amalto.core.objects.UpdateReportPOJO;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.state.StateManager;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class JournalGridPanel extends ContentPanel {

    private static JournalGridPanel instance;

    private JournalServiceAsync service = Registry.get(Journal.JOURNAL_SERVICE);

    private Grid<JournalGridModel> grid;

    private ListStore<JournalGridModel> store;

    private PagingToolBarEx pagetoolBar;

    private PagingLoader<PagingLoadResult<JournalGridModel>> loader;

    private final static int PAGE_SIZE = 20;

    private PagingLoadConfig pagingLoadConfig;

    private final String BEFORE_ACTION = "before"; //$NON-NLS-1$

    final JournalSearchCriteria criteria;
    
    public static JournalGridPanel getInstance() {
        if (instance == null) {
            instance = new JournalGridPanel();
        }
        return instance;
    }

    private JournalGridPanel() {
        this.setLayout(new FitLayout());
        this.setBodyBorder(false);
        this.setFrame(false);
        this.setHeaderVisible(false);

        List<ColumnConfig> ccList = new ArrayList<ColumnConfig>();
        ColumnConfig ccDataContainer = new ColumnConfig();
        ccDataContainer.setId("dataContainer"); //$NON-NLS-1$
        ccDataContainer.setHeader(MessagesFactory.getMessages().data_container_label());
        ccDataContainer.setWidth(120);
        ccList.add(ccDataContainer);

        ColumnConfig ccDataModel = new ColumnConfig();
        ccDataModel.setId("dataModel"); //$NON-NLS-1$
        ccDataModel.setHeader(MessagesFactory.getMessages().data_model_label());
        ccDataModel.setWidth(120);
        ccList.add(ccDataModel);

        ColumnConfig ccEntity = new ColumnConfig();
        ccEntity.setId("entity"); //$NON-NLS-1$
        ccEntity.setHeader(MessagesFactory.getMessages().entity_label());
        ccEntity.setWidth(120);
        ccList.add(ccEntity);

        ColumnConfig ccKey = new ColumnConfig();
        ccKey.setId("key"); //$NON-NLS-1$
        ccKey.setHeader(MessagesFactory.getMessages().key_label());
        ccKey.setWidth(120);
        ccList.add(ccKey);

        ColumnConfig ccOperationType = new ColumnConfig();
        ccOperationType.setId("operationType"); //$NON-NLS-1$
        ccOperationType.setHeader(MessagesFactory.getMessages().operation_type_label());
        ccOperationType.setWidth(120);
        ccList.add(ccOperationType);

        ColumnConfig ccOperationTime = new ColumnConfig();
        ccOperationTime.setId("operationTime"); //$NON-NLS-1$
        ccOperationTime.setHeader(MessagesFactory.getMessages().operation_time_label());
        ccOperationTime.setWidth(120);
        ccList.add(ccOperationTime);

        ColumnConfig ccSource = new ColumnConfig();
        ccSource.setId("source"); //$NON-NLS-1$
        ccSource.setHeader(MessagesFactory.getMessages().source_label());
        ccSource.setWidth(120);
        ccList.add(ccSource);

        ColumnConfig ccUserName = new ColumnConfig();
        ccUserName.setId("userName"); //$NON-NLS-1$
        ccUserName.setHeader(MessagesFactory.getMessages().user_name_label());
        ccUserName.setWidth(120);
        ccList.add(ccUserName);

        criteria = Registry.get(Journal.SEARCH_CRITERIA);
        RpcProxy<PagingLoadResult<JournalGridModel>> proxy = new RpcProxy<PagingLoadResult<JournalGridModel>>() {

            @Override
            protected void load(Object loadConfig, final AsyncCallback<PagingLoadResult<JournalGridModel>> callback) {
                pagingLoadConfig = (PagingLoadConfig) loadConfig;
                pagingLoadConfig.setLimit(pagetoolBar.getPageSize());
                service.getJournalList(criteria, BasePagingLoadConfigImpl.copyPagingLoad(pagingLoadConfig),
                        new SessionAwareAsyncCallback<ItemBasePageLoadResult<JournalGridModel>>() {

                            @Override
                            public void onSuccess(ItemBasePageLoadResult<JournalGridModel> result) {
                                callback.onSuccess(new BasePagingLoadResult<JournalGridModel>(result.getData(), result
                                        .getOffset(), result.getTotalLength()));
                            }

                            @Override
                            protected void doOnFailure(Throwable caught) {
                                super.doOnFailure(caught);
                                callback.onSuccess(new BasePagingLoadResult<JournalGridModel>(new ArrayList<JournalGridModel>(),
                                        0, 0));
                            }
                        });
            }
        };

        loader = new BasePagingLoader<PagingLoadResult<JournalGridModel>>(proxy);
        loader.setRemoteSort(true);

        store = new ListStore<JournalGridModel>(loader);
        grid = new Grid<JournalGridModel>(store, new ColumnModel(ccList));
        grid.getView().setAutoFill(true);
        grid.getView().setForceFit(true);
        int usePageSize = PAGE_SIZE;
        if (StateManager.get().get("journalgrid") != null) { //$NON-NLS-1$
            usePageSize = Integer.valueOf(((Map<?, ?>) StateManager.get().get("journalgrid")).get("limit").toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        pagetoolBar = new PagingToolBarEx(usePageSize);
        pagetoolBar.bind(loader);
        grid.setLoadMask(true);
        grid.setStateful(true);
        grid.setStateId("journalgrid");//$NON-NLS-1$
        grid.addListener(Events.Attach, new Listener<GridEvent<JournalGridModel>>() {

            @Override
            public void handleEvent(GridEvent<JournalGridModel> be) {
                PagingLoadConfig config = new BasePagingLoadConfig();
                config.setOffset(0);
                int pageSize = pagetoolBar.getPageSize();
                config.setLimit(pageSize);
                loader.load(config);
            }
        });

        GridSelectionModel<JournalGridModel> sm = new GridSelectionModel<JournalGridModel>();
        sm.setSelectionMode(SelectionMode.SINGLE);
        grid.setSelectionModel(sm);

        grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<JournalGridModel>>() {

            @Override
            public void handleEvent(GridEvent<JournalGridModel> be) {
                final JournalGridModel gridModel = be.getModel();
                service.isJournalHistoryExist(JournalSearchUtil.buildParameter(gridModel, BEFORE_ACTION, true),
                        new SessionAwareAsyncCallback<Boolean>() {

                            @Override
                            public void onSuccess(Boolean result) {
                                if (result) {
                                    JournalGridPanel.this.openTabPanel(gridModel);
                                }
                            }
                        });
            }
        });

        addContextMenu();
        this.add(grid);
        this.setBottomComponent(pagetoolBar);
    }

    public void refreshGrid() {
        pagetoolBar.first();
    }

    public void lastPage() {
        pagetoolBar.last();
    }

    public ListStore<JournalGridModel> getStore() {
        return this.store;
    }

    public void openTabPanel(final JournalGridModel gridModel) {
        service.getDetailTreeModel(gridModel.getIds(), new SessionAwareAsyncCallback<JournalTreeModel>() {

            @Override
            public void onSuccess(final JournalTreeModel root) {
                service.isEnterpriseVersion(new SessionAwareAsyncCallback<Boolean>() {

                    @Override
                    public void onSuccess(Boolean isEnterprise) {
                        if (GWT.isScript()) {
                            JournalGridPanel.this.openGWTPanel(isEnterprise, gridModel, root);
                        } else {
                            JournalGridPanel.this.openDebugPanel(isEnterprise, gridModel, root);
                        }
                    }
                });
            }
        });
    }

    private native void openHistoryTabPanel(String ids, JournalHistoryPanel source)/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		var dataLogViewer = tabPanel.getItem(ids);
		if (dataLogViewer == undefined) {
			var panel = @org.talend.mdm.webapp.journal.client.widget.JournalGridPanel::convertHistoryPanel(Lorg/talend/mdm/webapp/journal/client/widget/JournalHistoryPanel;)(source);
			tabPanel.add(panel);
		}
		tabPanel.setSelection(ids);
    }-*/;

    private native void openDataTabPanel(String ids, JournalDataPanel source)/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		var dataLogViewer = tabPanel.getItem(ids);
		if (dataLogViewer == undefined) {
			var panel = @org.talend.mdm.webapp.journal.client.widget.JournalGridPanel::convertDataPanel(Lorg/talend/mdm/webapp/journal/client/widget/JournalDataPanel;)(source);
			tabPanel.add(panel);
		}
		tabPanel.setSelection(ids);
    }-*/;

    private native static JavaScriptObject convertHistoryPanel(JournalHistoryPanel journalPanel)/*-{
		var panel = {
			// imitate extjs's render method, really call gxt code.
			render : function(el) {
				var rootPanel = @com.google.gwt.user.client.ui.RootPanel::get(Ljava/lang/String;)(el.id);
				rootPanel.@com.google.gwt.user.client.ui.RootPanel::add(Lcom/google/gwt/user/client/ui/Widget;)(journalPanel);
			},
			// imitate extjs's setSize method, really call gxt code.
			setSize : function(width, height) {
				journalPanel.@org.talend.mdm.webapp.journal.client.widget.JournalHistoryPanel::setSize(II)(width, height);
			},
			// imitate extjs's getItemId, really return itemId of ContentPanel of GXT.
			getItemId : function() {
				return journalPanel.@org.talend.mdm.webapp.journal.client.widget.JournalHistoryPanel::getItemId()();
			},
			// imitate El object of extjs
			getEl : function() {
				var el = journalPanel.@org.talend.mdm.webapp.journal.client.widget.JournalHistoryPanel::getElement()();
				return {
					dom : el
				};
			},
			// imitate extjs's doLayout method, really call gxt code.
			doLayout : function() {
				return journalPanel.@org.talend.mdm.webapp.journal.client.widget.JournalHistoryPanel::doLayout()();
			},
			title : function() {
				return journalPanel.@org.talend.mdm.webapp.journal.client.widget.JournalHistoryPanel::getHeading()();
			}
		};
		return panel;
    }-*/;

    private native static JavaScriptObject convertDataPanel(JournalDataPanel journalPanel)/*-{
		var panel = {
			// imitate extjs's render method, really call gxt code.
			render : function(el) {
				var rootPanel = @com.google.gwt.user.client.ui.RootPanel::get(Ljava/lang/String;)(el.id);
				rootPanel.@com.google.gwt.user.client.ui.RootPanel::add(Lcom/google/gwt/user/client/ui/Widget;)(journalPanel);
			},
			// imitate extjs's setSize method, really call gxt code.
			setSize : function(width, height) {
				journalPanel.@org.talend.mdm.webapp.journal.client.widget.JournalDataPanel::setSize(II)(width, height);
			},
			// imitate extjs's getItemId, really return itemId of ContentPanel of GXT.
			getItemId : function() {
				return journalPanel.@org.talend.mdm.webapp.journal.client.widget.JournalDataPanel::getItemId()();
			},
			// imitate El object of extjs
			getEl : function() {
				var el = journalPanel.@org.talend.mdm.webapp.journal.client.widget.JournalDataPanel::getElement()();
				return {
					dom : el
				};
			},
			// imitate extjs's doLayout method, really call gxt code.
			doLayout : function() {
				return journalPanel.@org.talend.mdm.webapp.journal.client.widget.JournalDataPanel::doLayout()();
			},
			title : function() {
				return journalPanel.@org.talend.mdm.webapp.journal.client.widget.JournalDataPanel::getHeadingString()();
			}
		};
		return panel;
    }-*/;

    private void openDebugPanel(Boolean isEnterprise, JournalGridModel gridModel, JournalTreeModel root) {
        if (isEnterprise) {
            JournalHistoryPanel journalHistoryPanel = new JournalHistoryPanel(root, gridModel, criteria, root.isAuth(), 550);
            Window window = new Window();
            window.setLayout(new FitLayout());
            window.add(journalHistoryPanel);
            window.setSize(1100, 700);
            window.setMaximizable(true);
            window.setModal(false);
            window.show();
            journalHistoryPanel.getJournalDataPanel().getTree().setExpanded(root, true);
        } else {
            JournalDataPanel journalDataPanel = new JournalDataPanel(root, gridModel);
            Window window = new Window();
            window.setLayout(new FitLayout());
            window.add(journalDataPanel);
            window.setSize(1100, 700);
            window.setMaximizable(true);
            window.setModal(false);
            window.show();
            journalDataPanel.getTree().setExpanded(root, true);
        }
    }

    private void openGWTPanel(Boolean isEnterprise, JournalGridModel gridModel, JournalTreeModel root) {
        if (isEnterprise) {
            int width = JournalTabPanel.getInstance().getWidth() / 2 - 2;
            JournalHistoryPanel journalHistoryPanel = new JournalHistoryPanel(root, gridModel, criteria, root.isAuth(), width);
            this.openHistoryTabPanel(gridModel.getIds().concat(criteria.toString()), journalHistoryPanel);
            journalHistoryPanel.getJournalDataPanel().getTree().setExpanded(root, true);
        } else {
            JournalDataPanel journalDataPanel = new JournalDataPanel(root, gridModel);
            this.openDataTabPanel(gridModel.getIds(), journalDataPanel);
            journalDataPanel.getTree().setExpanded(root, true);
        }
    }

    private void addContextMenu() {
        final Menu contextMenu = new Menu();
        final MenuItem viewChagesMenuItem = new MenuItem();
        viewChagesMenuItem.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.view()));
        viewChagesMenuItem.setText(MessagesFactory.getMessages().menu_item_viewchages());
        viewChagesMenuItem.setEnabled(true);
        viewChagesMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                JournalGridPanel.this.openTabPanel(grid.getSelectionModel().getSelectedItem());
            }
        });
        contextMenu.add(viewChagesMenuItem);

        service.isEnterpriseVersion(new SessionAwareAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    final MenuItem restoreMenuItem = new MenuItem();
                    restoreMenuItem.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.restore()));
                    restoreMenuItem.setText(MessagesFactory.getMessages().restore_button());
                    restoreMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

                        @Override
                        public void componentSelected(MenuEvent ce) {
                            JournalGridPanel.this.restore(JournalSearchUtil.buildParameter(grid.getSelectionModel()
                                    .getSelectedItem(), BEFORE_ACTION, true), false);
                        }
                    });
                    contextMenu.add(restoreMenuItem);
                    grid.addListener(Events.ContextMenu, new Listener<GridEvent<JournalGridModel>>() {

                        @Override
                        public void handleEvent(GridEvent<JournalGridModel> be) {
                            final JournalGridModel gridModel = be.getModel();

                            service.isAdmin(new SessionAwareAsyncCallback<Boolean>() {

                                @Override
                                public void onSuccess(Boolean isAdmin) {
                                    if (isAdmin
                                            && (UpdateReportPOJO.OPERATION_TYPE_UPDATE.equals(gridModel.getOperationType()) || UpdateReportPOJO.OPERATION_TYPE_LOGICAL_DELETE
                                                    .equals(gridModel.getOperationType()))) {
                                        restoreMenuItem.setEnabled(true);
                                    } else {
                                        restoreMenuItem.setEnabled(false);
                                    }

                                    service.isJournalHistoryExist(
                                            JournalSearchUtil.buildParameter(gridModel, BEFORE_ACTION, true),
                                            new SessionAwareAsyncCallback<Boolean>() {

                                                @Override
                                                public void onSuccess(Boolean result) {
                                                    if (result) {
                                                        viewChagesMenuItem.setEnabled(true);
                                                    } else {
                                                        viewChagesMenuItem.setEnabled(false);
                                                        restoreMenuItem.setEnabled(false);
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    });
                }
            }
        });
        grid.setContextMenu(contextMenu);
    }

    public String getLoaderConfigStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(","); //$NON-NLS-1$
        sb.append(loader.getLimit()).append(","); //$NON-NLS-1$
        sb.append(loader.getSortDir().toString()).append(","); //$NON-NLS-1$
        sb.append(loader.getSortField()).append(","); //$NON-NLS-1$
        sb.append(UrlUtil.getLanguage());
        return sb.toString();
    }

    public int getOffset() {
        return loader.getOffset();
    }

    public int getLimit() {
        return loader.getLimit();
    }

    public void restore(final JournalParameters parameter, final boolean isCloseTabPanel) {
        MessageBox.confirm(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages().restore_confirm(),
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                            if (UpdateReportPOJO.OPERATION_TYPE_LOGICAL_DELETE.equals(parameter.getOperationType())) {
                                service.checkConflict(parameter.getDataClusterName(), parameter.getConceptName(),
                                        parameter.getId()[0], new SessionAwareAsyncCallback<Boolean>() {

                                            @Override
                                            public void onSuccess(Boolean result) {
                                                if (result) {
                                                    MessageBox.confirm(BaseMessagesFactory.getMessages().confirm_title(),
                                                            BaseMessagesFactory.getMessages().overwrite_confirm(),
                                                            new Listener<MessageBoxEvent>() {

                                                                @Override
                                                                public void handleEvent(MessageBoxEvent be) {
                                                                    if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                                                                        restoreRecord(parameter, isCloseTabPanel);
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    restoreRecord(parameter, isCloseTabPanel);
                                                }
                                            }
                                        });
                            } else {
                                restoreRecord(parameter, isCloseTabPanel);
                            }
                        }
                    }
                });
    }

    private void restoreRecord(final JournalParameters parameter, final boolean isCloseTabPanel) {
        service.restoreRecord(parameter, UrlUtil.getLanguage(), new SessionAwareAsyncCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                MessageBox.info(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages().restore_success(),
                        new Listener<MessageBoxEvent>() {

                            @Override
                            public void handleEvent(MessageBoxEvent be) {
                                if (be.getButtonClicked().getItemId().equals(Dialog.OK)) {
                                    if (isCloseTabPanel) {
                                        closeTabPanel();
                                    }
                                    lastPage();
                                }
                            }
                        });
            }

            @Override
            protected void doOnFailure(Throwable caught) {
                MessageBox.alert(MessagesFactory.getMessages().error_level(), caught.getMessage(),
                        new Listener<MessageBoxEvent>() {

                            @Override
                            public void handleEvent(MessageBoxEvent be) {
                                if (be.getButtonClicked().getItemId().equals(Dialog.OK) && isCloseTabPanel) {
                                    closeTabPanel();
                                }
                            }
                        });
            }
        });
    }

    private native void closeTabPanel()/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		tabPanel.closeCurrentTab();
    }-*/;
}