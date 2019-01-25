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

package com.amalto.core.load.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.EUUIDCustomType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.load.LoadParser;
import com.amalto.core.load.context.StateContext;
import com.amalto.core.load.io.XMLRootInputStream;
import com.amalto.core.objects.UpdateReportItemPOJO;
import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.SaverSession;
import com.amalto.core.save.context.DocumentSaver;
import com.amalto.core.save.context.SaverContextFactory;
import com.amalto.core.save.generator.AutoIdGenerator;
import com.amalto.core.save.generator.AutoIncrementGenerator;
import com.amalto.core.save.generator.UUIDIdGenerator;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.XSDKey;
import com.amalto.core.util.XtentisException;

/**
 *
 */
public class OptimizedLoadAction implements LoadAction {
    private static final Logger log = Logger.getLogger(OptimizedLoadAction.class);
    private final String dataClusterName;
    private final String typeName;
    private final String dataModelName;
    private final boolean needAutoGenPK;

    private final boolean updateReport;

    private final String source;
    private StateContext context;

    public OptimizedLoadAction(String dataClusterName, String typeName, String dataModelName, boolean needAutoGenPK,
            boolean updateReport, String source) {
        this.dataClusterName = dataClusterName;
        this.typeName = typeName;
        this.dataModelName = dataModelName;
        this.needAutoGenPK = needAutoGenPK;
        this.updateReport = updateReport;
        this.source = source;
    }

    @Override
    public boolean supportValidation() {
        return false;
    }

    @Override
    public void load(InputStream stream, XSDKey keyMetadata, XmlServer server, SaverSession session) {
        if (!".".equals(keyMetadata.getSelector())) { //$NON-NLS-1$
            throw new UnsupportedOperationException("Selector '" + keyMetadata.getSelector() + "' isn't supported."); //$NON-NLS-1$//$NON-NLS-2$
        }
        AutoIdGenerator idGenerator = null;
        if (needAutoGenPK) {
            String[] idFieldTypes = keyMetadata.getFieldTypes();
            for (String idFieldType : idFieldTypes) {
                if (EUUIDCustomType.AUTO_INCREMENT.getName().equals(idFieldType)) {
                    idGenerator = AutoIncrementGenerator.get();
                } else if (EUUIDCustomType.UUID.getName().equals(idFieldType)) {
                    idGenerator = new UUIDIdGenerator();
                } else {
                    throw new UnsupportedOperationException(
                            "No support for key field type '" + idFieldType + "' with autogen pk on."); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }

        // Creates a load parser callback that loads data in server using a SAX handler
        ServerParserCallback callback = new ServerParserCallback(server, dataClusterName, needAutoGenPK, updateReport);

        java.io.InputStream inputStream = new XMLRootInputStream(stream, "root"); //$NON-NLS-1$
        LoadParser.Configuration configuration = new LoadParser.Configuration(typeName,
                keyMetadata.getFields(),
                needAutoGenPK,
                dataClusterName,
                dataModelName,
                idGenerator);
        context = LoadParser.parse(inputStream, configuration, callback);

        if (updateReport) {
            try {
                ILocalUser user = LocalUser.getLocalUser();
                Map<String, UpdateReportItemPOJO> updateReportItemsMap = new HashMap<String, UpdateReportItemPOJO>();
                String userName = user.getUsername();
                session.begin(UpdateReportPOJO.DATA_CLUSTER);
                Map<String, String> map = callback.getUpdateReportMap();
                for (String id : map.keySet()) {
                    UpdateReportPOJO updateReportPOJO = new UpdateReportPOJO(context.getMetadata().getName(), id,
                            map.get(id), source, System.currentTimeMillis(),
                            context.getMetadata().getContainer(), context.getMetadata().getContainer(), userName,
                            updateReportItemsMap);
                    String xmlString = updateReportPOJO.serialize();
                    SaverContextFactory contextFactory = session.getContextFactory();
                    DocumentSaverContext journalContext = contextFactory.create(UpdateReportPOJO.DATA_CLUSTER,
                            UpdateReportPOJO.DATA_MODEL, true, new ByteArrayInputStream(xmlString.getBytes("UTF-8"))); //$NON-NLS-1$
                    DocumentSaver saver = journalContext.createSaver();
                    saver.save(session, journalContext);
                }

                session.end();
            } catch (Exception e) {
                session.abort();
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Number of documents loaded: " + callback.getCount()); //$NON-NLS-1$
        }
    }

    @Override
    public void endLoad(XmlServer server) {
        if (context != null) {
            try {
                // This call should clean up everything (incl. save counter state in case of autogen pk).
                server.start(XSystemObjects.DC_CONF.getName());
                context.close(server);
                server.commit(XSystemObjects.DC_CONF.getName());
            } catch (Exception e) {
                try {
                    server.rollback(XSystemObjects.DC_CONF.getName());
                } catch (XtentisException e1) {
                    log.error("Unable to rollback upon error.", e); //$NON-NLS-1$
                }
                log.error(e.getLocalizedMessage(), e);
            }            
        }
    }
}
