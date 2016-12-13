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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.amalto.core.jobox.JobInfo;
import com.amalto.core.jobox.util.JoboxConfig;
import com.amalto.core.jobox.util.JoboxException;
import com.amalto.core.jobox.util.JoboxUtil;

public class JobAware {

    public static final Pattern jobVersionNamePattern = Pattern.compile("(.*?)_(\\d*.\\d*)$"); //$NON-NLS-1$

    private static final String JOBOX_RESERVED_FOLDER_NAME = "tmp"; //$NON-NLS-1$

    private final String workDir;

    private static final Logger LOGGER = Logger.getLogger(JobAware.class);

    public JobAware(JoboxConfig joboxConfig) {
        this.workDir = joboxConfig.getWorkPath();
    }

    public List<JobInfo> findJobsInBox() {
        File[] entities = new File(workDir).listFiles(new FileFilter() {

            public boolean accept(File pathName) {
                return !(pathName.isFile() || JOBOX_RESERVED_FOLDER_NAME.equalsIgnoreCase(pathName.getName()));
            }
        });
        List<JobInfo> jobList = new ArrayList<JobInfo>();
        for (File entity : entities) {
            boolean isTISEntry = recognizeTISJob(entity);
            if (isTISEntry) {
                // parse name and version
                String jobVersion = ""; //$NON-NLS-1$
                String jobName = ""; //$NON-NLS-1$
                Matcher m = jobVersionNamePattern.matcher(entity.getName());
                while (m.find()) {
                    jobName = m.group(1);
                    jobVersion = m.group(m.groupCount());
                }
                JobInfo jobInfo = new JobInfo(jobName, jobVersion);
                setClassPath4TISJob(entity, jobInfo);
                // get main class from command line
                guessMainClassFromCommandLine(entity, jobInfo);
                //not found then found it in context properties folder
                if (jobInfo.getMainClass() == null) {
                    String propFilePath = analyzeJobParams(entity, jobInfo);
                    guessMainClass(propFilePath, jobInfo);
                }
                jobList.add(jobInfo);
            }
        }
        return jobList;
    }

    public JobInfo loadJobInfo(String entityName) {
        JobInfo jobInfo = null;
        File entity = new File(workDir + File.separator + entityName);
        if (entity.exists()) {
            // parse name and version
            String jobVersion = StringUtils.EMPTY;
            String jobName = StringUtils.EMPTY;
            Matcher m = jobVersionNamePattern.matcher(entityName);
            while (m.find()) {
                jobName = m.group(1);
                jobVersion = m.group(m.groupCount());
            }
            jobInfo = new JobInfo(jobName, jobVersion);
            setClassPath4TISJob(entity, jobInfo);
            // get main class from command line
            guessMainClassFromCommandLine(entity, jobInfo);
            //not found then found it in context properties folder
            if (jobInfo.getMainClass() == null) {
                String propFilePath = analyzeJobParams(entity, jobInfo);
                guessMainClass(propFilePath, jobInfo);
            }
        }
        return jobInfo;
    }

