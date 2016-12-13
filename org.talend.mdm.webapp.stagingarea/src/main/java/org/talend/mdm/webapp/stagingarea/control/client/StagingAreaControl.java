/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.client;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import org.talend.mdm.webapp.base.client.ServiceEnhancer;
import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.stagingarea.control.client.view.StagingAreaMainView;
import org.talend.mdm.webapp.stagingarea.control.shared.controller.Controllers;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaConfiguration;

public class StagingAreaControl implements EntryPoint {

    public static final String STAGINGAREA_ID = "Stagingarea"; //$NON-NLS-1$

    private static final StagingAreaConfiguration stagingAreaConfig = new StagingAreaConfiguration();

    public static StagingAreaServiceAsync service;

    public static StagingAreaConfiguration getStagingAreaConfig() {
        return stagingAreaConfig;
    }

    @Override
    public void onModuleLoad() {
        service = GWT.create(StagingAreaService.class);
        ServiceEnhancer.customizeService((ServiceDefTarget) service);

        XDOM.setAutoIdPrefix(GWT.getModuleName() + "-" + XDOM.getAutoIdPrefix()); //$NON-NLS-1$
        registerPubService();
        Log.setUncaughtExceptionHandler();
    }

    private native void registerPubService()/*-{
                                            var instance = this;
                                            $wnd.amalto.stagingarea = {};
                                            $wnd.amalto.stagingarea.Stagingarea = function() {

                                            function initUI() {
                                            instance.@org.talend.mdm.webapp.stagingarea.control.client.StagingAreaControl::initUI()();
                                            }

                                            return {
                                            init : function() {
                                            initUI();
                                            }
                                            }
                                            }();
                                            }-*/;

    // Called by JS
    public static void cancelAutoRefresh() {
        Controllers.get().getStagingController().autoRefresh(false);
    }

    private native void _initUI()/*-{
                                 var tabPanel = $wnd.amalto.core.getTabPanel();
                                 var panel = tabPanel.getItem("Stagingarea");
                                 if (panel == undefined) {
                                 @org.talend.mdm.webapp.stagingarea.control.client.GenerateContainer::generateContentPanel()();
                                 panel = this.@org.talend.mdm.webapp.stagingarea.control.client.StagingAreaControl::createPanel()();
                                 var removeTabEvent = function(tabPanel, tabItem) {
                                    @org.talend.mdm.webapp.stagingarea.control.client.StagingAreaControl::cancelAutoRefresh()();
                                    return true;
                                 };
                                 tabPanel.on("beforeremove", removeTabEvent);
                                 tabPanel.add(panel);
                                 }
                                 tabPanel.setSelection(panel.getItemId());
                                 }-*/;

    native JavaScriptObject createPanel()/*-{
                                         var instance = this;
                                         var panel = {
                                         render : function(el) {
                                         instance.@org.talend.mdm.webapp.stagingarea.control.client.StagingAreaControl::renderContent(Ljava/lang/String;)(el.id);
                                         },
                                         setSize : function(width, height) {
                                         var cp = @org.talend.mdm.webapp.stagingarea.control.client.GenerateContainer::getContentPanel()();
                                         cp.@com.extjs.gxt.ui.client.widget.ContentPanel::setSize(II)(width, height);
                                         },
                                         getItemId : function() {
                                         var cp = @org.talend.mdm.webapp.stagingarea.control.client.GenerateContainer::getContentPanel()();
                                         return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getItemId()();
                                         },
                                         getEl : function() {
                                         var cp = @org.talend.mdm.webapp.stagingarea.control.client.GenerateContainer::getContentPanel()();
                                         var el = cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getElement()();
                                         return {
                                         dom : el
                                         };
                                         },
                                         doLayout : function() {
                                         var cp = @org.talend.mdm.webapp.stagingarea.control.client.GenerateContainer::getContentPanel()();
                                         return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::doLayout()();
                                         },
                                         title : function() {
                                         var cp = @org.talend.mdm.webapp.stagingarea.control.client.GenerateContainer::getContentPanel()();
                                         return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getHeading()();
                                         }
                                         };
                                         return panel;
                                         }-*/;

    public void renderContent(final String contentId) {
        onModuleRender();
        service.getStagingAreaConfig(new SessionAwareAsyncCallback<StagingAreaConfiguration>() {

            @Override
            public void onSuccess(StagingAreaConfiguration aStagingAreaConfig) {
                // Sets configuration with value from server
                StagingAreaControl.stagingAreaConfig.setRefreshIntervals(aStagingAreaConfig.getRefreshIntervals());
                ContentPanel content = GenerateContainer.getContentPanel();

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
        });
    }

    public void initUI() {
        _initUI();
    }

    private void onModuleRender() {
        final ContentPanel contentPanel = GenerateContainer.getContentPanel();
        StagingAreaMainView mainPanel = new StagingAreaMainView();
        contentPanel.add(mainPanel);
    }
}
