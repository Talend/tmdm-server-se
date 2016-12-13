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

import com.amalto.core.jobox.component.JobAware;
import com.amalto.core.jobox.component.JobInvoker;
import com.amalto.core.jobox.component.MDMJobInvoker;
import com.amalto.core.jobox.properties.ThreadIsolatedSystemProperties;
import com.amalto.core.jobox.util.JobNotFoundException;
import com.amalto.core.jobox.util.JoboxConfig;
import junit.framework.TestCase;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

@SuppressWarnings({"nls", "ResultOfMethodCallIgnored"})
public class JoboxTest extends TestCase {

    public static final String JOBOX_TEST_DIR = "/tmp/jobox";

    private JobContainer jobContainer;

    private Properties previousProperties;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        previousProperties = System.getProperties();
        System.setProperties(ThreadIsolatedSystemProperties.getInstance());
        jobContainer = JobContainer.getUniqueInstance();
        Properties props = new Properties();
        props.put(JoboxConfig.JOBOX_HOME_PATH, JOBOX_TEST_DIR);
        jobContainer.init(props);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        jobContainer.getJobDeployer().undeployAll();

        undeployAllFiles();

        jobContainer.close();
        System.setProperties(previousProperties);
    }

    private static void undeployAllFiles() {
        File deployDir = new File(JOBOX_TEST_DIR + "/deploy");
        File[] files = deployDir.listFiles();
        for (File file : files) {
            file.delete();
        }
    }

    public void testInit() throws Exception {
        File deployDir = new File(jobContainer.getDeployDir());
        assertTrue(deployDir.exists());

        File workDir = new File(jobContainer.getWorkDir());
        assertTrue(workDir.exists());
    }

    public void testSuccessfulDeploy() throws Exception {
        //init
        Properties props = new Properties();
        props.put(JoboxConfig.JOBOX_HOME_PATH, JOBOX_TEST_DIR);
        deployFileToJobox("testJob1.zip");
        jobContainer.init(props);

        JobInfo jobInfo = jobContainer.getJobInfo("TestTalendMDMJob", "0.1");
        assertNotNull(jobInfo);
        assertEquals("TestTalendMDMJob", jobInfo.getName());
        assertEquals("0.1", jobInfo.getVersion());
        assertEquals("tests.testtalendmdmjob_0_1.TestTalendMDMJob", jobInfo.getMainClass());
        assertEquals("Default", jobInfo.getContextStr());
        assertTrue(jobInfo.getDefaultParamMap().isEmpty());

        Set<JobInfo> allJobs = jobContainer.getAllJobInfo();
        assertEquals(1, allJobs.size());
        assertEquals(jobInfo, allJobs.iterator().next());


    }

    public void testJobClass() throws Exception {
        //init
        Properties props = new Properties();
        props.put(JoboxConfig.JOBOX_HOME_PATH, JOBOX_TEST_DIR);
        deployFileToJobox("testJob1.zip");
        jobContainer.init(props);

        JobInfo jobInfo = jobContainer.getJobInfo("TestTalendMDMJob", "0.1");
        assertNotNull(jobInfo);

        Class<?> jobClass = jobContainer.getJobClass(jobInfo);
        assertNotSame(Thread.currentThread().getContextClassLoader(), jobClass.getClassLoader());
    }

    public void testJobAware() throws Exception {
        //init
        Properties props = new Properties();
        props.put(JoboxConfig.JOBOX_HOME_PATH, JOBOX_TEST_DIR);
        deployFileToJobox("testJob1.zip");
        jobContainer.init(props);

        JobAware jobAware = jobContainer.getJobAware();
        List<JobInfo> jobsInBox = jobAware.findJobsInBox();
        assertEquals(1, jobsInBox.size());
        assertEquals("TestTalendMDMJob", jobsInBox.get(0).getName());
        assertEquals("0.1", jobsInBox.get(0).getVersion());

        JobInfo jobInfo = jobAware.loadJobInfo("TestTalendMDMJob");
        assertNull(jobInfo);

        jobInfo = jobAware.loadJobInfo("TestTalendMDMJob_0.1");
        assertNotNull(jobInfo);
        assertEquals("TestTalendMDMJob", jobInfo.getName());
        assertEquals("0.1", jobInfo.getVersion());
        
        //test main class
        assertEquals("tests.testtalendmdmjob_0_1.TestTalendMDMJob", jobInfo.getMainClass());
    }

    public void testExecuteMDMJob() throws Exception {
        //init
        Properties props = new Properties();
        props.put(JoboxConfig.JOBOX_HOME_PATH, JOBOX_TEST_DIR);
        deployFileToJobox("testJob1.zip");
        jobContainer.init(props);

        JobInfo jobInfo = jobContainer.getJobInfo("TestTalendMDMJob", "0.1");
        assertNotNull(jobInfo);

        Class<?> jobClass = jobContainer.getJobClass(jobInfo);
        assertNotNull(jobClass);
        assertEquals("tests.testtalendmdmjob_0_1.TestTalendMDMJob", jobClass.getName());

        assertNotSame(Thread.currentThread().getContextClassLoader(), jobClass.getClassLoader());

        Class<?>[] interfaces = jobClass.getInterfaces();
        assertEquals(1, interfaces.length);
        assertEquals("routines.system.api.TalendMDMJob", interfaces[0].getName());

        JobInvoker invoker = jobContainer.getJobInvoker("TestTalendMDMJob", "0.1");
        assertEquals(MDMJobInvoker.class, invoker.getClass());

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(MDMJobInvoker.EXCHANGE_XML_PARAMETER, "<exchange><report></report><item><key>0</key></item></exchange>");

        String[][] result = invoker.call(parameters);
        assertEquals(1, result.length);
        assertEquals(1, result[0].length);
        assertEquals("<exchange><report/><item><key>0</key></item></exchange>", result[0][0]);

        result = invoker.call();
        assertEquals(1, result.length);
        assertEquals(1, result[0].length);
        assertEquals("0", result[0][0]);

        assertNotSame(Thread.currentThread().getContextClassLoader(), jobClass.getClassLoader());
    }

    // This test is too I/O stressful and can cause build to fail when run in a test suite. Disables it
    public void __testDeployExecuteUndeploy() throws Exception {
        Properties props = new Properties();
        props.put(JoboxConfig.JOBOX_HOME_PATH, JOBOX_TEST_DIR);
        deployFileToJobox("TestTalendMDMJob_0.1.zip");
        jobContainer.init(props);

        Set<DeployUndeployRunnable> deployUndeployTests = new HashSet<DeployUndeployRunnable>();
        for (int i = 0; i < 1; i++) {
            deployUndeployTests.add(new DeployUndeployRunnable());
        }
        Set<ExecuteRunnable> executeTests = new HashSet<ExecuteRunnable>();
        for (int i = 0; i < 5; i++) {
            executeTests.add(new ExecuteRunnable());
        }

        Set<Thread> threads = new HashSet<Thread>();
        for (Runnable currentRunnable : deployUndeployTests) {
            threads.add(new Thread(currentRunnable));
        }
        for (Runnable currentRunnable : executeTests) {
            threads.add(new Thread(currentRunnable));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        for (DeployUndeployRunnable test : deployUndeployTests) {
            assertEquals(0, test.errorCount);
        }
        for (ExecuteRunnable test : executeTests) {
            assertEquals(0, test.errorCount);
        }
    }

    public void testFailExecuteMDMJob() throws Exception {
        //init
        Properties props = new Properties();
        props.put(JoboxConfig.JOBOX_HOME_PATH, JOBOX_TEST_DIR);
        deployFileToJobox("testJob1.zip");
        jobContainer.init(props);

        JobInfo jobInfo = jobContainer.getJobInfo("TestTalendMDMJob_NonExistent", "0.1");
        assertNull(jobInfo);

        try {
            jobContainer.getJobInvoker("TestTalendMDMJob_NonExistent", "0.1");
            fail("Job does not exist");
        } catch (JobNotFoundException e) {
            // Expected
        }

        try {
            jobContainer.getJobInvoker("TestTalendMDMJob", "NonExistentVersion");
            fail("Job version does not exist");
        } catch (JobNotFoundException e) {
            // Expected
        }
    }

    private static void deployFileToJobox(String jobZipFile) throws URISyntaxException, IOException {
        URL resource = JoboxTest.class.getResource(jobZipFile);
        assertNotNull(resource);
        URI uri = resource.toURI();
        File jobFile = new File(uri);

        // Copy zip file to jobox dir
        InputStream jobFileInputStream = null;
        OutputStream deployedFileOutputStream = null;
        File deployedFile = new File(JOBOX_TEST_DIR  + "/deploy/" + jobZipFile);
        if (!deployedFile.exists()) {
            deployedFile.createNewFile();
        }
        try {
            jobFileInputStream = new FileInputStream(jobFile);
            deployedFileOutputStream = new FileOutputStream(deployedFile);
            byte[] buffer = new byte[1024];
            int byteRead;
            while ((byteRead = jobFileInputStream.read(buffer)) > 0) {
                deployedFileOutputStream.write(buffer, 0, byteRead);
            }
            deployedFileOutputStream.flush();
        } finally {
            if (jobFileInputStream != null) {
                jobFileInputStream.close();
            }
            if (deployedFileOutputStream != null) {
                deployedFileOutputStream.close();
            }
        }
    }

    public void testDeployJob() throws InterruptedException {
        // init
        Properties props = new Properties();
        props.put(JoboxConfig.JOBOX_HOME_PATH, JOBOX_TEST_DIR);
        jobContainer.init(props);

        try {
            jobContainer.getJobDeployer().deploy("testJob1.zip");
        } catch (Exception e) {
            // Expected
        }
    }

    private static class DeployUndeployRunnable implements Runnable {
        private int errorCount = 0;

        private final static Object lock = new Object();
        
        public void run() {
            for (int i = 0; i < 10; i++) {
                synchronized (lock) {
                    try {
                        undeployAllFiles();
                        Thread.sleep((long) (Math.random() * 1000));
                        deployFileToJobox("TestTalendMDMJob_0.1.zip");
                    } catch (Exception e) {
                        errorCount++;
                    }
                }
            }
        }
    }

    private class ExecuteRunnable implements Runnable {
        private int errorCount = 0;

        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    HashMap<String, String> parameters = new HashMap<String, String>();
                    parameters.put(MDMJobInvoker.EXCHANGE_XML_PARAMETER, "<exchange><report></report><item><key>0</key></item></exchange>");
                    JobInvoker invoker;
                    try {
                        invoker = jobContainer.getJobInvoker("TestTalendMDMJob", "0.1");
                        invoker.call(parameters);
                    } catch (JobNotFoundException e) {
                        // Might happen (in case we executed right after a undeploy).
                    }
                    Thread.sleep((long) (Math.random() * 1000));
                } catch (Exception e) {
                    errorCount++;
                }
            }
        }
    }
}
