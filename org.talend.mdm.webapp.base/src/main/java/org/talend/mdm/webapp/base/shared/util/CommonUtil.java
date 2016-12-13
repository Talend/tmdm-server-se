/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.shared.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by yjli on 2013-8-1 Detailled comment
 * 
 */
public class CommonUtil {

    public static String escape(String src) {
        int i;
        char j;
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length() * 6);

        for (i = 0; i < src.length(); i++) {

            j = src.charAt(i);

            if (Character.isDigit(j) || Character.isLowerCase(j) || Character.isUpperCase(j)) {
                tmp.append(j);
            } else if (j < 256) {
                tmp.append("%"); //$NON-NLS-1$
                if (j < 16) {
                    tmp.append("0"); //$NON-NLS-1$
                }
                tmp.append(Integer.toString(j, 16));
            } else {
                tmp.append("%u"); //$NON-NLS-1$
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    public static String escapeSemicolon(String src) {
        int i;
        char j;
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (';' == j || '%' == j) {
                if (j < 256) {
                    tmp.append("%"); //$NON-NLS-1$
                    if (j < 16) {
                        tmp.append("0"); //$NON-NLS-1$
                    }
                    tmp.append(Integer.toString(j, 16));
                } else {
                    tmp.append("%u"); //$NON-NLS-1$
                    tmp.append(Integer.toString(j, 16));
                }
            } else {
                tmp.append(j);
            }
        }
        return tmp.toString();
    }

    public static String unescape(String src) {
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos); //$NON-NLS-1$
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    tmp.append(src.substring(lastPos, pos));
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }

    public static String convertListToString(List<String> itemList, String separator) {
        if (itemList == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < itemList.size(); i++) {
            result.append((i > 0) ? separator : ""); //$NON-NLS-1$ 
            result.append(escape(itemList.get(i)));
        }
        return result.toString();
    }

    public static List<String> convertStrigToList(String valueString, String separator) {
        if (valueString == null || valueString.isEmpty()) {
            return null;
        }
        List<String> valueList = new ArrayList<String>();
        String[] valueArray = valueString.split(separator);
        for (String value : valueArray) {
            valueList.add(unescape(value));
        }
        return valueList;
    }

    public static String convertListToString(List<String> itemList) {
        if (itemList == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < itemList.size(); i++) {
            result.append((i > 0) ? ";" : ""); //$NON-NLS-1$ //$NON-NLS-2$ 
            result.append(escapeSemicolon(itemList.get(i)));
        }
        return result.toString();
    }

    public static List<String> convertStrigToList(String valueString) {
        if (valueString == null || valueString.isEmpty()) {
            return null;
        }
        List<String> valueList = new ArrayList<String>();
        String[] valueArray = valueString.split(";"); //$NON-NLS-1$
        for (String value : valueArray) {
            valueList.add(unescape(value));
        }
        return valueList;
    }

    public static String[] getCriteriasByForeignKeyFilter(String fkFilter) {
        return fkFilter.split("#");//$NON-NLS-1$
    }

    public static String buildForeignKeyFilterByConditions(List<Map<String, String>> conditions) {
        String parsedFkfilter = ""; //$NON-NLS-1$
        if (conditions.size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (Map<String, String> map : conditions) {
                Map<String, String> conditionMap = map;
                if (conditionMap.size() > 0) {
                    String xpath = conditionMap.get("Xpath") == null ? "" : conditionMap.get("Xpath");//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                    String operator = conditionMap.get("Operator") == null ? "" : conditionMap.get("Operator");//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                    String value = conditionMap.get("Value") == null ? "" : conditionMap.get("Value");//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                    String predicate = conditionMap.get("Predicate") == null ? "" : conditionMap.get("Predicate");//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                    sb.append(xpath + "$$" + operator + "$$" + value + "$$" + predicate + "#");//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
                }
            }
            if (sb.length() > 0) {
                parsedFkfilter = sb.toString();
            }
        }
        return parsedFkfilter;
    }

    public static String wrapFkValue(String value) {
        if (value.startsWith("[") && value.endsWith("]")) { //$NON-NLS-1$//$NON-NLS-2$
            return value;
        }
        return "[" + value + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String unwrapFkValue(String value) {
        if (value.startsWith("[") && value.endsWith("]")) { //$NON-NLS-1$ //$NON-NLS-2$
            if (value.contains("][")) { //$NON-NLS-1$
                return value;
            } else {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    public static boolean isFilterValue(String foreignKeyFilterValue) {
        return (foreignKeyFilterValue.startsWith("\"") && foreignKeyFilterValue.endsWith("\"") || //$NON-NLS-1$//$NON-NLS-2$
        foreignKeyFilterValue.startsWith("'") && foreignKeyFilterValue.endsWith("'")); //$NON-NLS-1$//$NON-NLS-2$
    }

    public static boolean isRelativePath(String foreignKeyFilterValue) {
        return (foreignKeyFilterValue.startsWith(".") || foreignKeyFilterValue.startsWith("..")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static Map<String, String> buildConditionByCriteria(String criteria) {
        Map<String, String> conditionMap = new HashMap<String, String>();
        String[] values = criteria.split("\\$\\$");//$NON-NLS-1$
        for (int i = 0; i < values.length; i++) {

            switch (i) {
            case 0:
                conditionMap.put("Xpath", values[0]);//$NON-NLS-1$
                break;
            case 1:
                conditionMap.put("Operator", values[1]);//$NON-NLS-1$
                break;
            case 2:
                conditionMap.put("Value", values[2].trim());//$NON-NLS-1$
                break;
            case 3:
                conditionMap.put("Predicate", values[3]);//$NON-NLS-1$
                break;
            default:
                break;
            }
        }
        return conditionMap;
    }

    public static String unescapeXml(String value) {
        if (value != null) {
            StringBuilder result = new StringBuilder(value.length());
            int i = 0;
            int n = value.length();
            while (i < n) {
                char charAt = value.charAt(i);
                if (charAt != '&') {
                    result.append(charAt);
                    i++;
                } else {
                    if (value.startsWith("&amp;", i)) { //$NON-NLS-1$
                        result.append('&');
                        i += 5;
                    } else if (value.startsWith("&apos;", i)) { //$NON-NLS-1$
                        result.append('\'');
                        i += 6;
                    } else if (value.startsWith("&quot;", i)) { //$NON-NLS-1$
                        result.append('"');
                        i += 6;
                    } else if (value.startsWith("&lt;", i)) { //$NON-NLS-1$
                        result.append('<');
                        i += 4;
                    } else if (value.startsWith("&gt;", i)) { //$NON-NLS-1$
                        result.append('>');
                        i += 4;
                    }
                }
            }
            return result.toString().replaceAll("\t", "").replaceAll("\n", "").replaceAll("\r", ""); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$//$NON-NLS-6$
        } else {
            return null;
        }
    }
}
