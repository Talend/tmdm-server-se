/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class LocaleUtil {

    public static Locale getLocale() {
        HttpServletRequest request;
        RequestAttributes requestAttrs = RequestContextHolder.currentRequestAttributes();
        if (requestAttrs instanceof ServletRequestAttributes) {
            ServletRequestAttributes servletRequestAttrs = (ServletRequestAttributes) requestAttrs;
            request = servletRequestAttrs.getRequest();
        } else {
            request = null;
        }
        return getLocale(request);
    }

    public static Locale getLocale(HttpServletRequest request) {
        if (request == null) {
            return Locale.getDefault();
        }
        Locale locale;
        String language = request.getParameter("language"); //$NON-NLS-1$
        if (language == null) {
            language = (String) request.getSession().getAttribute("language"); //$NON-NLS-1$
        }
        if (language == null) {
            language = request.getHeader("X-MDM-Language"); //$NON-NLS-1$
        }
        if (language == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("Form_Locale")) { //$NON-NLS-1$
                        language = cookie.getValue();
                        break;
                    }
                }
            }
        }
        if (language == null) {
            locale = request.getLocale();
        } else {
            locale = getLocale(language);
        }
        return locale;
    }

    public static Locale getLocale(String language) {
        Locale locale;
        if (language.contains("_")) {
            String[] localeInfo = language.split("_");
            locale = new Locale(localeInfo[0].toLowerCase(), localeInfo[1]);
        } else {
            locale = new Locale(language.toLowerCase());
        }
        return locale;
    }
}
