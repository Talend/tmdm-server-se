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

public interface ServerAccess {

    public interface ServerAccessInfo {

        public String getProductName();
        
        public String getProductEdition();
        
        public String getProductKey();
    }

    public ServerAccessInfo getInfo();

    public boolean isEnterpriseVersion();

    public int getWorkflowTasksCount();

    public static final ServerAccess INSTANCE = ServerAccessFactory.createAccess();
    
    public static final class ServerAccessFactory {

        private ServerAccessFactory() {
        }

        private static ServerAccess createAccess() {
            try {
                return new ServerAccessImpl();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
