/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class MultilanguageMessageParser {

    /**
     * Parse a multiple language string and return the message corresponding to the current language.
     * 
     * errorString is expected to be in the following format:
     * 
     * [en:...][fr:...][zh:...]
     * 
     * Characters ] and \ can be escaped in these using backslash escapes, for example
     * 
     * [en: a message with a \] character in the middle]
     * 
     * A message for a language can also be embedded anywhere in the string, for example
     * 
     * abcd[en:...]abcd[fr:...]abcd
     * 
     * If a message for the current language exists, then it is returned.
     * 
     * If a message for the current language doesn't exist, but an english message exists, then it is returned.
     * 
     * If neither of the above are true, then no parsing takes place, and the original errorString is returned.
     * 
     * @param errorString Multiple language message string to be parsed
     * @param lang Language code of the desired message
     * @return Message corresponding to the current language
     */
    public static String pickOutISOMessage(String errorString, String lang) {
        return pickOutISOMessage(errorString, lang, false);
    }

    public static String pickOutISOMessage(String errorString) {
        return pickOutISOMessage(errorString, UrlUtil.getLanguage());
    }

    public static String getValueByLanguage(String multiLanguageString, String language) {
        return pickOutISOMessage(multiLanguageString, language, true);
    }

    public static boolean isExistMultiLanguageFormat(String multiLanguageString) {
        Map<String, String> map = getLanguageValueMap(multiLanguageString);
        return !map.isEmpty();
    }

    private static String pickOutISOMessage(String errorString, String lang, boolean isGetLanguageValue) {
        if (errorString != null && lang != null) {
            Map<String, String> errorMessageHash = getLanguageValueMap(errorString);
            String resultingErrorMessage = errorString;
            String langCode = lang.toLowerCase();
            if (isGetLanguageValue) {
                if (errorMessageHash.isEmpty()) {
                    return resultingErrorMessage;
                } else {
                    return FormatUtil.languageValueDecode(errorMessageHash.get(langCode));
                }
            }
            if (errorMessageHash.containsKey(langCode)) {
                resultingErrorMessage = errorMessageHash.get(langCode);
            } else if (errorMessageHash.containsKey("en")) { //$NON-NLS-1$
                resultingErrorMessage = errorMessageHash.get("en"); //$NON-NLS-1$
            }

            return resultingErrorMessage;

        } else {

            return null;
        }
    }

    public static LinkedHashMap<String, String> getLanguageValueMap(String errorString) {
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
    public static boolean isLetter(char c) {
        boolean result = ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');

        return result;
    }

    public static String getFormatValueByDefaultLanguage(String value, String language) {
        value = FormatUtil.languageValueEncode(value);
        return "[" + language.toUpperCase() + ":" + value + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
