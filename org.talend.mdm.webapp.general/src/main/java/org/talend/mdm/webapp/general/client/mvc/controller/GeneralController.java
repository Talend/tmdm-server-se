/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.client.mvc.controller;

import java.util.List;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.util.Cookies;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.client.util.UserContextUtil;
import org.talend.mdm.webapp.general.client.General;
import org.talend.mdm.webapp.general.client.GeneralServiceAsync;
import org.talend.mdm.webapp.general.client.i18n.MessageFactory;
import org.talend.mdm.webapp.general.client.layout.AccordionMenus;
import org.talend.mdm.webapp.general.client.layout.ActionsPanel;
import org.talend.mdm.webapp.general.client.layout.BrandingBar;
import org.talend.mdm.webapp.general.client.layout.WorkSpace;
import org.talend.mdm.webapp.general.client.mvc.GeneralEvent;
import org.talend.mdm.webapp.general.client.mvc.view.GeneralView;
import org.talend.mdm.webapp.general.model.ActionBean;
import org.talend.mdm.webapp.general.model.LanguageBean;
import org.talend.mdm.webapp.general.model.MenuGroup;
import org.talend.mdm.webapp.general.model.ProductInfo;
import org.talend.mdm.webapp.general.model.UserBean;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.Timer;

public class GeneralController extends Controller {

    private GeneralServiceAsync service;

    private GeneralView view;

    public GeneralController() {
        registerEventTypes(GeneralEvent.LoadProductInfo);
        registerEventTypes(GeneralEvent.LoadUser);
        registerEventTypes(GeneralEvent.InitFrame);
        registerEventTypes(GeneralEvent.LoadMenus);
        registerEventTypes(GeneralEvent.LoadLanguages);
        registerEventTypes(GeneralEvent.LoadActions);
        registerEventTypes(GeneralEvent.LoadWelcome);
        registerEventTypes(GeneralEvent.SwitchClusterAndModel);
        registerEventTypes(GeneralEvent.SupportStaging);
    }

    @Override
    public void initialize() {
        service = (GeneralServiceAsync) Registry.get(General.OVERALL_SERVICE);
        view = new GeneralView(this);
    }

    @Override
    public void handleEvent(final AppEvent event) {
        EventType type = event.getType();

        if (type == GeneralEvent.LoadProductInfo) {
            loadProductInfo();
        } else if (type == GeneralEvent.LoadUser) {
            loadUser(event);
        } else if (type == GeneralEvent.InitFrame) {
            forwardToView(view, event);
        } else if (type == GeneralEvent.LoadLanguages) {
            loadLanguages(event);
        } else if (type == GeneralEvent.LoadMenus) {
            loadMenu(event);
        } else if (type == GeneralEvent.LoadActions) {
            loadActions(event);
        } else if (type == GeneralEvent.SwitchClusterAndModel) {
            switchClusterAndModel(event);
        } else if (type == GeneralEvent.LoadWelcome) {
            forwardToView(view, event);
        } else if (type == GeneralEvent.SupportStaging) {
            supportStaging(event);
        }
    }

    private void loadProductInfo() {
        service.getProductInfo(new SessionAwareAsyncCallback<ProductInfo>() {

            public void onSuccess(ProductInfo info) {
                BrandingBar.getInstance().setProductInfo(info);
                Dispatcher dispatcher = Dispatcher.get();
                dispatcher.dispatch(GeneralEvent.LoadLanguages);
            }
        });
    }

    private void loadLanguages(AppEvent event) {
        service.getLanguages(UrlUtil.getLocaleProperty(), new SessionAwareAsyncCallback<List<LanguageBean>>() {

            public void onSuccess(List<LanguageBean> result) {
                BrandingBar.getInstance().buildLanguage(result);
                Dispatcher dispatcher = Dispatcher.get();
                dispatcher.dispatch(GeneralEvent.InitFrame);
            }
        });
    }

    private void loadMenu(final AppEvent event) {
        service.getMenus(UrlUtil.getLanguage(), new SessionAwareAsyncCallback<MenuGroup>() {

            public void onSuccess(MenuGroup result) {
                event.setData(result);
                forwardToView(view, event);
            }
        });
    }

    private void loadUser(AppEvent event) {
        service.getUser(new SessionAwareAsyncCallback<UserBean>() {

            public void onSuccess(UserBean userBean) {
                Registry.register(General.USER_BEAN, userBean);
                Dispatcher dispatcher = Dispatcher.get();
                dispatcher.dispatch(GeneralEvent.LoadProductInfo);
            }
        });
    }

    private void loadActions(AppEvent event) {
        service.getAction(new SessionAwareAsyncCallback<ActionBean>() {

            public void onSuccess(ActionBean result) {
                ActionsPanel.getInstance().loadAction(result);
                if ((Boolean) Cookies.getValue("ActionsPanel")) { //$NON-NLS-1$
                    ActionsPanel.getInstance().collapse();
                }
            }
        });
    }

    private void switchClusterAndModel(AppEvent event) {
        final String dataCluster = ActionsPanel.getInstance().getDataCluster();
        final String dataModel = ActionsPanel.getInstance().getDataModel();
        service.setClusterAndModel(dataCluster, dataModel, new SessionAwareAsyncCallback<Void>() {

            @Override
            protected void doOnFailure(Throwable caught) {
                MessageBox.alert(MessageFactory.getMessages().status(), MessageFactory.getMessages().status_msg_failure(), null);
            }

            public void onSuccess(Void result) {
                final MessageBox msgBox = new MessageBox();
                msgBox.setTitle(MessageFactory.getMessages().status());
                msgBox.setMessage(MessageFactory.getMessages().status_msg_success());
                msgBox.setButtons(""); //$NON-NLS-1$
                msgBox.setIcon(MessageBox.INFO);
                msgBox.show();
                Timer timer = new Timer() {

                    public void run() {
                        msgBox.close();
                        UserContextUtil.setDataContainer(dataCluster);
                        UserContextUtil.setDataModel(dataModel);
                        WorkSpace.getInstance().clearTabs();
                        WorkSpace.getInstance().loadApp(GeneralView.WELCOMECONTEXT, GeneralView.WELCOMEAPP);
                        Dispatcher dispatcher = Dispatcher.get();
                        AppEvent event = new AppEvent(GeneralEvent.SupportStaging);
                        event.setData("dataCluster", dataCluster); //$NON-NLS-1$
                        dispatcher.dispatch(event);
                    }
                };
                timer.schedule(700);
            }
        });
    }

    private void supportStaging(AppEvent event) {
        String dataCluster = event.getData("dataCluster"); //$NON-NLS-1$
        service.supportStaging(dataCluster, new SessionAwareAsyncCallback<Boolean>() {

            public void onSuccess(Boolean support) {
                AccordionMenus.getInstance().disabledMenuItem("stagingarea", "Stagingarea", !support); //$NON-NLS-1$//$NON-NLS-2$
                AccordionMenus.getInstance().disabledMenuItem("browserecords", "BrowseRecordsInStaging", !support); //$NON-NLS-1$//$NON-NLS-2$
            }
        });
    }
}
