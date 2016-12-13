/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.rpc.StatusCodeException;

public abstract class SessionAwareAsyncCallback<T> implements AsyncCallback<T> {

    public final void onFailure(Throwable caught) {
        if (Log.isErrorEnabled()) {
            Log.error(caught.toString());
        }
        if (sessionExpired(caught)) {
            MessageBox.alert(BaseMessagesFactory.getMessages().warning_title(), BaseMessagesFactory.getMessages()
                    .session_timeout_error(), new Listener<MessageBoxEvent>() {

                public void handleEvent(MessageBoxEvent be) {
                    Cookies.removeCookie("JSESSIONID"); //$NON-NLS-1$
                    Cookies.removeCookie("JSESSIONIDSSO"); //$NON-NLS-1$
                    Window.Location.replace(GWT.getHostPageBaseURL());
                }
            });
        } else {
            doOnFailure(caught);
        }
    }

    protected void doOnFailure(Throwable caught) {
        String errorMsg = caught.getLocalizedMessage();
        if (errorMsg == null || "".equals(errorMsg)) { //$NON-NLS-1$
            if (Log.isDebugEnabled()) {
                errorMsg = caught.toString(); // for debugging purpose
            } else {
                errorMsg = BaseMessagesFactory.getMessages().unknown_error();
            }
        } else if (caught instanceof StatusCodeException) {
            // see TMDM-4411 if call async method,StatusCodeException will be thrown when mdmserver down
            if (Log.isDebugEnabled()) {
                errorMsg = caught.toString(); // for debugging purpose
            } else {
                errorMsg = BaseMessagesFactory.getMessages().unknown_error();
            }
        }

        errorMsg = Format.htmlEncode(errorMsg);
        MessageBox.alert(BaseMessagesFactory.getMessages().error_title(), errorMsg, null);
    }

    private boolean sessionExpired(Throwable caught) {
        if (caught instanceof InvocationException) {
            String msg = caught.getMessage();
            // FIXME Is there a better way to detect session expiration?
            // When session expires container will use a configured expired Url to return the content of the login page
            // However, since an GWT RPC response cannot be of HTML text, the InvocationException is thrown with the
            // HTML content as the message.
            return msg == null ? false : msg.contains("<meta name=\"description\" content=\"Talend MDM login page\"/>"); //$NON-NLS-1$
        } else
            return false;
    }
}
