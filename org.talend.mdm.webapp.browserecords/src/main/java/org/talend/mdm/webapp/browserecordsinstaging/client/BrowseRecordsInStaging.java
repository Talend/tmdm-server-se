/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecordsinstaging.client;

import org.talend.mdm.webapp.base.client.ServiceEnhancer;
import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.util.StorageProvider;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsEvents;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsService;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.BrowseStagingRecordsService;
import org.talend.mdm.webapp.browserecords.client.ServiceFactory;
import org.talend.mdm.webapp.browserecords.client.WidgetFactory;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.mvc.BrowseRecordsController;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.GenerateContainer;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsListPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsToolBar.ItemsToolBarCreator;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;
import org.talend.mdm.webapp.browserecordsinstaging.client.widget.ItemsListPanel4Staging;
import org.talend.mdm.webapp.browserecordsinstaging.client.widget.ItemsToolBar4Staging;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.state.StateManager;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class BrowseRecordsInStaging implements EntryPoint {

    public static final String BROWSERECORD_ID = "BrowseRecordsInStaging"; //$NON-NLS-1$

    private static JavaScriptObject stagingArea;

    public static JavaScriptObject getStagingArea() {
        return stagingArea;
    }

    public native void regItemDetails()/*-{
       $wnd.amalto = $wnd.amalto || {};
       $wnd.amalto.itemsbrowser = $wnd.amalto.itemsbrowser || {};
       $wnd.amalto.itemsbrowser.ItemsBrowser = $wnd.amalto.itemsbrowser.ItemsBrowser || {};
       $wnd.amalto.itemsbrowser.ItemsBrowser.editItemDetails4Staging = function(fromWhichApp, ids, entity, callback){
           var checkArgs = true;
           checkArgs = checkArgs && (arguments.length >= 3);
           checkArgs = checkArgs && (typeof fromWhichApp === "string");
           checkArgs = checkArgs && (ids.length >= 1);
           checkArgs = checkArgs && (typeof entity === "string");
           if (!checkArgs){
               throw {message: "argument format error!"};
           }
        
           var idstr = ids.join(".");
           @org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailUtil::initItemsDetailPanelById(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/Boolean;)(fromWhichApp, idstr, entity, new Boolean(false), new Boolean(false));
       };
    
       $wnd.amalto.itemsbrowser.ItemsBrowser.lineageItem = function (lineageEntities, ids, dataObject){
       var tabPanel = $wnd.amalto.core.getTabPanel();
    
       var searchEntityPanel = tabPanel.getItem("searchEntityPanel");
    
       if (searchEntityPanel) {
           tabPanel.remove(searchEntityPanel);
           searchEntityPanel.destroy();
       }
    
       searchEntityPanel = new $wnd.amalto.itemsbrowser.SearchEntityPanel({
           lineageEntities : lineageEntities,
           ids : ids,
           dataObject : dataObject,
           language : $wnd.language
       });
    
       tabPanel.add(searchEntityPanel);
    
       searchEntityPanel.show();
       searchEntityPanel.doLayout();
       searchEntityPanel.doSearchList();
       $wnd.amalto.core.doLayout();
       }
    }-*/;

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {
        ServiceDefTarget service = GWT.create(BrowseRecordsInStagingService.class);
        ServiceEnhancer.customizeService(service);
        Registry.register(BrowseRecords.BROWSERECORDS_SERVICE, service);

        ServiceDefTarget browseMasterRecordsService = GWT.create(BrowseRecordsService.class);
        ServiceEnhancer.customizeService(browseMasterRecordsService);
        Registry.register(BrowseRecords.BROWSEMASTERRECORDS_SERVICE, browseMasterRecordsService);

        ServiceDefTarget browseStagingRecordsService = GWT.create(BrowseStagingRecordsService.class);
        ServiceEnhancer.customizeService(browseStagingRecordsService);
        Registry.register(BrowseRecords.BROWSESTAGINGRECORDS_SERVICE, browseStagingRecordsService);

        initCurrentStateProvicer();
        registerPubService();

        // register user session
        Registry.register(BrowseRecords.USER_SESSION, new UserSession());

        // add controller to dispatcher
        final Dispatcher dispatcher = Dispatcher.get();
        dispatcher.addController(new BrowseRecordsController());

        regItemDetails();
        Log.setUncaughtExceptionHandler();
    }

    private void initExtendedEnviroment() {
        ServiceFactory.initialize(new ServiceFactory4Staging());
        WidgetFactory.initialize(new WidgetFactory4Staging());
        ItemsListPanel.initialize(new ItemsListPanel4Staging());
        ItemsToolBar.initialize(new ItemsToolBarCreator() {

            @Override
            public ItemsToolBar newInstance() {
                return new ItemsToolBar4Staging();
            }
        });
    }

    private void initCurrentStateProvicer() {
        StorageProvider storageProvider = StorageProvider.newInstanceIfSupported();
        if (storageProvider != null) {
            StateManager.get().setProvider(storageProvider);
        }
    }

    private static BrowseRecordsServiceAsync getItemService() {

        BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);
        return service;

    }

    public void refreshGrid() {
        ButtonEvent be = new ButtonEvent(ItemsToolBar.getInstance().searchButton);
        ItemsToolBar.getInstance().searchButton.fireEvent(Events.Select, be);
    }

    public void showTreeDetailPanel(final String concept, final String ids) {

        final ItemsDetailPanel panel = ItemsDetailPanel.newInstance();
        getItemService().getItemBeanById(concept,
                ids.split("\\."), Locale.getLanguage(), new SessionAwareAsyncCallback<ItemBean>() { //$NON-NLS-1$

                    @Override
                    public void onSuccess(final ItemBean item) {
                        getItemService().getExsitedViewName(concept, new SessionAwareAsyncCallback<String>() {

                            @Override
                            public void onSuccess(String viewName) {
                                getItemService().getView(viewName, Locale.getLanguage(),
                                        new SessionAwareAsyncCallback<ViewBean>() {

                                            @Override
                                            public void onSuccess(ViewBean viewBean) {
                                                ItemPanel itemPanel = new ItemPanel(viewBean, item,
                                                        ItemDetailToolBar.VIEW_OPERATION, panel);
                                                itemPanel.getToolBar().setHierarchyCall(true);
                                                itemPanel.setItemId(concept + "_" + ids); //$NON-NLS-1$
                                                renderPubTreeDetailPanel(itemPanel.getItemId(), itemPanel);
                                            }

                                        });
                            }

                        });
                    }
                });
    }

    public native void renderPubTreeDetailPanel(String itemId, ItemPanel itemPanel)/*-{
		var tabPanel = $wnd.amalto.hierarchy.Hierarchy.getTabPanel();
		var panel = tabPanel.getItem(itemId);
		if (panel == undefined || panel == null) {
			panel = this.@org.talend.mdm.webapp.browserecordsinstaging.client.BrowseRecordsInStaging::wrapTreeDetailPanel(Lorg/talend/mdm/webapp/browserecords/client/widget/ItemPanel;)(itemPanel);
			tabPanel.add(panel);
		}
		tabPanel.setSelection(panel.getItemId());
    }-*/;

    public native JavaScriptObject wrapTreeDetailPanel(ItemPanel itemPanel)/*-{
		var panel = {
			// imitate extjs's render method, really call gxt code.
			render : function(el) {
				var rootPanel = @com.google.gwt.user.client.ui.RootPanel::get(Ljava/lang/String;)(el.id);
				rootPanel.@com.google.gwt.user.client.ui.RootPanel::add(Lcom/google/gwt/user/client/ui/Widget;)(itemPanel);
			},
			// imitate extjs's setSize method, really call gxt code.
			setSize : function(width, height) {
				itemPanel.@org.talend.mdm.webapp.browserecords.client.widget.ItemPanel::setSize(II)(width, height);
			},
			// imitate extjs's getItemId, really return itemId of ContentPanel of GXT.
			getItemId : function() {
				return itemPanel.@org.talend.mdm.webapp.browserecords.client.widget.ItemPanel::getItemId()();
			},
			// imitate El object of extjs
			getEl : function() {
				var el = itemPanel.@org.talend.mdm.webapp.browserecords.client.widget.ItemPanel::getElement()();
				return {
					dom : el
				};
			},
			// imitate extjs's doLayout method, really call gxt code.
			doLayout : function() {
				return itemPanel.@org.talend.mdm.webapp.browserecords.client.widget.ItemPanel::doLayout()();
			}
		};
		return panel;
    }-*/;

    public static UserSession getSession() {

        return Registry.get(BrowseRecords.USER_SESSION);
    }

    private native void registerPubService()/*-{
		var instance = this;
		$wnd.amalto.browserecords = $wnd.amalto.browserecords || {};
		$wnd.amalto.browserecords.BrowseRecordsInStaging = function() {

			function initUI(stagingarea) {
				instance.@org.talend.mdm.webapp.browserecordsinstaging.client.BrowseRecordsInStaging::initUI(Lcom/google/gwt/core/client/JavaScriptObject;)(stagingarea);
			}

			function showTreeDetailPanel(concept, ids) {
				instance.@org.talend.mdm.webapp.browserecordsinstaging.client.BrowseRecordsInStaging::showTreeDetailPanel(Ljava/lang/String;Ljava/lang/String;)(concept, ids);
			}

			function refreshGrid() {
				var tabPanel = $wnd.amalto.core.getTabPanel();
				var panel = tabPanel.getItem("Browse Records");
				if (panel != undefined) {
					tabPanel.setSelection(panel.getItemId());
				}
				instance.@org.talend.mdm.webapp.browserecordsinstaging.client.BrowseRecordsInStaging::refreshGrid()();
			}

			return {
				init : function(stagingarea) {
					initUI(stagingarea);
				},
				showTreeDetailPanel : function(concept, ids) {
					showTreeDetailPanel(concept, ids);
				},
				refreshGrid : function() {
					refreshGrid();
				}
			}
		}();
    }-*/;

    private native void _initUI(JavaScriptObject stagingarea)/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();

		var panel = tabPanel.getItem("BrowseRecordsInStaging");
		if (panel == undefined) {
			var msg = @org.talend.mdm.webapp.browserecordsinstaging.client.i18n.MessagesFactory::getMessages()();
			var heading = msg.@org.talend.mdm.webapp.browserecordsinstaging.client.i18n.BrowseRecordsInStagingMessages::staging_browse_record_title()();
			@org.talend.mdm.webapp.browserecords.client.widget.GenerateContainer::generateContentPanel(Ljava/lang/String;Ljava/lang/String;)("BrowseRecordsInStaging", heading);
			panel = this.@org.talend.mdm.webapp.browserecordsinstaging.client.BrowseRecordsInStaging::createPanel()();
			tabPanel.add(panel);
		}
		tabPanel.setSelection(panel.getItemId());
    }-*/;

    native JavaScriptObject createPanel()/*-{
		var instance = this;
		// imitate extjs Panel
		var panel = {
			// imitate extjs's render method, really call gxt code.
			render : function(el) {
				instance.@org.talend.mdm.webapp.browserecordsinstaging.client.BrowseRecordsInStaging::renderContent(Ljava/lang/String;)(el.id);
			},
			// imitate extjs's setSize method, really call gxt code.
			setSize : function(width, height) {
				var cp = @org.talend.mdm.webapp.browserecords.client.widget.GenerateContainer::getContentPanel()();
				cp.@com.extjs.gxt.ui.client.widget.ContentPanel::setSize(II)(width, height);
			},
			// imitate extjs's getItemId, really return itemId of ContentPanel of GXT.
			getItemId : function() {
				var cp = @org.talend.mdm.webapp.browserecords.client.widget.GenerateContainer::getContentPanel()();
				return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getItemId()();
			},
			// imitate El object of extjs
			getEl : function() {
				var cp = @org.talend.mdm.webapp.browserecords.client.widget.GenerateContainer::getContentPanel()();
				var el = cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getElement()();
				return {
					dom : el
				};
			},
			// imitate extjs's doLayout method, really call gxt code.
			doLayout : function() {
				var cp = @org.talend.mdm.webapp.browserecords.client.widget.GenerateContainer::getContentPanel()();
				return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::doLayout()();
			},
			title : function() {
				var cp = @org.talend.mdm.webapp.browserecords.client.widget.GenerateContainer::getContentPanel()();
				return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getHeading()();
			}
		};
		return panel;
    }-*/;

    public void renderContent(final String contentId) {
        onModuleRender();
        initExtendedEnviroment();
        final ContentPanel content = GenerateContainer.getContentPanel();
        if (GWT.isScript()) {
            RootPanel panel = RootPanel.get(contentId);
            panel.add(content);
        } else {
            final Element element = DOM.getElementById(contentId);
            SimplePanel panel = new SimplePanel() {

                @Override
                protected void setElement(Element elem) {
                    super.setElement(element);
                }
            };
            RootPanel rootPanel = RootPanel.get();
            rootPanel.clear();
            rootPanel.add(panel);
            panel.add(content);
        }
    }

    public void initUI(JavaScriptObject stagingarea) {
        _initUI(stagingarea);
    }

    private void onModuleRender() {
        getItemService().getAppHeader(new SessionAwareAsyncCallback<AppHeader>() {

            @Override
            public void onSuccess(AppHeader header) {
                if (header.getDatacluster() == null || header.getDatamodel() == null) {
                    MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                            .data_model_not_specified(), null);
                    return;
                }
                getSession().put(UserSession.APP_HEADER, header);
                Dispatcher dispatcher = Dispatcher.get();
                dispatcher.dispatch(BrowseRecordsEvents.InitFrame);
            }
        });
    }
}
