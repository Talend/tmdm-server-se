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
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.widget.PortletConstants;
import org.talend.mdm.webapp.welcomeportal.client.MainFramePanel;
import org.talend.mdm.webapp.welcomeportal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.welcomeportal.client.rest.StatisticsRestServiceHandler;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.HTML;

public class MatchingChart extends ChartPortlet {

    public MatchingChart(MainFramePanel portal) {
        super(PortletConstants.MATCHING_CHART_NAME, portal, MessagesFactory.getMessages().chart_matching_title(), true);
    }

    @Override
    protected void initChart() {
        String noDCAlertPrefix = "<span id=\"licenseAlert\" style=\"padding-right:8px;cursor: pointer;\" class=\"labelStyle\" title=\"" //$NON-NLS-1$
                + MessagesFactory.getMessages().alerts_title() + "\">"; //$NON-NLS-1$
        final String alertIcon = "<IMG SRC=\"secure/img/genericUI/alert-icon.png\"/>&nbsp;"; //$NON-NLS-1$
        final HTML alertHtml = new HTML();
        final StringBuilder noDCAlertMsg = new StringBuilder(noDCAlertPrefix);

        service.getCurrentDataContainer(new SessionAwareAsyncCallback<String>() {

            @Override
            public void onSuccess(String dataContainer) {

                if (dataContainer != null) {
                    dc = dataContainer;
                    dataContainerChanged = false;

                    StatisticsRestServiceHandler.getInstance().getContainerMatchingStats(dataContainer, configModel,
                            new SessionAwareAsyncCallback<JSONArray>() {

                                @Override
                                public void onSuccess(JSONArray jsonArray) {
                                    chartData = parseJSONData(jsonArray);
                                    initAndShow();
                                }
                            });
                } else {
                    noDCAlertMsg.append(alertIcon);
                    noDCAlertMsg.append(MessagesFactory.getMessages().no_container());
                    noDCAlertMsg.append("</span>"); //$NON-NLS-1$
                    alertHtml.setHTML(noDCAlertMsg.toString());
                    fieldSet.add(alertHtml);
                    fieldSet.layout(true);
                }
            }
        });
    }

    @Override
    public void refresh() {
        service.getCurrentDataContainer(new SessionAwareAsyncCallback<String>() {

            @Override
            public void onSuccess(String dataContainer) {

                if (dataContainer != null) {
                    dataContainerChanged = !dc.equals(dataContainer);
                    dc = dataContainer;

                    StatisticsRestServiceHandler.getInstance().getContainerMatchingStats(dataContainer, configModel,
                            new SessionAwareAsyncCallback<JSONArray>() {

                                @Override
                                public void onSuccess(JSONArray jsonArray) {
                                    Map<String, Object> newData = parseJSONData(jsonArray);
                                    if (plot == null) {
                                        chartData = newData;
                                        initAndShow();
                                    } else {
                                        doRefreshWith(newData);
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    protected Map<String, Object> parseJSONData(JSONArray jsonArray) {
        Map<String, Object> matchingDataNew = new HashMap<String, Object>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.get(i).isObject();
            String name = jsonObject.keySet().iterator().next();
            Double value = new Double(jsonObject.get(name).isString().stringValue());
            matchingDataNew.put(name, value);
        }

        return matchingDataNew;
    }

    @Override
    protected boolean isDifferentFrom(Map<String, Object> newData) {
        if (chartData.size() != newData.size()) {
            return true;
        } else {
            for (String entityName : chartData.keySet()) {
                if (!chartData.get(entityName).equals(newData.get(entityName))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    protected String[] getSeriesLabels(){
        String[] labels = { MessagesFactory.getMessages().chart_matching_duplicates() };
        return labels;
    }
    
    @Override
    protected String[] getSeriesColors() {
        String[] colors = { ChartPortlet.SERIES_2_COLOR };
        return colors;
    }

}
