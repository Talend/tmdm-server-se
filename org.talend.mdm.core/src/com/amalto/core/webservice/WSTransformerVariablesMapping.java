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

@XmlType(name="WSTransformerVariablesMapping")
public class WSTransformerVariablesMapping {
    protected java.lang.String pipelineVariable;
    protected java.lang.String pluginVariable;
    protected com.amalto.core.webservice.WSTypedContent hardCoding;
    
    public WSTransformerVariablesMapping() {
    }
    
    public WSTransformerVariablesMapping(java.lang.String pipelineVariable, java.lang.String pluginVariable, com.amalto.core.webservice.WSTypedContent hardCoding) {
        this.pipelineVariable = pipelineVariable;
        this.pluginVariable = pluginVariable;
        this.hardCoding = hardCoding;
    }
    
    public java.lang.String getPipelineVariable() {
        return pipelineVariable;
    }
    
    public void setPipelineVariable(java.lang.String pipelineVariable) {
        this.pipelineVariable = pipelineVariable;
    }
    
    public java.lang.String getPluginVariable() {
        return pluginVariable;
    }
    
    public void setPluginVariable(java.lang.String pluginVariable) {
        this.pluginVariable = pluginVariable;
    }
    
    public com.amalto.core.webservice.WSTypedContent getHardCoding() {
        return hardCoding;
    }
    
    public void setHardCoding(com.amalto.core.webservice.WSTypedContent hardCoding) {
        this.hardCoding = hardCoding;
    }
}
