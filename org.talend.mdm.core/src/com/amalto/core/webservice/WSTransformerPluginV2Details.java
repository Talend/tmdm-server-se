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

@XmlType(name="WSTransformerPluginV2Details")
public class WSTransformerPluginV2Details {
    protected com.amalto.core.webservice.WSTransformerPluginV2VariableDescriptor[] inputVariableDescriptors;
    protected com.amalto.core.webservice.WSTransformerPluginV2VariableDescriptor[] outputVariableDescriptors;
    protected java.lang.String description;
    protected java.lang.String documentation;
    protected java.lang.String parametersSchema;
    
    public WSTransformerPluginV2Details() {
    }
    
    public WSTransformerPluginV2Details(com.amalto.core.webservice.WSTransformerPluginV2VariableDescriptor[] inputVariableDescriptors, com.amalto.core.webservice.WSTransformerPluginV2VariableDescriptor[] outputVariableDescriptors, java.lang.String description, java.lang.String documentation, java.lang.String parametersSchema) {
        this.inputVariableDescriptors = inputVariableDescriptors;
        this.outputVariableDescriptors = outputVariableDescriptors;
        this.description = description;
        this.documentation = documentation;
        this.parametersSchema = parametersSchema;
    }
    
    public com.amalto.core.webservice.WSTransformerPluginV2VariableDescriptor[] getInputVariableDescriptors() {
        return inputVariableDescriptors;
    }
    
    public void setInputVariableDescriptors(com.amalto.core.webservice.WSTransformerPluginV2VariableDescriptor[] inputVariableDescriptors) {
        this.inputVariableDescriptors = inputVariableDescriptors;
    }
    
    public com.amalto.core.webservice.WSTransformerPluginV2VariableDescriptor[] getOutputVariableDescriptors() {
        return outputVariableDescriptors;
    }
    
    public void setOutputVariableDescriptors(com.amalto.core.webservice.WSTransformerPluginV2VariableDescriptor[] outputVariableDescriptors) {
        this.outputVariableDescriptors = outputVariableDescriptors;
    }
    
    public java.lang.String getDescription() {
        return description;
    }
    
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    public java.lang.String getDocumentation() {
        return documentation;
    }
    
    public void setDocumentation(java.lang.String documentation) {
        this.documentation = documentation;
    }
    
    public java.lang.String getParametersSchema() {
        return parametersSchema;
    }
    
    public void setParametersSchema(java.lang.String parametersSchema) {
        this.parametersSchema = parametersSchema;
    }
}
