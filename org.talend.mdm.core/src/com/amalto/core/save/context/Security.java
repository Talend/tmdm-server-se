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

package com.amalto.core.save.context;

import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.SaverSession;
import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.BaseSecurityCheck;

class Security implements DocumentSaver {

    private final DocumentSaver next;

    public Security(DocumentSaver next) {
        this.next = next;
    }

    public void save(SaverSession session, DocumentSaverContext context) {
        BaseSecurityCheck securityCheckDelegator = BeanDelegatorContainer.getInstance().getSecurityCheckDelegator();
        try {
            securityCheckDelegator.vetoableSave(session, context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save document." + e.getMessage());
        }

        next.save(session, context);
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

    public String getBeforeSavingMessageType() {
        return next.getBeforeSavingMessageType();
    }
}
