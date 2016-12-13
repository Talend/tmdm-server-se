/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.server.transformer;

import com.amalto.core.objects.transformers.util.TransformerCallBack;
import com.amalto.core.objects.transformers.util.TransformerContext;
import com.amalto.core.objects.transformers.util.TransformerGlobalContext;
import com.amalto.core.server.DefaultTransformer;
import com.amalto.core.util.XtentisException;

public class DefaultTransformerExt extends DefaultTransformer {

    @Override
    public void execute(TransformerContext context, TransformerCallBack callBack) throws XtentisException {
        TransformerGlobalContext globalContext = null;

        if (context instanceof TransformerGlobalContext) {
            globalContext = (TransformerGlobalContext) context;
        } else {
            globalContext = new TransformerGlobalContext(context);
        }

        try {
            callBack.done(globalContext);
        } catch (Exception e) {
            String err = "Unable to execute the Transformer: '" + context.getTransformerV2POJOPK().getUniqueId() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
            throw new XtentisException(err);
        } finally {
            globalContext.removeAll();
        }
    }
}
