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

import org.talend.mdm.webapp.welcomeportal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.welcomeportal.client.mvc.BaseConfigModel;
import org.talend.mdm.webapp.welcomeportal.client.mvc.ConfigModel;
import org.talend.mdm.webapp.welcomeportal.client.mvc.EntityConfigModel;
import org.talend.mdm.webapp.welcomeportal.client.mvc.TimeframeConfigModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class PortletConfigDialog extends Window {

    private static final String AUTO_ON = "on"; //$NON-NLS-1$

    private static final String AUTO_OFF = "off"; //$NON-NLS-1$

    private static final String TOP_ALL = "all"; //$NON-NLS-1$

    private static final String TOP_TEN = "10"; //$NON-NLS-1$

    private static final String TOP_FIVE = "5"; //$NON-NLS-1$

    private static final String TIMEFRAME_ALL = "all"; //$NON-NLS-1$

    private static final String TIMEFRAME_WEEK = "week"; //$NON-NLS-1$

    private static final String TIMEFRAME_DAY = "day"; //$NON-NLS-1$

    public interface PortletConfigListener {

        void onConfigUpdate(ConfigModel configModel);
    }

    private FormPanel container = new FormPanel();

    private String auto;

    private String top;

    private String timeframe;

    private ConfigModel configModel;

    private boolean isForNonCharts;

    private boolean isForTopEntities;

    private Button ok = new Button(MessagesFactory.getMessages().chart_config_ok());

    private Button cancel = new Button(MessagesFactory.getMessages().chart_config_cancel());

    public static PortletConfigDialog showConfig(ConfigModel configModel, PortletConfigListener listener) {
        PortletConfigDialog dialog = new PortletConfigDialog(configModel, listener);
        dialog.show();
        return dialog;
    }

    public PortletConfigDialog(ConfigModel configModel, final PortletConfigListener listener) {
        if (configModel instanceof EntityConfigModel || configModel instanceof TimeframeConfigModel) {
            this.configModel = new EntityConfigModel(configModel.isAutoRefresh(), configModel.getSetting());
        } else {
            this.configModel = new BaseConfigModel(configModel.isAutoRefresh());
        }

        if (configModel.getSetting().equals(configModel.isAutoRefresh().toString())) {
            isForNonCharts = true;
        } else if (configModel instanceof EntityConfigModel) {
            isForTopEntities = true;
            this.configModel = new EntityConfigModel(configModel.getSetting());
        } else {
            this.configModel = new TimeframeConfigModel(configModel.getSetting());
        }

        this.setClosable(false);
        this.setLayout(new FitLayout());
        this.setModal(true);
        this.setHeading(MessagesFactory.getMessages().chart_config_title());
        this.setFrame(true);
        this.setPlain(true);
        this.setBlinkModal(true);

        container.setBorders(false);
        container.setBodyBorder(false);
        container.setHeaderVisible(false);
        container.setStyleAttribute("background", "#fff"); //$NON-NLS-1$ //$NON-NLS-2$
        container.setLabelAlign(FormPanel.LabelAlign.TOP);

        final RadioGroup configGroupAutoRefresh = new RadioGroup() {

            @Override
            protected void onRadioSelected(Radio radio) {
                // Oddly, radio.getName returns RadioGroup's name, so use FieldLabel iso.
                String rdName = radio.getFieldLabel();
                auto = rdName;
            }
        };

        final RadioGroup configGroupChart = new RadioGroup() {

            @Override
            protected void onRadioSelected(Radio radio) {
                // Oddly, radio.getName returns RadioGroup's name, so use FieldLabel iso.
                String rdName = radio.getFieldLabel();
                if (isForTopEntities) {
                    top = rdName;
                } else {
                    timeframe = rdName;
                }
            }
        };

        configGroupAutoRefresh.setOrientation(Orientation.HORIZONTAL);
        configGroupAutoRefresh.setFieldLabel(MessagesFactory.getMessages().autorefresh());
        configGroupChart.setFieldLabel(MessagesFactory.getMessages().entities());
        configGroupChart.setOrientation(Orientation.VERTICAL);

        Radio autoOn = new Radio();
        autoOn.setFieldLabel(AUTO_ON);
        autoOn.setBoxLabel(MessagesFactory.getMessages().autorefresh_on());
        configGroupAutoRefresh.add(autoOn);

        Radio autoOff = new Radio();
        autoOff.setFieldLabel(AUTO_OFF);
        autoOff.setBoxLabel(MessagesFactory.getMessages().autorefresh_off());
        configGroupAutoRefresh.add(autoOff);

        if (configModel.isAutoRefresh()) {
            autoOn.setValue(true);
        } else {
            autoOff.setValue(true);
        }

        if (!isForNonCharts) {
            if (isForTopEntities) {

                configGroupChart.setName("Entities"); //$NON-NLS-1$
                configGroupChart.setFieldLabel(MessagesFactory.getMessages().entities());

                Radio top5Rd = new Radio();
                top5Rd.setFieldLabel(TOP_FIVE);
                top5Rd.setBoxLabel(MessagesFactory.getMessages().chart_config_top5());
                configGroupChart.add(top5Rd);

                Radio top10Rd = new Radio();
                top10Rd.setFieldLabel(TOP_TEN);
                top10Rd.setBoxLabel(MessagesFactory.getMessages().chart_config_top10());
                configGroupChart.add(top10Rd);

                Radio topAllRd = new Radio();
                topAllRd.setFieldLabel(TOP_ALL);
                topAllRd.setBoxLabel(MessagesFactory.getMessages().chart_config_all());
                configGroupChart.add(topAllRd);

                if (TOP_FIVE.equals(configModel.getSetting())) {
                    top5Rd.setValue(true);
                } else if (TOP_TEN.equals(configModel.getSetting())) {
                    top10Rd.setValue(true);
                } else {
                    topAllRd.setValue(true);
                }
            } else {
                configGroupChart.setName("Time Frame"); //$NON-NLS-1$
                configGroupChart.setFieldLabel(MessagesFactory.getMessages().timeframe());

                Radio lastWeekRd = new Radio();
                lastWeekRd.setFieldLabel(TIMEFRAME_WEEK);
                lastWeekRd.setBoxLabel(MessagesFactory.getMessages().chart_config_week());
                configGroupChart.add(lastWeekRd);

                Radio lastDayRd = new Radio();
                lastDayRd.setFieldLabel(TIMEFRAME_DAY);
                lastDayRd.setBoxLabel(MessagesFactory.getMessages().chart_config_day());
                configGroupChart.add(lastDayRd);

                Radio allTimeRd = new Radio();
                allTimeRd.setFieldLabel(TIMEFRAME_ALL);
                allTimeRd.setBoxLabel(MessagesFactory.getMessages().chart_config_all());
                configGroupChart.add(allTimeRd);

                if (TIMEFRAME_DAY.equals(configModel.getSetting())) {
                    lastDayRd.setValue(true);
                } else if (TIMEFRAME_WEEK.equals(configModel.getSetting())) {
                    lastWeekRd.setValue(true);
                } else {
                    allTimeRd.setValue(true);
                }
            }
        }

        FormData formData = new FormData();
        formData.setMargins(new Margins(0, -20, 0, 20));
        if (isForNonCharts) {
            container.add(configGroupAutoRefresh, formData);
            setSize(240, 150);
        } else {
            container.add(configGroupAutoRefresh, formData);
            container.add(configGroupChart, formData);
            setSize(240, 245);
        }

        ok.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                hide();
                ((BaseConfigModel) PortletConfigDialog.this.configModel).setAutoRefresh(auto.equals(AUTO_ON));

                if (!isForNonCharts) {
                    if (isForTopEntities) {
                        ((EntityConfigModel) PortletConfigDialog.this.configModel).setTopEntities(top);
                    } else {
                        ((TimeframeConfigModel) PortletConfigDialog.this.configModel).setTimeFrame(timeframe);
                    }
                }

                listener.onConfigUpdate(PortletConfigDialog.this.configModel);
            }
        });

        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                hide();
            }
        });

        this.setButtonAlign(HorizontalAlignment.CENTER);
        this.addButton(ok);
        this.addButton(cancel);

        this.add(container);

    }

}
