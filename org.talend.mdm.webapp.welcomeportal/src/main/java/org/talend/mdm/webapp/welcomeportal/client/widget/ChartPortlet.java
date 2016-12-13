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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.welcomeportal.client.MainFramePanel;
import org.talend.mdm.webapp.welcomeportal.client.mvc.ConfigModel;
import org.talend.mdm.webapp.welcomeportal.client.mvc.EntityConfigModel;
import org.talend.mdm.webapp.welcomeportal.client.mvc.PortalProperties;
import org.talend.mdm.webapp.welcomeportal.client.mvc.TimeframeConfigModel;
import org.talend.mdm.webapp.welcomeportal.client.resources.icon.Icons;
import org.talend.mdm.webapp.welcomeportal.client.widget.PortletConfigDialog.PortletConfigListener;
import org.talend.mdm.webapp.welcomeportal.client.widget.options.AxeTicks;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.googlecode.gflot.client.DataPoint;
import com.googlecode.gflot.client.Series;
import com.googlecode.gflot.client.SeriesHandler;
import com.googlecode.gflot.client.SimplePlot;
import com.googlecode.gflot.client.Tick;
import com.googlecode.gflot.client.event.PlotHoverListener;
import com.googlecode.gflot.client.event.PlotItem;
import com.googlecode.gflot.client.event.PlotPosition;
import com.googlecode.gflot.client.jsni.Plot;
import com.googlecode.gflot.client.options.AxesOptions;
import com.googlecode.gflot.client.options.AxisOptions;
import com.googlecode.gflot.client.options.BarSeriesOptions;
import com.googlecode.gflot.client.options.BarSeriesOptions.BarAlignment;
import com.googlecode.gflot.client.options.GlobalSeriesOptions;
import com.googlecode.gflot.client.options.GridOptions;
import com.googlecode.gflot.client.options.LegendOptions;
import com.googlecode.gflot.client.options.LineSeriesOptions;
import com.googlecode.gflot.client.options.PlotOptions;

public abstract class ChartPortlet extends BasePortlet {

    public static final String SERIES_1_COLOR = "#00a6ce"; //blue //$NON-NLS-1$

    public static final String SERIES_2_COLOR = "#b6be00"; //dark-green //$NON-NLS-1$

    public static final String SERIES_3_COLOR = "#f7a800"; //orange //$NON-NLS-1$

    public static final String SERIES_4_COLOR = "#8A6A29"; //brown //$NON-NLS-1$

    public static final String COLOR = "#555964"; //dark-grey //$NON-NLS-1$

    public static final String BACKGROUND_COLOR = "#FDFDFD"; //light-gray //$NON-NLS-1$ 

    public static final int MIN_WIDTH_TO_DISPLAY_TEXT = 200; // only display name when plot's width >= 200px

    public static final String[] SERIES_COLORS = { SERIES_1_COLOR, SERIES_2_COLOR, SERIES_2_COLOR, SERIES_3_COLOR };

    protected SimplePlot plot;

    protected Map<String, Object> chartData;

    protected List<String> entityNamesSorted;

    protected String dc;

    protected boolean dataContainerChanged;

    protected PlotHoverListener hoverListener;

    private int plotWidth;

    private int plotHeight;

    public ChartPortlet(String name, MainFramePanel portal, String heading, boolean isEntityConfigModel) {
        super(name, portal);
        setIcon();
        setHeading(heading);
        initConfigModel(isEntityConfigModel);
        initConfigSettings();
        initChart();
    }

