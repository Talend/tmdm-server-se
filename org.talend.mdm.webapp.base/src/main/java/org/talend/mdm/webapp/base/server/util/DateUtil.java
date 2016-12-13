/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.server.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.log4j.Logger;

public final class DateUtil {

    private static final Logger log = Logger.getLogger(DateUtil.class);

    private static String datePattern = "yyyy-MM-dd";//$NON-NLS-1$

    private static String timePattern = "HH:mm";//$NON-NLS-1$

    /**
     * Return default date format (yyyy/MM/dd)
     * 
     * @return the date format will be show
     */
    public static String getDatePattern() {
        return datePattern;
    }

    /**
     * 
     * 
     * @param aDate
     * @return
     */
    public static final String getDate(Date aDate) {
        SimpleDateFormat df = null;
        String returnValue = "";//$NON-NLS-1$

        if (aDate != null) {
            df = new SimpleDateFormat(datePattern);
            returnValue = df.format(aDate);
        }

        return (returnValue);
    }

    /**
     * 
     * 
     * @param aMask
     * @param strDate
     * @return Date
     * @see java.text.SimpleDateFormat
     * @throws ParseException
     */
    public static final Date convertStringToDate(String aMask, String strDate) {
        if (log.isDebugEnabled()) {
            log.debug("converting '" + strDate + "' to date with mask '" + aMask + "'");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        Date date = null;
        try {
            if (strDate != null && strDate.length() > 0) {
                SimpleDateFormat df = new SimpleDateFormat(aMask);
                date = df.parse(strDate);
            }
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return (date);
    }

    /**
     * This method returns the current date time in the format: yyyy/MM/dd HH:MM a
     * 
     * @param theTime the current time
     * @return the current date/time
     */
    public static String getTimeNow(Date theTime) {
        return getDateTime(timePattern, theTime);
    }

    /**
     * This method returns the current date in the format: yyyy/MM/dd
     * 
     * @return the current date
     * @throws ParseException
     */
    public static Calendar getToday() throws ParseException {
        Date today = new Date();
        SimpleDateFormat df = new SimpleDateFormat(datePattern);

        // This seems like quite a hack (date -> string -> date),
        // but it works ;-)
        String todayAsString = df.format(today);
        Calendar cal = new GregorianCalendar();
        cal.setTime(convertStringToDate(todayAsString));

        return cal;
    }

    /**
     * This method generates a string representation of a date's date/time in the format you specify on input
     * 
     * @param aMask the date pattern the string is in
     * @param aDate a date object
     * @return a formatted string representation of the date
     * 
     * @see java.text.SimpleDateFormat
     */
    public static final String getDateTime(String aMask, Date aDate) {
        SimpleDateFormat df = null;
        String returnValue = "";//$NON-NLS-1$

        if (aDate == null) {
            log.error("aDate is null!");//$NON-NLS-1$
        } else {
            df = new SimpleDateFormat(aMask);
            returnValue = df.format(aDate);
        }

        return (returnValue);
    }

    /**
     * 
     * 
     * @param aDate
     * @return
     */
    public static final String convertDateToString(Date aDate) {
        return getDateTime(datePattern, aDate);
    }

    /**
     * 
     * 
     * @param strDate (format yyyy/MM/dd)
     * @return
     * 
     * @throws ParseException
     */
    public static Date convertStringToDate(String strDate) throws ParseException {
        return convertStringToDate(datePattern, strDate);
    }

    public static String weekday(String date) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = DateFormat.getDateInstance();

        Date da = null;
        da = dateFormat.parse(date);

        calendar.setTime(da);
        int num = calendar.get(Calendar.DAY_OF_WEEK);

        if (num == 1) {
            num = 7;
        } else {
            num = num - 1;
        }

        return num + "";//$NON-NLS-1$

    }

    public static boolean isMonthEnd() {
        boolean yes = false;

        Calendar cal = new GregorianCalendar();
        if (cal.get(Calendar.DATE) == cal.getActualMaximum(Calendar.DATE)) {
            yes = true;
        }

        return yes;
    }
}
