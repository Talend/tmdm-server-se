/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import org.talend.mdm.webapp.browserecords.client.util.DateUtil;

@SuppressWarnings("nls")
public class DateUtilTest extends TestCase {

    public void testConvertStringToDateByFormat() throws ParseException {
        // 1
        String value = "13/02/2012";
        String format = "%1$te/%1$tm/%1$tY";
        String expectedValue = "2012-02-13";
        Date date = mock_DateUtil_convertStringToDateByFormat(value, format);
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.datePattern);
        String actualValue = sdf.format(date);
        assertEquals(expectedValue, actualValue);
        // 2
        value = "02/13/2012";
        format = "%1$tm/%1$te/%1$tY";
        date = mock_DateUtil_convertStringToDateByFormat(value, format);
        actualValue = sdf.format(date);
        assertEquals(expectedValue, actualValue);
        // 3
        value = "2012/02/13";
        format = "%1$tY/%1$tm/%1$te";
        date = mock_DateUtil_convertStringToDateByFormat(value, format);
        actualValue = sdf.format(date);
        assertEquals(expectedValue, actualValue);
        // 4
        value = "2012_02_13";
        format = "%1$tY_%1$tm_%1$te";
        date = mock_DateUtil_convertStringToDateByFormat(value, format);
        actualValue = sdf.format(date);
        assertEquals(expectedValue, actualValue);
        // 5
        value = "02-13-2012";
        format = "%1$tm-%1$te-%1$tY";
        date = mock_DateUtil_convertStringToDateByFormat(value, format);
        actualValue = sdf.format(date);
        assertEquals(expectedValue, actualValue);
    }

    /**
     * convert string to date according to the format
     * <ul>
     * <li>because no GWT Unit Test environment, so using JUnit To mock test
     * org.talend.mdm.webapp.browserecords.client.util.DateUtil.convertStringToDateByFormat(String, String)
     * </ul>
     * 
     * @param value
     * @param _format
     * @return
     * @throws ParseException
     */
    private static Date mock_DateUtil_convertStringToDateByFormat(String value, String _format) throws ParseException {
        String format;
        if (_format.contains("/")) { //$NON-NLS-1$
            format = getDateFormat(_format, "/"); //$NON-NLS-1$
        } else if (_format.contains("_")) { //$NON-NLS-1$
            format = getDateFormat(_format, "_"); //$NON-NLS-1$
        } else {
            format = getDateFormat(_format, "-"); //$NON-NLS-1$
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(value);
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