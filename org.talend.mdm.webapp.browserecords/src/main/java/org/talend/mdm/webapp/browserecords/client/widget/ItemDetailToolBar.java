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
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsEvents;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.ServiceFactory;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.BreadCrumbModel;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.mvc.BrowseRecordsView;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.rest.ExplainRestServiceHandler;
import org.talend.mdm.webapp.browserecords.client.rest.ValidationRestServiceHandler;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.util.ViewUtil;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ReturnCriteriaFK;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.ComboBoxField;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.CloseLineageTabPostDeleteAction;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.CloseTabPostDeleteAction;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.ContainerUpdate;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.DeleteAction;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.DeleteCallback;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.ListRefresh;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.LogicalDeleteAction;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.NoOpPostDeleteAction;
import org.talend.mdm.webapp.browserecords.client.widget.integrity.PostDeleteAction;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.ForeignKeyTreeDetail;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailUtil;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonGroup;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.ToolBarLayout;
import com.extjs.gxt.ui.client.widget.menu.HeaderMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;

public class ItemDetailToolBar extends ToolBar {

    public final static String CREATE_OPERATION = "CREATE"; //$NON-NLS-1$

    public final static String VIEW_OPERATION = "VIEW"; //$NON-NLS-1$

    public final static String SMARTVIEW_OPERATION = "SMARTVIEW"; //$NON-NLS-1$

    public final static String PERSONALEVIEW_OPERATION = "PERSONALVIEW"; //$NON-NLS-1$

    public final static String DUPLICATE_OPERATION = "DUPLICATE_OPERATION"; //$NON-NLS-1$

    public final static String BULK_UPDATE_OPERATION = "BULK_UPDATE_OPERATION"; //$NON-NLS-1$

    public final static int TYPE_DEFAULT = 0;

    public final static int TYPE_CREATE_FOREIGNKEY_ENTITY = 3;

    private Button saveButton;

    private Button saveAndCloseButton;

    private Button cancelButton;

    protected Button deleteButton;

    private Button relationButton;

    private Button personalviewButton;

    private Button generatedviewButton;

    private Button refreshButton;

    private Button moreActionsButton;

    private Menu subActionsMenu;

    private int separatorIndex;

    private MenuItem openTabMenuItem;

    private MenuItem duplicateMenuItem;

    private MenuItem validateMenuItem;

    private MenuItem journalMenuItem;

    private MenuItem navigatorMenuItem;

    private MenuItem dataLineageMenuItem;

    private MenuItem openMasterRecordMenuItem;

    private MenuItem openTaskMenuItem;

    private MenuItem explainMenuItem;

    private MenuItem relationMenuItem;

    private Button launchProcessButton;

    private ComboBoxField<ItemBaseModel> smartViewCombo;

    private ComboBoxField<ItemBaseModel> workFlowCombo;

    protected ItemBean itemBean;

    protected ViewBean viewBean;

    protected String operation;

    protected boolean isFkToolBar;

    private ItemBaseModel selectItem;

    private Menu deleteMenu;

    private MenuItem delete_SendToTrash;

    private MenuItem delete_Delete;

    protected ItemsDetailPanel itemsDetailPanel;

    protected boolean isOutMost;

    protected boolean isHierarchyCall;

    protected boolean openTab;

    private boolean isStaging;

    private int viewCode;

    private ReturnCriteriaFK returnCriteriaFK;

    private int type = TYPE_DEFAULT;

    public ItemDetailToolBar() {
        this.setBorders(false);
        this.setLayout(new ToolBarExLayout());
    }

    public ItemDetailToolBar(ItemsDetailPanel itemsDetailPanel) {
        this();
        this.itemsDetailPanel = itemsDetailPanel;
    }

    public ItemDetailToolBar(ItemBean itemBean, String operation, ViewBean viewBean, ItemsDetailPanel itemsDetailPanel) {
        this(itemsDetailPanel);
        this.itemBean = itemBean;
        this.operation = operation;
        this.viewBean = viewBean;
        initToolBar();
    }

    public ItemDetailToolBar(ItemBean itemBean, String operation, ViewBean viewBean, ItemsDetailPanel itemsDetailPanel,
            boolean openTab) {
        this(itemsDetailPanel);
        this.itemBean = itemBean;
        this.operation = operation;
        this.viewBean = viewBean;
        this.openTab = openTab;
        initToolBar();
    }

    public ItemDetailToolBar(ItemBean itemBean, String operation, boolean isFkToolBar, ViewBean viewBean,
            ItemsDetailPanel itemsDetailPanel) {
        this(itemsDetailPanel);
        this.itemBean = itemBean;
        this.operation = operation;
        this.isFkToolBar = isFkToolBar;
        this.viewBean = viewBean;
        initToolBar();
    }

    public ItemDetailToolBar(boolean isStaging, ItemsDetailPanel itemsDetailPanel) {
        this();
        this.isStaging = isStaging;
        this.itemsDetailPanel = itemsDetailPanel;
    }

    public ItemDetailToolBar(boolean isStaging, ItemBean itemBean, String operation, ViewBean viewBean,
            ItemsDetailPanel itemsDetailPanel) {
        this(isStaging, itemsDetailPanel);
        this.itemBean = itemBean;
        this.operation = operation;
        this.viewBean = viewBean;
        initToolBar();
    }

    public ItemDetailToolBar(boolean isStaging, ItemBean itemBean, String operation, boolean isFkToolBar, ViewBean viewBean,
            ItemsDetailPanel itemsDetailPanel, boolean openTab) {
        this(isStaging, itemsDetailPanel);
        this.itemBean = itemBean;
        this.operation = operation;
        this.isFkToolBar = isFkToolBar;
        this.viewBean = viewBean;
        this.openTab = openTab;
        initToolBar();
    }

    protected void checkEntitlement(ViewBean viewBean) {
        if (deleteButton == null) {
            return;
        }
        String concept = this.itemBean.getConcept();
        boolean denyLogicalDelete = viewBean.getBindingEntityModel().getMetaDataTypes().get(concept).isDenyLogicalDeletable();
        boolean denyPhysicalDelete = viewBean.getBindingEntityModel().getMetaDataTypes().get(concept).isDenyPhysicalDeleteable();

        if (denyLogicalDelete && denyPhysicalDelete) {
            deleteButton.setEnabled(false);
        } else {
            deleteButton.setEnabled(true);
            if (delete_SendToTrash != null && delete_Delete != null) {
                if (denyLogicalDelete) {
                    delete_SendToTrash.setEnabled(false);
                }
                if (denyPhysicalDelete) {
                    delete_Delete.setEnabled(false);
                }
            }
        }
    }

    protected void initToolBar() {
        this.addStyleName("ItemDetailToolBar"); //$NON-NLS-1$       
        if (operation.equalsIgnoreCase(ItemDetailToolBar.VIEW_OPERATION)
                || operation.equalsIgnoreCase(ItemDetailToolBar.PERSONALEVIEW_OPERATION)) {
            initViewToolBar();
        } else if (operation.equalsIgnoreCase(ItemDetailToolBar.CREATE_OPERATION)) {
            initCreateToolBar();
        } else if (operation.equalsIgnoreCase(ItemDetailToolBar.SMARTVIEW_OPERATION)) {
            initSmartViewToolBar();
        } else if (operation.equalsIgnoreCase(ItemDetailToolBar.DUPLICATE_OPERATION)) {
            initCreateToolBar();
        } else if (operation.equalsIgnoreCase(ItemDetailToolBar.BULK_UPDATE_OPERATION)) {
            initMassUpdateToolBar();
        }
    }

    protected void initViewToolBar() {
        if (!operation.equalsIgnoreCase(ItemDetailToolBar.VIEW_OPERATION)) {
            addPersonalViewButton();
            addSeparator();
        }
        addSaveButton();
        addSeparator();
        addSaveQuitButton();
        addSeparator();
        addDeleteButton();
        addSeparator();
        addFreshButton();
        addSeparator();
        addMoreActionsButton(false);
        addSeparator();
        if (!isStaging) {
            addWorkFlosCombo();
        }
        checkEntitlement(viewBean);
    }

    protected void initCreateToolBar() {
        this.addSaveButton();
        this.addSeparator();
        this.addSaveQuitButton();
        if (!isStaging) {
            if (isUseRelations()) {
                this.addRelationButton();
            }
            this.addWorkFlosCombo();
        }
    }

    protected void initMassUpdateToolBar() {
        this.addSaveAndCloseButtonOfBulkUpdate();
        this.addSeparator();
        this.addCanelButtonOfMassUpdate();
    }

    protected boolean isUseRelations() {
        boolean isUseRelations = false;
        if (BrowseRecords.getSession().getAppHeader() != null) {
            isUseRelations = BrowseRecords.getSession().getAppHeader().isUseRelations();
        }
        return isUseRelations;
    }

