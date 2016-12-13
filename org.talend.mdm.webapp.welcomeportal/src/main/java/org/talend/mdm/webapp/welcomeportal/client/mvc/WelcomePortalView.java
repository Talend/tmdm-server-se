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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.welcomeportal.client.GenerateContainer;
import org.talend.mdm.webapp.welcomeportal.client.MainFramePanel;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortal;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortalEvents;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortalServiceAsync;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * DOC Administrator class global comment. Detailled comment
 */

public class WelcomePortalView extends View {

    private static final int DEFAULT_COLUMN_NUM = 3;

    private static final int ALTERNATIVE_COLUMN_NUM = 2;

    private int numColumns;

    private MainFramePanel portal;

    private boolean isEnterprise;

    private static PortalProperties portalConfigCache;

    private WelcomePortalServiceAsync service = (WelcomePortalServiceAsync) Registry.get(WelcomePortal.WELCOMEPORTAL_SERVICE);

    public WelcomePortalView(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        if (event.getType() == WelcomePortalEvents.InitFrame) {
            onInitFrame(event);
        } else if (event.getType() == WelcomePortalEvents.RefreshPortlet) {
            onRefreshPortlet();
        } else if (event.getType() == WelcomePortalEvents.RefreshPortal) {
            onRefreshPortal(event);
        } else if (event.getType() == WelcomePortalEvents.RevertRefreshPortal) {
            onRevertRefreshPortal(event);
        }
    }

    private void onRefreshPortal(AppEvent event) {
        String dataString = event.getData();
        List<String> userConfig = parseConfig(dataString);
        int colNum = Integer.parseInt(userConfig.remove(userConfig.size() - 1));
        ContentPanel container = GenerateContainer.getContentPanel();
        if (isEnterprise) {
            if (colNum == numColumns) {
                ((MainFramePanel) (container.getItems().get(0))).refresh(userConfig);
            } else {// for switching to diff column number
                numColumns = colNum;
                updatePortal(userConfig);
            }
        } else {
            numColumns = colNum;
            updatePortal(userConfig);
        }
    }

    private void onRevertRefreshPortal(AppEvent event) {

        if (Log.isInfoEnabled()) {
            Log.info("Revert RefreshPortal due to saving to db failed... ");//$NON-NLS-1$
        }

        removePortal();

        portalConfigCache = (PortalProperties) event.getData();

        numColumns = portalConfigCache.getColumnNum();

        portal = new MainFramePanel(numColumns, portalConfigCache, isEnterprise);
        ContentPanel container = GenerateContainer.getContentPanel();
        container.add(portal);
        container.layout(true);
    }

    private List<String> parseConfig(String dataString) {
        String temp = dataString.substring(1, dataString.length() - 1);
        String[] names = temp.split(", "); //$NON-NLS-1$
        List<String> config = new ArrayList<String>(names.length);
        config.addAll(Arrays.asList(names));

        return config;
    }

    private void updatePortal(final List<String> userConfig) {
        if (Log.isInfoEnabled()) {
            Log.info("Refresh with different column number... ");//$NON-NLS-1$
        }

        removePortal();

        portal = new MainFramePanel(numColumns, portalConfigCache, userConfig, isEnterprise);
        ContentPanel container = GenerateContainer.getContentPanel();
        container.add(portal);
        container.layout(true);
    }

    public void removePortal() {
        portal.stopAutoRefresh();
        portal.removeAll();
        GenerateContainer.getContentPanel().remove(portal);
    }

    private void onRefreshPortlet() {
        ContentPanel container = GenerateContainer.getContentPanel();
        ((MainFramePanel) (container.getItems().get(0))).refreshPortlets();
    }

    private void onInitFrame(AppEvent event) {
        if (Log.isInfoEnabled()) {
            Log.info("Init frame... ");//$NON-NLS-1$
        }
        portalConfigCache = (PortalProperties) event.getData();
        final ContentPanel container = GenerateContainer.getContentPanel();
        // container.setHeaderVisible(false);
        container.setLayout(new FitLayout());
        container.setStyleAttribute("height", "100%");//$NON-NLS-1$ //$NON-NLS-2$

        service.isEnterpriseVersion(new SessionAwareAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean isEE) {
                isEnterprise = isEE;
                Integer numColumnsObj = portalConfigCache.getColumnNum();
                numColumns = (numColumnsObj == null) ? getDefaultColumNum() : numColumnsObj;

                portal = new MainFramePanel(numColumns, portalConfigCache, isEnterprise);
                container.add(portal);
                container.layout(true);
            }
        });

    }

    private Integer getDefaultColumNum() {
        return isEnterprise ? DEFAULT_COLUMN_NUM : ALTERNATIVE_COLUMN_NUM;
    }
}
