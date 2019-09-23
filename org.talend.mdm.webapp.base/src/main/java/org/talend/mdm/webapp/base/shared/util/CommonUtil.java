/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * created by yjli on 2013-8-1 Detailled comment
 *
 */
public class CommonUtil {

    public static final String FN_PREFIX = "fn:"; //$NON-NLS-1$

    public static final String XPATH_PREFIX = "xpath:"; //$NON-NLS-1$

    public static final String XPATH_STR = "Xpath"; //$NON-NLS-1$

    public static final String OPERATOR_STR = "Operator"; //$NON-NLS-1$

    public static final String VALUE_STR = "Value"; //$NON-NLS-1$

    public static final String PREDICATE_STR = "Predicate"; //$NON-NLS-1$

    public static final String DOLLAR_DELIMITER = "$$"; //$NON-NLS-1$

    public static final String EMPTY = ""; //$NON-NLS-1$

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
            result.append((i > 0) ? separator : EMPTY);
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
            result.append((i > 0) ? ";" : EMPTY); //$NON-NLS-1$
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
        String parsedFkfilter = EMPTY;
        if (conditions.size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (Map<String, String> map : conditions) {
                if (map.size() > 0) {
                    String xpath = map.get(XPATH_STR) == null ? EMPTY : map.get(XPATH_STR);
                    String operator = map.get(OPERATOR_STR) == null ? EMPTY : map.get(OPERATOR_STR);
                    String value = map.get(VALUE_STR) == null ? EMPTY : map.get(VALUE_STR);
                    String predicate = map.get(PREDICATE_STR) == null ? EMPTY : map.get(PREDICATE_STR);
                    sb.append(xpath).append(DOLLAR_DELIMITER).append(operator).append(DOLLAR_DELIMITER).append(value);
                    sb.append(DOLLAR_DELIMITER).append(predicate).append("#");//$NON-NLS-1$
                }
            }
            if (sb.length() > 0) {
                parsedFkfilter = sb.toString();
            }
        }
        return parsedFkfilter;
    }

    public static boolean isWrapedFkValue(String value) {
        return value.startsWith("[") && value.endsWith("]"); // $NON-NLS-1$//$NON-NLS-2$
    }

    public static String wrapFkValue(String value) {
        if (isWrapedFkValue(value)) {
            return value;
        }
        return "[" + value + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String unwrapFkValue(String value) {
        if (isWrapedFkValue(value)) {
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

    public static boolean isFunction(String foreignKeyFilterValue) {
        return foreignKeyFilterValue.startsWith(FN_PREFIX);
    }

    /**
     * Return the true if foreignKeyFilter contains 'xpath:', fielder's xpath and or a releative path
     * eg:
     *     fn:concat('hello', 'world!!!')") ==> false
     *     fn:concat(\"xpath:Product/Name\", \"xpath:Product/Description\")")  ==> true
     *     fn:concat(\"xpath:/Product/Name\", \"xpath:/Product/Description\")")  ==> true
     *     fn:concat(\"/Product/Name\", \"/Product/Description\")")  ==> false
     *     fn:concat(\"xpath:Product/Name\", \" s\")")  ==> true
     *     fn:string-length(\"xpath:Product/Name\") > 3\")")  ==> true
     *     fn:string-length(\"xpath:/Product/Name\") > 3\")")  ==> true
     *     fn:string-length(\"/BasicVisibleRuleWithFunctionXPath/name\") > 5")  ==> false
     *     fn:matches(\"xpath:../Name\" ,\"test\")")  ==> true
     *     fn:starts-with(\"xpath:Product/Name\",\"s\")")  ==> true
     *     fn:starts-with(\"xpath:/Product/Name\",\"s\")")  ==> true
     *     fn:starts-with(\"/Product/Name\",\"s\")")  ==> false
     *     fn:abs(xpath:Product/Name)")  ==> true
     *     fn:abs(xpath:/Product/Name)")  ==> true
     *     fn:abs(/Product/Name)")  ==> true
     * @param foreignKeyFilterValue
     * @return
     */
    public static boolean containsXPath(String foreignKeyFilterValue) {
        if (foreignKeyFilterValue.contains(XPATH_PREFIX)) {
            return true;
        }
        String filter = foreignKeyFilterValue
                .substring(foreignKeyFilterValue.lastIndexOf("(") + 1, foreignKeyFilterValue.indexOf(")")); //$NON-NLS-1$  //$NON-NLS-2$
        String[] filters = filter.split(","); //$NON-NLS-1$
        for (String filterContent : filters) {
            filterContent = filterContent.trim();
            if (filterContent.startsWith("/") || filterContent.startsWith(".") //$NON-NLS-1$ //$NON-NLS-2$
                    || filterContent.startsWith("..")) { //$NON-NLS-1$
                return true;
            }
        }
        return false;
    }

    public static Map<String, String> buildConditionByCriteria(String criteria) {
        Map<String, String> conditionMap = new HashMap<String, String>();
        String[] values = criteria.split("\\$\\$");//$NON-NLS-1$
        for (int i = 0; i < values.length; i++) {

            switch (i) {
            case 0:
                conditionMap.put(XPATH_STR, values[0]);
                break;
            case 1:
                conditionMap.put(OPERATOR_STR, values[1]);
                break;
            case 2:
                conditionMap.put(VALUE_STR, values[2].trim());
                break;
            case 3:
                conditionMap.put(PREDICATE_STR, values[3]);
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

    /**
     * Parse the xpath in the function content
     * eg:
     *     fn:string-length("xpath:/Product/Name") > 3") ==> key=Product/Name,value=Product/Name
     *     fn:concat("xpath:Product/Name", "xpath:Product/Description") ==> key=Product/Name,value=Product/Name & key=Product/Description,value=Product/Description
     *     fn:concat("xpath:Product/Name") ==> key=Product/Name,value=Product/Name
     *     fn:concat("xpath:Product/Name", " s") ==> key=Product/Name,value=Product/Name
     *     fn:starts-with("xpath:Product/Name","s")  ==> key=Product/Name,value=Product/Name
     *     fn:matches("xpath:../Name" ,"test")  ==> key=../Name,valye=../Name
     *     fn:abs(xpath:Product/Price) ==> key=xpath:Product/Price, value=Product/Price
     *     fn:abs(/Product/Price) ==> key=/Product/Price, value=Product/Price
     * @param function the parse function
     * @return the map contains origin xpath and pure xpath
     */
    public static Map<String, String> getArgumentsWithXpath(String function) {
        Map<String, String> arguments = new HashMap<String, String>();
        RegExp reg = RegExp.compile("\\((.*)\\)"); //$NON-NLS-1$
        MatchResult matchResult = reg.exec(function);
        String value = EMPTY;
        if (matchResult != null) {
            value = matchResult.getGroup(0);
        }
        RegExp regExp = RegExp
                .compile("((xpath:(([a-zA-Z]*)|((\\.)+)))|/([a-zA-Z]*))/(([a-zA-Z]*)/*)*", "g"); //$NON-NLS-1$ //$NON-NLS-2$
        MatchResult matcher = regExp.exec(value);
        while (matcher != null) {
            String xpathValue = matcher.getGroup(0);
            if (xpathValue.startsWith(XPATH_PREFIX)) {
                String path = xpathValue.replace(XPATH_PREFIX, EMPTY);
                if (path.startsWith("/")) { //$NON-NLS-1$
                    path = path.substring(1);
                }
                arguments.put(xpathValue, path);
            } else if (xpathValue.startsWith("/")) { //$NON-NLS-1$
                arguments.put(xpathValue, xpathValue.substring(1));
            }
            matcher = regExp.exec(value);
        }
        return arguments;
    }
}
