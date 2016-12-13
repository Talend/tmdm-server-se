/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.task.staging;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.amalto.core.storage.task.ClosureExecutionStats;
import com.amalto.core.storage.task.StagingTask;
import com.amalto.core.storage.task.Task;

/**
 * Simple implementation of {@link StagingTaskManager} where tasks are all running in the same node.
 */
public class LocalStagingTaskManager extends AbstractStagingTaskManager {
    
    private final Map<String, Task> runningTasks = new HashMap<String, Task>();
    
    @Override
    public String getCurrentTaskId(String dataContainer) {
        final Task currentTask = this.getCurrentTask(dataContainer);
        if(currentTask != null){
            return currentTask.getId();
        }
        return null;
    }

    @Override
    public void cancelTask(String dataContainer, String taskId) {
        synchronized (runningTasks) {
            Task task = this.getCurrentTask(dataContainer);
            if (task == null || !task.getId().equals(taskId)) {
                return;
            }
            task.cancel();
        }
    }

    public ExecutionStatistics getExecutionStats(String dataContainer, String executionId) {
        // TMDM-4827: Returns current execution if execution id is current execution.
        synchronized (runningTasks) {
            Task declaredTask = this.getCurrentTask(dataContainer);
            if (declaredTask != null && declaredTask.getId().equals(executionId)) {
                return this.getExecutionStatsForTask(declaredTask);
            } else {
                return this.getRepository().getExecutionStats(dataContainer, executionId);
            }
        }
    }
    
    @Override
    public void taskStarted(StagingTask task) {
        synchronized (runningTasks) {
            String dataContainer = task.getDataContainer();
            Task currentTask = runningTasks.get(dataContainer);
            if(currentTask != null){
                if(currentTask.hasFinished()){
                    runningTasks.remove(dataContainer);
                }
                else {
                    throw new IllegalStateException(String.format("There is already a running task (id=%s) for container %s", currentTask.getId(), dataContainer));
                }
            }
            runningTasks.put(dataContainer, task);
        }
        super.taskStarted(task);
    }

    @Override
    public void taskCompleted(StagingTask task, ClosureExecutionStats stats) {
        synchronized (runningTasks) {
            String dataContainer = task.getDataContainer();
            Task currentTask = runningTasks.get(dataContainer);
            if(currentTask != null && currentTask.getId().equals(task.getId())){
                runningTasks.remove(dataContainer);
                super.taskCompleted(task, stats);
            }
            else {
                throw new RuntimeException("Unknown task " + task.getId() + " on container " + dataContainer);
            }
        }
        
    }
    
    private Task getCurrentTask(final String dataContainer){
        synchronized (runningTasks) {
            Task task = runningTasks.get(dataContainer);
            if (task == null) {
                return null;
            } else if (task.hasFinished()) {
                runningTasks.remove(dataContainer);
                return null;
            }
            return task;
        }
    }
    
    private ExecutionStatistics getExecutionStatsForTask(final Task task){
        ExecutionStatistics status = new ExecutionStatistics();
        status.setId(task.getId());
        status.setProcessedRecords(task.getProcessedRecords());
        status.setTotalRecords(task.getRecordCount());
        status.setInvalidRecords(task.getErrorCount());
        long elapsedTime = System.currentTimeMillis() - task.getStartDate();
        String formattedElapsedTime = StagingTasksUtil.formatElapsedTime(elapsedTime);
        status.setRunningTime(formattedElapsedTime);
        long timeLeft = (long) ((task.getRecordCount() - task.getProcessedRecords()) / task.getPerformance());
        status.setStartDate(StagingTasksUtil.formatDate(new Date(task.getStartDate())));
        long endTime = (System.currentTimeMillis() + timeLeft);
        status.setEndDate(StagingTasksUtil.formatDate(new Date(endTime)));
        return status;
    }

}
