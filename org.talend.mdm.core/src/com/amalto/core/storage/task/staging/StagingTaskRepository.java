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


public interface StagingTaskRepository {
    
    public void saveNewTask(String dataContainer, String taskId, long startTime);
    
    public void saveTaskAsCancelled(String dataContainer, String taskId);
    
    public void saveTaskAsCompleted(String dataContainer, String taskId, long endMatchTime, int errorCount, int recordCount);
    
    public String getCurrentTaskId(String dataContainer);
    
    public ExecutionStatistics getExecutionStats(String dataContainer, String executionId);
    
    public List<String> listCompletedExecutions(String dataContainer, String beforeDate, int start, int size);
    
    

}
