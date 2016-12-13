/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox;

import java.util.HashMap;
import java.util.Map;

public class JobInfo {

    private final String name;

    private final String version;

    private String classpath;

    private String mainClass;

    private String contextStr;

    private final Map<String, String> defaultParamMap;

    public JobInfo(String name, String version) {
        super();
        this.name = name;
        this.version = version;
        this.classpath = "";//$NON-NLS-1$
        defaultParamMap = new HashMap<String, String>();
    }

    public void addParam(String key, String value) {
        defaultParamMap.put(key, value);
    }

    public Map<String, String> getDefaultParamMap() {
        return defaultParamMap;
    }

    public String getName() {
        return name;
    }

    String getVersion() {
        return version;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getContextStr() {
        return contextStr;
    }

    public void setContextStr(String cxt) {
        contextStr = cxt == null ? "Default" : cxt; //$NON-NLS-1$
    }

    public String toString() {
        return "Name:" + name  //$NON-NLS-1$
                + ";Version:" + version  //$NON-NLS-1$
                + ";Classpath:" + classpath  //$NON-NLS-1$
                + ";Main class:" + mainClass  //$NON-NLS-1$
                + ";Param:" + defaultParamMap;  //$NON-NLS-1$
    }

    public int hashCode() {
        return (this.getName() + "_" + this.getVersion()).hashCode(); //$NON-NLS-1$
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!o.getClass().equals(this.getClass())) {
            return false;
        }
        JobInfo other = (JobInfo) o;
        return (this.getName() + "_" + this.getVersion()).equals((other.getName() + "_" + other.getVersion())); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
