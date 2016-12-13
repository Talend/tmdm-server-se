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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import org.talend.mdm.webapp.stagingarea.control.client.resources.icon.Icons;
import org.talend.mdm.webapp.stagingarea.control.shared.controller.Controllers;

public class StagingAreaMainView extends AbstractView {

    private StagingContainerSummaryView summaryView;

    private CurrentValidationView       currentValidationView;

    private PreviousExecutionView       previousExecutionValidationView;

    private FieldSet wrapFieldSet(BoxComponent comp, String caption) {
        FieldSet fieldSet = new FieldSet();
        fieldSet.setLayout(new FitLayout());
        fieldSet.setHeading(caption);
        fieldSet.add(comp);
        return fieldSet;
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        mainPanel.setBodyBorder(false);
        mainPanel.setScrollMode(Scroll.AUTOY);
        this.setStyleAttribute("font-family", "tahoma,arial,helvetica,sans-serif"); //$NON-NLS-1$//$NON-NLS-2$
        this.setStyleAttribute("font-size", "11px"); //$NON-NLS-1$//$NON-NLS-2$
        ToolBar toolBar = new ToolBar();
        // Manual refresh button.
        Button refresh = new Button(messages.refresh(), AbstractImagePrototype.create(Icons.INSTANCE.refresh()));
        refresh.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Controllers.get().getSummaryController().refresh();
                Controllers.get().getValidationController().refresh();
                Controllers.get().getPreviousExecutionController().refresh();
            }
        });
        toolBar.add(refresh);
        // Auto refresh button.
        final ToggleButton autoRefreshToggle = new ToggleButton(messages.on());
        autoRefreshToggle.toggle();
        autoRefreshToggle.setTitle(messages.auto_refresh());
        autoRefreshToggle.addListener(Events.Toggle, new Listener<BaseEvent>() {

            public void handleEvent(BaseEvent be) {
                autoRefreshToggle.setText(autoRefreshToggle.isPressed() ? messages.on() : messages.off());
                Controllers.get().getStagingController().autoRefresh(autoRefreshToggle.isPressed());
            }
        });
        // Turn on (or off) auto refresh depending on initial toggle status
        Controllers.get().getStagingController().autoRefresh(autoRefreshToggle.isPressed());
        toolBar.add(autoRefreshToggle);
        mainPanel.setTopComponent(toolBar);
        // All sub panels (storage summary, current validation, previous execution list).
        summaryView = new StagingContainerSummaryView();
        currentValidationView = new CurrentValidationView();
        previousExecutionValidationView = new PreviousExecutionView();
        // Do an initial refresh for UI components
        Controllers.get().getSummaryController().refresh();
        Controllers.get().getValidationController().refresh();
        Controllers.get().getPreviousExecutionController().refresh();
    }

    @Override
    protected void initLayout() {
        RowLayout layout = new RowLayout();
        mainPanel.setLayout(layout);
        mainPanel.add(wrapFieldSet(summaryView, messages.status()), new RowData(1, -1, new Margins(0, 10, 0, 10)));
        mainPanel.add(wrapFieldSet(currentValidationView, messages.current_validation()), new RowData(1, -1, new Margins(0, 10,
                0, 10)));
        mainPanel.add(wrapFieldSet(previousExecutionValidationView, messages.previous_validation()), new RowData(1, 1,
                new Margins(0, 10, 0, 10)));
    }
}
