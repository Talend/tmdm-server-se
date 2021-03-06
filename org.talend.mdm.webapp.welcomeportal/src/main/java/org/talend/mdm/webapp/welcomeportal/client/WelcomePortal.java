/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.client;

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.base.client.ServiceEnhancer;
import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.shared.AppHeader;
import org.talend.mdm.webapp.general.client.GeneralService;
import org.talend.mdm.webapp.general.client.GeneralServiceAsync;
import org.talend.mdm.webapp.general.model.MenuBean;
import org.talend.mdm.webapp.welcomeportal.client.mvc.WelcomePortalController;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WelcomePortal implements EntryPoint {

    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    public final static String WELCOMEPORTAL_SERVICE = "WelcomePortalService"; //$NON-NLS-1$

    public final static String BROWSECONTEXT = "browserecords"; //$NON-NLS-1$

    public final static String BROWSEAPP = "BrowseRecords"; //$NON-NLS-1$

    public final static String JOURNALCONTEXT = "journal"; //$NON-NLS-1$

    public final static String JOURNALAPP = "Journal"; //$NON-NLS-1$

    public final static String WELCOMEPORTAL_ID = "Welcome"; //$NON-NLS-1$

    public static final String SEARCHCONTEXT = "search"; //$NON-NLS-1$

    public static final String SEARCHCONTEXTAPP = "Search"; //$NON-NLS-1$

    public static final String APP_HEADER = "appHeader"; //$NON-NLS-1$

    public static final String MENUS = "menus"; //$NON-NLS-1$

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {
        XDOM.setAutoIdPrefix(GWT.getModuleName() + "-" + XDOM.getAutoIdPrefix()); //$NON-NLS-1$
        registerPubService();
        // log setting
        Log.setUncaughtExceptionHandler();

        ServiceDefTarget service = GWT.create(WelcomePortalService.class);
        ServiceEnhancer.customizeService(service);
        Registry.register(WELCOMEPORTAL_SERVICE, service);

        // add controller to dispatcher
        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.addController(new WelcomePortalController());

        ((WelcomePortalServiceAsync) service).getAppHeader(new SessionAwareAsyncCallback<AppHeader>() {

            @Override
            public void onSuccess(AppHeader header) {
                Registry.register(APP_HEADER, header);
            }
        });

        ServiceDefTarget generalService = GWT.create(GeneralService.class);
        ServiceEnhancer.customizeService(generalService);
        ((GeneralServiceAsync) generalService).getMenuList(UrlUtil.getLanguage(),
                new SessionAwareAsyncCallback<List<MenuBean>>() {

                    @Override
                    public void onSuccess(List<MenuBean> menusList) {
                        List<String> menuApplicationList = new ArrayList<String>();
                        for (MenuBean menuBean : menusList) {
                            menuApplicationList.add(menuBean.getApplication());
                        }
                        Registry.register(MENUS, menuApplicationList);
                    }
                });

    }

    private native void registerPubService()/*-{
        var instance = this;
        $wnd.amalto.welcomeportal = {};
        $wnd.amalto.welcomeportal.WelcomePortal = function() {

            function initUI() {
                instance.@org.talend.mdm.webapp.welcomeportal.client.WelcomePortal::initUI()();
            }

            return {
                init : function() {
                    initUI();
                }
            }
        }();

        $wnd.amalto.core.refreshPortal = function(portalConfig) {
            instance.@org.talend.mdm.webapp.welcomeportal.client.WelcomePortal::refreshPortal(Ljava/lang/String;)(portalConfig);
        };
    }-*/;

    private native void _initUI()/*-{
        var instance = this;

        var tabPanel = $wnd.amalto.core.getTabPanel();
        var panel = tabPanel.getItem("Welcome");
        if (panel == undefined) {
            @org.talend.mdm.webapp.welcomeportal.client.GenerateContainer::generateContentPanel()();
            panel = this.@org.talend.mdm.webapp.welcomeportal.client.WelcomePortal::createPanel()();
            tabPanel.add(panel);
        } else {
            this.@org.talend.mdm.webapp.welcomeportal.client.WelcomePortal::refresh()();
        }
        tabPanel.setSelection(panel.getItemId());

        var removeTabEvent = function(tabPanel, tabItem) {
            if (tabItem.getId() == "Welcome") {
                instance.@org.talend.mdm.webapp.welcomeportal.client.WelcomePortal::removeWelcomePortal()();
            }
            return true;
        };
        tabPanel.on("beforeremove", removeTabEvent);
    }-*/;

    native JavaScriptObject createPanel()/*-{
        var instance = this;
        var panel = {
            render : function(el) {
                instance.@org.talend.mdm.webapp.welcomeportal.client.WelcomePortal::renderContent(Ljava/lang/String;)(el.id);
            },
            setSize : function(width, height) {
                var cp = @org.talend.mdm.webapp.welcomeportal.client.GenerateContainer::getContentPanel()();
                cp.@com.extjs.gxt.ui.client.widget.ContentPanel::setSize(II)(width, height);
            },
            getItemId : function() {
                var cp = @org.talend.mdm.webapp.welcomeportal.client.GenerateContainer::getContentPanel()();
                return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getItemId()();
            },
            getEl : function() {
                var cp = @org.talend.mdm.webapp.welcomeportal.client.GenerateContainer::getContentPanel()();
                var el = cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getElement()();
                return {
                    dom : el
                };
            },
            doLayout : function() {
                var cp = @org.talend.mdm.webapp.welcomeportal.client.GenerateContainer::getContentPanel()();
                return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::doLayout()();
            },
            title : function() {
                var cp = @org.talend.mdm.webapp.welcomeportal.client.GenerateContainer::getContentPanel()();
                return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getHeading()();
            }
        };
        return panel;
    }-*/;

    public void renderContent(final String contentId) {
        onModuleRender();

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

    public void initUI() {
        _initUI();
    }

    public void refresh() {
        Dispatcher dispatcher = Dispatcher.get();
        AppEvent event = new AppEvent(WelcomePortalEvents.RefreshPortlet);
        dispatcher.getControllers().get(0).handleEvent(event);
    }

    public void refreshPortal(String portalConfig) {
        Dispatcher dispatcher = Dispatcher.get();
        AppEvent event = new AppEvent(WelcomePortalEvents.RefreshPortal, portalConfig);
        dispatcher.getControllers().get(0).handleEvent(event);
    }

    public void removeWelcomePortal() {
        Dispatcher dispatcher = Dispatcher.get();
        ((WelcomePortalController) dispatcher.getControllers().get(0)).removeWelcomePortal();
    }

    private void onModuleRender() {
        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.dispatch(WelcomePortalEvents.InitFrame);
    }
}
