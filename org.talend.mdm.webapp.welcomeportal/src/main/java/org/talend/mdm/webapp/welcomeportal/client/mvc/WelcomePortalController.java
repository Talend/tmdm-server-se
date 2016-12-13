/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.client.mvc;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortal;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortalEvents;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortalServiceAsync;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class WelcomePortalController extends Controller {

    private WelcomePortalView view;

    private WelcomePortalServiceAsync service = (WelcomePortalServiceAsync) Registry.get(WelcomePortal.WELCOMEPORTAL_SERVICE);

    public WelcomePortalController() {
        registerEventTypes(WelcomePortalEvents.InitFrame);
        registerEventTypes(WelcomePortalEvents.RevertRefreshPortal);
    }

    @Override
    public void initialize() {
        view = new WelcomePortalView(this);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type == WelcomePortalEvents.InitFrame) {
            service.getPortalConfig(new SessionAwareAsyncCallback<PortalProperties>() {

                @Override
                public void onSuccess(PortalProperties portalConfig) {
                    AppEvent eventWithData = new AppEvent(WelcomePortalEvents.InitFrame, portalConfig);
                    forwardToView(view, eventWithData);
                }

            });
        } else if (type == WelcomePortalEvents.RefreshPortlet) {
            forwardToView(view, event);
        } else if (type == WelcomePortalEvents.RefreshPortal) {
            forwardToView(view, event);
        } else if (type == WelcomePortalEvents.RevertRefreshPortal) {
            forwardToView(view, event);
        }

    }

    public void removeWelcomePortal() {
        view.removePortal();
    }
}
