/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.filter;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;

public class NumericFilter extends com.extjs.gxt.ui.client.widget.grid.filters.NumericFilter {

    public NumericFilter(String dataIndex) {
        super(dataIndex);
    }

    @Override
    public boolean validateModel(ModelData model) {
        ModelData valueModelData = new BaseModelData();
        String value = model.get(dataIndex);
        valueModelData.set(dataIndex, Double.valueOf(value));
        return super.validateModel(valueModelData);
    }
}
