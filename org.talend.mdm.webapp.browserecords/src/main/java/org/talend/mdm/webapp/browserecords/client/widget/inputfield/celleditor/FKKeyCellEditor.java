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
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeyCellField;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ReturnCriteriaFK;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;

public class FKKeyCellEditor extends CellEditor {

    private Field<?> field;

    private TypeModel typeModel;

    ForeignKeyBean fkBean;

    ReturnCriteriaFK returnCriteriaFK;

    public FKKeyCellEditor(Field<? extends Object> field, TypeModel typeModel, ReturnCriteriaFK returnCriteriaFK) {
        super(field);
        this.field = field;
        this.typeModel = typeModel;
        this.returnCriteriaFK = returnCriteriaFK;
    }

    @Override
    public Object preProcessValue(Object value) {
        fkBean = (ForeignKeyBean) value;
        return value;
    }

    @Override
    public Object postProcessValue(Object value) {
        ForeignKeyBean tempValue = (ForeignKeyBean) value;
        if (field instanceof ForeignKeyCellField) {
            if (value instanceof ForeignKeyBean) {
                if (fkBean != null) {
                    String v = value != null ? value.toString() : ""; //$NON-NLS-1$
                    fkBean.getForeignKeyInfo().put(typeModel.getXpath(), v);
                }
            }
        }
        if (returnCriteriaFK != null && fkBean != null && tempValue != null && !fkBean.getId().equals(tempValue.getId())) {
            returnCriteriaFK.setCriteriaFK(fkBean);
        }
        return value;
    }
}