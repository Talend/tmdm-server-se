/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.configurationinfo.assemble;

import com.amalto.core.jobox.JobContainer;
import com.amalto.core.jobox.util.JoboxConfig;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

public class InitJoboxSubProc extends AssembleSubProc {

    protected static final Logger LOGGER = Logger.getLogger(InitJoboxSubProc.class);

    @Override
    public void run() throws Exception {
        String mdmRootDir = System.getProperty("mdm.root"); //$NON-NLS-1$
        if (!new File(mdmRootDir).exists()) {
            throw new FileNotFoundException();
        }
        String mdmHome = new File(mdmRootDir).getAbsolutePath();
        // init
        JobContainer jobContainer = JobContainer.getUniqueInstance();
        Properties props = new Properties();
        props.put(JoboxConfig.JOBOX_HOME_PATH, mdmHome + File.separator + "jobox");
        try {
            jobContainer.init(props);
        } catch (Exception e) {
            LOGGER.error("Could not start job container.", e);
        }
    }
}
