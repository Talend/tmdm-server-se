/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.task;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContext;

import com.amalto.core.query.user.Condition;
import com.amalto.core.storage.Storage;

/**
 *
 */
public class StagingTask implements Task {

    private static final Logger LOGGER = Logger.getLogger(StagingTask.class);

    private final TaskSubmitter taskSubmitter;

    private final Storage stagingStorage;

    private final String executionId;

    private boolean isCancelled = false;

    private final List<Task> tasks;

    private final AtomicBoolean startLock = new AtomicBoolean();

    private final AtomicBoolean executionLock = new AtomicBoolean();

    private final Object currentTaskMonitor = new Object();

    private final AtomicInteger recordCount = new AtomicInteger();

    private final ClosureExecutionStats stats;

    private Task currentTask;

    private long startTime;

    private boolean isFinished;
    
    private StagingTaskExecutionListener executionListener;
    
    public StagingTask(TaskSubmitter taskSubmitter, Storage stagingStorage, List<Task> tasks, 
            ClosureExecutionStats stats, StagingTaskExecutionListener executionListener) {
        this.taskSubmitter = taskSubmitter;
        this.stagingStorage = stagingStorage;
        this.stats = stats;
        this.executionId = UUID.randomUUID().toString();
        this.tasks = tasks;
        this.executionListener = executionListener;
        // Start recording the execution
        recordExecutionStart();
    }

    @Override
    public String getId() {
        return executionId;
    }

    @Override
    public int getRecordCount() {
        synchronized (currentTaskMonitor) {
            if (currentTask != null && currentTask instanceof MDMValidationTask) {
                recordCount.set(currentTask.getRecordCount());
            }
            return recordCount.get();
        }
    }

    @Override
    public int getErrorCount() {
        return stats.getErrorCount();
    }

    @Override
    public double getPerformance() {
        return (getRecordCount()) / ((System.currentTimeMillis() - startTime) / 1000f);
    }

    @Override
    public void cancel() {
        synchronized (currentTaskMonitor) {
            isCancelled = true;
            // we are still in the main execution loop, it will record stats by itself 
            if (currentTask != null) {
                currentTask.cancel();
            }
            else {
                // Ensure cancel execution stats are stored to database.
                recordExecutionEnd(stats);
            }
            synchronized (startLock) {
                startLock.notifyAll();
            }
            synchronized (executionLock) {
                executionLock.notifyAll();
            }
        }
    }

    @Override
    public void waitForCompletion() throws InterruptedException {
        while (!startLock.get()) {
            synchronized (startLock) {
                startLock.wait();
            }
        }
        while (!executionLock.get()) {
            synchronized (executionLock) {
                executionLock.wait();
            }
        }
    }

    @Override
    public long getStartDate() {
        return startTime;
    }

    @Override
    public boolean hasFinished() {
        return isFinished;
    }

    @Override
    public Condition getDefaultFilter() {
        synchronized (currentTaskMonitor) {
            return currentTask.getDefaultFilter();
        }
    }

    @Override
    public boolean hasFailed() {
        for (Task task : tasks) {
            if (task.hasFailed()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setSecurityContext(SecurityContext context) {
        for (Task task : tasks) {
            task.setSecurityContext(context);
        }
    }

    @Override
    public int getProcessedRecords() {
        return stats.getErrorCount() + stats.getSuccessCount();
    }

    @Override
    public void run() {
        synchronized (startLock) {
            startLock.set(true);
            startLock.notifyAll();
        }
        try {
            for (Task task : tasks) {
                synchronized (currentTaskMonitor) {
                    if (isCancelled) {
                        break;
                    }
                    currentTask = task;
                }
                LOGGER.info("--> " + task.toString());
                long taskExecTime = System.currentTimeMillis();
                {
                    taskSubmitter.submitAndWait(currentTask);
                }
                LOGGER.info("<-- DONE " + task.toString() + " (elapsed time: " + (System.currentTimeMillis() - taskExecTime)
                        + " ms)");
                if (currentTask.hasFailed()) {
                    LOGGER.warn("Task '" + currentTask + "' failed: abort staging validation task.");
                    break;
                }
            }
            recordExecutionEnd(stats);
        } finally {
            synchronized (executionLock) {
                executionLock.set(true);
                executionLock.notifyAll();
            }
            isFinished = true;
            synchronized (currentTaskMonitor) {
                currentTask = null;
            }
        }
    }
    
    public String getDataContainer(){
        return this.stagingStorage.getName();
    }

    private void recordExecutionStart() {
        this.startTime = System.currentTimeMillis();
        this.executionListener.taskStarted(this);
    }

    private void recordExecutionEnd(ClosureExecutionStats stats) {
        this.executionListener.taskCompleted(this, stats);
    }
}
