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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.widget.PortletConstants;
import org.talend.mdm.webapp.welcomeportal.client.MainFramePanel;
import org.talend.mdm.webapp.welcomeportal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.welcomeportal.client.rest.StatisticsRestServiceHandler;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

public class RoutingChart extends ChartPortlet {

    private static String ROUTING_STATUS_FAILED = "failed"; //$NON-NLS-1$

    private static String ROUTING_STATUS_COMPLETED = "completed"; //$NON-NLS-1$

    public RoutingChart(MainFramePanel portal) {
        super(PortletConstants.ROUTING_EVENT_CHART_NAME, portal, MessagesFactory.getMessages().chart_routing_event_title(), false);
    }

    @Override
    public void refresh() {
        StatisticsRestServiceHandler.getInstance().getRoutingEventStats(configModel, new SessionAwareAsyncCallback<JSONArray>() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                Map<String, Object> newData = parseJSONData(jsonArray);
                doRefreshWith(newData);
            }
        });

    }

    @Override
    protected void initChart() {
        StatisticsRestServiceHandler.getInstance().getRoutingEventStats(configModel, new SessionAwareAsyncCallback<JSONArray>() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                chartData = parseJSONData(jsonArray);
                initAndShow();
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Map<String, Object> parseJSONData(JSONArray jsonArray) {
        assert (jsonArray.size() == 2);

        Map<String, Object> routingDataNew = new HashMap<String, Object>();

        JSONObject failedJSONObj;
        JSONObject completedJSONObj;
        JSONArray failures;
        JSONArray completes;
        failedJSONObj = (JSONObject) jsonArray.get(0);
        completedJSONObj = (JSONObject) jsonArray.get(1);
        String currApp;
        Set<String> appNames = new HashSet<String>();
        failures = failedJSONObj.get(ROUTING_STATUS_FAILED).isArray();
        Map<String, Integer> completesMap = new HashMap<String, Integer>();

        JSONObject curFailure;
        Map<String, Integer> failureMap = new HashMap<String, Integer>();
        int numOfFailed = 0;
        for (int i = 0; i < failures.size(); i++) {
            curFailure = failures.get(i).isObject();
            currApp = curFailure.keySet().iterator().next();
            numOfFailed = (int) curFailure.get(currApp).isNumber().getValue();
            failureMap.put(currApp, numOfFailed);
            appNames.add(currApp);
        }

        completes = completedJSONObj.get(ROUTING_STATUS_COMPLETED).isArray();
        JSONObject curComplete;
        int numOfCompleted = 0;
        for (int i = 0; i < completes.size(); i++) {
            curComplete = completes.get(i).isObject();
            currApp = curComplete.keySet().iterator().next();
            numOfCompleted = (int) curComplete.get(currApp).isNumber().getValue();
            completesMap.put(currApp, numOfCompleted);
            appNames.add(currApp);
        }

        Map<String, Integer> status;
        for (String appName : appNames) {
            status = new HashMap<String, Integer>(2);
            status.put(ROUTING_STATUS_FAILED, !failureMap.containsKey(appName) ? 0 : failureMap.get(appName));
            status.put(ROUTING_STATUS_COMPLETED, !completesMap.containsKey(appName) ? 0 : completesMap.get(appName));
            routingDataNew.put(appName, status);
        }

        return routingDataNew;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean isDifferentFrom(Map<String, Object> newData) {
        if (chartData.size() != newData.size()) {
            return true;
        } else {
            Map<String, Integer> status;
            Map<String, Integer> statusNew;
            for (String appName : chartData.keySet()) {
                status = (Map<String, Integer>) chartData.get(appName);
                statusNew = newData.get(appName) != null ? (Map<String, Integer>) newData.get(appName) : null;
                if (statusNew == null) {
                    return true;
                }

                if (!status.get(ROUTING_STATUS_FAILED).equals(statusNew.get(ROUTING_STATUS_FAILED))
                        || !status.get(ROUTING_STATUS_COMPLETED).equals(statusNew.get(ROUTING_STATUS_COMPLETED))) {
                    return true;
                }

            }
        }
        return false;
    }

    @Override
    protected int getSeriesSize() {
        return 2;
    }
    
    @Override
    protected String[] getSeriesLabels(){
        String[] labels = { MessagesFactory.getMessages().chart_routing_event_completed(), MessagesFactory.getMessages().chart_routing_event_failed() };
        return labels;
    }
    
    @Override
    protected String[] getSeriesDataKeys(){
        String[] keys = { ROUTING_STATUS_COMPLETED, ROUTING_STATUS_FAILED };
        return keys;
    }
}
