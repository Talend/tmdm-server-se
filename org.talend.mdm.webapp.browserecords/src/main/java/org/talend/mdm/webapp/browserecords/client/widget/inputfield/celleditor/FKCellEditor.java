/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield.celleditor;

import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsListPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeyField;

import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.Grid;

public class FKCellEditor extends CellEditor {

    private ForeignKeyField field;
    
    public FKCellEditor(ForeignKeyField field) {
        super(field);
        this.field = field;
    }

    @Override
    public Object preProcessValue(Object value) {
        if (value == null) {
            return null;
        }
        Grid<ItemBean> grid = ItemsListPanel.getInstance().getGrid();

        ItemBean itemBean = grid.getSelectionModel().getSelectedItem();
        ForeignKeyBean fkBean = itemBean.getForeignkeyDesc((String) value);
        if (fkBean == null && field != null && field.getSuggestBox() != null) {
            field.getSuggestBox().setValue(null);
        }
        return fkBean;
    }

    @Override
    public Object postProcessValue(Object value) {
        if (value == null) {
            return value;
        }
        Grid<ItemBean> grid = ItemsListPanel.getInstance().getGrid();

        ItemBean itemBean = grid.getSelectionModel().getSelectedItem();
        ForeignKeyBean fkBean = (ForeignKeyBean) value;
        if (itemBean.getForeignkeyDesc(fkBean.getFullString()) == null) {
            itemBean.setForeignkeyDesc(fkBean.getFullString(), fkBean);
        }
        return fkBean.getFullString();
    }
}
