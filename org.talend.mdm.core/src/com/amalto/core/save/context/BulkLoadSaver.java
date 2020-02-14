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

package com.amalto.core.save.context;

import java.io.InputStream;
import java.util.Map;

import com.amalto.core.load.action.LoadAction;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.SaverSession;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.XSDKey;

class BulkLoadSaver implements DocumentSaver {

    private final LoadAction loadAction;

    private final InputStream documentStream;

    private final XSDKey autoKeyMetadata;

    private final Map<String, String> autoFieldTypeMap;

    private final XmlServer server;

    BulkLoadSaver(LoadAction loadAction, InputStream documentStream, XSDKey autoKeyMetadata, Map<String, String> autoFieldTypeMap,
            XmlServer server) {
        this.loadAction = loadAction;
        this.documentStream = documentStream;
        this.autoKeyMetadata = autoKeyMetadata;
        this.autoFieldTypeMap = autoFieldTypeMap;
        this.server = server;
    }

    public void save(SaverSession session, DocumentSaverContext context) {
        try {
            loadAction.load(documentStream, autoKeyMetadata, autoFieldTypeMap, server, session);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getSavedId() {
        throw new UnsupportedOperationException();
    }

    public String getSavedConceptName() {
        throw new UnsupportedOperationException();
    }

    public String getBeforeSavingMessage() {
        throw new UnsupportedOperationException();
    }

    public String getBeforeSavingMessageType() {
        throw new UnsupportedOperationException();
    }
}
