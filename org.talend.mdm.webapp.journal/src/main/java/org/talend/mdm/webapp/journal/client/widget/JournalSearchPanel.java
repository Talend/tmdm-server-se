/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.util.PostDataUtil;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.client.util.UserContextUtil;
import org.talend.mdm.webapp.journal.client.Journal;
import org.talend.mdm.webapp.journal.client.JournalEvents;
import org.talend.mdm.webapp.journal.client.JournalServiceAsync;
import org.talend.mdm.webapp.journal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.journal.client.util.KeyUtil;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;

import com.amalto.core.objects.UpdateReportPOJO;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * DOC Administrator  class global comment. Detailled comment
 */
public class JournalSearchPanel extends FormPanel {

    protected static JournalSearchPanel formPanel;
    
    private JournalServiceAsync service = Registry.get(Journal.JOURNAL_SERVICE);

    protected ComboBox<ItemBaseModel> dataModelCombo;
        
    protected TextField<String> entityField;
    
    protected TextField<String> keyField;
    
    protected ComboBox<ItemBaseModel> sourceCombo;
    
    protected ComboBox<ItemBaseModel> operationTypeCombo;
    
    protected DateField startDateField;
    
    protected DateField endDateField;
    
    protected CheckBox strictCheckBox;
        
    protected Button resetButton;
    
    protected Button searchButton;
    
    protected Button exportButton;

    protected LayoutContainer left;

    protected LayoutContainer right;
    
    protected FormData formData = new FormData();

    public static JournalSearchPanel getInstance() {
        if (formPanel == null) {
            formPanel = new JournalSearchPanel();
        }
        return formPanel;
    }
    
    protected JournalSearchPanel() {
        init();
    }

    protected void init() {
        this.setFrame(true);
        this.setHeight(-1);
        this.setPadding(5);
        this.setHeading(MessagesFactory.getMessages().search_panel_title());
        this.setButtonAlign(HorizontalAlignment.RIGHT);

        LayoutContainer main = new LayoutContainer();
        main.setLayout(new ColumnLayout());

        initLeft();
        initDataModelCombo();
        initEntityField();
        initStartDateField();
        initEndDateField();

        initRight();
        initKeyField();
        initOperationTypeCombo();
        initSourceCombo();

        main.add(left, new ColumnData(.5));
        main.add(right, new ColumnData(.5));
        this.add(main, new FormData("100%")); //$NON-NLS-1$

        initStrictCheckBox();
        intiSearchButton();
        initResetButton();
        initExportButton();
    }

