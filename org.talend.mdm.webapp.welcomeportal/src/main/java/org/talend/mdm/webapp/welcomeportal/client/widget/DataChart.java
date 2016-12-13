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
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.widget.PortletConstants;
import org.talend.mdm.webapp.welcomeportal.client.MainFramePanel;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortal;
import org.talend.mdm.webapp.welcomeportal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.welcomeportal.client.rest.StatisticsRestServiceHandler;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.googlecode.gflot.client.DataPoint;
import com.googlecode.gflot.client.PlotModel;
import com.googlecode.gflot.client.Series;
import com.googlecode.gflot.client.SeriesHandler;
import com.googlecode.gflot.client.event.PlotHoverListener;
import com.googlecode.gflot.client.event.PlotItem;
import com.googlecode.gflot.client.event.PlotLoadEvent;
import com.googlecode.gflot.client.event.PlotPosition;
import com.googlecode.gflot.client.jsni.Plot;
import com.googlecode.gflot.client.options.GlobalSeriesOptions;
import com.googlecode.gflot.client.options.GridOptions;
import com.googlecode.gflot.client.options.LegendOptions;
import com.googlecode.gflot.client.options.PieSeriesOptions;
import com.googlecode.gflot.client.options.PieSeriesOptions.Highlight;
import com.googlecode.gflot.client.options.PieSeriesOptions.Label.Background;
import com.googlecode.gflot.client.options.PieSeriesOptions.Label.Formatter;
import com.googlecode.gflot.client.options.PlotOptions;

public class DataChart extends ChartPortlet {

    public static final String COUNT_NAME = "count"; //$NON-NLS-1$

    public static final String PERCENTAGE_NAME = "percentage"; //$NON-NLS-1$

    private String hoveringTXT;

    private String entityName;

    private int cursorX;

    private int cursorY;

    final NumberFormat formatter = NumberFormat.getFormat("0.##"); //$NON-NLS-1$

    Map<String, Double> percentageValueMap = new HashMap<String, Double>();

    public DataChart(MainFramePanel portal) {
        super(PortletConstants.DATA_CHART_NAME, portal, MessagesFactory.getMessages().chart_data_title(), true);
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

                    StatisticsRestServiceHandler.getInstance().getContainerDataStats(dataContainer, configModel,
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

                dataContainerChanged = !dc.equals(dataContainer);
                dc = dataContainer;

                StatisticsRestServiceHandler.getInstance().getContainerDataStats(dataContainer, configModel,
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
        });

    }

