/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.migration.tasks;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.migration.AbstractMigrationTask;
import com.amalto.core.objects.configurationinfo.ConfigurationHelper;
import com.amalto.core.objects.transformers.TransformerV2POJO;
import com.amalto.core.objects.transformers.util.TransformerProcessStep;
import com.amalto.core.objects.transformers.util.TransformerVariablesMapping;
import com.amalto.core.util.Util;
import org.apache.log4j.Logger;
import com.amalto.core.server.api.Transformer;

import java.util.ArrayList;

public class ReplaceEmptyInfoInTransformersTask extends AbstractMigrationTask{

	@Override
	protected Boolean execute() {
		//Update Transformer POJOS
		org.apache.log4j.Logger.getLogger(ReplaceEmptyInfoInTransformersTask.class).info("Updating Transformers");
		try {
			
			String[] ids = ConfigurationHelper.getServer().getAllDocumentsUniqueID(ObjectPOJO.getCluster(TransformerV2POJO.class));
			if (ids != null) {
				Transformer tCtrl = Util.getTransformerV2CtrlLocal();
                for (String id : ids) {
                    String xml = ConfigurationHelper.getServer().getDocumentAsString(ObjectPOJO.getCluster(TransformerV2POJO.class), id);
                    TransformerV2POJO transformer = ObjectPOJO.unmarshal(TransformerV2POJO.class, xml);
                    ArrayList<TransformerProcessStep> steps = transformer.getProcessSteps();
                    if (steps != null) {
                        for (TransformerProcessStep step : steps) {
                            //add some text to empty descriptions
                            if (step.getDescription() == null || "".equals(step.getDescription().trim())) { //$NON-NLS-1$
                                step.setDescription("[no description]"); //$NON-NLS-1$
                            }
                            //Replace empty variable with "_DEFAULT_" variable
                            ArrayList<TransformerVariablesMapping> inputMappings = step.getInputMappings();
                            for (TransformerVariablesMapping transformerVariablesMapping : inputMappings) {
                                String pipelineVariable = transformerVariablesMapping.getPipelineVariable();
                                if (pipelineVariable == null || "".equals(pipelineVariable.trim())) { //$NON-NLS-1$
                                    transformerVariablesMapping.setPipelineVariable(Transformer.DEFAULT_VARIABLE);
                                }
                            }
                            ArrayList<TransformerVariablesMapping> outputMappings = step.getOutputMappings();
                            for (TransformerVariablesMapping transformerVariablesMapping : outputMappings) {
                                String pipelineVariable = transformerVariablesMapping.getPipelineVariable();
                                if (pipelineVariable == null || "".equals(pipelineVariable.trim())) { //$NON-NLS-1$
                                    transformerVariablesMapping.setPipelineVariable(Transformer.DEFAULT_VARIABLE);
                                }
                            }
                        }
                    }
                    tCtrl.putTransformer(transformer);
                    Logger.getLogger(ReplaceEmptyInfoInTransformersTask.class).info("Processed '" + transformer.getName() + "'");
                }
			}
		} catch (Exception e) {
			String err = "Unable to Rename Menu Entries.";
			org.apache.log4j.Logger.getLogger(ReplaceEmptyInfoInTransformersTask.class).error(err, e);
			return false;
		}
		org.apache.log4j.Logger.getLogger(ReplaceEmptyInfoInTransformersTask.class).info("Done Updating Transformers");
		return true;
	}

}
