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

import org.junit.Test;

import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import com.amalto.core.objects.transformers.util.TransformerContext;
import com.amalto.core.objects.transformers.util.TransformerGlobalContext;

public class TransformerTest {

    @Test
    public void testTransformerExecuteUntilDone() throws Exception {
        final String RUNNING = "XtentisWSBean.executeTransformerV2.test.running"; //$NON-NLS-1$
        TransformerContext context = new TransformerContext(new TransformerV2POJOPK("transformer_Test")); //$NON-NLS-1$
        context.put(RUNNING, Boolean.TRUE);

        TransformerGlobalContext globalContext = new TransformerGlobalContext(context);

        DefaultTransformerExt defaultTransformer = new DefaultTransformerExt();
        defaultTransformer.executeUntilDone(globalContext);
    }

}
