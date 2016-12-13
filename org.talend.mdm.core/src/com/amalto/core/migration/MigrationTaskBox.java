/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.migration;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.amalto.core.objects.marshalling.MarshallingFactory;

public class MigrationTaskBox implements  Serializable{
	
	private static final long serialVersionUID = 5658917963695194606L;

	public MigrationTaskBox() {
	
	}
	

	public MigrationTaskBox(Map<String, Boolean> handlerMap) {
		super();
		this.handlerMap = handlerMap;
	}



	private Map<String, Boolean> handlerMap = new HashMap<String, Boolean>();

	public Map<String, Boolean> getHandlerMap() {
		return handlerMap;
	}

	public void setHandlerMap(Map<String, Boolean> handlerMap) {
		this.handlerMap = handlerMap;
	}
	
	 
	public String toString() {
		try {
			StringWriter sw = new StringWriter();
			MarshallingFactory.getInstance().getMarshaller(this.getClass()).marshal(this, sw);
			return sw.toString();
	    } catch (Exception e) {
		    org.apache.log4j.Logger.getLogger(this.getClass()).error(e);
		    return null;
	    } 
	}
}
