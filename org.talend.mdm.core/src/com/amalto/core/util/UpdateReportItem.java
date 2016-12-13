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

public class UpdateReportItem {

	public UpdateReportItem(String path, String oldValue, String newValue) {
		super();
		// TODO Auto-generated constructor stub
		this.newValue = newValue;
		this.oldValue = oldValue;
		this.path = path;
	}
	public UpdateReportItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	String path;
	String oldValue;
	String newValue;
	
	public String getNewValue() {
		return newValue;
	}
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	public String getOldValue() {
		return oldValue;
	}
	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}
