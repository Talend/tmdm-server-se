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
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
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
            handleSessionExpired();
        } else {
            doOnFailure(caught);
        }
    }

    protected void doOnFailure(Throwable caught) {
        if (caught instanceof StatusCodeException) {
            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL());
            builder.setHeader("Accept", "text/plain");
            try {
                builder.sendRequest("", new RequestCallback() {

                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        if (response.getText().contains("<title>Talend MDM</title>")) {
                            // TMDM-11334 After use IAM,we can not receive a InvocationException exception to determine
                            // session expired.
                            handleSessionExpired();
                        } else {
                            // see TMDM-4411 if call async method,StatusCodeException will be thrown when mdmserver down
                            MessageBox.alert(BaseMessagesFactory.getMessages().error_title(),
                                    BaseMessagesFactory.getMessages().server_unavailable_error(), null);
                        }
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        handleException(exception);
                    }

                });
            } catch (RequestException exception) {
                handleException(exception);
            }
        } else {
            handleException(caught);
        }
    }

    private boolean sessionExpired(Throwable caught) {
        if (caught instanceof InvocationException) {
            String msg = caught.getMessage();
            // FIXME Is there a better way to detect session expiration?
            // When session expires container will use a configured expired Url to return the content of the login page
            // However, since an GWT RPC response cannot be of HTML text, the InvocationException is thrown with the
            // HTML content as the message.
            return msg == null ? false : (msg.contains("<meta name=\"description\" content=\"Talend MDM login page\"/>") || msg.contains("<title>Talend - Login</title>")); //$NON-NLS-1$
        } else
            return false;
    }

    private void handleSessionExpired() {
        MessageBox.alert(BaseMessagesFactory.getMessages().warning_title(),
                BaseMessagesFactory.getMessages().session_timeout_error(), new Listener<MessageBoxEvent>() {

                    public void handleEvent(MessageBoxEvent be) {
                        Cookies.removeCookie("JSESSIONID"); //$NON-NLS-1$
                        Cookies.removeCookie("JSESSIONIDSSO"); //$NON-NLS-1$
                        Window.Location.replace(GWT.getHostPageBaseURL());

                    }
                });
    }

    private void handleException(Throwable caught) {
        if (Log.isDebugEnabled()) {
            MessageBox.alert(BaseMessagesFactory.getMessages().error_title(), caught.toString(), null);// for debugging
        } else {
            String message = caught.getLocalizedMessage();
            String errorMessage = (message != null && !message.isEmpty()) ? message
                    : BaseMessagesFactory.getMessages().unknown_error();
            MessageBox.alert(BaseMessagesFactory.getMessages().error_title(), Format.htmlEncode(errorMessage), null);
        }
    }
}
