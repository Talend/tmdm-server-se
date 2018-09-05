/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client;

import org.talend.mdm.webapp.base.client.i18n.BaseMessagesFactory;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;

public abstract class SessionAwareAsyncCallback<T> implements AsyncCallback<T> {

    final private static String OIDC_LOGIN_TITLE = "<title>Talend - Login</title>"; //$NON-NLS-1$

    final private static String OIDC_OPTIONS_RESPONSE = "0"; //$NON-NLS-1$

    final private static String MDM_LOGIN_META = "<meta name=\"description\" content=\"Talend MDM login page\"/>"; //$NON-NLS-1$

    final private static String OIDC_REDIRECTION_META = "<meta name=\"description\" content=\"Redirection to UI site\" />"; //$NON-NLS-1$

    @Override
    public final void onFailure(Throwable caught) {
        Log.error(caught.toString());
        if (sessionExpired(caught)) {
            MessageBox.alert(BaseMessagesFactory.getMessages().warning_title(),
                    BaseMessagesFactory.getMessages().session_timeout_error(), new Listener<MessageBoxEvent>() {

                        @Override
                        public void handleEvent(MessageBoxEvent be) {
                            Cookies.removeCookie("JSESSIONID"); //$NON-NLS-1$
                            Cookies.removeCookie("JSESSIONIDSSO"); //$NON-NLS-1$
                            Window.Location.replace(GWT.getHostPageBaseURL() + "/logout"); //$NON-NLS-1$
                        }
                    });
        } else {
            doOnFailure(caught);
        }
    }

    protected void doOnFailure(Throwable caught) {
        String errorMsg = caught.getLocalizedMessage();
        if (errorMsg == null || errorMsg.isEmpty()) {
            errorMsg = BaseMessagesFactory.getMessages().unknown_error();
        }
        MessageBox.alert(BaseMessagesFactory.getMessages().error_title(), errorMsg, null);
    }

    private boolean sessionExpired(Throwable caught) {
        boolean isExpired = false;
        if (caught instanceof InvocationException) {
            String msg = caught.getMessage();
            if (msg != null) {
                // CE, Redirected to MDM Login Page
                boolean mdmExpired = msg.contains(MDM_LOGIN_META);
                // EE, Redirected to OIDC Login page, or OPTIONS invoke oidc/idp/logout
                boolean ssoExpired = msg.contains(OIDC_LOGIN_TITLE)
                        || OIDC_OPTIONS_RESPONSE.equals(msg.trim()) || msg.contains(OIDC_REDIRECTION_META);
                isExpired = mdmExpired || ssoExpired;
            }
        }
        return isExpired;
    }
}