    @Override
    protected void furtherInitPlot() {
        PlotModel model = plot.getModel();
        PlotOptions plotOptions = plot.getOptions();

        plotOptions.setGlobalSeriesOptions(GlobalSeriesOptions.create().setPieSeriesOptions(
                PieSeriesOptions
                        .create()
                        .setShow(true)
                        .setRadius(1)
                        .setInnerRadius(0.2)
                        .setHighlight(Highlight.create().setOpacity(0.3))
                        .setLabel(
                                com.googlecode.gflot.client.options.PieSeriesOptions.Label.create().setShow(true)
                                        .setRadius(3d / 4d).setBackground(Background.create().setOpacity(0.8)).setThreshold(0.05)
                                        .setFormatter(new Formatter() {

                                            @Override
                                            public String format(String label, Series series) {
                                                return "<div class='welcomePieChartLabel'>" //$NON-NLS-1$
                                                        + label + "<br/>" + formatter.format(series.getData().getY(0)) + " / " //$NON-NLS-1$//$NON-NLS-2$
                                                        + formatter.format(percentageValueMap.get(label)) + "%</div>"; //$NON-NLS-1$
                                            }
                                        }))));
        plotOptions.setLegendOptions(LegendOptions.create().setShow(false));
        plotOptions.setGridOptions(GridOptions.create().setHoverable(true));

        JsArrayString colors = JavaScriptObject.createArray().cast();
        colors.push(SERIES_1_COLOR);
        colors.push(SERIES_2_COLOR);
        colors.push(SERIES_3_COLOR);
        colors.push(SERIES_4_COLOR);
        plotOptions.setDefaultColorTheme(colors);

        // create series and add data
        for (String entityName : entityNamesSorted) {
            SeriesHandler seriesEntity = model.addSeries(Series.of(entityName));
            seriesEntity.add(DataPoint.of(entityName, (Integer) chartData.get(entityName)));
        }

        plot.addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                hoveringTXT = ""; //$NON-NLS-1$
                entityName = ""; //$NON-NLS-1$
                portal.setStyleAttribute("cursor", "default"); //$NON-NLS-1$//$NON-NLS-2$
            }
        }, MouseOutEvent.getType());

        plot.addDomHandler(new MouseMoveHandler() {

            @Override
            public void onMouseMove(MouseMoveEvent event) {
                cursorX = event.getClientX() + 10;
                cursorY = event.getClientY() - 10;
            }

        }, MouseMoveEvent.getType());

        plot.addLoadHandler(new PlotLoadEvent.Handler() {

            @Override
            public void onLoad(PlotLoadEvent event) {
                // Flot has limitations on the pie labels
                // If it cannot be drawn it creates a div of class error with message
                // "Could not draw pie with labels contained inside canvas"
                // Workaround the issue by redrawing but without the labels
                boolean errorOccurred = plot.getParent().getElement().getInnerHTML()
                        .contains("Could not draw pie with labels contained inside canvas"); //$NON-NLS-1$
                if (errorOccurred) {// don't show labels if error occurred
                    plot.getOptions().getGlobalSeriesOptions().getPieSeriesOptions().getLabel().setShow(false);
                    plot.redraw();
                } else {// reset to true for next creation
                    plot.getOptions().getGlobalSeriesOptions().getPieSeriesOptions().getLabel().setShow(true);
                }
            }
        });
    }

    @Override
    protected void updatePlot() {
        PlotModel model = plot.getModel();
        entityNamesSorted = sort(chartData.keySet());

        if (!dataContainerChanged && !configSettingChanged) {
            // keep SeriesHandler, just clean their data
            model.clear();
            List<? extends SeriesHandler> series = model.getHandlers();

            // new entities maybe added to DM and needs to be reflected on the new chart
            Map<String, SeriesHandler> seriesMap = new HashMap<String, SeriesHandler>(entityNamesSorted.size());
            int count = 0;
            int prevTotal = series.size();
            for (String entityName : entityNamesSorted) {
                if (count < prevTotal) {
                    seriesMap.put(entityName, series.get(count++));
                } else {// for newly added enities in the same dm
                    seriesMap.put(entityName, model.addSeries(Series.of(entityName)));
                }
            }

            for (String entityName : entityNamesSorted) {
                seriesMap.get(entityName).add(DataPoint.of(entityName, (Integer) chartData.get(entityName)));
            }
        } else {
            // switched to diff dm/updated config, them dump and rebuild all Series
            model.removeAllSeries();
            for (String entityName : entityNamesSorted) {
                SeriesHandler seriesEntity = model.addSeries(Series.of(entityName));
                seriesEntity.add(DataPoint.of(entityName, (Integer) chartData.get(entityName)));
            }
        }
    }

    @Override
    protected Map<String, Object> parseJSONData(JSONArray jsonArray) {
        Map<String, Object> entityDataNew = new HashMap<String, Object>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.get(i).isObject();
            String name = jsonObject.keySet().iterator().next();
            JSONArray valueArray = jsonObject.get(name).isArray();
            int count = 0;
            double percentage = 0;
            for (int j = 0; j < valueArray.size(); j++) {
                JSONObject countObject = valueArray.get(j).isObject();
                String countName = countObject.keySet().iterator().next();
                if (COUNT_NAME.equals(countName)) {
                    count = new Double(countObject.get(countName).isNumber().doubleValue()).intValue();
                } else if (PERCENTAGE_NAME.equals(countName)) {
                    percentage = Double.parseDouble(countObject.get(countName).isString().stringValue());
                }
            }
            entityDataNew.put(name, count);
            percentageValueMap.put(name, percentage);

        }

        return entityDataNew;
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
    protected void addPlotHovering() {
        final PopupPanel popup = new PopupPanel();
        final Label hoverLabel = new Label();
        popup.add(hoverLabel);

        plot.addHoverListener(new PlotHoverListener() {

            @Override
            public void onPlotHover(Plot plot, PlotPosition position, PlotItem item) {
                if (item != null) {
                    hoveringTXT = (entityNamesSorted.get(item.getSeriesIndex()) + " : " //$NON-NLS-1$ 
                            + formatter.format(item.getSeries().getData().getY(0)) + " / " //$NON-NLS-1$
                            + formatter.format(percentageValueMap.get(item.getSeries().getLabel())) + "%"); //$NON-NLS-1$
                    hoverLabel.setText(hoveringTXT);
                    hoverLabel.setStyleName("welcomePieChartHover"); //$NON-NLS-1$
                    popup.setPopupPosition(cursorX, cursorY);
                    popup.show();
                    entityName = entityNamesSorted.get(item.getSeriesIndex());
                    portal.setStyleAttribute("cursor", "pointer"); //$NON-NLS-1$ //$NON-NLS-2$ 
                } else {
                    popup.hide();
                    entityName = ""; //$NON-NLS-1$
                    portal.setStyleAttribute("cursor", "default"); //$NON-NLS-1$//$NON-NLS-2$
                }
            }
        }, false);
    }

    @Override
    protected void addPlotClick() {

        plot.addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (entityName != null && !entityName.equals("") && Cookies.isCookieEnabled()) { //$NON-NLS-1$
                    Cookies.setCookie(PortletConstants.PARAMETER_ENTITY, entityName);
                    portal.itemClick(WelcomePortal.BROWSECONTEXT, WelcomePortal.BROWSEAPP);
                }
            }
        }, ClickEvent.getType());
    }

    @Override
    protected boolean isResetXAxesOptions() {
        return false;
    }
}
