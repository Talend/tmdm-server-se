/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingareabrowser.client.view;

import org.talend.mdm.webapp.stagingareabrowser.client.controller.ControllerContainer;
import org.talend.mdm.webapp.stagingareabrowser.client.controller.SearchController;
import org.talend.mdm.webapp.stagingareabrowser.client.model.SearchModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.Label;

public class SearchView extends AbstractView {

    public static final int ALL_RECORDS = 1;

    public static final int INVALID_RECORDS = 2;

    public static final int VALID_RECORDS = 3;

    public static final int COMPONENT_SPACING = 10;

    private ContentPanel leftPanel;

    private ContentPanel centerPanel;

    private ContentPanel rightPanel;

    private ComboBox<BaseModel> entityCombo;

    private TextField<String> sourceField;

    private TextField<String> keyField;

    private DateField startDate;

    private DateField endDate;

    private ComboBox<BaseModel> stateCombo;

    private NumberField statusCodeField;

    private FieldSet fieldSet;

    private FieldSet dateTimeFieldSet;

    private Button searchButton;

    private Button resetButton;

    private ContentPanel buttonPanel;

    private HorizontalPanel hp;

    @Override
    protected void initComponents() {
        entityCombo = new ComboBox<BaseModel>();
        entityCombo.setStore(SearchController.getStore());
        entityCombo.setFieldLabel(messages.entity());
        entityCombo.setTriggerAction(TriggerAction.ALL);
        entityCombo.setEditable(false);
        entityCombo.setDisplayField("name");//$NON-NLS-1$
        entityCombo.setValueField("value");//$NON-NLS-1$
        entityCombo.setForceSelection(true);
        entityCombo.setWidth(250);

        sourceField = new TextField<String>();
        sourceField.setFieldLabel(messages.source());
        sourceField.setWidth(250);
        keyField = new TextField<String>();
        keyField.setFieldLabel(messages.key());
        keyField.setWidth(250);
        startDate = new DateField();
        startDate.setFieldLabel(messages.start_date());
        startDate.setWidth(250);
        endDate = new DateField();
        endDate.setFieldLabel(messages.end_date());
        endDate.setWidth(250);

        stateCombo = new ComboBox<BaseModel>();
        stateCombo.setFieldLabel(messages.state());
        stateCombo.setEditable(false);
        stateCombo.setTriggerAction(TriggerAction.ALL);
        stateCombo.setWidth(250);
        ListStore<BaseModel> stateStore = new ListStore<BaseModel>();
        BaseModel state1 = new BaseModel();
        state1.set("name", messages.all_records()); //$NON-NLS-1$
        state1.set("value", ALL_RECORDS); //$NON-NLS-1$

        BaseModel state2 = new BaseModel();
        state2.set("name", messages.invalid_records()); //$NON-NLS-1$
        state2.set("value", INVALID_RECORDS); //$NON-NLS-1$

        BaseModel state3 = new BaseModel();
        state3.set("name", messages.valid_records()); //$NON-NLS-1$
        state3.set("value", VALID_RECORDS); //$NON-NLS-1$

        stateStore.add(state1);
        stateStore.add(state2);
        stateStore.add(state3);
        stateCombo.setStore(stateStore);
        stateCombo.setDisplayField("name"); //$NON-NLS-1$
        stateCombo.setValueField("value"); //$NON-NLS-1$
        stateCombo.setWidth(250);
        statusCodeField = new NumberField();
        statusCodeField.setPropertyEditorType(Integer.class);
        statusCodeField.setValue(0);
        statusCodeField.setFieldLabel(messages.status_code());
        statusCodeField.setWidth(250);

        leftPanel = new ContentPanel();
        leftPanel.setHeaderVisible(false);
        leftPanel.setBodyBorder(false);

        centerPanel = new ContentPanel();
        centerPanel.setHeaderVisible(false);
        centerPanel.setBodyBorder(false);

        rightPanel = new ContentPanel();
        rightPanel.setHeaderVisible(false);
        rightPanel.setBodyBorder(false);

        fieldSet = new FieldSet();
        fieldSet.setHeading(messages.status());
        dateTimeFieldSet = new FieldSet();
        dateTimeFieldSet.setHeading(messages.date_time_title());

        buttonPanel = new ContentPanel();
        buttonPanel.setHeaderVisible(false);
        buttonPanel.setBodyBorder(false);
        buttonPanel.setHeight(40);
        buttonPanel.setFrame(true);

        searchButton = new Button(messages.search());
        searchButton.setWidth("90px"); //$NON-NLS-1$
        resetButton = new Button(messages.reset());
        resetButton.setWidth("90px"); //$NON-NLS-1$

        hp = new HorizontalPanel();
        hp.setLayout(new FitLayout());
        hp.setSpacing(COMPONENT_SPACING);
        hp.setScrollMode(Scroll.AUTO);
        hp.setAutoWidth(true);
    }

