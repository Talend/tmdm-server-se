/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.role;

import com.amalto.core.objects.ObjectPOJOPK;


public class RolePOJOPK extends ObjectPOJOPK{
	
	public RolePOJOPK(ObjectPOJOPK pk) {
		super(pk.getIds());
	}
	
	public RolePOJOPK(String name) {
		super(name);
	}

}
