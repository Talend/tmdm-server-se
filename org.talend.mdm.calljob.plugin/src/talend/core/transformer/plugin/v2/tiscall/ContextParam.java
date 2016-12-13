/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package talend.core.transformer.plugin.v2.tiscall;


public class ContextParam {
    private String name;

    private String value;

    private boolean isPipelineVariableName = false;

    public boolean isPipelineVariableName() {
        return isPipelineVariableName;
    }

    public void setPipelineVariableName(boolean isPipelineVariableName) {
        this.isPipelineVariableName = isPipelineVariableName;
    }

    public ContextParam(String key, String value, boolean isPipeline) {
        this.name = key;
        this.value = value;
        this.isPipelineVariableName = isPipeline;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
