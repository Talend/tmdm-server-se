/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox.watch;

import com.amalto.core.jobox.JobContainer;
import com.amalto.core.jobox.JobInfo;
import com.amalto.core.jobox.component.JobAware;
import com.amalto.core.jobox.component.JobDeploy;
import com.amalto.core.jobox.util.JoboxException;
import com.amalto.core.jobox.util.JoboxUtil;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.*;
import java.util.zip.ZipException;

public class JoboxListener implements DirListener {

    private static final Logger LOGGER = Logger.getLogger(JoboxListener.class);
    
    public void fileChanged(List<String> newFiles, List<String> deleteFiles, List<String> modifyFiles) {
        JobContainer container = JobContainer.getUniqueInstance();
        JobDeploy jobDeployer = container.getJobDeployer();
        JobAware jobAware = container.getJobAware();
        
        if (newFiles.size() > 0) { // new
            for (String jobPackageName : newFiles) {
                String warningMessage = "Attempted to deploy new job '" + jobPackageName + "' but has been deleted by concurrent process."; //$NON-NLS-1$ //$NON-NLS-2$
                String modifiedWarningMessage = "Attempted to update job '" + jobPackageName + "' but is being modified by concurrent process.";  //$NON-NLS-1$ //$NON-NLS-2$

                // deploy
                try {
                    jobDeployer.deploy(jobPackageName);
                    // add to classpath
                    JobInfo jobInfo = jobAware.loadJobInfo(JoboxUtil.trimExtension(jobPackageName));
                    if (jobInfo != null) {
                        container.updateJobLoadersPool(jobInfo);
                    } else {
                        LOGGER.warn(warningMessage); 
                    }
                } catch (JoboxException e) {
                    if (e.getCause() instanceof FileNotFoundException) {
                        LOGGER.warn(warningMessage);
                    } else if (e.getCause() instanceof EOFException) {
                        LOGGER.warn(modifiedWarningMessage);
                    } else if (e.getCause() instanceof ZipException) {
                        LOGGER.warn(modifiedWarningMessage);
                    } else {
                        throw e;
                    }
                }
                
            }
        }

        if (deleteFiles.size() > 0) {
            // delete
            for (String jobPackageName : deleteFiles) {
                String jobEntityName = JoboxUtil.trimExtension(jobPackageName);
                // undeploy
                jobDeployer.undeploy(jobEntityName);
                // remove classpath
                container.removeFromJobLoadersPool(jobEntityName);
            }
        }

        if (modifyFiles.size() > 0) {
            // modify
            for (String jobPackageName : modifyFiles) {
                String deletedWarningMessage = "Attempted to update job '" + jobPackageName + "' but has been deleted by concurrent process.";  //$NON-NLS-1$ //$NON-NLS-2$
                String modifiedWarningMessage = "Attempted to update job '" + jobPackageName + "' but has been modified by concurrent process.";  //$NON-NLS-1$ //$NON-NLS-2$
                
                // deploy
                try {
                    jobDeployer.deploy(jobPackageName);
                    // add to classpath
                    JobInfo jobInfo = jobAware.loadJobInfo(JoboxUtil.trimExtension(jobPackageName));
                    if (jobInfo != null) {
                        container.updateJobLoadersPool(jobInfo);
                    } else {
                        LOGGER.warn(deletedWarningMessage);
                    }
                } catch (JoboxException e) {
                    if (e.getCause() instanceof FileNotFoundException) {
                        LOGGER.warn(deletedWarningMessage);
                    } else if (e.getCause() instanceof EOFException) {
                        LOGGER.warn(modifiedWarningMessage);
                    } else if (e.getCause() instanceof ZipException) {
                        LOGGER.warn(modifiedWarningMessage);
                    } else {
                        throw e;
                    }
                }
                
            }
        }
    }

    public void contextChanged(String jobFile, String context) {
        File entity = new File(jobFile);
        String sourcePath = jobFile;
        int dotMark = jobFile.lastIndexOf("."); //$NON-NLS-1$
        int separateMark = jobFile.lastIndexOf(File.separatorChar);
        if (dotMark != -1) {
            sourcePath = System.getProperty("java.io.tmpdir") + File.separatorChar + jobFile.substring(separateMark, dotMark); //$NON-NLS-1$
        }
        try {
            JoboxUtil.extract(jobFile, System.getProperty("java.io.tmpdir") + File.separatorChar); //$NON-NLS-1$
        } catch (Exception e1) {
            LOGGER.error("Extraction exception occurred.", e1);
            return;
        }
        List<File> resultList = new ArrayList<File>();
        JoboxUtil.findFirstFile(null, new File(sourcePath), "classpath.jar", resultList); //$NON-NLS-1$
        if (!resultList.isEmpty()) {
            JarInputStream jarIn = null;
            JarOutputStream jarOut = null;
            try {
                JarFile jarFile = new JarFile(resultList.get(0));
                Manifest mf = jarFile.getManifest();
                jarIn = new JarInputStream(new FileInputStream(resultList.get(0)));
                Manifest newManifest = jarIn.getManifest();
                if (newManifest == null) {
                    newManifest = new Manifest();
                }
                newManifest.getMainAttributes().putAll(mf.getMainAttributes());
                newManifest.getMainAttributes().putValue("activeContext", context); //$NON-NLS-1$
                jarOut = new JarOutputStream(new FileOutputStream(resultList.get(0)), newManifest);
                byte[] buf = new byte[4096];
                JarEntry entry;
                while ((entry = jarIn.getNextJarEntry()) != null) {
                    if ("META-INF/MANIFEST.MF".equals(entry.getName())) { //$NON-NLS-1$
                        continue;
                    }
                    jarOut.putNextEntry(entry);
                    int read;
                    while ((read = jarIn.read(buf)) != -1) {
                        jarOut.write(buf, 0, read);
                    }
                    jarOut.closeEntry();
                }
            } catch (Exception e) {
                LOGGER.error("Extraction exception occurred.", e);
            } finally {
                IOUtils.closeQuietly(jarIn);
                IOUtils.closeQuietly(jarOut);
            }
            // re-zip file
            if (entity.getName().endsWith(".zip")) { //$NON-NLS-1$
                File sourceFile = new File(sourcePath);
                try {
                    JoboxUtil.zip(sourceFile, jobFile);
                } catch (Exception e) {
                    LOGGER.error("Zip exception occurred.", e);
                }
            }
        }
    }
}
