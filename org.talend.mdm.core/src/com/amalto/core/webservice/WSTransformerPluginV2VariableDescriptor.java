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

@XmlType(name="WSTransformerPluginV2VariableDescriptor")
public class WSTransformerPluginV2VariableDescriptor {
    protected java.lang.String variableName;
    protected boolean mandatory;
    protected java.lang.String description;
    protected java.lang.String[] contentTypesRegex;
    protected java.lang.String[] possibleValuesRegex;
    
    public WSTransformerPluginV2VariableDescriptor() {
    }
    
    public WSTransformerPluginV2VariableDescriptor(java.lang.String variableName, boolean mandatory, java.lang.String description, java.lang.String[] contentTypesRegex, java.lang.String[] possibleValuesRegex) {
        this.variableName = variableName;
        this.mandatory = mandatory;
        this.description = description;
        this.contentTypesRegex = contentTypesRegex;
        this.possibleValuesRegex = possibleValuesRegex;
    }
    
    public java.lang.String getVariableName() {
        return variableName;
    }
    
    public void setVariableName(java.lang.String variableName) {
        this.variableName = variableName;
    }
    
    public boolean isMandatory() {
        return mandatory;
    }
    
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }
    
    public java.lang.String getDescription() {
        return description;
    }
    
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    public java.lang.String[] getContentTypesRegex() {
        return contentTypesRegex;
    }
    
    public void setContentTypesRegex(java.lang.String[] contentTypesRegex) {
        this.contentTypesRegex = contentTypesRegex;
    }
    
    public java.lang.String[] getPossibleValuesRegex() {
        return possibleValuesRegex;
    }
    
    public void setPossibleValuesRegex(java.lang.String[] possibleValuesRegex) {
        this.possibleValuesRegex = possibleValuesRegex;
    }
}
