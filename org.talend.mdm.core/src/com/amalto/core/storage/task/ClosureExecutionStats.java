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

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class ClosureExecutionStats {

    private final AtomicInteger errorCount = new AtomicInteger();

    private final AtomicInteger successCount = new AtomicInteger();

    private long endMatchTime = 0;

    public void reportError() {
        errorCount.incrementAndGet();
    }

    public void reportSuccess() {
        successCount.incrementAndGet();
    }

    public void reportEndMatchTime() {
        endMatchTime = System.currentTimeMillis();
    }

    public int getErrorCount() {
        return errorCount.get();
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public long getEndMatchTime() {
        return endMatchTime;
    }
}
