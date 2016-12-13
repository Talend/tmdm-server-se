/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.shared.controller.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.data.MediaType;
import org.restlet.client.data.Method;
import org.restlet.client.ext.xml.DomRepresentation;
import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.rest.ClientResourceWrapper;
import org.talend.mdm.webapp.base.client.rest.ResourceSessionAwareCallbackHandler;
import org.talend.mdm.webapp.base.client.rest.RestServiceHelper;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaExecutionModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaValidationModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingContainerModel;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

public class StagingRestServiceHandler {

    private static final StagingRestServiceHandler handler = new StagingRestServiceHandler();

    private final DateTimeFormat DEFAULT_DATE_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss"); //$NON-NLS-1$

    private final String restServiceUrl = RestServiceHelper.BASE_URL + "/tasks/staging"; //$NON-NLS-1$

    private StagingRestServiceHandler() {
    }

    public static StagingRestServiceHandler get() {
        return handler;
    }

    /**
     * Get staging container summary
     * 
     * @param dataContainer staging container
     * @param dataModel staging container metadata model
     * @param model the model to update with response from server.
     */
    public void getStagingContainerSummary(String dataContainer, String dataModel, final StagingContainerModel model) {
        if (dataContainer == null || dataModel == null) {
            throw new IllegalArgumentException();
        }
        Map<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put("model", dataModel); //$NON-NLS-1$
        ClientResourceWrapper client = getClient();
        client.init(Method.GET, restServiceUrl + '/' + dataContainer + '/', parameterMap);
        client.setCallback(new ResourceSessionAwareCallbackHandler() {

            @Override
            public void doProcess(Request request, Response response) throws Exception {
                StagingModelConverter.set(response, model);
            }
        });
        client.request();
    }

    private ClientResourceWrapper getClient() {
        return new ClientResourceWrapper();
    }

