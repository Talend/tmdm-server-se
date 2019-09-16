/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
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
     * Parse a multiple language string and return the message corresponding to the current language.
     *
     * the string is expected to be in the following format:
     *
     * [en:...][fr:...][zh:...]
     *
     * Characters ] and \ can be escaped in these using backslash escapes, for example
     *
     * [en: a message with a \] character in the middle]
     *
     * Language logic for description field:
     * If value of XX exists, return XX's value
     * If value of XX not exist, return raw value
     * If XX is empty or null, return raw value
     *
     * @param errorString Multiple language message string to be parsed
     * @param lang Language code of the desired message
     * @return Message corresponding to the current language
     */
    public static String getLocaleValue(String value, String lang) {
        String rawValue = value;
        if (value != null && lang != null) {
            Map<String, String> errorMessageHash = getLanguageValueMap(value);
            String langCode = lang.toLowerCase();
            if (errorMessageHash.isEmpty()) {
                return rawValue;
            } else if (errorMessageHash.get(langCode) != null) {
                return languageValueDecode(errorMessageHash.get(langCode));
            } else {
                return rawValue;
            }
        } else {
            return rawValue;
        }
    }

    private static String languageValueDecode(String value) {
        if (value != null && value.trim().length() > 0) {
            if (value.contains("&amp;#92;")) {//$NON-NLS-1$
                value = value.replace("&amp;#92;", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (value.contains("&amp;#91;")) {//$NON-NLS-1$
                value = value.replace("&amp;#91;", "["); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (value.contains("&amp;#93;")) {//$NON-NLS-1$
                value = value.replace("&amp;#93;", "]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return value;
    }

    /**
     * NOTE THAT THIS FUNCTION IS DUPLICATED WITH SAME FUNCTION IN:
     * org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser
     */
    private static LinkedHashMap<String, String> getLanguageValueMap(String errorString) {
        // Parse states
        final byte PARSE_ERROR = 0;
        final byte LOOKING_FOR_OPENING_BRACKET = 1;
        final byte LOOKING_FOR_COUNTRY_CODE_FIRST_CHAR = 2;
        final byte LOOKING_FOR_COUNTRY_CODE_SECOND_CHAR = 3;
        final byte LOOKING_FOR_COLON = 4;
        final byte LOOKING_FOR_CLOSING_BRACKET = 5;
        final byte ENCOUNTERED_FIRST_BACKSLASH = 6;

        byte parseState = LOOKING_FOR_OPENING_BRACKET;
        // string buffer for constructing current country code
        StringBuffer countryCodeBuffer = new StringBuffer();
        // string buffer for constructing current error message
        StringBuffer errorMessageBuffer = new StringBuffer();
        // map between country code and message
        LinkedHashMap<String, String> errorMessageHash = new LinkedHashMap<String, String>();

        int i = 0;
        if (errorString != null) {
            int errorStringLen = errorString.length();
            for (i = 0; i < errorStringLen && parseState != PARSE_ERROR; ++i) {
                char c = errorString.charAt(i);

                switch (parseState) {
                case LOOKING_FOR_OPENING_BRACKET:
                    if (c == '[') {
                        parseState = LOOKING_FOR_COUNTRY_CODE_FIRST_CHAR;
                    }
                    break;
                case LOOKING_FOR_COUNTRY_CODE_FIRST_CHAR:
                    if (isLetter(c)) {
                        countryCodeBuffer.append(c);
                        parseState = LOOKING_FOR_COUNTRY_CODE_SECOND_CHAR;
                    } else {
                        parseState = LOOKING_FOR_OPENING_BRACKET;
                    }
                    break;
                case LOOKING_FOR_COUNTRY_CODE_SECOND_CHAR:
                    if (isLetter(c)) {
                        countryCodeBuffer.append(c);
                        parseState = LOOKING_FOR_COLON;
                    } else {
                        countryCodeBuffer = new StringBuffer();
                        parseState = LOOKING_FOR_OPENING_BRACKET;
                    }
                    break;
                case LOOKING_FOR_COLON:
                    if (c == ':') {
                        parseState = LOOKING_FOR_CLOSING_BRACKET;
                    } else {
                        countryCodeBuffer = new StringBuffer();
                        parseState = LOOKING_FOR_OPENING_BRACKET;
                    }
                    break;
                case LOOKING_FOR_CLOSING_BRACKET:
                    if (c == ']') {
                        errorMessageHash.put(countryCodeBuffer.toString().toLowerCase(), errorMessageBuffer.toString());
                        countryCodeBuffer = new StringBuffer();
                        errorMessageBuffer = new StringBuffer();
                        parseState = LOOKING_FOR_OPENING_BRACKET;
                    } else if (c == '\\') {
                        parseState = ENCOUNTERED_FIRST_BACKSLASH;
                    } else {
                        errorMessageBuffer.append(c);
                    }
                    break;
                case ENCOUNTERED_FIRST_BACKSLASH:
                    if (c == '\\' || c == ']') {
                        errorMessageBuffer.append(c);
                    }
                    parseState = LOOKING_FOR_CLOSING_BRACKET;
                    break;
                default:
                    parseState = PARSE_ERROR;
                }
            }
        }
        return errorMessageHash;
    }

    private static boolean isLetter(char c) {
        boolean result = ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
        return result;
    }
}