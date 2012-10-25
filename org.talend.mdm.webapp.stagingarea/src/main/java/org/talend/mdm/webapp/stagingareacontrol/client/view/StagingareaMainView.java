// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.stagingareacontrol.client.view;

import org.talend.mdm.webapp.stagingareacontrol.client.controller.ControllerContainer;
import org.talend.mdm.webapp.stagingareacontrol.client.resources.icon.Icons;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
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

public class StagingareaMainView extends AbstractView {
    
    private StagingContainerSummaryView summaryView;

    private CurrentValidationView currentValidationView;

    private PreviousExecutionView previousExecutionValidationView;

    public StagingareaMainView() {
        super();
    }

    private FieldSet wrapFieldSet(BoxComponent comp, String caption){
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
        Button refresh = new Button(messages.refresh(), AbstractImagePrototype.create(Icons.INSTANCE.refresh()));
        refresh.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                ControllerContainer.get().getSummaryController().refreshView();
                if (!ControllerContainer.get().getSummaryController().isEnabledStartValidation()) {
                    ControllerContainer.get().getCurrentValidationController().refreshView();
                }
            }
        });
        toolBar.add(refresh);

        final ToggleButton autoRefreshToggle = new ToggleButton(messages.on());
        autoRefreshToggle.toggle(true);
        autoRefreshToggle.setTitle(messages.auto_refresh());
        autoRefreshToggle.addListener(Events.Toggle, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                autoRefreshToggle.setText(autoRefreshToggle.isPressed() ? messages.on() : messages.off());
                ControllerContainer.get().getCurrentValidationController().autoRefresh(autoRefreshToggle.isPressed());
            }
        });
        toolBar.add(autoRefreshToggle);
        mainPanel.setTopComponent(toolBar);

        summaryView = new StagingContainerSummaryView();
        currentValidationView = new CurrentValidationView();
        previousExecutionValidationView = new PreviousExecutionView();
        ControllerContainer.initController(this, summaryView, currentValidationView, previousExecutionValidationView);
    }

    @Override
    protected void initLayout() {
        RowLayout layout = new RowLayout();
        mainPanel.setLayout(layout);
        mainPanel.add(wrapFieldSet(summaryView, messages.status()), new RowData(1, -1, new Margins(0, 10, 0, 10)));
        mainPanel.add(wrapFieldSet(currentValidationView, messages.current_validation()), new RowData(1, -1, new Margins(0, 10, 0, 10)));
        mainPanel.add(wrapFieldSet(previousExecutionValidationView, messages.previous_validation()), new RowData(1, -1, new Margins(0, 10, 0, 10)));
    }

    public void doLayout() {
        mainPanel.layout(true);
    }
}
