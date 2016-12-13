package com.amalto.core.migration.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.amalto.core.migration.AbstractMigrationTask;
import com.amalto.core.objects.transformers.TransformerV2POJO;
import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import com.amalto.core.objects.transformers.util.TransformerProcessStep;
import com.amalto.core.objects.transformers.util.TransformerVariablesMapping;
import com.amalto.core.util.Util;

/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

/**
 * see 0020008 Consistency of process results
 * 
 * @author achen
 * 
 */
public class ConsistencyErrorMessageTask extends AbstractMigrationTask {

    private static final Logger LOG = Logger.getLogger(ConsistencyErrorMessageTask.class);

    @Override
    @SuppressWarnings("unchecked")
    protected Boolean execute() {
        try {
            Collection<TransformerV2POJOPK> wst = Util.getTransformerV2CtrlLocal().getTransformerPKs("*"); //$NON-NLS-1$
            for (TransformerV2POJOPK id : wst) {
                if (id.getIds()[0].startsWith("beforeSaving_") || id.getIds()[0].startsWith("beforeDeleting_")) { //$NON-NLS-1$ //$NON-NLS-2$
                    TransformerV2POJO pojo = Util.getTransformerV2CtrlLocal().getTransformer(id);
                    ArrayList<TransformerProcessStep> steps = pojo.getProcessSteps();
                    if (steps.size() > 0) {
                        TransformerProcessStep st = steps.get(steps.size() - 1);
                        for (TransformerVariablesMapping mapping : st.getOutputMappings()) {// convert
                            if ("output_error_message".equals(mapping.getPipelineVariable())) { //$NON-NLS-1$
                                mapping.setPipelineVariable("output_report"); //$NON-NLS-1$
                            }
                        }
                        Pattern p = Pattern.compile("(.*?)<error code=\"(.*?)\">(.*?)</error>(.*?)", Pattern.DOTALL); //$NON-NLS-1$
                        Matcher m = p.matcher(st.getParameters());
                        if (m.matches()) {
                            String pre = m.group(1);
                            String suf = m.group(4);
                            String code = m.group(2);
                            String msg = m.group(3);
                            code = "0".equals(code) ? "info" : "error"; //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
                            String errorMsg = "<report><message type=\"" + code + "\">" + msg + "</message></report>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            st.setParameters(pre + errorMsg + suf);
                        }
                        pojo.store();
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
        return true;
    }

}
