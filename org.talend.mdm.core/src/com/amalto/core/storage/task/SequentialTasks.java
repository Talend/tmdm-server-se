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

import com.amalto.core.query.user.Condition;
import com.amalto.core.query.user.UserQueryHelper;
import org.springframework.security.core.context.SecurityContext;

public class SequentialTasks implements Task {

    private final Task[] tasks;

    public SequentialTasks(Task... tasks) {
        this.tasks = tasks;
    }

    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        for (Task task : tasks) {
            builder.append(task.getId());
        }
        return builder.toString();
    }

    @Override
    public int getRecordCount() {
        int sum = 0;
        for (Task task : tasks) {
            sum += task.getRecordCount();
        }
        return sum;
    }

    @Override
    public int getErrorCount() {
        int sum = 0;
        for (Task task : tasks) {
            sum += task.getErrorCount();
        }
        return sum;
    }

    @Override
    public int getProcessedRecords() {
        int sum = 0;
        for (Task task : tasks) {
            sum += task.getProcessedRecords();
        }
        return sum;
    }

    @Override
    public double getPerformance() {
        int sum = 0;
        for (Task task : tasks) {
            sum += task.getRecordCount();
        }
        return sum / tasks.length;
    }

    @Override
    public void cancel() {
        for (Task task : tasks) {
            task.cancel();
        }
    }

    @Override
    public void waitForCompletion() throws InterruptedException {
        for (Task task : tasks) {
            task.waitForCompletion();
        }
    }

    @Override
    public long getStartDate() {
        long startDate = Long.MAX_VALUE;
        for (Task task : tasks) {
            startDate = Math.min(task.getStartDate(), startDate);
        }
        return startDate;
    }

    @Override
    public boolean hasFinished() {
        for (Task task : tasks) {
            if (!task.hasFinished()) {
                return false;
            }
        }
        return true;
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
    public Condition getDefaultFilter() {
        return UserQueryHelper.TRUE;
    }

    @Override
    public void run() {
        for (Task task : tasks) {
            task.run();
            try {
                task.waitForCompletion();
            } catch (InterruptedException e) {
                throw new RuntimeException("Unable to sequentially run tasks.");
            }
        }
    }
}
