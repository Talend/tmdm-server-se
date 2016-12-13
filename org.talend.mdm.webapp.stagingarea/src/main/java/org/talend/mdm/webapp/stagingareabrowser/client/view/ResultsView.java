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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.UserContextModel;
import org.talend.mdm.webapp.base.client.util.UserContextUtil;
import org.talend.mdm.webapp.base.client.widget.ColumnAlignGrid;
import org.talend.mdm.webapp.base.client.widget.PagingToolBarEx;
import org.talend.mdm.webapp.stagingareabrowser.client.controller.ResultsController;
import org.talend.mdm.webapp.stagingareabrowser.client.model.RecordStatus;
import org.talend.mdm.webapp.stagingareabrowser.client.model.RecordStatusWrapper;
import org.talend.mdm.webapp.stagingareabrowser.client.model.ResultItem;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Image;

public class ResultsView extends AbstractView {

    private Grid<ResultItem> resultGrid;

    private PagingToolBarEx pagingBar;

    public static final int PAGE_SIZE = 20;

    private ColumnModel columnModel;

    UserContextModel ucx;

    private Map<Integer, String> errorTitles;

    private void initErrorTitles() {
        errorTitles = new HashMap<Integer, String>();
        errorTitles.put(RecordStatus.NEW.getStatusCode(), messages.status_000());
        errorTitles.put(RecordStatus.SUCCESS_IDENTIFIED_CLUSTERS.getStatusCode(), messages.status_201(ucx.getDataContainer()));
        errorTitles.put(RecordStatus.SUCCESS_MERGE_CLUSTERS.getStatusCode(), messages.status_202(ucx.getDataContainer()));
        errorTitles.put(RecordStatus.SUCCESS_MERGE_CLUSTER_TO_RESOLVE.getStatusCode(),
                messages.status_203(ucx.getDataContainer()));
        errorTitles.put(RecordStatus.SUCCESS_MERGED_RECORD.getStatusCode(), messages.status_204(ucx.getDataContainer()));
        errorTitles.put(RecordStatus.SUCCESS_VALIDATE.getStatusCode(), messages.status_205());
        errorTitles.put(RecordStatus.SUCCESS_DELETED.getStatusCode(), messages.status_206());
        errorTitles.put(RecordStatus.FAIL_IDENTIFIED_CLUSTERS.getStatusCode(), messages.status_401(ucx.getDataContainer()));
        errorTitles.put(RecordStatus.FAIL_MERGE_CLUSTERS.getStatusCode(), messages.status_402(ucx.getDataContainer()));
        errorTitles.put(RecordStatus.FAIL_VALIDATE_VALIDATION.getStatusCode(), messages.status_403());
        errorTitles.put(RecordStatus.FAIL_VALIDATE_CONSTRAINTS.getStatusCode(), messages.status_404());
    }

    @Override
    protected void initComponents() {
        ucx = UserContextUtil.getUserContext();
        initErrorTitles();
        initColumnModel();
        resultGrid = new ColumnAlignGrid<ResultItem>(ResultsController.getClearStore(), columnModel);
        pagingBar = new PagingToolBarEx(PAGE_SIZE);
        pagingBar.bind(ResultsController.getLoader());
    }

    @Override
    protected void initLayout() {
        mainPanel.setLayout(new FitLayout());
        mainPanel.add(resultGrid);
        mainPanel.setBottomComponent(pagingBar);
        mainPanel.setBodyBorder(false);
    }

    private void initColumnModel() {
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        ColumnConfig entityColumn = new ColumnConfig("entity", messages.entity(), 100); //$NON-NLS-1$
        entityColumn.setSortable(false);
        columns.add(entityColumn);
        columns.add(new ColumnConfig("key", messages.key(), 100)); //$NON-NLS-1$
        ColumnConfig dateTimeColumn = new ColumnConfig("dateTime", messages.date_time(), 200); //$NON-NLS-1$
        if (ucx.getDateTimeFormat() != null && !"".equals(ucx.getDateTimeFormat())) { //$NON-NLS-1$
            dateTimeColumn.setDateTimeFormat(DateTimeFormat.getFormat(ucx.getDateTimeFormat()));
        }
        columns.add(dateTimeColumn);
        columns.add(new ColumnConfig("source", messages.source(), 100)); //$NON-NLS-1$
        columns.add(new ColumnConfig("group", messages.group(), 100)); //$NON-NLS-1$
        ColumnConfig statusColumn = new ColumnConfig("status", messages.status(), 100); //$NON-NLS-1$
        statusColumn.setRenderer(new GridCellRenderer<ResultItem>() {

            @Override
            public Object render(ResultItem model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<ResultItem> store, Grid<ResultItem> grid) {

                com.google.gwt.user.client.ui.Grid g = new com.google.gwt.user.client.ui.Grid(1, 2);
                if (model.getStatus() == null) {
                    return null;
                }

                RecordStatusWrapper wrapper = new RecordStatusWrapper(RecordStatus.newStatus(model.getStatus().intValue()));

                String color = wrapper.getColor();

                if (color != null) {
                    g.getElement().getStyle().setColor(color);
                }
                if (wrapper.getIcon() != null) {
                    g.setWidget(0, 0, new Image(wrapper.getIcon()));
                }
                g.setText(0, 1, Integer.toString(model.getStatus()));
                g.setTitle(model.getStatus()
                        + ": " + (errorTitles.get(model.getStatus()) == null ? "" : errorTitles.get(model.getStatus()))); //$NON-NLS-1$ //$NON-NLS-2$
                return g;
            }
        });
        columns.add(statusColumn);
        columns.add(new ColumnConfig("error", messages.error(), 200)); //$NON-NLS-1$
        columnModel = new ColumnModel(columns);
    }

    @Override
    protected void registerEvent() {
        resultGrid.addListener(Events.RowDoubleClick, new Listener<GridEvent<ResultItem>>() {

            @Override
            public void handleEvent(GridEvent<ResultItem> ge) {
                ResultItem item = ge.getGrid().getStore().getAt(ge.getRowIndex());
                StringBuffer title = new StringBuffer();
                title.append(item.getEntity() + " "); //$NON-NLS-1$
                title.append(item.getKey() + " "); //$NON-NLS-1$

                if (ucx.getDateTimeFormat() != null && !"".equals(ucx.getDateTimeFormat())) { //$NON-NLS-1$
                    title.append(DateTimeFormat.getFormat(ucx.getDateTimeFormat()).format(item.getDateTime()));
                }
                StringBuffer message = new StringBuffer();
                String color = "green"; //$NON-NLS-1$
                if (item.getStatus() >= 400) {
                    color = "red"; //$NON-NLS-1$
                }
                message.append("<div style='width: 400px; margin-left: 50px;'>"); //$NON-NLS-1$
                message.append("<b style='color: " + color + ";'>" + item.getStatus() + ": " + errorTitles.get(item.getStatus()) + "</b>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

                if (item.getStatus() >= 400) {
                    Element el = DOM.createDiv();
                    el.setInnerText(item.getError());
                    message.append("<div style='margin-top: 5px; width: 380px; height: 100px; overflow: auto;'>"); //$NON-NLS-1$
                    message.append(el.getInnerHTML());
                    message.append("</div>"); //$NON-NLS-1$
                }

                message.append("</div>"); //$NON-NLS-1$
                if (item.getStatus() >= 400) {
                    MessageBox.alert(title.toString(), message.toString(), null).getDialog().setWidth(500);
                } else {
                    MessageBox.info(title.toString(), message.toString(), null).getDialog().setWidth(500);
                }
            }
        });
    }
}
