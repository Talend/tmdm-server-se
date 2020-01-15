/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.load.action;

import com.amalto.core.load.LoadParser;
import com.amalto.core.save.generator.AutoIdGenerator;
import com.amalto.core.load.context.StateContext;
import com.amalto.core.save.generator.AutoIncrementGenerator;
import com.amalto.core.save.generator.UUIDIdGenerator;
import com.amalto.core.load.io.XMLRootInputStream;
import com.amalto.core.save.SaverSession;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.XSDKey;
import com.amalto.core.util.XtentisException;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.EUUIDCustomType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import java.io.InputStream;

/**
 *
 */
public class OptimizedLoadAction implements LoadAction {
    private static final Logger log = Logger.getLogger(OptimizedLoadAction.class);
    private final String dataClusterName;
    private final String typeName;
    private final String dataModelName;
    private final boolean needAutoGenPK;
    private final boolean needAutoGenAutoFields;
    private StateContext context;

    public OptimizedLoadAction(String dataClusterName, String typeName, String dataModelName, boolean needAutoGenPK) {
        this(dataClusterName, typeName, dataModelName, needAutoGenPK, false);
    }

    public OptimizedLoadAction(String dataClusterName, String typeName, String dataModelName, boolean needAutoGenPK, boolean needAutoGenNor){
        this.dataClusterName = dataClusterName;
        this.typeName = typeName;
        this.dataModelName = dataModelName;
        this.needAutoGenPK = needAutoGenPK;
        this.needAutoGenAutoFields = needAutoGenNor;
    }

    public boolean supportValidation() {
        return false;
    }

    @Override
    public void load(InputStream stream, XSDKey autoKeyMetadata, XSDKey autoFieldMetadata, XmlServer server, SaverSession session) {
        if (!".".equals(autoKeyMetadata.getSelector())) { //$NON-NLS-1$
            throw new UnsupportedOperationException("Selector '" + autoKeyMetadata.getSelector() + "' isn't supported.");
        }
        AutoIdGenerator idGenerator = getAutoKeyGenerator(autoKeyMetadata, needAutoGenPK);
        AutoIdGenerator[] autoFieldGenerator = getAutoFieldGenerator(autoFieldMetadata, needAutoGenAutoFields);

        // Creates a load parser callback that loads data in server using a SAX handler
        ServerParserCallback callback = new ServerParserCallback(server, dataClusterName);

        java.io.InputStream inputStream = new XMLRootInputStream(stream, "root"); //$NON-NLS-1$
        LoadParser.Configuration configuration = null;
        if (needAutoGenAutoFields) {
            configuration = new LoadParser.Configuration(typeName, autoKeyMetadata.getFields(), autoFieldMetadata.getFields(),
                    needAutoGenPK, dataClusterName, dataModelName, idGenerator, autoFieldGenerator);
        } else {
            configuration = new LoadParser.Configuration(typeName, autoKeyMetadata.getFields(), needAutoGenPK, dataClusterName,
                    dataModelName, idGenerator);
        }

        context = LoadParser.parse(inputStream, configuration, callback);

        if (log.isDebugEnabled()) {
            log.debug("Number of documents loaded: " + callback.getCount()); //$NON-NLS-1$
        }
    }

    private AutoIdGenerator getAutoKeyGenerator(XSDKey normalMetadata, boolean needAutoGen) {
        if (!needAutoGen) {
            return null;
        }
        String[] idFieldTypes = normalMetadata.getFieldTypes();
        for (String idFieldType : idFieldTypes) {
            if (EUUIDCustomType.AUTO_INCREMENT.getName().equals(idFieldType)) {
                return AutoIncrementGenerator.get();
            } else if (EUUIDCustomType.UUID.getName().equals(idFieldType)) {
                return new UUIDIdGenerator();
            } else {
                throw new UnsupportedOperationException(
                        "No support for key field type '" + idFieldType + "' with autogen pk on."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return null;
    }

    private AutoIdGenerator[] getAutoFieldGenerator(XSDKey normalMetadata, boolean needAutoGen) {
        if (!needAutoGen) {
            return null;
        }
        String[] idFieldTypes = normalMetadata.getFieldTypes();
        AutoIdGenerator[] generator = new AutoIdGenerator[idFieldTypes.length];
        int i = 0;
        for (String idFieldType : idFieldTypes) {
            if (EUUIDCustomType.AUTO_INCREMENT.getName().equals(idFieldType)) {
                generator[i++] = AutoIncrementGenerator.get();
            } else if (EUUIDCustomType.UUID.getName().equals(idFieldType)) {
                generator[i++] = new UUIDIdGenerator();
            } else {
                throw new UnsupportedOperationException(
                        "No support for key field type '" + idFieldType + "' with autogen pk on."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return generator;
    }

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
