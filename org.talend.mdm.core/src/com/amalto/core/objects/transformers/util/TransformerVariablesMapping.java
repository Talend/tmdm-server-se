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

import java.io.Serializable;


public class TransformerVariablesMapping implements Serializable {
	String pipelineVariable;
	String pluginVariable;
	TypedContent hardCoding;
	
	public TypedContent getHardCoding() {
		return hardCoding;
	}
	public void setHardCoding(TypedContent hardCoding) {
		this.hardCoding = hardCoding;
	}
	public String getPipelineVariable() {
		return pipelineVariable;
	}
	public void setPipelineVariable(String pipelineVariable) {
		this.pipelineVariable = pipelineVariable;
	}
	public String getPluginVariable() {
		return pluginVariable;
	}
	public void setPluginVariable(String pluginVariable) {
		this.pluginVariable = pluginVariable;
	}
	
	
}
