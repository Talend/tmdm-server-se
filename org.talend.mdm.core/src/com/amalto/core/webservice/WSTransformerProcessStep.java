/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.webservice;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="WSTransformerProcessStep")
public class WSTransformerProcessStep {
    protected java.lang.String pluginJNDI;
    protected java.lang.String description;
    protected java.lang.String parameters;
    protected com.amalto.core.webservice.WSTransformerVariablesMapping[] inputMappings;
    protected com.amalto.core.webservice.WSTransformerVariablesMapping[] outputMappings;
    protected java.lang.Boolean disabled;
    
    public WSTransformerProcessStep() {
    }
    
    public WSTransformerProcessStep(java.lang.String pluginJNDI, java.lang.String description, java.lang.String parameters, com.amalto.core.webservice.WSTransformerVariablesMapping[] inputMappings, com.amalto.core.webservice.WSTransformerVariablesMapping[] outputMappings, java.lang.Boolean disabled) {
        this.pluginJNDI = pluginJNDI;
        this.description = description;
        this.parameters = parameters;
        this.inputMappings = inputMappings;
        this.outputMappings = outputMappings;
        this.disabled = disabled;
    }
    
    public java.lang.String getPluginJNDI() {
        return pluginJNDI;
    }
    
    public void setPluginJNDI(java.lang.String pluginJNDI) {
        this.pluginJNDI = pluginJNDI;
    }
    
    public java.lang.String getDescription() {
        return description;
    }
    
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    public java.lang.String getParameters() {
        return parameters;
    }
    
    public void setParameters(java.lang.String parameters) {
        this.parameters = parameters;
    }
    
    public com.amalto.core.webservice.WSTransformerVariablesMapping[] getInputMappings() {
        return inputMappings;
    }
    
    public void setInputMappings(com.amalto.core.webservice.WSTransformerVariablesMapping[] inputMappings) {
        this.inputMappings = inputMappings;
    }
    
    public com.amalto.core.webservice.WSTransformerVariablesMapping[] getOutputMappings() {
        return outputMappings;
    }
    
    public void setOutputMappings(com.amalto.core.webservice.WSTransformerVariablesMapping[] outputMappings) {
        this.outputMappings = outputMappings;
    }
    
    public java.lang.Boolean getDisabled() {
        return disabled;
    }
    
    public void setDisabled(java.lang.Boolean disabled) {
        this.disabled = disabled;
    }
}
