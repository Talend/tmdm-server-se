/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.widget.PortletConstants;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsEvents;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

public class GenerateContainer {

    private static ContentPanel instance;

    private static String defaultViewPk = ""; //$NON-NLS-1$

    public static void generateContentPanel(String panelId, String heading) {
        if (instance != null) {
            instance.removeFromParent();
        }

        instance = new ContentPanel() {

            @Override
            public void onAttach() {
                monitorWindowResize = true;
                Window.enableScrolling(true);
                super.onAttach();
                GXT.hideLoadingPanel("loading");//$NON-NLS-1$
            }

        };
        instance.setHeaderVisible(false);
        instance.setBorders(false);
        instance.setId(panelId);
        instance.setHeading(heading);
    }

    public static void setDefaultView() {
        String parameter = Cookies.getCookie(PortletConstants.PARAMETER_ENTITY);
        Cookies.removeCookie(PortletConstants.PARAMETER_ENTITY);
        setDefaultViewPk(parameter == null ? "" : parameter); //$NON-NLS-1$
        if (parameter != null) {
            BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);
            service.getAppHeader(new SessionAwareAsyncCallback<AppHeader>() {

                @Override
                public void onSuccess(AppHeader header) {
                    if (header.getDatacluster() == null || header.getDatamodel() == null) {
                        MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                                .data_model_not_specified(), null);
                        return;
                    }
                    BrowseRecords.getSession().put(UserSession.APP_HEADER, header);
                    Dispatcher dispatcher = Dispatcher.get();
                    dispatcher.dispatch(BrowseRecordsEvents.DefaultView);
                }
            });
        }
    }

    public static ContentPanel getContentPanel() {
        return instance;
    }

    public static String defaultTitle() {
        return MessagesFactory.getMessages().browse_record_title();
    }

    public static String getDefaultViewPk() {
        return defaultViewPk;
    }

    public static void setDefaultViewPk(String defaultViewPk) {
        GenerateContainer.defaultViewPk = defaultViewPk;
    }

}
