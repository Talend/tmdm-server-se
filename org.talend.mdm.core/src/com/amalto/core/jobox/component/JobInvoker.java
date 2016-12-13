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
import com.amalto.core.jobox.JobInfo;
import com.amalto.core.jobox.properties.ThreadIsolatedSystemProperties;
import com.amalto.core.jobox.util.JobNotFoundException;
import com.amalto.core.jobox.util.JoboxException;
import com.amalto.core.jobox.util.MissingMainClassException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Class that handles common tasks for job execution:
 * 
 * <ul>
 * <li>Class loader isolation: Makes sure current thread uses an isolated class loader.</li>
 * <li>System properties isolation: Makes sure current thread uses standard system properties (Removes all JBoss and MDM
 * properties).</li>
 * <li>'Locks' the job in the container: Container will prevent any modification to be done on the job.</li>
 * </ul>
 */
public abstract class JobInvoker {

    private static final Logger LOGGER = Logger.getLogger(JobInvoker.class);

    private final JobContainer container = JobContainer.getUniqueInstance();

    private final String jobName;

    private final String version;

    public JobInvoker(String jobName, String version) {
        this.jobName = jobName;
        this.version = version;
    }

    public String[][] call() throws JoboxException {
        return call(Collections.<String, String> emptyMap());
    }

    /**
     * @param parameters Input values for job execution
     * @return Result of job execution
     * @throws com.amalto.core.jobox.util.JoboxException In case of call error.
     */
    public final String[][] call(Map<String, String> parameters) {
        ClassLoader previousCallLoader = Thread.currentThread().getContextClassLoader();
        ThreadIsolatedSystemProperties isolatedSystemProperties = ThreadIsolatedSystemProperties.getInstance();

        try {
            container.lock(false);

            if (!System.getProperties().equals(isolatedSystemProperties.getThreadProperties(Thread.currentThread()))) {
                throw new IllegalStateException("Expected system properties to support thread isolation."); //$NON-NLS-1$
            }

            // well-behaved Java packages work relative to the
            // context class loader. Others don't (like commons-logging)
            JobInfo jobInfo = container.getJobInfo(jobName, version);
            if (jobInfo == null) {
                throw new JobNotFoundException(jobName, version);
            }

            LOGGER.info("Invoking job '" + jobName + '_' + version + "'"); //$NON-NLS-1$ //$NON-NLS-2$

            ClassLoader jobClassLoader = container.getJobClassLoader(jobInfo);
            Thread.currentThread().setContextClassLoader(jobClassLoader);

            // Isolate current running thread with JVM standard properties (TMDM-2933).
            isolatedSystemProperties.isolateThread(Thread.currentThread(), container.getStandardProperties());

            if (jobInfo.getMainClass() == null) {
                throw new MissingMainClassException();
            }

            // container.updateJobLoadersPool(jobInfo);
            Class<?> jobClass = container.getJobClass(jobInfo);
            Method runJobMethod = jobClass.getMethod("runJob", String[].class);//$NON-NLS-1$

            Map<String, String> paramMap = jobInfo.getDefaultParamMap();
            // merge with default map
            if (parameters != null) {
                for (String inputParamName : parameters.keySet()) {
                    String inputParamValue = parameters.get(inputParamName);
                    paramMap.put(inputParamName, inputParamValue);
                }
            }

            List<String> params = new ArrayList<String>();
            for (String paramName : paramMap.keySet()) {
                String paramValue = paramMap.get(paramName);
                if (paramName != null && paramValue != null) {
                    params.add("--context_param" + " " + paramName + "=" + paramValue); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                }
            }

            Object jobInstance = jobClass.newInstance();
            prepareJobInstance(jobInstance, parameters);

            String[] parameter = params.toArray(new String[params.size()]);
            return getReturn(runJobMethod, jobInstance, parameter);
        } catch (JobNotFoundException e) {
            throw e;
        } catch (Throwable e) {
            throw new JoboxException(e.getLocalizedMessage(), e);
        } finally {
            // Reintegrate thread into global system properties world.
            isolatedSystemProperties.integrateThread(Thread.currentThread());
            Thread.currentThread().setContextClassLoader(previousCallLoader);
            container.unlock(false);
        }
    }

    protected abstract String[][] getReturn(Method runJobMethod, Object jobInstance, Object parameter) throws JoboxException;

    protected abstract void prepareJobInstance(Object jobInstance, Map<String, String> parameters) throws JoboxException;
}
