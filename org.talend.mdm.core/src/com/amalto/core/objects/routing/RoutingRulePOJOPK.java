/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.routing;

import com.amalto.core.objects.ObjectPOJOPK;


public class RoutingRulePOJOPK extends ObjectPOJOPK{

	/**
	 * @param pk
	 */
	public RoutingRulePOJOPK(ObjectPOJOPK pk) {
		super(pk.getIds());
	}

	/**
	 * @param name
	 */
	public RoutingRulePOJOPK(String name) {
		super(name);
	}

}
