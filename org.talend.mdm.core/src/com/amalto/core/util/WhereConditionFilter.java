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

import java.util.Map;

import com.amalto.xmlserver.interfaces.WhereCondition;

public abstract class WhereConditionFilter {
	
	protected Map context;
	
	public WhereConditionFilter(Map context) {
		this.context=context;
	}
	
	public abstract void doFilter(WhereCondition ws);

}
