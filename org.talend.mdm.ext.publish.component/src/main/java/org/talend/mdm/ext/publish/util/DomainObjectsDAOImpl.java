/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish.util;

import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;

public class DomainObjectsDAOImpl implements DomainObjectsDAO {

    private Logger logger = Logger.getLogger(this.getClass());

    private static final String CLUSTER_NAME = "MDMDomainObjects"; //$NON-NLS-1$

    private XmlServer server;

    public DomainObjectsDAOImpl(XmlServer server) {
        this.server = server;
    }

    public String[] getAllPKs() throws XtentisException {
        return server.getAllDocumentsUniqueID(CLUSTER_NAME);
    }

    public boolean putResource(String domainObjectName, String xmlContent) {

        try {
            server.start(CLUSTER_NAME);
            long rtnStatus = server.putDocumentFromString(xmlContent, domainObjectName, CLUSTER_NAME);
            if (rtnStatus == -1) {
                server.rollback(CLUSTER_NAME);
                return false;
            } else {
                server.commit(CLUSTER_NAME);
                return true;
            }

        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }

    }

    public String getResource(String domainObjectName) throws XtentisException {
        return server.getDocumentAsString(CLUSTER_NAME, domainObjectName);
    }

}
