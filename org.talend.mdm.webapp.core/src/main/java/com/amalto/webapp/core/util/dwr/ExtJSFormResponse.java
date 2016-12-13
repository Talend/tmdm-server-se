/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.util.dwr;

import java.util.HashMap;

public abstract class ExtJSFormResponse {
	
	protected boolean success = false;
	protected HashMap<String, String> errors = new HashMap<String, String>();

    protected Object data;
	protected String message="";
	
	
	protected ExtJSFormResponse(
		boolean success,
		Object data,
		String message,
		HashMap<String, String> fieldErrors) {
		super();
		this.success = success;
		this.data = data;
		this.message = message;
		this.errors = fieldErrors;
	}

}
