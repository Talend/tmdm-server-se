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

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;

/**
 * Bulkload amount items client
 * 
 */
public class BulkloadClientUtil {

    public static final Integer MAX_HTTP_REQUESTS;
    public static final String CLIENT_CONNECTION_TIMEOUT = "ws_client_connection_timeout"; //$NON-NLS-1$
    public static final String CLIENT_SOCKET_TIMEOUT = "ws_client_receive_timeout"; //$NON-NLS-1$
    
    static {
        String httpRequests = System.getProperty("bulkload.concurrent.http.requests");//$NON-NLS-1$
        MAX_HTTP_REQUESTS = httpRequests == null? Integer.MAX_VALUE : Integer.parseInt(httpRequests);
    }

    public static void bulkload(String url, String cluster, String concept, String datamodel, boolean validate, boolean smartpk, boolean insertonly,
            InputStream itemdata, String username, String password, String transactionId, List<String> cookies, String universe, String tokenKey,
            String tokenValue) throws Exception {
        HostConfiguration config = new HostConfiguration();
        URI uri = new URI(url, false, "UTF-8"); //$NON-NLS-1$
        config.setHost(uri);

        NameValuePair[] parameters = { new NameValuePair("cluster", cluster), //$NON-NLS-1$
                new NameValuePair("concept", concept), //$NON-NLS-1$
                new NameValuePair("datamodel", datamodel), //$NON-NLS-1$
                new NameValuePair("validate", String.valueOf(validate)), //$NON-NLS-1$
                new NameValuePair("action", "load"), //$NON-NLS-1$ //$NON-NLS-2$
                new NameValuePair("smartpk", String.valueOf(smartpk)),//$NON-NLS-1$
                new NameValuePair("insertonly", String.valueOf(insertonly))}; //$NON-NLS-1$

        HttpClient client = new HttpClient();
        String user = universe == null || universe.trim().length() == 0 ? username : universe + "/" + username; //$NON-NLS-1$
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
        HttpClientParams clientParams = client.getParams();
        clientParams.setAuthenticationPreemptive(true);
        clientParams.setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        clientParams.setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, getClientTimeout(CLIENT_CONNECTION_TIMEOUT));
        clientParams.setParameter(HttpConnectionParams.SO_TIMEOUT, getClientTimeout(CLIENT_SOCKET_TIMEOUT));

        PutMethod putMethod = new PutMethod();
        // This setPath call is *really* important (if not set, request will be sent to the JBoss root '/')
        putMethod.setPath(url);
        String responseBody;
        try {
            // Configuration
            putMethod.setRequestHeader("Content-Type", "text/xml; charset=utf8"); //$NON-NLS-1$ //$NON-NLS-2$
            if (transactionId != null) {
                putMethod.setRequestHeader("transaction-id", transactionId); //$NON-NLS-1$
            }
            if (cookies != null) {
                for(String cookie : cookies) {
                    putMethod.addRequestHeader("Cookie", cookie); //$NON-NLS-1$
                }
            }
            if (tokenKey != null && tokenValue != null) {
                putMethod.setRequestHeader(tokenKey, tokenValue);
            }

            putMethod.setQueryString(parameters);
            putMethod.setContentChunked(true);
            // Set the content of the PUT request
            putMethod.setRequestEntity(new InputStreamRequestEntity(itemdata));

            client.executeMethod(config, putMethod);
            responseBody = putMethod.getResponseBodyAsString();
            if (itemdata instanceof InputStreamMerger) {
                ((InputStreamMerger) itemdata).setAlreadyProcessed(true);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            putMethod.releaseConnection();
        }

        int statusCode = putMethod.getStatusCode();
        if (statusCode >= 500) {
            throw new BulkloadException(responseBody);
        } else if (statusCode >= 400) {
            throw new BulkloadException("Could not send data to MDM (HTTP status code: " + statusCode + ")."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static int getClientTimeout(String property) throws Exception {
        int defaultTimeout = 60000;
        String inputTimeout = System.getProperty(property);
        if (inputTimeout != null) {
            try {
                int timeout = Integer.parseInt(inputTimeout);
                if (timeout > 0) {
                    return timeout;
                }
            } catch (Exception exception) {
                throw new RuntimeException(property + " property value '" + inputTimeout + "' is invalid", exception);  //$NON-NLS-1$//$NON-NLS-2$
            }
        }
        return defaultTimeout;
    }
    
    public static InputStreamMerger bulkload(String url, String cluster, String concept, String dataModel, boolean validate,
            boolean smartPK, boolean insertOnly, String username, String password, String transactionId, List<String> cookies, String universe, String tokenKey,
            String tokenValue, AtomicInteger startedBulkloadCount) {
        InputStreamMerger merger = new InputStreamMerger();
        while (startedBulkloadCount.get() >= MAX_HTTP_REQUESTS) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new BulkloadException("Waitting to create bulkload thread meets exception."); //$NON-NLS-1$
            }
        }
        Runnable loadRunnable = new AsyncLoadRunnable(url, cluster, concept, dataModel, validate, smartPK, insertOnly, merger, username,
                password, transactionId, cookies, universe, tokenKey, tokenValue, startedBulkloadCount);
        Thread loadThread = new Thread(loadRunnable);
        loadThread.start();
        return merger;
    }

    private static class AsyncLoadRunnable implements Runnable {

        private final String url;

        private final String cluster;

        private final String concept;

        private final String dataModel;

        private final boolean validate;

        private final boolean smartPK;
        
        private final boolean insertOnly;

        private final InputStreamMerger inputStream;

        private final String userName;

        private final String password;

        private final String transactionId;

        private final List<String> cookies;

        private final String universe;

        private final String tokenKey;

        private final String tokenValue;

        private final AtomicInteger startedBulkloadCount;

        public AsyncLoadRunnable(String url, String cluster, String concept, String dataModel, boolean validate, boolean smartPK, boolean insertOnly,
                InputStreamMerger inputStream, String userName, String password, String transactionId, List<String> cookies, String universe,
                String tokenKey, String tokenValue, AtomicInteger startedBulkloadCount) {
            this.url = url;
            this.cluster = cluster;
            this.concept = concept;
            this.dataModel = dataModel;
            this.validate = validate;
            this.smartPK = smartPK;
            this.insertOnly = insertOnly;
            this.inputStream = inputStream;
            this.userName = userName;
            this.password = password;
            this.transactionId = transactionId;
            this.cookies = cookies;
            this.universe = universe;
            this.tokenKey = tokenKey;
            this.tokenValue = tokenValue;
            this.startedBulkloadCount = startedBulkloadCount;
        }

        @Override
        public void run() {
            try {
                startedBulkloadCount.incrementAndGet();
                do {
                    bulkload(url, cluster, concept, dataModel, validate, smartPK, insertOnly, inputStream, userName, password,
                            transactionId, cookies, universe, tokenKey, tokenValue);
                } while(!inputStream.isConsumed());
            } catch (Throwable e) {
                inputStream.reportFailure(e);
            } finally {
                startedBulkloadCount.decrementAndGet();
                synchronized (startedBulkloadCount) {
                    startedBulkloadCount.notifyAll();
                }
            }
        }
    }
}