    /**
     * get the main class from the command line xxx_run.sh or xxx_run.bat
     */
    public void guessMainClassFromCommandLine(File entity, JobInfo jobInfo) {
        boolean found = false;
        InputStream in = null;
        try {
            List<File> checkList = new ArrayList<File>();
            // try windows .bat file
            String commandFileName = jobInfo.getName() + "_run.bat";//$NON-NLS-1$
            JoboxUtil.findFirstFile(null, entity, commandFileName, checkList); //$NON-NLS-1$
            if (checkList.size() > 0) {
                in = new FileInputStream(checkList.get(0));
                String content = IOUtils.toString(in);
                String mainClass = JoboxUtil.parseMainClassFromJCL(content);
                if (mainClass != null) {
                    jobInfo.setMainClass(mainClass);
                    found = true;
                }
            }
            // try linux  .sh file
            if (!found) {
                commandFileName = jobInfo.getName() + "_run.sh";//$NON-NLS-1$
                JoboxUtil.findFirstFile(null, entity, commandFileName, checkList); //$NON-NLS-1$
                if (checkList.size() > 0) {
                    in = new FileInputStream(checkList.get(0));
                    String content = IOUtils.toString(in);
                    String mainClass = JoboxUtil.parseMainClassFromJCL(content);
                    if (mainClass != null) {
                        jobInfo.setMainClass(mainClass);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred during executable class search.", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * it can't make sure to get the right main class from 'context' properties folder
     */
    private void guessMainClass(String propFilePath, JobInfo jobInfo) {
        // FIX ME:THIS WAY IS NOT GOOD
        if (propFilePath != null) {
            String jobName = jobInfo.getName();
            String className = ""; //$NON-NLS-1$
            String packageName = ""; //$NON-NLS-1$
            String splitTag = "/"; //$NON-NLS-1$
            if (File.separator.equals("\\")) { //$NON-NLS-1$
                splitTag = "\\\\"; //$NON-NLS-1$
            }
            String[] parts = propFilePath.split(splitTag);
            boolean startRecord = false;
            for (String part : parts) {
                if ("contexts".equals(part)) { //$NON-NLS-1$
                    break;
                }
                if (part.equals(jobName) && !startRecord) {
                    startRecord = true;
                    continue;
                }
                if (startRecord) {
                    if (packageName.length() == 0) {
                        packageName += part;
                    } else if (packageName.length() > 0) {
                        packageName += "." + part; //$NON-NLS-1$
                    }
                }
            }
            if (packageName.length() > 0) {
                className = packageName + "." + jobName; //$NON-NLS-1$
            }
            if (className.length() > 0) {
                jobInfo.setMainClass(className);
            }
        }
    }

    private String analyzeJobParams(File entity, JobInfo jobInfo) {
        String propFilePath = null;
        try {
            List<File> checkList = new ArrayList<File>();
            JoboxUtil.findFirstFile(jobInfo, entity, jobInfo.getContextStr() + ".properties", checkList);//$NON-NLS-1$
            if (checkList.size() > 0) {
                propFilePath = checkList.get(0).getAbsolutePath();
                Properties paramProperties = new Properties();
                FileInputStream fileReader = new FileInputStream(checkList.get(0));
                try {
                    paramProperties.load(fileReader);
                } finally {
                    fileReader.close();
                }
                for (Enumeration e = paramProperties.propertyNames(); e.hasMoreElements();) {
                    String key = (String) e.nextElement();
                    String value = paramProperties.getProperty(key);
                    jobInfo.addParam(key, value);
                }
            }
        } catch (Exception e) {
            throw new JoboxException(e);
        }
        return propFilePath;
    }

    private boolean recognizeTISJob(File entity) {
        boolean isTISEntry = false;
        List<File> checkList = new ArrayList<File>();
        JoboxUtil.findFirstFile(null, entity, "classpath.jar", checkList); //$NON-NLS-1$
        if (checkList.size() > 0) {
            try {
                JarFile jarFile = new JarFile(checkList.get(0).getAbsolutePath());
                Manifest jarFileManifest = jarFile.getManifest();
                String vendorInfo = jarFileManifest.getMainAttributes().getValue("Implementation-Vendor"); //$NON-NLS-1$
                if (vendorInfo.trim().toUpperCase().startsWith("TALEND")) //$NON-NLS-1$
                    isTISEntry = true;
            } catch (IOException e) {
                throw new JoboxException(e);
            }
        }
        return isTISEntry;
    }

    private void setClassPath4TISJob(File entity, JobInfo jobInfo) {
        String newClassPath = ""; //$NON-NLS-1$
        String separator = System.getProperty("path.separator"); //$NON-NLS-1$
        List<File> checkList = new ArrayList<File>();
        JoboxUtil.findFirstFile(null, entity, "classpath.jar", checkList); //$NON-NLS-1$
        if (checkList.size() > 0) {
            try {
                String basePath = checkList.get(0).getParent();
                JarFile jarFile = new JarFile(checkList.get(0).getAbsolutePath());
                Manifest jarFileManifest = jarFile.getManifest();
                String cxt = jarFileManifest.getMainAttributes().getValue("activeContext"); //$NON-NLS-1$
                jobInfo.setContextStr(cxt);
                String classPaths = jarFileManifest.getMainAttributes().getValue("Class-Path"); //$NON-NLS-1$
                String[] classPathsArray = classPaths.split("\\s+", 0); //$NON-NLS-1$
                List<String> classPathsArrayList = new ArrayList<String>(Arrays.asList(classPathsArray));
                List<String> classPathsExtArray = new ArrayList<String>();
                if (!classPathsArrayList.contains(".")) { //$NON-NLS-1$
                    classPathsExtArray.add("."); //$NON-NLS-1$
                }
                if (classPathsArrayList.size() > 0) {
                    classPathsExtArray.addAll(classPathsArrayList);
                }
                for (String classPath : classPathsExtArray) {
                    File libFile = new File(basePath + File.separator + classPath);
                    if (libFile.exists()) {
                        if (newClassPath.length() == 0) {
                            newClassPath += libFile.getAbsolutePath();
                        } else if (newClassPath.length() > 0) {
                            newClassPath += separator + libFile.getAbsolutePath();
                        }
                    }
                }
            } catch (IOException e) {
                throw new JoboxException(e);
            }
        }
        jobInfo.setClasspath(newClassPath);
    }

}