    protected void initLeft() {
        left = new LayoutContainer();
        left.setStyleAttribute("paddingRight", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
        left.setStyleAttribute("paddingTop", "1px"); //$NON-NLS-1$ //$NON-NLS-2$
        FormLayout layout = new FormLayout();
        layout.setLabelAlign(LabelAlign.LEFT);
        layout.setLabelWidth(110);
        left.setWidth(350);
        left.setLayout(layout);
    }

    protected void initRight() {
        right = new LayoutContainer();
        right.setStyleAttribute("paddingLeft", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
        right.setStyleAttribute("paddingTop", "1px"); //$NON-NLS-1$ //$NON-NLS-2$
        right.setWidth(350);
        FormLayout layout = new FormLayout();
        layout.setLabelAlign(LabelAlign.LEFT);
        right.setLayout(layout);
    }

    protected void initDataModelCombo() {
        RpcProxy<List<ItemBaseModel>> modelproxy = new RpcProxy<List<ItemBaseModel>>() {

            @Override
            public void load(Object loadConfig, final AsyncCallback<List<ItemBaseModel>> callback) {
                service.getDataModels(new SessionAwareAsyncCallback<List<ItemBaseModel>>() {

                    @Override
                    protected void doOnFailure(Throwable caught) {
                        super.doOnFailure(caught);
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(List<ItemBaseModel> result) {
                        callback.onSuccess(result);
                    }
                });
            }
        };

        ListLoader<ListLoadResult<ItemBaseModel>> modelloader = new BaseListLoader<ListLoadResult<ItemBaseModel>>(modelproxy);

        // HorizontalPanel entityPanel = new HorizontalPanel();
        final ListStore<ItemBaseModel> dataModelList = new ListStore<ItemBaseModel>(modelloader);
        dataModelCombo = new ComboBox<ItemBaseModel>();
        dataModelCombo.setId("dataModel");//$NON-NLS-1$
        dataModelCombo.setName("dataModel");//$NON-NLS-1$
        dataModelCombo.setFieldLabel(MessagesFactory.getMessages().data_model_label());
        dataModelCombo.setDisplayField("label"); //$NON-NLS-1$
        dataModelCombo.setValueField("key"); //$NON-NLS-1$
        dataModelCombo.setStore(dataModelList);
        dataModelCombo.setTriggerAction(TriggerAction.ALL);
        dataModelCombo.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (searchButton != null) {
                        searchButton.fireEvent(Events.Select);
                    }
                }
            }
        });
        left.add(dataModelCombo, formData);
        setCurrentDataModel();
    }

    protected void initEntityField() {
        entityField = new TextField<String>();
        entityField.setId("entity");//$NON-NLS-1$
        entityField.setName("entity");//$NON-NLS-1$
        entityField.setFieldLabel(MessagesFactory.getMessages().entity_label());
        entityField.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (searchButton != null) {
                        searchButton.fireEvent(Events.Select);
                    }
                }
            }
        });
        left.add(entityField, formData);
    }

    protected void initStartDateField() {
        startDateField = new DateField();
        startDateField.setId("startDate");//$NON-NLS-1$
        startDateField.setName("startDate");//$NON-NLS-1$
        startDateField.setFieldLabel(MessagesFactory.getMessages().start_date_label());
        startDateField.setPropertyEditor(new DateTimePropertyEditor("yyyy-MM-dd HH:mm:ss")); //$NON-NLS-1$
        startDateField.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (searchButton != null) {
                        searchButton.fireEvent(Events.Select);
                    }
                }
            }
        });
        left.add(startDateField, formData);
    }

    protected void initEndDateField() {
        endDateField = new DateField();
        endDateField.setId("endDate");//$NON-NLS-1$
        endDateField.setName("endDate");//$NON-NLS-1$
        endDateField.setFieldLabel(MessagesFactory.getMessages().end_date_label());
        endDateField.setPropertyEditor(new DateTimePropertyEditor("yyyy-MM-dd HH:mm:ss")); //$NON-NLS-1$
        endDateField.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (searchButton != null) {
                        searchButton.fireEvent(Events.Select);
                    }
                }
            }
        });
        left.add(endDateField, formData);
    }

    protected void initKeyField() {
        keyField = new TextField<String>();
        keyField.setId("key");//$NON-NLS-1$
        keyField.setName("key");//$NON-NLS-1$
        keyField.setFieldLabel(MessagesFactory.getMessages().key_label());
        keyField.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (searchButton != null) {
                        searchButton.fireEvent(Events.Select);
                    }
                }
            }
        });
        right.add(keyField, formData);
    }

    protected void initOperationTypeCombo() {
        List<String> list = generateOperatorList();

        operationTypeCombo = new ComboBox<ItemBaseModel>();
        operationTypeCombo.setId("operationType");//$NON-NLS-1$
        operationTypeCombo.setName("operationType");//$NON-NLS-1$
        operationTypeCombo.setFieldLabel(MessagesFactory.getMessages().operation_type_label());
        operationTypeCombo.setDisplayField("label"); //$NON-NLS-1$
        operationTypeCombo.setValueField("key"); //$NON-NLS-1$
        operationTypeCombo.setStore(this.getListStore(list));
        operationTypeCombo.setTriggerAction(TriggerAction.ALL);
        operationTypeCombo.setEditable(false);
        operationTypeCombo.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER && !operationTypeCombo.isExpanded()) {
                    if (searchButton != null) {
                        searchButton.fireEvent(Events.Select);
                    }
                } else if (KeyUtil.isCharacter(be.getKeyCode())) {
                    operationTypeCombo.doQuery(KeyUtil.getKeyValueByKeyCode(be.getKeyCode()), true);
                }
            }
        });
        right.add(operationTypeCombo, formData);
    }

    protected void initSourceCombo() {
        List<String> list = new ArrayList<String>();
        list.add(UpdateReportPOJO.GENERIC_UI_SOURCE);
        list.add("adminWorkbench"); //$NON-NLS-1$
        list.add("dataSynchronization"); //$NON-NLS-1$
        list.add("workflow"); //$NON-NLS-1$

        sourceCombo = new ComboBox<ItemBaseModel>();
        sourceCombo.setId("source");//$NON-NLS-1$
        sourceCombo.setName("source");//$NON-NLS-1$
        sourceCombo.setFieldLabel(MessagesFactory.getMessages().source_label());
        sourceCombo.setDisplayField("label"); //$NON-NLS-1$
        sourceCombo.setValueField("key"); //$NON-NLS-1$
        sourceCombo.setStore(this.getListStore(list));
        sourceCombo.setTriggerAction(TriggerAction.ALL);
        sourceCombo.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (searchButton != null) {
                        searchButton.fireEvent(Events.Select);
                    }
                }
            }
        });
        right.add(sourceCombo, formData);
    }

    protected void initStrictCheckBox() {
        strictCheckBox = new CheckBox();
        strictCheckBox.setId("strict"); //$NON-NLS-1$
        strictCheckBox.setName("strict"); //$NON-NLS-1$
        strictCheckBox.setEnabled(true);
        strictCheckBox.setValue(true);
        strictCheckBox.setBoxLabel(MessagesFactory.getMessages().strict_search_checkbox());
        this.getButtonBar().add(strictCheckBox);
    }

    protected void intiSearchButton() {
        searchButton = new Button(MessagesFactory.getMessages().search_button());
        searchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                searchAction();
            }
        });
        this.addButton(searchButton);
    }

    protected void initResetButton() {
        resetButton = new Button(MessagesFactory.getMessages().reset_button());
        resetButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                resetSearchAction();
            }
        });
        this.addButton(resetButton);
    }

    protected void initExportButton() {
        exportButton = new Button(MessagesFactory.getMessages().exprot_excel_button());
        exportButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                exportAction();
            }
        });
        this.addButton(exportButton);
    }
    
    protected void searchAction() {
        if (validSearchCondition()) {
            if (validDateValue()) {
                MessageBox.alert(MessagesFactory.getMessages().warning_title(),
                        MessagesFactory.getMessages().search_date_error_message(), null);
            } else {
                bundleCriteria();
                doSearch();
            }
        }
    }

    protected boolean validSearchCondition() {
        return entityField.isValid() && sourceCombo.isValid() && startDateField.isValid() && keyField.isValid()
                && operationTypeCombo.isValid() && endDateField.isValid();
    }

    protected boolean validDateValue() {
        return startDateField.getValue() != null && endDateField.getValue() != null
                && startDateField.getValue().after(endDateField.getValue());
    }

    protected void doSearch() {
        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.dispatch(JournalEvents.DoSearch);
    }

    protected void resetSearchAction() {
        setCurrentDataModel();
        entityField.clear();
        keyField.clear();
        sourceCombo.clear();
        operationTypeCombo.clear();
        startDateField.clear();
        endDateField.clear();
        strictCheckBox.setValue(true);
    }

    protected ListStore<ItemBaseModel> getListStore(List<String> list) {
        List<ItemBaseModel> modelList = new ArrayList<ItemBaseModel>();
        for (String str : list) {
            ItemBaseModel model = new ItemBaseModel();
            model.set("label", str); //$NON-NLS-1$
            model.set("key", str); //$NON-NLS-1$
            modelList.add(model);
        }
        ListStore<ItemBaseModel> store = new ListStore<ItemBaseModel>();
        store.add(modelList);
        return store;
    }
    
    protected void bundleCriteria() {
        JournalSearchCriteria criteria = Registry.get(Journal.SEARCH_CRITERIA);
        if (dataModelCombo.getValue() != null && !"ALL".equals(dataModelCombo.getValue().get("key").toString())) { //$NON-NLS-1$//$NON-NLS-2$
            criteria.setDataModel(dataModelCombo.getValue().get("key").toString()); //$NON-NLS-1$
        } else {
            criteria.setDataModel(null);
        }
        criteria.setEntity(entityField.getValue());
        criteria.setKey(keyField.getValue());
        if (sourceCombo.getValue() != null) {
            criteria.setSource(sourceCombo.getValue().get("key").toString()); //$NON-NLS-1$
        } else if (sourceCombo.getRawValue() != null && !"".equals(sourceCombo.getRawValue())) { //$NON-NLS-1$
            criteria.setSource(sourceCombo.getRawValue());
        } else {
            criteria.setSource(null);
        }

        if (operationTypeCombo.getValue() != null && !"ALL".equals(operationTypeCombo.getValue().get("key").toString())) { //$NON-NLS-1$//$NON-NLS-2$
            criteria.setOperationType(operationTypeCombo.getValue().get("key").toString()); //$NON-NLS-1$
        } else {
            criteria.setOperationType(null);
        }

        criteria.setStartDate(startDateField.getValue());
        criteria.setEndDate(endDateField.getValue());
        criteria.setStrict(strictCheckBox.getValue());
    }

    protected Map<String, String> getCriteriaMap() {
        Map<String, String> map = new HashMap<String, String>();
        if (dataModelCombo.getValue() != null && !dataModelCombo.getValue().get("key").equals("ALL")) { //$NON-NLS-1$//$NON-NLS-2$
            map.put("dataModel", dataModelCombo.getValue().get("key").toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (entityField.getValue() != null) {
            map.put("entity", entityField.getValue()); //$NON-NLS-1$
        }
        if (keyField.getValue() != null) {
            map.put("key", keyField.getValue()); //$NON-NLS-1$
        }
        if (sourceCombo.getValue() != null) {
            map.put("source", sourceCombo.getValue().get("key").toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (operationTypeCombo.getValue() != null && !operationTypeCombo.getValue().get("key").equals("ALL")) { //$NON-NLS-1$//$NON-NLS-2$
            map.put("operationType", operationTypeCombo.getValue().get("key").toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (startDateField.getValue() != null) {
            map.put("startDate", String.valueOf(startDateField.getValue().getTime())); //$NON-NLS-1$
        }
        if (endDateField.getValue() != null) {
            map.put("endDate", String.valueOf(endDateField.getValue().getTime())); //$NON-NLS-1$
        }
        map.put("isStrict", String.valueOf(strictCheckBox.getValue())); //$NON-NLS-1$
        map.put("language", UrlUtil.getLanguage()); //$NON-NLS-1$

        return map;
    }
    
    public void initPanel() {
        resetButton.fireEvent(Events.Select);
    }
    
    public void setEntityFieldValue(String value) {
        this.entityField.setValue(value);
    }
    
    public void setKeyFieldValue(String value) {
        this.keyField.setValue(value);
    }

    public void setCurrentDataModel() {
        ItemBaseModel currentModel = new ItemBaseModel();
        if (UserContextUtil.getDataModel() != null) {
            currentModel.set("key", UserContextUtil.getDataModel()); //$NON-NLS-1$
            currentModel.set("label", UserContextUtil.getDataModel()); //$NON-NLS-1$      
            this.dataModelCombo.setValue(currentModel);
        }
    }

    protected void exportAction() {
        PostDataUtil.postData(GWT.getHostPageBaseURL() + "/journal/journalExport", getCriteriaMap()); //$NON-NLS-1$
    }

    protected List<String> generateOperatorList() {
        List<String> list = new ArrayList<String>();
        list.add("ALL"); //$NON-NLS-1$
        list.add(UpdateReportPOJO.OPERATION_TYPE_CREATE);
        list.add(UpdateReportPOJO.OPERATION_TYPE_UPDATE);
        list.add(UpdateReportPOJO.OPERATION_TYPE_PHYSICAL_DELETE);
        list.add(UpdateReportPOJO.OPERATION_TYPE_LOGICAL_DELETE);
        list.add(UpdateReportPOJO.OPERATION_TYPE_RESTORED);
        list.add(UpdateReportPOJO.OPERATION_TYPE_ACTION);
        return list;
    }
}