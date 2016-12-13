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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.BrowseStagingRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.BreadCrumbModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.BreadCrumb;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsListPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsMainTabPanel;
import org.talend.mdm.webapp.browserecords.client.widget.TabItemListener;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.Constants;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * TreeDetail tool class
 */
public class TreeDetailUtil {

    public static Widget createWidget(final ItemNodeModel itemNode, final ViewBean viewBean, Map<String, Field<?>> fieldMap,
            ClickHandler h, ItemsDetailPanel itemsDetailPanel) {
        return createWidget(itemNode, viewBean, fieldMap, h, null, itemsDetailPanel);
    }

    public static MultiOccurrenceChangeItem createWidget(final ItemNodeModel itemNode, final ViewBean viewBean,
            Map<String, Field<?>> fieldMap, ClickHandler h, String operation, final ItemsDetailPanel itemsDetailPanel) {
        return new MultiOccurrenceChangeItem(itemNode, viewBean, fieldMap, operation, itemsDetailPanel);
    }

    public static void setAppHeader() {
        if (BrowseRecords.getSession().getAppHeader() == null) {
            final BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry
                    .get(BrowseRecords.BROWSERECORDS_SERVICE);

            service.getAppHeader(new SessionAwareAsyncCallback<AppHeader>() {

                @Override
                public void onSuccess(AppHeader header) {
                    BrowseRecords.getSession().put(UserSession.APP_HEADER, header);
                }
            });
        }
    }

    public static void initItemsDetailPanelById(final String fromWhichApp, String ids, final String concept,
            final Boolean isFkToolBar, final Boolean isHierarchyCall) {
        setAppHeader();
        initItemsDetailPanelById(fromWhichApp, ids, concept, isFkToolBar, isHierarchyCall, ItemDetailToolBar.VIEW_OPERATION);
    }

    public static void initItemsDetailPanelById(final String fromWhichApp, String ids, final String concept,
            final Boolean isFkToolBar, final Boolean isHierarchyCall, Boolean isStaging) {
        setAppHeader();
        initItemsDetailPanelById(fromWhichApp, ids, concept, isFkToolBar, isHierarchyCall, ItemDetailToolBar.VIEW_OPERATION,
                isStaging);
    }

