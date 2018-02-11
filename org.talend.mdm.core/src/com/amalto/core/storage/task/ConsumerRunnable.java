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
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class ConsumerRunnable implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ConsumerRunnable.class);

    private final Closure closure;

    private final BlockingQueue<DataRecord> queue;

    private final ClosureExecutionStats stats;

    private final SecurityContext context;

    private final AtomicBoolean hasEnded = new AtomicBoolean(false);

    private int committedRecordCount = 0;
    private SecurityContext previousContext;

    ConsumerRunnable(BlockingQueue<DataRecord> queue, Closure closure, ClosureExecutionStats stats, SecurityContext context) {
        this.closure = closure;
        this.queue = queue;
        this.stats = stats;
        this.context = context;
        hasEnded.set(false);
    }

    public void run() {
        closure.begin();
        // Overrides current context with supplied one, remembers previous one for end()
        previousContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(context);
        // Process records till queue is empty
        while (!hasEnded.get()) {
            try {
                DataRecord record = queue.take();
                if (record.getType() == null) {
                    try {
                        closure.end(stats);
                    } catch (Exception e) {
                        if (committedRecordCount == 0) {
                            LOGGER.warn("Ignore error on save (no record to save).");
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Ignored error.", e);
                            }
                        } else {
                            // Throws exception in case there were records to commit.
                            throw new RuntimeException(e);
                        }
                    }
                    end();
                } else {
                    closure.execute(record, stats);
                    committedRecordCount++;
                }
            } catch (Exception e) {
                LOGGER.error("Unexpected error during execution of task.", e);
                end();
            }
        }
    }

    private void end() {
        SecurityContextHolder.setContext(previousContext);
        synchronized (hasEnded) {
            hasEnded.set(true);
            hasEnded.notifyAll();
        }
    }

    public void release() {
        end();
    }

    public void waitForEnd() throws InterruptedException {
        synchronized (hasEnded) {
            while (!hasEnded.get()) {
                hasEnded.wait();
            }
        }
    }
}
