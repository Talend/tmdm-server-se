/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.task;

import org.springframework.security.core.context.SecurityContext;

import java.util.Date;

/**
 *
 */
public interface Task extends Runnable, Filterable {

    /**
     * @return Returns a unique ID for this task.
     */
    String getId();

    /**
     * @return The total number of records the task should process. <b>Note:</b> This methods returns 0 if task have not
     *         yet been started.
     * @see com.amalto.core.storage.task.TaskSubmitterFactory#getSubmitter()
     * @see TaskSubmitter#submit(Task)
     * @see TaskSubmitter#submitAndWait(Task)
     */
    int getRecordCount();

    /**
     * @return The number of errors met during record processing. Number of success can be computed by
     * ({@link #getProcessedRecords()}).
     */
    int getErrorCount();

    /**
     * @return The number of records processed by the task <b>so far</b>.
     */
    int getProcessedRecords();

    /**
     * @return Returns how many records per <b>second</b> validated. This method may return different values over time.
     *         It returns 0 if task have not yet been run.
     */
    double getPerformance();

    /**
     * Cancel 'as soon as possible' the task. Implementations should exit as soon as possible and release lock due to
     * current run of task.
     */
    void cancel();

    /**
     * Wait until task is complete. Calling {@link #cancel()} from another <b>MUST</b> wake up all threads blocked on
     * this method.
     *
     * @throws InterruptedException
     */
    void waitForCompletion() throws InterruptedException;

    /**
     * @return When the task was started with a number that can be passed as is to {@link Date}.
     */
    long getStartDate();

    /**
     * @return <code>true</code> if task is finished (no more record to process) <b>OR</b> if the task was cancelled,
     *         <code>false</code> otherwise.
     * @see #cancel()
     */
    boolean hasFinished();


    /**
     * @return <code>true</code> if the task failed (did not finish without an error), <code>false</code> otherwise.
     */
    boolean hasFailed();

    void setSecurityContext(SecurityContext context);
}
