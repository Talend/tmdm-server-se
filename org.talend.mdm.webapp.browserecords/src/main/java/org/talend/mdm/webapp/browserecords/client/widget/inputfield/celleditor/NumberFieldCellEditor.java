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

import java.math.BigDecimal;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsListPanel;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatNumberField;

import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.Grid;


public class NumberFieldCellEditor extends CellEditor {

    public NumberFieldCellEditor(NumberField field) {
        super(field);
    }
    public Object preProcessValue(Object value) {
        String numberType = getField().getData("numberType");//$NON-NLS-1$

        Grid<ItemBean> grid = ItemsListPanel.getInstance().getGrid();
        ItemBean itemBean = grid.getSelectionModel().getSelectedItem();
        Number num = null;
        if (itemBean.getOriginalMap().containsKey(getField().getName())) {
            num = (Number) itemBean.getOriginalMap().get(getField().getName());
        } else {
            if (DataTypeConstants.INTEGER.getBaseTypeName().equals(numberType)) {
                num = Integer.parseInt((String) value);
            } else if (DataTypeConstants.FLOAT.getBaseTypeName().equals(numberType)) {
                num = Float.parseFloat((String) value);
            } else if (DataTypeConstants.DECIMAL.getBaseTypeName().equals(numberType)) {
                num = ((FormatNumberField)getField()).getDecimalValue((String)value) ;
            } else {
                num = Double.parseDouble((String) value);
            }
        }
        return num;
    }

    public Object postProcessValue(Object value) {
        if (value == null)
            return ""; //$NON-NLS-1$

        Grid<ItemBean> grid = ItemsListPanel.getInstance().getGrid();
        ItemBean itemBean = grid.getSelectionModel().getSelectedItem();
        if (itemBean.getOriginalMap().containsKey(getField().getName()))
            itemBean.getOriginalMap().put(getField().getName(), (Number) value);

        String numberType = getField().getData("numberType");//$NON-NLS-1$
        if (DataTypeConstants.INTEGER.getBaseTypeName().equals(numberType)) {
            Integer v = (Integer) value;
            return Integer.toString(v.intValue());
        } else if (DataTypeConstants.FLOAT.getBaseTypeName().equals(numberType)) {
            Float v = (Float) value;
            return Float.toString(v);
        } else if (DataTypeConstants.DECIMAL.getBaseTypeName().equals(numberType)) {
            BigDecimal v = new BigDecimal(value.toString()) ;
            return v.toString();
        } else {
            Double v = (Double) value;
            return Double.toString(v);
        }
    }
}
