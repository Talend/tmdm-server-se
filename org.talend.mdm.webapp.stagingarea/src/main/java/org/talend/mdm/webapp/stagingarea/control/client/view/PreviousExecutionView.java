/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.client.view;

import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import org.talend.mdm.webapp.base.client.model.UserContextModel;
import org.talend.mdm.webapp.base.client.util.UserContextUtil;
import org.talend.mdm.webapp.base.client.widget.PagingToolBarEx;
import org.talend.mdm.webapp.stagingarea.control.client.GenerateContainer;
import org.talend.mdm.webapp.stagingarea.control.shared.controller.Controllers;
import org.talend.mdm.webapp.stagingarea.control.shared.controller.PreviousExecutionController;
import org.talend.mdm.webapp.stagingarea.control.shared.event.ModelEvent;
import org.talend.mdm.webapp.stagingarea.control.shared.event.ModelEventHandler;
import org.talend.mdm.webapp.stagingarea.control.shared.model.PreviousExecutionModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaExecutionModel;

import java.util.ArrayList;
import java.util.List;

class PreviousExecutionView extends AbstractView implements ModelEventHandler {

    private static final int                    PAGE_SIZE = 10;

    private static final PreviousExecutionModel model;

    private PagingToolBarEx                     taskPagingBar;

    private ToolBar                             bar;

    private Label                               beforeDateLabel;

    private DateField                           beforeDateField;

    private Button                              searchButton;

    private Grid<StagingAreaExecutionModel>     taskGrid;

    static {
        model = GenerateContainer.getPreviousExecutionModel();
    }

    private boolean doRefreshOnValidationEnd;

    public PreviousExecutionView() {
        GenerateContainer.getValidationModel().addModelEventHandler(this);
        model.addModelEventHandler(this);
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        // Build column for the execution details grid
        UserContextModel ucx = UserContextUtil.getUserContext();
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        ColumnConfig startDateColumn = new ColumnConfig("start_date", messages.start_date(), 100); //$NON-NLS-1$
        if (ucx.getDateTimeFormat() != null) {
            startDateColumn.setDateTimeFormat(DateTimeFormat.getFormat(ucx.getDateTimeFormat()));
        }
        columns.add(startDateColumn);
        ColumnConfig endDateColumn = new ColumnConfig("end_date", messages.end_date(), 100); //$NON-NLS-1$
        if (ucx.getDateTimeFormat() != null) {
            endDateColumn.setDateTimeFormat(DateTimeFormat.getFormat(ucx.getDateTimeFormat()));
        }
        columns.add(endDateColumn);
        ColumnConfig processRecordsColumn = new ColumnConfig("processed_records", messages.process_records(), 100); //$NON-NLS-1$
        columns.add(processRecordsColumn);
        ColumnConfig invalidRecordsColumn = new ColumnConfig("invalid_records", messages.invalid_records(), 100); //$NON-NLS-1$
        columns.add(invalidRecordsColumn);
        ColumnConfig recordLeftColumn = new ColumnConfig("total_record", messages.total_record(), 100); //$NON-NLS-1$
        columns.add(recordLeftColumn);
        ColumnModel columnModel = new ColumnModel(columns);
        // UI widgets
        taskGrid = new Grid<StagingAreaExecutionModel>(model.getStore(), columnModel);
        taskGrid.getView().setForceFit(true);
        taskGrid.setAutoExpandColumn(columns.get(0).getHeader());
        taskGrid.setAutoHeight(true);
        beforeDateLabel = new Label(messages.display_before());
        beforeDateField = new DateField();
        searchButton = new Button(messages.search());
        bar = new ToolBar();
        taskPagingBar = new PagingToolBarEx(PAGE_SIZE);
        taskPagingBar.bind((PagingLoader<?>) model.getStore().getLoader());
    }

    @Override
    protected void initLayout() {
        bar.add(beforeDateLabel);
        bar.add(beforeDateField);
        bar.add(searchButton);
        mainPanel.setLayout(new FitLayout());
        mainPanel.setHeaderVisible(false);
        mainPanel.setTopComponent(bar);
        mainPanel.add(taskGrid);
        mainPanel.setBottomComponent(taskPagingBar);
    }

    @Override
    protected void registerEvent() {
        searchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                PreviousExecutionController controller = Controllers.get().getPreviousExecutionController();
                controller.setBeforeDate(beforeDateField.getValue());
            }
        });
    }

    @Override
    public void onModelEvent(ModelEvent e) {
        GwtEvent.Type<ModelEventHandler> type = e.getAssociatedType();
        if (type == ModelEvent.Types.PREVIOUS_EXECUTION_CHANGED.getType()) {
            taskPagingBar.enable();
        } else if (type == ModelEvent.Types.VALIDATION_START.getType()) {
            doRefreshOnValidationEnd = true;
        } else if (type == ModelEvent.Types.VALIDATION_END.getType() || type == ModelEvent.Types.VALIDATION_CANCEL.getType()) {
            if (doRefreshOnValidationEnd) {
                taskPagingBar.refresh();
                taskPagingBar.enable();
                doRefreshOnValidationEnd = false;
            }
        }
    }
}
