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


public class FailedRoutingOrderV2POJOPK extends AbstractRoutingOrderV2POJOPK {

	public FailedRoutingOrderV2POJOPK(ObjectPOJOPK pk) {
		super(pk.getIds()[0],AbstractRoutingOrderV2POJO.FAILED);
	}

	public FailedRoutingOrderV2POJOPK(String name) {
		super(name,AbstractRoutingOrderV2POJO.FAILED);
	}


}