    @Override
    protected void initLayout() {
        mainPanel.setLayout(new RowLayout());
        FormLayout leftLayout = new FormLayout();
        leftLayout.setLabelWidth(80);
        leftPanel.setLayout(leftLayout);

        centerPanel.setLayout(new FormLayout());
        FormLayout centerLayout = new FormLayout();
        centerLayout.setLabelWidth(80);
        fieldSet.setLayout(centerLayout);

        FormData formDataTop = new FormData();
        formDataTop.setMargins(new Margins(10, 0, 0, 0));
        leftPanel.add(entityCombo, formDataTop);
        FormData formData = new FormData();
        formData.setMargins(new Margins(5, 0, 0, 0));
        leftPanel.add(sourceField, formData);
        leftPanel.add(keyField, formData);

        rightPanel.setLayout(new FormLayout());
        FormLayout rightLayout = new FormLayout();
        rightLayout.setLabelWidth(80);
        dateTimeFieldSet.setLayout(rightLayout);

        fieldSet.add(stateCombo);
        fieldSet.add(statusCodeField);
        centerPanel.add(fieldSet);

        dateTimeFieldSet.add(startDate);
        dateTimeFieldSet.add(endDate);
        rightPanel.add(dateTimeFieldSet);

        TableLayout buttonLayout = new TableLayout(3);
        buttonLayout.setWidth("100%"); //$NON-NLS-1$
        buttonPanel.setLayout(buttonLayout);

        TableData td0 = new TableData();
        buttonPanel.add(new Label(), td0);
        TableData td1 = new TableData();
        td1.setWidth("100px"); //$NON-NLS-1$
        td1.setHorizontalAlign(HorizontalAlignment.RIGHT);
        buttonPanel.add(searchButton, td1);
        TableData td2 = new TableData();
        td2.setWidth("120px"); //$NON-NLS-1$
        td2.setHorizontalAlign(HorizontalAlignment.CENTER);
        buttonPanel.add(resetButton, td2);

        hp.add(leftPanel);
        hp.add(centerPanel);
        hp.add(rightPanel);

        mainPanel.add(hp, new RowData(1, -1));
        mainPanel.add(buttonPanel, new RowData(1, -1));
        mainPanel.setBodyBorder(false);
    }

    @Override
    protected void registerEvent() {
        searchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                SearchModel searchModel = new SearchModel();
                if (entityCombo.getValue() != null) {
                    searchModel.setEntity((String) entityCombo.getValue().get("value")); //$NON-NLS-1$
                }
                searchModel.setSource(sourceField.getValue());
                searchModel.setKey(keyField.getValue());
                if (stateCombo.getValue() != null) {
                    searchModel.setState((Integer) stateCombo.getValue().get("value")); //$NON-NLS-1$
                }
                searchModel.setStatusCode((Integer) statusCodeField.getValue());
                searchModel.setStartDate(startDate.getValue());
                searchModel.setEndDate(endDate.getValue());
                ControllerContainer.get().getResultsController().searchResult(searchModel);
            }
        });

        resetButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                entityCombo.setValue(null);
                sourceField.setValue(null);
                keyField.setValue(null);
                stateCombo.setValue(null);
                statusCodeField.setValue(null);
                startDate.setValue(null);
                endDate.setValue(null);
                defaultDoSearch(ALL_RECORDS);
            }
        });
    }

    public void defaultDoSearch(int defaultState) {
        BaseModel stateModel = stateCombo.getStore().findModel("value", defaultState); //$NON-NLS-1$
        if (stateModel != null) {
            stateCombo.setValue(stateModel);
        }
        if (entityCombo.getValue() == null) {
            if (entityCombo.getStore().getCount() > 0) {
                BaseModel entityModel = entityCombo.getStore().getAt(0);
                entityCombo.setValue(entityModel);
            }
        }
        searchButton.fireEvent(Events.Select);
    }
}