    /**
     * call it only when save the foreignKey in primaryKey view or save the outMost entity
     */
    public void refresh(String ids) {
        if (this.operation.equals(CREATE_OPERATION) || this.operation.equals(DUPLICATE_OPERATION)
                || this.operation.equals(VIEW_OPERATION)) {
            this.removeAll();
            final boolean isClearBreadCrumb = this.operation.equals(CREATE_OPERATION)
                    || this.operation.equals(DUPLICATE_OPERATION);
            this.operation = VIEW_OPERATION;
            if (!this.isOutMost) {
                this.openTab = true;
            }
            getBrowseRecordsService().getForeignKeyModel(itemBean.getConcept(), ids, isStaging, Locale.getLanguage(),
                    new SessionAwareAsyncCallback<ForeignKeyModel>() {

                        @Override
                        public void onSuccess(ForeignKeyModel model) {
                            itemBean = model.getItemBean();
                            // refresh toolBar
                            initViewToolBar();
                            // refresh tree
                            ItemPanel itemPanel = (ItemPanel) itemsDetailPanel.getFirstTabWidget();
                            itemPanel.setItem(itemBean);
                            itemPanel.refreshTree();
                            // refresh itemsDetailPanel(include tab title, banner, breadCrumb)
                            TypeModel typeModel = viewBean.getBindingEntityModel().getMetaDataTypes().get(itemBean.getConcept());
                            String tabText = typeModel.getLabel(Locale.getLanguage()) + " " + itemBean.getIds(); //$NON-NLS-1$
                            if (isClearBreadCrumb) {
                                if (!itemsDetailPanel.isLineage()) {
                                    if (!ItemDetailToolBar.this.isOutMost) {
                                        ItemsMainTabPanel.getInstance().getSelectedItem().setText(tabText);
                                    } else {
                                        updateOutTabPanel(tabText);
                                    }
                                }
                                itemsDetailPanel.clearBreadCrumb();
                                List<BreadCrumbModel> breads = new ArrayList<BreadCrumbModel>();
                                breads.add(new BreadCrumbModel("", BreadCrumb.DEFAULTNAME, null, null, false)); //$NON-NLS-1$
                                breads.add(new BreadCrumbModel(itemBean.getConcept(), itemBean.getLabel(), itemBean.getIds(),
                                        itemBean.getDisplayPKInfo().equals(itemBean.getLabel()) ? null : itemBean
                                                .getDisplayPKInfo(), true));
                                itemsDetailPanel.initBreadCrumb(new BreadCrumb(breads, itemsDetailPanel));
                            }
                            itemsDetailPanel.clearBanner();
                            itemsDetailPanel.initBanner(itemBean.getPkInfoList(), itemBean.getDescription());
                        };
                    });
        }
    }

