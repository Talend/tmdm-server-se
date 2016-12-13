/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield.converter;

import java.util.Date;

import org.talend.mdm.webapp.browserecords.client.util.DateUtil;

import com.extjs.gxt.ui.client.binding.Converter;

public class DateConverter extends Converter {

    public Object convertModelValue(Object value) {
        return DateUtil.convertStringToDate((String)value);
    }

    public Object convertFieldValue(Object value) {
        return DateUtil.convertDateToString((Date)value);
    }
}
