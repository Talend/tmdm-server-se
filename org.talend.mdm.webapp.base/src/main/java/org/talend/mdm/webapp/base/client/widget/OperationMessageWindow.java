/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.base.client.model.ItemResult;
import org.talend.mdm.webapp.base.client.resources.icon.Icons;
import org.talend.mdm.webapp.base.client.widget.PagingToolBarEx;
import com.extjs.gxt.ui.client.Style.HideMode;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Image;


public class OperationMessageWindow extends Window {

    private static int WINDOW_WIDTH = 450;

    private static int WINDOW_HEIGH = 300;
    
    private List<ItemResult> messages;
    
    private Grid<ItemResult> messageGrid;
    
    private PagingLoader<PagingLoadResult<ModelData>> loader;
    
    private int PAGE_SIZE = 10;
    
    private PagingToolBarEx pagingBar;
    
    PagingModelMemoryProxy proxy;
    
    ListStore<ItemResult> store;
    
    public OperationMessageWindow(List<ItemResult> msgs) {
        messages = msgs;
        initWindow();
    }
    
    public void initWindow() {
        setWidth(WINDOW_WIDTH);
        setHeight(WINDOW_HEIGH);        
    }
    
    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
                
        FormPanel panel = new FormPanel();
        panel.setFrame(false);
        panel.setLayout(new FitLayout());
        panel.setHeaderVisible(false);
        panel.setSize(WINDOW_WIDTH - 14, WINDOW_HEIGH - 36);
        panel.setHeaderVisible(false);
        
        proxy = new PagingModelMemoryProxy(messages);
        loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
        store = new ListStore<ItemResult>(loader);
        
        List<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();              
        ColumnConfig colStatus = new ColumnConfig();
        colStatus.setId("status"); //$NON-NLS-1$
        colStatus.setWidth(65);
        colStatus.setHeader("status"); //$NON-NLS-1$
        colStatus.setRenderer(new GridCellRenderer<ItemResult>() {

            @Override
            public Object render(ItemResult model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<ItemResult> store, Grid<ItemResult> grid) {                
                Image image = new Image();                
                if(model.getStatus() < 2){
                    image.setResource(Icons.INSTANCE.statusValid());
                } else {
                    image.setResource(Icons.INSTANCE.statusInvalid());
                }
                com.google.gwt.user.client.ui.Grid g = new com.google.gwt.user.client.ui.Grid(1, 1);
                g.setCellPadding(0);
                g.setCellSpacing(0);
                g.setWidget(0, 0, image);
                return g;
            }
        });
        columnConfigs.add(colStatus);        
        columnConfigs.add(new ColumnConfig("key", "key", 100)); //$NON-NLS-1$ //$NON-NLS-2$ 
        columnConfigs.add(new ColumnConfig("message", "message", 380)); //$NON-NLS-1$ //$NON-NLS-2$        
        
        ColumnModel cm = new ColumnModel(columnConfigs);
        messageGrid = new Grid<ItemResult>(store, cm);
        messageGrid.getView().setForceFit(true);
        messageGrid.setLoadMask(true);
        messageGrid.setBorders(false);
        messageGrid.setStateful(true);
        messageGrid.setStateId("relatedRecordGrid"); //$NON-NLS-1$
        
        if (cm.getColumnCount() > 0) {
            messageGrid.setAutoExpandColumn(cm.getColumn(0).getHeader());
        }
        
        messageGrid.addListener(Events.Attach, new Listener<GridEvent<ItemResult>>() {

            @Override
            public void handleEvent(GridEvent<ItemResult> be) {
                PagingLoadConfig config = new BasePagingLoadConfig();
                config.setOffset(0);
                int pageSize = pagingBar.getPageSize();
                config.setLimit(pageSize);
                loader.load(config);
            }
        });        
        panel.add(messageGrid);
        
        pagingBar = new PagingToolBarEx(PAGE_SIZE);
        pagingBar.setHideMode(HideMode.VISIBILITY);
        
        pagingBar.bind(loader);
        loader.setRemoteSort(true);
        
        panel.setBottomComponent(pagingBar);
        this.setLayout(new FitLayout());        
        this.add(panel);
    }
    
}
