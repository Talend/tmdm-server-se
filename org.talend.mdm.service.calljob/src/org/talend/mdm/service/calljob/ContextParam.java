/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.service.calljob;


public class ContextParam {
	String name;
	String value;
	boolean isItemXML;

	public ContextParam(String key, String value, boolean isItemXML){
		this.name=key;
		this.value=value;
		this.isItemXML=isItemXML;
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

	public boolean isItemXML() {
		return isItemXML;
	}

	public void setItemXML(boolean isItemXML) {
		this.isItemXML = isItemXML;
	}
}
