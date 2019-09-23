/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.ForeignKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.shared.util.CommonUtil;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;

import com.extjs.gxt.ui.client.widget.form.Field;

public class ForeignKeyCellField extends ForeignKeyField {

    private Map<Integer, Map<String, Field<?>>> targetFields;

    public Map<Integer, Map<String, Field<?>>> getTargetFields() {
        return targetFields;
    }

    public ForeignKeyCellField(ForeignKeyField foreignKeyField, String foreignKeyFilter) {
        super(foreignKeyField.getDataType());
        super.setReadOnly(foreignKeyField.isReadOnly());
        super.setEnabled(!foreignKeyField.isReadOnly());
        this.foreignKeyFilter = foreignKeyFilter;
        this.originForeignKeyFilter = foreignKeyFilter;
    }

    public void setTargetField(Map<Integer, Map<String, Field<?>>> targetFields) {
        this.targetFields = targetFields;
    }

    @Override public String parseForeignKeyFilter() {
        if (foreignKeyFilter != null) {
            String[] criterias = CommonUtil.getCriteriasByForeignKeyFilter(foreignKeyFilter);
            List<Map<String, String>> conditions = new ArrayList<Map<String, String>>();
            for (int i = 0; i < criterias.length; i++) {
                String criteria = criterias[i];
                Map<String, String> conditionMap = CommonUtil.buildConditionByCriteria(criteria);
                String filterValue = conditionMap.get(CommonUtil.VALUE_STR);
                if (filterValue == null) {
                    continue;
                }

                filterValue = CommonUtil.unescapeXml(filterValue);
                if (org.talend.mdm.webapp.base.shared.util.CommonUtil.isFilterValue(filterValue)) {
                    filterValue = filterValue.substring(1, filterValue.length() - 1);
                } else {
                    if (targetFields != null && targetFields.get(i) != null) {
                        Map<String, Field<?>> targetFieldMap = targetFields.get(i);
                        Field<?> targetField = targetFieldMap.get(targetFieldMap.keySet().iterator().next());
                        Object targetValue = targetField.getValue();
                        if (targetValue != null) {
                            if (targetValue instanceof ForeignKeyBean) {
                                filterValue = CommonUtil.unwrapFkValue(((ForeignKeyBean) targetValue).getId());
                            } else {
                                filterValue = targetField.getValue().toString();
                            }
                        } else {
                            filterValue = CommonUtil.EMPTY;
                        }
                    }
                }
                conditionMap.put(CommonUtil.VALUE_STR, filterValue);
                conditions.add(conditionMap);
            }
            return CommonUtil.buildForeignKeyFilterByConditions(conditions);
        } else {
            return CommonUtil.EMPTY;
        }
    }
}
