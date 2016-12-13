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

import org.springframework.security.core.context.SecurityContext;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import com.amalto.core.storage.Storage;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
abstract class MetadataRepositoryTask implements Task {

    private static final Logger LOGGER = Logger.getLogger(MetadataRepository.class);

    private final List<Task> tasks = new LinkedList<Task>();

    private final AtomicBoolean startLock = new AtomicBoolean();

    private final MetadataRepository repository;

    private final AtomicBoolean executionLock = new AtomicBoolean();

    private final String id = UUID.randomUUID().toString();

    private final Object currentTypeTaskMonitor = new Object();

    protected final Filter filter;

    private long startTime;

    private long endTime = -1;

    private boolean isCancelled = false;

    private Task currentTypeTask;

    private boolean isFinished;

    final ClosureExecutionStats stats;

    final Storage storage;

    MetadataRepositoryTask(Storage storage,
                           MetadataRepository repository,
                           ClosureExecutionStats stats,
                           Filter filter) {
        this.storage = storage.asInternal();
        this.repository = repository;
        this.stats = stats;
        this.filter = filter;
    }

    protected abstract Task createTypeTask(ComplexTypeMetadata type);

    @Override
    public void run() {
        synchronized (startLock) {
            startLock.set(true);
            startLock.notifyAll();
        }
        try {
            List<ComplexTypeMetadata> types = MetadataUtils.sortTypes(repository, MetadataUtils.SortType.LENIENT);
            for (ComplexTypeMetadata type : types) {
                if (!filter.exclude(type) && type.isInstantiable() && processType(type)) {
                    Task task = createTypeTask(type);
                    tasks.add(task);
                }
            }
            startTime = System.currentTimeMillis();
            for (Task task : tasks) {
                synchronized (currentTypeTaskMonitor) {
                    currentTypeTask = task;
                }
                if (!isCancelled) {
                    LOGGER.info("--> Executing " + task + "...");
                    task.run();
                    LOGGER.info("<-- Executed (" + task.getRecordCount() + " record validated @ " + getPerformance() + " doc/s)");
                }
            }
            endTime = System.currentTimeMillis();
            LOGGER.info("Staging migration done @" + getPerformance() + " doc/s.");
        } finally {
            synchronized (executionLock) {
                executionLock.set(true);
                executionLock.notifyAll();
            }
            isFinished = true;
        }
    }

    private static boolean processType(ComplexTypeMetadata type) {
        // Do not process UpdateReport type
        return !"Update".equals(type.getName()); //$NON-NLS-1$
    }

    public String getId() {
        return id;
    }

    @Override
    public int getErrorCount() {
        return stats.getErrorCount();
    }

    @Override
    public int getProcessedRecords() {
        return stats.getErrorCount() + stats.getSuccessCount();
    }

    public double getPerformance() {
        if (getProcessedRecords() > 0) {
            float time;
            if (endTime > 0) {
                time = (endTime - startTime) / 1000f;
            } else {
                time = (System.currentTimeMillis() - startTime) / 1000f;
            }
            return getProcessedRecords() / time;
        } else {
            return 0;
        }
    }

    public void cancel() {
        synchronized (currentTypeTaskMonitor) {
            if (currentTypeTask != null) {
                currentTypeTask.cancel();
            }
            isCancelled = true;
        }
    }

    public void waitForCompletion() throws InterruptedException {
        synchronized (startLock) {
            while (!startLock.get()) {
                startLock.wait();
            }
        }
        synchronized (executionLock) {
            while (!executionLock.get()) {
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
        return isCancelled || isFinished;
    }

    @Override
    public boolean hasFailed() {
        return false;
    }
}
