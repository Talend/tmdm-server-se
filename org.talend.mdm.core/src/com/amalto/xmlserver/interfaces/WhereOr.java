/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.xmlserver.interfaces;

import java.util.List;

public class WhereOr extends WhereLogicOperator {

	public WhereOr() {
		super(WhereLogicOperator.OR);
	}
	
	public WhereOr(List<IWhereItem> whereItems) {
		super(WhereLogicOperator.OR, whereItems);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return super.toString();
	}

}
