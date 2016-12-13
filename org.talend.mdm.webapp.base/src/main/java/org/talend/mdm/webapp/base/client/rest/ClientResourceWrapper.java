/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.rest;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.Uniform;
import org.restlet.client.data.MediaType;
import org.restlet.client.data.Method;
import org.restlet.client.resource.ClientResource;
import org.talend.mdm.webapp.base.client.i18n.BaseMessagesFactory;

import com.extjs.gxt.ui.client.widget.MessageBox;

public class ClientResourceWrapper {

    private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.TEXT_XML;

    protected ClientResource client;

    protected Method method;

    protected String uri;

    private Object postEntity;

    public void init(Method methodParameter, String uriParameter) {
        this.method = methodParameter;
        this.uri = uriParameter;
        String timestamp = "timestamp=" + new Date().getTime(); //$NON-NLS-1$
        if (uri.lastIndexOf('?') == -1) {
            timestamp = "?" + timestamp; //$NON-NLS-1$
        } else {
            timestamp = "&" + timestamp; //$NON-NLS-1$
        }
        client = new ClientResource(method, uri + timestamp); //
    }

    public void init(Method methodParameter, String uriParameter, Map<String, String> parameterMap) {
        if (parameterMap != null) {
            StringBuilder parameter = new StringBuilder();
            Set<String> keySet = parameterMap.keySet();
            for (Object name : keySet) {
                if (parameter.length() > 0) {
                    parameter.append("&"); //$NON-NLS-1$
                }
                parameter.append(name).append("=").append(parameterMap.get(name)); //$NON-NLS-1$
            }
            if (parameter.length() > 0) {
                uriParameter = uriParameter + "?" + parameter.toString(); //$NON-NLS-1$
            }
        }
        init(methodParameter, uriParameter);
    }

    public void setCallback(final ResourceCallbackHandler callbackHandler) {
        client.setOnResponse(new Uniform() {

            @Override
            public void handle(Request request, Response response) {
                int statusCode = response.getStatus().getCode();
                // Why is there status "1223" in here check:
                // http://stackoverflow.com/questions/10046972/msie-returns-status-code-of-1223-for-ajax-request
                if (statusCode >= 200 && statusCode <= 299 || statusCode == 1223) {
                    callbackHandler.process(request, response);
                } else {
                    MessageBox.alert(BaseMessagesFactory.getMessages().server_error(), BaseMessagesFactory.getMessages()
                            .server_error_notification(), null);
                }
            }
        });
    }

    public void setPostEntity(Object postEntity) {
        this.postEntity = postEntity;
    }

    public void request() {
        request(null);
    }

    public void request(MediaType mediaType) {
        if (mediaType == null) {
            mediaType = DEFAULT_MEDIA_TYPE;
        }
        if (Method.GET.equals(method)) {
            client.get(mediaType);
        } else if (Method.POST.equals(method)) {
            client.post(postEntity, mediaType);
        } else if (Method.DELETE.equals(method)) {
            client.delete(mediaType);
        } else if (Method.PUT.equals(method)) {
            client.put(mediaType);
        } else {
            throw new IllegalArgumentException("Not supported method: " + method.getName());
        }
    }
}
