/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.amalto.core.jobox.component.JobAware;
import com.amalto.core.jobox.component.JobDeploy;
import com.amalto.core.jobox.component.JobInvoke;
import com.amalto.core.jobox.component.JobInvoker;
import com.amalto.core.jobox.component.MDMJobInvoker;
import com.amalto.core.jobox.properties.StandardPropertiesStrategyFactory;
import com.amalto.core.jobox.util.JobClassLoader;
import com.amalto.core.jobox.util.JobNotFoundException;
import com.amalto.core.jobox.util.JoboxConfig;
import com.amalto.core.jobox.util.JoboxException;
import com.amalto.core.jobox.util.JoboxUtil;
import com.amalto.core.jobox.watch.DirMonitor;
import com.amalto.core.jobox.watch.JoboxListener;

public class JobContainer {

    private static final int WATCH_INTERVAL = 2000;

    /**
     * unique instance
     */
    private static final JobContainer instance = new JobContainer();

    private static final Logger LOGGER = Logger.getLogger(JobContainer.class);

    /**
     * Count how many job executor thread have requested for execution lock.
     */
    private final AtomicInteger executionCount = new AtomicInteger(0);

    /**
     *
     *
     * Indicates whether a thread currently has exclusive access to the container (blocks all requests for execution till
     * the thread releases the exclusive lock).
     */
    private final AtomicBoolean modificationLock = new AtomicBoolean(false);

    private final Map<JobInfo, JobClassLoader> jobLoadersPool = Collections.synchronizedMap(new HashMap<JobInfo, JobClassLoader>());

    private JoboxConfig joboxConfig = null;

    private JobAware jobAware = null;

    private JobDeploy jobDeploy = null;

    private DirMonitor monitor;

    private Properties standardProperties = new Properties();

    /**
     * Private constructor
     */
    private JobContainer() {
    }

    /**
     * @return Returns the unique instance of this class. In order to improve the performance, removed synchronized, using pseudo
     *         singleton mode
     */
    public static JobContainer getUniqueInstance() {
        return instance;
    }

    /**
     * Initializes the Jobox container with the <code>props</code> properties.
     *
     * @param props A {@link Properties} instance used to create a {@link JoboxConfig}.
     */
    public void init(Properties props) {
        // init config
        joboxConfig = new JoboxConfig(props);
        // check home
        File joboxDeployPath = new File(joboxConfig.getDeployPath());
        File joboxWorkPath = new File(joboxConfig.getWorkPath());
        if (!joboxDeployPath.exists()) {
            if (!joboxDeployPath.mkdirs()) {
                LOGGER.error("Create folder failed for '" + joboxDeployPath.getAbsolutePath() + "'.");
            }
        }
        if (!joboxWorkPath.exists()) {
            if (!joboxWorkPath.mkdirs()) {
                LOGGER.error("Create folder failed for '" + joboxWorkPath.getAbsolutePath() + "'.");
            }
        }

        LOGGER.info("Jobox Home is: " + joboxConfig.getJoboxHome()); //$NON-NLS-1$
        // init component
        jobAware = new JobAware(joboxConfig);
        jobDeploy = new JobDeploy(joboxConfig);
        // clear work folder
        JoboxUtil.cleanFolder(joboxConfig.getWorkPath());
        // redeploy all to work folder
        jobDeploy.deployAll();
        // init classpath
        this.jobLoadersPool.clear();
        List<JobInfo> currentJobs = jobAware.findJobsInBox();
        for (JobInfo jobInfo : currentJobs) {
            URL[] urls = JoboxUtil.getClasspathURLs(jobInfo.getClasspath(), jobInfo);
            JobClassLoader cl = new JobClassLoader(urls);
            if (!this.jobLoadersPool.containsKey(jobInfo)) {
                this.jobLoadersPool.put(jobInfo, cl);
            }
        }
        // start monitor
        // Create the monitor
        monitor = new DirMonitor(WATCH_INTERVAL);
        // Add some files to listen for
        monitor.addFile(new File(this.getDeployDir()));
        // Add a jobox listener
        monitor.addListener(new JoboxListener());

        // Initialize default system properties
        standardProperties = StandardPropertiesStrategyFactory.create().getStandardProperties();
    }

    /**
     * @return Returns default JVM properties
     */
    public Properties getStandardProperties() {
        return standardProperties;
    }