    @Override
    protected PortletConfigListener initConfigListener() {
        PortletConfigListener configListener = new PortletConfigListener() {

            @Override
            public void onConfigUpdate(final ConfigModel configModelFromUser) {
                boolean autoUpdated;
                final ConfigModel configModelOrig = configModel;
                if (!configModel.equals(configModelFromUser)) {
                    autoUpdated = !configModel.isAutoRefresh().equals(configModelFromUser.isAutoRefresh());
                    configSettingChanged = !configModel.getSetting().equals(configModelFromUser.getSetting());
                    configModel = configModelFromUser;
                    if (!autoUpdated) { // only setting changed
                        service.savePortalConfig(PortalProperties.KEY_CHART_SETTINGS, portletName, configModel.getSetting(),
                                new SessionAwareAsyncCallback<Void>() {

                                    @Override
                                    public void onSuccess(Void result) {
                                        portalConfigs.add(PortalProperties.KEY_CHART_SETTINGS, portletName,
                                                configModel.getSetting());
                                        if (!configModelFromUser.isAutoRefresh()) {
                                            refresh();
                                        } // else->auto=true, so no need to call redundant refresh()
                                        return;
                                    }

                                    @Override
                                    protected void doOnFailure(Throwable caught) {
                                        super.doOnFailure(caught);
                                        configModel = configModelOrig; // if db update fails, revert to original on UI
                                    }
                                });
                    } else {
                        if (!configSettingChanged) {
                            service.savePortalConfig(PortalProperties.KEY_AUTO_ONOFFS, portletName, configModel.isAutoRefresh()
                                    .toString(), new SessionAwareAsyncCallback<Void>() {

                                @Override
                                public void onSuccess(Void result) {
                                    portalConfigs.add(PortalProperties.KEY_AUTO_ONOFFS, portletName, configModel.isAutoRefresh()
                                            .toString());
                                    ChartPortlet.this.autoRefresh(configModel.isAutoRefresh());
                                }

                                @Override
                                protected void doOnFailure(Throwable caught) {
                                    super.doOnFailure(caught);
                                    // revert to original auto & setting on UI
                                    configModel = configModelOrig;
                                }
                            });

                        } else {
                            List<String> autoAndSetting = Arrays.asList(configModel.isAutoRefresh().toString(),
                                    configModel.getSetting());

                            service.savePortalConfigAutoAndSetting(portletName, autoAndSetting,
                                    new SessionAwareAsyncCallback<Void>() {

                                        @Override
                                        public void onSuccess(Void result1) {
                                            portalConfigs.add(PortalProperties.KEY_AUTO_ONOFFS, portletName, configModel
                                                    .isAutoRefresh().toString());
                                            portalConfigs.add(PortalProperties.KEY_CHART_SETTINGS, portletName,
                                                    configModel.getSetting());
                                            ChartPortlet.this.autoRefresh(configModel.isAutoRefresh());
                                            // user turn off autorefresh, call refresh() to reflect charts with new
                                            // setting
                                            if (!configModel.isAutoRefresh()) {
                                                refresh();
                                            }
                                        }

                                        @Override
                                        protected void doOnFailure(Throwable caught) {
                                            super.doOnFailure(caught);
                                            // revert to original auto & setting on UI
                                            configModel = configModelOrig;
                                        }
                                    });

                        }
                    }
                } else {
                    configSettingChanged = false;
                    return;
                }
            }
        };

        return configListener;
    }

    protected void initPlot() {
        PlotOptions plotOptions = PlotOptions.create();
        plotOptions.setGlobalSeriesOptions(GlobalSeriesOptions.create());
        plot = new SimplePlot(plotOptions);
        plotWidth = this.getWidth() - 50;
        plotHeight = plotWidth - 10;
        plot.setWidth(plotWidth);
        plot.setHeight(plotHeight);
        entityNamesSorted = sort(chartData.keySet());
        furtherInitPlot();
    }

    protected void furtherInitPlot() {
        initOptions();
        initSeries();
    }

    protected void resizePlot() {
        if (isResetXAxesOptions()) {
            plot.getOptions().setXAxesOptions(getXAxesOptions());
        }
        plot.setWidth(plotWidth);
        plot.setHeight(plotHeight);
        plot.redraw();
        fieldSet.layout(true);
    }

    protected void refreshPlot() {
        updatePlot();
        plot.redraw();
        fieldSet.layout(true);
    }

