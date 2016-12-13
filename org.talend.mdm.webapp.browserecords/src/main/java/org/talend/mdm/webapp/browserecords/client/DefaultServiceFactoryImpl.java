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

import com.extjs.gxt.ui.client.Registry;

public class DefaultServiceFactoryImpl extends ServiceFactory {

    @Override
    public BrowseRecordsServiceAsync getMasterService() {
        return Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);
    }

    @Override
    public BrowseStagingRecordsServiceAsync getStagingService() {
        return Registry.get(BrowseRecords.BROWSESTAGINGRECORDS_SERVICE);
    }
}
