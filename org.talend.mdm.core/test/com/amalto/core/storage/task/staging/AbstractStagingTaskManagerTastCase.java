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

import org.mockito.Mockito;

import com.amalto.core.storage.task.ClosureExecutionStats;
import com.amalto.core.storage.task.StagingTask;


public abstract class AbstractStagingTaskManagerTastCase {
    
    protected StagingTask createTask(String dataContainer, String taskId, long startDate){
        StagingTask task = Mockito.mock(StagingTask.class);
        Mockito.when(task.getId()).thenReturn(taskId);
        Mockito.when(task.getDataContainer()).thenReturn(dataContainer);
        Mockito.when(task.getStartDate()).thenReturn(startDate);
        return task;
    }
    
    protected ClosureExecutionStats createStats(long endMatchTime){
        ClosureExecutionStats stats = Mockito.mock(ClosureExecutionStats.class);
        Mockito.when(stats.getEndMatchTime()).thenReturn(endMatchTime);
        return stats;
        
    }

}
