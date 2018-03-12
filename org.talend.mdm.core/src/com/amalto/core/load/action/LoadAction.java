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

import com.amalto.core.save.SaverSession;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.XSDKey;

import java.io.InputStream;

/**
 * Load strategy to be used during bulk load of documents in MDM.
 *
 * @see com.amalto.core.servlet.LoadServlet
 */
public interface LoadAction {
    /**
     * @return true if action supports XML validation, false otherwise
     */
    boolean supportValidation();

    /**
     * Loads XML documents from <code>request</code> in <code>server</code>.
     * @param stream      The stream that contains all XML documents to be loaded in MDM.
     * @param keyMetadata Key metadata <b>or <code>null</code> in case of autoGenPK</b>.
     * @param server      The database where the documents must be persisted.
     * @param session     The {@link SaverSession} to be used for saving records.
     */
    void load(InputStream stream, XSDKey keyMetadata, XmlServer server, SaverSession session);

    /**
     * End load and perform all post-load actions (such as save counter state in case of autogen pk).
     * @param server MDM underlying storage that might need operations (commit for instance).
     */
    void endLoad(XmlServer server);
}
