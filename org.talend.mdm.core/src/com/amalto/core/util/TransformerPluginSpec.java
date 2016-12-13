/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

/**
 * 
 * @author bgrieder
 * @deprecated - use TransformerV2 package
 *
 */

public class TransformerPluginSpec {
		
	private String pluginJNDI = null;
	private String description = null;
	private String parameters = null;
	private String input = null;
	private String output = null;
	
	public TransformerPluginSpec() {
		super();
	}
	
	
	public TransformerPluginSpec(String pluginJNDI, String description, String input, String output, String parameters) {
		super();
		this.pluginJNDI = pluginJNDI;
		this.description = description;
		this.parameters = parameters;
		this.input = input;
		this.output = output;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
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

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	
	
	
}
