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

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.amalto.core.storage.task.ClosureExecutionStats;
import com.amalto.core.storage.task.StagingTask;


public class LocalStagingTaskManagerTestCase extends AbstractStagingTaskManagerTastCase {
    
    private LocalStagingTaskManager taskManager;
    
    private StagingTaskRepository repository;
    
    @Before
    public void setUp() throws Exception {
        repository = Mockito.mock(StagingTaskRepository.class);
        taskManager = new LocalStagingTaskManager();
        taskManager.setRepository(repository);
    }
    
    @After
    public void tearDown() throws Exception {
        this.taskManager = null;
        this.repository = null;
    }
    
    @Test
    public void testTaskStartedAndCompleted() throws Exception {
        String id = "12345";
        String container = "container";
        long startDate = System.currentTimeMillis();
        StagingTask task = createTask(container, id, startDate);
        // start a new task
        this.taskManager.taskStarted(task);
        Mockito.verify(repository, Mockito.times(1)).saveNewTask(container, id, startDate);;
        Assert.assertEquals(id, this.taskManager.getCurrentTaskId(container));
        
        // task is finished
        this.taskManager.taskCompleted(task, this.createStats(12354));
        Assert.assertNull(this.taskManager.getCurrentTaskId(container));
        Mockito.verify(repository, Mockito.times(1)).saveTaskAsCompleted(container, id, 12354, 0, 0);
    }
    
    @Test(expected=RuntimeException.class)
    public void testUnknownTaskCompleted() throws Exception {
        // when declaring a task is completed on an unknown task, it should throw an Exception
        this.taskManager.taskCompleted(createTask("container", "ABC", 0), new ClosureExecutionStats());
        Mockito.verifyNoMoreInteractions(repository);
    }
    
    @Test
    public void testUnknownTaskCancelled() throws Exception {
        // when cancelling an unknown task, it should be ignored
        this.taskManager.cancelTask("container", "ABCD");
        Mockito.verifyNoMoreInteractions(repository);
    }
    
    @Test
    public void testDifferentTaskCancelled() throws Exception {
        String id = "12345";
        String container = "container";
        long startDate = System.currentTimeMillis();
        StagingTask task = createTask(container, id, startDate);
        // start a new task
        this.taskManager.taskStarted(task);
        Mockito.verify(repository, Mockito.times(1)).saveNewTask(container, id, startDate);;
        this.taskManager.cancelTask("container", "ABCD");
        Mockito.verifyNoMoreInteractions(repository);
    }
    
    @Test
    public void testTaskStartedAndCancelled() throws Exception {
        String id = "12345";
        String container = "container";
        long startDate = System.currentTimeMillis();
        StagingTask task = createTask(container, id, startDate);
        // start a new task
        this.taskManager.taskStarted(task);
        Mockito.verify(repository, Mockito.times(1)).saveNewTask(container, id, startDate);
        Assert.assertEquals(id, this.taskManager.getCurrentTaskId(container));
        
        // task is cancelled
        this.taskManager.cancelTask(container, id);
        // the current task is still the same
        Assert.assertEquals(id, this.taskManager.getCurrentTaskId(container));
        // database is not updated
        Mockito.verifyNoMoreInteractions(repository);
    }
    
    @Test
    public void testCurrentTaskCompleted() throws Exception {
        String id = "12345";
        String container = "container";
        long startDate = System.currentTimeMillis();
        StagingTask task = createTask(container, id, startDate);
        // start a new task
        this.taskManager.taskStarted(task);
        Mockito.verify(repository, Mockito.times(1)).saveNewTask(container, id, startDate);
        Assert.assertEquals(id, this.taskManager.getCurrentTaskId(container));
        
        Mockito.when(task.hasFinished()).thenReturn(Boolean.TRUE);
        Assert.assertNull(this.taskManager.getCurrentTaskId(container));
    }
    
    @Test(expected=IllegalStateException.class)
    public void testStartTwoTasks() throws Exception {
        String id = "12345";
        String container = "container";
        long startDate = System.currentTimeMillis();
        StagingTask task = createTask(container, id, startDate);
        // start a new task
        this.taskManager.taskStarted(task);
        Mockito.verify(repository, Mockito.times(1)).saveNewTask(container, id, startDate);
        Assert.assertEquals(id, this.taskManager.getCurrentTaskId(container));
        
        StagingTask newtask = createTask(container, "67890", startDate);
        this.taskManager.taskStarted(newtask);
    }
    
    
    @Test()
    public void testStartWhileOtherCompleted() throws Exception {
        String id = "12345";
        String container = "container";
        long startDate = System.currentTimeMillis();
        StagingTask task = createTask(container, id, startDate);
        // start a new task
        this.taskManager.taskStarted(task);
        Mockito.verify(repository, Mockito.times(1)).saveNewTask(container, id, startDate);
        Assert.assertEquals(id, this.taskManager.getCurrentTaskId(container));
        
        Mockito.when(task.hasFinished()).thenReturn(Boolean.TRUE);
        String newId = "67890";
        long newStartDate = System.currentTimeMillis();
        StagingTask newtask = createTask(container, newId, newStartDate);
        this.taskManager.taskStarted(newtask);
        Mockito.verify(repository, Mockito.times(1)).saveNewTask(container, newId, newStartDate);
        Assert.assertEquals(newId, this.taskManager.getCurrentTaskId(container));
    }
}
