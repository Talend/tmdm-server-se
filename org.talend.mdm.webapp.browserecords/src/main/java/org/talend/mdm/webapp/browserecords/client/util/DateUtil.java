package org.talend.mdm.webapp.browserecords.client.util;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
public class DateUtil {

    public static String datePattern = "yyyy-MM-dd";//$NON-NLS-1$    

    public static String timePattern = "HH:mm:ss";//$NON-NLS-1$

    public static String dateTimePattern = "yyyy-MM-ddTHH:mm:ss";//$NON-NLS-1$

    public static String formatDateTimePattern = "yyyy-MM-dd'T'HH:mm:ss";//$NON-NLS-1$

    public static String formatDateTimePattern2 = "yyyy-MM-dd'T'HH:mm:ss.S"; //$NON-NLS-1$

    public static String formatDateTimePattern3 = "yyyy-MM-dd HH:mm:ss.S"; //$NON-NLS-1$

    public static final Date convertStringToDate(String aMask, String strDate) {
        if (strDate != null && strDate.trim().length() != 0) {
            DateTimeFormat df = DateTimeFormat.getFormat(aMask);
            return df.parse(strDate.trim());
        }
        return null;
    }

    public static Date convertStringToDate(String strDate) {
        return convertStringToDate(datePattern, strDate);
    }

    public static Date tryConvertStringToDate(String strDate){
        String[] formats = new String[] { formatDateTimePattern3,
                formatDateTimePattern2, formatDateTimePattern,
                dateTimePattern, timePattern, datePattern };
        RuntimeException re = null;
        for (String format : formats){
            try {
                Date d = convertStringToDate(format, strDate);
                return d;
            } catch (RuntimeException e){
                re = (RuntimeException) e;
                continue;
            }
        }
        throw re;
    }
    
    public static final String convertDateToString(Date aDate) {
        return getDateTime(aDate);
    }

    public static final String getDate(Date aDate) {
        if (aDate != null) {
            DateTimeFormat df = DateTimeFormat.getFormat(datePattern);
            return df.format(aDate);
        }
        return "";//$NON-NLS-1$
    }

    public static final String getDateTime(Date aDate) {
        if (aDate != null) {
            DateTimeFormat df = DateTimeFormat.getFormat(datePattern);
            DateTimeFormat tf = DateTimeFormat.getFormat(timePattern);
            return df.format(aDate) + "T" + tf.format(aDate);//$NON-NLS-1$
        }
        return "";//$NON-NLS-1$
    }

    public static final String convertDate(Date date) {
        if (date != null) {
            DateTimeFormat df = DateTimeFormat.getFormat(datePattern);
            return df.format(date);
        }
        return "";//$NON-NLS-1$
    }

    /**
     * convert string to date according to the format
     * <ul>
     * <li>for instance: value = "13/2/2012" and format = "dd/MM/yyyy";
     * </ul>
     * 
     * @param value
     * @param _format
     * @return
     */
    public static Date convertStringToDateByFormat(String value, String _format) {
        String format;
        if (_format.contains("/")) { //$NON-NLS-1$
            format = getDateFormat(_format, "/"); //$NON-NLS-1$
        } else if (_format.contains("_")) { //$NON-NLS-1$
            format = getDateFormat(_format, "_"); //$NON-NLS-1$
        } else {
            format = getDateFormat(_format, "-"); //$NON-NLS-1$
        }
        DateTimeFormat dtf = DateTimeFormat.getFormat(format);
        return dtf.parse(value);
    }

    private static String getDateFormat(String _format, String dateIntervalSymbol) {
        String format;
        if (_format.startsWith("%1$tm")) { //$NON-NLS-1$
            format = "MM" + dateIntervalSymbol + "dd" + dateIntervalSymbol + "yyyy"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else if (_format.startsWith("%1$tY")) {//$NON-NLS-1$
            format = "yyyy" + dateIntervalSymbol + "MM" + dateIntervalSymbol + "dd"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            format = "dd" + dateIntervalSymbol + "MM" + dateIntervalSymbol + "yyyy"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        }
        return format;
    }
}
