/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox.properties;

import junit.framework.TestCase;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class SystemPropertiesIsolationTest extends TestCase {

    public static final int TEST_TIMES = 5;

    private Properties previous;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        previous = System.getProperties();
        ThreadIsolatedSystemProperties threadIsolatedProperties = ThreadIsolatedSystemProperties.getInstance();
        System.setProperties(threadIsolatedProperties);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        System.setProperties(previous);
    }

    public void testIsolation() throws Exception {
        SystemModifierRunnable run1 = new SystemModifierRunnable("thread1");
        SystemModifierRunnable run2 = new SystemModifierRunnable("thread2");
        SystemModifierRunnable run3 = new SystemModifierRunnable("thread3");
        List<SystemModifierRunnable> runs = Arrays.asList(run1, run2, run3);

        List<Thread> threads = new ArrayList<Thread>(runs.size());
        for (SystemModifierRunnable run : runs) {
            Thread newThread = new Thread(run);
            newThread.setContextClassLoader(new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader()));
            ThreadIsolatedSystemProperties.getInstance().isolateThread(newThread, new Properties());
            threads.add(newThread);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        boolean hasOnlySuccess = hasOnlySuccess(runs);
        assertTrue(hasOnlySuccess);
    }

    public void testFailedIsolation() throws Exception {
        SystemModifierRunnable run1 = new SystemModifierRunnable("thread1");
        SystemModifierRunnable run2 = new SystemModifierRunnable("thread2");
        SystemModifierRunnable run3 = new SystemModifierRunnable("thread3");
        List<SystemModifierRunnable> runs = Arrays.asList(run1, run2, run3);

        List<Thread> threads = new ArrayList<Thread>(runs.size());
        for (SystemModifierRunnable run : runs) {
            Thread newThread = new Thread(run);
            newThread.setContextClassLoader(new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader()));
            threads.add(newThread);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        boolean hasOneFailed = hasMetFailure(runs);
        assertTrue(hasOneFailed);
    }

    public void testMixedIsolation() throws Exception {
        SystemModifierRunnable run1 = new SystemModifierRunnable("thread1");
        SystemModifierRunnable run2 = new SystemModifierRunnable("thread2");
        SystemModifierRunnable run3 = new SystemModifierRunnable("thread3");
        List<SystemModifierRunnable> isolatedRuns = Arrays.asList(run1, run2, run3);

        SystemModifierRunnable run4 = new SystemModifierRunnable("thread4");
        SystemModifierRunnable run5 = new SystemModifierRunnable("thread5");
        SystemModifierRunnable run6 = new SystemModifierRunnable("thread6");
        List<SystemModifierRunnable> nonIsolatedRuns = Arrays.asList(run4, run5, run6);

        List<Thread> threads = new ArrayList<Thread>(isolatedRuns.size());
        for (SystemModifierRunnable run : isolatedRuns) {
            Thread newThread = new Thread(run);
            newThread.setContextClassLoader(new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader()));
            ThreadIsolatedSystemProperties.getInstance().isolateThread(newThread, new Properties());
            threads.add(newThread);
        }
        for (SystemModifierRunnable nonIsolatedRun : nonIsolatedRuns) {
            Thread newThread = new Thread(nonIsolatedRun);
            threads.add(newThread);
        }


        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        boolean hasOnlySuccess = hasOnlySuccess(isolatedRuns);
        assertTrue(hasOnlySuccess);
        boolean hasOneFailed = hasMetFailure(nonIsolatedRuns);
        assertTrue(hasOneFailed);
    }

    public void testReintegration() throws Exception {
        SystemModifierRunnable run1 = new SystemModifierRunnable("thread1");
        SystemModifierRunnable run2 = new SystemModifierRunnable("thread2");
        SystemModifierRunnable run3 = new SystemModifierRunnable("thread3");
        List<SystemModifierRunnable> isolatedRuns = Arrays.asList(run1, run2, run3);

        System.setProperty("test.reintegration", "true");

        List<Thread> threads = new ArrayList<Thread>(isolatedRuns.size());
        for (SystemModifierRunnable run : isolatedRuns) {
            Thread newThread = new Thread(run);
            newThread.setContextClassLoader(new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader()));
            ThreadIsolatedSystemProperties.getInstance().isolateThread(newThread, new Properties());
            threads.add(newThread);
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        boolean hasOnlySuccess = hasOnlySuccess(isolatedRuns);
        assertTrue(hasOnlySuccess);

        Thread threadToIntegrate = threads.get(0);
        Properties properties = ThreadIsolatedSystemProperties.getInstance().getThreadProperties(threadToIntegrate);
        assertFalse(Boolean.valueOf(String.valueOf(properties.get("test.reintegration"))));
        ThreadIsolatedSystemProperties.getInstance().integrateThread(threadToIntegrate);
        properties = ThreadIsolatedSystemProperties.getInstance().getThreadProperties(threadToIntegrate);
        assertTrue(Boolean.valueOf(String.valueOf(properties.get("test.reintegration"))));
    }


    private boolean hasMetFailure(List<SystemModifierRunnable> runs) {
        boolean hasOneFailed = false;
        for (SystemModifierRunnable run : runs) {
            if (!run.isSuccess()) {
                hasOneFailed = true;
                break;
            }
        }
        return hasOneFailed;
    }

    private boolean hasOnlySuccess(List<SystemModifierRunnable> runs) {
        boolean hasOnlySuccess = true;
        for (SystemModifierRunnable run : runs) {
            if (!run.isSuccess()) {
                hasOnlySuccess = false;
            }
        }
        return hasOnlySuccess;
    }

    private static class SystemModifierRunnable implements Runnable {

        private final String value;

        private boolean isSuccess;

        public SystemModifierRunnable(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public void run() {
            for (int i = 0; i < TEST_TIMES; i++) {
                try {
                    System.setProperty("test.thread", value);
                    System.getProperties().put("test.thread", value);
                    Thread.sleep((long) (Math.random() * 1000l));
                    isSuccess = value.equals(System.getProperty("test.thread"));
                    isSuccess = isSuccess && value.equals(System.getProperties().get("test.thread"));

                    if (!isSuccess) {
                        break;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void reset() {
            isSuccess = true;
        }
    }

}
