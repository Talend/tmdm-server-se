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

/**
 *
 */
public interface DocumentSaver {

    void save(SaverSession session, DocumentSaverContext context);

    String[] getSavedId();

    String getSavedConceptName();

    String getBeforeSavingMessage();

    String getBeforeSavingMessageType();
}
