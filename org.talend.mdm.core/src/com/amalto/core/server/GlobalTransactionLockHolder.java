// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.server;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;

public class GlobalTransactionLockHolder {

    private static final Logger LOGGER = Logger.getLogger(GlobalTransactionLockHolder.class);

    private static final int LOCK_TIMEOUT_SECONDS = 30;

    private static final Lock globalLock = new ReentrantLock();

    public static void acquireGlobalLock() {
        try {
            ReentrantLock mylock = (ReentrantLock)globalLock;
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("******* global lock is locked  : " + mylock.isLocked());
                LOGGER.trace("******* global lock self locked: " + mylock.isHeldByCurrentThread());
                StackTraceElement[] mStacks = Thread.currentThread().getStackTrace();
                StringBuilder sb = new StringBuilder();
                int i = 0;
                for (StackTraceElement s : mStacks) {
                    i++;
                    if (i > 20)
                        break;
                    sb.append("ClassName: " + s.getClassName() + ", MethodName: " + s.getMethodName() + ",Row:"
                                    + s.getLineNumber()).append("\n");
                }

                LOGGER.trace("*******get lock Current thread name: " + Thread.currentThread().getName());
                LOGGER.trace(sb.toString());
            }
            if (!globalLock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                LOGGER.error("Failed to acquire global lock within 30 seconds."); //$NON-NLS-1$
                throw new RuntimeException("Failed to acquire globalLock within " + LOCK_TIMEOUT_SECONDS + " seconds"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while trying to acquire globalLock on GlobalTransactionLockHolder.", e); //$NON-NLS-1$
        }
    }

    public static void releaseGlobalLock() {
        ReentrantLock mylock = (ReentrantLock)globalLock;
        if (mylock.isHeldByCurrentThread()) {
            globalLock.unlock();
        }
        if (LOGGER.isTraceEnabled()) {
            StackTraceElement[] mStacks = Thread.currentThread().getStackTrace();
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (StackTraceElement s : mStacks) {
                i++;
                if (i > 6)
                    break;
                sb.append("ClassName: " + s.getClassName() + ", MethodName: " + s.getMethodName() + ",Row:"
                                + s.getLineNumber()).append("\n");
            }
            LOGGER.trace("*******release lock thread: " + Thread.currentThread().getName());
            LOGGER.trace(sb.toString());
        }
    }
}
