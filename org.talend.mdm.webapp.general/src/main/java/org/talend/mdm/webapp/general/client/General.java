/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.client;

import org.talend.mdm.webapp.base.client.ServiceEnhancer;
import org.talend.mdm.webapp.base.client.util.Cookies;
import org.talend.mdm.webapp.general.client.boundary.PubService;
import org.talend.mdm.webapp.general.client.layout.AccordionMenus;
import org.talend.mdm.webapp.general.client.layout.ActionsPanel;
import org.talend.mdm.webapp.general.client.message.PublicMessageService;
import org.talend.mdm.webapp.general.client.mvc.GeneralEvent;
import org.talend.mdm.webapp.general.client.mvc.controller.GeneralController;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class General implements EntryPoint {

    public static final String OVERALL_SERVICE = "GeneralService"; //$NON-NLS-1$

    public static final String USER_BEAN = "UserBean"; //$NON-NLS-1$

    @Override
    public void onModuleLoad() {
        XDOM.setAutoIdPrefix(GWT.getModuleName() + "-" + XDOM.getAutoIdPrefix()); //$NON-NLS-1$
        recordStatus();
        // standalone initialization Ext nvironment. solve JavaScript code conflict.
        // these code come from Gxt sdk
        GeneralEnvironment.loadAll();

        ServiceDefTarget service = GWT.create(GeneralService.class);
        ServiceEnhancer.customizeService(service);
        Registry.register(OVERALL_SERVICE, service);

        registerPubServices();

        registerPubService();

        registerPortalConfigService();

        PublicMessageService.registerMessageService();

        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.addController(new GeneralController());
        dispatcher.dispatch(GeneralEvent.LoadUser);
        if (!GXT.isIE) {
            preventSelecStart();
        }
    }

    private native void preventSelecStart()/*-{
		$doc.body.onselectstart = function() {
			return false;
		};
    }-*/;

    public native void recordStatus()/*-{
		var instance = this;
		$wnd.onunload = function() {
			instance.@org.talend.mdm.webapp.general.client.General::recordPanelStatus()();
		};
    }-*/;

    public native void registerPortalConfigService()/*-{
		var portletConfigFieldSet = @org.talend.mdm.webapp.general.client.layout.PortletConfigFieldSet::getInstance()();

		$wnd.amalto.core.markPortlets = function(configs, allCharts) {
			portletConfigFieldSet.@org.talend.mdm.webapp.general.client.layout.PortletConfigFieldSet::updatePortletConfig(Ljava/lang/String;)(configs);
		};

		$wnd.amalto.core.unmarkPortlet = function(name, value) {
			portletConfigFieldSet.@org.talend.mdm.webapp.general.client.layout.PortletConfigFieldSet::updateWidgetCheckBox(Ljava/lang/String;Ljava/lang/Boolean;)(name,value);
		};
    }-*/;

    public void recordPanelStatus() {
        Cookies.setValue("ActionsPanel", ActionsPanel.getInstance().isCollapsed()); //$NON-NLS-1$
        Cookies.setValue("AccordionMenus", AccordionMenus.getInstance().isCollapsed()); //$NON-NLS-1$
    }

    private native void registerPubService()/*-{
        var workspace = @org.talend.mdm.webapp.general.client.layout.WorkSpace::getInstance()();
        //Create a fake TabPanel JavaScript Object, imitate original extjs code behaviour
        var tabPanel = {
        // imitate getItem method, return corresponding contentPanel by itemId
        getItem : function(itemId){
        return workspace.@org.talend.mdm.webapp.general.client.layout.WorkSpace::getItem(Ljava/lang/String;)(itemId);
        },
        // imitate add method, add fake extjs panel to gxt's TabPanel
        add : function(item){
        workspace.@org.talend.mdm.webapp.general.client.layout.WorkSpace::addWorkTab(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(item.getItemId(), item);
        },
        doLayout : function(){
        workspace.@org.talend.mdm.webapp.general.client.layout.WorkSpace::doLayout();
        },
        //select tabItem by itemId
        setSelection : function(itemId){
        workspace.@org.talend.mdm.webapp.general.client.layout.WorkSpace::setSelection(Ljava/lang/String;)(itemId);
        },

        remove: function(item){
        if(typeof item != "string"){
        item = item.getId();
        }
        workspace.@org.talend.mdm.webapp.general.client.layout.WorkSpace::remove(Ljava/lang/String;)(item);
        },
        on: function(eventName, handler){
        workspace.@org.talend.mdm.webapp.general.client.layout.WorkSpace::on(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(eventName, handler);
        },
        un: function(eventName, handler){
        workspace.@org.talend.mdm.webapp.general.client.layout.WorkSpace::un(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(eventName, handler);
        },
        updateCurrentTabText: function(tabText){
        workspace.@org.talend.mdm.webapp.general.client.layout.WorkSpace::updateCurrentTabText(Ljava/lang/String;)(tabText);
        },
        closeCurrentTab: function(){
        workspace.@org.talend.mdm.webapp.general.client.layout.WorkSpace::closeCurrentTab()();
        }
        };
        // return global TabPanel
        $wnd.amalto.core.getTabPanel = function(){
        return tabPanel;
        };
        // imitate original extjs project doLayout method
        $wnd.amalto.core.doLayout = function(){
        tabPanel.doLayout();
        };
    }-*/;

    private void registerPubServices() {
        PubService.registerLanguageService();
        PubService.registerUpdateProductInfo();
    }
}
