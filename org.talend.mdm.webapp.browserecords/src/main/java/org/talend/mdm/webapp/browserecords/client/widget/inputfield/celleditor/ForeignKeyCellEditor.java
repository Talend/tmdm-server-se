package org.talend.mdm.webapp.browserecords.client.widget.inputfield.celleditor;

import java.util.LinkedHashMap;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.MultiLanguageModel;
import org.talend.mdm.webapp.base.client.widget.MultiLanguageField;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.util.DateUtil;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeyField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.BooleanField;

import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;

public class ForeignKeyCellEditor extends CellEditor {

    private TypeModel typeModel;

    private Field<?> field;

    ForeignKeyBean fkBean;

    public ForeignKeyCellEditor(Field<? extends Object> field, TypeModel typeModel) {
        super(field);
        this.field = field;
        this.typeModel = typeModel;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object preProcessValue(Object value) {
        fkBean = (ForeignKeyBean) value;
        String v = fkBean.getForeignKeyInfo().get(typeModel.getXpath());
        if (value == null)
            return null;

        if (field instanceof SimpleComboBox) {
            if (field instanceof BooleanField) {
                Boolean bv = Boolean.parseBoolean(v.toString());
                return ((SimpleComboBox) getField()).findModel(bv);
            } else {
                return ((SimpleComboBox) getField()).findModel(v);
            }
        }

        if (field instanceof DateField) {
            return DateUtil.convertStringToDate(DateUtil.dateTimePattern, (String) v);
        } else if (field instanceof DateField) {
            return DateUtil.convertStringToDate((String) v);
        }

        if (field instanceof NumberField) {
            String numberType = getField().getData("numberType");//$NON-NLS-1$
            if ("integer".equals(numberType)) {//$NON-NLS-1$
                return Integer.parseInt((String) v);
            } else {
                return Double.parseDouble((String) v);
            }
        }

        if (field instanceof MultiLanguageField) {
            MultiLanguageModel multiLanguageModel = new MultiLanguageModel((String) v);
            ((MultiLanguageField) field).setMultiLanguageModel(multiLanguageModel);
            return multiLanguageModel.getValueByLanguage(Locale.getLanguage().toUpperCase());
        }

        if (field instanceof ForeignKeyField) {
            Map<String, String> foreignKeyInfo = new LinkedHashMap<String, String>();
            foreignKeyInfo.put(typeModel.getXpath(), v);
            ForeignKeyBean foreignKeyBean = new ForeignKeyBean();
            foreignKeyBean.setForeignKeyInfo(foreignKeyInfo);
            foreignKeyBean.setDisplayInfo(v);
            return foreignKeyBean;
        }

        if (field instanceof Field) {
            return v;
        }
        return null;
    }

    public Object postProcessValue(Object value) {
        if (fkBean != null){
            String v = value != null ? value.toString() : ""; //$NON-NLS-1$
            if (typeModel.getType().equals(DataTypeConstants.MLS)){
                v = ((MultiLanguageField) field).getMultiLanguageStringValue();
            }
            fkBean.getForeignKeyInfo().put(typeModel.getXpath(), v);
        }
        return fkBean;
    }
}