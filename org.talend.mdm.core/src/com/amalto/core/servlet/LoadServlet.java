// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.EUUIDCustomType;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.core.load.action.DefaultLoadAction;
import com.amalto.core.load.action.LoadAction;
import com.amalto.core.load.action.OptimizedLoadAction;
import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.SaverSession;
import com.amalto.core.save.context.DocumentSaver;
import com.amalto.core.save.context.SaverContextFactory;
import com.amalto.core.server.MetadataRepositoryAdmin;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.api.DataCluster;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.util.Util;
import com.amalto.core.util.XSDKey;

public class LoadServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(LoadServlet.class);

    private static final long serialVersionUID = 1L;

    public static final Map<String, XSDKey> typeNameToKeyDef = new HashMap<String, XSDKey>();

    private static final String PARAMETER_CLUSTER = "cluster"; //$NON-NLS-1$

    private static final String PARAMETER_CONCEPT = "concept"; //$NON-NLS-1$

    private static final String PARAMETER_DATAMODEL = "datamodel"; //$NON-NLS-1$

    private static final String PARAMETER_VALIDATE = "validate"; //$NON-NLS-1$

    private static final String PARAMETER_SMARTPK = "smartpk"; //$NON-NLS-1$

    private static final String PARAMETER_INSERTONLY = "insertonly"; //$NON-NLS-1$

    private static final Map<String, AtomicInteger> DB_REQUESTS_MAP = new HashMap<String, AtomicInteger>();

    private static final Integer MAX_DB_REQUESTS;

    private static final Long WAIT_MILLISECONDS;

    static {
        MAX_DB_REQUESTS = Integer.valueOf(MDMConfiguration.getConfiguration().getProperty("bulkload.concurrent.database.requests", "25")); //$NON-NLS-1$ //$NON-NLS-2$
        WAIT_MILLISECONDS = Long.valueOf(MDMConfiguration.getConfiguration().getProperty("bulkload.concurrent.wait.milliseconds", "200")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public LoadServlet() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        response.setContentType("text/html; charset=UTF-8"); //$NON-NLS-1$
        response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        // configure writer depending on logger configuration.
        PrintWriter writer = configureWriter(response);
        writer.write("<html><body>"); //$NON-NLS-1$
        writer.write("<p><b>Load data into MDM</b><br/>Check server log output to determine when load is completed</b></p>"); //$NON-NLS-1$
        String dataClusterName = request.getParameter(PARAMETER_CLUSTER);
        String typeName = request.getParameter(PARAMETER_CONCEPT);
        String dataModelName = request.getParameter(PARAMETER_DATAMODEL);
        boolean needValidate = Boolean.valueOf(request.getParameter(PARAMETER_VALIDATE));
        boolean needAutoGenPK = Boolean.valueOf(request.getParameter(PARAMETER_SMARTPK));
        boolean insertOnly = Boolean.valueOf(request.getParameter(PARAMETER_INSERTONLY));

        LoadAction loadAction = getLoadAction(dataClusterName, typeName, dataModelName, needValidate, needAutoGenPK);
        if (needValidate && !loadAction.supportValidation()) {
            throw new ServletException(new UnsupportedOperationException("XML Validation isn't supported")); //$NON-NLS-1$
        }
        // Get xml server and key information
        XmlServer server;
        XSDKey keyMetadata;
        try {
            keyMetadata = getTypeKey(dataModelName, typeName);
            server = Util.getXmlServerCtrlLocal();
        } catch (Exception e) {
            throw new ServletException(e);
        }
        DataRecord.CheckExistence.set(!insertOnly);
        SaverSession session = SaverSession.newSession();
        SaverContextFactory contextFactory = session.getContextFactory();
        DocumentSaverContext context = contextFactory.createBulkLoad(dataClusterName, dataModelName, keyMetadata,
                request.getInputStream(), loadAction, server);
        DocumentSaver saver = context.createSaver();

        // Wait until less that MAX_THREADS running
        synchronized (LoadServlet.class) {
            AtomicInteger dbRequests = getDbRequests(dataClusterName);
            try {
                while (dbRequests.get() >= MAX_DB_REQUESTS) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Up to " + dbRequests + " db requests, wait for " + WAIT_MILLISECONDS + " ms.");
                    }
                    Thread.sleep(WAIT_MILLISECONDS);
                }
                int newDbRequests = increaseDbRequests(dataClusterName);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Add 1 db request, currently " + newDbRequests + " requests left.");
                }
            } catch (InterruptedException e) {
                LOG.error("Waiting to start db request meets exception.", e);
            }
        }

        try {
            session.begin(dataClusterName);
            saver.save(session, context);
            session.end();
            // End the load (might persist counter state in case of autogen pk).
            loadAction.endLoad(server);
        } catch (Exception e) {
            try {
                session.abort();
            } catch (Exception rollbackException) {
                LOG.error("Ignoring rollback exception", rollbackException); //$NON-NLS-1$
            }
            throw new ServletException(e);
        } finally {
            DataRecord.CheckExistence.remove();
            // Decrease total threads
            int newDbRequests = decreaseDbRequests(dataClusterName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finish 1 db request, currently " + newDbRequests + " requests left.");
            }
        }
        writer.write("</body></html>"); //$NON-NLS-1$
    }

    protected int increaseDbRequests(String dataClusterName) {
        return DB_REQUESTS_MAP.get(dataClusterName).incrementAndGet();
    }

    protected int decreaseDbRequests(String dataClusterName) {
        return DB_REQUESTS_MAP.get(dataClusterName).decrementAndGet();
    }

    protected AtomicInteger getDbRequests(String dataClusterName) {
        AtomicInteger value = DB_REQUESTS_MAP.get(dataClusterName);
        if (value == null) {
            value = new AtomicInteger(0);
            DB_REQUESTS_MAP.put(dataClusterName, value);
        }
        return value;
    }

    protected LoadAction getLoadAction(String dataClusterName, String typeName, String dataModelName, boolean needValidate,
            boolean needAutoGenPK) {
        // Test if the data cluster actually exists
        DataClusterPOJO dataCluster = getDataCluster(dataClusterName);
        if (dataCluster == null) {
            throw new IllegalArgumentException("Data cluster '" + dataClusterName + "' does not exist.");
        }

        LoadAction loadAction;
        if (needValidate || XSystemObjects.DC_PROVISIONING.getName().equals(dataClusterName)) {
            loadAction = new DefaultLoadAction(dataClusterName, dataModelName, needValidate);
        } else {
            loadAction = new OptimizedLoadAction(dataClusterName, typeName, dataModelName, needAutoGenPK);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Load action selected for load: " + loadAction.getClass().getName() //$NON-NLS-1$
                    + " / needValidate:" + needValidate + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return loadAction;
    }

    protected DataClusterPOJO getDataCluster(String dataClusterName) {
        DataClusterPOJO dataCluster;
        try {
            DataCluster dataClusterCtrlLocal = Util.getDataClusterCtrlLocal();
            DataClusterPOJOPK dataClusterPOJOPK = new DataClusterPOJOPK(dataClusterName);
            dataCluster = dataClusterCtrlLocal.getDataCluster(dataClusterPOJOPK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dataCluster;
    }

    private XSDKey getTypeKey(String dataModelName, String typeName) throws Exception {
        MetadataRepositoryAdmin repositoryAdmin = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin();
        MetadataRepository repository = repositoryAdmin.get(dataModelName);
        ComplexTypeMetadata type = repository.getComplexType(typeName);
        if (type == null) {
            throw new IllegalArgumentException("Type '" + typeName + "' does not exist in data model '" + dataModelName + "'.");
        }
        String[] fields = new String[type.getKeyFields().size()];
        String[] fieldTypes = new String[type.getKeyFields().size()];
        int i = 0;
        for (FieldMetadata keyField : type.getKeyFields()) {
            fields[i] = keyField.getPath();
            String name = keyField.getType().getName();
            if (EUUIDCustomType.AUTO_INCREMENT.getName().equals(name) || EUUIDCustomType.UUID.getName().equals(name)) { // See
                                                                                                                        // TMDM-6687
                fieldTypes[i] = name;
            } else {
                fieldTypes[i] = "xsd:" + name; //$NON-NLS-1$
            }
            i++;
        }
        return new XSDKey(".", fields, fieldTypes); //$NON-NLS-1$
    }

    /**
     * Returns a writer that does not print anything if logger hasn't DEBUG level.
     * 
     * @param resp A servlet response output.
     * @return The same {@link HttpServletResponse} if debug <b>is</b> enabled, or a no-op one if debug is disabled.
     * @throws IOException Thrown by {@link javax.servlet.http.HttpServletResponse#getWriter()}.
     */
    private static PrintWriter configureWriter(HttpServletResponse resp) throws IOException {
        PrintWriter writer = resp.getWriter();
        if (LOG.isDebugEnabled()) {
            return writer;
        } else {
            return new NoOpPrintWriter(writer);
        }
    }

    /**
     * A {@link PrintWriter} implementation that intercepts all write method calls.
     * 
     * @see LoadServlet#configureWriter(javax.servlet.http.HttpServletResponse)
     */
    private static class NoOpPrintWriter extends PrintWriter {

        private NoOpPrintWriter(PrintWriter writer) {
            super(writer);
        }

        @Override
        public void write(int c) {
            // Nothing to do (debug isn't enabled)
        }

        @Override
        public void write(char[] buf, int off, int len) {
            // Nothing to do (debug isn't enabled)
        }

        @Override
        public void write(char[] buf) {
            // Nothing to do (debug isn't enabled)
        }

        @Override
        public void write(String s, int off, int len) {
            // Nothing to do (debug isn't enabled)
        }

        @Override
        public void write(String s) {
            // Nothing to do (debug isn't enabled)
        }
    }
}