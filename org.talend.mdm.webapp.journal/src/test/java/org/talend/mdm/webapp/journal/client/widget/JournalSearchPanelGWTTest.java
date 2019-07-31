/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client.widget;

import java.util.List;

import org.talend.mdm.webapp.base.client.model.ItemBaseModel;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.junit.client.GWTTestCase;

@SuppressWarnings("nls")
public class JournalSearchPanelGWTTest extends GWTTestCase {

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
    }

    public void testJournalSearchPanel() {
        JournalSearchPanel searchPanel = JournalSearchPanel.getInstance();

        List<Field<?>> fields = searchPanel.getFields();
        ComboBox<ItemBaseModel> comboBox = (ComboBox<ItemBaseModel>) fields.get(0);
        assertEquals("dataModel", comboBox.getId());
        assertEquals("entity", fields.get(1).getId());
        assertEquals("startDate", fields.get(2).getId());
        assertEquals("endDate", fields.get(3).getId());
        TextField<String> keyField = (TextField<String>) fields.get(4);
        assertEquals("key", keyField.getId());
        ComboBox<ItemBaseModel> operationTypeComboBox = (ComboBox<ItemBaseModel>) fields.get(5);
        assertEquals("operationType", operationTypeComboBox.getId());
        assertEquals("source", fields.get(6).getId());
        assertEquals("userName", fields.get(7).getId());

        assertEquals(true, comboBox.getAllowBlank());
        assertEquals(true, keyField.getAllowBlank());
        ListStore<ItemBaseModel> operationStore = operationTypeComboBox.getStore();
        assertEquals("ALL", operationStore.getAt(0).get("key"));
        assertEquals("CREATE", operationStore.getAt(1).get("key"));
        assertEquals("UPDATE", operationStore.getAt(2).get("key"));
        assertEquals("PHYSICAL_DELETE", operationStore.getAt(3).get("key"));
        assertEquals("LOGIC_DELETE", operationStore.getAt(4).get("key"));
        assertEquals("RESTORED", operationStore.getAt(5).get("key"));
        assertEquals("ACTION", operationStore.getAt(6).get("key"));
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.journal.TestJournal";
    }
}
