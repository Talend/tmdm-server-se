/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.save.generator;

import com.amalto.core.server.api.XmlServer;

import java.util.UUID;

public class UUIDIdGenerator implements AutoIdGenerator {

    public String generateId(String dataClusterName, String conceptName, String keyElementName) {
        return UUID.randomUUID().toString();
    }

    public void saveState(XmlServer server) {
        // Nothing to do.
    }

    @Override
    public void init() {
        // Nothing to do.
    }

}