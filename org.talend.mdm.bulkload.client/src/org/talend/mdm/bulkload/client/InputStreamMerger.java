/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.bulkload.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Merges multiple InputStreams into a single one by appending their content in order. Use
 * {@link InputStreamMerger#push(InputStream)} to append a new stream at the end. Use {@link InputStreamMerger#close()}
 * to end this stream.
 */
public class InputStreamMerger extends InputStream {

    private static final Logger log = Logger.getLogger(InputStreamMerger.class.getName());

    private static final int DEFAULT_CAPACITY = 1000;

    /**
     * Receives orders from the producer (can be either inputstreams or stop order)
     */
    private final LinkedBlockingQueue<InternalMessage> producerToConsumer;

    /**
     * Receives orders from the consumer (can be either stop order acknowledgment or failure notifications)
     */
    private final LinkedBlockingQueue<InternalMessage> consumerToProducer;

    /**
     * The current stream being processed
     */
    private InputStream currentStream;

    private Throwable lastReportedFailure;

    private volatile boolean stopped = false;

    private volatile boolean alreadyPushed = false;

    private volatile boolean alreadyProcessed = false;

    public InputStreamMerger() {
        this(DEFAULT_CAPACITY, NoWarmUpStrategy.INSTANCE);
    }

    public InputStreamMerger(int capacity) {
        this(capacity, NoWarmUpStrategy.INSTANCE);
    }

    public InputStreamMerger(int capacity, WarmUpStrategy warmUpStrategy) {
        this.producerToConsumer = new LinkedBlockingQueue<InternalMessage>(capacity);
        this.consumerToProducer = new LinkedBlockingQueue<InternalMessage>();
    }

    // producer side
    public void push(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream can not be null."); //$NON-NLS-1$
        }
        if (stopped) {
            throw new IOException("Stream is closed"); //$NON-NLS-1$
        }
        InternalMessage msg = InternalMessage.newStreamMessage(inputStream);
        try {
            do {
                InternalMessage consumerMessage = consumerToProducer.poll();
                if (consumerMessage != null) {
                    if (consumerMessage.isFailureMessage()) {
                        throw new IOException("Consumer error", consumerMessage.getFailure()); //$NON-NLS-1$
                    } else if (consumerMessage.mustStop()) {
                        consumerToProducer.put(consumerMessage);
                        throw new IOException("Cannot push to closed InputStream"); //$NON-NLS-1$
                    }
                }
            } while (!this.producerToConsumer.offer(msg, 30, TimeUnit.SECONDS));
            this.alreadyPushed = true;
        } catch (InterruptedException e) {
            throw new RuntimeException("Push was interrupted", e); //$NON-NLS-1$
        }
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    // producer side
    @Override
    public void close() throws IOException {
        InternalMessage msg = InternalMessage.newCloseMessage();
        if (this.lastReportedFailure != null) {
            throw new IOException("Consumer error", lastReportedFailure); //$NON-NLS-1$
        }
        if (!this.alreadyPushed) {
            debug("[P] Closing an empty stream"); //$NON-NLS-1$
            this.stopped = true;
            return;
        }
        try {
            producerToConsumer.put(msg);
            InternalMessage response = consumerToProducer.take();
            if (response.isFailureMessage()) {
                throw new IOException("Consumer error", response.getFailure()); //$NON-NLS-1$
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("close was interrupted", e); //$NON-NLS-1$
        }
        debug("[P] Close completed."); //$NON-NLS-1$
    }

    /**
     * 
     */
    public void clean() {
        while (!producerToConsumer.isEmpty()) {
            producerToConsumer.poll();
        }
        try {
            producerToConsumer.put(InternalMessage.newCloseMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException("clean was interrupted", e); //$NON-NLS-1$
        }
    }

    // consumer side
    public void reportFailure(Throwable e) {
        try {
            this.lastReportedFailure = new RuntimeException(e);
            this.consumerToProducer.put(InternalMessage.newErrorMessage(this.lastReportedFailure));
        } catch (InterruptedException e1) {
            // should never happen as consumerToProducer has no limited capacity
        }
    }

    // consumer side
    @Override
    public int available() throws IOException {
        if (currentStream != null) {
            return currentStream.available();
        }
        consume();
        if (currentStream != null) {
            return currentStream.available();
        }
        return 4096;
    }

    // consumer side
    @Override
    public int read() throws IOException {
        if (currentStream == null) {
            consume();
        }
        if (currentStream == null) {
            return -1;
        }
        int read = currentStream.read();
        if (read == -1) {
            consume();
        }
        if (currentStream == null) {
            // end of stream
            return -1;
        }
        return read;
    }

    // consumer side
    private void consume() throws IOException {
        if (this.stopped) {
            debug("[C] Skipping consume on already stopped stream"); //$NON-NLS-1$
            return;
        }
        if (currentStream != null) {
            currentStream.close();
        }
        try {
            debug("[C] Start waiting for a message"); //$NON-NLS-1$
            InternalMessage message = this.producerToConsumer.take();
            if (message.mustStop()) {
                debug("[C] Received a stop message"); //$NON-NLS-1$
                consumerToProducer.put(message);
                currentStream = null;
                this.stopped = true;
                return;
            }
            currentStream = message.getInputStream();
            debug("[C] Received a stream message"); //$NON-NLS-1$
            if (currentStream == null) {
                throw new IllegalStateException("Received an invalid message"); //$NON-NLS-1$
            }
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "Interrupted while consuming"); //$NON-NLS-1$
        }
    }

    public int getBufferSize() {
        return this.producerToConsumer.size();
    }

    public Throwable getLastReportedFailure() {
        return this.lastReportedFailure;
    }

    private static void debug(String message) {
        Level debugLevel = Level.FINEST;
        if (log.isLoggable(debugLevel)) {
            log.log(debugLevel, "[" + Thread.currentThread() + "] " + message); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /***
     * Internal message exchanged in queues
     */
    private static class InternalMessage {

        private final boolean mustStop;

        private final Throwable failure;

        private final InputStream inputStream;

        private InternalMessage(boolean mustStop, Throwable failure, InputStream inputStream) {
            this.mustStop = mustStop;
            this.failure = failure;
            this.inputStream = inputStream;
        }

        public static InternalMessage newErrorMessage(Throwable failure) {
            return new InternalMessage(true, failure, null);
        }

        public static InternalMessage newStreamMessage(InputStream stream) {
            return new InternalMessage(false, null, stream);
        }

        public static InternalMessage newCloseMessage() {
            return new InternalMessage(true, null, null);
        }

        public boolean mustStop() {
            return this.mustStop;
        }

        public boolean isFailureMessage() {
            return this.failure != null;
        }

        public Throwable getFailure() {
            return this.failure;
        }

        public InputStream getInputStream() {
            return this.inputStream;
        }

        @Override
        public String toString() {
            return (failure != null ? "Failure" : (mustStop ? "stop" : "stream")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    interface WarmUpStrategy {

        /**
         * @param inputStreamMerger The input stream to be checked.
         * @return <code>true</code> if the <code>inputStreamMerger</code> is ready to be used by consumers.
         * @see org.talend.mdm.bulkload.client.InputStreamMerger#getBufferSize()
         */
        boolean isReady(InputStreamMerger inputStreamMerger);
    }

    /**
     * A "no warm up" warm up strategy: always returns <code>true</code> (<code>inputStreamMerger</code> is always
     * ready).
     */
    public static class NoWarmUpStrategy implements WarmUpStrategy {

        static WarmUpStrategy INSTANCE = new NoWarmUpStrategy();

        @Override
        public boolean isReady(InputStreamMerger inputStreamMerger) {
            return true;
        }
    }

    /**
     * A threshold based strategy: indicate input stream merger isn't ready till
     * {@link org.talend.mdm.bulkload.client.InputStreamMerger#getBufferSize()} is greater or equals to
     * <code>threshold</code>. Once buffer size satisfies this condition, input stream is always considered as ready.
     */
    public static class ThresholdWarmUpStrategy implements WarmUpStrategy {

        private final int threshold;

        private boolean isReady = false;

        public ThresholdWarmUpStrategy(int threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean isReady(InputStreamMerger inputStreamMerger) {
            if (!isReady) {
                isReady = inputStreamMerger.getBufferSize() >= threshold;
            }
            return isReady;
        }
    }

    // Keep this method public for Studio
    public boolean isAlreadyProcessed() {
        return isConsumed();
    }

    protected boolean isConsumed() {
        return producerToConsumer.isEmpty();
    }
}
