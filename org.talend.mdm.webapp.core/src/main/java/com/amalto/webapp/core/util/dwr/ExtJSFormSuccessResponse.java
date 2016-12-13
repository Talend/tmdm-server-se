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


public class ExtJSFormSuccessResponse extends ExtJSFormResponse{

	public ExtJSFormSuccessResponse(String message) {
		super(true,null,message,null);
	}
	
	public ExtJSFormSuccessResponse(String message, Object data) {
		super(
			true,
			data == null ? new HashMap<String, String>() : data,
			message,
			null
		);
	}

}
