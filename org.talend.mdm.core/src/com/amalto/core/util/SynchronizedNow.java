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

package com.amalto.core.util;

import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * Utility class for "synchronized" time:  this class ensures consecutive calls to {@link #getTime()} always return
 * different and consecutive time values.
 * </p>
 * <p>
 * This class is helpful when code depends on {@link System#currentTimeMillis()} but precision is not precise enough
 * when called many times.
 * </p>
 * <p>
 * <b>Note:</b>This class is obviously slower than {@link System#currentTimeMillis()} and should be only used when
 * needed.
 * </p>
 */
public class SynchronizedNow {

    private static final Logger LOGGER = Logger.getLogger(SynchronizedNow.class);

    private static final AtomicLong lastUpdateTime = new AtomicLong();

    /**
     * @return A <i>time</i> similar to {@link System#currentTimeMillis()} but ensure time follow a strict sequence.
     */
    public long getTime() {
        synchronized (lastUpdateTime) {
            long time = System.currentTimeMillis();
            // System.currentTimeMillis() is not precise enough when stressed, this code ensures time follow a
            // strict sequence.
            if (lastUpdateTime.get() >= time) {
                long backup = time;
                time = lastUpdateTime.incrementAndGet();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Changed time from " + backup + " to " + time + " (diff: " + (time - backup) + " ms)");
                }
            } else {
                lastUpdateTime.set(time);
            }
            return time;
        }
    }
}
