/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.client.mvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PortalProperties implements IsSerializable {

    public static final String KEY_PORTLET_LOCATIONS = "mdm_pref_portletLocations"; //$NON-NLS-1$

    public static final String KEY_COLUMN_NUM = "mdm_pref_columnNum"; //$NON-NLS-1$

    public static final String KEY_AUTO_ONOFFS = "mdm_pref_autoOnOffs"; //$NON-NLS-1$

    public static final String KEY_CHART_SETTINGS = "mdm_pref_chartSettings"; //$NON-NLS-1$

    private Map<String, String> configProperties = new HashMap<String, String>(5);

    public PortalProperties() {
        super();
    }

    // create a deep copy
    public PortalProperties(PortalProperties props) {
        super();
        configProperties = new HashMap<String, String>(props.configProperties);
    }

    public PortalProperties(Map<String, String> props) {
        super();
        for (String name : props.keySet()) {
            add(name, props.get(name));
        }
    }

    public static void populateConfigs(String dataString, List<String> allPortlets, List<String> portletsToCheck,
            Boolean chartsOn, Integer colNum) {
        String[] temp = dataString.split(";"); //$NON-NLS-1$

        String all = temp[0].substring(1, temp[0].length() - 1);
        allPortlets = Arrays.asList(all.split(", ")); //$NON-NLS-1$

        String checks = temp[1].substring(1, temp[1].length() - 1);
        chartsOn = chartsOn.valueOf("true");//(checks.contains("chart_")); //$NON-NLS-1$
        portletsToCheck = Arrays.asList(checks.split(",")); //$NON-NLS-1$

        colNum = Integer.parseInt(temp[2]);
    }

    public void add(String name, String value) {
        configProperties.put(name, value);
    }

    public String get(String name) {
        return configProperties.get(name);
    }

    public Set<String> getKeys() {
        return configProperties.keySet();
    }

    public Map<String, List<Integer>> getPortletToLocations() {
        String portletTolocationsStr = configProperties.get(KEY_PORTLET_LOCATIONS);
        if (portletTolocationsStr != null) {
            String pattern = "((\\w+)=\\[(\\d+),\\s(\\d+)\\],?)"; //$NON-NLS-1$
            RegExp regExp = RegExp.compile(pattern, "g"); //$NON-NLS-1$

            String portletName;
            Map<String, List<Integer>> locs = new HashMap<String, List<Integer>>();

            MatchResult matcher = regExp.exec(portletTolocationsStr);
            while (matcher != null) {
                portletName = matcher.getGroup(2);
                List<Integer> loc = Arrays.asList(Integer.parseInt(matcher.getGroup(3)), Integer.parseInt(matcher.getGroup(4)));
                locs.put(portletName, loc);

                matcher = regExp.exec(portletTolocationsStr);
            }
            return locs;
        } else {
            return null;
        }
    }

    public Integer getColumnNum() {
        String columnNumStr = configProperties.get(KEY_COLUMN_NUM);
        if (columnNumStr == null || columnNumStr.length() == 0) {
            return null;
        } else {
            return Integer.parseInt(columnNumStr);
        }
    }

    public Map<String, Boolean> getAutoRefreshStatus() {
        return parseStringBooleanPair(configProperties.get(KEY_AUTO_ONOFFS));
    }

    private Map<String, Boolean> parseStringBooleanPair(String valuesStr) {
        if (valuesStr != null) {
            String pattern = "((\\w+)=(\\w+),?)"; //$NON-NLS-1$
            RegExp regExp = RegExp.compile(pattern, "g"); //$NON-NLS-1$

            String portletName;
            Map<String, Boolean> stringBooleanPairs = new HashMap<String, Boolean>();

            MatchResult matcher = regExp.exec(valuesStr);
            while (matcher != null) {
                portletName = matcher.getGroup(2);
                Boolean visible = Boolean.parseBoolean(matcher.getGroup(3));
                stringBooleanPairs.put(portletName, visible);

                matcher = regExp.exec(valuesStr);
            }

            return stringBooleanPairs;
        } else {
            return null;
        }
    }

    public Boolean getAutoRefreshStatus(String name) {
        Boolean auto = null;
        String autoRefreshStatusStr = configProperties.get(KEY_AUTO_ONOFFS);
        if (autoRefreshStatusStr != null) {
            String pattern = "((\\w+)=(\\w+),?)"; //$NON-NLS-1$
            RegExp regExp = RegExp.compile(pattern, "g"); //$NON-NLS-1$

            String portletName;

            MatchResult matcher = regExp.exec(autoRefreshStatusStr);
            while (matcher != null && auto == null) {
                portletName = matcher.getGroup(2);

                if (name.equals(portletName)) {
                    auto = Boolean.parseBoolean(matcher.getGroup(3));
                }
                matcher = regExp.exec(autoRefreshStatusStr);
            }
        } else {
            auto = null;
        }
        return auto;
    }

    public Map<String, String> getChartSettings() {
        String charSettingsStr = configProperties.get(KEY_CHART_SETTINGS);
        if (charSettingsStr != null) {
            String pattern = "((\\w+)=(\\w+),?)"; //$NON-NLS-1$
            RegExp regExp = RegExp.compile(pattern, "g"); //$NON-NLS-1$

            Map<String, String> chartSettings = new HashMap<String, String>();

            MatchResult matcher = regExp.exec(charSettingsStr);
            while (matcher != null) {
                chartSettings.put(matcher.getGroup(2), matcher.getGroup(3));

                matcher = regExp.exec(charSettingsStr);
            }

            return chartSettings;
        } else {
            return null;
        }
    }

    public String getChartSetting(String name) {
        String setting = null;
        String autoRefreshStatusStr = configProperties.get(KEY_CHART_SETTINGS);
        if (autoRefreshStatusStr != null) {
            String pattern = "((\\w+)=(\\w+),?)"; //$NON-NLS-1$
            RegExp regExp = RegExp.compile(pattern, "g"); //$NON-NLS-1$

            String portletName;

            MatchResult matcher = regExp.exec(autoRefreshStatusStr);
            while (matcher != null && setting == null) {
                portletName = matcher.getGroup(2);

                if (name.equals(portletName)) {
                    setting = matcher.getGroup(3);
                }
                matcher = regExp.exec(autoRefreshStatusStr);
            }
        } else {
            setting = null;
        }
        return setting;
    }

    public void disableAutoRefresh(Set<String> portletNames) {
        for (String name : portletNames) {
            add(KEY_AUTO_ONOFFS, name, ((Boolean) false).toString());
        }
    }

    public void add(String key, String portletName, String value) {
        String configs = null;
        if (key.equals(KEY_AUTO_ONOFFS)) {
            configs = configProperties.get(KEY_AUTO_ONOFFS);
        } else if (key.equals(KEY_CHART_SETTINGS)) {
            configs = configProperties.get(KEY_CHART_SETTINGS);
        } else {
            assert (false);
        }
        configs = updateConfigs(configs, portletName, value);
        configProperties.put(key, configs);
    }

    private String updateConfigs(String original, String name, String value) {
        String result = null;
        String newPair = name + "=" + value; //$NON-NLS-1$
        if (original != null && original.length() > 0) {
            int start = original.indexOf(name);
            int end = 0;
            if (start != -1) {
                end = original.indexOf(',', start);
                if (end == -1) {
                    end = original.indexOf('}', start);
                }
                String pair = original.substring(start, end);

                result = original.replace(pair, newPair);
            } else {
                String temp = ", " + newPair + "}"; //$NON-NLS-1$ //$NON-NLS-2$
                result = original.replace("}", temp); //$NON-NLS-1$
            }
        } else {
            result = "{" + newPair + "}"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;

    }

}
