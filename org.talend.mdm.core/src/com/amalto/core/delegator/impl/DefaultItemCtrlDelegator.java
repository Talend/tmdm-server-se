/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.delegator.impl;

import java.util.ArrayList;

import com.amalto.core.delegator.IItemCtrlDelegator;
import com.amalto.core.objects.view.ViewPOJOPK;
import com.amalto.xmlserver.interfaces.IWhereItem;


public class DefaultItemCtrlDelegator extends IItemCtrlDelegator {

	@Override
	protected ArrayList<IWhereItem> getViewWCFromRole(ViewPOJOPK viewPOJOPK)
			throws Exception {
		return null;
	}	
}
