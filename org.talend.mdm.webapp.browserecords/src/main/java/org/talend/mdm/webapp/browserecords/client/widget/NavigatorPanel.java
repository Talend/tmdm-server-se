// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.browserecords.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.i18n.BaseMessagesFactory;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.BreadCrumbModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailUtil;
import org.talend.mdm.webapp.browserecords.shared.Constants;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.SimplePanel;

public class NavigatorPanel extends ContentPanel {
    
    private String NAVIGATOR_PAGESIZE = "navigator_pagesize"; //$NON-NLS-1$
    
    private BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);
    
    private int NAVIGATOR_NODE_INIT_TYPE = 0;
    
    private int NAVIGATOR_NODE_IN_ENTITY_TYPE = 1;
    
    private int NAVIGATOR_NODE_OUT_ENTITY_TYPE = 2;

    private ContentPanel navigatorPanel;

    private ContentPanel detailPanel;

    private boolean isStaging = false;

    private boolean isFkToolBar = false;

    private boolean isHierarchyCall = false;

    private String operation = ItemDetailToolBar.VIEW_OPERATION;
    
    private int pageSize = 5;
    
    private Window settingWindow;
    
    public NavigatorPanel() {
        setId(MessagesFactory.getMessages().navigator_panel_label());
        initPanel();
        registerNavigatorService();
    }

    private void initPanel() {
        setHeaderVisible(false);
        setLayout(new BorderLayout());
        setStyleAttribute("height", "100%"); //$NON-NLS-1$ //$NON-NLS-2$  
        BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST,800);
        westData.setMargins(new Margins(0, 5, 0, 0));
        westData.setSplit(true);
        westData.setFloatable(false);
        westData.setMinSize(0);
        westData.setMaxSize(7000);
        initNavigatorPanel();
        add(navigatorPanel, westData);
        
        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
        initDetailPanel();
        add(detailPanel, centerData);
        
        if (Cookies.getCookie(NAVIGATOR_PAGESIZE) != null) {
            pageSize = Integer.parseInt(Cookies.getCookie(NAVIGATOR_PAGESIZE));
        }
        
        settingWindow = new Window();
        settingWindow.setHeading(MessagesFactory.getMessages().setting_window_title());
        settingWindow.setWidth(300);
        settingWindow.setHeight(131);
        settingWindow.setResizable(false);
        settingWindow.setDraggable(false);
        settingWindow.setLayout(new FitLayout());
        settingWindow.setModal(true);
        settingWindow.setBlinkModal(true);
        
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        TableData layoutData = new TableData();
        layoutData.setPadding(10);
        Label pageSizeLabel = new Label(MessagesFactory.getMessages().page_size_field_label());
        final NumberField pageSizeField = new NumberField();
        pageSizeField.setValue(pageSize);
        pageSizeField.setValidator(validator);
        pageSizeField.setWidth(150);
        horizontalPanel.add(pageSizeLabel,layoutData);
        horizontalPanel.add(pageSizeField,layoutData);
        Button okButton = new Button(MessagesFactory.getMessages().ok_btn());
        okButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (pageSizeField.isValid()) {
                    NavigatorPanel.this.pageSize = pageSizeField.getValue().intValue();
                    Cookies.setCookie(NAVIGATOR_PAGESIZE, String.valueOf(NavigatorPanel.this.pageSize));
                    settingWindow.close();
                }
            }
        });
        Button cancelButton = new Button(MessagesFactory.getMessages().cancel_btn());
        cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                pageSizeField.setValue(pageSize);
                settingWindow.close();
            }
        });
        settingWindow.add(horizontalPanel);
        settingWindow.setButtonAlign(HorizontalAlignment.CENTER);
        settingWindow.addButton(okButton);
        settingWindow.addButton(cancelButton);
    }

    private void initNavigatorPanel() {
        navigatorPanel = new ContentPanel();
        navigatorPanel.setLayout(new FitLayout());
        navigatorPanel.setHeaderVisible(false);
        SimplePanel navigator = new SimplePanel();
        navigator.getElement().setId("navigator"); //$NON-NLS-1$
        navigatorPanel.addListener(Events.Resize, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                resizeNavigator();
            }
        });
        navigatorPanel.add(navigator);
    }

    private void initDetailPanel() {
        detailPanel = new ContentPanel();
        detailPanel.setLayout(new FitLayout());
        detailPanel.setHeading(MessagesFactory.getMessages().navigator_detailPanel_label());
    }

    public void updateDetailPanel(final String ids, final String concept) {
        detailPanel.removeAll();
        service.getItemBeanById(concept, ids, Locale.getLanguage(), new SessionAwareAsyncCallback<ItemBean>() {

            @Override
            public void onSuccess(ItemBean itemBean) {
                String[] idArr = TreeDetailUtil.parseKey(ids);
                final ItemsDetailPanel panel = ItemsDetailPanel.newInstance();
                panel.setStaging(isStaging);
                final BrowseRecordsServiceAsync brService = (BrowseRecordsServiceAsync) Registry
                        .get(BrowseRecords.BROWSERECORDS_SERVICE);
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
                                        itemPanel.getToolBar().setOutMost(true);
                                        itemPanel.getToolBar().setFkToolBar(isFkToolBar);
                                        itemPanel.getToolBar().setHierarchyCall(isHierarchyCall);

                                        List<BreadCrumbModel> breads = new ArrayList<BreadCrumbModel>();
                                        if (item != null) {
                                            breads.add(new BreadCrumbModel("", BreadCrumb.DEFAULTNAME, null, null, false)); //$NON-NLS-1$
                                            breads.add(new BreadCrumbModel(item.getConcept(), item.getLabel(), item.getIds(),
                                                    item.getDisplayPKInfo().equals(item.getLabel()) ? null : item
                                                            .getDisplayPKInfo(), true));
                                        }

                                        panel.setOutMost(true);
                                        panel.setId(item.getIds());
                                        panel.initBanner(item.getPkInfoList(), item.getDescription());
                                        panel.addTabItem(item.getLabel(), itemPanel, ItemsDetailPanel.SINGLETON, item.getIds());
                                        panel.initBreadCrumb(new BreadCrumb(breads, panel));

                                        TypeModel typeModel = viewBean.getBindingEntityModel().getMetaDataTypes()
                                                .get(item.getConcept());

                                        String tabItemId = "Navigator" + typeModel.getLabel(Locale.getLanguage()) //$NON-NLS-1$
                                                + " " + panel.getItemId(); //$NON-NLS-1$
                                        panel.setHeading(tabItemId);
                                        panel.setItemId(tabItemId
                                                + (isStaging ? Constants.BROWSE_STAGING_SUFFIX_MARK
                                                        : Constants.BROWSE_MASTER_SUFFIX_MARK));
                                        detailPanel.add(panel);
                                        layout();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
    
    public int getPageSize() {
        return this.pageSize;
    }
    
    public void showSettingWindow() {
        settingWindow.show();
    }
    
    public void sessionExpired() {
        MessageBox.alert(BaseMessagesFactory.getMessages().warning_title(), BaseMessagesFactory.getMessages()
                .session_timeout_error(), new Listener<MessageBoxEvent>() {
            public void handleEvent(MessageBoxEvent be) {
                Cookies.removeCookie("JSESSIONID"); //$NON-NLS-1$
                Cookies.removeCookie("JSESSIONIDSSO"); //$NON-NLS-1$
                com.google.gwt.user.client.Window.Location.replace(GWT.getHostPageBaseURL());
            }
        });
    }
    
    private Validator validator = new Validator() {
        @Override
        public String validate(Field<?> field, String value) {
            String valueStr = value == null ? "" : value.toString(); //$NON-NLS-1$
            boolean success = false;
            try {
                success = validatePageSize(Integer.parseInt(valueStr));
            } catch (NumberFormatException e) {
                success = false;
            }
            if (!success) {
                return BaseMessagesFactory.getMessages().page_size_notice();
            }
            return null;
        }
    };

    private boolean validatePageSize(int pageSizeNumber) {
        if (pageSizeNumber <= 0) {
            return false;
        } else {
            return true;
        }
    }
    
    public void handleNodeLabel(String jsonString,String type) {
        final int nodeType = Integer.parseInt(type);
        service.handleNavigatorNodeLabel(jsonString,Locale.getLanguage(), new SessionAwareAsyncCallback<String>() {

            @Override
            public void onSuccess(String data) {
                if (nodeType == NAVIGATOR_NODE_INIT_TYPE) {
                    initDataNode(data);
                } else if (nodeType == NAVIGATOR_NODE_IN_ENTITY_TYPE) {
                    paintInDataNode(data);
                } else if (nodeType == NAVIGATOR_NODE_OUT_ENTITY_TYPE) {
                    paintOutDataNode(data);;
                }
                
            }
        });
    }

    public static void renderPanel(String baseUrl, String ids, String concept, String cluster, ContentPanel contentPanel) {
        if (GWT.isScript()) {
            String itemId = concept + "_" + ids; //$NON-NLS-1$
            renderGwtPanel(itemId, contentPanel);
        } else {
            renderDebugPanel(contentPanel);
        }
        String restServiceUrl = baseUrl + "services/rest"; //$NON-NLS-1$
        paintNavigator(restServiceUrl, ids, concept, cluster, Locale.getLanguage());
    }

    private static void renderDebugPanel(ContentPanel contentPanel) {
        Window window = new Window();
        window.setLayout(new FitLayout());
        window.add(contentPanel);
        window.setSize(1100, 700);
        window.setMaximizable(true);
        window.setModal(false);
        window.show();
    }

    private native void registerNavigatorService()/*-{
        var instance = this;
        $wnd.amalto = $wnd.amalto || {};
        $wnd.amalto.navigator = $wnd.amalto.navigator || {};
        $wnd.amalto.navigator.Navigator = $wnd.amalto.navigator.Navigator || {};
        $wnd.amalto.navigator.Navigator.openRecord = function(ids, concept) {
            instance.@org.talend.mdm.webapp.browserecords.client.widget.NavigatorPanel::updateDetailPanel(Ljava/lang/String;Ljava/lang/String;)(ids, concept);
        }
                
        $wnd.amalto.navigator.Navigator.handleNodeLabel = function(value,type) {
            return instance.@org.talend.mdm.webapp.browserecords.client.widget.NavigatorPanel::handleNodeLabel(Ljava/lang/String;Ljava/lang/String;)(value,type);
        }
        
        $wnd.amalto.navigator.Navigator.sessionExpired = function() {
            instance.@org.talend.mdm.webapp.browserecords.client.widget.NavigatorPanel::sessionExpired()();
        }
        $wnd.amalto.navigator.Navigator.getPageSize = function() {
            return instance.@org.talend.mdm.webapp.browserecords.client.widget.NavigatorPanel::getPageSize()();
        }
        
        $wnd.amalto.navigator.Navigator.showSettingWindow = function() {
            instance.@org.talend.mdm.webapp.browserecords.client.widget.NavigatorPanel::showSettingWindow()();
        }        
    }-*/;

    public native static void paintNavigator(String restServiceUrl, String ids, String concept, String cluster, String language)/*-{
        $wnd.amalto.itemsbrowser.NavigatorPanel.initUI(restServiceUrl, ids, concept,
                cluster, language);
    }-*/;
    
    public native static void initDataNode(String data)/*-{
        $wnd.amalto.itemsbrowser.NavigatorPanel.initDataNode(data);
    }-*/;
    
    public native static void paintInDataNode(String data)/*-{
        $wnd.amalto.itemsbrowser.NavigatorPanel.paintInDataNode(data);
    }-*/;
    
    public native static void paintOutDataNode(String data)/*-{
        $wnd.amalto.itemsbrowser.NavigatorPanel.paintOutDataNode(data);
    }-*/;
    
    public native static void resizeNavigator()/*-{
        $wnd.amalto.itemsbrowser.NavigatorPanel.resize();
    }-*/;

    public native static void renderGwtPanel(String itemId, ContentPanel contentPanel)/*-{
        var tabPanel = $wnd.amalto.core.getTabPanel();
        var panel = @org.talend.mdm.webapp.browserecords.client.widget.NavigatorPanel::transferPanel(Lcom/extjs/gxt/ui/client/widget/ContentPanel;)(contentPanel);
        tabPanel.add(panel);
        tabPanel.setSelection(itemId);
    }-*/;

    private native static JavaScriptObject transferPanel(ContentPanel contentPanel)/*-{
        var panel = {
            // imitate extjs's render method, really call gxt code.
            render : function(el) {
                var rootPanel = @com.google.gwt.user.client.ui.RootPanel::get(Ljava/lang/String;)(el.id);
                rootPanel.@com.google.gwt.user.client.ui.RootPanel::add(Lcom/google/gwt/user/client/ui/Widget;)(contentPanel);
            },
            // imitate extjs's setSize method, really call gxt code.
            setSize : function(width, height) {
                contentPanel.@com.extjs.gxt.ui.client.widget.ContentPanel::setSize(II)(width, height);
            },
            // imitate extjs's getItemId, really return itemId of ContentPanel of GXT.
            getItemId : function() {
                return contentPanel.@com.extjs.gxt.ui.client.widget.ContentPanel::getItemId()();
            },
            // imitate El object of extjs
            getEl : function() {
                var el = contentPanel.@com.extjs.gxt.ui.client.widget.ContentPanel::getElement()();
                return {
                    dom : el
                };
            },
            // imitate extjs's doLayout method, really call gxt code.
            doLayout : function() {
                return contentPanel.@com.extjs.gxt.ui.client.widget.ContentPanel::doLayout()();
            },
            title : function() {
                return contentPanel.@com.extjs.gxt.ui.client.widget.ContentPanel::getHeading()();
            }
        };
        return panel;
    }-*/;
}
