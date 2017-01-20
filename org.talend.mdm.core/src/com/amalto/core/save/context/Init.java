/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.SaverSession;
import com.amalto.core.util.ValidateException;

class Init implements DocumentSaver {

    private static final Logger LOGGER = Logger.getLogger(Init.class);

    private final DocumentSaver next;

    Init(DocumentSaver next) {
        this.next = next;
    }

    public void save(SaverSession session, DocumentSaverContext context) {
        SaverSource saverSource = session.getSaverSource();
        String dataClusterName = context.getDataCluster();
        // check cluster exist or not
        if (!XSystemObjects.isExist(XObjectType.DATA_CLUSTER, dataClusterName)) {
            // get the universe and revision ID
            if (!saverSource.existCluster(dataClusterName)) {
                throw new RuntimeException("Data container '" + dataClusterName + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        // Start a transaction on data container
        session.begin(dataClusterName);
        // Continue save
        try {
            next.save(session, context);
        } catch (Exception e) {
            // TMDM-3638: Don't log error if there's a before saving error.
            if (LOGGER.isDebugEnabled()) {
                if (getBeforeSavingMessage().isEmpty()) {
                    LOGGER.debug("Exception occurred during save.", e); //$NON-NLS-1$
                }
            }
            if (e instanceof ValidateException) {
                // In case of validation issue, don't include a potential before saving message in exception.
                throw new com.amalto.core.save.SaveException(e.getMessage(), e);
            }
            if (StringUtils.isEmpty(getBeforeSavingMessage())) {
                throw new com.amalto.core.save.SaveException(e.getMessage(), e);
            }
            throw new com.amalto.core.save.SaveException(getBeforeSavingMessage(), e);
        }
    }

    public String[] getSavedId() {
        return next.getSavedId();
    }

    public String getSavedConceptName() {
        return next.getSavedConceptName();
    }

    public String getBeforeSavingMessage() {
        return next.getBeforeSavingMessage();
    }
}
