/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server.api;

import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.backgroundjob.BackgroundJobPOJOPK;
import com.amalto.core.objects.transformers.TransformerV2POJO;
import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import com.amalto.core.objects.transformers.util.TransformerCallBack;
import com.amalto.core.objects.transformers.util.TransformerContext;
import com.amalto.core.objects.transformers.util.TransformerPluginContext;
import com.amalto.core.objects.transformers.util.TypedContent;
import com.amalto.core.util.XtentisException;

import java.util.Collection;

/**
 *
 */
public interface Transformer {

    String DEFAULT_VARIABLE = "_DEFAULT_";

    TransformerV2POJOPK putTransformer(TransformerV2POJO transformer) throws XtentisException;

    TransformerV2POJO getTransformer(TransformerV2POJOPK pk) throws XtentisException;

    TransformerV2POJO existsTransformer(TransformerV2POJOPK pk) throws XtentisException;

    TransformerV2POJOPK removeTransformer(TransformerV2POJOPK pk) throws XtentisException;

    Collection<TransformerV2POJOPK> getTransformerPKs(String regex) throws XtentisException;

    TransformerContext extractThroughTransformer(TransformerV2POJOPK transformerV2POJOPK, ItemPOJOPK itemPOJOPK)
            throws XtentisException;

    BackgroundJobPOJOPK executeAsJob(TransformerContext context, TransformerCallBack callBack) throws XtentisException;

    void execute(TransformerContext context, TransformerCallBack callBack) throws XtentisException;

    void execute(TransformerContext context, TypedContent content, TransformerCallBack callBack) throws XtentisException;

    TransformerContext executeUntilDone(TransformerContext context) throws XtentisException;

    TransformerContext executeUntilDone(TransformerContext context, TypedContent content) throws XtentisException;

    void contentIsReady(TransformerPluginContext pluginContext) throws XtentisException;
}
