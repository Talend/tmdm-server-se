/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.transformers.util;

import com.amalto.core.metadata.LongString;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * 
 * @author bgrieder
 */
public class TransformerProcessStep implements Serializable{
		
	private String pluginJNDI = null;
	private String description = null;
	private String parameters = null;
	/**
	 * Compiled Parameters for internal use of the plugin<br>
	 * They could be a Base64 serialized object
	 */
	private String compiledParameters = null;
	private ArrayList<TransformerVariablesMapping> inputMappings = new ArrayList<TransformerVariablesMapping>();
	private ArrayList<TransformerVariablesMapping> outputMappings= new ArrayList<TransformerVariablesMapping>();;
	private boolean disabled = false;
	
	public TransformerProcessStep() {
		super();
	}
	
	
	public TransformerProcessStep( 
			String description, 
			String pluginJNDI,
			ArrayList<TransformerVariablesMapping> inputMappings ,
			ArrayList<TransformerVariablesMapping> outputMappings , 
			String parameters) {
		super();
		this.pluginJNDI = pluginJNDI;
		this.description = description;
		this.parameters = parameters;
		this.inputMappings = inputMappings;
		this.outputMappings = outputMappings;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

    @LongString
	public String getParameters() {
		return parameters;
	}
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	
	public String getPluginJNDI() {
		return pluginJNDI;
	}
	public void setPluginJNDI(String pluginJNDI) {
		this.pluginJNDI = pluginJNDI;
	}

	public ArrayList<TransformerVariablesMapping> getInputMappings() {
		return inputMappings;
	}
	public void setInputMappings(ArrayList<TransformerVariablesMapping> inputMappings) {
		this.inputMappings = inputMappings;
	}

	public ArrayList<TransformerVariablesMapping> getOutputMappings() {
		return outputMappings;
	}
	public void setOutputMappings(ArrayList<TransformerVariablesMapping> outputMappings) {
		this.outputMappings = outputMappings;
	}

	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
    @LongString
	public String getCompiledParameters() {
		return compiledParameters;
	}
	/**
	 * Compiled Parameters are for internal use of the plugin<br>
	 * They could be a Base64 serialized object
	 */
	public void setCompiledParameters(String compiledParameters) {
		this.compiledParameters = compiledParameters;
	}
	
}
