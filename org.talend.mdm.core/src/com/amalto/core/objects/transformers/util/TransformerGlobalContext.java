/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.transformers.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.amalto.core.objects.backgroundjob.BackgroundJobPOJO;
import com.amalto.core.objects.transformers.TransformerV2POJO;
import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;


public class TransformerGlobalContext extends TransformerContext{
	
	private final static String PLUGIN_CONTEXTS = "com.amalto.core.util.TransformerGlobalContext.PLUGIN_CONTEXTS";
	private final static String TRANSFORMER_POJO = "com.amalto.core.util.TransformerGlobalContext.TRANSFORMER_POJO";
	
	private static final String EXECUTE_CALLBACK = "com.amalto.core.util.TransformerGlobalContext.EXECUTE_CALLBACK";
	private static final String PROCESS_CALLBACK = "com.amalto.core.util.TransformerGlobalContext.PROCESS_CALLBACK";
	private static final String INSTANTIATED_PLUGINS = "com.amalto.core.util.TransformerGlobalContext.INSTANTIATED_PLUGINS";
	private static final String COUNTER = "com.amalto.core.util.TransformerGlobalContext.COUNTER";
	private static final String TRANSFOMER_DONE = "com.amalto.core.util.TransformerGlobalContext.TRANSFORMER_DONE";
	private static final String DECISION_TABLE = "com.amalto.core.util.TransformerGlobalContext.DECISION_TABLE";
	private static final String TIME = "com.amalto.core.util.TransformerGlobalContext.TIME";
	private static final String JOB = "com.amalto.core.util.TransformerGlobalContext.JBO";
	private static final String USERTOKEN = "com.amalto.core.util.TransformerGlobalContext.USERTOKEN";

	
	//private HashMap<Integer, TransformerPluginContext> pluginContexts;
	//private TransformerPOJO transformerPOJO = null;

	
	public TransformerGlobalContext(TransformerV2POJOPK transformerPOJOPK) {
		super(transformerPOJOPK);
		initialize();
	}
	
	/**
	 * Create a Global Context from an existing context
	 * @param context  - An exitsing Transformer Context
	 */
	public TransformerGlobalContext(TransformerContext context) {
		super(context.getTransformerV2POJOPK());
		this.setContextMap(context.getContextMap());
		this.setPipeline(context.getPipeline());
		this.setProjectedPKs(context.getProjectedPKs());
		initialize();
	}	

	
	/**
	 * Initilializes the Global COntext with default values
	 */
	private void initialize() {
		this.setPluginContexts(Collections.synchronizedMap(new HashMap<Integer, TransformerPluginContext>()));
		setStartTime(System.currentTimeMillis());
		setTransformerDone(false);
		setIterationNumber(0);
		setInstantiatedPlugins(new LinkedHashMap<Integer, TransformerPluginV2LocalInterface>());
		setDecisionTable(new HashMap<String, String>());		
	}

	
	public TransformerPluginContext getTransformerPluginContext(int pluginNumber) throws XtentisException{
		TransformerPluginContext pluginContext = getPluginContexts().get(new Integer(pluginNumber));
		if (pluginContext == null) {
			pluginContext = new TransformerPluginContext(this, pluginNumber);
			getPluginContexts().put(new Integer(pluginNumber), pluginContext);
		}
		return pluginContext;
	}
	
	
	public TransformerV2POJO getTransformerPOJO() throws XtentisException {
		TransformerV2POJO transformerPOJO = (TransformerV2POJO)this.get(TRANSFORMER_POJO);
		if (transformerPOJO == null) {
	    	try {
				transformerPOJO = Util.getTransformerV2CtrlLocal().getTransformer(this.getTransformerV2POJOPK());
			} catch (Exception e) {
				String err = 
					"Unable to get the transformer "+this.getTransformerV2POJOPK().getUniqueId()+" from the global Context: "+
					e.getClass().getName()+": "+e.getMessage();
				org.apache.log4j.Logger.getLogger(this.getClass()).error("executeNextPlugin() "+err);
				throw new XtentisException(err);
			}
			this.put(TRANSFORMER_POJO, transformerPOJO);
		}
		return transformerPOJO;
	}
	
	
	private Map<Integer, TransformerPluginContext> getPluginContexts() {
		return (Map<Integer, TransformerPluginContext>)this.get(PLUGIN_CONTEXTS);
	}
	private void setPluginContexts(Map<Integer, TransformerPluginContext> contexts) {
		this.put(PLUGIN_CONTEXTS,contexts);
	}

	public TransformerCallBack getExecuteCallBack() {
		return (TransformerCallBack) get(EXECUTE_CALLBACK);
	}
	public void setExecuteCallBack(TransformerCallBack callback) {
		put(EXECUTE_CALLBACK,callback);
	}

	public TransformerCallBack getProcessCallBack() {
		return (TransformerCallBack) get(PROCESS_CALLBACK);
	}
	public void setProcessCallBack(TransformerCallBack callback) {
		put(PROCESS_CALLBACK,callback);
	}

	public LinkedHashMap<Integer, TransformerPluginV2LocalInterface> getInstantiatedPlugins() {
		return (LinkedHashMap<Integer, TransformerPluginV2LocalInterface>) get(INSTANTIATED_PLUGINS);
	}
	public void setInstantiatedPlugins(LinkedHashMap<Integer, TransformerPluginV2LocalInterface> instantiatedPlugins) {
		put(INSTANTIATED_PLUGINS,instantiatedPlugins);
	}
	
	public long getIterationNumber() {
		return ((Long) get(COUNTER)).longValue();
	}
	public void setIterationNumber(long counter) {
		put(COUNTER, new Long(counter));
	}
	
	public boolean isTransformerDone() {
		return ((Boolean) get(TRANSFOMER_DONE)).booleanValue();
	}
	public void setTransformerDone(boolean isDone) {
		put(TRANSFOMER_DONE, new Boolean(true));
	}

	public HashMap<String, String> getDecisionTable() {
		return (HashMap<String, String>) get(DECISION_TABLE);
	}
	public void setDecisionTable(HashMap<String, String> decisionTable) {
		put(DECISION_TABLE,decisionTable);
	}
	

	public long getStartTime() {
		return ((Long) get(TIME)).longValue();
	}
	public void setStartTime(long timeInMillis) {
		put(TIME, new Long(timeInMillis));
	}
	
	public BackgroundJobPOJO getJob() {
		return (BackgroundJobPOJO) get(JOB);
	}
	public void setJob(BackgroundJobPOJO jobPOJO) {
		put(JOB,jobPOJO);
	}
	
	public String getUserToken() {
		return (String) get(USERTOKEN);
	}
	public void setUserToken(String userToken) {
		put(USERTOKEN,userToken);
	}
	
//	
//	@Override
//	public byte[] toBytes() throws IOException {
//		TransformerContext ctx = new TransformerContext(this.getTransformerV2POJOPK());
//		ctx.setContextMap(this.getContextMap());
//		ctx.setPipeline(this.getPipeline());
//		ctx.setProjectedPKs(this.getProjectedPKs());
//		return ctx.toBytes();
//		
//	}
	
	
}
