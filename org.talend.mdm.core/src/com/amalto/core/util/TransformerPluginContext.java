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


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @deprecated - use TransformerV2 package
 * @author bgrieder
 *
 */

public class TransformerPluginContext {
	
	private Map<String, Object> map = Collections.synchronizedMap(new HashMap<String, Object>());
	
	public void put(String key, Object value) {
		map.put(key, value);
	}

	public Object get(String key) {
		return map.get(key);
	}
	
	public Set<String> getKeys() {
		return map.keySet();
	}
	
	public Object remove(String key) {
		return map.remove(key);
	}

}
