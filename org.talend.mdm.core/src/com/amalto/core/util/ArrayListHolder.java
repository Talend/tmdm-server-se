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

import java.util.ArrayList;
import java.util.List;

/**
 * Used to facilitate Castor marshalling/unmarshalling<br/> 
 * Castor has issues with variables of the type like HashMap<String, ArrayList<String>>
 * This can be replaced using HashMap<String, ArrayListHolder<String>>
 * @author Bruno Grieder
 * @param <T>
 *
 */
public class ArrayListHolder<T> {
	
	private  ArrayList<T> list = new ArrayList<T>();

	public ArrayList<T> getList() {
		return list;
	}

	public void setList(ArrayList<T> list) {
		this.list = list;
	}

	
	public ArrayListHolder() {
	}
	
	public ArrayListHolder(List<T> list) {
		this.list = new ArrayList<T>(list);
	}
	
	

}
