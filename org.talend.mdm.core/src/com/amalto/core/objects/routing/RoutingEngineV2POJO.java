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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;



/**
 * @author bgrieder
 * 
 */
public class RoutingEngineV2POJO extends ObjectPOJO{
   

	
    public static final int STOPPED = 1;
    public static final int  RUNNING = 2;
    public static final int  SUSPENDED = 3;
	
	private static int _NUMBER_OF_EXECUTORS_ = 20;
	private static long _MAX_EXECUTION_TIME_MILLIS_ = 1000*60*5; //5 minutes default;
	private static long _RUN_PERIOD_MILLIS_ = 2500; //3 seconds
	/**
	 * The minimum frequency between two runs
	 */
	public final static long _MIN_PERIOD_MILLIS_ = 150;
	
	static {
		Properties conf = MDMConfiguration.getConfiguration();
		//Number of simultaneous executors
		String executors = conf.getProperty("routing.engine.executors");
		if (executors != null) {
    		try {
    			_NUMBER_OF_EXECUTORS_ = Integer.valueOf(executors);
    		} catch (NumberFormatException e) {
    			String err="ERROR SYSTRACE. Routing Engine: value of number of routing.engine.executors '"+executors+"' incorrect in configuration"+
    							(e.getMessage()!=null ? ": "+e.getMessage() : "")+". Defaulting to "+_NUMBER_OF_EXECUTORS_;
    			org.apache.log4j.Logger.getLogger(RoutingEngineV2POJO.class).info(err,e);
    		}
		}
		//Max Execution time
		String executionTime = conf.getProperty("routing.engine.max.execution.time.millis");
		if (executionTime != null) {
    		try {
    			_MAX_EXECUTION_TIME_MILLIS_ = Long.valueOf(executionTime);
    		} catch (NumberFormatException e) {
    			String err="ERROR SYSTRACE. Routing Engine: value of routing.engine.max.execution.time.millis '"+executionTime+"' incorrect in configuration"+
    							(e.getMessage()!=null ? ": "+e.getMessage() : "")+". Defaulting to "+_MAX_EXECUTION_TIME_MILLIS_+" milliseconds";
    			org.apache.log4j.Logger.getLogger(RoutingEngineV2POJO.class).info(err,e);
    		}
		}
		//Run Period
		String runPeriod = conf.getProperty("routing.engine.run.period.millis");
		if (runPeriod != null) {
    		try {
    			_RUN_PERIOD_MILLIS_ = Long.valueOf(runPeriod);
    		} catch (NumberFormatException e) {
    			String err="ERROR SYSTRACE. Routing Engine: value of number of routing.engine.run.period.millis '"+runPeriod+"' incorrect in configuration"+
    							(e.getMessage()!=null ? ": "+e.getMessage() : "")+". Defaulting to "+_MAX_EXECUTION_TIME_MILLIS_+" milliseconds";
    			org.apache.log4j.Logger.getLogger(RoutingEngineV2POJO.class).info(err,e);
    		}
		}

	}
	
//    private String name;
    //Routing Engines run by default
    //private int status = RUNNING;
	private int status = STOPPED;
    //private int maxNumberOfExecutors = 1;
//    private long maxExecutionTimeMillis = _MAX_EXECUTION_TIME_MILLIS_; 
    
    private transient Map<String, RoutingEngineV2ExecutorPOJO> executors = Collections.synchronizedMap(new HashMap<String,RoutingEngineV2ExecutorPOJO>(_NUMBER_OF_EXECUTORS_));
    private transient long lastDeadRoutingOrdersSweep = 0;
    
    
    //singleton
    
    private static RoutingEngineV2POJO instance = null;
    
    public static RoutingEngineV2POJO getInstance() {
    	if (instance == null) {
    		instance = new RoutingEngineV2POJO();
    	}
    	return instance;
    }
    
    
    private RoutingEngineV2POJO() {super();}
    


	public int getStatus() {
		return status;
	}

	public void setStatus(
		int status) {
		this.status = status;
	}

	public int getMaxNumberOfExecutors() {
		return _NUMBER_OF_EXECUTORS_;
	}

	public void setMaxNumberOfExecutors(int maxNumberOfExecutors) {
		_NUMBER_OF_EXECUTORS_ = maxNumberOfExecutors;
	}

	public Map<String, RoutingEngineV2ExecutorPOJO> getExecutors() {
		return executors;
	}

	public void setExecutors(
		HashMap<String, RoutingEngineV2ExecutorPOJO> executors) {
		this.executors = Collections.synchronizedMap(executors);
	}
	
	public long getMaxExecutionTimeMillis() {
		return _MAX_EXECUTION_TIME_MILLIS_;
	}

	public void setMaxExecutionTimeMillis(long consideredDeadMillis) {
		_MAX_EXECUTION_TIME_MILLIS_ = consideredDeadMillis;
	}
	

	public long getRunPeriodMillis() {
		return _RUN_PERIOD_MILLIS_;
	}

	public void setRunPeriodMillis(long runPeriodMillis) {
		_RUN_PERIOD_MILLIS_ = Math.max(_MIN_PERIOD_MILLIS_, runPeriodMillis);
	}
	
	public long getMinRunPeriodMillis() {
		return _MIN_PERIOD_MILLIS_;
	}
	

	public long getLastDeadRoutingOrdersSweep() {
		return lastDeadRoutingOrdersSweep;
	}

	public void setLastDeadRoutingOrdersSweep(
		long lastDeadRoutingOrdersSweep) {
		this.lastDeadRoutingOrdersSweep = lastDeadRoutingOrdersSweep;
	}

	@Override
    public ObjectPOJOPK getPK() {
    	return new ObjectPOJOPK("ROUTING ENGINE");
    }
}
