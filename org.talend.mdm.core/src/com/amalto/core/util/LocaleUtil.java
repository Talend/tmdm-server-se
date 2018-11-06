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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class LocaleUtil {

    private static final Logger LOGGER = Logger.getLogger(LocaleUtil.class);

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
        String localLanguage = language.toLowerCase();
        if (localLanguage.contains("_")) {
            String[] localeInfo = localLanguage.split("_");
            localLanguage = localeInfo[0];
        }
        if (Locale.CHINESE.getLanguage().equals(localLanguage)) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        return new Locale(localLanguage);
    }

    /**
     * According to language to get corresponding value, 
     * @param value, like "[fr:Produit avec Magasins][en:Product with Stores]"
     * @param language, like "en", "zh_CN", if language is empty, assign to <b> en </b> by default
     * @return
     *
     * Please note that function getValueByLanguage(in class org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser) 
     * has same functionality as below function getLocaleValue, They should be consistent during change.
     * 
     */
    public static String getLocaleValue(String value, String language) {
        String result = null;
        if (StringUtils.isEmpty(value)) {
            return StringUtils.EMPTY;
        }
        language = checkAndGetLang(language);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Retrieves the description about a locale specified by " + language + " in the list of available locales.");
        }
        Map<String, String> localeMap = new HashMap<>();
        Pattern pattern = Pattern.compile("(?<=\\[)(.*?)(?=\\])");
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            String[] matcherPair = matcher.group().split(":", 2);
            localeMap.put(matcherPair[0].toLowerCase(), matcherPair[1]);
        }

        if (localeMap.containsKey(language)) {
            result = localeMap.get(language);
        }
        if (StringUtils.isEmpty(result)) {
            result = localeMap.get(Locale.ENGLISH.getLanguage());
            if (StringUtils.isEmpty(result)) {
                result = value;
            }
        }
        return result;
    }

    public static String checkAndGetLang(String language) {
        if (StringUtils.isEmpty(language)) {
            return Locale.ENGLISH.getLanguage();
        }
        String[] rawLocales = language.split("_", 2);
        if (rawLocales.length == 2 && !LocaleUtils.isAvailableLocale(new Locale(rawLocales[0], rawLocales[1]))) {
            return Locale.ENGLISH.getLanguage();
        } else if (rawLocales.length == 1 && !LocaleUtils.isAvailableLocale(new Locale(rawLocales[0]))) {
            return Locale.ENGLISH.getLanguage();
        }
        return rawLocales[0].toLowerCase();
    }
}