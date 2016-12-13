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

import org.talend.mdm.webapp.browserecords.shared.Constants;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;

public class BooleanFilter extends com.extjs.gxt.ui.client.widget.grid.filters.BooleanFilter {

    public BooleanFilter(String dataIndex) {
        super(dataIndex);
        // set FALSE CheckMenuItem checked property
        ((CheckMenuItem) menu.getItem(1)).setChecked(false);
    }

    @Override
    public void setMessages(FilterMessages messages) {
        BooleanFilterMessages booleanFilterMessages = (BooleanFilterMessages) messages;
        booleanFilterMessages.setYesText(Constants.BOOLEAN_TRUE_DISPLAY_VALUE);
        booleanFilterMessages.setNoText(Constants.BOOLEAN_FALSE_DISPLAY_VALUE);
        super.setMessages(booleanFilterMessages);
    }

    @Override
    public boolean validateModel(ModelData model) {
        Boolean val = Boolean.valueOf((String) getModelValue(model));
        return getValue().equals(val == null ? Boolean.FALSE : val);
    }
}