    public static void initItemsDetailPanelById(final String fromWhichApp, String ids, final String concept,
            final Boolean isFkToolBar, final Boolean isHierarchyCall, final String operation) {
        String[] idArr = parseKey(ids);
        final ItemsDetailPanel panel = ItemsDetailPanel.newInstance();
        final BrowseRecordsServiceAsync brService = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);
        brService.getItemBeanById(concept, idArr, Locale.getLanguage(), new SessionAwareAsyncCallback<ItemBean>() {

            @Override
            public void onSuccess(final ItemBean item) {

                brService.getExsitedViewName(concept, new SessionAwareAsyncCallback<String>() {

                    @Override
                    public void onSuccess(String viewName) {
                        brService.getView(viewName, Locale.getLanguage(), new SessionAwareAsyncCallback<ViewBean>() {

                            @Override
                            public void onSuccess(ViewBean viewBean) {
                                ItemPanel itemPanel = new ItemPanel(viewBean, item, operation, panel);
                                initDetailPanel(viewBean, panel, itemPanel, item, fromWhichApp, isFkToolBar, isHierarchyCall,
                                        false);
                            }

                        });
                    }
                });
            }
        });
    }

    public static void initItemsDetailPanelById(final String fromWhichApp, String ids, final String concept,
            final Boolean isFkToolBar, final Boolean isHierarchyCall, final String operation, final boolean isStaging) {
        String[] idArr = parseKey(ids);
        final ItemsDetailPanel panel = ItemsDetailPanel.newInstance();
        panel.setStaging(isStaging);
        final BrowseRecordsServiceAsync brService = getBrowseRecordsService(isStaging);
        brService.getItemBeanById(concept, idArr, Locale.getLanguage(), new SessionAwareAsyncCallback<ItemBean>() {

            @Override
            public void onSuccess(final ItemBean item) {
                brService.getExsitedViewName(concept, new SessionAwareAsyncCallback<String>() {

                    @Override
                    public void onSuccess(String viewName) {
                        brService.getView(viewName, Locale.getLanguage(), new SessionAwareAsyncCallback<ViewBean>() {

                            @Override
                            public void onSuccess(ViewBean viewBean) {
                                ItemPanel itemPanel = new ItemPanel(isStaging, viewBean, item, operation, panel);
                                initDetailPanel(viewBean, panel, itemPanel, item, fromWhichApp, isFkToolBar, isHierarchyCall,
                                        isStaging);
                            }

                        });
                    }
                });
            }
        });
    }

    private static void initDetailPanel(ViewBean viewBean, ItemsDetailPanel panel, ItemPanel itemPanel, ItemBean item,
            String fromWhichApp, Boolean isFkToolBar, Boolean isHierarchyCall, boolean isStaging) {
        // TMDM-7760: if the itemPanel opened from Hierarchy, then set its toolBar's outMost to false
        itemPanel.getToolBar().setOutMost(true);
        itemPanel.getToolBar().setFkToolBar(isFkToolBar);
        itemPanel.getToolBar().setHierarchyCall(isHierarchyCall);

        List<BreadCrumbModel> breads = new ArrayList<BreadCrumbModel>();
        if (item != null) {
            breads.add(new BreadCrumbModel("", BreadCrumb.DEFAULTNAME, null, null, false)); //$NON-NLS-1$
            breads.add(new BreadCrumbModel(item.getConcept(), item.getLabel(), item.getIds(), item.getDisplayPKInfo().equals(
                    item.getLabel()) ? null : item.getDisplayPKInfo(), true));
        }

        panel.setOutMost(true);
        panel.setId(item.getIds());
        panel.initBanner(item.getPkInfoList(), item.getDescription());
        panel.addTabItem(item.getLabel(), itemPanel, ItemsDetailPanel.SINGLETON, item.getIds());
        panel.initBreadCrumb(new BreadCrumb(breads, panel));

        TypeModel typeModel = viewBean.getBindingEntityModel().getMetaDataTypes().get(item.getConcept());

        String tabItemId = fromWhichApp + typeModel.getLabel(Locale.getLanguage()) + " " + panel.getItemId(); //$NON-NLS-1$
        panel.setHeading(tabItemId);
        panel.setItemId(tabItemId + (isStaging ? Constants.BROWSE_STAGING_SUFFIX_MARK : Constants.BROWSE_MASTER_SUFFIX_MARK));
        renderTreeDetailPanel(tabItemId, panel);
        CommonUtil.setCurrentCachedEntity(item.getConcept() + item.getIds() + panel.isOutMost(), itemPanel);
    }

    public static String[] parseKey(String keyStr) {
        String[] ids = keyStr.split("\\."); //$NON-NLS-1$

        if (keyStr.endsWith(".")) { //$NON-NLS-1$
            String[] idsPlus = new String[ids.length + 1];
            for (int i = 0; i < ids.length; i++) {
                idsPlus[i] = ids[i];
            }
            idsPlus[ids.length] = ""; //$NON-NLS-1$
            return idsPlus;
        } else {
            return ids;
        }
    }

    public static void initItemsDetailPanelByItemPanel(ViewBean viewBean, ItemBean itemBean, boolean isFkToolBar,
            boolean isHierarchyCall, boolean isOutMost) {

        final ItemsDetailPanel itemsDetailPanel = ItemsDetailPanel.newInstance();
        itemsDetailPanel.setOutMost(isOutMost);

        ItemPanel itemPanel = new ItemPanel(viewBean, itemBean, ItemDetailToolBar.DUPLICATE_OPERATION, itemsDetailPanel);
        itemPanel.getToolBar().setOutMost(isOutMost);
        itemPanel.getToolBar().setFkToolBar(isFkToolBar);
        itemPanel.getToolBar().setHierarchyCall(isHierarchyCall);

        List<BreadCrumbModel> breads = new ArrayList<BreadCrumbModel>();
        if (itemBean != null) {
            breads.add(new BreadCrumbModel("", BreadCrumb.DEFAULTNAME, null, null, false)); //$NON-NLS-1$
            breads.add(new BreadCrumbModel(itemBean.getConcept(), itemBean.getLabel(), itemBean.getIds(), itemBean
                    .getDisplayPKInfo().equals(itemBean.getLabel()) ? null : itemBean.getDisplayPKInfo(), true));
            itemsDetailPanel.setId(itemBean.getIds());
            itemsDetailPanel.initBanner(itemBean.getPkInfoList(), itemBean.getDescription());
            itemsDetailPanel.addTabItem(itemBean.getLabel(), itemPanel, ItemsDetailPanel.SINGLETON, itemBean.getIds());
            itemsDetailPanel.initBreadCrumb(new BreadCrumb(breads, itemsDetailPanel));

            if (isOutMost) {
                TypeModel typeModel = viewBean.getBindingEntityModel().getMetaDataTypes().get(itemBean.getConcept());
                String tabItemId = typeModel.getLabel(Locale.getLanguage()) + " " + new Date().getTime(); //$NON-NLS-1$
                itemsDetailPanel.setHeading(typeModel.getLabel(Locale.getLanguage()));
                itemsDetailPanel.setItemId(tabItemId);
                renderTreeDetailPanel(tabItemId, itemsDetailPanel);
            } else {
                ItemsMainTabPanel.getInstance().addMainTabItem(itemBean.getLabel(), itemsDetailPanel, itemBean.getConcept());
            }
            CommonUtil.setCurrentCachedEntity(itemBean.getConcept() + itemsDetailPanel.isOutMost(), itemPanel);
        }
    }

    public static void initItemsDetailPanelByItemPanel(ViewBean viewBean, ItemBean itemBean, boolean isFkToolBar,
            boolean isHierarchyCall, boolean isOutMost, boolean isStaging) {

        final ItemsDetailPanel itemsDetailPanel = ItemsDetailPanel.newInstance();
        itemsDetailPanel.setStaging(isStaging);
        itemsDetailPanel.setOutMost(isOutMost);

        ItemPanel itemPanel = new ItemPanel(isStaging, viewBean, itemBean, ItemDetailToolBar.DUPLICATE_OPERATION,
                itemsDetailPanel);
        itemPanel.getToolBar().setOutMost(isOutMost);
        itemPanel.getToolBar().setFkToolBar(isFkToolBar);
        itemPanel.getToolBar().setHierarchyCall(isHierarchyCall);

        List<BreadCrumbModel> breads = new ArrayList<BreadCrumbModel>();
        if (itemBean != null) {
            breads.add(new BreadCrumbModel("", BreadCrumb.DEFAULTNAME, null, null, false)); //$NON-NLS-1$
            breads.add(new BreadCrumbModel(itemBean.getConcept(), itemBean.getLabel(), itemBean.getIds(), itemBean
                    .getDisplayPKInfo().equals(itemBean.getLabel()) ? null : itemBean.getDisplayPKInfo(), true));
            itemsDetailPanel.setId(itemBean.getIds());
            itemsDetailPanel.initBanner(itemBean.getPkInfoList(), itemBean.getDescription());
            itemsDetailPanel.addTabItem(itemBean.getLabel(), itemPanel, ItemsDetailPanel.SINGLETON, itemBean.getIds());
            itemsDetailPanel.initBreadCrumb(new BreadCrumb(breads, itemsDetailPanel));

            if (isOutMost) {
                TypeModel typeModel = viewBean.getBindingEntityModel().getMetaDataTypes().get(itemBean.getConcept());
                String tabItemId = typeModel.getLabel(Locale.getLanguage()) + " " + new Date().getTime(); //$NON-NLS-1$
                itemsDetailPanel.setHeading(typeModel.getLabel(Locale.getLanguage()));
                itemsDetailPanel.setItemId(tabItemId);
                renderTreeDetailPanel(tabItemId, itemsDetailPanel);
            } else {
                ItemsMainTabPanel.getInstance().addMainTabItem(itemBean.getLabel(), itemsDetailPanel, itemBean.getConcept());
            }
            CommonUtil.setCurrentCachedEntity(itemBean.getConcept() + itemsDetailPanel.isOutMost(), itemPanel);
        }
    }

    /**
     * MessageBox will be displayed when item is updated <br>
     * <li>message info: Current record {0} has been modified. Are you sure you want to close the tab now ?
     */
    public static void checkRecord(final TabItem item, final ItemsDetailPanel itemsDetailPanel, final TabItemListener listener,
            final JavaScriptObject handler) {
        boolean isChangeCurrentRecord;
        if (itemsDetailPanel != null && itemsDetailPanel.getPrimaryKeyTabWidget() != null) {
            // get tree root node from the primary key tab
            ItemPanel itemPanel = (ItemPanel) itemsDetailPanel.getPrimaryKeyTabWidget();
            final ItemDetailToolBar toolBar = itemPanel.getToolBar();
            if (itemPanel.getOperation().equals(ItemDetailToolBar.VIEW_OPERATION)
                    || itemPanel.getOperation().equals(ItemDetailToolBar.CREATE_OPERATION)
                    || itemPanel.getOperation().equals(ItemDetailToolBar.DUPLICATE_OPERATION)) {
                ItemNodeModel root = itemPanel.getTree().getRootModel();
                isChangeCurrentRecord = root != null ? TreeDetailUtil.isChangeValue(root) : false;
                if (isChangeCurrentRecord) {
                    MessageBox msgBox = MessageBox.confirm(MessagesFactory.getMessages().confirm_title(), MessagesFactory
                            .getMessages().msg_confirm_close_tab(root.getLabel()), new Listener<MessageBoxEvent>() {

                        @Override
                        public void handleEvent(MessageBoxEvent be) {
                            if (Dialog.YES.equals(be.getButtonClicked().getItemId())) {
                                if (listener != null) {
                                    closeTabItem(item, toolBar, listener);
                                } else {
                                    closeOutTabItem(itemsDetailPanel.getItemId(), handler);
                                }
                            } else {
                                return;
                            }
                        }
                    });
                    msgBox.getDialog().setWidth(550);
                    return;
                } else {
                    if (listener != null) {
                        closeTabItem(item, toolBar, listener);
                    } else {
                        closeOutTabItem(itemsDetailPanel.getItemId(), handler);
                    }
                }
            } else {
                if (listener != null) {
                    closeTabItem(item, toolBar, listener);
                } else {
                    closeOutTabItem(itemsDetailPanel.getItemId(), handler);
                }
            }
        }
    }

    private static void closeTabItem(TabItem item, ItemDetailToolBar toolBar, TabItemListener listener) {
        if (toolBar != null && !toolBar.isOutMost() && !toolBar.isHierarchyCall() && !toolBar.isFkToolBar()) {
            ItemsListPanel.getInstance().deSelectCurrentItem();
        }
        listener.isConfirmedTabClose = true;
        item.close();
    }

    public native static void closeOutTabItem(String itemId, JavaScriptObject removeTabEvent)/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		tabPanel.un("beforeremove", removeTabEvent);
		tabPanel.remove(itemId);
    }-*/;

    public static void renderTreeDetailPanel(String itemId, ItemsDetailPanel detailPanel) {
        if (GWT.isScript()) {
            renderGwtTreeDetailPanel(itemId, detailPanel);
        } else {
            renderDebugTreeDetailPanel(itemId, detailPanel);
        }
    }

    private static void renderDebugTreeDetailPanel(String itemId, ItemsDetailPanel source) {
        Window window = new Window();
        window.setLayout(new FitLayout());
        window.add(source);
        window.setSize(1100, 700);
        window.setMaximizable(true);
        window.setModal(false);
        window.show();
    }

    public native static void renderGwtTreeDetailPanel(String itemId, ItemsDetailPanel detailPanel)/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		var panel = @org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailUtil::transferTreeDetailPanel(Lorg/talend/mdm/webapp/browserecords/client/widget/ItemsDetailPanel;)(detailPanel);
		var removeTabEvent = function(tabPanel, tabItem) {
			if (itemId == tabItem.getId()) {
				@org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailUtil::checkRecord(Lcom/extjs/gxt/ui/client/widget/TabItem;Lorg/talend/mdm/webapp/browserecords/client/widget/ItemsDetailPanel;Lorg/talend/mdm/webapp/browserecords/client/widget/TabItemListener;Lcom/google/gwt/core/client/JavaScriptObject;)(null,detailPanel,null,removeTabEvent);
				return false;
			} else {
				return true;
			}
		};
		tabPanel.on("beforeremove", removeTabEvent);
		tabPanel.add(panel);
		tabPanel.setSelection(itemId);
    }-*/;

    private native static JavaScriptObject transferTreeDetailPanel(ItemsDetailPanel itemDetailPanel)/*-{
		var panel = {
			// imitate extjs's render method, really call gxt code.
			render : function(el) {
				var rootPanel = @com.google.gwt.user.client.ui.RootPanel::get(Ljava/lang/String;)(el.id);
				rootPanel.@com.google.gwt.user.client.ui.RootPanel::add(Lcom/google/gwt/user/client/ui/Widget;)(itemDetailPanel);
			},
			// imitate extjs's setSize method, really call gxt code.
			setSize : function(width, height) {
				itemDetailPanel.@org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel::setSize(II)(width, height);
			},
			// imitate extjs's getItemId, really return itemId of ContentPanel of GXT.
			getItemId : function() {
				return itemDetailPanel.@org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel::getItemId()();
			},
			// imitate El object of extjs
			getEl : function() {
				var el = itemDetailPanel.@org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel::getElement()();
				return {
					dom : el
				};
			},
			// imitate extjs's doLayout method, really call gxt code.
			doLayout : function() {
				return itemDetailPanel.@org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel::doLayout()();
			},
			title : function() {
				return itemDetailPanel.@org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel::getHeading()();
			}
		};
		return panel;
    }-*/;

    public static boolean isChangeValue(ItemNodeModel model) {
        if (model.isChangeValue()) {
            return true;
        }
        for (ModelData node : model.getChildren()) {
            if (isChangeValue((ItemNodeModel) node)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEditValue(EntityModel entityModel, ItemNodeModel model) {
        String typePath = model.getTypePath();
        TypeModel typeModel = entityModel.getMetaDataTypes().get(typePath);
        if ((typeModel.isSimpleType() || (!typeModel.isSimpleType() && ((ComplexTypeModel) typeModel).getReusableComplexTypes()
                .size() > 0))
 && !model.isKey() && model.isMassUpdate() && model.isEdited()) {
            return true;
        }
        for (ModelData node : model.getChildren()) {
            if (isEditValue(entityModel, (ItemNodeModel) node)) {
                return true;
            }
        }
        return false;
    }

    public static String checkInvalidNode(ItemNodeModel model) {
        if (!model.isValid() && model.getChildCount() == 0 && model.isRendered()) {
            return model.getLabel();
        }
        for (ModelData node : model.getChildren()) {
            if (!checkInvalidNode((ItemNodeModel) node).isEmpty()) {
                return checkInvalidNode((ItemNodeModel) node);
            }
        }
        return ""; //$NON-NLS-1$
    }

    private static BrowseRecordsServiceAsync getBrowseRecordsService(boolean isStaging) {
        if (isStaging) {
            return (BrowseStagingRecordsServiceAsync) Registry.get(BrowseRecords.BROWSESTAGINGRECORDS_SERVICE);
        } else {
            return (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSEMASTERRECORDS_SERVICE);
        }
    }
}
