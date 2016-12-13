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

import java.util.Date;

import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.util.DateUtil;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsListPanel;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatDateField;

import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.Grid;

public class DateFieldCellEditor extends CellEditor {
    private FormatDateField field;
    
    public DateFieldCellEditor(FormatDateField field) {
        super(field);
        this.field = field;
    }

    public Object preProcessValue(Object value) {
        Grid<ItemBean> grid = ItemsListPanel.getInstance().getGrid();
        ItemBean itemBean = grid.getSelectionModel().getSelectedItem();
        if(itemBean.getOriginalMap().containsKey(field.getName()))
            return itemBean.getOriginalMap().get(field.getName());
        return DateUtil.tryConvertStringToDate((String) value);
    }

    public Object postProcessValue(Object value) {
        Grid<ItemBean> grid = ItemsListPanel.getInstance().getGrid();
        ItemBean itemBean = grid.getSelectionModel().getSelectedItem();
        if(itemBean.getOriginalMap().containsKey(field.getName()))
            itemBean.getOriginalMap().put(field.getName(), (Date)value);
        return DateUtil.getDate((Date) value);

    }
}
