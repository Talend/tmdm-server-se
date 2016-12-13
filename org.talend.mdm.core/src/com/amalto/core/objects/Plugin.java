/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects;

import com.amalto.core.objects.transformers.util.TransformerGlobalContext;
import com.amalto.core.objects.transformers.util.TransformerPluginContext;
import com.amalto.core.objects.transformers.util.TransformerPluginV2LocalInterface;
import com.amalto.core.util.XtentisException;

/**
 *
 */
public abstract class Plugin implements TransformerPluginV2LocalInterface {

    private TransformerGlobalContext globalContext;

    @Override
    public void setGlobalContext(TransformerGlobalContext globalContext) {
        this.globalContext = globalContext;
    }

    public TransformerGlobalContext getGlobalContext() {
        return globalContext;
    }

    @Override
    public void end(TransformerPluginContext context) throws XtentisException {
    }

    @Override
    public String getConfiguration(String optionalParameters) throws XtentisException {
        return null;
    }

    @Override
    public void putConfiguration(String configuration) throws XtentisException {
    }

    protected abstract String loadConfiguration();
}
