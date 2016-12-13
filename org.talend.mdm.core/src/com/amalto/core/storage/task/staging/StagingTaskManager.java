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

import java.util.List;

import com.amalto.core.storage.task.StagingTask;
import com.amalto.core.storage.task.StagingTaskExecutionListener;

/**
 * {@link StagingTask} manager: keeps track of running tasks, cancels tasks ...
 */
public interface StagingTaskManager extends StagingTaskExecutionListener {
    
    /**
     * @return the unique id of the {@link StagingTask} being currently executed on data container.
     * If no task is currently running for this data container, returns null
     * 
     * @param dataContainer
     * 
     */
    public String getCurrentTaskId(String dataContainer);
    
    /**
     * Asks for the task's cancellation. Will return even if the task is not fully stopped.
     * Will not update current task so that {@link #getCurrentTaskId(String)} should still return
     * the same value as before until the task is really stopped.
     * If there is no corresponding running task, this method will have no effect. 
     * 
     * @param dataContainer
     * @param taskId
     */
    public void cancelTask(String dataContainer, String taskId);
    
    /**
     * @return execution statistics about the task (running or not) 
     * @param dataContainer
     * @param taskId
     * 
     */
    public ExecutionStatistics getExecutionStats(String dataContainer, String taskId);
    
    /**
     * @return the list of tasks' id executed on container and matching provided criteria
     * @param dataContainer
     * @param beforeDate
     * @param start
     * @param size
     * 
     */
    public List<String> listCompletedExecutions(String dataContainer, String beforeDate, int start, int size);
    
}
