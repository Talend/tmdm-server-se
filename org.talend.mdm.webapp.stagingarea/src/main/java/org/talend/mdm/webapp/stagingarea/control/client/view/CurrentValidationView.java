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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.shared.GwtEvent;
import org.talend.mdm.webapp.stagingarea.control.client.GenerateContainer;
import org.talend.mdm.webapp.stagingarea.control.shared.controller.Controllers;
import org.talend.mdm.webapp.stagingarea.control.shared.event.ModelEvent;
import org.talend.mdm.webapp.stagingarea.control.shared.event.ModelEventHandler;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaValidationModel;

public class CurrentValidationView extends AbstractView implements ModelEventHandler {

    private Button       cancelButton;

    private CardLayout   cardLayout;

    private ContentPanel executionPanel;

    private ContentPanel defaultMessagePanel;

    private ProgressBar  progressBar;

    public CurrentValidationView() {
        GenerateContainer.getValidationModel().addModelEventHandler(this);
    }

    @Override
    protected void initComponents() {
        progressBar = new ProgressBar();
        cancelButton = new Button(messages.cancel());
        cancelButton.setSize(200, 30);

        executionPanel = new ContentPanel();
        executionPanel.setHeaderVisible(false);
        executionPanel.setBodyBorder(false);
        TableLayout contentLayout = new TableLayout(2);
        contentLayout.setCellSpacing(10);
        contentLayout.setWidth("100%"); //$NON-NLS-1$
        executionPanel.setLayout(contentLayout);
        // Message when no validation is being executed
        defaultMessagePanel = new ContentPanel();
        defaultMessagePanel.setLayout(new CenterLayout());
        defaultMessagePanel.setBodyBorder(false);
        defaultMessagePanel.setHeaderVisible(false);
        defaultMessagePanel.add(new Label(messages.no_validation()));
    }

    @Override
    protected void registerEvent() {
        cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                MessageBox.confirm(messages.please_confirm(), messages.confirm_message(), new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                            Controllers.get().getValidationController().cancel();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void initLayout() {
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);
        // Set panels to main panel
        Label validationInProgress = new Label(messages.validation_in_progress());
        TableData validationInProgressData = new TableData();
        validationInProgressData.setColspan(2);
        executionPanel.add(validationInProgress, validationInProgressData);
        // Progress bar
        TableData progressData = new TableData();
        progressData.setHorizontalAlign(Style.HorizontalAlignment.CENTER);
        progressData.setWidth("90%");
        executionPanel.add(progressBar, progressData);
        // Cancel button
        TableData cb = new TableData();
        cb.setHorizontalAlign(Style.HorizontalAlignment.CENTER);
        cb.setWidth("10%");
        executionPanel.add(cancelButton, cb);
        // Panel to contain both no execution message and current execution.
        mainPanel.add(new Label());
        mainPanel.add(defaultMessagePanel);
        mainPanel.add(executionPanel);
        mainPanel.setBodyBorder(false);
    }

    @Override
    public void onModelEvent(ModelEvent e) {
        GwtEvent.Type<ModelEventHandler> type = e.getAssociatedType();
        if (type == ModelEvent.Types.VALIDATION_MODEL_CHANGED.getType()) {
            StagingAreaValidationModel currentValidationModel = e.getModel();
            int total = currentValidationModel.getTotalRecord();
            if (total == 0) {
                return;
            }
        }
        if (type == ModelEvent.Types.VALIDATION_CANCEL.getType() || type == ModelEvent.Types.VALIDATION_END.getType()) {
            mainPanel.setHeight(30);
            progressBar.reset();
            cardLayout.setActiveItem(defaultMessagePanel);
            cancelButton.disable();
        }
        if (type == ModelEvent.Types.VALIDATION_START.getType()) {
            mainPanel.setHeight(90);
            cardLayout.setActiveItem(executionPanel);
            progressBar.enable();
            progressBar.auto();
            cancelButton.enable();
        }
        GenerateContainer.getContentPanel().layout(true);
    }

    // Called by JS
    public ContentPanel getContentPanel() {
        return executionPanel;
    }

    // Called by JS
    public ContentPanel getActivePanel() {
        return (ContentPanel) cardLayout.getActiveItem();
    }
}
