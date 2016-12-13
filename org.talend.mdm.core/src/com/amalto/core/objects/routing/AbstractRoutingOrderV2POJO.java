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

import com.amalto.core.metadata.LongString;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;


/**
 * @author bgrieder
 * 
 */
@SuppressWarnings("nls")
public abstract class AbstractRoutingOrderV2POJO extends ObjectPOJO{

    private static final long serialVersionUID = -4752706364468289676L;
    
    public final static int FAILED = 0;
	public final static int ACTIVE = 1;
	public final static int COMPLETED = 2;
	
    protected String name;
    protected int status = ACTIVE;
    protected long timeCreated = System.currentTimeMillis();
    protected long timeScheduled = -1;
    protected long timeLastRunStarted = -1;
    protected long timeLastRunCompleted = -1;
    protected ItemPOJOPK itemPOJOPK = null;
    protected String serviceJNDI = "";
    protected String serviceParameters = "";
    protected String message = "";
    protected RoutingEngineV2POJOPK routingEnginePOJOPK;
    protected String routingEngineToken = null;
    protected String bindingUniverseName = null;
    protected String bindingUserToken = null;
    
    protected AbstractRoutingOrderV2POJO() {
    	super();
    }
    
	protected AbstractRoutingOrderV2POJO(
		String name,
		int status,
		long timeCreated,
		long timeScheduled,
		long timeLastRunStarted,
		long timeLastRunCompleted,
		ItemPOJOPK itemPOJOPK,
		String serviceJNDI,
		String serviceParameters,
		String message,
		RoutingEngineV2POJOPK routingEnginePOJOPK,
		String routingEngineToken) {
		super();
		this.name = name;
		this.status = status;
		this.timeCreated = timeCreated;
		this.timeScheduled = timeScheduled;
		this.timeLastRunStarted = timeLastRunStarted;
		this.timeLastRunCompleted = timeLastRunCompleted;
		this.itemPOJOPK = itemPOJOPK;
		this.serviceJNDI = serviceJNDI;
		this.serviceParameters = serviceParameters;
		this.message = message;
		this.routingEnginePOJOPK = routingEnginePOJOPK;
		this.routingEngineToken = routingEngineToken;
	}

	protected AbstractRoutingOrderV2POJO(
			String name,
			int status,
			long timeCreated,
			long timeScheduled,
			long timeLastRunStarted,
			long timeLastRunCompleted,
			ItemPOJOPK itemPOJOPK,
			String serviceJNDI,
			String serviceParameters,
			String message,
			RoutingEngineV2POJOPK routingEnginePOJOPK,
			String routingEngineToken,
			String bindingUniverseName) {
		    this(name,status,timeCreated,timeScheduled,timeLastRunStarted,timeLastRunCompleted,itemPOJOPK,serviceJNDI,serviceParameters,message,routingEnginePOJOPK,routingEngineToken);
			this.bindingUniverseName=bindingUniverseName;
			
		}
	
	protected AbstractRoutingOrderV2POJO(
			String name,
			int status,
			long timeCreated,
			long timeScheduled,
			long timeLastRunStarted,
			long timeLastRunCompleted,
			ItemPOJOPK itemPOJOPK,
			String serviceJNDI,
			String serviceParameters,
			String message,
			RoutingEngineV2POJOPK routingEnginePOJOPK,
			String routingEngineToken,
			String bindingUniverseName,
			String bindingUserToken) {
		    this(name,status,timeCreated,timeScheduled,timeLastRunStarted,timeLastRunCompleted,itemPOJOPK,serviceJNDI,serviceParameters,message,routingEnginePOJOPK,routingEngineToken,bindingUniverseName);
			this.bindingUserToken=bindingUserToken;
			
		}



	public String getName() {
		return name;
	}

	public void setName(
		String name) {
		this.name = name;
	}

	public int getStatus() {
		return this.status;
	}

	protected void setStatus(int status) {
		this.status = status;
	}

	public long getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(
		long timeCreated) {
		this.timeCreated = timeCreated;
	}

	public long getTimeScheduled() {
		return timeScheduled;
	}

	public void setTimeScheduled(
		long timeScheduled) {
		this.timeScheduled = timeScheduled;
	}

	public long getTimeLastRunStarted() {
		return timeLastRunStarted;
	}

	public void setTimeLastRunStarted(
		long timeLastRunStarted) {
		this.timeLastRunStarted = timeLastRunStarted;
	}

	public long getTimeLastRunCompleted() {
		return timeLastRunCompleted;
	}
	public void setTimeLastRunCompleted(
		long timeLastRunCompleted) {
		this.timeLastRunCompleted = timeLastRunCompleted;
	}
	
	public ItemPOJOPK getItemPOJOPK() {
		return itemPOJOPK;
	}

	public void setItemPOJOPK(
		ItemPOJOPK itemPOJOPK) {
		this.itemPOJOPK = itemPOJOPK;
	}
	
	public String getServiceJNDI() {
		return serviceJNDI;
	}

	public void setServiceJNDI(
		String serviceJNDI) {
		this.serviceJNDI = serviceJNDI;
	}

    @LongString
	public String getServiceParameters() {
		return serviceParameters;
	}

	public void setServiceParameters(
		String serviceParameters) {
		this.serviceParameters = serviceParameters;
	}

    @LongString
	public String getMessage() {
		return message;
	}

	public void setMessage(
		String message) {
		this.message = message;
	}

	public RoutingEngineV2POJOPK getRoutingEnginePOJOPK() {
		return routingEnginePOJOPK;
	}

	public void setRoutingEnginePOJOPK(
		RoutingEngineV2POJOPK routingEnginePOJOPK) {
		this.routingEnginePOJOPK = routingEnginePOJOPK;
	}

	public String getRoutingEngineToken() {
		return routingEngineToken;
	}

	public void setRoutingEngineToken(
		String routingEngineToken) {
		this.routingEngineToken = routingEngineToken;
	}
	
    public String getBindingUniverseName() {
		return bindingUniverseName;
	}

	public void setBindingUniverseName(String bindingUniverseName) {
		this.bindingUniverseName = bindingUniverseName;
	}
	
	public String getBindingUserToken() {
		return this.bindingUserToken;
	}

	public void setBindingUserToken(String bindingUserToken) {
		this.bindingUserToken = bindingUserToken;
	}


	@Override
    public ObjectPOJOPK getPK() {
        return new ObjectPOJOPK(new String[] { name, status + "" });
    }

    public AbstractRoutingOrderV2POJOPK getAbstractRoutingOrderPOJOPK() {
        switch (this.status) {
        case AbstractRoutingOrderV2POJO.COMPLETED:
            return new CompletedRoutingOrderV2POJOPK(name);
        case AbstractRoutingOrderV2POJO.FAILED:
            return new FailedRoutingOrderV2POJOPK(name);
        }
        return null;
    }
}
