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

import com.extjs.gxt.ui.client.binding.Converter;
import com.extjs.gxt.ui.client.widget.form.Field;


public class NumberConverter extends Converter {
    
    Field field;
    
    public NumberConverter(Field field){
        this.field = field;
    }
    
    public Object convertModelValue(Object value) {
        if (value == null) return null;
        String numberType = field.getData("numberType");//$NON-NLS-1$
        if ("integer".equals(numberType)){//$NON-NLS-1$
            return Integer.parseInt((String)value);
        } else {
            return Double.parseDouble((String) value);
        }
    }

    public Object convertFieldValue(Object value) {
        if (value == null) return null;
        String numberType = field.getData("numberType");//$NON-NLS-1$
        if ("integer".equals(numberType)){//$NON-NLS-1$
            Integer v = (Integer) value;
            return Integer.toString(v.intValue());
        } else {
            Double v = (Double) value;
            return Double.toString(v);
        }
    }
}
