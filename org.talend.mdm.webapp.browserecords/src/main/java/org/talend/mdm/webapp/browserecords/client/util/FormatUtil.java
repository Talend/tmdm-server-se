/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.google.gwt.i18n.client.NumberFormat;

public class FormatUtil {
    
    public static String defaultDatePattern = "yyyy-MM-dd"; //$NON-NLS-1$
    
    public static String defaultDateTimePattern = "yyyy-MM-dd HH:mm:ss"; //$NON-NLS-1$

    
    public static String convertDatePattren(String pattern) {
        if(pattern == null)
            return defaultDatePattern;
        if(pattern.trim().equals("")) //$NON-NLS-1$
            return defaultDatePattern;
        if(!pattern.startsWith("%")) //$NON-NLS-1$
            return pattern;
        
        String result = ""; //$NON-NLS-1$
        pattern = pattern.trim();
        pattern = pattern.replace("%t", ""); //$NON-NLS-1$ //$NON-NLS-2$
        pattern = pattern.replace("%1$t", ""); //$NON-NLS-1$ //$NON-NLS-2$
        pattern = pattern.replace("%T", "");  //$NON-NLS-1$//$NON-NLS-2$
        pattern = pattern.replace("%1$T", ""); //$NON-NLS-1$ //$NON-NLS-2$
        
        for (int i=0;i<pattern.length();i++){
            result += transferPattern(String.valueOf(pattern.charAt(i)));
        }
        return result;
    }
    
    private static String transferPattern(String pattern) {
        if ("c".equals(pattern)){ //$NON-NLS-1$
            return "E M dd HH:mm:ss z y"; //$NON-NLS-1$
        }else if ("D".equals(pattern)){ //$NON-NLS-1$
            return "M/dd/yy"; //$NON-NLS-1$
        }else if ("F".equals(pattern)){ //$NON-NLS-1$
            return "yy-M-dd"; //$NON-NLS-1$
        }else if ("R".equals(pattern)){ //$NON-NLS-1$
            return "HH:mm"; //$NON-NLS-1$
        }else if ("T".equals(pattern)){ //$NON-NLS-1$
            return "HH:mm:ss"; //$NON-NLS-1$
        }else if ("r".equals(pattern)){ //$NON-NLS-1$
            return "h:m:s a"; //$NON-NLS-1$
        }else if ("H".equals(pattern) || "k".equals(pattern)){ //$NON-NLS-1$ //$NON-NLS-2$
            return "HH"; //$NON-NLS-1$
        }else if ("I".equals(pattern) || "l".equals(pattern)){ //$NON-NLS-1$ //$NON-NLS-2$
            return "hh"; //$NON-NLS-1$
        }else if ("M".equals(pattern)){ //$NON-NLS-1$
            return "mm"; //$NON-NLS-1$
        }else if ("S".equals(pattern)){ //$NON-NLS-1$
            return "ss"; //$NON-NLS-1$
        }else if ("L".equals(pattern)){ //$NON-NLS-1$
            return "SSS"; //$NON-NLS-1$
        }else if ("p".equals(pattern)){ //$NON-NLS-1$
            return "a"; //$NON-NLS-1$
        }else if ("Z".equals(pattern)){ //$NON-NLS-1$
            return "z"; //$NON-NLS-1$
        }else if ("z".equals(pattern)){ //$NON-NLS-1$
            return "Z";         //$NON-NLS-1$
        }else if ("B".equals(pattern) || "b".equals(pattern) || "h".equals(pattern) || "m".equals(pattern)){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            return "M"; //$NON-NLS-1$
        }else if ("A".equals(pattern) || "a".equals(pattern)){ //$NON-NLS-1$ //$NON-NLS-2$
            return "E"; //$NON-NLS-1$
        }else if ("C".equals(pattern) || "Y".equals(pattern) || "y".equals(pattern)){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return "y"; //$NON-NLS-1$
        }else if ("d".equals(pattern) || "e".equals(pattern)){ //$NON-NLS-1$ //$NON-NLS-2$
            return "d"; //$NON-NLS-1$
        }else{
            return pattern;
        }
    }
    
    public static String changeNumberToFormatedValue(String value) {
        StringBuilder pattern = new StringBuilder("###0.");
        if(value == null || value.equals("")){
            return "" ;
        }
        if(value.contains(",")){
            value = value.replace(",", ".") ;
        }
        int fractionDigits = 0;
        if (value.contains(".")) {
            fractionDigits = value.trim().split("\\.")[1].length();
        }

        if (fractionDigits == 0) {
            pattern.append("######");
        }

        for (int i = 0; i < fractionDigits; i++) {
            pattern.append("0");
        }
        NumberFormat nf = NumberFormat.getFormat(pattern.toString());
        return nf.format(Double.valueOf(value));
    }
    
    public static Number getDecimalValue(String value, Object franctionDigits) {
        if (value == null || "".equals(value)) {
            return null;
        }
        BigDecimal bigdecimal = new BigDecimal(value);
        if (franctionDigits != null && !franctionDigits.equals("")) {
            return bigdecimal.setScale(Integer.parseInt(franctionDigits.toString()), RoundingMode.HALF_UP);
        }
        return bigdecimal.setScale(2, RoundingMode.HALF_UP);
    }
}
