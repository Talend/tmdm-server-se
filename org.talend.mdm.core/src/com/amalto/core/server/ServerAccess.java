/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

        public boolean isLicenseValid();

        public String getLicense();
        
        public String getProductName();
        
        public String getProductEdition();
        
        public String getProductKey();
    }

    public ServerAccessInfo getInfo();

    public boolean isExpired() throws Exception;
    
    public boolean isExpired(String language) throws Exception;

    public boolean isDataSteWardShip() throws Exception;

    public boolean isEnterpriseVersion();

    public String getLicenseWarning(String language) throws Exception;

    public int getWorkflowTasksCount();

    public int[] getDSCTasksCount();
    
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
