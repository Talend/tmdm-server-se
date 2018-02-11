/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.load.context;

import com.amalto.core.load.Metadata;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 *
 */
class DefaultMetadata implements Metadata {
    private final String taskId = null;

    private final Map<String, String> id = new LinkedHashMap<String, String>();

    private String container = StringUtils.EMPTY;

    private String name = StringUtils.EMPTY;

    private String dmn = StringUtils.EMPTY;

    private String dataClusterName;

    private String[] cachedId;

    public void setContainer(String container) {
        this.container = container;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDmn(String dmn) {
        this.dmn = dmn;
    }

    public String[] getId() {
        if (cachedId == null) {
            Collection<String> values = id.values();
            String[] result = new String[values.size()];
            int i = 0;
            for (String value : values) {
                result[i++] = value;
            }
            cachedId = result;
        }

        return cachedId;
    }

    public String getContainer() {
        return container;
    }

    public String getName() {
        return name;
    }

    public String getDMR() {
        return StringUtils.EMPTY;
    }

    public String getSP() {
        return StringUtils.EMPTY;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getVersion() {
        return String.valueOf(System.currentTimeMillis());
    }

    public String getDMN() {
        return dmn;
    }

    public void setId(String idElementName, String id) {
        this.id.put(idElementName, id.trim());
    }

    public void reset() {
        id.clear();
        cachedId = null;
    }

    public String getDataClusterName() {
        return dataClusterName;
    }

    public void setDataClusterName(String dataClusterName) {
        this.dataClusterName = dataClusterName;
    }
}
