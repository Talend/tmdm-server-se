/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.recyclebin.client;

import org.talend.mdm.webapp.base.client.ServiceEnhancer;
import org.talend.mdm.webapp.recyclebin.client.mvc.RecycleBinController;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.core.XDOM;
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
public class RecycleBin implements EntryPoint {

    /**
     * Create a remote service proxy to talk to the server-side RecycleBin service.
     */
    public static final String RECYCLEBIN_SERVICE = "RecycleBinService"; //$NON-NLS-1$  

    public static final String RECYCLEBIN_ID = "Recycle Bin"; //$NON-NLS-1$  

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {
        XDOM.setAutoIdPrefix(GWT.getModuleName() + "-" + XDOM.getAutoIdPrefix()); //$NON-NLS-1$
        registerPubService();
        // log setting
        Log.setUncaughtExceptionHandler();

        ServiceDefTarget service = GWT.create(RecycleBinService.class);
        ServiceEnhancer.customizeService(service);
        Registry.register(RECYCLEBIN_SERVICE, service);

        // add controller to dispatcher
        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.addController(new RecycleBinController());
    }

    private native void registerPubService()/*-{
        var instance = this;
        $wnd.amalto.recyclebin = {};
        $wnd.amalto.recyclebin.RecycleBin = function() {

            function initUI() {
                instance.@org.talend.mdm.webapp.recyclebin.client.RecycleBin::initUI()();
            }

            return {
                init : function() {
                    initUI();
                }
            }
        }();
    }-*/;

    private native void _initUI()/*-{
        var tabPanel = $wnd.amalto.core.getTabPanel();
        var panel = tabPanel.getItem("Recycle Bin");
        if (panel == undefined) {
            @org.talend.mdm.webapp.recyclebin.client.GenerateContainer::generateContentPanel()();
            panel = this.@org.talend.mdm.webapp.recyclebin.client.RecycleBin::createPanel()();
            tabPanel.add(panel);
        }
        tabPanel.setSelection(panel.getItemId());
    }-*/;

    native JavaScriptObject createPanel()/*-{
        var instance = this;
        var panel = {
            render : function(el) {
                instance.@org.talend.mdm.webapp.recyclebin.client.RecycleBin::renderContent(Ljava/lang/String;)(el.id);
            },
            setSize : function(width, height) {
                var cp = @org.talend.mdm.webapp.recyclebin.client.GenerateContainer::getContentPanel()();
                cp.@com.extjs.gxt.ui.client.widget.ContentPanel::setSize(II)(width, height);
            },
            getItemId : function() {
                var cp = @org.talend.mdm.webapp.recyclebin.client.GenerateContainer::getContentPanel()();
                return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getItemId()();
            },
            getEl : function() {
                var cp = @org.talend.mdm.webapp.recyclebin.client.GenerateContainer::getContentPanel()();
                var el = cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getElement()();
                return {
                    dom : el
                };
            },
            doLayout : function() {
                var cp = @org.talend.mdm.webapp.recyclebin.client.GenerateContainer::getContentPanel()();
                return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::doLayout()();
            },
            title : function() {
                var cp = @org.talend.mdm.webapp.recyclebin.client.GenerateContainer::getContentPanel()();
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

    private void onModuleRender() {
        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.dispatch(RecycleBinEvents.InitFrame);
    }
}
