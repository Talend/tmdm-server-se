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

import com.amalto.core.storage.record.DataRecord;
import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContext;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

class ThreadDispatcher implements Closure {

    private static final Logger LOGGER = Logger.getLogger(ThreadDispatcher.class);

    private static final int QUEUE_SIZE_THRESHOLD;

    private final BlockingQueue<DataRecord> queue = new LinkedBlockingQueue<DataRecord>();

    private final Set<ConsumerRunnable> childClosures = new HashSet<ConsumerRunnable>();

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));

    private long startTime;

    private int count = 0;

    static {
        // staging.validation.buffer.threshold tells when reader should pause.
        String value = MDMConfiguration.getConfiguration().getProperty("staging.validation.buffer.threshold"); //$NON-NLS-1$
        QUEUE_SIZE_THRESHOLD = value == null ? 1000 : Integer.valueOf(value);
    }

    ThreadDispatcher(int threadNumber, Closure closure, ClosureExecutionStats stats, SecurityContext context) {
        for (int i = 0; i < threadNumber; i++) {
            childClosures.add(new ConsumerRunnable(queue, closure.copy(), stats, context));
        }
        executor.allowCoreThreadTimeOut(true);
    }

    private ExecutorService getManager() {
        return executor;
    }

    public void begin() {
        startTime = System.currentTimeMillis();
        ExecutorService workManager = getManager();
        try {
            for (ConsumerRunnable childThread : childClosures) {
                workManager.submit(childThread);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void execute(DataRecord stagingRecord, ClosureExecutionStats stats) {
        try {
            if (!queue.offer(stagingRecord)) {
                LOGGER.warn("Not enough consumers for records!");
                queue.put(stagingRecord); // Wait for free room in queue.
            }
            while (queue.size() > QUEUE_SIZE_THRESHOLD) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Pausing read operation / queue size: " + queue.size() + ".");
                }
                Thread.sleep(1000);
            }
            count++;
            if (LOGGER.isDebugEnabled()) {
                if (count % 1000 == 0) {
                    LOGGER.debug("doc/s -> " + count / ((System.currentTimeMillis() - startTime) / 1000f) + " / queue size: " + queue.size());
                }
            }
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exception occurred during dispatch of records.", e);
            }
        }
    }

    @Override
    public void cancel() {
        prepareEndFlag(true);
    }

    public void end(ClosureExecutionStats stats) {
        prepareEndFlag(false);
        for (ConsumerRunnable childClosure : childClosures) {
            try {
                childClosure.waitForEnd();
            } catch (InterruptedException e) {
                throw new RuntimeException("Child executions did not complete normally.", e);
            }
        }
        if (!queue.isEmpty()) {
            LOGGER.warn("After end of queue processors work, " + queue.size() + " remained in processing queue.");
            prepareEndFlag(true);
        }
    }

    private synchronized void prepareEndFlag(boolean cleanQueue) {
        if (cleanQueue){
            queue.clear();
        }
        for (int i = 0; i < childClosures.size(); i++) {
            queue.offer(new EndDataRecord());
        }
    }
    
    public Closure copy() {
        return this;
    }

    public static class EndDataRecord extends DataRecord {
        public EndDataRecord() {
            super(null, null);
        }
    }
}