    /**
     * Read list of executions, if start=-1 or size=-1 without paging
     */
    public void getStagingAreaExecutionIds(String dataContainer, int start, int pageSize,
            final SessionAwareAsyncCallback<List<String>> callback) {

        Map<String, String> parameterMap = new HashMap<String, String>();
        if (start != -1 && pageSize != -1) {
            parameterMap.put("start", String.valueOf(start)); //$NON-NLS-1$
            parameterMap.put("size", String.valueOf(pageSize)); //$NON-NLS-1$
        }
        ClientResourceWrapper client = getClient();
        client.init(Method.GET, restServiceUrl + '/' + dataContainer + "/execs/", parameterMap); //$NON-NLS-1$
        client.setCallback(new ResourceSessionAwareCallbackHandler() {

            @Override
            public void doProcess(Request request, Response response) throws Exception {

                final List<String> exeIds = new ArrayList<String>();
                DomRepresentation rep = new DomRepresentation(response.getEntity());
                NodeList list = rep.getDocument().getDocumentElement().getChildNodes();
                if (list != null) {
                    for (int i = 0; i < list.getLength(); i++) {
                        Node node = list.item(i);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            exeIds.add(node.getFirstChild().getNodeValue());
                        }
                    }
                }
                callback.onSuccess(exeIds);

            }
        });
        client.request();

    }

    /**
     * Get execution details by Id
     */
    void getStagingAreaExecution(String dataContainer, String exeId,
            final SessionAwareAsyncCallback<StagingAreaExecutionModel> callback) {
        ClientResourceWrapper client = getClient();
        client.init(Method.GET, restServiceUrl + '/' + dataContainer + "/execs/" + exeId); //$NON-NLS-1$
        client.setCallback(new ResourceSessionAwareCallbackHandler() {

            @Override
            public void doProcess(Request request, Response response) throws Exception {
                StagingAreaExecutionModel model = StagingModelConverter.response2StagingAreaExecutionModel(response);
                callback.onSuccess(model);
            }
        });
        client.request();
    }

    /**
     * Get StagingArea execution models by paging parameter
     */
    public void getStagingAreaExecutionsWithPaging(final String dataContainer, int start, int pageSize, Date before,
            final SessionAwareAsyncCallback<List<StagingAreaExecutionModel>> callback) {

        // build URI
        Map<String, String> parameterMap = new HashMap<String, String>();
        if (start != -1 && pageSize != -1) {
            parameterMap.put("start", String.valueOf(start)); //$NON-NLS-1$
            parameterMap.put("size", String.valueOf(pageSize)); //$NON-NLS-1$
        }
        if (before != null) {
            parameterMap.put("before", DEFAULT_DATE_FORMAT.format(before)); //$NON-NLS-1$
        }
        ClientResourceWrapper client = getClient();
        client.init(Method.GET, restServiceUrl + '/' + dataContainer + "/execs/", parameterMap); //$NON-NLS-1$
        client.setCallback(new ResourceSessionAwareCallbackHandler() {

            @Override
            public void doProcess(Request request, Response response) throws Exception {

                final Map<String, StagingAreaExecutionModel> exeIds = new LinkedHashMap<String, StagingAreaExecutionModel>();
                DomRepresentation rep = new DomRepresentation(response.getEntity());
                NodeList list = rep.getDocument().getDocumentElement().getChildNodes();
                if (list != null) {
                    for (int i = 0; i < list.getLength(); i++) {
                        Node node = list.item(i);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            exeIds.put(node.getFirstChild().getNodeValue(), null);
                        }
                    }
                }
                if (exeIds.size() == 0) {
                    callback.onSuccess(new ArrayList<StagingAreaExecutionModel>());
                    return;
                }
                final int[] counter = new int[1];
                for (final String exeId : exeIds.keySet()) {
                    getStagingAreaExecution(dataContainer, exeId, new SessionAwareAsyncCallback<StagingAreaExecutionModel>() {

                        @Override
                        public void onSuccess(StagingAreaExecutionModel result) {
                            exeIds.put(exeId, result);
                            counter[0]++;
                            if (counter[0] == exeIds.size()) {
                                callback.onSuccess(new ArrayList<StagingAreaExecutionModel>(exeIds.values()));
                            }
                        }

                        @Override
                        protected void doOnFailure(Throwable caught) {
                            counter[0]++;
                            alertStagingError(caught);
                        }
                    });
                }

            }
        });
        client.request();

    }

    /**
     * Get new validation task status
     */
    public void getValidationTaskStatus(String dataContainer, final StagingAreaValidationModel model) {
        ClientResourceWrapper client = getClient();
        client.init(Method.GET, restServiceUrl + '/' + dataContainer + "/execs/current"); //$NON-NLS-1$
        client.setCallback(new ResourceSessionAwareCallbackHandler() {

            @Override
            public void doProcess(Request request, Response response) throws Exception {
                StagingModelConverter.set(response, model);
            }
        });
        client.request();
    }

    /**
     * Run a validation task
     */
    public void runValidationTask(String dataContainer, String dataModel, Object entity) {
        if (dataContainer == null || dataModel == null) {
            throw new IllegalArgumentException();
        }
        Map<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put("model", dataModel); //$NON-NLS-1$
        ClientResourceWrapper client = getClient();
        client.init(Method.POST, restServiceUrl + '/' + dataContainer + '/', parameterMap);
        client.setPostEntity(entity);
        // Need a callback even if we're not interested in the result
        client.setCallback(new ResourceSessionAwareCallbackHandler() {

            @Override
            public void doProcess(Request request, Response response) throws Exception {
            }
        });
        client.request(MediaType.APPLICATION_XML);
    }

    /**
     * Cancel current validation task
     */
    public void cancelValidationTask(String dataContainer) {
        ClientResourceWrapper client = getClient();
        client.init(Method.DELETE, restServiceUrl + '/' + dataContainer + "/execs/current"); //$NON-NLS-1$
        // Need a callback even if we're not interested in the result
        client.setCallback(new ResourceSessionAwareCallbackHandler() {

            @Override
            public void doProcess(Request request, Response response) throws Exception {
            }
        });
        client.request();
    }

    public void countStagingAreaExecutions(final String dataContainer, StagingAreaExecutionModel criteria,
            final SessionAwareAsyncCallback<Integer> callback) {

        Map<String, String> parameterMap = new HashMap<String, String>();
        if (criteria != null && criteria.getStartDate() != null) {
            parameterMap.put("before", DEFAULT_DATE_FORMAT.format(criteria.getStartDate())); //$NON-NLS-1$
        }
        // do request
        ClientResourceWrapper client = getClient();
        client.init(Method.GET, restServiceUrl + '/' + dataContainer + "/execs/count", parameterMap); //$NON-NLS-1$
        client.setCallback(new ResourceSessionAwareCallbackHandler() {

            @Override
            public void doProcess(Request request, Response response) throws Exception {

                int count = 0;
                if (response.getEntity() != null && response.getEntity().getText() != null) {
                    try {
                        count = Integer.parseInt(response.getEntity().getText());
                    } catch (NumberFormatException e) {
                        count = Integer.MAX_VALUE;
                    }
                }
                callback.onSuccess(count);

            }
        });
        client.request(MediaType.TEXT_PLAIN);
    }
}