    public void updateJobLoadersPool(JobInfo jobInfo) {
        if (jobInfo == null) {
            throw new IllegalArgumentException("Job info argument can not be null."); //$NON-NLS-1$
        }

        if (jobLoadersPool.containsKey(jobInfo)) {
            JobClassLoader jobClassLoader = jobLoadersPool.get(jobInfo);
            log("Removing " + jobClassLoader); //$NON-NLS-1$
            jobLoadersPool.remove(jobInfo);
        } else {
            log("No previous class loader for " + jobInfo.getName());//$NON-NLS-1$
        }

        URL[] urls = JoboxUtil.getClasspathURLs(jobInfo.getClasspath(), jobInfo);
        JobClassLoader cl = new JobClassLoader(urls);
        jobLoadersPool.put(jobInfo, cl);
        LOGGER.info("Adding new class loader " + cl); //$NON-NLS-1$
    }

    public void removeFromJobLoadersPool(String jobEntityName) {
        // parse name and version
        String jobVersion = StringUtils.EMPTY; //$NON-NLS-1$
        String jobName = StringUtils.EMPTY; //$NON-NLS-1$
        Matcher m = JobAware.jobVersionNamePattern.matcher(jobEntityName);
        while (m.find()) {
            jobName = m.group(1);
            jobVersion = m.group(m.groupCount());
        }

        JobInfo jobInfo = new JobInfo(jobName, jobVersion);

        if (this.jobLoadersPool.containsKey(jobInfo)) {
            JobClassLoader jobClassLoader = jobLoadersPool.get(jobInfo);
            LOGGER.info("Removing class loader " + jobClassLoader); //$NON-NLS-1$
            jobLoadersPool.remove(jobInfo);
        }
    }

    public JobAware getJobAware() {
        return jobAware;
    }

    public JobDeploy getJobDeployer() {
        return jobDeploy;
    }

    /**
     * Return the {@link JobInvoker} implementation to execute the job.
     *
     * @param jobName A job name
     * @param version A job version
     * @return A {@link JobInvoker} implementation depending on job.
     * @throws JobNotFoundException if {@link #getJobInfo(String, String)} returns null (i.e. the job does not exist).
     */
    public JobInvoker getJobInvoker(String jobName, String version) {
        JobInfo jobInfo = getJobInfo(jobName, version);
        if (jobInfo == null) {
            throw new JobNotFoundException(jobName, version);
        }
        Class jobClass = getJobClass(jobInfo);
        Class[] interfaces = jobClass.getInterfaces();
        if (interfaces.length > 0) {
            for (Class currentInterface : interfaces) {
                if ("routines.system.api.TalendMDMJob".equals(currentInterface.getName())) { //$NON-NLS-1$
                    return new MDMJobInvoker(jobName, version);
                }
            }
        }
        // Default invocation
        return new JobInvoke(jobName, version);
    }

    /**
     * @return Returns the deploy directory for the jobs.
     * @see com.amalto.core.jobox.util.JoboxConfig#getDeployPath()
     */
    public String getDeployDir() {
        return joboxConfig.getDeployPath();
    }

    /**
     * @return Returns the deploy directory for the jobs.
     * @see com.amalto.core.jobox.util.JoboxConfig#getWorkPath()
     */
    public String getWorkDir() {
        return joboxConfig.getWorkPath();
    }

    /**
     * @param jobName    A job name
     * @param jobVersion A job version
     * @return Returns the {@link JobInfo} instance for this job if it exists, <code>null</code> otherwise.
     */
    public JobInfo getJobInfo(String jobName, String jobVersion) {
        JobInfo jobInfoPK = new JobInfo(jobName, jobVersion);
        Set<JobInfo> jobInformation = jobLoadersPool.keySet();
        for (JobInfo jobInfo : jobInformation) {
            if (jobInfo.equals(jobInfoPK)) {
                return jobInfo;
            }
        }
        return null;
    }

    /**
     * @return Returns information about all jobs deployed in this Jobox container.
     */
    public Set<JobInfo> getAllJobInfo() {
        return jobLoadersPool.keySet();
    }

    /**
     * <p>
     * Loads the job main class using a specific class loader (isolated from caller class loader).
     * </p>
     * <p>
     * Please note that this method also changes {@link Thread#getContextClassLoader()} <b>during</b> load, if
     * caller's class loader isn't already the job class loader (as returned by {@link #getJobClassLoader(JobInfo)}).
     * </p>
     * <p>
     * Once this method is completed {@link Thread#getContextClassLoader()} is equals to the caller's class loader.
     * </p>
     *
     * @param jobInfo A {@link JobInfo} instance.
     * @return The entry point class for job execution.
     */
    public Class getJobClass(JobInfo jobInfo) {
        ClassLoader previousCallLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader jobClassLoader = getJobClassLoader(jobInfo);

        try {
            if (previousCallLoader != jobClassLoader) { // TMDM-1733: Change context class loader during job class load.
                Thread.currentThread().setContextClassLoader(jobClassLoader);
            }
            return jobClassLoader.loadClass(jobInfo.getMainClass());
        } catch (ClassNotFoundException e) {
            LOGGER.error("Could not find class '" + jobInfo.getMainClass() + "'", e);
            return null;
        } finally {
            if (previousCallLoader != jobClassLoader) {
                Thread.currentThread().setContextClassLoader(previousCallLoader);
            }
        }
    }

