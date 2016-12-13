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

import java.util.HashMap;

public class RoleSpecification {
	private boolean isAdmin = false;
	//the key is the name of the object instance
	private HashMap<String, RoleInstance> instances = new HashMap<String, RoleInstance>();
	public HashMap<String, RoleInstance> getInstances() {
		return instances;
	}
	public void setInstances(HashMap<String, RoleInstance> instances) {
		this.instances = instances;
	}
	public boolean isAdmin() {
		return isAdmin;
	}
	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

}
