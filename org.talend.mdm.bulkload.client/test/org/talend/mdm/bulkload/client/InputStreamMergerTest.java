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

package org.talend.mdm.bulkload.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;

public class InputStreamMergerTest extends TestCase {

    public void testArguments() throws IOException {
        InputStreamMerger bis = new InputStreamMerger();
        try {
            bis.push(null);
            fail("Null input streams are not supported as argument of push()"); //$NON-NLS-1$
        } catch (Exception e) {
            // Expected
        }
        finally {
            bis.close();
        }
    }

    public void testSimpleClose() {
        InputStreamMerger bis = new InputStreamMerger();
        try {
            bis.close();
            bis.clean();
            assertEquals(-1, bis.read());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void testSimpleReadReaderFirst() {
        InputStreamMerger bis = new InputStreamMerger();
        String testString = "test"; //$NON-NLS-1$

        // Create a reader for the stream
        ReaderRunnable reader = new ReaderRunnable(bis);
        // Create a "pusher", a thread that will put a new stream
        Runnable pusher = new PusherRunnable(bis, testString, 1);

        // Start the test (start reader first)
        Thread readerThread = new Thread(reader);
        Thread pusherThread = new Thread(pusher);
        readerThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        pusherThread.start();

        // Wait for test end
        try {
            pusherThread.join();
            readerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals(testString, reader.getRebuiltString());
    }

    public void testSimpleReadPusherFirst() {
        InputStreamMerger bis = new InputStreamMerger();
        String testString = "test"; //$NON-NLS-1$

        // Create a reader for the stream
        ReaderRunnable reader = new ReaderRunnable(bis);
        // Create a "pusher", a thread that will put a new stream
        Runnable pusher = new PusherRunnable(bis, testString, 1);

        // Start the test (but now the pusher starts first)
        Thread readerThread = new Thread(reader);
        Thread pusherThread = new Thread(pusher);
        pusherThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        readerThread.start();

        // Wait for test end
        try {
            pusherThread.join();
            readerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals(testString, reader.getRebuiltString());
    }

    public void testSimpleReadPusherFirstWithInvalidCapacity() {
        try {
            _testSimpleReadPusherFirstWithLimitedCapacity(-1, 50);
            fail("Capacity -1 is invalid"); //$NON-NLS-1$
        } catch (Exception e) {
            // Expected (capacity must be greater or equals to 1).
        }
        try {
            _testSimpleReadPusherFirstWithLimitedCapacity(0, 50);
            fail("Capacity 0 is invalid"); //$NON-NLS-1$
        } catch (Exception e) {
            // Expected (capacity must be greater or equals to 1).
        }
    }

    public void testSimpleReadPusherFirstWithLimitedCapacity() {
        _testSimpleReadPusherFirstWithLimitedCapacity(1, 50);
        _testSimpleReadPusherFirstWithLimitedCapacity(20, 50);
        _testSimpleReadPusherFirstWithLimitedCapacity(50, 50);
        _testSimpleReadPusherFirstWithLimitedCapacity(100, 50);
        _testSimpleReadPusherFirstWithLimitedCapacity(50, 100);
    }

    private void _testSimpleReadPusherFirstWithLimitedCapacity(int capacity, int count) {
        InputStreamMerger bis = new InputStreamMerger(capacity);
        String testString = "test"; //$NON-NLS-1$

        // Create a reader for the stream
        ReaderRunnable reader = new ReaderRunnable(bis);
        // Create a "pusher", a thread that will put a new stream
        Runnable pusher = new PusherRunnable(bis, testString, count);

        // Start the test (but now the pusher starts first)
        Thread readerThread = new Thread(reader, "Consumer"); //$NON-NLS-1$
        Thread pusherThread = new Thread(pusher, "Producer"); //$NON-NLS-1$
        pusherThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        readerThread.start();

        // Wait for test end
        try {
            pusherThread.join();
            readerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(testString);
        }
        assertEquals(builder.toString(), reader.getRebuiltString());
    }

    public void testSimpleReadPusherFirstWithLimitedCapacityAndWarmUp() {
        _testSimpleReadPusherFirstWithLimitedCapacityAndWarmUp(1, 50, 1);
        _testSimpleReadPusherFirstWithLimitedCapacityAndWarmUp(20, 50, 0);
        _testSimpleReadPusherFirstWithLimitedCapacityAndWarmUp(100, 50, 100);
    }

    private void _testSimpleReadPusherFirstWithLimitedCapacityAndWarmUp(int capacity, int count, final int warmUp) {
        InputStreamMerger bis = new InputStreamMerger(capacity, new InputStreamMerger.ThresholdWarmUpStrategy(warmUp));
        String testString = "test"; //$NON-NLS-1$

        // Create a reader for the stream
        ReaderRunnable reader = new ReaderRunnable(bis);
        // Create a "pusher", a thread that will put a new stream
        Runnable pusher = new PusherRunnable(bis, testString, count);

        // Start the test (but now the pusher starts first)
        Thread readerThread = new Thread(reader);
        Thread pusherThread = new Thread(pusher);
        pusherThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        readerThread.start();

        // Wait for test end
        try {
            pusherThread.join();
            readerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(testString);
        }
        assertEquals(builder.toString(), reader.getRebuiltString());
    }

    public void testSimpleReadPusherFirstWithError() throws Exception {
        InputStreamMerger bis = new InputStreamMerger();
        String testString = "test"; //$NON-NLS-1$

        // Create a reader for the stream
        ReaderWithErrorsRunnable reader = new ReaderWithErrorsRunnable(bis, 4);
        // Create a "pusher", a thread that will put a new stream
        Runnable pusher = new PusherRunnable(bis, testString, 20);

        // Start the test (but now the pusher starts first)
        Thread readerThread = new Thread(reader);
        Thread pusherThread = new Thread(pusher);
        pusherThread.start();
        Thread.sleep(1000);
        readerThread.start();

        // Wait for test end
        readerThread.join();
        pusherThread.join();
        assertNotNull(bis.getLastReportedFailure());
        assertEquals("Expected exception text", bis.getLastReportedFailure().getCause().getMessage()); //$NON-NLS-1$
        try {
            bis.close();
            fail("Expected an exception on close."); //$NON-NLS-1$
        } catch (IOException e) {
            assertEquals("Expected exception text", e.getCause().getCause().getMessage()); //$NON-NLS-1$
        }
        assertEquals("testtesttesttest", reader.getRebuiltString()); //$NON-NLS-1$
    }
    
    /**
     * This test reproduces the case of a job with an error while using
     * tMDMBulkLoad
     */
    public void testReadErrorWhilePushing() throws Exception {
        InputStreamMerger bis = new InputStreamMerger();
        String testString = "test"; //$NON-NLS-1$

        // Create a reader for the stream
        ReaderRunnable reader = new ReaderRunnable(bis);
        // Create a "pusher", a thread that will put a new stream
        Runnable pusher = new PusherRunnableWithError(bis, testString, 20);

        // Start the test (but now the pusher starts first)
        Thread readerThread = new Thread(reader);
        Thread pusherThread = new Thread(pusher);
        pusherThread.start();
        Thread.sleep(1000);
        readerThread.start();

        // Wait for test end
        readerThread.join();
        pusherThread.join();
    }

    public void testManyReadReaderFirst() {
        int times = 10;
        InputStreamMerger bis = new InputStreamMerger();
        String testString = "test"; //$NON-NLS-1$

        // Create a reader for the stream
        ReaderRunnable reader = new ReaderRunnable(bis);
        // Create a "pusher", a thread that will put a new stream
        Runnable pusher = new PusherRunnable(bis, testString, times);

        // Start the test (start reader first)
        Thread readerThread = new Thread(reader);
        Thread pusherThread = new Thread(pusher);
        readerThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        pusherThread.start();

        // Wait for test end
        try {
            pusherThread.join();
            readerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String expectedOutput = ""; //$NON-NLS-1$
        for (int i = 0; i < times; i++) {
            expectedOutput += testString;
        }
        assertEquals(expectedOutput, reader.getRebuiltString());
    }
    
    
    public void testSlowConsumer() {
        int times = 50;
        InputStreamMerger bis = new InputStreamMerger();
        String testString = "test"; //$NON-NLS-1$

        // Create a reader for the stream
        SlowReaderRunnable reader = new SlowReaderRunnable(bis, 100, 8);
        // Create a "pusher", a thread that will put a new stream
        Runnable pusher = new PusherRunnable(bis, testString, times);

        // Start the test (start reader first)
        Thread readerThread = new Thread(reader);
        Thread pusherThread = new Thread(pusher);
        readerThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        pusherThread.start();

        // Wait for test end
        try {
            pusherThread.join();
            readerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String expectedOutput = ""; //$NON-NLS-1$
        for (int i = 0; i < times; i++) {
            expectedOutput += testString;
        }
        assertEquals(expectedOutput, reader.getRebuiltString());
    }

    public void testBulkload() throws InterruptedException, IOException {
        String testString = "<Product><Id>99</Id><Name>BaxLlvBgKJ6NuJsdOiAAGSGbWX7F3b</Name><ShortDescription></ShortDescription><LongDescription></LongDescription><Features><Sizes><Size></Size></Sizes><Colors><Color></Color></Colors></Features><Availability></Availability><Price></Price><Family></Family><Supplier></Supplier><CreationDate></CreationDate><RemovalDate></RemovalDate><Status></Status></Product>"; //$NON-NLS-1$
        int batchSize = 10;
        int inputSize = 15000;

        // Start the test
        InputStreamMerger inputStreamMerger = doSimulateBulkload();
        for (int i = 0; i < inputSize; i++) {
            if (i > 0 && i % batchSize == 0) {
                // Wait for reader to finish consume of xml chunks
                inputStreamMerger.close();
                // Create a new stream for a new batch
                inputStreamMerger = doSimulateBulkload();
            }

            inputStreamMerger.push(new ByteArrayInputStream(testString.getBytes()));
        }

        // Wait for remaining ones (in case (count % batchSize != 0))
        inputStreamMerger.close();
    }

    private static InputStreamMerger doSimulateBulkload() {
        InputStreamMerger merger = new InputStreamMerger();
        ReaderRunnable reader = new ReaderRunnable(merger);
        Thread readerThread = new Thread(reader);
        readerThread.start(); // eq bulkload client load

        return merger;
    }

    private static class ReaderRunnable implements Runnable {
        private final InputStreamMerger bis;
        private final Object runLock = new Object();
        private String rebuiltString = ""; //$NON-NLS-1$

        public ReaderRunnable(InputStreamMerger bis) {
            this.bis = bis;
        }

        public void run() {
            synchronized (runLock) {
                try {
                    int readBytes;
                    byte[] buffer = new byte[1024];
                    while ((readBytes = bis.read(buffer)) > 0) {
                        rebuiltString += new String(ArrayUtils.subarray(buffer, 0, readBytes));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public String getRebuiltString() {
            synchronized (runLock) {
                return rebuiltString;
            }
        }
    }
    
    
    private static class SlowReaderRunnable implements Runnable {
        private final InputStreamMerger bis;
        private final Object runLock = new Object();
        private final long waitBetweenReads;
        private final int bufferSize;
        private String rebuiltString = ""; //$NON-NLS-1$

        public SlowReaderRunnable(InputStreamMerger bis, long waitBetweenReads, int bufferSize) {
            this.bis = bis;
            this.waitBetweenReads = waitBetweenReads;
            this.bufferSize = bufferSize;
        }

        public void run() {
            synchronized (runLock) {
                try {
                    int readBytes;
                    byte[] buffer = new byte[bufferSize];
                    while ((readBytes = bis.read(buffer)) > 0) {
                        Thread.sleep(waitBetweenReads);
                        rebuiltString += new String(ArrayUtils.subarray(buffer, 0, readBytes));
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public String getRebuiltString() {
            synchronized (runLock) {
                return rebuiltString;
            }
        }
    }

    private static class ReaderWithErrorsRunnable implements Runnable {
        private final InputStreamMerger bis;
        private final Object runLock = new Object();
        private String rebuiltString = ""; //$NON-NLS-1$
        private final int nbReadsBeforeError;

        public ReaderWithErrorsRunnable(InputStreamMerger bis, int nbReadsBeforeError) {
            this.bis = bis;
            this.nbReadsBeforeError = nbReadsBeforeError;
        }

        public void run() {
            synchronized (runLock) {
                try {
                    int readBytes=0;
                    byte[] buffer = new byte[8];
                    int times=0;
                    while ((readBytes = bis.read(buffer)) > 0) {
                        rebuiltString += new String(ArrayUtils.subarray(buffer, 0, readBytes));
                        times++;
                        if(times == nbReadsBeforeError){
                            bis.reportFailure(new RuntimeException("Expected exception text")); //$NON-NLS-1$
                            break;
                        }
                    }
                } catch (IOException e) {
                    bis.reportFailure(e);
                }
            }
        }
        
        public String getRebuiltString() {
            synchronized (runLock) {
                return rebuiltString;
            }
        } 
    }


    private static class PusherRunnable implements Runnable {
        private final InputStreamMerger bis;
        private final String testString;
        private final int count;

        public PusherRunnable(InputStreamMerger bis, String testString, int count) {
            this.bis = bis;
            this.testString = testString;
            this.count = count;
        }

        public void run() {
            try {
                for (int i = 0; i < count; i++) {
                    bis.push(new ByteArrayInputStream(testString.getBytes()));
                }
                try {
                    bis.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                finally {
                    bis.clean();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private static class PusherRunnableWithError implements Runnable {
        private final InputStreamMerger bis;
        private final String testString;
        private final int count;

        public PusherRunnableWithError(InputStreamMerger bis, String testString, int count) {
            this.bis = bis;
            this.testString = testString;
            this.count = count;
        }

        public void run() {
            try {
                for (int i = 0; i < count; i++) {
                    bis.push(new ByteArrayInputStream(testString.getBytes()));
                }
                throw new RuntimeException("Pusher exception");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            finally {
                bis.clean();
            }
        }
    }
}
