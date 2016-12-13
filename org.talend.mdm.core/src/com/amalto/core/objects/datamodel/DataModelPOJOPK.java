/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.datamodel;

import com.amalto.core.objects.ObjectPOJOPK;


public class DataModelPOJOPK extends ObjectPOJOPK{
	
	/**
	 * @param pk
	 */
	public DataModelPOJOPK(ObjectPOJOPK pk) {
		super(pk.getIds());
	}
	
	/**
	 * @param name
	 */
	public DataModelPOJOPK(String name) {
		super(name);
	}

}
