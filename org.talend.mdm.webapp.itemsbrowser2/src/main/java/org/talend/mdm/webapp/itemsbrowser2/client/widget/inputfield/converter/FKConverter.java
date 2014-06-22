package org.talend.mdm.webapp.itemsbrowser2.client.widget.inputfield.converter;

import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;

import com.extjs.gxt.ui.client.binding.Converter;


public class FKConverter extends Converter {

    @Override
    public Object convertModelValue(Object value) {
        if (value == null) return null;
        ForeignKeyBean fkBean = new ForeignKeyBean();
        fkBean.setId((String) value);
        return fkBean;
    }

    @Override
    public Object convertFieldValue(Object value) {
        if (value == null) return null;
        return ((ForeignKeyBean) value).getId();
    }
}
