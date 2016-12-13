/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client;

public abstract class ServiceFactory {

    private static ServiceFactory factory;

    public ServiceFactory() {

    }

    public static void initialize(ServiceFactory factoryImpl) {
        ServiceFactory.factory = factoryImpl;
    }

    public static ServiceFactory getInstance() {
        if (factory == null) {
            factory = new DefaultServiceFactoryImpl();
        }
        return factory;
    }

    public BrowseRecordsServiceAsync getService(boolean staging) {
        return staging ? getStagingService() : getMasterService();
    }

    public abstract BrowseRecordsServiceAsync getMasterService();

    public abstract BrowseStagingRecordsServiceAsync getStagingService();
}
