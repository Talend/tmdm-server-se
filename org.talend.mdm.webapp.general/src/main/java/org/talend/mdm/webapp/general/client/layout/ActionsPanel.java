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

import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.client.util.UserContextUtil;
import org.talend.mdm.webapp.general.client.i18n.MessageFactory;
import org.talend.mdm.webapp.general.client.mvc.GeneralEvent;
import org.talend.mdm.webapp.general.model.ActionBean;
import org.talend.mdm.webapp.general.model.ComboBoxModel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class ActionsPanel extends FormPanel {

    private static ActionsPanel instance;

    private ListStore<ComboBoxModel> containerStore = new ListStore<ComboBoxModel>();

    private ComboBox<ComboBoxModel> dataContainerBox = new ComboBox<ComboBoxModel>();

    private ListStore<ComboBoxModel> dataStore = new ListStore<ComboBoxModel>();

    private ComboBox<ComboBoxModel> dataModelBox = new ComboBox<ComboBoxModel>();

    private FormData formData;

    private Button saveBtn = new Button(MessageFactory.getMessages().save());

    private Button saveConfigBtn = new Button(MessageFactory.getMessages().save());

    private ComboBoxModel emptyModelValue = new ComboBoxModel();

    private static Boolean modelSelectFlag = true;

    private static Boolean containerSelectFlag = true;

    private ActionsPanel() {
        super();
        this.setHeading(MessageFactory.getMessages().actions());
        this.setStyleAttribute("background", "#fff"); //$NON-NLS-1$ //$NON-NLS-2$
        FieldSet domainConfig = new FieldSet();
        FormLayout formLayout = new FormLayout(LabelAlign.TOP);
        domainConfig.setLayout(formLayout);
        domainConfig.setHeading(MessageFactory.getMessages().domain_configuration());

        dataContainerBox.setFieldLabel(MessageFactory.getMessages().data_container());
        dataContainerBox.setDisplayField("value"); //$NON-NLS-1$
        dataContainerBox.setValueField("value"); //$NON-NLS-1$
        dataContainerBox.setAllowBlank(false);
        dataContainerBox.setWidth(windowResizeDelay);
        dataContainerBox.setStore(containerStore);
        dataContainerBox.setTypeAhead(true);
        dataContainerBox.setTriggerAction(TriggerAction.ALL);
        dataContainerBox.setEditable(disabled);
        dataContainerBox.setTemplate(getTemplate());
        dataModelBox.setFieldLabel(MessageFactory.getMessages().data_model());
        dataModelBox.setDisplayField("value"); //$NON-NLS-1$
        dataModelBox.setValueField("value"); //$NON-NLS-1$
        dataModelBox.setAllowBlank(false);
        dataModelBox.setWidth(windowResizeDelay);
        dataModelBox.setStore(dataStore);
        dataModelBox.setTypeAhead(true);
        dataModelBox.setTriggerAction(TriggerAction.ALL);
        dataModelBox.setEditable(disabled);
        dataModelBox.setTemplate(getTemplate());
        saveBtn.disable();
        formData = new FormData();
        formData.setMargins(new Margins(3, 0, 3, 0));
        domainConfig.add(dataContainerBox, formData);
        domainConfig.add(dataModelBox, formData);
        domainConfig.add(saveBtn, formData);
        this.add(domainConfig);
        add(PortletConfigFieldSet.getInstance());
        this.setScrollMode(Scroll.AUTO);
        initEvent();
    }

    public static ActionsPanel getInstance() {
        if (instance == null) {
            instance = new ActionsPanel();
        }
        return instance;
    }

    private void initEvent() {
        saveBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher dispatcher = Dispatcher.get();
                dispatcher.dispatch(GeneralEvent.SwitchClusterAndModel);
            }
        });

        dataModelBox.addSelectionChangedListener(new SelectionChangedListener<ComboBoxModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<ComboBoxModel> se) {
                if (se.getSelectedItem() == null) {
                    saveBtn.disable();
                    return;
                }
                String selectedValue = se.getSelectedItem().get("value"); //$NON-NLS-1$
                if (selectedValue != null && !"".equals(selectedValue.trim())) { //$NON-NLS-1$
                    saveBtn.enable();
                    saveConfigBtn.enable();
                    if (!modelSelectFlag) {
                        modelSelectFlag = true;
                        return;
                    }
                    // look for data container
                    for (ComboBoxModel dataModel : dataContainerBox.getStore().getModels()) {
                        if (selectedValue.equals(dataModel.getValue())) {
                            dataContainerBox.setValue(dataModel);
                            if (dataModel.getText() != null && !dataModel.getText().isEmpty()) {
                                dataContainerBox.setTitle(dataModel.getText());
                            } else {
                                dataContainerBox.setTitle(null);
                            }
                            containerSelectFlag = true;
                            saveBtn.enable();
                            saveConfigBtn.enable();
                            return;
                        }
                    }
                    containerSelectFlag = false;
                    dataContainerBox.setValue(emptyModelValue);
                    saveBtn.disable();
                    saveConfigBtn.disable();
                }
            }
        });

        dataContainerBox.addSelectionChangedListener(new SelectionChangedListener<ComboBoxModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<ComboBoxModel> se) {
                if (se.getSelectedItem() == null) {
                    saveBtn.disable();
                    return;
                }
                String selectedValue = se.getSelectedItem().get("value"); //$NON-NLS-1$
                if (selectedValue != null && !"".equals(selectedValue.trim())) { //$NON-NLS-1$
                    saveBtn.enable();
                    saveConfigBtn.enable();
                    if (!containerSelectFlag) {
                        containerSelectFlag = true;
                        return;
                    }
                    // look for data model
                    for (ComboBoxModel dataModel : dataModelBox.getStore().getModels()) {
                        if (selectedValue.equals(dataModel.getValue())) {
                            dataModelBox.setValue(dataModel);
                            if (dataModel.getText() != null && !dataModel.getText().isEmpty()) {
                                dataModelBox.setTitle(dataModel.getText());
                            } else {
                                dataModelBox.setTitle(null);
                            }
                            modelSelectFlag = true;
                            return;
                        }
                    }
                    modelSelectFlag = false;
                    dataModelBox.setValue(emptyModelValue);
                    saveBtn.disable();
                    saveConfigBtn.disable();
                }
            }
        });
    }

    public void loadAction(ActionBean action) {
        containerStore.removeAll();
        dataStore.removeAll();

        List<ComboBoxModel> modelList = getDataModelListForTransferToCurrentLanguageValue(action.getModels());
        List<ComboBoxModel> clusterList = getClusterListCopyModelDescription(action.getClusters(), modelList);

        dataStore.add(modelList);
        containerStore.add(clusterList);

        ComboBoxModel cluster = containerStore.findModel("value", action.getCurrentCluster()); //$NON-NLS-1$
        if (cluster != null) {
            dataContainerBox.setValue(cluster);
        }
        ComboBoxModel model = dataStore.findModel("value", action.getCurrentModel()); //$NON-NLS-1$
        if (model != null) {
            dataModelBox.setValue(model);
        }

        UserContextUtil.setDataContainer(action.getCurrentCluster());
        UserContextUtil.setDataModel(action.getCurrentModel());
    }

    public String getDataCluster() {
        return dataContainerBox.getValue().getValue();
    }

    public String getDataModel() {
        return dataModelBox.getValue().getValue();
    }

    public ComboBox<ComboBoxModel> getDataContainerBox() {
        return dataContainerBox;
    }

    public ComboBox<ComboBoxModel> getDataModelBox() {
        return dataModelBox;
    }

    protected List<ComboBoxModel> getDataModelListForTransferToCurrentLanguageValue(List<ComboBoxModel> oldModelList) {
        List<ComboBoxModel> modelList = new ArrayList<ComboBoxModel>(oldModelList);

        for (ComboBoxModel model : modelList) {
            String transferValue = MultilanguageMessageParser.getValueByLanguage(model.getText(), UserContextUtil.getLanguage());
            transferValue = transferValue == null ? "" : transferValue ;
            model.setText(transferValue);
        }

        return modelList;
    }

    protected List<ComboBoxModel> getClusterListCopyModelDescription(List<ComboBoxModel> oldClusterList,
            List<ComboBoxModel> modelList) {
        List<ComboBoxModel> clusters = new ArrayList<ComboBoxModel>(oldClusterList);

        for (int i = 0; i < clusters.size(); i++) {
            ComboBoxModel cluster = clusters.get(i);
            for (ComboBoxModel model : modelList) {
                if (cluster.getValue().equals(model.getValue())) {
                    clusters.get(i).setText(model.getText());
                    break;
                }
            }
        }
        return clusters;
    }

    private native String getTemplate() /*-{
    return [
            '<tpl for=".">',
            '<div class="x-combo-list-item" title="{text}">{value}</div>',
            '</tpl>' ].join("");
}-*/;

}
