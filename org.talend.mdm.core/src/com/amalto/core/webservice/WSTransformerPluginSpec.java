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

@XmlType(name="WSTransformerPluginSpec")
public class WSTransformerPluginSpec {
    protected java.lang.String pluginJNDI;
    protected java.lang.String description;
    protected java.lang.String input;
    protected java.lang.String output;
    protected java.lang.String parameters;
    
    public WSTransformerPluginSpec() {
    }
    
    public WSTransformerPluginSpec(java.lang.String pluginJNDI, java.lang.String description, java.lang.String input, java.lang.String output, java.lang.String parameters) {
        this.pluginJNDI = pluginJNDI;
        this.description = description;
        this.input = input;
        this.output = output;
        this.parameters = parameters;
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
    
    public java.lang.String getInput() {
        return input;
    }
    
    public void setInput(java.lang.String input) {
        this.input = input;
    }
    
    public java.lang.String getOutput() {
        return output;
    }
    
    public void setOutput(java.lang.String output) {
        this.output = output;
    }
    
    public java.lang.String getParameters() {
        return parameters;
    }
    
    public void setParameters(java.lang.String parameters) {
        this.parameters = parameters;
    }
}
