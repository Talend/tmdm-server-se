/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.client.layout;

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.widget.PortletConstants;
import org.talend.mdm.webapp.general.client.General;
import org.talend.mdm.webapp.general.client.GeneralServiceAsync;
import org.talend.mdm.webapp.general.client.i18n.MessageFactory;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class PortletConfigFieldSet extends FieldSet {

    private static PortletConfigFieldSet instance;

    private GeneralServiceAsync service = (GeneralServiceAsync) Registry.get(General.OVERALL_SERVICE);

    private CheckBox startWidgetCheckBox;

    private CheckBox processWidgetCheckBox;

    private CheckBox alertWidgetCheckBox;

    private CheckBox searchWidgetCheckBox;

    private CheckBox tasksWidgetCheckBox;

    private CheckBox chartParentCheckBox;

    private CheckBox dataChartCheckBox;

    private CheckBox routtingEventChartCheckBox;

    private CheckBox journalChartCheckBox;

    private CheckBox matchingChartCheckBox;

    private Button saveButton;

    private Radio col2Radio;

    private Radio col3Radio;

    private static String WELCOMEPORTAL_CONTEXT = "welcomeportal"; //$NON-NLS-1$

    private boolean isEnterprise;

    private static String WELCOMEPORTAL_APP = "WelcomePortal"; //$NON-NLS-1$

    private List<String> allPortlets;

    private List<String> portletsToCheck;

    private int colNum = 3;

    public PortletConfigFieldSet() {
        FormLayout formLayout = new FormLayout();
        formLayout.setLabelAlign(LabelAlign.TOP);
        setLayout(formLayout);
        setHeading(MessageFactory.getMessages().portal_configuration());

        service.isEnterpriseVersion(new SessionAwareAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean isEE) {
                isEnterprise = isEE;
                initWidgetCheckBox();
                saveButton = new Button(MessageFactory.getMessages().save());
                saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        List<String> configUpdates = getPortalConfigUpdate();
                        switchToWelcomeportal();
                        refreshPortal(configUpdates.toString());

                    }
                });
                FormData buttonFormData = new FormData();
                buttonFormData.setMargins(new Margins(2, 0, 2, 0));
                add(saveButton, buttonFormData);
                layout(true);
            }
        });
    }

    public static PortletConfigFieldSet getInstance() {
        if (instance == null) {
            instance = new PortletConfigFieldSet();
        }
        return instance;
    }

    private void initWidgetCheckBox() {
        CheckBoxGroup widgetGroup = new CheckBoxGroup();
        widgetGroup.setFieldLabel(MessageFactory.getMessages().portal_portlets());
        widgetGroup.setOrientation(Orientation.VERTICAL);

        FormData widgetFormData = new FormData();
        widgetFormData.setMargins(new Margins(2, -20, 2, 20));

        startWidgetCheckBox = new CheckBox();
        startWidgetCheckBox.setBoxLabel(MessageFactory.getMessages().portlet_start());
        startWidgetCheckBox.setValue(false);
        startWidgetCheckBox.setVisible(true);
        widgetGroup.add(startWidgetCheckBox);

        processWidgetCheckBox = new CheckBox();
        processWidgetCheckBox.setBoxLabel(MessageFactory.getMessages().portlet_process());
        processWidgetCheckBox.setValue(false);
        processWidgetCheckBox.setVisible(true);
        widgetGroup.add(processWidgetCheckBox);

        if (isEnterprise) {
            alertWidgetCheckBox = new CheckBox();
            alertWidgetCheckBox.setBoxLabel(MessageFactory.getMessages().portlet_alert());
            alertWidgetCheckBox.setValue(false);
            alertWidgetCheckBox.setVisible(true);
            widgetGroup.add(alertWidgetCheckBox);

            searchWidgetCheckBox = new CheckBox();
            searchWidgetCheckBox.setBoxLabel(MessageFactory.getMessages().portlet_search());
            searchWidgetCheckBox.setValue(false);
            searchWidgetCheckBox.setVisible(true);
            widgetGroup.add(searchWidgetCheckBox);

            tasksWidgetCheckBox = new CheckBox();
            tasksWidgetCheckBox.setBoxLabel(MessageFactory.getMessages().portlet_tasks());
            tasksWidgetCheckBox.setValue(false);
            tasksWidgetCheckBox.setVisible(true);
            widgetGroup.add(tasksWidgetCheckBox);

            chartParentCheckBox = new CheckBox() {

                @Override
                protected void onClick(ComponentEvent be) {
                    if (this.getValue()) {
                        showChartCheckBox();
                        dataChartCheckBox.setValue(true);
                        routtingEventChartCheckBox.setValue(true);
                        journalChartCheckBox.setValue(true);
                        matchingChartCheckBox.setValue(true);
                    } else {
                        dataChartCheckBox.setValue(false);
                        routtingEventChartCheckBox.setValue(false);
                        journalChartCheckBox.setValue(false);
                        matchingChartCheckBox.setValue(false);
                    }
                }
            };
            chartParentCheckBox.setBoxLabel(MessageFactory.getMessages().portal_chart_portlets());
            chartParentCheckBox.setValue(false);
            chartParentCheckBox.setVisible(true);
            widgetGroup.add(chartParentCheckBox);
            add(widgetGroup, widgetFormData);
            initChartCheckBox();
            initLayoutRadio();
        } else {
            add(widgetGroup, widgetFormData);
        }
    }

    private void initChartCheckBox() {
        CheckBoxGroup chartGroup = new CheckBoxGroup();
        chartGroup.setFieldLabel(MessageFactory.getMessages().portal_portlets());
        chartGroup.setOrientation(Orientation.VERTICAL);
        chartGroup.setHideLabel(true);
        FormData chartFormData = new FormData();
        chartFormData.setMargins(new Margins(-2, -40, 2, 40));

        dataChartCheckBox = new CheckBox() {

            @Override
            protected void onClick(ComponentEvent be) {
                updateChartParentCheckBox(this.getValue());
            }
        };
        dataChartCheckBox.setBoxLabel(MessageFactory.getMessages().portlet_data());
        dataChartCheckBox.setValue(false);
        dataChartCheckBox.setVisible(false);
        chartGroup.add(dataChartCheckBox);

        routtingEventChartCheckBox = new CheckBox() {

            @Override
            protected void onClick(ComponentEvent be) {
                updateChartParentCheckBox(this.getValue());
            }
        };
        routtingEventChartCheckBox.setBoxLabel(MessageFactory.getMessages().portlet_routing());
        routtingEventChartCheckBox.setValue(false);
        routtingEventChartCheckBox.setVisible(false);
        chartGroup.add(routtingEventChartCheckBox);

        journalChartCheckBox = new CheckBox() {

            @Override
            protected void onClick(ComponentEvent be) {
                updateChartParentCheckBox(this.getValue());
            }
        };
        journalChartCheckBox.setBoxLabel(MessageFactory.getMessages().portlet_journal());
        journalChartCheckBox.setValue(false);
        journalChartCheckBox.setVisible(false);
        chartGroup.add(journalChartCheckBox);

        matchingChartCheckBox = new CheckBox() {

            @Override
            protected void onClick(ComponentEvent be) {
                updateChartParentCheckBox(this.getValue());
            }
        };
        matchingChartCheckBox.setBoxLabel(MessageFactory.getMessages().portlet_matching());
        matchingChartCheckBox.setValue(false);
        matchingChartCheckBox.setVisible(false);
        chartGroup.add(matchingChartCheckBox);
        add(chartGroup, chartFormData);
    }

    private void initLayoutRadio() {
        RadioGroup colRadioGroup = new RadioGroup();
        colRadioGroup.setFieldLabel(MessageFactory.getMessages().portal_columns());
        colRadioGroup.setOrientation(Orientation.VERTICAL);
        FormData layoutFormData = new FormData();
        layoutFormData.setMargins(new Margins(2, -20, 2, 20));

        col2Radio = new Radio();
        col2Radio.setBoxLabel(MessageFactory.getMessages().portal_columns_two());
        colRadioGroup.add(col2Radio);
        col3Radio = new Radio();
        col3Radio.setBoxLabel(MessageFactory.getMessages().portal_columns_three());
        colRadioGroup.add(col3Radio);
        add(colRadioGroup, layoutFormData);
    }

    private void updateChartParentCheckBox(boolean value) {
        if (value) {
            chartParentCheckBox.setValue(true);
        } else if (!dataChartCheckBox.getValue() && !routtingEventChartCheckBox.getValue() && !journalChartCheckBox.getValue()
                && !matchingChartCheckBox.getValue()) {
            chartParentCheckBox.setValue(false);
        }
    }

    private void updatePortletConfig(String dataString) {
        unCheckWidgetCheckBox();
        String[] temp = dataString.split("; "); //$NON-NLS-1$
        String[] selectedCheckBox = temp[0].substring(1, temp[0].length() - 1).split(", "); //$NON-NLS-1$
        for (String checkBox : selectedCheckBox) {
            updateWidgetCheckBox(checkBox, true);
        }
        if (isEnterprise) {
            chartParentCheckBox.setValue(dataChartCheckBox.getValue() || routtingEventChartCheckBox.getValue()
                    || journalChartCheckBox.getValue() || matchingChartCheckBox.getValue());
            int columnNumber = Integer.parseInt(temp[1]);
            col3Radio.setValue(((columnNumber == 3) ? true : false));
            col2Radio.setValue(((columnNumber == 2) ? true : false));
        }
        layout(true);
    }

    private List<String> getPortalConfigUpdate() {
        List<String> updates = new ArrayList<String>();
        if (startWidgetCheckBox.getValue()) {
            updates.add(PortletConstants.START_NAME);
        }
        if (processWidgetCheckBox.getValue()) {
            updates.add(PortletConstants.PROCESS_NAME);
        }
        if (isEnterprise) {
            if (alertWidgetCheckBox.getValue()) {
                updates.add(PortletConstants.ALERT_NAME);
            }
            if (searchWidgetCheckBox.getValue()) {
                updates.add(PortletConstants.SEARCH_NAME);
            }
            if (tasksWidgetCheckBox.getValue()) {
                updates.add(PortletConstants.TASKS_NAME);
            }
            if (dataChartCheckBox.getValue()) {
                updates.add(PortletConstants.DATA_CHART_NAME);
            }
            if (routtingEventChartCheckBox.getValue()) {
                updates.add(PortletConstants.ROUTING_EVENT_CHART_NAME);
            }
            if (journalChartCheckBox.getValue()) {
                updates.add(PortletConstants.JOURNAL_CHART_NAME);
            }
            if (matchingChartCheckBox.getValue()) {
                updates.add(PortletConstants.MATCHING_CHART_NAME);
            }
            updates.add((col3Radio.getValue() ? "3" : "2")); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            updates.add("2"); //$NON-NLS-1$
        }
        return updates;
    }

    public void updateWidgetCheckBox(String name, Boolean value) {
        if (PortletConstants.START_NAME.equals(name)) {
            startWidgetCheckBox.setValue(value);
        } else if (PortletConstants.PROCESS_NAME.equals(name)) {
            processWidgetCheckBox.setValue(value);
        } else if (PortletConstants.ALERT_NAME.equals(name)) {
            alertWidgetCheckBox.setValue(value);
        } else if (PortletConstants.SEARCH_NAME.equals(name)) {
            searchWidgetCheckBox.setValue(value);
        } else if (PortletConstants.TASKS_NAME.equals(name)) {
            tasksWidgetCheckBox.setValue(value);
        } else if (PortletConstants.DATA_CHART_NAME.equals(name)) {
            dataChartCheckBox.setValue(value);
            if (!dataChartCheckBox.isVisible()) {
                showChartCheckBox();
            }
        } else if (PortletConstants.ROUTING_EVENT_CHART_NAME.equals(name)) {
            routtingEventChartCheckBox.setValue(value);
            if (!routtingEventChartCheckBox.isVisible()) {
                showChartCheckBox();
            }
        } else if (PortletConstants.JOURNAL_CHART_NAME.equals(name)) {
            journalChartCheckBox.setValue(value);
            if (!journalChartCheckBox.isVisible()) {
                showChartCheckBox();
            }
        } else if (PortletConstants.MATCHING_CHART_NAME.equals(name)) {
            matchingChartCheckBox.setValue(value);
            if (!matchingChartCheckBox.isVisible()) {
                showChartCheckBox();
            }
        }
    }

    private void showChartCheckBox() {
        dataChartCheckBox.setVisible(true);
        routtingEventChartCheckBox.setVisible(true);
        journalChartCheckBox.setVisible(true);
        matchingChartCheckBox.setVisible(true);
    }

    private void unCheckWidgetCheckBox() {
        startWidgetCheckBox.setValue(false);
        processWidgetCheckBox.setValue(false);
        if (isEnterprise) {
            alertWidgetCheckBox.setValue(false);
            searchWidgetCheckBox.setValue(false);
            tasksWidgetCheckBox.setValue(false);
            dataChartCheckBox.setValue(false);
            routtingEventChartCheckBox.setValue(false);
            journalChartCheckBox.setValue(false);
            matchingChartCheckBox.setValue(false);
        }
    }

    protected void switchToWelcomeportal() {
        display(WELCOMEPORTAL_CONTEXT, WELCOMEPORTAL_APP);
        AccordionMenus.getInstance().selectedItem(AccordionMenus.getInstance().getWelcomeportalItem());
    }

    // call refresh in WelcomePortal
    private native void refreshPortal(String portalConfig)/*-{
		$wnd.amalto.core.refreshPortal(portalConfig);
    }-*/;

    private native void display(String context, String application)/*-{
		if ($wnd.amalto[context]) {
			if ($wnd.amalto[context][application]) {
				$wnd.amalto[context][application].init();
			}
		}
    }-*/;

    public void activateSaveButton() {
        if (!saveButton.isEnabled()) {
            saveButton.enable();
        }
    }
}
