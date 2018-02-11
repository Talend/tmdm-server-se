/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server;

public class ServerAccessImpl implements ServerAccess {

    private ServerAccessInfo serverAccessInfo;

    public ServerAccessImpl() {
        serverAccessInfo = new ServerAccessInfo() {

            @Override
            public String getProductName() {
                return "Talend MDM"; //$NON-NLS-1$
            }

            @Override
            public String getProductEdition() {
                return "Community Edition"; //$NON-NLS-1$
            }

            @Override
            public String getProductKey() {
                return null;
            }
        };
    }

    @Override
    public ServerAccessInfo getInfo() {
        return serverAccessInfo;
    }

    @Override
    public boolean isEnterpriseVersion() {
        return false;
    }

    @Override
    public int getWorkflowTasksCount() {
        return 0;
    }
}
