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

import java.util.concurrent.*;

import org.springframework.beans.factory.DisposableBean;

public class DefaultTaskSubmitter implements TaskSubmitter, DisposableBean {
    /*
     * Executor service with the following features:
     * - 1 thread minimum
     * - 4 thread max in pool
     * - Keep alive of 10 minutes (when pool exceeds 1 thread, threads inactive for more than 10 minutes are removed).
     * - A processing queue of 10 requests.
     */
    private final ExecutorService service = new ThreadPoolExecutor(1, 4, 10, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(
                                                  10));
    
    @Override
    public void submit(Task task) {
        service.submit(task);
    }

    @Override
    public void submitAndWait(Task task) {
        task.run();
        try {
            task.waitForCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException("Task did not successfully completed.", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        if(service != null){
            service.shutdown();
        }
    }
}
