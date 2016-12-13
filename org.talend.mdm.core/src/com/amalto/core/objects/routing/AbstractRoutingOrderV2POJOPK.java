/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.routing;

import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.util.XtentisException;


public abstract class AbstractRoutingOrderV2POJOPK extends ObjectPOJOPK {

	public AbstractRoutingOrderV2POJOPK(ObjectPOJOPK pk) {
		super(pk.getIds());
	}
	
	public AbstractRoutingOrderV2POJOPK(String name, int status) {
		super(new String[] {name, status+""});
	}
	
	public String getName() {
		return getIds()[0];
	}
	
	public int getStatus() {
		return Integer.valueOf(getIds()[1]).intValue();
	}

    public Class<? extends AbstractRoutingOrderV2POJO> getRoutingOrderClass() throws XtentisException{
        switch (getStatus()) {
           	case AbstractRoutingOrderV2POJO.COMPLETED:
           		return CompletedRoutingOrderV2POJO.class;
           	case AbstractRoutingOrderV2POJO.FAILED:
           		return FailedRoutingOrderV2POJO.class;
           	default:
           		String err= "Unknown Routing Order Class :"+getStatus();           		
           		throw new XtentisException(err);        		
        }
    }

	
}