    protected void initAndShow() {
        initPlot();
        addPlotHovering();
        addPlotClick();
        fieldSet.removeAll();
        fieldSet.add(plot);
        fieldSet.layout(true);
        this.autoRefresh(configModel.isAutoRefresh());

        this.addListener(Events.Resize, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                plotWidth = ChartPortlet.this.getWidth() - 50;
                plotHeight = plotWidth - 10;
                resizePlot();
            }
        });
    }

    protected void doRefreshWith(Map<String, Object> newData) {
        if (dataContainerChanged || configSettingChanged || isDifferentFrom(newData)) {
            chartData = newData;
            refreshPlot();
            if (configSettingChanged) {
                configSettingChanged = !configSettingChanged;
            }
        }
    }

    protected List<String> sort(Set<String> names) {
        List<String> appnamesSorted = new ArrayList<String>(names);
        Collections.sort(appnamesSorted);

        return appnamesSorted;
    }

    protected void setIcon() {
        this.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.chart()));
    }

    abstract Map<String, Object> parseJSONData(JSONArray jsonArray);

    protected void updatePlot() {
        entityNamesSorted = sort(chartData.keySet());
        plot.getOptions().setXAxesOptions(getXAxesOptions());
        for (int i = 0; i < getSeriesSize(); i++) {
            plot.getModel().clearSeries(i);
        }
        addDataToSeries();
    }

    abstract protected boolean isDifferentFrom(Map<String, Object> newData);

    abstract protected void initChart();

    protected void initConfigModel(boolean isEntityConfigModel) {
        String setting = portalConfigs.getChartSetting(portletName);
        if (setting != null) {
            configModel = isEntityConfigModel ? new EntityConfigModel(startedAsOn, setting) : new TimeframeConfigModel(
                    startedAsOn, setting);
        } else {
            configModel = isEntityConfigModel ? new EntityConfigModel(startedAsOn) : new TimeframeConfigModel(startedAsOn);
        }
    }

    protected void initSeries() {
        String[] labels = getSeriesLabels();
        String[] colors = getSeriesColors();
        for (int i = 0; i < getSeriesSize(); i++) {
            plot.getModel().addSeries(Series.of(labels[i]).setColor(colors[i]));
        }
        addDataToSeries();
    }

    protected void initOptions() {
        PlotOptions plotOptions = plot.getOptions();
        plotOptions
                .setGlobalSeriesOptions(
                        GlobalSeriesOptions
                                .create()
                                .setHighlightColor("rgba(255, 255, 255, 0.3)") //$NON-NLS-1$
                                .setLineSeriesOptions(LineSeriesOptions.create().setShow(false).setSteps(false))
                                .setBarsSeriesOptions(
                                        BarSeriesOptions.create().setShow(true).setBarWidth(0.9).setFill(1)
                                                .setAlignment(BarAlignment.CENTER)).setStack(false))
                .setYAxesOptions(AxesOptions.create().addAxisOptions(AxisOptions.create().setTickDecimals(0).setMinimum(0)))
                .setXAxesOptions(getXAxesOptions());

        plotOptions.setLegendOptions(LegendOptions.create().setShow(true));
        plotOptions.setGridOptions(GridOptions.create().setHoverable(true).setBorderWidth(0).setColor(COLOR)
                .setBackgroundColor(BACKGROUND_COLOR));
    }

    @SuppressWarnings("unchecked")
    protected void addDataToSeries() {
        List<? extends SeriesHandler> series = plot.getModel().getHandlers();
        double x = 0;
        int step = getSeriesSize() == 1 ? 1 : getSeriesSize() + 1;
        for (String entityName : entityNamesSorted) {
            if (getSeriesSize() == 1) {
                series.get(0).add(DataPoint.of(x, (Double) chartData.get(entityName)));
            } else {
                String[] keys = getSeriesDataKeys();
                Map<String, Integer> data = (Map<String, Integer>) chartData.get(entityName);
                for (int i = 0; i < series.size(); i++) {
                    series.get(i).add(DataPoint.of(x + i, data.get(keys[i])));
                }
            }
            x += step;
        }
    }

    protected boolean isResetXAxesOptions() {
        return true;
    }

    protected int getSeriesSize() {
        return 1;
    }

    protected String[] getSeriesLabels() {
        return null;
    }

    protected String[] getSeriesDataKeys() {
        return null;
    }

    protected String[] getSeriesColors() {
        String[] colors = new String[getSeriesSize()];
        for (int i = 0; i < colors.length; i++) {
            if (i <= SERIES_COLORS.length - 1) {
                colors[i] = SERIES_COLORS[i];
            } else {
                colors[i] = COLOR;
            }
        }
        return colors;
    }

    protected void addPlotHovering() {
        final PopupPanel popup = new PopupPanel();
        final Label hoverLabel = new Label();
        popup.add(hoverLabel);
        plot.addHoverListener(new PlotHoverListener() {

            @Override
            public void onPlotHover(Plot plotArg, PlotPosition position, PlotItem item) {
                if (item != null) {
                    String text = getHoveringText(item);
                    hoverLabel.setText(text);
                    hoverLabel.setStyleName("welcomePieChartHover"); //$NON-NLS-1$
                    popup.setPopupPosition(item.getPageX() + 10, item.getPageY() - 25);
                    popup.show();
                } else {
                    popup.hide();
                }

            }

        }, false);
    }

    protected void addPlotClick() {

    }

    protected String getHoveringText(PlotItem item) {
        int valueY = (int) item.getDataPoint().getY();
        int valueX = (int) item.getDataPoint().getX();
        int nameIndex = valueX / (getSeriesSize() == 1 ? 1 : getSeriesSize() + 1);

        String hoveringText = entityNamesSorted.get(nameIndex) + ": " + valueY;//$NON-NLS-1$
        if (getSeriesSize() > 1) {
            hoveringText += "(" + item.getSeries().getLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return hoveringText;
    }

    protected AxesOptions getXAxesOptions() {
        return AxesOptions.create().addAxisOptions(
                AxisOptions.create().setAxisLabelAngle(70d).setTicks(getTicks()).setAutoscaleMargin(0.1));
    }

    protected AxeTicks getTicks() {
        AxeTicks entityTicks = AxeTicks.create();
        double x = getSeriesSize() == 1 ? 0 : 1;
        int step = getSeriesSize() == 1 ? 1 : getSeriesSize() + 1;
        for (String entityName : entityNamesSorted) {
            entityTicks.push(Tick.of(x, plotWidth >= MIN_WIDTH_TO_DISPLAY_TEXT ? entityName : "")); //$NON-NLS-1$
            x += step;
        }
        return entityTicks;
    }

}
