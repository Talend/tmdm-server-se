/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.client.widget;

import java.util.List;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.welcomeportal.client.MainFramePanel;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortal;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortalServiceAsync;
import org.talend.mdm.webapp.welcomeportal.client.mvc.BaseConfigModel;
import org.talend.mdm.webapp.welcomeportal.client.mvc.ConfigModel;
import org.talend.mdm.webapp.welcomeportal.client.mvc.PortalProperties;
import org.talend.mdm.webapp.welcomeportal.client.widget.PortletConfigDialog.PortletConfigListener;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Timer;

public abstract class BasePortlet extends Portlet {

    protected WelcomePortalServiceAsync service = (WelcomePortalServiceAsync) Registry.get(WelcomePortal.WELCOMEPORTAL_SERVICE);

    protected MainFramePanel portal;

    protected String portletName;

    protected Label label;

    protected FieldSet fieldSet;

    protected Timer autoRefresher;

    protected ToolButton refreshBtn;

    protected PortalProperties portalConfigs;

    private boolean autoOn;

    protected boolean startedAsOn;

    private int interval;

    protected ConfigModel configModel;

    protected boolean configSettingChanged;

    protected boolean autoRefreshSwithced;

    public BasePortlet() {
        super();
        this.setLayout(new FitLayout());
        this.setStyleAttribute("padding", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
        this.setCollapsible(true);
        this.setAnimCollapse(false);
        this.setAutoHeight(true);

        this.getHeader().addTool(new ToolButton("x-tool-close", new SelectionListener<IconButtonEvent>() { //$NON-NLS-1$

                    @Override
                    public void componentSelected(IconButtonEvent ce) {
                        final List<BasePortlet> portlets = portal.getPortlets();
                        final int index = portlets.indexOf(BasePortlet.this);
                        portlets.remove(index);
                        portal.remove(BasePortlet.this, portal.getPortletColumn(BasePortlet.this));
                        String portletToLocationsStr = portal.getUpdatedLocations().toString();

                        service.savePortalConfig(PortalProperties.KEY_PORTLET_LOCATIONS, portletToLocationsStr,
                                new SessionAwareAsyncCallback<Void>() {

                                    @Override
                                    public void onSuccess(Void result1) {
                                        portal.render();
                                        unmarkPortlet(portletName, false);
                                        boolean auto = BasePortlet.this.isAutoOn();
                                        if (auto) {
                                            BasePortlet.this.autoRefresh(false);
                                        }
                                        BasePortlet.this.removeAllListeners();
                                        BasePortlet.this.removeAll();
                                    }

                                    @Override
                                    protected void doOnFailure(Throwable caught) {
                                        super.doOnFailure(caught);
                                        portal.removeAllPortlets();

                                        portlets.add(index, BasePortlet.this);
                                        portal.refresh();
                                    }
                                });
                    }

                }));

        // will be replaced only in StartPortlet, all other inherit it
        refreshBtn = new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() { //$NON-NLS-1$

                    @Override
                    public void componentSelected(IconButtonEvent ce) {
                        refresh();
                    }
                });

        this.getHeader().addTool(refreshBtn);
    }

    public BasePortlet(String name, MainFramePanel portal) {

        this();

        this.portletName = name;

        this.portal = portal;

        this.setItemId(name + "Portlet"); //$NON-NLS-1$

        label = new Label();
        label.setItemId(name + "Label"); //$NON-NLS-1$
        label.setStyleAttribute("font-weight", "bold"); //$NON-NLS-1$ //$NON-NLS-2$
        label.setAutoHeight(true);
        this.add(label);

        fieldSet = new FieldSet();
        fieldSet.setItemId(name + "Set"); //$NON-NLS-1$
        fieldSet.setStyleAttribute("padding", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
        fieldSet.setStyleAttribute("margin-left", "10px");//$NON-NLS-1$ //$NON-NLS-2$
        fieldSet.setStyleAttribute("margin-right", "10px");//$NON-NLS-1$ //$NON-NLS-2$
        fieldSet.setBorders(false);

        this.add(fieldSet);

        portalConfigs = portal.getProps();
        Boolean autoRefreshOn = portalConfigs.getAutoRefreshStatus(portletName);
        if (autoRefreshOn == null) {
            startedAsOn = portal.getStartedAsOn();
        } else {
            startedAsOn = autoRefreshOn;
        }
        interval = portal.getInterval();

        // init and used for non-chart portlets, overwritten in chart portlets
        configModel = new BaseConfigModel(startedAsOn);

    }

    public void resetAutofresh(boolean auto) {
        autoRefresh(auto);
        configModel.setAutoRefresh(auto);
    }

    protected PortletConfigListener initConfigListener() {
        PortletConfigListener configListener = new PortletConfigListener() {

            @Override
            public void onConfigUpdate(final ConfigModel configModelFromUser) {
                if (!configModel.equals(configModelFromUser)) {
                    service.savePortalConfig(PortalProperties.KEY_AUTO_ONOFFS, portletName, configModelFromUser.isAutoRefresh()
                            .toString(), new SessionAwareAsyncCallback<Void>() {

                        @Override
                        public void onSuccess(Void result1) {
                            configModel = configModelFromUser;
                            autoRefresh(configModel.isAutoRefresh());
                            portalConfigs.add(PortalProperties.KEY_AUTO_ONOFFS, portletName, configModel.isAutoRefresh()
                                    .toString());
                            return;
                        }

                        @Override
                        protected void doOnFailure(Throwable caught) {
                            super.doOnFailure(caught);
                        }
                    });
                    return;
                } else {
                    return;
                }
            }

        };

        return configListener;
    }

    protected void initConfigSettings() {

        autoRefresher = new Timer() {

            @Override
            public void run() {
                if (autoOn) {
                    refresh();
                    schedule(interval);
                } else {
                    cancel();
                }
            }

        };

        this.getHeader().addTool(new ToolButton("x-tool-gear", new SelectionListener<IconButtonEvent>() { //$NON-NLS-1$

                    @Override
                    public void componentSelected(IconButtonEvent ce) {
                        PortletConfigDialog.showConfig(configModel, initConfigListener());
                    }

                }));

    }

    public boolean isAutoOn() {
        return this.autoOn;
    }

    public void autoRefresh(boolean auto) {
        if (autoOn != auto) {
            autoOn = auto;

            if (autoOn) {
                autoRefresher.run();
            }
        }
    }

    abstract public void refresh();

    public String getPortletName() {
        return this.portletName;
    }

    private native void unmarkPortlet(String name, boolean value)/*-{
		$wnd.amalto.core.unmarkPortlet(name, value);
    }-*/;
}
