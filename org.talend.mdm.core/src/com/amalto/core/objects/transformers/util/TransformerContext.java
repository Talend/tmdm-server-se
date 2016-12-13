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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import org.apache.log4j.Logger;

public class TransformerContext implements Serializable {

	private LinkedHashMap<String, Object> contextMap = null;
	
    private LinkedHashMap<String, TypedContent> pipeline = null;
	
    private SortedSet<ItemPOJOPK>projectedPKs = null;
	
    private TransformerV2POJOPK transformerPOJOPK = null;
	
    private final Object contextLock = new Object();
    
    private final Object pipelineLock = new Object();

    private final Logger LOGGER = Logger.getLogger(TransformerContext.class);

	/**
	 * For serialization only
	 */
	protected TransformerContext() {}
	
	/**
	 * Instantiate a TransformerContext
	 * The transformer Context contains
	 * 	-"local" values that can be manipulated via the get, put, etc... method
	 * @param transformerPOJOPK - The Transformer PK
	 */
	public TransformerContext(TransformerV2POJOPK transformerPOJOPK) {
		this.transformerPOJOPK = transformerPOJOPK;
		contextMap = new LinkedHashMap<String, Object>();
		pipeline = new LinkedHashMap<String, TypedContent>();
		projectedPKs = Collections.synchronizedSortedSet(new TreeSet<ItemPOJOPK>());
	}
	
	public TransformerV2POJOPK getTransformerV2POJOPK() {
		return transformerPOJOPK;
	}

	
	/******************************************************************
	 * 
	 * Local Variables Manipulation
	 * 
	 ******************************************************************/
	
	
	public void put(String key, Object value) {
		synchronized (contextLock) {
			contextMap.put(key, value);
		}
	}

	public Object get(String key) {
		synchronized (contextLock) {
			return contextMap.get(key);
		}
	}
	
	public Set<String> keySet() {
		synchronized (contextLock) {
			return contextMap.keySet();
		}
	}
	
	public Object remove(String key) {
		synchronized (contextLock) {
			return contextMap.remove(key);
		}
	}

	public TransformerContext removeAll() {
		synchronized(contextLock) {
			contextMap = new LinkedHashMap<String, Object>();
		}
		return this;
	}
	
	public void dumpLocalVariables() {
		synchronized(contextLock) {
			Set<String> keys = new TreeSet<String>(contextMap.keySet());
            for (String key : keys) {
                LOGGER.info("Dump Local Vars.: " + key + " -" + this.get(key));
            }
		}
	}
	
	protected LinkedHashMap<String, Object> getContextMap() {
		return contextMap;
	}

	protected void setContextMap(LinkedHashMap<String, Object> contextMap) {
		this.contextMap = contextMap;
	}

	
	/******************************************************************
	 * 
	 * Pipeline Manipulation
	 * 
	 ******************************************************************/
	
	
	public void putInPipeline(String key, TypedContent content) {
		synchronized (pipelineLock) {
			pipeline.put(key, content);
		}
	}

	public TypedContent getFromPipeline(String variableName) {
		synchronized (pipelineLock) {
			return pipeline.get(variableName);
		}
	}
	
	public Set<String> getPipelineVariableNames() {
		synchronized (pipelineLock) {
			return new LinkedHashSet<String>(pipeline.keySet());
		}
	}
	
	public Object removeFrompipeline(String variableName) {
		synchronized (pipelineLock) {
			return pipeline.remove(variableName);
		}
	}

	public TransformerContext removeAllFromPipeline() {
		synchronized(pipelineLock) {
			pipeline = new LinkedHashMap<String, TypedContent>();
		}
		return this;
	}
	
	public void dumpPipeline() {
		synchronized(pipelineLock) {
			Set<String> keys = new TreeSet<String>(pipeline.keySet());
            for (String key : keys) {
                LOGGER.info("Pipeline Dump: " + key + " -" + pipeline.get(key));
            }
		}
	}
	
	public LinkedHashMap<String, TypedContent> getPipelineClone() {
		synchronized (pipelineLock) {
			return new LinkedHashMap<String, TypedContent>(pipeline);
		}
	}
	
	protected LinkedHashMap<String, TypedContent> getPipeline() {
        synchronized (pipelineLock) {
            return pipeline;
        }
    }

	protected void setPipeline(LinkedHashMap<String, TypedContent> pipeline) {
        synchronized (pipelineLock) {
            this.pipeline = pipeline;
        }
    }


	
	/******************************************************************
	 * 
	 * Projected Item PKs Manipulation
	 * 
	 ******************************************************************/

	public SortedSet<ItemPOJOPK> getProjectedPKs() {
		return projectedPKs;
	}

	protected void setProjectedPKs(TreeSet<ItemPOJOPK> projectedPKs) {
		this.projectedPKs = Collections.synchronizedSortedSet(projectedPKs);
	}
	
	protected void setProjectedPKs(SortedSet<ItemPOJOPK> projectedPKs) {
		this.projectedPKs = projectedPKs;
	}
		
	
	/******************************************************************
	 * 
	 * Serializable Implementation
	 * 
	 ******************************************************************/
	
	private void writeObject(java.io.ObjectOutputStream out)  throws IOException {
		HashMap<String, Serializable> serializable = new HashMap<String, Serializable>();
		serializable.put("transformerPK", this.getTransformerV2POJOPK());
		serializable.put("pipeline", this.getPipeline());
		serializable.put("projectedPKs", (Serializable)this.getProjectedPKs());
		
		LinkedHashMap<String, Serializable> ctxMap = new LinkedHashMap<String, Serializable>();
		//do not copy over non serializable stuff from the context Map
		//FIXME: this creates tons of issues
//		Set<String> keys = contextMap.keySet();
//		for (Iterator<String> iter = keys.iterator(); iter.hasNext(); ) {
//			String key = iter.next();
//			Object value = contextMap.get(key);
//			if (value instanceof Serializable) {
//				ctxMap.put(key, (Serializable)value);
//			}
//		}
		serializable.put("ctxMap", ctxMap);
		
		out.writeObject(serializable);
	}
	
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        HashMap<String, Serializable> serializable = (HashMap<String, Serializable>) in.readObject();
        this.transformerPOJOPK = (TransformerV2POJOPK) serializable.get("transformerPK");
        this.contextMap = new LinkedHashMap<String, Object>((LinkedHashMap<String, Serializable>) serializable.get("ctxMap"));
        this.pipeline = (LinkedHashMap<String, TypedContent>) serializable.get("pipeline");
        this.projectedPKs = (SortedSet<ItemPOJOPK>) serializable.get("projectedPKs");
    }

    public static TransformerContext parseFromBytes(byte[] bytes) throws IOException, ClassNotFoundException{
		TransformerContext ctx = new TransformerContext();
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		ctx.readObject(ois);
		return ctx;
	}

}
