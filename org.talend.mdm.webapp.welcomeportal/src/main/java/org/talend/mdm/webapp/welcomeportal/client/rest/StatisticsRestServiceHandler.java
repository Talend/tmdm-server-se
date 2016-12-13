/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.client.rest;

import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.data.MediaType;
import org.restlet.client.data.Method;
import org.restlet.client.ext.json.JsonRepresentation;
import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.rest.ClientResourceWrapper;
import org.talend.mdm.webapp.base.client.rest.ResourceSessionAwareCallbackHandler;
import org.talend.mdm.webapp.base.client.rest.RestServiceHelper;
import org.talend.mdm.webapp.welcomeportal.client.mvc.ConfigModel;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;

/**
 * created by glzhou
 * 
 */
public class StatisticsRestServiceHandler {

    private String restServiceUrl = RestServiceHelper.BASE_URL + "/system/stats"; //$NON-NLS-1$

    private ClientResourceWrapper client;

    private static StatisticsRestServiceHandler handler;

    private StatisticsRestServiceHandler() {
        client = new ClientResourceWrapper();
    }

    public static StatisticsRestServiceHandler getInstance() {
        if (handler == null) {
            handler = new StatisticsRestServiceHandler();
        }
        return handler;
    }

    public void setClient(ClientResourceWrapper client) {
        this.client = client;
    }

    /**
     * Get entity count summary for a container
     */
    public void getContainerDataStats(String dataContainer, ConfigModel configModel,
            final SessionAwareAsyncCallback<JSONArray> callback) {
        if (dataContainer == null) {
            throw new IllegalArgumentException("Data container required"); //$NON-NLS-1$
        }
        client.init(Method.GET, restServiceUrl  + "/data/" + dataContainer + "?top=" + configModel.getSettingValue()); //$NON-NLS-1$ //$NON-NLS-2$
        client.setCallback(new ResourceSessionAwareCallbackHandler() {

            @Override
            public void doProcess(Request request, Response response) throws Exception {

                JsonRepresentation jsonRepresentation = RestServiceHelper.getJsonRepresentationFromResponse(response);
                if (jsonRepresentation != null) {
                    JSONValue jsonValue = jsonRepresentation.getJsonObject().get("data"); //$NON-NLS-1$
                    callback.onSuccess(jsonValue.isArray());
                }

            }
        });
        client.request(MediaType.APPLICATION_JSON);
    }

    public void getContainerJournalStats(String dataContainer, ConfigModel configModel,
            final SessionAwareAsyncCallback<JSONArray> callback) {
        if (dataContainer == null) {
            throw new IllegalArgumentException("Data container required"); //$NON-NLS-1$
        }

        client.init(Method.GET, restServiceUrl + "/journal/" + dataContainer + "?top=0&timeframe=" + configModel.getSettingValue()); //$NON-NLS-1$ //$NON-NLS-2$
        client.setCallback(new ResourceSessionAwareCallbackHandler() {

            @Override
            public void doProcess(Request request, Response response) throws Exception {

                JsonRepresentation jsonRepresentation = RestServiceHelper.getJsonRepresentationFromResponse(response);
                if (jsonRepresentation != null) {
                    JSONValue jsonValue = jsonRepresentation.getJsonObject().get("journal"); //$NON-NLS-1$
                    callback.onSuccess(jsonValue.isArray());
                }

            }
        });
        client.request(MediaType.APPLICATION_JSON);

    }

    public void getContainerMatchingStats(String dataContainer, ConfigModel configModel,
            final SessionAwareAsyncCallback<JSONArray> callback) {
        if (dataContainer == null) {
            throw new IllegalArgumentException("Data container required"); //$NON-NLS-1$
        }

        client.init(Method.GET, restServiceUrl + "/matching/" + dataContainer + "?top=" + configModel.getSettingValue()); //$NON-NLS-1$ //$NON-NLS-2$
        client.setCallback(new ResourceSessionAwareCallbackHandler() {

            @Override
            public void doProcess(Request request, Response response) throws Exception {

                JsonRepresentation jsonRepresentation = RestServiceHelper.getJsonRepresentationFromResponse(response);
                if (jsonRepresentation != null) {
                    JSONValue jsonValue = jsonRepresentation.getJsonObject().get("matching"); //$NON-NLS-1$
                    callback.onSuccess(jsonValue.isArray());
                }

            }
        });
        client.request(MediaType.APPLICATION_JSON);

    }

    public void getRoutingEventStats(ConfigModel configModel, final SessionAwareAsyncCallback<JSONArray> callback) {
        client.init(Method.GET, restServiceUrl + "/events?top=5&timeframe=" + configModel.getSettingValue()); //$NON-NLS-1$
        client.setCallback(new ResourceSessionAwareCallbackHandler() {

            @Override
            public void doProcess(Request request, Response response) throws Exception {

                JsonRepresentation jsonRepresentation = RestServiceHelper.getJsonRepresentationFromResponse(response);
                if (jsonRepresentation != null) {
                    JSONValue jsonValue = jsonRepresentation.getJsonObject().get("events"); //$NON-NLS-1$
                    callback.onSuccess(jsonValue.isArray());
                }

            }
        });
        client.request(MediaType.APPLICATION_JSON);

    }
}
