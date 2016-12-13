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
import java.util.SortedSet;
import java.util.TreeSet;

import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.transformers.TransformerV2POJO;
import com.amalto.core.util.XtentisException;

public class TransformerPluginContext extends TransformerContext {

    private final static String GLOBAL_CONTEXT = "com.amalto.core.util.TransformerPluginContext.GLOBAL_CONTEXT";

    private final static String PLUGIN_NUMBER = "com.amalto.core.util.TransformerPluginContext.PLUGIN_HANDLE";

    private final static String PLUGINS_CALLBACK = "com.amalto.core.util.TransformerPluginContext.PLUGINS_CALLBACK";

    /**
     * A Transformer Plugin Context should never be instantiated directly
     * but via the {@link TransformerGlobalContext#getTransformerPluginContext(int)}
     *
     * @param globalContext - The Transformer Global Context used to create this Plugin Context
     * @param pluginNumber
     */
    public TransformerPluginContext(TransformerGlobalContext globalContext, int pluginNumber) {
        super(globalContext.getTransformerV2POJOPK());
        this.put(GLOBAL_CONTEXT, globalContext);
        this.put(PLUGIN_NUMBER, pluginNumber);
        this.setPipeline(globalContext.getPipeline());
        this.setProjectedPKs(globalContext.getProjectedPKs());
    }

    public TransformerGlobalContext getTransformerGlobalContext() {
        return (TransformerGlobalContext) this.get(GLOBAL_CONTEXT);
    }

    public int getPluginNumber() {
        return (Integer) this.get(PLUGIN_NUMBER);
    }

    public TransformerV2POJO getTransformerPOJO() throws XtentisException {
        return getTransformerGlobalContext().getTransformerPOJO();
    }


    public TransformerPluginCallBack getPluginCallBack() {
        return (TransformerPluginCallBack) get(PLUGINS_CALLBACK);
    }

    public void setPluginCallBack(TransformerPluginCallBack callback) {
        put(PLUGINS_CALLBACK, callback);
    }

    public synchronized void setProjectedPKToGlobalContext(ItemPOJOPK projectedPK) {
        SortedSet<ItemPOJOPK> itemPOJOPKs = this.getTransformerGlobalContext().getProjectedPKs();
        if (itemPOJOPKs == null) {
            itemPOJOPKs = Collections.synchronizedSortedSet(new TreeSet<ItemPOJOPK>());
        }
        itemPOJOPKs.add(projectedPK);
        this.getTransformerGlobalContext().setProjectedPKs(itemPOJOPKs);
    }
}
