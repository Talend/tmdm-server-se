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

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.client.widget.PortletConstants;
import org.talend.mdm.webapp.welcomeportal.client.MainFramePanel;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortal;
import org.talend.mdm.webapp.welcomeportal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.welcomeportal.client.resources.icon.Icons;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;

public class AlertPortlet extends BasePortlet {

    private static String mesgPrefix = "<span id=\"licenseAlert\" style=\"padding-right:8px;cursor: pointer;\" class=\"labelStyle\" title=\"" //$NON-NLS-1$
            + MessagesFactory.getMessages().alerts_title() + "\">"; //$NON-NLS-1$

    private static String alertIcon = "<IMG SRC=\"secure/img/genericUI/alert-icon.png\"/>&nbsp;"; //$NON-NLS-1$

    private String message = ""; //$NON-NLS-1$

    public AlertPortlet(MainFramePanel portal) {
        super(PortletConstants.ALERT_NAME, portal);
        setIcon(AbstractImagePrototype.create(Icons.INSTANCE.alert()));
        setHeading(MessagesFactory.getMessages().alerts_title());
        label.setText(MessagesFactory.getMessages().loading_alert_msg());
        initConfigSettings();

        updateLinks();
        autoRefresh(configModel.isAutoRefresh());
    }

    @Override
    public void refresh() {
        updateLinks();
    }

    private void updateLinks() {
        final StringBuilder sb = new StringBuilder(mesgPrefix);
        service.getAlert(UrlUtil.getLanguage(), new SessionAwareAsyncCallback<Integer>() {

            @Override
            public void onSuccess(Integer type) {
                if (type == 0) {
                    service.getLicenseWarning(UrlUtil.getLanguage(), new SessionAwareAsyncCallback<String>() {

                        @Override
                        public void onSuccess(String warning) {
                            if (!warning.isEmpty()) {
                                sb.append(alertIcon);
                                sb.append(warning);
                                label.setText(MessagesFactory.getMessages().alerts_desc());
                            } else {
                                label.setText(MessagesFactory.getMessages().no_alerts());
                                fieldSet.setVisible(false);
                            }
                            sb.append("</span>"); //$NON-NLS-1$

                            updateMessage(sb.toString());
                        }

                    });
                } else {
                    if (type == WelcomePortal.NOLICENSE) {
                        sb.append(alertIcon);
                        sb.append(MessagesFactory.getMessages().no_license_msg());
                    } else if (type == WelcomePortal.EXPIREDLICENSE) {
                        sb.append(alertIcon);
                        sb.append(MessagesFactory.getMessages().license_expired_msg());
                    } else {
                        // error msg
                        sb.append(type);
                    }
                    label.setText(MessagesFactory.getMessages().alerts_desc());
                    sb.append("</span>"); //$NON-NLS-1$
                    updateMessage(sb.toString());
                }
            }
        });
    }

    private void updateMessage(String newMessage) {
        if (!message.equals(newMessage)) {
            message = newMessage;
            fieldSet.removeAll();
            HTML alertHtml = new HTML(message);
            alertHtml.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    portal.initUI(WelcomePortal.LICENSECONTEXT, WelcomePortal.LICENSEAPP);
                }
            });
            fieldSet.add(alertHtml);
            fieldSet.layout(true);
        }
    }
}
