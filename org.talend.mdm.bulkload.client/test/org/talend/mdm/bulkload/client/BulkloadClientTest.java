/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.bulkload.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.commons.lang.math.RandomUtils;

public class BulkloadClientTest extends TestCase {

    public void testClient() throws Exception {
        String serverURL = "http://localhost:8180/talendmdm/services/bulkload";
        boolean isServerRunning = isServerRunning(serverURL);
        if (isServerRunning) {
            BulkloadClient client = new BulkloadClient(serverURL, "administrator", "administrator", null, "Product", "Product", "Product");
            client.setOptions(new BulkloadOptions());
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= 20; i++) {
                sb.append("<Product><Id>Product_"+ i +" </Id><Name>a</Name><Description>a</Description><Features><Sizes/><Colors/></Features><Price>2.00</Price><Stores/></Product>\n");
            }
            InputStream bin = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
            client.load(bin);
        }
    }

    private static boolean isServerRunning(String serverURL) {
        boolean isServerRunning = false;
        try {
            URL testURL = new URL(serverURL);
            URLConnection urlConnection = testURL.openConnection();
            urlConnection.connect();
            isServerRunning = true;
        } catch (IOException e) {
            System.out.println("Server is not running on '" + serverURL + "', skip this test.");
        }
        return isServerRunning;
    }

    public void testPerformance() throws Exception {
        String serverURL = "http://localhost:8180/talendmdm/services/bulkload";
        boolean isServerRunning = isServerRunning(serverURL);
        if (isServerRunning) {
            BulkloadClient client = new BulkloadClient(serverURL, "administrator", "administrator", null, "Product", "Product", "Product");
            client.setOptions(new BulkloadOptions());

            String xml = "<Product><Id>Product_0</Id><Name>a</Name><Description>a</Description><Features><Sizes/><Colors/></Features><Price>2.00</Price><Stores/></Product>";
            int num = 1000;
            int gap_num = 200;
            int gap = num / gap_num;
            for (int i = 0; i < gap; i++) {
                StringBuffer sb = new StringBuffer();
                for (int j = 0; j < gap_num; j++) {
                    int n = gap_num * i + j;
                    xml = "<Product><Id>Product_"+ n +"</Id><Name>a</Name><Description>a</Description><Features><Sizes/><Colors/></Features><Price>2.00</Price><Stores/></Product>\n";
                    sb.append(xml);
                }
                InputStreamMerger manager = client.load();

                // InputStream bin = BulkloadClientTest.class.getResourceAsStream("test.xml");
                InputStream bin = new ByteArrayInputStream(sb.toString().getBytes("utf-8"));
                manager.push(bin);
                manager.close();
                // client.load(bin);
            }
        }
    }

    public void testInterruptedBulkLoad() throws Exception {
        String serverURL = "http://localhost:8180/talendmdm/services/bulkload";
        boolean isServerRunning = isServerRunning(serverURL);
        if (isServerRunning) {
            final InterruptedTestResult result = new InterruptedTestResult();
            BulkloadClient client = new BulkloadClient(serverURL, "administrator", "administrator", null, "Product", "Product", "Product");
            client.setOptions(new BulkloadOptions());

            int count = 10;
            InputStream bin = new InputStream() {

                @Override
                public synchronized int read() throws IOException {
                    try {
                        Thread.sleep(RandomUtils.nextInt(5000));
                        synchronized (result) {
                            result.setSuccess(true);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return -1;
                }
            };
            for (int i = 0; i < count; i++) {
                InputStreamMerger manager = client.load();
                manager.push(bin);
                manager.close();
            }

            // client.waitForEndOfQueue();
            synchronized (result) {
                assertEquals(count, result.getSuccessCount());
                assertTrue(result.isSuccess());
            }
        }
    }
    
    // TMDM-9452
    public void testBulkLoadWithMultiClients() throws Exception {
        String serverURL = "http://localhost:8180/talendmdm/services/bulkload";
        boolean isServerRunning = isServerRunning(serverURL);
        if (isServerRunning) {
            BulkloadClient client1 = new BulkloadClient(serverURL, "administrator", "administrator", null, "Product", "Product", "Product");
            client1.setOptions(new BulkloadOptions());
            InputStreamMerger merger1 = client1.load();
            InputStream data1 = new ByteArrayInputStream(("<Product><Id>P1</Id><Name>P1</Name><Description>P1</Description><Features><Sizes/><Colors/></Features><Price>2.00</Price><Stores/></Product>").getBytes("UTF-8"));
            merger1.push(data1);
            
            BulkloadClient client2 = new BulkloadClient(serverURL, "administrator", "administrator", null, "Product", "Product", "Product");
            client2.setOptions(new BulkloadOptions());
            InputStreamMerger merger2 = client2.load();
            InputStream data2 = new ByteArrayInputStream(("<Product><Id>P2</Id><Name>P2</Name><Description>P2</Description><Features><Sizes/><Colors/></Features><Price>2.00</Price><Stores/></Product>").getBytes("UTF-8"));
            merger2.push(data2);
            
            merger1.close();
            client1.waitForEndOfQueue();
            merger2.close();
            client2.waitForEndOfQueue();
            
            assertEquals(true, merger1.isAlreadyProcessed());
            assertEquals(true, merger2.isAlreadyProcessed());
            assertEquals(true, merger1.isConsumed());
            assertEquals(true, merger2.isConsumed());
        }
    }

    public void testInterruptedBulkLoadOrder() throws Exception {
        String serverURL = "http://localhost:8180/talendmdm/services/bulkload";
        boolean isServerRunning = isServerRunning(serverURL);
        if (isServerRunning) {
            BulkloadClient client = new BulkloadClient(serverURL, "administrator", "administrator", null, "Product", "Product", "Product");
            client.setOptions(new BulkloadOptions());
            int count = 5;
            for (int i = 0; i < count; i++) {
                InputStreamMerger merger = client.load();
                synchronized (merger) {
                    InputStream bin = new InputStream() {

                        @Override
                        public synchronized int read() throws IOException {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            return -1;
                        }
                    };
                    merger.push(bin);
                    merger.close();
                    while (!merger.isAlreadyProcessed()) {
                        merger.wait(100);
                    }
                    assertEquals(true, merger.isAlreadyProcessed());
                    assertEquals(true, merger.isConsumed());
                    merger.notify();
                }
            }
        }
    }

    class InterruptedTestResult {

        boolean success = false;

        AtomicInteger successCount = new AtomicInteger();

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            successCount.incrementAndGet();
            this.success = success;
        }

        public int getSuccessCount() {
            return successCount.get();
        }
    }
    
    @SuppressWarnings("nls")
    public void testInsertOnly() throws Exception {
        String serverURL = "http://localhost:8180/talendmdm/services/bulkload"; //$NON-NLS-1$
        boolean isServerRunning = isServerRunning(serverURL);
        if (isServerRunning) {
            BulkloadClient client = new BulkloadClient(serverURL, "administrator", "administrator", null, "Product", "Product", "Product");
            BulkloadOptions options = new BulkloadOptions();
            client.setOptions(options);
            
            String xml1 = "<Product><Id>1</Id><Name>a</Name><Description>a</Description><Features><Sizes/><Colors/></Features><Price>2.00</Price><Stores/></Product>\n" +
            		      "<Product><Id>1</Id><Name>a</Name><Description>b</Description><Features><Sizes/><Colors/></Features><Price>3.00</Price><Stores/></Product>";

            options.setInsertOnly(false);            
            try {
                client.load(new ByteArrayInputStream(xml1.getBytes("utf-8")));
            } catch (Exception e) {
                fail("Insert should not fail with same ID when insertOnly=false");
            }
            
            String xml2 = "<Product><Id>2</Id><Name>a</Name><Description>a</Description><Features><Sizes/><Colors/></Features><Price>2.00</Price><Stores/></Product>\n" +
                          "<Product><Id>2</Id><Name>a</Name><Description>b</Description><Features><Sizes/><Colors/></Features><Price>3.00</Price><Stores/></Product>";
            options.setInsertOnly(true);
            try {
                client.load(new ByteArrayInputStream(xml2.getBytes("utf-8")));
                fail("Insert should fail with same ID when insertOnly=true");
            } catch (Exception e) {
                System.out.println("Excepted exception:" + e.getMessage());
            }
            
        }
    }
}
