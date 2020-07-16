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
import com.amalto.core.save.generator.AutoIncrementUtil;
import com.amalto.core.save.generator.UUIDIdGenerator;
import com.amalto.core.load.io.XMLRootInputStream;
import com.amalto.core.save.SaverSession;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.XSDKey;
import com.amalto.core.util.XtentisException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.talend.mdm.commmon.util.core.EUUIDCustomType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class OptimizedLoadAction implements LoadAction {
    private static final Logger LOG = LogManager.getLogger(OptimizedLoadAction.class);
    private static final AutoIdGenerator UUID_ID_GENERATOR = new UUIDIdGenerator();
    private final String dataClusterName;
    private final String typeName;
    private final String dataModelName;
    private final boolean needAutoGenPK;
    private StateContext context;

    public OptimizedLoadAction(String dataClusterName, String typeName, String dataModelName, boolean needAutoGenPK) {
        this.dataClusterName = dataClusterName;
        this.typeName = typeName;
        this.dataModelName = dataModelName;
        this.needAutoGenPK = needAutoGenPK;
    }

    public boolean supportValidation() {
        return false;
    }

    @Override
    public void load(InputStream stream, XSDKey autoKeyMetadata, Map<String, String> autoFieldTypeMap, XmlServer server, SaverSession session) {
        if (!".".equals(autoKeyMetadata.getSelector())) { //$NON-NLS-1$
            throw new UnsupportedOperationException("Selector '" + autoKeyMetadata.getSelector() + "' isn't supported.");
        }
        String content = StringUtils.EMPTY;
        if (!autoFieldTypeMap.isEmpty()) {
            try {
                content = IOUtils.toString(stream);
            } catch (Exception e) {
                throw new UnsupportedOperationException("Failed to parse input stream to string", e);
            }
        }
        String[] fieldsToGenerate = AutoIncrementUtil.getAutoNormalFieldsToGenerate(autoFieldTypeMap.keySet(), content);
        Map<String, AutoIdGenerator> normalFieldGenerators = getNormalFieldGenerators(autoFieldTypeMap, fieldsToGenerate);
        AutoIdGenerator idGenerator = needAutoGenPK ? getAutoIdGenerators(autoKeyMetadata)[0] : null;

        // Creates a load parser callback that loads data in server using a SAX handler
        ServerParserCallback callback = new ServerParserCallback(server, dataClusterName);

        InputStream inputStream;
        if (!autoFieldTypeMap.isEmpty()) {
            inputStream = new XMLRootInputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                    "root"); //$NON-NLS-1$
        } else {
            inputStream = new XMLRootInputStream(stream, "root"); //$NON-NLS-1$
        }

        LoadParser.Configuration configuration = new LoadParser.Configuration(typeName, autoKeyMetadata.getFields(),
                needAutoGenPK, dataClusterName, dataModelName, idGenerator, normalFieldGenerators);

        context = LoadParser.parse(inputStream, configuration, callback);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of documents loaded: " + callback.getCount()); //$NON-NLS-1$
        }
    }

    private AutoIdGenerator[] getAutoIdGenerators(XSDKey fieldMetadata) {
        String[] fieldTypes = fieldMetadata.getFieldTypes();
        AutoIdGenerator[] generator = new AutoIdGenerator[fieldTypes.length];
        int i = 0;
        for (String fieldType : fieldTypes) {
            if (EUUIDCustomType.AUTO_INCREMENT.getName().equals(fieldType)) {
                generator[i++] = AutoIncrementGenerator.get();
            } else if (EUUIDCustomType.UUID.getName().equals(fieldType)) {
                generator[i++] = UUID_ID_GENERATOR;
            } else {
                throw new UnsupportedOperationException(
                        "No support for field type '" + fieldType + "' with autogen on."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return generator;
    }

    private Map<String, AutoIdGenerator> getNormalFieldGenerators(Map<String, String> autoFieldTypeMap,
            String[] fieldsToGenerate) {
        Map<String, AutoIdGenerator> normalFieldGenerators = new HashMap<>();
        if (fieldsToGenerate.length == 0) {
            return normalFieldGenerators;
        }
        for (String fieldPath : fieldsToGenerate) {
            if (EUUIDCustomType.AUTO_INCREMENT.getName().equals(autoFieldTypeMap.get(fieldPath))) {
                normalFieldGenerators.put(fieldPath, AutoIncrementGenerator.get());
            } else if (EUUIDCustomType.UUID.getName().equals(autoFieldTypeMap.get(fieldPath))) {
                normalFieldGenerators.put(fieldPath, UUID_ID_GENERATOR);
            } else {
                throw new UnsupportedOperationException("No support for field type '" + autoFieldTypeMap.get(fieldPath)
                        + "' with autogen on."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return normalFieldGenerators;
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
                    LOG.error("Unable to rollback upon error.", e); //$NON-NLS-1$
                }
                LOG.error("Failed to load data", e);
            }
        }
    }
}
