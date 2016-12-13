/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield.propertyeditor;

import java.math.BigDecimal;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.form.NumberPropertyEditor;
import com.google.gwt.i18n.client.NumberFormat;


public class FormatNumberPropertyEditor extends NumberPropertyEditor {

    public FormatNumberPropertyEditor() {
        super();
    }

    public FormatNumberPropertyEditor(Class<?> type) {
        super(type);
    }

    @Override
    public Number convertStringValue(String value) {
        // first try to create a typed value directly from the raw text
        try {
            if (type == Short.class) {
                return Short.valueOf(value);
            } else if (type == Integer.class) {
                return Integer.valueOf(value);
            } else if (type == Long.class) {
                return Long.valueOf(value);
            } else if (type == Float.class) {
                return Float.valueOf(value);
            } else if (type == BigDecimal.class) {
                return new BigDecimal(value);
            } else {
                return Double.valueOf(value);
            }
        } catch (Exception e) {
            if (Log.isErrorEnabled()) {
                Log.error(e.toString());
            }
        }

        // second, stip all unwanted characters
        String stripValue = stripValue(value);
        try {
            if (type == Short.class) {
                return Short.valueOf(stripValue);
            } else if (type == Integer.class) {
                return Integer.valueOf(stripValue);
            } else if (type == Long.class) {
                return Long.valueOf(stripValue);
            } else if (type == Float.class) {
                return Float.valueOf(stripValue);
            } else if (type == BigDecimal.class) {
                return new BigDecimal(value);
            } else {
                return Double.valueOf(stripValue);
            }
        } catch (Exception e) {
            if (Log.isErrorEnabled()) {
                Log.error(e.toString());
            }
        }

        // third try parsing with the formatter
        if (format != null) {
            Double d = format.parse(value);
            return returnTypedValue(d);
        } else {
            Double d = NumberFormat.getDecimalFormat().parse(value);
            return returnTypedValue(d);
        }
    }
    
    @Override
    protected Number returnTypedValue(Number number) {
        if (type == Short.class) {
            return number.shortValue();
        } else if (type == Integer.class) {
            return number.intValue();
        } else if (type == Long.class) {
            return number.longValue();
        } else if (type == Float.class) {
            return number.floatValue();
        } else if (type == BigDecimal.class) {
            return new BigDecimal(number.toString());
        } else if(type == Double.class){
            return number.doubleValue() ;
        }
        return number;
    }
    
    public String getStringValue(Number value) {
        if (format != null) {
            return format.format(value.doubleValue());
        }

        if (type == Float.class || type == Double.class) {
            if (!value.toString().contains(".")) {
                return value.toString() + ".0";
            }
        }
        return value.toString();
    }
}