    protected void addSaveButton() {
        if (saveButton == null) {
            saveButton = new Button(MessagesFactory.getMessages().save_btn());
            saveButton.setId("saveButton"); //$NON-NLS-1$
            saveButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Save()));
            saveButton.setToolTip(MessagesFactory.getMessages().save_tip());
            saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    executeSaveAction(false);
                }
            });
        }
        add(saveButton);
    }

    protected void addSaveQuitButton() {
        if (saveAndCloseButton == null) {
            saveAndCloseButton = new Button(MessagesFactory.getMessages().save_close_btn());
            saveAndCloseButton.setId("saveAndCloseButton"); //$NON-NLS-1$
            saveAndCloseButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.save_and_close()));
            saveAndCloseButton.setToolTip(MessagesFactory.getMessages().save_close_tip());

            saveAndCloseButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    executeSaveAction(true);
                }
            });
        }
        add(saveAndCloseButton);
    }

    protected void addDeleteButton() {
        if (deleteButton == null) {
            if (isStaging) {
                deleteButton = new Button(MessagesFactory.getMessages().mark_as_deleted());
                deleteButton.setId("deleteButton"); //$NON-NLS-1$
                deleteButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Delete()));

                deleteButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        MessageBox.confirm(MessagesFactory.getMessages().confirm_title(), MessagesFactory.getMessages()
                                .mark_deleted_confirm(), new Listener<MessageBoxEvent>() {

                            @Override
                            public void handleEvent(MessageBoxEvent be) {
                                if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                                    deleteRecord();
                                }
                            }
                        });
                    }
                });
            } else {
                deleteButton = new Button(MessagesFactory.getMessages().delete_btn());
                deleteButton.setId("deleteButton"); //$NON-NLS-1$
                deleteButton.setToolTip(MessagesFactory.getMessages().delete_tip());
                deleteButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Delete()));
                addDeleteMenu();
            }
        }
        add(deleteButton);
    }

    private void addDeleteMenu() {
        deleteMenu = new Menu();
        deleteMenu.setId("deleteMenu"); //$NON-NLS-1$
        delete_SendToTrash = new MenuItem(MessagesFactory.getMessages().trash_btn());
        delete_SendToTrash.setId("delete_SendToTrash"); //$NON-NLS-1$
        delete_SendToTrash.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                PostDeleteAction postDeleteAction = new CloseTabPostDeleteAction(ItemDetailToolBar.this, new ListRefresh(
                        ItemDetailToolBar.this, new ContainerUpdate(ItemDetailToolBar.this, NoOpPostDeleteAction.INSTANCE)));
                DeleteAction deleteAction = new LogicalDeleteAction("/"); //$NON-NLS-1$
                // Collections.singletonList(itemBean) --- it could not be sent to backend correctly
                List<ItemBean> list = new ArrayList<ItemBean>();
                list.add(itemBean);
                getBrowseRecordsService().checkFKIntegrity(list,
                        new DeleteCallback(deleteAction, postDeleteAction, getBrowseRecordsService()));
            }
        });
        delete_SendToTrash.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Send_to_trash()));
        deleteMenu.add(delete_SendToTrash);

        delete_Delete = new MenuItem(MessagesFactory.getMessages().delete_btn());
        delete_Delete.setId("delete_Delete"); //$NON-NLS-1$
        deleteMenu.add(delete_Delete);
        delete_Delete.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                MessageBox.confirm(MessagesFactory.getMessages().confirm_title(), MessagesFactory.getMessages().delete_confirm(),
                        new Listener<MessageBoxEvent>() {

                            @Override
                            public void handleEvent(MessageBoxEvent be) {
                                if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                                    deleteRecord();
                                }
                            }
                        });
            }
        });

        delete_Delete.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Delete()));
        deleteButton.setMenu(deleteMenu);
    }

    protected void addFreshButton() {
        if (refreshButton == null) {
            refreshButton = new Button();
            refreshButton.setId("refreshButton"); //$NON-NLS-1$
            refreshButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.refreshToolbar()));
            refreshButton.setToolTip(MessagesFactory.getMessages().refresh_tip());
            refreshButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    refreshTreeDetail();
                }
            });
        }
        add(refreshButton);
    }

    protected void addMoreActionsButton(final boolean fromSmartView) {
        if (moreActionsButton == null) {
            moreActionsButton = new Button(MessagesFactory.getMessages().moreActions_btn());
            moreActionsButton.setToolTip(MessagesFactory.getMessages().moreActions_tip());
            if (subActionsMenu == null) {
                subActionsMenu = new Menu();
                boolean hasTaskId = itemBean.getTaskId() != null && !itemBean.getTaskId().isEmpty()
                        && !"null".equalsIgnoreCase(itemBean.getTaskId().trim()); //$NON-NLS-1$

                if (openTab && openTabMenuItem == null) {
                    openTabMenuItem = new MenuItem(MessagesFactory.getMessages().openitem_tab());
                    openTabMenuItem.setId("openTabMenuItem"); //$NON-NLS-1$
                    openTabMenuItem.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.openTab()));
                    openTabMenuItem.setToolTip(MessagesFactory.getMessages().openitem_tab());
                    openTabMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

                        @Override
                        public void componentSelected(MenuEvent menuEvent) {
                            // TMDM-3202 open in a top-level tab
                            String fromWhichApp = isHierarchyCall ? MessagesFactory.getMessages().hierarchy_title() : ""; //$NON-NLS-1$
                            String opt = ItemDetailToolBar.VIEW_OPERATION;
                            if (fromSmartView) {
                                opt = ItemDetailToolBar.SMARTVIEW_OPERATION;
                            } else {
                                String smartViewMode = itemBean.getSmartViewMode();
                                if (smartViewMode.equals(ItemBean.PERSOMODE) || smartViewMode.equals(ItemBean.SMARTMODE)) {
                                    opt = ItemDetailToolBar.PERSONALEVIEW_OPERATION;
                                }
                            }
                            TreeDetailUtil.initItemsDetailPanelById(fromWhichApp, itemBean.getIds(), itemBean.getConcept(),
                                    isFkToolBar, isHierarchyCall, opt, isStaging);
                        }
                    });
                    subActionsMenu.add(openTabMenuItem);
                }

                if (duplicateMenuItem == null) {
                    duplicateMenuItem = new MenuItem(MessagesFactory.getMessages().duplicate_btn());
                    duplicateMenuItem.setId("duplicateMenuItem"); //$NON-NLS-1$
                    String concept = ViewUtil.getConceptFromBrowseItemView(itemBean.getConcept());
                    if (viewBean.getBindingEntityModel().getMetaDataTypes().get(concept).isDenyCreatable()) {
                        duplicateMenuItem.setEnabled(false);
                    }
                    duplicateMenuItem.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.duplicate()));
                    duplicateMenuItem.setToolTip(MessagesFactory.getMessages().duplicate_tip());
                    duplicateMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

                        @Override
                        public void componentSelected(MenuEvent menuEvent) {
                            if (!isFkToolBar) {
                                ItemsListPanel.getInstance().setCreate(true);
                            }

                            ItemBean duplicateItemBean = new ItemBean(itemBean.getConcept(), itemBean.getIds(), itemBean
                                    .getItemXml(), itemBean.getDescription(), itemBean.getPkInfoList());
                            duplicateItemBean.copy(itemBean);
                            duplicateItemBean.setIds(""); //$NON-NLS-1$

                            TreeDetailUtil.initItemsDetailPanelByItemPanel(viewBean, duplicateItemBean, isFkToolBar,
                                    isHierarchyCall, (itemsDetailPanel.isLineage() ? true : isOutMost), isStaging);
                            if (!isOutMost && !isFkToolBar) {
                                if (ItemsListPanel.getInstance().getGrid() != null) {
                                    ItemsListPanel.getInstance().getGrid().getSelectionModel().deselectAll();
                                }
                            }
                        }
                    });
                    subActionsMenu.add(duplicateMenuItem);
                }

                if (isStaging && validateMenuItem == null) {
                    validateMenuItem = new MenuItem(MessagesFactory.getMessages().validate_record_btn());
                    validateMenuItem.setId("validateMenuItem"); //$NON-NLS-1$
                    validateMenuItem.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.validateRecord()));
                    validateMenuItem.setToolTip(MessagesFactory.getMessages().validate_record_tip());
                    validateMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

                        @Override
                        public void componentSelected(MenuEvent menuEvent) {
                            ValidationRestServiceHandler.get().validateRecord(
                                    BrowseRecords.getSession().getAppHeader().getMasterDataCluster(), itemBean.getItemXml(),
                                    new SessionAwareAsyncCallback<JSONObject>() {

                                        @Override
                                        public void onSuccess(JSONObject result) {
                                            showValidateResult(result);
                                        }
                                    });
                        }
                    });
                    subActionsMenu.add(validateMenuItem);
                }

                if (!isStaging && journalMenuItem == null) {
                    journalMenuItem = new MenuItem(MessagesFactory.getMessages().journal_btn());
                    journalMenuItem.setId("journalMenuItem"); //$NON-NLS-1$
                    journalMenuItem.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.journal()));
                    journalMenuItem.setToolTip(MessagesFactory.getMessages().journal_tip());
                    journalMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

                        @Override
                        public void componentSelected(MenuEvent menuEvent) {
                            String ids = itemBean.getIds();
                            initJournal(ids, itemBean.getConcept());
                        }

                    });
                    subActionsMenu.add(new SeparatorMenuItem());
                    subActionsMenu.add(journalMenuItem);
                }

                if (navigatorMenuItem == null && BrowseRecords.getSession().getAppHeader().isEnterprise() && !isStaging) {
                    navigatorMenuItem = new MenuItem(MessagesFactory.getMessages().navigator_btn());
                    navigatorMenuItem.setId("navigatorMenuItem"); //$NON-NLS-1$
                    navigatorMenuItem.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.navigator()));
                    navigatorMenuItem.setToolTip(MessagesFactory.getMessages().navigator_tip());
                    navigatorMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

                        @Override
                        public void componentSelected(MenuEvent menuEvent) {
                            String ids = itemBean.getIds();
                            String concept = itemBean.getConcept();
                            boolean hasPrimaryKeyInfo = !viewBean.getBindingEntityModel().getMetaDataTypes()
                                    .get(itemBean.getConcept()).getPrimaryKeyInfo().isEmpty();
                            NavigatorPanel panel = new NavigatorPanel();
                            panel.updateDetailPanel(ids, concept);
                            NavigatorPanel.renderPanel(GWT.getHostPageBaseURL(), ids, concept, BrowseRecords.getSession()
                                    .getAppHeader().getMasterDataCluster(), hasPrimaryKeyInfo, panel);
                        }

                    });
                    subActionsMenu.add(navigatorMenuItem);
                }
                separatorIndex = subActionsMenu.indexOf(duplicateMenuItem) + 1;
                if (journalMenuItem != null) {
                    separatorIndex = subActionsMenu.indexOf(journalMenuItem) + 1;
                }
                if (hasTaskId) {
                    getBrowseRecordsService().checkTask(BrowseRecords.getSession().getAppHeader().getStagingDataCluster(),
                            itemBean.getConcept(), itemBean.getTaskId(), new SessionAwareAsyncCallback<Integer>() {

                                @Override
                                public void onSuccess(Integer result) {
                                    int count = result.intValue();

                                    if (isStaging) {
                                        getBrowseRecordsService().getGoldenRecordIdByGroupId(
                                                BrowseRecords.getSession().getAppHeader().getStagingDataCluster(),
                                                BrowseRecords.getSession().getCurrentView().getViewPK(), itemBean.getConcept(),
                                                BrowseRecords.getSession().getCurrentEntityModel().getKeys(),
                                                itemBean.getTaskId(), new SessionAwareAsyncCallback<String>() {

                                                    @Override
                                                    public void onSuccess(final String ids) {
                                                        if (!ids.isEmpty()) {
                                                            if (openMasterRecordMenuItem == null) {
                                                                openMasterRecordMenuItem = new MenuItem(MessagesFactory
                                                                        .getMessages().masterRecords_btn());
                                                                openMasterRecordMenuItem.setId("openMasterRecordMenuItem"); //$NON-NLS-1$
                                                                openMasterRecordMenuItem.setItemId("openMasterRecordMenuItem"); //$NON-NLS-1$
                                                                openMasterRecordMenuItem.setIcon(AbstractImagePrototype
                                                                        .create(Icons.INSTANCE.masterRecords()));
                                                                openMasterRecordMenuItem
                                                                        .addSelectionListener(new SelectionListener<MenuEvent>() {

                                                                            @Override
                                                                            public void componentSelected(MenuEvent menuEvent) {
                                                                                if (!ids.isEmpty()) {
                                                                                    TreeDetailUtil
                                                                                            .initItemsDetailPanelById(
                                                                                                    "", ids, itemBean.getConcept(), isFkToolBar, //$NON-NLS-1$
                                                                                                    isHierarchyCall,
                                                                                                    ItemDetailToolBar.VIEW_OPERATION,
                                                                                                    false);
                                                                                } else {
                                                                                    MessageBox
                                                                                            .alert(MessagesFactory.getMessages()
                                                                                                    .warning_title(),
                                                                                                    MessagesFactory
                                                                                                            .getMessages()
                                                                                                            .no_golden_record_in_group(
                                                                                                                    itemBean.getTaskId()),
                                                                                                    null);
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                            if (!(subActionsMenu.getItem(separatorIndex) instanceof SeparatorMenuItem)) {
                                                                subActionsMenu.insert(new SeparatorMenuItem(), separatorIndex);
                                                            }
                                                            subActionsMenu.insert(openMasterRecordMenuItem,
                                                                    ItemDetailToolBar.this.separatorIndex + 1);
                                                        }
                                                    }
                                                });
                                    } else {
                                        if (dataLineageMenuItem == null && count > 0) {
                                            dataLineageMenuItem = new MenuItem(MessagesFactory.getMessages().stagingRecords_btn());
                                            dataLineageMenuItem.setId("dataLineageMenuItem"); //$NON-NLS-1$
                                            dataLineageMenuItem.setItemId("dataLineageMenuItem"); //$NON-NLS-1$
                                            dataLineageMenuItem.setIcon(AbstractImagePrototype.create(Icons.INSTANCE
                                                    .stagingRecords()));
                                            dataLineageMenuItem.setToolTip(MessagesFactory.getMessages().stagingRecords_tip());
                                            dataLineageMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

                                                @Override
                                                public void componentSelected(MenuEvent menuEvent) {
                                                    LineagePanel lineagePanel = LineagePanel.getInstance();
                                                    lineagePanel.init(itemBean.getConcept(), itemBean.getTaskId());
                                                    if (GWT.isScript()) {
                                                        ItemDetailToolBar.this.openLineagePanel(itemBean.getIds(), lineagePanel);
                                                    } else {
                                                        ItemDetailToolBar.this.openDebugLineagePanel(itemBean.getIds(),
                                                                lineagePanel);
                                                    }

                                                }
                                            });
                                            subActionsMenu.add(dataLineageMenuItem);
                                        }
                                    }
                                    if (explainMenuItem == null && count > 0) {
                                        explainMenuItem = new MenuItem(MessagesFactory.getMessages().explain_button());
                                        explainMenuItem.setId("explainMenuItem"); //$NON-NLS-1$
                                        explainMenuItem.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Save()));
                                        explainMenuItem.setToolTip(MessagesFactory.getMessages().explain_tip());
                                        explainMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

                                            @Override
                                            public void componentSelected(MenuEvent menuEvent) {
                                                String taskId = itemBean.getTaskId();
                                                if (taskId != null && !taskId.isEmpty()) {
                                                    ExplainRestServiceHandler.get().explainGroupResult(
                                                            BrowseRecords.getSession().getAppHeader().getMasterDataCluster(),
                                                            itemBean.getConcept(), taskId,
                                                            new SessionAwareAsyncCallback<BaseTreeModel>() {

                                                                @Override
                                                                public void onSuccess(BaseTreeModel root) {
                                                                    showExplainResult(root);
                                                                }
                                                            });
                                                } else {
                                                    MessageBox.alert(MessagesFactory.getMessages().warning_title(),
                                                            MessagesFactory.getMessages().no_taskid_warning_message(), null);
                                                }
                                            }
                                        });
                                        subActionsMenu.add(explainMenuItem);
                                    }

                                    if (openTaskMenuItem == null && count > 1) {
                                        openTaskMenuItem = new MenuItem(MessagesFactory.getMessages().open_task());
                                        openTaskMenuItem.setId("openTaskMenuItem"); //$NON-NLS-1$
                                        openTaskMenuItem.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.openTask()));
                                        openTaskMenuItem.setToolTip(MessagesFactory.getMessages().open_task_tooltip());
                                        openTaskMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

                                            @Override
                                            public void componentSelected(MenuEvent menuEvent) {
                                                initDSC(itemBean.getTaskId());
                                            }
                                        });
                                        int explainMenuItemIndex = subActionsMenu.indexOf(explainMenuItem);
                                        if (explainMenuItemIndex != -1) {
                                            subActionsMenu.insert(openTaskMenuItem, explainMenuItemIndex + 1);
                                        } else {
                                            subActionsMenu.add(openTaskMenuItem);
                                        }
                                    }
                                }
                            });
                }

                if (subActionsMenu.indexOf(dataLineageMenuItem) != -1 || subActionsMenu.indexOf(openTaskMenuItem) != -1
                        || subActionsMenu.indexOf(explainMenuItem) != -1) {
                    subActionsMenu.insert(new SeparatorMenuItem(), separatorIndex);
                }

                if (isUseRelations() && !isStaging && relationMenuItem == null) {
                    final Map<String, List<String>> lineageEntityMap = (Map<String, List<String>>) BrowseRecords.getSession()
                            .get(UserSession.CURRENT_LINEAGE_ENTITY_LIST);
                    if (lineageEntityMap != null && lineageEntityMap.containsKey(itemBean.getConcept())) {
                        addRelationMenuItem(lineageEntityMap.get(itemBean.getConcept()));
                    } else {
                        getBrowseRecordsService().getLineageEntity(itemBean.getConcept(),
                                new SessionAwareAsyncCallback<List<String>>() {

                                    @Override
                                    public void onSuccess(List<String> list) {
                                        if (lineageEntityMap != null) {
                                            lineageEntityMap.put(itemBean.getConcept(), list);
                                        } else {
                                            Map<String, List<String>> map = new HashMap<String, List<String>>(1);
                                            map.put(itemBean.getConcept(), list);
                                            BrowseRecords.getSession().put(UserSession.CURRENT_LINEAGE_ENTITY_LIST, map);
                                        }
                                        addRelationMenuItem(list);
                                    }

                                });
                    }

                }
                moreActionsButton.setMenu(subActionsMenu);
            }
        }
        add(moreActionsButton);
    }

    private void addRelationMenuItem(List<String> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        final List<String> lineageList = list;
        relationMenuItem = new MenuItem(MessagesFactory.getMessages().relations_btn());
        relationMenuItem.setId("relationMenuItem"); //$NON-NLS-1$
        relationMenuItem.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.relations()));
        relationMenuItem.setToolTip(MessagesFactory.getMessages().relations_tooltip());
        relationMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent menuEvent) {
                StringBuilder entityStr = new StringBuilder();
                for (String str : lineageList) {
                    entityStr.append(str).append(","); //$NON-NLS-1$
                }
                String arrStr = entityStr.toString().substring(0, entityStr.length() - 1);
                String ids = itemBean.getIds();
                if (ids == null || ids.trim().isEmpty()) {
                    ids = ""; //$NON-NLS-1$
                }
                initSearchEntityPanel(arrStr, ids, itemBean.getConcept());
            }
        });
        subActionsMenu.add(new SeparatorMenuItem());
        subActionsMenu.add(relationMenuItem);
    }

    private void refreshTreeDetail() {
        Widget widget = itemsDetailPanel.getFirstTabWidget();
        if (widget instanceof ForeignKeyTreeDetail) {
            isFkToolBar = true;
            final ForeignKeyTreeDetail fkTree = (ForeignKeyTreeDetail) widget;
            ItemNodeModel root = fkTree.getRootModel();
            refreshTree(null, fkTree, root);
        } else {
            ItemPanel itemPanel = (ItemPanel) widget;
            ItemNodeModel root = itemPanel.getTree().getRootModel();
            refreshTree(itemPanel, null, root);
        }
    }

    public void refreshTreeDetailByIds(String ids) {
        Widget widget = itemsDetailPanel.getFirstTabWidget();
        if (widget instanceof ForeignKeyTreeDetail) {
            isFkToolBar = true;
            final ForeignKeyTreeDetail fkTree = (ForeignKeyTreeDetail) widget;
            fkTree.getFkModel().getItemBean().setIds(ids);
            fkTree.refreshTree();
        } else {
            ItemPanel itemPanel = (ItemPanel) widget;
            itemPanel.getItem().setIds(ids);
            itemPanel.refreshTree();
        }
        this.removeAll();
        operation = ItemDetailToolBar.VIEW_OPERATION;
        initToolBar();
    }

    private void refreshTree(final ItemPanel itemPanel, final ForeignKeyTreeDetail fkTree, final ItemNodeModel root) {
        ItemBean itemBean = itemPanel.getItem();
        getBrowseRecordsService().isItemModifiedByOthers(itemBean, new SessionAwareAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                if (TreeDetailUtil.isChangeValue(root) || result) {
                    MessageBox
                            .confirm(MessagesFactory.getMessages().confirm_title(),
                                    MessagesFactory.getMessages().msg_confirm_refresh_tree_detail(),
                                    new Listener<MessageBoxEvent>() {

                                        @Override
                                        public void handleEvent(MessageBoxEvent be) {
                                            if (Dialog.YES.equals(be.getButtonClicked().getItemId())) {
                                                itemPanel.refreshTree();
                                            }
                                        }
                                    }).getDialog().setWidth(600);
                } else {
                    itemPanel.refreshTree();
                }
            }

            @Override
            protected void doOnFailure(Throwable caught) {
                MessageBox
                        .alert(MessagesFactory.getMessages().refresh_tip(), MessagesFactory.getMessages().refresh_error(), null);
            }

        });
    }

    protected void addRelationButton() {
        final Map<String, List<String>> lineageEntityMap = (Map<String, List<String>>) BrowseRecords.getSession().get(
                UserSession.CURRENT_LINEAGE_ENTITY_LIST);
        if (lineageEntityMap != null && lineageEntityMap.containsKey(itemBean.getConcept())) {
            setRelation(lineageEntityMap.get(itemBean.getConcept()));
        } else {
            getBrowseRecordsService().getLineageEntity(itemBean.getConcept(), new SessionAwareAsyncCallback<List<String>>() {

                @Override
                public void onSuccess(List<String> list) {
                    if (lineageEntityMap != null) {
                        lineageEntityMap.put(itemBean.getConcept(), list);
                    } else {
                        Map<String, List<String>> map = new HashMap<String, List<String>>(1);
                        map.put(itemBean.getConcept(), list);
                        BrowseRecords.getSession().put(UserSession.CURRENT_LINEAGE_ENTITY_LIST, map);
                    }
                    setRelation(list);
                }

            });
        }
    }

    private void setRelation(List<String> list) {
        if (list == null || list.size() == 0) {
            return;
        }

        final List<String> lineageList = list;
        relationButton = new Button(MessagesFactory.getMessages().relations_btn());
        relationButton.setId("relationButton"); //$NON-NLS-1$
        relationButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.relations()));
        relationButton.setToolTip(MessagesFactory.getMessages().relations_tooltip());
        relationButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                StringBuilder entityStr = new StringBuilder();
                for (String str : lineageList) {
                    entityStr.append(str).append(","); //$NON-NLS-1$
                }
                String arrStr = entityStr.toString().substring(0, entityStr.length() - 1);
                String ids = itemBean.getIds();
                if (ids == null || ids.trim().isEmpty()) {
                    ids = ""; //$NON-NLS-1$
                }
                initSearchEntityPanel(arrStr, ids, itemBean.getConcept());
            }
        });
        ItemDetailToolBar.this.addSeparator();
        add(relationButton);
    }

    protected void addWorkFlosCombo() {
        final Map<String, List<ItemBaseModel>> runnableProcessListMap = (Map<String, List<ItemBaseModel>>) BrowseRecords
                .getSession().get(UserSession.CURRENT_RUNNABLE_PROCESS_LIST);
        if (runnableProcessListMap != null && runnableProcessListMap.containsKey(itemBean.getConcept())) {
            setWorkFlowCombo(runnableProcessListMap.get(itemBean.getConcept()));
        } else {
            getBrowseRecordsService().getRunnableProcessList(itemBean.getConcept(), Locale.getLanguage(),
                    new SessionAwareAsyncCallback<List<ItemBaseModel>>() {

                        @Override
                        public void onSuccess(List<ItemBaseModel> processList) {
                            if (runnableProcessListMap != null) {
                                runnableProcessListMap.put(itemBean.getConcept(), processList);
                            } else {
                                Map<String, List<ItemBaseModel>> map = new HashMap<String, List<ItemBaseModel>>(1);
                                map.put(itemBean.getConcept(), processList);
                                BrowseRecords.getSession().put(UserSession.CURRENT_RUNNABLE_PROCESS_LIST, map);
                            }
                            setWorkFlowCombo(processList);
                        }
                    });
        }
    }

    private void setWorkFlowCombo(List<ItemBaseModel> processList) {
        add(new FillToolItem());
        ListStore<ItemBaseModel> workFlowList = new ListStore<ItemBaseModel>();
        workFlowList.add(processList);
        if (workFlowCombo == null) {
            workFlowCombo = new ComboBoxField<ItemBaseModel>();
            workFlowCombo.setId("workFlowCombo"); //$NON-NLS-1$
            workFlowCombo.setStore(workFlowList);
            workFlowCombo.setDisplayField("value");//$NON-NLS-1$
            workFlowCombo.setValueField("key");//$NON-NLS-1$
            workFlowCombo.setTypeAhead(true);
            workFlowCombo.setTriggerAction(TriggerAction.ALL);
            workFlowCombo.addSelectionChangedListener(new SelectionChangedListener<ItemBaseModel>() {

                @Override
                public void selectionChanged(SelectionChangedEvent<ItemBaseModel> se) {
                    selectItem = se.getSelectedItem();
                }
            });
        }
        add(workFlowCombo);
        if (launchProcessButton == null) {
            launchProcessButton = new Button();
            launchProcessButton.setId("launchProcessButton"); //$NON-NLS-1$
            launchProcessButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.launch_process()));
            launchProcessButton.setToolTip(MessagesFactory.getMessages().launch_process_tooltip());
            launchProcessButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    if (selectItem == null) {
                        MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                                .process_select(), null);
                        return;
                    }
                    final MessageBox waitBar = MessageBox.wait(MessagesFactory.getMessages().process_progress_bar_title(),
                            MessagesFactory.getMessages().process_progress_bar_message(), MessagesFactory.getMessages()
                                    .process_progress_bar_title() + "..."); //$NON-NLS-1$
                    String[] ids = new String[] { itemBean.getIds() };

                    getBrowseRecordsService().processItem(itemBean.getConcept(), ids,
                            (String) selectItem.get("key"), new SessionAwareAsyncCallback<String>() { //$NON-NLS-1$

                                @Override
                                public void onSuccess(final String urlResult) {
                                    waitBar.close();
                                    final MessageBox msgBox = new MessageBox();
                                    msgBox.setTitle(MessagesFactory.getMessages().status());
                                    msgBox.setMessage(MessagesFactory.getMessages().process_launched());
                                    msgBox.setButtons(""); //$NON-NLS-1$
                                    msgBox.setIcon(MessageBox.INFO);
                                    msgBox.show();
                                    Timer timer = new Timer() {

                                        @Override
                                        public void run() {
                                            msgBox.close();
                                            if (urlResult != null && urlResult.length() > 0) {
                                                openWindow(urlResult);
                                            }
                                        }
                                    };
                                    timer.schedule(700);
                                }

                                @Override
                                protected void doOnFailure(Throwable caught) {
                                    waitBar.close();
                                    MessageBox.alert(MessagesFactory.getMessages().status(), MessagesFactory.getMessages()
                                            .process_failed(), null);
                                }
                            });
                }
            });
        }
        add(launchProcessButton);
    }

    protected void addPersonalViewButton() {
        personalviewButton = new Button(MessagesFactory.getMessages().personalview_btn());
        personalviewButton.setId("personalviewButton"); //$NON-NLS-1$
        personalviewButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                updateSmartViewToolBar();
                if (itemsDetailPanel.getFirstTabWidget() instanceof ItemPanel) {
                    ItemPanel itemPanel = (ItemPanel) itemsDetailPanel.getFirstTabWidget();
                    itemPanel.getTree().setVisible(false);
                    itemPanel.getSmartPanel().setVisible(true);
                    itemsDetailPanel.selectTabAtIndex(0);
                }
            }
        });
        add(personalviewButton);
    }

    protected void addGeneratedViewButton() {
        generatedviewButton = new Button(MessagesFactory.getMessages().generatedview_btn());
        generatedviewButton.setId("generatedviewButton"); //$NON-NLS-1$
        generatedviewButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                updateViewToolBar();
                if (itemsDetailPanel.getFirstTabWidget() instanceof ItemPanel) {
                    ItemPanel itemPanel = (ItemPanel) itemsDetailPanel.getFirstTabWidget();
                    itemPanel.getTree().setVisible(true);
                    itemPanel.getSmartPanel().setVisible(false);
                    itemsDetailPanel.selectTabAtIndex(0);
                }
            }
        });
        add(generatedviewButton);
    }

    protected void addSaveAndCloseButtonOfBulkUpdate() {
        if (saveAndCloseButton == null) {
            saveAndCloseButton = new Button(MessagesFactory.getMessages().save_close_btn());
            saveAndCloseButton.setId("saveAndCloseButtonOfMassUpdate"); //$NON-NLS-1$
            saveAndCloseButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.save_and_close()));
            saveAndCloseButton.setToolTip(MessagesFactory.getMessages().save_close_tip());
            saveAndCloseButton.setWidth("105px");
            saveAndCloseButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    Widget widget = itemsDetailPanel.getFirstTabWidget();
                    TreeDetail treeDetail = ((ItemPanel) widget).getTree();
                    if (!BrowseRecords.getSession().getAppHeader().isAutoValidate()) {
                        treeDetail.makeWarning(treeDetail.getRoot());
                    }
                    ItemNodeModel root = treeDetail.getRootModel();
                    if (TreeDetailUtil.isEditValue(viewBean.getBindingEntityModel(), root)) {
                        String invalidNodeName = TreeDetailUtil.checkInvalidNode(root);
                        if (invalidNodeName.isEmpty()) {
                            MessageBox.confirm(MessagesFactory.getMessages().confirm_title(), MessagesFactory.getMessages()
                                    .bulkUpdate_confirm(), new Listener<MessageBoxEvent>() {

                                @Override
                                public void handleEvent(MessageBoxEvent be) {
                                    if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                                        Widget widget = itemsDetailPanel.getFirstTabWidget();
                                        Dispatcher dispatch = Dispatcher.get();
                                        AppEvent app = new AppEvent(BrowseRecordsEvents.BulkUpdateItem);
                                        ItemNodeModel model = null;
                                        if (widget instanceof ItemPanel) {// save primary key
                                            ItemPanel itemPanel = (ItemPanel) widget;
                                            model = itemPanel.getTree().getRootModel();
                                            app.setData("ItemBean", itemPanel.getItem()); //$NON-NLS-1$
                                            app.setData(
                                                    "isCreate", itemPanel.getOperation().equals(ItemDetailToolBar.CREATE_OPERATION) //$NON-NLS-1$
                                                            || itemPanel.getOperation().equals(
                                                                    ItemDetailToolBar.DUPLICATE_OPERATION) ? true : false);
                                        }
                                        app.setData("viewBean", viewBean); //$NON-NLS-1$
                                        app.setData("concept", itemBean.getConcept()); //$NON-NLS-1$
                                        app.setData(model);
                                        app.setData("isClose", true); //$NON-NLS-1$
                                        app.setData("itemDetailToolBar", this); //$NON-NLS-1$
                                        app.setData("isStaging", isStaging); //$NON-NLS-1$
                                        app.setData(BrowseRecordsView.ITEMS_DETAIL_PANEL, itemsDetailPanel);
                                        dispatch.dispatch(app);
                                    }
                                }
                            });
                        } else {
                            MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                                    .validation_error(invalidNodeName), null);
                        }
                    } else {
                        MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                                    .bulkUpdate_no_edit_info(), null);
                    }
                }
            });
        }
        add(saveAndCloseButton);
    }

    protected void addCanelButtonOfMassUpdate() {
        if (cancelButton == null) {
            cancelButton = new Button(MessagesFactory.getMessages().cancel_btn());
            cancelButton.setId("cancelButtonOfMassUpdate"); //$NON-NLS-1$
            cancelButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Delete()));
            cancelButton.setToolTip(MessagesFactory.getMessages().save_close_tip());
            cancelButton.setWidth("61px");
            cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    BulkUpdatePanel bulkUpdatePanel = Registry.get(BrowseRecords.BULK_UPDATE_PANEL);
                    bulkUpdatePanel.closePanel();
                }
            });
        }
        add(cancelButton);
    }

    private native boolean initDSC(String taskId)/*-{
		$wnd.amalto.datastewardship.Datastewardship.taskItem(taskId);
		return true;
    }-*/;

    protected void initSmartViewToolBar() {
        addGeneratedViewButton();
        addSeparator();
        addSmartViewCombo();
        addSeparator();
        addDeleteButton();
        addSeparator();
        addFreshButton();
        addSeparator();
        addMoreActionsButton(true);
        addSeparator();
        addPrintButton();
        if (!isStaging) {
            this.addWorkFlosCombo();
        }
    }

    private void updateSmartViewToolBar() {
        int tabCount = itemsDetailPanel.getTabCount();
        for (int i = 0; i < tabCount; ++i) {
            Widget widget = itemsDetailPanel.getTabWidgetAtIndex(i);
            if (widget instanceof ItemPanel) {
                ItemPanel itemPanel = (ItemPanel) widget;
                ItemDetailToolBar toolbar = itemPanel.getToolBar();
                if (toolbar.getOperation().equals(ItemDetailToolBar.VIEW_OPERATION)
                        || toolbar.getOperation().equals(ItemDetailToolBar.PERSONALEVIEW_OPERATION)) {
                    toolbar.removeAll();
                    toolbar.initSmartViewToolBar();
                    toolbar.setOperation(ItemDetailToolBar.SMARTVIEW_OPERATION);
                    toolbar.layout(true);
                }
            }
        }
    }

    private void updateViewToolBar() {
        int tabCount = itemsDetailPanel.getTabCount();

        for (int i = 0; i < tabCount; ++i) {
            Widget widget = itemsDetailPanel.getTabWidgetAtIndex(i);
            if (widget instanceof ItemPanel) {
                ItemPanel itemPanel = (ItemPanel) widget;
                ItemDetailToolBar toolbar = itemPanel.getToolBar();

                if (toolbar.getOperation().equals(ItemDetailToolBar.SMARTVIEW_OPERATION)) {
                    itemsDetailPanel.selectTabAtIndex(i);
                    toolbar.removeAll();
                    toolbar.initViewToolBar();
                    toolbar.setOperation(ItemDetailToolBar.PERSONALEVIEW_OPERATION);
                    toolbar.layout(true);
                }
            }
        }
    }

    protected void addSmartViewCombo() {
        final ListStore<ItemBaseModel> smartViewList = new ListStore<ItemBaseModel>();
        if (smartViewCombo == null) {
            smartViewCombo = new ComboBoxField<ItemBaseModel>();
            smartViewCombo.setId("smartViewCombo"); //$NON-NLS-1$
            smartViewCombo.setStore(smartViewList);
            smartViewCombo.setDisplayField("value"); //$NON-NLS-1$
            smartViewCombo.setValueField("key"); //$NON-NLS-1$
            smartViewCombo.setTypeAhead(true);
            smartViewCombo.setTriggerAction(TriggerAction.ALL);
            smartViewCombo.addSelectionChangedListener(new SelectionChangedListener<ItemBaseModel>() {

                @Override
                public void selectionChanged(SelectionChangedEvent<ItemBaseModel> se) {
                    if (itemsDetailPanel.getFirstTabWidget() instanceof ItemPanel) {
                        ItemPanel itemPanel = (ItemPanel) itemsDetailPanel.getFirstTabWidget();
                        String frameUrl = GWT.getHostPageBaseURL()
                                + "/browserecords/SmartViewServlet?ids=" + URL.encodeQueryString(itemBean.getIds()) + "&concept=" //$NON-NLS-1$ //$NON-NLS-2$
                                + itemBean.getConcept() + "&isStaging=" + isStaging + "&language=" + Locale.getLanguage(); //$NON-NLS-1$ //$NON-NLS-2$
                        if (se.getSelectedItem().get("key") != null) { //$NON-NLS-1$
                            frameUrl += ("&name=" + se.getSelectedItem().get("key")); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        itemPanel.getSmartPanel().setUrl(frameUrl);
                        itemPanel.getSmartPanel().layout(true);
                    }
                }

            });
        }

        String regex = itemBean.getConcept() + "&" + Locale.getLanguage(); //$NON-NLS-1$
        getBrowseRecordsService().getSmartViewList(regex, new SessionAwareAsyncCallback<List<ItemBaseModel>>() {

            @Override
            public void onSuccess(List<ItemBaseModel> list) {
                smartViewList.add(list);
                smartViewList.sort("value", SortDir.ASC); //$NON-NLS-1$
                if (smartViewCombo.getValue() == null) {
                    smartViewCombo.setValue(ViewUtil.getDefaultSmartViewModel(smartViewList.getModels(), itemBean.getConcept()));
                }
            }

        });
        add(smartViewCombo);
    }

    protected void addPrintButton() {
        Button printBtn = new Button(MessagesFactory.getMessages().print_btn());
        printBtn.setId("printBtn"); //$NON-NLS-1$
        printBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (smartViewCombo.getSelection() != null && smartViewCombo.getSelection().size() > 0) {

                    StringBuilder url = new StringBuilder();
                    url.append(GWT.getHostPageBaseURL()).append("/browserecords/SmartViewServlet?ids=") //$NON-NLS-1$
                            .append(URL.encodeQueryString(itemBean.getIds())).append("&concept=") //$NON-NLS-1$
                            .append(itemBean.getConcept()).append("&isStaging=").append(isStaging).append("&language=") //$NON-NLS-1$ //$NON-NLS-2$
                            .append(Locale.getLanguage()).append("&name=") //$NON-NLS-1$
                            .append(smartViewCombo.getSelection().get(0).<String> get("value")); //$NON-NLS-1$
                    openWindow(url.toString());
                }
            }

        });
        add(printBtn);
    }

    protected void addSeparator() {
        add(new SeparatorToolItem());
    }

    public void updateToolBar() {
        // do nothing
    }

    private native boolean initJournal(String ids, String concept)/*-{
		$wnd.amalto.journal.Journal.browseJournalWithCriteria(ids, concept,
				true);
		return true;
    }-*/;

    private native boolean showDataNavigator(String ids, String concept)/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		var panel = @org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailUtil::transferTreeDetailPanel(Lorg/talend/mdm/webapp/browserecords/client/widget/ItemsDetailPanel;)(detailPanel);
		tabPanel.add(panel);
		tabPanel.setSelection(itemId);
    }-*/;

    // Please note that this method is duplicated in
    // org.talend.mdm.webapp.browserecords.client.widget.integrity.SingletonDeleteStrategy.initSearchEntityPanel()
    private native boolean initSearchEntityPanel(String arrStr, String ids, String dataObject)/*-{
		var lineageEntities = arrStr.split(",");
		$wnd.amalto.itemsbrowser.ItemsBrowser.lineageItem(lineageEntities, ids,
				dataObject);
		return true;
    }-*/;

    public void saveItemAndClose(final boolean isClose) {
        ItemBean bean = itemBean;
        Widget widget = itemsDetailPanel.getPrimaryKeyTabWidget();
        if (widget instanceof ItemPanel) {// save primary key
            ItemPanel itemPanel = (ItemPanel) widget;
            bean = itemPanel.getItem();
        }
        if (bean.getIds().trim().equals("")) { //$NON-NLS-1$
            saveItemWithIdCheck(isClose);
            return;
        }
        getBrowseRecordsService().isItemModifiedByOthers(bean, new SessionAwareAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    MessageBox
                            .confirm(MessagesFactory.getMessages().confirm_title(),
                                    MessagesFactory.getMessages().save_concurrent_fail(), new Listener<MessageBoxEvent>() {

                                        @Override
                                        public void handleEvent(MessageBoxEvent be) {
                                            if (Dialog.YES.equals(be.getButtonClicked().getItemId())) {
                                                saveItem(isClose);
                                            }
                                        }
                                    }).getDialog().setWidth(600);
                } else {
                    saveItem(isClose);
                }
            }
        });
    }

    private void saveItemWithIdCheck(final boolean isClose) {

        Widget widget = itemsDetailPanel.getFirstTabWidget();
        if (widget instanceof ItemPanel) {
            ItemPanel itemPanel = (ItemPanel) widget;
            ItemNodeModel model = itemPanel.getTree().getRootModel();
            String[] keyArray = CommonUtil.extractIDs(model, viewBean);

            getBrowseRecordsService().isExistId(this.itemBean.getConcept(), keyArray, Locale.getLanguage(),
                    new SessionAwareAsyncCallback<Boolean>() {

                        @Override
                        public void onSuccess(Boolean flag) {
                            if (flag) {
                                MessageBox
                                        .alert(MessagesFactory.getMessages().info_title(),
                                                MessagesFactory.getMessages().record_exists(), null).getDialog().setWidth(400);
                            } else {
                                saveItem(isClose);
                            }
                        }
                    });

        } else {
            saveItem(isClose);
        }
    }

    private void saveItem(boolean isClose) {
        boolean validate = false;
        Widget widget = itemsDetailPanel.getFirstTabWidget();
        Dispatcher dispatch = Dispatcher.get();
        AppEvent app = new AppEvent(BrowseRecordsEvents.SaveItem);
        ItemNodeModel model = null;
        if (widget instanceof ItemPanel) {// save primary key
            ItemPanel itemPanel = (ItemPanel) widget;
            validate = true;
            model = itemPanel.getTree().getRootModel();
            app.setData("ItemBean", itemPanel.getItem()); //$NON-NLS-1$
            app.setData("isCreate", itemPanel.getOperation().equals(ItemDetailToolBar.CREATE_OPERATION) //$NON-NLS-1$
                    || itemPanel.getOperation().equals(ItemDetailToolBar.DUPLICATE_OPERATION) ? true : false);
        } else if (widget instanceof ForeignKeyTreeDetail) { // save foreign key
            ForeignKeyTreeDetail fkDetail = (ForeignKeyTreeDetail) widget;
            if (fkDetail.validateTree()) {
                validate = true;
                model = fkDetail.getRootModel();
                app.setData(
                        "ItemBean", fkDetail.isCreate() ? new ItemBean(fkDetail.getViewBean().getBindingEntityModel().getConceptName(), "", "") : itemBean); //$NON-NLS-1$ 
                app.setData("isCreate", fkDetail.isCreate()); //$NON-NLS-1$
            }
        }
        app.setData("viewBean", viewBean); //$NON-NLS-1$
        app.setData(model);
        app.setData("isClose", isClose); //$NON-NLS-1$
        app.setData("itemDetailToolBar", this); //$NON-NLS-1$
        app.setData("isStaging", isStaging); //$NON-NLS-1$
        app.setData(BrowseRecordsView.ITEMS_DETAIL_PANEL, itemsDetailPanel);

        if (validate) {
            dispatch.dispatch(app);
        } else {
            // TMDM-3349 button 'save and close' function
            if (isClose && !isOutMost && !isHierarchyCall) {
                ItemsListPanel.getInstance().initSpecialVariable();
            }
            MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages().save_error(), null);
        }
    }

    private void massUpdateItem(boolean isClose) {
        boolean validate = false;
        Widget widget = itemsDetailPanel.getFirstTabWidget();
        Dispatcher dispatch = Dispatcher.get();
        AppEvent app = new AppEvent(BrowseRecordsEvents.SaveItem);
        ItemNodeModel model = null;
        if (widget instanceof ItemPanel) {// save primary key
            ItemPanel itemPanel = (ItemPanel) widget;
            validate = true;
            model = itemPanel.getTree().getRootModel();
            app.setData("ItemBean", itemPanel.getItem()); //$NON-NLS-1$
            app.setData("isCreate", itemPanel.getOperation().equals(ItemDetailToolBar.CREATE_OPERATION) //$NON-NLS-1$
                    || itemPanel.getOperation().equals(ItemDetailToolBar.DUPLICATE_OPERATION) ? true : false);
        } else if (widget instanceof ForeignKeyTreeDetail) { // save foreign key
            ForeignKeyTreeDetail fkDetail = (ForeignKeyTreeDetail) widget;
            if (fkDetail.validateTree()) {
                validate = true;
                model = fkDetail.getRootModel();
                app.setData(
                        "ItemBean", fkDetail.isCreate() ? new ItemBean(fkDetail.getViewBean().getBindingEntityModel().getConceptName(), "", "") : itemBean); //$NON-NLS-1$ 
                app.setData("isCreate", fkDetail.isCreate()); //$NON-NLS-1$
            }
        }
        app.setData("viewBean", viewBean); //$NON-NLS-1$
        app.setData(model);
        app.setData("isClose", isClose); //$NON-NLS-1$
        app.setData("itemDetailToolBar", this); //$NON-NLS-1$
        app.setData("isStaging", isStaging); //$NON-NLS-1$
        app.setData(BrowseRecordsView.ITEMS_DETAIL_PANEL, itemsDetailPanel);

        if (validate) {
            dispatch.dispatch(app);
        } else {
            // TMDM-3349 button 'save and close' function
            if (isClose && !isOutMost && !isHierarchyCall) {
                ItemsListPanel.getInstance().initSpecialVariable();
            }
            MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages().save_error(), null);
        }
    }

    private void showExplainResult(BaseTreeModel root) {
        Window explainWindow = new Window();
        SimulateTablePanel simulateTable = new SimulateTablePanel();
        explainWindow.setHeading(MessagesFactory.getMessages().lineage_explain_tab_title());
        explainWindow.setSize(800, 600);
        explainWindow.setLayout(new FitLayout());
        explainWindow.setScrollMode(Scroll.NONE);
        simulateTable.buildTree(root);
        explainWindow.add(simulateTable);
        explainWindow.show();
    }

    public void setSelectItem(ItemBaseModel selectItem) {
        this.selectItem = selectItem;
    }

    public String getOperation() {
        return operation;
    }

    public ItemBean getItemBean() {
        return itemBean;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setOutMost(boolean isOutMost) {
        this.isOutMost = isOutMost;
    }

    public boolean isOutMost() {
        return isOutMost;
    }

    public boolean isHierarchyCall() {
        return isHierarchyCall;
    }

    public void setHierarchyCall(boolean isHierarchyCall) {
        this.isHierarchyCall = isHierarchyCall;
    }

    public boolean isOpenTab() {
        return openTab;
    }

    public void setOpenTab(boolean openTab) {
        this.openTab = openTab;
    }

    public int getViewCode() {
        return this.viewCode;
    }

    public void setViewCode(int viewCode) {
        this.viewCode = viewCode;
    }

    public void closeCurrentTabPanel() {
        ItemsMainTabPanel.getInstance().remove(ItemsMainTabPanel.getInstance().getSelectedItem());
    }

    /**
     * call when save the most out tabItem
     */
    public void refreshNodeStatus() {
        ItemPanel itemPanel = (ItemPanel) itemsDetailPanel.getFirstTabWidget();
        ItemNodeModel root = itemPanel.getTree().getRootModel();
        setChangeValue(root);
    }

    private void setChangeValue(ItemNodeModel model) {
        if (model.isChangeValue()) {
            model.setChangeValue(false);
        }
        for (ModelData node : model.getChildren()) {
            setChangeValue((ItemNodeModel) node);
        }
    }

    public native void closeOutTabPanel()/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		tabPanel.closeCurrentTab();
    }-*/;

    public native void updateOutTabPanel(String tabText)/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		tabPanel.updateCurrentTabText(tabText);
    }-*/;

    class MenuEx extends Menu {

        public MenuEx() {
            super();
            monitorWindowResize = false;
        }
    }

    public class ToolBarExLayout extends ToolBarLayout {

        @Override
        protected void onComponentShow(Component component) {
            if (component.isRendered()) {
                if (component.el().getParent() != null) {
                    component.el().getParent().removeStyleName(component.getHideMode().value());
                }
            }
        }

        @Override
        protected void initMore() {
            if (more == null) {
                moreMenu = new MenuEx();
                moreMenu.addListener(Events.BeforeShow, new Listener<MenuEvent>() {

                    @Override
                    public void handleEvent(MenuEvent be) {
                        clearMenu();
                        for (Component c : container.getItems()) {

                            if (isHidden(c)) {
                                addComponentToMenu(be.getContainer(), c);
                            }
                        }
                        if (be.getContainer().getItemCount() == 0) {
                            be.getContainer().add(new HeaderMenuItem(getNoItemsMenuText()));
                        }
                    }

                });

                more = new Button();
                more.addStyleName("x-toolbar-more");
                more.setIcon(GXT.IMAGES.toolbar_more());
                more.setMenu(moreMenu);

            }
            Element td = insertCell(more, getExtrasTr(), 100);
            if (more.isRendered()) {
                td.appendChild(more.el().dom);
            } else {
                more.render(td);
            }
            ComponentHelper.doAttach(more);

            moreMenu.setWidth(230);
        }

        private native El getExtrasTr()/*-{
			return this.@com.extjs.gxt.ui.client.widget.layout.ToolBarLayout::extrasTr;
        }-*/;

        @Override
        @SuppressWarnings("unchecked")
        protected void addComponentToMenu(Menu menu, Component c) {
            if (c instanceof SeparatorToolItem) {
                menu.add(new SeparatorMenuItem());
            } else if (c instanceof SplitButton) {
                final SplitButton sb = (SplitButton) c;
                MenuItem item = new MenuItem(sb.getText(), sb.getIcon());
                item.setEnabled(c.isEnabled());
                item.setItemId(c.getItemId());
                if (sb.getMenu() != null) {
                    item.setSubMenu(sb.getMenu());
                }
                item.addSelectionListener(new SelectionListener<MenuEvent>() {

                    @Override
                    public void componentSelected(MenuEvent ce) {
                        ButtonEvent e = new ButtonEvent(sb);
                        e.setEvent(ce.getEvent());
                        sb.fireEvent(Events.Select, e);
                    }

                });
                menu.add(item);
            } else if (c instanceof LabelToolItem) {
                LabelToolItem l = (LabelToolItem) c;
                MenuItem item = new MenuItem(l.getLabel());
                menu.add(item);
            } else if (c instanceof ComboBox<?>) {
                final ComboBox<ItemBaseModel> cb = (ComboBox<ItemBaseModel>) c;
                ComboBox<ItemBaseModel> comboBoxClone = new ComboBox<ItemBaseModel>();
                comboBoxClone.setStore(cb.getStore());
                comboBoxClone.setDisplayField("value");//$NON-NLS-1$
                comboBoxClone.setValueField("key");
                comboBoxClone.setTypeAhead(true);
                comboBoxClone.setTriggerAction(TriggerAction.ALL);
                comboBoxClone.addSelectionChangedListener(new SelectionChangedListener<ItemBaseModel>() {

                    @Override
                    public void selectionChanged(SelectionChangedEvent<ItemBaseModel> se) {
                        cb.setValue(se.getSelectedItem());
                    }
                });
                if (cb.getValue() != null) {
                    comboBoxClone.setValue(cb.getValue());
                }
                menu.add(comboBoxClone);
            } else if (c instanceof ComboBoxField<?>) {
                final ComboBoxField<ItemBaseModel> cb = (ComboBoxField<ItemBaseModel>) c;
                ComboBoxField<ItemBaseModel> comboBoxClone = new ComboBoxField<ItemBaseModel>();
                comboBoxClone.setStore(cb.getStore());
                comboBoxClone.setDisplayField("value");//$NON-NLS-1$
                comboBoxClone.setValueField("key");
                comboBoxClone.setTypeAhead(true);
                comboBoxClone.setTriggerAction(TriggerAction.ALL);
                comboBoxClone.addSelectionChangedListener(new SelectionChangedListener<ItemBaseModel>() {

                    @Override
                    public void selectionChanged(SelectionChangedEvent<ItemBaseModel> se) {
                        cb.setValue(se.getSelectedItem());
                    }
                });
                if (cb.getValue() != null) {
                    comboBoxClone.setValue(cb.getValue());
                }
                menu.add(comboBoxClone);
            } else if (c instanceof Button) {
                final Button b = (Button) c;
                String menuText = b.getText();
                if (menuText == null || menuText.trim().length() == 0) {
                    menuText = b.getToolTip().getToolTipConfig() == null ? "" : b.getToolTip().getToolTipConfig().getText();
                }
                MenuItem item = new MenuItem(menuText, b.getIcon());
                if (b.getToolTip() != null) {
                    item.setToolTip(b.getToolTip().getToolTipConfig());
                }
                item.setItemId(c.getItemId());
                if (b.getMenu() != null) {
                    item.setHideOnClick(false);
                    item.setSubMenu(b.getMenu());
                }
                item.setEnabled(c.isEnabled());
                item.addSelectionListener(new SelectionListener<MenuEvent>() {

                    @Override
                    public void componentSelected(MenuEvent ce) {
                        ButtonEvent e = new ButtonEvent(b);
                        e.setEvent(ce.getEvent());
                        b.fireEvent(Events.Select, e);
                    }

                });
                menu.add(item);
            } else if (c instanceof ButtonGroup) {
                ButtonGroup g = (ButtonGroup) c;
                g.setItemId(c.getItemId());
                menu.add(new SeparatorMenuItem());
                String heading = g.getHeading();
                if (heading != null && heading.length() > 0 && !heading.equals("&#160;")) {
                    menu.add(new HeaderMenuItem(g.getHeading()));
                }
                for (Component c2 : g.getItems()) {
                    addComponentToMenu(menu, c2);
                }
                menu.add(new SeparatorMenuItem());
            }

            if (menu.getItemCount() > 0) {
                if (menu.getItem(0) instanceof SeparatorMenuItem) {
                    menu.remove(menu.getItem(0));
                }
                if (menu.getItemCount() > 0) {
                    if (menu.getItem(menu.getItemCount() - 1) instanceof SeparatorMenuItem) {
                        menu.remove(menu.getItem(menu.getItemCount() - 1));
                    }
                }
            }
        }
    }

    private native void openWindow(String url)/*-{
		window.open(url);
    }-*/;

    protected void openDebugLineagePanel(String ids, LineagePanel panel) {
        Window window = new Window();
        window.setLayout(new FitLayout());
        window.add(panel);
        window.setSize(1100, 700);
        window.setMaximizable(true);
        window.setModal(false);
        window.show();
    }

    protected native void openLineagePanel(String ids, LineagePanel lineagePanel)/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		var browseStagingRecordsPanel = tabPanel.getItem(ids);
		if (browseStagingRecordsPanel == undefined) {
			var panel = @org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar::convertLineagePanel(Lorg/talend/mdm/webapp/browserecords/client/widget/LineagePanel;)(lineagePanel);
			tabPanel.add(panel);
		}
		tabPanel.setSelection(ids);
    }-*/;

    private native static JavaScriptObject convertLineagePanel(LineagePanel lineagePanel)/*-{
		var panel = {
			// imitate extjs's render method, really call gxt code.
			render : function(el) {
				var rootPanel = @com.google.gwt.user.client.ui.RootPanel::get(Ljava/lang/String;)(el.id);
				rootPanel.@com.google.gwt.user.client.ui.RootPanel::add(Lcom/google/gwt/user/client/ui/Widget;)(lineagePanel);
			},
			// imitate extjs's setSize method, really call gxt code.
			setSize : function(width, height) {
				lineagePanel.@org.talend.mdm.webapp.browserecords.client.widget.LineagePanel::setSize(II)(width, height);
			},
			// imitate extjs's getItemId, really return itemId of ContentPanel of GXT.
			getItemId : function() {
				return lineagePanel.@org.talend.mdm.webapp.browserecords.client.widget.LineagePanel::getItemId()();
			},
			// imitate El object of extjs
			getEl : function() {
				var el = lineagePanel.@org.talend.mdm.webapp.browserecords.client.widget.LineagePanel::getElement()();
				return {
					dom : el
				};
			},
			// imitate extjs's doLayout method, really call gxt code.
			doLayout : function() {
				return lineagePanel.@org.talend.mdm.webapp.browserecords.client.widget.LineagePanel::doLayout()();
			},
			title : function() {
				return lineagePanel.@org.talend.mdm.webapp.browserecords.client.widget.LineagePanel::getHeading()();
			}
		};
		return panel;
    }-*/;

    public boolean isFkToolBar() {
        return isFkToolBar;
    }

    public void setFkToolBar(boolean isFkToolBar) {
        this.isFkToolBar = isFkToolBar;
    }

    protected void deleteRecord() {
        List<ItemBean> list = new ArrayList<ItemBean>();
        list.add(itemBean);
        getBrowseRecordsService().checkFKIntegrity(list,
                new DeleteCallback(DeleteAction.PHYSICAL, buildPostDeleteAction(), getBrowseRecordsService()));
    }

    private void showValidateResult(JSONObject result) {
        JSONBoolean isValid = (JSONBoolean) result.get("isValid"); //$NON-NLS-1$
        if (isValid.booleanValue()) {
            MessageBox
                    .info(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages().validate_record_passed_msg(),
                            null).getDialog().setWidth(400);
        } else {
            JSONString message = (JSONString) result.get("message"); //$NON-NLS-1$
            String errorMsg = MultilanguageMessageParser.getValueByLanguage(message.stringValue(), UrlUtil.getLanguage());
            if ("".equals(errorMsg)) {
                errorMsg = message.stringValue();
            }
            MessageBox
                    .alert(MessagesFactory.getMessages().info_title(),
                            MessagesFactory.getMessages().validate_record_failed_msg(errorMsg), null).getDialog().setWidth(400);
        }
    }

    protected BrowseRecordsServiceAsync getBrowseRecordsService() {
        if (isStaging) {
            return ServiceFactory.getInstance().getStagingService();
        } else {
            return ServiceFactory.getInstance().getMasterService();
        }

    }

    protected PostDeleteAction buildPostDeleteAction() {
        if (itemsDetailPanel.isLineage()) {
            return new CloseLineageTabPostDeleteAction(ItemDetailToolBar.this, NoOpPostDeleteAction.INSTANCE);
        } else {
            return new CloseTabPostDeleteAction(ItemDetailToolBar.this, new ListRefresh(ItemDetailToolBar.this,
                    new ContainerUpdate(ItemDetailToolBar.this, NoOpPostDeleteAction.INSTANCE)));
        }

    }

    public void setReturnCriteriaFK(ReturnCriteriaFK returnCriteriaFK) {
        this.returnCriteriaFK = returnCriteriaFK;
    }

    public ReturnCriteriaFK getReturnCriteriaFK() {
        return this.returnCriteriaFK;
    }

    public boolean isStaging() {
        return isStaging;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private void executeSaveAction(boolean isClose) {
        Widget widget = itemsDetailPanel.getFirstTabWidget();
        TreeDetail treeDetail = ((ItemPanel) widget).getTree();
        if (!BrowseRecords.getSession().getAppHeader().isAutoValidate()) {
            treeDetail.makeWarning(treeDetail.getRoot());
        }
        ItemNodeModel root = treeDetail.getRootModel();
        String invalidNodeName = TreeDetailUtil.checkInvalidNode(root);
        if (invalidNodeName.isEmpty()) {
            if (operation.equalsIgnoreCase(ItemDetailToolBar.CREATE_OPERATION)
                    || operation.equalsIgnoreCase(ItemDetailToolBar.DUPLICATE_OPERATION) || TreeDetailUtil.isChangeValue(root)) {
                saveItemAndClose(isClose);
            } else {
                MessageBox.info(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages().no_change_info(), null);
            }
        } else {
            MessageBox.alert(MessagesFactory.getMessages().error_title(),
                    MessagesFactory.getMessages().validation_error(invalidNodeName), null);
        }
    }
}
