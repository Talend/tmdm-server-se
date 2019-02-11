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
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BulkloadClient {

    private String url;

    private String username;

    private String password;

    private String cluster;

    private String concept;

    private String dataModel;

    private String transactionId;

    private List<String> cookies;

    private String tokenKey;

    private String tokenValue;

    private BulkloadOptions options = new BulkloadOptions();

    private final AtomicInteger startedBulkloadCount = new AtomicInteger(0);

    public BulkloadClient(String url, String username, String password, String cluster, String concept, String dataModel) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.cluster = cluster;
        this.concept = concept;
        this.dataModel = dataModel;
    }

    public void startThreadCount() {
        // Kept for compatibility with tMDMBulkload.
    }

    public void waitForEndOfQueue() {
        try {
            synchronized (startedBulkloadCount) {
                while (startedBulkloadCount.get() > 0) {
                    startedBulkloadCount.wait();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getDatamodel() {
        return dataModel;
    }

    public void setDatamodel(String dataModel) {
        this.dataModel = dataModel;
    }

    public BulkloadOptions getOptions() {
        return options;
    }

    public void setOptions(BulkloadOptions options) {
        this.options = options;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTokenKey() {
        return this.tokenKey;
    }

    public String getTokenValue() {
        return this.tokenValue;
    }

    public void setToken(String tokenKey, String tokenValue) {
        this.tokenKey = tokenKey;
        this.tokenValue = tokenValue;
    }

    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

    /**
     * load from a huge xml string
     *
     * @param xmlString A full xml document.
     * @throws Exception Thrown in case of bulk load error.
     */
    public void load(String xmlString) throws Exception {
        load(new ByteArrayInputStream(xmlString.getBytes("UTF-8"))); //$NON-NLS-1$
    }

    /**
     * <p>
     * Loads XML documents in MDM using an InputStream. All documents should follow each other.
     * For instance this InputStream can be used with this method.
     * </p>
     * <p/>
     * <code>
     * InputStream is = new ByteArrayInputStream("<doc></doc><doc></doc><doc></doc>".getBytes());
     * </code>
     * <p/>
     * <p>
     * This method blocks until all documents in <code>xmlDocuments</code> are read. For non blocking use cases
     * see {@link #load()}.
     * </p>
     *
     * @param xmlDocuments A stream that contains several XML documents.
     * @throws Exception Thrown in case of bulk load error
     */
    public void load(InputStream xmlDocuments) throws Exception {
        BulkloadClientUtil.bulkload(url, cluster, concept, dataModel, options.isValidate(), options.isSmartpk(),
                options.isInsertOnly(), options.isUpdateReport(), options.getSource(), xmlDocuments,
                username, password, transactionId, cookies, tokenKey, tokenValue);
    }

    /**
     * <p>
     * Loads XML documents in MDM using an InputStream created for the load. This method creates a {@link Thread} that
     * wait for {@link InputStreamMerger#push(java.io.InputStream)} to get called.
     * </p>
     * <p/>
     * <code>
     * BulkloadClient client = ...<br/>
     * InputStreamMerger is = client.load();<br/>
     * for(...) {<br/>
     * is.push(new ByteArrayInputStream("...".getBytes());<br/>
     * }
     * </code>
     *
     * @return A {@link InputStreamMerger} that allow asynchronous push to bulkload client.
     * @throws Exception Thrown in case of communication error
     * @see InputStreamMerger
     */
    public InputStreamMerger load() throws Exception {
        return BulkloadClientUtil.bulkload(url, cluster, concept, dataModel, options.isValidate(), options.isSmartpk(),
                options.isInsertOnly(), options.isUpdateReport(), options.getSource(), username,
                password, transactionId, cookies, tokenKey, tokenValue, startedBulkloadCount);
    }
}
