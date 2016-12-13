/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.client.layout;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.google.gwt.junit.client.GWTTestCase;

@SuppressWarnings("nls")
public class WorkSpaceGWTTest extends GWTTestCase {

    public void testAddWorkTab() {
        WorkSpace workSpace = WorkSpace.getInstance();
        TabPanel tabPanel = (TabPanel) workSpace.getItem(0);

        // 1. add tabItemOne to workSpace
        TabItem tabItemOne = new TabItem("One");
        tabItemOne.setItemId("One");
        workSpace.addWorkTab(tabItemOne.getItemId(), tabItemOne.getElement());
        assertEquals(1, tabPanel.getItemCount());
        assertEquals(tabItemOne.getElement(), workSpace.getItem(tabItemOne.getItemId()));

        // 2. add tabItemTwo to workSpace
        TabItem tabItemTwo = new TabItem("Two");
        tabItemTwo.setItemId("Two");
        workSpace.addWorkTab(tabItemTwo.getItemId(), tabItemTwo.getElement());
        assertEquals(2, tabPanel.getItemCount());
        assertEquals(tabItemTwo.getElement(), workSpace.getItem(tabItemTwo.getItemId()));

        // 3. add a new tabItemOne to workSpace
        TabItem tabItemOneNew = new TabItem("One");
        tabItemOneNew.setItemId("One");
        workSpace.addWorkTab(tabItemOneNew.getItemId(), tabItemOneNew.getElement());
        assertEquals(2, tabPanel.getItemCount());
        assertEquals(tabItemOneNew.getElement(), workSpace.getItem(tabItemOneNew.getItemId()));
        assertNotSame(tabItemOne.getElement(), workSpace.getItem(tabItemOneNew.getItemId()));
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.general.TestGeneral";
    }

}