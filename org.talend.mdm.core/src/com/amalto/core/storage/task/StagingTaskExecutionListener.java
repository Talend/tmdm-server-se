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

/**
 * {@link StagingTask} events listener
 */
public interface StagingTaskExecutionListener {
    
    /**
     * Called when the task is ready to start or just started
     * @param task
     */
    public void taskStarted(StagingTask task);
    
    /**
     * Called when the task is finished
     * @param task
     * @param stats
     */
    public void taskCompleted(StagingTask task, ClosureExecutionStats stats);

}
