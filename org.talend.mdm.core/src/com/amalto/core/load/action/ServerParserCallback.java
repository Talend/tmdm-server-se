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

import com.amalto.core.load.LoadParserCallback;
import com.amalto.core.server.api.XmlServer;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 *
 */
class ServerParserCallback implements LoadParserCallback {

    private static final Logger log = Logger.getLogger(ServerParserCallback.class);

    private final XmlServer server;

    private final String dataClusterName;

    private int currentCount;

    public ServerParserCallback(XmlServer server, String dataClusterName) {
        this.server = server;
        this.dataClusterName = dataClusterName;
        currentCount = 0;
    }

    public void flushDocument(XMLReader docReader, InputSource input) {
        try {
            server.putDocumentFromSAX(dataClusterName, docReader, input);
            currentCount++;

            if (currentCount % 1000 == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Loaded documents: " + (currentCount / 1000) + "K."); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getCount() {
        return currentCount;
    }
}
