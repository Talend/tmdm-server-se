/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.recyclebin.client;

import org.talend.mdm.webapp.recyclebin.shared.ItemsTrashItem;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.RootPanel;

public class MainFramePanelGWTTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.recyclebin.RecycleBinTest";
    }

    public void testDeleteSelectedItem() {

        Registry.register(RecycleBin.RECYCLEBIN_SERVICE, new RecycleBinServiceAsyncMock());

        MainFramePanel mainPanel = MainFramePanel.getInstance();

        RootPanel.get().add(mainPanel);

        Grid<ItemsTrashItem> grid = getGrid(mainPanel);

        int count = grid.getStore().getCount();
        assertEquals(10, count);

        grid.getSelectionModel().select(0, 2, true);
        mainPanel.deleteSelected(grid.getSelectionModel().getSelectedItems());
        count = grid.getStore().getCount();
        assertEquals(7, count);

        grid.getSelectionModel().select(0, 3, true);
        mainPanel.deleteSelected(grid.getSelectionModel().getSelectedItems());
        count = grid.getStore().getCount();
        assertEquals(3, count);

        grid.getSelectionModel().select(0, 1, true);
        mainPanel.deleteSelected(grid.getSelectionModel().getSelectedItems());
        count = grid.getStore().getCount();
        assertEquals(1, count);
    }

    private native Grid<ItemsTrashItem> getGrid(MainFramePanel mainPanel)/*-{
		return mainPanel.@org.talend.mdm.webapp.recyclebin.client.MainFramePanel::grid;
    }-*/;
}
