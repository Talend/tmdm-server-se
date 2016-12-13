/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;

import com.extjs.gxt.ui.client.widget.form.Field;

public class ForeignKeyCellField extends ForeignKeyField {

    private String foreignKeyFilter;

    private Map<Integer, Field<?>> targetFields;

    public ForeignKeyCellField(ForeignKeyField foreignKeyField, String foreignKeyFilter) {
        super(foreignKeyField.getDataType());
        this.foreignKeyFilter = foreignKeyFilter;
    }

    public void setTargetField(Map<Integer, Field<?>> targetFields) {
        this.targetFields = targetFields;
    }

    @Override
    public String parseForeignKeyFilter() {
        if (foreignKeyFilter != null) {
            String[] criterias = org.talend.mdm.webapp.base.shared.util.CommonUtil
                    .getCriteriasByForeignKeyFilter(foreignKeyFilter);
            List<Map<String, String>> conditions = new ArrayList<Map<String, String>>();
            for (int i = 0; i < criterias.length; i++) {
                String criteria = criterias[i];
                Map<String, String> conditionMap = org.talend.mdm.webapp.base.shared.util.CommonUtil
                        .buildConditionByCriteria(criteria);
                String filterValue = conditionMap.get("Value"); //$NON-NLS-1$
                if (filterValue == null) {
                    continue;
                }

                filterValue = org.talend.mdm.webapp.base.shared.util.CommonUtil.unescapeXml(filterValue);
                if (org.talend.mdm.webapp.base.shared.util.CommonUtil.isFilterValue(filterValue)) {
                    filterValue = filterValue.substring(1, filterValue.length() - 1);
                } else {
                    if (targetFields != null && targetFields.get(i) != null) {
                        Field<?> targetField = targetFields.get(i);
                        Object targetValue = targetField.getValue();
                        if (targetValue != null) {
                            if (targetValue instanceof ForeignKeyBean) {
                                filterValue = org.talend.mdm.webapp.base.shared.util.CommonUtil
                                        .unwrapFkValue(((ForeignKeyBean) targetValue).getId());
                            } else {
                                filterValue = targetField.getValue().toString();
                            }
                        } else {
                            filterValue = ""; //$NON-NLS-1$
                        }
                    }
                }
                conditionMap.put("Value", filterValue); //$NON-NLS-1$
                conditions.add(conditionMap);
            }
            return org.talend.mdm.webapp.base.shared.util.CommonUtil.buildForeignKeyFilterByConditions(conditions);
        } else {
            return ""; //$NON-NLS-1$
        }
    }
}
