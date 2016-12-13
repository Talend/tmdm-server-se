/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecordsinstaging.client.widget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.UserContextModel;
import org.talend.mdm.webapp.base.client.util.UserContextUtil;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.RecordStatus;
import org.talend.mdm.webapp.browserecords.client.model.RecordStatusWrapper;
import org.talend.mdm.webapp.browserecords.client.util.StagingConstant;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsListPanel;
import org.talend.mdm.webapp.browserecordsinstaging.client.i18n.BrowseRecordsInStagingMessages;
import org.talend.mdm.webapp.browserecordsinstaging.client.i18n.MessagesFactory;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Image;

public class ItemsListPanel4Staging extends ItemsListPanel {

    private Map<Integer, String> errorTitles;

    private ItemBean selectedItem;

    private ToolTip tip = new ToolTip();

    public ItemsListPanel4Staging() {
        initErrorTitles();
    }

    private void initErrorTitles() {
        UserContextModel ucx = UserContextUtil.getUserContext();
        BrowseRecordsInStagingMessages messages = MessagesFactory.getMessages();
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
        errorTitles.put(RecordStatus.FAIL_DELETE_CONSTRAINTS.getStatusCode(), messages.status_405());
    }

    @Override
    public void updateGrid(CheckBoxSelectionModel<ItemBean> sm, List<ColumnConfig> columnConfigList) {
        final EntityModel em = (EntityModel) BrowseRecords.getSession().get(UserSession.CURRENT_ENTITY_MODEL);
        ColumnConfig groupColumn = new ColumnConfig(em.getConceptName() + StagingConstant.STAGING_TASKID, MessagesFactory
                .getMessages().match_group(), 200);
        columnConfigList.add(groupColumn);

        ColumnConfig statusColumn = new ColumnConfig(em.getConceptName() + StagingConstant.STAGING_STATUS, "Status", 200); //$NON-NLS-1$
        statusColumn.setRenderer(new GridCellRenderer<ItemBean>() {

            @Override
            public Object render(ItemBean model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<ItemBean> store, Grid<ItemBean> aGrid) {
                com.google.gwt.user.client.ui.Grid g = new com.google.gwt.user.client.ui.Grid(1, 2);
                g.setCellPadding(0);
                g.setCellSpacing(0);
                String status = model.get(em.getConceptName() + StagingConstant.STAGING_STATUS);

                if (status == null || status.trim().length() == 0) {
                    status = "0"; //$NON-NLS-1$
                }
                RecordStatusWrapper wrapper = new RecordStatusWrapper(RecordStatus.newStatus(Integer.valueOf(status)));

                String color = wrapper.getColor();

                if (color != null) {
                    g.getElement().getStyle().setColor(color);
                }
                if (wrapper.getIcon() != null) {
                    g.setWidget(0, 0, new Image(wrapper.getIcon()));
                }
                g.setText(0, 1, status);
                g.setTitle(status
                        + ": " + (errorTitles.get(Integer.valueOf(status)) == null ? "" : errorTitles.get(Integer.valueOf(status)))); //$NON-NLS-1$ //$NON-NLS-2$
                return g;
            }
        });
        columnConfigList.add(statusColumn);
        ColumnConfig sourceColumn = new ColumnConfig(em.getConceptName() + StagingConstant.STAGING_SOURCE, MessagesFactory
                .getMessages().source(), 200);
        columnConfigList.add(sourceColumn);

        for (ColumnConfig cc : columnConfigList) {
            final GridCellRenderer<ModelData> render = cc.getRenderer();

            GridCellRenderer<ModelData> renderProxy = new GridCellRenderer<ModelData>() {

                @Override
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                        ListStore<ModelData> store, Grid<ModelData> g) {
                    Object value = null;
                    if (render != null) {
                        value = render.render(model, property, config, rowIndex, colIndex, store, g);
                    } else {
                        value = model.get(property);
                    }
                    if (value instanceof String) {
                        ItemBean item = (ItemBean) model;
                        String matchGroup = model.get(item.getConcept() + StagingConstant.STAGING_TASKID);
                        if (matchGroup != null && matchGroup.length() != 0) {
                            String status = model.get(item.getConcept() + StagingConstant.STAGING_STATUS);
                            if ("205".equals(status) || "203".equals(status) || "204".equals(status)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                return "<b>" + value + "</b>"; //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                    return value;
                }
            };
            cc.setRenderer(renderProxy);
        }

        super.updateGrid(sm, columnConfigList);
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<ItemBean>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<ItemBean> se) {
                if (selectedItem != null) {
                    markGroup(false);
                }
                selectedItem = se.getSelectedItem();
                if (selectedItem != null) {
                    markGroup(true);
                }
            }
        });
        grid.addListener(Events.OnMouseOut, new Listener<GridEvent<ItemBean>>() {

            @Override
            public void handleEvent(GridEvent<ItemBean> ge) {
                tip.hide();
            }
        });
        grid.addListener(Events.OnMouseOver, new Listener<GridEvent<ItemBean>>() {

            @Override
            public void handleEvent(GridEvent<ItemBean> ge) {
                int rowIndex = ge.getRowIndex();
                if (rowIndex != -1) {
                    ItemBean item = grid.getStore().getAt(rowIndex);
                    String error = (String) item.get(item.getConcept() + StagingConstant.STAGING_ERROR);
                    if (error != null && error.trim().length() != 0) {
                        tip.update(new ToolTipConfig("<b>" + MessagesFactory.getMessages().error() + "</b>:" + error)); //$NON-NLS-1$ //$NON-NLS-2$ 
                        tip.showAt(DOM.eventGetCurrentEvent().getClientX() + 6, DOM.eventGetCurrentEvent().getClientY() + 6);
                    }
                }
            }
        });
    }

    private void markGroup(boolean mark) {
        String matchGroup = selectedItem.get(selectedItem.getConcept() + StagingConstant.STAGING_TASKID);
        if (matchGroup == null || matchGroup.length() == 0) {
            return;
        }
        ListStore<ItemBean> store = grid.getStore();
        for (int i = 0; i < store.getCount(); i++) {
            ItemBean item = store.getAt(i);
            if (matchGroup.equals(item.get(item.getConcept() + StagingConstant.STAGING_TASKID))) {
                Element rowEl = grid.getView().getRow(item);
                if (mark) {
                    rowEl.addClassName("staging-matchgroup-highlight"); //$NON-NLS-1$
                } else {
                    rowEl.removeClassName("staging-matchgroup-highlight"); //$NON-NLS-1$
                }
            }
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        ItemsListPanel.initialize(new ItemsListPanel4Staging());
    }

    @Override
    protected Boolean isStaging() {
        return true;
    }
}
