/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox.component;

import com.amalto.core.jobox.JobContainer;
import com.amalto.core.jobox.util.JoboxConfig;
import com.amalto.core.jobox.util.JoboxException;
import com.amalto.core.jobox.util.JoboxUtil;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * This class handles all deployment relative actions for the Jobox container.
 */
public class JobDeploy {
    
    private static final Logger LOGGER = Logger.getLogger(JobDeploy.class);

    private final String deployDir;

    private final String workDir;

    /**
     * @param joboxConfig A {@link JoboxConfig} configuration.
     */
    public JobDeploy(JoboxConfig joboxConfig) {
        this.deployDir = joboxConfig.getDeployPath();
        this.workDir = joboxConfig.getWorkPath();
    }

    /**
     * <p>
     * Force deployment of all files in deploy directory.
     * </p>
     * <p>
     * This method does not fail if anything goes wrong (error is printed out in log only). This prevents the whole
     * container to fail if only one ZIP file is corrupted.
     * </p>
     * @see com.amalto.core.jobox.util.JoboxConfig#getDeployPath()
     * @see #deploy(String)
     */
    public void deployAll() {
        String[] fileNames = new File(deployDir).list();
        if (fileNames != null) {
            for (String filename : fileNames) {
                try {
                    deploy(filename);
                } catch (JoboxException e) {
                    LOGGER.error("Could not deploy '" + filename + "' due to exception.", e);
                }
            }
        }
    }

    /**
     * Undeploy all files in deploy directory.
     * @see com.amalto.core.jobox.util.JoboxConfig#getDeployPath()
     */
    public void undeployAll() {
        String[] fileNames = new File(deployDir).list();
        if (fileNames != null) {
            for (String filename : fileNames) {
                undeploy(filename);
            }
        }
    }

    /**
     * @param jobZipFileName 'Deploys' the job zip file in the Jobox container. This operation consists in unzipping the
     *                       job zip file in the <em>deploy</em> directory in the <em>work</em> directory.
     * @see com.amalto.core.jobox.util.JoboxConfig#getDeployPath()
     * @see com.amalto.core.jobox.util.JoboxConfig#getWorkPath()
     * @see JobContainer#lock(boolean)
     */
    public void deploy(String jobZipFileName) {
        try {
            JoboxUtil.extract(deployDir + File.separator + jobZipFileName, workDir + File.separator);
            LOGGER.info("Job " + jobZipFileName + " has been successfully deployed.");//$NON-NLS-1$//$NON-NLS-2$
        } catch (Exception e) {
            throw new JoboxException(e);
        }
    }

    /**
     * @param jobEntityName 'Undeploys' the job in the Jobox container. This operation consists in deleting the job from the <em>work</em>
     * directory.
     * @see com.amalto.core.jobox.util.JoboxConfig#getWorkPath()
     * @see JobContainer#lock(boolean)
     */
    public void undeploy(String jobEntityName) {
        try {
            JoboxUtil.deleteFolder(workDir + File.separator + jobEntityName);
            LOGGER.info("Job " + jobEntityName + " has been undeployed successfully! ");//$NON-NLS-1$//$NON-NLS-2$
        } catch (Exception e) {
            throw new JoboxException(e);
        }
    }
}
