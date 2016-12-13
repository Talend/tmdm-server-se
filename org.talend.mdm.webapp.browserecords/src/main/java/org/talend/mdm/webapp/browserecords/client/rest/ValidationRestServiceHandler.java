/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.rest;

import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.data.MediaType;
import org.restlet.client.data.Method;
import org.restlet.client.ext.json.JsonRepresentation;
import org.restlet.client.representation.StringRepresentation;
import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.rest.ClientResourceWrapper;
import org.talend.mdm.webapp.base.client.rest.ResourceSessionAwareCallbackHandler;
import org.talend.mdm.webapp.base.client.rest.RestServiceHelper;

import com.google.gwt.json.client.JSONObject;

public class ValidationRestServiceHandler {

    private String restServiceUrl = RestServiceHelper.BASE_URL + "/data"; //$NON-NLS-1$

    private ClientResourceWrapper client;

    private static ValidationRestServiceHandler handler;

    private ValidationRestServiceHandler() {
        client = new ClientResourceWrapper();
    }

    public static ValidationRestServiceHandler get() {
        if (handler == null) {
            handler = new ValidationRestServiceHandler();
        }
        return handler;
    }

    public void setClient(ClientResourceWrapper client) {
        this.client = client;
    }

    public void validateRecord(String dataCluster, String documentXml, final SessionAwareAsyncCallback<JSONObject> callback) {
        if (dataCluster == null || dataCluster.isEmpty() || documentXml == null || documentXml.isEmpty()) {
            throw new IllegalArgumentException();
        }

        client.init(Method.POST, restServiceUrl + '/' + dataCluster + "/validate", null); //$NON-NLS-1$
        client.setPostEntity(new StringRepresentation("<root>" + documentXml + "</root>", MediaType.APPLICATION_XML));
        client.setCallback(new ResourceSessionAwareCallbackHandler() {

            @Override
            public void doProcess(Request request, Response response) throws Exception {
                JsonRepresentation jsonRepresentation = RestServiceHelper.getJsonRepresentationFromResponse(response);
                callback.onSuccess((JSONObject)jsonRepresentation.getJsonArray().get(0));
            }
        });
        client.request(MediaType.APPLICATION_JSON);
    }
}