    public ClassLoader getJobClassLoader(JobInfo jobInfo) {
        return this.jobLoadersPool.get(jobInfo);
    }

    public void setContextStrToBeSaved(String entryPath, String cxt) {
        monitor.changeContextStr(entryPath, cxt);
        int idxSeparator = entryPath.lastIndexOf(File.separatorChar);
        if (idxSeparator != -1) {
            String entryName = entryPath.substring(idxSeparator + 1);
            // undeploy
            JobContainer.getUniqueInstance().getJobDeployer().undeploy(entryName);
            // remove classpath
            JobContainer.getUniqueInstance().removeFromJobLoadersPool(entryName);
            jobDeploy.deploy(entryName);
            // add to classpath
            JobInfo jobInfo = JobContainer.getUniqueInstance().getJobAware().loadJobInfo(JoboxUtil.trimExtension(entryName));
            JobContainer.getUniqueInstance().updateJobLoadersPool(jobInfo);
        }

    }

    /**
     * Locks repository <i>if needed</i>, this depends on <code>forModification</code> if:
     * <ul>
     * <li>true: calling thread is a deploy thread as it's trying to modify the container.</li>
     * <li>false: calling thread is an executor thread only trying to see if no 'deploy' thread is working on container</li>
     * </ul>
     * <p>
     * If calling thread is a deploy thread, it will wait for all executors to complete (whatever job they may be executing).
     * Once wait is over, the thread will acquire all execution permits so no executor thread can run before the deploy
     * thread is complete.
     * </p>
     * @param forModification <code>true</code> if calling thread is a deploy thread (a thread that performs modification
     *                        on the container), <code>false</code> otherwise.
     * @see #unlock(boolean)
     */
    public void lock(boolean forModification) {
        try {
            if (forModification) {
                synchronized (executionCount) {
                    while (executionCount.get() > 0) {
                        log("[MOD] " + Thread.currentThread().getName() + " waiting for executors to finish"); //$NON-NLS-1$ //$NON-NLS-2$
                        executionCount.wait();
                    }
                }
                synchronized (modificationLock) {
                    while (modificationLock.get()) {
                        log("[MOD] " + Thread.currentThread().getName() + " waiting for exclusive modification lock"); //$NON-NLS-1$ //$NON-NLS-2$
                        modificationLock.wait();
                    }
                    modificationLock.set(true);
                }
                log("[MOD] " + Thread.currentThread().getName() + " acquired exclusive modification lock"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                synchronized (modificationLock) {
                    while (modificationLock.get()) {
                        log("[EXE] " + Thread.currentThread().getName() + " waiting for exclusive modification lock release."); //$NON-NLS-1$ //$NON-NLS-2$
                        modificationLock.wait();
                    }
                }
                synchronized (executionCount) {
                    executionCount.getAndIncrement();
                }
                log("[EXE] " + Thread.currentThread().getName() + " acquired execution lock"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (InterruptedException e) {
            throw new JoboxException(e);
        }
    }

    /**
     * Unlocks repository, action actually performed depends on <code>forModification</code>, if:
     * <ul>
     * <li>true: calling thread is a deploy thread as it's trying to modify the container.</li>
     * <li>false: calling thread is an executor thread only trying to see if no 'deploy' thread is working on container</li>
     * </ul>
     * <p>
     * If calling thread is a deploy thread, it will release exclusive lock so all executors threads can now
     * run.
     * </p>
     * <p>
     * If calling thread is a executor thread, it will decrease the count of current executors threads and wake up any
     * thread waiting for executor count = 0.
     * </p>
     *
     * @param forModification <code>true</code> if calling thread is a deploy thread (a thread that performs modification
     *                        on the container), <code>false</code> otherwise.
     * @see #lock(boolean)
     */
    public void unlock(boolean forModification) {
        if (forModification) {
            synchronized (modificationLock) {
                modificationLock.set(false);
                modificationLock.notifyAll();
            }
            log("[MOD] " + Thread.currentThread().getName() + " released exclusive modification lock."); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            synchronized (executionCount) {
                executionCount.getAndDecrement();
                executionCount.notifyAll();
            }
            log("[EXE] " + Thread.currentThread().getName() + " released executor lock"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static synchronized void log(String msg) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(msg);
        }
    }

    public void close() {
        jobLoadersPool.clear();
        monitor.stop();
    }
}
